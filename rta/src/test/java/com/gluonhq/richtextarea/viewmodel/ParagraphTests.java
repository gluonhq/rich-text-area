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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author johan
 */
public class ParagraphTests {

    @Test
    public void testEmptyList() {
        RichTextAreaViewModel viewModel = new RichTextAreaViewModel(this::getNextRowPosition, this::getNextTableCellPosition);
        Document document = new Document();
        PieceTable pieceTable = new PieceTable(document);
        viewModel.setTextBuffer(pieceTable);
        viewModel.updateParagraphList();
        ObservableList<Paragraph> paragraphList1 = viewModel.getParagraphList();
        assertEquals(1, paragraphList1.size());
        Paragraph first1 = paragraphList1.get(0);
        assertEquals(0, first1.getStart());
        assertEquals(0, first1.getEnd());
        assertEquals(paragraphList1.get(0), viewModel.lastParagraph);
    }

    @Test
    public void testAddElements() {
        RichTextAreaViewModel viewModel = new RichTextAreaViewModel(this::getNextRowPosition, this::getNextTableCellPosition);
        Document document = new Document();
        PieceTable pieceTable = new PieceTable(document);
        viewModel.setTextBuffer(pieceTable);
        for (int i = 0; i < 10; i++) {
            appendAndUpdate(pieceTable, viewModel, Integer.toString(i));
        }
        assertEquals(1, viewModel.getParagraphList().size());
        assertEquals(viewModel.getParagraphList().get(0), viewModel.lastParagraph);

        pieceTable.append("\n");
        pieceTable.resetCharacterIterator();
        assertEquals(1, viewModel.getParagraphList().size());
        assertEquals(viewModel.getParagraphList().get(0), viewModel.lastParagraph);
        viewModel.updateParagraphList();
        assertEquals(2, viewModel.getParagraphList().size());
        for (int i = 0; i < 10; i++) {
            appendAndUpdate(pieceTable, viewModel, Integer.toString(i));
        }
        assertEquals(2, viewModel.getParagraphList().size());
        assertEquals(viewModel.getParagraphList().get(1), viewModel.lastParagraph);
    }

    @Test
    public void testAddRemoveLineBreaks() {
        RichTextAreaViewModel viewModel = new RichTextAreaViewModel(this::getNextRowPosition, this::getNextTableCellPosition);
        Document document = new Document();
        PieceTable pieceTable = new PieceTable(document);
        viewModel.setTextBuffer(pieceTable);
        appendAndUpdate(pieceTable, viewModel, "Hello\n");
        assertEquals(2, viewModel.getParagraphList().size());
        assertEquals(viewModel.getParagraphList().get(1), viewModel.lastParagraph);

        pieceTable.delete(5, 1);
        pieceTable.resetCharacterIterator();
        viewModel.updateParagraphList();
        assertEquals(1, viewModel.getParagraphList().size());
        assertEquals(viewModel.getParagraphList().get(0), viewModel.lastParagraph);
    }

    @Test
    public void testUpdate() {
        RichTextAreaViewModel viewModel = new RichTextAreaViewModel(this::getNextRowPosition, this::getNextTableCellPosition);
        Document document = new Document();
        PieceTable pieceTable = new PieceTable(document);
        viewModel.setTextBuffer(pieceTable);
        viewModel.updateParagraphList();
        ObservableList<Paragraph> paragraphList1 = viewModel.getParagraphList();
        Paragraph first1 = paragraphList1.get(0);

        String text = "Hello";
        pieceTable.append(text);
        pieceTable.resetCharacterIterator();
        viewModel.updateParagraphList();
        ObservableList<Paragraph> paragraphList2 = viewModel.getParagraphList();
        assertEquals(1, paragraphList1.size());
        Paragraph first2 = paragraphList2.get(0);
        assertEquals(0, first2.getStart());
        assertEquals(text.length(), first2.getEnd());

        pieceTable.append("\nWorld");
        pieceTable.resetCharacterIterator();
        viewModel.updateParagraphList();
        ObservableList<Paragraph> paragraphList3 = viewModel.getParagraphList();
        Paragraph first3 = paragraphList3.get(0);
        assertEquals(first1, first3, "A new paragraph was created instead of reused");

        pieceTable.delete(2, 6);
        pieceTable.resetCharacterIterator();
        viewModel.updateParagraphList();
        ObservableList<Paragraph> paragraphList4 = viewModel.getParagraphList();
        assertEquals(1, paragraphList4.size());
        assertEquals(viewModel.getParagraphList().get(0), viewModel.lastParagraph);
    }

    @Test
    public void testInternalUpdate() throws InterruptedException {
        RichTextAreaViewModel viewModel = new RichTextAreaViewModel(this::getNextRowPosition, this::getNextTableCellPosition);
        Document document = new Document();
        PieceTable pieceTable = new PieceTable(document);
        viewModel.setTextBuffer(pieceTable);
        viewModel.updateParagraphList();
        ObservableList<Paragraph> paragraphList1 = viewModel.getParagraphList();
        assertEquals(1, paragraphList1.size());
        Paragraph first1 = paragraphList1.get(0);
        assertEquals(0, first1.getStart());
        assertEquals(0, first1.getEnd());

        pieceTable.append("Hello\nWorld\nLine 3");
        pieceTable.resetCharacterIterator();
        viewModel.updateParagraphList();
        ObservableList<Paragraph> paragraphList3 = viewModel.getParagraphList();
        assertEquals(3, paragraphList3.size());
        assertEquals(6, paragraphList3.get(0).getEnd());

        pieceTable.insert("Z", 1);
        pieceTable.resetCharacterIterator();
        viewModel.updateParagraphList();
        assertEquals(7, paragraphList3.get(0).getEnd());

        CountDownLatch cdl = new CountDownLatch(1);
        AtomicInteger ai = new AtomicInteger(0);
        paragraphList3.addListener(new ListChangeListener<Paragraph>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Paragraph> change) {
                ai.incrementAndGet();
                cdl.countDown();
            }
        });
        pieceTable.insert("Y", 10);
        pieceTable.resetCharacterIterator();
        viewModel.updateParagraphList();
        boolean await = cdl.await(1, TimeUnit.SECONDS);
        assertTrue(await, "No callback received after making changes to paragraphs");
        assertEquals(2, ai.get(), "paragraph 2 and 3 should be changed, but we noticed "+ai.get()+" changes");
    }

    private void appendAndUpdate(PieceTable pieceTable, RichTextAreaViewModel viewModel, String text) {
        pieceTable.append(text);
        pieceTable.resetCharacterIterator();
        viewModel.updateParagraphList();
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
