package com.gluonhq.chat.service;

import com.gluonhq.chat.model.ChatImage;
import com.gluonhq.chat.model.ChatMessage;
import com.gluonhq.cloudlink.client.data.DataClient;
import com.gluonhq.cloudlink.client.data.DataClientBuilder;
import com.gluonhq.cloudlink.client.data.OperationMode;
import com.gluonhq.cloudlink.client.data.SyncFlag;
import com.gluonhq.connect.GluonObservable;
import com.gluonhq.connect.GluonObservableList;
import com.gluonhq.connect.GluonObservableObject;
import com.gluonhq.connect.provider.DataProvider;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.gluonhq.chat.service.ImageUtils.IMAGE_PREFIX;

public class Service {
    private static final Logger LOG = Logger.getLogger(Service.class.getName());

    private static final String MESSAGES = "messages";
    private static final String IMAGES = "images";
    private static final String CHATAPP_USER_NAMES = "usernames";

    private DataClient dataClient;
    private GluonObservableList<ChatMessage> messages;
    private GluonObservableList<ChatImage> images;
    private GluonObservableList<String> names;

    private DataClient localDataClient;
    private GluonObservableObject<String> name;

    @PostConstruct
    public void postConstruct() {
        this.dataClient = DataClientBuilder.create()
                .operationMode(OperationMode.CLOUD_FIRST)
                .build();

        this.localDataClient = DataClientBuilder.create()
                .operationMode(OperationMode.LOCAL_ONLY)
                .build();

        images = DataProvider
                .retrieveList(dataClient.createListDataReader(IMAGES, ChatImage.class));

        name = DataProvider
                .retrieveObject(localDataClient.createObjectDataReader("name", String.class,
                        SyncFlag.OBJECT_READ_THROUGH, SyncFlag.OBJECT_WRITE_THROUGH));

        getRemoteUsers();
    }

    public GluonObservableList<ChatMessage> getMessages() {
        return getMessages(null);
    }

    public GluonObservableList<ChatMessage> getMessages(Consumer<GluonObservableList<ChatMessage>> consumer) {
        if (messages == null) {
            messages = DataProvider
                    .retrieveList(dataClient.createListDataReader(MESSAGES, ChatMessage.class));
            messages.setOnFailed(e -> System.out.println("Messages failed: " + e));
        }
        processConsumer(messages, consumer);
        return messages;
    }

    public GluonObservableList<ChatImage> getImages() {
        return images;
    }

    public String addImage(String id, Image image) {
        ChatImage chatImage = ImageUtils.encodeImage(id, image);
        if (chatImage != null) {
            images.add(chatImage);
            return IMAGE_PREFIX + chatImage.getId() + IMAGE_PREFIX;
        }
        return null;
    }

    private void getRemoteUsers() {
        names = DataProvider.retrieveList(
                dataClient.createListDataReader(CHATAPP_USER_NAMES, String.class, SyncFlag.LIST_WRITE_THROUGH));
        names.exceptionProperty().addListener((obs, ov, nv) -> LOG.log(Level.WARNING, "Error Remote Users: " + nv));
    }

    public boolean saveUser(String userName) {
        if (names.contains(userName)) {
            return false;
        } else {
            names.add(userName);
            name.set(userName);
            localDataClient.push(name);
            return true;
        }
    }

    public GluonObservableObject<String> getName() {
        return getName(null);
    }

    public GluonObservableObject<String> getName(Consumer<GluonObservableObject<String>> consumer) {
        processConsumer(name, consumer);
        return name;
    }

    @SuppressWarnings("unchecked")
    private static <T> void processConsumer(GluonObservable o, Consumer<T> consumer) {
        if (o == null || consumer == null) {
            return;
        }
        if (o.isInitialized()) {
            consumer.accept((T) o);
        } else {
            o.initializedProperty().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    if (o.isInitialized()) {
                        o.initializedProperty().removeListener(this);
                        consumer.accept((T) o);
                    }
                }
            });
        }
    }

    public ObservableList<String> getNames(GluonObservableList<ChatMessage> o) {
        return FXCollections.observableArrayList(
                o.stream()
                        .map(ChatMessage::getAuthor)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList()));
    }

    public static String getInitials(String name) {
        if (name != null) {
            return Arrays.stream(name.split(" "))
                    .map(String::trim)
                    .filter(s -> s.length() > 0)
                    .map(s -> s.substring(0, 1))
                    .map(String::toUpperCase)
                    .collect(Collectors.joining());
        } else {
            return "";
        }
    }
}
