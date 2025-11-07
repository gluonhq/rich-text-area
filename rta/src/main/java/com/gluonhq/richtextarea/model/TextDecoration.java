/*
 * Copyright (c) 2022, 2024, Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GLUON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.richtextarea.model;

import javafx.scene.paint.Color;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.util.Objects;

import static com.gluonhq.richtextarea.RichTextAreaSkin.DEFAULT_FONT_SIZE;
import static com.gluonhq.richtextarea.Tools.getFirstLetter;

/**
 * TextDecoration is a {@link Decoration} that can be applied to a fragment of text in order to generate
 * rich text, that is, styled text with different text attributes like color of font.
 */
public class TextDecoration implements Decoration {

    private String foreground;
    private String background;
    private String fontFamily;
    private double fontSize;
    private FontPosture fontPosture;
    private FontWeight fontWeight;
    private Boolean strikethrough;
    private Boolean underline;
    private String url;

    private TextDecoration() {}

    /**
     * Gets the foreground color of the text.
     * Any string value that can be parsed with {@link Color#web(String)}
     *
     * @defaultValue #000000, black
     *
     * @return the foreground color of the text
     */
    public String getForeground() {
        return foreground;
    }

    /**
     * Gets the background color of the text.
     * Any string value that can be parsed with {@link Color#web(String)}
     *
     * @defaultValue #00000000, transparent
     *
     * @return the background color of the text
     */
    public String getBackground() {
        return background;
    }

    /**
     * Gets the font size of the text.
     *
     * @defaultValue {@link com.gluonhq.richtextarea.RichTextAreaSkin#DEFAULT_FONT_SIZE}
     *
     * @return the font size of the text
     */
    public double getFontSize() {
        return fontSize;
    }

    /**
     * Gets the font family of the text.
     *
     * @defaultValue System
     *
     * @return the font family of the text
     */
    public String getFontFamily() {
        return fontFamily;
    }

    /**
     * Gets the font posture of the text.
     *
     * @defaultValue {@link FontPosture#REGULAR}
     *
     * @return the font posture of the text
     */
    public FontPosture getFontPosture() {
        return fontPosture;
    }

    /**
     * Gets the font weight of the text.
     *
     * @defaultValue {@link FontWeight#NORMAL}
     *
     * @return the font weight of the text
     */
    public FontWeight getFontWeight() {
        return fontWeight;
    }

    /**
     * Gets if the text has strike-through formatting.
     *
     * @defaultValue false
     *
     * @return if strike-through formatting is applied
     */
    public Boolean isStrikethrough() {
        return strikethrough != null && strikethrough;
    }

    /**
     * Gets if the text has underline formatting.
     *
     * @defaultValue false
     *
     * @return if underline formatting is applied
     */
    public Boolean isUnderline() {
        return underline != null && underline;
    }

    /**
     * Gets a string with a URL that can be used to decorate a fragment of text as a hyperlink
     *
     * @defaultValue null
     *
     * @return a string with the URL of a link or null
     */
    public String getURL() {
        return url;
    }

    /**
     * Returns a Builder that can be used to generate text decorations with several
     * attributes
     *
     * @return a {@link TextDecoration.Builder}
     */
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

        private String foreground;
        private String background;
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
            foreground = "black";
            background = "transparent";
            fontFamily = "System";
            fontSize = DEFAULT_FONT_SIZE;
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

        public Builder foreground(String color) {
            this.foreground = Objects.requireNonNull(color);
            return this;
        }

        public Builder background(String color) {
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






