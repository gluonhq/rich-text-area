package com.gluonhq.richtext.viewmodel;

import com.gluonhq.richtext.model.HyperlinkDecoration;

import java.util.Objects;

class ActionCmdDecorateHyperlink implements ActionCmd {

    private final HyperlinkDecoration decoration;

    public ActionCmdDecorateHyperlink(HyperlinkDecoration decoration) {
        this.decoration = decoration;
    }

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        if (viewModel.isEditable()) {
            viewModel.getCommandManager().execute(new DecorateCmd(Objects.requireNonNull(decoration)));
        }
    }
}
