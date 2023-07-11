/*
 * Copyright (c) 2022, 2023, Gluon
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

import com.gluonhq.richtextarea.Tools;

import java.util.Objects;

import static com.gluonhq.richtextarea.Tools.getFirstLetter;

public final class Piece {

    public enum BufferType {ORIGINAL, ADDITION}

    final PieceTable source;
    final BufferType bufferType;
    final int start;                // start position with the buffer
    final int length;               // text length
    final Decoration decoration;    // the piece can contain only a single TextDecoration or ImageDecoration
    final ParagraphDecoration paragraphDecoration; // the piece can contain only a single ParagraphDecoration,
                                                   // but it can contain zero, one or more line feed characters

    public Piece(final PieceTable source, final BufferType bufferType, final int start, final int length) {
        this(source, bufferType, start, length, null, null);
    }

    public Piece(final PieceTable source, final BufferType bufferType, final int start, final int length, Decoration decoration) {
        this(source, bufferType, start, length, decoration, null);
    }

    public Piece(final PieceTable source, final BufferType bufferType, final int start, final int length, Decoration decoration, ParagraphDecoration paragraphDecoration) {
        this.bufferType = bufferType;
        this.start = start;
        this.length = Math.max(length, 0);
        this.source = Objects.requireNonNull(source);
        this.decoration = decoration == null ? TextDecoration.builder().presets().build() : decoration;
        this.paragraphDecoration = paragraphDecoration;
    }

    public boolean isEmpty() {
        return length <= 0;
    }

    // validate offset within piece
    public void validateOffset(int offset) {
        if (offset < 0 || offset >= length) {
            throw new IllegalArgumentException(
                    String.format("Piece offset (%d) is not in range (%d,%d)", offset, 0, length));
        }
    }

    public String getInternalText() {
        UnitBuffer buffer = BufferType.ORIGINAL == bufferType ? source.originalText : source.additionBuffer;
        return length == 0 ? "" : buffer.getInternalText().substring(start, start + length);
    }

    public Unit getUnit() {
        UnitBuffer buffer = BufferType.ORIGINAL == bufferType ? source.originalText : source.additionBuffer;
        return length == 0 ? new TextUnit("") : buffer.getUnitWithRange(start, start + length);
    }

    public Decoration getDecoration() {
        return decoration;
    }

    public ParagraphDecoration getParagraphDecoration() {
        return paragraphDecoration;
    }

    Piece copy(int newStart, int newLength) {
        return new Piece(source, bufferType, newStart, newLength, decoration, paragraphDecoration);
    }

    Piece copy(int newStart, int newLength, Decoration newDecoration) {
        if (decoration instanceof TextDecoration) {
            return new Piece(source, bufferType, newStart, newLength,
                    newDecoration instanceof TextDecoration ?
                            ((TextDecoration) newDecoration).normalize((TextDecoration) decoration) : newDecoration, paragraphDecoration);
        } else {
            return new Piece(source, bufferType, newStart, newLength, decoration, paragraphDecoration);
        }
    }

    Piece copy(int newStart, int newLength, Decoration decoration, ParagraphDecoration newParagraphDecoration) {
        return new Piece(source, bufferType, newStart, newLength, decoration, newParagraphDecoration.normalize(paragraphDecoration));
    }

    // excludes char at offset
    public Piece pieceBefore(int offset) {
        validateOffset(offset);
        return copy(start, offset);
    }

    // includes char at offset
    public Piece pieceFrom(int offset) {
        validateOffset(offset);
        return copy(start + offset, length - offset);
    }

    @Override
    public String toString() {
        return "Piece{type=" + getFirstLetter(bufferType.name()) +
                ", [" + start + ", " + length + "], " + decoration +
                ", " + paragraphDecoration +
                ", \"" + Tools.formatTextWithAnchors(getInternalText()) + "\"}";
    }
}
