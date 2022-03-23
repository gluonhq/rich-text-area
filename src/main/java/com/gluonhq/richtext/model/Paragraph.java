package com.gluonhq.richtext.model;

import java.util.Objects;

public class Paragraph {

    /**
     * Global index of the initial position of the paragraph
     */
    private final int start;

    /**
     * Global index of the final position of the paragraph,
     * that is defined by the position of the '\n' character
     */
    private final int end;

    public Paragraph(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Paragraph paragraph = (Paragraph) o;
        return start == paragraph.start && end == paragraph.end;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public String toString() {
        return "Paragraph[" + start + ", " + end + ")";
    }
}
