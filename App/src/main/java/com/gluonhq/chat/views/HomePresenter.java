package com.gluonhq.chat.views;

import com.gluonhq.attach.display.DisplayService;
import com.gluonhq.attach.orientation.OrientationService;
import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.application.ViewStackPolicy;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.chat.service.Service;
import javafx.geometry.Orientation;
import com.gluonhq.chat.GluonChat;
import javafx.fxml.FXML;

import javax.inject.Inject;

public class HomePresenter extends GluonPresenter<GluonChat> {

    @FXML private View homeView;
    @Inject private Service service;

    public void initialize() {
        boolean tablet = DisplayService.create()
                .map(DisplayService::isTablet)
                .orElse(false);

        OrientationService.create().ifPresent(o -> {
            o.orientationProperty().addListener((obs, ov, nv) -> setupView(tablet, nv));
        });

        Orientation orientation = OrientationService.create()
                .flatMap(OrientationService::getOrientation)
                .orElse(Orientation.HORIZONTAL);

        homeView.showingProperty().addListener((obs, ov, nv) -> {
            if (nv) {
                setupView(tablet, orientation);
            }
        });
    }

    private void setupView(boolean tablet, Orientation orientation) {
        service.getName(n -> {
            if (n != null && n.get() != null && !n.get().isEmpty()) {
                setupAuthenticatedView(tablet, orientation);
            } else {
                AppViewManager.LOGIN_VIEW.switchView(ViewStackPolicy.SKIP);
            }
        });
    }

    private void setupAuthenticatedView(boolean tablet, Orientation orientation) {
        if (tablet && orientation == Orientation.HORIZONTAL) {
            AppViewManager.LANDSCAPE_VIEW.switchView()
                    .ifPresent(p -> ((LandscapePresenter) p).loadLandscapeView());
        } else {
            AppViewManager.PORTRAIT_VIEW.switchView()
                    .ifPresent(p -> ((PortraitPresenter) p).loadChat());
        }
    }
}
