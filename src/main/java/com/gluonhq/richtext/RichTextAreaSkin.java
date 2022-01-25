package com.gluonhq.richtext;

import com.gluonhq.richtext.model.PieceTable;
import com.gluonhq.richtext.model.TextChangeListener;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.Observable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import javafx.util.Duration;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyCombination.*;


class RichTextAreaSkin extends SkinBase<RichTextArea> {

    private final PieceTable textBuffer = new PieceTable("Simple text text text");

    private final EditableTextFlow textFlow = new EditableTextFlow();
    private final CommandManager commandManager = new CommandManager(this);
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

        textBuffer.addChangeListener(textChangeListener);
        refreshTextFlow();

    }

    /// PROPERTIES ///////////////////////////////////////////////////////////////


    // caretPositionProperty
    private final IntegerProperty caretPositionProperty = new SimpleIntegerProperty(this, "caretPosition", -1){
        @Override
        public void set(int value) {
            updateCaretShape(value);
            super.set(value);
        }

        private void updateCaretShape(int newPos) {
            caretShape.getElements().clear();
            if (newPos < 0 ) {
                caretTimeline.stop();
            } else {
                caretShape.getElements().addAll(textFlow.caretShape(newPos, true));
                caretTimeline.play();
            }
        }
    };
    public final IntegerProperty caretPositionProperty() {
        return caretPositionProperty;
    }
    public final int getCaretPosition() {
        return caretPositionProperty.get();
    }
    public final void setCaretPosition(int value) {
        caretPositionProperty.set(value);
    }


    // selectionProperty
    private final ObjectProperty<IndexRange> selectionProperty = new SimpleObjectProperty<>(this, "selection", Tools.NO_SELECTION) {
        @Override
        public void set(IndexRange value) {
            IndexRange selection = Objects.requireNonNull(value);
            selection = IndexRange.normalize(selection.getStart(), selection.getEnd());
            if (!Tools.isIndexRangeValid(selection) || selection.getStart() > getTextLength() ) {
                selection = Tools.NO_SELECTION;
            } else if ( selection.getStart() > getTextLength() ){
                selection = IndexRange.normalize( selection.getStart(), getTextLength());
            }
            updateSelectionShape(selection);
            super.set(selection);
        }

        private void updateSelectionShape( IndexRange selection ) {
            selectionShape.getElements().clear();
            if ( selection != null && Tools.isIndexRangeValid(selection)) {
                selectionShape.getElements().setAll(textFlow.rangeShape( selection.getStart(), selection.getEnd() ));
            }
        }
    };
    public final ObjectProperty<IndexRange> selectionProperty() {
        return selectionProperty;
    }
    public final IndexRange getSelection() {
        return selectionProperty.get();
    }
    final void setSelection(IndexRange value) {
        selectionProperty.set(value);
    }


    /// PUBLIC METHODS  /////////////////////////////////////////////////////////

    @Override
    public void dispose() {
        getSkinnable().setEditable(false); // removes all related listeners
        getSkinnable().editableProperty().removeListener(this::editableChangeListener);
        textBuffer.removeChangeListener(textChangeListener);
    }

    public int getTextLength() {
        return textBuffer.getTextLength();
    }

    public void moveCaretPosition(final int charCount) {
        int pos = getCaretPosition() + charCount;
        if ( pos >= 0 && pos <= getTextLength()) {
            setCaretPosition(pos);
        }
    }

    public void insert( String text ) {
        if (hasSelection()) {
            removeSelection();
        }
        textBuffer.insert(text, getCaretPosition());
        moveCaretPosition(1);
    }

    public void remove(int caretOffset) {
        if (hasSelection()) {
            removeSelection();
        } else {
            int position = getCaretPosition() + caretOffset;
            if (position >= 0 && position < getTextLength() ) {
                textBuffer.delete(position, 1);
                setCaretPosition(position);
            }
        }
    }

    public boolean hasSelection() {
        return Tools.isIndexRangeValid(getSelection());
    }

    public  void clearSelection() {
        setSelection(Tools.NO_SELECTION);
    }

    // deletes selection if exists and set caret to the start position of the deleted selection
    public void removeSelection() {
        if ( hasSelection() ) {
            IndexRange selection = getSelection();
            textBuffer.delete(selection.getStart(), selection.getEnd() - selection.getStart() );
            setSelection(Tools.NO_SELECTION);
            setCaretPosition(selection.getStart());
        }
    }

    /// PRIVATE METHODS /////////////////////////////////////////////////////////

    private void refreshTextFlow() {
        var fragments = textBuffer.getPieces().stream()
                .map(piece -> piece.getDecoration().asText( piece.getText()))
                .collect(Collectors.toList());
        textFlow.getChildren().setAll(fragments);
    }

    private void editableChangeListener(Observable o ) {

        boolean editable = getSkinnable().isEditable();

        clearSelection();
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

        setCaretPosition( editable? 0:-1 );
        textFlow.setCursor( editable? Cursor.TEXT: Cursor.DEFAULT);

    }

    private void setCaretVisibility(boolean on) {
        if (caretShape.getElements().size() > 0) {
            // Opacity is used since we don't want the changing caret size to affect the layout
            // Otherwise text appears to be jumping
            caretShape.setOpacity( on? 1: 0 );
        }
    }

    private int dragStart = -1;

    private void mousePressedListener(MouseEvent e) {
        HitInfo hitInfo = textFlow.hitTest(new Point2D( e.getX(), e.getY()));
        if (hitInfo.getCharIndex() >= 0) {
            setCaretPosition(hitInfo.getCharIndex());
            dragStart = getCaretPosition();
        }
        clearSelection();
        getSkinnable().requestFocus();
        e.consume();
    }

    private void mouseDraggedListener(MouseEvent e) {
        HitInfo hitInfo = textFlow.hitTest(new Point2D( e.getX(), e.getY()));
        if (hitInfo.getCharIndex() >= 0) {
            setSelection( IndexRange.normalize(dragStart, hitInfo.getCharIndex()));
            setCaretPosition(hitInfo.getCharIndex());
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

        IndexRange prevSelection = getSelection();
        int prevCaretPosition = getCaretPosition();
        switch (direction) {
            case FORWARD:
            case BACK:
                moveCaretPosition( Direction.FORWARD == direction ? 1:-1);
                break;
            case DOWN:
            case UP:
                int rowCharIndex = getNextRowPosition(Direction.DOWN == direction);
                if (rowCharIndex >= 0) {
                    setCaretPosition(rowCharIndex);
                }
                break;
        }

        if (changeSelection) {
            int pos = Tools.isIndexRangeValid(prevSelection)?
                prevCaretPosition == prevSelection.getStart()? prevSelection.getEnd(): prevSelection.getStart():
                prevCaretPosition;
            setSelection(IndexRange.normalize(pos, getCaretPosition()));
        } else {
            clearSelection();
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
            if (kc.match(e)) {
                ACTION action = keyMap.get(kc);
                actionMap.get(action).accept(e);
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