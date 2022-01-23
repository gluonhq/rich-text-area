package com.gluonhq.richtext.decoration;

import javafx.scene.paint.Color;

public class TextDecoration {

    public static TextDecoration DEFAULT = builder().foreground(Color.BLACK).build();

    private Color foreground;
    private Color background;

    private TextDecoration() {}

    public Color getForeground() {
        return foreground;
    }

    public Color getBackground() {
        return background;
    }

    public static Builder builder() {
        return new Builder();
    }

    static class Builder {

        private Color foreground;
        private Color background;

        private Builder() {}

        TextDecoration build() {
            TextDecoration decoration = new TextDecoration();
            decoration.foreground = this.foreground;
            decoration.background = this.background;
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
    }

}






