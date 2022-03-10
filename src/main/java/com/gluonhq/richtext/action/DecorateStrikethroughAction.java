package com.gluonhq.richtext.action;

import com.gluonhq.richtext.RichTextArea;
import com.gluonhq.richtext.Selection;
import com.gluonhq.richtext.model.TextDecoration;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;

public class DecorateStrikethroughAction extends DecorateAction<Boolean> {

    private ChangeListener<Boolean> strikethroughChangeListener;
    private ChangeListener<TextDecoration> textDecorationChangeListener;

    public DecorateStrikethroughAction(RichTextArea control, ObjectProperty<Boolean> valueProperty) {
        super(control, valueProperty);
    }

    @Override
    protected void bind() {
        strikethroughChangeListener = (obs, ov, nv) -> {
            if (nv != null && !updating) {
                updating = true;
                TextDecoration newTextDecoration;
                if (viewModel.getSelection() == Selection.UNDEFINED) {
                    newTextDecoration = TextDecoration.builder().fromDecoration(viewModel.getTextDecoration()).strikethrough(nv).build();
                } else {
                    newTextDecoration = TextDecoration.builder().strikethrough(nv).build();
                }
                ACTION_CMD_FACTORY.decorateText(newTextDecoration).apply(viewModel);
                control.requestFocus();
                updating = false;
            }
        };
        valueProperty.addListener(strikethroughChangeListener);
        textDecorationChangeListener = (obs, ov, nv) -> {
            if (!updating && nv != null && !nv.equals(ov) && nv.isStrikethrough() != ov.isStrikethrough()) {
                updating = true;
                valueProperty.set(nv.isStrikethrough());
                updating = false;
            }
        };
        viewModel.textDecorationProperty().addListener(textDecorationChangeListener);
    }

    @Override
    protected void unbind() {
        valueProperty.removeListener(strikethroughChangeListener);
        viewModel.textDecorationProperty().removeListener(textDecorationChangeListener);
    }
}
