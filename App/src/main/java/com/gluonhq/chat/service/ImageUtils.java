package com.gluonhq.chat.service;

import com.gluonhq.attach.cache.Cache;
import com.gluonhq.attach.cache.CacheService;
import com.gluonhq.attach.position.Position;
import com.gluonhq.chat.model.ChatImage;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.*;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ImageUtils {

    public static final String IMAGE_PREFIX = "$$";
    public static final String LATLON = "LATLON-";
    public static final String LATLON_SEP = "#";
    public static final Position DEFAULT_POSITION = new Position(50, 4);

    private static final int MAX_IMAGE_SIZE = 192;
    private static final Cache<String, Image> CACHE;
    private static Image image;

    static {
        CACHE = CacheService.create()
                .map(cache -> cache.<String, Image>getCache("images"))
                .orElseThrow(() -> new RuntimeException("No CacheService available"));
    }

    public static ChatImage encodeImage(String id, Image image) {
        if (image == null) {
            return null;
        }

        Image scaledImage = getSnapshot(getImageView(image), false);

        PixelReader pixelReader = scaledImage.getPixelReader();
        int width = (int) scaledImage.getWidth();
        int height = (int) scaledImage.getHeight();
        byte[] buffer = new byte[width * height * 4];
        pixelReader.getPixels(0, 0, width, height, PixelFormat.getByteBgraInstance(), buffer, 0, width * 4);
        String content = Base64.getEncoder().encodeToString(buffer);
        ChatImage chatImage = new ChatImage(id, content, width, height);
        CACHE.put(chatImage.getId(), scaledImage);
        return chatImage;
    }

    public static Optional<Image> getImage(String id) {
        return Optional.ofNullable(CACHE.get(id));
    }

    public static ImageView getImageView(ChatImage chatImage) {
        if (chatImage == null || chatImage.getContent() == null ||
                chatImage.getContent().isEmpty()) {
            return null;
        }

        Image image = getImage(chatImage.getId())
                .orElseGet(()-> {
                    Image cachedImage = decodeImage(chatImage);
                    CACHE.put(chatImage.getId(), cachedImage);
                    return cachedImage;
                });
        if (image == null) {
            return null;
        }
        return getImageView(image);
    }

    private static Image decodeImage(ChatImage chatImage) {
        if (chatImage == null || chatImage.getContent() == null ||
            chatImage.getContent().isEmpty()) {
            return null;
        }

        byte[] imageBytes = Base64.getDecoder().decode(chatImage.getContent().getBytes(StandardCharsets.UTF_8));

        WritablePixelFormat<ByteBuffer> wf = PixelFormat.getByteBgraInstance();
        WritableImage writableImage = new WritableImage(chatImage.getWidth(), chatImage.getHeight());
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        pixelWriter.setPixels(0, 0, chatImage.getWidth(), chatImage.getHeight(), wf, imageBytes, 0, chatImage.getWidth() * 4);
        return writableImage;
    }

    public static ImageView getImageView(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(Math.min(MAX_IMAGE_SIZE, (image == null ? MAX_IMAGE_SIZE : image.getWidth())));
        imageView.setFitHeight(Math.min(MAX_IMAGE_SIZE, (image == null ? MAX_IMAGE_SIZE : image.getHeight())));
        return imageView;
    }

    public static Image getSnapshot(Node node, boolean transparent) {
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setTransform(
                new Scale(Screen.getPrimary().getOutputScaleX(), Screen.getPrimary().getOutputScaleY()));
        if (transparent) {
            parameters.setFill(javafx.scene.paint.Color.TRANSPARENT);
        }
        if (Platform.isFxApplicationThread()) {
            image = node.snapshot(parameters, null);
        } else {
            final CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                image = node.snapshot(parameters, null);
                latch.countDown();
            });
            try {
                latch.await(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return image;
    }
}
