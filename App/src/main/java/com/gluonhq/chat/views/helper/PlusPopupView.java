package com.gluonhq.chat.views.helper;

import com.airhacks.afterburner.injection.Injector;
import com.gluonhq.attach.position.Position;
import com.gluonhq.attach.position.PositionService;
import com.gluonhq.charm.glisten.layout.layer.PopupView;
import com.gluonhq.chat.model.ChatMessage;
import com.gluonhq.chat.service.ImageUtils;
import com.gluonhq.chat.service.Service;
import com.gluonhq.chat.views.ChatView;
import com.gluonhq.connect.GluonObservableList;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import javafx.animation.PauseTransition;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ResourceBundle;

import static com.gluonhq.chat.service.ImageUtils.*;
import static com.gluonhq.chat.service.ImageUtils.LATLON_SEP;

public class PlusPopupView extends PopupView {

    private Node ownerNode;
    private ResourceBundle resources;
    private Service service;
    private GluonObservableList<ChatMessage> messages;

    public PlusPopupView(Node ownerNode, ResourceBundle resources) {
        super(ownerNode);
        this.ownerNode = ownerNode;
        this.resources = resources;

        getStylesheets().add(PlusPopupView.class.getResource("plus.css").toExternalForm());

        service = Injector.instantiateModelOrService(Service.class);
        messages = service.getMessages();
        initialize();
    }

    private void initialize() {
        Label icon = new Label(resources.getString("popup.image.icon"));
        icon.getStyleClass().add("font-icon");
        Label upload = new Label(resources.getString("popup.image.text"));
        upload.setOnMouseClicked(e -> {
            hide();

            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
            File file = fileChooser.showOpenDialog(ownerNode.getScene().getWindow());
            if (file != null) {
                try {
                    Image image = new Image(new FileInputStream(file));
                    String id = service.addImage(file.getName() + "-" + System.currentTimeMillis(), image);
                    if (id != null) {
                        var message = new ChatMessage(id, service.getName().get());
                        messages.add(message);
                    }
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                }
            }
        });
        HBox imageBox = new HBox(icon, upload);
        imageBox.getStyleClass().add("item-box");

        Label location = new Label(resources.getString("popup.location.icon"));
        location.getStyleClass().add("font-icon");
        Label shareLocation = new Label(resources.getString("popup.location.text"));
        shareLocation.setOnMouseClicked(e -> {
            hide();
            PositionService.create().ifPresent(positionService ->
                    positionService.positionProperty().addListener((obs, ov, nv) ->
                            sendLocation(nv)));
        });
        HBox locationBox = new HBox(location, shareLocation);
        locationBox.getStyleClass().add("item-box");

        VBox content = new VBox(imageBox, locationBox);
        content.getStyleClass().add("content-box");
        setContent(content);
    }

    private void sendLocation(Position position) {

        MapView mapView = new MapView();
        Scene scene = new Scene(mapView, 400, 400);
        scene.getStylesheets().addAll(ChatView.class.getResource("chat.css").toExternalForm(),
                ChatView.class.getResource("/styles.css").toExternalForm());
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
        stage.toBack();

        if (position == null) {
            position = DEFAULT_POSITION;
        }
        String name = service.getName().get();

        MapPoint mapPoint = new MapPoint(position.getLatitude(), position.getLongitude());

        PoiLayer poiLayer = new PoiLayer();
        poiLayer.addPoint(mapPoint, PoiLayer.createUserPointer(Service.getInitials(name)));
        mapView.addLayer(poiLayer);
        mapView.setZoom(15);
        mapView.setCenter(mapPoint);

        messages.removeIf(m -> m.getMessage().startsWith(IMAGE_PREFIX + LATLON + name));
        service.getImages().removeIf(m -> m.getId().startsWith(LATLON + name));

        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(f -> {
            Image snapshot = ImageUtils.getSnapshot(mapView, false);

            String id = service.addImage(LATLON + name + LATLON_SEP + mapPoint.getLatitude() + LATLON_SEP + mapPoint.getLongitude() + LATLON_SEP + System.currentTimeMillis(), snapshot);
            if (id != null) {
                var message = new ChatMessage(id, name);
                messages.add(message);
            }
        });
        pause.play();
    }


}
