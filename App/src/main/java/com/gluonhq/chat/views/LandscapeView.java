package com.gluonhq.chat.views;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;

import java.util.ResourceBundle;

public class LandscapeView extends View {

    private final ResourceBundle resources;

    public LandscapeView() {
        resources = ResourceBundle.getBundle("com.gluonhq.chat.views.landscape");

        showingProperty().addListener((obs, ov, nv) -> {
            AppBar appBar = MobileApplication.getInstance().getAppBar();
            if (nv) {
                appBar.setNavIcon(MaterialDesignIcon.MENU.button());
                appBar.setTitleText(resources.getString("landscape.view.title"));
            }
        });

        ChatView chatView = new ChatView();
        setCenter(chatView);

        UsersView usersView = new UsersView();
        setLeft(usersView);
    }
}
