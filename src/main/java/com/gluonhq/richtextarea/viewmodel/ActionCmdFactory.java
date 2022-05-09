package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.Selection;
import com.gluonhq.richtextarea.model.Decoration;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.TableDecoration;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.TextAlignment;

public final class ActionCmdFactory {

    private final ActionCmd copy  = new ActionCmdCopy();
    private final ActionCmd cut   = new ActionCmdCut();
    private final ActionCmd paste = new ActionCmdPaste();

    private final ActionCmd undo = new ActionCmdUndo();
    private final ActionCmd redo = new ActionCmdRedo();

    private final ActionCmd newDocument = new ActionCmdNew();
    private final ActionCmd save = new ActionCmdSave();

    private final ActionCmd selectAll = new ActionCmdSelectAll();

    public ActionCmd copy() {
        return copy;
    }

    public ActionCmd cut() {
        return cut;
    }

    public ActionCmd paste() {
        return paste;
    }

    public ActionCmd undo() {
        return undo;
    }

    public ActionCmd redo() {
        return redo;
    }

    public ActionCmd newDocument() {
        return newDocument;
    }

    public ActionCmd open(Document document) {
        return new ActionCmdOpen(document);
    }

    public ActionCmd save() {
        return save;
    }

    public ActionCmd selectAll() {
        return selectAll;
    }

    public ActionCmd insertText(String text) {
        return new ActionCmdInsertText(text);
    }

    public ActionCmd insertTable(TableDecoration tableDecoration) {
        return new ActionCmdTable(tableDecoration);
    }

    public ActionCmd deleteTable() {
        return new ActionCmdTable(ActionCmdTable.TableOperation.DELETE_TABLE);
    }

    public ActionCmd insertTableColumnBefore() {
        return new ActionCmdTable(ActionCmdTable.TableOperation.ADD_COLUMN_BEFORE);
    }

    public ActionCmd insertTableColumnAfter() {
        return new ActionCmdTable(ActionCmdTable.TableOperation.ADD_COLUMN_AFTER);
    }

    public ActionCmd deleteTableColumn() {
        return new ActionCmdTable(ActionCmdTable.TableOperation.DELETE_COLUMN);
    }

    public ActionCmd insertTableRowAbove() {
        return new ActionCmdTable(ActionCmdTable.TableOperation.ADD_ROW_ABOVE);
    }

    public ActionCmd insertTableRowBelow() {
        return new ActionCmdTable(ActionCmdTable.TableOperation.ADD_ROW_BELOW);
    }

    public ActionCmd deleteTableRow() {
        return new ActionCmdTable(ActionCmdTable.TableOperation.DELETE_ROW);
    }

    public ActionCmd deleteTableCell() {
        return new ActionCmdTable(ActionCmdTable.TableOperation.DELETE_CELL_CONTENT);
    }

    public ActionCmd alignTableCell(TextAlignment textAlignment) {
        return new ActionCmdTable(textAlignment);
    }

    public ActionCmd removeText(int caretOffset) {
        return new ActionCmdRemoveText(caretOffset);
    }

    public ActionCmd decorate(Decoration... decorations) {
        return new ActionCmdDecorate(decorations);
    }

    public ActionCmd caretMove(RichTextAreaViewModel.Direction direction, KeyEvent event) {
        return new ActionCmdCaretMove(direction, event);
    }

    public ActionCmd caretMove(RichTextAreaViewModel.Direction direction, boolean changeSelection, boolean wordSelection, boolean lineSelection) {
        return new ActionCmdCaretMove(direction, changeSelection, wordSelection, lineSelection);
    }

    public ActionCmd insertAndDecorate(String content, Decoration decoration) {
        return new ActionCmdInsertAndDecorate(content, decoration);
    }

    public ActionCmd selectCell(Selection selection) {
        return new ActionCmdSelectCell(selection);
    }

}
