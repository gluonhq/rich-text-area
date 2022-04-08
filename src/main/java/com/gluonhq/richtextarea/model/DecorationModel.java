package com.gluonhq.richtextarea.model;

import java.util.Objects;

/**
 * A DecorationModel contains the text and paragraph decorations for a fragment of text,
 * defined by a start position and a length.
 */
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

    /**
     * Returns the start position of the fragment that has this decoration model
     *
     * @return the start position of the fragment
     */
    public int getStart() {
        return start;
    }

    /**
     * Returns the length of the fragment that has this decoration model
     *
     * @return the length of the fragment
     */
    public int getLength() {
        return length;
    }

    /**
     * Returns the decoration for the fragment. It can be a {@link TextDecoration} like font or color,
     * or an {@link ImageDecoration}, which inserts an image at the start of the fragment
     *
     * @return the decoration for the fragment
     */
    public Decoration getDecoration() {
        return decoration;
    }

    /**
     * Returns the {@link ParagraphDecoration} for the fragment, that is used to style the paragraph that contains
     * such fragment.
     *
     * @return the paragraph decoration for the fragment
     */
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
