/*
 * Copyright (c) 2022, Gluon
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

import javafx.geometry.Insets;
import javafx.scene.text.TextAlignment;

import java.util.Objects;

/**
 * ParagraphDecoration is a {@link Decoration} that can be applied to a fragment of text in order to style
 * the paragraph that contains such fragment.
 */
public class ParagraphDecoration implements Decoration {

    /**
     * The type of graphic applied to the left of the paragraph
     */
    public enum GraphicType {
        /**
         * By default, no graphic
         */
        NONE,
        /**
         * The graphic is a {@link javafx.scene.control.Label} with a number, and possibly some formatting,
         * that is used to define a numbered list.
         * See {@link com.gluonhq.richtextarea.RichTextArea#paragraphGraphicFactoryProperty()}
         */
        NUMBERED_LIST,
        /**
         * The graphic is a {@link javafx.scene.Node} that is used to define a bulleted list.
         * See {@link com.gluonhq.richtextarea.RichTextArea#paragraphGraphicFactoryProperty()}
         */
        BULLETED_LIST
    }

    private Double spacing;
    private TextAlignment alignment;
    private Double topInset, rightInset, bottomInset, leftInset;
    private int indentationLevel = -1;
    private GraphicType graphicType;

    private TableDecoration tableDecoration;

    private ParagraphDecoration() {}

    /**
     * Returns the line spacing of the paragraph
     *
     * @defaultValue 0
     *
     * @return the line spacing
     */
    public double getSpacing() {
        return spacing;
    }

    /**
     * Returns the alignment of the paragraph
     *
     * @defaultValue {@link TextAlignment#LEFT}
     *
     * @return the paragraph alignment
     */
    public TextAlignment getAlignment() {
        return alignment;
    }

    /**
     * Returns the top padding of the paragraph
     *
     * @defaultValue 0
     *
     * @return the top padding
     */
    public double getTopInset() {
        return topInset;
    }

    /**
     * Returns the right padding of the paragraph
     *
     * @defaultValue 0
     *
     * @return the right padding
     */
    public double getRightInset() {
        return rightInset;
    }

    /**
     * Returns the bottom padding of the paragraph
     *
     * @defaultValue 0
     *
     * @return the bottom padding
     */
    public double getBottomInset() {
        return bottomInset;
    }

    /**
     * Returns the left padding of the paragraph
     *
     * @defaultValue 0
     *
     * @return the left padding
     */
    public double getLeftInset() {
        return leftInset;
    }

    /**
     * Returns the level of indentation of the paragraph
     *
     * @defaultValue 0
     *
     * @return the level of indentation
     */
    public int getIndentationLevel() {
        return indentationLevel;
    }

    /**
     * Returns the type of graphic of the paragraph, if any
     *
     * @defaultValue {@link GraphicType#NONE}
     *
     * @return the {@link GraphicType} used for the paragraph
     */
    public GraphicType getGraphicType() {
        return graphicType;
    }

    /**
     * Returns true if the paragraph decoration has a {@link TableDecoration}
     *
     * @return true if the paragraph has a table
     */
    public boolean hasTableDecoration() {
        return tableDecoration != null && tableDecoration.getRows() > 0 && tableDecoration.getColumns() > 0;
    }

    /**
     * Returns the {@link TableDecoration} for this paragraph
     *
     * @return the {@link TableDecoration} for this paragraph
     */
    public TableDecoration getTableDecoration() {
        return tableDecoration;
    }

    /**
     * Returns a Builder that can be used to generate paragraph decorations with several
     * attributes
     *
     * @return a {@link ParagraphDecoration.Builder}
     */
    public static ParagraphDecoration.Builder builder() {
        return new ParagraphDecoration.Builder();
    }

    /**
     * Returns a new instance of the current ParagraphDecoration
     * and normalizes properties based on the supplied decoration.
     * @param decoration ParagraphDecoration to compare and normalize
     * @return New ParagraphDecoration instance with properties normalized
     */
    public ParagraphDecoration normalize(ParagraphDecoration decoration) {
        if (decoration == null) {
            return this;
        }

        ParagraphDecoration pd = new ParagraphDecoration();
        pd.spacing = Objects.requireNonNullElse(spacing, decoration.spacing);
        pd.alignment = Objects.requireNonNullElse(alignment, decoration.alignment);
        pd.topInset = Objects.requireNonNullElse(topInset, decoration.topInset);
        pd.rightInset = Objects.requireNonNullElse(rightInset, decoration.rightInset);
        pd.bottomInset = Objects.requireNonNullElse(bottomInset, decoration.bottomInset);
        pd.leftInset = Objects.requireNonNullElse(leftInset, decoration.leftInset);
        pd.indentationLevel = indentationLevel < 0 ? decoration.indentationLevel : indentationLevel;
        pd.graphicType = Objects.requireNonNullElse(graphicType, decoration.graphicType);
        pd.tableDecoration = Objects.requireNonNullElse(tableDecoration, decoration.tableDecoration);
        return pd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParagraphDecoration that = (ParagraphDecoration) o;
        return Double.compare(that.spacing, spacing) == 0 &&
                Double.compare(that.topInset, topInset) == 0 &&
                Double.compare(that.rightInset, rightInset) == 0 &&
                Double.compare(that.bottomInset, bottomInset) == 0 &&
                Double.compare(that.leftInset, leftInset) == 0 &&
                that.indentationLevel == indentationLevel &&
                that.graphicType == graphicType &&
                that.alignment == alignment &&
                Objects.equals(that.tableDecoration, tableDecoration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(spacing, alignment, topInset, rightInset, bottomInset, leftInset, indentationLevel, graphicType, tableDecoration);
    }

    @Override
    public String toString() {
        return "PDec{" +
                "s=" + spacing +
                ", a=" + alignment +
                ", [" + topInset + ", " + rightInset + ", " + bottomInset + ", " + leftInset + "]" +
                ", i=" + indentationLevel + ", type=" + graphicType + ", " + tableDecoration + "}";
    }

    public static class Builder {
        private Double spacing;
        private TextAlignment alignment;
        private Double topInset, rightInset, bottomInset, leftInset;
        private int indentationLevel = -1;
        private GraphicType graphicType;
        private TableDecoration tableDecoration;

        private Builder() {}

        public ParagraphDecoration build() {
            ParagraphDecoration decoration = new ParagraphDecoration();
            decoration.spacing = this.spacing;
            decoration.alignment = this.alignment;
            decoration.topInset = this.topInset;
            decoration.rightInset = this.rightInset;
            decoration.bottomInset = this.bottomInset;
            decoration.leftInset = this.leftInset;
            decoration.indentationLevel = this.indentationLevel;
            decoration.graphicType = this.graphicType;
            decoration.tableDecoration = tableDecoration;
            return decoration;
        }

        public Builder presets() {
            spacing = 0d;
            alignment = TextAlignment.LEFT;
            topInset = 0d;
            rightInset = 0d;
            bottomInset = 0d;
            leftInset = 0d;
            indentationLevel = 0;
            graphicType = GraphicType.NONE;
            tableDecoration = new TableDecoration();
            return this;
        }

        public Builder fromDecoration(ParagraphDecoration decoration) {
            this.spacing = decoration.spacing;
            this.alignment = decoration.alignment;
            this.topInset = decoration.topInset;
            this.rightInset = decoration.rightInset;
            this.bottomInset = decoration.bottomInset;
            this.leftInset = decoration.leftInset;
            this.indentationLevel = decoration.indentationLevel;
            this.graphicType = decoration.graphicType;
            this.tableDecoration = decoration.tableDecoration;
            return this;
        }

        public Builder spacing(double spacing) {
            this.spacing = spacing;
            return this;
        }

        public Builder alignment(TextAlignment alignment) {
            this.alignment = Objects.requireNonNull(alignment);
            return this;
        }

        public Builder insets(double topInset, double rightInset, double bottomInset, double leftInset) {
            this.topInset = topInset;
            this.rightInset = rightInset;
            this.bottomInset = bottomInset;
            this.leftInset = leftInset;
            return this;
        }

        public Builder insets(Insets insets) {
            this.topInset = Objects.requireNonNull(insets).getTop();
            this.rightInset = insets.getRight();
            this.bottomInset = insets.getBottom();
            this.leftInset = insets.getLeft();
            return this;
        }

        public Builder topInset(double topInset) {
            this.topInset = topInset;
            return this;
        }

        public Builder rightInset(double rightInset) {
            this.rightInset = rightInset;
            return this;
        }

        public Builder bottomInset(double bottomInset) {
            this.bottomInset = bottomInset;
            return this;
        }

        public Builder leftInset(double leftInset) {
            this.leftInset = leftInset;
            return this;
        }

        public Builder indentationLevel(int indentationLevel) {
            this.indentationLevel = indentationLevel;
            this.graphicType = indentationLevel == 0 ? GraphicType.NONE : graphicType;
            return this;
        }

        public Builder graphicType(GraphicType graphicType) {
            if (graphicType == GraphicType.NONE) {
                // removing graphic type, moves one level back
                this.indentationLevel = Math.max(0, indentationLevel - 1);
            } else {
                // applying graphic type requires at least level one
                this.indentationLevel = Math.max(1, indentationLevel);
            }
            this.graphicType = graphicType;
            return this;
        }

        public Builder tableDecoration(TableDecoration tableDecoration) {
            this.tableDecoration = tableDecoration;
            return this;
        }
    }
}
