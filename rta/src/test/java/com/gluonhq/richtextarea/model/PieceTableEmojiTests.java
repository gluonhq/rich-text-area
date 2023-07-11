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

import javafx.scene.paint.Color;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

public class PieceTableEmojiTests {

    private static final Document FACE_EMOJI_MODEL = new Document("Emoji: \ud83d\ude00!");

    @Test
    @DisplayName("Original emoji text is intact")
    public void originalEmojiTextIntact() {
        PieceTable pt = new PieceTable(FACE_EMOJI_MODEL);
        Assertions.assertEquals(FACE_EMOJI_MODEL.getText(), pt.getText());
    }

    @Test
    @DisplayName("Emoji text append")
    public void emojiTextAppend() {
        String appended = " and some";
        PieceTable pt = new PieceTable(FACE_EMOJI_MODEL);
        pt.append(appended);
        Assertions.assertEquals(FACE_EMOJI_MODEL.getText() + appended, pt.getText());
    }

    @Test
    @DisplayName("Emoji text insert")
    public void emojiTextInsert() {
        String insert = " smiley";
        PieceTable pt = new PieceTable(FACE_EMOJI_MODEL);
        pt.insert(insert, 5);
        //"Emoji smiley: 😀!"
        Assertions.assertEquals(
            new StringBuilder(FACE_EMOJI_MODEL.getText()).insert(5, insert).toString(),
            pt.getText()
        );
    }

    @Test
    @DisplayName("Emoji text insert at the end is converted to append")
    public void emojiInsertAtTheEnd() {
        String insert = " and more";
        PieceTable pt = new PieceTable(FACE_EMOJI_MODEL);
        pt.insert(insert, pt.getTextLength());
        Assertions.assertEquals(FACE_EMOJI_MODEL.getText() + insert, pt.getText());
    }

    @Test
    @DisplayName("Emoji text insert at wrong position throws exception")
    public void emojiInsertAtInvalidPositionFails() {
        PieceTable pt = new PieceTable(FACE_EMOJI_MODEL);
        Assertions.assertThrows(IllegalArgumentException.class, () -> pt.insert("xxx", 100));
        Assertions.assertThrows(IllegalArgumentException.class, () -> pt.insert("xxx", -1));
    }

    @Test
    @DisplayName("Emoji same block delete")
    public void emojiSameBlockDelete() {
        String text = "Emoji smiley: \ud83d\ude00!";
        PieceTable pt = new PieceTable(new Document(text));
        pt.delete(5, 7);
        Assertions.assertEquals(
            new StringBuilder(text).delete(5, 5+7).toString(),
            pt.getText()
        );
    }

    @Test
    @DisplayName("Emoji multi block delete")
    public void emojiMultiBlockDelete() {
        PieceTable pt = new PieceTable(FACE_EMOJI_MODEL);
        String append = "!";
        String insert = " smiley";
        pt.append(append); // 'Emoji: \ud83d\ude00!!'
        pt.insert(insert, 5); // 'Emoji smiley: \ud83d\ude00!!'
        pt.insert(insert, 12); // 'Emoji smiley smiley: \ud83d\ude00!!'
        pt.delete(5, 14); // 'Emoji: \ud83d\ude00!!'
        Assertions.assertEquals(
                new StringBuilder(FACE_EMOJI_MODEL.getText())
                        .append(append)
                        .insert(5, insert)
                        .insert(12, insert)
                        .delete(5, 5+14)
                        .toString(),
            pt.getText());
    }

    @Test
    @DisplayName("Emoji text delete beyond text length")
    public void emojiDeleteBeyondLength() {
        String text = "Emoji smiley: \ud83d\ude00!";
        PieceTable pt = new PieceTable(new Document(text));
        pt.delete(8,107);
        Assertions.assertEquals(
                new StringBuilder(text).delete(8, 8+107).toString(),
                pt.getText());
    }

    @Test
    @DisplayName("Emoji text delete at wrong position throws exception")
    public void emojiDeleteAtInvalidPositionFails() {
        PieceTable pt = new PieceTable(FACE_EMOJI_MODEL);
        Assertions.assertThrows(IllegalArgumentException.class, () -> pt.delete(-1, 100));
    }

    @Test
    @DisplayName("Emoji events are fired")
    public void emojiEventsAreFired() {

        final int[] insertCount = {0};
        final int[] deleteCount = {0};

        final Consumer<TextBuffer.Event> changeListener = e -> {

            if (e instanceof TextBuffer.InsertEvent) {
                insertCount[0]++;
            } else if (e instanceof TextBuffer.DeleteEvent) {
                deleteCount[0]++;
            }
        };

        PieceTable pt = new PieceTable(FACE_EMOJI_MODEL);
        pt.addChangeListener(changeListener::accept);
        pt.addChangeListener(changeListener::accept);

        pt.insert("XXX", 5);
        pt.delete(5, 3);

        Assertions.assertTrue(insertCount[0] == 2 && deleteCount[0] == 2);

    }

    @Test
    @DisplayName("Emoji change listeners are removed")
    public void emojiChangeListenerRemoved() {

        final int[] insertCount = {0};
        final int[] deleteCount = {0};

        final Consumer<TextBuffer.Event> changeListener = e -> {

            if (e instanceof TextBuffer.InsertEvent) {
                insertCount[0]++;
            } else if (e instanceof TextBuffer.DeleteEvent) {
                deleteCount[0]++;
            }
        };

        PieceTable pt = new PieceTable(FACE_EMOJI_MODEL);
        Consumer<TextBuffer.Event> listener = changeListener::accept;
        pt.addChangeListener(listener);
        pt.addChangeListener(changeListener::accept);
        pt.insert("XXX", 5);
        pt.removeChangeListener(listener);
        pt.delete(5, 3);

        Assertions.assertTrue(insertCount[0] == 2 && deleteCount[0] == 1);

    }

    @Test
    @DisplayName("Partial emoji text from one piece")
    public void getPartialEmojiTextFromOnePiece() {
        PieceTable pt = new PieceTable(FACE_EMOJI_MODEL);
        Assertions.assertEquals("Emoji", pt.getText(0, 5));
        Assertions.assertEquals(": ", pt.getText(5, 7));
        Assertions.assertEquals("\ud83d\ude00", pt.getText(7, 8));
        Assertions.assertEquals(" \ud83d\ude00!", pt.getText(6, 9));
    }

    @Test
    @DisplayName("Partial emoji text from multiple pieces")
    public void getPartialEmojiTextFromMultiplePieces() {
        PieceTable pt = new PieceTable(FACE_EMOJI_MODEL);
        pt.insert("Even ", 0);

        pt.insert("More ", 5);
//        "Even More Emoji: \ud83d\ude00!!"

        Assertions.assertEquals("More Emoji: \ud83d\ude00!", pt.getText(5,100));
    }

    @Test
    @DisplayName("Emoji same block decorate weight")
    public void emojiSameBlockDecorateWeight() {
        String text = "Emoji smiley: \ud83d\ude00!";
        PieceTable pt = new PieceTable(new Document(text));
        pt.decorate(1, 2, TextDecoration.builder().fontWeight(FontWeight.BOLD).build());
        Assertions.assertEquals(text, pt.getText());
        Assertions.assertTrue(pt.pieces.stream()
                .filter(piece -> piece.getInternalText().equals("m"))
                .anyMatch(piece -> ((TextDecoration) piece.getDecoration()).getFontWeight() == FontWeight.BOLD)
        );
    }


    @Test
    @DisplayName("Same block decorate foreground color")
    public void sameBlockDecorateForegroundColor() {
        String text = "Original Bigger Text";
        PieceTable pt = new PieceTable(new Document(text));
        pt.decorate(1, 2, TextDecoration.builder().foreground(Color.AQUA).build());
        Assertions.assertEquals(text, pt.getText());
        Assertions.assertTrue(pt.pieces.stream()
                .filter(piece -> piece.getInternalText().equals("r"))
                .anyMatch(piece -> ((TextDecoration) piece.getDecoration()).getForeground() == Color.AQUA)
        );
    }

    @Test
    @DisplayName("Emoji same block decorate first character")
    public void EmojiSameBlockDecorateFirstCharacter() {
        String text = "Emoji smiley: \ud83d\ude00!";
        PieceTable pt = new PieceTable(new Document(text));
        pt.decorate(0, 1, TextDecoration.builder().fontWeight(FontWeight.BOLD).build());
        Assertions.assertEquals(text, pt.getText());
        Assertions.assertTrue(pt.pieces.stream()
                .filter(piece -> piece.getInternalText().equals("E"))
                .anyMatch(piece -> ((TextDecoration) piece.getDecoration()).getFontWeight() == FontWeight.BOLD)
        );
    }

    @Test
    @DisplayName("Emoji same block decorate last character")
    public void emojiSameBlockDecorateLastCharacter() {
        String text = "Emoji smiley: \ud83d\ude00!";
        PieceTable pt = new PieceTable(new Document(text));
        pt.decorate(pt.getTextLength() - 1, pt.getTextLength(), TextDecoration.builder().fontWeight(FontWeight.BOLD).build());
        Assertions.assertEquals(text, pt.getText());
        Assertions.assertTrue(pt.pieces.stream()
                .filter(piece -> piece.getInternalText().equals("!"))
                .anyMatch(piece -> ((TextDecoration) piece.getDecoration()).getFontWeight() == FontWeight.BOLD)
        );
    }

    @Test
    @DisplayName("Emoji same block multiple decoration across pieces")
    public void emojiSameBlockMultiDecorateSpanningMultiplePieces() {
        PieceTable pt = new PieceTable(new Document(FACE_EMOJI_MODEL.getText() + " One Two Three"));
        pt.decorate(10, 23, TextDecoration.builder().fontWeight(FontWeight.BOLD).build());
        pt.decorate(14, 17, TextDecoration.builder().fontPosture(FontPosture.ITALIC).build());
        Assertions.assertEquals("Emoji: \ud83d\ude00! One Two Three", pt.getText());
        Assertions.assertTrue(pt.pieces.stream()
                .filter(piece -> piece.getInternalText().equals("One "))
                .anyMatch(piece -> ((TextDecoration) piece.getDecoration()).getFontWeight() == FontWeight.BOLD)
        );
        Assertions.assertTrue(pt.pieces.stream()
                .filter(piece -> piece.getInternalText().equals("Two"))
                .anyMatch(piece -> ((TextDecoration) piece.getDecoration()).getFontWeight() == FontWeight.BOLD &&
                        ((TextDecoration) piece.getDecoration()).getFontPosture() == FontPosture.ITALIC)
        );
        Assertions.assertTrue(pt.pieces.stream()
                .filter(piece -> piece.getInternalText().equals(" Three"))
                .anyMatch(piece -> ((TextDecoration) piece.getDecoration()).getFontWeight() == FontWeight.BOLD)
        );
    }

    @Test
    @DisplayName("Emoji multi block decorate")
    public void emojiMultiBlockDecorate() {
        PieceTable pt = new PieceTable(FACE_EMOJI_MODEL);
        String insert = " smiley";
        pt.insert(insert, 5); // 'Emoji smiley: \ud83d\ude00!'
        pt.insert(insert, 5); // 'Emoji smiley smiley: \ud83d\ude00!'
        pt.decorate(6, 12, TextDecoration.builder().fontWeight(FontWeight.BOLD).build());
        Assertions.assertEquals("Emoji smiley smiley: \ud83d\ude00!", pt.getText());
        Assertions.assertTrue(pt.pieces.stream()
                .filter(piece -> piece.getInternalText().equals("smiley"))
                .anyMatch(piece -> ((TextDecoration) piece.getDecoration()).getFontWeight() == FontWeight.BOLD)
        );
    }

    @Test
    @DisplayName("Emoji multi block decorate across pieces")
    public void emojiMultiBlockDecorateSpanningMultiplePieces() {
        PieceTable pt = new PieceTable(FACE_EMOJI_MODEL);
        String insert = " smiley";
        pt.insert(insert, 5); // 'Emoji smiley: \ud83d\ude00!'
        pt.decorate(3, 15, TextDecoration.builder().fontWeight(FontWeight.BOLD).build());
        Assertions.assertEquals("Emoji smiley: \ud83d\ude00!", pt.getText());
        Assertions.assertTrue(pt.pieces.stream()
                        .peek(System.out::println)
                .filter(piece -> piece.getInternalText().equals("ji"))
                .anyMatch(piece -> ((TextDecoration) piece.getDecoration()).getFontWeight() == FontWeight.BOLD)
        );
        Assertions.assertTrue(pt.pieces.stream()
                .filter(piece -> piece.getInternalText().equals(" smiley"))
                .anyMatch(piece -> ((TextDecoration) piece.getDecoration()).getFontWeight() == FontWeight.BOLD)
        );
        Assertions.assertTrue(pt.pieces.stream()
                .filter(piece -> piece.getInternalText().equals(TextBuffer.EMOJI_ANCHOR_TEXT))
                .anyMatch(piece -> ((TextDecoration) piece.getDecoration()).getFontWeight() == FontWeight.BOLD)
        );
        Assertions.assertTrue(pt.pieces.stream()
                .filter(piece -> piece.getInternalText().equals("!"))
                .anyMatch(piece -> ((TextDecoration) piece.getDecoration()).getFontWeight() == FontWeight.NORMAL)
        );
    }

    @Test
    @DisplayName("Appended emoji text should use existing decoration")
    public void emojiTextAppendDecoration() {
        String appended = " and some";
        PieceTable pt = new PieceTable(FACE_EMOJI_MODEL);
        pt.decorate(0, pt.getTextLength(), TextDecoration.builder().fontSize(20).build());
        pt.append(appended);
        Assertions.assertEquals(FACE_EMOJI_MODEL.getText() + appended, pt.getText());
        Assertions.assertTrue(pt.pieces.stream()
                .filter(piece -> piece.getInternalText().equals(appended))
                .anyMatch(piece -> ((TextDecoration) piece.getDecoration()).getFontSize() == 20));
    }

    @Test
    @DisplayName("Inserted emoji text after decorated text")
    public void emojiTextInsertAfterDecoration() {
        String insert = " smiley";
        PieceTable pt = new PieceTable(FACE_EMOJI_MODEL);
        pt.decorate(0, pt.getTextLength(), TextDecoration.builder().fontSize(20).build());
        pt.insert(insert, 5);  // 'Emoji smiley: \ud83d\ude00!'
        Assertions.assertEquals(
                new StringBuilder(FACE_EMOJI_MODEL.getText()).insert(5, insert).toString(),
                pt.getText()
        );
        Assertions.assertTrue(pt.pieces.stream()
                .filter(piece -> piece.getInternalText().equals(insert))
                .anyMatch(piece -> ((TextDecoration) piece.getDecoration()).getFontSize() == 20));
    }

    @Test
    @DisplayName("Inserted emoji text after default decorated text")
    public void emojiTextInsertAfterDefaultDecoratedText() {
        String insert = " smiley";
        PieceTable pt = new PieceTable(FACE_EMOJI_MODEL);
        double defaultFontSize = TextDecoration.builder().presets().build().getFontSize();
        pt.decorate(0, 4, TextDecoration.builder().fontSize(20).build());
        pt.insert(insert, 5); //  // 'Emoji smiley: \ud83d\ude00!'
        Assertions.assertEquals(
                new StringBuilder(FACE_EMOJI_MODEL.getText()).insert(5, insert).toString(),
                pt.getText()
        );
        Assertions.assertTrue(pt.pieces.stream()
                .filter(piece -> piece.getInternalText().equals(insert))
                .anyMatch(piece -> ((TextDecoration) piece.getDecoration()).getFontSize() == defaultFontSize));
    }

    @Test
    @DisplayName("delete emoji text")
    public void deleteAndUndoEmojiText() {
        PieceTable pt = new PieceTable(FACE_EMOJI_MODEL);
        Assertions.assertEquals(FACE_EMOJI_MODEL.getText(), pt.getText());
        pt.delete(7, 1); // 'Emoji: !'
        Assertions.assertEquals(
                new StringBuilder(FACE_EMOJI_MODEL.getText()).delete(7, 9).toString(),
                pt.getText()
        );
        Assertions.assertEquals(pt.getTextLength(), 8);
        pt.undo();
        Assertions.assertEquals(FACE_EMOJI_MODEL.getText(), pt.getText());
        Assertions.assertEquals(pt.getTextLength(), 9);
    }

    @Test
    @DisplayName("replace emoji text")
    public void replaceAndUndoEmojiText() {
        String text = "\uD83C\uDFF4\uDB40\uDC67\uDB40\uDC62\uDB40\uDC73\uDB40\uDC63\uDB40\uDC74\uDB40\uDC7F";
        PieceTable pt = new PieceTable(FACE_EMOJI_MODEL);
        Assertions.assertEquals(FACE_EMOJI_MODEL.getText(), pt.getText());
        pt.delete(7, 1); // 'Emoji: !'
        pt.insert(text, 7);
        Assertions.assertEquals(
                new StringBuilder(FACE_EMOJI_MODEL.getText()).replace(7, 9, text).toString(),
                pt.getText()
        );
        Assertions.assertEquals(pt.getTextLength(), 9);
        pt.undo();
        Assertions.assertEquals(
                new StringBuilder(FACE_EMOJI_MODEL.getText()).delete(7, 9).toString(),
                pt.getText()
        );
        pt.undo();
        Assertions.assertEquals(FACE_EMOJI_MODEL.getText(), pt.getText());
    }
}
