package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.model.Decoration;
import com.gluonhq.richtextarea.model.ImageDecoration;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.TextDecoration;

import java.util.List;
import java.util.Objects;

class DecorateCmd extends AbstractEditCmd {

    private final List<Decoration> decorations;
    private Decoration prevDecoration;

    public DecorateCmd(Decoration decoration) {
        this(List.of(decoration));
    }

    public DecorateCmd(List<Decoration> decorations) {
        this.decorations = decorations;
    }

    @Override
    public void doRedo(RichTextAreaViewModel viewModel) {
        if (!selection.isDefined() && decorations.size() == 1 && decorations.get(0) instanceof TextDecoration) {
            prevDecoration = Objects.requireNonNull(viewModel).getDecorationAtCaret();
            Objects.requireNonNull(viewModel).setDecorationAtCaret(decorations.get(0));
        } else {
            decorations.forEach(decoration -> {
                if (selection.isDefined() || decoration instanceof ImageDecoration || decoration instanceof ParagraphDecoration) {
                    Objects.requireNonNull(viewModel).decorate(decoration);
                } else {
                    Objects.requireNonNull(viewModel).setDecorationAtCaret(decoration);
                }
            });
        }
    }

    @Override
    public void doUndo(RichTextAreaViewModel viewModel) {
        if (prevDecoration != null && prevDecoration instanceof TextDecoration) {
            Objects.requireNonNull(viewModel).setDecorationAtCaret(prevDecoration);
        }
        decorations.forEach(decoration -> Objects.requireNonNull(viewModel).undoDecoration());
    }

    @Override
    public String toString() {
        return "DecorateCmd [" + super.toString() + ", " + decorations + "]";
    }
}
