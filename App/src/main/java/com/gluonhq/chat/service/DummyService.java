package com.gluonhq.chat.service;

import com.gluonhq.chat.model.Channel;
import com.gluonhq.chat.model.ChatImage;
import com.gluonhq.chat.model.ChatMessage;
import com.gluonhq.chat.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.image.Image;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class DummyService implements Service {

    private User loggedUser;

    public DummyService() {
        login("Alice");
    }

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
        final ObservableList<User> generalUsers = getUsers();
        final FilteredList<User> notificationUsers = getUsers().filtered(u -> u.getUsername().startsWith("j"));
        final FilteredList<User> trackUsers = getUsers().filtered(u -> !u.getUsername().startsWith("j"));
        ObservableList<Channel> answer = FXCollections.observableArrayList(
                new Channel("general", generalUsers, createDummyMessages(generalUsers.toArray(new User[0]))),
                new Channel("notification", notificationUsers, createDummyMessages(notificationUsers.toArray(new User[0]))),
                new Channel("track", trackUsers, createDummyMessages(trackUsers.toArray(new User[0]))),
                // Directs
                new Channel(getUsers().get(0), createDummyMessages(getUsers().get(1))),
                new Channel(getUsers().get(1), createDummyMessages(getUsers().get(1))),
                new Channel(getUsers().get(2), createDummyMessages(getUsers().get(1))),
                new Channel(getUsers().get(3), createDummyMessages(getUsers().get(2))),
                new Channel(getUsers().get(4), createDummyMessages(getUsers().get(3)))
        );
        answer.stream().forEach(c -> {
            if (Math.random() > .5) c.setUnread(true);
        });
        return answer;
    }

    @Override
    public User loggedUser() {
        return loggedUser;
    }
    
    private ObservableList<ChatMessage> createDummyMessages(User... members) {
        return Arrays.stream(members)
                .map(user -> new ChatMessage("Message from " + user.displayName(), user, randomDateTime()))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    private long randomDateTime() {
        long minDay = LocalDateTime.of(1970, 1, 1, 0, 0).toEpochSecond(ZoneOffset.UTC);
        long maxDay = LocalDateTime.of(2021, 1, 1, 0, 0).toEpochSecond(ZoneOffset.UTC);
        long randomTime = ThreadLocalRandom.current().nextLong(minDay, maxDay);
        return randomTime;
//        return LocalDateTime.ofEpochSecond(randomTime, 0, ZoneOffset.UTC);
    }
}
