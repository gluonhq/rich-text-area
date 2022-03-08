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

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns a new instance of the current TextDecoration
     * and normalizes properties based on the supplied decoration.
     * FontWeight and FontPosture normalize into
     * {@link FontWeight#NORMAL} and {@link FontPosture#REGULAR} respectively.
     * @param decoration Decoration to compare and normalize
     * @return New TextDecoration instance with properties normalized
     */
    public TextDecoration normalize(TextDecoration decoration) {
        if (decoration == null) {
            return this;
        }
        TextDecoration td = new TextDecoration();
        final double normalizedFontSize = this.fontSize < 1.0 ? decoration.getFontSize() : this.fontSize;
        final FontWeight normalizedWeight = this.fontWeight == decoration.getFontWeight() ? FontWeight.NORMAL : this.fontWeight;
        final FontPosture normalizedPosture = this.fontPosture == decoration.getFontPosture() ? FontPosture.REGULAR : this.fontPosture;
        td.foreground  = Objects.requireNonNullElse(foreground, decoration.getForeground());
        td.background  = Objects.requireNonNullElse(background, decoration.getBackground());
        td.fontFamily  = Objects.requireNonNullElse(fontFamily, decoration.getFontFamily());
        td.fontSize    = normalizedFontSize;
        td.fontWeight  = Objects.requireNonNullElse(normalizedWeight, decoration.getFontWeight());
        td.fontPosture = Objects.requireNonNullElse(normalizedPosture, decoration.getFontPosture());
        return td;
    }

    public static class Builder {

        private Color foreground;
        private Color background;
        private String fontFamily;
        private double fontSize;
        private FontPosture fontPosture;
        private FontWeight fontWeight;

        private Builder() {}

        public TextDecoration build() {
            TextDecoration decoration = new TextDecoration();
            decoration.foreground  = this.foreground;
            decoration.background  = this.background;
            decoration.fontFamily  = this.fontFamily;
            decoration.fontSize    = this.fontSize;
            decoration.fontWeight  = this.fontWeight;
            decoration.fontPosture = this.fontPosture;
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
                "]}";
    }
}






