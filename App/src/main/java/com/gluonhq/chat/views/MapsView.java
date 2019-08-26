package com.gluonhq.chat.views;

import com.airhacks.afterburner.injection.Injector;
import com.gluonhq.attach.position.Position;
import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.FloatingActionButton;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.chat.model.ChatMessage;
import com.gluonhq.chat.service.ImageUtils;
import com.gluonhq.chat.service.Service;
import com.gluonhq.chat.views.helper.PoiLayer;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import javafx.scene.image.Image;

import java.util.ResourceBundle;
import java.util.function.Consumer;

import static com.gluonhq.chat.service.ImageUtils.DEFAULT_POSITION;
import static com.gluonhq.chat.service.ImageUtils.LATLON;
import static com.gluonhq.chat.service.ImageUtils.LATLON_SEP;

public class MapsView extends View {

    private final MapView mapView;
    private final ResourceBundle resources;
    private PoiLayer poiLayer;
    private final FloatingActionButton floatingActionButton;

    public MapsView() {
        floatingActionButton = new FloatingActionButton(MaterialDesignIcon.SEND.text, e -> {});

        setShowTransitionFactory(BounceInRightTransition::new);
        resources = ResourceBundle.getBundle("com.gluonhq.chat.views.maps");
        getStylesheets().add(UsersView.class.getResource("users.css").toExternalForm());

        showingProperty().addListener((obs, ov, nv) -> {
            AppBar appBar = MobileApplication.getInstance().getAppBar();
            if (nv) {
                appBar.setNavIcon(MaterialDesignIcon.CHEVRON_LEFT.button(e -> {
                        floatingActionButton.hide();
                        MobileApplication.getInstance().goHome();
                    }));
                appBar.setTitleText(resources.getString("maps.view.title"));
            }
        });

        mapView = new MapView();
        mapView.setZoom(12);
        mapView.setCenter(new MapPoint(DEFAULT_POSITION.getLatitude(), DEFAULT_POSITION.getLongitude()));
        poiLayer = new PoiLayer();
        mapView.addLayer(poiLayer);
        setCenter(mapView);
    }

    public void flyTo(Position position, String name, String initials, Consumer<ChatMessage> consumer) {
        MapPoint mapPoint = new MapPoint(position.getLatitude(), position.getLongitude());
        poiLayer.addPoint(mapPoint, PoiLayer.createUserPointer(initials));
        mapView.setCenter(mapPoint);
        if (consumer != null) {
            floatingActionButton.setOnAction(e -> {
                floatingActionButton.hide();
                Image snapshot = ImageUtils.getSnapshot(mapView, false);
                Service service = Injector.instantiateModelOrService(Service.class);
                String id = service.addImage(LATLON + initials + LATLON_SEP + mapPoint.getLatitude() + LATLON_SEP + mapPoint.getLongitude() + LATLON_SEP + System.currentTimeMillis(), snapshot);
                if (id != null) {
                    var message = new ChatMessage(id, name);
                    consumer.accept(message);
                }
                MobileApplication.getInstance().switchToPreviousView();
            });
            floatingActionButton.show();
        }
    }
}
