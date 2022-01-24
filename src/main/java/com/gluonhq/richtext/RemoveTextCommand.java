package com.gluonhq.richtext;

import com.gluonhq.richtext.model.TextBuffer;
import javafx.scene.control.IndexRange;

import java.util.Objects;

class RemoveTextCommand implements Command {

    private int caretOffset;
    private int caretPosition;
    private IndexRange selection;

    public RemoveTextCommand( int caretOffset ) {
        this.caretOffset = caretOffset;
    }

    @Override
    public void redo(RichTextAreaSkin skin) {
        this.caretPosition = skin.getCaretPosition();
        this.selection = skin.getSelection();
        Objects.requireNonNull(skin).remove(caretOffset);
    }

    @Override
    public void undo( RichTextAreaSkin skin ) {
        //TODO need to know the removed text with all attributes
        //     to be able to add it correctly
//        textFlow.setSelection(selection);
//        textFlow.setCaretPosition(caretPosition);
    }
}
