package com.gluonhq.richtextarea.action;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.*;
import com.gluonhq.richtextarea.viewmodel.ActionCmdFactory;

public class ActionFactory {

    private static final ActionCmdFactory ACTION_CMD_FACTORY = new ActionCmdFactory();
    private final RichTextArea control;

    public ActionFactory(RichTextArea control) {
        this.control = control;
    }

    private Action undo;
    public Action undo() {
        if (undo == null) {
            undo = new BasicAction(control, action -> ACTION_CMD_FACTORY.undo());
        }
        return undo;
    }

    private Action redo;
    public Action redo() {
        if (redo == null) {
            redo = new BasicAction(control, action -> ACTION_CMD_FACTORY.redo());
        }
        return redo;
    }

    private Action copy;
    public Action copy() {
        if (copy == null) {
            copy = new BasicAction(control, action -> ACTION_CMD_FACTORY.copy());
        }
        return copy;
    }

    private Action cut;
    public Action cut() {
        if (cut == null) {
            cut = new BasicAction(control, action -> ACTION_CMD_FACTORY.cut());
        }
        return cut;
    }

    private Action paste;
    public Action paste() {
        if (paste == null) {
            paste = new BasicAction(control, action -> ACTION_CMD_FACTORY.paste());
        }
        return paste;
    }

    private Action newDocument;
    public Action newDocument() {
        if (newDocument == null) {
            newDocument = new BasicAction(control, action -> ACTION_CMD_FACTORY.newDocument());
        }
        return newDocument;
    }

    public Action open(Document document) {
        return new BasicAction(control, action -> ACTION_CMD_FACTORY.open(document));
    }

    private Action save;
    public Action save() {
        if (save == null) {
            save = new BasicAction(control, action -> ACTION_CMD_FACTORY.save());
        }
        return save;
    }

    public Action insertTable(TableDecoration tableDecoration) {
        return new BasicAction(control, action -> ACTION_CMD_FACTORY.insertTable(tableDecoration));
    }

    public Action decorate(Decoration decoration) {
        return new BasicAction(control, action -> {
            if (decoration instanceof TextDecoration) {
                return ACTION_CMD_FACTORY.decorateText((TextDecoration) decoration);
            } else if (decoration instanceof ImageDecoration) {
                return ACTION_CMD_FACTORY.decorateImage((ImageDecoration) decoration);
            } else if (decoration instanceof ParagraphDecoration) {
                return ACTION_CMD_FACTORY.decorateParagraph((ParagraphDecoration) decoration);
            }
            throw new IllegalArgumentException("Decoration type not supported: " + decoration);
        });
    }
}
