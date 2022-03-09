package com.gluonhq.richtext.action;

import com.gluonhq.richtext.RichTextArea;
import com.gluonhq.richtext.model.TextDecoration;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;

public class DecorateUnderlineAction extends DecorateAction<Boolean> {

    private ChangeListener<Boolean> underlineChangeListener;
    private ChangeListener<TextDecoration> textDecorationChangeListener;

    public DecorateUnderlineAction(RichTextArea control, ObjectProperty<Boolean> valueProperty) {
        super(control, valueProperty);
    }

    @Override
    protected void bind() {
        underlineChangeListener = (obs, ov, nv) -> {
            if (nv != null && !updating) {
                updating = true;
                TextDecoration newTextDecoration = TextDecoration.builder().fromDecoration(viewModel.getTextDecoration()).underline(nv).build();
                ACTION_CMD_FACTORY.decorateText(newTextDecoration).apply(viewModel);
                control.requestFocus();
                updating = false;
            }
        };
        valueProperty.addListener(underlineChangeListener);
        textDecorationChangeListener = (obs, ov, nv) -> {
            if (!updating && nv != null && !nv.equals(ov) && nv.isUnderline() != ov.isUnderline()) {
                updating = true;
                valueProperty.set(nv.isUnderline());
                updating = false;
            }
        };
        viewModel.textDecorationProperty().addListener(textDecorationChangeListener);
    }

    @Override
    protected void unbind() {
        valueProperty.removeListener(underlineChangeListener);
        viewModel.textDecorationProperty().removeListener(textDecorationChangeListener);
    }
}
