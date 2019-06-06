package com.gluonhq.emoji.popup.util;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;
import com.gluonhq.emoji.Emoji;

public class EmojiImageUtils {

    private static Image emojiSprite20;
    private static Image emojiSprite32;
    private static Image emojiSprite64;
    
    // private static final Map<String, Image> emojiMap20;
    // private static final Map<String, Image> emojiMap32;
    /*static {
        emojiSprite20 = new Image(EmojiImageUtils.class.getResourceAsStream("/org/signal/sheet_apple_20.png"));
        emojiSprite32 = new Image(EmojiImageUtils.class.getResourceAsStream("/org/signal/sheet_apple_32.png"));
        emojiMap20 = new HashMap<>();
        emojiMap32 = new HashMap<>();
    }*/

    public static Image getImage20() {
        if (emojiSprite20 == null) {
            emojiSprite20 = new Image(EmojiImageUtils.class.getResourceAsStream("sheet_apple_20.png"));
        }
        return emojiSprite20;
    }

    public static Image getImage32() {
        if (emojiSprite32 == null) {
            emojiSprite32 = new Image(EmojiImageUtils.class.getResourceAsStream("sheet_apple_32.png"));
        }
        return emojiSprite32;
    }
    
    public static Image getImage64() {
        if (emojiSprite64 == null) {
            emojiSprite64 = new Image(EmojiImageUtils.class.getResourceAsStream("sheet_apple_64.png"));
        }
        return emojiSprite64;
    }

    public static Rectangle2D getViewportFor64(Emoji emoji) {
        return new Rectangle2D(
                emoji.getSheet_x() * 66,
                emoji.getSheet_y() * 66,
                66,
                66
        );
    }

    public static Rectangle2D getViewportFor32(Emoji emoji) {
        return new Rectangle2D(
                emoji.getSheet_x() * 34,
                emoji.getSheet_y() * 34,
                34,
                34
        );
    }

    public static Rectangle2D getViewportFor20(Emoji emoji) {
        return new Rectangle2D(
                emoji.getSheet_x() * 22,
                emoji.getSheet_y() * 22,
                22,
                22
        );
    }

    /**
     * Returns true is one of the Screen is a Retina display
     * or a scaling factor of more than equal to 1.5
     * @return true if one of the screen is a Retina display
     */
    public static boolean isRetina() {
        return Screen.getScreens().stream().mapToDouble(Screen::getOutputScaleX).max().orElse(1.0) >= 1.5;
    }

    /**
     * Provides ImageView containing emoji with a max size of 64 pixels.
     * When {@link #isRetina()} is true, it creates {@link ImageView} with a
     * larger pixel image and fit them to the specified size.
     * 
     * @param emoji Emoji for which we need to create the ImageView
     * @param size The height and width of the ImageView
     * @return ImageView containing the emoji image
     */
    public static ImageView emojiView(Emoji emoji, double size) {
        return emojiView(emoji, size, 1.0);
    }

    /**
     * Provides ImageView containing emoji with a max size of 64 pixels.
     * When {@link #isRetina()} is true, it creates {@link ImageView} with a
     * larger pixel image and fit them to the specified size.
     *
     * @param emoji Emoji for which we need to create the ImageView
     * @param size The height and width of the ImageView
     * @param offset Offset for the ImageView
     * @return ImageView containing the emoji image
     */
    public static ImageView emojiView(Emoji emoji, double size, double offset) {
        final ImageView emojiView = new ImageView() {
            @Override
            public double getBaselineOffset() {
                return super.getBaselineOffset() * offset;
            }
        };
        emojiView.setSmooth(true);
        emojiView.setPreserveRatio(true);
        
        if (isRetina() || size > 32) {
            emojiView.setImage(getImage64());
            emojiView.setViewport(getViewportFor64(emoji));
        } else {
            if (size <= 20) {
                emojiView.setImage(getImage20());
                emojiView.setViewport(getViewportFor20(emoji));
            } else if (size <= 32) {
                emojiView.setImage(getImage32());
                emojiView.setViewport(getViewportFor32(emoji));
            }
        }
        emojiView.setFitHeight(size);
        return emojiView;
    }

    /*public static Image getImage20(Emoji emoji) {
        return emojiMap20.computeIfAbsent(emoji.getShort_name().orElse(""), s -> {
            final PixelReader pixelReader = emojiSprite20.getPixelReader();
            // each image has a padding of 1px
            final int x = emoji.getSheet_x() * 22;
            final int y = emoji.getSheet_y() * 22;
            return new WritableImage(pixelReader, x, y, 20, 20);
        });
    }
    
    public static Image getImage32(Emoji emoji) {
        return emojiMap32.computeIfAbsent(emoji.getShort_name().orElse(""), s -> {
            final PixelReader pixelReader = emojiSprite32.getPixelReader();
            // each image has a padding of 1px
            final int x = emoji.getSheet_x() * 34;
            final int y = emoji.getSheet_y() * 34;
            return new WritableImage(pixelReader, x, y, 32, 32);
        });
    }*/
}