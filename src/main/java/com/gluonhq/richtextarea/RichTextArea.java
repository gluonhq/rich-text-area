package com.gluonhq.richtextarea;

import com.gluonhq.richtextarea.action.ActionFactory;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.SkinBase;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class RichTextArea extends Control {

    public static final String STYLE_CLASS = "rich-text-area";
    private static final PseudoClass PSEUDO_CLASS_READONLY = PseudoClass.getPseudoClass("readonly");

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

    // documentProperty
    private final ObjectProperty<Document> documentProperty = new SimpleObjectProperty<>(this, "document", new Document()) {
        @Override
        protected void invalidated() {
            System.out.println(get());
        }
    };
    public final ObjectProperty<Document> documentProperty() {
       return documentProperty;
    }
    public final Document getDocument() {
       return documentProperty.get();
    }
    public final void setDocument(Document value) {
        documentProperty.set(value);
    }

    // autoSaveProperty
    private final BooleanProperty autoSaveProperty = new SimpleBooleanProperty(this, "autoSave");
    public final BooleanProperty autoSaveProperty() {
       return autoSaveProperty;
    }
    public final boolean isAutoSave() {
       return autoSaveProperty.get();
    }
    public final void setAutoSave(boolean value) {
        autoSaveProperty.set(value);
    }

    // modifiedProperty
    final ReadOnlyBooleanWrapper modifiedProperty = new ReadOnlyBooleanWrapper(this, "modified");
    public final ReadOnlyBooleanProperty modifiedProperty() {
       return modifiedProperty.getReadOnlyProperty();
    }
    public final boolean isModified() {
       return modifiedProperty.get();
    }

    // editableProperty
    private final BooleanProperty editableProperty = new SimpleBooleanProperty(this, "editable", true) {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(PSEUDO_CLASS_READONLY, !get());
        }
    };
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

    // paragraphGraphicFactoryProperty
    private final ObjectProperty<BiFunction<Integer, ParagraphDecoration.GraphicType, Node>> paragraphGraphicFactoryProperty = new SimpleObjectProperty<>(this, "paragraphGraphicFactory");
    public final ObjectProperty<BiFunction<Integer, ParagraphDecoration.GraphicType, Node>> paragraphGraphicFactoryProperty() {
       return paragraphGraphicFactoryProperty;
    }
    public final BiFunction<Integer, ParagraphDecoration.GraphicType, Node> getParagraphGraphicFactory() {
       return paragraphGraphicFactoryProperty.get();
    }
    public final void setParagraphGraphicFactory(BiFunction<Integer, ParagraphDecoration.GraphicType, Node> value) {
        paragraphGraphicFactoryProperty.set(value);
    }

    // linkCallbackFactoryProperty
    private final ObjectProperty<Function<Node, Consumer<String>>> linkCallbackFactoryProperty =
            new SimpleObjectProperty<>(this, "linkCallbackFactory", DefaultLinkCallbackFactory.getFactory());
    public final ObjectProperty<Function<Node, Consumer<String>>> linkCallbackFactoryProperty() {
       return linkCallbackFactoryProperty;
    }
    public final Function<Node, Consumer<String>> getLinkCallbackFactory() {
       return linkCallbackFactoryProperty.get();
    }
    public final void setLinkCallbackFactory(Function<Node, Consumer<String>> value) {
        linkCallbackFactoryProperty.set(value);
    }


    public final ActionFactory getActionFactory() {
        return actionFactory;
    }

}

