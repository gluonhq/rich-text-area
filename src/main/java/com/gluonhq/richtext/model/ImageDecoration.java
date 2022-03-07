package com.gluonhq.richtext.model;

public class ImageDecoration implements Decoration {

    private final int width;
    private final int height;
    private final String url;
    private final String link;

    public ImageDecoration(String url) {
        this(url, -1, -1, null);
    }

    public ImageDecoration(String url, int width, int height) {
        this(url, width, height, null);
    }

    public ImageDecoration(String url, String link) {
        this(url, -1, -1, link);
    }

    public ImageDecoration(String url, int width, int height, String link) {
        this.url = url;
        this.width = width;
        this.height = height;
        this.link = link;
    }

    public String getUrl() {
        return url;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getLink() {
        return link;
    }

    @Override
    public String toString() {
        return "ImageDecoration{" +
                "width=" + width +
                ", height=" + height +
                ", url='" + url + '\'' +
                ", link='" + link + '\'' +
                '}';
    }
}
