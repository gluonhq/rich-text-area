package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.Selection;
import com.gluonhq.richtextarea.model.Decoration;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.TextDecoration;
import javafx.beans.binding.BooleanBinding;

import java.util.List;
import java.util.Objects;

class ActionCmdDecorate implements ActionCmd {

    private final List<Decoration> decorations;

    public ActionCmdDecorate(Decoration... decorations) {
        this.decorations = List.of(decorations);
    }

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        if (viewModel.isEditable()) {
            viewModel.getCommandManager().execute(new DecorateCmd(Objects.requireNonNull(decorations)));
        }
    }

    @Override
    public BooleanBinding getDisabledBinding(RichTextAreaViewModel viewModel) {
        return viewModel.selectionProperty().isEqualTo(Selection.UNDEFINED).or(viewModel.editableProperty().not());
    }
}
