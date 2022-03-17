package com.gluonhq.richtext.model;

import java.util.Objects;

public class HyperlinkDecoration implements NonTextDecoration {

    private final String url;
    private final String text;

    public HyperlinkDecoration(String url) {
        this(url, url);
    }
    
    public HyperlinkDecoration(String url,  String text) {
        this.url = url;
        this.text = text;
    }

    public String getUrl() {
        return url;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HyperlinkDecoration that = (HyperlinkDecoration) o;
        return url.equals(that.url) && text.equals(that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, text);
    }

    @Override
    public String toString() {
        return "HyperlinkDecoration{" +
                ", url='" + url + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
