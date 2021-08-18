package com.gluonhq.chat.service;

import com.gluonhq.chat.model.ChatImage;
import com.gluonhq.chat.model.ChatMessage;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.gluonhq.chat.service.ImageUtils.IMAGE_PREFIX;

public interface Service {

    ObservableList<ChatMessage> getMessages();

    ObservableList<ChatMessage> getMessages(Consumer<ObservableList<ChatMessage>> consumer);

    ObservableList<ChatImage> getImages();

    boolean saveUser(String userName);

    String getName();

    String getName(Consumer<ObjectProperty<String>> consumer);

    default ObservableList<String> getNames(ObservableList<ChatMessage> o) {
        return FXCollections.observableArrayList(
                o.stream()
                        .map(ChatMessage::getAuthor)
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
}
