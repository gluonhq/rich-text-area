package com.gluonhq.richtext.viewmodel;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;

class ActionCmdRedo implements ActionCmd {

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        viewModel.getCommandManager().redo();
    }

    @Override
    public BooleanBinding getDisabledBinding(RichTextAreaViewModel viewModel) {
        return Bindings.createBooleanBinding(viewModel::isRedoStackEmpty, viewModel.redoStackEmptyProperty());
    }
}
