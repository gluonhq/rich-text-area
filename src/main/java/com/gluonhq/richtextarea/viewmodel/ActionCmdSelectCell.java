package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.Selection;
import javafx.beans.binding.BooleanBinding;

class ActionCmdSelectCell implements ActionCmd {

    private final Selection selection;

    public ActionCmdSelectCell(Selection selection) {
        this.selection = selection;
    }

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        viewModel.setSelection(selection);
    }

    @Override
    public BooleanBinding getDisabledBinding(RichTextAreaViewModel viewModel) {
        return viewModel.textLengthProperty().lessThanOrEqualTo(0);
    }
}
