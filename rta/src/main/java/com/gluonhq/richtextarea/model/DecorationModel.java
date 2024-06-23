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

import java.io.Serializable;
import java.util.Objects;

/**
 * A DecorationModel contains the text and paragraph decorations for a fragment of text,
 * defined by a start position and a length.
 */
public class DecorationModel implements Serializable {
    private final int start;
    private final int length;
    private final Decoration decoration;    // TextDecoration or ImageDecoration
    private final ParagraphDecoration paragraphDecoration;


    public DecorationModel(int start, int length, Decoration decoration, ParagraphDecoration paragraphDecoration) {
        this.start = start;
        this.length = length;
        this.decoration = decoration;
        this.paragraphDecoration = paragraphDecoration;
    }

    /**
     * Returns the start position of the fragment that has this decoration model
     *
     * @return the start position of the fragment
     */
    public int getStart() {
        return start;
    }

    /**
     * Returns the length of the fragment that has this decoration model
     *
     * @return the length of the fragment
     */
    public int getLength() {
        return length;
    }

    /**
     * Returns the decoration for the fragment. It can be a {@link TextDecoration} like font or color,
     * or an {@link ImageDecoration}, which inserts an image at the start of the fragment
     *
     * @return the decoration for the fragment
     */
    public Decoration getDecoration() {
        return decoration;
    }

    /**
     * Returns the {@link ParagraphDecoration} for the fragment, that is used to style the paragraph that contains
     * such fragment.
     *
     * @return the paragraph decoration for the fragment
     */
    public ParagraphDecoration getParagraphDecoration() {
        return paragraphDecoration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DecorationModel that = (DecorationModel) o;
        return start == that.start && length == that.length &&
                Objects.equals(decoration, that.decoration) &&
                Objects.equals(paragraphDecoration, that.paragraphDecoration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, length, decoration, paragraphDecoration);
    }

    @Override
    public String toString() {
        return "DecorationModel{" +
                "start=" + start +
                ", length=" + length +
                ", decoration=" + decoration +
                ", paragraphDecoration=" + paragraphDecoration +
                '}';
    }

    public static DecorationModel createDefaultDecorationModel(int length) {
        return new DecorationModel(0, length,
                TextDecoration.builder().presets().build(),
                ParagraphDecoration.builder().presets().build());
    }
}
