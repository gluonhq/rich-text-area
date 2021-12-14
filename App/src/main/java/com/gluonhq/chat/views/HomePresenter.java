package com.gluonhq.chat.views;

//import com.gluonhq.attach.orientation.OrientationService;

import com.gluonhq.charm.glisten.application.AppManager;
import com.gluonhq.charm.glisten.application.ViewStackPolicy;
import com.gluonhq.charm.glisten.control.ProgressIndicator;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.chat.model.Channel;
import com.gluonhq.chat.service.Service;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

import static com.gluonhq.chat.GluonChat.VIEW_CHANGE_WIDTH;
import java.util.Objects;

public class HomePresenter {

    @FXML private View homeView;
    @Inject private Service service;
    
    private final ChangeListener<Number> widthListener = (o, ov, nv) -> changeOrientation(nv.doubleValue());

    public void initialize() {
        System.err.println("HOMEPRESENTER init");
//        OrientationService.create().ifPresent(o -> o.orientationProperty().addListener(obs -> setupView()));

        homeView.showingProperty().addListener((obs, ov, nv) -> {
            System.err.println("HOMEPRESENTER shoing: "+nv);
            if (nv) {
                AppManager.getInstance().getAppBar().setVisible(false);
                setupView();
                homeView.getScene().widthProperty().addListener(widthListener);
            } else {
                homeView.getScene().widthProperty().removeListener(widthListener);
            }
        });
    }

    private void setupView() {
        if (service.loggedUser() == null) {
            System.err.println("HOMEPRESENTER setupView lu = null");
            AppViewManager.LOGIN_VIEW.switchView(ViewStackPolicy.SKIP);
        } else {
            showProgressIndicator();
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

    private void showProgressIndicator() {
        final ObservableList<Channel> channels = service.getChannels();
        System.err.println("HV SHOWPI, channels = "+Objects.hash(channels)+" = " + channels);
        if ((service.loggedUser() == null) && (channels.isEmpty())) {
            System.err.println("HV EMPTY!");
            final VBox vBox = new VBox(10, new ProgressIndicator(), new Label("Retrieving contacts.."));
            vBox.setAlignment(Pos.CENTER);
            homeView.setCenter(new StackPane(vBox));
            channels.addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable o) {
                    System.err.println("HV INVALIDATED");
                    if (homeView.isShowing()) {
                        changeOrientation(homeView.getScene().getWidth());
                    }
                    channels.removeListener(this);
                }
            });
        } else {
            if (homeView.isShowing()) {
                System.err.println("HV showing");
                changeOrientation(homeView.getScene().getWidth());
            } else {
                System.err.println("HV NOT showing");
            }
        }
        service.initializeService();
    }
}
