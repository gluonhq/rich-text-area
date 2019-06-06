module com.gluonhq.chat {
    requires javafx.controls;

    requires com.gluonhq.charm.glisten;
    requires com.gluonhq.cloudlink.client;
    requires com.gluonhq.attach.display;
    requires com.gluonhq.attach.orientation;
    requires com.gluonhq.attach.position;
    requires com.gluonhq.attach.cache;

    requires com.gluonhq.maps;

    requires com.gluonhq.chat.ChatListView;
//    requires com.gluonhq.emoji;
//    requires com.gluonhq.emoji.popup;
//    requires com.gluonhq.emoji.control;
    requires javafaker;
    requires java.sql;

    requires com.gluonhq.glisten.afterburner;
    requires java.annotation;
    requires afterburner.mfx;

    opens com.gluonhq.chat.service to afterburner.mfx;

    opens com.gluonhq.chat.model to com.gluonhq.cloudlink.client;

    exports com.gluonhq.chat;
}