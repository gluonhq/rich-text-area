package com.gluonhq.richtext.model;

import javafx.scene.paint.Color;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.util.Objects;

import static com.gluonhq.richtext.Tools.getFirstLetter;

public class TextDecoration {

    private Color foreground;
    private Color background;
    private String fontFamily;
    private double fontSize;
    private FontPosture fontPosture;
    private FontWeight fontWeight;
    private boolean strikethrough;
    private boolean underline;

    private TextDecoration() {}

    public Color getForeground() {
        return foreground;
    }

    public Color getBackground() {
        return background;
    }

    public double getFontSize() {
        return fontSize;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public FontPosture getFontPosture() {
        return fontPosture;
    }

    public FontWeight getFontWeight() {
        return fontWeight;
    }

    public boolean isStrikethrough() {
        return strikethrough;
    }

    public boolean isUnderline() {
        return underline;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Color foreground;
        private Color background;
        private String fontFamily;
        private double fontSize;
        private FontPosture fontPosture;
        private FontWeight fontWeight;
        private boolean strikethrough;
        private boolean underline;

        private Builder() {}

        public TextDecoration build() {
            TextDecoration decoration = new TextDecoration();
            decoration.foreground  = this.foreground;
            decoration.background  = this.background;
            decoration.fontFamily  = this.fontFamily;
            decoration.fontSize    = this.fontSize;
            decoration.fontWeight  = this.fontWeight;
            decoration.fontPosture = this.fontPosture;
            decoration.strikethrough = this.strikethrough;
            decoration.underline = this.underline;
            return decoration;
        }

        public Builder presets() {
            foreground = Color.BLUE;
            background = Color.TRANSPARENT;
            fontFamily = "Arial";
            fontSize = 17.0;
            fontPosture = FontPosture.REGULAR;
            fontWeight = FontWeight.NORMAL;
            return this;
        }

        public Builder fromDecoration(TextDecoration decoration) {
            foreground = decoration.foreground;
            background = decoration.background;
            fontFamily = decoration.fontFamily;
            fontSize = decoration.fontSize;
            fontPosture = decoration.fontPosture;
            fontWeight = decoration.fontWeight;
            strikethrough = decoration.strikethrough;
            underline = decoration.underline;
            return this;
        }

        public Builder foreground(Color color) {
            this.foreground = Objects.requireNonNull(color);
            return this;
        }

        public Builder background(Color color) {
            this.background = Objects.requireNonNull(color);
            return this;
        }

        public Builder fontFamily(String fontFamily) {
            this.fontFamily = Objects.requireNonNull(fontFamily);
            return this;
        }

        public Builder fontSize(double fontSize) {
            this.fontSize = fontSize;
            return this;
        }

        public Builder fontWeight(FontWeight fontWeight) {
            this.fontWeight = Objects.requireNonNull(fontWeight);
            return this;
        }

        public Builder fontPosture(FontPosture fontPosture) {
            this.fontPosture = Objects.requireNonNull(fontPosture);
            return this;
        }

        public Builder strikethrough(boolean strikethrough) {
            this.strikethrough = strikethrough;
            return this;
        }

        public Builder underline(boolean underline) {
            this.underline = underline;
            return this;
        }
    }

    @Override
    public String toString() {
        return "deco{" +
                "fcolor=" + foreground +
                ", bcolor=" + background +
                ", font['" + fontFamily + '\'' +
                ", " + fontSize +
                ", " + (fontPosture != null ? getFirstLetter(fontPosture.name()) : "-") +
                ", " + (fontWeight != null ? getFirstLetter(fontWeight.name()) : "-") +
                "]" +
                ", strikethrough=" + strikethrough +
                ", underline=" + underline +
                "}";
    }
}






