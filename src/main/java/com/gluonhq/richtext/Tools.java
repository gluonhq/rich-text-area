package com.gluonhq.richtext;

import javafx.scene.control.IndexRange;

class Tools {

    private Tools() {}

    public static IndexRange NO_SELECTION = new IndexRange(-1,-1);

    public static boolean isIndexRangeValid( IndexRange range ) {
        return range.getStart() >= 0 && range.getEnd() >= 0;
    }

    public static String insertText( String text, int position, String textToInsert ) {
        return new StringBuilder(text).insert(position, textToInsert).toString();
    }

    public static String deleteText( String text, int start, int end) {
        return new StringBuilder(text).delete(start, end).toString();
    }

}
