package com.gluonhq.chat.service;

import javafx.scene.image.Image;

public interface BootstrapClient {

    /**
     * Callback method, invoked by a Service, when it has an image ready to be
     * scanned.
     * @param img the image provided by the Service. Typically, this image contains
     * a QR code that needs to be scanned.
     */
    void gotImage(Image img);
    
    /**
     * Callback method, invoked by a Service, when bootstrap has been completed
     * successful.
     */
    void bootstrapSucceeded();
    
}
