package com.gluonhq.richtext.viewmodel;

import com.gluonhq.richtext.Selection;
import com.gluonhq.richtext.model.TextDecoration;
import javafx.beans.binding.BooleanBinding;

import java.util.Objects;

class ActionCmdDecorateText implements ActionCmd {

    private final TextDecoration decoration;

    public ActionCmdDecorateText(TextDecoration decoration) {
        this.decoration = decoration;
    }

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        if (viewModel.isEditable()) {
            viewModel.getCommandManager().execute(new DecorateCmd(Objects.requireNonNull(decoration)));
        }
    }

    @Override
    public BooleanBinding getDisabledBinding(RichTextAreaViewModel viewModel) {
        return viewModel.selectionProperty().isEqualTo(Selection.UNDEFINED).or(viewModel.editableProperty().not());
    }
}
