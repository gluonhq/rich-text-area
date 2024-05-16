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
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.gluonhq.emoji.EmojiData.getEmojiCollection;
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
import static org.testfx.util.WaitForAsyncUtils.sleep;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@ExtendWith(ApplicationExtension.class)
public class RTATest {

    private static boolean fxStarted;
    private BorderPane root;
    private RichTextArea richTextArea;

    private static final Pattern codenamePattern = Pattern.compile(":([a-zA-Z0-9_-]*):");
    private static final Pattern asciiPattern = Pattern.compile("D:|C:|c:|:D|:-D|>:\\(|>:-\\(|=\\)|=-\\)|<3|</3|;p|;-p|;b|;-b|;P|;-P|" +
            ";\\)|;-\\)|:\\||:-\\||:p|:-p|:P|:-P|:b|:-b|:o|:-o|:O|:-O|:\\\\|:-\\\\|:/|:-/|:>|:->|" +
            ":\\*|:-\\*|:\\)|\\(:|:-\\)|:'\\(|8\\)|\\):|:\\(|:-\\("); // "D:|:D|:-D" don't work, ":o)" is superseded by ":o"
    private static final List<Emoji> asciiEmojiList = getEmojiCollection().stream()
            .filter(e -> e.getTextList() != null)
            .collect(Collectors.toList());

    private static final String MARKER_BOLD = "*", MARKER_ITALIC = "_", MARKER_MONO = "`";
    private static final Pattern markdownDetector = Pattern.compile(
            "(\\`)([^s]?.*?[^s]?|[^s]*)(\\`)|(\\_)([^s]?.*?[^s]?|[^s]*)(\\_)|(\\*)([^s]?.*?[^s]?|[^s]*)(\\*)",
            Pattern.DOTALL);

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
    }

    @Test
    public void findASCIIEmojiTest(FxRobot robot) {
        run(() -> {
            richTextArea.documentProperty().subscribe(d -> {
                String nv = d.getText();
                if (nv != null) {
                    String substring = nv.substring(0, Math.min(richTextArea.getCaretPosition(), nv.length())).toLowerCase(Locale.ROOT);
                    findEmoji(substring,
                            (emoji, start) -> richTextArea.getActionFactory().selectAndInsertEmoji(
                                    new Selection(start, richTextArea.getCaretPosition()), emoji, true).execute(new ActionEvent()));
                }
            });
            richTextArea.getActionFactory().newDocument().execute(new ActionEvent());
            richTextArea.setAutoSave(true);
        });
        waitForFxEvents();

        RichTextArea rta = robot.lookup(".rich-text-area").query();

        robot.write("<3");
        assertEquals(1, rta.getTextLength());
        assertEquals(2, rta.getDocument().getText().length());
        String text = "\u2764\ufe0f";
        assertEquals(text, rta.getDocument().getText());
        assertEquals(1, robot.lookup(node -> node instanceof ImageView).queryAll().size());

        run(() -> richTextArea.getActionFactory().undo().execute(new ActionEvent()));
        waitForFxEvents();

        assertEquals(0, robot.lookup(node -> node instanceof ImageView).queryAll().size());
        assertEquals("<", rta.getDocument().getText());

        run(() -> richTextArea.getActionFactory().redo().execute(new ActionEvent()));
        waitForFxEvents();

        assertEquals(1, robot.lookup(node -> node instanceof ImageView).queryAll().size());
        assertEquals(text, rta.getDocument().getText());

        robot.push(new KeyCodeCombination(A, SHORTCUT_DOWN));
        robot.write("<3 <3 <3 <3 <3 <3 <3 <3");
        assertEquals(15, rta.getTextLength());
        assertEquals(23, rta.getDocument().getText().length());
        text = "\u2764\ufe0f \u2764\ufe0f \u2764\ufe0f \u2764\ufe0f \u2764\ufe0f \u2764\ufe0f \u2764\ufe0f \u2764\ufe0f";
        assertEquals(text, rta.getDocument().getText());
        assertEquals(8, robot.lookup(node -> node instanceof ImageView).queryAll().size());

        robot.push(new KeyCodeCombination(A, SHORTCUT_DOWN));
        robot.write("C: c: >:( >:-( =) =-) <3 </3 ;p ;-p ;b ;-b ;P ;-P " +
                ";) ;-) :| :-| :p :-p :P :-P :b :-b :o :-o :O :-O :\\ :-\\ :/ :-/ :> :-> " +
                ":* :-* :) (: :-) :'( 8) ): :( :-(");

        assertEquals(87, rta.getTextLength());
        assertEquals(131, rta.getDocument().getText().length());
        text = "\ud83d\ude04 \ud83d\ude04 \ud83d\ude20 \ud83d\ude20 \ud83d\ude03 \ud83d\ude03 \u2764\ufe0f \ud83d\udc94 \ud83d\ude1c \ud83d\ude1c \ud83d\ude1c \ud83d\ude1c \ud83d\ude1c \ud83d\ude1c \ud83d\ude09 \ud83d\ude09 \ud83d\ude10 \ud83d\ude10 \ud83d\ude1b \ud83d\ude1b \ud83d\ude1b \ud83d\ude1b \ud83d\ude1b \ud83d\ude1b \ud83d\ude2e \ud83d\ude2e \ud83d\ude2e \ud83d\ude2e \ud83d\ude15 \ud83d\ude15 \ud83d\ude15 \ud83d\ude15 \ud83d\ude06 \ud83d\ude06 \ud83d\ude18 \ud83d\ude18 \ud83d\ude42 \ud83d\ude42 \ud83d\ude42 \ud83d\ude22 \ud83d\ude0e \ud83d\ude1e \ud83d\ude1e \ud83d\ude1e";
        assertEquals(text, rta.getDocument().getText());
        assertEquals(44, robot.lookup(node -> node instanceof ImageView).queryAll().size());

        robot.push(new KeyCodeCombination(A, SHORTCUT_DOWN));
        robot.write("https://www.wikipedia.org");
        assertEquals(0, robot.lookup(node -> node instanceof ImageView).queryAll().size());
        assertEquals("https://www.wikipedia.org", rta.getDocument().getText());
    }

    @Test
    public void findCodeNameEmojiTest(FxRobot robot) {
        run(() -> {
            richTextArea.documentProperty().subscribe(d -> {
                String nv = d.getText();
                if (nv != null) {
                    String substring = nv.substring(0, Math.min(richTextArea.getCaretPosition(), nv.length())).toLowerCase(Locale.ROOT);
                    findEmoji(substring,
                            (emoji, start) -> richTextArea.getActionFactory().selectAndInsertEmoji(
                                    new Selection(start, richTextArea.getCaretPosition()), emoji, true).execute(new ActionEvent()));
                }
            });
            richTextArea.getActionFactory().newDocument().execute(new ActionEvent());
            richTextArea.setAutoSave(true);
        });
        waitForFxEvents();

        RichTextArea rta = robot.lookup(".rich-text-area").query();

        robot.write(":wink:");
        assertEquals(1, rta.getTextLength());
        assertEquals(2, rta.getDocument().getText().length());
        String text = "\ud83d\ude09";
        assertEquals(text, rta.getDocument().getText());
        assertEquals(1, robot.lookup(node -> node instanceof ImageView).queryAll().size());

        run(() -> richTextArea.getActionFactory().undo().execute(new ActionEvent()));
        waitForFxEvents();

        assertEquals(0, robot.lookup(node -> node instanceof ImageView).queryAll().size());
        assertEquals(":wink", rta.getDocument().getText());

        run(() -> richTextArea.getActionFactory().redo().execute(new ActionEvent()));
        waitForFxEvents();

        assertEquals(1, robot.lookup(node -> node instanceof ImageView).queryAll().size());
        assertEquals(text, rta.getDocument().getText());

        robot.push(new KeyCodeCombination(A, SHORTCUT_DOWN));
        robot.write(":flag-wales:");
        assertEquals(1, rta.getTextLength());
        assertEquals(14, rta.getDocument().getText().length());
        text = "\ud83c\udff4\udb40\udc67\udb40\udc62\udb40\udc77\udb40\udc6c\udb40\udc73\udb40\udc7f";
        assertEquals(text, rta.getDocument().getText());
        assertEquals(1, robot.lookup(node -> node instanceof ImageView).queryAll().size());

        robot.push(new KeyCodeCombination(A, SHORTCUT_DOWN));
        robot.write("this is a:wink:");
        assertEquals(0, robot.lookup(node -> node instanceof ImageView).queryAll().size());
        assertEquals("this is a:wink:", rta.getDocument().getText());
    }

    @Test
    public void findMarkdownTest(FxRobot robot) {
        run(() -> {
            richTextArea.documentProperty().subscribe(d -> {
                String nv = d.getText();
                if (nv != null) {
                    String substring = nv.substring(0, Math.min(richTextArea.getCaretPosition(), nv.length())).toLowerCase(Locale.ROOT);
                    findMarkdown(substring, (start, marker) -> {
                        richTextArea.caretPositionProperty().subscribe(c -> assertEquals(c, richTextArea.getTextLength()));
                        richTextArea.getActionFactory().removeExtremesAndDecorate(
                                        new Selection(start, richTextArea.getCaretPosition()), getStyleFromMarker(marker))
                                .execute(new ActionEvent());
                    });
                }
            });
            richTextArea.getActionFactory().newDocument().execute(new ActionEvent());
            richTextArea.setAutoSave(true);
        });
        waitForFxEvents();

        RichTextArea rta = robot.lookup(".rich-text-area").query();

        robot.write("hello *bold*");
        assertEquals(10, rta.getTextLength());
        assertEquals(10, rta.getDocument().getText().length());
        String text = "hello bold";
        assertEquals(text, rta.getDocument().getText());
        assertEquals(2, rta.getDocument().getDecorations().size());
        DecorationModel dm = rta.getDocument().getDecorations().get(0);
        assertEquals(0, dm.getStart());
        assertEquals(6, dm.getLength());
        assertInstanceOf(TextDecoration.class, dm.getDecoration());
        TextDecoration td = (TextDecoration) dm.getDecoration();
        assertEquals(NORMAL, td.getFontWeight());
        DecorationModel dm2 = rta.getDocument().getDecorations().get(1);
        assertEquals(6, dm2.getStart());
        assertEquals(4, dm2.getLength());
        assertInstanceOf(TextDecoration.class, dm2.getDecoration());
        td = (TextDecoration) dm2.getDecoration();
        assertEquals(BOLD, td.getFontWeight());

        run(() -> richTextArea.getActionFactory().undo().execute(new ActionEvent()));
        waitForFxEvents();

        assertEquals(11, rta.getTextLength());
        assertEquals(11, rta.getDocument().getText().length());
        assertEquals("hello *bold", rta.getDocument().getText());
        assertEquals(1, rta.getDocument().getDecorations().size());

        run(() -> richTextArea.getActionFactory().redo().execute(new ActionEvent()));
        waitForFxEvents();

        assertEquals(text, rta.getDocument().getText());
        assertEquals(2, rta.getDocument().getDecorations().size());
    }

    private static void findEmoji(String text, BiConsumer<Emoji, Integer> onCodeNameFound) {
        if (text.endsWith(" ")) {
            return;
        }
        int wordBeginIndex = Math.max(text.lastIndexOf(" ") + 1, text.lastIndexOf("\n") + 1);
        String word = text.substring(wordBeginIndex);
        Matcher asciiMatcher = asciiPattern.matcher(word);
        if (asciiMatcher.find() && asciiMatcher.start() == 0) {
            // check if word is an ascii emoji and replace
            asciiEmojiList.stream()
                    .filter(e -> e.getTextList().contains(asciiMatcher.group()))
                    .findFirst()
                    .ifPresent(emoji -> onCodeNameFound.accept(emoji, wordBeginIndex));
            return;
        }
        if (word.startsWith(":") && word.length() > 2) {
            // check if word contains an emoji codename :name:
            if (word.substring(1).contains(":")) {
                Matcher matcher = codenamePattern.matcher(word);
                if (matcher.find()) {
                    EmojiData.emojiFromShortName(matcher.group(1))
                            .ifPresent(emoji -> onCodeNameFound.accept(emoji, wordBeginIndex));
                }
            }
        }
    }

    private static void findMarkdown(String text, BiConsumer<Integer, String> onFound) {
        Matcher matcher = markdownDetector.matcher(text);
        while (matcher.find()) {
            for (int i = 1; i < matcher.groupCount(); i++) {
                String marker = matcher.group(i);
                if (marker != null) {
                    onFound.accept(matcher.start(), marker);
                    break;
                }
            }
        }
    }

    private TextDecoration getStyleFromMarker(String marker) {
        TextDecoration.Builder builder = TextDecoration.builder();
        switch (marker) {
            case MARKER_BOLD:
                return builder.fontWeight(BOLD).build();
            case MARKER_ITALIC:
                return builder.fontPosture(ITALIC).build();
            case MARKER_MONO:
                return builder.fontFamily("monospaced").build();
        }
        return builder.build();
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
