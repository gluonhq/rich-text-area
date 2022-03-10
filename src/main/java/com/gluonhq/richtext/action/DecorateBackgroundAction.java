package com.gluonhq.richtext.action;

import com.gluonhq.richtext.RichTextArea;
import com.gluonhq.richtext.Selection;
import com.gluonhq.richtext.model.TextDecoration;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.paint.Color;

public class DecorateBackgroundAction extends DecorateAction<Color> {

    private ChangeListener<Color> colorChangeListener;
    private ChangeListener<TextDecoration> textDecorationChangeListener;

    public DecorateBackgroundAction(RichTextArea control, ObjectProperty<Color> valueProperty) {
        super(control, valueProperty);
    }

    @Override
    protected void bind() {
        colorChangeListener = (obs, ov, nv) -> {
            if (nv != null && !updating) {
                updating = true;
                TextDecoration newTextDecoration;
                if (viewModel.getSelection() == Selection.UNDEFINED) {
                    newTextDecoration = TextDecoration.builder().fromDecoration(viewModel.getTextDecoration()).background(nv).build();
                } else {
                    newTextDecoration = TextDecoration.builder().background(nv).build();
                }
                ACTION_CMD_FACTORY.decorateText(newTextDecoration).apply(viewModel);
                control.requestFocus();
                updating = false;
            }
        };
        valueProperty.addListener(colorChangeListener);
        textDecorationChangeListener = (obs, ov, nv) -> {
            if (!updating && nv != null && !nv.equals(ov) && nv.getBackground() != null && !nv.getBackground().equals(ov.getBackground())) {
                updating = true;
                valueProperty.set(nv.getBackground());
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
