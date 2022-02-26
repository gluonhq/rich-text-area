package com.gluonhq.richtext.viewmodel;

import com.gluonhq.richtext.Selection;
import javafx.beans.binding.BooleanBinding;

class ActionCmdCopy implements ActionCmd {

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        viewModel.clipboardCopy(false);
    }

    @Override
    public BooleanBinding getDisabledBinding(RichTextAreaViewModel viewModel) {
        return viewModel.selectionProperty().isEqualTo(Selection.UNDEFINED);
    }
}
