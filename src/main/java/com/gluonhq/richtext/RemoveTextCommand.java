package com.gluonhq.richtext;

import javafx.scene.control.IndexRange;

class RemoveTextCommand implements Command {

    private int caretOffset;

    private int caretPosition;
    private IndexRange selection;

    public RemoveTextCommand( int caretOffset ) {
        this.caretOffset = caretOffset;
    }

    @Override
    public void redo( EditableTextFlow textFlow ) {
        this.caretPosition = textFlow.getCaretPosition();
        this.selection = textFlow.getSelection();
        textFlow.removeText(caretOffset);
    }

    @Override
    public void undo( EditableTextFlow textFlow ) {
        //TODO need to know the removed text with all attributes
        //     to be able to add it correctly
        textFlow.setSelection(selection);
        textFlow.setCaretPosition(caretPosition);
    }
}
