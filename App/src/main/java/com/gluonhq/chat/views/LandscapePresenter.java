package com.gluonhq.chat.views;

import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.chat.GluonChat;
import javafx.fxml.FXML;

import java.util.ResourceBundle;

public class LandscapePresenter extends GluonPresenter<GluonChat> {

    @FXML private View landscapeView;

    @FXML private ResourceBundle resources;

    public void initialize() {
        landscapeView.showingProperty().addListener((obs, ov, nv) -> {
            AppBar appBar = getApp().getAppBar();
            if (nv) {
                appBar.setNavIcon(MaterialDesignIcon.MENU.button(/*e ->
                        getApp().getDrawer().open()*/));
                appBar.setTitleText(resources.getString("landscape.view.title"));
            }
        });
    }

    void loadLandscapeView() {
        MobileApplication.getInstance().retrieveView(AppViewManager.CHAT_VIEW.getId())
                .ifPresentOrElse(landscapeView::setCenter,
                        () -> System.out.println("Error finding CHAT_VIEW"));

        MobileApplication.getInstance().retrieveView(AppViewManager.CHANNEL_VIEW.getId())
                .ifPresentOrElse(landscapeView::setLeft,
                        () -> System.out.println("Error finding USERS_VIEW"));
    }

}
