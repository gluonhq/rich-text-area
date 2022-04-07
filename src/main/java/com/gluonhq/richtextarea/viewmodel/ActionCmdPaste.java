package com.gluonhq.richtextarea.viewmodel;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;

class ActionCmdPaste implements ActionCmd {

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        if (viewModel.isEditable()) {
            viewModel.clipboardPaste();
        }
    }

    @Override
    public BooleanBinding getDisabledBinding(RichTextAreaViewModel viewModel) {
        return Bindings.createBooleanBinding(() -> !(viewModel.clipboardHasString() || viewModel.clipboardHasImage() || viewModel.clipboardHasUrl())
                        || !viewModel.isEditable(),
                viewModel.caretPositionProperty(), viewModel.editableProperty());
    }
}
