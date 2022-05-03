package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.model.Decoration;
import javafx.beans.binding.BooleanBinding;

import java.util.Objects;

class ActionCmdInsertAndDecorate implements ActionCmd {

    private final String content;
    private final Decoration decoration;

    public ActionCmdInsertAndDecorate(String content, Decoration decoration) {
        this.content = content;
        this.decoration = decoration;
    }

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        if (viewModel.isEditable()) {
            viewModel.getCommandManager().execute(new InsertAndDecorateTableCmd(content, Objects.requireNonNull(decoration)));
        }
    }

    @Override
    public BooleanBinding getDisabledBinding(RichTextAreaViewModel viewModel) {
        return viewModel.editableProperty().not();
    }
}
