package com.gluonhq.richtext;

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

class RichTextAreaViewModel {

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
    private final ObjectProperty<IndexRange> selectionProperty = new SimpleObjectProperty<>(this, "selection", Tools.NO_SELECTION) {
        @Override
        public void set(IndexRange value) {
            IndexRange selection = Objects.requireNonNull(value);
            selection = IndexRange.normalize(selection.getStart(), selection.getEnd());
            if (!Tools.isIndexRangeValid(selection) || selection.getStart() > getTextLength() ) {
                selection = Tools.NO_SELECTION;
            } else if ( selection.getStart() > getTextLength() ){
                selection = IndexRange.normalize( selection.getStart(), getTextLength());
            }
            super.set(selection);
        }

    };
    public final ObjectProperty<IndexRange> selectionProperty() {
        return selectionProperty;
    }
    public final IndexRange getSelection() {
        return selectionProperty.get();
    }
    final void setSelection(IndexRange value) {
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


    public void moveCaretPosition(final int charCount) {
        int pos = getCaretPosition() + charCount;
        if ( pos >= 0 && pos <= getTextLength()) {
            setCaretPosition(pos);
        }
    }

    public boolean hasSelection() {
        return Tools.isIndexRangeValid( getSelection());
    }

    public  void clearSelection() {
        setSelection(Tools.NO_SELECTION);
    }

    public void insert( String text ) {
        if (hasSelection()) {
            removeSelection();
        }
        textBuffer.insert(text, getCaretPosition());
        moveCaretPosition(1);
    }

    public void remove(int caretOffset) {
        if (hasSelection()) {
            removeSelection();
        } else {
            int position = getCaretPosition() + caretOffset;
            if (position >= 0 && position < getTextLength() ) {
                textBuffer.delete(position, 1);
                setCaretPosition(position);
            }
        }
    }

    // deletes selection if exists and set caret to the start position of the deleted selection
    public void removeSelection() {
        if ( hasSelection() ) {
            IndexRange selection = getSelection();
            textBuffer.delete(selection.getStart(), selection.getEnd() - selection.getStart() );
            setSelection(Tools.NO_SELECTION);
            setCaretPosition(selection.getStart());
        }
    }

    void executeAction(EditorAction action, KeyEvent e) {
        actionMap.get(Objects.requireNonNull(action)).accept(e);
    }

    void moveCaret( Direction direction, boolean changeSelection ) {

        IndexRange prevSelection = getSelection();
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
            int pos = Tools.isIndexRangeValid(prevSelection)?
                    prevCaretPosition == prevSelection.getStart()? prevSelection.getEnd(): prevSelection.getStart():
                    prevCaretPosition;
            setSelection(IndexRange.normalize(pos, getCaretPosition()));
        } else {
            clearSelection();
        }

    }

    public void walkFragments(BiConsumer<String, TextDecoration> onFragment) {
        textBuffer.walkFragments(onFragment);
    }

}
