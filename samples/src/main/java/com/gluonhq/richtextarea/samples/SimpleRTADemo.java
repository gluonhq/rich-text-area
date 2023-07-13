package com.gluonhq.richtextarea.samples;

import com.gluonhq.richtextarea.RichTextArea;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Sample with the Rich Text Area control, showing a prompt message.
 */
public class SimpleRTADemo extends Application {

    @Override
    public void start(Stage stage) {
        RichTextArea editor = new RichTextArea();
        editor.setPadding(new Insets(20));
        editor.setPromptText("Type something!");
        BorderPane root = new BorderPane(editor);
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle("RichTextArea");

        stage.show();
    }

}
