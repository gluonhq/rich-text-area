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
package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.Selection;
import com.gluonhq.richtextarea.Tools;
import com.gluonhq.richtextarea.model.Decoration;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.ImageDecoration;
import com.gluonhq.richtextarea.model.Paragraph;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.TextBuffer;
import com.gluonhq.richtextarea.model.TextDecoration;
import com.gluonhq.richtextarea.model.Unit;
import com.gluonhq.richtextarea.model.UnitBuffer;
import com.gluonhq.richtextarea.undo.CommandManager;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.gluonhq.richtextarea.RichTextArea.RTA_DATA_FORMAT;

public class RichTextAreaViewModel {

    public static final Logger LOGGER = Logger.getLogger(RichTextAreaViewModel.class.getName());

    public enum Direction { FORWARD, BACK, UP, DOWN }
    public enum Remove { LETTER, WORD, LINE }

    private final CommandManager<RichTextAreaViewModel> commandManager = new CommandManager<>(this, this::updateProperties);
    private BreakIterator wordIterator;
    private int undoStackSizeWhenSaved = 0;

    private final ObservableList<Paragraph> paragraphList = FXCollections.observableArrayList();
    Paragraph lastParagraph;
    private final BiFunction<Double, Boolean, Integer> getNextRowPosition;
    private final Function<Boolean, Integer> getNextTableCellPosition;

    /// PROPERTIES ///////////////////////////////////////////////////////////////

    // textBufferProperty
    private final ObjectProperty<TextBuffer> textBufferProperty = new SimpleObjectProperty<>(this, "textBuffer") {
        @Override
        protected void invalidated() {
            // invalidate undo/redo stack
            commandManager.clearStacks();
            undoStackSizeWhenSaved = 0;
            undoStackSizeProperty.set(0);
            redoStackSizeProperty.set(0);
        }
    };
    public final ObjectProperty<TextBuffer> textBufferProperty() {
        return textBufferProperty;
    }
    public final TextBuffer getTextBuffer() {
        TextBuffer textBuffer = textBufferProperty.get();
        if (textBuffer == null) {
            throw new RuntimeException("Fatal error: TextBuffer was null");
        }
        return textBuffer;
    }
    public final void setTextBuffer(TextBuffer value) {
        textBufferProperty.set(value);
    }

    // caretPositionProperty
    private final IntegerProperty caretPositionProperty = new SimpleIntegerProperty(this, "caretPosition", -1) {
        @Override
        protected void invalidated() {
            if (!hasSelection()) {
                Decoration decorationAtCaret = getTextBuffer().getDecorationAtCaret(get());
                if (decorationAtCaret instanceof TextDecoration) {
                    setDecorationAtCaret(decorationAtCaret);
                }
                getParagraphWithCaret().ifPresent(p -> setDecorationAtParagraph(p.getDecoration()));
            }
        }
    };
    public final IntegerProperty caretPositionProperty() {
        return caretPositionProperty;
    }
    public final int getCaretPosition() {
        return caretPositionProperty.get();
    }
    public final void setCaretPosition(int value) {
        caretPositionProperty.set(value);
    }

    // selectionProperty
    private final ObjectProperty<Selection> selectionProperty = new SimpleObjectProperty<>(this, "selection", Selection.UNDEFINED) {
        @Override
        public void set(Selection value) {
            Selection selection = Selection.UNDEFINED;
            if (value != null) {
                selection = value.getEnd() >= getTextLength()?
                    new Selection(value.getStart(), getTextLength()): value;
            }
            super.set(selection);
        }

        @Override
        protected void invalidated() {
            if (!get().isDefined()) {
                setDecorationAtCaret(getTextBuffer().getDecorationAtCaret(getCaretPosition()));
            }
        }
    };
    public final ObjectProperty<Selection> selectionProperty() {
        return selectionProperty;
    }
    public final Selection getSelection() {
        return selectionProperty.get();
    }
    public final void setSelection(Selection value) {
        selectionProperty.set(value);
    }

    // textLengthProperty
    public final ReadOnlyIntegerProperty textLengthProperty() {
        return getTextBuffer().textLengthProperty();
    }
    public final int getTextLength() {
       return getTextBuffer().getTextLength();
    }

    // undoStackSizeProperty
    private final ReadOnlyIntegerWrapper undoStackSizeProperty = new ReadOnlyIntegerWrapper(this, "undoStackSize") {
        @Override
        protected void invalidated() {
            if (isAutoSave()) {
                save();
            } else {
                savedProperty.set(get() == undoStackSizeWhenSaved);
            }
        }
    };
    public final ReadOnlyIntegerProperty undoStackSizeProperty() {
       return undoStackSizeProperty.getReadOnlyProperty();
    }
    public final int getUndoStackSize() {
       return undoStackSizeProperty.get();
    }

    // redoStackSizeProperty
    private final ReadOnlyIntegerWrapper redoStackSizeProperty = new ReadOnlyIntegerWrapper(this, "redoStackSize");
    public final ReadOnlyIntegerProperty redoStackSizeProperty() {
       return redoStackSizeProperty.getReadOnlyProperty();
    }
    public final int getRedoStackSize() {
       return redoStackSizeProperty.get();
    }

    // editableProperty
    private final BooleanProperty editableProperty = new SimpleBooleanProperty(this, "editable");
    final BooleanProperty editableProperty() {
       return editableProperty;
    }
    final boolean isEditable() {
       return editableProperty.get();
    }
    public final void setEditable(boolean value) {
        editableProperty.set(value);
    }

    // tableAllowedProperty
    private final BooleanProperty tableAllowedProperty = new SimpleBooleanProperty(this, "tableAllowed", true);
    public final BooleanProperty tableAllowedProperty() {
       return tableAllowedProperty;
    }
    public final boolean isTableAllowed() {
       return tableAllowedProperty.get();
    }
    public final void setTableAllowed(boolean value) {
        tableAllowedProperty.set(value);
    }

    // decorationAtCaretProperty
    private final ObjectProperty<Decoration> decorationAtCaretProperty = new SimpleObjectProperty<>(this, "decorationAtCaret") {
        @Override
        protected void invalidated() {
            if (get() instanceof TextDecoration && !getSelection().isDefined()) {
                getTextBuffer().setDecorationAtCaret((TextDecoration) get());
            }
        }
    };
    public final ObjectProperty<Decoration> decorationAtCaretProperty() {
       return decorationAtCaretProperty;
    }
    public final Decoration getDecorationAtCaret() {
       return decorationAtCaretProperty.get();
    }
    public final void setDecorationAtCaret(Decoration value) {
        decorationAtCaretProperty.set(value);
    }

    // decorationAtParagraphProperty
    private final ObjectProperty<ParagraphDecoration> decorationAtParagraphProperty = new SimpleObjectProperty<>(this, "decorationAtParagraph") {
        @Override
        protected void invalidated() {
        }
    };
    public final ObjectProperty<ParagraphDecoration> decorationAtParagraphProperty() {
        return decorationAtParagraphProperty;
    }
    public final ParagraphDecoration getDecorationAtParagraph() {
        return decorationAtParagraphProperty.get();
    }
    public final void setDecorationAtParagraph(ParagraphDecoration value) {
        decorationAtParagraphProperty.set(value);
    }

    // documentProperty
    private final ObjectProperty<Document> documentProperty = new SimpleObjectProperty<>(this, "document") {
        @Override
        protected void invalidated() {
            paragraphList.clear();
            Document document = get();
            if (document != null) {
                updateParagraphList();
                // update decorationAtParagraph once the paragraph list is ready
                getParagraphWithCaret().ifPresent(p -> setDecorationAtParagraph(p.getDecoration()));
            }
        }
    };
    public final ObjectProperty<Document> documentProperty() {
       return documentProperty;
    }
    public final Document getDocument() {
       return documentProperty.get();
    }
    public final void setDocument(Document value) {
        documentProperty.set(value);
    }

    // autoSaveProperty
    private final BooleanProperty autoSaveProperty = new SimpleBooleanProperty(this, "autoSave");
    public final BooleanProperty autoSaveProperty() {
       return autoSaveProperty;
    }
    public final boolean isAutoSave() {
       return autoSaveProperty.get();
    }
    public final void setAutoSave(boolean value) {
        autoSaveProperty.set(value);
    }

    // savedProperty
    final ReadOnlyBooleanWrapper savedProperty = new ReadOnlyBooleanWrapper(this, "saved", true);
    public final ReadOnlyBooleanProperty savedProperty() {
       return savedProperty.getReadOnlyProperty();
    }
    public final boolean isSaved() {
       return savedProperty.get();
    }

    public RichTextAreaViewModel(BiFunction<Double, Boolean, Integer> getNextRowPosition, Function<Boolean, Integer> getNextTableCellPosition) {
        this.getNextRowPosition = Objects.requireNonNull(getNextRowPosition);
        this.getNextTableCellPosition = Objects.requireNonNull(getNextTableCellPosition);
    }

    public ObservableList<Paragraph> getParagraphList() {
        return paragraphList;
    }

    public boolean isEmptyParagraph(Paragraph paragraph) {
        if (paragraph == null || paragraph.getEnd() - paragraph.getStart() < 1) {
            return true;
        }
        return (paragraph.getEnd() - paragraph.getStart() == 1 &&
                "\n".equals(getTextBuffer().getText(paragraph.getStart(), paragraph.getEnd())));
    }

    public final void addChangeListener(Consumer<TextBuffer.Event> listener) {
        this.getTextBuffer().addChangeListener(listener);
    }

    public final void removeChangeListener(Consumer<TextBuffer.Event> listener) {
        this.getTextBuffer().removeChangeListener(listener);
    }

    CommandManager<RichTextAreaViewModel> getCommandManager() {
        return commandManager;
    }

    void moveCaretPosition(final int charCount) {
        int pos = getCaretPosition() + charCount;
        if (pos >= 0 && pos <= getTextLength()) {
            setCaretPosition(pos);
        }
    }

    boolean hasSelection() {
        return getSelection().isDefined();
    }

    public void clearSelection() {
        setSelection(Selection.UNDEFINED);
    }

    /**
     * Inserts new text into the document at current caret position
     * Smart enough to distinguish between append and insert operations
     * @param text text to insert
     */
    void insert(String text) {
        removeSelection();
        int caretPosition = getCaretPosition();
        if (caretPosition >= getTextLength()) {
            getTextBuffer().append(text);
            // text (with 0+ LF) appended to last paragraph or as new paragraphs
        } else {
            getTextBuffer().insert(text, caretPosition);
            // text (with 0+ LF) inserted to some paragraph or as new paragraphs
        }
        moveCaretPosition(UnitBuffer.convertTextToUnits(text).getInternalText().length());
    }

    void remove(int caretOffset, int length) {
        if (!removeSelection()) {
            int position = getCaretPosition() + caretOffset;
            if (position >= 0 && position <= getTextLength()) {
                getTextBuffer().delete(position, length);
                setCaretPosition(position);
            }
        }
    }

    void decorate(Decoration decoration) {
        if (decoration instanceof TextDecoration) {
            if (getSelection().isDefined()) {
                Selection selection = getSelection();
                clearSelection();
                int caretPosition = getCaretPosition();
                setCaretPosition(-1);
                getTextBuffer().decorate(selection.getStart(), selection.getEnd(), decoration);
                setCaretPosition(caretPosition);
                setSelection(selection);
            }
        } else if (decoration instanceof ImageDecoration) {
            int caretPosition = getCaretPosition();
            setCaretPosition(-1);
            getTextBuffer().decorate(caretPosition, 1, decoration);
            setCaretPosition(caretPosition + 1);
        } else if (decoration instanceof ParagraphDecoration) {
            if (getSelection().isDefined()) {
                // check all possible paragraphs within selection
                List<Paragraph> paragraphsWithSelection = getParagraphsWithSelection();
                if (!paragraphsWithSelection.isEmpty()) {
                    Selection selection = getSelection();
                    int caretPosition = getCaretPosition();
                    int start = paragraphsWithSelection.get(0).getStart();
                    int end = paragraphsWithSelection.get(paragraphsWithSelection.size() - 1).getEnd();
                    setCaretPosition(-1);
                    clearSelection();
                    getTextBuffer().decorate(start, end, decoration);
                    setCaretPosition(caretPosition);
                    setSelection(selection);
                }
            } else {
                // only paragraph where caret is
                int caretPosition = getCaretPosition();
                Paragraph paragraph = getParagraphWithCaret().orElseThrow(() -> new IllegalArgumentException("No paragraph available"));
                setCaretPosition(-1);
                getTextBuffer().decorate(paragraph.getStart(), paragraph.getEnd(), decoration);
                setCaretPosition(caretPosition);
            }
        }
    }

    /**
     * Deletes selection if exists and sets caret to the start position of the deleted selection
     */
    private boolean removeSelection() {
        if (hasSelection()) {
            Selection selection = getSelection();
            getTextBuffer().delete(selection.getStart(), selection.getLength() );
            clearSelection();
            setCaretPosition(selection.getStart());
            return true;
        }
        return false;
    }

    void clipboardCopy(final boolean cutText) {
        Selection selection = getSelection();
        if (selection.isDefined()) {
            String selectedText = getTextBuffer().getText(selection.getStart(), selection.getEnd());
            final ClipboardContent content = new ClipboardContent();
            content.put(RTA_DATA_FORMAT, selectedText);
            content.putString(selectedText.replaceAll(TextBuffer.ZERO_WIDTH_NO_BREAK_SPACE_TEXT, ""));
            if (cutText) {
                commandManager.execute(new RemoveTextCmd(0));
            }
            Clipboard.getSystemClipboard().setContent(content);
        }
    }

    boolean clipboardHasImage() {
        return Clipboard.getSystemClipboard().hasImage();
    }

    boolean clipboardHasString() {
        return Clipboard.getSystemClipboard().hasString();
    }

    boolean clipboardHasUrl() {
        return Clipboard.getSystemClipboard().hasUrl();
    }

    void clipboardPaste() {
        if (clipboardHasImage()) {
            final Image image = Clipboard.getSystemClipboard().getImage();
            if (image != null) {
                String url = image.getUrl() != null ? image.getUrl() : Clipboard.getSystemClipboard().getUrl();
                if (url != null) {
                    commandManager.execute(new DecorateCmd(new ImageDecoration(url)));
                }
            }
        } else if (clipboardHasUrl()) {
            final String url = Clipboard.getSystemClipboard().getUrl();
            if (url != null) {
                if (!getSelection().isDefined()) {
                    int caret = getCaretPosition();
                    commandManager.execute(new InsertCmd(url));
                    setSelection(new Selection(caret, caret + url.length()));
                }
                commandManager.execute(new DecorateCmd(TextDecoration.builder().url(url).build()));
            }
        } else {
            String text = null;
            if (Clipboard.getSystemClipboard().hasContent(RTA_DATA_FORMAT)) {
                text = (String) Clipboard.getSystemClipboard().getContent(RTA_DATA_FORMAT);
            } else if (clipboardHasString()) {
                text = Clipboard.getSystemClipboard().getString();
            }
            if (text != null) {
                if (getSelection().isDefined()) {
                    commandManager.execute(new ReplaceCmd(text));
                } else {
                    commandManager.execute(new InsertCmd(text));
                }
            }
        }
    }

    void moveCaret(Direction direction, boolean changeSelection, boolean wordSelection, boolean lineSelection, boolean paragraphSelection) {
        Selection prevSelection = getSelection();
        int prevCaretPosition = getCaretPosition();
        switch (direction) {
            case FORWARD:
                if (wordSelection) {
                    nextWord(c -> c != ' ' && c != '\t' && c != TextBuffer.ZERO_WIDTH_TABLE_SEPARATOR);
                } else if (lineSelection) {
                    lineEnd();
                } else if (paragraphSelection) {
                    paragraphEnd();
                } else {
                    moveCaretPosition(1);
                }
                break;
            case BACK:
                if (wordSelection) {
                    previousWord();
                } else if (lineSelection) {
                    lineStart();
                } else if (paragraphSelection) {
                    paragraphStart();
                } else {
                    moveCaretPosition(-1);
                }
                break;
            case DOWN:
            case UP:
                if (wordSelection) { //  Mac
                    if (direction == Direction.UP) {
                        paragraphStart();
                    } else {
                        paragraphEnd();
                    }
                } else if (lineSelection) { // home, end on Mac
                    setCaretPosition (direction == Direction.UP ? 0 : getTextLength());
                } else {
                    if (getParagraphWithCaret()
                            .map(p -> p.getDecoration().hasTableDecoration())
                            .orElse(false)) {
                        int charIndex = getNextTableCellPosition.apply(Direction.DOWN == direction);
                        if (charIndex >= 0) {
                            setCaretPosition(charIndex);
                        }
                    } else {
                        int rowCharIndex = getNextRowPosition.apply(-1d, Direction.DOWN == direction);
                        if (rowCharIndex >= 0) {
                            setCaretPosition(rowCharIndex);
                        }
                    }
                }
                break;
        }

        if (changeSelection) {
            int pos = prevSelection.isDefined()?
                    prevCaretPosition == prevSelection.getStart()? prevSelection.getEnd(): prevSelection.getStart():
                    prevCaretPosition;
            setSelection(new Selection(pos, getCaretPosition()));
        } else {
            clearSelection();
        }

    }

    public void resetCharacterIterator() {
        getTextBuffer().resetCharacterIterator();
        updateParagraphList();
        LOGGER.log(Level.FINE, getTextBuffer().toString());
    }

    public void walkFragments(BiConsumer<Unit, Decoration> onFragment, int start, int end) {
        getTextBuffer().walkFragments(onFragment, start, end);
    }

    private void updateParagraphList() {
        List<Integer> lineFeeds = getTextBuffer().getLineFeeds();
        AtomicInteger pos = new AtomicInteger();
        List<Paragraph> newParagraphList = new ArrayList<>();
        lineFeeds.forEach(lfPos ->
                newParagraphList.add(getParagraphAt(pos.getAndSet(lfPos), pos.incrementAndGet())));
        if (pos.get() <= getTextLength()) {
            lastParagraph = getParagraphAt(pos.get(), getTextLength());
            newParagraphList.add(lastParagraph);
        }
        paragraphList.setAll(newParagraphList);
    }

    private Paragraph getParagraphAt(int start, int end) {
        ParagraphDecoration pd = getTextBuffer().getParagraphDecorationAtCaret(start);
        return new Paragraph(start, end, pd != null ? pd : ParagraphDecoration.builder().presets().build());
    }

    public Optional<Paragraph> getParagraphWithCaret() {
        int position = getCaretPosition();
        return paragraphList.stream()
                .filter(p -> p.getStart() <= position &&
                        position < (p.equals(lastParagraph) ? p.getEnd() + 1 : p.getEnd()))
                .findFirst();
    }

    private List<Paragraph> getParagraphsWithSelection() {
        Selection selection = getSelection();
        if (!selection.isDefined()) {
            return List.of();
        }
        return paragraphList.stream()
                .filter(p -> !(p.getStart() > selection.getEnd() || p.getEnd() <= selection.getStart()))
                .collect(Collectors.toList());
    }

    void undo() {
        this.getTextBuffer().undo();
    }

    void undoDecoration() {
        Selection selection = getSelection();
        clearSelection();
        int caretPosition = getCaretPosition();
        setCaretPosition(-1);
        undo();
        setCaretPosition(caretPosition);
        setSelection(selection);
    }

    public void selectCurrentWord() {
        if (getTextLength() <= 0) {
            return;
        }
        moveCaret(Direction.BACK, false, true, false, false);
        int prevCaretPosition = getCaretPosition();
        nextWord(c -> !Character.isLetterOrDigit(c));
        setSelection(new Selection(prevCaretPosition, getCaretPosition()));
    }

    public void selectCurrentParagraph() {
        if (getTextLength() <= 0) {
            return;
        }
        moveCaret(Direction.BACK, false, false, false, true);
        moveCaret(Direction.FORWARD, true, false, false, true);
    }

    void removeWord() {
        int wordPos = previousWordPosition();
        int finalPos = getCaretPosition() - wordPos;
        commandManager.execute(new RemoveTextCmd(-finalPos, finalPos));
    }

    void removeLine() {
        int pos = getNextRowPosition.apply(0d, null);
        int finalPos = getCaretPosition() - pos;
        commandManager.execute(new RemoveTextCmd(-finalPos, finalPos));
    }

    private int previousWordPosition() {
        int textLength = getTextLength();
        if (textLength <= 0) {
            return 0;
        }
        if (wordIterator == null) {
            wordIterator = BreakIterator.getWordInstance();
        }
        wordIterator.setText(getTextBuffer().getCharacterIterator());

        int prevCaretPosition = getCaretPosition();
        int position = wordIterator.preceding(Tools.clamp(0, prevCaretPosition, textLength));
        while (position != BreakIterator.DONE &&
                !Character.isLetterOrDigit(getTextBuffer().charAt(Tools.clamp(0, position, textLength - 1)))) {
            position = wordIterator.preceding(Tools.clamp(0, position, textLength));
        }
        return Tools.clamp(0, position, textLength);
    }

    private void previousWord() {
        setCaretPosition(previousWordPosition());
    }

    private void nextWord(Predicate<Character> filter) {
        int textLength = getTextLength();
        if (wordIterator == null) {
            wordIterator = BreakIterator.getWordInstance();
        }
        wordIterator.setText(getTextBuffer().getCharacterIterator());

        int prevCaretPosition = getCaretPosition();
        int last = wordIterator.following(Tools.clamp(0, prevCaretPosition, textLength - 1));
        int current = wordIterator.next();
        while (current != BreakIterator.DONE) {
            for (int i = last; i <= current; i++) {
                char c = getTextBuffer().charAt(Tools.clamp(0, i, textLength - 1));
                if (filter.test(c)) {
                    setCaretPosition(Tools.clamp(0, i, textLength));
                    return;
                }
            }
            last = current;
            current = wordIterator.next();
        }
        setCaretPosition(textLength);
    }

    private void lineStart() {
        int pos = getNextRowPosition.apply(0d, null);
        setCaretPosition(pos);
    }

    private void lineEnd() {
        int pos = getNextRowPosition.apply(Double.MAX_VALUE, null);
        setCaretPosition(Math.max(pos == getTextLength() ? pos : pos - 1, 0));
    }

    private void paragraphStart() {
        int pos = getCaretPosition();
        if (pos > 0) {
            while (pos > 0 && getTextBuffer().charAt(pos - 1) != 0x0a) {
                pos--;
            }
            setCaretPosition(pos);
        }
    }

    private void paragraphEnd() {
        int pos = getCaretPosition();
        int len = getTextLength();
        if (pos < len) {
            while (pos < len && getTextBuffer().charAt(pos) != 0x0a) {
                pos++;
            }
            setCaretPosition(pos);
        }
    }

    private void updateProperties() {
        undoStackSizeProperty.set(commandManager.getUndoStackSize());
        redoStackSizeProperty.set(commandManager.getRedoStackSize());
    }

    private Document getCurrentDocument() {
        // text and indices should be based on the exportable text
        int caret = getTextBuffer().getText(0, getCaretPosition()).length();
        return new Document(getTextBuffer().getText(), getTextBuffer().getDecorationModelList(), caret);
    }

    void newDocument() {
        Platform.runLater(() -> {
            // invalidate documentProperty
            setDocument(null);
            setDocument(new Document());
        });
    }

    void open(Document document) {
        Platform.runLater(() -> {
            // invalidate documentProperty
            setDocument(null);
            setDocument(document);
        });
    }

    void save() {
        Document currentDocument = getCurrentDocument();
        undoStackSizeWhenSaved = getUndoStackSize();
        savedProperty.set(true);
        setDocument(currentDocument);
    }
}
