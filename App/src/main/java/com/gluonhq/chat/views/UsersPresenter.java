package com.gluonhq.chat.views;

import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.control.CharmListView;
import com.gluonhq.chat.GluonChat;
import com.gluonhq.chat.model.ChatMessage;
import com.gluonhq.chat.service.Service;
import com.gluonhq.connect.GluonObservableList;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.fxml.FXML;

import javax.inject.Inject;
import java.util.ResourceBundle;

public class UsersPresenter extends GluonPresenter<GluonChat> {

    @FXML private CharmListView<String, String> usersList;

    @Inject private Service service;
    @FXML private ResourceBundle resources;

    public void initialize() {
        service.getMessages(m -> usersList.setItems(service.getNames(m)));
    }
}
