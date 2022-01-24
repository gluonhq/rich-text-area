package com.gluonhq.richtext.model;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class TextDecoration {

    public static TextDecoration DEFAULT = builder()
            .foreground(Color.BLACK)
            .font( Font.font( "Consolas", FontWeight.NORMAL, 15 ))
            .build();

    private Color foreground;
    private Color background;
    private Font font;

    private TextDecoration() {}

    public Color getForeground() {
        return foreground;
    }

    public Color getBackground() {
        return background;
    }

    public Font getFont() {
        return font;
    }

    public static Builder builder() {
        return new Builder();
    }

    static class Builder {

        private Color foreground;
        private Color background;
        private Font font;

        private Builder() {}

        TextDecoration build() {
            TextDecoration decoration = new TextDecoration();
            decoration.foreground = this.foreground;
            decoration.background = this.background;
            decoration.font = this.font;
            return decoration;
        }

        Builder foreground( Color color ) {
            this.foreground = color;
            return this;
        }

        Builder background( Color color ) {
            this.background = color;
            return this;
        }

        Builder font( Font font ) {
            this.font = font;
            return this;
        }
    }

}






