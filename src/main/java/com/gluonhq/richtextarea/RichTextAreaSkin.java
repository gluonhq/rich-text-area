package com.gluonhq.richtextarea;

import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.ImageDecoration;
import com.gluonhq.richtextarea.model.Paragraph;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.PieceTable;
import com.gluonhq.richtextarea.model.TextBuffer;
import com.gluonhq.richtextarea.model.TextDecoration;
import com.gluonhq.richtextarea.viewmodel.ActionCmd;
import com.gluonhq.richtextarea.viewmodel.ActionCmdFactory;
import com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.control.skin.ListViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.image.Image;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel.Direction;
import static java.util.Map.entry;
import static javafx.scene.input.KeyCode.A;
import static javafx.scene.input.KeyCode.B;
import static javafx.scene.input.KeyCode.BACK_SPACE;
import static javafx.scene.input.KeyCode.C;
import static javafx.scene.input.KeyCode.DELETE;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.END;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.HOME;
import static javafx.scene.input.KeyCode.I;
import static javafx.scene.input.KeyCode.LEFT;
import static javafx.scene.input.KeyCode.RIGHT;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import static javafx.scene.input.KeyCode.V;
import static javafx.scene.input.KeyCode.X;
import static javafx.scene.input.KeyCode.Z;
import static javafx.scene.input.KeyCombination.ALT_ANY;
import static javafx.scene.input.KeyCombination.CONTROL_ANY;
import static javafx.scene.input.KeyCombination.SHIFT_ANY;
import static javafx.scene.input.KeyCombination.SHIFT_DOWN;
import static javafx.scene.input.KeyCombination.SHORTCUT_ANY;
import static javafx.scene.input.KeyCombination.SHORTCUT_DOWN;
import static javafx.scene.text.FontPosture.ITALIC;
import static javafx.scene.text.FontPosture.REGULAR;
import static javafx.scene.text.FontWeight.BOLD;
import static javafx.scene.text.FontWeight.NORMAL;

public class RichTextAreaSkin extends SkinBase<RichTextArea> {

    interface ActionBuilder extends Function<KeyEvent, ActionCmd>{}

    // TODO need to find a better way to find next row caret position
    private final RichTextAreaViewModel viewModel = new RichTextAreaViewModel(this::getNextRowPosition);

    private static final ActionCmdFactory ACTION_CMD_FACTORY = new ActionCmdFactory();

    private final Map<KeyCombination, ActionBuilder> INPUT_MAP = Map.ofEntries(
        entry( new KeyCodeCombination(RIGHT, SHIFT_ANY, ALT_ANY, CONTROL_ANY, SHORTCUT_ANY), e -> ACTION_CMD_FACTORY.caretMove(Direction.FORWARD, e)),
        entry( new KeyCodeCombination(LEFT,  SHIFT_ANY, ALT_ANY, CONTROL_ANY, SHORTCUT_ANY), e -> ACTION_CMD_FACTORY.caretMove(Direction.BACK, e)),
        entry( new KeyCodeCombination(DOWN,  SHIFT_ANY, ALT_ANY, SHORTCUT_ANY),              e -> ACTION_CMD_FACTORY.caretMove(Direction.DOWN, e)),
        entry( new KeyCodeCombination(UP,    SHIFT_ANY, ALT_ANY, SHORTCUT_ANY),              e -> ACTION_CMD_FACTORY.caretMove(Direction.UP, e)),
        entry( new KeyCodeCombination(HOME,  SHIFT_ANY),                                     e -> ACTION_CMD_FACTORY.caretMove(Direction.FORWARD, e.isShiftDown(), false, true)),
        entry( new KeyCodeCombination(END,   SHIFT_ANY),                                     e -> ACTION_CMD_FACTORY.caretMove(Direction.BACK, e.isShiftDown(), false, true)),
        entry( new KeyCodeCombination(A, SHORTCUT_DOWN),                                     e -> ACTION_CMD_FACTORY.selectAll()),
        entry( new KeyCodeCombination(C, SHORTCUT_DOWN),                                     e -> ACTION_CMD_FACTORY.copy()),
        entry( new KeyCodeCombination(X, SHORTCUT_DOWN),                                     e -> ACTION_CMD_FACTORY.cut()),
        entry( new KeyCodeCombination(V, SHORTCUT_DOWN),                                     e -> ACTION_CMD_FACTORY.paste()),
        entry( new KeyCodeCombination(Z, SHORTCUT_DOWN),                                     e -> ACTION_CMD_FACTORY.undo()),
        entry( new KeyCodeCombination(Z, SHORTCUT_DOWN, SHIFT_DOWN),                         e -> ACTION_CMD_FACTORY.redo()),
        entry( new KeyCodeCombination(ENTER, SHIFT_ANY),                                     e -> ACTION_CMD_FACTORY.insertText("\n")),
        entry( new KeyCodeCombination(BACK_SPACE, SHIFT_ANY),                                e -> {
            int caret = viewModel.getCaretPosition();
            return viewModel.getParagraphWithCaret()
                    .filter(p -> p.getStart() == caret)
                    .map(p -> {
                        ParagraphDecoration decoration = viewModel.getDecorationAtParagraph();
                        if (decoration.getGraphicType() != ParagraphDecoration.GraphicType.NONE) {
                            return ACTION_CMD_FACTORY.decorateParagraph(ParagraphDecoration.builder().fromDecoration(decoration).graphicType(ParagraphDecoration.GraphicType.NONE).build());
                        }
                        return null;
                    })
                    .orElse(ACTION_CMD_FACTORY.removeText(-1));
        }),
        entry( new KeyCodeCombination(DELETE),                                               e -> ACTION_CMD_FACTORY.removeText(0)),
        entry( new KeyCodeCombination(B, SHORTCUT_DOWN),                                     e -> {
            TextDecoration decoration = (TextDecoration) viewModel.getDecorationAtCaret();
            FontWeight fontWeight = decoration.getFontWeight() == BOLD ? NORMAL : BOLD;
            return ACTION_CMD_FACTORY.decorateText(TextDecoration.builder().fromDecoration(decoration).fontWeight(fontWeight).build());
        }),
        entry(new KeyCodeCombination(I, SHORTCUT_DOWN),                                      e -> {
            TextDecoration decoration = (TextDecoration) viewModel.getDecorationAtCaret();
            FontPosture fontPosture = decoration.getFontPosture() == ITALIC ? REGULAR : ITALIC;
            return ACTION_CMD_FACTORY.decorateText(TextDecoration.builder().fromDecoration(decoration).fontPosture(fontPosture).build());
        }),
        entry(new KeyCodeCombination(TAB, SHIFT_ANY),                                        e -> {
            ParagraphDecoration decoration = viewModel.getDecorationAtParagraph();
            if (decoration.getGraphicType() != ParagraphDecoration.GraphicType.NONE) {
                int level = Math.max(decoration.getIndentationLevel() + (e.isShiftDown() ? -1 : 1), 0);
                return ACTION_CMD_FACTORY.decorateParagraph(ParagraphDecoration.builder().fromDecoration(decoration).indentationLevel(level).build());
            }
            return null;
        })
    );

    private final ParagraphListView paragraphListView;
    private final SortedList<Paragraph> paragraphSortedList = new SortedList<>(viewModel.getParagraphList(), Comparator.comparing(Paragraph::getStart));

    final ContextMenu contextMenu = new ContextMenu();
    private ObservableList<MenuItem> editableContextMenuItems;
    private ObservableList<MenuItem> nonEditableContextMenuItems;
    private final EventHandler<ContextMenuEvent> contextMenuEventEventHandler = e -> {
        contextMenu.show((Node) e.getSource(), e.getScreenX(), e.getScreenY());
        e.consume();
    };

    private final Map<Integer, Font> fontCache = new ConcurrentHashMap<>();
    private final Map<String, Image> imageCache = new ConcurrentHashMap<>();
    private final SmartTimer objectsCacheEvictionTimer;

    private final Consumer<TextBuffer.Event> textChangeListener = e -> refreshTextFlow();
    int lastValidCaretPosition = -1;
    int mouseDragStart = -1;
    int dragAndDropStart = -1;
    int anchorIndex = -1;
    Paragraph lastParagraph = null;

    final DoubleProperty textFlowPrefWidthProperty = new SimpleDoubleProperty() {
        @Override
        protected void invalidated() {
            if (paragraphListView != null) {
                Platform.runLater(paragraphListView::updateLayout);
            }
        }
    };
    private final ChangeListener<Number> controlPrefWidthListener;
    private int nonTextNodesCount;
    AtomicInteger nonTextNodes = new AtomicInteger();

    private final ChangeListener<Document> documentChangeListener = (obs, ov, nv) -> {
        if (ov == null && nv != null) {
            // new/open
            dispose();
            setup(nv);
            getSkinnable().setDocument(nv);
        } else if (nv != null) {
            // save
            getSkinnable().setDocument(nv);
        }
    };

    private final ChangeListener<Number> caretChangeListener;
    private final InvalidationListener focusListener;
    private final EventHandler<DragEvent> dndHandler = this::dndListener;

    private class RichVirtualFlow extends VirtualFlow<ListCell<Paragraph>> {

        RichVirtualFlow(RichTextArea control) {
            ReadOnlyObjectProperty<Bounds> clippedBounds = lookup(".clipped-container").layoutBoundsProperty();
            textFlowPrefWidthProperty.bind(Bindings.createDoubleBinding(() -> control.getContentAreaWidth() > 0 ?
                    control.getContentAreaWidth() :
                            clippedBounds.get().getWidth() > 0 ? clippedBounds.get().getWidth() - 10 : -1,
                    control.contentAreaWidthProperty(), clippedBounds));
        }

        @Override
        protected void rebuildCells() {
            super.rebuildCells();
        }
    }

    private class ParagraphListView extends ListView<Paragraph> {

        private final RichVirtualFlow virtualFlow;

        public ParagraphListView(RichTextArea control) {
            virtualFlow = new RichVirtualFlow(control);
            getStyleClass().setAll("paragraph-list-view");
        }

        @Override
        protected Skin<?> createDefaultSkin() {
            return new ListViewSkin<>(this) {
                @Override
                protected VirtualFlow<ListCell<Paragraph>> createVirtualFlow() {
                    return virtualFlow;
                }
            };
        }

        void evictUnusedObjects() {
            ((Group) virtualFlow.lookup(".sheet")).getChildren().stream()
                    .filter(RichListCell.class::isInstance)
                    .map(RichListCell.class::cast)
                    .forEach(RichListCell::evictUnusedObjects);
        }

        int getNextRowPosition(double x, boolean down) {
            return ((Group) virtualFlow.lookup(".sheet")).getChildren().stream()
                    .filter(RichListCell.class::isInstance)
                    .map(RichListCell.class::cast)
                    .filter(RichListCell::hasCaret)
                    .mapToInt(cell -> cell.getNextRowPosition(x, down))
                    .findFirst()
                    .orElse(-1);
        }

        void updateLayout() {
            // force updateItem call to recalculate backgroundPath positions
            virtualFlow.rebuildCells();
        }

        void scrollIfNeeded() {
            final Bounds vfBounds = virtualFlow.localToScene(virtualFlow.getBoundsInLocal());
            double viewportMinY = vfBounds.getMinY();
            double viewportMaxY = vfBounds.getMaxY();
            virtualFlow.lookupAll(".caret").stream()
                    .filter(Path.class::isInstance)
                    .map(Path.class::cast)
                    .filter(path -> !path.getElements().isEmpty())
                    .findFirst()
                    .ifPresentOrElse(caret -> {
                            final Bounds bounds = caret.localToScene(caret.getBoundsInLocal());
                            double minY = bounds.getMinY();
                            double maxY = bounds.getMaxY();
                            if (!(maxY <= viewportMaxY && minY >= viewportMinY)) {
                                // If caret is not fully visible, scroll line by line as needed
                                virtualFlow.scrollPixels(maxY > viewportMaxY ?
                                        maxY - viewportMaxY + 1 : minY - viewportMinY - 1);
                            }
                        }, () -> {
                            // In case no caret was found (paragraph is not in a listCell yet),
                            // scroll directly to the paragraph
                            viewModel.getParagraphWithCaret().ifPresent(this::scrollTo);
                        });
        }
    }

    protected RichTextAreaSkin(final RichTextArea control) {
        super(control);

        paragraphListView = new ParagraphListView(control);
        paragraphListView.setItems(paragraphSortedList);
        paragraphListView.setFocusTraversable(false);
        getChildren().add(paragraphListView);
        paragraphListView.setCellFactory(p -> new RichListCell(this));
        objectsCacheEvictionTimer = new SmartTimer(paragraphListView::evictUnusedObjects, 1000, 60000);
        controlPrefWidthListener = (obs, ov, nv) -> {
            refreshTextFlow();
            paragraphListView.updateLayout();
        };

        caretChangeListener = (obs, ov, nv) -> viewModel.getParagraphWithCaret()
                .ifPresent(paragraph -> Platform.runLater(paragraphListView::scrollIfNeeded));
        focusListener = o -> paragraphListView.updateLayout();

        control.documentProperty().addListener((obs, ov, nv) -> {
            if (viewModel.isSaved()) {
                getSkinnable().requestFocus();
                return;
            }
            if (ov != null) {
                dispose();
            }
            setup(nv);
        });
        setup(control.getDocument());
    }

    /// PROPERTIES ///////////////////////////////////////////////////////////////


    /// PUBLIC METHODS  /////////////////////////////////////////////////////////

    @Override
    public void dispose() {
        viewModel.clearSelection();
        viewModel.caretPositionProperty().removeListener(caretChangeListener);
        viewModel.removeChangeListener(textChangeListener);
        viewModel.documentProperty().removeListener(documentChangeListener);
        viewModel.autoSaveProperty().unbind();
        lastValidCaretPosition = -1;
        getSkinnable().editableProperty().removeListener(this::editableChangeListener);
        getSkinnable().textLengthProperty.unbind();
        getSkinnable().modifiedProperty.unbind();
        getSkinnable().setOnKeyPressed(null);
        getSkinnable().setOnKeyTyped(null);
        getSkinnable().widthProperty().removeListener(controlPrefWidthListener);
        getSkinnable().focusedProperty().removeListener(focusListener);
        getSkinnable().removeEventHandler(DragEvent.ANY, dndHandler);
        contextMenu.getItems().clear();
        editableContextMenuItems = null;
        nonEditableContextMenuItems = null;
    }

    public RichTextAreaViewModel getViewModel() {
        return viewModel;
    }

    Map<Integer, Font> getFontCache() {
        return fontCache;
    }

    Map<String, Image> getImageCache() {
        return imageCache;
    }

    /// PRIVATE METHODS /////////////////////////////////////////////////////////

    private void setup(Document document) {
        if (document == null) {
            return;
        }
        viewModel.caretPositionProperty().addListener(caretChangeListener);
        viewModel.setTextBuffer(new PieceTable(document));
        lastValidCaretPosition = document.getCaretPosition();
        viewModel.setCaretPosition(lastValidCaretPosition);
        viewModel.addChangeListener(textChangeListener);
        viewModel.setDocument(document);
        viewModel.documentProperty().addListener(documentChangeListener);
        viewModel.autoSaveProperty().bind(getSkinnable().autoSaveProperty());
        getSkinnable().textLengthProperty.bind(viewModel.textLengthProperty());
        getSkinnable().modifiedProperty.bind(viewModel.savedProperty().not());
        getSkinnable().setOnContextMenuRequested(contextMenuEventEventHandler);
        getSkinnable().editableProperty().addListener(this::editableChangeListener);
        getSkinnable().setOnKeyPressed(this::keyPressedListener);
        getSkinnable().setOnKeyTyped(this::keyTypedListener);
        getSkinnable().widthProperty().addListener(controlPrefWidthListener);
        getSkinnable().focusedProperty().addListener(focusListener);
        getSkinnable().addEventHandler(DragEvent.ANY, dndHandler);
        refreshTextFlow();
        requestLayout();
        editableChangeListener(null); // sets up all related listeners
    }

    // TODO Need more optimal way of rendering text fragments.
    //  For now rebuilding the whole text flow
    private void refreshTextFlow() {
        objectsCacheEvictionTimer.pause();
        try {
            nonTextNodes.set(0);
            viewModel.resetCharacterIterator();
            lastParagraph = paragraphSortedList.get(paragraphSortedList.size() - 1);
            // this ensures changes in decoration are applied:
            paragraphListView.updateLayout();

            if (nonTextNodesCount != nonTextNodes.get()) {
                // when number of images changes, caret
                requestLayout();
                nonTextNodesCount = nonTextNodes.get();
            }
            getSkinnable().requestFocus();
        } finally {
            objectsCacheEvictionTimer.start();
        }
    }

    private void editableChangeListener(Observable o) {
        boolean editable = getSkinnable().isEditable();
        viewModel.setEditable(editable);
        viewModel.setCaretPosition(editable ? lastValidCaretPosition : -1);
        paragraphListView.setCursor(editable ? Cursor.TEXT : Cursor.DEFAULT);
        populateContextMenu(editable);
        Platform.runLater(paragraphListView::scrollIfNeeded);
    }

    private void requestLayout() {
        paragraphListView.refresh();
        getSkinnable().requestLayout();
    }

    // So far the only way to find prev/next row location is to use the size of the caret,
    // which always has the height of the row. Adding line spacing to it allows us to find a point which
    // belongs to the desired row. Then using the `hitTest` we can find the related caret position.
    private int getNextRowPosition(double x, boolean down) {
        ObservableList<Paragraph> items = paragraphListView.getItems();
        int caretPosition = viewModel.getCaretPosition();
        int nextRowPosition = Math.min(viewModel.getTextLength(), paragraphListView.getNextRowPosition(x, down));
        // if the caret is at the top or bottom of the paragraph:
        if ((down && nextRowPosition <= caretPosition) ||
                (!down && nextRowPosition >= caretPosition)) {
            int paragraphWithCaretIndex = items.stream()
                    .filter(p -> p.getStart() <= caretPosition &&
                            caretPosition < (p.equals(lastParagraph) ? p.getEnd() + 1 : p.getEnd()))
                    .mapToInt(items::indexOf)
                    .findFirst()
                    .orElse(-1);
            if (down) {
                // move to beginning of next paragraph or end
                int nextIndex = Math.min(items.size() - 1, paragraphWithCaretIndex + 1);
                Paragraph nextParagraph = items.get(nextIndex);
                return items.indexOf(nextParagraph) != paragraphWithCaretIndex ?
                        nextParagraph.getStart() : viewModel.getTextLength();
            } else {
                // move to end of previous paragraph or home
                int prevIndex = Math.max(0, paragraphWithCaretIndex - 1);
                Paragraph prevParagraph = items.get(prevIndex);
                return items.indexOf(prevParagraph) != paragraphWithCaretIndex ?
                        Math.max(0, prevParagraph.getEnd() - 1) : 0;
            }
        }
        return nextRowPosition;
    }

    private static boolean isPrintableChar(char c) {
        Character.UnicodeBlock changeBlock = Character.UnicodeBlock.of(c);
        return (c == '\n' || c == '\t' || !Character.isISOControl(c)) &&
                !KeyEvent.CHAR_UNDEFINED.equals(String.valueOf(c)) &&
                changeBlock != null && changeBlock != Character.UnicodeBlock.SPECIALS;
    }

    private static boolean isCharOnly(KeyEvent e) {
        char c = e.getCharacter().isEmpty()? 0: e.getCharacter().charAt(0);
        return isPrintableChar(c) &&
               !e.isControlDown() &&
               !e.isMetaDown() &&
               !e.isAltDown();
    }

    private void execute(ActionCmd action) {
        Objects.requireNonNull(action).apply(viewModel);
    }

    private void keyPressedListener(KeyEvent e) {
        // Find an applicable action and execute it if found
        for (KeyCombination kc : INPUT_MAP.keySet()) {
            if (kc.match(e)) {
                ActionBuilder actionBuilder = INPUT_MAP.get(kc);
                ActionCmd actionCmd = actionBuilder.apply(e);
                if (actionCmd != null) {
                    execute(actionCmd);
                }
                e.consume();
                return;
            }
        }
    }

    private void keyTypedListener(KeyEvent e) {
        if (isCharOnly(e)) {
            if ("\t".equals(e.getCharacter())) {
                ParagraphDecoration decoration = viewModel.getDecorationAtParagraph();
                if (decoration.getGraphicType() != ParagraphDecoration.GraphicType.NONE) {
                    // processed via keyPressedListener
                    e.consume();
                    return;
                }
            }
            if (viewModel.getSelection().isDefined()) {
                execute(ACTION_CMD_FACTORY.removeText(-1));
            }
            execute(ACTION_CMD_FACTORY.insertText(e.getCharacter()));
            e.consume();
        }
    }

    private void populateContextMenu(boolean isEditable) {
        if (isEditable && editableContextMenuItems == null) {
            editableContextMenuItems = FXCollections.observableArrayList(
                    createMenuItem("Undo", ACTION_CMD_FACTORY.undo()),
                    createMenuItem("Redo", ACTION_CMD_FACTORY.redo()),
                    new SeparatorMenuItem(),
                    createMenuItem("Copy", ACTION_CMD_FACTORY.copy()),
                    createMenuItem("Cut", ACTION_CMD_FACTORY.cut()),
                    createMenuItem("Paste", ACTION_CMD_FACTORY.paste()),
                    new SeparatorMenuItem(),
                    createMenuItem("Select All", ACTION_CMD_FACTORY.selectAll()));
        } else if (!isEditable && nonEditableContextMenuItems == null) {
            nonEditableContextMenuItems = FXCollections.observableArrayList(
                    createMenuItem("Copy", ACTION_CMD_FACTORY.copy()),
                    new SeparatorMenuItem(),
                    createMenuItem("Select All", ACTION_CMD_FACTORY.selectAll()));
        }
        contextMenu.getItems().setAll(isEditable ? editableContextMenuItems : nonEditableContextMenuItems);
    }

    private MenuItem createMenuItem(String text, ActionCmd actionCmd) {
        MenuItem menuItem = new MenuItem(text);
        menuItem.disableProperty().bind(actionCmd.getDisabledBinding(viewModel));
        menuItem.setOnAction(e -> actionCmd.apply(viewModel));
        return menuItem;
    }

    private void dndListener(DragEvent dragEvent) {
        if (dragEvent.getEventType() == DragEvent.DRAG_ENTERED) {
            dragAndDropStart = 1;
        } else if (dragEvent.getEventType() == DragEvent.DRAG_DONE || dragEvent.getEventType() == DragEvent.DRAG_EXITED) {
            dragAndDropStart = -1;
        } else if (dragEvent.getEventType() == DragEvent.DRAG_OVER) {
            Dragboard dragboard = dragEvent.getDragboard();
            if (dragboard.hasImage() || dragboard.hasString() || dragboard.hasUrl() | dragboard.hasFiles()) {
                dragEvent.acceptTransferModes(TransferMode.ANY);
            }
        } else if (dragEvent.getEventType() == DragEvent.DRAG_DROPPED) {
            Dragboard dragboard = dragEvent.getDragboard();
            if (!dragboard.getFiles().isEmpty()) {
                dragboard.getFiles().forEach(file -> {
                    String url = file.toURI().toString();
                    // validate image before adding it
                    if (url != null && new Image(url).getException() == null) {
                        ACTION_CMD_FACTORY.decorateImage(new ImageDecoration(url)).apply(viewModel);
                    }
                });
            } else if (dragboard.hasUrl()) {
                String url = dragboard.getUrl();
                // validate if url is an image before adding it:
                if (url != null) {
                    if (new Image(url).getException() == null) {
                        ACTION_CMD_FACTORY.decorateImage(new ImageDecoration(url)).apply(viewModel);
                    } else {
                        // add hyperlink to text, makes a selection at the current word where is dropped
                        viewModel.selectCurrentWord();
                        if (!viewModel.getSelection().isDefined()) {
                            int caret = viewModel.getCaretPosition();
                            ACTION_CMD_FACTORY.insertText(url).apply(viewModel);
                            viewModel.setSelection(new Selection(caret, caret + url.length()));
                        }
                        ACTION_CMD_FACTORY.decorateText(TextDecoration.builder().url(url).build()).apply(viewModel);
                    }
                }
            } else if (dragboard.hasString()) {
                ACTION_CMD_FACTORY.insertText(dragboard.getString()).apply(viewModel);
            }
            requestLayout();
            dragAndDropStart = -1;
        }
    }

}