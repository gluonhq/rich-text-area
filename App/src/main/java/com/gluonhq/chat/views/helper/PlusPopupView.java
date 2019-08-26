package com.gluonhq.chat.views.helper;

import com.airhacks.afterburner.injection.Injector;
import com.gluonhq.attach.pictures.PicturesService;
import com.gluonhq.attach.position.Position;
import com.gluonhq.attach.position.PositionService;
import com.gluonhq.attach.util.Platform;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.layout.layer.PopupView;
import com.gluonhq.chat.model.ChatMessage;
import com.gluonhq.chat.service.Service;
import com.gluonhq.chat.views.MapsView;
import com.gluonhq.connect.GluonObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ResourceBundle;

import static com.gluonhq.chat.GluonChat.MAPS_VIEW;
import static com.gluonhq.chat.service.ImageUtils.DEFAULT_POSITION;
import static com.gluonhq.chat.service.ImageUtils.IMAGE_PREFIX;
import static com.gluonhq.chat.service.ImageUtils.LATLON;

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

            if (Platform.isDesktop()) {
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
            } else {
                PicturesService.create().ifPresent(pictures ->
                        pictures.takePhoto(false)
                                .ifPresent(image -> {
                                    String id = service.addImage("photo" + "-" + System.currentTimeMillis(), image);
                                    if (id != null) {
                                        var message = new ChatMessage(id, service.getName().get());
                                        messages.add(message);
                                    }
                                }));
            }
        });
        HBox imageBox = new HBox(icon, upload);
        imageBox.getStyleClass().add("item-box");

        Label location = new Label(resources.getString("popup.location.icon"));
        location.getStyleClass().add("font-icon");
        Label shareLocation = new Label(resources.getString("popup.location.text"));
        shareLocation.setOnMouseClicked(e -> {
            hide();
            PositionService.create().ifPresentOrElse(positionService -> {
                positionService.start();
                Position position = positionService.getPosition();
                if (position != null) {
                    sendLocation(position);
                    positionService.stop();
                } else {
                    positionService.positionProperty().addListener((obs, ov, nv) -> {
                        if (nv != null) {
                            sendLocation(nv);
                            positionService.stop();
                        }
                    });
                }
            }, () -> sendLocation(DEFAULT_POSITION));
        });
        HBox locationBox = new HBox(location, shareLocation);
        locationBox.getStyleClass().add("item-box");

        VBox content = new VBox(imageBox, locationBox);
        content.getStyleClass().add("content-box");
        setContent(content);
    }

    private void sendLocation(Position newPosition) {
        Position position = newPosition == null ? DEFAULT_POSITION : newPosition;

        String name = service.getName().get();
        String initials = Service.getInitials(name);

        MobileApplication.getInstance().switchView(MAPS_VIEW).ifPresent(p ->
                ((MapsView) p).flyTo(position, name, initials, e -> {
                    messages.removeIf(m -> m.getMessage().startsWith(IMAGE_PREFIX + LATLON + initials));
                    service.getImages().removeIf(m -> m.getId().startsWith(LATLON + initials));
                    messages.add(e);
            }));
    }

}
