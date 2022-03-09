package com.gluonhq.richtext.viewmodel;

import com.gluonhq.richtext.model.Decoration;

import java.util.Objects;

class DecorateCmd extends AbstractEditCmd {

    private final Decoration decoration;

    public DecorateCmd(Decoration decoration) {
        this.decoration = decoration;
    }

    @Override
    public void doRedo(RichTextAreaViewModel viewModel) {
        Objects.requireNonNull(viewModel).decorate(decoration);
    }

    @Override
    public void doUndo(RichTextAreaViewModel viewModel) {
        Objects.requireNonNull(viewModel).undoDecoration();
    }

    @Override
    public String toString() {
        return "DecorateTextCmd[" + decoration + "]";
    }
}
