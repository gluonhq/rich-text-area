package com.gluonhq.richtextarea.ui;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.Selection;
import com.gluonhq.richtextarea.action.TextDecorateAction;
import com.gluonhq.richtextarea.model.DecorationModel;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.TextDecoration;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Init;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.base.NodeMatchers;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static javafx.scene.input.KeyCode.A;
import static javafx.scene.input.KeyCombination.SHORTCUT_DOWN;
import static javafx.scene.text.FontPosture.ITALIC;
import static javafx.scene.text.FontPosture.REGULAR;
import static javafx.scene.text.FontWeight.BOLD;
import static javafx.scene.text.FontWeight.NORMAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@ExtendWith(ApplicationExtension.class)
public class RTATest {

    private static boolean fxStarted;
    private BorderPane root;
    private RichTextArea richTextArea;

    @BeforeEach
    public void setup() {
        if (!fxStarted) {
            try {
                Platform.startup(() -> fxStarted = true);
            } catch (IllegalStateException e) {
                // Platform already initialized
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
        stage.setTitle("RichTextArea");
        stage.show();
    }

    @Test
    public void basicTest(FxRobot robot) {
        verifyThat(".rich-text-area", node -> node instanceof RichTextArea);
        verifyThat(".rich-text-area", NodeMatchers.isFocused());
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        assertEquals(0, rta.getTextLength());
        assertEquals(0, rta.getCaretPosition());
        assertNotNull(rta.getDocument());
        assertEquals(0, rta.getDocument().getCaretPosition());
        assertEquals("", rta.getDocument().getText());
    }

    @Test
    public void basicPromptDemoTest(FxRobot robot) {
        run(() -> richTextArea.setPromptText("Type something!"));
        waitForFxEvents();
        verifyThat(".rich-text-area", node -> node instanceof RichTextArea);
        verifyThat(".rich-text-area", NodeMatchers.isFocused());
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        assertEquals(0, rta.getTextLength());
        assertEquals(0, rta.getCaretPosition());
        assertNotNull(rta.getDocument());
        assertEquals(0, rta.getDocument().getCaretPosition());
        assertEquals("", rta.getDocument().getText());
        verifyThat(".prompt", node -> node instanceof Text &&
                "Type something!".equals(((Text) node).getText()));
    }

    @Test
    public void basicDocumentDemoTest(FxRobot robot) {
        run(() -> {
            String text = "Hello RTA";
            TextDecoration textDecoration = TextDecoration.builder().presets()
                    .fontFamily("Arial")
                    .fontSize(20)
                    .foreground("red")
                    .build();
            ParagraphDecoration paragraphDecoration = ParagraphDecoration.builder().presets().build();
            DecorationModel decorationModel = new DecorationModel(0, text.length(), textDecoration, paragraphDecoration);
            Document document = new Document(text, List.of(decorationModel), text.length());
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        });
        waitForFxEvents();

        verifyThat(".rich-text-area", node -> node instanceof RichTextArea);
        verifyThat(".rich-text-area", NodeMatchers.isFocused());
        RichTextArea rta = robot.lookup(".rich-text-area").query();
        assertEquals(9, rta.getTextLength());
        assertEquals(9, rta.getCaretPosition());
        assertNotNull(rta.getDocument());
        assertEquals(9, rta.getDocument().getCaretPosition());
        assertEquals("Hello RTA", rta.getDocument().getText());
        assertNotNull(rta.getDocument().getDecorations());
        assertEquals(1, rta.getDocument().getDecorations().size());
        assertInstanceOf(TextDecoration.class, rta.getDocument().getDecorations().get(0).getDecoration());
        TextDecoration td = (TextDecoration) rta.getDocument().getDecorations().get(0).getDecoration();
        assertEquals("red", td.getForeground());
        assertEquals("transparent", td.getBackground());
        assertEquals("Arial", td.getFontFamily());
        assertEquals(20, td.getFontSize());
        assertEquals(NORMAL, td.getFontWeight());
        assertEquals(REGULAR, td.getFontPosture());
    }

    @Test
    public void actionsDemoTest(FxRobot robot) {
        run(() -> {
            String text = "Document is the basic model that contains all the information required";
            TextDecoration textDecoration = TextDecoration.builder().presets()
                    .fontFamily("Arial")
                    .fontSize(14)
                    .build();
            ParagraphDecoration paragraphDecoration = ParagraphDecoration.builder().presets().build();
            DecorationModel decorationModel = new DecorationModel(0, text.length(), textDecoration, paragraphDecoration);
            Document document = new Document(text, List.of(decorationModel), text.length());
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
            richTextArea.setAutoSave(true);

            ToggleButton fontBoldToggle = new ToggleButton("Bold");
            fontBoldToggle.getStyleClass().add("bold-toggle-button");
            new TextDecorateAction<>(richTextArea, fontBoldToggle.selectedProperty().asObject(),
                    d -> d.getFontWeight() == BOLD,
                    (builder, a) -> builder.fontWeight(a ? BOLD : NORMAL).build());
            ToggleButton fontItalicToggle = new ToggleButton("Italic");
            fontItalicToggle.getStyleClass().add("italic-toggle-button");
            new TextDecorateAction<>(richTextArea, fontItalicToggle.selectedProperty().asObject(),
                    d -> d.getFontPosture() == ITALIC,
                    (builder, a) -> builder.fontPosture(a ? ITALIC : REGULAR).build());
            ToggleButton fontUnderlinedToggle = new ToggleButton("Underline");
            fontUnderlinedToggle.getStyleClass().add("underline-toggle-button");
            new TextDecorateAction<>(richTextArea, fontUnderlinedToggle.selectedProperty().asObject(),
                    TextDecoration::isUnderline, (builder, a) -> builder.underline(a).build());
            HBox actionsBox = new HBox(fontBoldToggle, fontItalicToggle, fontUnderlinedToggle);
            root.setTop(actionsBox);
        });
        waitForFxEvents();
        RichTextArea rta = robot.lookup(".rich-text-area").query();

        robot.push(new KeyCodeCombination(A, SHORTCUT_DOWN));
        waitForFxEvents();
        Selection selection = rta.getSelection();
        assertNotNull(selection);
        assertEquals(0, selection.getStart());
        assertEquals(70, selection.getEnd());


        assertNotNull(rta.getDocument().getDecorations());
        assertEquals(1, rta.getDocument().getDecorations().size());
        assertInstanceOf(TextDecoration.class, rta.getDocument().getDecorations().get(0).getDecoration());
        TextDecoration td = (TextDecoration) rta.getDocument().getDecorations().get(0).getDecoration();
        assertEquals("Arial", td.getFontFamily());
        assertEquals(14, td.getFontSize());
        assertEquals(NORMAL, td.getFontWeight());
        assertEquals(REGULAR, td.getFontPosture());

        robot.clickOn(".bold-toggle-button");
        waitForFxEvents();
        td = (TextDecoration) rta.getDocument().getDecorations().get(0).getDecoration();
        assertEquals(BOLD, td.getFontWeight());
        assertEquals(REGULAR, td.getFontPosture());
        assertEquals(false, td.isUnderline());

        robot.clickOn(".italic-toggle-button");
        waitForFxEvents();
        td = (TextDecoration) rta.getDocument().getDecorations().get(0).getDecoration();
        assertEquals(BOLD, td.getFontWeight());
        assertEquals(ITALIC, td.getFontPosture());
        assertEquals(false, td.isUnderline());

        robot.clickOn(".underline-toggle-button");
        waitForFxEvents();
        td = (TextDecoration) rta.getDocument().getDecorations().get(0).getDecoration();
        assertEquals(BOLD, td.getFontWeight());
        assertEquals(ITALIC, td.getFontPosture());
        assertEquals(true, td.isUnderline());

        robot.clickOn(".bold-toggle-button")
                .clickOn(".italic-toggle-button")
                .clickOn(".underline-toggle-button");
        waitForFxEvents();
        td = (TextDecoration) rta.getDocument().getDecorations().get(0).getDecoration();
        assertEquals(NORMAL, td.getFontWeight());
        assertEquals(REGULAR, td.getFontPosture());
        assertEquals(false, td.isUnderline());

        run(() -> {
            // select some text and change its decoration;
            richTextArea.getActionFactory().selectAndDecorate(new Selection(12, 27),
                    TextDecoration.builder().presets().fontFamily("Arial")
                            .fontWeight(BOLD).underline(true)
                            .build()).execute(new ActionEvent());
        });
        waitForFxEvents();
        selection = rta.getSelection();
        assertNotNull(selection);
        assertEquals(Selection.UNDEFINED, selection);
        assertEquals(3, rta.getDocument().getDecorations().size());
        td = (TextDecoration) rta.getDocument().getDecorations().get(0).getDecoration();
        assertEquals(NORMAL, td.getFontWeight());
        assertEquals(REGULAR, td.getFontPosture());
        assertEquals(false, td.isUnderline());
        td = (TextDecoration) rta.getDocument().getDecorations().get(1).getDecoration();
        assertEquals(BOLD, td.getFontWeight());
        assertEquals(REGULAR, td.getFontPosture());
        assertEquals(true, td.isUnderline());
        td = (TextDecoration) rta.getDocument().getDecorations().get(2).getDecoration();
        assertEquals(NORMAL, td.getFontWeight());
        assertEquals(REGULAR, td.getFontPosture());
        assertEquals(false, td.isUnderline());
    }

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
}
