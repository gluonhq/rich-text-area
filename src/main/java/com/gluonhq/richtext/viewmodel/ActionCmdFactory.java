package com.gluonhq.richtext.viewmodel;

import com.gluonhq.richtext.model.FaceModel;
import com.gluonhq.richtext.model.ImageDecoration;
import com.gluonhq.richtext.model.TextDecoration;
import javafx.scene.input.KeyEvent;

public final class ActionCmdFactory {

    private final ActionCmd copy  = new ActionCmdCopy();
    private final ActionCmd cut   = new ActionCmdCut();
    private final ActionCmd paste = new ActionCmdPaste();

    private final ActionCmd undo = new ActionCmdUndo();
    private final ActionCmd redo = new ActionCmdRedo();

    private final ActionCmd newFaceModel = new ActionCmdNew();
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

    public ActionCmd newFaceModel() {
        return newFaceModel;
    }

    public ActionCmd open(FaceModel faceModel) {
        return new ActionCmdOpen(faceModel);
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

    public ActionCmd removeText(int caretOffset) {
        return new ActionCmdRemoveText(caretOffset);
    }

    public ActionCmd decorateText(TextDecoration decoration) {
        return new ActionCmdDecorateText(decoration);
    }

    public ActionCmd decorateImage(ImageDecoration decoration) {
        return new ActionCmdDecorateImage(decoration);
    }

    public ActionCmd caretMove(RichTextAreaViewModel.Direction direction, KeyEvent event) {
        return new ActionCmdCaretMove(direction, event);
    }

    public ActionCmd caretMove(RichTextAreaViewModel.Direction direction, boolean changeSelection, boolean wordSelection, boolean lineSelection) {
        return new ActionCmdCaretMove(direction, changeSelection, wordSelection, lineSelection);
    }

}
