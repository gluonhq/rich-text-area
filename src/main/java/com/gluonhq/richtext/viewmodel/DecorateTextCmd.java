package com.gluonhq.richtext.viewmodel;

import com.gluonhq.richtext.model.TextDecoration;

import java.util.Objects;

class DecorateTextCmd extends AbstractEditCmd {

    private TextDecoration decoration;

    public DecorateTextCmd(TextDecoration decoration) {
        this.decoration = decoration;
    }

    @Override
    public void doRedo( RichTextAreaViewModel viewModel ) {
        Objects.requireNonNull(viewModel).decorate(decoration);
    }

    @Override
    public void doUndo( RichTextAreaViewModel viewModel ) {
        Objects.requireNonNull(viewModel).undo();
    }
}
