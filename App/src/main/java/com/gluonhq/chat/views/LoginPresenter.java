package com.gluonhq.chat.views;

import com.gluonhq.charm.glisten.application.AppManager;
import com.gluonhq.charm.glisten.application.ViewStackPolicy;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.chat.service.BootstrapClient;
import com.gluonhq.chat.service.Service;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;


import javax.inject.Inject;
import java.util.ResourceBundle;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;

public class LoginPresenter implements BootstrapClient{

    @FXML private View loginView;
    @FXML private ImageView qrImageView;

    @Inject private Service service;

    @FXML private ResourceBundle resources;

    public void initialize() {
        System.err.println("LOGINPRESENTER init");
        loginView.setOnShowing(e -> {
            System.err.println("LOGINPRESENTER showing!");
            AppManager.getInstance().getAppBar().setVisible(false);
            AppManager.getInstance().getAppBar().setManaged(false);
        });
        if (service.loggedUser() != null) {
            System.err.println("LOGINPRESENTER nu");
            nextStep();
        } else {
            System.err.println("LOGINPRESENTER bootstrap");
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

    @Override 
    public void bootstrapFailed(String msg) {
        System.err.println("BOOTSTRAP FAILED");
        Dialog dialog = new Dialog();
        dialog.setTitle("bootstrap failed");
        ButtonType type = new ButtonType("Ok", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(type);
        dialog.setContentText(msg);
        dialog.showAndWait();
    }

    private void nextStep() {
        System.err.println("LOGINPRESENTER, nextstep!");
        AppViewManager.FIRST_VIEW.switchView(ViewStackPolicy.SKIP);
    }
}
