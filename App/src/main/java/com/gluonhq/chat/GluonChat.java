package com.gluonhq.chat;

import com.gluonhq.attach.lifecycle.LifecycleService;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.visual.Swatch;
import com.gluonhq.chat.views.AppViewManager;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.LogManager;

public class GluonChat extends MobileApplication {

    static {
        try {
            LogManager.getLogManager().readConfiguration(GluonChat.class.getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init() {
        AppViewManager.registerViewsAndDrawer(this);
    }

    @Override
    public void postInit(Scene scene) {
        Swatch.RED.assignTo(scene);

        scene.getStylesheets().add(GluonChat.class.getResource("/styles.css").toExternalForm());
        ((Stage) scene.getWindow()).getIcons().add(new Image(GluonChat.class.getResourceAsStream("/icon.png")));

        scene.getWindow().setOnCloseRequest(e ->
                LifecycleService.create().ifPresent(LifecycleService::shutdown));
    }

    public static void main(String[] args) {
//        System.setProperty("charm-desktop-form", "tablet");
        launch(args);
    }

}