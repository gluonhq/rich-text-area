package com.gluonhq.richtext;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.IndexRange;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SkinBase;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.text.*;
import javafx.util.Duration;

import java.util.Map;
import java.util.function.Consumer;

import static javafx.scene.input.KeyCombination.*;
import static javafx.scene.input.KeyCode.*;


class RichTextAreaSkin extends SkinBase<RichTextArea> {

    private final EditableTextFlow textFlow = new EditableTextFlow();
    private final CommandManager commandManager = new CommandManager(textFlow);
    private final Path caretShape = new Path();
    private final Path selectionShape = new Path();

    private final Timeline caretTimeline = new Timeline(
        new KeyFrame(Duration.ZERO        , e -> setCaretVisibility(false)),
        new KeyFrame(Duration.seconds(0.5), e -> setCaretVisibility(true)),
        new KeyFrame(Duration.seconds(1.0))
    );

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
        editableChangeListener( null); // sets up all related listeners

        // REMOVE -- FOR TESTING ONLY
        Text text1 = new javafx.scene.text.Text("Big italic\n red text");
        text1.setFocusTraversable(false);
        text1.setFill(Color.ORANGERED);
        text1.setFont(Font.font("Arial", FontPosture.ITALIC, 40));
        Text text2 = new Text(" little bold\n blue text");
        text2.setFocusTraversable(false);
        text2.setFill(Color.STEELBLUE);
        text2.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        textFlow.getChildren().addAll(text1, text2);

    }

    @Override public void dispose() {
        getSkinnable().setEditable(false); // removes all related listeners
        getSkinnable().editableProperty().removeListener(this::editableChangeListener);
    }

    /// PROPERTIES ///////////////////////////////////////////////////////////////


    /// PRIVATE METHODS /////////////////////////////////////////////////////////


    private void editableChangeListener(Observable o ) {

        boolean editable = getSkinnable().isEditable();

        if (editable) {

            textFlow.caretPositionProperty().addListener(caretPositionListener);

            textFlow.selectionProperty().addListener(selectionChangeListener);
            selectionChangeListener.invalidated(null);

            getSkinnable().setOnKeyPressed( this::keyPressedListener);
            getSkinnable().setOnKeyTyped(this::keyTypedListener);
            textFlow.setOnMousePressed(this::mousePressedListener);
            textFlow.setOnMouseDragged(this::mouseDraggedListener);

        } else {
            textFlow.caretPositionProperty().removeListener(caretPositionListener);

            textFlow.clearSelection();
            textFlow.selectionProperty().removeListener(selectionChangeListener);

            getSkinnable().setOnKeyPressed(null);
            getSkinnable().setOnKeyTyped(null);
            textFlow.setOnMousePressed(null);
            textFlow.setOnMouseDragged(null);

        }

        textFlow.setCaretPosition( editable? 0:-1 );
        textFlow.setCursor( editable? Cursor.TEXT: Cursor.DEFAULT);

    }

    private final InvalidationListener caretPositionListener = (Observable o) -> {
        caretShape.getElements().clear();
        int newPos = textFlow.getCaretPosition();
        if (newPos < 0 ) {
            caretTimeline.stop();
        } else {
            caretShape.getElements().addAll(textFlow.caretShape(newPos, true));
            caretTimeline.play();
        }
    };

    private void setCaretVisibility(boolean on) {
        if (caretShape.getElements().size() > 0) {
            caretShape.setVisible(on);
        }
    }

    private final InvalidationListener selectionChangeListener = (Observable o) -> {
        selectionShape.getElements().clear();
        IndexRange selection = textFlow.getSelection();
        if ( selection != null && Tools.isIndexRangeValid(selection)) {
            selectionShape.getElements().setAll(textFlow.rangeShape( selection.getStart(), selection.getEnd() ));
        }
        getSkinnable().setSelection(textFlow.getSelection());
    };

    private int dragStart = -1;

    private void mousePressedListener(MouseEvent e) {
        HitInfo hitInfo = textFlow.hitTest(new Point2D( e.getX(), e.getY()));
        if (hitInfo.getCharIndex() >= 0) {
            textFlow.setCaretPosition(hitInfo.getCharIndex());
            dragStart = textFlow.getCaretPosition();
        }
        textFlow.clearSelection();
        getSkinnable().requestFocus();
        e.consume();
    }

    private void mouseDraggedListener(MouseEvent e) {
        HitInfo hitInfo = textFlow.hitTest(new Point2D( e.getX(), e.getY()));
        if (hitInfo.getCharIndex() >= 0) {
            textFlow.setSelection( IndexRange.normalize(dragStart, hitInfo.getCharIndex()));
            textFlow.setCaretPosition(hitInfo.getCharIndex());
        }
        e.consume();
    }

    // So far the most pragmatic way to find prev/next row location is ot use the size of the caret,
    // which always has the height of the row. Adding line spacing to it allows us to find a point which
    // belongs to the desired row. Then using the `hitTest` we can find the related caret position.
    private int getNextRowPosition( boolean down ) {
        Bounds caretBounds = caretShape.getBoundsInLocal();
        double nextRowPos =  down? caretBounds.getMaxY() + textFlow.getLineSpacing():
                caretBounds.getMinY() - textFlow.getLineSpacing();
        HitInfo hitInfo = textFlow.hitTest(new Point2D( caretBounds.getMinX(), nextRowPos));
        return hitInfo.getCharIndex();
    }

    private void moveCaret( Direction direction, boolean changeSelection ) {

        IndexRange prevSelection = textFlow.getSelection();
        int prevCaretPosition = textFlow.getCaretPosition();
        switch (direction) {
            case FORWARD:
            case BACK:
                textFlow.incrementCaretPosition( Direction.FORWARD == direction ? 1:-1);
                break;
            case DOWN:
            case UP:
                int rowCharIndex = getNextRowPosition(Direction.DOWN == direction);
                if (rowCharIndex >= 0) {
                    textFlow.setCaretPosition(rowCharIndex);
                }
                break;
        }

        if (changeSelection) {
            int pos = Tools.isIndexRangeValid(prevSelection)?
                    prevCaretPosition == prevSelection.getStart()? prevSelection.getEnd(): prevSelection.getStart():
                    prevCaretPosition;
            textFlow.setSelection(IndexRange.normalize(pos, textFlow.getCaretPosition()));
        } else {
            textFlow.clearSelection();
        }

    }

    private static boolean isPrintableChar(char c) {
        Character.UnicodeBlock changeBlock = Character.UnicodeBlock.of(c);
        return c == '\n' &&
                !Character.isISOControl(c) &&
                !KeyEvent.CHAR_UNDEFINED.equals(String.valueOf(c))&&
                changeBlock != null && changeBlock != Character.UnicodeBlock.SPECIALS;
    }

    private static KeyCombination kc( KeyCode code, Modifier... modifiers ) {
        return  new KeyCodeCombination( code, modifiers );
    }

    private enum ACTION {
        FORWARD, BACK, DOWN, UP, BACKSPACE, DELETE,
        UNDO
    }

    private final Map<ACTION, Consumer<KeyEvent>> actionMap = Map.of(
        ACTION.FORWARD,   e -> moveCaret(Direction.FORWARD, e.isShiftDown()),
        ACTION.BACK,      e -> moveCaret(Direction.BACK, e.isShiftDown()),
        ACTION.DOWN,      e -> moveCaret(Direction.DOWN, e.isShiftDown()),
        ACTION.UP,        e -> moveCaret(Direction.UP, e.isShiftDown()),
        ACTION.BACKSPACE, e -> commandManager.execute(new RemoveTextCommand(-1)),
        ACTION.DELETE,    e -> commandManager.execute(new RemoveTextCommand(0)),
        ACTION.UNDO,      e -> commandManager.undo()
    );

    private static final Map<KeyCombination, ACTION> keyMap = Map.of(
        kc(RIGHT, SHIFT_ANY),      ACTION.FORWARD,
        kc(LEFT,  SHIFT_ANY),      ACTION.BACK,
        kc(DOWN,  SHIFT_ANY),      ACTION.DOWN,
        kc(UP,    SHIFT_ANY),      ACTION.UP,
        kc(BACK_SPACE, SHIFT_ANY), ACTION.BACKSPACE,
        kc(DELETE),                ACTION.DELETE,
        kc(Z, SHORTCUT_DOWN ),     ACTION.UNDO
    );

    private boolean executeKeyboardAction(KeyEvent e) {
        for ( KeyCombination kc: keyMap.keySet()) {
//            System.out.println(kc);
            if (kc.match(e)) {
                ACTION action = keyMap.get(kc);
                actionMap.get(action).accept(e);
//                System.out.println("Executing action: " + action );
                e.consume();
                return true;
            }
        }
        return false;
    }

    private void keyPressedListener(KeyEvent e) {
        executeKeyboardAction(e);
    }

    private void keyTypedListener(KeyEvent e) {
        if ( !executeKeyboardAction(e) ) { //&& isPrintableChar( e.getCharacter().charAt(0) )) {
            // TODO this should only be done for printable chars
            commandManager.execute(new InsertTextCommand(e.getCharacter()));
            e.consume();
        }
    }

}


