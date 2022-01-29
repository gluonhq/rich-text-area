package com.gluonhq.richtext.viewmodel;

import javafx.scene.control.IndexRange;

import java.util.Objects;

class InsertTextCommand implements Command {

    private final String content;
    private int caretPosition;
    private IndexRange selection;

    public InsertTextCommand(String content) {
        this.content = content;
    }

    @Override
    public void redo( RichTextAreaViewModel viewModel ) {
        this.caretPosition = viewModel.getCaretPosition();
        this.selection = viewModel.getSelection();
        Objects.requireNonNull(viewModel).insert(content);
    }

    @Override
    public void undo( RichTextAreaViewModel viewModel ) {
    }
}
