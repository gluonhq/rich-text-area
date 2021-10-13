package com.gluonhq.chat.model;

import java.util.Objects;
import java.util.UUID;

public class User extends Searchable {

    /**
     * @return the avatarPath
     */
    public String getAvatarPath() {
        return avatarPath;
    }

    /**
     * @param avatarPath the avatarPath to set
     */
    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    private final String id;
    private String username;
    private String firstname;
    private String lastname;
    private String avatarPath;

    public User(String username, String firstname, String lastname) {
        this(username, firstname, lastname, UUID.randomUUID().toString());
    }

    public User(String username, String firstname, String lastname, String uuid) {
        this.id = uuid;
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String displayName() {
        return firstname + " " + lastname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                '}';
    }

    @Override
    public boolean contains(String keyword) {
        return containsKeyword(getUsername(), keyword) ||
               containsKeyword(getFirstname(), keyword)  ||
               containsKeyword(getLastname(), keyword);
    }
}
