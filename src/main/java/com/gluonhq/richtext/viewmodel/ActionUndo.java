package com.gluonhq.richtext.viewmodel;

import com.gluonhq.richtext.Action;

class ActionUndo implements Action {
    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        viewModel.getCommandManager().undo();
    }
}
