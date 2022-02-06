package com.gluonhq.richtext.undo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
        commander.execute( new TestCommand());
        commander.undo();
        commander.redo();
        Assertions.assertEquals( 1,  commander.undoStack.size());
        Assertions.assertEquals( 0,  commander.redoStack.size());
        Assertions.assertEquals( "Text-redo",  commander.context.toString());

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
