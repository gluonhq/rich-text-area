package com.gluonhq;

import com.gluonhq.richtext.RichTextArea;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        RichTextArea editor = new RichTextArea();

        BorderPane root = new BorderPane(editor);

        CheckBox editableProp = new CheckBox("Editable");
        editableProp.selectedProperty().bindBidirectional(editor.editableProperty());
        VBox props = new VBox(
            editableProp
        );
        props.setStyle("-fx-padding: 10");

        root.setRight( props );

        Scene scene = new Scene( root, 640, 480);
        stage.setTitle("Rich Text Demo");
        stage.setScene(scene);
        stage.show();

        editor.requestFocus();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
