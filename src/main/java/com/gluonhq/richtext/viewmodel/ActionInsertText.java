package com.gluonhq.richtext.viewmodel;

import com.gluonhq.richtext.Action;

class ActionInsertText implements Action {

    private final String text;

    public ActionInsertText(String text ) {
        this.text = text;
    }

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        viewModel.getCommandManager().execute(new InsertTextCmd(text));
    }
}
