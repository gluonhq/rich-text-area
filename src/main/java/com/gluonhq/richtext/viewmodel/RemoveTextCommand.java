package com.gluonhq.richtext.viewmodel;

import java.util.Objects;

class RemoveTextCommand extends AbstractCommand {

    private int caretOffset;

    public RemoveTextCommand(int caretOffset ) {
        this.caretOffset = caretOffset;
    }

    @Override
    public void redo( RichTextAreaViewModel viewModel ) {
        storeContext(viewModel);
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
