package com.gluonhq.richtext.model;

import java.util.Objects;

public class DecorationModel {
    private final int start;
    private final int length;
    private final Decoration decoration;

    public DecorationModel(int start, int length, Decoration decoration) {
        this.start = start;
        this.length = length;
        this.decoration = decoration;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DecorationModel that = (DecorationModel) o;
        return start == that.start && length == that.length && decoration.equals(that.decoration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, length, decoration);
    }

    @Override
    public String toString() {
        return "DecorationModel{" +
                "start=" + start +
                ", length=" + length +
                ", decoration=" + decoration +
                '}';
    }
}
