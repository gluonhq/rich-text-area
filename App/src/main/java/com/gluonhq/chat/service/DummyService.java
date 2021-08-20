package com.gluonhq.chat.service;

import com.gluonhq.chat.model.ChatImage;
import com.gluonhq.chat.model.ChatMessage;
import com.gluonhq.chat.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

import java.util.function.Consumer;

public class DummyService implements Service {

    private User loggedUser;

    @Override
    public ObservableList<ChatImage> getImages() {
        return FXCollections.observableArrayList(
                ImageUtils.encodeImage("1", new Image("/icon.png")),
                ImageUtils.encodeImage("2", new Image("/icon.png")),
                ImageUtils.encodeImage("3", new Image("/icon.png"))
        );
    }

    @Override
    public String addImage(String id, Image image) {
        // no-op
        return id;
    }

    @Override
    public ObservableList<ChatMessage> getMessages(User user) {
        return FXCollections.observableArrayList(
                new ChatMessage("Message 1 with " + user, loggedUser),
                new ChatMessage("Message 2 with " + user, user),
                new ChatMessage("Message 3 with " + user, user)
        );
    }

    @Override
    public boolean login(String username) {
        this.loggedUser = new User(username, "First Name", "Last Name");
        return true;
    }

    @Override
    public ObservableList<User> getUsers() {
        return FXCollections.observableArrayList(
                new User("aa", "Abhinay", "Agarwal"),
                new User("em", "Erwin", "Morrhey"),
                new User("jv", "Johan", "Vos"),
                new User("js", "Joeri", "Sykora"),
                new User("jp", "José", "Pereda")
        );
    }

    @Override
    public User loggedUser() {
        return loggedUser;
    }

    /*@Override
    public String getName(Consumer<ObjectProperty<String>> consumer) {
        consumer.accept(new SimpleObjectProperty<>("name"));
        return getName();
    }*/
}
