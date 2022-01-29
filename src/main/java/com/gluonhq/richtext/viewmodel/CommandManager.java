package com.gluonhq.richtext.viewmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class CommandManager {

    private final RichTextAreaViewModel viewModel;
    private final List<Command> commands = new ArrayList<>();
    private int undoCmdIndex = -1;

    CommandManager( RichTextAreaViewModel viewModel ) {
        this.viewModel =  Objects.requireNonNull(viewModel);
    }

    public void execute( Command command) {
        Objects.requireNonNull(command).redo(viewModel);
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
            commands.get(undoCmdIndex--).undo(viewModel);
        }
    }

    public void redo() {
        if (canRedo()) {
            commands.get(undoCmdIndex++).redo(viewModel);
        }
    }

}
