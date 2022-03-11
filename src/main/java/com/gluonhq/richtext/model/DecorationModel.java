package com.gluonhq.richtext.model;

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
}
