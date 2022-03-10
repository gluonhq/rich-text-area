package com.gluonhq.richtext.action;

import com.gluonhq.richtext.RichTextArea;
import com.gluonhq.richtext.Selection;
import com.gluonhq.richtext.model.TextDecoration;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;

public class DecorateFontSizeAction extends DecorateAction<Double> {

    private ChangeListener<Double> fontSizeChangeListener;
    private ChangeListener<TextDecoration> textDecorationChangeListener;

    public DecorateFontSizeAction(RichTextArea control, ObjectProperty<Double> valueProperty) {
        super(control, valueProperty);
    }

    @Override
    protected void bind() {
        fontSizeChangeListener = (obs, ov, nv) -> {
            if (nv != null && !updating) {
                updating = true;
                TextDecoration newTextDecoration;
                if (viewModel.getSelection() == Selection.UNDEFINED) {
                    newTextDecoration = TextDecoration.builder().fromDecoration(viewModel.getTextDecoration()).fontSize(nv).build();
                } else {
                    newTextDecoration = TextDecoration.builder().fontSize(nv).build();
                }
                ACTION_CMD_FACTORY.decorateText(newTextDecoration).apply(viewModel);
                control.requestFocus();
                updating = false;
            }
        };
        valueProperty.addListener(fontSizeChangeListener);
        textDecorationChangeListener = (obs, ov, nv) -> {
            if (!updating && nv != null && !nv.equals(ov) && nv.getFontSize() != ov.getFontSize()) {
                updating = true;
                valueProperty.set(nv.getFontSize());
                updating = false;
            }
        };
        viewModel.textDecorationProperty().addListener(textDecorationChangeListener);
    }

    @Override
    protected void unbind() {
        valueProperty.removeListener(fontSizeChangeListener);
        viewModel.textDecorationProperty().removeListener(textDecorationChangeListener);
    }
}
