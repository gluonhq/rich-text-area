package com.gluonhq.richtext.viewmodel;

import java.util.Objects;

class RemoveTextCmd extends AbstractEditCmd {

    private final int caretOffset;

    public RemoveTextCmd(int caretOffset) {
        this.caretOffset = caretOffset;
    }

    @Override
    public void doRedo( RichTextAreaViewModel viewModel ) {
        Objects.requireNonNull(viewModel);
        viewModel.remove(caretOffset);
    }

    @Override
    public void doUndo( RichTextAreaViewModel viewModel ) {
        Objects.requireNonNull(viewModel);
        viewModel.undo();
    }
}
