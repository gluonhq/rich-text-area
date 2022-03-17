package com.gluonhq.richtext.viewmodel;

import com.gluonhq.richtext.model.Decoration;
import com.gluonhq.richtext.model.TextDecoration;
import com.gluonhq.richtext.model.NonTextDecoration;

import java.util.Objects;

class DecorateCmd extends AbstractEditCmd {

    private final Decoration decoration;
    private Decoration prevDecoration;

    public DecorateCmd(Decoration decoration) {
        this.decoration = decoration;
    }

    @Override
    public void doRedo(RichTextAreaViewModel viewModel) {
        if (selection.isDefined() || decoration instanceof NonTextDecoration) {
            Objects.requireNonNull(viewModel).decorate(decoration);
        } else {
            prevDecoration = Objects.requireNonNull(viewModel).getDecoration();
            Objects.requireNonNull(viewModel).setDecoration(decoration);
        }
    }

    @Override
    public void doUndo(RichTextAreaViewModel viewModel) {
        if (prevDecoration != null && prevDecoration instanceof TextDecoration) {
            Objects.requireNonNull(viewModel).setDecoration(prevDecoration);
        }
        Objects.requireNonNull(viewModel).undoDecoration();
    }

    @Override
    public String toString() {
        return "DecorateCmd [" + super.toString() + ", " + decoration + "]";
    }
}
