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
package com.gluonhq.richtextarea.samples;

import com.gluonhq.emoji.Emoji;
import com.gluonhq.emoji.EmojiData;
import com.gluonhq.emoji.EmojiSkinTone;
import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.DecorationModel;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.TextDecoration;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This basic sample shows how to use the RichTextArea control to render text and
 * emojis.
 * <p>
 * This sample doesn't include a control to select interactively emojis (See
 * {@link SimpleRTAWithEmojiPopupDemo} for that).
 */
public class SimpleRTAWithEmojiDemo extends Application {

    private static final String text =
            "Document with emojis \ud83d\ude03!\n" +
            "\uD83D\uDC4B\uD83C\uDFFC, this is some random text with some emojis " +
            "like \uD83E\uDDD1\uD83C\uDFFC\u200D\uD83E\uDD1D\u200D\uD83E\uDDD1\uD83C\uDFFD or " +
            "\uD83C\uDFF4\uDB40\uDC67\uDB40\uDC62\uDB40\uDC73\uDB40\uDC63\uDB40\uDC74\uDB40\uDC7F.\n" +
            "These are emojis with skin tone or hair style, like:\n";

    private static final StringBuilder fullText = new StringBuilder(text);
    private static final TextDecoration preset =
            TextDecoration.builder().presets().fontFamily("Arial").fontSize(14).build();
    private static final ParagraphDecoration parPreset =
            ParagraphDecoration.builder().presets().build();

    @Override
    public void start(Stage stage) {
        String personText = EmojiData.search("person").stream()
                .limit(10)
                .map(Emoji::character)
                .collect(Collectors.joining(", "));
        fullText.append(personText).append(".\nAnd this is another emoji with skin tone: ");

        List<DecorationModel> decorationList = getDecorations();
        Document document = new Document(fullText.toString(), decorationList, fullText.length());

        RichTextArea editor = new RichTextArea();
        editor.setDocument(document);

        BorderPane root = new BorderPane(editor);
        Scene scene = new Scene(root, 800, 300);
        scene.getStylesheets().add(SimpleRTAWithEmojiDemo.class.getResource("simplertawithemojidemo.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("RTA: Text and emojis");
        stage.show();

        editor.setSkinTone(EmojiSkinTone.MEDIUM_SKIN_TONE);

        // Use ActionFactory to insert emojis
        EmojiData.emojiFromShortName("runner")
                .ifPresent(emoji -> {
                    Emoji emojiWithTone = emoji.getSkinVariationMap().get(editor.getSkinTone().getUnicode());
                    editor.getActionFactory().insertEmoji(emojiWithTone).execute(new ActionEvent());
                });
    }

    private List<DecorationModel> getDecorations() {
        List<DecorationModel> decorations = new ArrayList<>();
        // decoration for text
        decorations.add(new DecorationModel(0, fullText.length(), preset, parPreset));
        return decorations;
    }

}
