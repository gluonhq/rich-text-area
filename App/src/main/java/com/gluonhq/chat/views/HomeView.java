package com.gluonhq.chat.views;

import com.gluonhq.attach.display.DisplayService;
import com.gluonhq.attach.orientation.OrientationService;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.mvc.View;
import javafx.geometry.Orientation;

import static com.gluonhq.chat.GluonChat.LANDSCAPE_VIEW;
import static com.gluonhq.chat.GluonChat.PORTRAIT_VIEW;

public class HomeView extends View {

    public HomeView() {
        boolean tablet = DisplayService.create()
                .map(DisplayService::isTablet)
                .orElse(false);

        OrientationService.create().ifPresent(o -> {
            o.orientationProperty().addListener((obs, ov, nv) -> configView(tablet, nv));
        });

        Orientation orientation = OrientationService.create()
                .flatMap(OrientationService::getOrientation)
                .orElse(Orientation.HORIZONTAL);

        showingProperty().addListener((obs, ov, nv) -> {
            if (nv) {
                configView(tablet, orientation);
            }
        });
    }

    private void configView(boolean tablet, Orientation orientation) {
        if (tablet && orientation == Orientation.HORIZONTAL) {
            MobileApplication.getInstance().switchView(LANDSCAPE_VIEW);
        } else {
            MobileApplication.getInstance().switchView(PORTRAIT_VIEW)
                    .ifPresent(p -> ((PortraitView) p).loadChat());
        }
    }
}
