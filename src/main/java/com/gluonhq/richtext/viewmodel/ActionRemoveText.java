package com.gluonhq.richtext.viewmodel;

import com.gluonhq.richtext.Action;

class ActionRemoveText implements Action {

    private final int caretOffset;

    public ActionRemoveText( int caretOffset) {
        this.caretOffset = caretOffset;
    }

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        viewModel.getCommandManager().execute(new RemoveTextCmd(caretOffset));
    }
}
