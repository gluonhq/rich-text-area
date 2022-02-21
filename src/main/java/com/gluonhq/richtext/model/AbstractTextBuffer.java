package com.gluonhq.richtext.model;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public abstract class AbstractTextBuffer implements TextBuffer {

    private final Set<Consumer<Event>> listeners = new HashSet<>();

    // textLengthProperty
    final ReadOnlyIntegerWrapper textLengthProperty = new ReadOnlyIntegerWrapper(this, "textLength");
    public ReadOnlyIntegerProperty textLengthProperty() {
        return textLengthProperty.getReadOnlyProperty();
    }
    public int getTextLength() {
        return textLengthProperty.get();
    }

    // undoStackSizeProperty
    final ReadOnlyIntegerWrapper undoStackSizeProperty = new ReadOnlyIntegerWrapper(this, "undoStackSize");
    public ReadOnlyIntegerProperty undoStackSizeProperty() {
        return undoStackSizeProperty.getReadOnlyProperty();
    }
    public int getUndoStackSize() {
        return undoStackSizeProperty.get();
    }

    // redoStackSizeProperty
    final ReadOnlyIntegerWrapper redoStackSizeProperty = new ReadOnlyIntegerWrapper(this, "redoStackSize");
    public ReadOnlyIntegerProperty redoStackSizeProperty() {
        return redoStackSizeProperty.getReadOnlyProperty();
    }
    public int getRedoStackSize() {
        return redoStackSizeProperty.get();
    }

    public final void addChangeListener(Consumer<TextBuffer.Event> listener) {
        listeners.add(Objects.requireNonNull(listener));
    }

    public final void removeChangeListener(Consumer<TextBuffer.Event> listener) {
        listeners.remove(Objects.requireNonNull(listener));
    }

    protected void fire( TextBuffer.Event event ) {
        listeners.forEach(l -> l.accept(event));
    }

}