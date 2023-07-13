package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.Selection;
import com.gluonhq.richtextarea.model.Decoration;
import javafx.beans.binding.BooleanBinding;

import java.util.Objects;

public class ActionCmdSelectAndDecorate implements ActionCmd {

    private final Selection selection;
    private final Decoration decoration;

    public ActionCmdSelectAndDecorate(Selection selection, Decoration decoration) {
        this.selection = Objects.requireNonNull(selection);
        this.decoration = Objects.requireNonNull(decoration);
    }

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        if (viewModel.isEditable()) {
            viewModel.getCommandManager().execute(new SelectAndDecorateCmd(selection, decoration));
        }
    }

    @Override
    public BooleanBinding getDisabledBinding(RichTextAreaViewModel viewModel) {
        return viewModel.editableProperty().not();
    }
}
