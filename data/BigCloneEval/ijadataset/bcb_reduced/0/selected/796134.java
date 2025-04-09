package com.myJava.system.viewer;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import com.myJava.system.NoBrowserFoundException;
import com.myJava.util.log.Logger;

public class DesktopViewerHandler implements ViewerHandler {

    public boolean test() {
        return Desktop.isDesktopSupported() && isBrowseSupported();
    }

    public void browse(URL url) throws IOException, NoBrowserFoundException {
        try {
            URI uri = new URI(url.toExternalForm());
            Desktop.getDesktop().browse(uri);
        } catch (URISyntaxException e) {
            Logger.defaultLogger().error(e);
            throw new IOException(e);
        }
    }

    public boolean isBrowseSupported() {
        return Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
    }

    public boolean isOpenSupported() {
        return Desktop.getDesktop().isSupported(Desktop.Action.OPEN);
    }

    public void open(File file) throws IOException {
        Desktop.getDesktop().open(file);
    }
}
