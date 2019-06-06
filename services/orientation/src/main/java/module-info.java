module com.gluonhq.attach.orientation {

    requires javafx.controls;
    requires com.gluonhq.attach.util;

    exports com.gluonhq.attach.orientation;
    exports com.gluonhq.attach.orientation.impl to com.gluonhq.attach.util;
}