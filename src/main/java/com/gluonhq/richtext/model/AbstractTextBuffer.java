package com.gluonhq.richtext.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract class AbstractTextBuffer implements TextBuffer {

    private final Set<TextChangeListener> listeners = new HashSet<>();

    public final void addChangeListener(TextChangeListener listener) {
        listeners.add(Objects.requireNonNull(listener));
    }

    public final void removeChangeListener(TextChangeListener listener) {
        listeners.remove(Objects.requireNonNull(listener));
    }

    protected void fireInsert(String text, int position) {
        listeners.forEach(l -> l.onInsert(text, position));
    }

    protected void fireDelete(int position, int length) {
        listeners.forEach(l -> l.onDelete(position, length));
    }

}