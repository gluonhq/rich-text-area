package com.gluonhq.chat.model;

public class ChatImage {

    String id;
    String content;
    int width;
    int height;

    public ChatImage(String id, String content, int width, int height) {
        this.id = id;
        this.content = content;
        this.width = width;
        this.height = height;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return "ChatImage{" +
                "id='" + id + '\'' +
                ", width=" + width +
                ", height=" + height +
                '}';
    }

}
