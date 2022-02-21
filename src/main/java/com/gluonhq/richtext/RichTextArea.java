package com.gluonhq.richtext;


import com.gluonhq.richtext.viewmodel.ActionFactory;
import javafx.beans.property.*;
import javafx.scene.control.Control;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

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

    // undoStackSizeProperty
    final ReadOnlyIntegerWrapper undoStackSizeProperty = new ReadOnlyIntegerWrapper(this, "undoStackSize");
    public ReadOnlyIntegerProperty undoStackSizeProperty() {
        return undoStackSizeProperty.getReadOnlyProperty();
    }
    public int getUndoStackSize() {
        return undoStackSizeProperty.get();
    }

    // redoStackSizeProperty
    final ReadOnlyIntegerWrapper redoStackSizeProperty = new ReadOnlyIntegerWrapper(this, "redoStackSize");
    public ReadOnlyIntegerProperty redoStackSizeProperty() {
        return redoStackSizeProperty.getReadOnlyProperty();
    }
    public int getRedoStackSize() {
        return redoStackSizeProperty.get();
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

