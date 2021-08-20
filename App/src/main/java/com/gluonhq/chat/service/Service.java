package com.gluonhq.chat.service;

import com.gluonhq.chat.model.ChatImage;
import com.gluonhq.chat.model.ChatMessage;
import com.gluonhq.chat.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.gluonhq.chat.service.ImageUtils.IMAGE_PREFIX;

public interface Service {

    ObservableList<ChatImage> getImages();

    boolean login(String userName);

    ObservableList<User> getUsers();

    /**
     * Returns the currently logged-in user
     * @return Logged-in user. If no user is logged in, it will return null.
     */
    User loggedUser();

    /*String getName(Consumer<ObjectProperty<String>> consumer);*/

    default ObservableList<String> getNames(ObservableList<ChatMessage> o) {
        return FXCollections.observableArrayList(
                o.stream()
                        .map(m -> m.getUser().toString())
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList()));
    }

    default String addImage(String id, Image image) {
        ChatImage chatImage = ImageUtils.encodeImage(id, image);
        if (chatImage != null) {
            getImages().add(chatImage);
            return IMAGE_PREFIX + chatImage.getId() + IMAGE_PREFIX;
        }
        return null;
    }

    static String getInitials(String name) {
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

    ObservableList<ChatMessage> getMessages(User user);
}
