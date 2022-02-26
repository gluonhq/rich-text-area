package com.gluonhq.richtext.action;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.event.ActionEvent;

public interface Action {

    enum ActionType {
        UNDO, REDO,
        COPY, CUT, PASTE,
        DECORATE
    }

    void apply(ActionEvent event);

    ReadOnlyBooleanProperty disabledProperty();

}
