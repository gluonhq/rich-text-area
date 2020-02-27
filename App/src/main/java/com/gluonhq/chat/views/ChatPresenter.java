package com.gluonhq.chat.views;

import com.gluonhq.attach.util.Platform;
import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.chat.GluonChat;
import com.gluonhq.chat.chatlistview.ChatListView;
import com.gluonhq.chat.model.ChatMessage;
import com.gluonhq.chat.service.Service;
import com.gluonhq.chat.views.helper.PlusPopupView;
import com.gluonhq.connect.GluonObservableList;
import com.gluonhq.connect.GluonObservableObject;
//import com.gluonhq.emoji.control.EmojiTextArea;
import javafx.animation.PauseTransition;
import javafx.beans.Observable;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.ResourceBundle;

// TODO Show user images when available

public class ChatPresenter extends GluonPresenter<GluonChat> {

    @FXML private View chatView;
    @FXML private StackPane stackPane;
    @FXML private HBox unreadBox;
    @FXML private Label unread;
    @FXML private Button addButton;
    @FXML private BorderPane bottomPane;
    @FXML private Button sendButton;

    @Inject private Service service;
    @FXML private ResourceBundle resources;

    private ChatListView<ChatMessage> chatList;
    private GluonObservableList<ChatMessage> messages;
    private PauseTransition pause;

    public void initialize() {
        chatList = new ChatListView<>();
        chatList.setPlaceholder(new Label(resources.getString("no.messages.yet")));
        chatList.getStyleClass().add("chat-list");
        service.getMessages(this::createSortList);
        service.getName(this::setCellFactory);

        chatList.unreadIndexProperty().addListener((Observable o) -> updateUnreadLabel());
        chatList.unreadMessagesProperty().addListener((Observable o) -> updateUnreadLabel());
        stackPane.getChildren().add(0, chatList);

        unreadBox.visibleProperty().bind(chatList.unreadMessagesProperty().greaterThan(-1));
        unreadBox.setOnMouseClicked(e -> chatList.scrollTo(chatList.getUnreadIndex()));

//        EmojiTextArea messageEditor = new EmojiTextArea();
        TextArea messageEditor = new TextArea();
        messageEditor.getStyleClass().add("chat-text-editor");
        HBox.setHgrow(messageEditor, Priority.ALWAYS);
        bottomPane.setCenter(messageEditor);

        sendButton.prefHeightProperty().bind(addButton.heightProperty());

        sendButton.disableProperty().bind(messageEditor.textProperty().isEmpty());
        sendButton.setOnAction(e -> {
            String text = messageEditor.getText().trim();
            if (! text.isEmpty()) {
                var message = new ChatMessage(text, service.getName().get());
                messages.add(message);
                messageEditor.setText("");
            }
        });

        setupAddButton();

        if (Platform.isIOS()) {
            // allow dismissing the soft keyboard when tapping outside textArea
            chatList.mouseTransparentProperty().bind(messageEditor.focusedProperty());
            chatView.setOnMouseClicked(e -> {
                if (messageEditor.isFocused()) {
                    // Try to hide keyboard
                    addButton.requestFocus();
                }
            });

            // process button "click" when the keyboard was still showing
            sendButton.setOnMousePressed(e -> sendButton.fire());
        }

    }

    private void createSortList(GluonObservableList<ChatMessage> messages) {
        this.messages = messages;
        SortedList<ChatMessage> sortedList = new SortedList<>(messages);
        sortedList.setComparator(Comparator.comparing(ChatMessage::getTime));
        chatList.setItems(sortedList);
    }

    private void setCellFactory(GluonObservableObject<String> name) {
        chatList.setCellFactory(listView -> new MessageCell(name.get()));
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