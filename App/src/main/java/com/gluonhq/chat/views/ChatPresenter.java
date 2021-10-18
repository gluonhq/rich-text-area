package com.gluonhq.chat.views;

import com.gluonhq.attach.display.DisplayService;
import com.gluonhq.attach.keyboard.KeyboardService;
import com.gluonhq.attach.util.Platform;
import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.control.Avatar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.chat.GluonChat;
import com.gluonhq.chat.chatlistview.ChatListView;
import com.gluonhq.chat.model.Channel;
import com.gluonhq.chat.model.ChatMessage;
import com.gluonhq.chat.service.Service;
import com.gluonhq.chat.util.ImageCache;
import com.gluonhq.chat.views.helper.PlusPopupView;
import com.gluonhq.emoji.control.EmojiTextArea;
import javafx.animation.PauseTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.ResourceBundle;

//import com.gluonhq.emoji.control.EmojiTextArea;

// TODO Show user images when available

public class ChatPresenter extends GluonPresenter<GluonChat> {

    @FXML private View chatView;

    @FXML private HBox channelBox;
    @FXML private VBox channelIcon;
    @FXML private Label channelName;
    @FXML private Button closeChannel;

    @FXML private StackPane stackPane;
    @FXML private HBox unreadBox;
    @FXML private Label unread;
    @FXML private BorderPane bottomPane;
    @FXML private Button addButton;
    @FXML private Button sendButton;

    @Inject private Service service;
    @FXML private ResourceBundle resources;

    private ChatListView<ChatMessage> chatList;
    private ObservableList<ChatMessage> messages;
    private PauseTransition pause;

    public void initialize() {
        closeChannel.setOnAction(e -> updateMessages(null));
        channelBox.visibleProperty().bind(channelName.textProperty().isNotEmpty());
        chatList = new ChatListView<>();
        Label placeholder = new Label();
        placeholder.textProperty().bind(Bindings.createStringBinding(() -> {
            if (channelName.getText() == null || channelName.getText().isEmpty()) {
                return resources.getString("select.channel");
            }
            return resources.getString("empty.channel");
        }, channelName.textProperty()));
        chatList.setPlaceholder(placeholder);
        chatList.getStyleClass().add("chat-list");
        chatList.setCellFactory(listView -> new MessageCell());

        chatList.unreadIndexProperty().addListener((Observable o) -> updateUnreadLabel());
        chatList.unreadMessagesProperty().addListener((Observable o) -> updateUnreadLabel());
        stackPane.getChildren().add(0, chatList);

        unreadBox.visibleProperty().bind(chatList.unreadMessagesProperty().greaterThan(-1));
        unreadBox.setOnMouseClicked(e -> chatList.scrollTo(chatList.getUnreadIndex()));

        EmojiTextArea messageEditor = new EmojiTextArea();
        messageEditor.getStyleClass().add("chat-text-editor");
        HBox.setHgrow(messageEditor, Priority.ALWAYS);
        bottomPane.setCenter(messageEditor);
        KeyboardService.create().ifPresent(k -> k.keepVisibilityForNode(messageEditor, chatView));

        sendButton.prefHeightProperty().bind(addButton.heightProperty());

        addButton.managedProperty().bind(messageEditor.textProperty().isEmpty());
        addButton.visibleProperty().bind(messageEditor.textProperty().isEmpty());
        sendButton.managedProperty().bind(messageEditor.textProperty().isNotEmpty());
        sendButton.visibleProperty().bind(messageEditor.textProperty().isNotEmpty());
        sendButton.setOnAction(e -> sendMessage(messageEditor));
        messageEditor.setOnAction(e -> sendMessage(messageEditor));

        setupAddButton();

        if (Platform.isIOS() || Platform.isAndroid()) {

            if (Platform.isIOS()) {
                // style classes
                messageEditor.getStyleClass().add("ios");
                chatList.getStyleClass().add("ios");
                if (DisplayService.create().map(DisplayService::hasNotch).orElse(false)) {
                    bottomPane.getStyleClass().add("notch");
                }
            }

            // allow dismissing the soft keyboard when tapping outside textArea
            chatList.mouseTransparentProperty().bind(messageEditor.focusedProperty());
            chatView.setOnMouseClicked(e -> {
                if (messageEditor.isFocused()) {
                    // Hide keyboard
                    addButton.requestFocus();
                }
            });

            // process button "click" when the keyboard was still showing
            sendButton.setOnMousePressed(e -> sendButton.fire());

            // scroll to last element
            chatList.sceneProperty().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    if (chatList.getScene() != null) {
                        PauseTransition pause = new PauseTransition(Duration.millis(100));
                        pause.setOnFinished(f -> chatList.scrollTo(chatList.getItems().size() - 1));
                        pause.playFromStart();
                        chatList.sceneProperty().removeListener(this);
                    }
                }
            });
        }
    }

    void updateMessages(Channel channel) {
        if (channel != null) {
            createSortList(channel.getMessages());
            channelName.setText(channel.displayName());
            ImageCache.getImage(channel.getMembers().isEmpty() ? null : channel.getMembers().get(0).getAvatarPath())
                    .ifPresentOrElse(im -> {
                        Avatar avatar = new Avatar(0, im);
                        avatar.setMouseTransparent(true);
                        channelIcon.getChildren().setAll(avatar);
                    }, () -> {
                        String initials = Service.getInitials(channel.displayName());
                        Label icon = new Label(initials.substring(0, Math.min(initials.length(), 2)));
                        icon.getStyleClass().add("chat-channel-icon");
                        channelIcon.getChildren().setAll(icon);
                    });
        } else {
            chatList.setItems(null);
            channelIcon.getChildren().clear();
            channelName.setText(null);
        }
        bottomPane.setDisable(false);

        AppViewManager.CHANNEL_VIEW.getPresenter()
                .ifPresent(presenter -> ((ChannelPresenter) presenter).updateChannels(channel == null));
    }

    private void sendMessage(EmojiTextArea messageEditor) {
        String text = messageEditor.getText().trim();
        if (!text.isEmpty()) {
            var message = new ChatMessage(text, service.loggedUser(), LocalDateTime.now(), true);
            messages.add(message);
            messageEditor.clear();
            messageEditor.requestFocus();
        }
    }

    private void createSortList(ObservableList<ChatMessage> messages) {
        this.messages = messages;
        SortedList<ChatMessage> sortedList = new SortedList<>(messages);
        sortedList.setComparator(Comparator.comparing(ChatMessage::getTime));
        chatList.setItems(sortedList);
    }

    private void updateUnreadLabel() {
        int unreadMessages = chatList.getUnreadMessages() > -1 ? chatList.getUnreadMessages() : 0;
        if (unreadMessages > 0) {
            unread.setText(unreadMessages + " " + (unreadMessages > 1 ? resources.getString("unread.messages") : resources.getString("unread.message")));
        }
        if (pause == null) {
            pause = new PauseTransition(Duration.millis(1000));
            pause.setOnFinished(f -> chatList.refresh());
        }
        pause.playFromStart();
    }

    private void setupAddButton() {
        PlusPopupView popup = new PlusPopupView(addButton, resources);
        addButton.setOnAction(event -> popup.show());
    }
}