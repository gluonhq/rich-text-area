package com.gluonhq.richtext.model;

public interface TextChangeListener {
    void onInsert( String text, int position );
    void onDelete( int position, int length );
}
