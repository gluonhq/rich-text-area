package com.gluonhq.richtext;


import com.gluonhq.richtext.viewmodel.ActionFactory;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.SkinBase;

import java.util.Objects;

public class RichTextArea extends Control {

    public static final String STYLE_CLASS = "rich-text";

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

    public void execute( Action action ) {
        if ( getSkin() instanceof RichTextAreaSkin ) {
            RichTextAreaSkin rtaSkin  = (RichTextAreaSkin)getSkin();
            rtaSkin.execute(Objects.requireNonNull(action));
            requestFocus();
        }
    }

    public ActionFactory getActionFactory() {
        return RichTextAreaSkin.getActionFactory();
    }


}

