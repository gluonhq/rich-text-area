package com.gluonhq.richtext.viewmodel;

class ActionCmdNew implements ActionCmd {

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        viewModel.newFaceModel();
    }

}
