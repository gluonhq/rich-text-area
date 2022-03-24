package com.gluonhq.richtext.action;

import com.gluonhq.richtext.RichTextArea;
import com.gluonhq.richtext.Selection;
import com.gluonhq.richtext.model.Decoration;
import com.gluonhq.richtext.model.TextDecoration;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;

import java.util.function.BiFunction;
import java.util.function.Function;

public class TextDecorateAction<T> extends DecorateAction<T> {

    private ChangeListener<T> valuePropertyChangeListener;
    private ChangeListener<Decoration> decorationChangeListener;
    private final Function<TextDecoration, T> function;
    private final BiFunction<TextDecoration.Builder, T, TextDecoration> builderTFunction;

    public TextDecorateAction(RichTextArea control, ObjectProperty<T> valueProperty, Function<TextDecoration, T> valueFunction, BiFunction<TextDecoration.Builder, T, TextDecoration> builderTFunction) {
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
                Decoration decoration = viewModel.getDecorationAtCaret();
                if (viewModel.getSelection().isDefined() && decoration instanceof TextDecoration) {
                    builder = builder.fromDecoration((TextDecoration) decoration);
                }
                TextDecoration newTextDecoration = builderTFunction.apply(builder, nv);
                Platform.runLater(() ->  {
                    ACTION_CMD_FACTORY.decorateText(newTextDecoration).apply(viewModel);
                    control.requestFocus();
                });
                updating = false;
            }
        };
        valueProperty.addListener(valuePropertyChangeListener);
        decorationChangeListener = (obs, ov, nv) -> {
            if (!updating && nv instanceof TextDecoration && ov instanceof TextDecoration && !nv.equals(ov)) {
                T newValue = function.apply((TextDecoration) nv);
                if (newValue != null && !newValue.equals(function.apply((TextDecoration) ov))) {
                    updating = true;
                    valueProperty.set(newValue);
                    updating = false;
                }
            }
        };
        viewModel.decorationAtCaretProperty().addListener(decorationChangeListener);
    }

    @Override
    protected void unbind() {
        valueProperty.removeListener(valuePropertyChangeListener);
        viewModel.decorationAtCaretProperty().removeListener(decorationChangeListener);
    }
}
