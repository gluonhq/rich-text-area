package com.gluonhq.richtext.viewmodel;

import com.gluonhq.richtext.Tools;
import javafx.scene.input.KeyEvent;

public class ActionCmdCaretMove implements ActionCmd {

    //TODO move direction into  this action?
    RichTextAreaViewModel.Direction direction;
    boolean changeSelection;
    boolean wordSelection;
    boolean lineSelection;

    public ActionCmdCaretMove(RichTextAreaViewModel.Direction direction, boolean changeSelection, boolean wordSelection, boolean lineSelection) {
        this.direction = direction;
        this.changeSelection = changeSelection;
        this.wordSelection = wordSelection;
        this.lineSelection = lineSelection;
    }

    public ActionCmdCaretMove(RichTextAreaViewModel.Direction direction, KeyEvent event ) {
        this( direction,
              event.isShiftDown(),
              Tools.MAC ? event.isAltDown() : event.isControlDown(),
              Tools.MAC && event.isShortcutDown());
    }

    public void apply(RichTextAreaViewModel viewModel) {
        viewModel.moveCaret(direction,changeSelection, wordSelection, lineSelection);
    }

}
