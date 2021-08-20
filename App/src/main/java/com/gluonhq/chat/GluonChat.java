package com.gluonhq.chat;

import com.airhacks.afterburner.injection.Injector;
import com.gluonhq.attach.lifecycle.LifecycleService;
import com.gluonhq.attach.util.Platform;
import com.gluonhq.charm.glisten.afterburner.GluonInstanceProvider;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.visual.Swatch;
import com.gluonhq.chat.service.DummyService;
import com.gluonhq.chat.service.Service;
import com.gluonhq.chat.views.AppViewManager;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class GluonChat extends MobileApplication {

    private static final Logger LOG = Logger.getLogger(GluonChat.class.getName());
    private static final int DESKTOP_DEFAULT_WIDTH = 800;
    private static final int DESKTOP_DEFAULT_HEIGHT = 600;
    
    public static final int VIEW_CHANGE_WIDTH = 600;
    
    static {
        try {
            LogManager.getLogManager().readConfiguration(GluonChat.class.getResourceAsStream("/logging.properties"));

        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Storage Service Error", e);
        }
    }

    private static final GluonInstanceProvider instanceSupplier = new GluonInstanceProvider() {{
        // bindProvider(Service.class, CloudlinkService::new);
        bindProvider(Service.class, DummyService::new);

        Injector.setInstanceSupplier(this);
    }};

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

        // Default size for desktop
        if (Platform.isDesktop()) {
            scene.getWindow().setWidth(DESKTOP_DEFAULT_WIDTH);
            scene.getWindow().setHeight(DESKTOP_DEFAULT_HEIGHT);
        }

        // TODO: Remove
        //ScenicView.show(scene);
    }

    public static void main(String[] args) {
//        System.setProperty("charm-desktop-form", "tablet");
        launch(args);
    }
}