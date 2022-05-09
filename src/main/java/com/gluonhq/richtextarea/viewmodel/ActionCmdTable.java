package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.Selection;
import com.gluonhq.richtextarea.model.Paragraph;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.Table;
import com.gluonhq.richtextarea.model.TableDecoration;
import com.gluonhq.richtextarea.model.TextBuffer;
import com.gluonhq.richtextarea.undo.CommandManager;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.text.TextAlignment;

import static com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel.Direction;

class ActionCmdTable implements ActionCmd {

    public enum TableOperation {
        CREATE_TABLE,
        DELETE_TABLE,

        ADD_COLUMN_BEFORE,
        ADD_COLUMN_AFTER,
        DELETE_COLUMN,
        ADD_ROW_ABOVE,
        ADD_ROW_BELOW,
        DELETE_ROW,

        DELETE_CELL_CONTENT,
        ALIGN_CELL_CONTENT
    }
    private final TableDecoration tableDecoration;
    private final TableOperation tableOperation;
    private final TextAlignment textAlignment;
    private String text;

    public ActionCmdTable(TableDecoration tableDecoration) {
        this(tableDecoration, TableOperation.CREATE_TABLE, null);
    }

    public ActionCmdTable(TableOperation tableOperation) {
        this(null, tableOperation, null);
    }

    public ActionCmdTable(TextAlignment textAlignment) {
        this(null, TableOperation.ALIGN_CELL_CONTENT, textAlignment);
    }

    ActionCmdTable(TableDecoration tableDecoration, TableOperation tableOperation, TextAlignment textAlignment) {
        this.tableDecoration = tableDecoration;
        this.tableOperation = tableOperation;
        this.textAlignment = textAlignment;
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
            viewModel.clearSelection();
            // modify existing table by adding or removing a column or a row
            TableDecoration oldTableDecoration = viewModel.getDecorationAtParagraph().getTableDecoration();
            int caret = viewModel.getCaretPosition();
            int oldColumns = oldTableDecoration.getColumns();
            int oldRows = oldTableDecoration.getRows();

            viewModel.getParagraphWithCaret().ifPresent(p -> {
                text = viewModel.getTextBuffer().getText(p.getStart(), p.getEnd());
                Table table = new Table(text, p.getStart(), oldRows, oldColumns);
                int currentRow = table.getCurrentRow(caret);
                int currentCol = table.getCurrentColumn(caret);
                switch (tableOperation) {
                    case ADD_ROW_BELOW:
                    case ADD_ROW_ABOVE: {
                        Direction direction = tableOperation == TableOperation.ADD_ROW_BELOW ? Direction.DOWN : Direction.UP;
                        // add a row
                        int newRow = table.getNextRow(caret, direction);
                        TableDecoration newTableDecoration = TableDecoration.fromTableDecorationInsertingRow(oldTableDecoration, newRow);
                        int newCaret = table.getCaretAt(caret, direction);
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
                            int newCaret = table.getCaretAt(caret, Direction.UP);
                            viewModel.setCaretPosition(newCaret);
                            int length = table.getRowLength(caret);
                            commandManager.execute(new RemoveAndDecorateTableCmd(0, length,
                                    ParagraphDecoration.builder().tableDecoration(newTableDecoration).build()));
                            viewModel.setCaretPosition(newCaret);
                        }
                    }
                    break;
                    case ADD_COLUMN_BEFORE:
                    case ADD_COLUMN_AFTER: {
                        Direction direction = tableOperation == TableOperation.ADD_COLUMN_AFTER ? Direction.FORWARD : Direction.BACK;
                        // add a column
                        int newCol = table.getNextColumn(caret, direction);
                        TableDecoration newTableDecoration = TableDecoration.fromTableDecorationInsertingColumn(oldTableDecoration, newCol);
                        String newText = table.addColumnAndGetTableText(caret, direction);
                        viewModel.setCaretPosition(p.getStart());
                        commandManager.execute(new ReplaceAndDecorateTableCmd(0, table.getTableTextLength(), newText,
                                ParagraphDecoration.builder().tableDecoration(newTableDecoration).build()));
                        viewModel.setCaretPosition(new Table(newText, p.getStart(), oldRows, oldColumns + 1).getCaretAtColumn(newCol));
                    }
                    break;
                    case DELETE_COLUMN: {
                        if (oldColumns > 1) {
                            // remove a column
                            TableDecoration newTableDecoration = TableDecoration.fromTableDecorationDeletingColumn(oldTableDecoration, currentCol);
                            String newText = table.removeColumnAndGetText(caret);
                            viewModel.setCaretPosition(p.getStart());
                            commandManager.execute(new ReplaceAndDecorateTableCmd(0, table.getTableTextLength(), newText,
                                    ParagraphDecoration.builder().tableDecoration(newTableDecoration).build()));
                            viewModel.setCaretPosition(new Table(newText, p.getStart(), oldRows, oldColumns - 1).getCaretAtColumn(Math.max(currentCol - 1, 0)));
                        }
                    }
                    break;
                    case DELETE_TABLE: {
                        viewModel.setCaretPosition(p.getStart());
                        commandManager.execute(new RemoveAndDecorateTableCmd(0, p.getEnd() - p.getStart() - 1, ParagraphDecoration.builder().presets().build()));
                        viewModel.setCaretPosition(Math.max(p.getStart() - 1, 0));
                    }
                    break;
                    case DELETE_CELL_CONTENT: {
                        Selection cellSelection = table.getCellSelection(caret);
                        if (cellSelection.isDefined()) {
                            viewModel.setCaretPosition(cellSelection.getStart());
                            commandManager.execute(new RemoveTextCmd(0, cellSelection.getEnd() - cellSelection.getStart()));
                        }
                    }
                    break;
                    case ALIGN_CELL_CONTENT: {
                        oldTableDecoration.getCellAlignment()[currentRow][currentCol] = textAlignment;
                        commandManager.execute(new DecorateCmd(ParagraphDecoration.builder().tableDecoration(oldTableDecoration).build()));
                    }
                    break;
                    default:
                        break;
                }
            });
        }
        viewModel.getParagraphWithCaret().filter(p -> p.getEnd() > 0 && p.getDecoration().hasTableDecoration()).ifPresent(p -> {
            text = viewModel.getTextBuffer().getText(p.getStart(), p.getEnd());
            TableDecoration tableDecoration = viewModel.getDecorationAtParagraph().getTableDecoration();
            new Table("[" + text, 0, tableDecoration.getRows(), tableDecoration.getColumns()).printTable();
        });
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
                        Paragraph paragraph = viewModel.getParagraphWithCaret().orElse(null);
                        if (paragraph != null) {
                            int caret = viewModel.getCaretPosition();
                            text = viewModel.getTextBuffer().getText(paragraph.getStart(), paragraph.getEnd());
                            Table table = new Table(text, paragraph.getStart(), oldRows, oldColumns);
                            if (tableOperation == TableOperation.DELETE_CELL_CONTENT) {
                                // disable if cell has no content
                                return table.isCaretAtEmptyCell(caret);
                            }
                            if (tableOperation == TableOperation.ALIGN_CELL_CONTENT) {
                                // disable if alignment is the same
                                return (tableDecoration.getCellAlignment()[table.getCurrentRow(caret)][table.getCurrentColumn(caret)] == textAlignment);
                            }
                        }
                        return false;
                    }
                },
                viewModel.editableProperty(), viewModel.decorationAtParagraphProperty(), viewModel.caretPositionProperty());
    }

}
