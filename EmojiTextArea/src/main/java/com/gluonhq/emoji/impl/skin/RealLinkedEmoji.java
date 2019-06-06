package com.gluonhq.emoji.impl.skin;

import com.gluonhq.emoji.Emoji;
import javafx.scene.Node;

import java.util.Optional;

import static com.gluonhq.emoji.popup.util.EmojiImageUtils.emojiView;


/**
 * A custom object which contains a file path to an image file.
 * When rendered in the rich text editor, the image is loaded from the
 * specified file.
 */
public class RealLinkedEmoji implements LinkedEmoji {

    private final Emoji emoji;

    /**
     * Creates a new linked emoji object.
     *
     * @param emoji The emoji.
     */
    public RealLinkedEmoji(Emoji emoji) {
        this.emoji = emoji;
    }

    @Override
    public boolean isReal() {
        return true;
    }

    @Override
    public Optional<Emoji> getEmoji() {
        return Optional.ofNullable(emoji);
    }

    @Override
    public String toString() {
        return "RealLinkedEmoji:"+ emoji.toString();
    }

    @Override
    public Node createNode() {
        return emojiView(emoji, 20, 0.75);
    }
}
