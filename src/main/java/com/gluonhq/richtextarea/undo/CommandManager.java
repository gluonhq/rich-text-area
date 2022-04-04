package com.gluonhq.richtextarea.undo;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandManager<T> {

    public static final Logger LOGGER = Logger.getLogger(CommandManager.class.getName());

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
        Objects.requireNonNull(cmd).execute(context);
        undoStack.push(cmd);
        redoStack.clear();
        end();
        LOGGER.log(Level.FINE, "Execute: " + this);
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            var cmd = undoStack.pop();
            cmd.undo(context);
            redoStack.push(cmd);
            end();
            LOGGER.log(Level.FINE, "Undo: " + this);
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            var cmd = redoStack.pop();
            cmd.redo(context);
            undoStack.push(cmd);
            end();
            LOGGER.log(Level.FINE, "Redo: " + this);
        }
    }

    public int getUndoStackSize() {
        return undoStack.size();
    }

    public int getRedoStackSize() {
        return redoStack.size();
    }

    public void clearStacks() {
        undoStack.clear();
        redoStack.clear();
    }

    private void end() {
        if (runnable != null) {
            runnable.run();
        }
    }

    @Override
    public String toString() {
        return "CommandManager<" + context.getClass().getSimpleName() + ">{\n" +
                " - undoStack=" + undoStack +
                "\n - redoStack=" + redoStack +
                "\n}";
    }
}
