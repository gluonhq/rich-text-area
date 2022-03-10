package com.gluonhq.richtext.action;

import com.gluonhq.richtext.RichTextArea;
import com.gluonhq.richtext.Selection;
import com.gluonhq.richtext.model.TextDecoration;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.paint.Color;

import static javafx.scene.text.FontWeight.BOLD;
import static javafx.scene.text.FontWeight.NORMAL;

public class DecorateForegroundAction extends DecorateAction<Color> {

    private ChangeListener<Color> colorChangeListener;
    private ChangeListener<TextDecoration> textDecorationChangeListener;

    public DecorateForegroundAction(RichTextArea control, ObjectProperty<Color> valueProperty) {
        super(control, valueProperty);
    }

    @Override
    protected void bind() {
        colorChangeListener = (obs, ov, nv) -> {
            if (nv != null && !updating) {
                updating = true;
                TextDecoration newTextDecoration;
                if (viewModel.getSelection() == Selection.UNDEFINED) {
                    newTextDecoration = TextDecoration.builder().fromDecoration(viewModel.getTextDecoration()).foreground(nv).build();
                } else {
                    newTextDecoration = TextDecoration.builder().foreground(nv).build();
                }
                ACTION_CMD_FACTORY.decorateText(newTextDecoration).apply(viewModel);
                control.requestFocus();
                updating = false;
            }
        };
        valueProperty.addListener(colorChangeListener);
        textDecorationChangeListener = (obs, ov, nv) -> {
            if (!updating && nv != null && !nv.equals(ov) && nv.getForeground() != null && !nv.getForeground().equals(ov.getForeground())) {
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
