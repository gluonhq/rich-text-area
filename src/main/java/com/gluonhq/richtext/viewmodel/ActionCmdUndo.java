package com.gluonhq.richtext.viewmodel;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;

class ActionCmdUndo implements ActionCmd {

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        if (viewModel.isEditable()) {
            viewModel.getCommandManager().undo();
        }
    }

    @Override
    public BooleanBinding getDisabledBinding(RichTextAreaViewModel viewModel) {
        return Bindings.createBooleanBinding(() -> viewModel.isUndoStackEmpty() || !viewModel.isEditable(),
                viewModel.undoStackEmptyProperty(), viewModel.editableProperty());
    }
}
