package com.gluonhq;

import com.gluonhq.richtext.action.Action;
import com.gluonhq.richtext.RichTextArea;
import com.gluonhq.richtext.FaceModel;
import com.gluonhq.richtext.action.ActionFactory;
import com.gluonhq.richtext.model.TextDecoration;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
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
        final ActionFactory actionFactory = editor.getActionFactory();

        ToggleGroup alignmentToggleGroup = new ToggleGroup();

        ComboBox<String> fontFamilies = new ComboBox<>();
        fontFamilies.setPrefWidth(100);
        fontFamilies.getItems().setAll(Font.getFamilies());
        fontFamilies.setValue("Arial");
        fontFamilies.setOnAction(e -> actionFactory.decorate(
                TextDecoration.builder().fontFamily(fontFamilies.getSelectionModel().getSelectedItem()).build()).execute(e));

        final ComboBox<Double> fontSize = new ComboBox<>();
        fontSize.setEditable(true);
        fontSize.setPrefWidth(60);
        fontSize.getItems().addAll(IntStream.range(1, 100)
                .filter(i -> i % 2 == 0 || i < 10)
                .asDoubleStream().boxed().collect(Collectors.toList()));
        fontSize.setValue(17.0);
        fontSize.setOnAction(e -> actionFactory.decorate(
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
        textForeground.setOnAction(e -> actionFactory.decorate(
                TextDecoration.builder().foreground(textForeground.getValue()).build()).execute(e));

        final ColorPicker textBackground = new ColorPicker();
        textBackground.getStyleClass().add("background");
        textBackground.setOnAction(e -> actionFactory.decorate(
                TextDecoration.builder().background(textBackground.getValue()).build()).execute(e));

        CheckBox editableProp = new CheckBox("Editable");
        editableProp.selectedProperty().bindBidirectional(editor.editableProperty());

        ToolBar toolbar = new ToolBar();
        toolbar.getItems().setAll(
                actionButton(LineAwesomeSolid.CUT,   actionFactory.cut()),
                actionButton(LineAwesomeSolid.COPY,  actionFactory.copy()),
                actionButton(LineAwesomeSolid.PASTE, actionFactory.paste()),
                new Separator(Orientation.VERTICAL),
                actionButton(LineAwesomeSolid.UNDO, actionFactory.undo()),
                actionButton(LineAwesomeSolid.REDO, actionFactory.redo()),
                new Separator(Orientation.VERTICAL),
                actionToggleButton(LineAwesomeSolid.ALIGN_LEFT, alignmentToggleGroup, actionFactory.align(TextAlignment.LEFT)),
                actionToggleButton(LineAwesomeSolid.ALIGN_CENTER, alignmentToggleGroup, actionFactory.align(TextAlignment.CENTER)),
                actionToggleButton(LineAwesomeSolid.ALIGN_RIGHT, alignmentToggleGroup, actionFactory.align(TextAlignment.RIGHT)),
                actionToggleButton(LineAwesomeSolid.ALIGN_JUSTIFY, alignmentToggleGroup, actionFactory.align(TextAlignment.JUSTIFY)),
                new Separator(Orientation.VERTICAL),
                fontFamilies,
                fontSize,
                actionButton(LineAwesomeSolid.BOLD, actionFactory.decorate(TextDecoration.builder().fontWeight(FontWeight.BOLD).build())),
                actionButton(LineAwesomeSolid.ITALIC, actionFactory.decorate(TextDecoration.builder().fontPosture(FontPosture.ITALIC).build())),
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
        Menu alignmentMenu = new Menu("Alignment");
        alignmentMenu.getItems().addAll(
                actionMenuItem("Left", LineAwesomeSolid.ALIGN_LEFT, actionFactory.align(TextAlignment.LEFT)),
                actionMenuItem("Center", LineAwesomeSolid.ALIGN_CENTER, actionFactory.align(TextAlignment.CENTER)),
                actionMenuItem("Right", LineAwesomeSolid.ALIGN_RIGHT, actionFactory.align(TextAlignment.RIGHT)),
                actionMenuItem("Justify", LineAwesomeSolid.ALIGN_JUSTIFY, actionFactory.align(TextAlignment.JUSTIFY))
        );
        editMenu.getItems().addAll(
                actionMenuItem("Undo", LineAwesomeSolid.UNDO, actionFactory.undo()),
                actionMenuItem("Redo", LineAwesomeSolid.REDO, actionFactory.redo()),
                new SeparatorMenuItem(),
                actionMenuItem("Copy", LineAwesomeSolid.COPY, actionFactory.copy()),
                actionMenuItem("Cut", LineAwesomeSolid.CUT, actionFactory.cut()),
                actionMenuItem("Paste", LineAwesomeSolid.PASTE, actionFactory.paste()),
                new SeparatorMenuItem(),
                alignmentMenu);
        MenuBar menuBar = new MenuBar(fileMenu, editMenu);
//        menuBar.setUseSystemMenuBar(true);

        BorderPane root = new BorderPane(editor);
        root.setTop(new VBox(menuBar, toolbar));
        root.setBottom(statusBar);

        Scene scene = new Scene(root, 850, 480);
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

    private ToggleButton actionToggleButton(Ikon ikon, ToggleGroup group, Action action) {
        ToggleButton toggleButton = new ToggleButton();
        toggleButton.setToggleGroup(group);
        FontIcon icon = new FontIcon(ikon);
        icon.setIconSize(20);
        toggleButton.setGraphic(icon);
        toggleButton.disableProperty().bind(action.disabledProperty());
        toggleButton.setOnAction(action::execute);
        return toggleButton;
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



