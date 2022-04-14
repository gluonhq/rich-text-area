package com.gluonhq.richtextarea.model;

import javafx.scene.text.TextAlignment;

import java.util.Arrays;
import java.util.Objects;

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

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

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
