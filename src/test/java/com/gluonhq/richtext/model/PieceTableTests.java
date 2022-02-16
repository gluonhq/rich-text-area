package com.gluonhq.richtext.model;

import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

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

        final Consumer<TextBuffer.Event> changeListener = e -> {

            if ( e instanceof TextBuffer.InsertEvent) {
                insertCount[0]++;
            } else if ( e instanceof TextBuffer.DeleteEvent) {
                deleteCount[0]++;
            }
        };

        PieceTable pt = new PieceTable(originalText);
        pt.addChangeListener(changeListener::accept);
        pt.addChangeListener(changeListener::accept);

        pt.insert( "XXX", 9);
        pt.delete(9, 3);

        Assertions.assertTrue( insertCount[0] == 2 && deleteCount[0] == 2 );

    }

    @Test
    @DisplayName("Change listeners are removed")
    public void changeListenerRemoved() {

        final int[] insertCount = {0};
        final int[] deleteCount = {0};

        final Consumer<TextBuffer.Event> changeListener = e -> {

            if ( e instanceof TextBuffer.InsertEvent) {
                insertCount[0]++;
            } else if ( e instanceof TextBuffer.DeleteEvent) {
                deleteCount[0]++;
            }
        };

        PieceTable pt = new PieceTable(originalText);
        Consumer<TextBuffer.Event> listener = changeListener::accept;
        pt.addChangeListener( listener );
        pt.addChangeListener( changeListener::accept);
        pt.insert( "XXX", 9);
        pt.removeChangeListener(listener);
        pt.delete(9, 3);

        Assertions.assertTrue( insertCount[0] == 2 && deleteCount[0] == 1 );

    }

    @Test
    @DisplayName("Partial text from one piece")
    public void getPartialTextFromOnePiece() {
        PieceTable pt = new PieceTable(originalText);
        Assertions.assertEquals( "Text", pt.getText(9,13));
    }

    @Test
    @DisplayName("Partial text from mutiple pieces")
    public void getPartialTextFromMultiplePieces() {
        PieceTable pt = new PieceTable(originalText);
        pt.insert("Even ", 0);
//        System.out.println(pt.getText());

        pt.insert("More ", 5);
        System.out.println(pt.getText());
//        "Even More Original Text"

        Assertions.assertEquals( "More Original", pt.getText(5,18));
    }

    @Test
    @DisplayName("Same block decorate")
    public void sameBlockDecorate() {
        String text = "Original Bigger Text";
        PieceTable pt = new PieceTable(text);
        pt.decorate(1,2, TextDecoration.builder().fontWeight(FontWeight.BOLD).build());
        Assertions.assertEquals(
                text,
                pt.getText()
        );
        Assertions.assertTrue(pt.pieces.stream()
                .filter(piece -> piece.getText().equals("r"))
                .anyMatch(piece -> piece.getDecoration().getFontWeight() == FontWeight.BOLD)
        );
    }

    @Test
    @DisplayName("Same block decorate first character")
    public void sameBlockDecorateFirstCharacter() {
        String text = "Original Bigger Text";
        PieceTable pt = new PieceTable(text);
        pt.decorate(0,1, TextDecoration.builder().fontWeight(FontWeight.BOLD).build());
        Assertions.assertEquals(
                text,
                pt.getText()
        );
        Assertions.assertTrue(pt.pieces.stream()
                .filter(piece -> piece.getText().equals("O"))
                .anyMatch(piece -> piece.getDecoration().getFontWeight() == FontWeight.BOLD)
        );
    }

    @Test
    @DisplayName("Same block decorate last character")
    public void sameBlockDecorateLastCharacter() {
        String text = "Original Bigger Text";
        PieceTable pt = new PieceTable(text);
        pt.decorate(text.length() - 1, text.length(), TextDecoration.builder().fontWeight(FontWeight.BOLD).build());
        Assertions.assertEquals(
                text,
                pt.getText()
        );
        Assertions.assertTrue(pt.pieces.stream()
                .filter(piece -> piece.getText().equals("t"))
                .anyMatch(piece -> piece.getDecoration().getFontWeight() == FontWeight.BOLD)
        );
    }

    @Test
    @DisplayName("Same block decorate twice")
    public void sameBlockDecorateTwice() {
        String text = "Original Bigger Text";
        PieceTable pt = new PieceTable(text);
        pt.decorate(1,2, TextDecoration.builder().fontWeight(FontWeight.BOLD).build());
        pt.decorate(1,2, TextDecoration.builder().fontPosture(FontPosture.ITALIC).build());
        Assertions.assertEquals(
                text,
                pt.getText()
        );
        Assertions.assertTrue(pt.pieces.stream()
                .filter(piece -> piece.getText().equals("r"))
                .anyMatch(piece -> piece.getDecoration().getFontPosture() == FontPosture.ITALIC)
        );
        Assertions.assertFalse(pt.pieces.stream()
                .filter(piece -> piece.getText().equals("r"))
                .anyMatch(piece -> piece.getDecoration().getFontWeight() == FontWeight.BOLD)
        );
    }

    @Test
    @DisplayName("Multi block decorate")
    public void multiBlockDecorate() {
        PieceTable pt = new PieceTable(originalText);
        String insert = "Bigger ";
        pt.insert( insert, 9); // 'Original Bigger Text'
        pt.insert( insert, 9); // 'Original Bigger Bigger Text'
        pt.decorate(9, 15, TextDecoration.builder().fontWeight(FontWeight.BOLD).build());
        Assertions.assertEquals(
                "Original Bigger Bigger Text",
                pt.getText());
        Assertions.assertTrue(pt.pieces.stream()
                .filter(piece -> piece.getText().equals("Bigger"))
                .anyMatch(piece -> piece.getDecoration().getFontWeight() == FontWeight.BOLD)
        );
    }

    @Test
    @DisplayName("Multi block decorate across pieces")
    public void multiBlockDecorateSpanningMultiplePieces() {
        PieceTable pt = new PieceTable(originalText);
        String insert = "Bigger ";
        pt.insert( insert, 9); // 'Original Bigger Text'
        pt.decorate(6, 18, TextDecoration.builder().fontWeight(FontWeight.BOLD).build());
        Assertions.assertEquals(
                "Original Bigger Text",
                pt.getText());
        Assertions.assertTrue(pt.pieces.stream()
                .filter(piece -> piece.getText().equals("al "))
                .anyMatch(piece -> piece.getDecoration().getFontWeight() == FontWeight.BOLD)
        );
        Assertions.assertTrue(pt.pieces.stream()
                .filter(piece -> piece.getText().equals("Bigger "))
                .anyMatch(piece -> piece.getDecoration().getFontWeight() == FontWeight.BOLD)
        );
        Assertions.assertTrue(pt.pieces.stream()
                .filter(piece -> piece.getText().equals("Te"))
                .anyMatch(piece -> piece.getDecoration().getFontWeight() == FontWeight.BOLD)
        );
    }

    @Test
    @DisplayName("Multi block decorate twice")
    public void multiBlockDecorateTwice() {
        PieceTable pt = new PieceTable(originalText);
        String insert = "Bigger ";
        pt.insert( insert, 9); // 'Original Bigger Text'
        pt.insert( insert, 9); // 'Original Bigger Bigger Text'
        pt.decorate(9, 15, TextDecoration.builder().fontWeight(FontWeight.BOLD).build());
        pt.decorate(9, 15, TextDecoration.builder().fontPosture(FontPosture.ITALIC).build());
        Assertions.assertEquals(
                "Original Bigger Bigger Text",
                pt.getText());
        Assertions.assertTrue(pt.pieces.stream()
                .filter(piece -> piece.getText().equals("Bigger"))
                .anyMatch(piece -> piece.getDecoration().getFontPosture() == FontPosture.ITALIC)
        );
        Assertions.assertFalse(pt.pieces.stream()
                .filter(piece -> piece.getText().equals("Bigger"))
                .anyMatch(piece -> piece.getDecoration().getFontWeight() == FontWeight.BOLD)
        );
    }

}
