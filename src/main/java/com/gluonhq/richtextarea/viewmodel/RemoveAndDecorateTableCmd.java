package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.model.Decoration;
import com.gluonhq.richtextarea.model.ParagraphDecoration;

import java.util.Objects;

class RemoveAndDecorateTableCmd extends AbstractEditCmd {

    private final int caretOffset;
    private final int length;
    private final Decoration decoration;

    public RemoveAndDecorateTableCmd(int caretOffset, int length, Decoration decoration) {
        this.caretOffset = caretOffset;
        this.length = length;
        this.decoration = decoration;
    }

    @Override
    public void doRedo(RichTextAreaViewModel viewModel) {
        // 1. Remove
        Objects.requireNonNull(viewModel);
        viewModel.remove(caretOffset, length);
        // 2. Decorate
        if (decoration instanceof ParagraphDecoration) {
            Objects.requireNonNull(viewModel).decorate(decoration);
        }
    }

    @Override
    public void doUndo(RichTextAreaViewModel viewModel) {
        // 1. Decorate
        Objects.requireNonNull(viewModel).undoDecoration();
        // 2. Remove
        viewModel.undo();
    }

    @Override
    public String toString() {
        return "RemoveAndDecorateTableCmd[" + super.toString() + ", Remove <" + caretOffset + ", " + length + "]> " +
                " <Decorate: [" + decoration + "]>";
    }
}
