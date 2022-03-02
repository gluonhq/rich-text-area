package com.gluonhq.richtext.action;

import com.gluonhq.richtext.RichTextArea;
import com.gluonhq.richtext.model.TextDecoration;
import com.gluonhq.richtext.viewmodel.ActionCmdFactory;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.scene.text.TextAlignment;

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

    public Action decorate(TextDecoration decoration) {
        return new BasicAction(control, action -> ACTION_CMD_FACTORY.decorateText(decoration));
    }

    public Action align(TextAlignment alignment) {

        return new Action() {
            @Override
            public void execute(ActionEvent event) {
                control.setTextAlignment(alignment);
            }

            @Override
            public ReadOnlyBooleanProperty disabledProperty() {
                return new SimpleBooleanProperty(false);
            }
        };
    }
}
