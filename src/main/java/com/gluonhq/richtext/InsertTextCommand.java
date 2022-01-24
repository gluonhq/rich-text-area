package com.gluonhq.richtext;

import javafx.scene.control.IndexRange;

import java.util.Objects;

class InsertTextCommand implements Command {

    private final String content;
    private int caretPosition;
    private IndexRange selection;

    public InsertTextCommand(String content) {
        this.content = content;
    }

    @Override
    public void redo( RichTextAreaSkin skin ) {
        this.caretPosition = skin.getCaretPosition();
        this.selection = skin.getSelection();
        Objects.requireNonNull(skin).insert(content);
    }

    @Override
    public void undo( RichTextAreaSkin skin ) {
//        textFlow.removeText(textFlow.getCaretPosition(), content.length());
//        textFlow.setSelection(selection);
//        textFlow.setCaretPosition(caretPosition);
    }
}
