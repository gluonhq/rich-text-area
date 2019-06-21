package com.gluonhq.chat.views;

import com.airhacks.afterburner.injection.Injector;
import com.gluonhq.attach.util.Platform;
import com.gluonhq.charm.glisten.mvc.View;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.Comparator;
import java.util.ResourceBundle;

public class ChatView extends View {

    private Service service;

    private final StackPane stackPane;
    private final HBox unreadBox;
    private final Label goButton;
    private final Label unread;

    private final HBox bottomPane;
    private final Button addButton;
    private final Button sendButton;

    private final ResourceBundle resources;

    private ChatListView<ChatMessage> chatList;
    private GluonObservableList<ChatMessage> messages;
    private PauseTransition pause;

    public ChatView() {
        resources = ResourceBundle.getBundle("com.gluonhq.chat.views.chat");
        getStylesheets().add(UsersView.class.getResource("chat.css").toExternalForm());

        chatList = new ChatListView<>();
        chatList.setPlaceholder(new Label(resources.getString("no.messages.yet")));
        chatList.getStyleClass().add("chat-list");

        service = Injector.instantiateModelOrService(Service.class);
        service.getMessages(this::createSortList);
        service.getName(this::setCellFactory);

        chatList.unreadIndexProperty().addListener((Observable o) -> updateUnreadLabel());
        chatList.unreadMessagesProperty().addListener((Observable o) -> updateUnreadLabel());

        goButton = new Label(resources.getString("button.down.text"));
        goButton.getStyleClass().add("go-bottom");
        unread = new Label();
        unread.getStyleClass().add("unread");
        unreadBox = new HBox(goButton, unread);
        unreadBox.getStyleClass().add("outter-box");
        unreadBox.visibleProperty().bind(chatList.unreadMessagesProperty().greaterThan(-1));
        unreadBox.setOnMouseClicked(e -> chatList.scrollTo(chatList.getUnreadIndex()));

        stackPane = new StackPane(chatList, unreadBox);
        setCenter(stackPane);

        addButton = new Button(resources.getString("button.plus.text"));
        addButton.getStyleClass().add("chat-button");

//        EmojiTextArea messageEditor = new EmojiTextArea();
        TextArea messageEditor = new TextArea();
        messageEditor.getStyleClass().add("chat-text-editor");
        HBox.setHgrow(messageEditor, Priority.ALWAYS);

        sendButton = new Button(resources.getString("button.send.text"));
        sendButton.getStyleClass().add("chat-button");
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

        bottomPane = new HBox();
        bottomPane.getStyleClass().add("chat-bottom-bar");
        bottomPane.getChildren().addAll(addButton, messageEditor, sendButton);
        setBottom(bottomPane);

        setupAddButton();

        if (Platform.isIOS()) {
            // allow dismissing the soft keyboard when tapping outside textArea
            chatList.mouseTransparentProperty().bind(messageEditor.focusedProperty());
            setOnMouseClicked(e -> {
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
