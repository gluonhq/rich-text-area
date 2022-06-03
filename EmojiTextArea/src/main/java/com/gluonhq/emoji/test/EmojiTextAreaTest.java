package com.gluonhq.emoji.test;

import com.gluonhq.emoji.control.EmojiTextArea;
import com.gluonhq.emoji.popup.EmojiSkinTone;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class EmojiTextAreaTest extends Application {

    @Override
    public void start(Stage primaryStage) {
        final BorderPane borderPane = new BorderPane();
        Button test = new Button("Test");
        final StackPane stackPane = new StackPane(test);
        stackPane.setAlignment(Pos.CENTER);
        borderPane.setCenter(stackPane);
        EmojiTextArea textArea = new EmojiTextArea();
        textArea.setSkinTone(EmojiSkinTone.MEDIUM_SKIN_TONE);
        test.setOnAction(e -> textArea.setText("Text and \uD83D\uDE00"));
        borderPane.setBottom(textArea);
        final Scene scene = new Scene(borderPane, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static void main(String[] args) {
        Application.launch();
    }
}
