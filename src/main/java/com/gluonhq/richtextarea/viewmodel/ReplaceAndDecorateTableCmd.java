package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.model.Decoration;
import com.gluonhq.richtextarea.model.ParagraphDecoration;

import java.util.Objects;

class ReplaceAndDecorateTableCmd extends AbstractEditCmd {

    private final int caretOffset;
    private final int length;
    private final String content;
    private final Decoration decoration;

    public ReplaceAndDecorateTableCmd(int caretOffset, int length, String content, Decoration decoration) {
        this.caretOffset = caretOffset;
        this.length = length;
        this.content = content;
        this.decoration = decoration;
    }

    @Override
    public void doRedo(RichTextAreaViewModel viewModel) {
        // 1. Remove
        Objects.requireNonNull(viewModel);
        viewModel.remove(caretOffset, length);

        // 2. Insert
        int caretPosition = viewModel.getCaretPosition();
        if (!content.isEmpty()) {
            viewModel.insert(content);
        }

        // move caret back to paragraph with table
        viewModel.setCaretPosition(caretPosition + (content.length() > 1 && content.startsWith("\n") ? 1 : 0));

        // 3. Decorate
        if (decoration instanceof ParagraphDecoration) {
            Objects.requireNonNull(viewModel).decorate(decoration);
        }
    }

    @Override
    public void doUndo(RichTextAreaViewModel viewModel) {
        // 1. Decorate
        if (decoration instanceof ParagraphDecoration) {
            Objects.requireNonNull(viewModel).undoDecoration();
        }
        // 2. Insert
        if (!content.isEmpty()) {
            viewModel.undo();
        }
        // 3- Remove
        viewModel.undo();
    }

    @Override
    public String toString() {
        return "ReplaceAndDecorateTableCmd[" + super.toString() + ", Remove <" + caretOffset + ", " + length + "]> " +
                ", Insert: <" + (content != null ? content.replace("\n", "<n>") : "") + "]>" +
                " <Decorate: [" + decoration + "]>";
    }
}
