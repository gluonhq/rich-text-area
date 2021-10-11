package com.gluonhq.chat.views;

import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.application.ViewStackPolicy;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.chat.GluonChat;
import com.gluonhq.chat.service.BootstrapClient;
import com.gluonhq.chat.service.Service;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;


import javax.inject.Inject;
import java.util.ResourceBundle;
import javafx.scene.image.Image;

public class LoginPresenter extends GluonPresenter<GluonChat> implements BootstrapClient{

    @FXML private View loginView;
    @FXML private ImageView qrImageView;

    @Inject private Service service;

    @FXML private ResourceBundle resources;

    public void initialize() {
        loginView.setOnShowing(e -> {
            getApp().getAppBar().setVisible(false);
            getApp().getAppBar().setManaged(false);
        });
        if (service.loggedUser() != null) {
            nextStep();
        } else {
            service.bootstrap(this);
        }
    }

    @Override
    public void gotImage(Image img) {
        this.qrImageView.setImage(img);
    }

    @Override
    public void bootstrapSucceeded() {
        nextStep();
    }

    private void nextStep() {
        AppViewManager.FIRST_VIEW.switchView(ViewStackPolicy.SKIP);
    }
}
