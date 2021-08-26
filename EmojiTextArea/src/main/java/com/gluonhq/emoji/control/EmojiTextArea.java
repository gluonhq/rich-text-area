package com.gluonhq.emoji.control;

import com.gluonhq.emoji.impl.skin.EmojiTextAreaSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class EmojiTextArea extends Control {

    public EmojiTextArea() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    // text
    private final StringProperty text = new SimpleStringProperty(this, "text");
    public final StringProperty textProperty() {
        return text;
    }
    public final String getText() {
        return text.get();
    }
    public final void setText(String value) {
        text.set(value);
    }

    /**
     * Defines the action to be performed when an enter is pressed
     */
    private final ObjectProperty<EventHandler<ActionEvent>> onAction = new SimpleObjectProperty<>(this, "onAction");
    public final ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
        return onAction;
    }
    public final EventHandler<ActionEvent> getOnAction() {
        return onAction.get();
    }
    public final void setOnAction(EventHandler<ActionEvent> value) {
        onAction.set(value);
    }

    public void clear() {
        if (!text.isBound()) {
            setText("");
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new EmojiTextAreaSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return getClass().getResource("emoji-text-area.css").toExternalForm();
    }

    // treat private
//    public void doSetFocused(boolean focused) {
//        this.setFocused(focused);
//    }

    /***************************************************************************
     *                                                                         *
     *                         Stylesheet Handling                             *
     *                                                                         *
     **************************************************************************/
    
    private static final String DEFAULT_STYLE_CLASS = "emoji-text-area";
}


