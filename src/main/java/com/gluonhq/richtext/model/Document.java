package com.gluonhq.richtext.model;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.gluonhq.richtext.model.TextBuffer.ZERO_WIDTH_TEXT;

public class Document {

    private final String text;
    private final List<DecorationModel> decorationList;
    private final List<Paragraph> paragraphList;
    private final int caretPosition;

    public Document() {
        this("");
    }

    public Document(String text) {
        this(text, 0);
    }

    public Document(String text, int caretPosition) {
        this(text,
                List.of(new DecorationModel(0, text.length(), TextDecoration.builder().presets().build())),
                List.of(new Paragraph(0, text.length(), ParagraphDecoration.builder().presets().build())),
                caretPosition);
    }

    public Document(String text, List<DecorationModel> decorationList, List<Paragraph> paragraphList, int caretPosition) {
        this.text = text;
        this.decorationList = decorationList;
        this.paragraphList = paragraphList;
        this.caretPosition = caretPosition;
    }

    public String getText() {
        return text;
    }

    public List<DecorationModel> getDecorationList() {
        return decorationList;
    }

    public List<Paragraph> getParagraphList() {
        return paragraphList;
    }

    public int getCaretPosition() {
        return caretPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return Objects.equals(text, document.text) &&
                Objects.equals(decorationList, document.decorationList) &&
                Objects.equals(paragraphList, document.paragraphList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, decorationList, paragraphList, caretPosition);
    }

    @Override
    public String toString() {
        return "Document{" +
                "text='" + text.replaceAll("\n", "<n>").replaceAll(ZERO_WIDTH_TEXT, "<a>")  + '\'' +
                ", decorationList=" + (decorationList == null ? "null" : "{" +
                    decorationList.stream().map(decorationModel -> " - " + decorationModel.toString()).collect(Collectors.joining("\n", "\n", ""))) +
                ", paragraphDecorationList=" + (paragraphList == null ? "null" : "{" +
                    paragraphList.stream().map(decorationModel -> " - " + decorationModel.toString()).collect(Collectors.joining("\n", "\n", ""))) +
                "\n}, caretPosition=" + caretPosition +
                '}';
    }
}
