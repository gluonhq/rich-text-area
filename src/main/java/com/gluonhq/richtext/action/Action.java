package com.gluonhq.richtext.action;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.event.ActionEvent;

public interface Action {

    void execute(ActionEvent event);

    ReadOnlyBooleanProperty disabledProperty();

}
