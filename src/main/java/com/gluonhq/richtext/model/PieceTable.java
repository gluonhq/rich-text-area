package com.gluonhq.richtext.model;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Piece table implementation.<br>
 * More info at  https://en.wikipedia.org/wiki/Piece_table
 */
public final class PieceTable extends AbstractTextBuffer {

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
        pieces.add( piece( Piece.BufferType.ORIGINAL, 0, originalText.length()));
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

    public List<Piece> getPieces() {
        return Collections.unmodifiableList(pieces);
    }

    // internal append
    Piece appendTextInternal(String text) {
        int pos = additionBuffer.length();
        additionBuffer += text;
        textLength = getTextLength() + text.length();
        return piece(Piece.BufferType.ADDITION, pos, text.length());
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
            pieces.add(appendTextInternal(text));
            fireInsert(text, pos);
        }
    }

    @Override
    public void walkFragments(BiConsumer<String, TextDecoration> onFragment) {
        for (Piece piece : getPieces()) {
            onFragment.accept( piece.getText(), piece.getDecoration());
        }
    }

    @FunctionalInterface
    private interface WalkStep {
        // process step, return true of walk has to be interrupted
        boolean process(final Piece piece, final int textPosition, final int pieceIndex);
    }

    // Walks through pieces. Returns true if process was interrupted
    private void walkPieces(WalkStep step) {
        int textPosition = 0;
        int pieceIndex = 0;
        for (Piece piece : pieces) {
            if (step.process(piece, textPosition, pieceIndex)) {
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
                            appendTextInternal(text),
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
            //throw new IllegalArgumentException("Position is outside of text bounds");
            return;
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


