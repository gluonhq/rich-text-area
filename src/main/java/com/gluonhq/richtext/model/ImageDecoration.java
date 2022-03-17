package com.gluonhq.richtext.model;

import java.util.Objects;

public class ImageDecoration implements NonTextDecoration {

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageDecoration that = (ImageDecoration) o;
        return width == that.width && height == that.height && url.equals(that.url) && link.equals(that.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, url, link);
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
