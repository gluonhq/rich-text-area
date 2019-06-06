module com.gluonhq.emoji {
    requires java.json;
    requires com.gluonhq.connect;
    opens com.gluonhq.emoji to com.gluonhq.connect;

    exports com.gluonhq.emoji;
}