package com.gluonhq.richtext.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PieceTableTests {

    private static final String originalText = "Original Text";

    @Test
    @DisplayName("Original text is intact")
    public void originalTextIntact() {
        PieceTable pt = new PieceTable(originalText);
        Assertions.assertEquals( originalText, pt.getText());
    }

    @Test
    @DisplayName("Text append")
    public void textAppend() {
        String appended = " and some";
        PieceTable pt = new PieceTable(originalText);
        pt.append(appended);
        Assertions.assertEquals( originalText+appended, pt.getText());
    }

    @Test
    @DisplayName("Text insert")
    public void textInsert() {
        String insert = "Bigger ";
        PieceTable pt = new PieceTable(originalText);
        pt.insert(insert, 9);
        //"Original Bigger Text"
        Assertions.assertEquals(
            new StringBuilder(originalText).insert(9, insert).toString(),
            pt.getText()
        );
    }

    @Test
    @DisplayName("Text insert at the end is converted to append")
    public void insertAtTheEnd() {
        String insert = " and more";
        PieceTable pt = new PieceTable(originalText);
        pt.insert(insert, 13);
        Assertions.assertEquals( originalText+insert, pt.getText());
    }

    @Test
    @DisplayName("Text insert at wrong position throws exception")
    public void insertAtInvalidPositionFails() {
        PieceTable pt = new PieceTable(originalText);
        Assertions.assertThrows( IllegalArgumentException.class, () -> pt.insert("xxx", 100));
        Assertions.assertThrows( IllegalArgumentException.class, () -> pt.insert("xxx", -1));
    }

    @Test
    @DisplayName("Same block delete")
    public void sameBlockDelete() {
        String text = "Original Bigger Text";
        PieceTable pt = new PieceTable(text);
        pt.delete(9,7);
        Assertions.assertEquals(
            new StringBuilder(text).delete(9, 9+7).toString(),
            pt.getText()
        );
    }

    @Test
    @DisplayName("Multi block delete")
    public void multiBlockDelete() {
        PieceTable pt = new PieceTable(originalText);
        String append = "um";
        String insert = "Bigger ";
        pt.append(append); // 'Original Textum'
        pt.insert( insert, 9); // 'Original Bigger Textum'
        pt.insert( insert, 9); // 'Original Bigger Bigger Textum'
        pt.delete(9, 14);
        Assertions.assertEquals(
                new StringBuilder(originalText)
                        .append(append)
                        .insert(9, insert)
                        .insert(9, insert)
                        .delete( 9, 9+14)
                        .toString(),
            pt.getText());
    }


    @Test
    @DisplayName("Text delete beyond text length")
    public void deleteBeyondLength() {
        String text = "Original Bigger Text";
        PieceTable pt = new PieceTable(text);
        pt.delete(8,107);
        Assertions.assertEquals(
                new StringBuilder(text).delete(8,8+107).toString(),
                pt.getText());
    }

    @Test
    @DisplayName("Text delete at wrong position throws exception")
    public void deleteAtInvalidPositionFails() {
        PieceTable pt = new PieceTable(originalText);
        Assertions.assertThrows( IllegalArgumentException.class, () -> pt.delete(-1, 100));
    }

//    @Test
//    @DisplayName("Block line stops generated correctly")
//    public void lineStopsGeneratedCorrectly() {
//        PieceTable pt = new PieceTable(originalText);
//        Piece piece = pt.appendTextInternal(" \n \n \n   ");
//        Assertions.assertArrayEquals( new int[]{1,3,5}, piece.lineStops );
//    }

    @Test
    @DisplayName("Events are fired")
    public void eventsAreFired() {

        final int[] insertCount = {0};
        final int[] deleteCount = {0};

        class ChangeListener implements TextChangeListener {
            @Override
            public void onInsert(String text, int position) {
                insertCount[0]++;
            }

            @Override
            public void onDelete(int position, int length) {
                deleteCount[0]++;
            }
        }

        PieceTable pt = new PieceTable(originalText);
        pt.addChangeListener( new ChangeListener());
        pt.addChangeListener( new ChangeListener());

        pt.insert( "XXX", 9);
        pt.delete(9, 3);

        Assertions.assertTrue( insertCount[0] == 2 && deleteCount[0] == 2 );

    }

    @Test
    @DisplayName("Change listeners are removed")
    public void changeListenerRemoved() {

        final int[] insertCount = {0};
        final int[] deleteCount = {0};

        class ChangeListener implements TextChangeListener {

            @Override
            public void onInsert(String text, int position) {
                insertCount[0]++; 
            }

            @Override
            public void onDelete(int position, int length) {
                deleteCount[0]++;
            }
        }

        PieceTable pt = new PieceTable(originalText);
        var listener = new ChangeListener();
        pt.addChangeListener( listener );
        pt.addChangeListener( new ChangeListener());
        pt.insert( "XXX", 9);
        pt.removeChangeListener(listener);
        pt.delete(9, 3);

        Assertions.assertTrue( insertCount[0] == 2 && deleteCount[0] == 1 );

    }

}
