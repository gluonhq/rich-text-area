package com.gluonhq.attach.orientation.impl;

import com.gluonhq.attach.orientation.OrientationService;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Optional;

public class DesktopOrientationService implements OrientationService {

    private final ReadOnlyObjectWrapper<Orientation> orientation = new ReadOnlyObjectWrapper<>(Orientation.HORIZONTAL);

    public DesktopOrientationService() {

        if ("ios".equals(System.getProperty("os.target"))) {
            return;
        }

        ToggleButton button = new ToggleButton("Orientation");

        button.setSelected(orientation.getValue() == Orientation.HORIZONTAL);
        button.setOnAction(e -> {
            orientation.setValue(button.isSelected() ?
                    Orientation.HORIZONTAL : Orientation.VERTICAL);
            button.setText(button.isSelected() ? "Landscape" : "Portrait");
        });
        button.setText(button.isSelected() ? "Landscape" : "Portrait");

        VBox box = new VBox(button);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        Scene scene = new Scene(box, 200, 300);
        Stage stage = new Stage();
        stage.setTitle("Mock Orientation Service");
        stage.setScene(scene);
        stage.show();

        stage.setX(Screen.getPrimary().getBounds().getMaxX() - 200);
    }

    @Override
    public ReadOnlyObjectProperty<Orientation> orientationProperty() {
        return orientation.getReadOnlyProperty();
    }

    @Override
    public Optional<Orientation> getOrientation() {
        return Optional.of(orientation.getValue());
    }
}
