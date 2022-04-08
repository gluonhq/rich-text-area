package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.model.ImageDecoration;

import java.util.Objects;

class ActionCmdDecorateImage implements ActionCmd {

    private final ImageDecoration decoration;

    public ActionCmdDecorateImage(ImageDecoration decoration) {
        this.decoration = decoration;
    }

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        if (viewModel.isEditable()) {
            viewModel.getCommandManager().execute(new DecorateCmd(Objects.requireNonNull(decoration)));
        }
    }
}
