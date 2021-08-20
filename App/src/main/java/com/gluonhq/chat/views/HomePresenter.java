package com.gluonhq.chat.views;

import com.gluonhq.attach.orientation.OrientationService;
import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.application.ViewStackPolicy;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.chat.GluonChat;
import com.gluonhq.chat.service.Service;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;

import javax.inject.Inject;

import static com.gluonhq.chat.GluonChat.VIEW_CHANGE_WIDTH;

public class HomePresenter extends GluonPresenter<GluonChat> {

    @FXML private View homeView;
    @Inject private Service service;
    
    private final ChangeListener<Number> widthListener = (o, ov, nv) -> changeOrientation(nv.doubleValue());

    public void initialize() {

        OrientationService.create().ifPresent(o -> o.orientationProperty().addListener(obs -> setupView()));

        homeView.showingProperty().addListener((obs, ov, nv) -> {
            if (nv) {
                setupView();
                final double width = homeView.getScene().getWidth();
                changeOrientation(width);
                homeView.getScene().widthProperty().addListener(widthListener);
            } else {
                homeView.getScene().widthProperty().removeListener(widthListener);
            }
        });
    }

    private void setupView() {
        if (service.loggedUser() == null) {
            AppViewManager.LOGIN_VIEW.switchView(ViewStackPolicy.SKIP);
        }
    }

    private void changeOrientation(double width) {
        if (width > VIEW_CHANGE_WIDTH) {
            AppViewManager.LANDSCAPE_VIEW.switchView()
                    .ifPresent(p -> ((LandscapePresenter) p).loadLandscapeView());
        } else {
            AppViewManager.PORTRAIT_VIEW.switchView()
                    .ifPresent(p -> ((PortraitPresenter) p).loadChat());
        }
    }
}
