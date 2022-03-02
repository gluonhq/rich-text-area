package com.gluonhq.richtext.viewmodel;

import javafx.beans.binding.BooleanBinding;

class ActionCmdInsertText implements ActionCmd {

    private final String text;

    public ActionCmdInsertText(String text) {
        this.text = text;
    }

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        if (viewModel.isEditable()) {
            viewModel.getCommandManager().execute(new InsertTextCmd(text));
        }
    }

    @Override
    public BooleanBinding getDisabledBinding(RichTextAreaViewModel viewModel) {
        return viewModel.editableProperty().not();
    }
}
