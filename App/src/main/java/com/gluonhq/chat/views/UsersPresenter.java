package com.gluonhq.chat.views;

import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.control.CharmListView;
import com.gluonhq.charm.glisten.control.TextField;
import com.gluonhq.chat.GluonChat;
import com.gluonhq.chat.model.User;
import com.gluonhq.chat.service.Service;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.util.Callback;

import javax.inject.Inject;
import java.util.ResourceBundle;

public class UsersPresenter extends GluonPresenter<GluonChat> {

    @FXML private TextField search;
    @FXML private CharmListView<User, String> usersList;

    @Inject private Service service;
    @FXML private ResourceBundle resources;
    
    private FilteredList<User> userFilteredList;

    public void initialize() {
        search.textProperty().addListener((o, ov, nv) -> userFilteredList.setPredicate(user -> {
            return user.getUsername().contains(nv) || user.getFirstname().contains(nv) || user.getLastname().contains(nv);
        }));
        userFilteredList = new FilteredList<>(service.getUsers());
        usersList.setItems(userFilteredList);
        usersList.setCellFactory(param -> new UserCell());
    }
 }
