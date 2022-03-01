package com.gluonhq.richtext.viewmodel;

class ActionCmdRemoveText implements ActionCmd {

    private final int caretOffset;

    public ActionCmdRemoveText(int caretOffset) {
        this.caretOffset = caretOffset;
    }

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        viewModel.getCommandManager().execute(new RemoveTextCmd(caretOffset));
    }

}
