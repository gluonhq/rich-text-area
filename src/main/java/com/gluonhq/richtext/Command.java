package com.gluonhq.richtext;

import com.gluonhq.richtext.model.TextBuffer;

public interface Command {
    void redo( RichTextAreaViewModel viewModel );
    void undo( RichTextAreaViewModel viewModel );
}
