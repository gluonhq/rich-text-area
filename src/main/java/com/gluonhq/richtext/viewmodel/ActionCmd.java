package com.gluonhq.richtext.viewmodel;

import javafx.beans.binding.BooleanBinding;

public interface ActionCmd {
    void apply(RichTextAreaViewModel viewModel);

    default BooleanBinding getDisabledBinding(RichTextAreaViewModel viewModel) {
        return null;
    }
}
