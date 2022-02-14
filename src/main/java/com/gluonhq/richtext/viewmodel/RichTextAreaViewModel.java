package com.gluonhq.richtext.viewmodel;

import com.gluonhq.richtext.EditorAction;
import com.gluonhq.richtext.Selection;
import com.gluonhq.richtext.model.TextBuffer;
import com.gluonhq.richtext.model.TextDecoration;
import com.gluonhq.richtext.undo.CommandManager;
import javafx.beans.property.*;
import javafx.scene.input.KeyEvent;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Map.entry;
import static javafx.scene.text.FontWeight.BOLD;

public class RichTextAreaViewModel {

    public enum Direction { FORWARD, BACK, UP, DOWN }

    private final TextBuffer textBuffer;
    private final CommandManager<RichTextAreaViewModel> commandManager = new CommandManager<>(this);

    private final Map<EditorAction, Consumer<KeyEvent>> actionMap = Map.ofEntries(
        entry(EditorAction.FORWARD,   e -> moveCaret(Direction.FORWARD, e.isShiftDown())),
        entry(EditorAction.BACK,      e -> moveCaret(Direction.BACK, e.isShiftDown())),
        entry(EditorAction.DOWN,      e -> moveCaret(Direction.DOWN, e.isShiftDown())),
        entry(EditorAction.UP,        e -> moveCaret(Direction.UP, e.isShiftDown())),

        entry(EditorAction.INSERT,    e -> commandManager.execute(new InsertTextCmd(e.getCharacter()))),
        entry(EditorAction.BACKSPACE, e -> commandManager.execute(new RemoveTextCmd(-1))),
        entry(EditorAction.DELETE,    e -> commandManager.execute(new RemoveTextCmd(0))),
        entry(EditorAction.ENTER,     e -> commandManager.execute(new InsertTextCmd("\n"))),
        entry(EditorAction.DECORATE,  e -> commandManager.execute(new DecorateTextCmd())),

        entry(EditorAction.UNDO,      e -> commandManager.undo()),
        entry(EditorAction.REDO,      e -> commandManager.redo())

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
        if ( pos >= 0 && pos <= getTextLength()) {
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

    // TODO: Add logic to create various TextDecoration(s)
    void decorate() {
        if (getSelection().isDefined()) {
            Selection selection = getSelection();
            textBuffer.decorate(selection.getStart(), selection.getEnd(), TextDecoration.builder().fontWeight(BOLD).build());
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

    public void executeAction(EditorAction action, KeyEvent e) {
        actionMap.get(Objects.requireNonNull(action)).accept(e);
    }

    void moveCaret( Direction direction, boolean changeSelection ) {

        Selection prevSelection = getSelection();
        int prevCaretPosition = getCaretPosition();
        switch (direction) {
            case FORWARD:
            case BACK:
                moveCaretPosition( Direction.FORWARD == direction ? 1:-1);
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
            setSelection( new Selection(pos, getCaretPosition()));
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

}
