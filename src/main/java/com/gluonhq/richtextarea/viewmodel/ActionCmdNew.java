package com.gluonhq.richtextarea.viewmodel;

class ActionCmdNew implements ActionCmd {

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        viewModel.newDocument();
    }

}
