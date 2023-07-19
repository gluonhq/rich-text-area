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
import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.DecorationModel;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.TableDecoration;
import com.gluonhq.richtextarea.model.TextDecoration;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

import static javafx.scene.text.FontWeight.BOLD;

/**
 * Sample that shows how to embed a table into the RichTextArea control, combining emojis and text
 */
public class TableDemo extends Application {

    private static final TextDecoration bold16 =
            TextDecoration.builder().presets().fontFamily("Arial").fontWeight(BOLD).fontSize(16).build();
    private static final TextDecoration preset =
            TextDecoration.builder().presets().fontFamily("Arial").fontSize(14).build();
    private static final ParagraphDecoration parPreset =
            ParagraphDecoration.builder().presets().build();

    @Override
    public void start(Stage stage) {
        RichTextArea editor = new RichTextArea();
        editor.setDocument(getDocument());
        BorderPane root = new BorderPane(editor);
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(TableDemo.class.getResource("tableDemo.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private List<Emoji> getAllFlags() {
        return EmojiData.emojiFromCategory("Flags");
    }

    private List<String> getFlagsAsText() {
        return getAllFlags().stream()
                .map(emoji -> emoji.character() + "\u200b" + emoji.getName())
                .collect(Collectors.toList());
    }

    private Document getDocument() {

        List<String> flagsAsText = getFlagsAsText();
        int flags = flagsAsText.size();

        TextAlignment[][] alignment = new TextAlignment[flags][2];
        for (int i = 0; i < flags; i++) {
            alignment[i][0] = TextAlignment.CENTER;
            alignment[i][1] = TextAlignment.LEFT;
        }
        TableDecoration tableDecoration = new TableDecoration(flags, 2, alignment);
        ParagraphDecoration table = ParagraphDecoration.builder().presets().alignment(TextAlignment.CENTER)
                .tableDecoration(tableDecoration)
                .topInset(5).rightInset(5).bottomInset(5).leftInset(5)
                .build();

        String title = "Table of flags\n";
        String subtitle = "This is a sample of a table\n";
        String text = String.join("\u200b", flagsAsText) + "\n";
        String end = "\n";
        return new Document(title + subtitle + text + end,
                List.of(new DecorationModel(0, title.length(), bold16, parPreset),
                        new DecorationModel(title.length(), subtitle.length(), preset, parPreset),
                        new DecorationModel(title.length() + subtitle.length(), text.length(), preset, table),
                        new DecorationModel(title.length() + subtitle.length() + text.length(), end.length(), preset, parPreset)),
                title.length() + subtitle.length() + text.length() + end.length());
    }
}
