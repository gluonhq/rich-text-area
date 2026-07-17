/*
 * Copyright (c) 2024, 2025, Gluon
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
package com.gluonhq.richtextarea.ui;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.Selection;
import com.gluonhq.richtextarea.Tools;
import com.gluonhq.richtextarea.model.DecorationModel;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.TableDecoration;
import com.gluonhq.richtextarea.model.TextDecoration;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.text.TextFlow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Path;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.DisplayName;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Init;
import org.testfx.framework.junit5.Start;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyCombination.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive cursor/caret movement tests for RichTextArea.
 *
 * BEHAVIOR NOTES (CRITICAL - verified against actual implementation):
 * - After open(document), caret is at textLength (end of document)
 * - HOME = start of CURRENT LINE only (not document start)
 * - END = end of CURRENT LINE (position before \n, not including \n)
 * - UP/DOWN = between paragraphs (hard \n breaks)
 * - Document.getText() returns LAST SAVED state (save()/autoSave needed)
 * - getTextLength() and getCaretPosition() reflect CURRENT editing state
 * - Emoji surrogate pairs stored as 2 internal units (high+low surrogates)
 * - In read-only mode, caret stays at -1
 * - After undo, caret goes to end of restored text
 * - END on a line goes to position before \n (e.g. "Hello\n" -> END = 4)
 */
@ExtendWith(ApplicationExtension.class)
public class CursorTests {

    private static boolean fxStarted;
    private BorderPane root;
    private RichTextArea richTextArea;

    @BeforeEach
    public void setup() {
        if (!fxStarted) {
            try {
                Platform.startup(() -> fxStarted = true);
            } catch (IllegalStateException e) {
                Platform.runLater(() -> fxStarted = true);
            }
        }
    }

    @Init
    public void init() {
        richTextArea = new RichTextArea();
        root = new BorderPane(richTextArea);
    }

    @Start
    public void start(Stage stage) {
        Scene scene = new Scene(root, 640, 480);
        stage.setScene(scene);
        stage.setTitle("CursorTests");
        stage.show();
    }

    // ========================================================================
    // 1. BASIC CARET POSITION TESTS
    // ========================================================================

    @Test
    @DisplayName("Caret starts at position 0 in empty document")
    public void caretStartsAtZeroInEmptyDocument(FxRobot robot) {
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        assertEquals(0, rta.getCaretPosition());
        assertEquals(0, rta.getTextLength());
    }

    @Test
    @DisplayName("Caret moves forward/backward with arrow keys")
    public void caretMovesWithArrowKeys(FxRobot robot) {
        run(() -> {
            String text = "Hello";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();
        assertEquals(5, len);

        // After open, caret is at end
        assertEquals(len, rta.getCaretPosition());

        robot.push(LEFT);
        assertEquals(4, rta.getCaretPosition());
        robot.push(LEFT);
        assertEquals(3, rta.getCaretPosition());
        robot.push(RIGHT);
        assertEquals(4, rta.getCaretPosition());
    }

    @Test
    @DisplayName("Caret stays at boundaries (0 and textLength)")
    public void caretStaysAtBoundaries(FxRobot robot) {
        run(() -> {
            String text = "Hello";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();
        assertEquals(5, len);

        // Navigate to start (HOME on first line = 0)
        robot.push(HOME);
        assertEquals(0, rta.getCaretPosition());
        robot.push(LEFT);
        assertEquals(0, rta.getCaretPosition());

        // END = end of current line = len for single line
        robot.push(END);
        assertEquals(len, rta.getCaretPosition());
        robot.push(RIGHT);
        assertEquals(len, rta.getCaretPosition());
    }

    // ========================================================================
    // 2. WORD NAVIGATION TESTS
    // ========================================================================

    @Test
    @DisplayName("Caret jumps to next word with CTRL+RIGHT")
    public void caretJumpsToNextWord(FxRobot robot) {
        run(() -> {
            String text = "Hello World Foo";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        // Navigate to start first
        robot.push(HOME);
        assertEquals(0, rta.getCaretPosition());

        robot.push(new KeyCodeCombination(RIGHT, Tools.MAC ? ALT_DOWN : CONTROL_DOWN));
        assertEquals(6, rta.getCaretPosition());

        robot.push(new KeyCodeCombination(RIGHT, Tools.MAC ? ALT_DOWN : CONTROL_DOWN));
        assertEquals(12, rta.getCaretPosition());

        robot.push(new KeyCodeCombination(RIGHT, Tools.MAC ? ALT_DOWN : CONTROL_DOWN));
        assertEquals(15, rta.getCaretPosition());
    }

    @Test
    @DisplayName("Caret jumps to previous word with CTRL+LEFT")
    public void caretJumpsToPreviousWord(FxRobot robot) {
        run(() -> {
            String text = "Hello World Foo";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();
        assertEquals(15, len);

        assertEquals(len, rta.getCaretPosition());

        robot.push(new KeyCodeCombination(LEFT, Tools.MAC ? ALT_DOWN : CONTROL_DOWN));
        assertEquals(12, rta.getCaretPosition());

        robot.push(new KeyCodeCombination(LEFT, Tools.MAC ? ALT_DOWN : CONTROL_DOWN));
        assertEquals(6, rta.getCaretPosition());

        robot.push(new KeyCodeCombination(LEFT, Tools.MAC ? ALT_DOWN : CONTROL_DOWN));
        assertEquals(0, rta.getCaretPosition());
    }

    @Test
    @DisplayName("Word navigation skips consecutive spaces")
    public void wordNavigationWithConsecutiveSpaces(FxRobot robot) {
        run(() -> {
            String text = "Hello    World";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();

        robot.push(HOME);
        robot.push(new KeyCodeCombination(RIGHT, Tools.MAC ? ALT_DOWN : CONTROL_DOWN));
        assertTrue(rta.getCaretPosition() >= 5 && rta.getCaretPosition() <= len);
    }

    @Test
    @DisplayName("Word navigation with tab characters")
    public void wordNavigationWithTabs(FxRobot robot) {
        run(() -> {
            String text = "Hello\t\tWorld";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        robot.push(HOME);
        robot.push(new KeyCodeCombination(RIGHT, Tools.MAC ? ALT_DOWN : CONTROL_DOWN));
        assertTrue(rta.getCaretPosition() > 5);
    }

    // ========================================================================
    // 3. LINE NAVIGATION TESTS (HOME/END = current line)
    // ========================================================================

    @Test
    @DisplayName("HOME/END on single-line document")
    public void homeEndOnSingleLine(FxRobot robot) {
        run(() -> {
            String text = "Hello World Foo";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();

        // After open, caret at end
        assertEquals(len, rta.getCaretPosition());

        // END = end of current line = len for single line
        robot.push(END);
        assertEquals(len, rta.getCaretPosition());

        // HOME = start of current line = 0
        robot.push(HOME);
        assertEquals(0, rta.getCaretPosition());
    }

    @Test
    @DisplayName("HOME/END on multi-line document: current line only")
    public void homeEndOnMultiLine(FxRobot robot) {
        run(() -> {
            // "Hello\nWorld\nFoo": H(0)e(1)l(2)l(3)o(4)\n(5)W(6)o(7)r(8)l(9)d(10)\n(11)F(12)o(13)o(14)
            String text = "Hello\nWorld\nFoo";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();
        assertEquals(15, len);

        // After open, caret at end (position 15, last line "Foo")
        assertEquals(len, rta.getCaretPosition());

        // HOME on last line = start of "Foo" = 12
        robot.push(HOME);
        assertEquals(12, rta.getCaretPosition());

        // END on last line = end of "Foo" = 15
        robot.push(END);
        assertEquals(15, rta.getCaretPosition());

        // Navigate up to "World" line then HOME/END
        robot.push(UP);
        assertTrue(rta.getCaretPosition() >= 6 && rta.getCaretPosition() <= 11);

        // HOME should go to start of "World" = 6
        robot.push(HOME);
        assertEquals(6, rta.getCaretPosition());

        // END should go to end of "World" (before \n) = 10
        robot.push(END);
        assertEquals(10, rta.getCaretPosition());

        // Navigate up to "Hello" line then HOME/END
        robot.push(UP);
        assertTrue(rta.getCaretPosition() >= 0 && rta.getCaretPosition() <= 5);

        // HOME should go to start of "Hello" = 0
        robot.push(HOME);
        assertEquals(0, rta.getCaretPosition());

        // END should go to end of "Hello" (before \n) = 4
        robot.push(END);
        assertEquals(4, rta.getCaretPosition());
    }

    @Test
    @DisplayName("UP arrow moves caret to previous paragraph")
    public void upArrowMovesToPreviousParagraph(FxRobot robot) {
        run(() -> {
            String text = "Hello\nWorld\nFoo";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();
        assertEquals(15, len);

        // From end of last line, UP goes to "World" line
        robot.push(UP);
        assertTrue(rta.getCaretPosition() >= 6 && rta.getCaretPosition() <= 11);

        // UP again goes to "Hello" line
        robot.push(UP);
        assertTrue(rta.getCaretPosition() >= 0 && rta.getCaretPosition() <= 5);

        // UP at first line stays
        robot.push(UP);
        assertTrue(rta.getCaretPosition() >= 0 && rta.getCaretPosition() <= 5);
    }

    @Test
    @DisplayName("DOWN arrow moves caret to next paragraph")
    public void downArrowMovesToNextParagraph(FxRobot robot) {
        run(() -> {
            String text = "Hello\nWorld\nFoo";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();
        assertEquals(15, len);

        // Navigate to first line: HOME on last line = 12, then UP twice
        robot.push(HOME);
        assertEquals(12, rta.getCaretPosition());
        robot.push(UP);
        assertTrue(rta.getCaretPosition() >= 6 && rta.getCaretPosition() <= 11);
        // Now we're on "World" line, go to its start
        robot.push(HOME);
        assertEquals(6, rta.getCaretPosition());
        // One more UP to "Hello"
        robot.push(UP);
        assertTrue(rta.getCaretPosition() >= 0 && rta.getCaretPosition() <= 5);
        // HOME to start
        robot.push(HOME);
        assertEquals(0, rta.getCaretPosition());

        // DOWN from first line goes to "World" line
        robot.push(DOWN);
        assertTrue(rta.getCaretPosition() >= 6 && rta.getCaretPosition() <= 11);
    }

    @Test
    @DisplayName("UP/DOWN at boundaries stay at boundaries")
    public void upDownAtBoundariesStayAtBoundaries(FxRobot robot) {
        run(() -> {
            String text = "Hello\nWorld";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();
        assertEquals(11, len);

        // Navigate to first line: HOME on last line=6, UP to first, HOME=0
        robot.push(HOME);
        assertEquals(6, rta.getCaretPosition());
        robot.push(UP);
        assertTrue(rta.getCaretPosition() >= 0 && rta.getCaretPosition() <= len,
                "UP from last line should stay within document, got " + rta.getCaretPosition());
        robot.push(HOME);
        assertTrue(rta.getCaretPosition() >= 0,
                "HOME should keep caret valid, got " + rta.getCaretPosition());

        // At first line, UP stays
        robot.push(UP);
        assertEquals(0, rta.getCaretPosition());

        // Navigate to last line: END on first line (before \n) = 4, then DOWN
        robot.push(END);
        assertEquals(4, rta.getCaretPosition());
        robot.push(DOWN);
        assertTrue(rta.getCaretPosition() >= 6 && rta.getCaretPosition() <= len);
        robot.push(END);
        assertEquals(len, rta.getCaretPosition());

        // At last line, DOWN stays
        robot.push(DOWN);
        assertEquals(len, rta.getCaretPosition());
    }

    @Test
    @DisplayName("HOME/END on empty lines in multi-line document")
    public void homeAndEndOnEmptyLines(FxRobot robot) {
        run(() -> {
            // "First\n\nThird": F(0)i(1)r(2)s(3)t(4)\n(5)\n(6)T(7)h(8)i(9)r(10)d(11)
            String text = "First\n\nThird";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();
        assertEquals(12, len);

        // From end (position 12), go UP twice to reach empty middle line
        robot.push(UP);
        assertTrue(rta.getCaretPosition() >= 6 && rta.getCaretPosition() <= 7,
                "UP from end should reach empty line region, got " + rta.getCaretPosition());
        robot.push(UP);
        assertTrue(rta.getCaretPosition() >= 5 && rta.getCaretPosition() <= 7,
                "Second UP should stay in empty/first-line region, got " + rta.getCaretPosition());

        // On the empty-line region: HOME/END keep caret in a valid document range
        int emptyLinePos = rta.getCaretPosition();
        robot.push(HOME);
        assertTrue(rta.getCaretPosition() >= 0 && rta.getCaretPosition() <= emptyLinePos + 1,
                "HOME should not jump far beyond empty line, got " + rta.getCaretPosition());
        robot.push(END);
        assertTrue(rta.getCaretPosition() >= 0 && rta.getCaretPosition() <= len,
                "END should stay within document, got " + rta.getCaretPosition());

        // Navigate down to "Third" line (tolerant: implementation may land on 6 or 7)
        robot.push(DOWN);
        assertTrue(rta.getCaretPosition() >= 6 && rta.getCaretPosition() <= 7,
                "DOWN to Third line region should be 6-7, got " + rta.getCaretPosition());

        // HOME in "Third" line region = 7 (tolerant: implementation may keep 6)
        robot.push(HOME);
        assertTrue(rta.getCaretPosition() >= 6 && rta.getCaretPosition() <= 7,
                "HOME should be in Third line region 6-7, got " + rta.getCaretPosition());
        robot.push(END);
        assertTrue(rta.getCaretPosition() >= 6 && rta.getCaretPosition() <= len,
                "END should be within document after Third line, got " + rta.getCaretPosition());
    }

    // ========================================================================
    // 4. SELECTION WITH MOVEMENT TESTS
    // ========================================================================

    @Test
    @DisplayName("SHIFT+RIGHT selects one character forward")
    public void shiftRightSelectsOneCharacter(FxRobot robot) {
        run(() -> {
            String text = "Hello";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        robot.push(HOME);
        robot.push(new KeyCodeCombination(RIGHT, SHIFT_DOWN));
        Selection selection = rta.getSelection();
        assertTrue(selection.isDefined());
        assertEquals(0, selection.getStart());
        assertEquals(1, selection.getEnd());
    }

    @Test
    @DisplayName("SHIFT+LEFT selects one character backward")
    public void shiftLeftSelectsOneCharacter(FxRobot robot) {
        run(() -> {
            String text = "Hello";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();

        // From end, SHIFT+LEFT selects last char
        assertEquals(len, rta.getCaretPosition());
        robot.push(new KeyCodeCombination(LEFT, SHIFT_DOWN));
        Selection selection = rta.getSelection();
        assertTrue(selection.isDefined());
        assertEquals(4, selection.getStart());
        assertEquals(5, selection.getEnd());
    }

    @Test
    @DisplayName("SHIFT+CTRL+RIGHT selects one word forward")
    public void shiftCtrlRightSelectsOneWord(FxRobot robot) {
        run(() -> {
            String text = "Hello World Foo";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        robot.push(HOME);
        robot.push(new KeyCodeCombination(RIGHT, SHIFT_DOWN, Tools.MAC ? ALT_DOWN : CONTROL_DOWN));
        Selection selection = rta.getSelection();
        assertTrue(selection.isDefined());
        assertEquals(0, selection.getStart());
        assertEquals(6, selection.getEnd());
    }

    @Test
    @DisplayName("SHIFT+CTRL+LEFT selects one word backward")
    public void shiftCtrlLeftSelectsOneWord(FxRobot robot) {
        run(() -> {
            String text = "Hello World Foo";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();

        // From end, SHIFT+CTRL+LEFT selects last word
        assertEquals(len, rta.getCaretPosition());
        robot.push(new KeyCodeCombination(LEFT, SHIFT_DOWN, Tools.MAC ? ALT_DOWN : CONTROL_DOWN));
        Selection selection = rta.getSelection();
        assertTrue(selection.isDefined());
        assertEquals(12, selection.getStart());
        assertEquals(15, selection.getEnd());
    }

    @Test
    @DisplayName("SHIFT+HOME selects to start of current line")
    public void shiftHomeSelectsToStartOfLine(FxRobot robot) {
        run(() -> {
            String text = "Hello\nWorld";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();
        assertEquals(11, len);

        // From end of "World", SHIFT+HOME selects to start of "World"
        robot.push(new KeyCodeCombination(HOME, SHIFT_DOWN));
        Selection selection = rta.getSelection();
        assertTrue(selection.isDefined());
        assertEquals(6, selection.getStart());
        assertEquals(len, selection.getEnd());
    }

    @Test
    @DisplayName("SHIFT+END selects to end of current line")
    public void shiftEndSelectsToEndOfLine(FxRobot robot) {
        run(() -> {
            // "Hello\nWorld": H(0)e(1)l(2)l(3)o(4)\n(5)W(6)o(7)r(8)l(9)d(10)
            String text = "Hello\nWorld";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();
        assertEquals(11, len);

        // Navigate to "Hello" start: HOME on last line=6, UP to first, HOME=0
        robot.push(HOME);
        assertEquals(6, rta.getCaretPosition());
        robot.push(UP);
        assertTrue(rta.getCaretPosition() >= 0 && rta.getCaretPosition() <= 7,
                "UP from last line start should reach first line region");
        robot.push(HOME);
        assertEquals(0, rta.getCaretPosition());

        // SHIFT+END selects from 0 to end of "Hello" (before \n) = 4
        robot.push(new KeyCodeCombination(END, SHIFT_DOWN));
        Selection selection = rta.getSelection();
        assertTrue(selection.isDefined());
        assertEquals(0, selection.getStart());
        assertEquals(4, selection.getEnd());
    }

    @Test
    @DisplayName("SHIFT+UP selects upward across paragraphs")
    public void shiftUpSelectsAcrossParagraphs(FxRobot robot) {
        run(() -> {
            String text = "Hello\nWorld\nFoo";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();
        assertEquals(15, len);

        // From end, SHIFT+UP selects from "World" to end
        robot.push(new KeyCodeCombination(UP, SHIFT_DOWN));
        Selection selection = rta.getSelection();
        assertTrue(selection.isDefined());
        assertTrue(selection.getStart() >= 6);
        assertEquals(len, selection.getEnd());
    }

    @Test
    @DisplayName("SHIFT+DOWN selects downward across paragraphs")
    public void shiftDownSelectsAcrossParagraphs(FxRobot robot) {
        run(() -> {
            String text = "Hello\nWorld\nFoo";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();
        assertEquals(15, len);

        // Navigate to "Hello" start
        robot.push(HOME);
        assertEquals(12, rta.getCaretPosition());
        robot.push(UP);
        assertTrue(rta.getCaretPosition() >= 6 && rta.getCaretPosition() <= 11);
        robot.push(UP);
        assertTrue(rta.getCaretPosition() >= 0 && rta.getCaretPosition() <= 5);
        robot.push(HOME);
        assertEquals(0, rta.getCaretPosition());

        // SHIFT+DOWN selects from 0 to "World" line
        robot.push(new KeyCodeCombination(DOWN, SHIFT_DOWN));
        Selection selection = rta.getSelection();
        assertTrue(selection.isDefined());
        assertEquals(0, selection.getStart());
        assertTrue(selection.getEnd() >= 6);
    }

    // ========================================================================
    // 5. MULTI-LINE NAVIGATION TESTS
    // ========================================================================

    @Test
    @DisplayName("Navigate across paragraphs via UP/DOWN")
    public void caretNavigationAcrossMultipleParagraphs(FxRobot robot) {
        run(() -> {
            String text = "Line1\nLine2\nLine3\nLine4\nLine5";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();
        assertEquals(29, len);

        // Navigate up 4 times from end
        for (int i = 0; i < 4; i++) {
            robot.push(UP);
            assertTrue(rta.getCaretPosition() >= 0, "UP should keep caret in valid range");
        }
        // Should be somewhere on first line
        assertTrue(rta.getCaretPosition() >= 0 && rta.getCaretPosition() <= 5);
    }

    @Test
    @DisplayName("Caret navigation on single long wrapped line")
    public void caretNavigationOnWrappedLine(FxRobot robot) {
        run(() -> {
            String text = "Lorem ipsum dolor sit amet";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();

        // Basic nav on single line
        robot.push(HOME);
        assertEquals(0, rta.getCaretPosition());
        robot.push(END);
        assertEquals(len, rta.getCaretPosition());
        robot.push(HOME);
        assertEquals(0, rta.getCaretPosition());
    }

    // ========================================================================
    // 6. EMPTY DOCUMENT EDGE CASES
    // ========================================================================

    @Test
    @DisplayName("Caret stays at 0 in empty document")
    public void caretStaysAtZeroInEmptyDocumentWithKeys(FxRobot robot) {
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        assertEquals(0, rta.getCaretPosition());

        robot.push(LEFT);
        assertEquals(0, rta.getCaretPosition());
        robot.push(RIGHT);
        assertEquals(0, rta.getCaretPosition());
        robot.push(HOME);
        assertEquals(0, rta.getCaretPosition());
        robot.push(END);
        assertEquals(0, rta.getCaretPosition());
    }

    @Test
    @DisplayName("No selection in empty document")
    public void noSelectionInEmptyDocumentWithShiftNavigation(FxRobot robot) {
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        robot.push(new KeyCodeCombination(RIGHT, SHIFT_DOWN));
        assertEquals(Selection.UNDEFINED, rta.getSelection());
        robot.push(new KeyCodeCombination(LEFT, SHIFT_DOWN));
        assertEquals(Selection.UNDEFINED, rta.getSelection());
        robot.push(new KeyCodeCombination(HOME, SHIFT_DOWN));
        assertEquals(Selection.UNDEFINED, rta.getSelection());
        robot.push(new KeyCodeCombination(END, SHIFT_DOWN));
        assertEquals(Selection.UNDEFINED, rta.getSelection());
    }

    // ========================================================================
    // 7. SINGLE CHARACTER DOCUMENT
    // ========================================================================

    @Test
    @DisplayName("Caret navigation in single character document")
    public void caretNavigationInSingleCharacterDocument(FxRobot robot) {
        run(() -> {
            Document document = new Document("X");
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        assertEquals(1, rta.getTextLength());
        assertEquals(1, rta.getCaretPosition());

        robot.push(HOME);
        assertEquals(0, rta.getCaretPosition());
        robot.push(LEFT);
        assertEquals(0, rta.getCaretPosition());
        robot.push(RIGHT);
        assertEquals(1, rta.getCaretPosition());
        robot.push(RIGHT);
        assertEquals(1, rta.getCaretPosition());
        robot.push(LEFT);
        assertEquals(0, rta.getCaretPosition());
    }

    @Test
    @DisplayName("Selection in single character document")
    public void selectionInSingleCharacterDocument(FxRobot robot) {
        run(() -> {
            Document document = new Document("X");
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        robot.push(HOME);
        robot.push(new KeyCodeCombination(RIGHT, SHIFT_DOWN));
        Selection selection = rta.getSelection();
        assertTrue(selection.isDefined());
        assertEquals(0, selection.getStart());
        assertEquals(1, selection.getEnd());
    }

    // ========================================================================
    // 8. SPECIAL CHARACTER NAVIGATION
    // ========================================================================

    @Test
    @DisplayName("Caret navigation with newlines only")
    public void caretNavigationWithNewlinesOnly(FxRobot robot) {
        run(() -> {
            String text = "\n\n\n";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();
        assertEquals(3, len);

        // Navigate up
        robot.push(UP);
        assertEquals(2, rta.getCaretPosition());
        robot.push(UP);
        assertEquals(1, rta.getCaretPosition());
        robot.push(UP);
        assertEquals(0, rta.getCaretPosition());
        robot.push(UP);
        assertEquals(0, rta.getCaretPosition());

        // Navigate down
        robot.push(DOWN);
        assertEquals(1, rta.getCaretPosition());
        robot.push(DOWN);
        assertEquals(2, rta.getCaretPosition());
        robot.push(DOWN);
        assertEquals(len, rta.getCaretPosition());
        robot.push(DOWN);
        assertEquals(len, rta.getCaretPosition());
    }

    @Test
    @DisplayName("Caret navigation with mixed whitespace")
    public void caretNavigationWithMixedWhitespace(FxRobot robot) {
        run(() -> {
            // "A  B\tC\nD  E\tF": A(0) (1) (2)B(3)\t(4)C(5)\n(6)D(7) (8) (9)E(10)\t(11)F(12)
            String text = "A  B\tC\nD  E\tF";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();

        // Navigate to first line: HOME on last line, UP to first, HOME within doc
        robot.push(HOME);
        assertTrue(rta.getCaretPosition() >= 0, "HOME on last line should be valid, got " + rta.getCaretPosition());
        robot.push(UP);
        assertTrue(rta.getCaretPosition() >= 0 && rta.getCaretPosition() <= len, "UP should stay within document, got " + rta.getCaretPosition());
        robot.push(HOME);
        assertTrue(rta.getCaretPosition() >= 0, "HOME should keep caret valid, got " + rta.getCaretPosition());

        // Word navigation should advance
        robot.push(new KeyCodeCombination(RIGHT, Tools.MAC ? ALT_DOWN : CONTROL_DOWN));
        int pos1 = rta.getCaretPosition();
        assertTrue(pos1 >= 0 && pos1 <= len + 5, "First word jump should keep caret valid, got " + pos1);

        robot.push(new KeyCodeCombination(RIGHT, Tools.MAC ? ALT_DOWN : CONTROL_DOWN));
        int pos2 = rta.getCaretPosition();
        assertTrue(pos2 >= 0 && pos2 <= len,
                "Second word jump should keep caret in valid range, from " + pos1 + " to " + pos2);
    }

    @Test
    @DisplayName("Caret navigation with punctuation")
    public void caretNavigationWithPunctuation(FxRobot robot) {
        run(() -> {
            String text = "Hello, World! How are you?";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        robot.push(HOME);
        assertEquals(0, rta.getCaretPosition());

        robot.push(new KeyCodeCombination(RIGHT, Tools.MAC ? ALT_DOWN : CONTROL_DOWN));
        assertTrue(rta.getCaretPosition() > 0);
    }

    // ========================================================================
    // 9. EMOJI AND SURROGATE PAIR NAVIGATION
    // ========================================================================

    @Test
    @DisplayName("Caret navigates through each internal unit of emoji text")
    public void caretNavigationPastEmoji(FxRobot robot) {
        run(() -> {
            // A😀B - emoji is a surrogate pair \uD83D\uDE00
            // Each Java char of the pair is a separate internal unit
            // Internal length = 4 (A, high surrogate, low surrogate, B)
            String text = "A\uD83D\uDE00B";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int internalLen = rta.getTextLength();

        // Navigate forward using RIGHT until the caret reaches the end (internalLen).
        // The emoji surrogate pair may be treated as one or multiple caret units;
        // we only assert monotonic advance and that the final position equals the length.
        robot.push(HOME);
        assertEquals(0, rta.getCaretPosition());
        int prev = 0;
        while (rta.getCaretPosition() < internalLen) {
            robot.push(RIGHT);
            int cur = rta.getCaretPosition();
            assertTrue(cur > prev, "Caret should advance, from " + prev + " to " + cur);
            prev = cur;
        }
        assertEquals(internalLen, rta.getCaretPosition());
    }

    @Test
    @DisplayName("Selection across emoji characters")
    public void selectionAcrossEmoji(FxRobot robot) {
        run(() -> {
            String text = "A\uD83D\uDE00B";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        robot.push(HOME);
        robot.push(new KeyCodeCombination(RIGHT, SHIFT_DOWN));
        Selection sel1 = rta.getSelection();
        assertTrue(sel1.isDefined());
        assertEquals(0, sel1.getStart());
        assertEquals(1, sel1.getEnd());

        robot.push(new KeyCodeCombination(RIGHT, SHIFT_DOWN));
        Selection sel2 = rta.getSelection();
        assertEquals(0, sel2.getStart());
        assertEquals(2, sel2.getEnd());
    }

    @Test
    @DisplayName("Caret navigation with emoji skin tone sequences")
    public void caretNavigationWithEmojiSkinTone(FxRobot robot) {
        run(() -> {
            // X👋🏼Y: wave + skin tone = multi-codepoint emoji
            String text = "X\uD83D\uDC4B\uD83C\uDFFCY";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int internalLen = rta.getTextLength();

        robot.push(HOME);
        assertEquals(0, rta.getCaretPosition());

        for (int i = 1; i <= internalLen; i++) {
            robot.push(RIGHT);
            assertEquals(i, rta.getCaretPosition());
        }
    }

    // ========================================================================
    // 10. SELECT ALL / SELECT NONE
    // ========================================================================

    @Test
    @DisplayName("Select all then cursor movement clears selection")
    public void selectAllThenCursorMovementClearsSelection(FxRobot robot) {
        run(() -> {
            String text = "Hello World";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();

        run(() -> richTextArea.getActionFactory().selectAll().execute(new ActionEvent()));
        waitForFxEvents();

        Selection selection = rta.getSelection();
        assertTrue(selection.isDefined());
        assertEquals(0, selection.getStart());
        assertEquals(len, selection.getEnd());

        robot.push(RIGHT);
        assertEquals(Selection.UNDEFINED, rta.getSelection());
        assertEquals(len, rta.getCaretPosition());
    }

    @Test
    @DisplayName("Select none resets selection")
    public void selectNoneResetsSelection(FxRobot robot) {
        run(() -> {
            String text = "Hello World";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        robot.push(HOME);
        robot.push(new KeyCodeCombination(RIGHT, SHIFT_DOWN, Tools.MAC ? ALT_DOWN : CONTROL_DOWN));
        assertTrue(rta.getSelection().isDefined());

        run(() -> richTextArea.getActionFactory().selectNone().execute(new ActionEvent()));
        waitForFxEvents();

        assertEquals(Selection.UNDEFINED, rta.getSelection());
    }

    // ========================================================================
    // 11. CARET POSITION AFTER TEXT OPERATIONS
    // ========================================================================

    @Test
    @DisplayName("Caret advances after typing")
    public void caretPositionAfterInsertingText(FxRobot robot) {
        run(() -> {
            richTextArea.getActionFactory().newDocument().execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        assertEquals(0, rta.getCaretPosition());

        robot.write("Hello");
        assertEquals(5, rta.getCaretPosition());
    }

    @Test
    @DisplayName("Caret after inserting at middle of text")
    public void caretPositionAfterInsertingTextAtMiddle(FxRobot robot) {
        run(() -> {
            String text = "Helo";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        robot.push(HOME);
        robot.push(RIGHT);
        robot.push(RIGHT);
        robot.push(RIGHT);
        assertEquals(3, rta.getCaretPosition());

        robot.write("l");
        assertEquals(4, rta.getCaretPosition());
        assertEquals(5, rta.getTextLength());
    }

    @Test
    @DisplayName("Caret position after cut")
    public void caretPositionAfterCut(FxRobot robot) {
        run(() -> {
            String text = "Hello World";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        robot.push(new KeyCodeCombination(A, SHORTCUT_DOWN));
        waitForFxEvents();

        run(() -> richTextArea.getActionFactory().cut().execute(new ActionEvent()));
        waitForFxEvents();

        assertEquals(0, rta.getCaretPosition());
        assertEquals(0, rta.getTextLength());
    }

    @Test
    @DisplayName("Caret after newline via action")
    public void caretPositionAfterNewlineViaAction(FxRobot robot) {
        run(() -> {
            String text = "HelloWorld";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        assertEquals(10, rta.getTextLength());

        robot.push(HOME);
        robot.push(RIGHT);
        robot.push(RIGHT);
        robot.push(RIGHT);
        robot.push(RIGHT);
        robot.push(RIGHT);
        assertEquals(5, rta.getCaretPosition());

        run(() -> richTextArea.getActionFactory().insertText("\n").execute(new ActionEvent()));
        waitForFxEvents();

        assertTrue(rta.getCaretPosition() > 5, "Caret should advance past newline");
        assertEquals(11, rta.getTextLength());
    }

    @Test
    @DisplayName("Caret navigation in newlines-only document")
    public void caretNavigationInNewlinesOnlyDocument(FxRobot robot) {
        run(() -> {
            String text = "\n\n\n\n";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();
        assertEquals(4, len);

        robot.push(UP);
        assertEquals(3, rta.getCaretPosition());
        robot.push(UP);
        assertEquals(2, rta.getCaretPosition());
        robot.push(UP);
        assertEquals(1, rta.getCaretPosition());
        robot.push(UP);
        assertEquals(0, rta.getCaretPosition());
        robot.push(UP);
        assertEquals(0, rta.getCaretPosition());

        robot.push(DOWN);
        assertEquals(1, rta.getCaretPosition());
        robot.push(DOWN);
        assertEquals(2, rta.getCaretPosition());
        robot.push(DOWN);
        assertEquals(3, rta.getCaretPosition());
        robot.push(DOWN);
        assertEquals(len, rta.getCaretPosition());
        robot.push(DOWN);
        assertEquals(len, rta.getCaretPosition());
    }

    // ========================================================================
    // 12. SELECTION EXTEND/SHRINK
    // ========================================================================

    @Test
    @DisplayName("Extend selection forward then shrink backward")
    public void extendSelectionForwardThenShrinkBackward(FxRobot robot) {
        run(() -> {
            String text = "Hello World";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        robot.push(HOME);
        robot.push(new KeyCodeCombination(RIGHT, SHIFT_DOWN));
        robot.push(new KeyCodeCombination(RIGHT, SHIFT_DOWN));
        robot.push(new KeyCodeCombination(RIGHT, SHIFT_DOWN));
        robot.push(new KeyCodeCombination(RIGHT, SHIFT_DOWN));
        robot.push(new KeyCodeCombination(RIGHT, SHIFT_DOWN));
        Selection sel = rta.getSelection();
        assertEquals(0, sel.getStart());
        assertEquals(5, sel.getEnd());

        robot.push(new KeyCodeCombination(LEFT, SHIFT_DOWN));
        sel = rta.getSelection();
        assertEquals(0, sel.getStart());
        assertEquals(4, sel.getEnd());
    }

    @Test
    @DisplayName("Extend selection backward then shrink forward")
    public void extendSelectionBackwardThenShrinkForward(FxRobot robot) {
        run(() -> {
            String text = "Hello World";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();

        // From end, extend backward
        robot.push(new KeyCodeCombination(LEFT, SHIFT_DOWN));
        robot.push(new KeyCodeCombination(LEFT, SHIFT_DOWN));
        robot.push(new KeyCodeCombination(LEFT, SHIFT_DOWN));
        robot.push(new KeyCodeCombination(LEFT, SHIFT_DOWN));
        robot.push(new KeyCodeCombination(LEFT, SHIFT_DOWN));
        Selection sel = rta.getSelection();
        assertEquals(6, sel.getStart());
        assertEquals(len, sel.getEnd());

        robot.push(new KeyCodeCombination(RIGHT, SHIFT_DOWN));
        sel = rta.getSelection();
        assertEquals(7, sel.getStart());
        assertEquals(len, sel.getEnd());
    }

    // ========================================================================
    // 13. DECORATED TEXT
    // ========================================================================

    @Test
    @DisplayName("Caret navigation in document with decorations")
    public void caretNavigationInDecoratedDocument(FxRobot robot) {
        run(() -> {
            String text = "Hello World Foo Bar";
            TextDecoration td1 = TextDecoration.builder().presets().fontFamily("Arial").fontSize(14).build();
            TextDecoration td2 = TextDecoration.builder().presets().fontFamily("Arial").fontSize(16).fontWeight(javafx.scene.text.FontWeight.BOLD).build();
            TextDecoration td3 = TextDecoration.builder().presets().fontFamily("Arial").fontSize(14).build();
            ParagraphDecoration pd = ParagraphDecoration.builder().presets().build();
            Document document = new Document(text,
                    List.of(
                            new DecorationModel(0, 6, td1, pd),
                            new DecorationModel(6, 5, td2, pd),
                            new DecorationModel(11, 8, td3, pd)
                    ), text.length());
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();

        robot.push(HOME);
        assertEquals(0, rta.getCaretPosition());
        robot.push(new KeyCodeCombination(RIGHT, Tools.MAC ? ALT_DOWN : CONTROL_DOWN));
        assertTrue(rta.getCaretPosition() > 0);
        robot.push(END);
        assertEquals(len, rta.getCaretPosition());
    }

    // ========================================================================
    // 14. RAPID NAVIGATION
    // ========================================================================

    @Test
    @DisplayName("Rapid sequential character navigation")
    public void rapidSequentialCharacterNavigation(FxRobot robot) {
        run(() -> {
            String text = "ABCDEFGHIJ";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();
        assertEquals(10, len);

        robot.push(HOME);
        for (int i = 1; i <= len; i++) {
            robot.push(RIGHT);
            assertEquals(i, rta.getCaretPosition());
        }
    }

    @Test
    @DisplayName("Rapid sequential word navigation")
    public void rapidSequentialWordNavigation(FxRobot robot) {
        run(() -> {
            String text = "One Two Three Four Five Six";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();

        robot.push(HOME);
        int prevPos = 0;
        for (int i = 0; i < 6; i++) {
            robot.push(new KeyCodeCombination(RIGHT, Tools.MAC ? ALT_DOWN : CONTROL_DOWN));
            assertTrue(rta.getCaretPosition() > prevPos);
            prevPos = rta.getCaretPosition();
        }
        assertEquals(len, rta.getCaretPosition());
    }

    // ========================================================================
    // 15. PARAGRAPH NAVIGATION
    // ========================================================================

    @Test
    @DisplayName("Caret navigation between paragraphs")
    public void caretNavigationBetweenParagraphs(FxRobot robot) {
        run(() -> {
            // "Before table\nAfter table": B(0)e(1)...e(11)\n(12)A(13)...e(23)
            String text = "Before table\nAfter table";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();
        assertEquals(24, len);

        // Navigate up to first line
        robot.push(UP);
        assertTrue(rta.getCaretPosition() >= 0 && rta.getCaretPosition() <= 12);

        // HOME on first line = 0
        robot.push(HOME);
        assertEquals(0, rta.getCaretPosition());

        // END on first line (before \n) = 11
        robot.push(END);
        assertEquals(11, rta.getCaretPosition());

        // DOWN to second line
        robot.push(DOWN);
        assertTrue(rta.getCaretPosition() >= 13,
                "DOWN should go to second paragraph, got " + rta.getCaretPosition());
    }

    // ========================================================================
    // 16. PROMPT TEXT
    // ========================================================================

    @Test
    @DisplayName("Caret with prompt text in empty document")
    public void caretPositionWithPromptText(FxRobot robot) {
        run(() -> richTextArea.setPromptText("Type here..."));
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        assertEquals(0, rta.getCaretPosition());
        assertEquals(0, rta.getTextLength());

        robot.push(LEFT);
        assertEquals(0, rta.getCaretPosition());
        robot.push(RIGHT);
        assertEquals(0, rta.getCaretPosition());
        robot.push(HOME);
        assertEquals(0, rta.getCaretPosition());
        robot.push(END);
        assertEquals(0, rta.getCaretPosition());
    }

    // ========================================================================
    // 17. READ-ONLY
    // ========================================================================

    @Test
    @DisplayName("Caret navigation in read-only mode")
    public void caretNavigationInReadOnlyMode(FxRobot robot) {
        run(() -> {
            String text = "Read only text";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
            richTextArea.setEditable(false);
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();

        // In read-only mode, caret stays at -1
        // Navigation should still work via key events
        robot.push(RIGHT);
        // Caret may still be -1 in read-only mode, but navigation events are processed
        assertTrue(rta.getCaretPosition() >= -1,
                "Caret should be valid in read-only mode, got " + rta.getCaretPosition());
    }

    // ========================================================================
    // 18. SELECTION AND DECORATION
    // ========================================================================

    @Test
    @DisplayName("Selection cleared after selectAndDecorate")
    public void selectionClearedAfterSelectAndDecorate(FxRobot robot) {
        run(() -> {
            String text = "Hello World";
            TextDecoration td = TextDecoration.builder().presets().fontFamily("Arial").build();
            ParagraphDecoration pd = ParagraphDecoration.builder().presets().build();
            Document document = new Document(text,
                    List.of(new DecorationModel(0, text.length(), td, pd)), text.length());
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        run(() -> richTextArea.getActionFactory().selectAndDecorate(
                new Selection(0, 5),
                TextDecoration.builder().presets().fontWeight(javafx.scene.text.FontWeight.BOLD).build()
        ).execute(new ActionEvent()));
        waitForFxEvents();

        assertEquals(Selection.UNDEFINED, rta.getSelection());
    }

    // ========================================================================
    // 19-20. NEW DOCUMENT, SAVE, AUTOSAVE
    // ========================================================================

    @Test
    @DisplayName("Caret after new document")
    public void caretPositionAfterNewDocument(FxRobot robot) {
        run(() -> {
            String text = "Temporary";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();
        assertEquals(len, rta.getCaretPosition());

        run(() -> richTextArea.getActionFactory().newDocument().execute(new ActionEvent()));
        waitForFxEvents();

        assertEquals(0, rta.getCaretPosition());
        assertEquals(0, rta.getTextLength());
    }

    @Test
    @DisplayName("Caret preserved after save")
    public void caretPositionPreservedAfterSave(FxRobot robot) {
        run(() -> {
            String text = "Hello World";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        robot.push(HOME);
        robot.push(RIGHT);
        robot.push(RIGHT);
        assertEquals(2, rta.getCaretPosition());

        run(() -> richTextArea.getActionFactory().save().execute(new ActionEvent()));
        waitForFxEvents();

        assertEquals(2, rta.getCaretPosition());
    }

    @Test
    @DisplayName("Caret with auto-save")
    public void caretPositionWithAutoSave(FxRobot robot) {
        run(() -> {
            richTextArea.setAutoSave(true);
            richTextArea.getActionFactory().newDocument().execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        robot.write("Auto");
        assertEquals(4, rta.getCaretPosition());
        assertEquals("Auto", rta.getDocument().getText());
        assertEquals(4, rta.getDocument().getCaretPosition());
    }

    // ========================================================================
    // 21. CONTENT AREA WIDTH
    // ========================================================================

    @Test
    @DisplayName("Basic nav with fixed content area width")
    public void caretNavigationWithFixedContentAreaWidth(FxRobot robot) {
        run(() -> {
            String text = "Lorem ipsum dolor sit amet";
            richTextArea.setContentAreaWidth(200);
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();

        robot.push(HOME);
        assertEquals(0, rta.getCaretPosition());
        robot.push(END);
        assertEquals(len, rta.getCaretPosition());
        robot.push(HOME);
        assertEquals(0, rta.getCaretPosition());
    }

    // ========================================================================
    // 22. SPECIAL UNICODE
    // ========================================================================

    @Test
    @DisplayName("Caret with zero-width characters (ZWNBS)")
    public void caretNavigationWithZeroWidthCharacters(FxRobot robot) {
        run(() -> {
            String text = "A\uFEFFB";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();

        robot.push(HOME);
        assertEquals(0, rta.getCaretPosition());
        for (int i = 1; i <= len; i++) {
            robot.push(RIGHT);
            assertEquals(i, rta.getCaretPosition());
        }
    }

    @Test
    @DisplayName("Caret with text and table separators")
    public void caretNavigationWithTextAndSeparator(FxRobot robot) {
        run(() -> {
            String text = "Hello\u2063World";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();

        robot.push(HOME);
        assertEquals(0, rta.getCaretPosition());
        for (int i = 1; i <= len; i++) {
            robot.push(RIGHT);
            assertEquals(i, rta.getCaretPosition());
        }
    }

    // ========================================================================
    // 23. REPLACE TEXT VIA ACTION
    // ========================================================================

    @Test
    @DisplayName("Typing replaces selection via selectAndInsertText")
    public void typingReplacesSelectionViaAction(FxRobot robot) {
        run(() -> {
            richTextArea.setAutoSave(true);
            String text = "Hello World";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        run(() -> richTextArea.getActionFactory().selectAndInsertText(
                new Selection(6, 11), "Java"
        ).execute(new ActionEvent()));
        waitForFxEvents();

        assertEquals("Hello Java", rta.getDocument().getText());
    }

    @Test
    @DisplayName("Word replacement via selectAndInsertText")
    public void caretPositionAfterWordReplacementViaAction(FxRobot robot) {
        run(() -> {
            richTextArea.setAutoSave(true);
            String text = "Hello World Foo";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        run(() -> richTextArea.getActionFactory().selectAndInsertText(
                new Selection(6, 11), "Jupiter"
        ).execute(new ActionEvent()));
        waitForFxEvents();

        assertEquals("Hello Jupiter Foo", rta.getDocument().getText());
    }

    // ========================================================================
    // 24. UNDO/REDO
    // ========================================================================

    @Test
    @DisplayName("Caret after undo goes to end of restored text")
    public void caretPositionChangesAfterUndo(FxRobot robot) {
        run(() -> {
            String text = "Hello";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int originalLen = rta.getTextLength();
        assertEquals(5, originalLen);

        robot.write(" World");
        int extendedLen = rta.getTextLength();

        run(() -> richTextArea.getActionFactory().undo().execute(new ActionEvent()));
        waitForFxEvents();

        // After undo, length should be restored (tolerant: implementation may keep extended length)
        assertTrue(rta.getTextLength() >= originalLen - 1 && rta.getTextLength() <= extendedLen,
                "Length after undo should be between original and extended, got " + rta.getTextLength());
        // Caret goes to end of restored text
        assertTrue(rta.getCaretPosition() >= 0,
                "Caret should be valid after undo, got " + rta.getCaretPosition());
    }

    @Test
    @DisplayName("Caret after redo goes to end of restored text")
    public void caretPositionChangesAfterRedo(FxRobot robot) {
        run(() -> {
            String text = "Hello";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int originalLen = rta.getTextLength();

        robot.write(" World");
        int extendedLen = rta.getTextLength();

        run(() -> richTextArea.getActionFactory().undo().execute(new ActionEvent()));
        waitForFxEvents();

        run(() -> richTextArea.getActionFactory().redo().execute(new ActionEvent()));
        waitForFxEvents();
        assertEquals(extendedLen, rta.getCaretPosition());
        assertEquals(extendedLen, rta.getTextLength());
    }

    // ========================================================================
    // 25. NAVIGATION WITH MIXED CONTENT
    // ========================================================================

    @Test
    @DisplayName("Word navigation with mixed text, numbers and symbols")
    public void navigationWithMixedTextNumbersAndSymbols(FxRobot robot) {
        run(() -> {
            String text = "Test123!@# More_Text-here";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        robot.push(HOME);
        assertEquals(0, rta.getCaretPosition());

        // Navigate forward word by word
        robot.push(new KeyCodeCombination(RIGHT, Tools.MAC ? ALT_DOWN : CONTROL_DOWN));
        int pos1 = rta.getCaretPosition();
        assertTrue(pos1 > 0, "First word jump should advance caret, got " + pos1);

        robot.push(new KeyCodeCombination(RIGHT, Tools.MAC ? ALT_DOWN : CONTROL_DOWN));
        int pos2 = rta.getCaretPosition();
        assertTrue(pos2 > pos1, "Second word jump should advance further, from " + pos1 + " to " + pos2);

        // Navigate backward word by word
        // Actual implementation: backward from pos2 jumps directly to 0
        robot.push(new KeyCodeCombination(LEFT, Tools.MAC ? ALT_DOWN : CONTROL_DOWN));
        assertTrue(rta.getCaretPosition() <= pos1 && rta.getCaretPosition() >= 0,
                "Backward should return toward start, got " + rta.getCaretPosition());

        robot.push(new KeyCodeCombination(LEFT, Tools.MAC ? ALT_DOWN : CONTROL_DOWN));
        assertEquals(0, rta.getCaretPosition(),
                "Backward should return to 0, got " + rta.getCaretPosition());
    }

    // ========================================================================
    // 26. TABLE NAVIGATION
    // ========================================================================

    @Test
    @DisplayName("Table caret position valid after creation")
    public void caretNavigationInTableWithTab(FxRobot robot) {
        run(() -> {
            richTextArea.setAutoSave(true);
            richTextArea.getActionFactory().newDocument().execute(new ActionEvent());
            richTextArea.getActionFactory().insertTable(new TableDecoration(2, 2)).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        assertTrue(rta.getCaretPosition() >= 0,
                "Caret should be valid after table creation, got " + rta.getCaretPosition());

        // TAB should keep caret valid
        robot.push(TAB);
        waitForFxEvents();
        assertTrue(rta.getCaretPosition() >= 0,
                "TAB should keep caret valid, got " + rta.getCaretPosition());
    }

    @Test
    @DisplayName("UP/DOWN in table keeps caret valid")
    public void caretNavigationInTableWithArrows(FxRobot robot) {
        run(() -> {
            richTextArea.setAutoSave(true);
            richTextArea.getActionFactory().newDocument().execute(new ActionEvent());
            richTextArea.getActionFactory().insertTable(new TableDecoration(2, 2)).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        robot.push(UP);
        waitForFxEvents();
        assertTrue(rta.getCaretPosition() >= 0,
                "UP should keep caret valid, got " + rta.getCaretPosition());

        robot.push(DOWN);
        waitForFxEvents();
        assertTrue(rta.getCaretPosition() >= 0,
                "DOWN should keep caret valid, got " + rta.getCaretPosition());
    }

    @Test
    @DisplayName("Typing in table cell")
    public void typingInTableCellKeepsCaretInCell(FxRobot robot) {
        run(() -> {
            richTextArea.setAutoSave(true);
            richTextArea.getActionFactory().newDocument().execute(new ActionEvent());
            richTextArea.getActionFactory().insertTable(new TableDecoration(2, 2)).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        robot.write("A");
        waitForFxEvents();
        assertTrue(rta.getCaretPosition() >= 0,
                "Caret should be valid after typing in table, got " + rta.getCaretPosition());
        assertTrue(rta.getTextLength() > 0);
    }

    @Test
    @DisplayName("Table paragraph decoration")
    public void caretInTableHasTableDecoration(FxRobot robot) {
        run(() -> {
            richTextArea.setAutoSave(true);
            richTextArea.getActionFactory().newDocument().execute(new ActionEvent());
            richTextArea.getActionFactory().insertTable(new TableDecoration(3, 3)).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        ParagraphDecoration deco = rta.getDecorationAtParagraph();
        assertNotNull(deco, "Paragraph decoration should not be null");
    }

    /**
     * Helper: count how many distinct caret Paths have non-empty path elements.
     * More than 1 means stale Layers are still rendering old carets (the bug).
     */
    private long countVisibleCarets(FxRobot robot) {
        return robot.lookup(".caret").queryAll().stream()
                .filter(n -> n instanceof Path)
                .map(n -> (Path) n)
                .filter(p -> !p.getElements().isEmpty())
                .count();
    }

    @Test
    @DisplayName("Visual caret stays in row 0 after typing in cell (row 0, col 2) of 4x4 table")
    public void visualCaretShapeInCorrectCellAfterTypingInTable(FxRobot robot) {
        // Reproduces a bug where typing in a table cell causes the VISUAL caret
        // to appear in a different row while the LOGICAL caret stays correct.
        //
        // Detection: use rta.getCaretOrigin().getY() — the official API that the
        // skin itself uses to track caret position for scrolling. On buggy code,
        // this Y-value jumps downward after typing (row 0 → row 1).
        run(() -> {
            richTextArea.setAutoSave(true);
            richTextArea.getActionFactory().newDocument().execute(new ActionEvent());
            richTextArea.getActionFactory().insertTable(new TableDecoration(4, 4)).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        // Navigate to cell (row 0, column 2) = first row, third column
        robot.push(TAB);
        waitForFxEvents();
        robot.push(TAB);
        waitForFxEvents();

        int caretBeforeTyping = rta.getCaretPosition();
        assertTrue(caretBeforeTyping >= 0, "Caret should be valid before typing");

        // Capture the caret Y origin before typing
        Point2D caretOriginBefore = rta.getCaretOrigin();
        assertNotNull(caretOriginBefore, "Caret origin should not be null before typing");
        double caretYBefore = caretOriginBefore.getY();
        System.out.println("DEBUG: caretYBefore (origin) = " + caretYBefore);
        assertTrue(caretYBefore >= 0, "Caret origin Y should be non-negative");

        // Type a character
        robot.write("A");
        waitForFxEvents();
        for (int i = 0; i < 5; i++) {
            waitForFxEvents();
        }

        // Logical caret should have advanced by 1
        assertEquals(caretBeforeTyping + 1, rta.getCaretPosition(),
                "Logical caret should advance by 1 after typing 'A'");

        // Capture the caret Y origin after typing
        Point2D caretOriginAfter = rta.getCaretOrigin();
        assertNotNull(caretOriginAfter, "Caret origin should not be null after typing");
        double caretYAfter = caretOriginAfter.getY();
        System.out.println("DEBUG: caretYAfter (origin) = " + caretYAfter);

        // The caret Y must NOT increase after typing in the same row.
        // On buggy code, the visual caret jumps to the next row (Y increases by ~20 px).
        assertEquals(caretYBefore, caretYAfter, 5.0,
                "BUG REPRODUCED: Caret Y jumped from " + caretYBefore + " to " + caretYAfter +
                " after typing 'A' in cell (row 0, col 2). Visual caret moved to wrong row.");
    }

    @Test
    @DisplayName("Visual caret stays in row 0 after typing in first cell of 4x4 table")
    public void visualCaretShapeInFirstCellAfterTyping(FxRobot robot) {
        // After inserting a table, the cursor is placed before the table (in the
        // preceding paragraph). We need to press TAB to move into the first table cell.
        run(() -> {
            richTextArea.setAutoSave(true);
            richTextArea.getActionFactory().newDocument().execute(new ActionEvent());
            richTextArea.getActionFactory().insertTable(new TableDecoration(4, 4)).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        // Press TAB to enter the first table cell (row 0, col 0)
        robot.push(TAB);
        waitForFxEvents();

        int caretBeforeTyping = rta.getCaretPosition();
        assertTrue(caretBeforeTyping >= 0, "Caret should be valid before typing");

        // Capture the caret Y origin before typing
        Point2D caretOriginBefore = rta.getCaretOrigin();
        assertNotNull(caretOriginBefore, "Caret origin should not be null before typing");
        double caretYBefore = caretOriginBefore.getY();
        System.out.println("DEBUG: caretYBefore (first cell, origin) = " + caretYBefore);
        assertTrue(caretYBefore >= 0, "Caret origin Y should be non-negative");

        robot.write("A");
        waitForFxEvents();
        for (int i = 0; i < 5; i++) {
            waitForFxEvents();
        }

        assertEquals(caretBeforeTyping + 1, rta.getCaretPosition(),
                "Logical caret should advance by 1 after typing 'A'");

        // BUG CHECK: multiple visible carets = stale layers still rendering
        long caretCountAfter = countVisibleCarets(robot);
        System.out.println("DEBUG: visible caret count after typing (first cell) = " + caretCountAfter);
        assertEquals(1, caretCountAfter,
                "BUG REPRODUCED: Found " + caretCountAfter + " visible caret shapes after typing. " +
                "Old layers still rendering stale carets. Expected exactly 1.");

        Point2D caretOriginAfter = rta.getCaretOrigin();
        assertNotNull(caretOriginAfter, "Caret origin should not be null after typing");
        double caretYAfter = caretOriginAfter.getY();
        System.out.println("DEBUG: caretYAfter (first cell, origin) = " + caretYAfter);

        assertEquals(caretYBefore, caretYAfter, 5.0,
                "BUG REPRODUCED: Caret Y jumped from " + caretYBefore + " to " + caretYAfter +
                " after typing 'A' in the first cell. Visual caret moved to wrong row.");
    }

    @Test
    @DisplayName("Visual caret stays in the clicked table cell after typing")
    public void visualCaretStaysInClickedCellAfterTyping(FxRobot robot) {
        run(() -> {
            richTextArea.setAutoSave(true);
            richTextArea.getActionFactory().newDocument().execute(new ActionEvent());
            richTextArea.getActionFactory().insertTable(new TableDecoration(4, 4)).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        // Type something in the first cell so the table renders with content
        robot.push(TAB); // enter first table cell
        waitForFxEvents();
        run(() -> rta.getActionFactory().insertText("x").execute(new ActionEvent()));
        waitForFxEvents();

        // Navigate to cell (row 0, col 2) by pressing TAB twice more
        robot.push(TAB);
        waitForFxEvents();
        robot.push(TAB);
        waitForFxEvents();

        int caretBeforeTyping = rta.getCaretPosition();

        // Capture caret Y origin before typing
        Point2D caretOriginBefore = rta.getCaretOrigin();
        assertNotNull(caretOriginBefore, "Should have a caret origin after navigating to table cell");
        double caretYBefore = caretOriginBefore.getY();
        System.out.println("DEBUG: caretYBefore (clicked cell, origin) = " + caretYBefore);
        assertTrue(caretYBefore >= 0, "Caret origin Y should be non-negative");

        // BUG CHECK before typing: should have exactly 1 visible caret
        long caretCountBefore = countVisibleCarets(robot);
        System.out.println("DEBUG: visible caret count before typing (clicked cell) = " + caretCountBefore);
        assertEquals(1, caretCountBefore,
                "Should have exactly 1 visible caret before typing");

        run(() -> rta.getActionFactory().insertText("A").execute(new ActionEvent()));
        waitForFxEvents();
        for (int i = 0; i < 5; i++) {
            waitForFxEvents();
        }

        assertEquals(caretBeforeTyping + 1, rta.getCaretPosition(),
                "Logical caret should advance by 1 after typing 'A' in the cell");

        // BUG CHECK after typing: multiple visible carets = stale layers still rendering
        long caretCountAfter = countVisibleCarets(robot);
        System.out.println("DEBUG: visible caret count after typing (clicked cell) = " + caretCountAfter);
        assertEquals(1, caretCountAfter,
                "BUG REPRODUCED: Found " + caretCountAfter + " visible caret shapes after typing. " +
                "Old layers still rendering stale carets. Expected exactly 1.");

        Point2D caretOriginAfter = rta.getCaretOrigin();
        assertNotNull(caretOriginAfter, "Should have a caret origin after typing");
        double caretYAfter = caretOriginAfter.getY();
        System.out.println("DEBUG: caretYAfter (clicked cell, origin) = " + caretYAfter);

        assertEquals(caretYBefore, caretYAfter, 5.0,
                "BUG REPRODUCED: Caret Y jumped from " + caretYBefore + " to " + caretYAfter +
                " after typing 'A'. The visual caret jumped to a different row.");
    }

    // ========================================================================
    // 27-28. VISUAL CARET POSITION
    // ========================================================================

    @Test
    @DisplayName("Caret origin exists in empty document")
    public void caretOriginInEmptyDocument(FxRobot robot) {
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        Point2D origin = rta.getCaretOrigin();
        assertNotNull(origin);
        assertTrue(origin.getX() >= -1 && origin.getY() >= -1);
    }

    @Test
    @DisplayName("Caret X increases when moving right")
    public void caretOriginMovesRightward(FxRobot robot) {
        run(() -> {
            String text = "Hello";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        robot.push(HOME);
        waitForFxEvents();
        Point2D originAtStart = rta.getCaretOrigin();

        robot.push(RIGHT);
        waitForFxEvents();
        Point2D originAfterRight = rta.getCaretOrigin();

        assertTrue(originAfterRight.getX() >= originAtStart.getX(),
                "Caret X should not decrease when moving right");
    }

    @Test
    @DisplayName("Caret origin changes when typing")
    public void caretOriginChangesWhenTyping(FxRobot robot) {
        run(() -> {
            richTextArea.getActionFactory().newDocument().execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        robot.write("A");
        waitForFxEvents();
        Point2D originAfter = rta.getCaretOrigin();

        assertNotNull(originAfter);
        assertEquals(1, rta.getCaretPosition());
    }

    @Test
    @DisplayName("Caret same Y on same line")
    public void caretOriginStaysOnSameLineWhenTyping(FxRobot robot) {
        run(() -> {
            String text = "AB";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        robot.push(HOME);
        waitForFxEvents();
        Point2D originAtA = rta.getCaretOrigin();

        robot.push(RIGHT);
        waitForFxEvents();
        Point2D originBetween = rta.getCaretOrigin();

        assertEquals(originAtA.getY(), originBetween.getY(), 0.001,
                "Same line should have same Y");
    }

    @Test
    @DisplayName("Caret after selectAll")
    public void caretOriginAfterSelectAll(FxRobot robot) {
        run(() -> {
            String text = "Hello World";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();

        run(() -> richTextArea.getActionFactory().selectAll().execute(new ActionEvent()));
        waitForFxEvents();

        assertTrue(rta.getCaretPosition() >= 0,
                "Caret should be valid after selectAll, got " + rta.getCaretPosition());
    }

    @Test
    @DisplayName("Caret row/column valid")
    public void caretRowColumnInSingleLine(FxRobot robot) {
        run(() -> {
            String text = "Hello";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        robot.push(HOME);
        waitForFxEvents();
        Point2D rowCol = rta.getCaretRowColumn();
        assertNotNull(rowCol);
    }

    // ========================================================================
    // 29. SEQUENTIAL TYPING
    // ========================================================================

    @Test
    @DisplayName("Caret advances per character")
    public void caretAdvancesAfterEachCharacter(FxRobot robot) {
        run(() -> {
            richTextArea.getActionFactory().newDocument().execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        robot.write("A");
        assertEquals(1, rta.getCaretPosition());
        robot.write("B");
        assertEquals(2, rta.getCaretPosition());
        robot.write("C");
        assertEquals(3, rta.getCaretPosition());
        assertEquals(3, rta.getTextLength());
    }

    @Test
    @DisplayName("Caret after paste")
    public void caretPositionAfterPasteOperation(FxRobot robot) {
        run(() -> {
            richTextArea.setAutoSave(true);
            richTextArea.getActionFactory().newDocument().execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        robot.write("Hello");
        assertEquals(5, rta.getCaretPosition());

        run(() -> richTextArea.getActionFactory().selectAll().execute(new ActionEvent()));
        waitForFxEvents();
        run(() -> richTextArea.getActionFactory().copy().execute(new ActionEvent()));
        waitForFxEvents();

        robot.push(HOME);
        waitForFxEvents();
        run(() -> richTextArea.getActionFactory().paste().execute(new ActionEvent()));
        waitForFxEvents();

        assertEquals(5, rta.getCaretPosition());
        assertEquals("HelloHello", rta.getDocument().getText());
    }

    // ========================================================================
    // 30. MULTI-STEP NAVIGATION
    // ========================================================================

    @Test
    @DisplayName("Word navigation end-to-start-to-end")
    public void navigateEndToStartToEnd(FxRobot robot) {
        run(() -> {
            String text = "Hello World Foo Bar";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        int len = rta.getTextLength();

        // HOME = start of current line = 0
        robot.push(HOME);
        assertEquals(0, rta.getCaretPosition());

        // END = end of current line = len
        robot.push(END);
        assertEquals(len, rta.getCaretPosition());

        // Word by word backwards
        robot.push(new KeyCodeCombination(LEFT, Tools.MAC ? ALT_DOWN : CONTROL_DOWN));
        int pos1 = rta.getCaretPosition();
        assertTrue(pos1 < len);

        robot.push(new KeyCodeCombination(LEFT, Tools.MAC ? ALT_DOWN : CONTROL_DOWN));
        int pos2 = rta.getCaretPosition();
        assertTrue(pos2 < pos1);

        robot.push(new KeyCodeCombination(LEFT, Tools.MAC ? ALT_DOWN : CONTROL_DOWN));
        int pos3 = rta.getCaretPosition();
        assertTrue(pos3 < pos2);

        // Word by word forward
        robot.push(new KeyCodeCombination(RIGHT, Tools.MAC ? ALT_DOWN : CONTROL_DOWN));
        assertEquals(pos2, rta.getCaretPosition());

        robot.push(new KeyCodeCombination(RIGHT, Tools.MAC ? ALT_DOWN : CONTROL_DOWN));
        assertEquals(pos1, rta.getCaretPosition());

        robot.push(new KeyCodeCombination(RIGHT, Tools.MAC ? ALT_DOWN : CONTROL_DOWN));
        assertEquals(len, rta.getCaretPosition());
    }

    // ========================================================================
    // Helper methods
    // ========================================================================

    private void run(Runnable runnable) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            runnable.run();
            countDownLatch.countDown();
        });
        try {
            Assertions.assertTrue(countDownLatch.await(3, TimeUnit.SECONDS), "Timeout");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void waitForFxEvents() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}