package com.gluonhq.richtext.viewmodel;

import java.util.Objects;

class InsertTextCommand extends AbstractCommand {

    private final String content;

    public InsertTextCommand(String content) {
        this.content = content;
    }

    @Override
    public void redo( RichTextAreaViewModel viewModel ) {
        storeContext(viewModel);
        Objects.requireNonNull(viewModel).insert(content);
    }

    @Override
    public void undo( RichTextAreaViewModel viewModel ) {
    }
}
