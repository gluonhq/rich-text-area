package com.gluonhq.richtextarea.model;

import java.util.Objects;

/**
 * ImageDecoration is a {@link Decoration} that can be applied to a fragment of text in order to place
 * an image at its location.
 */
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

    /**
     * Returns the string with the url of the image. It can be a resource path, a file path, or a valid URL.
     *
     * @return a string with the image's url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns the width used for the image inserted in the RichTextArea control. Resizing, if needed,
     * is done preserving the aspect ratio of the image. If the value is -1,
     * the image will use its original width, but being limited to the control area.
     *
     * @defaultValue -1
     *
     * @return the width of the image in the text, or -1 to use the original image width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height used for the image inserted in the RichTextArea control. Resizing, if needed,
     * is done preserving the aspect ratio of the image. If the value is -1,
     * the image will use its original height, but being limited to the control area.
     *
     * @defaultValue -1
     *
     * @return the height of the image in the text, or -1 to use the original image height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gets a string with a URL, if any, that can be used to set a hyperlink on the image itself
     *
     * @defaultValue null
     *
     * @return a string with a URL or null
     */
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
        return "IDec{" +
                "width=" + width +
                ", height=" + height +
                ", url='" + url + '\'' +
                ", link='" + link + '\'' +
                '}';
    }
}
