package com.gluonhq;

import com.gluonhq.richtext.action.Action;
import com.gluonhq.richtext.RichTextArea;
import com.gluonhq.richtext.action.DecorateAction;
import com.gluonhq.richtext.action.ParagraphDecorateAction;
import com.gluonhq.richtext.action.TextDecorateAction;
import com.gluonhq.richtext.model.DecorationModel;
import com.gluonhq.richtext.model.Document;
import com.gluonhq.richtext.model.ImageDecoration;
import com.gluonhq.richtext.model.ParagraphDecoration;
import com.gluonhq.richtext.model.TextDecoration;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
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
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
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
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static javafx.scene.text.FontPosture.ITALIC;
import static javafx.scene.text.FontPosture.REGULAR;
import static javafx.scene.text.FontWeight.BOLD;
import static javafx.scene.text.FontWeight.NORMAL;

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

    private final List<DecorationModel> decorations;

    {
        TextDecoration bold14 = TextDecoration.builder().presets().fontWeight(BOLD).fontSize(14).build();
        TextDecoration preset = TextDecoration.builder().presets().build();
        ParagraphDecoration center63 = ParagraphDecoration.builder().presets().alignment(TextAlignment.CENTER).topInset(6).bottomInset(3).build();
        ParagraphDecoration justify22 = ParagraphDecoration.builder().presets().alignment(TextAlignment.JUSTIFY).topInset(2).bottomInset(2).build();
        ParagraphDecoration right22 = ParagraphDecoration.builder().presets().alignment(TextAlignment.RIGHT).topInset(2).bottomInset(2).build();
        ParagraphDecoration left535 = ParagraphDecoration.builder().presets().alignment(TextAlignment.LEFT).topInset(5).bottomInset(3).spacing(5).build();
        ParagraphDecoration center42 = ParagraphDecoration.builder().presets().alignment(TextAlignment.CENTER).topInset(4).bottomInset(2).build();
        decorations = List.of(
                new DecorationModel(0, 21, bold14, center63),
                new DecorationModel(21, 575, preset, justify22),
                new DecorationModel(596, 18, bold14, center63),
                new DecorationModel(614, 614, preset, right22),
                new DecorationModel(1228, 25, bold14, center63),
                new DecorationModel(1253, 764, preset, left535),
                new DecorationModel(2017, 295, preset, center42)
        );
    }

    private final Document document = new Document("What is Lorem Ipsum?\n" +
            "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.\n" +
            "Why do we use it?\n" +
            "It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout. The point of using Lorem Ipsum is that it has a more-or-less normal distribution of letters, as opposed to using 'Content here, content here', making it look like readable English. Many desktop publishing packages and web page editors now use Lorem Ipsum as their default model text, and a search for 'lorem ipsum' will uncover many web sites still in their infancy. Various versions have evolved over the years, sometimes by accident, sometimes on purpose (injected humour and the like).\n" +
            "Where does it come from?\n" +
            "Contrary to popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC, making it over 2000 years old. Richard McClintock, a Latin professor at Hampden-Sydney College in Virginia, looked up one of the more obscure Latin words, consectetur, from a Lorem Ipsum passage, and going through the cites of the word in classical literature, discovered the undoubtable source. Lorem Ipsum comes from sections 1.10.32 and 1.10.33 of \"de Finibus Bonorum et Malorum\" (The Extremes of Good and Evil) by Cicero, written in 45 BC. This book is a treatise on the theory of ethics, very popular during the Renaissance. The first line of Lorem Ipsum, \"Lorem ipsum dolor sit amet..\", comes from a line in section 1.10.32.\n" +
            "The standard chunk of Lorem Ipsum used since the 1500s is reproduced below for those interested. Sections 1.10.32 and 1.10.33 from \"de Finibus Bonorum et Malorum\" by Cicero are also reproduced in their exact original form, accompanied by English versions from the 1914 translation by H. Rackham.\n",
            decorations, 2312);

//    private final List<DecorationModel> decorations = List.of(
//                new DecorationModel(0, 12, TextDecoration.builder().presets().build()),
//                new DecorationModel(12, 3, TextDecoration.builder().presets().fontWeight(FontWeight.BOLD).build()),
//                new DecorationModel(15, 1, new ImageDecoration(Main.class.getResource("gluon_logo-150x150@2x.png").toURI().toString(), 32, 32)),
//                new DecorationModel(16, 11, TextDecoration.builder().presets().fontPosture(FontPosture.ITALIC).build()),
//                new DecorationModel(27, 15, TextDecoration.builder().presets().foreground(Color.RED).build())
//        );
//
//    private final Document document = new Document("Simple text one\u200b two three\nExtra line text", decorations, 42);

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
        fontFamilies.setValue("Serif");
        fontFamilies.setPrefWidth(100);
        new TextDecorateAction<>(editor, fontFamilies.valueProperty(), TextDecoration::getFontFamily, (builder, a) -> builder.fontFamily(a).build());

        final ComboBox<Double> fontSize = new ComboBox<>();
        fontSize.setEditable(true);
        fontSize.setPrefWidth(60);
        fontSize.getItems().addAll(IntStream.range(1, 100)
                .filter(i -> i % 2 == 0 || i < 18)
                .asDoubleStream().boxed().collect(Collectors.toList()));
        new TextDecorateAction<>(editor, fontSize.valueProperty(), TextDecoration::getFontSize, (builder, a) -> builder.fontSize(a).build());
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
        fontSize.setValue(12.0);

        final ColorPicker textForeground = new ColorPicker();
        textForeground.getStyleClass().add("foreground");
        new TextDecorateAction<>(editor, textForeground.valueProperty(), TextDecoration::getForeground, (builder, a) -> builder.foreground(a).build());
        textForeground.setValue(Color.BLACK);

        final ColorPicker textBackground = new ColorPicker();
        textBackground.getStyleClass().add("background");
        new TextDecorateAction<>(editor, textBackground.valueProperty(), TextDecoration::getBackground, (builder, a) -> builder.background(a).build());
        textBackground.setValue(Color.TRANSPARENT);

        CheckBox editableProp = new CheckBox("Editable");
        editableProp.selectedProperty().bindBidirectional(editor.editableProperty());

        ToolBar toolbar = new ToolBar();
        toolbar.getItems().setAll(
                actionButton(LineAwesomeSolid.FILE,  editor.getActionFactory().newDocument()),
                actionButton(LineAwesomeSolid.FOLDER_OPEN, editor.getActionFactory().open(document)),
                actionButton(LineAwesomeSolid.SAVE,  editor.getActionFactory().save()),
                new Separator(Orientation.VERTICAL),
                actionButton(LineAwesomeSolid.CUT,   editor.getActionFactory().cut()),
                actionButton(LineAwesomeSolid.COPY,  editor.getActionFactory().copy()),
                actionButton(LineAwesomeSolid.PASTE, editor.getActionFactory().paste()),
                new Separator(Orientation.VERTICAL),
                actionButton(LineAwesomeSolid.UNDO,  editor.getActionFactory().undo()),
                actionButton(LineAwesomeSolid.REDO,  editor.getActionFactory().redo()),
                new Separator(Orientation.VERTICAL),
                actionImage(LineAwesomeSolid.IMAGE),
                new Separator(Orientation.VERTICAL),
                fontFamilies,
                fontSize,
                createToggleButton(LineAwesomeSolid.BOLD, property -> new TextDecorateAction<>(editor, property, d -> d.getFontWeight() == BOLD, (builder, a) -> builder.fontWeight(a ? BOLD : NORMAL).build())),
                createToggleButton(LineAwesomeSolid.ITALIC, property -> new TextDecorateAction<>(editor, property, d -> d.getFontPosture() == ITALIC, (builder, a) -> builder.fontPosture(a ? ITALIC : REGULAR).build())),
                createToggleButton(LineAwesomeSolid.STRIKETHROUGH, property -> new TextDecorateAction<>(editor, property, TextDecoration::isStrikethrough, (builder, a) -> builder.strikethrough(a).build())),
                createToggleButton(LineAwesomeSolid.UNDERLINE, property -> new TextDecorateAction<>(editor, property, TextDecoration::isUnderline, (builder, a) -> builder.underline(a).build())),
                textForeground,
                textBackground,
                new Separator(Orientation.VERTICAL),
                editableProp);

        ToolBar paragraphToolbar = new ToolBar();
        paragraphToolbar.getItems().setAll(
                createToggleButton(LineAwesomeSolid.ALIGN_LEFT, property -> new ParagraphDecorateAction<>(editor, property, d -> d.getAlignment() == TextAlignment.LEFT, (builder, a) -> builder.alignment(TextAlignment.LEFT).build())),
                createToggleButton(LineAwesomeSolid.ALIGN_CENTER, property -> new ParagraphDecorateAction<>(editor, property, d -> d.getAlignment() == TextAlignment.CENTER, (builder, a) -> builder.alignment(TextAlignment.CENTER).build())),
                createToggleButton(LineAwesomeSolid.ALIGN_RIGHT, property -> new ParagraphDecorateAction<>(editor, property, d -> d.getAlignment() == TextAlignment.RIGHT, (builder, a) -> builder.alignment(TextAlignment.RIGHT).build())),
                createToggleButton(LineAwesomeSolid.ALIGN_JUSTIFY, property -> new ParagraphDecorateAction<>(editor, property, d -> d.getAlignment() == TextAlignment.JUSTIFY, (builder, a) -> builder.alignment(TextAlignment.JUSTIFY).build())),
                new Separator(Orientation.VERTICAL),
                createSpinner("Spacing", p -> new ParagraphDecorateAction<>(editor, p, v -> (int) v.getSpacing(), (builder, a) -> builder.spacing(a).build())),
                new Separator(Orientation.VERTICAL),
                createSpinner("Top", p -> new ParagraphDecorateAction<>(editor, p, v -> (int) v.getTopInset(), (builder, a) -> builder.topInset(a).build())),
                createSpinner("Right", p -> new ParagraphDecorateAction<>(editor, p, v -> (int) v.getRightInset(), (builder, a) -> builder.rightInset(a).build())),
                createSpinner("Bottom", p -> new ParagraphDecorateAction<>(editor, p, v -> (int) v.getBottomInset(), (builder, a) -> builder.bottomInset(a).build())),
                createSpinner("Left", p -> new ParagraphDecorateAction<>(editor, p, v -> (int) v.getLeftInset(), (builder, a) -> builder.leftInset(a).build())),
                new Separator(Orientation.VERTICAL)
        );

        HBox statusBar = new HBox(10);
        statusBar.getStyleClass().add("status-bar");
        statusBar.setAlignment(Pos.CENTER_RIGHT);
        statusBar.getChildren().setAll(textLengthLabel);

        Menu fileMenu = new Menu("File");
        fileMenu.getItems().addAll(
                actionMenuItem("New Text", LineAwesomeSolid.FILE, editor.getActionFactory().newDocument()),
                actionMenuItem("Open Text", LineAwesomeSolid.FOLDER_OPEN, editor.getActionFactory().open(document)),
                new SeparatorMenuItem(),
                actionMenuItem("Save Text", LineAwesomeSolid.SAVE, editor.getActionFactory().save()));
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
        root.setTop(new VBox(menuBar, toolbar, paragraphToolbar));
        root.setBottom(statusBar);

        Scene scene = new Scene(root, 960, 580);
        scene.getStylesheets().add(Main.class.getResource("main.css").toExternalForm());
        stage.titleProperty().bind(Bindings.createStringBinding(() -> "Rich Text Demo" + (editor.isModified() ? " *" : ""), editor.modifiedProperty()));
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

    private ToggleButton createToggleButton(Ikon ikon, Function<ObjectProperty<Boolean>, DecorateAction<Boolean>> function) {
        final ToggleButton toggleButton = new ToggleButton();
        FontIcon icon = new FontIcon(ikon);
        icon.setIconSize(20);
        toggleButton.setGraphic(icon);
        function.apply(toggleButton.selectedProperty().asObject());
        return toggleButton;
    }

    private HBox createSpinner(String text, Function<ObjectProperty<Integer>, DecorateAction<Integer>> function) {
        Spinner<Integer> spinner = new Spinner<>();
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20);
        spinner.setValueFactory(valueFactory);
        spinner.setPrefWidth(80);
        spinner.setEditable(false);
        function.apply(valueFactory.valueProperty());
        HBox spinnerBox = new HBox(5, new Label(text), spinner);
        spinnerBox.setAlignment(Pos.CENTER);
        return spinnerBox;
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



