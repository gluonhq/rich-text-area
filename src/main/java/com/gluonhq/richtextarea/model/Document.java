package com.gluonhq.richtextarea.model;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.gluonhq.richtextarea.model.TextBuffer.ZERO_WIDTH_TEXT;

/**
 * A Document is the basic model that contains all the information required for the {@link com.gluonhq.richtextarea.RichTextArea}
 * control, in order to render all the rich content, including decorated text, images and other non-text objects.
 *
 * A document is basically a string with the full text, and a list of {@link DecorationModel} that contain the text and
 * paragraph decorations for one or more fragments of the text, where a fragment can be defined as the longest
 * substring of the text that shares the same text and paragraph decorations.
 */
public class Document {

    private final String text;
    private final List<DecorationModel> decorationList;
    private final int caretPosition;

    public Document() {
        this("");
    }

    public Document(String text) {
        this(text, 0);
    }

    public Document(String text, int caretPosition) {
        this(text,
                List.of(new DecorationModel(0, text.length(),
                        TextDecoration.builder().presets().build(),
                        ParagraphDecoration.builder().presets().build())),
                caretPosition);
    }

    public Document(String text, List<DecorationModel> decorationList, int caretPosition) {
        this.text = text;
        this.decorationList = decorationList;
        this.caretPosition = caretPosition;
    }

    /**
     * Gets the full text of the document
     *
     * @return a string with the full text of the document
     */
    public String getText() {
        return text;
    }

    /**
     * Gets the list of {@link DecorationModel} that define the fragments of text
     * that share the same decorations
     *
     * @return the list of {@link DecorationModel}
     */
    public List<DecorationModel> getDecorations() {
        return decorationList;
    }

    /**
     * Gets the caret position in order to restore the caret when the document
     * is opened
     *
     * @return the caret position when document was saved
     */
    public int getCaretPosition() {
        return caretPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return Objects.equals(text, document.text) &&
                Objects.equals(decorationList, document.decorationList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, decorationList, caretPosition);
    }

    @Override
    public String toString() {
        return "Document{" +
                "text='" + text.replaceAll("\n", "<n>").replaceAll(ZERO_WIDTH_TEXT, "<a>")  + '\'' +
                ", decorationList=" + (decorationList == null ? "null" : "{" +
                    decorationList.stream().map(decorationModel -> " - " + decorationModel.toString()).collect(Collectors.joining("\n", "\n", ""))) +
                "\n}, caretPosition=" + caretPosition +
                '}';
    }
}
