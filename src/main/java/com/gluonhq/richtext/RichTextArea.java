package com.gluonhq.richtext;


import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.IndexRange;
import javafx.scene.control.SkinBase;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
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
    private final ReadOnlyObjectWrapper<IndexRange> selectionProperty = new ReadOnlyObjectWrapper<>(this, "selection", Tools.NO_SELECTION);
    public final ReadOnlyObjectProperty<IndexRange> selectionProperty() {
       return selectionProperty.getReadOnlyProperty();
    }
    public final IndexRange getSelection() {
       return selectionProperty.get();
    }
    final void setSelection(IndexRange value) {
        selectionProperty.set(Objects.requireNonNull(value));
    }

    // codecProperty
    private final ObjectProperty<Codec> codecProperty = new SimpleObjectProperty<>(this, "codec");
    public final ObjectProperty<Codec> codecProperty() {
       return codecProperty;
    }
    public final Codec getCodec() {
       return codecProperty.get();
    }
    public final void setCodec(Codec value) {
        codecProperty.set(value);
    }


    public static interface Codec {
        OutputStream decode(List<Node> nodes);
        List<Node> encode(InputStream stream);
    }
}

