package com.gluonhq.chat.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.UUID;

public class Channel extends Searchable {

    private final String id;
    private String name;
    private boolean isDirect;
    private final ObservableList<User> members;
    private final ObservableList<ChatMessage> messages;

    public Channel(String name, ObservableList<User> members, ObservableList<ChatMessage> messages) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.members = members;
        this.messages = messages;
    }

    /**
     * Create a direct channel with a user
     * @param user Direct channel with
     */
    public Channel(User user, ObservableList<ChatMessage> messages) {
        this(user.displayName(), FXCollections.observableArrayList(user), messages);
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

    @Override
    public boolean contains(String keyword) {
        return containsKeyword(getName(), keyword);
    }
    
    public String displayName() {
        return isDirect ? name : "# " + name;
    }
}
