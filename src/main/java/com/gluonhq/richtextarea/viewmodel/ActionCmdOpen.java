package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.model.Document;

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
