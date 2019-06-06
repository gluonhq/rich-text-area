module com.gluonhq.emoji.control {
    requires javafx.controls;
    requires com.gluonhq.emoji;
    requires com.gluonhq.emoji.popup;
//    requires com.gluonhq.attach.keyboard;
    requires org.controlsfx.controls;
    requires reactfx;
    requires richtextfx;
    requires flowless;
    requires java.json;

    exports com.gluonhq.emoji.control;
    exports com.gluonhq.emoji.event;
    exports com.gluonhq.emoji.test;
}