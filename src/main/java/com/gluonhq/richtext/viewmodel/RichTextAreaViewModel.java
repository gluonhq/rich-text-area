package com.gluonhq.richtext.viewmodel;

import com.gluonhq.richtext.Selection;
import com.gluonhq.richtext.Tools;
import com.gluonhq.richtext.model.Decoration;
import com.gluonhq.richtext.model.ImageDecoration;
import com.gluonhq.richtext.model.TextBuffer;
import com.gluonhq.richtext.model.TextDecoration;
import com.gluonhq.richtext.undo.CommandManager;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
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

    private final CommandManager<RichTextAreaViewModel> commandManager = new CommandManager<>(this, this::updateProperties);
    private BreakIterator wordIterator;

    /// PROPERTIES ///////////////////////////////////////////////////////////////

    // textBufferProperty
    private final ObjectProperty<TextBuffer> textBufferProperty = new SimpleObjectProperty<>(this, "textBuffer") {
        @Override
        protected void invalidated() {
            // invalidate undo/redo stack
            commandManager.clearStacks();
            undoStackEmptyProperty.set(true);
            redoStackEmptyProperty.set(true);
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
                    setDecoration(decorationAtCaret);
                }
            }
        }
    };
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
                    new Selection(value.getStart(), getTextLength()): value;
            }
            super.set(selection);
        }

        @Override
        protected void invalidated() {
            if (!get().isDefined()) {
                setDecoration(getTextBuffer().getDecorationAtCaret(getCaretPosition()));
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
    final ReadOnlyBooleanWrapper undoStackEmptyProperty = new ReadOnlyBooleanWrapper(this, "undoStackEmpty", true);
    ReadOnlyBooleanProperty undoStackEmptyProperty() {
        return undoStackEmptyProperty.getReadOnlyProperty();
    }
    boolean isUndoStackEmpty() {
        return undoStackEmptyProperty.get();
    }

    // redoStackSizeProperty
    final ReadOnlyBooleanWrapper redoStackEmptyProperty = new ReadOnlyBooleanWrapper(this, "redoStackEmpty", true);
    ReadOnlyBooleanProperty redoStackEmptyProperty() {
        return redoStackEmptyProperty.getReadOnlyProperty();
    }
    boolean isRedoStackEmpty() {
        return redoStackEmptyProperty.get();
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

    // textDecorationProperty
    private final ObjectProperty<Decoration> decorationProperty = new SimpleObjectProperty<>(this, "decoration") {
        @Override
        protected void invalidated() {
            if (get() instanceof TextDecoration && !getSelection().isDefined()) {
                getTextBuffer().setDecorationAtCaret((TextDecoration) get());
            }
        }
    };
    public final ObjectProperty<Decoration> decorationProperty() {
       return decorationProperty;
    }
    public final Decoration getDecoration() {
       return decorationProperty.get();
    }
    public final void setDecoration(Decoration value) {
        decorationProperty.set(value);
    }

    public RichTextAreaViewModel(BiFunction<Double, Boolean, Integer> getNextRowPosition) {
        this.getNextRowPosition = Objects.requireNonNull(getNextRowPosition);
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
        } else {
            getTextBuffer().insert(text, caretPosition);
        }
        moveCaretPosition(text.length());
    }

    void remove(int caretOffset) {
        if (!removeSelection()) {
            int position = getCaretPosition() + caretOffset;
            if (position >= 0 && position <= getTextLength()) {
                getTextBuffer().delete(position, 1);
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
            content.putString(selectedText);
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

    void clipboardPaste() {
        if (clipboardHasImage()) {
            final Image image = Clipboard.getSystemClipboard().getImage();
            if (image != null) {
                String url = image.getUrl() != null ? image.getUrl() : Clipboard.getSystemClipboard().getUrl();
                if (url != null) {
                    commandManager.execute(new DecorateCmd(new ImageDecoration(url)));
                }
            }
        } else if (clipboardHasString()) {
            final String text = Clipboard.getSystemClipboard().getString();
            if (text != null) {
                commandManager.execute(new InsertTextCmd(text));
            }
        }
    }

    void moveCaret(Direction direction, boolean changeSelection, boolean wordSelection, boolean lineSelection, boolean paragraphSelection) {
        Selection prevSelection = getSelection();
        int prevCaretPosition = getCaretPosition();
        switch (direction) {
            case FORWARD:
                if (wordSelection) {
                    nextWord(c -> c != ' ' && c != '\t');
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
                    int rowCharIndex = getNextRowPosition.apply(-1d, Direction.DOWN == direction);
                    if (rowCharIndex >= 0) {
                        setCaretPosition(rowCharIndex);
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

    public void walkFragments(BiConsumer<String, Decoration> onFragment) {
        getTextBuffer().resetCharacterIterator();
        getTextBuffer().walkFragments(onFragment);
        LOGGER.log(Level.FINE, getTextBuffer().toString());
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

    private void previousWord() {
        int textLength = getTextLength();
        if (textLength <= 0) {
            return;
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
        setCaretPosition(Tools.clamp(0, position, textLength));
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
        int pos = getNextRowPosition.apply(0d, false);
        setCaretPosition(pos);
    }

    private void lineEnd() {
        int pos = getNextRowPosition.apply(Double.MAX_VALUE, false);
        setCaretPosition(pos);
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
        undoStackEmptyProperty.set(commandManager.isUndoStackEmpty());
        redoStackEmptyProperty.set(commandManager.isRedoStackEmpty());
    }
}
