package com.gluonhq.richtext.model;

import javafx.beans.property.ReadOnlyIntegerProperty;

import java.text.CharacterIterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface TextBuffer {

    int getTextLength();
    ReadOnlyIntegerProperty textLengthProperty();
    String getText();
    String getText(int start, int end);

    CharacterIterator getCharacterIterator();
    char charAt(int pos);
    void resetCharacterIterator();

    void insert(String text, int insertPosition);
    void append(String text);
    void delete(final int deletePosition, int length);

    /**
     * Adds decoration to Text in the specified range.
     * @param start index to start, inclusive.
     * @param end index to end, exclusive.
     * @param decoration decoration to apply.
     */
    void decorate(int start, int end, Decoration decoration);

    void undo();
    void redo();

    void walkFragments(BiConsumer<String, Decoration> onFragment);

    void addChangeListener(Consumer<TextBuffer.Event> listener);
    void removeChangeListener(Consumer<TextBuffer.Event> listener);

    // TextDecoration getDecorationForSelection(int startPos, int endPos);
    Decoration getDecorationAtCaret(int caretPosition);
    void setDecorationAtCaret(TextDecoration decoration);

    interface Event {}

    class InsertEvent implements Event {

        private final String text;
        private final int position;

        InsertEvent(String text, int position) {
            this.text = text;
            this.position = position;
        }

        public String getText() {
            return text;
        }

        public int getPosition() {
            return position;
        }
    }

    class DeleteEvent implements Event {

        private final int position;
        private final int length;

        DeleteEvent(int position, int length) {
            this.position = position;
            this.length = length;
        }

        public int getPosition() {
            return position;
        }

        public int getLength() {
            return length;
        }
    }

    class DecorateEvent implements Event {

        private final int start;
        private final int end;
        private final Decoration decoration;

        DecorateEvent(int start, int end, Decoration decoration) {
            this.start = start;
            this.end = end;
            this.decoration = decoration;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public Decoration getDecoration() {
            return decoration;
        }
    }

}

