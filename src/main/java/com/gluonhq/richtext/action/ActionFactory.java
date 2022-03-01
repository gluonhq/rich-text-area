package com.gluonhq.richtext.action;

import com.gluonhq.richtext.RichTextArea;
import com.gluonhq.richtext.model.TextDecoration;
import com.gluonhq.richtext.viewmodel.ActionCmdFactory;

public class ActionFactory {

    private static final ActionCmdFactory ACTION_CMD_FACTORY = new ActionCmdFactory();
    private final RichTextArea control;

    public ActionFactory(RichTextArea control) {
        this.control = control;
    }

    private Action undo;
    public Action undo() {
        if (undo == null) {
            undo = new UndoAction(control);
        }
        return undo;
    }

    private Action redo;
    public Action redo() {
        if (redo == null) {
            redo = new RedoAction(control);
        }
        return redo;
    }

    private Action copy;
    public Action copy() {
        if (copy == null) {
            copy = new CopyAction(control);
        }
        return copy;
    }

    private Action cut;
    public Action cut() {
        if (cut == null) {
            cut = new CutAction(control);
        }
        return cut;
    }

    private Action paste;
    public Action paste() {
        if (paste == null) {
            paste = new PasteAction(control);
        }
        return paste;
    }

    public Action decorate(TextDecoration decoration) {
        return new DecorateAction(control, decoration);
    }

    static class UndoAction extends BasicAction {
        public UndoAction(RichTextArea control) {
            super(control, action -> ACTION_CMD_FACTORY.undo());
        }
    }

    static class RedoAction extends BasicAction {
        public RedoAction(RichTextArea control) {
            super(control, action -> ACTION_CMD_FACTORY.redo());
        }
    }

    static class CopyAction extends BasicAction {
        public CopyAction(RichTextArea control) {
            super(control, action -> ACTION_CMD_FACTORY.copy());
        }
    }

    static class CutAction extends BasicAction {
        public CutAction(RichTextArea control) {
            super(control, action -> ACTION_CMD_FACTORY.cut());
        }
    }

    static class PasteAction extends BasicAction {
        public PasteAction(RichTextArea control) {
            super(control, action -> ACTION_CMD_FACTORY.paste());
        }
    }

    static class DecorateAction extends BasicAction {
        public DecorateAction(RichTextArea control, TextDecoration decoration) {
            super(control, action -> ACTION_CMD_FACTORY.decorateText(decoration));
        }
    }
}
