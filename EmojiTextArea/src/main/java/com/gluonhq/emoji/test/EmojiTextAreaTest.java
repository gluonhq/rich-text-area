package com.gluonhq.emoji.test;

import com.gluonhq.emoji.control.EmojiTextArea;
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
        test.setOnAction(e -> textArea.setText("His there"));
        borderPane.setBottom(textArea);
        final Scene scene = new Scene(borderPane, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static void main(String[] args) {
        Application.launch();
    }
}
