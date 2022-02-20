package com.gluonhq.richtext.viewmodel;

import com.gluonhq.richtext.Action;
import com.gluonhq.richtext.model.TextDecoration;

public final class ActionFactory {

    private final Action copy  = new ActionCopy();
    private final Action cut   = new ActionCut();
    private final Action paste = new ActionPaste();

    private final Action undo = new ActionUndo();
    private final Action redo = new ActionRedo();

    public Action copy() {
        return copy;
    }

    public Action cut() {
        return cut;
    }

    public Action paste() {
        return paste;
    }

    public Action undo() {
        return undo;
    }

    public Action redo() {
        return redo;
    }

    public Action insertText(String text) {
        return new ActionInsertText(text);
    }

    public Action removeText(int caretOffset) {
        return new ActionRemoveText(caretOffset);
    }

    public Action decorateText(TextDecoration decoration) {
        return new ActionDecorateText(decoration);
    }
}
