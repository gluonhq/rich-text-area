package com.gluonhq.richtextarea.viewmodel;

import javafx.beans.binding.BooleanBinding;

import java.util.Objects;

class ActionCmdInsertText implements ActionCmd {

    private final String content;

    public ActionCmdInsertText(String content) {
        this.content = content;
    }

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        if (viewModel.isEditable()) {
            String text;
            if (Objects.requireNonNull(viewModel).getDecorationAtParagraph() != null &&
                    viewModel.getDecorationAtParagraph().hasTableDecoration()) {
                text = content.replace("\n", "");
            } else {
                text = content;
            }
            if (!text.isEmpty()) {
                viewModel.getCommandManager().execute(new InsertTextCmd(text));
            }
        }
    }

    @Override
    public BooleanBinding getDisabledBinding(RichTextAreaViewModel viewModel) {
        return viewModel.editableProperty().not();
    }
}
