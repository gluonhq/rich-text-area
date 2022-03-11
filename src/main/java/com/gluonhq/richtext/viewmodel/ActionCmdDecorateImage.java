package com.gluonhq.richtext.viewmodel;

import com.gluonhq.richtext.model.ImageDecoration;

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
