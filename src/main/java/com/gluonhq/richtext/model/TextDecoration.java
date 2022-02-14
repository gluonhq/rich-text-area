package com.gluonhq.richtext.model;

import javafx.scene.paint.Color;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.util.Objects;

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

    public static class Builder {

        private Color foreground = Color.BLUE;
        private Color background = Color.BLUE;
        private String fontFamily = "Arial";
        private double fontSize = 17.0;
        private FontPosture fontPosture = FontPosture.REGULAR;
        private FontWeight fontWeight = FontWeight.MEDIUM;

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

        Builder foreground(Color color) {
            this.foreground = Objects.requireNonNull(color);
            return this;
        }

        Builder background(Color color) {
            this.background = Objects.requireNonNull(color);
            return this;
        }

        Builder fontFamily(String fontFamily) {
            this.fontFamily = Objects.requireNonNull(fontFamily);
            return this;
        }

        public Builder fontSize(double fontSize) {
            this.fontSize = fontSize;
            return this;
        }

        Builder fontWeight(FontWeight fontWeight) {
            this.fontWeight = Objects.requireNonNull(fontWeight);
            return this;
        }

        Builder fontPosture(FontPosture fontPosture) {
            this.fontPosture = Objects.requireNonNull(fontPosture);
            return this;
        }

    }

}






