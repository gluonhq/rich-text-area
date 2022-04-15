package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.TableDecoration;
import com.gluonhq.richtextarea.model.TextBuffer;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;

class ActionCmdInsertTable implements ActionCmd {

    private final TableDecoration tableDecoration;
    private String text;

    public ActionCmdInsertTable(TableDecoration tableDecoration) {
        this.tableDecoration = tableDecoration;
    }

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        if (viewModel.isEditable() &&
                (viewModel.getDecorationAtParagraph() == null || !viewModel.getDecorationAtParagraph().hasTableDecoration()) &&
                tableDecoration != null) {
            viewModel.clearSelection();

            int length = tableDecoration.getRows() * tableDecoration.getColumns() - 1;
            text = (length <= 0 ? "" : ("" + TextBuffer.ZERO_WIDTH_TABLE_SEPARATOR).repeat(length));
            viewModel.getParagraphWithCaret().ifPresent(p -> {
                if (p.getEnd() - p.getStart() > 1) {
                    text = "\n" + text;
                }
                if (viewModel.getCaretPosition() != p.getEnd() - 1) {
                    text = text + "\n";
                }
            });

            viewModel.getCommandManager().execute(new InsertTextCmd(text));
            viewModel.moveCaret(RichTextAreaViewModel.Direction.BACK, false, false, false, false);
            viewModel.getCommandManager().execute(new DecorateCmd(ParagraphDecoration.builder().tableDecoration(tableDecoration).build()));
        }
    }

    @Override
    public BooleanBinding getDisabledBinding(RichTextAreaViewModel viewModel) {
        return Bindings.createBooleanBinding(
                () -> !viewModel.isEditable() ||
                        (viewModel.getDecorationAtParagraph() != null && viewModel.getDecorationAtParagraph().hasTableDecoration()),
                viewModel.editableProperty(), viewModel.decorationAtParagraphProperty());
    }
}
