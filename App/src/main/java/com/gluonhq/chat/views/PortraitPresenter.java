package com.gluonhq.chat.views;

import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import javafx.scene.control.Button;
import com.gluonhq.chat.GluonChat;
import javafx.fxml.FXML;

import java.util.ResourceBundle;

public class PortraitPresenter extends GluonPresenter<GluonChat> {

    @FXML private View portraitView;

    @FXML private ResourceBundle resources;

    private Button users;

    public void initialize() {
        portraitView.showingProperty().addListener((obs, ov, nv) -> {
            AppBar appBar = getApp().getAppBar();
            appBar.setNavIcon(MaterialDesignIcon.MENU.button());
            appBar.setTitleText(resources.getString("portrait.view.title"));
            users = MaterialDesignIcon.PEOPLE.button(e -> loadUsers());
            users.managedProperty().bind(users.visibleProperty());
            Button chat = MaterialDesignIcon.CHAT.button(e -> loadChat());
            chat.visibleProperty().bind(users.visibleProperty().not());
            chat.managedProperty().bind(chat.visibleProperty());
            appBar.getActionItems().addAll(users, chat);
        });
    }

    void loadChat() {
        MobileApplication.getInstance().retrieveView(AppViewManager.CHAT_VIEW.getId())
                .ifPresentOrElse(portraitView::setCenter,
                        () -> System.out.println("Error finding CHAT_VIEW"));
        users.setVisible(true);
    }

    void loadUsers() {
        MobileApplication.getInstance().retrieveView(AppViewManager.USERS_VIEW.getId())
                .ifPresentOrElse(portraitView::setCenter,
                        () -> System.out.println("Error finding USERS_VIEW"));
        users.setVisible(false);
    }
}
