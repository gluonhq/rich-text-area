package com.gluonhq.richtextarea.samples;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.DecorationModel;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.TextDecoration;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample with a list generated programmatically with
 * custom numbered and bulleted decorations created via
 * {@link RichTextArea#paragraphGraphicFactoryProperty()}.
 */
public class SimpleRTAWithListsDemo extends Application {

    private static final String title = "This is a list\n";
    private static final StringBuilder fullText = new StringBuilder(title);

    private static final TextDecoration presetSection =
            TextDecoration.builder().presets().fontFamily("Arial")
                    .fontWeight(FontWeight.BOLD).underline(true).build();

    private static final TextDecoration preset =
            TextDecoration.builder().presets().fontFamily("Arial").build();

    private static final ParagraphDecoration parPreset =
            ParagraphDecoration.builder().presets().build();


    @Override
    public void start(Stage stage) {
        List<DecorationModel> decorationList = getDecorations();
        Document document = new Document(fullText.toString(), decorationList, fullText.toString().length());

        RichTextArea editor = new RichTextArea();
        editor.setParagraphGraphicFactory((i, t) -> {
            if (i < 1) {
                return null;
            } else if (i == 1) {
                return new Rectangle(5, 5);
            }
            Label label = new Label("#.-");
            label.setMouseTransparent(true);
            label.getStyleClass().add("numbered-list-label");
            return label;
        });
        editor.setDocument(document);

        BorderPane root = new BorderPane(editor);
        Scene scene = new Scene(root, 600, 500);
        scene.getStylesheets().add(SimpleRTAWithTextDemo.class.getResource("simplertawithlistsdemo.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("RTA: Numbered and bulleted list");
        stage.show();
    }

    private List<DecorationModel> getDecorations() {
        List<DecorationModel> decorations = new ArrayList<>();

        int counter = fullText.length();
        decorations.add(new DecorationModel(0, counter, preset, parPreset));

        for (int i = 0; i < 4; i++) {
            String text = "Section " + (i + 1) + "\n";
            fullText.append(text);
            decorations.add(new DecorationModel(counter, text.length(), presetSection,
                    ParagraphDecoration.builder().presets()
                            .graphicType(ParagraphDecoration.GraphicType.BULLETED_LIST)
                            .indentationLevel(1)
                            .build()));
            counter += text.length();
            for (int j = 0; j < 3; j++) {
                String subtext = "Item " + (i + 1) + "." + (j + 1) + "\n";
                fullText.append(subtext);
                decorations.add(new DecorationModel(counter, subtext.length(), preset,
                        ParagraphDecoration.builder().presets()
                                .graphicType(ParagraphDecoration.GraphicType.NUMBERED_LIST)
                                .indentationLevel(2)
                                .build()));
                counter += subtext.length();
            }
        }
        fullText.append("End of list\n");
        decorations.add(new DecorationModel(counter, 4, preset, parPreset));
        return decorations;
    }
}
