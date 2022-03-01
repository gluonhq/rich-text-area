package com.gluonhq.richtext.viewmodel;

import javafx.beans.binding.BooleanBinding;

class ActionCmdRemoveText implements ActionCmd {

    private final int caretOffset;

    public ActionCmdRemoveText(int caretOffset) {
        this.caretOffset = caretOffset;
    }

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        if (viewModel.isEditable()) {
            viewModel.getCommandManager().execute(new RemoveTextCmd(caretOffset));
        }
    }

    @Override
    public BooleanBinding getDisabledBinding(RichTextAreaViewModel viewModel) {
        return viewModel.editableProperty().not();
    }
}
