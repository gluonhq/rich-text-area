package com.gluonhq.richtext.viewmodel;

import com.gluonhq.richtext.model.FaceModel;

class ActionCmdOpen implements ActionCmd {

    private final FaceModel faceModel;

    public ActionCmdOpen(FaceModel faceModel) {
        this.faceModel = faceModel;
    }

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        viewModel.open(faceModel);
    }

}
