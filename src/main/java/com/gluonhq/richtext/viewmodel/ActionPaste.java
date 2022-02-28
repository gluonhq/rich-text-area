package com.gluonhq.richtext.viewmodel;

import com.gluonhq.richtext.Action;

class ActionPaste implements Action {
    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        viewModel.clipboardPaste();
    }
}
