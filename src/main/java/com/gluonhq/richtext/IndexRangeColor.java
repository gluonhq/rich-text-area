package com.gluonhq.richtext;

import javafx.scene.paint.Color;

class IndexRangeColor {

    private final int start;
    private final int end;
    private final Color color;

    public IndexRangeColor(int start, int end, Color color) {
        this.start = start;
        this.end = end;
        this.color = color;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public Color getColor() {
        return color;
    }
}
