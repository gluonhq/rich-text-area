package com.gluonhq.richtext;

import java.util.Objects;

public class Selection {

    public static Selection UNDEFINED = new Selection(-1,-1);

    private final int start;
    private final int end;

    public Selection(int start, int end) {
        if (start < 0 || end < 0 || start == end ) {
            this.start = -1;
            this.end   = -1;
        } else {
            // auto normalize
            this.start = Math.min(start, end);
            this.end   = Math.max(start, end);
        }
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getLength() {
        return end - start;
    }

    public boolean isDefined() {
        return !UNDEFINED.equals(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Selection selection = (Selection) o;
        return start == selection.start && end == selection.end;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public String toString() {
        return "Selection {" +
                "start=" + start +
                ", end=" + end +
                '}';
    }
}