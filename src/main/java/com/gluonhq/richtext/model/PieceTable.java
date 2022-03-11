package com.gluonhq.richtext.model;

import com.gluonhq.richtext.undo.AbstractCommand;
import com.gluonhq.richtext.undo.CommandManager;

import java.text.CharacterIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.gluonhq.richtext.model.PieceTable.ZERO_WIDTH_TEXT;

/**
 * Piece table implementation.<br>
 * More info at  https://en.wikipedia.org/wiki/Piece_table
 */
public final class PieceTable extends AbstractTextBuffer {

    static final String ZERO_WIDTH_TEXT = "\u200b";

    final String originalText;
    String additionBuffer = "";

    final List<Piece> pieces = new ArrayList<>();
    private final CommandManager<PieceTable> commander = new CommandManager<>(this);

    private final PieceCharacterIterator pieceCharacterIterator;
    TextDecoration decorationAtCaret;

    /**
     * Creates piece table using original text
     * @param faceModel model with decorated text to start with
     */
     public PieceTable(FaceModel faceModel) {
        this.originalText = Objects.requireNonNull(Objects.requireNonNull(faceModel).getText());
        if (faceModel.getDecorationList() == null) {
            pieces.add(piece(Piece.BufferType.ORIGINAL, 0, originalText.length()));
        } else {
            faceModel.getDecorationList().forEach(d ->
                    pieces.add(new Piece(PieceTable.this, Piece.BufferType.ORIGINAL, d.getStart(), d.getLength(), d.getDecoration())));
        }
        textLengthProperty.set(pieces.stream().mapToInt(b -> b.length).sum());
        pieceCharacterIterator = new PieceCharacterIterator(this);
    }

    private Piece piece(Piece.BufferType bufferType, int start, int length ) {
        return new Piece( this, bufferType, start, length );
    }

    /**
     * Returns full text.
     * This is a costly operation as it walks through all the pieces
     * @return full text
     */
    @Override
    public String getText() {
        return pieces.stream()
                     .map(Piece::getText)
                     .reduce("", (s1,s2) -> s1+s2);
    }

    /**
     * Returns partial text
     * @param start start position within text, inclusive
     * @param end end position within text, exclusive
     * @return partial text
     * @throws IllegalArgumentException if start or end are not in index range of the text
     */
    @Override
    public String getText(final int start, final int end) {

        if (!inRange(start, 0, getTextLength())) {
             throw new IllegalArgumentException("Start index is not in range");
        }
        if ( end < 0 ) {
            throw new IllegalArgumentException("End index is not in range");
        }
        int realEnd = Math.min(end,getTextLength());

        StringBuilder sb = new StringBuilder();
        for (Piece value : pieces) {
            if (sb.length() > realEnd) {
                break;
            }
            sb.append(value.getText());
        }
        return sb.substring(start, realEnd);

    }

    @Override
    public CharacterIterator getCharacterIterator() {
        return pieceCharacterIterator;
    }

    @Override
    public char charAt(int pos) {
        return pieceCharacterIterator.charAt(pos);
    }

    @Override
    public void resetCharacterIterator() {
        pieceCharacterIterator.reset();
    }

    // internal append
    Piece appendTextInternal(String text, Decoration decoration) {
        int pos = additionBuffer.length();
        additionBuffer += text;
        textLengthProperty.set(getTextLength() + text.length());
        return new Piece(this, Piece.BufferType.ADDITION, pos, text.length(), decoration);
    }

    /**
     * Appends text
     * @param text new text
     */
    @Override
    public void append(String text) {
        commander.execute(new AppendCmd(text));
    }

    @Override
    public void decorate(int start, int end, Decoration decoration) {
        if (decoration instanceof TextDecoration) {
            commander.execute(new TextDecorateCmd(start, end, decoration));
        } else if (decoration instanceof ImageDecoration) {
            commander.execute(new ImageDecorateCmd((ImageDecoration) decoration, start));
        } else {
            throw new IllegalArgumentException("Decoration type not supported: " + decoration);
        }
    }

    /**
     * Walks through text fragments. Each fragment is represented by related text and decoration
     * @param onFragment callback to get fragment info
     */
    @Override
    public void walkFragments(BiConsumer<String, Decoration> onFragment) {
        pieces.forEach(p -> onFragment.accept(p.getText(), p.getDecoration()));
    }

    @Override
    public Decoration getDecorationAtCaret(int caretPosition) {
        int textPosition = 0;
        int index = 0;
        for (; index < pieces.size(); index++) {
            Piece piece = pieces.get(index);
            if (textPosition < caretPosition && caretPosition <= textPosition + piece.length) {
                return piece.getDecoration();
            }
            textPosition += piece.length;
        }
        return previousPieceDecoration(index);
    }

    @Override
    public void setDecorationAtCaret(TextDecoration decoration) {
        this.decorationAtCaret = decoration;
    }

    /**
     * Inserts text at insertPosition
     * @param text to insert
     * @param insertPosition to insert text at
     * @throws IllegalArgumentException if insertPosition is not valid
     */
    @Override
    public void insert(final String text, final int insertPosition) {
        commander.execute(new InsertCmd(text, insertPosition));
    }

    /**
     * Deletes text with 'length' starting at 'deletePosition'
     * @param deletePosition deletePosition to start deletion from
     * @param length length of text to delete
     * @throws IllegalArgumentException if deletePosition is not valid
     */
    @Override
    public void delete(final int deletePosition, int length) {
        commander.execute(new DeleteCmd(deletePosition, length));
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

    Decoration previousPieceDecoration(int index) {
        return pieces.isEmpty() || !(pieces.get(index > 0 ? index - 1 : 0).getDecoration() instanceof TextDecoration) ?
                TextDecoration.builder().presets().build() : pieces.get(index > 0 ? index - 1 : 0).getDecoration();
    }

    @Override
    public String toString() {
        String p = pieces.stream().map(piece -> " - " + piece.toString()).collect(Collectors.joining("\n", "\n", ""));
        return "PieceTable{\n O=\"" + originalText.replaceAll("\n", "<n>").replaceAll(ZERO_WIDTH_TEXT, "<a>") + "\"" + "" +
                ",\n A=\"" + additionBuffer.replaceAll("\n", "<n>").replaceAll(ZERO_WIDTH_TEXT, "<a>") + "\"" +
                ",\n L=" + getTextLength() +
                ", pieces ->" + p +
                "\n}";
    }
}

class PieceCharacterIterator implements CharacterIterator {

    private final PieceTable pt;
    private int begin;
    private int end;
    private int pos;
    private int[] posArray;

    public PieceCharacterIterator(PieceTable pt) {
        this.pt = Objects.requireNonNull(pt);
        reset();
    }

    public void reset() {
        this.begin = 0;
        this.end = pt.getTextLength();
        this.pos = 0;

        posArray = new int[pt.pieces.size() + 1];
        pt.walkPieces((p, i, tp) -> {
            posArray[i] = tp;
            return false;
        });
        posArray[pt.pieces.size()] = end;
    }

    public char charAt(int pos) {
        if (pos < 0 || pos >= pt.getTextLength()) {
            throw new IllegalArgumentException("Invalid pos value");
        }
        for (int i = 0; i < posArray.length; i++) {
            if (posArray[i] <= pos && pos < posArray[i + 1]) {
                return pt.pieces.get(i).getText().charAt(pos - posArray[i]);
            }
        }
        return 0;
    }

    @Override
    public char first() {
        pos = begin;
        return current();
    }

    @Override
    public char last() {
        if (end != begin) {
            pos = end - 1;
        } else {
            pos = end;
        }
        return current();
    }

    @Override
    public char current() {
        if (pos >= begin && pos < end) {
            return pt.charAt(pos);
        } else {
            return DONE;
        }
    }

    @Override
    public char next() {
        if (pos < end - 1) {
            pos++;
            return pt.charAt(pos);
        } else {
            pos = end;
            return DONE;
        }
    }

    @Override
    public char previous() {
        if (pos > begin) {
            pos--;
            return pt.charAt(pos);
        } else {
            return DONE;
        }
    }

    @Override
    public char setIndex(int position) {
        if (position < begin || position > end) {
            throw new IllegalArgumentException("Invalid index");
        }
        pos = position;
        return current();
    }

    @Override
    public int getIndex() {
        return pos;
    }

    @Override
    public int getBeginIndex() {
        return begin;
    }

    @Override
    public int getEndIndex() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PieceCharacterIterator that = (PieceCharacterIterator) o;
        return begin == that.begin && end == that.end && pos == that.pos && Objects.equals(pt, that.pt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pt, begin, end, pos);
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException("Clone exception");
        }
    }
}

abstract class AbstractPTCmd extends AbstractCommand<PieceTable> {}

class AppendCmd extends AbstractPTCmd {

    private final String text;
    private Piece newPiece;
    private boolean execSuccess = false;

    AppendCmd(String text) {
        this.text = Objects.requireNonNull(text);
    }

    @Override
    protected void doUndo(PieceTable pt) {
        if (execSuccess) {
            pt.pieces.remove(newPiece);
            pt.fire(new TextBuffer.DeleteEvent(pt.getTextLength() - text.length(), text.length()));
            pt.textLengthProperty.set( pt.getTextLength() - text.length());
        }
    }

    @Override
    protected void doRedo(PieceTable pt) {
        if (!text.isEmpty()) {
            int pos = pt.getTextLength();
            newPiece = pt.appendTextInternal(text, pt.decorationAtCaret != null ?
                    pt.decorationAtCaret : pt.previousPieceDecoration(pt.pieces.size()));
            pt.pieces.add(newPiece);
            pt.fire(new TextBuffer.InsertEvent(text, pos));
            execSuccess = true;
        }
    }

    @Override
    public String toString() {
        return "AppendCmd[\"" + text + "\"]";
    }
}

class InsertCmd extends AbstractCommand<PieceTable> {

    private final String text;
    private final int insertPosition;

    private Collection<Piece> newPieces;
    private Piece oldPiece;
    private int opPieceIndex;
    private boolean execSuccess = false;

    InsertCmd(String text, int insertPosition) {
        this.text = Objects.requireNonNull(text);
        this.insertPosition = insertPosition;
    }

    @Override
    protected void doUndo(PieceTable pt) {
        if (execSuccess) {
            pt.pieces.add(opPieceIndex, oldPiece);
            pt.pieces.removeAll(newPieces);
            pt.fire(new TextBuffer.DeleteEvent(insertPosition, text.length()));
            pt.textLengthProperty.set(pt.getTextLength() - text.length());
        }
    }

    @Override
    protected void doRedo(PieceTable pt) {

        if (text.isEmpty()) {
            return; // no need to insert empty text
        }

        if (insertPosition < 0 || insertPosition > pt.getTextLength()) {
            throw new IllegalArgumentException("Position is outside text bounds");
        }

        if (insertPosition == pt.getTextLength()) {
            pt.append(text);
        } else {
            pt.walkPieces((piece, pieceIndex, textPosition) -> {
                if (PieceTable.inRange(insertPosition, textPosition, piece.length)) {
                    int pieceOffset = insertPosition - textPosition;
                    final Decoration decoration = pieceOffset > 0 ? (TextDecoration) piece.getDecoration() : pt.previousPieceDecoration(pieceIndex);
                    newPieces = PieceTable.normalize(List.of(
                            piece.pieceBefore(pieceOffset),
                            pt.appendTextInternal(text, decoration),
                            piece.pieceFrom(pieceOffset)
                    ));
                    oldPiece = piece;
                    pt.pieces.addAll(pieceIndex, newPieces);
                    pt.pieces.remove(oldPiece);
                    opPieceIndex = pieceIndex;

                    pt.fire(new TextBuffer.InsertEvent(text, insertPosition));
                    execSuccess = true;
                    return true;
                }
                return false;
            });
        }
    }

    @Override
    public String toString() {
        return "InsertCmd[\"" + text + "\" at " + insertPosition + "]";
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
            pt.textLengthProperty.set(pt.getTextLength() + length);
        }
    }

    @Override
    protected void doRedo(PieceTable pt) {
        if (deletePosition < 0 || deletePosition > pt.getTextLength()) {
            throw new IllegalArgumentException("Position " + deletePosition + " is outside of text bounds [0," + pt.getTextLength() +"]");
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
                if (!removals.contains(piece)) {
                    removals.add(piece);
                }
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
            pt.textLengthProperty.set( pt.getTextLength() - length);
            pt.fire(new TextBuffer.DeleteEvent(deletePosition, length));
            execSuccess = true;
        }
    }

    @Override
    public String toString() {
        return "DeleteCmd[" + deletePosition + " x " + length + "]";
    }

}

class ImageDecorateCmd extends AbstractCommand<PieceTable> {

    private final ImageDecoration decoration;
    private final int insertPosition;

    private boolean execSuccess = false;
    private Piece newPiece;
    private Piece oldPiece;
    private int opPieceIndex;
    private Collection<Piece> newPieces = new ArrayList<>();

    /**
     * Inserts an image at the given insertion point
     * @param decoration the image decoration
     * @param insertPosition index of the character to decorate
     */
    ImageDecorateCmd(ImageDecoration decoration, int insertPosition) {
        this.decoration = decoration;
        this.insertPosition = insertPosition;
    }

    @Override
    protected void doUndo(PieceTable pt) {
        if (execSuccess) {
            if (newPiece != null) {
                pt.pieces.remove(newPiece);
                pt.fire(new TextBuffer.DeleteEvent(pt.getTextLength() - 1, 1));
            } else {
                pt.pieces.add(opPieceIndex, oldPiece);
                pt.pieces.removeAll(newPieces);
                pt.fire(new TextBuffer.DeleteEvent(insertPosition, 1));
            }
            pt.textLengthProperty.set(pt.getTextLength() - 1);
        }
    }

    @Override
    protected void doRedo(PieceTable pt) {
        if (insertPosition < 0 || insertPosition > pt.getTextLength()) {
            throw new IllegalArgumentException("Position " + insertPosition + " is outside of text bounds [0, " + pt.getTextLength() + "]");
        }

        if (insertPosition == pt.getTextLength()) {
            int pos = pt.getTextLength();
            newPiece = pt.appendTextInternal(ZERO_WIDTH_TEXT, decoration);
            pt.pieces.add(newPiece);
            pt.fire(new TextBuffer.InsertEvent(ZERO_WIDTH_TEXT, pos));
            execSuccess = true;
        } else {
            pt.walkPieces((piece, pieceIndex, textPosition) -> {
                if (PieceTable.inRange(insertPosition, textPosition, piece.length)) {
                    int pieceOffset = insertPosition - textPosition;
                    newPieces = PieceTable.normalize(List.of(
                            piece.pieceBefore(pieceOffset),
                            pt.appendTextInternal(ZERO_WIDTH_TEXT, decoration),
                            piece.pieceFrom(pieceOffset)
                    ));
                    oldPiece = piece;
                    pt.pieces.addAll(pieceIndex, newPieces);
                    pt.pieces.remove(oldPiece);
                    opPieceIndex = pieceIndex;

                    pt.fire(new TextBuffer.InsertEvent(ZERO_WIDTH_TEXT, insertPosition));
                    execSuccess = true;
                    return true;
                }
                return false;
            });
        }

    }

    @Override
    public String toString() {
        return "ImageDecorateCmd[" + decoration + " at " + insertPosition + "]";
    }
}

class TextDecorateCmd extends AbstractCommand<PieceTable> {

    private int start;
    private int end;
    private final Decoration decoration;

    private boolean execSuccess = false;
    private int pieceIndex = -1;
    private Collection<Piece> newPieces = new ArrayList<>();
    private Collection<Piece> oldPieces = new ArrayList<>();

    /**
     * Decorates the text within the given range with the supplied decoration.
     * @param start index of the first character to decorate
     * @param end index of the last character to decorate
     * @param decoration Decorations to apply on the selected text
     */
    TextDecorateCmd(int start, int end, Decoration decoration) {
        this.start = start;
        this.end = end;
        this.decoration = decoration;
    }

    @Override
    protected void doUndo(PieceTable pt) {
        if (execSuccess) {
            pt.pieces.addAll(pieceIndex, oldPieces);
            pt.pieces.removeAll(newPieces);

            oldPieces.forEach(piece -> {
                pt.fire(new TextBuffer.DecorateEvent(piece.start, piece.start + piece.length, piece.decoration));
            });
        }
    }

    @Override
    protected void doRedo(PieceTable pt) {
        if (!PieceTable.inRange(start, 0, pt.getTextLength())) {
            throw new IllegalArgumentException("Position " + start + " is outside of text bounds [0, " + pt.getTextLength() + ")");
        }

        //  Accept length larger than actual and adjust it to actual
        if (end >= pt.getTextLength()) {
            end = pt.getTextLength();
        }

        final int[] startPieceIndex = new int[1];
        final List<Piece> additions = new ArrayList<>(); // start and end pieces
        final List<Piece> removals = new ArrayList<>();

        pt.walkPieces((piece, pieceIndex, textPosition) -> {
            if (isPieceInSelection(piece, textPosition)) {
                startPieceIndex[0] = pieceIndex;
                if (textPosition <= start) {
                    int offset = start - textPosition;
                    int length;
                    if (textPosition + piece.length > end) {
                        length = Math.min(end - start, piece.length); // selection ends in current piece
                    } else {
                        length = piece.length - offset; // selection spans over next piece(s)
                    }
                    if (offset > 0) {
                        additions.add(piece.pieceBefore(offset));
                    }
                    additions.add(piece.copy(piece.start + offset, length, decoration));
                    if (end < textPosition + piece.length) {
                        additions.add(piece.pieceFrom(end - textPosition));
                    }
                    removals.add(piece);
                }  else if (textPosition + piece.length <= end) { // entire piece is in selection
                    additions.add(piece.copy(piece.start, piece.length, decoration));
                    removals.add(piece);
                } else if (textPosition < end) {
                    int offset = end - textPosition;
                    additions.add(piece.copy(piece.start, offset, decoration));
                    additions.add(piece.pieceFrom(offset));
                    removals.add(piece);
                }
            }
            return false;
        });

        newPieces = PieceTable.normalize(additions);
        oldPieces = removals;
        if (newPieces.size() > 0 || oldPieces.size() > 0) {
            pieceIndex = startPieceIndex[0];
            pt.pieces.addAll(pieceIndex, newPieces);
            pt.pieces.removeAll(oldPieces);
            pt.fire(new TextBuffer.DecorateEvent(start, end, decoration));
            execSuccess = true;
        }
    }

    private boolean isPieceInSelection(Piece piece, int textPosition) {
        int pieceEndPosition = textPosition + piece.length - 1;
        return start <= pieceEndPosition && (end >= pieceEndPosition || end >= textPosition);
    }

    @Override
    public String toString() {
        return "TextDecorateCmd[" + start +
                " x " + end + "]";
    }
}




