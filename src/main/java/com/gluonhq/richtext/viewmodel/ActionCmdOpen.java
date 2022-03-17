package com.gluonhq.richtext.viewmodel;

import com.gluonhq.richtext.model.Document;

class ActionCmdOpen implements ActionCmd {

    private final Document document;

    public ActionCmdOpen(Document document) {
        this.document = document;
    }

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        viewModel.open(document);
    }

}
