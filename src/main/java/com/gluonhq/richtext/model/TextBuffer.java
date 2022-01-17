package com.gluonhq.richtext.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public interface TextBuffer {

    int getTextLength();
    String getText();

    void insert( String text, int insertPosition );
    void append( String text );
    void delete( final int deletePosition, int length );

    void addChangeListener(TextChangeListener listener);
    void removeChangeListener(TextChangeListener listener);
}

abstract class AbstractTextBuffer implements TextBuffer {

    private final Set<TextChangeListener> listeners = new HashSet<>();

    @Override
    public final void addChangeListener( TextChangeListener listener ) {
        listeners.add( Objects.requireNonNull(listener) );
    }

    @Override
    public final void removeChangeListener( TextChangeListener listener ) {
        listeners.remove( Objects.requireNonNull(listener) );
    }

    protected void fireInsert(String text, int position) {
        listeners.forEach( l -> l.onInsert(text, position));
    }

    protected void fireDelete(int position, int length) {
        listeners.forEach( l -> l.onDelete(position, length));
    }

}
