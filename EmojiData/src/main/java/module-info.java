module com.gluonhq.emoji {
    requires javafx.controls;
    requires java.json;
    requires com.gluonhq.connect;
    opens com.gluonhq.emoji to com.gluonhq.connect;

    exports com.gluonhq.emoji;
    exports com.gluonhq.emoji.util;
}