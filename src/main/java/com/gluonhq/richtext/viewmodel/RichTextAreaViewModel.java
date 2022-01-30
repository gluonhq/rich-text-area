package com.gluonhq.richtext.viewmodel;

import com.gluonhq.richtext.*;
import com.gluonhq.richtext.model.TextBuffer;
import com.gluonhq.richtext.model.TextChangeListener;
import com.gluonhq.richtext.model.TextDecoration;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.IndexRange;
import javafx.scene.input.KeyEvent;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class RichTextAreaViewModel {

    private final TextBuffer textBuffer;
    private final CommandManager commandManager = new CommandManager(this);

    private final Map<EditorAction, Consumer<KeyEvent>> actionMap = Map.of(
        EditorAction.FORWARD,   e -> moveCaret(Direction.FORWARD, e.isShiftDown()),
        EditorAction.BACK,      e -> moveCaret(Direction.BACK, e.isShiftDown()),
        EditorAction.DOWN,      e -> moveCaret(Direction.DOWN, e.isShiftDown()),
        EditorAction.UP,        e -> moveCaret(Direction.UP, e.isShiftDown()),

        EditorAction.INSERT,    e -> commandManager.execute(new InsertTextCommand(e.getCharacter())),
        EditorAction.BACKSPACE, e -> commandManager.execute(new RemoveTextCommand(-1)),
        EditorAction.DELETE,    e -> commandManager.execute(new RemoveTextCommand(0)),

        EditorAction.UNDO,      e -> commandManager.undo()
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


    public RichTextAreaViewModel(TextBuffer textBuffer, Function<Boolean,Integer> getNextRowPosition) {
        this.textBuffer = Objects.requireNonNull(textBuffer); // TODO convert to property
        this.getNextRowPosition = Objects.requireNonNull(getNextRowPosition);
    }


    public int getTextLength() {
        return textBuffer.getTextLength();
    }

    public final void addChangeListener(TextChangeListener listener) {
        this.textBuffer.addChangeListener(listener);
    }

    public final void removeChangeListener(TextChangeListener listener) {
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

    void insert( String text ) {
        if (hasSelection()) {
            removeSelection();
        }
        textBuffer.insert(text, getCaretPosition());
        moveCaretPosition(1);
    }

    void remove(int caretOffset) {
        if (hasSelection()) {
            removeSelection();
        } else {

            System.out.println("Current pos: " + getCaretPosition());
            System.out.println("Offset: " + caretOffset);

            int position = getCaretPosition() + caretOffset;
            if (position >= 0 && position < getTextLength() ) {
                textBuffer.delete(position, 1);
                setCaretPosition(position);
            }
            System.out.println("New pos: " + getCaretPosition());
        }
    }

    // deletes selection if exists and set caret to the start position of the deleted selection
    private void removeSelection() {
        if ( hasSelection() ) {
            Selection selection = getSelection();
            textBuffer.delete(selection.getStart(), selection.getLength() );
            clearSelection();
            setCaretPosition(selection.getStart());
        }
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

}
