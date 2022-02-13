package com.gluonhq.richtext.model;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.util.Objects;

public class TextDecoration {

    public static TextDecoration DEFAULT = builder()
            .foreground(Color.BLUE)
            .background(Color.BLUE)
            .build();

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

    public Font getFont() {
        return Font.font( fontFamily, fontWeight, fontPosture, fontSize);
    }

    public static Builder builder() {
        return new Builder();
    }

    static class Builder {

        private Color foreground;
        private Color background;
        private String fontFamily = "Arial";
        private double fontSize = 17.0;
        private FontPosture fontPosture = FontPosture.REGULAR;
        private FontWeight fontWeight = FontWeight.MEDIUM;

        private Builder() {}

        TextDecoration build() {
            TextDecoration decoration = new TextDecoration();
            decoration.foreground = this.foreground;
            decoration.background = this.background;
            decoration.fontFamily = this.fontFamily;
            decoration.fontSize   = this.fontSize;
            decoration.fontWeight = this.fontWeight;
            decoration.fontPosture = this.fontPosture;
            return decoration;
        }

        Builder foreground(Color color) {
            this.foreground = color;
            return this;
        }

        Builder background(Color color) {
            this.background = color; 
            return this;
        }

        Builder fontFamily(String fontFamily) {
            this.fontFamily = Objects.requireNonNull(fontFamily);
            return this;
        }

        Builder fontSize(double fontSize) {
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






