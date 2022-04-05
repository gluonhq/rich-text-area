package com.gluonhq.richtextarea.model;

import javafx.geometry.Insets;
import javafx.scene.text.TextAlignment;

import java.util.Objects;

public class ParagraphDecoration implements Decoration {

    public enum GraphicType {
        NONE,
        NUMBERED_LIST,
        BULLETED_LIST
    }
    private double spacing;
    private TextAlignment alignment;
    private double topInset, rightInset, bottomInset, leftInset;
    private int indentationLevel;
    private GraphicType graphicType;

    private ParagraphDecoration() {}

    public double getSpacing() {
        return spacing;
    }

    public TextAlignment getAlignment() {
        return alignment;
    }

    public double getTopInset() {
        return topInset;
    }

    public double getRightInset() {
        return rightInset;
    }

    public double getBottomInset() {
        return bottomInset;
    }

    public double getLeftInset() {
        return leftInset;
    }

    public void setBottomInset(double bottomInset) {
        this.bottomInset = bottomInset;
    }

    public int getIndentationLevel() {
        return indentationLevel;
    }

    public GraphicType getGraphicType() {
        return graphicType;
    }

    public static ParagraphDecoration.Builder builder() {
        return new ParagraphDecoration.Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParagraphDecoration that = (ParagraphDecoration) o;
        return Double.compare(that.spacing, spacing) == 0 &&
                Double.compare(that.topInset, topInset) == 0 &&
                Double.compare(that.rightInset, rightInset) == 0 &&
                Double.compare(that.bottomInset, bottomInset) == 0 &&
                Double.compare(that.leftInset, leftInset) == 0 &&
                that.indentationLevel == indentationLevel &&
                that.graphicType == graphicType &&
                alignment == that.alignment;
    }

    @Override
    public int hashCode() {
        return Objects.hash(spacing, alignment, topInset, rightInset, bottomInset, leftInset, indentationLevel, graphicType);
    }

    @Override
    public String toString() {
        return "PDec{" +
                "s=" + spacing +
                ", a=" + alignment +
                ", [" + topInset + ", " + rightInset + ", " + bottomInset + ", " + leftInset + "]" +
                ", i=" + indentationLevel + ", type=" + graphicType + "}";
    }

    public static class Builder {
        private double spacing;
        private TextAlignment alignment;
        private double topInset, rightInset, bottomInset, leftInset;
        private int indentationLevel;
        private GraphicType graphicType;

        private Builder() {}

        public ParagraphDecoration build() {
            ParagraphDecoration decoration = new ParagraphDecoration();
            decoration.spacing = this.spacing;
            decoration.alignment = this.alignment;
            decoration.topInset = this.topInset;
            decoration.rightInset = this.rightInset;
            decoration.bottomInset = this.bottomInset;
            decoration.leftInset = this.leftInset;
            decoration.indentationLevel = this.indentationLevel;
            decoration.graphicType = this.graphicType;
            return decoration;
        }

        public Builder presets() {
            spacing = 0;
            alignment = TextAlignment.LEFT;
            topInset = 0;
            rightInset = 0;
            bottomInset = 0;
            leftInset = 0;
            indentationLevel = 0;
            graphicType = GraphicType.NONE;
            return this;
        }

        public Builder fromDecoration(ParagraphDecoration decoration) {
            this.spacing = decoration.spacing;
            this.alignment = decoration.alignment;
            this.topInset = decoration.topInset;
            this.rightInset = decoration.rightInset;
            this.bottomInset = decoration.bottomInset;
            this.leftInset = decoration.leftInset;
            this.indentationLevel = decoration.indentationLevel;
            this.graphicType = decoration.graphicType;
            return this;
        }

        public Builder spacing(double spacing) {
            this.spacing = spacing;
            return this;
        }

        public Builder alignment(TextAlignment alignment) {
            this.alignment = Objects.requireNonNull(alignment);
            return this;
        }

        public Builder insets(double topInset, double rightInset, double bottomInset, double leftInset) {
            this.topInset = topInset;
            this.rightInset = rightInset;
            this.bottomInset = bottomInset;
            this.leftInset = leftInset;
            return this;
        }

        public Builder insets(Insets insets) {
            this.topInset = Objects.requireNonNull(insets).getTop();
            this.rightInset = insets.getRight();
            this.bottomInset = insets.getBottom();
            this.leftInset = insets.getLeft();
            return this;
        }

        public Builder topInset(double topInset) {
            this.topInset = topInset;
            return this;
        }

        public Builder rightInset(double rightInset) {
            this.rightInset = rightInset;
            return this;
        }

        public Builder bottomInset(double bottomInset) {
            this.bottomInset = bottomInset;
            return this;
        }

        public Builder leftInset(double leftInset) {
            this.leftInset = leftInset;
            return this;
        }

        public Builder indentationLevel(int indentationLevel) {
            this.indentationLevel = indentationLevel;
            this.graphicType = indentationLevel == 0 ? GraphicType.NONE : graphicType;
            return this;
        }

        public Builder graphicType(GraphicType graphicType) {
            this.graphicType = graphicType;
            this.indentationLevel = Math.max(graphicType != GraphicType.NONE ? 1 : 0, indentationLevel);
            return this;
        }
    }
}
