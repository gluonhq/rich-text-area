package com.gluonhq.richtext.action;

import com.gluonhq.richtext.RichTextArea;
import com.gluonhq.richtext.model.TextDecoration;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;

public class FontSizeDecorateAction extends DecorateAction<Double> {

    private ChangeListener<Double> fontSizeChangeListener;
    private ChangeListener<TextDecoration> textDecorationChangeListener;

    public FontSizeDecorateAction(RichTextArea control, ObjectProperty<Double> valueProperty) {
        super(control, valueProperty);
    }

    @Override
    protected void bind() {
        fontSizeChangeListener = (obs, ov, nv) -> {
            if (nv != null && !updating) {
                updating = true;
                TextDecoration newTextDecoration = TextDecoration.builder().fromDecoration(viewModel.getTextDecoration()).fontSize(nv).build();
                /*viewModel.setTextDecoration(newTextDecoration);
                if (viewModel.getSelection().isDefined()) {*/
                ACTION_CMD_FACTORY.decorateText(newTextDecoration).apply(viewModel);
                //}
                control.requestFocus();
                updating = false;
            }
        };
        valueProperty.addListener(fontSizeChangeListener);
        textDecorationChangeListener = (obs, ov, nv) -> {
            System.out.println("FontSizeDecorateAction.bind");
            System.out.println("textdecoration listener");
            System.out.println("nv: " + nv);
            System.out.println("updating: " + updating);
            if (nv != null && !updating) {
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
