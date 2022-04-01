package com.gluonhq.richtext.model;

import javafx.scene.paint.Color;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.util.Objects;

import static com.gluonhq.richtext.Tools.getFirstLetter;

public class TextDecoration implements Decoration {

    private Color foreground;
    private Color background;
    private String fontFamily;
    private double fontSize;
    private FontPosture fontPosture;
    private FontWeight fontWeight;
    private Boolean strikethrough;
    private Boolean underline;
    private String url;

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

    public Boolean isStrikethrough() {
        return strikethrough != null && strikethrough;
    }

    public Boolean isUnderline() {
        return underline != null && underline;
    }

    public String getURL() {
        return url;
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
        td.foreground  = Objects.requireNonNullElse(foreground, decoration.foreground);
        td.background  = Objects.requireNonNullElse(background, decoration.background);
        td.fontFamily  = Objects.requireNonNullElse(fontFamily, decoration.fontFamily);
        td.fontSize    = this.fontSize < 1.0 ? decoration.fontSize : this.fontSize;
        td.fontWeight  = Objects.requireNonNullElse(fontWeight, decoration.fontWeight);
        td.fontPosture = Objects.requireNonNullElse(fontPosture, decoration.fontPosture);
        td.strikethrough = Objects.requireNonNullElse(strikethrough, decoration.strikethrough);
        td.underline = Objects.requireNonNullElse(underline, decoration.underline);
        td.url = url == null ? decoration.url : url;
        return td;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TextDecoration that = (TextDecoration) o;
        return Double.compare(that.fontSize, fontSize) == 0 &&
                Objects.equals(foreground, that.foreground) &&
                Objects.equals(background, that.background) &&
                Objects.equals(fontFamily, that.fontFamily) &&
                fontPosture == that.fontPosture &&
                fontWeight == that.fontWeight &&
                Objects.equals(strikethrough, that.strikethrough) &&
                Objects.equals(underline, that.underline) &&
                Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(foreground, background, fontFamily, fontSize, fontPosture, fontWeight, strikethrough, underline, url);
    }

    public static class Builder {

        private Color foreground;
        private Color background;
        private String fontFamily;
        private double fontSize;
        private FontPosture fontPosture;
        private FontWeight fontWeight;
        private Boolean strikethrough;
        private Boolean underline;
        private String url;

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
            decoration.url = this.url;
            return decoration;
        }

        public Builder presets() {
            foreground = Color.BLACK;
            background = Color.TRANSPARENT;
            fontFamily = "Serif";
            fontSize = 12.0;
            fontPosture = FontPosture.REGULAR;
            fontWeight = FontWeight.NORMAL;
            strikethrough = false;
            underline = false;
            url = null;
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
            url = decoration.url;
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

        public Builder strikethrough(Boolean strikethrough) {
            this.strikethrough = strikethrough;
            return this;
        }

        public Builder underline(Boolean underline) {
            this.underline = underline;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }
    }

    @Override
    public String toString() {
        return "TDec{" +
                "fcolor=" + foreground +
                ", bcolor=" + background +
                ", font['" + fontFamily + '\'' +
                ", " + fontSize +
                ", " + (fontPosture != null ? getFirstLetter(fontPosture.name()) : "-") +
                ", " + (fontWeight != null ? getFirstLetter(fontWeight.name()) : "-") +
                "]" +
                ", S:" + strikethrough +
                ", U:" + underline +
                ", URL:" + url +
                "}";
    }
}






