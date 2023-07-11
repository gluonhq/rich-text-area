/*
 * Copyright (c) 2023, Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GLUON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.richtextarea.samples.popup;

import com.gluonhq.emoji.Emoji;
import com.gluonhq.emoji.EmojiCategory;
import com.gluonhq.emoji.EmojiData;
import com.gluonhq.emoji.EmojiSkinTone;
import com.gluonhq.emoji.util.EmojiImageUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import static com.gluonhq.emoji.EmojiData.emojiFromCategory;
import static com.gluonhq.emoji.EmojiData.search;

public class EmojiPopup extends PopupControl {

    private static final String DEFAULT_STYLE_CLASS = "emoji-popup";

    private final StackPane root;
    private Window ownerWindow;
    private final EventHandler<WindowEvent> closePopupOnOwnerWindowCloseLambda;
    private final WeakEventHandler<WindowEvent> closePopupOnOwnerWindowClose;

    private final ResourceBundle resourceBundle;

    public EmojiPopup() {
        super();

        root = new StackPane();
        resourceBundle = ResourceBundle.getBundle("com.gluonhq.richtextarea.samples.popup.emoji-popup", Locale.getDefault());

        getStyleClass().add(DEFAULT_STYLE_CLASS);
        root.getStylesheets().add(EmojiPopup.class.getResource("emoji-popup.css").toExternalForm());

        setContentNode(createContent());

        closePopupOnOwnerWindowCloseLambda = e -> ownerWindowHiding();
        closePopupOnOwnerWindowClose = new WeakEventHandler<>(closePopupOnOwnerWindowCloseLambda);

        setAutoHide(true);
    }

    public StackPane getRoot() {
        return root;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new EmojiPopupSkin(this);
    }

    @Override
    public final void show(Window ownerWindow, double anchorX, double anchorY) {
        super.show(ownerWindow, anchorX, anchorY);
        this.ownerWindow = ownerWindow;
        ownerWindow.addEventFilter(WindowEvent.WINDOW_HIDING, closePopupOnOwnerWindowClose);
    }

    public void show(Node owner) {
        Bounds bounds = Objects.requireNonNull(owner).localToScreen(owner.getBoundsInLocal());
        show(owner, bounds.getMaxX(), bounds.getMinY());
    }

    @Override
    public void show(Node owner, double x, double y) {
        if (ownerWindow != null && this.isShowing()) {
            super.hide();
        }
        if (owner == null) {
            throw new IllegalArgumentException("owner can not be null");
        }
        ownerWindow = owner.getScene().getWindow();
        super.show(owner, x, y);
        ownerWindow.addEventFilter(WindowEvent.WINDOW_HIDING, closePopupOnOwnerWindowClose);
    }

    private void ownerWindowHiding() {
        this.hide();
        if (this.ownerWindow != null) {
            this.ownerWindow.removeEventFilter(WindowEvent.WINDOW_HIDING, this.closePopupOnOwnerWindowClose);
        }

    }
    // contentNodeProperty
    private final ObjectProperty<Node> contentNodeProperty = new SimpleObjectProperty<>(this, "contentNode") {
        @Override
        protected void invalidated() {
            if (get() == null) {
                throw new IllegalArgumentException("content node can not be null");
            }
            root.getChildren().setAll(get());
        }
    };
    public final ObjectProperty<Node> contentNodeProperty() {
       return contentNodeProperty;
    }
    public final Node getContentNode() {
       return contentNodeProperty.get();
    }
    public final void setContentNode(Node value) {
        contentNodeProperty.set(value);
    }

    /**
     * Defines the action to be performed when an emoji is selected
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

    // skinToneProperty
    private final ObjectProperty<EmojiSkinTone> skinToneProperty = new SimpleObjectProperty<>(this, "skinTone", EmojiSkinTone.NO_SKIN_TONE);
    public final ObjectProperty<EmojiSkinTone> skinToneProperty() {
       return skinToneProperty;
    }
    public final EmojiSkinTone getSkinTone() {
       return skinToneProperty.get();
    }
    public final void setSkinTone(EmojiSkinTone value) {
        skinToneProperty.set(value);
    }

    private Node createContent() {
        final TopPanel topPanel = new TopPanel();
        final EmojiPanel emojiPanel = new EmojiPanel(topPanel.getSelected());
        emojiPanel.categoryProperty().bind(topPanel.selectedProperty());

        final BorderPane borderPane = new BorderPane();
        borderPane.getStyleClass().add("content-node");
        borderPane.setTop(topPanel);
        borderPane.setCenter(emojiPanel);
        return borderPane;
    }

    /******************************************
     * 
     * Private Classes
     * 
     ******************************************/
    private static class TopPanel extends HBox {
        
        TopPanel() {
            final ToggleGroup toggleGroup = new ToggleGroup();
            toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    setSelected((EmojiCategory) newValue.getUserData());
                }
            });
            
            for (EmojiCategory category : EmojiCategory.values()) {
                final ToggleButton button = new ToggleButton() {
                    @Override
                    public void fire() {
                        if (!isSelected()) {
                            super.fire();
                        }
                    }
                };
                button.setToggleGroup(toggleGroup);
                button.getStyleClass().add("emoji-toggle");
                button.setUserData(category);
                final Tooltip tooltip = new Tooltip(category.categoryName());
                tooltip.setShowDuration(Duration.millis(2000));
                button.setTooltip(tooltip);
                final Region graphic = new Region();
                graphic.getStyleClass().addAll(category.getStyleClass(), "graphic");
                button.setGraphic(graphic);
                HBox.setHgrow(button, Priority.ALWAYS);
                getChildren().add(button);
            }
            toggleGroup.selectToggle(toggleGroup.getToggles().get(0));
            getStyleClass().add("top-box");
        }
        
        // selected
        private final ObjectProperty<EmojiCategory> selected = new SimpleObjectProperty<>(this, "selected");
        final ObjectProperty<EmojiCategory> selectedProperty() {
           return selected;
        }
        final EmojiCategory getSelected() {
           return selected.get();
        }
        final void setSelected(EmojiCategory value) {
            selected.set(value);
        }
    }
    
    private class EmojiPanel extends BorderPane {

        private final ObjectProperty<Emoji> focusedEmoji;
        private final GridPane content;
        private final ToggleGroup skinToneToogleGroup = new ToggleGroup();

        // category
        private final ObjectProperty<EmojiCategory> category = new SimpleObjectProperty<>(this, "category");
        final ObjectProperty<EmojiCategory> categoryProperty() {
           return category;
        }
        final EmojiCategory getCategory() {
           return category.get();
        }
        final void setCategory(EmojiCategory value) {
            category.set(value);
        }

        EmojiPanel(EmojiCategory category) {

            this.focusedEmoji = new SimpleObjectProperty<>();
            content = new GridPane();
            content.getStyleClass().add("emoji-container");
            setCategory(category);
            
            final ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            setTop(createHeader());
            setCenter(scrollPane);
            setBottom(createFootPanel());
            getStyleClass().add("emoji-panel");
            
            loadEmojis(category);
            categoryProperty().addListener((observable, oldValue, newValue) -> loadEmojis(newValue));
            skinToneProperty().addListener((observable, oldValue, newValue) -> loadEmojis(getCategory()));
        }

        private VBox createHeader() {
            
            final TextField searchField = new TextField();

            final Label headerLabel = new Label();
            headerLabel.textProperty().bind(Bindings.createStringBinding(() ->
                    resourceBundle.getString("TAB.NAME." + getCategory()), categoryProperty()));
            headerLabel.getStyleClass().add("header-label");

            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.isEmpty()) {
                    focusedEmoji.set(null);
                    loadEmojis(getCategory());
                    headerLabel.textProperty().bind(Bindings.createStringBinding(() -> getCategory().categoryName(), categoryProperty()));
                } else {
                    loadEmojisFromText(newValue);
                    headerLabel.textProperty().unbind();
                    headerLabel.setText(resourceBundle.getString("LABEL.NAME.SEARCH"));
                }
            });

            final VBox header = new VBox(searchField, headerLabel);
            header.getStyleClass().add("header");
            return header;
        }

        private void loadEmojisFromText(String text) {
            content.getChildren().clear();

            int columnIndex = 0;
            int rowIndex = 0;
            final List<Emoji> emojiList = search(text);
            if (emojiList.size() > 0) {
                for (Emoji emoji : emojiList) {
                    final StackPane emojiContainer = createEmoji(emoji);
                    content.add(emojiContainer, columnIndex++, rowIndex);

                    // Keep GridPane to a maximum of 9 rows
                    if (columnIndex > 8) {
                        columnIndex = 0;
                        rowIndex++;
                    }
                }
                focusedEmoji.set(null);
            } else {
                focusedEmoji.set(noEmojiFound());
            }
        }

        private void loadEmojis(EmojiCategory category) {
            content.getChildren().clear();
            
            int columnIndex = 0;
            int rowIndex = 0;
            for (Emoji emoji : emojiFromCategory(category.categoryName())) {
                if (!EmojiData.emojiWithoutTone(emoji).equals(emoji)) {
                    // only select toneless emojis, so skin tone can be applied later on
                    continue;
                }
                final StackPane emojiContainer = createEmoji(emoji);
                content.add(emojiContainer, columnIndex++, rowIndex);

                // Keep GridPane to a maximum of 9 rows
                if (columnIndex > 8) {
                    columnIndex = 0;
                    rowIndex++;
                }
            }
        }

        private StackPane createEmoji(Emoji emoji) {
            final Emoji emojiWithTone = EmojiData.emojiWithTone(emoji, getSkinTone());
            final ImageView imageView = EmojiImageUtils.emojiView(emojiWithTone, 20);
            final StackPane emojiContainer = new StackPane(imageView);
            emojiContainer.getStyleClass().add("emoji");
            emojiContainer.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> focusedEmoji.set(emoji));
            emojiContainer.addEventHandler(MouseEvent.MOUSE_EXITED, e -> focusedEmoji.set(null));
            emojiContainer.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
                if (getOnAction() != null) {
                    getOnAction().handle(new ActionEvent(emojiWithTone, this));
                }
            });
            return emojiContainer;
        }

        private HBox createFootPanel() {
            final HBox box = new HBox();
            box.getStyleClass().add("foot-box");
            box.getChildren().add(createHoverPanel());
            final Pane pane = new Pane();
            pane.setPrefWidth(10);
            pane.setPrefHeight(10);
            HBox.setHgrow(pane, Priority.SOMETIMES);
            box.getChildren().add(pane);

            Button skinButton = new Button();
            skinButton.getStyleClass().setAll("skin-button");
            skinButton.graphicProperty().bind(Bindings.createObjectBinding(() -> getSkinTone().getImageView(), skinToneProperty()));
            skinButton.setOnAction(a -> showSkinsPopup(skinButton));

            box.getChildren().add(skinButton);
            return box;
        }

        private Node createHoverPanel() {
            final Label emojiName = new Label();
            emojiName.getStyleClass().add("name");
            final Label emojiShortName = new Label();
            emojiShortName.getStyleClass().add("code-name");
            final VBox vBox = new VBox(emojiName, emojiShortName);
            vBox.getStyleClass().add("labels");
            
            final StackPane emojiPane = new StackPane();
            emojiPane.getStyleClass().add("image-pane");
            final HBox hoverPanel = new HBox(emojiPane, vBox);
            hoverPanel.getStyleClass().add("hover-panel");

            focusedEmoji.addListener((o, ov, nv) -> {
                if (nv == null) {
                    emojiPane.getChildren().clear();
                    emojiName.setText("");
                    emojiShortName.setText("");
                } else {
                    final Emoji emojiWithTone = (nv.getSkinVariationMap() != null && getSkinTone() != EmojiSkinTone.NO_SKIN_TONE) ?
                            nv.getSkinVariationMap().getOrDefault(getSkinTone().getUnicode(), nv) :
                            nv;
                    emojiPane.getChildren().add(EmojiImageUtils.emojiView(emojiWithTone, 32));
                    emojiName.setText(nv.getShortName());
                    emojiShortName.setText(nv.getCodeName());
                }
            });
            return hoverPanel;
        }

        private void showSkinsPopup(Button skinButton) {
            final Popup popup = new Popup();
            popup.setAutoFix(true);
            popup.setAutoHide(true);
            popup.setHideOnEscape(true);

            HBox content = new HBox();
            content.getStylesheets().addAll(skinButton.getScene().getStylesheets());
            content.getStylesheets().add(EmojiPopup.class.getResource("emoji-popup.css").toExternalForm());
            content.getStyleClass().add("popup-box");
            content.setPrefHeight(skinButton.getHeight());
            Stream.of(EmojiSkinTone.values())
                    .forEach(tone -> {
                        Button item = new Button(null, tone.getImageView());
                        item.getStyleClass().setAll("item");
                        if (getSkinTone().getUnicode().equals(tone.getUnicode())) {
                            item.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), true);
                        }
                        item.setOnAction(e -> {
                            setSkinTone(tone);
                            popup.hide();
                        });
                        content.getChildren().add(item);
                    });
            popup.getContent().add(content);
            Bounds point2D = skinButton.localToScreen(skinButton.getLayoutBounds());
            content.layout();
            content.applyCss();
            popup.show(skinButton, point2D.getMaxX() - content.prefWidth(-1), point2D.getMinY());
        }
        
        private Emoji noEmojiFound() {
            final Emoji noEmojiFound = new Emoji() {
                @Override
                public String getCodeName() {
                    return resourceBundle.getString("LABEL.NAME.NO.EMOJI");
                }
            };
            EmojiData.emojiFromShortName("cry").ifPresent(cry -> {
                noEmojiFound.setSheetY(cry.getSheetY());
                noEmojiFound.setSheetX(cry.getSheetX());
            });
            noEmojiFound.setShortName(resourceBundle.getString("LABEL.HEAD.NO.EMOJI"));
            return noEmojiFound;
        }
    }
    
}