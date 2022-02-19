package com.gluonhq.richtext.viewmodel;

import com.gluonhq.richtext.Action;

class ActionCopy implements Action {
    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        viewModel.clipboardCopy(false);
    }
}
