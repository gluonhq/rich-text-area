package com.gluonhq.richtext.decoration;

import java.util.Objects;

public class TextFragmentDecoration {

    private int start;
    private int length;
    private TextDecoration decoration;

    public TextFragmentDecoration(int start, int length, TextDecoration decoration) {
        this.start = start;
        this.length = length;
        this.decoration = Objects.requireNonNull(decoration);
    }

    public int getStart() {
        return start;
    }

    public int getLength() {
        return length;
    }

    public TextDecoration getDecoration() {
        return decoration;
    }
}
