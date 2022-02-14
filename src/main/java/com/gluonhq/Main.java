package com.gluonhq;

import com.gluonhq.richtext.RichTextArea;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    private final Label textLengthLabel = new Label();

    @Override
    public void start(Stage stage) {

        RichTextArea editor = new RichTextArea();
        editor.textLengthProperty().addListener( (o, ov, nv) ->
           textLengthLabel.setText( "Text length: " + nv)
        );

        CheckBox editableProp = new CheckBox("Editable");
        editableProp.selectedProperty().bindBidirectional(editor.editableProperty());
        HBox toolbar = new HBox(editableProp);
        toolbar.setStyle("-fx-padding: 10");

        HBox statusBar = new HBox(10);
        statusBar.setAlignment(Pos.CENTER_RIGHT);
        statusBar.getChildren().setAll(textLengthLabel);
        statusBar.setStyle("-fx-padding: 10");

        BorderPane root = new BorderPane(editor);
        root.setTop(toolbar);
        root.setBottom(statusBar);

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
