package com.gluonhq.richtext.action;

import com.gluonhq.richtext.RichTextArea;
import com.gluonhq.richtext.model.TextDecoration;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;

import static javafx.scene.text.FontPosture.ITALIC;
import static javafx.scene.text.FontPosture.REGULAR;

public class DecorateFontPostureAction extends DecorateAction<Boolean> {

    private ChangeListener<Boolean> fontPostureChangeListener;
    private ChangeListener<TextDecoration> textDecorationChangeListener;

    public DecorateFontPostureAction(RichTextArea control, ObjectProperty<Boolean> valueProperty) {
        super(control, valueProperty);
    }

    @Override
    protected void bind() {
        fontPostureChangeListener = (obs, ov, nv) -> {
            if (nv != null && !updating) {
                updating = true;
                TextDecoration newTextDecoration = TextDecoration.builder().fromDecoration(viewModel.getTextDecoration()).fontPosture(nv ? ITALIC:REGULAR).build();
                ACTION_CMD_FACTORY.decorateText(newTextDecoration).apply(viewModel);
                control.requestFocus();
                updating = false;
            }
        };
        valueProperty.addListener(fontPostureChangeListener);
        textDecorationChangeListener = (obs, ov, nv) -> {
            if (!updating && nv != null && !nv.equals(ov) && !nv.getFontPosture().equals(ov.getFontPosture())) {
                updating = true;
                valueProperty.set(nv.getFontPosture() == ITALIC);
                updating = false;
            }
        };
        viewModel.textDecorationProperty().addListener(textDecorationChangeListener);
    }

    @Override
    protected void unbind() {
        valueProperty.removeListener(fontPostureChangeListener);
        viewModel.textDecorationProperty().removeListener(textDecorationChangeListener);
    }
}
