package com.gluonhq.emoji.impl.skin;

import com.gluonhq.emoji.Emoji;
import com.gluonhq.emoji.event.EmojiEvent;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class EmojiSuggestion extends Control {

    public EmojiSuggestion() {

        setManaged(false);
        setVisible(false);
        setMouseTransparent(true);
        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    private final ObservableList<Emoji> emojis = FXCollections.observableArrayList();
    public ObservableList<Emoji> getEmojis() {
        return emojis;
    }

    public final ObjectProperty<EventHandler<EmojiEvent>> onActionProperty() { return onAction; }
    public final void setOnAction(EventHandler<EmojiEvent> value) { onActionProperty().set(value); }
    public final EventHandler<EmojiEvent> getOnAction() { return onActionProperty().get(); }
    private ObjectProperty<EventHandler<EmojiEvent>> onAction = new SimpleObjectProperty<>();
    
    public void show() {
        setMouseTransparent(false);
        setVisible(true);
    }
    
    public void hide() {
        setMouseTransparent(true);
        setVisible(false);
    }
    
    @Override
    protected Skin<?> createDefaultSkin() {
        return new EmojiSuggestionSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return EmojiSuggestion.class.getResource("emoji-suggestion.css").toExternalForm();
    }

    private static final String DEFAULT_STYLE_CLASS = "search-popup";
}
