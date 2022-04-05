package com.gluonhq.richtextarea.model;

import java.util.Objects;

public class DecorationModel {
    private final int start;
    private final int length;
    private final Decoration decoration;    // TextDecoration or ImageDecoration
    private final ParagraphDecoration paragraphDecoration;


    public DecorationModel(int start, int length, Decoration decoration, ParagraphDecoration paragraphDecoration) {
        this.start = start;
        this.length = length;
        this.decoration = decoration;
        this.paragraphDecoration = paragraphDecoration;
    }

    public int getStart() {
        return start;
    }

    public int getLength() {
        return length;
    }

    public Decoration getDecoration() {
        return decoration;
    }

    public ParagraphDecoration getParagraphDecoration() {
        return paragraphDecoration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DecorationModel that = (DecorationModel) o;
        return start == that.start && length == that.length &&
                decoration.equals(that.decoration) &&
                paragraphDecoration.equals(that.paragraphDecoration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, length, decoration, paragraphDecoration);
    }

    @Override
    public String toString() {
        return "DecorationModel{" +
                "start=" + start +
                ", length=" + length +
                ", decoration=" + decoration +
                ", paragraphDecoration=" + paragraphDecoration +
                '}';
    }
}
