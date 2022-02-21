package com.gluonhq.richtext.undo;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public class CommandManager<T> {

    final Deque<AbstractCommand<T>> undoStack = new ArrayDeque<>();
    final Deque<AbstractCommand<T>> redoStack = new ArrayDeque<>();
    final T context;

    public CommandManager(T context) {
        this.context = context;
    }

    public void execute(AbstractCommand<T> cmd) {
        execute(cmd, null);
    }

    public void execute(AbstractCommand<T> cmd, Runnable runnable) {
        Objects.requireNonNull(cmd).redo(context);
        undoStack.push(cmd);
        redoStack.clear();
        if (runnable != null) {
            runnable.run();
        }
    }

    public void undo() {
        undo(null);
    }

    public void undo(Runnable runnable) {
        if (!undoStack.isEmpty()) {
            var cmd = undoStack.pop();
            cmd.undo(context);
            redoStack.push(cmd);
            if (runnable != null) {
                runnable.run();
            }
        }
    }

    public void redo() {
        redo(null);
    }

    public void redo(Runnable runnable) {
        if (!redoStack.isEmpty()) {
            var cmd = redoStack.pop();
            cmd.redo(context);
            undoStack.push(cmd);
            if (runnable != null) {
                runnable.run();
            }
        }
    }

    public int getUndoStackSize() {
        return undoStack.size();
    }

    public int getRedoStackSize() {
        return redoStack.size();
    }

}
