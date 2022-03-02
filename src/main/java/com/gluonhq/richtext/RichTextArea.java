package com.gluonhq.richtext;

import com.gluonhq.richtext.action.ActionFactory;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.SkinBase;

import java.util.Objects;

public class RichTextArea extends Control {

    public static final String STYLE_CLASS = "rich-text";

    private final ActionFactory actionFactory = new ActionFactory(this);

    public RichTextArea() {
        getStyleClass().add(STYLE_CLASS);
    }

    @Override
    protected SkinBase<RichTextArea> createDefaultSkin() {
        return new RichTextAreaSkin(this);
    }

    @Override public String getUserAgentStylesheet() {
        return getClass().getResource("rich-text-area.css").toExternalForm();
    }

    // faceModelProperty
    private final ObjectProperty<FaceModel> faceModelProperty = new SimpleObjectProperty<>(this, "faceModel", new FaceModel());
    public final ObjectProperty<FaceModel> faceModelProperty() {
       return faceModelProperty;
    }
    public final FaceModel getFaceModel() {
       return faceModelProperty.get();
    }
    public final void setFaceModel(FaceModel value) {
        faceModelProperty.set(value);
    }

    // editableProperty
    private final BooleanProperty editableProperty = new SimpleBooleanProperty(this, "editable", true);
    public final BooleanProperty editableProperty() {
       return editableProperty;
    }
    public final boolean isEditable() {
       return editableProperty.get();
    }
    public final void setEditable(boolean value) {
        editableProperty.set(value);
    }

    // selectionProperty
    private final ReadOnlyObjectWrapper<Selection> selectionProperty = new ReadOnlyObjectWrapper<>(this, "selection", Selection.UNDEFINED);
    public final ReadOnlyObjectProperty<Selection> selectionProperty() {
       return selectionProperty.getReadOnlyProperty();
    }
    public final Selection getSelection() {
       return selectionProperty.get();
    }
    final void setSelection(Selection value) {
        selectionProperty.set(Objects.requireNonNull(value));
    }

    // textLengthProperty
    final ReadOnlyIntegerWrapper textLengthProperty = new ReadOnlyIntegerWrapper(this, "textLength");
    public final ReadOnlyIntegerProperty textLengthProperty() {
        return textLengthProperty.getReadOnlyProperty();
    }
    public final int getTextLength() {
        return textLengthProperty.get();
    }

    // contentAreaWidthProperty
    /**
     * Defines a width constraint for the content area of the rich text control,
     * in user space coordinates, where text can be added.
     * The width is measured in pixels (and not glyph or character count).
     * If the value is {@code <= 0}, the content area extends to the whole viewport of control
     * and will change whenever the viewport gets resized.
     * If the value is {@code > 0}, the content area is exactly set to this value,
     * and the control will provide a horizontal scrollbar if needed.
     *
     * In any case, text will be line wrapped as needed to satisfy this constraint.
     *
     * @defaultValue 0
     */
    private final DoubleProperty contentAreaWidthProperty = new SimpleDoubleProperty(this, "contentAreaWidth", 0d);
    public final DoubleProperty contentAreaWidthProperty() {
        return contentAreaWidthProperty;
    }
    public final double getContentAreaWidth() {
        return contentAreaWidthProperty.get();
    }
    public final void setContentAreaWidth(double value) {
        contentAreaWidthProperty.set(value);
    }

    public final ActionFactory getActionFactory() {
        return actionFactory;
    }

}

