package com.gluonhq.chat.service;

import com.gluonhq.chat.model.Channel;
import com.gluonhq.chat.model.ChatImage;
import com.gluonhq.chat.model.ChatMessage;
import com.gluonhq.chat.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

import java.util.stream.Collectors;

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
    public ObservableList<ChatMessage> getMessages(Channel channel) {
        return channel.getMembers().stream()
                .map(user -> new ChatMessage("Message from " + user.displayName(), user))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
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
    public ObservableList<Channel> getChannels() {
        return FXCollections.observableArrayList(
                new Channel("general", getUsers()),
                new Channel("notification", getUsers().filtered(u -> u.getUsername().startsWith("j"))),
                new Channel("track", getUsers().filtered(u -> !u.getUsername().startsWith("j"))),
                // Directs
                new Channel(getUsers().get(1)),
                new Channel(getUsers().get(2)),
                new Channel(getUsers().get(3))
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
