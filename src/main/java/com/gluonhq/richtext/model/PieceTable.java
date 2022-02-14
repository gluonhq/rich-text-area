package com.gluonhq.richtext.model;

import com.gluonhq.richtext.undo.AbstractCommand;
import com.gluonhq.richtext.undo.CommandManager;

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

    final List<Piece> pieces = new ArrayList<>();
    private final CommandManager<PieceTable> commander = new CommandManager<>(this);

    int textLength = -1;

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
        commander.execute( new AppendCmd(text));
    }

    @Override
    public void decorate(int start, int end, TextDecoration textDecoration) {
        commander.execute(new TextDecorateCmd(start, end, textDecoration));
    }

    /**
     * Walks through text fragments. Each fragment is represented by related text and decoration
     * @param onFragment callback to get fragment info
     */
    @Override
    public void walkFragments(BiConsumer<String, TextDecoration> onFragment) {
        pieces.forEach( p -> onFragment.accept( p.getText(), p.getDecoration()));
    }


    /**
     * Inserts text at insertPosition
     * @param text to insert
     * @param insertPosition to insert text at
     * @throws IllegalArgumentException if insertPosition is not valid
     */
    @Override
    public void insert( final String text, final int insertPosition ) {
        commander.execute( new InsertCmd(text, insertPosition));
    }

    /**
     * Deletes text with 'length' starting at 'deletePosition'
     * @param deletePosition deletePosition to start deletion from
     * @param length length of text to delete
     * @throws IllegalArgumentException if deletePosition is not valid
     */
    @Override
    public void delete( final int deletePosition, int length ) {
        commander.execute( new DeleteCmd(deletePosition, length));
    }

    /**
     * Undo latest text modification
     */
    @Override
    public void undo() {
        commander.undo();
    }

    @Override
    public void redo() {
        commander.redo();
    }

    /**
     *        Piece Table
     *  Piece A  Piece B   Piece C
     *  |_____||_______||__________|
     *
     *  piece | pieceIndex | textPosition
     *    A   |     0      |     0
     *    B   |     1      |     5  (length of Piece A)
     *    C   |     2      |     12 (length of Piece A + Piece B)
     */
    @FunctionalInterface
    interface WalkStep {
        // process step, return true of walk has to be interrupted
        boolean process(final Piece piece, final int pieceIndex, final int textPosition);
    }

    // Walks through pieces. Returns true if process was interrupted
    void walkPieces(WalkStep step) {

        int textPosition = 0;
        for (int i = 0; i < pieces.size(); i++) {
            Piece piece = pieces.get(i);
            if (step.process(piece, i, textPosition)) {
                return;
            }
            textPosition += piece.length;
        }

    }

    // Normalized list of pieces
    // Empty pieces purged
    static Collection<Piece> normalize(Collection<Piece> pieces) {
        return Objects.requireNonNull(pieces)
                  .stream()
                  .filter(b -> b == null ||  !b.isEmpty())
                  .collect(Collectors.toList());

    }

    // TODO is there standard APIs?
    static boolean inRange( int index, int start, int length ) {
        return index >= start && index < start+length;
    }

}

abstract class AbstractPTCmd extends AbstractCommand<PieceTable> {}

class AppendCmd extends AbstractPTCmd {

    private final String text;
    private Piece newPiece;

    AppendCmd(String text) {
        this.text = Objects.requireNonNull(text);
    }

    @Override
    protected void doUndo(PieceTable pt) {
        pt.pieces.remove(newPiece);
        pt.fire( new TextBuffer.DeleteEvent(pt.getTextLength()-text.length(), text.length() ));
    }

    @Override
    protected void doRedo(PieceTable pt) {
        if (!text.isEmpty()) {
            int pos = pt.getTextLength();
            newPiece = pt.appendTextInternal(text);
            pt.pieces.add(newPiece);
            pt.fire( new TextBuffer.InsertEvent(text, pos));
        }
    }
}

class InsertCmd extends AbstractCommand<PieceTable> {

    private final String text;
    private final int insertPosition;

    private Collection<Piece> newPieces;
    private Piece oldPiece;
    private int opPieceIndex;
    private boolean execSuccess = false;

    InsertCmd( String text, int insertPosition )  {
        this.text = Objects.requireNonNull(text);
        this.insertPosition = insertPosition;
    }

    @Override
    protected void doUndo(PieceTable pt) {
        if (execSuccess) {
            pt.pieces.add(opPieceIndex, oldPiece);
            pt.pieces.removeAll(newPieces);
            pt.fire(new TextBuffer.DeleteEvent(insertPosition, text.length()));
        }
    }

    @Override
    protected void doRedo(PieceTable pt) {

        if ( text.isEmpty() ) {
            return; // no need to insert empty text
        }

        if ( insertPosition < 0 || insertPosition > pt.getTextLength() ) {
            throw new IllegalArgumentException("Position is outside text bounds");
        }

        if (insertPosition == pt.getTextLength()) {
            pt.append(text);
        } else {
            pt.walkPieces((piece, pieceIndex, textPosition) -> {
                if ( PieceTable.inRange(insertPosition, textPosition, piece.length)) {
                    int pieceOffset = insertPosition - textPosition;
                    newPieces = PieceTable.normalize( List.of(
                            piece.pieceBefore(pieceOffset),
                            pt.appendTextInternal(text),
                            piece.pieceFrom(pieceOffset)
                    ));
                    oldPiece = piece;
                    pt.pieces.addAll( pieceIndex, newPieces );
                    pt.pieces.remove(oldPiece);
                    opPieceIndex = pieceIndex;

                    pt.fire( new TextBuffer.InsertEvent(text, insertPosition));
                    execSuccess = true;
                    return true;
                }
                return false;
            });
        }
    }
}

class DeleteCmd extends AbstractCommand<PieceTable> {

    private final int deletePosition;
    private int length;

    private boolean execSuccess = false;
    private int pieceIndex = -1;
    private Collection<Piece> newPieces;
    private Collection<Piece> oldPieces;

    /**
     * Command to delete text starting from an index position to a given length.
     * @param deletePosition position from where delete operation is to be executed. Normally, this is position of the caret.
     * @param length Length of the text following the deletePosition to delete.
     */
    DeleteCmd(int deletePosition, int length) {
        this.deletePosition = deletePosition;
        this.length = length;
    }

    @Override
    protected void doUndo(PieceTable pt) {
        if (execSuccess) {
            pt.pieces.addAll(pieceIndex, oldPieces);
            pt.pieces.removeAll(newPieces);

            String text = oldPieces.stream()
              .map(Piece::getText)
              .reduce( "", (id, s) -> id + s );

            pt.fire(new TextBuffer.InsertEvent(text, deletePosition));
        }
    }

    @Override
    protected void doRedo(PieceTable pt) {

        if (!PieceTable.inRange(deletePosition, 0, pt.getTextLength())) {
            throw new IllegalArgumentException("Position is outside of text bounds");
        }

        //  Accept length larger than actual and adjust it to actual
        if ((deletePosition + length) >= pt.getTextLength()) {
            length = pt.getTextLength() - deletePosition;
        }

        int endPosition = deletePosition + length;

        final int[] startPieceIndex = new int[1];
        final List<Piece> additions = new ArrayList<>(); // start and end pieces
        final List<Piece> removals = new ArrayList<>();

        pt.walkPieces((piece, pieceIndex, textPosition) -> {

            if (PieceTable.inRange(deletePosition, textPosition, piece.length)) {
                int pieceOffset = deletePosition - textPosition;
                startPieceIndex[0] = pieceIndex;
                additions.add(piece.pieceBefore(pieceOffset));
                removals.add(piece);
            }

            if (!additions.isEmpty()) {
                removals.add(piece);
                if (PieceTable.inRange(endPosition, textPosition, piece.length)) {
                    additions.add(piece.pieceFrom(endPosition - textPosition));
                    return true;
                }
            }
            return false;
        });

        newPieces = PieceTable.normalize(additions);
        oldPieces = removals;
        if (newPieces.size() > 0 || oldPieces.size() > 0) { // split actually happened
            pieceIndex = startPieceIndex[0];
            pt.pieces.addAll(pieceIndex, newPieces);
            pt.pieces.removeAll(oldPieces);
            pt.textLength = pt.getTextLength() - length;
            pt.fire(new TextBuffer.DeleteEvent(deletePosition, length));
            execSuccess = true;
        }
    }

}

class TextDecorateCmd extends AbstractCommand<PieceTable> {

    private final int start;
    private int end;
    private final TextDecoration decoration;

    private boolean execSuccess = false;
    private int pieceIndex = -1;
    private Collection<Piece> newPieces;
    private Collection<Piece> oldPieces;

    /**
     * Command to delete text starting from an index position to a given length.
     * @param start index of the first character to be decorated.
     * @param end index of the last character to be decorated.
     */
    TextDecorateCmd(int start, int end, TextDecoration decoration) {
        this.start = start;
        this.end = end;
        this.decoration = decoration;
    }

    @Override
    protected void doUndo(PieceTable pt) {
        if (execSuccess) {
            pt.pieces.add(pieceIndex, oldPiece);
            pt.pieces.removeAll(newPieces);
            pt.fire(new TextBuffer.DecorateEvent(start, end, decoration));
        }
    }

    @Override
    protected void doRedo(PieceTable pt) {

        if (!PieceTable.inRange(start, 0, pt.getTextLength())) {
            throw new IllegalArgumentException("Position is outside of text bounds");
        }

        //  Accept length larger than actual and adjust it to actual
        if (end >= pt.getTextLength()) {
            end = pt.getTextLength();
        }

        pt.walkPieces((piece, pieceIndex, textPosition) -> {
            if ( PieceTable.inRange(start, textPosition, piece.length)) {
                int offset = start - textPosition;
                newPieces = PieceTable.normalize( List.of(
                        piece.pieceBefore(offset),
                        new Piece(pt, Piece.BufferType.ADDITION, piece.start + offset, end - start, decoration),
                        piece.pieceFrom(offset)
                ));
                oldPiece = piece;
                pt.pieces.addAll( pieceIndex, newPieces );
                pt.pieces.remove(oldPiece);
                this.pieceIndex = pieceIndex;

                pt.fire(new TextBuffer.DecorateEvent(start, end, decoration));
                execSuccess = true;
                return true;
            }
            return false;
        });

        /*pt.walkPieces((piece, pieceIndex, textPosition) -> {

            if (PieceTable.inRange(start, textPosition, piece.length)) {
                int pieceOffset = start - textPosition;
                startPieceIndex[0] = pieceIndex;
                additions.add(piece.pieceBefore(pieceOffset));
                removals.add(piece);
            }

            if (!additions.isEmpty()) {
                removals.add(piece);
                if (PieceTable.inRange(end, textPosition, piece.length)) {
                    int offset = end - textPosition;
                    Piece p = new Piece(pt, Piece.BufferType.ADDITION, piece.start + offset, piece.length - offset, decoration);
                    additions.add(p);
                    return true;
                }
            }
            return false;
        });

        newPieces = PieceTable.normalize(additions);
        oldPieces = removals;
        if (newPieces.size() > 0 || oldPieces.size() > 0) { // split actually happened
            pieceIndex = startPieceIndex[0];
            pt.pieces.addAll(pieceIndex, newPieces);
            pt.pieces.removeAll(oldPieces);
            pt.fire(new TextBuffer.DecorateEvent(start, end, decoration));
            execSuccess = true;
        }*/
    }
}




