/*
 * Copyright (c) 2022, 2025, Gluon
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
package com.gluonhq.richtextarea;

import com.gluonhq.emoji.EmojiSkinTone;
import com.gluonhq.richtextarea.model.Decoration;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.ImageDecoration;
import com.gluonhq.richtextarea.model.Paragraph;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.PieceTable;
import com.gluonhq.richtextarea.model.Table;
import com.gluonhq.richtextarea.model.TableDecoration;
import com.gluonhq.richtextarea.model.TextBuffer;
import com.gluonhq.richtextarea.model.TextDecoration;
import com.gluonhq.richtextarea.model.UnitBuffer;
import com.gluonhq.richtextarea.viewmodel.ActionCmd;
import com.gluonhq.richtextarea.viewmodel.ActionCmdFactory;
import com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel.Direction;
import static java.util.Map.entry;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import static javafx.scene.input.KeyCombination.ALT_DOWN;
import static javafx.scene.input.KeyCombination.CONTROL_ANY;
import static javafx.scene.input.KeyCombination.SHIFT_ANY;
import static javafx.scene.input.KeyCombination.SHORTCUT_ANY;
import static javafx.scene.input.KeyCombination.SHORTCUT_DOWN;
import static javafx.scene.text.FontPosture.ITALIC;
import static javafx.scene.text.FontPosture.REGULAR;
import static javafx.scene.text.FontWeight.BOLD;
import static javafx.scene.text.FontWeight.NORMAL;

public class RichTextAreaSkin extends SkinBase<RichTextArea> {

    static final Logger LOG = Logger.getLogger(RichTextAreaSkin.class.getName());
    
    interface ActionBuilder extends Function<KeyEvent, ActionCmd>{}

    // TODO need to find a better way to find next row caret position
    private final RichTextAreaViewModel viewModel = new RichTextAreaViewModel(this::getNextRowPosition, this::getNextTableCellPosition);

    public static final double DEFAULT_FONT_SIZE = 14;

    private static final ActionCmdFactory ACTION_CMD_FACTORY = new ActionCmdFactory();

    private final Map<KeyCombination, ActionBuilder> INPUT_MAP = Map.ofEntries(
        entry( new KeyCodeCombination(RIGHT, SHIFT_ANY, ALT_ANY, CONTROL_ANY, SHORTCUT_ANY), e -> ACTION_CMD_FACTORY.caretMove(Direction.FORWARD, e)),
        entry( new KeyCodeCombination(LEFT,  SHIFT_ANY, ALT_ANY, CONTROL_ANY, SHORTCUT_ANY), e -> ACTION_CMD_FACTORY.caretMove(Direction.BACK, e)),
        entry( new KeyCodeCombination(DOWN,  SHIFT_ANY, ALT_ANY, SHORTCUT_ANY),              e -> ACTION_CMD_FACTORY.caretMove(Direction.DOWN, e)),
        entry( new KeyCodeCombination(UP,    SHIFT_ANY, ALT_ANY, SHORTCUT_ANY),              e -> ACTION_CMD_FACTORY.caretMove(Direction.UP, e)),
        entry( new KeyCodeCombination(HOME,  SHIFT_ANY),                                     e -> ACTION_CMD_FACTORY.caretMove(Direction.BACK, e.isShiftDown(), false, true)),
        entry( new KeyCodeCombination(END,   SHIFT_ANY),                                     e -> ACTION_CMD_FACTORY.caretMove(Direction.FORWARD, e.isShiftDown(), false, true)),
        entry( new KeyCodeCombination(A, SHORTCUT_DOWN),                                     e -> ACTION_CMD_FACTORY.selectAll()),
        entry( new KeyCodeCombination(C, SHORTCUT_DOWN),                                     e -> ACTION_CMD_FACTORY.copy()),
        entry( new KeyCodeCombination(X, SHORTCUT_DOWN),                                     e -> ACTION_CMD_FACTORY.cut()),
        entry( new KeyCodeCombination(V, SHORTCUT_DOWN),                                     e -> ACTION_CMD_FACTORY.paste()),
        entry( new KeyCodeCombination(Z, SHORTCUT_DOWN, SHIFT_ANY),                          e -> e.isShiftDown() ? ACTION_CMD_FACTORY.redo() : ACTION_CMD_FACTORY.undo()),
        entry( new KeyCodeCombination(ENTER, SHIFT_ANY),                                     e -> {
            ParagraphDecoration decoration = viewModel.getDecorationAtParagraph();
            Paragraph paragraph = viewModel.getParagraphWithCaret().orElse(null);
            if (decoration != null && decoration.getGraphicType() != ParagraphDecoration.GraphicType.NONE) {
                int level = decoration.getIndentationLevel();
                if (level > 0 && paragraph != null && viewModel.isEmptyParagraph(paragraph)) {
                    // on empty paragraphs, Enter is the same as shift+tab
                    return ACTION_CMD_FACTORY.decorate(ParagraphDecoration.builder().fromDecoration(decoration).indentationLevel(level - 1).build());
                }
            } else if (paragraph != null && paragraph.getStart() < paragraph.getEnd() &&
                    decoration != null && decoration.hasTableDecoration()) {
                int caretPosition = viewModel.getCaretPosition();
                UnitBuffer buffer = new UnitBuffer();
                viewModel.walkFragments((u, d) -> buffer.append(u), paragraph.getStart(), paragraph.getEnd());
                Table table = new Table(buffer,
                        paragraph.getStart(), decoration.getTableDecoration().getRows(), decoration.getTableDecoration().getColumns());
                // move up/down rows
                int nextCaretAt = table.getCaretAtNextRow(caretPosition, e.isShiftDown() ? Direction.UP : Direction.DOWN);
                viewModel.setCaretPosition(nextCaretAt);
                if (nextCaretAt == 0 || nextCaretAt == viewModel.getTextLength()) {
                    // insert new line before/after the table and reset decoration
                    return ACTION_CMD_FACTORY.insertAndDecorate("\n", ParagraphDecoration.builder().presets().build());
                }
                return null;
            }
            if (getSkinnable().getOnAction() != null && !e.isShiftDown()) {
                getSkinnable().getOnAction().handle(new ActionEvent());
                return null;
            }
            return ACTION_CMD_FACTORY.insertText("\n");
        }),
        entry( new KeyCodeCombination(BACK_SPACE, SHIFT_ANY),                                e -> {
            int caret = viewModel.getCaretPosition();
            Paragraph paragraph = viewModel.getParagraphWithCaret().orElse(null);
            ParagraphDecoration decoration = viewModel.getDecorationAtParagraph();
            if (decoration != null && paragraph != null) {
                if (decoration.hasTableDecoration()) {
                    UnitBuffer buffer = new UnitBuffer();
                    viewModel.walkFragments((u, d) -> buffer.append(u), paragraph.getStart(), paragraph.getEnd());
                    Table table = new Table(buffer,
                            paragraph.getStart(), decoration.getTableDecoration().getRows(), decoration.getTableDecoration().getColumns());
                    if (table.isCaretAtStartOfCell(caret)) {
                        // check backspace at beginning of each cell to prevent moving text from one cell to the other.
                        // and just move caret if cell was empty:
                        if (table.isCaretAtEmptyCell(caret)) {
                            return ACTION_CMD_FACTORY.caretMove(Direction.BACK, false, false, false);
                        }
                        return null;
                    }
                } else if (paragraph.getStart() == caret) {
                    // check backspace at beginning of paragraph:
                    if (decoration.getGraphicType() != ParagraphDecoration.GraphicType.NONE) {
                        // remove graphic type decoration
                        return ACTION_CMD_FACTORY.decorate(ParagraphDecoration.builder().fromDecoration(decoration).graphicType(ParagraphDecoration.GraphicType.NONE).build());
                    } else if (decoration.getIndentationLevel() > 0) {
                        // decrease indentation level
                        return ACTION_CMD_FACTORY.decorate(ParagraphDecoration.builder().fromDecoration(decoration).indentationLevel(decoration.getIndentationLevel() - 1).build());
                    } else {
                        // if previous paragraph is a table:
                        int index = viewModel.getParagraphList().indexOf(paragraph);
                        if (index > 0) {
                            if (viewModel.getParagraphList().get(index - 1).getDecoration().hasTableDecoration()) {
                                // just move to last cell
                                return ACTION_CMD_FACTORY.caretMove(Direction.BACK, false, false, false);
                            }
                        }
                    }
                }
            }
            return ACTION_CMD_FACTORY.removeText(-1);
        }),
        entry( new KeyCodeCombination(BACK_SPACE, SHORTCUT_DOWN, SHIFT_ANY),                 e -> {
            int caret = viewModel.getCaretPosition();
            Paragraph paragraph = viewModel.getParagraphWithCaret().orElse(null);
            ParagraphDecoration decoration = viewModel.getDecorationAtParagraph();
            if (paragraph != null && decoration != null && decoration.hasTableDecoration()) {
                // TODO: remove cell content, else if empty move to prev cell
                return null;
            } else if (paragraph != null && paragraph.getStart() == caret) {
                // if previous paragraph is a table:
                int index = viewModel.getParagraphList().indexOf(paragraph);
                if (index > 0) {
                    if (viewModel.getParagraphList().get(index - 1).getDecoration().hasTableDecoration()) {
                        // just move to last cell
                        return ACTION_CMD_FACTORY.caretMove(Direction.BACK, false, false, false);
                    }
                }
            }
            if (Tools.MAC) {
                // CMD + BACKSPACE or CMD + SHIFT + BACKSPACE removes line in Mac
                return ACTION_CMD_FACTORY.removeText(0, RichTextAreaViewModel.Remove.LINE);
            }
            // CTRL + BACKSPACE removes word in Windows and Linux
            // SHIFT + CTRL + BACKSPACE removes line in Windows and Linux
            return ACTION_CMD_FACTORY.removeText(0, e.isShiftDown() ? RichTextAreaViewModel.Remove.LINE : RichTextAreaViewModel.Remove.WORD);
        }),
        entry( new KeyCodeCombination(BACK_SPACE, ALT_DOWN),                                 e -> {
            if (Tools.MAC) {
                int caret = viewModel.getCaretPosition();
                Paragraph paragraph = viewModel.getParagraphWithCaret().orElse(null);
                ParagraphDecoration decoration = viewModel.getDecorationAtParagraph();
                if (paragraph != null && decoration != null && decoration.hasTableDecoration()) {
                    // TODO: remove prev word from cell if any, else if empty move to prev cell, else nothing
                    return null;
                } else if (paragraph != null && paragraph.getStart() == caret) {
                    // if previous paragraph is a table:
                    int index = viewModel.getParagraphList().indexOf(paragraph);
                    if (index > 0) {
                        if (viewModel.getParagraphList().get(index - 1).getDecoration().hasTableDecoration()) {
                            // just move to last cell
                            return ACTION_CMD_FACTORY.caretMove(Direction.BACK, false, false, false);
                        }
                    }
                }
                return ACTION_CMD_FACTORY.removeText(0, RichTextAreaViewModel.Remove.WORD);
            }
            return null;
        }),
        entry( new KeyCodeCombination(DELETE),                                               e -> ACTION_CMD_FACTORY.removeText(0)),
        entry( new KeyCodeCombination(B, SHORTCUT_DOWN),                                     e -> {
            TextDecoration decoration = (TextDecoration) viewModel.getDecorationAtCaret();
            FontWeight fontWeight = decoration.getFontWeight() == BOLD ? NORMAL : BOLD;
            return ACTION_CMD_FACTORY.decorate(TextDecoration.builder().fromDecoration(decoration).fontWeight(fontWeight).build());
        }),
        entry(new KeyCodeCombination(I, SHORTCUT_DOWN),                                      e -> {
            TextDecoration decoration = (TextDecoration) viewModel.getDecorationAtCaret();
            FontPosture fontPosture = decoration.getFontPosture() == ITALIC ? REGULAR : ITALIC;
            return ACTION_CMD_FACTORY.decorate(TextDecoration.builder().fromDecoration(decoration).fontPosture(fontPosture).build());
        }),
        entry(new KeyCodeCombination(TAB, SHIFT_ANY),                                        e -> {
            ParagraphDecoration decoration = viewModel.getDecorationAtParagraph();
            Paragraph paragraph = viewModel.getParagraphWithCaret().orElse(null);
            if (decoration != null && decoration.getGraphicType() != ParagraphDecoration.GraphicType.NONE) {
                int level = Math.max(decoration.getIndentationLevel() + (e.isShiftDown() ? -1 : 1), 0);
                return ACTION_CMD_FACTORY.decorate(ParagraphDecoration.builder().fromDecoration(decoration).indentationLevel(level).build());
            } else if (decoration != null && decoration.hasTableDecoration() &&
                    paragraph != null && paragraph.getStart() < paragraph.getEnd()) {
                int caretPosition = viewModel.getCaretPosition();
                UnitBuffer buffer = new UnitBuffer();
                viewModel.walkFragments((u, d) -> buffer.append(u), paragraph.getStart(), paragraph.getEnd());
                Table table = new Table(buffer,
                        paragraph.getStart(), decoration.getTableDecoration().getRows(), decoration.getTableDecoration().getColumns());
                // select content of prev/next cell if non-empty, or move to prev/next cell
                List<Integer> selectionAtNextCell = table.selectNextCell(caretPosition, e.isShiftDown() ? Direction.BACK : Direction.FORWARD);
                int start = selectionAtNextCell.get(0);
                viewModel.clearSelection();
                viewModel.setCaretPosition(start);
                if (selectionAtNextCell.size() == 2) {
                    int end = selectionAtNextCell.get(1);
                    if (start < end) {
                        // select content
                        return ACTION_CMD_FACTORY.selectCell(new Selection(start, end));
                    }
                }
            }
            return null;
        })
    );

    private static final Point2D DEFAULT_POINT_2D = new Point2D(-1, -1);

    private final ParagraphListView paragraphListView;
    private final SortedList<Paragraph> paragraphSortedList = new SortedList<>(viewModel.getParagraphList(), Comparator.comparing(Paragraph::getStart));

    final ContextMenu contextMenu = new ContextMenu();
    private ObservableList<MenuItem> tableCellContextMenuItems;
    private ObservableList<MenuItem> tableContextMenuItems;
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

    private final Text promptNode;

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

    // attachedProperty
    private final BooleanProperty attachedProperty = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            if (promptVisibleBinding == null) {
                promptVisibleBinding = Bindings.createBooleanBinding(
                        () -> {
                            Point2D point2D = caretOriginProperty.get();
                            boolean visible = viewModel.getTextLength() == 0 && viewModel.getCaretPosition() == 0 &&
                                    point2D.getX() > DEFAULT_POINT_2D.getX() && point2D.getY() > DEFAULT_POINT_2D.getY();
                            if (visible) {
                                updatePromptNodeLocation();
                            }
                            return visible;
                        },
                        viewModel.caretPositionProperty(), viewModel.textLengthProperty(), caretOriginProperty);
            }

            if (get()) {
                // bind control properties to viewModel properties, to forward the changes of the later
                getSkinnable().textLengthProperty.bind(viewModel.textLengthProperty());
                getSkinnable().modifiedProperty.bind(viewModel.savedProperty().not());
                getSkinnable().selectionProperty.bind(viewModel.selectionProperty());
                getSkinnable().decorationAtCaret.bind(viewModel.decorationAtCaretProperty());
                getSkinnable().decorationAtParagraph.bind(viewModel.decorationAtParagraphProperty());
                caretPositionProperty.bind(viewModel.caretPositionProperty());
                getSkinnable().caretOriginProperty.bind(caretOriginProperty);
                getSkinnable().caretRowColumnProperty.bind(caretRowColumnProperty);
                promptNode.visibleProperty().bind(promptVisibleBinding);
                promptNode.fontProperty().bind(promptFontBinding);
            } else {
                // unbind control properties from viewModel properties, to avoid forwarding
                // the internal changes of the latter, while it performs an action
                getSkinnable().textLengthProperty.unbind();
                getSkinnable().modifiedProperty.unbind();
                getSkinnable().selectionProperty.unbind();
                getSkinnable().decorationAtCaret.unbind();
                getSkinnable().decorationAtParagraph.unbind();
                caretPositionProperty.unbind();
                getSkinnable().caretOriginProperty.unbind();
                getSkinnable().caretRowColumnProperty.unbind();
                promptNode.visibleProperty().unbind();
                promptNode.fontProperty().unbind();
            }
        }
    };

    private final ChangeListener<Document> documentChangeListener = (obs, ov, nv) -> {
        if (!attachedProperty.get()) {
            return;
        }
        if (ov == null && nv != null) {
            // new/open
            dispose();
            setup(nv);
            getSkinnable().documentProperty.set(nv);
        } else if (nv != null) {
            // save
            getSkinnable().documentProperty.set(nv);
        }
    };

    final ObjectProperty<Point2D> caretOriginProperty = new SimpleObjectProperty<>(this, "caretOrigin", DEFAULT_POINT_2D) {
        @Override
        protected void invalidated() {
            viewModel.getParagraphWithCaret().ifPresentOrElse(p -> {
                int row = viewModel.getParagraphList().indexOf(p);
                int col = caretPositionProperty.get() - p.getStart();
                caretRowColumnProperty.set(new Point2D(col, row));
            }, () -> caretRowColumnProperty.set(DEFAULT_POINT_2D));
        }
    };
    private final ObjectProperty<Point2D> caretRowColumnProperty = new SimpleObjectProperty<>(this, "caretRowColumn", DEFAULT_POINT_2D);

    private final ObjectBinding<Font> promptFontBinding = Bindings.createObjectBinding(this::getPromptNodeFont,
            viewModel.decorationAtCaretProperty(), viewModel.decorationAtParagraphProperty());
    private BooleanBinding promptVisibleBinding;

    private final IntegerProperty caretPositionProperty = new SimpleIntegerProperty() {
        @Override
        protected void invalidated() {
            int caret = get();
            int externalCaret = caret;
            if (caret > -1) {
                String text = viewModel.getTextBuffer().getText(0, caret);
                externalCaret = text.length();
            }
            getSkinnable().caretPosition.set(externalCaret);
            viewModel.getParagraphWithCaret()
                    .ifPresent(paragraph -> Platform.runLater(paragraphListView::scrollIfNeeded));
        }
    };

    private final InvalidationListener focusListener;
    private final EventHandler<DragEvent> dndHandler = this::dndListener;

    private final ChangeListener<Boolean> tableAllowedListener;
    private final ChangeListener<EmojiSkinTone> skinToneChangeListener;

    private final ResourceBundle resources;

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
        private Group sheet;
        private Region container;

        public ParagraphListView(RichTextArea control) {
            virtualFlow = new RichVirtualFlow(control);
            getStyleClass().setAll("paragraph-list-view");

            addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
                // if a dragging event was started from a listCell, but the event gets out of the
                // listView bounds, forward event to cell -> paragraphTile -> TextFlow, so selection
                // can continue
                if (anchorIndex != -1 && !getLayoutBounds().contains(e.getX(), e.getY())) {
                    // Translate listView event to a point (in listView coordinates) that could be over a listCell
                    Point2D listPoint = localToScreen(getPointInListView(e));

                    getSheet().getChildren().stream()
                            .filter(RichListCell.class::isInstance)
                            .filter(cell -> cell.getLayoutBounds().contains(cell.screenToLocal(listPoint)))
                            .map(RichListCell.class::cast)
                            .findFirst()
                            .ifPresent(cell -> {
                                Point2D cellPoint = cell.screenToLocal(listPoint);
                                Point2D cellScreenPoint = cell.localToScreen(cellPoint);
                                MouseEvent mouseEvent = new MouseEvent(e.getSource(), e.getTarget(), e.getEventType(),
                                        cellPoint.getX(), cellPoint.getY(), cellScreenPoint.getX(), cellScreenPoint.getY(),
                                        MouseButton.PRIMARY, 1,
                                        false, false, false, false,
                                        true, false, false,
                                        false, false, false, null);
                                cell.forwardDragEvent(mouseEvent);
                            });
                    double y = e.getY();
                    if (y < 0 || y > getHeight()) {
                        // scroll some pixels to prevent mouse getting stuck in a cell
                        virtualFlow.scrollPixels(y < 0 ? (y / 10) - 1 : (y - getHeight()) / 10 + 1);
                    }
                }
            });

            addEventHandler(DragEvent.DRAG_OVER, event -> {
                if (dragAndDropStart != -1) {
                    Point2D localEvent = getContainer().screenToLocal(event.getScreenX(), event.getScreenY());
                    if (localEvent.getY() < getContainer().getLayoutBounds().getMinY() || localEvent.getY() > getContainer().getLayoutBounds().getMaxY()) {
                        virtualFlow.scrollPixels(localEvent.getY() <= getContainer().getLayoutBounds().getMinY() ? -5 : 5);
                    }
                }
            });
        }

        private Point2D getPointInListView(MouseEvent e) {
            double iniX = getInsets().getLeft();
            double endX = iniX + getContainer().getLayoutBounds().getWidth() - getInsets().getRight();
            double iniY = getInsets().getTop();
            double endY = iniY + getContainer().getLayoutBounds().getHeight() - getInsets().getBottom();
            double deltaX = e.getX() < iniX ? e.getX() - iniX : e.getX() > endX ? e.getX() - endX : 0;
            double deltaY = e.getY() < iniY ? e.getY() - iniY : e.getY() > endY ? e.getY() - endY : 0;
            return new Point2D(e.getX() - deltaX, e.getY() - deltaY);
        }

        private Group getSheet() {
            if (sheet == null) {
                sheet = (Group) virtualFlow.lookup(".sheet");
            }
            return sheet;
        }

        private Region getContainer() {
            if (container == null) {
                container = (Region) virtualFlow.lookup(".clipped-container");
            }
            return container;
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
            Set<Font> usedFonts = new HashSet<>();
            Set<Image> usedImages = new HashSet<>();
            getSheet().getChildren().stream()
                    .filter(RichListCell.class::isInstance)
                    .map(RichListCell.class::cast)
                    .forEach(cell -> cell.evictUnusedObjects(usedFonts, usedImages));

            List<Font> cachedFonts = new ArrayList<>(getFontCache().values());
            cachedFonts.removeAll(usedFonts);
            if (!cachedFonts.isEmpty()) {
                getFontCache().values().removeAll(cachedFonts);
            }

            List<Image> cachedImages = new ArrayList<>(getImageCache().values());
            cachedImages.removeAll(usedImages);
            if (!cachedImages.isEmpty()) {
                getImageCache().values().removeAll(cachedImages);
            }
        }

        int getNextRowPosition(double x, boolean down) {
            return getSheet().getChildren().stream()
                    .filter(RichListCell.class::isInstance)
                    .map(RichListCell.class::cast)
                    .filter(RichListCell::hasCaret)
                    .mapToInt(cell -> cell.getNextRowPosition(x, down))
                    .findFirst()
                    .orElse(-1);
        }

        int getNextTableCellPosition(boolean down) {
            return getSheet().getChildren().stream()
                    .filter(RichListCell.class::isInstance)
                    .map(RichListCell.class::cast)
                    .filter(RichListCell::hasCaret)
                    .mapToInt(cell -> cell.getNextTableCellPosition(down))
                    .findFirst()
                    .orElse(-1);
        }

        void resetCaret() {
            getSheet().getChildren().stream()
                    .filter(RichListCell.class::isInstance)
                    .map(RichListCell.class::cast)
                    .filter(RichListCell::hasCaret)
                    .findFirst()
                    .ifPresent(RichListCell::resetCaret);
        }

        void updateLayout() {
            // force updateItem call to recalculate backgroundPath positions
            virtualFlow.rebuildCells();
        }

        void scrollIfNeeded() {
            final Bounds vfBounds = virtualFlow.localToScene(virtualFlow.getBoundsInLocal());
            double viewportMinY = vfBounds.getMinY();
            double viewportMaxY = vfBounds.getMaxY();
            caret().ifPresentOrElse(caret -> {
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


    // --- prompt text fill
    private final StyleableProperty<Paint> promptTextFill = FACTORY.createStyleablePaintProperty(getSkinnable(), "promptTextFill", "-fx-prompt-text-fill", c -> {
        final RichTextAreaSkin skin = (RichTextAreaSkin) c.getSkin();
        return skin.promptTextFill;
    }, Color.GRAY);
    protected final void setPromptTextFill(Paint value) {
        promptTextFill.setValue(value);
    }
    protected final Paint getPromptTextFill() {
        return promptTextFill.getValue();
    }
    protected final ObjectProperty<Paint> promptTextFillProperty() {
        return (ObjectProperty<Paint>) promptTextFill;
    }

    private final DoubleProperty fullHeightProperty = new SimpleDoubleProperty(this, "fullHeight") {
        @Override
        protected void invalidated() {
            getSkinnable().fullHeightProperty.setValue(get() + getSkinnable().getPadding().getTop() + getSkinnable().getPadding().getBottom());
        }
    };

    protected RichTextAreaSkin(final RichTextArea control) {
        super(control);
        resources = ResourceBundle.getBundle("com.gluonhq.richtextarea.rich-text-area");

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

        tableAllowedListener = (obs, ov, nv) -> viewModel.setTableAllowed(nv);
        skinToneChangeListener = (obs, ov, nv) -> refreshTextFlow();

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

        // set prompt text
        promptNode = new Text();
        setupPromptNode();

        viewModel.attachedProperty().subscribe((b0, b) -> attachedProperty.set(b));
        setup(control.getDocument());
    }

    /// PROPERTIES ///////////////////////////////////////////////////////////////


    /// PUBLIC METHODS  /////////////////////////////////////////////////////////

    @Override
    public void dispose() {
        viewModel.clearSelection();
        viewModel.removeChangeListener(textChangeListener);
        viewModel.documentProperty().removeListener(documentChangeListener);
        viewModel.autoSaveProperty().unbind();
        lastValidCaretPosition = -1;
        promptNode.textProperty().unbind();
        promptNode.fillProperty().unbind();
        getSkinnable().editableProperty().removeListener(this::editableChangeListener);
        getSkinnable().tableAllowedProperty().removeListener(tableAllowedListener);
        getSkinnable().setOnKeyPressed(null);
        getSkinnable().setOnKeyTyped(null);
        getSkinnable().widthProperty().removeListener(controlPrefWidthListener);
        getSkinnable().focusedProperty().removeListener(focusListener);
        getSkinnable().removeEventHandler(DragEvent.ANY, dndHandler);
        getSkinnable().skinToneProperty().removeListener(skinToneChangeListener);
        contextMenu.getItems().clear();
        tableCellContextMenuItems = null;
        tableContextMenuItems = null;
        editableContextMenuItems = null;
        nonEditableContextMenuItems = null;
        attachedProperty.set(false);
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
        attachedProperty.set(false);
        viewModel.setTextBuffer(new PieceTable(document));
        lastValidCaretPosition = viewModel.getTextBuffer().getInternalPosition(document.getCaretPosition());
        viewModel.setCaretPosition(lastValidCaretPosition);
        viewModel.setDecorationAtParagraph(viewModel.getTextBuffer().getParagraphDecorationAtCaret(lastValidCaretPosition));
        viewModel.addChangeListener(textChangeListener);
        viewModel.setDocument(document);
        viewModel.documentProperty().addListener(documentChangeListener);
        viewModel.autoSaveProperty().bind(getSkinnable().autoSaveProperty());
        promptNode.textProperty().bind(getSkinnable().promptTextProperty());
        promptNode.fillProperty().bind(promptTextFillProperty());
        getSkinnable().setOnContextMenuRequested(contextMenuEventEventHandler);
        getSkinnable().editableProperty().addListener(this::editableChangeListener);
        getSkinnable().tableAllowedProperty().addListener(tableAllowedListener);
        viewModel.setTableAllowed(getSkinnable().isTableAllowed());
        getSkinnable().setOnKeyPressed(this::keyPressedListener);
        getSkinnable().setOnKeyTyped(this::keyTypedListener);
        getSkinnable().widthProperty().addListener(controlPrefWidthListener);
        getSkinnable().focusedProperty().addListener(focusListener);
        getSkinnable().addEventHandler(DragEvent.ANY, dndHandler);
        getSkinnable().skinToneProperty().addListener(skinToneChangeListener);
        refreshTextFlow();
        requestLayout();
        editableChangeListener(null); // sets up all related listeners
        attachedProperty.set(true);
    }

    private void setupPromptNode() {
        promptNode.setMouseTransparent(true);
        promptNode.setManaged(false);
        promptNode.setVisible(false);
        promptNode.getStyleClass().add("prompt");
        double promptNodeWidth = promptNode.prefWidth(-1);
        double promptNodeHeight = promptNode.prefHeight(promptNodeWidth);
        promptNode.resize(promptNodeWidth, promptNodeHeight);
        getChildren().add(promptNode);
    }

    private Font getPromptNodeFont() {
        Decoration decorationAtCaret = viewModel.getDecorationAtCaret();
        if (decorationAtCaret instanceof TextDecoration) {
            TextDecoration textDecoration = (TextDecoration) decorationAtCaret;

            int hash = Objects.hash(
                    textDecoration.getFontFamily(),
                    textDecoration.getFontWeight(),
                    textDecoration.getFontPosture(),
                    textDecoration.getFontSize());

            return getFontCache().computeIfAbsent(hash,
                    h -> Font.font(
                    textDecoration.getFontFamily(),
                    textDecoration.getFontWeight(),
                    textDecoration.getFontPosture(),
                    textDecoration.getFontSize()
            ));
        }
        return Font.font(DEFAULT_FONT_SIZE);
    }

    private void updatePromptNodeLocation() {
        double promptNodeWidth = promptNode.prefWidth(-1);
        TextAlignment alignment = viewModel.getDecorationAtParagraph().getAlignment();
        Point2D origin = caretOriginProperty.get();
        double x = origin.getX();
        if (alignment == TextAlignment.CENTER) {
            x -= promptNodeWidth / 2;
        } else if (alignment == TextAlignment.RIGHT) {
            x -= promptNodeWidth;
        }
        promptNode.relocate(x, origin.getY());
    }

    // TODO Need more optimal way of rendering text fragments.
    //  For now rebuilding the whole text flow
    private void refreshTextFlow() {
        objectsCacheEvictionTimer.pause();
        try {
            nonTextNodes.set(0);
            viewModel.resetCharacterIterator();
            // this ensures changes in decoration are applied:
            paragraphListView.updateLayout();
            computeFullHeight();

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

    Paragraph getLastParagraph() {
        return paragraphSortedList.get(paragraphSortedList.size() - 1);
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

    private Optional<Path> caret() {
        return paragraphListView.lookupAll(".caret").stream()
                .filter(Path.class::isInstance)
                .map(Path.class::cast)
                .filter(path -> !path.getElements().isEmpty())
                .findFirst();
    }

    // So far the only way to find prev/next row location is to use the size of the caret,
    // which always has the height of the row. Adding line spacing to it allows us to find a point which
    // belongs to the desired row. Then using the `hitTest` we can find the related caret position.
    private int getNextRowPosition(double x, Boolean down) {
        ObservableList<Paragraph> items = paragraphListView.getItems();
        int caretPosition = viewModel.getCaretPosition();
        int nextRowPosition = Math.min(viewModel.getTextLength(),
                paragraphListView.getNextRowPosition(x, down != null && down));
        // if the caret is at the top or bottom of the paragraph:
        if (down != null && ((down && nextRowPosition <= caretPosition) ||
                (!down && nextRowPosition >= caretPosition))) {
            int paragraphWithCaretIndex = items.stream()
                    .filter(p -> p.getStart() <= caretPosition &&
                            caretPosition < (p.equals(getLastParagraph()) ? p.getEnd() + 1 : p.getEnd()))
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

    private int getNextTableCellPosition(Boolean down) {
        return Math.min(viewModel.getTextLength(),
                paragraphListView.getNextTableCellPosition(down != null && down));
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
               !e.isMetaDown();
    }

    private void execute(ActionCmd action) {
        Objects.requireNonNull(action).apply(viewModel);
    }

    private void keyPressedListener(KeyEvent e) {
        long a0 = System.nanoTime();
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
        if (LOG.isLoggable(Level.FINEST)) {
            long a1 = System.nanoTime();
            LOG.finest("KeyPressed processed in "+ (a1-a0) + "ns");
        }

    }

    // not private for testing
    void keyTypedListener(KeyEvent e) {
        long a0 = System.nanoTime();

        if (isCharOnly(e)) {
            if ("\t".equals(e.getCharacter())) {
                ParagraphDecoration decoration = viewModel.getDecorationAtParagraph();
                if (decoration != null &&
                    (decoration.getGraphicType() != ParagraphDecoration.GraphicType.NONE || decoration.hasTableDecoration())) {
                    // processed via keyPressedListener
                    e.consume();
                    return;
                }
            }
            paragraphListView.resetCaret();
            if (viewModel.getSelection().isDefined()) {
                execute(ACTION_CMD_FACTORY.replaceText(e.getCharacter()));
            } else {
                execute(ACTION_CMD_FACTORY.insertText(e.getCharacter()));
            }
            e.consume();
        }
        if (LOG.isLoggable(Level.FINEST)) {
            long a1 = System.nanoTime();
            LOG.finest("KeyTyped processed in "+ (a1-a0) + "ns");
        }
    }

    private void computeFullHeight() {
        ObservableList<Paragraph> items = paragraphListView.getItems();
        int psize = items.size();
        int unknown = 0;
        double total = 0.;
        for (Paragraph p : items) {
            double ph = p.getPreferredHeight();
            if (ph < 0) {
                unknown++;
            } else {
                total = total + ph;
            }
        }
        if (unknown > 0) {
            if (unknown == psize) {
                total = 20*psize;
            } else {
                total = total * psize / (psize - unknown);
            }
        }
        fullHeightProperty.set(total);
    }

    private void populateContextMenu(boolean isEditable) {
        if (isEditable && editableContextMenuItems == null) {
            editableContextMenuItems = FXCollections.observableArrayList(
                    createMenuItem(resources.getString("rta.context.menu.undo"), ACTION_CMD_FACTORY.undo()),
                    createMenuItem(resources.getString("rta.context.menu.redo"), ACTION_CMD_FACTORY.redo()),
                    new SeparatorMenuItem(),
                    createMenuItem(resources.getString("rta.context.menu.copy"), ACTION_CMD_FACTORY.copy()),
                    createMenuItem(resources.getString("rta.context.menu.cut"), ACTION_CMD_FACTORY.cut()),
                    createMenuItem(resources.getString("rta.context.menu.paste"), ACTION_CMD_FACTORY.paste()),
                    new SeparatorMenuItem(),
                    createMenuItem(resources.getString("rta.context.menu.selectall"), ACTION_CMD_FACTORY.selectAll()));

            if (getSkinnable().isTableAllowed()) {
                tableCellContextMenuItems = FXCollections.observableArrayList(
                        createMenuItem(resources.getString("rta.context.menu.table.cell.delete"), ACTION_CMD_FACTORY.deleteTableCell()),
                        new SeparatorMenuItem(),
                        createMenuItem(resources.getString("rta.context.menu.table.cell.alignleft"), ACTION_CMD_FACTORY.alignTableCell(TextAlignment.LEFT)),
                        createMenuItem(resources.getString("rta.context.menu.table.cell.centre"), ACTION_CMD_FACTORY.alignTableCell(TextAlignment.CENTER)),
                        createMenuItem(resources.getString("rta.context.menu.table.cell.justify"), ACTION_CMD_FACTORY.alignTableCell(TextAlignment.JUSTIFY)),
                        createMenuItem(resources.getString("rta.context.menu.table.cell.alignright"), ACTION_CMD_FACTORY.alignTableCell(TextAlignment.RIGHT))
                );
                Menu tableCellMenu = new Menu(resources.getString("rta.context.menu.table.cell"));
                tableCellMenu.getItems().addAll(tableCellContextMenuItems);
                MenuItem insertTableMenuItem = createMenuItem(resources.getString("rta.context.menu.table.insert"), ACTION_CMD_FACTORY.insertTable(new TableDecoration(1, 2)));
                tableCellMenu.disableProperty().bind(insertTableMenuItem.disableProperty().not());
                tableContextMenuItems = FXCollections.observableArrayList(
                        insertTableMenuItem,
                        createMenuItem(resources.getString("rta.context.menu.table.delete"), ACTION_CMD_FACTORY.deleteTable()),
                        new SeparatorMenuItem(),
                        createMenuItem(resources.getString("rta.context.menu.table.column.before"), ACTION_CMD_FACTORY.insertTableColumnBefore()),
                        createMenuItem(resources.getString("rta.context.menu.table.column.after"), ACTION_CMD_FACTORY.insertTableColumnAfter()),
                        createMenuItem(resources.getString("rta.context.menu.table.column.delete"), ACTION_CMD_FACTORY.deleteTableColumn()),
                        new SeparatorMenuItem(),
                        createMenuItem(resources.getString("rta.context.menu.table.row.above"), ACTION_CMD_FACTORY.insertTableRowAbove()),
                        createMenuItem(resources.getString("rta.context.menu.table.row.below"), ACTION_CMD_FACTORY.insertTableRowBelow()),
                        createMenuItem(resources.getString("rta.context.menu.table.row.delete"), ACTION_CMD_FACTORY.deleteTableRow()),
                        new SeparatorMenuItem(),
                        tableCellMenu
                );
                Menu tableMenu = new Menu(resources.getString("rta.context.menu.table"));
                tableMenu.getItems().addAll(tableContextMenuItems);
                editableContextMenuItems.addAll(new SeparatorMenuItem(), tableMenu);
            }
        } else if (!isEditable && nonEditableContextMenuItems == null) {
            nonEditableContextMenuItems = FXCollections.observableArrayList(
                    createMenuItem(resources.getString("rta.context.menu.copy"), ACTION_CMD_FACTORY.copy()),
                    new SeparatorMenuItem(),
                    createMenuItem(resources.getString("rta.context.menu.selectall"), ACTION_CMD_FACTORY.selectAll()));
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
                        ACTION_CMD_FACTORY.decorate(new ImageDecoration(url)).apply(viewModel);
                    }
                });
            } else if (dragboard.hasUrl()) {
                String url = dragboard.getUrl();
                // validate if url is an image before adding it:
                if (url != null) {
                    if (new Image(url).getException() == null) {
                        ACTION_CMD_FACTORY.decorate(new ImageDecoration(url)).apply(viewModel);
                    } else {
                        // add text and hyperlink
                        int caret = viewModel.getCaretPosition();
                        ACTION_CMD_FACTORY.insertText(url).apply(viewModel);
                        viewModel.setSelection(new Selection(caret, caret + url.length()));
                        ACTION_CMD_FACTORY.decorate(TextDecoration.builder().url(url).build()).apply(viewModel);
                    }
                }
            } else if (dragboard.hasString()) {
                ACTION_CMD_FACTORY.insertText(dragboard.getString()).apply(viewModel);
            }
            requestLayout();
            dragAndDropStart = -1;
        }
    }

    private static final StyleablePropertyFactory<RichTextArea> FACTORY = new StyleablePropertyFactory<>(SkinBase.getClassCssMetaData());

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return FACTORY.getCssMetaData();
    }

    @Override public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }
}
