package com.gluonhq.richtext.viewmodel;

import javafx.scene.control.IndexRange;

abstract class AbstractCommand {

    private CommandContext context;

    abstract void redo(RichTextAreaViewModel viewModel );
    abstract void undo( RichTextAreaViewModel viewModel );

    protected void storeContext( RichTextAreaViewModel viewModel ) {
        this.context = new CommandContext(viewModel);
    }

    protected CommandContext getContext() {
        return context;
    }
}

class CommandContext {

    private final int caretPosition;
    private final IndexRange selection;

    CommandContext(RichTextAreaViewModel model) {
        this.caretPosition = model.getCaretPosition();
        this.selection = model.getSelection();
    }

    public int getCaretPosition() {
        return caretPosition;
    }

    public IndexRange getSelection() {
        return selection;
    }
}
