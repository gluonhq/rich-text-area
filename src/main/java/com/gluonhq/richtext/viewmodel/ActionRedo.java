package com.gluonhq.richtext.viewmodel;

import com.gluonhq.richtext.Action;

class ActionRedo implements Action {
    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        viewModel.getCommandManager().redo();
    }
}
