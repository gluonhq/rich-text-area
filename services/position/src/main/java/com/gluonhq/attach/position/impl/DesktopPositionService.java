package com.gluonhq.attach.position.impl;

import com.gluonhq.attach.position.Parameters;
import com.gluonhq.attach.position.Position;
import com.gluonhq.attach.position.PositionService;
import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Pair;

public class DesktopPositionService implements PositionService {

    private final ReadOnlyObjectWrapper<Position> position = new ReadOnlyObjectWrapper<>();

    private MapView mapView;
    private PoiLayer poiLayer;
    private MapPoint mapPoint;

    public DesktopPositionService() {

        if ("ios".equals(System.getProperty("os.target"))) {
            return;
        }

        TextField lat = new TextField();
        TextField lon = new TextField();
        Button send = new Button("Send Location");
        send.disableProperty().bind(lat.textProperty().isEmpty().or(lon.textProperty().isEmpty()));
        send.setOnAction(e ->
                position.set(new Position(mapPoint.getLatitude(), mapPoint.getLongitude())));

        lat.textProperty().addListener(o -> setPosition(lat, lon));
        lon.textProperty().addListener(o -> setPosition(lat, lon));
        HBox latBox = new HBox(10, new Label("Lat: "), lat);
        latBox.setAlignment(Pos.CENTER_LEFT);
        HBox lonBox = new HBox(10, new Label("Lon: "), lon);
        lonBox.setAlignment(Pos.CENTER_LEFT);

        mapView = new MapView();
        poiLayer = new PoiLayer();
        mapView.addLayer(poiLayer);
        mapView.setZoom(15);

        VBox.setVgrow(mapView, Priority.ALWAYS);

        VBox box = new VBox(10, latBox, lonBox, send, mapView);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        Scene scene = new Scene(box, 400, 600);
        Stage stage = new Stage();
        stage.setTitle("Mock Position Service");
        stage.setScene(scene);
        stage.show();

        stage.setX(Screen.getPrimary().getBounds().getMaxX() - 400);
    }

    private void setPosition(TextField lat, TextField lon) {
        if (lat.getText() == null || lat.getText().isEmpty()) {
            return;
        }
        if (lon.getText() == null || lon.getText().isEmpty()) {
            return;
        }

        try {
            mapPoint = new MapPoint(Double.parseDouble(lat.getText()),
                    Double.parseDouble(lon.getText()));
            poiLayer.setPoint(mapPoint, new Circle(7, Color.RED));
            mapView.setCenter(mapPoint);
        } catch (NumberFormatException nfe) {}

    }

    @Override
    public ReadOnlyObjectProperty<Position> positionProperty() {
        return position.getReadOnlyProperty();
    }

    @Override
    public Position getPosition() {
        return position.get();
    }

    @Override
    public void start() {
        // no-op
    }

    @Override
    public void start(Parameters parameters) {
        // no-op
    }

    @Override
    public void stop() {
        // no-op
    }

    class PoiLayer extends MapLayer {

        private final ObservableList<Pair<MapPoint, Node>> points = FXCollections.observableArrayList();

        public PoiLayer() {
        }

        public void setPoint(MapPoint p, Node icon) {
            points.setAll(new Pair<>(p, icon));
            this.getChildren().add(icon);
            this.markDirty();
        }

        @Override
        protected void layoutLayer() {
            for (Pair<MapPoint, Node> candidate : points) {
                MapPoint point = candidate.getKey();
                Node icon = candidate.getValue();
                Point2D mapPoint = getMapPoint(point.getLatitude(), point.getLongitude());
                icon.setVisible(true);
                icon.setTranslateX(mapPoint.getX());
                icon.setTranslateY(mapPoint.getY());
            }
        }
    }
}
