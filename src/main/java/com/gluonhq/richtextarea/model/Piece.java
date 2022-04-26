package com.gluonhq.richtextarea.model;

import java.util.Objects;

import static com.gluonhq.richtextarea.Tools.getFirstLetter;
import static com.gluonhq.richtextarea.model.TextBuffer.ZERO_WIDTH_TABLE_SEPARATOR;

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
        return length <= 0 && paragraphDecoration.getGraphicType() == ParagraphDecoration.GraphicType.NONE && paragraphDecoration.getIndentationLevel() == 0;
    }

    // validate offset within piece
    public void validateOffset(int offset) {
        if (offset < 0 || offset >= length) {
            throw new IllegalArgumentException(
                    String.format("Piece offset (%d) is not in range (%d,%d)", offset, 0, length));
        }
    }

    public String getText() {
        String buffer = BufferType.ORIGINAL == bufferType ? source.originalText : source.additionBuffer;
        return length == 0 ? "" : buffer.substring(start, start + length);
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
                ", [" + start +
                ", " + length +
                "], " + decoration +
                ", " + paragraphDecoration +
                ", \"" + getText().replaceAll("\n", "<n>")
                            .replaceAll(TextBuffer.ZERO_WIDTH_TEXT, "<a>")
                            .replaceAll("" + ZERO_WIDTH_TABLE_SEPARATOR, "<t>")+ "\"}";
    }
}
