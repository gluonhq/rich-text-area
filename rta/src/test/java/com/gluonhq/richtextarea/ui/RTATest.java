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

import com.gluonhq.emoji.Emoji;
import com.gluonhq.emoji.EmojiData;
import com.gluonhq.emoji.EmojiSkinTone;
import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.Selection;
import com.gluonhq.richtextarea.action.TextDecorateAction;
import com.gluonhq.richtextarea.model.DecorationModel;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.PieceTable;
import com.gluonhq.richtextarea.model.TextDecoration;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
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

import java.nio.CharBuffer;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static javafx.scene.input.KeyCode.A;
import static javafx.scene.input.KeyCode.LEFT;
import static javafx.scene.input.KeyCode.RIGHT;
import static javafx.scene.input.KeyCode.Z;
import static javafx.scene.input.KeyCombination.ALT_DOWN;
import static javafx.scene.input.KeyCombination.SHIFT_DOWN;
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

    @Test
    public void emojiDemoTest(FxRobot robot) {
        run(() -> {
            String title = "Document with emojis \ud83d\ude03!\n";
            String contentText = "\uD83D\uDC4B\uD83C\uDFFC, this is some random text with some emojis " +
                            "like \uD83E\uDDD1\uD83C\uDFFC\u200D\uD83E\uDD1D\u200D\uD83E\uDDD1\uD83C\uDFFD or " +
                            "\uD83C\uDFF4\uDB40\uDC67\uDB40\uDC62\uDB40\uDC73\uDB40\uDC63\uDB40\uDC74\uDB40\uDC7F.\n" +
                            "These are emojis with skin tone or hair style, like:\n";
            String personText = EmojiData.search("person").stream()
                    .limit(10)
                    .map(Emoji::character)
                    .collect(Collectors.joining(", "));
            String endText = ".\nAnd this is another emoji with skin tone: ";
            String text = title + contentText + personText + endText;
            ParagraphDecoration paragraphDecoration = ParagraphDecoration.builder().presets().build();
            List<DecorationModel> decorationModels = List.of(
                    new DecorationModel(0, title.length(), TextDecoration.builder().presets().fontFamily("Arial").fontWeight(BOLD).fontSize(16).build(), paragraphDecoration),
                    new DecorationModel(title.length(), contentText.length(), TextDecoration.builder().presets().fontFamily("Arial").fontSize(14).build(), paragraphDecoration),
                    new DecorationModel(title.length() + contentText.length(), personText.length() + endText.length(), TextDecoration.builder().presets().fontFamily("Arial").fontPosture(ITALIC).fontSize(14).build(), paragraphDecoration));
            Document document = new Document(text, decorationModels, text.length());
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
            richTextArea.setAutoSave(true);
        });
        waitForFxEvents();

        run(() -> EmojiData.emojiFromShortName("runner").ifPresent(emoji -> {
            Emoji emojiWithTone = emoji.getSkinVariationMap().get(EmojiSkinTone.MEDIUM_SKIN_TONE.getUnicode());
            richTextArea.getActionFactory().insertEmoji(emojiWithTone).execute(new ActionEvent());
        }));
        waitForFxEvents();

        RichTextArea rta = robot.lookup(".rich-text-area").query();

        assertEquals(208, rta.getTextLength());
        assertEquals(267, rta.getDocument().getText().length());
        assertEquals(267, rta.getCaretPosition());
        assertEquals(267, rta.getDocument().getCaretPosition());

        String serialText = CharBuffer.wrap(rta.getDocument().getText().toCharArray()).chars()
                .mapToObj(i -> i > 255 ? String.format("\\u%x", i) : String.valueOf((char) i))
                .collect(Collectors.joining());
        String text = "Document with emojis \ud83d\ude03!\n" +
                "\ud83d\udc4b\ud83c\udffc, this is some random text with some emojis like \ud83e\uddd1\ud83c\udffc\u200d\ud83e\udd1d\u200d\ud83e\uddd1\ud83c\udffd or \ud83c\udff4\udb40\udc67\udb40\udc62\udb40\udc73\udb40\udc63\udb40\udc74\udb40\udc7f.\n" +
                "These are emojis with skin tone or hair style, like:\n" +
                "\ud83d\udc71, \ud83d\udc71\ud83c\udffb, \ud83d\udc71\ud83c\udffd, \ud83d\udc71\ud83c\udffc, \ud83d\udc71\ud83c\udfff, \ud83d\udc71\ud83c\udffe, \ud83e\uddd4\ud83c\udffd, \ud83e\uddd4\ud83c\udffc, \ud83e\uddd4\ud83c\udffb, \ud83e\uddd4\ud83c\udfff.\n" +
                "And this is another emoji with skin tone: \ud83c\udfc3\ud83c\udffd";
        assertEquals(text, rta.getDocument().getText());
        assertEquals(267, text.length());

        String internalText = "Document with emojis \u2063!\n" +
                "\u2063, this is some random text with some emojis like \u2063 or \u2063.\n" +
                "These are emojis with skin tone or hair style, like:\n" +
                "\u2063, \u2063, \u2063, \u2063, \u2063, \u2063, \u2063, \u2063, \u2063, \u2063.\n" +
                "And this is another emoji with skin tone: \u2063";
        assertEquals(208, internalText.length());
        PieceTable pt = new PieceTable(rta.getDocument());
        StringBuilder internalSb = new StringBuilder();
        pt.walkFragments((u, d) -> internalSb.append(u.getInternalText()), 0, 208);
        assertEquals(internalText, internalSb.toString());

        assertEquals(3, rta.getDocument().getDecorations().size());
        DecorationModel dm1 = rta.getDocument().getDecorations().get(0);
        assertEquals(0, dm1.getStart());
        assertEquals(25, dm1.getLength());
        assertInstanceOf(TextDecoration.class, dm1.getDecoration());
        assertEquals(16, ((TextDecoration) dm1.getDecoration()).getFontSize());
        assertEquals(BOLD, ((TextDecoration) dm1.getDecoration()).getFontWeight());
        DecorationModel dm2 = rta.getDocument().getDecorations().get(1);
        assertEquals(25, dm2.getStart());
        assertEquals(138, dm2.getLength());
        assertInstanceOf(TextDecoration.class, dm2.getDecoration());
        assertEquals(NORMAL, ((TextDecoration) dm2.getDecoration()).getFontWeight());
        DecorationModel dm3 = rta.getDocument().getDecorations().get(2);
        assertEquals(163, dm3.getStart());
        assertEquals(104, dm3.getLength());
        assertInstanceOf(TextDecoration.class, dm3.getDecoration());
        assertEquals(ITALIC, ((TextDecoration) dm3.getDecoration()).getFontPosture());

        assertEquals(15, robot.lookup(node -> node instanceof ImageView).queryAll().size());
        AtomicInteger counter = new AtomicInteger();
        for (int i = 0; i < 15; i++) {
            ImageView imageView = robot.lookup(node -> node instanceof ImageView).nth(i).query();
            Object emojiUnified = imageView.getProperties().get("emoji_unified");
            assertNotNull(emojiUnified);
            EmojiData.emojiFromCodepoints(emojiUnified.toString()).ifPresent(emoji -> {
                counter.addAndGet(emoji.character().length());
                assertEquals(emojiUnified, emoji.getUnified());
            });
        }
        assertEquals(267 - 208, counter.get() - 15);

        run(() -> richTextArea.getActionFactory().selectAll().execute(new ActionEvent()));
        waitForFxEvents();

        Selection selection = rta.getSelection();
        assertNotNull(selection);
        assertEquals(0, selection.getStart());
        assertEquals(208, selection.getEnd());

        run(() -> richTextArea.getActionFactory().selectNone().execute(new ActionEvent()));
        waitForFxEvents();

        int noToneEmojis = 0;
        int mediumDarkToneEmojis = 0;
        for (int i = 0; i < 15; i++) {
            ImageView imageView = robot.lookup(node -> node instanceof ImageView).nth(i).query();
            Object emojiUnified = imageView.getProperties().get("emoji_unified");
            assertNotNull(emojiUnified);
            if ("1F471".equals(emojiUnified.toString())) {
                noToneEmojis++;
            }else if ("1F471-1F3FD".equals(emojiUnified.toString())) {
                mediumDarkToneEmojis++;
            }
        }
        assertEquals(1, noToneEmojis);
        assertEquals(1, mediumDarkToneEmojis);

        run(() -> richTextArea.setSkinTone(EmojiSkinTone.MEDIUM_SKIN_TONE));
        waitForFxEvents();

        noToneEmojis = 0;
        mediumDarkToneEmojis = 0;
        for (int i = 0; i < 15; i++) {
            ImageView imageView = robot.lookup(node -> node instanceof ImageView).nth(i).query();
            Object emojiUnified = imageView.getProperties().get("emoji_unified");
            assertNotNull(emojiUnified);
            if ("1F471".equals(emojiUnified.toString())) {
                noToneEmojis++;
            }else if ("1F471-1F3FD".equals(emojiUnified.toString())) {
                mediumDarkToneEmojis++;
            }
        }
        assertEquals(0, noToneEmojis);
        assertEquals(2, mediumDarkToneEmojis);

        run(() -> richTextArea.getActionFactory().selectAll().execute(new ActionEvent()));
        waitForFxEvents();

        run(() -> richTextArea.getActionFactory().save().execute(new ActionEvent()));
        waitForFxEvents();
        Document oldDocument = rta.getDocument();

        run(() -> {
            richTextArea.getActionFactory().copy().execute(new ActionEvent());
            richTextArea.getActionFactory().newDocument().execute(new ActionEvent());
        });
        waitForFxEvents();

        run(() -> richTextArea.getActionFactory().paste().execute(new ActionEvent()));
        waitForFxEvents();

        Document newDocument = rta.getDocument();
        assertEquals(oldDocument.getText(), newDocument.getText());
        assertEquals(oldDocument.getDecorations(), newDocument.getDecorations());
        assertEquals(oldDocument.getCaretPosition(), newDocument.getCaretPosition());
        assertEquals(oldDocument, newDocument);
    }

    @Test
    public void copyPasteTest(FxRobot robot) {
        run(() -> {
            String text = "One \ud83d\ude00 Text \ufeff@name\ufeff!!";
            TextDecoration textDecoration1 = TextDecoration.builder().presets().fontFamily("Arial").fontSize(20).build();
            TextDecoration textDecoration2 = TextDecoration.builder().presets().fontFamily("Arial").fontSize(16).fontWeight(BOLD).build();
            TextDecoration textDecoration3 = TextDecoration.builder().presets().fontFamily("Arial").fontSize(14).foreground("red").build();
            ParagraphDecoration paragraphDecoration = ParagraphDecoration.builder().presets().build();
            Document document = new Document(text,
                    List.of(new DecorationModel(0, 7, textDecoration1, paragraphDecoration),
                            new DecorationModel(7, 5, textDecoration2, paragraphDecoration),
                            new DecorationModel(12, 7, textDecoration3, paragraphDecoration),
                            new DecorationModel(19, 2, TextDecoration.builder().presets().fontFamily("Arial").build(), paragraphDecoration)), text.length());
            richTextArea.getActionFactory().open(document).execute(new ActionEvent());
            richTextArea.setAutoSave(true);
        });
        waitForFxEvents();

        RichTextArea rta = robot.lookup(".rich-text-area").query();

        assertEquals(14, rta.getTextLength());
        assertEquals(21, rta.getDocument().getText().length());
        assertEquals(21, rta.getCaretPosition());
        assertEquals(21, rta.getDocument().getCaretPosition());
        assertEquals(4, rta.getDocument().getDecorations().size());

        Document document = rta.getDocument();

        robot.push(new KeyCodeCombination(LEFT, SHORTCUT_DOWN));
        robot.push(new KeyCodeCombination(RIGHT, SHIFT_DOWN, ALT_DOWN));
        robot.push(new KeyCodeCombination(RIGHT, SHIFT_DOWN, ALT_DOWN));

        run(() -> richTextArea.getActionFactory().copy().execute(new ActionEvent()));
        waitForFxEvents();

        robot.push(new KeyCodeCombination(RIGHT, SHORTCUT_DOWN));
        robot.push(new KeyCodeCombination(LEFT));

        run(() -> richTextArea.getActionFactory().paste().execute(new ActionEvent()));
        waitForFxEvents();

        String newText = "One \ud83d\ude00 Text \ufeff@name\ufeff!One \ud83d\ude00 !";
        assertEquals(newText, rta.getDocument().getText());
        assertEquals(20, rta.getTextLength());
        assertEquals(28, rta.getDocument().getText().length());
        assertEquals(27, rta.getCaretPosition());
        assertEquals(27, rta.getDocument().getCaretPosition());
        assertEquals(6, rta.getDocument().getDecorations().size());

        DecorationModel dm1 = rta.getDocument().getDecorations().get(4);
        assertEquals(20, dm1.getStart());
        assertEquals(7, dm1.getLength());
        assertInstanceOf(TextDecoration.class, dm1.getDecoration());
        assertEquals(20, ((TextDecoration) dm1.getDecoration()).getFontSize());
        assertEquals(NORMAL, ((TextDecoration) dm1.getDecoration()).getFontWeight());
        DecorationModel dm2 = rta.getDocument().getDecorations().get(5);
        assertEquals(27, dm2.getStart());
        assertEquals(1, dm2.getLength());
        assertInstanceOf(TextDecoration.class, dm2.getDecoration());
        assertEquals(NORMAL, ((TextDecoration) dm2.getDecoration()).getFontWeight());

        Document newDocument = rta.getDocument();

        robot.push(new KeyCodeCombination(Z, SHORTCUT_DOWN));
        assertEquals(document, rta.getDocument());
        robot.push(new KeyCodeCombination(Z, SHORTCUT_DOWN, SHIFT_DOWN));
        assertEquals(newDocument, rta.getDocument());

        robot.push(new KeyCodeCombination(RIGHT));

        run(() -> richTextArea.getActionFactory().paste().execute(new ActionEvent()));
        waitForFxEvents();

        newText = "One \ud83d\ude00 Text \ufeff@name\ufeff!One \ud83d\ude00 !One \ud83d\ude00 ";
        assertEquals(newText, rta.getDocument().getText());
        assertEquals(26, rta.getTextLength());
        assertEquals(35, rta.getDocument().getText().length());
        assertEquals(35, rta.getCaretPosition());
        assertEquals(35, rta.getDocument().getCaretPosition());
        assertEquals(7, rta.getDocument().getDecorations().size());
        dm1 = rta.getDocument().getDecorations().get(6);
        assertEquals(28, dm1.getStart());
        assertEquals(7, dm1.getLength());
        assertInstanceOf(TextDecoration.class, dm1.getDecoration());
        assertEquals(20, ((TextDecoration) dm1.getDecoration()).getFontSize());
        assertEquals(NORMAL, ((TextDecoration) dm1.getDecoration()).getFontWeight());
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
