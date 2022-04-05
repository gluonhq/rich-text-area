package com.gluonhq.richtextarea;

import javafx.scene.control.IndexRange;

public class Tools {

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

    private static final String os = System.getProperty("os.name");
    public static final boolean WINDOWS = os.startsWith("Windows");
    public static final boolean MAC = os.startsWith("Mac");
    public static final boolean LINUX = os.startsWith("Linux");

    public static int clamp(int min, int value, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    public static String getFirstLetter(String name) {
        if (name == null || name.isEmpty()) {
            return "-";
        }
        return name.substring(0, 1);
    }
}
