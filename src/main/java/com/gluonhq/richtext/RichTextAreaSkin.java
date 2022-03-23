package com.gluonhq.richtext;

import com.gluonhq.richtext.model.Document;
import com.gluonhq.richtext.model.Paragraph;
import com.gluonhq.richtext.model.PieceTable;
import com.gluonhq.richtext.model.TextBuffer;
import com.gluonhq.richtext.model.TextDecoration;
import com.gluonhq.richtext.viewmodel.ActionCmd;
import com.gluonhq.richtext.viewmodel.ActionCmdFactory;
import com.gluonhq.richtext.viewmodel.RichTextAreaViewModel;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
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
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.gluonhq.richtext.viewmodel.RichTextAreaViewModel.Direction;
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
        entry( new KeyCodeCombination(BACK_SPACE, SHIFT_ANY),                                e -> ACTION_CMD_FACTORY.removeText(-1)),
        entry( new KeyCodeCombination(DELETE),                                               e -> ACTION_CMD_FACTORY.removeText(0)),
        entry( new KeyCodeCombination(B, SHORTCUT_DOWN),                                     e -> {
            TextDecoration decoration = (TextDecoration) viewModel.getDecoration();
            FontWeight fontWeight = decoration.getFontWeight() == BOLD ? NORMAL : BOLD;
            return ACTION_CMD_FACTORY.decorateText(TextDecoration.builder().fromDecoration(decoration).fontWeight(fontWeight).build());
        }),
        entry( new KeyCodeCombination(I, SHORTCUT_DOWN),                                    e -> {
            TextDecoration decoration = (TextDecoration) viewModel.getDecoration();
            FontPosture fontPosture = decoration.getFontPosture() == ITALIC ? REGULAR : ITALIC;
            return ACTION_CMD_FACTORY.decorateText(TextDecoration.builder().fromDecoration(decoration).fontPosture(fontPosture).build());
        })
    );

    private final ParagraphListView paragraphListView;

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

    private final ObservableSet<Paragraph> paragraphSet = FXCollections.observableSet();
    private final SetChangeListener<Paragraph> paragraphsChangeListener;

    private final Consumer<TextBuffer.Event> textChangeListener = e -> refreshTextFlow();
    int lastValidCaretPosition = -1;
    int dragStart = -1;
    int anchorIndex = -1;

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
    }

    protected RichTextAreaSkin(final RichTextArea control) {
        super(control);

        paragraphListView = new ParagraphListView(control);
        paragraphListView.setFocusTraversable(false);
        getChildren().add(paragraphListView);
        paragraphListView.setCellFactory(p -> new RichListCell(this));
        paragraphsChangeListener = c -> {
            if (c.wasAdded()) {
                paragraphListView.getItems().add(c.getElementAdded());
            } else if (c.wasRemoved()) {
                paragraphListView.getItems().remove(c.getElementRemoved());
            }
        };
        objectsCacheEvictionTimer = new SmartTimer(paragraphListView::evictUnusedObjects, 1000, 60000);
        controlPrefWidthListener = (obs, ov, nv) -> {
            refreshTextFlow();
            paragraphListView.updateLayout();
        };

        // all listeners have to be removed within dispose method
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
        viewModel.removeChangeListener(textChangeListener);
        viewModel.documentProperty().removeListener(documentChangeListener);
        lastValidCaretPosition = -1;
        getSkinnable().editableProperty().removeListener(this::editableChangeListener);
        getSkinnable().textLengthProperty.unbind();
        getSkinnable().modifiedProperty.unbind();
        getSkinnable().setOnKeyPressed(null);
        getSkinnable().setOnKeyTyped(null);
        getSkinnable().widthProperty().removeListener(controlPrefWidthListener);
        paragraphSet.removeListener(paragraphsChangeListener);
        paragraphSet.clear();
        paragraphListView.getItems().clear();
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
        viewModel.setTextBuffer(new PieceTable(document));
        lastValidCaretPosition = document.getCaretPosition();
        viewModel.setCaretPosition(lastValidCaretPosition);
        viewModel.addChangeListener(textChangeListener);
        viewModel.setDocument(document);
        viewModel.documentProperty().addListener(documentChangeListener);
        getSkinnable().textLengthProperty.bind(viewModel.textLengthProperty());
        getSkinnable().modifiedProperty.bind(viewModel.savedProperty().not());
        getSkinnable().setOnContextMenuRequested(contextMenuEventEventHandler);
        getSkinnable().editableProperty().addListener(this::editableChangeListener);
        getSkinnable().setOnKeyPressed(this::keyPressedListener);
        getSkinnable().setOnKeyTyped(this::keyTypedListener);
        getSkinnable().widthProperty().addListener(controlPrefWidthListener);
        paragraphSet.addListener(paragraphsChangeListener);
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
            List<Paragraph> paragraphList = buildParagraphs();
            paragraphSet.removeIf(p -> !paragraphList.contains(p));
            paragraphSet.addAll(paragraphList);

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

    private List<Paragraph> buildParagraphs() {
        List<Integer> lineFeeds = viewModel.getTextBuffer().getLineFeeds();
        List<Paragraph> paragraphList = new ArrayList<>();
        AtomicInteger pos = new AtomicInteger();
        lineFeeds.forEach(lfPos -> {
            paragraphList.add(new Paragraph(pos.getAndSet(lfPos), pos.incrementAndGet()));
        });
        if (pos.get() < viewModel.getTextLength()) {
            paragraphList.add(new Paragraph(pos.get(), viewModel.getTextLength()));
        }
        return paragraphList;
    }

    private void editableChangeListener(Observable o) {
        boolean editable = getSkinnable().isEditable();
        viewModel.setEditable(editable);
        viewModel.setCaretPosition(editable ? lastValidCaretPosition : -1);
        paragraphListView.setCursor(editable ? Cursor.TEXT : Cursor.DEFAULT);

        populateContextMenu(editable);
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
        int paragraphWithCaretIndex = items.stream()
                .filter(p -> p.getStart() <= caretPosition && caretPosition < p.getEnd())
                .mapToInt(items::indexOf)
                .findFirst()
                .orElse(-1);
        int nextRowPosition = paragraphListView.getNextRowPosition(x, down);
        // when the caret is at the top or bottom of the paragraph, the
        // next row position is the same
        if (nextRowPosition == viewModel.getCaretPosition()) {
            if (down) {
                // move to next paragraph
                int nextIndex = Math.min(items.size() - 1, paragraphWithCaretIndex + 1);
                Paragraph nextParagraph = items.get(nextIndex);
                return nextParagraph.getStart();
            } else {
                // move to previous paragraph
                int prevIndex = Math.max(0, paragraphWithCaretIndex - 1);
                Paragraph prevParagraph = items.get(prevIndex);
                return Math.max(0, prevParagraph.getEnd() - 1);
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
                execute(INPUT_MAP.get(kc).apply(e));
                e.consume();
                return;
            }
        }
    }

    private void keyTypedListener(KeyEvent e) {
        if (isCharOnly(e)) {
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

}