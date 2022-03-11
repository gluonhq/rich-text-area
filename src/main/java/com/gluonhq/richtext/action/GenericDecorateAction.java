package com.gluonhq.richtext.action;

import com.gluonhq.richtext.RichTextArea;
import com.gluonhq.richtext.Selection;
import com.gluonhq.richtext.model.TextDecoration;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;

import java.util.function.BiFunction;
import java.util.function.Function;

public class GenericDecorateAction<T> extends DecorateAction<T> {

    private ChangeListener<T> valuePropertyChangeListener;
    private ChangeListener<TextDecoration> textDecorationChangeListener;
    private final Function<TextDecoration, T> function;
    private final BiFunction<TextDecoration.Builder, T, TextDecoration> builderTFunction;

    public GenericDecorateAction(RichTextArea control, ObjectProperty<T> valueProperty, Function<TextDecoration, T> valueFunction, BiFunction<TextDecoration.Builder, T, TextDecoration> builderTFunction) {
        super(control, valueProperty);
        this.function = valueFunction;
        this.builderTFunction = builderTFunction;
    }

    @Override
    protected void bind() {
        valuePropertyChangeListener = (obs, ov, nv) -> {
            if (nv != null && !updating) {
                updating = true;
                TextDecoration.Builder builder = TextDecoration.builder();
                if (viewModel.getSelection() == Selection.UNDEFINED) {
                    builder = builder.fromDecoration(viewModel.getTextDecoration());
                }
                TextDecoration newTextDecoration = builderTFunction.apply(builder, nv);
                ACTION_CMD_FACTORY.decorateText(newTextDecoration).apply(viewModel);
                control.requestFocus();
                updating = false;
            }
        };
        valueProperty.addListener(valuePropertyChangeListener);
        textDecorationChangeListener = (obs, ov, nv) -> {
            if (!updating && nv != null && !nv.equals(ov)) {
                T newValue = function.apply(nv);
                if (newValue != null && !newValue.equals(function.apply(ov))) {
                    updating = true;
                    valueProperty.set(newValue);
                    updating = false;
                }
            }
        };
        viewModel.textDecorationProperty().addListener(textDecorationChangeListener);
    }

    @Override
    protected void unbind() {
        valueProperty.removeListener(valuePropertyChangeListener);
        viewModel.textDecorationProperty().removeListener(textDecorationChangeListener);
    }
}
