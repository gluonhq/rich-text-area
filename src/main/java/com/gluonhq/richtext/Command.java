package com.gluonhq.richtext;

import com.gluonhq.richtext.model.TextBuffer;

public interface Command {
    void redo( RichTextAreaSkin skin );
    void undo( RichTextAreaSkin skin );
}
