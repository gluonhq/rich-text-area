package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.Selection;
import com.gluonhq.richtextarea.model.Decoration;
import com.gluonhq.richtextarea.model.ImageDecoration;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.TextDecoration;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
            // one single textDecoration, without selection, it is applied at caret only (to decorate next characters)
            prevDecoration = viewModel.getDecorationAtCaret();
            viewModel.setDecorationAtCaret(decorations.get(0));
        } else {
            // if decoration at caret is part of a list, it should be applied at the end
            Optional<Decoration> decorationAtCaret = decorations.stream()
                    .filter(decoration -> !selection.isDefined() && decoration instanceof TextDecoration)
                    .findFirst();

            Selection prevSelection = selection;
            if (!selection.isDefined() && decorations.stream().anyMatch(ParagraphDecoration.class::isInstance)) {
                // select current paragraph before applying all decorations, without EOL character
                viewModel.getParagraphWithCaret().ifPresent(p -> {
                    selection = new Selection(p.getStart(), p.getEnd() - (p.getEnd() == viewModel.getTextLength() ? 0 : 1));
                    viewModel.setSelection(selection);
                });
            }
            // apply decorations
            decorations.stream()
                    .filter(decoration -> selection.isDefined() ||
                            decoration instanceof ImageDecoration || decoration instanceof ParagraphDecoration)
                    .forEach(viewModel::decorate);

            // restore selection if needed
            if (prevSelection != selection) {
                viewModel.setSelection(prevSelection);
            }

            // finally, set decoration at caret if present
            decorationAtCaret.ifPresent(viewModel::setDecorationAtCaret);
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
