package com.gluonhq.richtextarea.undo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class CmdManagerTests {

    @Test
    @DisplayName("Passes command execution rules")
    public void executionRules() {

        StringBuilder text = new StringBuilder("Text");
        CommandManager<StringBuilder> commander = new CommandManager<>(text);
        commander.execute( new TestCommand());

        Assertions.assertEquals( 1,  commander.undoStack.size());
        Assertions.assertEquals( 0,  commander.redoStack.size());
        Assertions.assertEquals( "Text-redo",  commander.context.toString());

    }

    @Test
    @DisplayName("Passes undo rules")
    public void undoRules() {

        StringBuilder text = new StringBuilder("Text");
        CommandManager<StringBuilder> commander = new CommandManager<>(text);

        commander.execute( new TestCommand());
        Assertions.assertEquals( 1,  commander.undoStack.size());

        commander.undo();
        Assertions.assertEquals( 0,  commander.undoStack.size());
        Assertions.assertEquals( 1,  commander.redoStack.size());
        Assertions.assertEquals( "Text",  commander.context.toString());

    }

    @Test
    @DisplayName("Passes redo rules")
    public void redoRules() {

        StringBuilder text = new StringBuilder("Text");
        CommandManager<StringBuilder> commander = new CommandManager<>(text);
        commander.execute(new TestCommand());
        commander.undo();
        commander.redo();
        Assertions.assertEquals( 1,  commander.undoStack.size());
        Assertions.assertEquals( 0,  commander.redoStack.size());
        Assertions.assertEquals( "Text-redo",  commander.context.toString());

    }

    @Test
    @DisplayName("undo and redo stack must be empty initially")
    public void undoAndRedoStackEmpty() {
        StringBuilder text = new StringBuilder("Text");
        CommandManager<StringBuilder> commander = new CommandManager<>(text);
        Assertions.assertAll(
                () -> Assertions.assertEquals(0, commander.getUndoStackSize()),
                () -> Assertions.assertEquals(0, commander.getRedoStackSize())
        );
    }

    @Test
    @DisplayName("undo stack must not be empty after a command execution")
    public void undoStackNotEmptyAfterCommandExecution() {
        StringBuilder text = new StringBuilder("Text");
        CommandManager<StringBuilder> commander = new CommandManager<>(text);
        commander.execute(new TestCommand());
        Assertions.assertNotEquals(0, commander.getUndoStackSize());
    }

    @Test
    @DisplayName("redo stack must be empty after a command execution")
    public void redoStackEmptyAfterCommandExecution() {
        StringBuilder text = new StringBuilder("Text");
        CommandManager<StringBuilder> commander = new CommandManager<>(text);
        commander.execute(new TestCommand());
        Assertions.assertEquals(0, commander.getRedoStackSize());
    }

    @Test
    @DisplayName("undo stack must be empty after a undo operation")
    public void undoStackEmptyAfterUndoExecution() {
        StringBuilder text = new StringBuilder("Text");
        CommandManager<StringBuilder> commander = new CommandManager<>(text);
        commander.execute(new TestCommand());
        commander.undo();
        Assertions.assertEquals(0, commander.getUndoStackSize());
    }

    @Test
    @DisplayName("redo stack must not be empty after undo operation")
    public void redoStackNotEmptyAfterUndoOperation() {
        StringBuilder text = new StringBuilder("Text");
        CommandManager<StringBuilder> commander = new CommandManager<>(text);
        commander.execute(new TestCommand());
        commander.undo();
        Assertions.assertNotEquals(0, commander.getRedoStackSize());
    }

    @Test
    @DisplayName("redo stack must be empty after redo operation")
    public void redoStackEmptyAfterRedoOperation() {
        StringBuilder text = new StringBuilder("Text");
        CommandManager<StringBuilder> commander = new CommandManager<>(text);
        commander.execute(new TestCommand());
        commander.undo();
        commander.redo();
        Assertions.assertEquals(0, commander.getRedoStackSize());
    }

    @Test
    @DisplayName("undo stack must be not empty after redo operation")
    public void undoStackNotEmptyAfterRedoOperation() {
        StringBuilder text = new StringBuilder("Text");
        CommandManager<StringBuilder> commander = new CommandManager<>(text);
        commander.execute(new TestCommand());
        commander.undo();
        commander.redo();
        Assertions.assertNotEquals(0, commander.getUndoStackSize());
    }

    @Test
    @DisplayName("runnable called when command executed")
    public void runnableIsCalledWhenCommandIsExecuted() {
        StringBuilder text = new StringBuilder("Text");
        AtomicInteger aInteger = new AtomicInteger();
        CommandManager<StringBuilder> commander = new CommandManager<>(text, aInteger::incrementAndGet);
        commander.execute(new TestCommand());
        Assertions.assertEquals(1, aInteger.get());
    }

    @Test
    @DisplayName("runnable called when undo is executed")
    public void runnableIsCalledWhenUndoIsExecuted() {
        StringBuilder text = new StringBuilder("Text");
        AtomicInteger aInteger = new AtomicInteger();
        CommandManager<StringBuilder> commander = new CommandManager<>(text, aInteger::incrementAndGet);
        commander.execute(new TestCommand());
        commander.undo();
        Assertions.assertEquals(2, aInteger.get());
    }

    @Test
    @DisplayName("runnable called when redo is executed")
    public void runnableIsCalledWhenRedoIsExecuted() {
        StringBuilder text = new StringBuilder("Text");
        AtomicInteger aInteger = new AtomicInteger();
        CommandManager<StringBuilder> commander = new CommandManager<>(text, aInteger::incrementAndGet);
        commander.execute(new TestCommand());
        commander.undo();
        commander.redo();
        Assertions.assertEquals(3, aInteger.get());
    }
}

class TestCommand extends AbstractCommand<StringBuilder> {

    int pos = 0;
    int length = 0;

    @Override
    protected void doUndo(StringBuilder context) {
        context.delete(pos, pos+length);
    }

    @Override
    protected void doRedo(StringBuilder context) {
        String xxx = "-redo";
        pos = context.length();
        length = xxx.length();
        context.append("-redo");
    }
}
