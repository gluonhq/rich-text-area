package com.gluonhq.richtext.action;

import com.gluonhq.richtext.RichTextArea;
import com.gluonhq.richtext.model.TextDecoration;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.paint.Color;

public class ForegroundDecorateAction extends DecorateAction<Color> {

    private ChangeListener<Color> colorChangeListener;
    private ChangeListener<TextDecoration> textDecorationChangeListener;

    public ForegroundDecorateAction(RichTextArea control, ObjectProperty<Color> valueProperty) {
        super(control, valueProperty);
    }

    @Override
    protected void bind() {
        colorChangeListener = (obs, ov, nv) -> {
            if (nv != null && !updating) {
                updating = true;
                TextDecoration newTextDecoration = TextDecoration.builder().fromDecoration(viewModel.getTextDecoration()).foreground(nv).build();
                viewModel.setTextDecoration(newTextDecoration);
                if (viewModel.getSelection().isDefined()) {
                    ACTION_CMD_FACTORY.decorateText(newTextDecoration).apply(viewModel);
                }
                control.requestFocus();
                updating = false;
            }
        };
        valueProperty.addListener(colorChangeListener);
        textDecorationChangeListener = (obs, ov, nv) -> {
            if (nv != null && !updating) {
                updating = true;
                valueProperty.set(nv.getForeground());
                updating = false;
            }
        };
        viewModel.textDecorationProperty().addListener(textDecorationChangeListener);
    }

    @Override
    protected void unbind() {
        valueProperty.removeListener(colorChangeListener);
        viewModel.textDecorationProperty().removeListener(textDecorationChangeListener);
    }
}
