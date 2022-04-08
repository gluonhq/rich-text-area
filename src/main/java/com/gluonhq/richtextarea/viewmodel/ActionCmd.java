package com.gluonhq.richtextarea.viewmodel;

import javafx.beans.binding.BooleanBinding;

public interface ActionCmd {
    void apply(RichTextAreaViewModel viewModel);

    default BooleanBinding getDisabledBinding(RichTextAreaViewModel viewModel) {
        return null;
    }
}
