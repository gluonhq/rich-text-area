package com.gluonhq.chat;

import com.gluonhq.attach.lifecycle.LifecycleService;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.visual.Swatch;
import com.gluonhq.chat.views.*;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class GluonChat extends MobileApplication {

    public static final String LANDSCAPE_VIEW = "LandscapeView";
    public static final String PORTRAIT_VIEW = "PortraitView";
    public static final String MAPS_VIEW = "MapsView";

    @Override
    public void init() {
        addViewFactory(HOME_VIEW, HomeView::new);
        addViewFactory(LANDSCAPE_VIEW, LandscapeView::new);
        addViewFactory(PORTRAIT_VIEW, PortraitView::new);
        addViewFactory(MAPS_VIEW, MapsView::new);
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
        launch(args);
    }

}