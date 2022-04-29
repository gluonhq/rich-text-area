package com.gluonhq.richtextarea.viewmodel;

import java.util.Objects;

class RemoveTextCmd extends AbstractEditCmd {

    private final int caretOffset;
    private final int length;

    public RemoveTextCmd(int caretOffset) {
        this(caretOffset, 1);
    }

    public RemoveTextCmd(int caretOffset, int length) {
        this.caretOffset = caretOffset;
        this.length = length;
    }

    @Override
    public void doRedo(RichTextAreaViewModel viewModel) {
        Objects.requireNonNull(viewModel);
        viewModel.remove(caretOffset, length);
    }

    @Override
    public void doUndo(RichTextAreaViewModel viewModel) {
        Objects.requireNonNull(viewModel);
        viewModel.undo();
    }

    @Override
    public String toString() {
        return "RemoveTextCmd[" + super.toString() + ", " + caretOffset + ", " + length + "]";
    }
}
