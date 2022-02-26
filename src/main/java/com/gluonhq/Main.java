package com.gluonhq;

import com.gluonhq.richtext.Action;
import com.gluonhq.richtext.RichTextArea;
import com.gluonhq.richtext.FaceModel;
import com.gluonhq.richtext.model.TextDecoration;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.lineawesome.LineAwesomeSolid;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main extends Application {

    static {
        try (InputStream resourceAsStream = Main.class.getResourceAsStream("/logging.properties")) {
            if (resourceAsStream != null) {
                LogManager.getLogManager().readConfiguration(resourceAsStream);
            }
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Error opening logging.properties file", ex);
        }
    }

    private final List<FaceModel.Decoration> decorations = List.of(
                new FaceModel.Decoration(0, 12, TextDecoration.builder().presets().build()),
                new FaceModel.Decoration(12, 3, TextDecoration.builder().presets().fontWeight(FontWeight.BOLD).build()),
                new FaceModel.Decoration(15, 11, TextDecoration.builder().presets().fontPosture(FontPosture.ITALIC).build()),
                new FaceModel.Decoration(26, 15, TextDecoration.builder().presets().foreground(Color.RED).build())
        );

    private final FaceModel faceModel = new FaceModel("Simple text one two three\nExtra line text", decorations, 41);

    private final Label textLengthLabel = new Label();
    private final RichTextArea editor = new RichTextArea();

    @Override
    public void start(Stage stage) {

        editor.textLengthProperty().addListener( (o, ov, nv) ->
           textLengthLabel.setText( "Text length: " + nv)
        );

        ComboBox<String> fontFamilies = new ComboBox<>();
        fontFamilies.getItems().setAll(Font.getFamilies());
        fontFamilies.setValue("Arial");
        fontFamilies.setOnAction(e -> {
            String ff = fontFamilies.getSelectionModel().getSelectedItem();
            Action action = editor.getActionFactory().decorateText(TextDecoration.builder().fontFamily(ff).build());
            editor.execute(action);
        });

        final ComboBox<Double> fontSize = new ComboBox<>();
        fontSize.setEditable(true);
        fontSize.setPrefWidth(60);
        fontSize.getItems().addAll(IntStream.range(1, 100)
                .filter(i -> i % 2 == 0 || i < 10)
                .asDoubleStream().boxed().collect(Collectors.toList()));
        fontSize.setValue(17.0);
        fontSize.setOnAction(e -> {
            final Double fontSizeValue = fontSize.getValue();
            final Action action = editor.getActionFactory().decorateText(TextDecoration.builder().fontSize(fontSizeValue).build());
            editor.execute(action);
        });
        fontSize.setConverter(new StringConverter<>() {
            @Override
            public String toString(Double aDouble) {
                return Integer.toString(aDouble.intValue());
            }

            @Override
            public Double fromString(String s) {
                return Double.parseDouble(s);
            }
        });

        final ColorPicker textForeground = new ColorPicker();
        textForeground.getStyleClass().add("foreground");
        textForeground.setOnAction(e -> {
            Action action = editor.getActionFactory().decorateText(TextDecoration.builder()
                    .foreground(textForeground.getValue())
                    .build());
            editor.execute(action);
        });

        final ColorPicker textBackground = new ColorPicker();
        textBackground.getStyleClass().add("background");
        textBackground.setOnAction(e -> {
            Action action = editor.getActionFactory().decorateText(TextDecoration.builder()
                    .background(textBackground.getValue())
                    .build());
            editor.execute(action);
        });

        CheckBox editableProp = new CheckBox("Editable");
        editableProp.selectedProperty().bindBidirectional(editor.editableProperty());

        Button undoButton = actionButton(LineAwesomeSolid.UNDO, editor.getActionFactory().undo());
        undoButton.disableProperty().bind(editor.undoStackEmptyProperty());
        Button redoButton = actionButton(LineAwesomeSolid.REDO, editor.getActionFactory().redo());
        redoButton.disableProperty().bind(editor.redoStackEmptyProperty());

        ToolBar toolbar = new ToolBar();
        toolbar.getItems().setAll(
                actionButton(LineAwesomeSolid.CUT,   editor.getActionFactory().cut()),
                actionButton(LineAwesomeSolid.COPY,  editor.getActionFactory().copy()),
                actionButton(LineAwesomeSolid.PASTE, editor.getActionFactory().paste()),
                new Separator(Orientation.VERTICAL),
                undoButton,
                redoButton,
                new Separator(Orientation.VERTICAL),
                fontFamilies,
                fontSize,
                actionButton(LineAwesomeSolid.BOLD, editor.getActionFactory().decorateText(TextDecoration.builder().fontWeight(FontWeight.BOLD).build())),
                actionButton(LineAwesomeSolid.ITALIC, editor.getActionFactory().decorateText(TextDecoration.builder().fontPosture(FontPosture.ITALIC).build())),
                textForeground,
                textBackground,
                new Separator(Orientation.VERTICAL),
                editableProp);

        HBox statusBar = new HBox(10);
        statusBar.getStyleClass().add("status-bar");
        statusBar.setAlignment(Pos.CENTER_RIGHT);
        statusBar.getChildren().setAll(textLengthLabel);

        MenuItem newFileMenu = new MenuItem("New Text");
        newFileMenu.setOnAction(e -> editor.setFaceModel(new FaceModel()));
        MenuItem openFileMenu = new MenuItem("Open Text");
        // For now, just load a decorated text
        openFileMenu.setOnAction(e -> editor.setFaceModel(faceModel));

        Menu fileMenu = new Menu("File");
        fileMenu.getItems().addAll(newFileMenu, openFileMenu);
        MenuBar menuBar = new MenuBar(fileMenu);
        menuBar.setUseSystemMenuBar(true);

        BorderPane root = new BorderPane(editor);
        root.setTop(new VBox(menuBar, toolbar));
        root.setBottom(statusBar);

        Scene scene = new Scene(root, 800, 480);
        scene.getStylesheets().add(Main.class.getResource("main.css").toExternalForm());
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



