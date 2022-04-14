package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.TableDecoration;
import com.gluonhq.richtextarea.model.TextBuffer;
import javafx.beans.binding.BooleanBinding;

class ActionCmdInsertTable implements ActionCmd {

    private final String text;
    private final TableDecoration tableDecoration;

    public ActionCmdInsertTable(TableDecoration tableDecoration) {
        int length = tableDecoration.getRows() * tableDecoration.getColumns() - 1;
        this.text = (length <= 0 ? "" : ("" + TextBuffer.ZERO_WIDTH_TABLE_SEPARATOR).repeat(length)) + "\n";
        this.tableDecoration = tableDecoration;
    }

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        if (viewModel.isEditable()) {
            viewModel.clearSelection();
            viewModel.getCommandManager().execute(new InsertTextCmd(text));
            viewModel.moveCaret(RichTextAreaViewModel.Direction.BACK, false, false, false, false);
            viewModel.getCommandManager().execute(new DecorateCmd(ParagraphDecoration.builder().tableDecoration(tableDecoration).build()));
        }
    }

    @Override
    public BooleanBinding getDisabledBinding(RichTextAreaViewModel viewModel) {
        return viewModel.editableProperty().not();
    }
}
