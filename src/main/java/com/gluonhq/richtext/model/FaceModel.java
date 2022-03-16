package com.gluonhq.richtext.model;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.gluonhq.richtext.model.PieceTable.ZERO_WIDTH_TEXT;

public class FaceModel {

    private static final String EMPTY_TEXT = "\u0001";
    public static FaceModel EMPTY_FACE_MODEL = new FaceModel(EMPTY_TEXT);

    private final String text;
    private final List<DecorationModel> decorationList;
    private final int caretPosition;

    public FaceModel() {
        this("");
    }

    public FaceModel(String text) {
        this(text, 0);
    }

    public FaceModel(String text, int caretPosition) {
        this(text, List.of(new DecorationModel(0, text.length(), TextDecoration.builder().presets().build())), caretPosition);
    }

    public FaceModel(String text, List<DecorationModel> decorationList, int caretPosition) {
        this.text = text;
        this.decorationList = decorationList;
        this.caretPosition = caretPosition;
    }

    public String getText() {
        return text;
    }

    public List<DecorationModel> getDecorationList() {
        return decorationList;
    }

    public int getCaretPosition() {
        return caretPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FaceModel faceModel = (FaceModel) o;
        return Objects.equals(text, faceModel.text) && Objects.equals(decorationList, faceModel.decorationList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, decorationList, caretPosition);
    }

    @Override
    public String toString() {
        return "FaceModel{" +
                "text='" + text.replaceAll("\n", "<n>").replaceAll(ZERO_WIDTH_TEXT, "<a>")  + '\'' +
                ", decorationList=" + (decorationList == null ? "null" : "{" +
                    decorationList.stream().map(decorationModel -> " - " + decorationModel.toString()).collect(Collectors.joining("\n", "\n", ""))) +
                "\n}, caretPosition=" + caretPosition +
                '}';
    }
}
