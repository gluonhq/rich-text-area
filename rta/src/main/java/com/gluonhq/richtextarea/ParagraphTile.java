/*
 * Copyright (c) 2022, 2024, Gluon
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

import com.gluonhq.richtextarea.model.Paragraph;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.TextDecoration;
import com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Paint;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.HitInfo;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gluonhq.richtextarea.model.TableDecoration.TABLE_SEPARATOR;

class ParagraphTile extends HBox {

    private static final double INDENT_PADDING = 20.0;

    // ParagraphTile is HBox
    // | graphicBox | contentPane |
    // If no table, contentPane has single layer of (background + selection + caret shapes, textFlow)
    // If table, contentPane has gridBox, an HBox that can be aligned per paragraph text alignment,
    //     and has a grid of cxr layers

    private Paragraph paragraph;
    private final HBox graphicBox;
    private final Pane contentPane;

    private final List<Layer> layers;

    private final RichTextArea control;
    private final RichTextAreaSkin richTextAreaSkin;
    private final RichTextAreaViewModel viewModel;
    private final ChangeListener<Number> caretPositionListener = (o, ocp, p) -> updateCaretPosition(p.intValue());
    private final ChangeListener<Selection> selectionListener = (o, os, selection) -> updateSelection(selection);

    public ParagraphTile(RichTextAreaSkin richTextAreaSkin) {
        this.richTextAreaSkin = richTextAreaSkin;
        this.control = richTextAreaSkin.getSkinnable();
        this.viewModel = richTextAreaSkin.getViewModel();
        getStyleClass().setAll("paragraph-tile");

        contentPane = new Pane();
        contentPane.setPadding(new Insets(1));
        contentPane.getStyleClass().setAll("content-area");
        layers = new ArrayList<>();

        graphicBox = new HBox();
        graphicBox.getStyleClass().add("graphic-box");
        graphicBox.setAlignment(Pos.TOP_RIGHT);
        getChildren().addAll(graphicBox, contentPane);
        setSpacing(0);
    }

    void setParagraph(Paragraph paragraph, List<Node> fragments, List<Integer> positions, List<IndexRangeColor> background) {
        layers.forEach(Layer::reset);
        layers.clear();
        graphicBox.getChildren().clear();
        contentPane.getChildren().clear();
        viewModel.caretPositionProperty().removeListener(caretPositionListener);
        viewModel.selectionProperty().removeListener(selectionListener);
        this.paragraph = paragraph;
        if (paragraph == null) {
            contentPane.setPrefWidth(0);
            return;
        }
        ParagraphDecoration decoration = paragraph.getDecoration();
        viewModel.caretPositionProperty().addListener(caretPositionListener);
        viewModel.selectionProperty().addListener(selectionListener);
        if (decoration.hasTableDecoration()) {
            if (!fragments.isEmpty()) {
                HBox gridBox = createGridBox(fragments, positions, background, decoration);
                contentPane.getChildren().add(gridBox);
                contentPane.layout();
            }
        } else {
            Layer layer = new Layer(paragraph.getStart(), paragraph.getEnd(), false);
            layer.setContent(fragments, background, decoration);
            layers.add(layer);
            contentPane.getChildren().add(layer);
            updateGraphicBox(layer, control.getParagraphGraphicFactory());
            graphicBox.setPadding(new Insets(decoration.getTopInset(), 2, decoration.getBottomInset(), 0));
            contentPane.layout();
        }
    }

    private HBox createGridBox(List<Node> fragments, List<Integer> positions, List<IndexRangeColor> background, ParagraphDecoration decoration) {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("table");
        int r = decoration.getTableDecoration().getRows();
        int c = decoration.getTableDecoration().getColumns();
        TextAlignment[][] ta = decoration.getTableDecoration().getCellAlignment();
        for (int j = 0; j < c; j++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / (double) c);
            grid.getColumnConstraints().add(cc);
        }
        int index = 0;
        for (int i = 0; i < r; i++) {
            double prefHeight = 0d;
            for (int j = 0; j < c; j++) {
                if (index + 1 >= positions.size()) {
                    break;
                }
                Layer layer = new Layer(positions.get(index), positions.get(index + 1), true);
                ParagraphDecoration pd = ParagraphDecoration.builder().fromDecoration(decoration).alignment(ta[i][j]).build();
                int tableIndex = index;
                layer.setContent(fragments.stream()
                        .filter(n -> {
                            int p = (int) n.getProperties().getOrDefault(TABLE_SEPARATOR, -1);
                            return (positions.get(tableIndex) <= p && p < positions.get(tableIndex + 1));
                        })
                        .collect(Collectors.toList()), background, pd);
                layer.updatePrefWidth(100);
                layers.add(layer);
                grid.add(layer, j, i);
                prefHeight = Math.max(prefHeight, layer.prefHeight(100));
                index++;
            }
            RowConstraints rc = new RowConstraints();
            rc.setMinHeight(prefHeight);
            rc.setMaxHeight(Double.MAX_VALUE);
            grid.getRowConstraints().add(rc);
        }
        HBox gridBox = new HBox(grid);
        gridBox.setPrefHeight(grid.getPrefHeight() + 1);
        gridBox.setPrefWidth(richTextAreaSkin.textFlowPrefWidthProperty.get());
        gridBox.setAlignment(decoration.getAlignment().equals(TextAlignment.LEFT) ? Pos.TOP_LEFT :
                decoration.getAlignment().equals(TextAlignment.RIGHT) ? Pos.TOP_RIGHT : Pos.TOP_CENTER);
        return gridBox;
    }

    private void updateGraphicBox(Layer layer, BiFunction<Integer, ParagraphDecoration.GraphicType, Node> graphicFactory) {
        ParagraphDecoration decoration = paragraph.getDecoration();
        int indentationLevel = decoration.getIndentationLevel();
        Node graphicNode = null;
        if (graphicFactory != null) {
            graphicNode = graphicFactory.apply(indentationLevel, decoration.getGraphicType());
        }
        double spanPrefWidth = Math.max((indentationLevel - (graphicNode == null ? 0 : 1)) * INDENT_PADDING, 0d);

        if (graphicNode == null) {
            graphicBox.setMinWidth(spanPrefWidth);
            graphicBox.setMaxWidth(spanPrefWidth);
            layer.updatePrefWidth(richTextAreaSkin.textFlowPrefWidthProperty.get() - spanPrefWidth);
            return;
        }
        graphicBox.getChildren().add(graphicNode);

        double nodePrefWidth = 0d, nodePrefHeight = 0d;
         if (graphicNode instanceof Label) {
            Label numberedListLabel = (Label) graphicNode;
            String text = numberedListLabel.getText();
            if (text != null) {
                if (text.contains("#")) {
                    // numbered list
                    AtomicInteger ordinal = new AtomicInteger();
                    viewModel.getParagraphList().stream()
                            .peek(p -> {
                                if (p.getDecoration().getGraphicType() != ParagraphDecoration.GraphicType.NUMBERED_LIST ||
                                        p.getDecoration().getIndentationLevel() != indentationLevel) {
                                    // restart ordinal if previous paragraph has different indentation,
                                    // or if it is not a numbered list
                                    ordinal.set(0);
                                } else {
                                    ordinal.incrementAndGet();
                                }
                            })
                            .filter(p -> paragraph.equals(p))
                            .findFirst()
                            .ifPresent(p ->
                                    numberedListLabel.setText(text.replace("#", "" + ordinal.get())));
                }

                Font font = layer.getFont();
                numberedListLabel.setFont(font);
                double w = Tools.computeStringWidth(font, numberedListLabel.getText());
                nodePrefWidth = Math.max(w + 1, INDENT_PADDING);
                nodePrefHeight = Tools.computeStringHeight(font, numberedListLabel.getText());
            }
        } else {
            nodePrefWidth = Math.max(graphicNode.prefWidth(-1), INDENT_PADDING);
            nodePrefHeight = graphicNode.prefHeight(nodePrefWidth);
        }

        graphicNode.setTranslateY(Math.max(0d, (layer.getCaretY() - nodePrefHeight)/ 2d));
        double boxPrefWidth = spanPrefWidth + nodePrefWidth;
        graphicBox.setMinWidth(boxPrefWidth);
        graphicBox.setMaxWidth(boxPrefWidth);
        layer.updatePrefWidth(richTextAreaSkin.textFlowPrefWidthProperty.get() - boxPrefWidth);
    }

    void mousePressedListener(MouseEvent e) {
        if (control.isDisabled()) {
            return;
        }
        layers.forEach(l -> {
            Point2D localEvent = l.screenToLocal(e.getScreenX(), e.getScreenY());
            if (l.getLayoutBounds().contains(localEvent)) {
                l.mousePressedListener(e);
            }
        });
    }

    void mouseDraggedListener(MouseEvent e) {
        layers.forEach(l -> {
            Point2D localEvent = l.screenToLocal(e.getScreenX(), e.getScreenY());
            if (e.getEventType() == MouseDragEvent.MOUSE_DRAG_OVER || l.isTableCell) {
                // mouse drag from own cell
                if (l.getLayoutBounds().contains(localEvent)) {
                    l.mouseDraggedListener(e);
                }
            } else if (e.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                // synthetic event when mouse is outside the control:
                // check only vertical bounds (layer can be indented)
                if (l.getLayoutBounds().getMinY() <= localEvent.getY() && localEvent.getY() <= l.getLayoutBounds().getMaxY()) {
                    l.mouseDraggedListener(e);
                }
            }
        });
    }

    void evictUnusedObjects(Set<Font> usedFonts, Set<Image> usedImages) {
        layers.forEach(layer -> layer.evictUnusedObjects(usedFonts, usedImages));
    }

    void updateLayout() {
        if (control == null || viewModel == null) {
            return;
        }
        layers.forEach(l -> {
            l.updateSelection(viewModel.getSelection());
            l.updateCaretPosition(viewModel.getCaretPosition());
        });
    }

    boolean hasCaret() {
        return layers.stream().anyMatch(Layer::hasCaret);
    }

    void resetCaret() {
        layers.stream().filter(Layer::hasCaret).findFirst().ifPresent(Layer::reset);
    }

    int getNextRowPosition(double x, boolean down) {
        return layers.stream()
                .findFirst()
                .map(l -> l.getNextRowPosition(x, down))
                .orElse(0);
    }

    int getNextTableCellPosition(boolean down) {
        if (!paragraph.getDecoration().hasTableDecoration() ||
            layers.stream().noneMatch(Layer::hasCaret)) {
            return -1;
        }
        int r = paragraph.getDecoration().getTableDecoration().getRows();
        int c = paragraph.getDecoration().getTableDecoration().getColumns();
        int nextCell = -1;
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                int cellWithCaret = j + i * c;
                if (layers.get(cellWithCaret).hasCaret()) {
                    int p = viewModel.getCaretPosition();
                    if ((down && p < layers.get(cellWithCaret).end - 1 && i == r - 1) ||
                            (!down && p > layers.get(cellWithCaret).start && i == 0)) {
                        // up & first row, or down & last row, move to start or to end before leaving the table
                        return down ? Math.max(0, layers.get(cellWithCaret).end - 1) : layers.get(cellWithCaret).start;
                    }
                    nextCell = j + (i + (down ? 1 : -1)) * c;
                    break;
                }
            }
        }

        return down ? // down: move to start of next row, or beginning of paragraph after table
                (nextCell < layers.size() ? layers.get(nextCell).start : layers.get(r * c - 1).end) :
                // up: move to end of prev row, or end of paragraph before table
                (nextCell >= 0 ? Math.max(0, layers.get(nextCell).end - 1) : Math.max(0, layers.get(0).start - 1));
    }

    private void updateCaretPosition(int caretPosition) {
        layers.forEach(l -> l.updateCaretPosition(caretPosition));
    }

    private void updateSelection(Selection selection) {
        layers.forEach(l -> l.updateSelection(selection));
    }

    private class Layer extends Pane {

        private final Timeline caretTimeline = new Timeline(
                new KeyFrame(Duration.ZERO        , e -> setCaretVisibility(true)),
                new KeyFrame(Duration.seconds(0.5), e -> setCaretVisibility(false)),
                new KeyFrame(Duration.seconds(1.0))
        );

        private final ObservableSet<Path> textBackgroundColorPaths = FXCollections.observableSet();
        private final Path caretShape = new Path();
        private final Path selectionShape = new Path();
        private final TextFlow textFlow = new TextFlow();
        private double textFlowLayoutX, textFlowLayoutY;

        private final int start, end;
        private final boolean isTableCell;

        public Layer(int start, int end, boolean isTableCell) {
            this.start = start;
            this.end = end;
            this.isTableCell = isTableCell;
            caretTimeline.setCycleCount(Timeline.INDEFINITE);
            textFlow.setFocusTraversable(false);
            textFlow.getStyleClass().setAll("text-flow");
            textFlow.setOnMousePressed(this::mousePressedListener);

            caretShape.setFocusTraversable(false);
            caretShape.getStyleClass().add("caret");
            selectionShape.getStyleClass().setAll("selection");
            textBackgroundColorPaths.addListener(this::updateLayer);

            getChildren().addAll(textBackgroundColorPaths);
            getChildren().addAll(selectionShape, caretShape, textFlow);
            getStyleClass().add("layer");
        }

        @Override
        protected double computePrefHeight(double width) {
            // take into account caret height: whether it is visible or not,
            // the layer's height doesn't change
            return Math.max(caretShape.getLayoutBounds().getHeight(), textFlow.prefHeight(textFlow.getPrefWidth()) + 1);
        }

        @Override
        protected double computePrefWidth(double height) {
            // take into account selection width: whether it is visible or not,
            // the layer's width doesn't change
            return textFlow.prefWidth(textFlow.getPrefHeight()) + 2;
        }

        void setContent(List<Node> fragments, List<IndexRangeColor> background, ParagraphDecoration decoration) {
            textFlow.getChildren().setAll(fragments);
            textFlow.setTextAlignment(decoration.getAlignment());
            textFlow.setLineSpacing(decoration.getSpacing());
            textFlow.setPadding(new Insets(decoration.getTopInset(), decoration.getRightInset(), decoration.getBottomInset(), decoration.getLeftInset()));

            textFlowLayoutX = 1d + decoration.getLeftInset();
            textFlowLayoutY = 1d + decoration.getTopInset();

            Platform.runLater(() -> addBackgroundPathsToLayers(background));
        }

        void reset() {
            caretTimeline.stop();
        }

        private void addBackgroundPathsToLayers(List<IndexRangeColor> backgroundIndexRanges) {
            Map<Paint, Path> fillPathMap = backgroundIndexRanges.stream()
                    .map(indexRangeBackground -> {
                        final Path path = new BackgroundColorPath(textFlow.rangeShape(indexRangeBackground.getStart(), indexRangeBackground.getEnd()));
                        path.setStrokeWidth(0);
                        path.setFill(indexRangeBackground.getColor());
                        path.setLayoutX(textFlowLayoutX);
                        path.setLayoutY(textFlowLayoutY);
                        return path;
                    })
                    .collect(Collectors.toMap(Path::getFill, Function.identity(), (p1, p2) -> {
                        Path union = (Path) Shape.union(p1, p2);
                        union.setFill(p1.getFill());
                        return union;
                    }));
            textBackgroundColorPaths.removeIf(path -> !fillPathMap.containsValue(path));
            textBackgroundColorPaths.addAll(fillPathMap.values());
        }

        void mousePressedListener(MouseEvent e) {
            Point2D localEvent = screenToLocal(e.getScreenX(), e.getScreenY());
            if (e.getButton() == MouseButton.PRIMARY && !(e.isMiddleButtonDown() || e.isSecondaryButtonDown())) {
                HitInfo hitInfo = textFlow.hitTest(new Point2D(localEvent.getX() - textFlowLayoutX, localEvent.getY() - textFlowLayoutY));
                Selection prevSelection = viewModel.getSelection();
                int prevCaretPosition = viewModel.getCaretPosition();
                int insertionIndex = hitInfo.getInsertionIndex();
                if (insertionIndex >= 0) {
                    // get global insertion point, preventing insertionIndex after linefeed
                    int globalInsertionIndex = Math.min(start + insertionIndex, getParagraphLimit() - 1);
                    if (!(e.isControlDown() || e.isAltDown() || e.isShiftDown() || e.isMetaDown() || e.isShortcutDown())) {
                        viewModel.setCaretPosition(globalInsertionIndex);
                        if (e.getClickCount() == 2) {
                            viewModel.selectCurrentWord();
                        } else if (e.getClickCount() == 3) {
                            if (isTableCell) {
                                viewModel.setSelection(new Selection(start, end));
                            } else {
                                viewModel.selectCurrentParagraph();
                            }
                        } else {
                            richTextAreaSkin.mouseDragStart = globalInsertionIndex;
                            viewModel.clearSelection();
                        }
                    } else if (e.isShiftDown() && e.getClickCount() == 1 && !(e.isControlDown() || e.isAltDown() || e.isMetaDown() || e.isShortcutDown())) {
                        int pos = prevSelection.isDefined() ?
                                globalInsertionIndex < prevSelection.getStart() ? prevSelection.getEnd() : prevSelection.getStart() :
                                prevCaretPosition;
                        viewModel.setSelection(new Selection(pos, globalInsertionIndex));
                        viewModel.setCaretPosition(globalInsertionIndex);
                    }
                }
                control.requestFocus();
                e.consume();
            }
            if (richTextAreaSkin.contextMenu.isShowing()) {
                richTextAreaSkin.contextMenu.hide();
            }
        }

        void mouseDraggedListener(MouseEvent e) {
            Point2D localEvent = screenToLocal(e.getScreenX(), e.getScreenY());
            HitInfo hitInfo = textFlow.hitTest(new Point2D(localEvent.getX() - textFlowLayoutX, localEvent.getY() - textFlowLayoutY));
            if (hitInfo.getInsertionIndex() >= 0) {
                int dragEnd = start + hitInfo.getInsertionIndex();
                viewModel.setSelection(new Selection(richTextAreaSkin.mouseDragStart, dragEnd));
                viewModel.setCaretPosition(dragEnd);
            }
            e.consume();
        }

        void evictUnusedObjects(Set<Font> usedFonts, Set<Image> usedImages) {
            usedFonts.addAll(textFlow.getChildren()
                    .stream()
                    .filter(Text.class::isInstance)
                    .map(t -> ((Text) t).getFont())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet()));

            usedImages.addAll(textFlow.getChildren()
                    .stream()
                    .filter(ImageView.class::isInstance)
                    .map(t -> ((ImageView) t).getImage())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet()));
        }

        double getCaretY() {
            var pathElements = textFlow.caretShape(0, false);
            return Stream.of(pathElements)
                    .filter(LineTo.class::isInstance)
                    .map(LineTo.class::cast)
                    .findFirst().map(LineTo::getY)
                    .orElse(0d);
        }

        Font getFont() {
            Text textNode = textFlow.getChildren().stream()
                    .filter(Text.class::isInstance)
                    .map(Text.class::cast)
                    .findFirst()
                    .orElse(null);
            return Font.font(textNode != null ? textNode.getFont().getSize() : 14d);
        }

        int getNextRowPosition(double x, boolean down) {
            Bounds caretBounds = caretShape.getLayoutBounds();
            double nextRowPos = x < 0d ?
                    down ?
                            caretBounds.getMaxY() + textFlow.getLineSpacing() :
                            caretBounds.getMinY() - textFlow.getLineSpacing() :
                    caretBounds.getCenterY();
            double xPos = x < 0d ? caretBounds.getMaxX() : x;
            HitInfo hitInfo = textFlow.hitTest(new Point2D(xPos, nextRowPos));
            return start + hitInfo.getInsertionIndex();
        }

        void updatePrefWidth(double prefWidth) {
            textFlow.setPrefWidth(prefWidth);
        }

        boolean hasCaret() {
            return !caretShape.getElements().isEmpty();
        }

        private void updateCaretPosition(int caretPosition) {
            caretShape.getElements().clear();
            if ((!control.isFocused() && richTextAreaSkin.dragAndDropStart == -1) ||
                    paragraph == null || caretPosition < start || getParagraphLimit() <= caretPosition) {
                caretTimeline.stop();
                return;
            }
            if (caretPosition < 0 || !control.isEditable()) {
                caretTimeline.stop();
            } else {
                var pathElements = textFlow.caretShape(caretPosition - start, true);
                if (pathElements.length > 0) {
                    caretShape.getElements().addAll(pathElements);
                    // prevent tiny caret
                    if (caretShape.getLayoutBounds().getHeight() < 5) {
                        double originX = caretShape.getElements().stream()
                                .filter(MoveTo.class::isInstance)
                                .map(MoveTo.class::cast)
                                .findFirst()
                                .map(MoveTo::getX)
                                .orElse(0d);
                        // Default caret size for font size 14 is 18.49
                        double caretSize = 18.0;
                        if (viewModel.getDecorationAtCaret() instanceof TextDecoration) {
                            TextDecoration td = (TextDecoration) viewModel.getDecorationAtCaret();
                            caretSize = td.getFontSize() * 1.2;
                        }
                        caretShape.getElements().add(new LineTo(originX, caretSize));
                    }
                    richTextAreaSkin.lastValidCaretPosition = caretPosition;
                    caretTimeline.play();
                    updateCaretOrigin();
                }
            }
            caretShape.setLayoutX(textFlowLayoutX);
            caretShape.setLayoutY(textFlowLayoutY);
        }

        private int getParagraphLimit() {
            int limit = end;
            if (paragraph.equals(richTextAreaSkin.getLastParagraph())) {
                // at the end of the last paragraph there is no linefeed, so we need
                // an extra position for the caret
                limit += 1;
            }
            return limit;
        }

        private void setCaretVisibility(boolean on) {
            if (!caretShape.getElements().isEmpty()) {
                // Opacity is used since we don't want the changing caret bounds to affect the layout
                // Otherwise text appears to be jumping
                caretShape.setOpacity(on ? 1 : 0);
            }
        }

        private void updateSelection(Selection selection) {
            selectionShape.getElements().clear();
            if (selection != null && selection.isDefined() && !(start > selection.getEnd() || end <= selection.getStart())) {
                PathElement[] pathElements = textFlow.rangeShape(
                        Math.max(start, selection.getStart()) - start,
                        Math.min(end, selection.getEnd()) - start);
                if (pathElements.length > 0) {
                    selectionShape.getElements().setAll(pathElements);
                }
            }
            selectionShape.setLayoutX(textFlowLayoutX);
            selectionShape.setLayoutY(textFlowLayoutY);
        }

        private void updateLayer(SetChangeListener.Change<? extends Path> change) {
            if (change.wasAdded()) {
                getChildren().add(0, change.getElementAdded());
            } else if (change.wasRemoved()) {
                getChildren().remove(change.getElementRemoved());
            }
        }

        private void updateCaretOrigin() {
            Platform.runLater(() -> {
                Bounds sceneBounds = caretShape.localToScene(caretShape.getBoundsInLocal());
                final Bounds boundsInRTA = richTextAreaSkin.getSkinnable().sceneToLocal(sceneBounds);
                richTextAreaSkin.caretOriginProperty.set(new Point2D(boundsInRTA.getMinX(), boundsInRTA.getMinY()));
            });
        }
    }
}
