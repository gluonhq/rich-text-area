package com.gluonhq.richtext;

import javafx.scene.control.IndexRange;

class InsertTextCommand implements Command {

    private final String content;

    private int caretPosition;
    private IndexRange selection;

    public InsertTextCommand(String content) {
        this.content = content;
    }

    @Override
    public void redo( EditableTextFlow textFlow ) {
        this.caretPosition = textFlow.getCaretPosition();
        this.selection = textFlow.getSelection();
        textFlow.insertText(content);
    }

    @Override
    public void undo( EditableTextFlow textFlow ) {
        textFlow.removeText(textFlow.getCaretPosition(), content.length());
        textFlow.setSelection(selection);
        textFlow.setCaretPosition(caretPosition);
    }
}
