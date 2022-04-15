package com.gluonhq.richtextarea.viewmodel;

import java.util.Objects;

class InsertTextCmd extends AbstractEditCmd {

    private final String content;

    public InsertTextCmd(String content) {
        this.content = content;
    }

    @Override
    public void doRedo( RichTextAreaViewModel viewModel ) {
        String text;
        if (Objects.requireNonNull(viewModel).getDecorationAtParagraph() != null &&
                viewModel.getDecorationAtParagraph().hasTableDecoration()) {
            text = content.replace("\n", "");
        } else {
            text = content;
        }
        if (!text.isEmpty()) {
            viewModel.insert(text);
        }
    }

    @Override
    public void doUndo( RichTextAreaViewModel viewModel ) {
        Objects.requireNonNull(viewModel).undo();
    }

    @Override
    public String toString() {
        return "InsertTextCmd[" + super.toString() + ", " + (content != null ? content.replace("\n", "<n>") : "") + "]";
    }
}
