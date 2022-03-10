package com.gluonhq.richtext.action;

import com.gluonhq.richtext.RichTextArea;
import com.gluonhq.richtext.RichTextAreaSkin;
import com.gluonhq.richtext.viewmodel.ActionCmd;
import com.gluonhq.richtext.viewmodel.RichTextAreaViewModel;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Skin;

import java.util.Objects;
import java.util.function.Function;

class BasicAction implements Action {

    private RichTextAreaViewModel viewModel;
    private final Function<Action, ActionCmd> actionCmdFunction;

    private final BooleanProperty disabledImplProperty = new SimpleBooleanProperty(this, "disabledImpl", false);

    public BasicAction(RichTextArea control, Function<Action, ActionCmd> actionCmdFunction) {
        this.actionCmdFunction = Objects.requireNonNull(actionCmdFunction);
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

    private ActionCmd getActionCmd() {
        return actionCmdFunction.apply(this);
    }

    @Override
    public void execute(ActionEvent event) {
        Platform.runLater(() -> getActionCmd().apply(viewModel));
    }

    @Override
    public ReadOnlyBooleanProperty disabledProperty() {
        return disabledImplProperty;
    }
}

