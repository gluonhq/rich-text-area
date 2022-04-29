package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.model.Decoration;
import com.gluonhq.richtextarea.model.ParagraphDecoration;

import java.util.Objects;

class InsertAndDecorateTableCmd extends AbstractEditCmd {

    private final String content;
    private final Decoration decoration;

    public InsertAndDecorateTableCmd(String content, Decoration decoration) {
        this.content = content;
        this.decoration = decoration;
    }

    @Override
    public void doRedo(RichTextAreaViewModel viewModel) {
        int caretPosition = viewModel.getCaretPosition();
        // 1. Insert
        if (!content.isEmpty()) {
            viewModel.insert(content);
        }

        // move caret back to paragraph with table
        viewModel.setCaretPosition(caretPosition + (content.startsWith("\n") ? 1 : 0));

        // 2. Decorate
        if (decoration instanceof ParagraphDecoration) {
            Objects.requireNonNull(viewModel).decorate(decoration);
        }
    }

    @Override
    public void doUndo(RichTextAreaViewModel viewModel) {
        // 1. Decorate
        Objects.requireNonNull(viewModel).undoDecoration();
        // 2. Insert
        viewModel.undo();
    }

    @Override
    public String toString() {
        return "InsertAndDecorateTableCmd[" + super.toString() + ", Insert: <" + (content != null ? content.replace("\n", "<n>") : "") + "]>"
                + " <Decorate: [" + decoration + "]>";
    }
}
