package com.gluonhq.richtext.viewmodel;

class ActionCmdInsertText implements ActionCmd {

    private final String text;

    public ActionCmdInsertText(String text) {
        this.text = text;
    }

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        viewModel.getCommandManager().execute(new InsertTextCmd(text));
    }
}
