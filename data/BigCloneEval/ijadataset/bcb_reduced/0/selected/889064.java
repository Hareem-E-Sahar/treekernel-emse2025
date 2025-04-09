package com.googlecode.maratische.google.utils;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Специальный класс для работы с Desktop
 * @author maratische@gmail.com
 *
 */
public final class DesktopUtil {

    private DesktopUtil() {
    }

    private static Desktop desktop = null;

    public static void openUrlInBrowser(String url) throws IOException, URISyntaxException {
        openUrlInBrowser(new URI(url));
    }

    public static void openUrlInBrowser(URI uri) throws IOException {
        if (Desktop.isDesktopSupported()) {
            if (desktop == null) {
                desktop = Desktop.getDesktop();
            }
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(uri);
            }
        }
    }
}
