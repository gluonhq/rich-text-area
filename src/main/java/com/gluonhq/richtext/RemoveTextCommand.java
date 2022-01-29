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
    public void redo( RichTextAreaViewModel viewModel ) {
        this.caretPosition = viewModel.getCaretPosition();
        this.selection = viewModel.getSelection();
        Objects.requireNonNull(viewModel).remove(caretOffset);
    }

    @Override
    public void undo( RichTextAreaViewModel viewModel ) {
        //TODO need to know the removed text with all attributes
        //     to be able to add it correctly
//        textFlow.setSelection(selection);
//        textFlow.setCaretPosition(caretPosition);
    }
}
