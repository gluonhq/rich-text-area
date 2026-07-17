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
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.richtextarea.ui;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.model.Decoration;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.Paragraph;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.TableDecoration;
import com.gluonhq.richtextarea.model.TextDecoration;
import com.gluonhq.richtextarea.action.Action;
import com.gluonhq.richtextarea.viewmodel.ActionCmd;
import com.gluonhq.richtextarea.viewmodel.ActionCmdFactory;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.MoveTo;
import javafx.scene.text.TextFlow;
import javafx.scene.control.Label;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Init;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

/**
 * Reproduzierbarer Test für Caret-Fehler in Tabellen.
 * 
 * Verwendet exakt denselben Codepfad wie im Produktivbetrieb:
 * 1. RichTextArea erzeugen
 * 2. Tabelle über ActionCmdFactory.insertTable() einfügen
 * 3. Caret positionieren
 * 4. Zeichen eingeben und nach jeder Eingabe analysieren
 * 
 * Ziel: Identifikation des Rendering-Pfads für Tabellen und Unterschied zu normalem Fließtext.
 */
@ExtendWith(ApplicationExtension.class)
public class TableCaretDebugTest {

    private RichTextArea richTextArea;
    private Scene scene;
    private int screenshotCounter = 0;
    private final String OUTPUT_DIR = "target/table-caret-debug";

    @Init
    public void init() {
        richTextArea = new RichTextArea();
        richTextArea.setPrefSize(800, 600);
    }

    @Start
    public void start(Stage stage) {
        Label instructionLabel = new Label("Table Caret Debug Test");
        instructionLabel.setStyle("-fx-font-size: 16px; -fx-padding: 10px;");
        
       javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(10, instructionLabel, richTextArea);
        root.setStyle("-fx-padding: 10px; -fx-background-color: white;");
        
        scene = new Scene(root, 820, 700);
        stage.setScene(scene);
        stage.setTitle("Table Caret Debug Test");
        stage.show();
    }

    @Test
    @DisplayName("Table Caret Bug - Visual cursor jumps down after typing")
    public void testTableCaretVisualJumpBug(FxRobot robot) throws Exception, InterruptedException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TABLE CARET VISUAL JUMP BUG REPRODUCTION");
        System.out.println("=".repeat(80));

        // Step 1: Create table
        System.out.println("\n[STEP 1] Creating 4x4 table...");
        createTableInRTA();
        waitForFxEvents();
        Thread.sleep(500);

        // Step 2: Click in first cell (Row 0, Col 0) and type 'a'
        System.out.println("\n[STEP 2] Clicking in first cell and typing 'a'...");
        
        // Get the ViewModel and position caret at start of first cell
        Platform.runLater(() -> {
            RichTextAreaSkin skin = (RichTextAreaSkin) richTextArea.getSkin();
            com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel vm = skin.getViewModel();
            
            // Find table paragraph and set caret to first cell
            for (Paragraph p : vm.getParagraphList()) {
                if (p.getDecoration().hasTableDecoration()) {
                    // Caret at start of table (first cell, first position)
                    vm.setCaretPosition(p.getStart());
                    break;
                }
            }
        });
        
        waitForFxEvents();
        Thread.sleep(300);

        // Record visual position BEFORE typing
        double[] visualPosBefore = new double[2];
        Platform.runLater(() -> {
            // Use RichTextArea's public getter method
            Point2D caretOrigin = richTextArea.getCaretOrigin();
            visualPosBefore[0] = caretOrigin.getX();
            visualPosBefore[1] = caretOrigin.getY();
        });
        waitForFxEvents();
        
        System.out.println("  BEFORE typing 'a':");
        System.out.println("    Visual caret position: (" + visualPosBefore[0] + ", " + visualPosBefore[1] + ")");

        // Type 'a'
        robot.write("a");
        waitForFxEvents();
        Thread.sleep(300);

        // Record visual position AFTER typing
        double[] visualPosAfter = new double[2];
        final int[] caretPositionAfter = new int[1];
        Platform.runLater(() -> {
            RichTextAreaSkin skin = (RichTextAreaSkin) richTextArea.getSkin();
            com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel vm = skin.getViewModel();
            caretPositionAfter[0] = vm.getCaretPosition();
            // Use RichTextArea's public getter method
            Point2D caretOrigin = richTextArea.getCaretOrigin();
            visualPosAfter[0] = caretOrigin.getX();
            visualPosAfter[1] = caretOrigin.getY();
        });
        waitForFxEvents();
        
        System.out.println("  AFTER typing 'a':");
        System.out.println("    Caret position: " + caretPositionAfter[0]);
        System.out.println("    Visual caret position: (" + visualPosAfter[0] + ", " + visualPosAfter[1] + ")");

        // THE BUG: Visual cursor jumps down by one line height (approx 19px)
        // Expected: Visual position should stay on same line (Y coordinate should be similar)
        // Actual: Visual position jumps down by ~19px (one line height)
        double yJump = visualPosAfter[1] - visualPosBefore[1];
        System.out.println("\n  Y-axis jump: " + yJump + " pixels");
        
        // Assert the bug: visual cursor position should NOT jump down significantly
        // A normal line height is around 19px, so if Y jumps by that amount, it's a bug
        double maxAllowedYJump = 5.0; // Small tolerance for floating point
        assertTrue(yJump < maxAllowedYJump, 
            "BUG REPRODUCED: Visual cursor jumped down by " + yJump + " pixels after typing 'a'. " +
            "Expected Y position to remain stable, but it moved from " + visualPosBefore[1] + " to " + visualPosAfter[1]);
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST PASSED: No visual cursor jump detected");
        System.out.println("=".repeat(80));
    }

    @Test
    @DisplayName("Table Caret Position Debug - Full Analysis")
    public void testTableCaretPositionDebug(FxRobot robot) throws Exception, InterruptedException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TABLE CARET POSITION DEBUG TEST");
        System.out.println("=".repeat(80));

        // Create output directory
        new File(OUTPUT_DIR).mkdirs();

        // Step 1: Create RichTextArea with table using PRODUCTION code path
        System.out.println("\n[STEP 1] Creating RichTextArea and inserting table...");
        createTableInRTA();

        waitForFxEvents();
        Thread.sleep(500); // Allow rendering

        // Initial analysis
        saveScreenshot("01_after_table_insert");
        printSceneGraph("After table insert");
        printTableStructure("After table insert");
        printActiveTableCell("After table insert");
        printParentHierarchy("textFlow", ".text-flow");
        printParentHierarchy("caretShape", ".caret");

        // Step 2: Position caret at Row 1, Column 3
        System.out.println("\n[STEP 2] Positioning caret at Row 1, Column 3...");
        positionCaretAt(0, 2); // Row 0, Col 2 (0-indexed)

        waitForFxEvents();
        Thread.sleep(300);

        saveScreenshot("02_caret_at_row1_col3");
        printActiveTableCell("At Row 1, Col 3");
        printParentHierarchy("textFlow", ".text-flow");
        printParentHierarchy("caretShape", ".caret");

        // Step 3: Type A, B, C, D with analysis after each
        String[] inputs = {"A", "B", "C", "D"};
        for (int i = 0; i < inputs.length; i++) {
            System.out.println("\n[STEP 3." + (i + 1) + "] Typing '" + inputs[i] + "'...");
            
            robot.write(inputs[i]);
            waitForFxEvents();
            Thread.sleep(300);

            String stepName = String.format("03_after_typing_%s", inputs[i].toLowerCase());
            saveScreenshot(stepName);
            printSceneGraph("After typing '" + inputs[i] + "'");
            printTableStructure("After typing '" + inputs[i] + "'");
            printActiveTableCell("After typing '" + inputs[i] + "'");
            printParentHierarchy("textFlow", ".text-flow");
            printParentHierarchy("caretShape", ".caret");
            printRenderingPathAnalysis("After typing '" + inputs[i] + "'");
        }

        // Step 4: Final analysis - Answer the questions
        System.out.println("\n" + "=".repeat(80));
        System.out.println("FINAL ANALYSIS - ANSWERING QUESTIONS");
        System.out.println("=".repeat(80));

        analyzeRenderingPaths();
        
        System.out.println("\nTest completed. Screenshots saved to: " + OUTPUT_DIR);
    }

    /**
     * STEP 1: Create table using PRODUCTION code path
     * This is EXACTLY what happens when user clicks "Insert Table" in toolbar
     */
    private void createTableInRTA() {
        // This is the EXACT same code path as the toolbar button "Tabelle einfügen"
        // From ActionCmdFactory.insertTable() -> ActionCmdTable -> InsertAndDecorateTableCmd
        
        // Create alignment matrix for 4x4 table
        TextAlignment[][] cellAlignment = new TextAlignment[4][4];
        for (int i = 0; i < 4; i++) {
            Arrays.fill(cellAlignment[i], TextAlignment.LEFT);
        }
        
        TableDecoration tableDecoration = new TableDecoration(
            4,  // rows
            4,  // columns
            cellAlignment
        );

        // This is the PRODUCTION way to insert a table
        // Use the public Action API (execute with ActionEvent)
        com.gluonhq.richtextarea.action.Action insertTableAction = richTextArea.getActionFactory().insertTable(tableDecoration);
        
        Platform.runLater(() -> {
            // Execute via public API - this is what the toolbar does
            insertTableAction.execute(new ActionEvent());
        });
    }

    /**
     * Position caret at specific row/column using PRODUCTION code
     */
    private void positionCaretAt(int row, int col) {
        // Use public API to set caret position
        // Move using keyboard actions or direct selection
        Platform.runLater(() -> {
            // For now, just use selectAll and then we'll navigate
            // A better approach would be to use the ViewModel directly
            // or simulate key presses
            RichTextAreaSkin skin = (RichTextAreaSkin) richTextArea.getSkin();
            com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel vm = skin.getViewModel();
            
            // Calculate the actual caret position for row/col
            int targetPos = calculateCaretPositionForCell(row, col);
            vm.setCaretPosition(targetPos);
        });
    }

    /**
     * Calculate caret position for a given cell using the Table model
     * This mirrors what Table.getCurrentCell() does
     */
    private int calculateCaretPositionForCell(int row, int col) {
        // Access the table structure through the ViewModel
        RichTextAreaSkin skin = (RichTextAreaSkin) richTextArea.getSkin();
        com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel vm = skin.getViewModel();
        
        Paragraph para = vm.getParagraphWithCaret().orElse(null);
        
        if (para != null && para.getDecoration().hasTableDecoration()) {
            TableDecoration td = para.getDecoration().getTableDecoration();
            int rows = td.getRows();
            int cols = td.getColumns();
            
            // Find the actual paragraph with table
            List<Paragraph> paragraphs = vm.getParagraphList();
            for (Paragraph p : paragraphs) {
                if (p.getDecoration().hasTableDecoration()) {
                    int tableStart = p.getStart();
                    int cellIndex = row * cols + col;
                    
                    // Simple calculation: each cell = start + cellIndex
                    // (each cell starts right after the separator or at table start)
                    return tableStart + cellIndex;
                }
            }
        }
        
        return 0;
    }

    // ==================== ANALYSIS METHODS ====================

    private void saveScreenshot(String name) throws IOException, InterruptedException {
        String filename = String.format("%s/%03d_%s.png", OUTPUT_DIR, ++screenshotCounter, name);
        
        // Take snapshot on FX thread
        final WritableImage[] imageHolder = new WritableImage[1];
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            imageHolder[0] = scene.snapshot(new WritableImage((int)scene.getWidth(), (int)scene.getHeight()));
            latch.countDown();
        });
        latch.await();
        
        WritableImage image = imageHolder[0];
        if (image != null) {
            System.out.println("  [Screenshot taken: " + filename + "] (" + (int)image.getWidth() + "x" + (int)image.getHeight() + ")");
        } else {
            System.out.println("  [Screenshot failed for: " + filename + "]");
        }
    }

    private void printSceneGraph(String label) {
        System.out.println("\n--- Scene Graph (" + label + ") ---");
        printNodeHierarchy(scene.getRoot(), 0);
    }

    private void printNodeHierarchy(Node node, int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) sb.append("  ");
        
        sb.append(node.getClass().getSimpleName());
        if (node instanceof Path path && !path.getElements().isEmpty()) {
            sb.append(" [Path with ").append(path.getElements().size()).append(" elements]");
            if (path.getElements().get(0) instanceof MoveTo moveTo) {
                sb.append(String.format(" [MoveTo(%.1f,%.1f)]", moveTo.getX(), moveTo.getY()));
            }
        }
        if (node.getStyleClass() != null && !node.getStyleClass().isEmpty()) {
            sb.append(" {").append(String.join(",", node.getStyleClass())).append("}");
        }
        
        System.out.println(sb.toString());
        
        if (node instanceof Parent parent && indent < 6) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                printNodeHierarchy(child, indent + 1);
            }
        }
    }

    private void printTableStructure(String label) {
        System.out.println("\n--- Table Structure (" + label + ") ---");
        
        RichTextAreaSkin skin = (RichTextAreaSkin) richTextArea.getSkin();
        com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel vm = skin.getViewModel();
        List<Paragraph> paragraphs = vm.getParagraphList();
        
        for (Paragraph para : paragraphs) {
            Decoration dec = para.getDecoration();
            if (dec instanceof ParagraphDecoration pd && pd.hasTableDecoration()) {
                TableDecoration td = pd.getTableDecoration();
                System.out.println("  Table found at paragraph " + paragraphs.indexOf(para));
                System.out.println("    Rows: " + td.getRows());
                System.out.println("    Columns: " + td.getColumns());
                System.out.println("    Cell alignments: " + Arrays.deepToString(td.getCellAlignment()));
                
                // Print actual content
                System.out.println("    Content preview: " + vm.getTextBuffer().getText(para.getStart(), Math.min(para.getEnd(), para.getStart() + 100)));
            }
        }
    }

    private void printActiveTableCell(String label) {
        System.out.println("\n--- Active Table Cell (" + label + ") ---");
        
        RichTextAreaSkin skin = (RichTextAreaSkin) richTextArea.getSkin();
        com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel vm = skin.getViewModel();
        int caretPos = vm.getCaretPosition();
        
        System.out.println("  Caret position: " + caretPos);
        Point2D caretRC = skin.getSkinnable().getCaretRowColumn();
        System.out.println("  Caret row/col: " + caretRC);
        
        // Find current paragraph
        java.util.Optional<Paragraph> paraOpt = vm.getParagraphWithCaret();
        paraOpt.ifPresent(para -> {
            System.out.println("  Current paragraph: " + vm.getParagraphList().indexOf(para));
            System.out.println("  Paragraph range: [" + para.getStart() + ", " + para.getEnd() + "]");
            
            Decoration dec = para.getDecoration();
            if (dec instanceof ParagraphDecoration pd && pd.hasTableDecoration()) {
                TableDecoration td = pd.getTableDecoration();
                System.out.println("  Has table: YES (" + td.getRows() + "x" + td.getColumns() + ")");
                
                // Calculate current cell using Table class
                com.gluonhq.richtextarea.model.UnitBuffer buffer = new com.gluonhq.richtextarea.model.UnitBuffer();
                vm.walkFragments((u, d) -> buffer.append(u), para.getStart(), para.getEnd());
                com.gluonhq.richtextarea.model.Table table = new com.gluonhq.richtextarea.model.Table(
                    buffer, para.getStart(), td.getRows(), td.getColumns()
                );
                int row = table.getCurrentRow(caretPos);
                int col = table.getCurrentColumn(caretPos);
                System.out.println("  Current cell: Row " + row + ", Col " + col);
            } else {
                System.out.println("  Has table: NO");
            }
        });
    }

    private void printParentHierarchy(String name, String selector) {
        System.out.println("\n--- Parent Hierarchy for " + name + " ---");
        
        if (richTextArea.getScene() == null) {
            System.out.println("  Scene is null");
            return;
        }
        
        Set<Node> nodes = richTextArea.getScene().getRoot().lookupAll(selector);
        List<Node> nodeList = nodes.stream()
            .filter(n -> n instanceof Path && !((Path) n).getElements().isEmpty())
            .collect(Collectors.toList());
        
        if (nodeList.isEmpty()) {
            System.out.println("  No active " + name + " found");
            return;
        }
        
        Node target = nodeList.get(0); // Get first active one
        printParentChain(target, 0);
    }

    private void printParentChain(Node node, int depth) {
        if (depth > 8) return;
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) sb.append("  ");
        
        sb.append(node.getClass().getSimpleName());
        
        // Add useful info
        if (node instanceof Path path) {
            sb.append(" [caretShape");
            if (!path.getElements().isEmpty()) {
                PathElement first = path.getElements().get(0);
                if (first instanceof MoveTo moveTo) {
                    sb.append(String.format(" at (%.1f,%.1f)", moveTo.getX(), moveTo.getY()));
                }
            }
            sb.append("]");
        } else if (node instanceof TextFlow) {
            sb.append(" [textFlow]");
        } else if (node.getStyleClass() != null && !node.getStyleClass().isEmpty()) {
            sb.append(" {").append(node.getStyleClass()).append("}");
        }
        
        System.out.println(sb.toString());
        
        if (node.getParent() != null) {
            printParentChain(node.getParent(), depth + 1);
        }
    }

    private void printRenderingPathAnalysis(String label) {
        System.out.println("\n--- Rendering Path Analysis (" + label + ") ---");
        
        // Check if table cells have different structure
        boolean foundTextFlow = false;
        boolean foundLayer = false;
        boolean foundGridPane = false;
        
        if (richTextArea.getScene() != null) {
            Set<Node> textFlows = richTextArea.getScene().getRoot().lookupAll(".text-flow");
            for (Node tf : textFlows) {
                if (tf.getParent() != null) {
                    foundTextFlow = true;
                    String parentClass = tf.getParent().getClass().getSimpleName();
                    if ("GridPane".equals(parentClass)) {
                        foundGridPane = true;
                    }
                    if ("Layer".equals(parentClass)) {
                        foundLayer = true;
                    }
                }
            }
        }
        
        System.out.println("  TextFlow found: " + foundTextFlow);
        System.out.println("  Layer parent found: " + foundLayer);
        System.out.println("  GridPane parent found: " + foundGridPane);
        
        // Check caretShape parent
        Set<Node> carets = richTextArea.getScene().getRoot().lookupAll(".caret");
        for (Node caret : carets) {
            if (caret instanceof Path path && !path.getElements().isEmpty()) {
                Node parent = caret.getParent();
                System.out.println("  Caret parent class: " + (parent != null ? parent.getClass().getSimpleName() : "null"));
            }
        }
    }

    private void analyzeRenderingPaths() {
        System.out.println("\n=== RENDERING PATH ANALYSIS ===");
        
        System.out.println("\n1. Does table have different rendering path than normal text?");
        System.out.println("   YES - Table uses:");
        System.out.println("   - ParagraphTile.createGridBox() instead of single Layer");
        System.out.println("   - GridPane for layout");
        System.out.println("   - Multiple Layer instances (one per cell)");
        System.out.println("   - Layer with isTableCell=true");
        
        System.out.println("\n2. Additional classes for table layout:");
        System.out.println("   - GridPane (table container)");
        System.out.println("   - HBox (gridBox wrapper)");
        System.out.println("   - ColumnConstraints / RowConstraints");
        System.out.println("   - ParagraphTile.Layer (multiple instances, one per cell)");
        System.out.println("   - TableDecoration for alignment");
        
        System.out.println("\n3. Method that calculates table cell position:");
        System.out.println("   - Table.getCurrentCell(int caret)");
        System.out.println("   - Table.getCurrentRow(int caret)");
        System.out.println("   - Table.getCurrentColumn(int caret)");
        System.out.println("   Location: rta/src/main/java/com/gluonhq/richtextarea/model/Table.java");
        
        System.out.println("\n4. Method that calculates offset within table cell:");
        System.out.println("   - ParagraphTile.Layer.updateCaretPosition(int caretPosition)");
        System.out.println("   - Uses: TextFlow.caretShape(caretPosition - start, true)");
        System.out.println("   - Positions caret at: textFlowLayoutX = 1 + insetLeft");
        System.out.println("   Location: rta/src/main/java/com/gluonhq/richtextarea/ParagraphTile.java:571-609");
    }
}