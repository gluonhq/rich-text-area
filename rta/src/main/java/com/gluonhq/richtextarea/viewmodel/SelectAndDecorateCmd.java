package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.Selection;
import com.gluonhq.richtextarea.model.Decoration;
import com.gluonhq.richtextarea.model.TextDecoration;

import java.util.Objects;

public class SelectAndDecorateCmd extends AbstractEditCmd {

    private final Selection selection;
    private final Decoration decoration;
    private Decoration prevDecoration;

    public SelectAndDecorateCmd(Selection selection, Decoration decoration) {
        this.selection = Objects.requireNonNull(selection);
        this.decoration = Objects.requireNonNull(decoration);
    }

    @Override
    protected void doUndo(RichTextAreaViewModel viewModel) {
        Objects.requireNonNull(viewModel);
        if (prevDecoration != null && prevDecoration instanceof TextDecoration) {
            viewModel.setDecorationAtCaret(prevDecoration);
        }
        viewModel.undo();
    }

    @Override
    protected void doRedo(RichTextAreaViewModel viewModel) {
        Objects.requireNonNull(viewModel);
        if (viewModel.isEditable()) {
            prevDecoration = viewModel.getDecorationAtCaret();
            viewModel.setSelection(selection);
            viewModel.decorate(decoration);
            viewModel.setSelection(Selection.UNDEFINED);
        }
    }
}
