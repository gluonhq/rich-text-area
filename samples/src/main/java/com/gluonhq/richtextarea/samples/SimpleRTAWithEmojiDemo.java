package com.gluonhq.richtextarea.samples;

import com.gluonhq.emoji.Emoji;
import com.gluonhq.emoji.EmojiData;
import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.DecorationModel;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.TextDecoration;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This sample shows how to use the RichTextArea control to render text and
 * emojis.
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
        fullText.append(personText).append(".");

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
    }

    private List<DecorationModel> getDecorations() {
        List<DecorationModel> decorations = new ArrayList<>();
        // decoration for text
        decorations.add(new DecorationModel(0, fullText.length(), preset, parPreset));
        return decorations;
    }

}
