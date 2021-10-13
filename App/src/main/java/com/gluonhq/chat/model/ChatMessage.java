package com.gluonhq.chat.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class ChatMessage extends Searchable {

    private String id;
    private String message;
    private LocalDateTime time;
    private User user;
    private boolean localOriginated;

    public ChatMessage(String message, User user, LocalDateTime time) {
        this (message, user, time, false);
    }

    public ChatMessage(String message, User user, LocalDateTime time, boolean localOriginated) {
        this.id = UUID.randomUUID().toString();
        this.message = message;
        this.user = user;
        this.time = time;
        this.localOriginated = localOriginated;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public User getUser() {
        return user;
    }

    public String getId() {
        return id;
    }

   /**
    * Returns true when this message originates from the current device
    */
    public boolean getLocalOriginated() {
        return this.localOriginated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatMessage)) return false;
        ChatMessage that = (ChatMessage) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "id='" + id + '\'' +
                ", message='" + message + '\'' +
                ", time=" + time +
                ", user=" + user.toString() +
                ", localoriginated = " + localOriginated + 
                '}';
    }

    @Override
    public boolean contains(String keyword) {
        return containsKeyword(getMessage(), keyword);
    }

    public String getFormattedTime() {
        return getTime().toLocalDate().toString() + " " +
                String.format("%02d:%02d", getTime().getHour(),getTime().getMinute());
    }
}
