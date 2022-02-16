package com.gluonhq.richtext.viewmodel;

import com.gluonhq.richtext.EditorAction;
import com.gluonhq.richtext.Selection;
import com.gluonhq.richtext.Tools;
import com.gluonhq.richtext.model.TextBuffer;
import com.gluonhq.richtext.model.TextDecoration;
import com.gluonhq.richtext.undo.CommandManager;
import javafx.beans.property.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;

import java.text.BreakIterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Map.entry;
import static javafx.scene.text.FontPosture.ITALIC;
import static javafx.scene.text.FontWeight.BOLD;

public class RichTextAreaViewModel {

    public enum Direction { FORWARD, BACK, UP, DOWN }

    private final TextBuffer textBuffer;
    private final CommandManager<RichTextAreaViewModel> commandManager = new CommandManager<>(this);
    private BreakIterator wordIterator;

    private final Map<EditorAction, Consumer<KeyEvent>> actionMap = Map.ofEntries(

        entry( EditorAction.FORWARD,            e -> moveCaret(Direction.FORWARD, e.isShiftDown(),
                                                    Tools.MAC ? e.isAltDown() : e.isControlDown())),
        entry( EditorAction.BACK,               e -> moveCaret(Direction.BACK, e.isShiftDown(),
                                                    Tools.MAC ? e.isAltDown() : e.isControlDown())),
        entry( EditorAction.DOWN,               e -> moveCaret(Direction.DOWN, e.isShiftDown(), e.isAltDown())),
        entry( EditorAction.UP,                 e -> moveCaret(Direction.UP, e.isShiftDown(), e.isAltDown())),

        entry( EditorAction.INSERT,              e -> commandManager.execute(new InsertTextCmd(e.getCharacter()))),
        entry( EditorAction.BACKSPACE,           e -> commandManager.execute(new RemoveTextCmd(-1))),
        entry( EditorAction.DELETE,              e -> commandManager.execute(new RemoveTextCmd(0))),
        entry( EditorAction.ENTER,               e -> commandManager.execute(new InsertTextCmd("\n"))),
        entry( EditorAction.DECORATE_WEIGHT,     e -> commandManager.execute(new DecorateTextCmd(TextDecoration.builder().fontWeight(BOLD).build()))),
        entry( EditorAction.DECORATE_POSTURE,    e -> commandManager.execute(new DecorateTextCmd(TextDecoration.builder().fontPosture(ITALIC).build()))),

        entry( EditorAction.UNDO,                e -> commandManager.undo()),
        entry( EditorAction.REDO,                e -> commandManager.redo()),

        entry( EditorAction.COPY,                e -> clipboardCopy(false)),
        entry( EditorAction.CUT,                 e -> clipboardCopy(true)),
        entry( EditorAction.PASTE,               e -> clipboardPaste())

    );

    /// PROPERTIES ///////////////////////////////////////////////////////////////

    // caretPositionProperty
    private final IntegerProperty caretPositionProperty = new SimpleIntegerProperty(this, "caretPosition", -1);
    private final Function<Boolean, Integer> getNextRowPosition;

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


    public RichTextAreaViewModel(TextBuffer textBuffer, Function<Boolean,Integer> getNextRowPosition) {
        this.textBuffer = Objects.requireNonNull(textBuffer); // TODO convert to property
        this.getNextRowPosition = Objects.requireNonNull(getNextRowPosition);
    }

    public final void addChangeListener(Consumer<TextBuffer.Event> listener) {
        this.textBuffer.addChangeListener(listener);
    }

    public final void removeChangeListener(Consumer<TextBuffer.Event> listener) {
        this.textBuffer.removeChangeListener(listener);
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
            textBuffer.decorate(selection.getStart(), selection.getEnd(), decoration);
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

    public void executeAction(EditorAction action, KeyEvent e) {
        actionMap.get(Objects.requireNonNull(action)).accept(e);
    }

    void moveCaret(Direction direction, boolean changeSelection, boolean wordSelection) {
        Selection prevSelection = getSelection();
        int prevCaretPosition = getCaretPosition();
        switch (direction) {
            case FORWARD:
                if (wordSelection) {
                    nextWord();
                } else {
                    moveCaretPosition(1);
                }
                break;
            case BACK:
                if (wordSelection) {
                    previousWord();
                } else {
                    moveCaretPosition(-1);
                }
                break;
            case DOWN:
            case UP:
                int rowCharIndex = getNextRowPosition.apply(Direction.DOWN == direction);
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
        textBuffer.walkFragments(onFragment);
    }

    public void undo() {
        this.textBuffer.undo();
    }

    private void previousWord() {
        int textLength = getTextLength();
        if (textLength <= 0) {
            return;
        }
        if (wordIterator == null) {
            wordIterator = BreakIterator.getWordInstance();
        }
        String text = textBuffer.getText();
        wordIterator.setText(text);

        int prevCaretPosition = getCaretPosition();
        int position = wordIterator.preceding(Tools.clamp(0, prevCaretPosition, textLength));
        while (position != BreakIterator.DONE &&
                !Character.isLetterOrDigit(text.charAt(Tools.clamp(0, position, textLength - 1)))) {
            position = wordIterator.preceding(Tools.clamp(0, position, textLength));
        }
        setCaretPosition(Tools.clamp(0, position, textLength));
    }

    private void nextWord() {
        int textLength = getTextLength();
        if (wordIterator == null) {
            wordIterator = BreakIterator.getWordInstance();
        }
        String text = textBuffer.getText();
        wordIterator.setText(text);

        int prevCaretPosition = getCaretPosition();
        int last = wordIterator.following(Tools.clamp(0, prevCaretPosition, textLength - 1));
        int current = wordIterator.next();
        while (current != BreakIterator.DONE) {
            for (int i = last; i <= current; i++) {
                char c = text.charAt(Tools.clamp(0, i, textLength - 1));
                if (c != ' ' && c != '\t') {
                    setCaretPosition(Tools.clamp(0, i, textLength));
                    return;
                }
            }
            last = current;
            current = wordIterator.next();
        }
        setCaretPosition(textLength);
    }

}
