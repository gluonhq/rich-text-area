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
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GLUON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.richtextarea.ui;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.model.Paragraph;
import com.gluonhq.richtextarea.model.TableDecoration;
import com.gluonhq.richtextarea.action.Action;
import com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.MoveTo;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Init;
import org.testfx.framework.junit5.Start;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

/**
 * COMPARISON TEST: Manual Mouse Click vs Automated API Caret Positioning
 * 
 * Ziel: Den ersten Unterschied im Verhalten zwischen manuellem Klick und API-basierter
 * Caret-Setzung identifizieren.
 * 
 * Wichtig: robot.write() wird NICHT verwendet, da TestFX das Fenster nicht findet.
 * Stattdessen wird insertTextAtCaret() für beide Phasen verwendet.
 * Der Unterschied liegt im Klick-Event (robot.clickOn) vs. direktem setCaretPosition.
 */
@ExtendWith(ApplicationExtension.class)
public class ManualVsAutomatedComparisonTest {

    private RichTextArea richTextArea;
    private Scene scene;
    private int screenshotCounter = 0;
    private final String OUTPUT_DIR = "target/manual-vs-automated";

    @Init
    public void init() {
        richTextArea = new RichTextArea();
        richTextArea.setPrefSize(800, 600);
    }

    @Start
    public void start(Stage stage) {
        Label instructionLabel = new Label("Manual vs Automated Comparison Test");
        instructionLabel.setStyle("-fx-font-size: 16px; -fx-padding: 10px;");
        
        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(10, instructionLabel, richTextArea);
        root.setStyle("-fx-padding: 10px; -fx-background-color: white;");
        
        scene = new Scene(root, 820, 700);
        stage.setScene(scene);
        stage.setTitle("Manual vs Automated Comparison");
        stage.show();
    }

    // ===================================================================
    // TEST 1: FULL COMPARISON - Manual Click vs Automated API
    // ===================================================================
    @Test
    @DisplayName("Full Comparison: Manual Click vs Automated API")
    public void testManualVsAutomatedFullComparison(FxRobot robot) throws Exception, InterruptedException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("FULL COMPARISON: MANUAL CLICK vs AUTOMATED API");
        System.out.println("=".repeat(80));

        new File(OUTPUT_DIR).mkdirs();

        // ==============================================================
        // PHASE A: MANUAL APPROACH (simulated mouse click)
        // ==============================================================
        System.out.println("\n" + "=".repeat(70));
        System.out.println("PHASE A: MANUAL APPROACH (robot.clickOn + insertText API)");
        System.out.println("=".repeat(70));

        System.out.println("\n[A.1] Tabelle einfügen (4x4) via ActionFactory...");
        createTableInRTA();
        waitForFxEvents();
        Thread.sleep(500);

        saveScreenshot("A01_initial_after_table");
        printFullState("A.1 - After table insert");

        System.out.println("\n[A.2] Manueller Klick auf den RichTextArea...");
        Point2D clickPoint = getClickPoint();
        System.out.println("  -> Klick-Punkt: (" + clickPoint.getX() + ", " + clickPoint.getY() + ")");
        
        long manualStart = System.currentTimeMillis();
        robot.clickOn(clickPoint.getX(), clickPoint.getY());
        waitForFxEvents();
        Thread.sleep(500);
        long manualEnd = System.currentTimeMillis();
        System.out.println("  -> Klick-Dauer: " + (manualEnd - manualStart) + "ms");
        
        saveScreenshot("A02_after_click");
        printFullState("A.2 - After manual click");
        printSceneGraph("A.2 - After manual click");

        System.out.println("\n[A.3] 'A' per insertText API einfuegen (nach manuellem Klick)...");
        insertTextAtCaret("A");
        waitForFxEvents();
        Thread.sleep(500);

        saveScreenshot("A03_after_insert_A");
        printFullState("A.3 - After inserting 'A' (after manual click)");
        printSceneGraph("A.3 - After inserting 'A' (after manual click)");

        // ==============================================================
        // PHASE B: AUTOMATED APPROACH (ViewModel API)
        // ==============================================================
        System.out.println("\n" + "=".repeat(70));
        System.out.println("PHASE B: AUTOMATED APPROACH (vm.setCaretPosition + insertText)");
        System.out.println("=".repeat(70));

        System.out.println("\n[B.1] Neues Dokument erstellen und Tabelle einfuegen...");
        clearContent();
        waitForFxEvents();
        Thread.sleep(300);
        
        createTableInRTA();
        waitForFxEvents();
        Thread.sleep(500);

        saveScreenshot("B01_initial_after_table");
        printFullState("B.1 - After table insert");

        System.out.println("\n[B.2] Caret via API auf Cell (0,2) setzen...");
        int targetPos = calculateCellPosition(0, 2);
        System.out.println("  -> Cell (0,2) ist an Dokument-Position: " + targetPos);
        
        long autoStart = System.currentTimeMillis();
        setCaretPositionDirect(targetPos);
        waitForFxEvents();
        Thread.sleep(500);
        long autoEnd = System.currentTimeMillis();
        System.out.println("  -> API-Caret-Dauer: " + (autoEnd - autoStart) + "ms");

        saveScreenshot("B02_after_setCaret");
        printFullState("B.2 - After API setCaret");
        printSceneGraph("B.2 - After API setCaret");

        System.out.println("\n[B.3] 'A' via API einfuegen...");
        insertTextAtCaret("A");
        waitForFxEvents();
        Thread.sleep(500);

        saveScreenshot("B03_after_insert_A");
        printFullState("B.3 - After inserting 'A' (API)");
        printSceneGraph("B.3 - After inserting 'A' (API)");

        // ==============================================================
        // PHASE C: COMPARISON
        // ==============================================================
        System.out.println("\n" + "=".repeat(70));
        System.out.println("PHASE C: COMPARISON RESULTS");
        System.out.println("=".repeat(70));
        
        compareStates();
    }

    // ===================================================================
    // TEST 2: DETAILED CARET SHAPE ANALYSIS
    // ===================================================================
    @Test
    @DisplayName("Caret Shape Analysis: Manual Click vs API")
    public void testCaretShapeAnalysis(FxRobot robot) throws Exception, InterruptedException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("CARET SHAPE ANALYSIS");
        System.out.println("=".repeat(80));

        new File(OUTPUT_DIR).mkdirs();

        System.out.println("\n[1] Tabelle einfuegen...");
        createTableInRTA();
        waitForFxEvents();
        Thread.sleep(500);

        // Analyse: Caret-Shape nach API setCaret
        System.out.println("\n[2] Analyse: Caret-Shape nach API setCaret auf Position 2...");
        setCaretPositionDirect(2);
        waitForFxEvents();
        Thread.sleep(500);

        System.out.println("  Caret Shape nach API setCaret(2):");
        printCaretShapeDetails();

        System.out.println("\n[3] Caret-Shape nach insertText('A') (API-Pfad)...");
        insertTextAtCaret("A");
        waitForFxEvents();
        Thread.sleep(500);

        System.out.println("  Caret Shape nach insertText 'A' (API-Pfad):");
        printCaretShapeDetails();

        // Caret-Shape nach manuellem Click
        System.out.println("\n[4] Analyse: Caret-Shape nach manuellem Klick...");
        clearContent();
        waitForFxEvents();
        Thread.sleep(300);
        
        createTableInRTA();
        waitForFxEvents();
        Thread.sleep(500);

        robot.clickOn(richTextArea);
        waitForFxEvents();
        Thread.sleep(500);

        System.out.println("  Caret Shape nach manuellem Klick:");
        printCaretShapeDetails();

        System.out.println("\n[5] Caret-Shape nach insertText('A') (manueller Pfad)...");
        insertTextAtCaret("A");
        waitForFxEvents();
        Thread.sleep(500);

        System.out.println("  Caret Shape nach insertText 'A' (manueller Pfad):");
        printCaretShapeDetails();
    }

    // ===================================================================
    // HELPER METHODS
    // ===================================================================

    private void createTableInRTA() {
        TextAlignment[][] cellAlignment = new TextAlignment[4][4];
        for (int i = 0; i < 4; i++) {
            Arrays.fill(cellAlignment[i], TextAlignment.LEFT);
        }
        
        TableDecoration tableDecoration = new TableDecoration(4, 4, cellAlignment);
        Action insertTableAction = richTextArea.getActionFactory().insertTable(tableDecoration);
        
        Platform.runLater(() -> {
            insertTableAction.execute(new ActionEvent());
        });
    }

    private void clearContent() {
        Platform.runLater(() -> {
            richTextArea.getActionFactory().newDocument().execute(new ActionEvent());
        });
    }

    private void setCaretPositionDirect(int position) {
        Platform.runLater(() -> {
            RichTextAreaSkin skin = (RichTextAreaSkin) richTextArea.getSkin();
            RichTextAreaViewModel vm = skin.getViewModel();
            System.out.println("  -> vm.setCaretPosition(" + position + ")");
            vm.setCaretPosition(position);
        });
    }

    private void insertTextAtCaret(String text) {
        Action insertAction = richTextArea.getActionFactory().insertText(text);
        Platform.runLater(() -> {
            insertAction.execute(new ActionEvent());
        });
    }

    private int calculateCellPosition(int row, int col) {
        RichTextAreaSkin skin = (RichTextAreaSkin) richTextArea.getSkin();
        RichTextAreaViewModel vm = skin.getViewModel();
        
        for (Paragraph para : vm.getParagraphList()) {
            if (para.getDecoration().hasTableDecoration()) {
                TableDecoration td = para.getDecoration().getTableDecoration();
                int cols = td.getColumns();
                int tableStart = para.getStart();
                return tableStart + (row * cols + col);
            }
        }
        return 0;
    }

    private Point2D getClickPoint() {
        Bounds rtaBounds = richTextArea.localToScene(richTextArea.getBoundsInLocal());
        if (rtaBounds != null) {
            double centerX = rtaBounds.getMinX() + rtaBounds.getWidth() / 2;
            double centerY = rtaBounds.getMinY() + rtaBounds.getHeight() / 2;
            return new Point2D(centerX, centerY);
        }
        return new Point2D(400, 300);
    }

    // ===================================================================
    // ANALYSIS METHODS
    // ===================================================================

    private void printFullState(String label) {
        System.out.println("\n--- Full State (" + label + ") ---");
        
        RichTextAreaSkin skin = (RichTextAreaSkin) richTextArea.getSkin();
        RichTextAreaViewModel vm = skin.getViewModel();
        
        int caretPos = vm.getCaretPosition();
        System.out.println("  Caret position in document: " + caretPos);
        System.out.println("  Caret row/col: " + richTextArea.getCaretRowColumn());
        System.out.println("  Text length: " + richTextArea.getTextLength());
        
        String content = vm.getTextBuffer().getText(0, Math.min(50, richTextArea.getTextLength()));
        System.out.println("  Content (first 50): '" + escapeString(content) + "'");
        
        for (Paragraph para : vm.getParagraphList()) {
            if (para.getDecoration().hasTableDecoration()) {
                TableDecoration td = para.getDecoration().getTableDecoration();
                System.out.println("  Table: " + td.getRows() + "x" + td.getColumns());
                int start = para.getStart();
                int end = para.getEnd();
                System.out.println("  Table range: [" + start + ", " + end + "]");
                String tableContent = vm.getTextBuffer().getText(start, end);
                System.out.println("  Content: '" + escapeString(tableContent) + "'");
            }
        }
        
        int layerCount = 0;
        if (richTextArea.getScene() != null) {
            layerCount = richTextArea.getScene().getRoot().lookupAll(".layer").size();
        }
        System.out.println("  Total Layer nodes: " + layerCount);
    }

    private void printCaretShapeDetails() {
        if (richTextArea.getScene() == null) {
            System.out.println("  Scene is null!");
            return;
        }
        
        Set<Node> carets = richTextArea.getScene().getRoot().lookupAll(".caret");
        System.out.println("  Number of caret shapes found: " + carets.size());
        
        for (Node caret : carets) {
            if (caret instanceof Path path && !path.getElements().isEmpty()) {
                PathElement first = path.getElements().get(0);
                if (first instanceof MoveTo moveTo) {
                    Point2D scenePos = path.localToScene(path.getLayoutBounds().getMinX(), path.getLayoutBounds().getMinY());
                    System.out.println("  Caret at scene: ("
                        + String.format("%.1f, %.1f", scenePos.getX(), scenePos.getY()) + ")");
                    System.out.println("  Caret local bounds: "
                        + String.format("(%.1f, %.1f, %.1f, %.1f)", 
                            path.getLayoutBounds().getMinX(), path.getLayoutBounds().getMinY(),
                            path.getLayoutBounds().getWidth(), path.getLayoutBounds().getHeight()));
                }
                
                System.out.println("  Parent chain:");
                Node current = caret;
                int depth = 0;
                while (current != null && depth < 8) {
                    String styleInfo = current.getStyleClass().isEmpty() ? "" : " {" + String.join(",", current.getStyleClass()) + "}";
                    System.out.println("    " + "  ".repeat(depth) + current.getClass().getSimpleName() + styleInfo);
                    current = current.getParent();
                    depth++;
                }
            }
        }
        
        Set<Node> textFlows = richTextArea.getScene().getRoot().lookupAll(".text-flow");
        for (Node tf : textFlows) {
            Point2D scenePos = tf.localToScene(tf.getLayoutBounds().getMinX(), tf.getLayoutBounds().getMinY());
            System.out.println("  TextFlow at scene: ("
                + String.format("%.1f, %.1f", scenePos.getX(), scenePos.getY()) + ")");
        }
    }

    private void printSceneGraph(String label) {
        System.out.println("\n--- Scene Graph (" + label + ") ---");
        printNode(scene.getRoot(), 0, 4);
    }

    private void printNode(Node node, int indent, int maxDepth) {
        if (maxDepth <= 0) return;
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) sb.append("  ");
        
        sb.append(node.getClass().getSimpleName());
        
        if (node instanceof Path path && !path.getElements().isEmpty()) {
            PathElement first = path.getElements().get(0);
            if (first instanceof MoveTo moveTo) {
                sb.append(" [caret at ").append(String.format("%.1f,%.1f", moveTo.getX(), moveTo.getY())).append("]");
            }
        }
        
        if (!node.getStyleClass().isEmpty()) {
            sb.append(" {").append(String.join(",", node.getStyleClass())).append("}");
        }
        
        System.out.println(sb.toString());
        
        if (node instanceof Parent parent && indent < 6) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                printNode(child, indent + 1, maxDepth - 1);
            }
        }
    }

    private void saveScreenshot(String name) throws IOException, InterruptedException {
        String filename = String.format("%s/%03d_%s.png", OUTPUT_DIR, ++screenshotCounter, name);
        
        final javafx.scene.image.WritableImage[] imageHolder = new javafx.scene.image.WritableImage[1];
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            imageHolder[0] = scene.snapshot(new javafx.scene.image.WritableImage((int)scene.getWidth(), (int)scene.getHeight()));
            latch.countDown();
        });
        latch.await();
        
        if (imageHolder[0] != null) {
            System.out.println("  [Screenshot: " + filename + "]");
        }
    }

    private void compareStates() {
        System.out.println("\n=== KEY DIFFERENCES TO CHECK ===");
        System.out.println("\n1. MOUSE EVENTS:");
        System.out.println("   Manual: Generiert MOUSE_PRESSED, MOUSE_RELEASED, MOUSE_CLICKED");
        System.out.println("   Automated: KEINE Mouse-Events - nur caret position change");
        
        System.out.println("\n2. FOCUS:");
        System.out.println("   Manual: Triggert Fokus-Events, wenn das Control nicht fokussiert war");
        System.out.println("   Automated: Nur Caret-Aenderung, kein Fokus-Wechsel");
        
        System.out.println("\n3. SCENE GRAPH (Layer-Ordnung, Caret-Parent):");
        System.out.println("   Pruefen: Ist der Caret-Shape im selben Layer nach beiden Methoden?");
        
        System.out.println("\n4. DOCUMENT POSITION:");
        System.out.println("   Pruefen: Ist die resultierende Position nach 'A' identisch?");
        
        System.out.println("\n5. TIMING:");
        System.out.println("   Manual: ~100-800ms (Event-Processing, Layout-Pass)");
        System.out.println("   Automated: ~1ms (direkte Setter)");
        
        System.out.println("\n6. FIRST VISIBLE DIFFERENCE:");
        System.out.println("   Vermutung: Nach setCaretPosition() + insertText() ist die");
        System.out.println("   Caret-Position korrekt, aber die visuelle Position des Carets");
        System.out.println("   (caretShape im Scene-Graph) koennte abweichen.");
        System.out.println("   Ursache: TextFlow.caretShape() verwendet Offset innerhalb der Zelle,");
        System.out.println("   aber ParagraphTile.updateCaretPosition() koennte andere Koordinaten");
        System.out.println("   fuer isTableCell=true verwenden.");
    }

    private String escapeString(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '\n') sb.append("\\n");
            else if (c == '\r') sb.append("\\r");
            else if (c == '\t') sb.append("\\t");
            else if (c == '\u200B') sb.append("\u200B");
            else if (c < 32) sb.append("\\x").append(Integer.toHexString(c));
            else sb.append(c);
        }
        return sb.toString();
    }
}