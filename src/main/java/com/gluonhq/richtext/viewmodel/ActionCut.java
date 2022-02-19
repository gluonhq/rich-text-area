package com.gluonhq.richtext.viewmodel;

import com.gluonhq.richtext.Action;

class ActionCut implements Action {
    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        viewModel.clipboardCopy(true);
    }
}
