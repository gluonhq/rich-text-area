package com.gluonhq.chat;

import com.gluonhq.attach.lifecycle.LifecycleService;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.visual.Swatch;
import com.gluonhq.chat.views.HomeView;
import com.gluonhq.chat.views.LandscapeView;
import com.gluonhq.chat.views.MapsView;
import com.gluonhq.chat.views.PortraitView;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Random;

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
        
        // Re-adjust the window at random co-ordinates in Primary Screen
        final Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        final int x = new Random().nextInt((int) visualBounds.getWidth());
        final int y = new Random().nextInt((int) visualBounds.getHeight());
        scene.getWindow().setX(x);
        scene.getWindow().setY(y);
    }

    public static void main(String[] args) {
        launch(args);
    }

}