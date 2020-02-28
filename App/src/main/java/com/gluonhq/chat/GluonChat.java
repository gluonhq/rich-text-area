package com.gluonhq.chat;

import com.gluonhq.attach.lifecycle.LifecycleService;
import com.gluonhq.attach.storage.StorageService;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.visual.Swatch;
import com.gluonhq.chat.views.AppViewManager;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class GluonChat extends MobileApplication {

    private static final Logger LOG = Logger.getLogger(GluonChat.class.getName());
    static {
        try {
            LogManager.getLogManager().readConfiguration(GluonChat.class.getResourceAsStream("/logging.properties"));

            File root = StorageService.create().flatMap(StorageService::getPrivateStorage)
                    .orElseThrow(() -> new IOException("Error: Storage is required"));
            Path securityPath = Path.of(root.getAbsolutePath(), "lib", "security");
            if (!Files.exists(securityPath)) {
                if (!Files.exists(Path.of(root.getAbsolutePath()))) {
                    Files.createDirectories(Path.of(root.getAbsolutePath()));
                }
                if (!Files.exists(Path.of(root.getAbsolutePath(), "lib"))) {
                    Files.createDirectories(Path.of(root.getAbsolutePath(), "lib"));
                }
                if (!Files.exists(Path.of(root.getAbsolutePath(), "lib", "security"))) {
                    Files.createDirectories(Path.of(root.getAbsolutePath(), "lib", "security"));
                }
                copyFileFromResources("/security/blacklisted.certs", securityPath.resolve("blacklisted.certs").toString());
                copyFileFromResources("/security/cacerts.remove", securityPath.resolve("cacerts").toString());
                copyFileFromResources("/security/default.policy", securityPath.resolve("default.policy").toString());
                copyFileFromResources("/security/public_suffix_list.dat", securityPath.resolve("public_suffix_list.dat").toString());
            }
            LOG.log(Level.INFO, "securityPath = " + securityPath);
            System.setProperty("java.home", root.getAbsolutePath());
            System.setProperty("javax.net.ssl.trustStore", securityPath.resolve("cacerts").toString());
            System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Storage Service Error", e);
        }
    }

    @Override
    public void init() {
        AppViewManager.registerViewsAndDrawer(this);
    }

    @Override
    public void postInit(Scene scene) {
        Swatch.RED.assignTo(scene);

        scene.getStylesheets().add(GluonChat.class.getResource("/styles.css").toExternalForm());
        ((Stage) scene.getWindow()).getIcons().add(new Image(GluonChat.class.getResourceAsStream("/icon.png")));

        scene.getWindow().setOnCloseRequest(e ->
                LifecycleService.create().ifPresent(LifecycleService::shutdown));
    }

    public static void main(String[] args) {
//        System.setProperty("charm-desktop-form", "tablet");
        launch(args);
    }

    private static boolean copyFileFromResources(String pathIni, String pathEnd)  {
        try (InputStream myInput = GluonChat.class.getResourceAsStream(pathIni)) {
            if (myInput == null) {
                LOG.log(Level.WARNING, "Error file " + pathIni + " not found");
                return false;
            }
            try (OutputStream myOutput = new FileOutputStream(pathEnd)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = myInput.read(buffer)) > 0) {
                    myOutput.write(buffer, 0, length);
                }
                myOutput.flush();
                LOG.log(Level.INFO, "File copied to " + pathEnd);
                return true;
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Error copying file", ex);
            }
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Error copying file", ex);
        }
        return false;
    }
}