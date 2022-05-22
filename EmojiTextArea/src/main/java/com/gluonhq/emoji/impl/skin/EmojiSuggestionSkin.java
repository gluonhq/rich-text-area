package com.gluonhq.emoji.impl.skin;

import com.gluonhq.emoji.Emoji;
import com.gluonhq.emoji.event.EmojiEvent;
import com.gluonhq.emoji.util.EmojiImageUtils;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import com.gluonhq.emoji.control.EmojiTextArea;

public class EmojiSuggestionSkin extends SkinBase<EmojiSuggestion> {

    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    private BorderPane root;
    private FlowPane content;
    private Label matching;

    EmojiSuggestionSkin(EmojiSuggestion emojiSuggestion) {
        super(emojiSuggestion);

        matching = new Label();
        content = new FlowPane();
        root = new BorderPane(content);
        root.setTop(createTopPanel());
        getChildren().add(root);

        matching.getStyleClass().add("matching");
        content.getStyleClass().add("content");
        
        createEmojis();
        getSkinnable().getEmojis().addListener((InvalidationListener) o -> {
            if (getSkinnable().getEmojis().size() > 0) {
                createEmojis();
                resetSelection();
                selectFirst();
            }
        });
        final StringProperty textProperty = ((EmojiTextArea) getSkinnable().getParent()).textProperty();
        matching.textProperty().bind(Bindings.createStringBinding(() -> {
            return "Emoji matching " + "\"" + textProperty.get() + "\"";
        }, textProperty));
        
        getSkinnable().addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            for (Node node : content.getChildren()) {
                EmojiLabel emojiLabel = (EmojiLabel) node;
                if (emojiLabel.isSelected()) {
                    emojiLabel.fireEvent(e);
                    break;
                }
            }
        });
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return root.prefHeight(width);
    }

    private HBox createTopPanel() {
        final HBox topPanel = new HBox(10);
        topPanel.getStyleClass().add("top-panel");

        final Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);

        final Label navigation = new Label("TAB or \u2190 \u2192 to navigate");
        final Label select = new Label("\u21B5 to select");
        final Label dismiss = new Label("ESC to dismiss");

        topPanel.getChildren().addAll(matching, gap, navigation, select, dismiss);
        return topPanel;
    }

    private void createEmojis() {
        content.getChildren().clear();

        final ObservableList<Emoji> emojis = getSkinnable().getEmojis();
        emojis.forEach(emoji -> {
            final Label emojiLabel = new EmojiLabel(emoji);
            content.getChildren().addAll(emojiLabel);
        });
    }

    private void resetSelection() {
        content.getChildren().forEach(child -> ((EmojiLabel)child).setSelected(false));
    }
    
    private void selectNext(Node node) {
        resetSelection();
        Traversal.findNext(node).ifPresentOrElse(arg -> {
            ((EmojiLabel) arg).setSelected(true);
        }, () -> selectFirst());
    }

    private void selectPrevious(Node node) {
        resetSelection();
        Traversal.findPrevious(node).ifPresentOrElse(arg -> {
            ((EmojiLabel) arg).setSelected(true);
        }, () -> selectLast());
    }

    private void selectTop(Node node) {
        resetSelection();
        Traversal.findTop(node, content.getVgap(), content.getPadding().getTop()).ifPresentOrElse(arg -> {
            ((EmojiLabel) arg).setSelected(true);
        }, () -> selectFirst());
    }

    private void selectBottom(Node node) {
        resetSelection();
        Traversal.findBottom(node, content.getVgap()).ifPresentOrElse(arg -> {
            ((EmojiLabel) arg).setSelected(true);
        }, () -> selectLast());
    }

    private void selectFirst() {
        if (!content.getChildren().isEmpty())
            ((EmojiLabel) content.getChildren().get(0)).setSelected(true);
    }

    private void selectLast() {
        if (!content.getChildren().isEmpty())
            ((EmojiLabel) content.getChildren().get(content.getChildren().size() - 1)).setSelected(true);
    }

    private class EmojiLabel extends Label {

        EmojiLabel(Emoji emoji) {
            final ImageView imageView = EmojiImageUtils.emojiView(emoji, 16);
            setText(emoji.getCodeName());
            setGraphic(imageView);
            getStyleClass().add("emoji-label");

            KeyCombination SHIFT_TAB = new KeyCodeCombination(KeyCode.TAB, KeyCombination.SHIFT_DOWN);
            addEventHandler(KeyEvent.KEY_PRESSED, e -> {
                if (isSelected()) {
                    if (SHIFT_TAB.match(e)) {
                        selectPrevious(this);
                    } else {
                        switch (e.getCode()) {
                            case ENTER:
                                handleAction(emoji, e);
                                break;
                            case RIGHT:
                            case TAB:
                                selectNext(this);
                                break;
                            case LEFT:
                                selectPrevious(this);
                                break;
                            case UP:
                                selectTop(this);
                                break;
                            case DOWN:
                                selectBottom(this);
                                break;
                        }
                    }
                }
                e.consume();
            });
            addEventHandler(MouseEvent.MOUSE_CLICKED, e -> handleAction(emoji, e));
            addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
                resetSelection();
                setSelected(true);
            });
        }

        // selectedProperty
        private final BooleanProperty selectedProperty = new SimpleBooleanProperty(this, "selected", false) {
            @Override
            protected void invalidated() {
                super.invalidated();
                pseudoClassStateChanged(SELECTED, get());
            }
        };
        final BooleanProperty selectedProperty() {
           return selectedProperty;
        }
        final boolean isSelected() {
           return selectedProperty.get();
        }
        final void setSelected(boolean value) {
            selectedProperty.set(value);
        }

        private void handleAction(Emoji emoji, Event e) {
            final EventHandler<EmojiEvent> onAction = getSkinnable().getOnAction();
            if (onAction != null) {
                onAction.handle(new EmojiEvent(emoji));
            }
            e.consume();
        }
    }
}
