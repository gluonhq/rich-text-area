package com.gluonhq.richtext.viewmodel;

import com.gluonhq.richtext.Selection;
import com.gluonhq.richtext.Tools;
import com.gluonhq.richtext.model.TextBuffer;
import com.gluonhq.richtext.model.TextDecoration;
import com.gluonhq.richtext.undo.CommandManager;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.text.BreakIterator;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RichTextAreaViewModel {

    public static final Logger LOGGER = Logger.getLogger(RichTextAreaViewModel.class.getName());

    public enum Direction { FORWARD, BACK, UP, DOWN }

    private final TextBuffer textBuffer;
    private final CommandManager<RichTextAreaViewModel> commandManager = new CommandManager<>(this, this::updateProperties);
    private BreakIterator wordIterator;


    /// PROPERTIES ///////////////////////////////////////////////////////////////

    // caretPositionProperty
    private final IntegerProperty caretPositionProperty = new SimpleIntegerProperty(this, "caretPosition", -1);
    private final BiFunction<Double, Boolean, Integer> getNextRowPosition;

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
                    new Selection( value.getStart(), getTextLength()): value;
            }
            super.set(selection);
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
       return textBuffer.textLengthProperty();
    }
    public final int getTextLength() {
       return textBuffer.getTextLength();
    }

    // undoStackSizeProperty
    final ReadOnlyBooleanWrapper undoStackEmptyProperty = new ReadOnlyBooleanWrapper(this, "undoStackEmpty", true);
    public ReadOnlyBooleanProperty undoStackEmptyProperty() {
        return undoStackEmptyProperty.getReadOnlyProperty();
    }
    public boolean isUndoStackEmpty() {
        return undoStackEmptyProperty.get();
    }

    // redoStackSizeProperty
    final ReadOnlyBooleanWrapper redoStackEmptyProperty = new ReadOnlyBooleanWrapper(this, "redoStackEmpty", true);
    public ReadOnlyBooleanProperty redoStackEmptyProperty() {
        return redoStackEmptyProperty.getReadOnlyProperty();
    }
    public boolean isRedoStackEmpty() {
        return redoStackEmptyProperty.get();
    }

    public RichTextAreaViewModel(TextBuffer textBuffer, BiFunction<Double, Boolean, Integer> getNextRowPosition) {
        this.textBuffer = Objects.requireNonNull(textBuffer); // TODO convert to property
        this.getNextRowPosition = Objects.requireNonNull(getNextRowPosition);
    }

    public final void addChangeListener(Consumer<TextBuffer.Event> listener) {
        this.textBuffer.addChangeListener(listener);
    }

    public final void removeChangeListener(Consumer<TextBuffer.Event> listener) {
        this.textBuffer.removeChangeListener(listener);
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
    void insert( String text ) {
        removeSelection();
        int caretPosition =  getCaretPosition();
        if ( caretPosition >= getTextLength()) {
            textBuffer.append(text);
        } else {
            textBuffer.insert(text, caretPosition);
        }
        moveCaretPosition(text.length());
    }

    void remove(int caretOffset) {
        if (!removeSelection()) {
            int position = getCaretPosition() + caretOffset;
            if (position >= 0 && position <= getTextLength() ) {
                textBuffer.delete(position, 1);
                setCaretPosition(position);
            }
        }
    }

    void decorate(TextDecoration decoration) {
        if (getSelection().isDefined()) {
            Selection selection = getSelection();
            clearSelection();
            int caretPosition = getCaretPosition();
            setCaretPosition(-1);
            textBuffer.decorate(selection.getStart(), selection.getEnd(), decoration);
            setCaretPosition(caretPosition);
            setSelection(selection);
        }
    }

    /**
     * Deletes selection if exists and sets caret to the start position of the deleted selection
     */
    private boolean removeSelection() {
        if ( hasSelection() ) {
            Selection selection = getSelection();
            textBuffer.delete(selection.getStart(), selection.getLength() );
            clearSelection();
            setCaretPosition(selection.getStart());
            return true;
        }
        return false;
    }

    void clipboardCopy( final boolean cutText ) {
        Selection selection = getSelection();
        if (selection.isDefined()) {
            String selectedText = textBuffer.getText(selection.getStart(), selection.getEnd());
            final ClipboardContent content = new ClipboardContent();
            content.putString(selectedText);
            if (cutText) {
                commandManager.execute(new RemoveTextCmd(0));
            }
            Clipboard.getSystemClipboard().setContent(content);
        }
    }

    void clipboardPaste() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasString()) {
            final String text = clipboard.getString();
            if (text != null) {
                commandManager.execute(new InsertTextCmd(text));
            }
        }
    }

    void moveCaret(Direction direction, boolean changeSelection, boolean wordSelection, boolean lineSelection) {
        Selection prevSelection = getSelection();
        int prevCaretPosition = getCaretPosition();
        switch (direction) {
            case FORWARD:
                if (wordSelection) {
                    nextWord(c -> c != ' ' && c != '\t');
                } else if (lineSelection) {
                    lineEnd();
                } else {
                    moveCaretPosition(1);
                }
                break;
            case BACK:
                if (wordSelection) {
                    previousWord();
                } else if (lineSelection) {
                    lineStart();
                } else {
                    moveCaretPosition(-1);
                }
                break;
            case DOWN:
            case UP:
                int rowCharIndex = getNextRowPosition.apply(-1d, Direction.DOWN == direction);
                if (rowCharIndex >= 0) {
                    setCaretPosition(rowCharIndex);
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

    public void walkFragments(BiConsumer<String, TextDecoration> onFragment) {
        textBuffer.resetCharacterIterator();
        textBuffer.walkFragments(onFragment);
        LOGGER.log(Level.FINE, textBuffer.toString());
    }

    void undo() {
        this.textBuffer.undo();
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
        moveCaret(Direction.BACK, false, true, false);
        int prevCaretPosition = getCaretPosition();
        nextWord(c -> !Character.isLetterOrDigit(c));
        setSelection(new Selection(prevCaretPosition, getCaretPosition()));
    }

    public void selectCurrentLine() {
        moveCaret(Direction.BACK, false, false, true);
        moveCaret(Direction.FORWARD, true, false, true);
    }

    private void previousWord() {
        int textLength = getTextLength();
        if (textLength <= 0) {
            return;
        }
        if (wordIterator == null) {
            wordIterator = BreakIterator.getWordInstance();
        }
        wordIterator.setText(textBuffer.getCharacterIterator());

        int prevCaretPosition = getCaretPosition();
        int position = wordIterator.preceding(Tools.clamp(0, prevCaretPosition, textLength));
        while (position != BreakIterator.DONE &&
                !Character.isLetterOrDigit(textBuffer.charAt(Tools.clamp(0, position, textLength - 1)))) {
            position = wordIterator.preceding(Tools.clamp(0, position, textLength));
        }
        setCaretPosition(Tools.clamp(0, position, textLength));
    }

    private void nextWord(Predicate<Character> filter) {
        int textLength = getTextLength();
        if (wordIterator == null) {
            wordIterator = BreakIterator.getWordInstance();
        }
        wordIterator.setText(textBuffer.getCharacterIterator());

        int prevCaretPosition = getCaretPosition();
        int last = wordIterator.following(Tools.clamp(0, prevCaretPosition, textLength - 1));
        int current = wordIterator.next();
        while (current != BreakIterator.DONE) {
            for (int i = last; i <= current; i++) {
                char c = textBuffer.charAt(Tools.clamp(0, i, textLength - 1));
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
        int pos = getNextRowPosition.apply(0d, false);
        setCaretPosition(pos);
    }

    private void lineEnd() {
        int pos = getNextRowPosition.apply(Double.MAX_VALUE, false);
        setCaretPosition(pos);
    }

    private void updateProperties() {
        undoStackEmptyProperty.set(commandManager.isUndoStackEmpty());
        redoStackEmptyProperty.set(commandManager.isRedoStackEmpty());
    }
}
