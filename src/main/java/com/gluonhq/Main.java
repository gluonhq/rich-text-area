package com.gluonhq;

import com.gluonhq.richtext.action.Action;
import com.gluonhq.richtext.RichTextArea;
import com.gluonhq.richtext.model.DecorationModel;
import com.gluonhq.richtext.model.FaceModel;
import com.gluonhq.richtext.model.ImageDecoration;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.lineawesome.LineAwesomeSolid;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
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

    private final List<DecorationModel> decorations = List.of(
                new DecorationModel(0, 12, TextDecoration.builder().presets().build()),
                new DecorationModel(12, 3, TextDecoration.builder().presets().fontWeight(FontWeight.BOLD).build()),
                new DecorationModel(15, 1, new ImageDecoration(Main.class.getResource("gluon_logo-150x150@2x.png").toURI().toString(), 32, 32)),
                new DecorationModel(16, 11, TextDecoration.builder().presets().fontPosture(FontPosture.ITALIC).build()),
                new DecorationModel(27, 15, TextDecoration.builder().presets().foreground(Color.RED).build())
        );

    private final FaceModel faceModel = new FaceModel("Simple text one\u200b two three\nExtra line text", decorations, 42);

    private final Label textLengthLabel = new Label();
    private final RichTextArea editor = new RichTextArea();

    public Main() throws URISyntaxException {
    }

    @Override
    public void start(Stage stage) {

        editor.textLengthProperty().addListener( (o, ov, nv) ->
           textLengthLabel.setText( "Text length: " + nv)
        );

        ComboBox<String> fontFamilies = new ComboBox<>();
        fontFamilies.getItems().setAll(Font.getFamilies());
        fontFamilies.setValue("Arial");
        fontFamilies.setOnAction(e -> editor.getActionFactory().decorate(
                TextDecoration.builder().fontFamily(fontFamilies.getSelectionModel().getSelectedItem()).build()).execute(e));

        final ComboBox<Double> fontSize = new ComboBox<>();
        fontSize.setEditable(true);
        fontSize.setPrefWidth(60);
        fontSize.getItems().addAll(IntStream.range(1, 100)
                .filter(i -> i % 2 == 0 || i < 10)
                .asDoubleStream().boxed().collect(Collectors.toList()));
        fontSize.setValue(17.0);
        fontSize.setOnAction(e -> editor.getActionFactory().decorate(
                TextDecoration.builder().fontSize(fontSize.getValue()).build()).execute(e));
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
        textForeground.setOnAction(e -> editor.getActionFactory().decorate(
                TextDecoration.builder().foreground(textForeground.getValue()).build()).execute(e));

        final ColorPicker textBackground = new ColorPicker();
        textBackground.getStyleClass().add("background");
        textBackground.setOnAction(e -> editor.getActionFactory().decorate(
                TextDecoration.builder().background(textBackground.getValue()).build()).execute(e));

        CheckBox editableProp = new CheckBox("Editable");
        editableProp.selectedProperty().bindBidirectional(editor.editableProperty());

        ToolBar toolbar = new ToolBar();
        toolbar.getItems().setAll(
                actionButton(LineAwesomeSolid.CUT,   editor.getActionFactory().cut()),
                actionButton(LineAwesomeSolid.COPY,  editor.getActionFactory().copy()),
                actionButton(LineAwesomeSolid.PASTE, editor.getActionFactory().paste()),
                new Separator(Orientation.VERTICAL),
                actionButton(LineAwesomeSolid.UNDO, editor.getActionFactory().undo()),
                actionButton(LineAwesomeSolid.REDO, editor.getActionFactory().redo()),
                new Separator(Orientation.VERTICAL),
                actionImage(LineAwesomeSolid.IMAGE),
                new Separator(Orientation.VERTICAL),
                fontFamilies,
                fontSize,
                actionButton(LineAwesomeSolid.BOLD, editor.getActionFactory().decorate(TextDecoration.builder().fontWeight(FontWeight.BOLD).build())),
                actionButton(LineAwesomeSolid.ITALIC, editor.getActionFactory().decorate(TextDecoration.builder().fontPosture(FontPosture.ITALIC).build())),
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
        Menu editMenu = new Menu("Edit");
        editMenu.getItems().addAll(
                actionMenuItem("Undo", LineAwesomeSolid.UNDO, editor.getActionFactory().undo()),
                actionMenuItem("Redo", LineAwesomeSolid.REDO, editor.getActionFactory().redo()),
                new SeparatorMenuItem(),
                actionMenuItem("Copy", LineAwesomeSolid.COPY, editor.getActionFactory().copy()),
                actionMenuItem("Cut", LineAwesomeSolid.CUT, editor.getActionFactory().cut()),
                actionMenuItem("Paste", LineAwesomeSolid.PASTE, editor.getActionFactory().paste()));
        MenuBar menuBar = new MenuBar(fileMenu, editMenu);
//        menuBar.setUseSystemMenuBar(true);

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
        FontIcon icon = new FontIcon(ikon);
        icon.setIconSize(20);
        button.setGraphic(icon);
        button.disableProperty().bind(action.disabledProperty());
        button.setOnAction(action::execute);
        return button;
    }

    private Button actionImage(Ikon ikon) {
        Button button = new Button();
        FontIcon icon = new FontIcon(ikon);
        icon.setIconSize(20);
        button.setGraphic(icon);
        button.setOnAction(e -> {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image", "*.png", ".jpeg", ".gif"));
            File file = fileChooser.showOpenDialog(button.getScene().getWindow());
            if (file != null) {
                String url = file.toURI().toString();
                editor.getActionFactory().decorate(new ImageDecoration(url)).execute(e);
            }
        });
        return button;
    }

    private MenuItem actionMenuItem(String text, Ikon ikon, Action action) {
        FontIcon icon = new FontIcon(ikon);
        icon.setIconSize(16);
        MenuItem menuItem = new MenuItem(text, icon);
        menuItem.disableProperty().bind(action.disabledProperty());
        menuItem.setOnAction(action::execute);
        return menuItem;
    }

    public static void main(String[] args) {
        launch(args);
    }
}



