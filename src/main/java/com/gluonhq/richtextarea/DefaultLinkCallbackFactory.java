package com.gluonhq.richtextarea;

import javafx.scene.Node;
import javafx.scene.text.Text;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

public class DefaultLinkCallbackFactory {

    public static Function<Node, Consumer<String>> getFactory() {
        return n -> {
            if (n instanceof Text) {
                return launchBrowser();
            }
            return null;
        };
    }

    private static Consumer<String> launchBrowser() {
        return url -> {
            if (url == null || url.isEmpty()) {
                return;
            }
            String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
            try {
                List<String> command = os.contains("mac") ?
                        List.of("open", url) :
                        os.contains("win") ?
                                List.of("rundll32", "url.dll,FileProtocolHandler", url) :
                                List.of("xdg-open", url);
                Runtime.getRuntime().exec(command.toArray(String[]::new));
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }
}
