package com.gluonhq.chat.model;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class ChatMessage extends Searchable {

    public enum ReceiptType {
        UNKNOWN (0),
        DELIVERY (1),
        READ (2),
        VIEWED (3);

        private final int v;

        ReceiptType(int val) {
            this.v = val;
        }

        public int getV() {
            return v;
        }

        public static ReceiptType valueOf(int v) {
            return Stream.of(values())
                    .filter(t -> t.getV() == v)
                    .findFirst()
                    .orElse(UNKNOWN);
        }

    }
    
    private final String id;
    private final String message;
    private long timestamp;
    private final LocalDateTime time;
    private final User user;
    private final boolean localOriginated;
    private final ObjectProperty<ReceiptType> receiptType = new SimpleObjectProperty<>(ReceiptType.UNKNOWN);
    private List<Path> attachment = new LinkedList<>();
    
    public ChatMessage(String message, User user, long timestamp) {
        this (message, user, timestamp, false);
    }

    public ChatMessage(String message, User user, long timestamp, boolean localOriginated) {
        this.id = UUID.randomUUID().toString();
        this.message = message;
        this.user = user;
        this.timestamp = timestamp;
        this.time = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        this.localOriginated = localOriginated;
    }

    public String getMessage() {
        return message;
    }

    public void setTimestamp(long v) {
        this.timestamp = v;
    }
    
    public long getTimestamp() {
        return this.timestamp;
    }
    
    public LocalDateTime getTime() {
        return time;
    }

    public void setReceiptType(ReceiptType t) {
        receiptType.set(t);
    }

    public ObjectProperty<ReceiptType> receiptProperty() {
        return receiptType;
    }

    public ReceiptType getReceiptType() {
        return receiptType.get();
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

    public void setAttachment(List<Path> v) {
        this.attachment = v;
    }
    
    public List<Path> getAttachment() {
        return this.attachment;
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
