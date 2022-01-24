package com.gluonhq.richtext;

import com.gluonhq.richtext.model.TextBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommandManager {

    private final RichTextAreaSkin skin;
    private final List<Command> commands = new ArrayList<>();
    private int undoCmdIndex = -1;

    CommandManager( RichTextAreaSkin skin ) {
        this.skin =  Objects.requireNonNull(skin);
    }

    public void execute( Command command) {
        Objects.requireNonNull(command).redo(skin);
        if (canRedo()) {
          // clear all commands after current one
          commands.removeAll( commands.subList( undoCmdIndex+1, commands.size()));
        }
        commands.add(command);
        undoCmdIndex = commands.size()-1;
    }

    public boolean canUndo() {
        return !commands.isEmpty() && undoCmdIndex >= 0;
    }

    public boolean canRedo() {
       return !commands.isEmpty() &&
               undoCmdIndex < commands.size()-1; // at least one more command available for redo op
    }

    public void undo() {
        if (canUndo()) {
            commands.get(undoCmdIndex--).undo(skin);
        }
    }

    public void redo() {
        if (canRedo()) {
            commands.get(undoCmdIndex++).redo(skin);
        }
    }

}
