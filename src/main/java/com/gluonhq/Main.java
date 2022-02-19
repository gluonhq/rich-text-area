package com.gluonhq;

import com.gluonhq.richtext.Action;
import com.gluonhq.richtext.RichTextArea;
import com.gluonhq.richtext.model.TextDecoration;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.lineawesome.LineAwesomeRegular;
import org.kordamp.ikonli.lineawesome.LineAwesomeSolid;

public class Main extends Application {

    private final Label textLengthLabel = new Label();
    private final RichTextArea editor = new RichTextArea();

    @Override
    public void start(Stage stage) {

        editor.textLengthProperty().addListener( (o, ov, nv) ->
           textLengthLabel.setText( "Text length: " + nv)
        );

        CheckBox editableProp = new CheckBox("Editable");
        editableProp.selectedProperty().bindBidirectional(editor.editableProperty());

        ToolBar toolbar = new ToolBar();
        toolbar.getItems().setAll(
                actionButton(LineAwesomeSolid.CUT,   editor.getActionFactory().cut()),
                actionButton(LineAwesomeSolid.COPY,  editor.getActionFactory().copy()),
                actionButton(LineAwesomeSolid.PASTE, editor.getActionFactory().paste()),
                new Separator(Orientation.VERTICAL),
                actionButton(LineAwesomeSolid.BOLD, editor.getActionFactory().decorateText(TextDecoration.builder().fontWeight(FontWeight.BOLD).build())),
                actionButton(LineAwesomeSolid.ITALIC, editor.getActionFactory().decorateText(TextDecoration.builder().fontPosture(FontPosture.ITALIC).build())),
                new Separator(Orientation.VERTICAL),
                editableProp);

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

    private Button actionButton(Ikon ikon, Action action) {
        Button button = new Button();
        FontIcon icon  = new FontIcon(ikon);
        icon.setIconSize(20);
        button.setGraphic( icon );
        button.setOnAction(e->editor.execute(action));
        return button;
    }


    public static void main(String[] args) {
        launch(args);
    }
}



