package com.gluonhq.richtext.model;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.Optional;

public class TextDecoration {

    public static TextDecoration DEFAULT = builder()
            .foreground(Color.BLUE)
            .font( Font.font( "Consolas", 15 ))
            .build();

    private Color foreground;
    private Color background;
    private Font font;

    private TextDecoration() {}

    public Text asText( String content ) {
        Text text = new javafx.scene.text.Text(content);
        Optional.ofNullable(foreground).ifPresent(text::setStroke);
        Optional.ofNullable(background).ifPresent(text::setFill);
        Optional.ofNullable(font).ifPresentOrElse(text::setFont, () -> text.setFont(DEFAULT.font) );
        return text;
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






