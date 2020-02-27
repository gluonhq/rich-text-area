package com.gluonhq.chat.views;

import com.gluonhq.attach.display.DisplayService;
import com.gluonhq.attach.orientation.OrientationService;
import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.mvc.View;
import javafx.geometry.Orientation;
import com.gluonhq.chat.GluonChat;
import javafx.fxml.FXML;

public class HomePresenter extends GluonPresenter<GluonChat> {

    @FXML private View homeView;

    public void initialize() {
        boolean tablet = DisplayService.create()
                .map(DisplayService::isTablet)
                .orElse(false);

        OrientationService.create().ifPresent(o -> {
            o.orientationProperty().addListener((obs, ov, nv) -> configView(tablet, nv));
        });

        Orientation orientation = OrientationService.create()
                .flatMap(OrientationService::getOrientation)
                .orElse(Orientation.HORIZONTAL);

        homeView.showingProperty().addListener((obs, ov, nv) -> {
            if (nv) {
                configView(tablet, orientation);
            }
        });
    }

    private void configView(boolean tablet, Orientation orientation) {
        if (tablet && orientation == Orientation.HORIZONTAL) {
            AppViewManager.LANDSCAPE_VIEW.switchView()
                    .ifPresent(p -> ((LandscapePresenter) p).loadLandscapeView());
        } else {
            AppViewManager.PORTRAIT_VIEW.switchView()
                    .ifPresent(p -> ((PortraitPresenter) p).loadChat());
        }
    }
}
