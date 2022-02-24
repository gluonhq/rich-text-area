package com.gluonhq.richtext.undo;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public class CommandManager<T> {

    final Deque<AbstractCommand<T>> undoStack = new ArrayDeque<>();
    final Deque<AbstractCommand<T>> redoStack = new ArrayDeque<>();
    final T context;
    private final Runnable runnable;

    public CommandManager(T context) {
        this(context, null);
    }

    public CommandManager(T context, Runnable runnable) {
        this.context = context;
        this.runnable = runnable;
    }

    public void execute(AbstractCommand<T> cmd) {
        Objects.requireNonNull(cmd).redo(context);
        undoStack.push(cmd);
        redoStack.clear();
        end();
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            var cmd = undoStack.pop();
            cmd.undo(context);
            redoStack.push(cmd);
            end();
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            var cmd = redoStack.pop();
            cmd.redo(context);
            undoStack.push(cmd);
            end();
        }
    }

    public boolean isUndoStackEmpty() {
        return undoStack.isEmpty();
    }

    public boolean isRedoStackEmpty() {
        return redoStack.isEmpty();
    }

    private void end() {
        if (runnable != null) {
            runnable.run();
        }
    }
}
