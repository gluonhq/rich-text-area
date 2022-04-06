package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.model.ParagraphDecoration;

import java.util.Objects;

class ActionCmdDecorateParagraph implements ActionCmd {

    private final ParagraphDecoration decoration;

    public ActionCmdDecorateParagraph(ParagraphDecoration decoration) {
        this.decoration = decoration;
    }

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        if (viewModel.isEditable()) {
            viewModel.getCommandManager().execute(new DecorateCmd(Objects.requireNonNull(decoration)));
        }
    }
}