package com.gluonhq.richtextarea.viewmodel;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;

class ActionCmdRedo implements ActionCmd {

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        if (viewModel.isEditable()) {
            viewModel.getCommandManager().redo();
        }
    }

    @Override
    public BooleanBinding getDisabledBinding(RichTextAreaViewModel viewModel) {
        return Bindings.createBooleanBinding(() -> viewModel.getRedoStackSize() == 0 || !viewModel.isEditable(),
                viewModel.redoStackSizeProperty(), viewModel.editableProperty());
    }
}
