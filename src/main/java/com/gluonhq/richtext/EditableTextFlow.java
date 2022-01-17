package com.gluonhq.richtext;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import javafx.scene.input.InputMethodRequests;
import javafx.scene.text.HitInfo;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class EditableTextFlow extends TextFlow {

    private int textLength = -1; // negative value == undefined

    ////////// PROPERTIES //////////////////////////////////////////////////////////////

    // caretPositionProperty
    private final IntegerProperty caretPositionProperty = new SimpleIntegerProperty(this, "caretPosition", -1);
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
    private final ObjectProperty<IndexRange> selectionProperty = new SimpleObjectProperty<>(this, "selection", Tools.NO_SELECTION);
    public final ObjectProperty<IndexRange> selectionProperty() {
        return selectionProperty;
    }
    public final IndexRange getSelection() {
        return selectionProperty.get();
    }
    final void setSelection(IndexRange value) {
        IndexRange selection = Objects.requireNonNull(value);
        selection = IndexRange.normalize(selection.getStart(), selection.getEnd());
        if (!Tools.isIndexRangeValid(selection) || selection.getStart() > getTextLength() ) {
            selection = Tools.NO_SELECTION;
        } else if ( selection.getStart() > getTextLength() ){
            selection = IndexRange.normalize( selection.getStart(), getTextLength());
        }
        selectionProperty.set(selection);
    }

    ////////// PUBLIC METHODS //////////////////////////////////////////////////////////////

    public void incrementCaretPosition( final int increment ) {
        int pos = getCaretPosition() + increment;
        if ( pos >= 0 && pos <= getTextLength()) {
            setCaretPosition(pos);
        }
    }

    public int getTextLength() {
        if (textLength < 0) {
            int size = 0;
            for (Node node : getChildren()) {
                if (node instanceof Text) {
                    size += ((Text) node).getText().length();
                }
            }
            textLength = size;
        }
        return textLength;
    }

    void resetTextLength() {
        textLength = -1;
    }

    public boolean hasSelection() {
        return Tools.isIndexRangeValid(getSelection());
    }

    public  void clearSelection() {
        setSelection(Tools.NO_SELECTION);
    }

    // deletes selection if exists and set caret to the start position of the deleted selection
    public void deleteSelection() {
        if ( hasSelection() ) {
            IndexRange selection = getSelection();
            removeText(selection.getStart(), selection.getEnd() - selection.getStart());
            setSelection(Tools.NO_SELECTION);
            setCaretPosition(selection.getStart());
        }
    }

    // Basic text insertion, not tied to the component state
    public boolean insertText( int position, final String content ) {
        return findByPosition(position)
                .map( tl -> {
                    Text textNode = tl.getNode();
                    textNode.setText( Tools.insertText( textNode.getText(), tl.textLocation, content));
                    resetTextLength();
                    return true;
                }).orElse(false);
    }

    // Full text insertion, tied to component state
    public void insertText( final String content ) {
        deleteSelection();
        if ( insertText( getCaretPosition(), content)) {
            incrementCaretPosition(content.length());
        }
    }

    // basic text removal, not tied to the component state
    public boolean removeText( final int start, final int length ) {

        return start >= 0 &&
               start < getTextLength() &&
               findByRange(start, start + length).map(tlr -> {
                    if (tlr.start.nodeIndex == tlr.end.nodeIndex) {
                        Text textNode = tlr.start.getNode();
                        textNode.setText( Tools.deleteText( textNode.getText(), tlr.start.textLocation, tlr.end.textLocation));
                    } else {
                        Text startNode = tlr.start.getNode();
                        startNode.setText(startNode.getText().substring(tlr.start.nodeIndex));
                        Text endNode = tlr.start.getNode();
                        endNode.setText(endNode.getText().substring(0, tlr.end.nodeIndex));
                        if (tlr.end.nodeIndex - tlr.start.nodeIndex > 1) {
                            List<Node> removals = IntStream.of(tlr.start.nodeIndex + 1, tlr.end.nodeIndex - 1).mapToObj(i -> getChildren().get(i)).collect(Collectors.toList());
                            getChildren().removeAll(removals);
                        }
                    }
                    textLength -= length;
                    return true;
                }).orElse(false);
    }

    // basic text removal, tied to the component state
    public void removeText(int caretOffset) {
        if ( hasSelection()) {
            deleteSelection();
        } else {
            int position = getCaretPosition() + caretOffset;
            if ( position >= 0 && position < getTextLength() && removeText(position, 1)) {
                setCaretPosition(position);
            }
        }
    }


    ////////// PRIVATE METHODS //////////////////////////////////////////////////////////////

    private boolean isPositionValid( int position ) {
        return position >= 0 && position < getTextLength();
    }

    // if found, return an appropriate Text node and the position within that node
    Optional<TextLocation> findByPosition(int position) {

        if (isPositionValid(position)) {
            int nodeOffset = 0;
            for (int childIndex = 0; childIndex < getChildren().size(); childIndex++) {
                Node node = getChildren().get(childIndex);
                if (node instanceof Text) {
                    Text textNode = (Text) node;
                    String text = textNode.getText();
                    int relativePosition = position - nodeOffset;
                    if (relativePosition >= 0 && relativePosition < text.length()) {
                        return Optional.of(new TextLocation(childIndex, relativePosition));
                    }
                    nodeOffset += text.length();
                }
                // TODO If we want to support non-Text nodes:
                //   for positions just before the non-Text node or beyond last node - create new Text node
                //   new Text node creation should be controlled by additional parameter
            }
        }
        return Optional.empty();

    }


    Optional<TextLocationRange> findByRange( int textStart, int textEnd ) {

        if (isPositionValid(textStart) && isPositionValid(textEnd)) {

            int start = Math.min(textStart, textEnd);
            int end   = Math.max(textStart, textEnd);


            List<TextLocation> textLocations = new ArrayList<>();

            int nodeOffset = 0;
            for (int childIndex = 0; childIndex < getChildren().size(); childIndex++) {
                Node node = getChildren().get(childIndex);
                if (node instanceof Text) {
                    Text textNode = (Text) node;
                    String text = textNode.getText();

                    int relativeStart = start - nodeOffset;
                    if (relativeStart >= 0 && relativeStart < textNode.getText().length()) {
                        textLocations.add(new TextLocation(childIndex, relativeStart));
                    }

                    int relativeEnd = end - nodeOffset;
                    if (relativeEnd >= 0 && relativeEnd < textNode.getText().length()) {
                        textLocations.add(new TextLocation(childIndex, relativeEnd));
                    }

                    nodeOffset += text.length();
                }

            }

            if (textLocations.size() != 2) {
                throw new RuntimeException("There should be both start and end locations found!");
            }
            return Optional.of(new TextLocationRange(textLocations.get(0), textLocations.get(1)));

        }
        return Optional.empty();

    }

    class TextLocation {

        public final int nodeIndex;
        public final int textLocation;

        TextLocation(int nodeIndex, int textLocation) {
            this.nodeIndex = nodeIndex;
            this.textLocation = textLocation;
        }

        public Text getNode() {
            return (Text) getChildren().get(nodeIndex);
        }

    }

    static class TextLocationRange {
        public final TextLocation start;
        public final TextLocation end;

        TextLocationRange(TextLocation start, TextLocation end) {
            this.start = start;
            this.end = end;
        }
    }



}
