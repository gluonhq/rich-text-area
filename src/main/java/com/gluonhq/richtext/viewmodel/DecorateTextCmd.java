package com.gluonhq.richtext.viewmodel;

import com.gluonhq.richtext.Selection;

import java.util.Objects;

class DecorateTextCmd extends AbstractEditCmd {

    public DecorateTextCmd() {}

    @Override
    public void doRedo( RichTextAreaViewModel viewModel ) {
        Objects.requireNonNull(viewModel).decorate();
    }

    @Override
    public void doUndo( RichTextAreaViewModel viewModel ) {
        Objects.requireNonNull(viewModel).undo();
    }
}
