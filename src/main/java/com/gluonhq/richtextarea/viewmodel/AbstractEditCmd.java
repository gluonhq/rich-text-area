package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.Selection;

import java.util.Objects;


/**
 * Abstract command add context store/restore operations
 * to already existing undo/redo framework
 */
abstract class AbstractEditCmd extends com.gluonhq.richtextarea.undo.AbstractCommand<RichTextAreaViewModel> {

    private int caretPosition;
    Selection selection;


    protected void storeContext( RichTextAreaViewModel viewModel ) {
        Objects.requireNonNull(viewModel);
        this.caretPosition = viewModel.getCaretPosition();
        this.selection = viewModel.getSelection();
    }

    protected void restoreContext( RichTextAreaViewModel viewModel ) {
        Objects.requireNonNull(viewModel);
        viewModel.setCaretPosition(caretPosition);
        viewModel.setSelection(selection);
    }

    @Override
    public String toString() {
        return "AbstractEditCmd { " +
                "c=" + caretPosition +
                ", s=[" + selection.getStart() + "," + selection.getEnd() + "] " +
                "}";
    }
}
