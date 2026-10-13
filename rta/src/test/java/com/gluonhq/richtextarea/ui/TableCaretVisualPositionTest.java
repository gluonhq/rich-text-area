/*
 * Copyright (c) 2024, Gluon
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
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.richtextarea.ui;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.action.Action;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.TableDecoration;
import com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.stage.Stage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Init;
import org.testfx.framework.junit5.Start;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

/**
 * Verifies the <em>visual</em> caret position, i.e. the "Table Caret Visual Jump" bug: while
 * typing in a table cell the caret was rendered one line too low, although the logical caret
 * position was correct.
 *
 * <p>All measurements are taken by this test, the production code stays silent:</p>
 * <ul>
 *   <li>the caret {@link Path} node of the active layer (style class {@code .caret}), i.e.
 *       exactly the values that used to be logged with {@code System.out.println("Caret
 *       position: ... , visual position: (...)")}: the raw {@link MoveTo} coordinates inside
 *       the TextFlow plus the same point transformed to scene coordinates,</li>
 *   <li>{@link RichTextArea#getCaretOrigin()}, the public caret origin of the control,</li>
 *   <li>the logical caret position of the view model.</li>
 * </ul>
 *
 * <p>Every assertion message contains the full measurement, so a failing run reproduces the old
 * debug output, while a successful run stays quiet.</p>
 */
@ExtendWith(ApplicationExtension.class)
public class TableCaretVisualPositionTest {

    /** One line of the default font is about 19px high, anything above that is a line jump. */
    private static final double MAX_SAME_LINE_OFFSET_PX = 5.0;
    /** Minimum distance between two text lines, proves that the measurement reacts at all. */
    private static final double MIN_LINE_DISTANCE_PX = 10.0;
    private static final long UPDATE_TIMEOUT_MS = 5_000;

    private RichTextArea richTextArea;
    private Scene scene;

    @Init
    public void init() {
        richTextArea = new RichTextArea();
        richTextArea.setPrefSize(800, 600);
    }

    @Start
    public void start(Stage stage) {
        VBox root = new VBox(richTextArea);
        root.setStyle("-fx-padding: 10px; -fx-background-color: white;");
        scene = new Scene(root, 820, 700);
        stage.setScene(scene);
        stage.setTitle("Table Caret Visual Position Test");
        stage.show();
    }

    /**
     * Measurement of the caret at one point in time. {@code localX/localY} are the raw path
     * coordinates inside the TextFlow (the values of the former debug output),
     * {@code sceneX/sceneY} the same point in scene coordinates, {@code originX/originY} the
     * public caret origin of the control.
     */
    private record CaretSnapshot(int logicalPosition, boolean rendered,
                                 double localX, double localY,
                                 double sceneX, double sceneY,
                                 double originX, double originY) {

        @Override
        public String toString() {
            return "Caret position: " + logicalPosition +
                    (rendered
                            ? ", visual position: (" + sceneX + ", " + sceneY + ")"
                              + ", text flow position: (" + localX + ", " + localY + ")"
                              + ", caret origin: (" + originX + ", " + originY + ")"
                            : ", no caret path rendered");
        }
    }

    @Test
    @DisplayName("Table caret: typing keeps the visual caret on the same line")
    public void typingInTableCellKeepsCaretOnSameLine(FxRobot robot) {
        insertTable(4, 4);
        waitForFxEvents();

        int tableStart = moveCaretToTableStart();
        assertTrue(tableStart >= 0, "The document must contain a table paragraph");

        CaretSnapshot before = waitForCaret(tableStart, "caret at the beginning of the first cell");
        assertTrue(before.rendered(), "Caret must be rendered before typing: " + before);

        robot.write("a");

        // The logical caret is updated synchronously, ParagraphTile renders the caret path in a
        // later FX pulse - waitForCaret() drains that pulse before it takes the measurement.
        CaretSnapshot after = waitForCaret(tableStart + 1, "caret after typing 'a'");

        assertTrue(after.rendered(), "Caret must be rendered after typing: " + after);

        // Regression of the visual jump bug: the caret path was calculated from the stale
        // (empty) TextFlow content and ended up one line lower, a Y difference of about 19px.
        assertEquals(before.sceneY(), after.sceneY(), MAX_SAME_LINE_OFFSET_PX,
                "The visual caret jumped to another line while typing in the table cell.\n"
                        + "before: " + before + "\nafter:  " + after);
        assertEquals(before.localY(), after.localY(), MAX_SAME_LINE_OFFSET_PX,
                "The caret path inside the TextFlow moved to another line while typing.\n"
                        + "before: " + before + "\nafter:  " + after);
        assertEquals(before.originY(), after.originY(), MAX_SAME_LINE_OFFSET_PX,
                "The public caret origin moved to another line while typing.\n"
                        + "before: " + before + "\nafter:  " + after);

        // The caret has to move past the typed character as well - with a stale TextFlow the
        // caret path stays at the beginning of the (then empty) line.
        assertTrue(after.sceneX() > before.sceneX(),
                "The caret must advance horizontally after typing 'a'.\n"
                        + "before: " + before + "\nafter:  " + after);
    }

    @Test
    @DisplayName("Caret visual position follows the logical caret from line to line")
    public void caretVisualPositionFollowsLogicalCaret(FxRobot robot) {
        String firstLine = "Line 1";
        openDocument(firstLine + "\nLine 2");
        waitForFxEvents();

        CaretSnapshot onFirstLine = moveCaretTo(0, "caret on the first line");

        // Moving the caret to the second line has to change the measured Y coordinate,
        // otherwise the assertions of the table test could never detect a line jump.
        CaretSnapshot onSecondLine = moveCaretTo(firstLine.length() + 1, "caret on the second line");
        double lineDistance = onSecondLine.sceneY() - onFirstLine.sceneY();
        assertTrue(lineDistance >= MIN_LINE_DISTANCE_PX,
                "The measurement must react when the caret moves to the next line, but the Y "
                        + "distance was only " + lineDistance + "px.\n"
                        + "first line:  " + onFirstLine + "\nsecond line: " + onSecondLine);

        // Moving back has to return to the original position.
        CaretSnapshot backOnFirstLine = moveCaretTo(0, "caret back on the first line");
        assertEquals(onFirstLine.sceneY(), backOnFirstLine.sceneY(), 1.0,
                "The caret must return to its previous visual position.\n"
                        + "original: " + onFirstLine + "\nreturned: " + backOnFirstLine);
        assertEquals(onFirstLine.sceneX(), backOnFirstLine.sceneX(), 1.0,
                "The caret must return to its previous visual position.\n"
                        + "original: " + onFirstLine + "\nreturned: " + backOnFirstLine);
    }

    // __TESTS__

    // ==================== MEASUREMENT ====================

    /**
     * Waits until the logical caret reaches {@code expectedPosition} and the caret path has been
     * rendered for it, then returns the settled snapshot. Fails with the last measurement if
     * that does not happen within {@link #UPDATE_TIMEOUT_MS}.
     */
    private CaretSnapshot waitForCaret(int expectedPosition, String context) {
        long deadline = System.currentTimeMillis() + UPDATE_TIMEOUT_MS;
        CaretSnapshot snapshot = snapshotCaret();
        while (System.currentTimeMillis() < deadline) {
            if (snapshot.rendered() && snapshot.logicalPosition() == expectedPosition) {
                // ParagraphTile updates the caret path in a later FX pulse (Platform.runLater),
                // so drain the event queue once more and take the final measurement.
                waitForFxEvents();
                CaretSnapshot settled = snapshotCaret();
                if (settled.rendered() && settled.logicalPosition() == expectedPosition) {
                    return settled;
                }
                snapshot = settled;
            } else {
                waitForFxEvents();
                pause(20);
                snapshot = snapshotCaret();
            }
        }
        fail("Timed out waiting for the " + context + " (expected caret position "
                + expectedPosition + "), last measurement: " + snapshot);
        return snapshot;
    }

    private CaretSnapshot moveCaretTo(int position, String context) {
        runOnFxThread(() -> viewModel().setCaretPosition(position));
        waitForFxEvents();
        return waitForCaret(position, context);
    }

    /** Reads the caret state on the FX thread, the scene graph must not be touched elsewhere. */
    private CaretSnapshot snapshotCaret() {
        CaretSnapshot[] holder = new CaretSnapshot[1];
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            int logicalPosition = viewModel().getCaretPosition();
            Optional<Path> caretPath = activeCaretPath();
            double localX = 0, localY = 0, sceneX = 0, sceneY = 0;
            boolean rendered = caretPath.isPresent();
            if (rendered) {
                Path path = caretPath.get();
                MoveTo moveTo = (MoveTo) path.getElements().get(0);
                localX = moveTo.getX();
                localY = moveTo.getY();
                Point2D scenePoint = path.localToScene(localX, localY);
                sceneX = scenePoint.getX();
                sceneY = scenePoint.getY();
            }
            Point2D origin = richTextArea.getCaretOrigin();
            holder[0] = new CaretSnapshot(logicalPosition, rendered, localX, localY,
                    sceneX, sceneY, origin.getX(), origin.getY());
            latch.countDown();
        });
        awaitFxThread(latch, "the caret snapshot");
        return holder[0];
    }

    /** The caret path of the layer that currently holds the caret, all others are empty. */
    private Optional<Path> activeCaretPath() {
        if (scene == null) {
            return Optional.empty();
        }
        Set<Node> caretNodes = scene.getRoot().lookupAll(".caret");
        return caretNodes.stream()
                .filter(Path.class::isInstance)
                .map(Path.class::cast)
                .filter(path -> path.getScene() != null)
                .filter(path -> !path.getElements().isEmpty())
                .filter(path -> path.getElements().get(0) instanceof MoveTo)
                .findFirst();
    }

    // ==================== SCENE SETUP ====================

    private void insertTable(int rows, int columns) {
        TableDecoration tableDecoration = new TableDecoration(rows, columns);
        Action insertTableAction = richTextArea.getActionFactory().insertTable(tableDecoration);
        runOnFxThread(() -> insertTableAction.execute(new ActionEvent()));
        waitForFxEvents();
    }

    private void openDocument(String text) {
        Action openAction = richTextArea.getActionFactory().open(new Document(text));
        runOnFxThread(() -> openAction.execute(new ActionEvent()));
        waitForFxEvents();
    }

    /** Places the caret in the first cell of the table and returns the paragraph start. */
    private int moveCaretToTableStart() {
        int[] tableStart = {-1};
        runOnFxThread(() -> {
            RichTextAreaViewModel viewModel = viewModel();
            viewModel.getParagraphList().stream()
                    .filter(paragraph -> paragraph.getDecoration() instanceof ParagraphDecoration decoration
                            && decoration.hasTableDecoration())
                    .findFirst()
                    .ifPresent(paragraph -> {
                        tableStart[0] = paragraph.getStart();
                        viewModel.setCaretPosition(paragraph.getStart());
                    });
            richTextArea.requestFocus();
        });
        waitForFxEvents();
        return tableStart[0];
    }

    private RichTextAreaViewModel viewModel() {
        return ((RichTextAreaSkin) richTextArea.getSkin()).getViewModel();
    }

    // ==================== THREAD HELPERS ====================

    private void runOnFxThread(Runnable runnable) {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                runnable.run();
            } finally {
                latch.countDown();
            }
        });
        awaitFxThread(latch, "the requested action");
    }

    private void awaitFxThread(CountDownLatch latch, String context) {
        try {
            assertTrue(latch.await(3, TimeUnit.SECONDS), "The FX thread did not execute " + context);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrupted while waiting for the FX thread: " + e.getMessage());
        }
    }

    private void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrupted while waiting for the caret update: " + e.getMessage());
        }
    }
}
