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

import com.gluonhq.richtextarea.Selection;
import com.gluonhq.richtextarea.Tools;
import com.gluonhq.richtextarea.undo.AbstractCommand;
import com.gluonhq.richtextarea.undo.CommandManager;

import java.text.CharacterIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.gluonhq.richtextarea.model.TextBuffer.ZERO_WIDTH_TABLE_SEPARATOR;
import static com.gluonhq.richtextarea.model.TextBuffer.ZERO_WIDTH_TEXT;

/**
 * Piece table implementation.<br>
 * More info at  https://en.wikipedia.org/wiki/Piece_table
 */
public final class PieceTable extends AbstractTextBuffer {

    final UnitBuffer originalText;
    UnitBuffer additionBuffer = new UnitBuffer();

    final List<Piece> pieces = new ArrayList<>();
    private final CommandManager<PieceTable> commander = new CommandManager<>(this);

    private final PieceCharacterIterator pieceCharacterIterator;
    TextDecoration decorationAtCaret;

    /**
     * Creates a piece table using the original text of a document, and
     * sets the original unit buffer, that will contain one or more units.
     * A document contains 0, 1 or more decorations.
     * If there is no decoration present, for each unit a piece is defined, that
     * spans over the length of the unit.
     * If there are decorations, for each one, units are defined, pieces are defined, that spans over its length
     * @param document model with decorated text to start with
     */
    public PieceTable(Document document) {
        String text = Objects.requireNonNull(Objects.requireNonNull(document).getText());
        List<DecorationModel> decorations = document.getDecorations();
        if (decorations == null || decorations.isEmpty()) {
            decorations = List.of(new DecorationModel(0, text.length(), null, null));
        }
        this.originalText = new UnitBuffer(List.of());
        // For each decoration in the document:
        AtomicInteger accum = new AtomicInteger(0);
        decorations.forEach(d -> {
            // parse external text that spans the decoration into units
            UnitBuffer units = UnitBuffer.convertTextToUnits(text.substring(d.getStart(), d.getStart() + d.getLength()));
            if (units.isEmpty()) {
                units.append(new TextUnit(""));
            }
            // For each unit present:
            units.getUnitList().forEach(unit -> {
                originalText.append(unit);
                // create a new piece that spans the unit
                pieces.add(new Piece(PieceTable.this, Piece.BufferType.ORIGINAL, accum.getAndAdd(unit.length()), unit.length(), d.getDecoration(), d.getParagraphDecoration()));
            });
        });
        textLengthProperty.set(originalText.length());
        pieceCharacterIterator = new PieceCharacterIterator(this);
    }

    /**
     * Returns full text.
     * This is a costly operation as it walks through all the pieces
     * @return full text
     */
    @Override
    public String getText() {
        return getText(0, getTextLength());
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
        if (getTextLength() > 0 && !inRange(start, 0, getTextLength())) {
             throw new IllegalArgumentException("Start index " + start + " is not in range [0, " + getTextLength() + ")");
        }
        if (end < 0) {
            throw new IllegalArgumentException("End index is not in range");
        }
        StringBuilder textSB = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        walkPieces((p, i, tp) -> {
            Unit unit = p.getUnit();
            sb.append(p.getInternalText());
            if (start <= tp + p.length && end > tp && !unit.isEmpty()) {
                String text = sb.substring(Math.max(start, tp), Math.min(end, tp + p.length));
                if (!text.isEmpty()) {
                    textSB.append(unit instanceof TextUnit ? text : unit.getText());
                }
            }
            return (end <= tp);
        });
        return textSB.toString();
    }

    private int s0, s1;

    /**
     * Converts a position or index referred to the exportable text into the
     * position of internal text
     *
     * @param position the index from exportable text
     * @return an index from internal text
     */
    @Override
    public int getInternalPosition(int position) {
        if (position < 0) {
            return position;
        }
        s0 = -1;
        StringBuilder sb = new StringBuilder();
        walkPieces((p, i, tp) -> {
            Unit unit = p.getUnit();
            int sbMin = sb.length();
            int sbMax = sbMin + unit.getText().length();
            if (sbMin <= position && position <= sbMax) {
                s0 = tp + p.length;
            }
            sb.append(unit.getText());
            return (s0 > -1);
        });
        return s0;
    }

    /**
     * Converts the indices of a selection of exportable text into the
     * indices of a selection of internal text
     *
     * @param selection a selection with indices from exportable text
     * @return a selection with indices from internal text
     */
    @Override
    public Selection getInternalSelection(Selection selection) {
        if (selection == null) {
            throw new IllegalArgumentException("Selection can't be null");
        }
        if (!selection.isDefined()) {
            return Selection.UNDEFINED;
        }
        int start = selection.getStart();
        int end = selection.getEnd();
        if (end < 0) {
            throw new IllegalArgumentException("End index is not in range");
        }
        s0 = -1;
        s1 = -1;
        StringBuilder sb = new StringBuilder();
        walkPieces((p, i, tp) -> {
            Unit unit = p.getUnit();
            int sbMin = sb.length();
            int sbMax = sbMin + unit.getText().length();
            if (sbMin <= start && start <= sbMax) {
                s0 = tp;
            }
            if (sbMin <= end && end <= sbMax) {
                s1 = tp + p.length;
            }
            sb.append(unit.getText());
            return (s0 > -1 && s1 > -1);
        });
        return new Selection(s0, s1);
    }

    @Override
    public List<DecorationModel> getDecorationModelList() {
        List<DecorationModel> mergedList = new ArrayList<>();
        if (!pieces.isEmpty()) {
            AtomicInteger start = new AtomicInteger();
            DecorationModel dm = null;
            for (Piece piece : pieces) {
                int length = piece.getUnit().getText().length();
                if (mergedList.isEmpty()) {
                    dm = new DecorationModel(start.addAndGet(piece.start), length, piece.getDecoration(), piece.getParagraphDecoration());
                } else if (piece.getDecoration().equals(dm.getDecoration()) && piece.getParagraphDecoration().equals(dm.getParagraphDecoration())) {
                    mergedList.remove(mergedList.size() - 1);
                    dm = new DecorationModel(dm.getStart(), dm.getLength() + length, dm.getDecoration(), dm.getParagraphDecoration());
                } else {
                    dm = new DecorationModel(start.addAndGet(dm.getLength()), length, piece.getDecoration(), piece.getParagraphDecoration());
                }
                mergedList.add(dm);
            }
        }
        return mergedList;
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
    public List<Integer> getLineFeeds() {
        return pieceCharacterIterator.getLineFeedList();
    }

    @Override
    public void resetCharacterIterator() {
        pieceCharacterIterator.reset();
    }

    // internal append
    List<Piece> appendInternal(UnitBuffer unitBuffer, Decoration decoration, ParagraphDecoration paragraphDecoration) {
        int pos = additionBuffer.length();
        textLengthProperty.set(getTextLength() + unitBuffer.length());
        AtomicInteger accum = new AtomicInteger(pos);
        return unitBuffer.getUnitList().stream()
                .peek(unit -> additionBuffer.append(unit))
                .map(unit -> new Piece(this, Piece.BufferType.ADDITION, accum.getAndAdd(unit.length()), unit.length(), decoration, paragraphDecoration))
                .collect(Collectors.toList());
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
        } else if (decoration instanceof ParagraphDecoration) {
            commander.execute(new ParagraphDecorateCmd(start, end, (ParagraphDecoration) decoration));
        } else {
            throw new IllegalArgumentException("Decoration type not supported: " + decoration);
        }
    }

    /**
     * Walks through unit fragments. Each fragment is represented by related text and decoration
     * @param onFragment callback to get fragment info
     * @param start the initial position of the fragment
     * @param end the end position of the fragment (not included)
     */
    @Override
    public void walkFragments(BiConsumer<Unit, Decoration> onFragment, int start, int end) {
        StringBuilder sb = new StringBuilder();
        walkPieces((p, i, tp) -> {
            Unit unit = p.getUnit();
            sb.append(p.getInternalText());
            if (start <= tp + p.length && end > tp && !unit.isEmpty()) {
                String text = sb.substring(Math.max(start, tp), Math.min(end, tp + p.length));
                if (!text.isEmpty()) {
                    onFragment.accept(unit instanceof TextUnit ? new TextUnit(text) : unit, p.getDecoration());
                }
            }
            return (end <= tp);
        });
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
    public ParagraphDecoration getParagraphDecorationAtCaret(int caretPosition) {
        int textPosition = 0;
        int index = 0;
        for (; index < pieces.size(); index++) {
            Piece piece = pieces.get(index);
            if (textPosition <= caretPosition && caretPosition < textPosition + piece.length) {
                return piece.getParagraphDecoration();
            }
            textPosition += piece.length;
        }
        ParagraphDecoration prevDecoration = previousPieceParagraphDecoration(index);
        if (prevDecoration.hasTableDecoration()) {
            // remove table decoration from the previous paragraph
            return ParagraphDecoration.builder().fromDecoration(prevDecoration).tableDecoration(new TableDecoration()).build();
        }
        return prevDecoration;
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
                  .filter(b -> b == null || !b.isEmpty())
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

    ParagraphDecoration previousPieceParagraphDecoration(int index) {
        return pieces.isEmpty() ?
                ParagraphDecoration.builder().presets().build() : pieces.get(index > 0 ? index - 1 : 0).getParagraphDecoration();
    }

    @Override
    public String toString() {
        String p = pieces.stream().map(piece -> " - " + piece.toString()).collect(Collectors.joining("\n", "\n", ""));
        return "PieceTable{\n O=\"" + Tools.formatTextWithAnchors(originalText.getInternalText()) + "\"" + "" +
                ",\n A=\"" + Tools.formatTextWithAnchors(additionBuffer.getInternalText()) + "\"" +
                ",\n L=" + getTextLength() +
                ", pieces ->" + p +
                ",\n OU -> " + originalText.getUnitList() +
                ",\n AU -> " + additionBuffer.getUnitList() +
                "\n}";
    }
}

class PieceCharacterIterator implements CharacterIterator {

    private static final char LF = 0x0a;
    private final PieceTable pt;
    private int begin;
    private int end;
    private int pos;
    private int[] posArray;
    private List<Integer> lineFeedList;

    public PieceCharacterIterator(PieceTable pt) {
        this.pt = Objects.requireNonNull(pt);
        reset();
    }

    public void reset() {
        this.begin = 0;
        this.end = pt.getTextLength();
        this.pos = 0;

        posArray = new int[pt.pieces.size() + 1];
        lineFeedList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        pt.walkPieces((p, i, tp) -> {
            sb.append(p.getInternalText());
            String text = sb.substring(tp);
            IntStream.iterate(text.indexOf(LF),
                            index -> index >= 0,
                            index -> text.indexOf(LF, index + 1))
                    .boxed()
                    .forEach(index -> lineFeedList.add(tp + index));
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
                char c = pt.pieces.get(i).getInternalText().charAt(pos - posArray[i]);
                return c == ZERO_WIDTH_TABLE_SEPARATOR ? ' ' : c;
            }
        }
        return 0;
    }

    public List<Integer> getLineFeedList() {
        return lineFeedList;
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

    private final UnitBuffer unitBuffer;
    private List<Piece> newPieces;
    private boolean execSuccess = false;

    AppendCmd(String text) {
        this.unitBuffer = UnitBuffer.convertTextToUnits(Objects.requireNonNull(text));
    }

    @Override
    protected void doUndo(PieceTable pt) {
        if (execSuccess) {
            pt.pieces.removeAll(newPieces);
            pt.fire(new TextBuffer.DeleteEvent(pt.getTextLength() - unitBuffer.length(), unitBuffer.length()));
            pt.textLengthProperty.set(pt.getTextLength() - unitBuffer.length());
        }
    }

    @Override
    protected void doRedo(PieceTable pt) {
        if (!unitBuffer.isEmpty()) {
            int pos = pt.getTextLength();
            newPieces = pt.appendInternal(unitBuffer,
                    pt.decorationAtCaret != null ?
                    pt.decorationAtCaret : pt.previousPieceDecoration(pt.pieces.size()),
                    pt.getParagraphDecorationAtCaret(pos) != null ?
                    pt.getParagraphDecorationAtCaret(pos) : pt.previousPieceParagraphDecoration(pt.pieces.size()));
            pt.pieces.addAll(newPieces);
            pt.fire(new TextBuffer.InsertEvent(unitBuffer.getInternalText(), pos));
            execSuccess = true;
        }
    }

    @Override
    public String toString() {
        return "AppendCmd[\"" + unitBuffer + "\"]";
    }
}

class InsertCmd extends AbstractCommand<PieceTable> {

    private final UnitBuffer unitBuffer;
    private final int insertPosition;

    private Collection<Piece> newPieces;
    private Piece oldPiece;
    private int opPieceIndex;
    private boolean execSuccess = false;

    InsertCmd(String text, int insertPosition) {
        this.unitBuffer = UnitBuffer.convertTextToUnits(Objects.requireNonNull(text));
        this.insertPosition = insertPosition;
    }

    @Override
    protected void doUndo(PieceTable pt) {
        if (execSuccess) {
            pt.pieces.add(opPieceIndex, oldPiece);
            pt.pieces.removeAll(newPieces);
            pt.fire(new TextBuffer.DeleteEvent(insertPosition, unitBuffer.length()));
            pt.textLengthProperty.set(pt.getTextLength() - unitBuffer.length());
        }
    }

    @Override
    protected void doRedo(PieceTable pt) {
        if (unitBuffer.isEmpty()) {
            return; // no need to insert empty text
        }

        if (insertPosition < 0 || insertPosition > pt.getTextLength()) {
            throw new IllegalArgumentException("Position is outside text bounds");
        }

        if (insertPosition == pt.getTextLength()) {
            pt.append(unitBuffer.getInternalText());
        } else {
            pt.walkPieces((piece, pieceIndex, textPosition) -> {
                if (PieceTable.inRange(insertPosition, textPosition, piece.length)) {
                    int pieceOffset = insertPosition - textPosition;
                    final Decoration decoration = pieceOffset > 0 ? (TextDecoration) piece.getDecoration() : pt.previousPieceDecoration(pieceIndex);
                    final ParagraphDecoration paragraphDecoration = pt.getParagraphDecorationAtCaret(insertPosition) != null ?
                            pt.getParagraphDecorationAtCaret(insertPosition) : pt.previousPieceParagraphDecoration(pieceIndex);
                    List<Piece> pieces = pt.appendInternal(unitBuffer, pt.decorationAtCaret != null ? pt.decorationAtCaret : decoration, paragraphDecoration);
                    List<Piece> allPieces = new ArrayList<>(List.of(piece.pieceBefore(pieceOffset)));
                    allPieces.addAll(pieces);
                    allPieces.add(piece.pieceFrom(pieceOffset));
                    newPieces = PieceTable.normalize(allPieces);
                    oldPiece = piece;
                    pt.pieces.addAll(pieceIndex, newPieces);
                    pt.pieces.remove(oldPiece);
                    opPieceIndex = pieceIndex;

                    pt.fire(new TextBuffer.InsertEvent(unitBuffer.getInternalText(), insertPosition));
                    execSuccess = true;
                    return true;
                }
                return false;
            });
        }
    }

    @Override
    public String toString() {
        return "InsertCmd[\"" + unitBuffer + "\" at " + insertPosition + "]";
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
     * Command to delete units starting from an index position to a given length.
     * @param deletePosition position from where delete operation is to be executed. Normally, this is position of the caret.
     * @param length Length of the unit following the deletePosition to delete.
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
              .map(Piece::getInternalText)
              .reduce( "", (id, s) -> id + s );
            pt.textLengthProperty.set(pt.getTextLength() + length);
            pt.fire(new TextBuffer.InsertEvent(text, deletePosition));
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
                    // the next piece after the deletion point should use the paragraph decoration from the previous piece, if any
                    int offset = endPosition - textPosition;
                    ParagraphDecoration paragraphDecoration = additions.get(additions.size() - 1).getParagraphDecoration();
                    Piece nextPiece = piece.copy(piece.start + offset, piece.length - offset, piece.decoration,
                            paragraphDecoration == null ? piece.paragraphDecoration : paragraphDecoration);
                    additions.add(nextPiece);
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
            pt.textLengthProperty.set(pt.getTextLength() - length);
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
    private final UnitBuffer unitBuffer;
    private final int insertPosition;

    private boolean execSuccess = false;
    private List<Piece> newPiece;
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
        this.unitBuffer = new UnitBuffer(new ImageUnit(decoration.getUrl()));
    }

    @Override
    protected void doUndo(PieceTable pt) {
        if (execSuccess) {
            if (newPiece != null) {
                pt.pieces.removeAll(newPiece);
                pt.fire(new TextBuffer.DeleteEvent(pt.getTextLength() - 1, unitBuffer.length()));
            } else {
                pt.pieces.add(opPieceIndex, oldPiece);
                pt.pieces.removeAll(newPieces);
                pt.fire(new TextBuffer.DeleteEvent(insertPosition, unitBuffer.length()));
            }
            pt.textLengthProperty.set(pt.getTextLength() - 1);
        }
    }

    @Override
    protected void doRedo(PieceTable pt) {
        if (insertPosition < 0 || insertPosition > pt.getTextLength()) {
            throw new IllegalArgumentException("Position " + insertPosition + " is outside of text bounds [0, " + pt.getTextLength() + "]");
        }

        final ParagraphDecoration paragraphDecoration = pt.getParagraphDecorationAtCaret(insertPosition) != null ?
                pt.getParagraphDecorationAtCaret(insertPosition) : pt.previousPieceParagraphDecoration(insertPosition);

        if (insertPosition == pt.getTextLength()) {
            int pos = pt.getTextLength();
            newPiece = pt.appendInternal(unitBuffer, decoration, paragraphDecoration);
            pt.pieces.addAll(newPiece);
            pt.fire(new TextBuffer.InsertEvent(ZERO_WIDTH_TEXT, pos));
            execSuccess = true;
        } else {
            pt.walkPieces((piece, pieceIndex, textPosition) -> {
                if (PieceTable.inRange(insertPosition, textPosition, piece.length)) {
                    int pieceOffset = insertPosition - textPosition;
                    List<Piece> pieces = pt.appendInternal(unitBuffer, decoration, paragraphDecoration);
                    List<Piece> allPieces = new ArrayList<>(List.of(piece.pieceBefore(pieceOffset)));
                    allPieces.addAll(pieces);
                    allPieces.add(piece.pieceFrom(pieceOffset));
                    newPieces = PieceTable.normalize(allPieces);
                    oldPiece = piece;
                    pt.pieces.addAll(pieceIndex, newPieces);
                    pt.pieces.remove(oldPiece);
                    opPieceIndex = pieceIndex;

                    pt.fire(new TextBuffer.InsertEvent(unitBuffer.getInternalText(), insertPosition));
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

class ParagraphDecorateCmd extends AbstractCommand<PieceTable> {

    private int start;
    private int end;
    private final ParagraphDecoration paragraphDecoration;

    private boolean execSuccess = false;
    private int pieceIndex = -1;
    private Collection<Piece> newPieces = new ArrayList<>();
    private Collection<Piece> oldPieces = new ArrayList<>();

    /**
     * Decorates the text within the given paragraph with the supplied decoration.
     * @param start index of the first character to decorate
     * @param end index of the last character to decorate
     * @param paragraphDecoration Decorations to apply on the selected paragraph
     */
    ParagraphDecorateCmd(int start, int end, ParagraphDecoration paragraphDecoration) {
        this.start = start;
        this.end = end;
        this.paragraphDecoration = paragraphDecoration;
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
        if (!PieceTable.inRange(start, 0, pt.getTextLength() + 1)) {
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
            int pieceEndPosition = textPosition + piece.length + (start == pt.getTextLength() ? 0 : - 1);
            if (start <= pieceEndPosition && (end >= pieceEndPosition || end >= textPosition)) {
                startPieceIndex[0] = pieceIndex;
                if (start == pt.getTextLength()) {
                    int offset = start - textPosition;
                    if (offset > 0) {
                        additions.add(piece.copy(piece.start, offset));
                    }
                    additions.add(piece.copy(start, 0, piece.decoration, paragraphDecoration));
                    removals.add(piece);
                } else if (textPosition <= start) {
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
                    additions.add(piece.copy(piece.start + offset, length, piece.decoration, paragraphDecoration));
                    if (end < textPosition + piece.length) {
                        additions.add(piece.pieceFrom(end - textPosition));
                    }
                    removals.add(piece);
                }  else if (textPosition + piece.length <= end) { // entire piece is in selection
                    additions.add(piece.copy(piece.start, piece.length, piece.decoration, paragraphDecoration));
                    removals.add(piece);
                } else if (textPosition < end) {
                    int offset = end - textPosition;
                    additions.add(piece.copy(piece.start, offset, piece.decoration, paragraphDecoration));
                    additions.add(piece.pieceFrom(offset));
                    removals.add(piece);
                }
            }
            return false;
        });

        newPieces = additions.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        oldPieces = removals;
        if (newPieces.size() > 0 || oldPieces.size() > 0) {
            pieceIndex = startPieceIndex[0];
            pt.pieces.addAll(pieceIndex, newPieces);
            pt.pieces.removeAll(oldPieces);
            pt.fire(new TextBuffer.DecorateEvent(start, end, paragraphDecoration));
            execSuccess = true;
        }
    }

    private boolean isPieceInSelection(Piece piece, int textPosition) {
        int pieceEndPosition = textPosition + piece.length - 1;
        return start <= pieceEndPosition && (end >= pieceEndPosition || end >= textPosition);
    }

    @Override
    public String toString() {
        return "ParagraphDecorateCmd[" + start + " x " + end + "]";
    }
}




