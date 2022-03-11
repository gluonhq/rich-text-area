package com.gluonhq.richtext.action;

import com.gluonhq.richtext.RichTextArea;
import com.gluonhq.richtext.RichTextAreaSkin;
import com.gluonhq.richtext.viewmodel.ActionCmdFactory;
import com.gluonhq.richtext.viewmodel.RichTextAreaViewModel;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Skin;

public class DecorateAction<T> {

    protected static final ActionCmdFactory ACTION_CMD_FACTORY = new ActionCmdFactory();

    protected final RichTextArea control;
    protected RichTextAreaViewModel viewModel;
    protected final ObjectProperty<T> valueProperty;
    protected boolean updating;

    protected DecorateAction(RichTextArea control, ObjectProperty<T> valueProperty) {
        this.valueProperty = valueProperty;
        this.control = control;
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
        bind();
    }

    protected void bind() {}
    protected void unbind() {}

}

