package com.gluonhq.richtext.viewmodel;

class ActionCmdPaste implements ActionCmd {

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        viewModel.clipboardPaste();
    }
}
