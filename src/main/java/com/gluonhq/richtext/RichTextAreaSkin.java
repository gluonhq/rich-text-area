package com.gluonhq.richtext;

import com.gluonhq.richtext.model.PieceTable;
import com.gluonhq.richtext.model.TextBuffer;
import com.gluonhq.richtext.model.TextDecoration;
import com.gluonhq.richtext.viewmodel.RichTextAreaViewModel;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SkinBase;
import javafx.scene.input.*;
import javafx.scene.shape.Path;
import javafx.scene.text.HitInfo;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyCombination.*;


class RichTextAreaSkin extends SkinBase<RichTextArea> {

    private static final Map<KeyCombination, EditorAction> INPUT_MAP = Map.of(
        new KeyCodeCombination(RIGHT, SHIFT_ANY),      EditorAction.FORWARD,
        new KeyCodeCombination(LEFT,  SHIFT_ANY),      EditorAction.BACK,
        new KeyCodeCombination(DOWN,  SHIFT_ANY),      EditorAction.DOWN,
        new KeyCodeCombination(UP,    SHIFT_ANY),      EditorAction.UP,
        new KeyCodeCombination(BACK_SPACE, SHIFT_ANY), EditorAction.BACKSPACE,
        new KeyCodeCombination(DELETE),                EditorAction.DELETE,
        new KeyCodeCombination(Z, SHORTCUT_DOWN),             EditorAction.UNDO,
        new KeyCodeCombination(Z, SHORTCUT_DOWN, SHIFT_DOWN), EditorAction.REDO,
        new KeyCodeCombination(ENTER, SHIFT_ANY),      EditorAction.ENTER
    );

    private final RichTextAreaViewModel viewModel =
        new RichTextAreaViewModel(
            new PieceTable("Simple text text text"),
            this::getNextRowPosition // TODO need to find a better way to find next row caret position
        );

    private final ScrollPane scrollPane;
    private final TextFlow textFlow = new TextFlow();
    private final Path caretShape = new Path();
    private final Path selectionShape = new Path();

    private final Timeline caretTimeline = new Timeline(
        new KeyFrame(Duration.ZERO        , e -> setCaretVisibility(false)),
        new KeyFrame(Duration.seconds(0.5), e -> setCaretVisibility(true)),
        new KeyFrame(Duration.seconds(1.0))
    );

    private final Consumer<TextBuffer.Event> textChangeListener = e -> refreshTextFlow();
    private ChangeListener<Boolean> focusChangeListener;

    protected RichTextAreaSkin(final RichTextArea control) {
        super(control);

        textFlow.setFocusTraversable(false);
        caretShape.setFocusTraversable(false);

        selectionShape.getStyleClass().setAll("selection");

        Group layers = new Group(selectionShape, caretShape, textFlow);
        caretTimeline.setCycleCount(Timeline.INDEFINITE);

        scrollPane = new ScrollPane(layers);
        scrollPane.setFocusTraversable(false);
        focusChangeListener = (obs, ov, nv) -> {
            if (nv) {
                getSkinnable().requestFocus();
            }
        };
        scrollPane.focusedProperty().addListener(focusChangeListener);
        getChildren().add(scrollPane);

        // all listeners have to be removed within dispose method
        control.editableProperty().addListener(this::editableChangeListener);
        editableChangeListener(null); // sets up all related listeners

        //TODO remove listener on viewModel change
        viewModel.caretPositionProperty().addListener( (o,ocp, p) -> {
            int caretPosition = p.intValue();
            caretShape.getElements().clear();
            if (caretPosition < 0 ) {
                caretTimeline.stop();
            } else {
                var pathElements = textFlow.caretShape(caretPosition, true);
                caretShape.getElements().addAll(pathElements);
                caretTimeline.play();
            }
        });

        //TODO remove listener on viewModel change
        viewModel.selectionProperty().addListener( (o, os, selection) -> {
            selectionShape.getElements().clear();
            if (selection.isDefined()) {
                selectionShape.getElements().setAll(textFlow.rangeShape( selection.getStart(), selection.getEnd() ));
            }
        });


        viewModel.addChangeListener(textChangeListener);
        refreshTextFlow();

    }

    /// PROPERTIES ///////////////////////////////////////////////////////////////


    /// PUBLIC METHODS  /////////////////////////////////////////////////////////

    @Override
    public void dispose() {
        getSkinnable().setEditable(false); // removes all related listeners
        getSkinnable().editableProperty().removeListener(this::editableChangeListener);
        viewModel.removeChangeListener(textChangeListener);
    }

    /// PRIVATE METHODS /////////////////////////////////////////////////////////

    //TODO Need more optimal way of rendering text fragments.
    //  For now rebuilding the whole text flow
    private void refreshTextFlow() {
        var fragments = new ArrayList<Text>();
        viewModel.walkFragments( (text, decoration) -> fragments.add( buildText(text, decoration)));
        textFlow.getChildren().setAll(fragments);
    }

    private Text buildText(String content, TextDecoration decoration ) {
        Text text = new Text(content);
        Optional.ofNullable(decoration.getForeground()).ifPresent(text::setFill); // has to be fill for font to render properly
        Optional.ofNullable(decoration.getFont()).ifPresentOrElse(text::setFont, () -> text.setFont(TextDecoration.DEFAULT.getFont()) );
        return text;
    }

    private void editableChangeListener(Observable o) {

        boolean editable = getSkinnable().isEditable();

        viewModel.clearSelection();
        if (editable) {
            getSkinnable().setOnKeyPressed( this::keyPressedListener);
            getSkinnable().setOnKeyTyped(this::keyTypedListener);
            textFlow.setOnMousePressed(this::mousePressedListener);
            textFlow.setOnMouseDragged(this::mouseDraggedListener);
        } else {
            getSkinnable().setOnKeyPressed(null);
            getSkinnable().setOnKeyTyped(null);
            scrollPane.focusedProperty().removeListener(focusChangeListener);
            textFlow.setOnMousePressed(null);
            textFlow.setOnMouseDragged(null);
        }

        viewModel.setCaretPosition( editable? 0:-1 );
        textFlow.setCursor( editable? Cursor.TEXT: Cursor.DEFAULT);

    }

    private void setCaretVisibility(boolean on) {
        if (caretShape.getElements().size() > 0) {
            // Opacity is used since we don't want the changing caret bounds to affect the layout
            // Otherwise text appears to be jumping
            caretShape.setOpacity( on? 1: 0 );
        }
    }

    private int dragStart = -1;

    private void mousePressedListener(MouseEvent e) {
        HitInfo hitInfo = textFlow.hitTest(new Point2D( e.getX(), e.getY()));
        if (hitInfo.getCharIndex() >= 0) {
            viewModel.setCaretPosition(hitInfo.getCharIndex());
            dragStart = viewModel.getCaretPosition();
        }
        viewModel.clearSelection();
        getSkinnable().requestFocus();
        e.consume();
    }

    private void mouseDraggedListener(MouseEvent e) {
        HitInfo hitInfo = textFlow.hitTest(new Point2D( e.getX(), e.getY()));
        if (hitInfo.getCharIndex() >= 0) {
            int dragEnd = hitInfo.getCharIndex();
            viewModel.setSelection( new Selection(dragStart, dragEnd));
            viewModel.setCaretPosition(hitInfo.getCharIndex());
        }
        e.consume();
    }

    // So far the only way to find prev/next row location is to use the size of the caret,
    // which always has the height of the row. Adding line spacing to it allows us to find a point which
    // belongs to the desired row. Then using the `hitTest` we can find the related caret position.
    private int getNextRowPosition( boolean down ) {
        Bounds caretBounds = caretShape.getBoundsInLocal();
        double nextRowPos =  down? caretBounds.getMaxY() + textFlow.getLineSpacing():
                caretBounds.getMinY() - textFlow.getLineSpacing();
        HitInfo hitInfo = textFlow.hitTest(new Point2D( caretBounds.getMinX(), nextRowPos));
        return hitInfo.getCharIndex();
    }

    private static boolean isPrintableChar(char c) {
        Character.UnicodeBlock changeBlock = Character.UnicodeBlock.of(c);
        return  ( c == '\n' || c == '\t' || !Character.isISOControl(c)) &&
                !KeyEvent.CHAR_UNDEFINED.equals(String.valueOf(c)) &&
                changeBlock != null && changeBlock != Character.UnicodeBlock.SPECIALS;
    }

    private static boolean isCharOnly(KeyEvent e ) {
        char c = e.getCharacter().isEmpty()? 0: e.getCharacter().charAt(0);
        return isPrintableChar(c) &&
               !e.isControlDown() &&
               !e.isMetaDown() &&
               !e.isAltDown();
    }

    private void keyPressedListener(KeyEvent e) {
        // Find an applicable action and execute it if found
        for (KeyCombination kc : INPUT_MAP.keySet()) {
            if (kc.match(e)) {
                viewModel.executeAction(INPUT_MAP.get(kc), e);
                e.consume();
            }
        }
    }

    private void keyTypedListener(KeyEvent e) {
        if ( isCharOnly(e) ) {
            viewModel.executeAction(EditorAction.INSERT, e);
            e.consume();
        }
    }

}