/*
 * Copyright (c) 2023, Gluon
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
package com.gluonhq.richtextarea.model;

import com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TableTests {

    private static final TableDecoration tableDecoration = new TableDecoration(1, 3);
    private static final Document FACE_MODEL = new Document(
            "One\u200bText\u200bname!\u200bend\n",
            List.of(new DecorationModel(0, 19,
                    TextDecoration.builder().presets().build(),
                    ParagraphDecoration.builder().tableDecoration(tableDecoration).build())),
            0);

    private static final Document FACE_MODEL_WITH_EMOJI = new Document(
            "One \ud83d\ude00\u200bText\u200bname!\u200bend\n",
            List.of(new DecorationModel(0, 22,
                    TextDecoration.builder().presets().build(),
                    ParagraphDecoration.builder().tableDecoration(tableDecoration).build())),
            0);

    @Test
    @DisplayName("Table: table with text")
    public void originalTextIntact() {
        PieceTable pt = new PieceTable(FACE_MODEL);
        Assertions.assertEquals(FACE_MODEL.getText(), pt.getText());
        Table table = new Table(pt.originalText, 0, 1, 4);
        Assertions.assertEquals(pt.getTextLength() - 1, table.getTableTextLength()); // 18
    }

    @Test
    @DisplayName("Table: table with text and emoji")
    public void originalTextIntactWithEmoji() {
        PieceTable pt = new PieceTable(FACE_MODEL_WITH_EMOJI);
        Assertions.assertEquals(FACE_MODEL_WITH_EMOJI.getText(), pt.getText());
        Table table = new Table(pt.originalText, 0, 1, 4);
        Assertions.assertEquals(pt.getTextLength() - 1, table.getTableTextLength()); // 21
    }

    @Test
    @DisplayName("Table: table with four columns")
    public void tablePositions1x4() {
        PieceTable pt = new PieceTable(FACE_MODEL);
        Assertions.assertEquals(FACE_MODEL.getText(), pt.getText());
        Table table = new Table(pt.originalText, 0, 1, 4);
        List<Integer> tablePositions = table.getTablePositions();
        Assertions.assertEquals(table.getRows() * table.getColumns(), tablePositions.size()); // 4
        Assertions.assertEquals("[3, 8, 14, 18]", tablePositions.toString());
        int col = 0;
        for (int i = 0; i <= 18; i++) {
            Assertions.assertEquals(0, table.getCurrentRow(i));
            Assertions.assertEquals(col, table.getCurrentColumn(i));
            if (i == tablePositions.get(col)) {
                col++;
            }
        }
    }

    @Test
    @DisplayName("Table: table with four columns and emoji")
    public void tableEmojiPositions1x4() {
        PieceTable pt = new PieceTable(FACE_MODEL_WITH_EMOJI);
        Assertions.assertEquals(FACE_MODEL_WITH_EMOJI.getText(), pt.getText());
        Table table = new Table(pt.originalText, 0, 1, 4);
        List<Integer> tablePositions = table.getTablePositions();
        Assertions.assertEquals(table.getRows() * table.getColumns(), tablePositions.size()); // 4
        Assertions.assertEquals("[5, 10, 16, 20]", tablePositions.toString());
        int col = 0;
        for (int i = 0; i <= 20; i++) {
            Assertions.assertEquals(0, table.getCurrentRow(i));
            Assertions.assertEquals(col, table.getCurrentColumn(i));
            if (i == tablePositions.get(col)) {
                col++;
            }
        }
    }

    @Test
    @DisplayName("Table: 2x2 table")
    public void tablePositions2x2() {
        PieceTable pt = new PieceTable(FACE_MODEL);
        Assertions.assertEquals(FACE_MODEL.getText(), pt.getText());
        Table table = new Table(pt.originalText, 0, 2, 2);
        List<Integer> tablePositions = table.getTablePositions();
        Assertions.assertEquals(table.getRows() * table.getColumns(), tablePositions.size()); // 4
        Assertions.assertEquals("[3, 8, 14, 18]", tablePositions.toString());
        for (int i = 0; i <= 18; i++) {
            Assertions.assertEquals(i <= tablePositions.get(1) ? 0 : 1, table.getCurrentRow(i));
            Assertions.assertEquals(i <= tablePositions.get(0) ||
                    (tablePositions.get(1) < i && i <= tablePositions.get(2)) ? 0 : 1, table.getCurrentColumn(i));
        }
    }

    @Test
    @DisplayName("Table: 2x2 table with emoji")
    public void tableEmojiPositions2x2() {
        PieceTable pt = new PieceTable(FACE_MODEL_WITH_EMOJI);
        Assertions.assertEquals(FACE_MODEL_WITH_EMOJI.getText(), pt.getText());
        Table table = new Table(pt.originalText, 0, 2, 2);
        List<Integer> tablePositions = table.getTablePositions();
        Assertions.assertEquals(table.getRows() * table.getColumns(), tablePositions.size()); // 4
        Assertions.assertEquals("[5, 10, 16, 20]", tablePositions.toString());
        for (int i = 0; i <= 21; i++) {
            Assertions.assertEquals(i <= tablePositions.get(1) ? 0 : 1, table.getCurrentRow(i));
            Assertions.assertEquals(i <= tablePositions.get(0) ||
                    (tablePositions.get(1) < i && i <= tablePositions.get(2)) ? 0 : 1, table.getCurrentColumn(i));
        }
    }

    @Test
    @DisplayName("Table: 2x2 table + column")
    public void addColumnToTable2x2() {
        PieceTable pt = new PieceTable(FACE_MODEL);
        Assertions.assertEquals(FACE_MODEL.getText(), pt.getText());
        Table table = new Table(pt.originalText, 0, 2, 2);
        UnitBuffer newBuffer = table.addColumnAndGetTableText(0, RichTextAreaViewModel.Direction.BACK);
        Assertions.assertEquals("\u200bOne\u200bText\u200b\u200bname!\u200bend", newBuffer.getText());
        newBuffer = table.addColumnAndGetTableText(0, RichTextAreaViewModel.Direction.FORWARD);
        Assertions.assertEquals("One\u200b\u200bText\u200bname!\u200b\u200bend", newBuffer.getText());
        newBuffer = table.addColumnAndGetTableText(4, RichTextAreaViewModel.Direction.FORWARD);
        Assertions.assertEquals("One\u200bText\u200b\u200bname!\u200bend\u200b", newBuffer.getText());
    }

    @Test
    @DisplayName("Table: 2x2 table with emoji + column")
    public void addColumnToTableEmoji2x2() {
        PieceTable pt = new PieceTable(FACE_MODEL_WITH_EMOJI);
        Assertions.assertEquals(FACE_MODEL_WITH_EMOJI.getText(), pt.getText());
        Table table = new Table(pt.originalText, 0, 2, 2);
        UnitBuffer newBuffer = table.addColumnAndGetTableText(0, RichTextAreaViewModel.Direction.BACK);
        Assertions.assertEquals("\u200bOne \ud83d\ude00\u200bText\u200b\u200bname!\u200bend", newBuffer.getText());
        newBuffer = table.addColumnAndGetTableText(0, RichTextAreaViewModel.Direction.FORWARD);
        Assertions.assertEquals("One \ud83d\ude00\u200b\u200bText\u200bname!\u200b\u200bend", newBuffer.getText());
        newBuffer = table.addColumnAndGetTableText(7, RichTextAreaViewModel.Direction.FORWARD);
        Assertions.assertEquals("One \ud83d\ude00\u200bText\u200b\u200bname!\u200bend\u200b", newBuffer.getText());
    }

    @Test
    @DisplayName("Table: 2x2 table - column")
    public void removeColumnFromTable2x2() {
        PieceTable pt = new PieceTable(FACE_MODEL);
        Assertions.assertEquals(FACE_MODEL.getText(), pt.getText());
        Table table = new Table(pt.originalText, 0, 2, 2);
        UnitBuffer newBuffer = table.removeColumnAndGetText(0);
        Assertions.assertEquals("Text\u200bend", newBuffer.getText());
        newBuffer = table.removeColumnAndGetText(4);
        Assertions.assertEquals("One\u200bname!", newBuffer.getText());
    }

    @Test
    @DisplayName("Table: 2x2 table with emoji - column")
    public void removeColumnFromTableEmoji2x2() {
        PieceTable pt = new PieceTable(FACE_MODEL_WITH_EMOJI);
        Assertions.assertEquals(FACE_MODEL_WITH_EMOJI.getText(), pt.getText());
        Table table = new Table(pt.originalText, 0, 2, 2);
        UnitBuffer newBuffer = table.removeColumnAndGetText(0);
        Assertions.assertEquals("Text\u200bend", newBuffer.getText());
        newBuffer = table.removeColumnAndGetText(7);
        Assertions.assertEquals("One \ud83d\ude00\u200bname!", newBuffer.getText());
    }

}
