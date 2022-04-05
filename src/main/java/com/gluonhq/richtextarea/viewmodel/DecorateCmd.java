package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.model.Decoration;
import com.gluonhq.richtextarea.model.ImageDecoration;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.TextDecoration;

import java.util.Objects;

class DecorateCmd extends AbstractEditCmd {

    private final Decoration decoration;
    private Decoration prevDecoration;

    public DecorateCmd(Decoration decoration) {
        this.decoration = decoration;
    }

    @Override
    public void doRedo(RichTextAreaViewModel viewModel) {
        if (selection.isDefined() || decoration instanceof ImageDecoration || decoration instanceof ParagraphDecoration) {
            Objects.requireNonNull(viewModel).decorate(decoration);
        } else {
            prevDecoration = Objects.requireNonNull(viewModel).getDecorationAtCaret();
            Objects.requireNonNull(viewModel).setDecorationAtCaret(decoration);
        }
    }

    @Override
    public void doUndo(RichTextAreaViewModel viewModel) {
        if (prevDecoration != null && prevDecoration instanceof TextDecoration) {
            Objects.requireNonNull(viewModel).setDecorationAtCaret(prevDecoration);
        }
        Objects.requireNonNull(viewModel).undoDecoration();
    }

    @Override
    public String toString() {
        return "DecorateCmd [" + super.toString() + ", " + decoration + "]";
    }
}
