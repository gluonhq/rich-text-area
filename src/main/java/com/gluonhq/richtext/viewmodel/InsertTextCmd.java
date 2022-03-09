package com.gluonhq.richtext.viewmodel;

import java.util.Objects;

class InsertTextCmd extends AbstractEditCmd {

    private final String content;

    public InsertTextCmd(String content) {
        this.content = content;
    }

    @Override
    public void doRedo( RichTextAreaViewModel viewModel ) {
        Objects.requireNonNull(viewModel).insert(content);
    }

    @Override
    public void doUndo( RichTextAreaViewModel viewModel ) {
        Objects.requireNonNull(viewModel).undo();
    }

    @Override
    public String toString() {
        return "InsertTextCmd[" + content + "]";
    }
}
