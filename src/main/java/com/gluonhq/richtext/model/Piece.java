package com.gluonhq.richtext.model;

import java.util.Objects;

import static javafx.scene.text.FontPosture.REGULAR;
import static javafx.scene.text.FontWeight.NORMAL;

public final class Piece {

    public enum BufferType {ORIGINAL, ADDITION}

    final PieceTable source;
    final BufferType bufferType;
    final int start;                // start position with the buffer
    final int length;               // text length
//    final int[] lineStops;          // array if line stop indexes
    final TextDecoration decoration;

    public Piece(final PieceTable source, final BufferType bufferType, final int start, final int length) {
        this(source, bufferType, start, length, TextDecoration.builder().build());
    }

    public Piece(final PieceTable source, final BufferType bufferType, final int start, final int length, TextDecoration decoration) {

        this.bufferType = bufferType;
        this.start = start;
        this.length = Math.max(length, 0);
        this.source = Objects.requireNonNull(source);
        this.decoration = decoration == null ? TextDecoration.builder().build() : decoration;


        // find all the line stops
//        if (length == 0) {
//            lineStops = new int[0];
//        } else {
//            String text = getText();
//            lineStops = IntStream
//                    .range(0, text.length())
//                    .filter(i -> text.charAt(i) == '\n')
//                    .toArray();
//        }
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

    public String getText() {
        String buffer = BufferType.ORIGINAL == bufferType ? source.originalText : source.additionBuffer;
        return length == 0 ? "" : buffer.substring(start, start + length);
    }

    public TextDecoration getDecoration() {
        return decoration;
    }

    Piece copy(int newStart, int newLength) {
        return copy(newStart, newLength, decoration);
    }

    Piece copy(int newStart, int newLength, TextDecoration textDecoration) {
        return new Piece(source, bufferType, newStart, newLength, textDecoration.normalize(decoration));
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
        return "Piece{ " + getText() +
                ", bufferType=" + bufferType +
                ", start=" + start +
                ", length=" + length +
                ", decoration=" + decoration +
                '}';
    }
}
