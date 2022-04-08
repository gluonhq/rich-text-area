package com.gluonhq.richtextarea;

import javafx.scene.control.IndexRange;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

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

    private static final Text helperText = new Text();
    private static final double TEXT_WRAPPING_WIDTH = helperText.getWrappingWidth();
    private static final double TEXT_LINE_SPACING = helperText.getLineSpacing();
    private static final String TEXT_CONTENT = helperText.getText();
    private static final TextBoundsType TEXT_BOUNDS_TYPE = helperText.getBoundsType();

    public static double computeStringWidth(Font font, String text) {
        helperText.setText(text);
        helperText.setFont(font);
        helperText.setWrappingWidth(0);
        helperText.setLineSpacing(0);
        double width = Math.min(helperText.prefWidth(-1), Double.MAX_VALUE);
        helperText.setWrappingWidth((int) Math.ceil(width));
        width = helperText.getLayoutBounds().getWidth();
        helperText.setWrappingWidth(TEXT_WRAPPING_WIDTH);
        helperText.setLineSpacing(TEXT_LINE_SPACING);
        helperText.setText(TEXT_CONTENT);
        return width;
    }

    public static double computeStringHeight(Font font, String text) {
        helperText.setText(text);
        helperText.setFont(font);
        helperText.setWrappingWidth((int) Double.MAX_VALUE);
        helperText.setLineSpacing(0);
        helperText.setBoundsType(TextBoundsType.LOGICAL);
        final double height = helperText.getLayoutBounds().getHeight();
        helperText.setWrappingWidth(TEXT_WRAPPING_WIDTH);
        helperText.setLineSpacing(TEXT_LINE_SPACING);
        helperText.setText(TEXT_CONTENT);
        helperText.setBoundsType(TEXT_BOUNDS_TYPE);
        return height;
    }
}
