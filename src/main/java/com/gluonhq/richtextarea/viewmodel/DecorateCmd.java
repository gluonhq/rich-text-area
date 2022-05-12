package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.Selection;
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
        Objects.requireNonNull(viewModel);
        if (!selection.isDefined() && decorations.size() == 1 && decorations.get(0) instanceof TextDecoration) {
            prevDecoration = viewModel.getDecorationAtCaret();
            viewModel.setDecorationAtCaret(decorations.get(0));
        } else {
            Selection prevSelection = selection;
            if (!selection.isDefined() && decorations.stream().anyMatch(ParagraphDecoration.class::isInstance)) {
                // select current paragraph before applying all decorations
                viewModel.getParagraphWithCaret().ifPresent(p -> {
                    selection = new Selection(p.getStart(), p.getEnd());
                    viewModel.setSelection(selection);
                });
            }
            decorations.forEach(decoration -> {
                if (selection.isDefined() || decoration instanceof ImageDecoration || decoration instanceof ParagraphDecoration) {
                    viewModel.decorate(decoration);
                } else {
                    viewModel.setDecorationAtCaret(decoration);
                }
            });
            if (prevSelection != selection) {
                viewModel.setSelection(prevSelection);
            }
        }
    }

    @Override
    public void doUndo(RichTextAreaViewModel viewModel) {
        Objects.requireNonNull(viewModel);
        if (prevDecoration != null && prevDecoration instanceof TextDecoration) {
            viewModel.setDecorationAtCaret(prevDecoration);
        }
        decorations.forEach(decoration -> viewModel.undoDecoration());
    }

    @Override
    public String toString() {
        return "DecorateCmd [" + super.toString() + ", " + decorations + "]";
    }
}
