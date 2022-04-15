package com.gluonhq.richtextarea.model;

import javafx.scene.text.TextAlignment;

import java.util.Arrays;
import java.util.Objects;

/**
 * TableDecoration is a {@link Decoration} that can be applied to a paragrpah in order to place
 * a table with a number of rows and columns where text can be added, with a given text alignment
 * defined per table cell.
 */
public class TableDecoration implements Decoration {

    public static final String TABLE_SEPARATOR = "table_separator";

    private final int rows;
    private final int columns;
    private final TextAlignment[][] cellAlignment;

    public TableDecoration() {
        this(0, 0, null);
    }

    public TableDecoration(int rows, int columns) {
        this(rows, columns, null);
    }

    public TableDecoration(int rows, int columns, TextAlignment[][] cellAlignment) {
        this.rows = rows;
        this.columns = columns;
        TextAlignment[][] defaultCellAlignment = new TextAlignment[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                defaultCellAlignment[i][j] = TextAlignment.LEFT;
            }
        }
        this.cellAlignment = cellAlignment != null ? cellAlignment : defaultCellAlignment;
    }

    /**
     * Returns the number of rows that define the table
     *
     * @return the number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Returns the number of columns that define the table
     *
     * @return the number of columns
     */
    public int getColumns() {
        return columns;
    }

    /**
     * Returns a two-dimensional array with the text alignment for each row and column.
     *
     * By default, the text alignment is set to {@link TextAlignment#LEFT}.
     *
     * @return the {@link TextAlignment} for each cell
     */
    public TextAlignment[][] getCellAlignment() {
        return cellAlignment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableDecoration that = (TableDecoration) o;
        return rows == that.rows && columns == that.columns && Arrays.deepEquals(cellAlignment, that.cellAlignment);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(rows, columns);
        result = 31 * result + Arrays.deepHashCode(cellAlignment);
        return result;
    }

    @Override
    public String toString() {
        return "TabDec[" + rows + " x " + columns + "] - " + Arrays.deepToString(cellAlignment);
    }
}
