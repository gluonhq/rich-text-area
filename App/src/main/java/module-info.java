module com.gluonhq.chat {
    requires javafx.controls;
    requires javafx.fxml;
    // TODO: Remove
    /*requires javafx.swing;
    requires javafx.web;*/

    requires com.gluonhq.charm.glisten;
    requires com.gluonhq.cloudlink.client;
    requires com.gluonhq.attach.cache;
    requires com.gluonhq.attach.device;
    requires com.gluonhq.attach.display;
    requires com.gluonhq.attach.keyboard;
    requires com.gluonhq.attach.lifecycle;
    requires com.gluonhq.attach.orientation;
    requires com.gluonhq.attach.pictures;
    requires com.gluonhq.attach.position;
    requires com.gluonhq.attach.storage;
    requires com.gluonhq.attach.util;

    requires com.gluonhq.maps;

    requires com.gluonhq.chat.ChatListView;
    requires com.gluonhq.emoji;
    requires com.gluonhq.emoji.popup;
    requires com.gluonhq.emoji.control;
    requires java.sql;
    // TODO: Remove
    // requires org.scenicview.scenicview;

    requires com.gluonhq.glisten.afterburner;
    requires java.annotation;
    requires afterburner.mfx;

    opens com.gluonhq.chat.service to afterburner.mfx;
    opens com.gluonhq.chat.views to com.gluonhq.glisten.afterburner,
            afterburner.mfx, javafx.fxml;

    opens com.gluonhq.chat.model to com.gluonhq.cloudlink.client;

    exports com.gluonhq.chat;
}