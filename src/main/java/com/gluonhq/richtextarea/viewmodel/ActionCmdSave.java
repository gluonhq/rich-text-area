package com.gluonhq.richtextarea.viewmodel;

import javafx.beans.binding.BooleanBinding;

class ActionCmdSave implements ActionCmd {

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        viewModel.save();
    }

    @Override
    public BooleanBinding getDisabledBinding(RichTextAreaViewModel viewModel) {
        return viewModel.textBufferProperty().isNull().or(viewModel.savedProperty());
    }
}
