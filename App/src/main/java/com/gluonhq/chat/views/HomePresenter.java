package com.gluonhq.chat.views;

import com.gluonhq.attach.display.DisplayService;
import com.gluonhq.attach.orientation.OrientationService;
import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.application.ViewStackPolicy;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.chat.GluonChat;
import com.gluonhq.chat.service.Service;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;

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
        if (/*tablet && */orientation == Orientation.HORIZONTAL) {
            AppViewManager.LANDSCAPE_VIEW.switchView()
                    .ifPresent(p -> ((LandscapePresenter) p).loadLandscapeView());
            MobileApplication.getInstance().getGlassPane().getScene().getWindow().setWidth(800);
            MobileApplication.getInstance().getGlassPane().getScene().getWindow().setHeight(600);
        } else {
            AppViewManager.PORTRAIT_VIEW.switchView()
                    .ifPresent(p -> ((PortraitPresenter) p).loadChat());
            MobileApplication.getInstance().getGlassPane().getScene().getWindow().setWidth(350);
            MobileApplication.getInstance().getGlassPane().getScene().getWindow().setHeight(600);
        }
    }
}
