package com.gluonhq.richtextarea.samples;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.DecorationModel;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.TextDecoration;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static javafx.scene.text.FontWeight.BOLD;
import static javafx.scene.text.FontPosture.ITALIC;

/**
 * This sample shows how to use the RichTextArea control to add a document
 * with some decorations, that are generated programmatically by searching
 * some keywords over a given text.
 */
public class SimpleRTAWithTextDemo extends Application {

    private static final String title = "The Document\n";
    private static final String text =
            "Document is the basic model that contains all the information required for the RichTextArea control, " +
            "in order to render all the rich content, including decorated text, images and other non-text objects.\n" +
            "A document is basically a string with the full text, and a list of DecorationModel that contain the text and paragraph decorations for one or more fragments of the text, " +
            "where a fragment can be defined as the longest substring of the text that shares the same text and paragraph decorations.\n" +
            "Any change to the document invalidates the undo/redo stack, forces the RichTextAreaSkin to recreate the PieceTable and sets it on the RichTextAreaViewModel.";
    private static final String fullText = title + text;

    private static final List<String> keywords = List.of(
            "Document", "RichTextArea", "DecorationModel", "RichTextAreaSkin",
            "PieceTable", "RichTextAreaViewModel"
    );
    private static final TextDecoration preset =
            TextDecoration.builder().presets().fontFamily("Arial").build();
    private static final TextDecoration bold16 =
            TextDecoration.builder().presets().fontFamily("Arial").fontWeight(BOLD).fontSize(16).build();
    private static final TextDecoration mono =
            TextDecoration.builder().presets().fontFamily("Monospaced").fontWeight(BOLD)
                    .fontPosture(ITALIC).background(Color.CORNFLOWERBLUE).build();
    private static final ParagraphDecoration parPreset =
            ParagraphDecoration.builder().presets().build();

    @Override
    public void start(Stage stage) {
        List<DecorationModel> decorationList = getDecorations();
        Document document = new Document(fullText, decorationList, fullText.length());

        RichTextArea editor = new RichTextArea();
        editor.setDocument(document);
        editor.setEditable(false);

        BorderPane root = new BorderPane(editor);
        Scene scene = new Scene(root, 800, 300);
        scene.getStylesheets().add(SimpleRTAWithTextDemo.class.getResource("simplertawithtextdemo.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("RTA: Text and highlighted keywords");
        stage.show();
    }

    private List<DecorationModel> getDecorations() {
        List<DecorationModel> decorations = new ArrayList<>();
        // decoration for title
        decorations.add(new DecorationModel(0, title.length(), bold16, parPreset));

        // search keywords in text
        AtomicInteger counter = new AtomicInteger(title.length());
        keywords.forEach(key -> {
            int i = fullText.substring(counter.get()).indexOf(key);
            if (i > 0) {
                // decoration for regular text
                decorations.add(new DecorationModel(counter.getAndAdd(i), i, preset, parPreset));
            }
            // decoration for keyword
            decorations.add(new DecorationModel(counter.getAndAdd(key.length()), key.length(), mono, parPreset));
        });
        // decoration for regular text
        decorations.add(new DecorationModel(counter.get(), fullText.length() - counter.get(), preset, parPreset));
        return decorations;
    }

}
