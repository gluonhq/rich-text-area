/*
 * Copyright (c) 2023, 2024, Gluon
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
import com.gluonhq.richtextarea.model.DecorationModel;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.TextDecoration;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.List;

/**
 * Basic sample with the RichTextArea control, showing a text with a default
 * font family, font size, and foreground color.
 * <p>
 * While all the control features are available, but there are no menus or
 * toolbars included, so user interaction is limited to shortcuts or context
 * menu.
 * <p>
 * For instance, after typing some text, select all (Ctrl/Cmd + A) or part of it
 * (with mouse or keyboard) and press Ctrl/Cmd + I for italic or Ctrl/Cmd + B for bold.
 * <p>
 * Undo/Redo, Cut/Copy/Paste options work as usual.
 * You can copy text with emoji unicode, and paste it on the editor.
 * For instance, while running this sample, copy this text:
 * <pre>
 *     {@code Hello 👋🏼}
 * </pre>
 * and paste it, you should see the waving hand emoji and some text. Also copying from
 * the control and pasting it on the control itself or on any other application will work
 * too, keeping the rich content when possible.
 * <p>
 * Right click to display a context menu with different options, like inserting a
 * 2x1 table.
 * <p>
 * To apply the rest of the control features, some UI is needed for user interaction. See
 * the {@link FullFeaturedDemo} sample for a complete and advanced showcase.
 */
public class BasicDocumentDemo extends Application {

    @Override
    public void start(Stage stage) {
        // String to decorate
        String text = "Hello RTA";
        /*
         * Defines the text decorations with
         * font family as Arial,
         * font size as 20,
         * foreground color as RED
         */
        TextDecoration textDecoration = TextDecoration.builder().presets()
                .fontFamily("Arial")
                .fontSize(20)
                .foreground("red")
                .build();
        ParagraphDecoration paragraphDecoration = ParagraphDecoration.builder().presets().build();
        DecorationModel decorationModel = new DecorationModel(0, text.length(), textDecoration, paragraphDecoration);
        Document document = new Document(text, List.of(decorationModel), text.length());
        RichTextArea richTextArea = new RichTextArea();
        richTextArea.getActionFactory().open(document).execute(new ActionEvent());
        StackPane root = new StackPane(richTextArea);
        Scene scene = new Scene(root, 640, 480);
        stage.setScene(scene);
        stage.setTitle("RichTextArea");
        stage.show();
    }
}
