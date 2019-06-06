package com.gluonhq.emoji.event;

import com.gluonhq.emoji.Emoji;
import javafx.event.ActionEvent;

public class EmojiEvent extends ActionEvent {

    private Emoji emoji;

    public EmojiEvent(Emoji emoji) {
        super();
        this.emoji = emoji;
    }

    /**
     * Retrieves the emoji the event represents.
     *
     * @return the value of the emoji.
     */
    public Emoji getEmoji() {
        return emoji;
    }
}
