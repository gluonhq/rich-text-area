package com.gluonhq.chat.chatlistview;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Demo extends Application {

    private final ObservableList<Message> lines = FXCollections.observableArrayList();
//    private final List<String> messages = Arrays.asList("This is a new message", "How are you doing", "Hello there!");
    private final List<String> messages = Arrays.asList("This is a\nnew message", "How are\nyou\ndoing", "Hello there!");
    
    private final int batchSize = 10;
    private ChatListView<Message> chatListView;
    
    private Label unread;
    private PauseTransition pause;
    
    @Override
    public void start(Stage stage) throws Exception {
        
        Label goButton = new Label("\uf0ab");
        goButton.getStyleClass().addAll("go-bottom");
        unread = new Label("");
        unread.getStyleClass().addAll("unread");
        
        HBox box = new HBox(goButton, unread);
        box.getStyleClass().add("unread-box");
        box.setOnMouseClicked(e -> chatListView.scrollTo(chatListView.getUnreadIndex()));
        
        HBox outterBox = new HBox(box);
        outterBox.getStyleClass().add("outter-box");
        StackPane.setAlignment(outterBox, Pos.TOP_CENTER);
        
        chatListView = new ChatListView<>(lines);
        chatListView.setPlaceholder(new Label("There are no messages"));
        chatListView.setCellFactory(p -> new ChatListCell());
        chatListView.setOnDataRequest(createBatchService());
        chatListView.unreadIndexProperty().addListener((Observable o) -> updateUnreadLabel());
        chatListView.unreadMessagesProperty().addListener((Observable o) -> updateUnreadLabel());
        box.visibleProperty().bind(chatListView.unreadMessagesProperty().greaterThan(-1));

        Button addButton = new Button("Add Message");
        addButton.setOnAction(e -> addMessage());
        
        Scene scene = new Scene(new VBox(addButton, new StackPane(chatListView, outterBox)), 450, 300);
        scene.getStylesheets().add(Demo.class.getResource("/style.css").toExternalForm());
        
        stage.setTitle("ChatListView Demo");
        stage.setScene(scene);
        stage.show();
    }
    
    private void addMessage() {
        LocalDateTime time = LocalDateTime.now();
        lines.add(createMessage(time, lines.size()));
    }

    private Message createMessage(LocalDateTime time, int i) {
        return new Message(/*"#" + i + " " + */messages.get(new Random().nextInt(3)), new Random().nextInt(2), time.toEpochSecond(ZoneOffset.UTC));
    }
    
    private Service<Collection<Message>> createBatchService() {
        return new Service<Collection<Message>>() {
            @Override
            protected Task<Collection<Message>> createTask() {
               return new Task<Collection<Message>>() {
                    @Override
                    protected Collection<Message> call() throws Exception {

                        Thread.sleep(1000);

                        int size = lines.size();
                        List<Message> list = new ArrayList<>();
                        LocalDateTime timeRef = LocalDateTime.ofEpochSecond(lines.get(0).getTimestamp(), 0, ZoneOffset.UTC);
                        for (int i = batchSize; i >= 1; i--) {
                            LocalDateTime time = timeRef.minusMinutes(i);
                            list.add(createMessage(time, size + i));
                        }

                        return list;
                    }
                };
            }
        };
    }
    
    private void updateUnreadLabel() {
        int unreadMessages = chatListView.getUnreadMessages() > -1 ? chatListView.getUnreadMessages() : 0;
        if (unreadMessages > 0) {
            unread.setText(String.valueOf(unreadMessages) + " unread message" + (unreadMessages > 1 ? "s" : " "));
        }
        if (pause == null) {
            pause = new PauseTransition(Duration.millis(500));
            pause.setOnFinished(f -> chatListView.refresh());
        }
        pause.playFromStart();
    }
    
    class Message {
    
        private final String message;
        private final int user;
        private final long timestamp;

        public Message(String message, int user, long timestamp) {
            this.message = message;
            this.user = user;
            this.timestamp = timestamp;
        }

        public String getMessage() {
            return message;
        }

        public int getUser() {
            return user;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return "Message{" + "message=" + message.replaceAll("\n", "_") + ", user=" + user + '}';
        }
    }
    
    class ChatListCell extends ListCell<Message> {

        private static final String DEFAULT_STYLE_CLASS = "chat-list-cell";
        
        private final PseudoClass LEFT = PseudoClass.getPseudoClass("left");
        private final PseudoClass RIGHT = PseudoClass.getPseudoClass("right");
        private final PseudoClass UNREAD = PseudoClass.getPseudoClass("unread");
    
        private final StackPane main;
        private final HBox box;
        private final VBox bubble;
        private final Label message;
        private final Label time;
        private final Label check;
        private final Label unread;

        public ChatListCell() {
            super();
            getStyleClass().addAll(DEFAULT_STYLE_CLASS);
            
            this.message = new Label();
            message.getStyleClass().add("message");
            this.time = new Label();
            time.getStyleClass().add("time");
            this.check = new Label("\uf058");
            check.getStyleClass().add("font-icon");
            Pane pane = new Pane();
            pane.setMinWidth(10);
            HBox.setHgrow(pane, Priority.ALWAYS);
            HBox secondRow = new HBox(time, pane, check);
            this.bubble = new VBox(message, secondRow);
            bubble.getStyleClass().add("bubble");
            
            this.box = new HBox(bubble);
            box.getStyleClass().add("box");
            
            this.unread = new Label("New messages");
            unread.getStyleClass().add("unread");
            
            HBox unreadBox = new HBox(unread);
            unreadBox.getStyleClass().add("unread-box");
            StackPane.setAlignment(unreadBox, Pos.TOP_CENTER);
            
            this.main = new StackPane(box, unreadBox);
            main.getStyleClass().add("main");
            setText(null);
        }
        
        @Override
        protected void updateItem(Message item, boolean empty) {
            super.updateItem(item, empty); 
            if (item != null && ! empty) {
                message.setText(item.getMessage());
                LocalDateTime timeRef = LocalDateTime.ofEpochSecond(item.getTimestamp(), 0, ZoneOffset.UTC);
                time.setText(timeRef.format(DateTimeFormatter.ISO_TIME));
                pseudoClassStateChanged(LEFT, item.getUser() == 0);
                pseudoClassStateChanged(RIGHT, item.getUser() == 1);
                pseudoClassStateChanged(UNREAD, getIndex() == chatListView.getUnreadIndex());
                setGraphic(main);
            } else {
                setGraphic(null);
            }
        }

        @Override
        public String toString() {
            return "ChatListCell{" + "message=" + message.getText().replaceAll("\n", "_") + '}';
        }

    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
