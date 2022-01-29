package com.gluonhq.richtext;

import com.gluonhq.richtext.model.PieceTable;
import com.gluonhq.richtext.model.TextChangeListener;
import com.gluonhq.richtext.model.TextDecoration;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.Observable;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.IndexRange;
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

import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyCombination.*;


class RichTextAreaSkin extends SkinBase<RichTextArea> {

    private static final Map<KeyCombination, EditorAction> INPUT_MAP = Map.of(
            kc(RIGHT, SHIFT_ANY),      EditorAction.FORWARD,
            kc(LEFT,  SHIFT_ANY),      EditorAction.BACK,
            kc(DOWN,  SHIFT_ANY),      EditorAction.DOWN,
            kc(UP,    SHIFT_ANY),      EditorAction.UP,
            kc(BACK_SPACE, SHIFT_ANY), EditorAction.BACKSPACE,
            kc(DELETE),                EditorAction.DELETE,
            kc(Z, SHORTCUT_DOWN ),     EditorAction.UNDO
    );


    private RichTextAreaViewModel viewModel =
        new RichTextAreaViewModel(
            new PieceTable("Simple text text text"),
            this::getNextRowPosition // TODO need to find a better way to find a better way
        );

    private final TextFlow textFlow = new TextFlow();
    private final Path caretShape = new Path();
    private final Path selectionShape = new Path();

    private final Timeline caretTimeline = new Timeline(
        new KeyFrame(Duration.ZERO        , e -> setCaretVisibility(false)),
        new KeyFrame(Duration.seconds(0.5), e -> setCaretVisibility(true)),
        new KeyFrame(Duration.seconds(1.0))
    );

    private final TextChangeListener textChangeListener = new TextChangeListener() {
        @Override
        public void onInsert(String text, int position) {
            refreshTextFlow();
        }

        @Override
        public void onDelete(int position, int length) {
            refreshTextFlow();
        }
    };

    protected RichTextAreaSkin(final RichTextArea control) {
        super(control);

        textFlow.setFocusTraversable(false);
        caretShape.setFocusTraversable(false);

        selectionShape.getStyleClass().setAll("selection");

        Group layers = new Group(selectionShape, caretShape, textFlow);
        caretTimeline.setCycleCount(Timeline.INDEFINITE);

        ScrollPane scrollPane = new ScrollPane(layers);
        scrollPane.setFocusTraversable(false);
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
                caretShape.getElements().addAll(textFlow.caretShape(caretPosition, true));
                caretTimeline.play();
            }
        });

        //TODO remove listener on viewModel change
        viewModel.selectionProperty().addListener( (o, os, selection) -> {
            selectionShape.getElements().clear();
            if ( selection != null && Tools.isIndexRangeValid(selection)) {
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
            viewModel.setSelection( IndexRange.normalize(dragStart, hitInfo.getCharIndex()));
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

//    private static boolean isPrintableChar(char c) {
//        Character.UnicodeBlock changeBlock = Character.UnicodeBlock.of(c);
//        return c == '\n' &&
//                !Character.isISOControl(c) &&
//                !KeyEvent.CHAR_UNDEFINED.equals(String.valueOf(c))&&
//                changeBlock != null && changeBlock != Character.UnicodeBlock.SPECIALS;
//    }

    private static KeyCombination kc( KeyCode code, Modifier... modifiers ) {
        return  new KeyCodeCombination( code, modifiers );
    }

    private void executeKeyboardAction(KeyEvent e) {
        for ( KeyCombination kc: INPUT_MAP.keySet()) {
            if (kc.match(e)) {
                viewModel.executeAction(INPUT_MAP.get(kc),e);
                e.consume();
            }
        }
    }

    private void keyPressedListener(KeyEvent e) {
        executeKeyboardAction(e);
    }

    private void keyTypedListener(KeyEvent e) {
            // TODO this should only be done for printable chars
            viewModel.executeAction(EditorAction.INSERT,e);
            e.consume();
    }

}