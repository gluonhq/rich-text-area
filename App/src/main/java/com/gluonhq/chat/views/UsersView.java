package com.gluonhq.chat.views;


import com.airhacks.afterburner.injection.Injector;
import com.gluonhq.charm.glisten.control.CharmListView;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.chat.service.Service;

import java.util.ResourceBundle;

public class UsersView extends View {

    private Service service;

    private final CharmListView<String, String> usersList;

    private final ResourceBundle resources;

    public UsersView() {
        resources = ResourceBundle.getBundle("com.gluonhq.chat.views.users");
        getStylesheets().add(UsersView.class.getResource("users.css").toExternalForm());

        usersList = new CharmListView<>();
        setCenter(usersList);

        service = Injector.instantiateModelOrService(Service.class);
        service.getMessages(m -> usersList.setItems(service.getNames(m)));

    }
}
