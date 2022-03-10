package com.gluonhq.richtext.viewmodel;

import com.gluonhq.richtext.model.TextDecoration;

import java.util.Objects;

class DecorateTextCmd extends AbstractEditCmd {

    private final TextDecoration decoration;
    private TextDecoration prevDecoration;

    public DecorateTextCmd(TextDecoration decoration) {
        this.decoration = decoration;
    }

    @Override
    public void doRedo(RichTextAreaViewModel viewModel) {
        if (selection.isDefined()) {
            Objects.requireNonNull(viewModel).decorate(decoration);
        } else {
            prevDecoration = Objects.requireNonNull(viewModel).getTextDecoration();
            Objects.requireNonNull(viewModel).setTextDecoration(decoration);
        }
    }

    @Override
    public void doUndo(RichTextAreaViewModel viewModel) {
        if (!selection.isDefined()) {
            Objects.requireNonNull(viewModel).undoDecoration();
        } else {
            if (prevDecoration != null) {
                Objects.requireNonNull(viewModel).setTextDecoration(prevDecoration);
            }
        }
    }

    @Override
    public String toString() {
        return "DecorateTextCmd[" + decoration + "]";
    }

}
