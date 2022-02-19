package com.gluonhq.richtext.viewmodel;

import com.gluonhq.richtext.Action;
import com.gluonhq.richtext.model.TextDecoration;

class ActionDecorateText implements Action {

    private final TextDecoration decoration;

    public ActionDecorateText(TextDecoration decoration) {
        this.decoration = decoration;
    }

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        viewModel.getCommandManager().execute(new DecorateTextCmd(decoration));
    }
}
