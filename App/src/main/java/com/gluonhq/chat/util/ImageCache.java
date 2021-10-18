package com.gluonhq.chat.util;

import com.gluonhq.attach.cache.Cache;
import com.gluonhq.attach.cache.CacheService;
import javafx.scene.image.Image;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class ImageCache {

    private static final Cache<String, Image> CACHE;
    private static final int AVATAR_SIZE = 64;

    static {
        CACHE = CacheService.create()
                .map(cache -> cache.<String, Image>getCache("images"))
                .orElseThrow(() -> new RuntimeException("No CacheService available"));
    }

    public static Optional<Image> getImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty() || "null".equals(imagePath)) {
            return Optional.empty();
        }
        Image cachedImage = CACHE.get(imagePath);
        if (cachedImage == null) {
            cachedImage = generateImageForPath(imagePath);
            CACHE.put(imagePath, cachedImage);
        }
        return Optional.ofNullable(cachedImage);
    }

    private static Image generateImageForPath(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        try (InputStream is = new FileInputStream(path)) {
            return new Image(is, AVATAR_SIZE, AVATAR_SIZE, true, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
