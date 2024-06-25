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

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.Selection;
import com.gluonhq.richtextarea.action.TextDecorateAction;
import com.gluonhq.richtextarea.model.DecorationModel;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.TextDecoration;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

import static javafx.scene.text.FontPosture.ITALIC;
import static javafx.scene.text.FontPosture.REGULAR;
import static javafx.scene.text.FontWeight.BOLD;
import static javafx.scene.text.FontWeight.NORMAL;

/**
 * This sample shows how to use the RichTextArea control to render some text and
 * interact with it in a basic way via three toggle buttons.
 * <p>
 * Run the sample and select some or all text, via mouse or keyboard, and then
 * press the toggles to see how the decoration of the selection changes accordingly.
 * <p>
 * Note that when you move the caret over the text, the toggles update their state
 * (enabled means bold/italic/underline active), showing at any time the current
 * decoration at the caret.
 */
public class ActionsDemo extends Application {

    private static final String text =
            "Document is the basic model that contains all the information required for the RichTextArea control, " +
            "in order to render all the rich content, including decorated text, images and other non-text objects.\n" +
            "A document is basically a string with the full text, and a list of DecorationModel that contain the text and paragraph decorations for one or more fragments of the text, " +
            "where a fragment can be defined as the longest substring of the text that shares the same text and paragraph decorations.\n" +
            "Any change to the document invalidates the undo/redo stack, forces the RichTextAreaSkin to recreate the PieceTable and sets it on the RichTextAreaViewModel.";

    private static final TextDecoration preset =
            TextDecoration.builder().presets().fontFamily("Arial").fontSize(14).build();
    private static final ParagraphDecoration parPreset =
            ParagraphDecoration.builder().presets().build();

    @Override
    public void start(Stage stage) {
        List<DecorationModel> decorationList = getDecorations();
        Document document = new Document(text, decorationList, text.length());

        RichTextArea editor = new RichTextArea();
        editor.getActionFactory().open(document).execute(new ActionEvent());

        BorderPane root = new BorderPane(editor);

        // decorate actions
        ToggleButton fontBoldToggle = new ToggleButton("Bold");
        new TextDecorateAction<>(editor, fontBoldToggle.selectedProperty().asObject(),
                d -> d.getFontWeight() == BOLD,
                (builder, a) -> builder.fontWeight(a ? BOLD : NORMAL).build());
        ToggleButton fontItalicToggle = new ToggleButton("Italic");
        new TextDecorateAction<>(editor, fontItalicToggle.selectedProperty().asObject(),
                d -> d.getFontPosture() == ITALIC,
                (builder, a) -> builder.fontPosture(a ? ITALIC : REGULAR).build());
        ToggleButton fontUnderlinedToggle = new ToggleButton("Underline");
        new TextDecorateAction<>(editor, fontUnderlinedToggle.selectedProperty().asObject(),
                TextDecoration::isUnderline, (builder, a) -> builder.underline(a).build());
        HBox actionsBox = new HBox(fontBoldToggle, fontItalicToggle, fontUnderlinedToggle);
        actionsBox.getStyleClass().add("actions-box");
        root.setTop(actionsBox);

        Scene scene = new Scene(root, 800, 300);
        scene.getStylesheets().add(ActionsDemo.class.getResource("actionsDemo.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("RTA: Actions");
        stage.show();

        // select some text and change its decoration;
        editor.getActionFactory().selectAndDecorate(new Selection(12, 27),
                TextDecoration.builder().presets().fontFamily("Arial")
                        .fontWeight(BOLD).underline(true)
                        .build()).execute(new ActionEvent());
    }

    private List<DecorationModel> getDecorations() {
        List<DecorationModel> decorations = new ArrayList<>();
        // decoration for text
        decorations.add(new DecorationModel(0, text.length(), preset, parPreset));
        return decorations;
    }

}
