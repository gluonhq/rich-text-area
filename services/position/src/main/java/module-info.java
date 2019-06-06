module com.gluonhq.attach.position {

    requires javafx.controls;
    requires com.gluonhq.attach.util;
    requires com.gluonhq.maps;

    exports com.gluonhq.attach.position;
    exports com.gluonhq.attach.position.impl to com.gluonhq.attach.util;
}