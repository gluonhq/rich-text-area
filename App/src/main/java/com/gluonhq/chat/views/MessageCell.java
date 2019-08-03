package com.gluonhq.chat.views;

import com.airhacks.afterburner.injection.Injector;
import com.gluonhq.attach.position.Position;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.CharmListCell;
import com.gluonhq.chat.chatlistview.ChatListView;
import com.gluonhq.chat.model.ChatImage;
import com.gluonhq.chat.model.ChatMessage;
import com.gluonhq.chat.service.ImageUtils;
import com.gluonhq.chat.service.Service;
import com.gluonhq.connect.GluonObservableList;
//import com.gluonhq.emoji.EmojiData;
//import com.gluonhq.emoji.popup.util.EmojiImageUtils;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

//import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gluonhq.chat.GluonChat.MAPS_VIEW;
import static com.gluonhq.chat.service.ImageUtils.*;

class MessageCell extends CharmListCell<ChatMessage> {

//    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd HH:mm");
    private static final Insets meInsets = new Insets(10, 0, 10, 0);
    private static final Insets notMeInsets = new Insets(10, 0, 10, 0);
    private static final Image clockImage = new Image( "/clock.png");
    private static final Image leftPointerImage = new Image( "/lpointer.png");
    private static final Image rightPointerImage = new Image( "/rpointer.png");
    private static final PseudoClass SIDE_RIGHT = PseudoClass.getPseudoClass("right");
    private static final PseudoClass UNREAD = PseudoClass.getPseudoClass("unread");
    private final ImageView leftImageView = new ImageView(leftPointerImage);
    private final ImageView rightImageView = new ImageView(rightPointerImage);
    private static final Image loading = new Image(MessageCell.class.getResourceAsStream("/InternetSlowdown_Day.gif"), 150, 150, true, true);
    private ChatListView<ChatMessage> chatList;
    private final String name;

    private final TextFlow message;
    private final Label msgHandle = new Label("", null);

    private final BorderPane messageBubble;
    private final Label date  = new Label();
    private final Label status  = new Label();
    private final Label icon = new Label("ICON");
    private final Label unread;
    private final BorderPane pane;
    private final StackPane stackPane;
    private double imageOffset = -1;
    private final double imageSize = 20;
    private final Service service;
    private final ResourceBundle resources;

    public MessageCell(String name) {
        super();
        this.name = name;
        service = Injector.instantiateModelOrService(Service.class);
        resources = ResourceBundle.getBundle("com.gluonhq.chat.views.chat");

        getStyleClass().addAll("chat-list-cell");

        icon.getStyleClass().add("chat-message-icon");
        BorderPane.setAlignment(icon, Pos.TOP_CENTER);

        setWrapText(true);
        this.message = new TextFlow();
        message.setMinSize(200, 20);
        message.getStyleClass().add("chat-message-text");

        messageBubble = new BorderPane(message);

        pane = new BorderPane(messageBubble);
        pane.setBottom(new HBox(10, date, status));
        pane.getStyleClass().add("chat-message");
        pane.prefWidthProperty().bind(widthProperty());

        date.getStyleClass().add("chat-message-date");
        status.setGraphic(new ImageView(clockImage));
        status.getStyleClass().add("chat-message-status");

        msgHandle.getStyleClass().add("chat-message-text-handle");

        this.unread = new Label("New messages");
        unread.getStyleClass().add("unread");

        HBox unreadBox = new HBox(unread);
        unreadBox.getStyleClass().add("unread-box");
        StackPane.setAlignment(unreadBox, Pos.TOP_CENTER);

        stackPane = new StackPane(pane, unreadBox);
        stackPane.getStyleClass().add("stack-pane");

        setText(null);
    }

    @Override
    public void updateItem(ChatMessage item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            boolean isMe = item.getAuthor() != null &&
                    item.getAuthor().equals(name);

            message.getChildren().setAll(formatText(item.getMessage()));
            BorderPane.setMargin(message, isMe ? meInsets : notMeInsets);

            if (item.getTime() != null) {
//                status.setText(/*item.getTime().format(formatter) + */"  ✓"); // check mark if read
                date.setText(item.getFormattedTime());
                status.setText(resources.getString("label.status.check")); // check mark if read
            }
            BorderPane.setAlignment(status, isMe ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);

            icon.setText(isMe ? "ME" : Service.getInitials(item.getAuthor()));

            if (isMe) {
                msgHandle.setGraphic(rightImageView);
                msgHandle.pseudoClassStateChanged(SIDE_RIGHT, true);
                messageBubble.setLeft(null);
                messageBubble.setRight(msgHandle);
                pane.setLeft(null);
                pane.setRight(icon);
            } else {
                msgHandle.setGraphic(leftImageView);
                msgHandle.pseudoClassStateChanged(SIDE_RIGHT, false);
                messageBubble.setRight(null);
                messageBubble.setLeft(msgHandle);
                pane.setRight(null);
                pane.setLeft(icon);
            }
            pseudoClassStateChanged(UNREAD, getIndex() == getUnreadIndex());

            setGraphic(stackPane);
        }
    }

    private int getUnreadIndex() {
        if (chatList == null && getListView() instanceof ChatListView<?>) {
            chatList = (ChatListView<ChatMessage>) getListView();
        }
        return chatList == null ? 0 : chatList.getUnreadIndex();
    }

    private Text getTextNode(String text) {
        Text node = new Text(text);
        node.getStyleClass().add("text");
        return node;
    }

    private double getImageOffset(double size) {
        if (imageOffset == -1) {
            Text node = getTextNode("Dummy");
            double textOffset = node.getBaselineOffset() - node.getLayoutBounds().getHeight() / 2;
            imageOffset = textOffset / size + 0.5;
        }
        return imageOffset;
    }

    private List<Node> formatText(String value) {
        List<Node> list = new ArrayList<>();

        Pattern pattern = Pattern.compile(":[\\w-]*:");
        Matcher matcher = pattern.matcher(value);
        AtomicInteger diff = new AtomicInteger(0);
        while (matcher.find()) {
            if (diff.get() < matcher.start()) {
                Text text = getTextNode(value.substring(diff.get(), matcher.start()));
                list.add(text);
                diff.addAndGet(matcher.start() - diff.get());
            }
//            list.add(EmojiData.emojiFromCodeName(matcher.group())
//                    .map(emoji -> (Node) EmojiImageUtils.emojiView(emoji, imageSize, getImageOffset(imageSize)))
//                    .orElse(getTextNode(matcher.group())));
            diff.addAndGet(matcher.group().length());
        }
        if (diff.get() < value.length()) {
            if (value.startsWith(IMAGE_PREFIX) && value.endsWith(IMAGE_PREFIX)) {
                formatImage(value).ifPresentOrElse(imageView -> {
                        list.add(imageView);
                        setOnMouseClicked(e -> flyTo(value));
                    },
                    () -> list.add(new Text(value)));
            } else {
                list.add(getTextNode(value.substring(diff.get())));
            }
        }
        return list;
    }

    private Optional<ImageView> formatImage(String value) {
        if (value == null || value.isEmpty()) {
            return Optional.empty();
        }
        String[] split = value.split("\\$\\$");
        if (split.length < 2) {
            return Optional.empty();
        }

        String imageId = split[1];
        Optional<Image> cache = ImageUtils.getImage(imageId);
        if (cache.isPresent()) {
            return Optional.of(ImageUtils.getImageView(cache.get()));
        }

        GluonObservableList<ChatImage> images = service.getImages();
        if (! images.isInitialized()) {
            ImageView imageView = ImageUtils.getImageView(loading);
            images.setOnSucceeded(e ->
                        formatImage(value).ifPresent(iv -> {
                            imageView.setImage(iv.getImage());
                            getListView().refresh();
                        }));
            return Optional.of(imageView);
        }

        return Optional.ofNullable(images.stream()
                .filter(chatImage -> chatImage != null && chatImage.getId() != null)
                .filter(chatImage -> chatImage.getId().equals(imageId))
                .findFirst()
                .map(ImageUtils::getImageView)
                .orElse(null));
    }

    private void flyTo(String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        String[] coordinates = value.split("#");
        if (coordinates.length != 4) {
            return;
        }
        try {
            Position position = new Position(Double.parseDouble(coordinates[1]),
                    Double.parseDouble(coordinates[2]));

            String userName = coordinates[0].substring((IMAGE_PREFIX + LATLON).length());
            MobileApplication.getInstance().switchView(MAPS_VIEW).ifPresent(p ->
                    ((MapsView) p).flyTo(position, userName));
        } catch (NumberFormatException nfe) {}
    }

}
