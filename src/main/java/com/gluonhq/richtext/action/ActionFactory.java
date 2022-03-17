package com.gluonhq.richtext.action;

import com.gluonhq.richtext.RichTextArea;
import com.gluonhq.richtext.model.Decoration;
import com.gluonhq.richtext.model.FaceModel;
import com.gluonhq.richtext.model.ImageDecoration;
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

    private Action newFaceModel;
    public Action newFaceModel() {
        if (newFaceModel == null) {
            newFaceModel = new BasicAction(control, action -> ACTION_CMD_FACTORY.newFaceModel());
        }
        return newFaceModel;
    }

    public Action open(FaceModel faceModel) {
        return new BasicAction(control, action -> ACTION_CMD_FACTORY.open(faceModel));
    }

    private Action save;
    public Action save() {
        if (save == null) {
            save = new BasicAction(control, action -> ACTION_CMD_FACTORY.save());
        }
        return save;
    }

    public Action decorate(Decoration decoration) {
        return new BasicAction(control, action -> {
            if (decoration instanceof TextDecoration) {
                return ACTION_CMD_FACTORY.decorateText((TextDecoration) decoration);
            } else if (decoration instanceof ImageDecoration) {
                return ACTION_CMD_FACTORY.decorateImage((ImageDecoration) decoration);
            }
            throw new IllegalArgumentException("Decoration type not supported: " + decoration);
        });
    }
}
