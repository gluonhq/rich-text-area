package com.gluonhq.richtext.undo;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public class CommandManager<T> {

    final Deque<AbstractCommand<T>> undoStack = new ArrayDeque<>();
    final Deque<AbstractCommand<T>> redoStack = new ArrayDeque<>();
    final T context;

    public CommandManager(T context ) {
        this.context = context;
    }

    public void execute( AbstractCommand<T> cmd ) {
        Objects.requireNonNull(cmd).redo(context);
        undoStack.push(cmd);
        redoStack.clear();
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            var cmd = undoStack.pop();
            cmd.undo(context);
            redoStack.push(cmd);

        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            var cmd = redoStack.pop();
            cmd.redo(context);
            undoStack.push(cmd);
        }
    }


}
