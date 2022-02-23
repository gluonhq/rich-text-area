package com.gluonhq.richtext.undo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public class CmdManagerTests {

   @Test
   @DisplayName("Passes command execution rules")
   public void executionRules() {

       StringBuilder text = new StringBuilder("Text");
       CommandManager<StringBuilder> commander = new CommandManager<>(text);
       Assertions.assertTrue(commander.isUndoStackEmpty());
       Assertions.assertTrue(commander.isRedoStackEmpty());

       commander.execute(new TestCommand());

       Assertions.assertEquals(1, commander.undoStack.size());
       Assertions.assertEquals(0, commander.redoStack.size());
       Assertions.assertFalse(commander.isUndoStackEmpty());
       Assertions.assertTrue(commander.isRedoStackEmpty());
       Assertions.assertEquals("Text-redo", commander.context.toString());

   }

    @Test
    @DisplayName("Passes undo rules")
    public void undoRules() {

        StringBuilder text = new StringBuilder("Text");
        CommandManager<StringBuilder> commander = new CommandManager<>(text);

        commander.execute( new TestCommand());
        Assertions.assertEquals(1, commander.undoStack.size());

        commander.undo();
        Assertions.assertEquals(0, commander.undoStack.size());
        Assertions.assertEquals(1, commander.redoStack.size());
        Assertions.assertTrue(commander.isUndoStackEmpty());
        Assertions.assertFalse(commander.isRedoStackEmpty());
        Assertions.assertEquals("Text", commander.context.toString());

    }

    @Test
    @DisplayName("Passes redo rules")
    public void redoRules() {

        StringBuilder text = new StringBuilder("Text");
        CommandManager<StringBuilder> commander = new CommandManager<>(text);
        commander.execute(new TestCommand());
        commander.undo();
        commander.redo();
        Assertions.assertEquals(1, commander.undoStack.size());
        Assertions.assertEquals(0, commander.redoStack.size());
        Assertions.assertFalse(commander.isUndoStackEmpty());
        Assertions.assertTrue(commander.isRedoStackEmpty());
        Assertions.assertEquals("Text-redo", commander.context.toString());

    }

    @Test
    @DisplayName("Passes multiple redo rules")
    public void multipleRedoRules() {
        StringBuilder text = new StringBuilder("Text");
        CommandManager<StringBuilder> commander = new CommandManager<>(text);
        Assertions.assertEquals("Text",  commander.context.toString());
        Assertions.assertTrue(commander.isUndoStackEmpty());
        Assertions.assertTrue(commander.isRedoStackEmpty());
        commander.execute(new TestCommand());
        commander.execute(new TestCommand());
        commander.execute(new TestCommand());
        Assertions.assertEquals( "Text-redo-redo-redo",  commander.context.toString());
        Assertions.assertEquals( 3,  commander.undoStack.size());
        Assertions.assertFalse(commander.isUndoStackEmpty());
        Assertions.assertTrue(commander.isRedoStackEmpty());
        commander.undo();
        commander.undo();
        commander.undo();
        Assertions.assertEquals( "Text",  commander.context.toString());
        Assertions.assertEquals( 3,  commander.redoStack.size());
        Assertions.assertTrue(commander.isUndoStackEmpty());
        Assertions.assertFalse(commander.isRedoStackEmpty());
        commander.redo();
        commander.redo();
        commander.redo();
        Assertions.assertEquals( 3,  commander.undoStack.size());
        Assertions.assertFalse(commander.isUndoStackEmpty());
        Assertions.assertTrue(commander.isRedoStackEmpty());
        Assertions.assertEquals( "Text-redo-redo-redo",  commander.context.toString());
    }

    private CommandManager<StringBuilder> commander;
    private AtomicBoolean emptyUndo;
    private AtomicBoolean emptyRedo;

    @Test
    @DisplayName("Passes redo rules with runnable")
    public void redoRulesWithRunnable() {
        StringBuilder text = new StringBuilder("Text");
        emptyUndo = new AtomicBoolean(true);
        emptyRedo = new AtomicBoolean(true);
        commander = new CommandManager<>(text, this::update);
        Assertions.assertTrue(emptyUndo.get());
        Assertions.assertTrue(emptyRedo.get());
        commander.execute(new TestCommand());
        Assertions.assertFalse(emptyUndo.get());
        Assertions.assertTrue(emptyRedo.get());
        commander.undo();
        Assertions.assertTrue(emptyUndo.get());
        Assertions.assertFalse(emptyRedo.get());
        commander.redo();
        Assertions.assertFalse(emptyUndo.get());
        Assertions.assertTrue(emptyRedo.get());
    }

    private void update() {
        emptyUndo.set(commander.isUndoStackEmpty());
        emptyRedo.set(commander.isRedoStackEmpty());
    }
}

class TestCommand extends AbstractCommand<StringBuilder> {

    int pos = 0;
    int length = 0;

    @Override
    protected void doUndo(StringBuilder context) {
        context.delete( pos, pos+length);
    }

    @Override
    protected void doRedo(StringBuilder context) {
        String xxx = "-redo";
        pos = context.length();
        length = xxx.length();
        context.append("-redo");
    }
}
