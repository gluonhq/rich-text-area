package com.gluonhq.chat.views;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import javafx.scene.control.Button;

import java.util.ResourceBundle;

public class PortraitView extends View {

    private final ResourceBundle resources;

    private Button users;

    public PortraitView() {
        resources = ResourceBundle.getBundle("com.gluonhq.chat.views.portrait");
        showingProperty().addListener((obs, ov, nv) -> {
            AppBar appBar = MobileApplication.getInstance().getAppBar();
            if (nv) {
                appBar.setNavIcon(MaterialDesignIcon.MENU.button());
                appBar.setTitleText(resources.getString("portrait.view.title"));
                users = MaterialDesignIcon.PEOPLE.button(e -> loadUsers());
                users.managedProperty().bind(users.visibleProperty());
                Button chat = MaterialDesignIcon.CHAT.button(e -> loadChat());
                chat.visibleProperty().bind(users.visibleProperty().not());
                chat.managedProperty().bind(chat.visibleProperty());
                appBar.getActionItems().addAll(users, chat);
            }
        });
    }

    void loadChat() {
        ChatView chatView = new ChatView();
        setCenter(chatView);
        users.setVisible(true);
    }

    void loadUsers() {
        UsersView usersView = new UsersView();
        setCenter(usersView);
        users.setVisible(false);
    }
}
