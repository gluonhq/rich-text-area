package com.gluonhq.emoji.impl.skin;

import com.gluonhq.emoji.Emoji;
import com.gluonhq.emoji.EmojiData;
import javafx.scene.Node;

import java.util.Optional;

public class EmptyLinkedEmoji implements LinkedEmoji {

    @Override
    public boolean isReal() {
        return false;
    }

    @Override
    public Optional<Emoji> getEmoji() {
        // Dummy, will never be called
        return EmojiData.emojiFromShortName("smile");
    }

    @Override
    public Node createNode() {
        throw new AssertionError("Unreachable code");
    }
}
