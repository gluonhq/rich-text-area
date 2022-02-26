package com.gluonhq.richtext.action;

import com.gluonhq.richtext.RichTextArea;
import com.gluonhq.richtext.model.TextDecoration;
import com.gluonhq.richtext.viewmodel.ActionCmd;

public class ActionFactory {

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

    static class UndoAction extends AbstractAction {
        public UndoAction(RichTextArea control) {
            super(control, ActionType.UNDO);
        }
    }

    static class RedoAction extends AbstractAction {
        public RedoAction(RichTextArea control) {
            super(control, ActionType.REDO);
        }
    }

    static class CopyAction extends AbstractAction {
        public CopyAction(RichTextArea control) {
            super(control, ActionType.COPY);
        }
    }

    static class CutAction extends AbstractAction {
        public CutAction(RichTextArea control) {
            super(control, ActionType.CUT);
        }
    }

    static class PasteAction extends AbstractAction {
        public PasteAction(RichTextArea control) {
            super(control, ActionType.PASTE);
        }
    }

    static class DecorateAction extends AbstractAction {

        private final TextDecoration decoration;

        public DecorateAction(RichTextArea control, TextDecoration decoration) {
            super(control, ActionType.DECORATE);
            this.decoration = decoration;
        }

        @Override
        protected ActionCmd getActionCmd() {
            return ACTION_CMD_FACTORY.decorateText(decoration);
        }

    }
}
