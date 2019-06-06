package com.gluonhq.chat.views;

import com.gluonhq.attach.position.Position;
import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.chat.service.Service;
import com.gluonhq.chat.views.helper.PoiLayer;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;

import java.util.ResourceBundle;

import static com.gluonhq.chat.service.ImageUtils.DEFAULT_POSITION;

public class MapsView extends View {

    private final MapView mapView;
    private final ResourceBundle resources;
    private PoiLayer poiLayer;

    public MapsView() {
        setShowTransitionFactory(BounceInRightTransition::new);
        resources = ResourceBundle.getBundle("com.gluonhq.chat.views.maps");
        getStylesheets().add(UsersView.class.getResource("users.css").toExternalForm());

        showingProperty().addListener((obs, ov, nv) -> {
            AppBar appBar = MobileApplication.getInstance().getAppBar();
            if (nv) {
                appBar.setNavIcon(MaterialDesignIcon.CHEVRON_LEFT.button(e ->
                        MobileApplication.getInstance().goHome()));
                appBar.setTitleText(resources.getString("maps.view.title"));
            }
        });

        mapView = new MapView();
        mapView.setZoom(10);
        mapView.setCenter(new MapPoint(DEFAULT_POSITION.getLatitude(), DEFAULT_POSITION.getLongitude()));
        poiLayer = new PoiLayer();
        mapView.addLayer(poiLayer);
        setCenter(mapView);
    }

    void flyTo(Position position, String name) {
        MapPoint mapPoint = new MapPoint(position.getLatitude(), position.getLongitude());
        poiLayer.addPoint(mapPoint, PoiLayer.createUserPointer(Service.getInitials(name)));
        mapView.setCenter(mapPoint);
    }
}
