package com.gluonhq.richtext.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.gluonhq.richtext.model.Piece.BufferType.*;

/**
 * Piece table implementation.<br>
 * More info at  https://en.wikipedia.org/wiki/Piece_table
 */
public class PieceTable extends  AbstractTextBuffer {

    final String originalText;
    String additionBuffer = "";

    private final List<Piece> pieces = new ArrayList<>();

    private int textLength = -1;

    /**
     * Creates piece table using original text
     * @param originalText text to start with
     */
    public PieceTable( String originalText ) {
        this.originalText = Objects.requireNonNull(originalText);
        pieces.add( piece(ORIGINAL, 0, originalText.length()));
    }

    private Piece piece(Piece.BufferType bufferType, int start, int length ) {
        return new Piece( this, bufferType, start, length );
    }


    /**
     * Returns full text length.
     * Efficient enough as the internal value is cached and updated on each operation
     * @return full text length
     */
    @Override
    public int getTextLength() {
        if ( textLength < 0 ) {
           textLength = pieces.stream().mapToInt(b -> b.length).sum();
        }
        return textLength;
    }

    /**
     * Returns full text.
     * This is a costly operation as it walks through all the pieces
     * @return full text
     */
    @Override
    public String getText() {
        StringBuilder sb = new StringBuilder();
        pieces.forEach(piece -> sb.append(piece.getText()));
        return sb.toString();
    }

    // internal append
    Piece appendText(String text) {
        int pos = additionBuffer.length();
        additionBuffer += text;
        textLength = getTextLength() + text.length();
        return piece(ADDITION, pos, text.length());
    }

    /**
     * Appends text
     * @param text new text
     */
    @Override
    public void append( String text ) {
        Objects.requireNonNull(text);
        if (!text.isEmpty()) {
            int pos = additionBuffer.length();
            pieces.add(appendText(text));
            fireInsert(text, pos);
        }
    }

    @FunctionalInterface
    private interface WalkStep {
        // process step, return true of walk has to be interrupted
        boolean process(final Piece piece, final int textPosition, final int pieceIndex);
    }

    // Walks through pieces. Returns true if process was interrupted
    private void walkPieces(WalkStep iteration) {
        int textPosition = 0;
        int pieceIndex = 0;
        for (Piece piece : pieces) {
            if (iteration.process(piece, textPosition, pieceIndex)) {
                return;
            }
            textPosition += piece.length;
            pieceIndex++;
        }
    }

    /**
     * Inserts text at insertPosition
     * @param text to insert
     * @param insertPosition to insert text at
     * @throws IllegalArgumentException if insertPosition is not valid
     */
    @Override
    public void insert( String text, int insertPosition ) {

        Objects.requireNonNull(text);

        if ( text.isEmpty() ) {
            return; // no need to insert empty text
        }

        if ( insertPosition < 0 || insertPosition > getTextLength() ) {
            throw new IllegalArgumentException("Position is outside text bounds");
        }

        if (insertPosition == getTextLength()) {
            append(text);
        } else {
            walkPieces(( piece, textPosition, pieceIndex) -> {
                if ( inRange(insertPosition, textPosition, piece.length)) {
                    int pieceOffset = insertPosition-textPosition;
                    pieces.addAll( pieceIndex,
                        normalize( List.of(
                            piece.pieceBefore(pieceOffset),
                            appendText(text),
                            piece.pieceFrom(pieceOffset)
                        ))
                    );
                    pieces.remove(piece);
                    fireInsert(text, insertPosition);
                    return true;
                }
                return false;
            });
        }

    }

    /**
     * Deletes text with 'length' starting at 'deletePosition'
     * @param deletePosition deletePosition to start deletion from
     * @param length length of text to delete
     * @throws IllegalArgumentException if deletePosition is not valid
     */
    @Override
    public void delete( final int deletePosition, int length ) {

        if ( !inRange(deletePosition, 0, getTextLength())) {
            throw new IllegalArgumentException("Position is outside of text bounds");
        }

        //  Accept length larger than actual and adjust it to actual
        if ( (deletePosition + length) >= getTextLength() ) {
            length = getTextLength()-deletePosition;
        }

        int endPosition = deletePosition+length;

        final int[] startPieceIndex = new int[1];
        final List<Piece> additions = new ArrayList<>(); // start and end pieces
        final List<Piece> removals  = new ArrayList<>();

        walkPieces( (piece, textPosition, pieceIndex) -> {

            if ( inRange( deletePosition, textPosition, piece.length) ) {
                int pieceOffset = deletePosition-textPosition;
                startPieceIndex[0] = pieceIndex;
                additions.add(piece.pieceBefore(pieceOffset));
                removals.add(piece);
            }

            if (!additions.isEmpty()) {
                removals.add(piece);
                if (inRange( endPosition, textPosition, piece.length)) {
                    additions.add(piece.pieceFrom(endPosition-textPosition));
                    return true;
                }
            }
            return false;
        });

        Collection<Piece> newPieces = normalize(additions);
        if (newPieces.size() > 0 || removals.size() > 0) { // split actually happened
            this.pieces.addAll(startPieceIndex[0], newPieces );
            this.pieces.removeAll(removals);
            textLength = getTextLength() - length;
            fireDelete(deletePosition, length);
        }

    }

    // Normalized list of pieces
    // Empty pieces purged
    private static Collection<Piece> normalize(Collection<Piece> pieces) {
        return Objects.requireNonNull(pieces)
                  .stream()
                  .filter(b -> b == null ||  !b.isEmpty())
                  .collect(Collectors.toList());

    }

    // TODO is there standard APIs?
    private static boolean inRange( int index, int start, int length ) {
        return index >= start && index < start+length;
    }

}

class Piece {

    enum BufferType { ORIGINAL, ADDITION }

    final PieceTable source;
//    final boolean isOriginalBuffer; // which buffer the text is in
    final BufferType bufferType;
    final int start;                // start position with the buffer
    final int length;               // text length
    final int[] lineStops;          // array if line stop indexes

    public Piece(PieceTable source, final BufferType bufferType, final int start, final int length) {

        this.bufferType = bufferType;
        this.start  = start;
        this.length = Math.max(length, 0);
        this.source = Objects.requireNonNull(source);

        // find all the line stops
        if ( length == 0 ) {
            lineStops = new int[0];
        } else {
            String text = getText();
            lineStops = IntStream
                    .range(0, text.length())
                    .filter(i -> text.charAt(i) == '\n')
                    .toArray();
        }
    }

    public boolean isEmpty() {
        return length <= 0;
    }

    // validate offset within piece
    protected void validateOffset(int offset ) {
        if ( offset < 0 || offset >= length ) {
            throw new IllegalArgumentException(
                    String.format("Piece offset (%d) is not in range (%d,%d)", offset, 0, length ));
        }
    }

    public String getText() {
        String buffer = ORIGINAL == bufferType ? source.originalText : source.additionBuffer;
        return  length == 0? "": buffer.substring(start, start+length);
    }

    private Piece copy( int newStart, int newLength ) {
        return new Piece(source, bufferType, newStart, newLength);
    }

    // excludes char at offset
    public Piece pieceBefore(int offset) {
        validateOffset(offset);
        return copy(start,offset);
    }

    // includes char at offset
    public Piece pieceFrom(int offset) {
        validateOffset(offset);
        return copy(start+offset, length-offset);
    }

}


