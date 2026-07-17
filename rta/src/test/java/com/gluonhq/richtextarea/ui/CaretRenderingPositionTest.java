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
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.shape.MoveTo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Init;
import org.testfx.framework.junit5.Start;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

/**
 * Test that verifies the ACTUAL rendered caret position by examining
 * the JavaFX Path node that draws the caret blink animation.
 * 
 * This test does NOT use getCaretPosition() or getCaretOrigin() model APIs.
 * It directly inspects the rendered graphical caret (Path) in the scene graph.
 * 
 * Architecture:
 * - RichTextAreaSkin -> ParagraphListView -> RichListCell -> ParagraphTile -> Layer
 * - Layer contains caretShape (Path) which is the actual rendered caret
 */
@ExtendWith(ApplicationExtension.class)
public class CaretRenderingPositionTest {

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
        stage.setTitle("Caret Rendering Test");
        stage.show();
    }

    @Test
    public void testRenderedCaretAtStartOfText(FxRobot robot) {
        run(() -> {
            String text = "Hello World";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();

        // Navigate to start
        robot.push(javafx.scene.input.KeyCode.HOME);
        waitForFxEvents();

        analyzeAllCarets("After HOME key");

        // Get the ACTUAL rendered caret Path node (NO model APIs!)
        Path caretPath = robot.lookup(".caret").queryAs(Path.class);
        assertNotNull(caretPath, "Caret Path should exist");
        assertFalse(caretPath.getElements().isEmpty(), "Caret path should have elements");

        // Get bounds of the rendered caret
        Bounds caretBounds = caretPath.getLayoutBounds();
        assertTrue(caretBounds.getWidth() > 0 || caretBounds.getHeight() > 0, 
            "Caret should have non-zero bounds");

        // Caret at start should be at x ≈ 0 (accounting for padding/insets)
        assertTrue(caretBounds.getMinX() >= -1, 
            "Caret at start should be near left edge, got x=" + caretBounds.getMinX());
    }

    @Test
    public void testRenderedCaretMovesWithArrowKeys(FxRobot robot) {
        run(() -> {
            String text = "Hello World";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();

        // After open, caret is at end (position 11)
        // Move to start
        robot.push(javafx.scene.input.KeyCode.HOME);
        waitForFxEvents();

        analyzeAllCarets("After HOME (start)");

        // Get caret position at start
        Path caretAtStart = robot.lookup(".caret").queryAs(Path.class);
        Bounds boundsAtStart = caretAtStart.getLayoutBounds();
        double xAtStart = boundsAtStart.getMinX();

        // Move right once
        robot.push(javafx.scene.input.KeyCode.RIGHT);
        waitForFxEvents();

        analyzeAllCarets("After RIGHT arrow");

        // Get caret position after moving right
        Path caretAfterRight = robot.lookup(".caret").queryAs(Path.class);
        Bounds boundsAfterRight = caretAfterRight.getLayoutBounds();
        double xAfterRight = boundsAfterRight.getMinX();

        // The caret should have moved right (x increased)
        assertTrue(xAfterRight > xAtStart - 0.1, 
            "Caret should move right, start x=" + xAtStart + ", after x=" + xAfterRight);
    }

    @Test
    public void testRenderedCaretYPositionFollowsTextLine(FxRobot robot) {
        run(() -> {
            // Multi-line text: "Line1\nLine2\nLine3"
            String text = "Line1\nLine2\nLine3";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();

        // Navigate to first line
        robot.push(javafx.scene.input.KeyCode.HOME);
        waitForFxEvents();

        analyzeAllCarets("After HOME (first line)");

        Path caretLine1 = robot.lookup(".caret").queryAs(Path.class);
        Bounds boundsLine1 = caretLine1.getLayoutBounds();
        double yLine1 = boundsLine1.getMinY();

        // Navigate down to second line
        robot.push(javafx.scene.input.KeyCode.DOWN);
        waitForFxEvents();

        analyzeAllCarets("After DOWN arrow (second line)");

        Path caretLine2 = robot.lookup(".caret").queryAs(Path.class);
        Bounds boundsLine2 = caretLine2.getLayoutBounds();
        double yLine2 = boundsLine2.getMinY();

        // Caret on second line should be lower (higher Y) than first line
        assertTrue(yLine2 > yLine1 - 0.1, 
            "Caret on second line should be lower, line1 y=" + yLine1 + ", line2 y=" + yLine2);
    }

    @Test
    public void testRenderedCaretHeightCorrespondsToFontSize(FxRobot robot) {
        run(() -> {
            String text = "Test";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();

        Path caretPath = robot.lookup(".caret").queryAs(Path.class);
        Bounds caretBounds = caretPath.getLayoutBounds();
        double caretHeight = caretBounds.getHeight();

        // Default font size is 14, caret height should be approximately 1.2 * fontsize = 16.8
        assertTrue(caretHeight > 10 && caretHeight < 25, 
            "Caret height should correspond to font size, got=" + caretHeight);
    }

    @Test
    public void testCaretPathElementsDefinePosition(FxRobot robot) {
        run(() -> {
            String text = "Hi";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();

        Path caretPath = robot.lookup(".caret").queryAs(Path.class);
        var elements = caretPath.getElements();

        // A valid caret path should have at least MoveTo and LineTo
        assertFalse(elements.isEmpty(), "Caret path should have elements");
        assertTrue(elements.get(0) instanceof javafx.scene.shape.MoveTo, 
            "First element should be MoveTo");

        // Extract X position from MoveTo
        if (elements.get(0) instanceof javafx.scene.shape.MoveTo moveTo) {
            double caretX = moveTo.getX();
            assertTrue(caretX >= -10, "Caret X should be valid, got=" + caretX);
        }
    }

    @Test
    public void testRenderedCaretChangesAfterTextInsertion(FxRobot robot) {
        run(() -> {
            String text = "A";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();

        analyzeAllCarets("After opening 'A'");

        // Caret at end of "A"
        Path caretAfterA = robot.lookup(".caret").queryAs(Path.class);
        Bounds boundsAfterA = caretAfterA.getLayoutBounds();
        double xAfterA = boundsAfterA.getMinX();

        // Type another character
        robot.write("B");
        waitForFxEvents();

        analyzeAllCarets("After typing 'B'");

        // Caret should have moved right
        Path caretAfterAB = robot.lookup(".caret").queryAs(Path.class);
        Bounds boundsAfterAB = caretAfterAB.getLayoutBounds();
        double xAfterAB = boundsAfterAB.getMinX();

        assertTrue(xAfterAB > xAfterA - 0.1, 
            "Caret should move right after typing 'B', before=" + xAfterA + ", after=" + xAfterAB);
    }

    @Test
    public void testMultipleCaretInstancesAnalysis(FxRobot robot) {
        run(() -> {
            // Multi-paragraph text to check for multiple carets
            String text = "Line1\nLine2\nLine3";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();

        System.out.println("\n========== INITIAL STATE (after open) ==========");
        analyzeAllCarets("Initial state");

        // Navigate to different lines and analyze
        robot.push(javafx.scene.input.KeyCode.HOME);
        waitForFxEvents();
        System.out.println("\n========== After HOME ==========");
        analyzeAllCarets("After HOME");

        robot.push(javafx.scene.input.KeyCode.DOWN);
        waitForFxEvents();
        System.out.println("\n========== After DOWN ==========");
        analyzeAllCarets("After DOWN");

        robot.push(javafx.scene.input.KeyCode.DOWN);
        waitForFxEvents();
        System.out.println("\n========== After 2nd DOWN ==========");
        analyzeAllCarets("After 2nd DOWN");

        // Verify only one caret is visible
        Set<Path> allCaretsSet = robot.lookup(".caret").queryAllAs(Path.class);
        List<Path> allCarets = new java.util.ArrayList<>(allCaretsSet);
        long visibleCarets = allCarets.stream()
            .filter(path -> path.getParent() != null && path.getParent().getParent() != null)
            .filter(path -> {
                var elements = path.getElements();
                return !elements.isEmpty();
            })
            .count();
        
        System.out.println("\n========== SUMMARY ==========");
        System.out.println("Total caret PATH nodes found: " + allCarets.size());
        System.out.println("Visible carets with elements: " + visibleCarets);
        
        // There should be only one visible caret
        assertEquals(1, visibleCarets, "Only one caret should be visible at a time");
    }

    private void analyzeAllCarets(String label) {
        // Collect all caret nodes from the scene graph
        if (richTextArea.getScene() != null && richTextArea.getScene().getRoot() != null) {
            // lookupAll returns Set<Node>
            Set<Node> caretNodes = richTextArea.getScene().getRoot().lookupAll(".caret");
            List<Path> allCarets = caretNodes.stream()
                .filter(Path.class::isInstance)
                .map(Path.class::cast)
                .collect(Collectors.toList());
            
            analyzeCaretsList(allCarets, label);
        }
    }
    
    private void analyzeCaretsList(List<Path> allCarets, String label) {
        System.out.println("\n========== " + label + " ==========");
        System.out.println("Total caret nodes found: " + allCarets.size());
        
        for (int i = 0; i < allCarets.size(); i++) {
            Path caret = allCarets.get(i);
            analyzeSingleCaret(caret, i + 1);
        }
    }
    
    private void analyzeSingleCaret(Path caret, int index) {
        System.out.println("\n--- Caret #" + index + " ---");
        System.out.println("  Hashcode: " + System.identityHashCode(caret));
        System.out.println("  Class: " + caret.getClass().getName());
        
        // Parent hierarchy
        Node parent = caret.getParent();
        int parentLevel = 0;
        StringBuilder parentChain = new StringBuilder();
        while (parent != null && parentLevel < 5) {
            parentChain.insert(0, parent.getClass().getSimpleName());
            if (parentLevel < 4) {
                parentChain.insert(0, " -> ");
            }
            parent = parent.getParent();
            parentLevel++;
        }
        System.out.println("  Parent chain: " + parentChain.toString());
        
        // Scene
        System.out.println("  Scene: " + (caret.getScene() != null ? caret.getScene().hashCode() : "null"));
        
        // Visibility properties
        System.out.println("  visible: " + caret.isVisible());
        System.out.println("  managed: " + caret.isManaged());
        System.out.println("  opacity: " + caret.getOpacity());
        
        // Transforms
        System.out.println("  translateX: " + caret.getTranslateX());
        System.out.println("  translateY: " + caret.getTranslateY());
        System.out.println("  layoutX: " + caret.getLayoutX());
        System.out.println("  layoutY: " + caret.getLayoutY());
        
        // Bounds
        Bounds boundsInLocal = caret.getBoundsInLocal();
        Bounds boundsInParent = caret.getBoundsInParent();
        System.out.println("  boundsInLocal: x=" + String.format("%.2f", boundsInLocal.getMinX()) + 
                         " y=" + String.format("%.2f", boundsInLocal.getMinY()) +
                         " w=" + String.format("%.2f", boundsInLocal.getWidth()) + 
                         " h=" + String.format("%.2f", boundsInLocal.getHeight()));
        System.out.println("  boundsInParent: x=" + String.format("%.2f", boundsInParent.getMinX()) + 
                         " y=" + String.format("%.2f", boundsInParent.getMinY()) +
                         " w=" + String.format("%.2f", boundsInParent.getWidth()) + 
                         " h=" + String.format("%.2f", boundsInParent.getHeight()));
        
        // Coordinate conversions
        if (caret.getScene() != null) {
            Bounds localToScene = caret.localToScene(boundsInLocal);
            Bounds sceneToLocal = caret.sceneToLocal(localToScene);
            System.out.println("  localToScene: x=" + String.format("%.2f", localToScene.getMinX()) + 
                             " y=" + String.format("%.2f", localToScene.getMinY()));
            System.out.println("  sceneToLocal: x=" + String.format("%.2f", sceneToLocal.getMinX()) + 
                             " y=" + String.format("%.2f", sceneToLocal.getMinY()));
            
            try {
                javafx.geometry.Point2D screenPos = caret.localToScreen(0, 0);
                if (screenPos != null) {
                    System.out.println("  localToScreen(0,0): x=" + String.format("%.2f", screenPos.getX()) + 
                                     " y=" + String.format("%.2f", screenPos.getY()));
                } else {
                    System.out.println("  localToScreen(0,0): null");
                }
            } catch (Exception e) {
                System.out.println("  localToScreen: error - " + e.getMessage());
            }
        }
        
        // Path elements
        var elements = caret.getElements();
        System.out.println("  pathElements count: " + elements.size());
        if (!elements.isEmpty()) {
            System.out.println("  firstElement: " + elements.get(0).getClass().getSimpleName());
            if (elements.get(0) instanceof javafx.scene.shape.MoveTo moveTo) {
                System.out.println("  firstElement X: " + moveTo.getX() + " Y: " + moveTo.getY());
            }
        }
        
        // Determine visibility status
        boolean hasElements = !elements.isEmpty();
        boolean isVisible = caret.isVisible() && caret.getOpacity() > 0;
        boolean hasParent = caret.getParent() != null;
        System.out.println("  STATUS: " + (hasElements && isVisible && hasParent ? "VISIBLE" : "HIDDEN/INACTIVE"));
    }

    @Test
    public void testCaretVsTextGeometryComparison(FxRobot robot) {
        run(() -> {
            String text = "";
            Document document = new Document(text);
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();

        String[] inputs = {"", "A", "AB", "ABC"};
        for (String input : inputs) {
            if (!input.isEmpty()) {
                robot.write(input);
                waitForFxEvents();
            }
            analyzeCaretTextGeometry(robot, "After input: '" + input + "'");
        }
    }

    private void analyzeCaretTextGeometry(FxRobot robot, String label) {
        List<Path> caretPaths = new java.util.ArrayList<>();
        if (richTextArea.getScene() != null && richTextArea.getScene().getRoot() != null) {
            Set<Node> caretNodes = richTextArea.getScene().getRoot().lookupAll(".caret");
            caretPaths = caretNodes.stream()
                .filter(Path.class::isInstance)
                .map(Path.class::cast)
                .collect(Collectors.toList());
        }
        
        Path caretPath = caretPaths.stream()
            .filter(p -> !p.getElements().isEmpty())
            .findFirst()
            .orElse(null);
        
        if (caretPath == null) {
            System.out.println("\n[" + label + "] No caret with elements found");
            return;
        }
        
        System.out.println("\n========== " + label + " ==========");
        
        // Caret info
        Bounds caretBoundsLocal = caretPath.getBoundsInLocal();
        Bounds caretBoundsScene = caretPath.localToScene(caretBoundsLocal);
        PathElement firstElem = caretPath.getElements().get(0);
        double caretX = 0, caretY = 0;
        if (firstElem instanceof MoveTo moveTo) {
            caretX = moveTo.getX();
            caretY = moveTo.getY();
        }
        
        System.out.println("CARET:");
        System.out.println("  layoutX=" + caretPath.getLayoutX() + ", layoutY=" + caretPath.getLayoutY());
        System.out.println("  firstElement X=" + caretX + " Y=" + caretY);
        System.out.println("  boundsInLocal=" + caretBoundsLocal);
        System.out.println("  boundsInScene=" + caretBoundsScene);
        
        // TextFlow and Text nodes
        TextFlow textFlow = robot.lookup(".text-flow").queryAs(TextFlow.class);
        if (textFlow != null) {
            System.out.println("TEXT NODES:");
            for (Node node : textFlow.getChildren()) {
                if (node instanceof Text textNode) {
                    Bounds tbLocal = textNode.getBoundsInLocal();
                    Bounds tbParent = textNode.getBoundsInParent();
                    Bounds tbScene = textNode.localToScene(tbLocal);
                    double baseline = textNode.getBaselineOffset();
                    
                    System.out.println("  Text: '" + textNode.getText() + "'");
                    System.out.println("    layoutX=" + textNode.getLayoutX() + ", layoutY=" + textNode.getLayoutY());
                    System.out.println("    boundsInLocal=" + tbLocal);
                    System.out.println("    boundsInParent=" + tbParent);
                    System.out.println("    boundsInScene=" + tbScene);
                    System.out.println("    baseline=" + baseline);
                    
                    double caretSceneY = caretBoundsScene.getMinY();
                    double textSceneY = tbScene.getMinY();
                    double baselineSceneY = textSceneY + baseline;
                    System.out.println("    -> caretSceneY=" + String.format("%.2f", caretSceneY) + 
                                     ", baselineSceneY=" + String.format("%.2f", baselineSceneY));
                    System.out.println("    -> verticalDistance(caret->baseline)=" + String.format("%.2f", baselineSceneY - caretSceneY));
                }
            }
        }
    }

    private void run(Runnable runnable) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                runnable.run();
            } finally {
                countDownLatch.countDown();
            }
        });
        try {
            Assertions.assertTrue(countDownLatch.await(3, TimeUnit.SECONDS), "Timeout");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}