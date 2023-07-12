package com.gluonhq.richtextarea.model;

/*
 * Copyright (c) 2022, Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GLUON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
import javafx.scene.text.TextAlignment;

import java.util.Arrays;
import java.util.Objects;

/**
 * TableDecoration is a {@link Decoration} that can be applied to a paragraph in order to place
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

    public static TableDecoration fromTableDecorationInsertingRow(TableDecoration tableDecoration, int row) {
        int rows = tableDecoration.getRows();
        int columns = tableDecoration.getColumns();
        TextAlignment[][] newCellAlignment = new TextAlignment[rows + 1][columns];
        for (int i = 0; i < rows + 1; i++) {
            int rowIndex = i > row ? i - 1 : i;
            for (int j = 0; j < columns; j++) {
                newCellAlignment[i][j] = i == row ?
                        TextAlignment.LEFT : tableDecoration.getCellAlignment()[rowIndex][j];
            }
        }
        return new TableDecoration(rows + 1, columns, newCellAlignment);
    }

    public static TableDecoration fromTableDecorationDeletingRow(TableDecoration tableDecoration, int row) {
        int rows = tableDecoration.getRows();
        int columns = tableDecoration.getColumns();
        TextAlignment[][] newCellAlignment = new TextAlignment[rows - 1][columns];
        for (int i = 0; i < rows; i++) {
            int rowIndex = i;
            if (i == row) {
                continue;
            } else if (i > row) {
                rowIndex = i - 1;
            }
            for (int j = 0; j < columns; j++) {
                newCellAlignment[rowIndex][j] = tableDecoration.getCellAlignment()[i][j];
            }
        }
        return new TableDecoration(rows - 1, columns, newCellAlignment);
    }

    public static TableDecoration fromTableDecorationInsertingColumn(TableDecoration tableDecoration, int column) {
        int rows = tableDecoration.getRows();
        int columns = tableDecoration.getColumns();
        TextAlignment[][] newCellAlignment = new TextAlignment[rows][columns + 1];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns + 1; j++) {
                int colIndex = j > column ? j - 1 : j;
                newCellAlignment[i][j] = j == column ?
                        TextAlignment.LEFT : tableDecoration.getCellAlignment()[i][colIndex];
            }
        }
        return new TableDecoration(rows, columns + 1, newCellAlignment);
    }

    public static TableDecoration fromTableDecorationDeletingColumn(TableDecoration tableDecoration, int column) {
        int rows = tableDecoration.getRows();
        int columns = tableDecoration.getColumns();
        TextAlignment[][] newCellAlignment = new TextAlignment[rows][columns - 1];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                int colIndex = j;
                if (j == column) {
                    continue;
                } else if (j > column) {
                    colIndex = j - 1;
                }
                newCellAlignment[i][colIndex] = tableDecoration.getCellAlignment()[i][j];
            }
        }
        return new TableDecoration(rows, columns - 1, newCellAlignment);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableDecoration that = (TableDecoration) o;
        return rows == that.rows &&
                columns == that.columns &&
                Arrays.deepEquals(cellAlignment, that.cellAlignment);
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
