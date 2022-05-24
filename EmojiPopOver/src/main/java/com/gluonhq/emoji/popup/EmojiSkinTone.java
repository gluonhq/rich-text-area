package com.gluonhq.emoji.popup;

import com.gluonhq.emoji.util.TextUtils;
import javafx.scene.Node;

import java.util.stream.Stream;

public enum EmojiSkinTone {

    NO_SKIN_TONE("\u270b", ""),
    LIGHT_SKIN_TONE("\u270b\ud83c\udffb", "1F3FB"),
    MEDIUM_LIGHT_SKIN_TONE("\u270b\ud83c\udffc", "1F3FC"),
    MEDIUM_SKIN_TONE("\u270b\ud83c\udffd", "1F3FD"),
    MEDIUM_DARK_SKIN_TONE("\u270b\ud83c\udffe", "1F3FE"),
    DARK_SKIN_TONE("\u270b\ud83c\udfff", "1F3FF");

    private final String text;
    private final String unicode;

    EmojiSkinTone(String text, String unicode) {
        this.text = text;
        this.unicode = unicode;
    }

    public String getText() {
        return text;
    }

    public String getUnicode() {
        return unicode;
    }

    public Node getImageView() {
        return TextUtils.convertToTextAndImageNodes(text).get(0);
    }

    public static EmojiSkinTone fromUnicode(String unicode) {
        return Stream.of(values())
                .filter(emojiSkinTone -> emojiSkinTone.unicode.equals(unicode))
                .findFirst()
                .orElse(NO_SKIN_TONE);
    }
}
