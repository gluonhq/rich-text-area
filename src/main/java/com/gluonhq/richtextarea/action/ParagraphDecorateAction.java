package com.gluonhq.richtextarea.action;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;

import java.util.function.BiFunction;
import java.util.function.Function;

public class ParagraphDecorateAction<T> extends DecorateAction<T> {

    private ChangeListener<T> valuePropertyChangeListener;
    private ChangeListener<ParagraphDecoration> decorationChangeListener;
    private final Function<ParagraphDecoration, T> function;
    private final BiFunction<ParagraphDecoration.Builder, T, ParagraphDecoration> builderTFunction;

    public ParagraphDecorateAction(RichTextArea control,
                                   ObjectProperty<T> valueProperty, Function<ParagraphDecoration, T> valueFunction,
                                   BiFunction<ParagraphDecoration.Builder, T, ParagraphDecoration> builderTFunction) {
        super(control, valueProperty);
        this.function = valueFunction;
        this.builderTFunction = builderTFunction;
    }

    @Override
    protected void bind() {
        valuePropertyChangeListener = (obs, ov, nv) -> {
            if (nv != null && !updating) {
                updating = true;
                ParagraphDecoration.Builder builder = ParagraphDecoration.builder();
                if (!viewModel.getSelection().isDefined() && viewModel.getDecorationAtParagraph() != null) {
                    builder = builder.fromDecoration(viewModel.getDecorationAtParagraph());
                }
                ParagraphDecoration newParagraphDecoration = builderTFunction.apply(builder, nv);
                Platform.runLater(() ->  {
                    ACTION_CMD_FACTORY.decorateParagraph(newParagraphDecoration).apply(viewModel);
                    control.requestFocus();
                });
                updating = false;
            }
        };
        valueProperty.addListener(valuePropertyChangeListener);
        decorationChangeListener = (obs, ov, nv) -> {
            if (!updating && !nv.equals(ov)) {
                T newValue = function.apply(nv);
                if (newValue != null && (ov == null || !newValue.equals(function.apply(ov)))) {
                    updating = true;
                    valueProperty.set(newValue);
                    updating = false;
                }
            }
        };
        viewModel.decorationAtParagraphProperty().addListener(decorationChangeListener);
    }

    @Override
    protected void unbind() {
        valueProperty.removeListener(valuePropertyChangeListener);
        viewModel.decorationAtParagraphProperty().removeListener(decorationChangeListener);
    }
}
