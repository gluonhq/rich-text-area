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
import com.gluonhq.emoji.EmojiSkinTone;
import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.DecorationModel;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.TextDecoration;
import com.gluonhq.richtextarea.samples.popup.EmojiPopup;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.util.List;

/**
 * This basic sample shows how to use the RichTextArea control to render text and
 * emojis, including a popup control to select interactively emojis.
 */
public class EmojiPopupDemo extends Application {

    /**
     * Defines the text and paragraph decorations, based on the default presets,
     * but with Arial font
     */
    private static final List<DecorationModel> decorations = List.of(
            new DecorationModel(0, 0,
                    TextDecoration.builder().presets().fontFamily("Arial").build(),
                    ParagraphDecoration.builder().presets().build()));

    /**
     * Creates an empty document with the new decorations
     */
    private static final Document emptyDocument =
            new Document("", decorations, 0);

    @Override
    public void start(Stage stage) {
        RichTextArea editor = new RichTextArea();
        editor.getActionFactory().open(emptyDocument).execute(new ActionEvent());
        editor.setPromptText("Type something or insert emojis!");
        editor.setSkinTone(EmojiSkinTone.MEDIUM_SKIN_TONE);
        editor.setPadding(new Insets(20));

        Region region = new Region();
        region.getStyleClass().addAll("icon", "emoji-outline");
        Button emojiButton = new Button(null, region);
        emojiButton.getStyleClass().add("emoji-button");
        emojiButton.setOnAction(e -> {
            EmojiPopup emojiPopup = new EmojiPopup();
            emojiPopup.setSkinTone(editor.getSkinTone());
            editor.skinToneProperty().bindBidirectional(emojiPopup.skinToneProperty());
            emojiPopup.setOnAction(ev -> {
                Emoji emoji = (Emoji) ev.getSource();
                editor.getActionFactory().insertEmoji(emoji).execute(new ActionEvent());
            });
            emojiPopup.show(emojiButton);
        });

        HBox root = new HBox(20, editor, emojiButton);
        HBox.setHgrow(editor, Priority.ALWAYS);
        root.getStyleClass().add("root-box");

        Scene scene = new Scene(root, 600, 300);
        scene.getStylesheets().add(EmojiPopupDemo.class.getResource("emojiPopupDemo.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("RTA: Text and emoji popup");
        stage.show();

        editor.requestFocus();
    }

}
