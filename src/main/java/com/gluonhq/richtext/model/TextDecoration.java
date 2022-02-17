package com.gluonhq.richtext.model;

import javafx.scene.paint.Color;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.util.Objects;

import static javafx.scene.text.FontPosture.REGULAR;
import static javafx.scene.text.FontWeight.NORMAL;

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
        Builder copy = Builder.copy(this);
        if (fontWeight == decoration.getFontWeight()) {
            copy.fontWeight = NORMAL;
        }
        if (fontPosture == decoration.getFontPosture()) {
            copy.fontPosture = REGULAR;
        }
        return copy.build();
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

        /**
         * Returns a new instance of builder by copying the properties from the supplied decoration
         * @param decoration TextDecoration to copy properties from
         * @return An instance of TextDecoration.Builder
         */
        private static Builder copy(TextDecoration decoration) {
            return TextDecoration.builder()
                    .foreground(decoration.getForeground())
                    .background(decoration.getBackground())
                    .fontFamily(decoration.getFontFamily())
                    .fontSize(decoration.getFontSize())
                    .fontPosture(decoration.getFontPosture())
                    .fontWeight(decoration.getFontWeight());
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

        public Builder fontWeight(FontWeight fontWeight) {
            this.fontWeight = Objects.requireNonNull(fontWeight);
            return this;
        }

        public Builder fontPosture(FontPosture fontPosture) {
            this.fontPosture = Objects.requireNonNull(fontPosture);
            return this;
        }
    }

}






