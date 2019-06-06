package com.gluonhq.chat.views.helper;

import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.util.Duration;
import javafx.util.Pair;

public class PoiLayer extends MapLayer {

    private final ObservableList<Pair<MapPoint, Node>> points = FXCollections.observableArrayList();

    public PoiLayer() {
        getStylesheets().add(PoiLayer.class.getResource("poi.css").toExternalForm());
    }

    public void addPoint(MapPoint p, Node icon) {
        // clear nodes with same id
        points.removeIf(pair -> {
            if (pair.getValue().getId() != null &&
                    pair.getValue().getId().equals(icon.getId())) {
                this.getChildren().remove(pair.getValue());
                return true;
            }
            return false;
        });

        points.add(new Pair<>(p, icon));
        icon.setVisible(false);
        this.getChildren().add(icon);
        // required to layout first the node and be able to find its
        // bounds
        PauseTransition pause = new PauseTransition(Duration.millis(100));
        pause.setOnFinished(f -> {
            markDirty();
            icon.setVisible(true);
        });
        pause.play();
    }

    @Override
    protected void layoutLayer() {
        for (Pair<MapPoint, Node> candidate : points) {
            MapPoint point = candidate.getKey();
            Node icon = candidate.getValue();
            Point2D mapPoint = getMapPoint(point.getLatitude(), point.getLongitude());
            Bounds bounds = icon.getBoundsInLocal();

            // take into account possible dropshadow effect
            double radiusEffect = - bounds.getMinX();
            double iconWidth = bounds.getWidth() - 2 * radiusEffect;
            double iconHeight = bounds.getHeight() - 2 * radiusEffect;
            // translate icon so marker base point is at the center
            icon.setLayoutX(mapPoint.getX() - iconWidth / 2);
            icon.setLayoutY(mapPoint.getY() - iconHeight);
        }
    }

    public static Node createUserPointer(String text) {
        Label marker = new Label(text);
        marker.getStyleClass().add("marker");
        Group box = new Group(marker);
        box.setId(text);
        box.getStyleClass().add("marker-container");
        return box;
    }
}
