package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.model.Paragraph;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.TableDecoration;
import com.gluonhq.richtextarea.model.TextBuffer;
import com.gluonhq.richtextarea.undo.CommandManager;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class ActionCmdTable implements ActionCmd {

    public enum TableOperation {
        CREATE_TABLE,
        ADD_COLUMN_BEFORE,
        ADD_COLUMN_AFTER,
        DELETE_COLUMN,
        ADD_ROW_ABOVE,
        ADD_ROW_BELOW,
        DELETE_ROW
    }
    private final TableDecoration tableDecoration;
    private final TableOperation tableOperation;
    private String text;

    public ActionCmdTable(TableDecoration tableDecoration) {
        this.tableDecoration = tableDecoration;
        this.tableOperation = TableOperation.CREATE_TABLE;
    }

    public ActionCmdTable(TableOperation tableOperation) {
        this.tableDecoration = null;
        this.tableOperation = tableOperation;
    }

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        if (!viewModel.isEditable() || (tableOperation == TableOperation.CREATE_TABLE && tableDecoration == null)) {
            return;
        }
        CommandManager<RichTextAreaViewModel> commandManager = viewModel.getCommandManager();

        // Create table, if there is no one yet
        if (tableOperation == TableOperation.CREATE_TABLE &&
                (viewModel.getDecorationAtParagraph() == null || !viewModel.getDecorationAtParagraph().hasTableDecoration())) {
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
            commandManager.execute(new InsertAndDecorateTableCmd(text, ParagraphDecoration.builder().tableDecoration(tableDecoration).build()));
        } else if (tableOperation != TableOperation.CREATE_TABLE &&
                viewModel.getDecorationAtParagraph() != null && viewModel.getDecorationAtParagraph().hasTableDecoration()) {
            // modify existing table by adding or removing a column or a row
            TableDecoration oldTableDecoration = viewModel.getDecorationAtParagraph().getTableDecoration();
            int caret = viewModel.getCaretPosition();
            int oldColumns = oldTableDecoration.getColumns();
            int oldRows = oldTableDecoration.getRows();

            viewModel.getParagraphWithCaret().ifPresent(p -> {
                text = viewModel.getTextBuffer().getText(p.getStart(), p.getEnd());
                List<Integer> positions = getTablePositions(text, p.getStart());
                int separators = (int) text.substring(0, caret - p.getStart()).codePoints()
                        .filter(c -> c == TextBuffer.ZERO_WIDTH_TABLE_SEPARATOR)
                        .count();
                int currentRow = separators / oldColumns;
                int currentCol = separators % oldColumns;
                switch (tableOperation) {
                    case ADD_ROW_BELOW:
                    case ADD_ROW_ABOVE: {
                        // add a row
                        int newRow = currentRow + (tableOperation == TableOperation.ADD_ROW_BELOW ? 1 : 0);
                        TableDecoration newTableDecoration = TableDecoration.fromTableDecorationInsertingRow(oldTableDecoration, newRow);
                        int newCaret;
                        if (tableOperation == TableOperation.ADD_ROW_BELOW) {
                            // move caret to end of current row
                            newCaret = positions.get((currentRow + 1) * oldColumns - 1);
                        } else {
                            // move caret to beginning of current row
                            newCaret = currentRow == 0 ? p.getStart() : positions.get(currentRow * oldColumns - 1);
                        }
                        viewModel.setCaretPosition(newCaret);
                        commandManager.execute(new InsertAndDecorateTableCmd(("" + TextBuffer.ZERO_WIDTH_TABLE_SEPARATOR).repeat(oldColumns),
                                ParagraphDecoration.builder().tableDecoration(newTableDecoration).build()));
                        viewModel.setCaretPosition(newCaret + (tableOperation == TableOperation.ADD_ROW_BELOW ? 1 : 0));
                    }
                    break;
                    case DELETE_ROW: {
                        if (oldRows > 1) {
                            // remove a row
                            TableDecoration newTableDecoration = TableDecoration.fromTableDecorationDeletingRow(oldTableDecoration, currentRow);
                            // move caret to beginning of current row
                            int newCaret = currentRow == 0 ? p.getStart() : positions.get(currentRow * oldColumns - 1);
                            viewModel.setCaretPosition(newCaret);
                            int length = positions.get((currentRow + 1) * oldColumns - 1) - newCaret + (currentRow == 0 ? 1 : 0);
                            commandManager.execute(new RemoveAndDecorateTableCmd(0, length,
                                    ParagraphDecoration.builder().tableDecoration(newTableDecoration).build()));
                            viewModel.setCaretPosition(newCaret);
                        }
                    }
                    break;
                    case ADD_COLUMN_BEFORE:
                    case ADD_COLUMN_AFTER: {
                        // add a column
                        int newCol = currentCol + (tableOperation == TableOperation.ADD_COLUMN_AFTER ? 1 : 0);
                        TableDecoration newTableDecoration = TableDecoration.fromTableDecorationInsertingColumn(oldTableDecoration, newCol);
                        if (text.endsWith("\n")) {
                            text = text.substring(0, text.length() - 1);
                        }
                        String newText = text;
                        for (int i = oldRows - 1; i >= 0; i--) {
                            int pos;
                            if (tableOperation == TableOperation.ADD_COLUMN_AFTER) {
                                // add separator to end of current column, for each row
                                pos = positions.get(i * oldColumns + currentCol) - p.getStart();
                            } else {
                                // add separator to beginning of current column, for each row
                                pos = currentCol == 0 && i == 0 ? 0 : positions.get(i * oldColumns + currentCol - 1) - p.getStart();
                            }
                            newText = newText.substring(0, pos) + TextBuffer.ZERO_WIDTH_TABLE_SEPARATOR + newText.substring(pos);
                        }
                        viewModel.setCaretPosition(p.getStart());
                        commandManager.execute(new ReplaceAndDecorateTableCmd(0, text.length(), newText,
                                ParagraphDecoration.builder().tableDecoration(newTableDecoration).build()));
                        positions = getTablePositions(newText, p.getStart());
                        viewModel.setCaretPosition(positions.get(newCol));
                    }
                    break;
                    case DELETE_COLUMN: {
                        if (oldColumns > 1) {
                            // remove a column
                            TableDecoration newTableDecoration = TableDecoration.fromTableDecorationDeletingColumn(oldTableDecoration, currentCol);
                            if (text.endsWith("\n")) {
                                text = text.substring(0, text.length() - 1);
                            }
                            String newText = text;
                            for (int i = oldRows - 1; i >= 0; i--) {
                                // remove text from current column, for each row
                                int posStart = currentCol == 0 && i == 0 ? 0 : positions.get(i * oldColumns + currentCol - 1) - p.getStart();
                                int posEnd = positions.get(i * oldColumns + currentCol) - p.getStart();
                                newText = newText.substring(0, posStart) + newText.substring(posEnd);
                            }
                            viewModel.setCaretPosition(p.getStart());
                            commandManager.execute(new ReplaceAndDecorateTableCmd(0, text.length(), newText,
                                    ParagraphDecoration.builder().tableDecoration(newTableDecoration).build()));
                            positions = getTablePositions(newText, p.getStart());
                            viewModel.setCaretPosition(positions.get(currentCol));
                        }
                    }
                    break;
                    default:
                        break;
                }
            });
        }
    }

    @Override
    public BooleanBinding getDisabledBinding(RichTextAreaViewModel viewModel) {
        return Bindings.createBooleanBinding(
                () -> {
                    if (!viewModel.isEditable()) {
                        // table options disabled if control is not editable
                        return true;
                    }
                    if (tableOperation == TableOperation.CREATE_TABLE) {
                        // create table disabled if already exists one
                        return (viewModel.getDecorationAtParagraph() != null && viewModel.getDecorationAtParagraph().hasTableDecoration());
                    } else {
                        if (viewModel.getDecorationAtParagraph() == null || !viewModel.getDecorationAtParagraph().hasTableDecoration()) {
                            // table operations disabled if there is no table
                            return true;
                        }
                        TableDecoration tableDecoration = viewModel.getDecorationAtParagraph().getTableDecoration();
                        int oldColumns = tableDecoration.getColumns();
                        int oldRows = tableDecoration.getRows();
                        if (tableOperation == TableOperation.DELETE_ROW) {
                            // delete row disabled if rows <= 1
                            return oldRows <= 1;
                        }
                        if (tableOperation == TableOperation.DELETE_COLUMN) {
                            // delete row disabled if columns <= 1
                            return oldColumns <= 1;
                        }
                        return false;
                    }
                },
                viewModel.editableProperty(), viewModel.decorationAtParagraphProperty());
    }

    private List<Integer> getTablePositions(String text, int start) {
        List<Integer> positions = IntStream.iterate(text.indexOf(TextBuffer.ZERO_WIDTH_TABLE_SEPARATOR),
                        index -> index >= 0,
                        index -> text.indexOf(TextBuffer.ZERO_WIDTH_TABLE_SEPARATOR, index + 1))
                .boxed()
                .map(i -> i + start)
                .collect(Collectors.toList());
        positions.add(start + text.length() - 1);
        return positions;
    }
}
