package com.gluonhq.richtext.model;

import java.util.List;

public class FaceModel {

    private final String text;
    private final List<DecorationModel> decorationList;
    private final int caretPosition;

    public FaceModel() {
        this("", null, 0);
    }

    public FaceModel(String text) {
        this(text, null, 0);
    }

    public FaceModel(String text, int caretPosition) {
        this(text, null, caretPosition);
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

}
