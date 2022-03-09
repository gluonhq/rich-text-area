package com.gluonhq.richtext.action;

import com.gluonhq.richtext.RichTextArea;
import com.gluonhq.richtext.model.TextDecoration;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;

import static javafx.scene.text.FontWeight.BOLD;
import static javafx.scene.text.FontWeight.NORMAL;

public class DecorateFontWeightAction extends DecorateAction<Boolean> {

    private ChangeListener<Boolean> fontWeightChangeListener;
    private ChangeListener<TextDecoration> textDecorationChangeListener;

    public DecorateFontWeightAction(RichTextArea control, ObjectProperty<Boolean> valueProperty) {
        super(control, valueProperty);
    }

    @Override
    protected void bind() {
        fontWeightChangeListener = (obs, ov, nv) -> {
            if (nv != null && !updating) {
                updating = true;
                TextDecoration newTextDecoration = TextDecoration.builder().fromDecoration(viewModel.getTextDecoration()).fontWeight(nv ? BOLD:NORMAL).build();
                ACTION_CMD_FACTORY.decorateText(newTextDecoration).apply(viewModel);
                control.requestFocus();
                updating = false;
            }
        };
        valueProperty.addListener(fontWeightChangeListener);
        textDecorationChangeListener = (obs, ov, nv) -> {
            if (!updating && nv != null && !nv.equals(ov) && !nv.getFontWeight().equals(ov.getFontWeight())) {
                updating = true;
                valueProperty.set(nv.getFontWeight() == BOLD);
                updating = false;
            }
        };
        viewModel.textDecorationProperty().addListener(textDecorationChangeListener);
    }

    @Override
    protected void unbind() {
        valueProperty.removeListener(fontWeightChangeListener);
        viewModel.textDecorationProperty().removeListener(textDecorationChangeListener);
    }
}
