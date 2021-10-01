package com.gluonhq.chat.service;

import com.gluonhq.chat.model.Channel;
import com.gluonhq.chat.model.ChatImage;
import com.gluonhq.chat.model.ChatMessage;
import com.gluonhq.chat.model.User;
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
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CloudlinkService implements Service {

    private static final Logger LOG = Logger.getLogger(CloudlinkService.class.getName());

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

    public GluonObservableList<ChatMessage> getMessages(Consumer<ObservableList<ChatMessage>> consumer) {
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

    public boolean login(String userName) {
        if (names.contains(userName)) {
            return false;
        } else {
            names.add(userName);
            name.set(userName);
            localDataClient.push(name);
            return true;
        }
    }

    @Override
    public ObservableList<User> getUsers() {
        // TODO: implement
        return null;
    }

    @Override
    public ObservableList<Channel> getChannels() {
        // TODO: implement
        return null;
    }

    @Override
    public User loggedUser() {
        // TODO: implement
        return null;
    }

    public String getName() {
        return getName(null);
    }

    public String getName(Consumer<ObjectProperty<String>> consumer) {
        processConsumer(name, consumer);
        return name.getValue();
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

    private void getRemoteUsers() {
        names = DataProvider.retrieveList(
                dataClient.createListDataReader(CHATAPP_USER_NAMES, String.class, SyncFlag.LIST_WRITE_THROUGH));
        names.exceptionProperty().addListener((obs, ov, nv) -> LOG.log(Level.WARNING, "Error Remote Users: " + nv));
    }
}
