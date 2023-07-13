/*
 * Copyright (c) 2022, 2023, Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GLUON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.richtextarea.samples;

import com.gluonhq.emoji.Emoji;
import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.action.Action;
import com.gluonhq.richtextarea.action.DecorateAction;
import com.gluonhq.richtextarea.action.ParagraphDecorateAction;
import com.gluonhq.richtextarea.action.TextDecorateAction;
import com.gluonhq.richtextarea.model.DecorationModel;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.ImageDecoration;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.TableDecoration;
import com.gluonhq.richtextarea.model.TextDecoration;
import com.gluonhq.richtextarea.samples.popup.EmojiPopup;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.gluonhq.richtextarea.model.ParagraphDecoration.GraphicType.BULLETED_LIST;
import static com.gluonhq.richtextarea.model.ParagraphDecoration.GraphicType.NONE;
import static com.gluonhq.richtextarea.model.ParagraphDecoration.GraphicType.NUMBERED_LIST;
import static javafx.scene.text.FontPosture.ITALIC;
import static javafx.scene.text.FontPosture.REGULAR;
import static javafx.scene.text.FontWeight.BOLD;
import static javafx.scene.text.FontWeight.NORMAL;

/**
 * This is an advance sample that shows how to create a rich text editor, by using
 * the RichTextArea control and adding actions for the user interaction, via toolbars and
 * menus, and most of the features of the control are showcased in this sample.
 * <p>
 * For more basic test cases with single features, check the rest of the samples.
 */
public class FullFeaturedDemo extends Application {

    static {
        try (InputStream resourceAsStream = FullFeaturedDemo.class.getResourceAsStream("/logging.properties")) {
            if (resourceAsStream != null) {
                LogManager.getLogManager().readConfiguration(resourceAsStream);
            }
        } catch (IOException ex) {
            Logger.getLogger(FullFeaturedDemo.class.getName()).log(Level.SEVERE, "Error opening logging.properties file", ex);
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
        TableDecoration tdec = new TableDecoration(2, 3, new TextAlignment[][]{{TextAlignment.CENTER, TextAlignment.LEFT, TextAlignment.RIGHT}, {TextAlignment.JUSTIFY, TextAlignment.RIGHT, TextAlignment.CENTER}});
        ParagraphDecoration table = ParagraphDecoration.builder().presets().alignment(TextAlignment.CENTER).tableDecoration(tdec).build();
        decorations = List.of(
                new DecorationModel(0, 21, bold14, center63),
                new DecorationModel(21, 575, preset, justify22),
                new DecorationModel(596, 18, bold14, center63),
                new DecorationModel(614, 614, preset, right22),
                new DecorationModel(1228, 27, bold14, table),
                new DecorationModel(1255, 764, preset, left535),
                new DecorationModel(2019, 295, preset, center42)
        );
    }

    private final Document document = new Document("What is Lorem Ipsum?\n" +
            "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.\n" +
            "Why do we use it?\n" +
            "It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout. The point of using Lorem Ipsum is that it has a more-or-less normal distribution of letters, as opposed to using 'Content here, content here', making it look like readable English. Many desktop publishing packages and web page editors now use Lorem Ipsum as their default model text, and a search for 'lorem ipsum' will uncover many web sites still in their infancy. Various versions have evolved over the years, sometimes by accident, sometimes on purpose (injected humour and the like).\n" +
            "Where does\u200bit\u200bcome\u200bfrom?\u200b\u200b\n" +
            "Contrary to popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC, making it over 2000 years old. Richard McClintock, a Latin professor at Hampden-Sydney College in Virginia, looked up one of the more obscure Latin words, consectetur, from a Lorem Ipsum passage, and going through the cites of the word in classical literature, discovered the undoubtable source. Lorem Ipsum comes from sections 1.10.32 and 1.10.33 of \"de Finibus Bonorum et Malorum\" (The Extremes of Good and Evil) by Cicero, written in 45 BC. This book is a treatise on the theory of ethics, very popular during the Renaissance. The first line of Lorem Ipsum, \"Lorem ipsum dolor sit amet..\", comes from a line in section 1.10.32.\n" +
            "The standard chunk of Lorem Ipsum used since the 1500s is reproduced below for those interested. Sections 1.10.32 and 1.10.33 from \"de Finibus Bonorum et Malorum\" by Cicero are also reproduced in their exact original form, accompanied by English versions from the 1914 translation by H. Rackham.\n",
            decorations, 2314);

    private final Label textLengthLabel = new Label();
    private final RichTextArea editor = new RichTextArea();

    @Override
    public void start(Stage stage) {
        editor.textLengthProperty().addListener( (o, ov, nv) ->
           textLengthLabel.setText( "Text length: " + nv)
        );

        ComboBox<Presets> presets = new ComboBox<>();
        presets.getItems().setAll(Presets.values());
        presets.setValue(Presets.DEFAULT);
        presets.setPrefWidth(100);
        presets.setConverter(new StringConverter<>() {
            @Override
            public String toString(Presets presets) {
                return presets.getName();
            }

            @Override
            public Presets fromString(String s) {
                return Presets.valueOf(s.replaceAll(" ", ""));
            }
        });
        presets.getSelectionModel().selectedItemProperty().addListener((observableValue, ov, nv) -> {
            // presets should define all decoration attributes.
            // For now just they set only font size and weight for text, and alignment for paragraphs,
            // so the rest of attributes come for the Builder::presets
            editor.getActionFactory()
                    .decorate(TextDecoration.builder().presets().fontSize(nv.getFontSize()).fontWeight(nv.getWeight()).build(),
                            ParagraphDecoration.builder().presets().alignment(nv.getTextAlignment()).build()).execute(new ActionEvent());
            editor.requestFocus();
        });

        ComboBox<String> fontFamilies = new ComboBox<>();
        fontFamilies.getItems().setAll(Font.getFamilies());
        fontFamilies.setValue("Arial");
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
        fontSize.setValue(14.0);

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
                actionButton(LineAwesomeSolid.FOLDER_OPEN, editor.getActionFactory()
                        .open(new Document("Emoji: \uD83C\uDFF4\uDB40\uDC67\uDB40\uDC62\uDB40\uDC73\uDB40\uDC63\uDB40\uDC74\uDB40\uDC7F! and \ufeff@name\ufeff!",
                                List.of(new DecorationModel(0, 35,
                                        TextDecoration.builder().presets().build(),
                                        ParagraphDecoration.builder().presets().build())), 35))),
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
                actionEmoji(),
                actionHyperlink(LineAwesomeSolid.LINK),
                actionTable(LineAwesomeSolid.TABLE, td -> editor.getActionFactory().insertTable(td)),
                new Separator(Orientation.VERTICAL));

        ToolBar fontsToolbar = new ToolBar();
        fontsToolbar.getItems().setAll(
                presets,
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
                createToggleButton(LineAwesomeSolid.ALIGN_CENTER, property -> new ParagraphDecorateAction<>(editor, property, d -> d.getAlignment() == TextAlignment.CENTER, (builder, a) -> builder.alignment(a ? TextAlignment.CENTER : TextAlignment.LEFT).build())),
                createToggleButton(LineAwesomeSolid.ALIGN_RIGHT, property -> new ParagraphDecorateAction<>(editor, property, d -> d.getAlignment() == TextAlignment.RIGHT, (builder, a) -> builder.alignment(a ? TextAlignment.RIGHT : TextAlignment.LEFT).build())),
                createToggleButton(LineAwesomeSolid.ALIGN_JUSTIFY, property -> new ParagraphDecorateAction<>(editor, property, d -> d.getAlignment() == TextAlignment.JUSTIFY, (builder, a) -> builder.alignment(a ? TextAlignment.JUSTIFY : TextAlignment.LEFT).build())),
                new Separator(Orientation.VERTICAL),
                createSpinner("Spacing", p -> new ParagraphDecorateAction<>(editor, p, v -> (int) v.getSpacing(), (builder, a) -> builder.spacing(a).build())),
                new Separator(Orientation.VERTICAL),
                createSpinner("Top", p -> new ParagraphDecorateAction<>(editor, p, v -> (int) v.getTopInset(), (builder, a) -> builder.topInset(a).build())),
                createSpinner("Right", p -> new ParagraphDecorateAction<>(editor, p, v -> (int) v.getRightInset(), (builder, a) -> builder.rightInset(a).build())),
                createSpinner("Bottom", p -> new ParagraphDecorateAction<>(editor, p, v -> (int) v.getBottomInset(), (builder, a) -> builder.bottomInset(a).build())),
                createSpinner("Left", p -> new ParagraphDecorateAction<>(editor, p, v -> (int) v.getLeftInset(), (builder, a) -> builder.leftInset(a).build())),
                new Separator(Orientation.VERTICAL),
                createToggleButton(LineAwesomeSolid.LIST_OL, property -> new ParagraphDecorateAction<>(editor, property, d -> d.getGraphicType() == NUMBERED_LIST, (builder, a) -> builder.graphicType(a ? NUMBERED_LIST : NONE).build())),
                createToggleButton(LineAwesomeSolid.LIST_UL, property -> new ParagraphDecorateAction<>(editor, property, d -> d.getGraphicType() == BULLETED_LIST, (builder, a) -> builder.graphicType(a ? BULLETED_LIST : NONE).build())),
                createSpinner("Indent", p -> new ParagraphDecorateAction<>(editor, p, ParagraphDecoration::getIndentationLevel, (builder, a) -> builder.indentationLevel(a).build())),
                new Separator(Orientation.VERTICAL)
        );

        HBox statusBar = new HBox(10);
        statusBar.getStyleClass().add("status-bar");
        statusBar.setAlignment(Pos.CENTER_RIGHT);
        statusBar.getChildren().setAll(textLengthLabel);

        Menu fileMenu = new Menu("File");
        CheckMenuItem autoSaveMenuItem = new CheckMenuItem("Auto Save");
        editor.autoSaveProperty().bind(autoSaveMenuItem.selectedProperty());
        fileMenu.getItems().addAll(
                actionMenuItem("New Text", LineAwesomeSolid.FILE, editor.getActionFactory().newDocument()),
                actionMenuItem("Open Text", LineAwesomeSolid.FOLDER_OPEN, editor.getActionFactory().open(document)),
                new SeparatorMenuItem(),
                autoSaveMenuItem,
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

        editor.setPromptText("Hello!");
        BorderPane root = new BorderPane(editor);
        root.setTop(new VBox(menuBar, toolbar, fontsToolbar, paragraphToolbar));
        root.setBottom(statusBar);

        Scene scene = new Scene(root, 960, 580);
        scene.getStylesheets().add(FullFeaturedDemo.class.getResource("fullFeaturedDemo.css").toExternalForm());
        stage.titleProperty().bind(Bindings.createStringBinding(() -> "Rich Text Editor Demo" + (editor.isModified() ? " *" : ""), editor.modifiedProperty()));
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
        spinner.setPrefWidth(60);
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

    private Button actionEmoji() {
        Region region = new Region();
        region.getStyleClass().addAll("icon", "emoji-outline");
        Button emojiButton = new Button(null, region);
        emojiButton.getStyleClass().add("emoji-button");
        emojiButton.setOnAction(e -> {
            EmojiPopup emojiPopup = new EmojiPopup();
            emojiPopup.setSkinTone(editor.getSkinTone());
            editor.skinToneProperty().bindBidirectional(emojiPopup.skinToneProperty());
            emojiPopup.setOnAction(ev -> {
                Emoji emoji = (Emoji) ev.getSource();
                editor.getActionFactory().insertEmoji(emoji).execute(new ActionEvent());
            });
            emojiPopup.show(emojiButton);
        });
        return emojiButton;
    }

    private Button actionHyperlink(Ikon ikon) {
        Button button = new Button();
        FontIcon icon = new FontIcon(ikon);
        icon.setIconSize(20);
        button.setGraphic(icon);
        button.setOnAction(e -> {
            final Dialog<String> hyperlinkDialog = createHyperlinkDialog();
            Optional<String> result = hyperlinkDialog.showAndWait();
            result.ifPresent(textURL -> {
                editor.getActionFactory().decorate(TextDecoration.builder().url(textURL).build()).execute(e);
            });
        });
        return button;
    }

    private Dialog<String> createHyperlinkDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Hyperlink");

        // Set the button types
        ButtonType textButtonType = new ButtonType("Create", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(textButtonType);

        // Create the text and url labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField url = new TextField();
        url.setPromptText("URL");

        grid.add(new Label("URL:"), 0, 1);
        grid.add(url, 1, 1);

        Node loginButton = dialog.getDialogPane().lookupButton(textButtonType);
        loginButton.setDisable(true);
        loginButton.disableProperty().bind(url.textProperty().isEmpty());

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == textButtonType) {
                return url.getText();
            }
            return null;
        });

        // Request focus on the username field by default.
        dialog.setOnShown(e -> Platform.runLater(url::requestFocus));

        return dialog;
    }

    private Button actionTable(Ikon ikon, Function<TableDecoration, Action> actionFunction) {
        Button button = new Button();
        FontIcon icon = new FontIcon(ikon);
        icon.setIconSize(20);
        button.setGraphic(icon);
        button.disableProperty().bind(actionFunction.apply(null).disabledProperty());
        button.setOnAction(e -> {
            final Dialog<TableDecoration> tableDialog = insertTableDialog();
            Optional<TableDecoration> result = tableDialog.showAndWait();
            result.ifPresent(td -> actionFunction.apply(td).execute(e));
        });
        return button;
    }

    private Dialog<TableDecoration> insertTableDialog() {
        Dialog<TableDecoration> dialog = new Dialog<>();
        dialog.setTitle("Insert table");

        // Set the button types
        ButtonType textButtonType = new ButtonType("Create", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(textButtonType);

        // Create the text and rows labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField rows = new TextField();
        rows.setPromptText("Rows ");

        grid.add(new Label("Rows:"), 0, 1);
        grid.add(rows, 1, 1);

        TextField cols = new TextField();
        cols.setPromptText("Columns ");

        grid.add(new Label("Columns:"), 0, 2);
        grid.add(cols, 1, 2);

        Node tableButton = dialog.getDialogPane().lookupButton(textButtonType);
        tableButton.setDisable(true);
        tableButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> {
                    if (rows.getText().isEmpty() || cols.getText().isEmpty()) {
                        return true;
                    }
                    try {
                        Integer.parseInt(rows.getText());
                        Integer.parseInt(cols.getText());
                    } catch (NumberFormatException nfe) {
                        return true;
                    }
                    return false;
                },
                rows.textProperty(), cols.textProperty()));

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == textButtonType) {
                int r = Integer.parseInt(rows.getText());
                int c = Integer.parseInt(cols.getText());
                return new TableDecoration(r, c); // TODO: add cell alignment
            }
            return null;
        });

        // Request focus on the username field by default.
        dialog.setOnShown(e -> Platform.runLater(rows::requestFocus));

        return dialog;
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

    private enum Presets {

        DEFAULT("Default",12, NORMAL, TextAlignment.LEFT),
        HEADER1("Header 1", 32, BOLD, TextAlignment.CENTER),
        HEADER2("Header 2", 24, BOLD, TextAlignment.LEFT),
        HEADER3("Header 3", 19, BOLD, TextAlignment.LEFT);

        private final String name;
        private final int fontSize;
        private final FontWeight weight;
        private final TextAlignment textAlignment;

        Presets(String name, int fontSize, FontWeight weight, TextAlignment textAlignment) {
            this.name = name;
            this.fontSize = fontSize;
            this.weight = weight;
            this.textAlignment = textAlignment;
        }

        public String getName() {
            return name;
        }

        public int getFontSize() {
            return fontSize;
        }

        public FontWeight getWeight() {
            return weight;
        }

        public TextAlignment getTextAlignment() {
            return textAlignment;
        }
    }
}



