package com.gluonhq.richtext;

import com.gluonhq.richtext.model.Paragraph;
import com.gluonhq.richtext.model.ParagraphDecoration;
import com.gluonhq.richtext.viewmodel.RichTextAreaViewModel;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Font;
import javafx.scene.text.HitInfo;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ParagraphTile extends HBox {

    private final Timeline caretTimeline = new Timeline(
            new KeyFrame(Duration.ZERO        , e -> setCaretVisibility(false)),
            new KeyFrame(Duration.seconds(0.5), e -> setCaretVisibility(true)),
            new KeyFrame(Duration.seconds(1.0))
    );

    private final Pane root;
    private final Group layers;
    private final ObservableSet<Path> textBackgroundColorPaths = FXCollections.observableSet();
    private final Path caretShape = new Path();
    private final Path selectionShape = new Path();
    private final TextFlow textFlow = new TextFlow() {
        @Override
        protected double computePrefHeight(double width) {
            double prefHeight = super.computePrefHeight(width);
            // take into account caret
            root.setPrefHeight(prefHeight + 1);
            return prefHeight;
        }
    };

    private final RichTextArea control;
    private final RichTextAreaSkin richTextAreaSkin;
    private final RichTextAreaViewModel viewModel;

    private Paragraph paragraph;
    private double textFlowLayoutX, textFlowLayoutY;
    private final ChangeListener<Number> caretPositionListener = (o, ocp, p) -> updateCaretPosition(p.intValue());
    private final ChangeListener<Selection> selectionListener = (o, os, selection) -> updateSelection(selection);

    public ParagraphTile(RichTextAreaSkin richTextAreaSkin) {
        this.richTextAreaSkin = richTextAreaSkin;
        this.control = richTextAreaSkin.getSkinnable();
        this.viewModel = richTextAreaSkin.getViewModel();
        getStyleClass().setAll("paragraph-tile");
        caretTimeline.setCycleCount(Timeline.INDEFINITE);
        textFlow.setFocusTraversable(false);
        textFlow.getStyleClass().setAll("text-flow");
        textFlow.setOnMousePressed(this::mousePressedListener);

        caretShape.setFocusTraversable(false);
        caretShape.getStyleClass().add("caret");

        selectionShape.getStyleClass().setAll("selection");

        layers = new Group(selectionShape, caretShape, textFlow);
        layers.getStyleClass().add("layers");
        root = new Pane(layers);
        root.setPadding(new Insets(1));
        root.getStyleClass().setAll("content-area");
        layers.getChildren().addAll(0, textBackgroundColorPaths);
        textBackgroundColorPaths.addListener(this::updateLayers);
        // TODO: add left label for list decoration, line number, indentation
        getChildren().add(root);
    }

    void setParagraph(Paragraph paragraph) {
        viewModel.caretPositionProperty().removeListener(caretPositionListener);
        viewModel.selectionProperty().removeListener(selectionListener);
        if (paragraph == null) {
            return;
        }
        this.paragraph = paragraph;
        ParagraphDecoration decoration = paragraph.getDecoration();
        textFlow.setTextAlignment(decoration.getAlignment());
        textFlow.setLineSpacing(decoration.getSpacing());
        textFlow.setPadding(new Insets(decoration.getTopInset(), decoration.getRightInset(), decoration.getBottomInset(), decoration.getLeftInset()));
        textFlowLayoutX = 1d + decoration.getLeftInset();
        textFlowLayoutY = 1d + decoration.getTopInset();
        viewModel.caretPositionProperty().addListener(caretPositionListener);
        viewModel.selectionProperty().addListener(selectionListener);
    }

    TextFlow getTextFlow() {
        return textFlow;
    }

    double getTextFlowLayoutX() {
        return textFlowLayoutX;
    }

    double getTextFlowLayoutY() {
        return textFlowLayoutY;
    }

    ObservableSet<Path> getTextBackgroundColorPaths() {
        return textBackgroundColorPaths;
    }

    void mousePressedListener(MouseEvent e) {
        if (control.isDisabled()) {
            return;
        }
        if (e.getButton() == MouseButton.PRIMARY && !(e.isMiddleButtonDown() || e.isSecondaryButtonDown())) {
            HitInfo hitInfo = textFlow.hitTest(new Point2D(e.getX() - textFlowLayoutX, e.getY() - textFlowLayoutY));
            Selection prevSelection = viewModel.getSelection();
            int prevCaretPosition = viewModel.getCaretPosition();
            int insertionIndex = hitInfo.getInsertionIndex();
            if (insertionIndex >= 0) {
                // get global insertion point, preventing insertionIndex after linefeed
                int globalInsertionIndex = Math.min(paragraph.getStart() + insertionIndex, getParagraphLimit() - 1);
                if (!(e.isControlDown() || e.isAltDown() || e.isShiftDown() || e.isMetaDown() || e.isShortcutDown())) {
                    viewModel.setCaretPosition(globalInsertionIndex);
                    if (e.getClickCount() == 2) {
                        viewModel.selectCurrentWord();
                    } else if (e.getClickCount() == 3) {
                        viewModel.selectCurrentParagraph();
                    } else {
                        richTextAreaSkin.dragStart = globalInsertionIndex;
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
        HitInfo hitInfo = textFlow.hitTest(new Point2D(e.getX() - textFlowLayoutX, e.getY() - textFlowLayoutY));
        if (hitInfo.getInsertionIndex() >= 0) {
            int dragEnd = paragraph.getStart() + hitInfo.getInsertionIndex();
            viewModel.setSelection(new Selection(richTextAreaSkin.dragStart, dragEnd));
            viewModel.setCaretPosition(dragEnd);
        }
        e.consume();
    }

    private void updateCaretPosition(int caretPosition) {
        caretShape.getElements().clear();
        if (paragraph == null || caretPosition < paragraph.getStart() || getParagraphLimit() <= caretPosition) {
            caretTimeline.stop();
            return;
        }
        if (caretPosition < 0 || !control.isEditable()) {
            caretTimeline.stop();
        } else {
            var pathElements = textFlow.caretShape(caretPosition - paragraph.getStart(), true);
            if (pathElements.length > 0) {
                caretShape.getElements().addAll(pathElements);
                // prevent tiny caret
                if (caretShape.getLayoutBounds().getHeight() < 5) {
                    caretShape.getElements().add(new LineTo(0, 16));
                }
                richTextAreaSkin.lastValidCaretPosition = caretPosition;
                caretTimeline.play();
            }
        }
        caretShape.setLayoutX(textFlowLayoutX);
        caretShape.setLayoutY(textFlowLayoutY);
    }

    private void setCaretVisibility(boolean on) {
        if (caretShape.getElements().size() > 0) {
            // Opacity is used since we don't want the changing caret bounds to affect the layout
            // Otherwise text appears to be jumping
            caretShape.setOpacity(on ? 1 : 0);
        }
    }

    private void updateSelection(Selection selection) {
        selectionShape.getElements().clear();
        if (selection != null && selection.isDefined()) {
            PathElement[] pathElements = textFlow.rangeShape(
                    Math.max(paragraph.getStart(), selection.getStart()) - paragraph.getStart(),
                    Math.min(paragraph.getEnd(), selection.getEnd()) - paragraph.getStart());
            if (pathElements.length > 0) {
                selectionShape.getElements().setAll(pathElements);
            }
        }
        selectionShape.setLayoutX(textFlowLayoutX);
        selectionShape.setLayoutY(textFlowLayoutY);
    }

    private void updateLayers(SetChangeListener.Change<? extends Path> change) {
        if (change.wasAdded()) {
            layers.getChildren().add(0, change.getElementAdded());
        } else if (change.wasRemoved()) {
            layers.getChildren().remove(change.getElementRemoved());
        }
    }

    void evictUnusedObjects() {
        Set<Font> usedFonts = textFlow.getChildren()
                .stream()
                .filter(Text.class::isInstance)
                .map(t -> ((Text) t).getFont())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<Font> cachedFonts = new ArrayList<>(richTextAreaSkin.getFontCache().values());
        cachedFonts.removeAll(usedFonts);
        richTextAreaSkin.getFontCache().values().removeAll(cachedFonts);

        Set<Image> usedImages = textFlow.getChildren()
                .stream()
                .filter(ImageView.class::isInstance)
                .map(t -> ((ImageView) t).getImage())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<Image> cachedImages = new ArrayList<>(richTextAreaSkin.getImageCache().values());
        cachedImages.removeAll(usedImages);
        richTextAreaSkin.getImageCache().values().removeAll(cachedImages);
    }

    void updateLayout() {
        if (control == null || viewModel == null) {
            return;
        }
        updateSelection(viewModel.getSelection());
        updateCaretPosition(viewModel.getCaretPosition());
    }

    boolean hasCaret() {
        return !caretShape.getElements().isEmpty();
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
        return paragraph.getStart() + hitInfo.getInsertionIndex();
    }

    private int getParagraphLimit() {
        int limit = paragraph.getEnd();
        if (paragraph.equals(richTextAreaSkin.lastParagraph)) {
            // at the end of the last paragraph there is no linefeed, so we need
            // an extra position for the caret
            limit += 1;
        }
        return limit;
    }

}
