package com.gluonhq.richtext.action;

import com.gluonhq.richtext.RichTextArea;
import com.gluonhq.richtext.RichTextAreaSkin;
import com.gluonhq.richtext.viewmodel.ActionCmd;
import com.gluonhq.richtext.viewmodel.ActionCmdFactory;
import com.gluonhq.richtext.viewmodel.RichTextAreaViewModel;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Skin;

import java.util.Map;
import java.util.function.Function;

import static java.util.Map.entry;

abstract class AbstractAction implements Action {

    protected static final ActionCmdFactory ACTION_CMD_FACTORY = new ActionCmdFactory();
    private static final Map<ActionType, Function<AbstractAction, ActionCmd>> ACTION_MAP = Map.ofEntries(
            entry(ActionType.COPY,  action -> ACTION_CMD_FACTORY.copy()),
            entry(ActionType.CUT,   action -> ACTION_CMD_FACTORY.cut()),
            entry(ActionType.PASTE, action -> ACTION_CMD_FACTORY.paste()),
            entry(ActionType.UNDO,  action -> ACTION_CMD_FACTORY.undo()),
            entry(ActionType.REDO,  action -> ACTION_CMD_FACTORY.redo())
    );

    private final ActionType actionType;
    private RichTextAreaViewModel viewModel;

    private final BooleanProperty disabledImplProperty = new SimpleBooleanProperty(this, "disabledImpl", false);

    public AbstractAction(RichTextArea control, ActionType actionType) {
        this.actionType = actionType;
        if (control.getSkin() != null) {
            initialize(control.getSkin());
        } else {
            control.skinProperty().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    if (control.getSkin() != null) {
                        initialize(control.getSkin());
                        control.skinProperty().removeListener(this);
                    }
                }
            });
        }
    }

    private void initialize(Skin<?> skin) {
        if (!(skin instanceof RichTextAreaSkin)) {
            return;
        }
        viewModel = ((RichTextAreaSkin) skin).getViewModel();
        BooleanBinding binding = getActionCmd().getDisabledBinding(viewModel);
        if (binding != null) {
            disabledImplProperty.bind(binding);
        }
    }

    protected ActionCmd getActionCmd() {
        return ACTION_MAP.get(actionType).apply(this);
    }

    @Override
    public void apply(ActionEvent event) {
        getActionCmd().apply(viewModel);
    }

    @Override
    public ReadOnlyBooleanProperty disabledProperty() {
        return disabledImplProperty;
    }
}

