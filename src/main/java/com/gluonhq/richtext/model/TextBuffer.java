package com.gluonhq.richtext.model;

import java.util.function.BiConsumer;

public interface TextBuffer {

    int getTextLength();
    String getText();

    void insert( String text, int insertPosition );
    void append( String text );
    void delete( final int deletePosition, int length );

    void walkFragments(BiConsumer<String, TextDecoration> onFragment);

    void addChangeListener(TextChangeListener listener);
    void removeChangeListener(TextChangeListener listener);
}

