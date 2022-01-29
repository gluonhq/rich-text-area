package com.gluonhq.richtext.viewmodel;

interface Command {
    void redo( RichTextAreaViewModel viewModel );
    void undo( RichTextAreaViewModel viewModel );
}
