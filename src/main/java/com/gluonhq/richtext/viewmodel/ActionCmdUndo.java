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
        return Bindings.createBooleanBinding(() -> viewModel.getUndoStackSize() == 0 || !viewModel.isEditable(),
                viewModel.undoStackSizeProperty(), viewModel.editableProperty());
    }
}
