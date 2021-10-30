package com.gluonhq.chat.model;

import com.airhacks.afterburner.injection.Injector;
import com.gluonhq.chat.service.Service;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Objects;
import java.util.UUID;

public class Channel extends Searchable {

    private final String id;
    private String name;
    private boolean isDirect;
    private final ObservableList<User> members;
    private final ObservableList<ChatMessage> messages;
    private final BooleanProperty unread = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            service.updateUnreadList(getId(), get());
        }
    };
    private final BooleanProperty typing = new SimpleBooleanProperty();
    private final Service service;


    public Channel(String id, String name, ObservableList<User> members, ObservableList<ChatMessage> messages) {
        this.id = id;
        this.name = name;
        this.members = members;
        this.messages = messages;
        service = Injector.instantiateModelOrService(Service.class);
    }

    public Channel(String name, ObservableList<User> members, ObservableList<ChatMessage> messages) {
        this(UUID.randomUUID().toString(), name, members, messages);
    }

    /**
     * Create a direct channel with a user. In this case, the unique ID of the channel is 
     * c + the unique userid
     * @param user Direct channel with
     */
    public Channel(User user, ObservableList<ChatMessage> messages) {
        this("c"+user.getId(), user.displayName(), FXCollections.observableArrayList(user), messages);
        this.isDirect = true;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDirect() {
        return isDirect;
    }

    public void setDirect(boolean direct) {
        isDirect = direct;
    }

    public ObservableList<User> getMembers() {
        return members;
    }

    public void addMember(User member) {
        this.members.add(member);
    }

    public ObservableList<ChatMessage> getMessages() {
        return messages;
    }

    /**
     * Checks if this channel contains messages that are not yet read by this 
     * user
     * @return true if there are new messages in this channel, false otherwise 
     */
    public boolean isUnread() {
        return unread.get();
    }

    /**
     * Sets the unread status of this channel to true if there are messages not
     * yet read by the user, or to false otherwise
     * @param unread true if there are unread messages, false otherwise
     */
    public void setUnread(boolean unread) {
        this.unread.set(unread);
    }

    public BooleanProperty unreadProperty() {
        return unread;
    }

    @Override
    public boolean contains(String keyword) {
        return containsKeyword(getName(), keyword);
    }
    
    public BooleanProperty typingProperty() {
        return this.typing;
    }

    public boolean isTyping() {
        return typing.get();
    }

    public void setTyping(boolean typing) {
        this.typing.set(typing);
    }

    public String displayName() {
        return isDirect ? name : "# " + name;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.id);
        hash = 53 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Channel other = (Channel) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }
}
