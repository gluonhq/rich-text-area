package com.gluonhq.richtext;

public interface Command {
    void redo( EditableTextFlow textFlow );
    void undo( EditableTextFlow textFlow );
}
