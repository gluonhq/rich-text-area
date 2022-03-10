package com.gluonhq.richtext.action;

import com.gluonhq.richtext.RichTextArea;
import com.gluonhq.richtext.Selection;
import com.gluonhq.richtext.model.TextDecoration;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;

import static javafx.scene.text.FontWeight.BOLD;
import static javafx.scene.text.FontWeight.NORMAL;

public class DecorateFontFamilyAction extends DecorateAction<String> {

    private ChangeListener<String> fontFamilyChangeListener;
    private ChangeListener<TextDecoration> textDecorationChangeListener;

    public DecorateFontFamilyAction(RichTextArea control, ObjectProperty<String> valueProperty) {
        super(control, valueProperty);
    }

    @Override
    protected void bind() {
        fontFamilyChangeListener = (obs, ov, nv) -> {
            if (nv != null && !updating) {
                updating = true;
                TextDecoration newTextDecoration;
                if (viewModel.getSelection() == Selection.UNDEFINED) {
                    newTextDecoration = TextDecoration.builder().fromDecoration(viewModel.getTextDecoration()).fontFamily(nv).build();
                } else {
                    newTextDecoration = TextDecoration.builder().fontFamily(nv).build();
                }
                ACTION_CMD_FACTORY.decorateText(newTextDecoration).apply(viewModel);
                control.requestFocus();
                updating = false;
            }
        };
        valueProperty.addListener(fontFamilyChangeListener);
        textDecorationChangeListener = (obs, ov, nv) -> {
            if (!updating && nv != null && !nv.equals(ov) && nv.getFontFamily() != null && !nv.getFontFamily().equals(ov.getFontFamily())) {
                updating = true;
                valueProperty.set(nv.getFontFamily());
                updating = false;
            }
        };
        viewModel.textDecorationProperty().addListener(textDecorationChangeListener);
    }

    @Override
    protected void unbind() {
        valueProperty.removeListener(fontFamilyChangeListener);
        viewModel.textDecorationProperty().removeListener(textDecorationChangeListener);
    }
}
