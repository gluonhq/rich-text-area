package com.gluonhq.richtext;

import com.gluonhq.richtext.model.TextDecoration;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.List;

public class FaceModel {

    private final String text;
    private final List<Decoration> decorationList;
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

    public FaceModel(String text, List<Decoration> decorationList, int caretPosition) {
        this.text = text;
        this.decorationList = decorationList;
        this.caretPosition = caretPosition;
    }

    public String getText() {
        return text;
    }

    public List<Decoration> getDecorationList() {
        return decorationList;
    }

    public int getCaretPosition() {
        return caretPosition;
    }

    public static class Decoration {
        private final int start;
        private final int length;
        private final TextDecoration textDecoration;

        public Decoration(int start, int length, TextDecoration textDecoration) {
            this.start = start;
            this.length = length;
            this.textDecoration = textDecoration;
        }

        public int getStart() {
            return start;
        }

        public int getLength() {
            return length;
        }

        public TextDecoration getTextDecoration() {
            return textDecoration;
        }
    }
}
