package com.gluonhq.richtext.viewmodel;

import com.gluonhq.richtext.Selection;
import javafx.beans.binding.BooleanBinding;

class ActionCmdSelectAll implements ActionCmd {

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        viewModel.setSelection(new Selection(0, viewModel.getTextLength()));
    }

    @Override
    public BooleanBinding getDisabledBinding(RichTextAreaViewModel viewModel) {
        return viewModel.textLengthProperty().lessThanOrEqualTo(0);
    }
}
