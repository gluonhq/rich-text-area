/*
 * Copyright (C) 2025 Gluon
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
 */
package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.Paragraph;
import com.gluonhq.richtextarea.model.PieceTable;
import com.gluonhq.richtextarea.model.TextBuffer;
import com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel;
import javafx.collections.ObservableList;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author johan
 */
public class ParagraphTests {

    @Test
    public void testUpdate() {
        RichTextAreaViewModel viewModel = new RichTextAreaViewModel(this::getNextRowPosition, this::getNextTableCellPosition);
        Document document = new Document();
        PieceTable pieceTable = new PieceTable(document);
        viewModel.setTextBuffer(pieceTable);
        TextBuffer textBuffer = viewModel.getTextBuffer();
        viewModel.updateParagraphList();
        ObservableList<Paragraph> paragraphList1 = viewModel.getParagraphList();
        assertEquals(1, paragraphList1.size());
        Paragraph first1 = paragraphList1.get(0);
        assertEquals(0, first1.getStart());
        assertEquals(0, first1.getEnd());
        
        
        String text = "Hello";
        pieceTable.append(text);
        pieceTable.resetCharacterIterator();
        viewModel.updateParagraphList();
        ObservableList<Paragraph> paragraphList2 = viewModel.getParagraphList();
        assertEquals(1, paragraphList1.size());
        Paragraph first2 = paragraphList2.get(0);
        assertEquals(0, first2.getStart());
        assertEquals(text.length(), first2.getEnd());
        
        System.err.println("PL2 = "+paragraphList2);
        pieceTable.append("\nWorld");
        pieceTable.resetCharacterIterator();
        viewModel.updateParagraphList();
        ObservableList<Paragraph> paragraphList3 = viewModel.getParagraphList();
        System.err.println("PL3 = "+paragraphList3);
        Paragraph first3 = paragraphList3.get(0);
        int hash1 = System.identityHashCode(first1);
     //   assertEquals(first1, first3);

        textBuffer.delete(2, 6);
        System.err.println("TB = "+textBuffer.getText());
        pieceTable.resetCharacterIterator();
        viewModel.updateParagraphList();
        ObservableList<Paragraph> paragraphList4 = viewModel.getParagraphList();
        System.err.println("PL4 = "+paragraphList4);
        assertEquals(1, paragraphList4.size());
    }

    private int getNextRowPosition(double x, Boolean down) {
        Thread.dumpStack();
        return -1;
    }

    private int getNextTableCellPosition(Boolean down) {
        Thread.dumpStack();
        return -1;
    }
}
