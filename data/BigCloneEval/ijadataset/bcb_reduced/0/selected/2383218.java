package it.jwallpaper.platform;

import it.jwallpaper.JWallpaperChanger;
import it.jwallpaper.util.MessageUtils;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractPlatform {

    protected Log logger = LogFactory.getLog(this.getClass());

    public String[] getImageExtensions() {
        return new String[] { ".jpg", ".bmp" };
    }

    public boolean openUrl(URL url) throws IOException, URISyntaxException {
        if (!java.awt.Desktop.isDesktopSupported()) {
            return false;
        }
        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
        if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
            return false;
        }
        desktop.browse(url.toURI());
        return true;
    }

    public void showBalloonMessage(Class resourceBundleClass, String titleKey, String messageKey, TrayIconMessageType messageType) {
        String msgTitle = MessageUtils.getMessage(resourceBundleClass, titleKey);
        String msgText = MessageUtils.getMessage(resourceBundleClass, messageKey);
        showBalloonMessage(msgTitle, msgText, messageType);
    }

    public abstract void showBalloonMessage(String title, String message, TrayIconMessageType messageType);

    protected String getDefaultTrayCaption() {
        return MessageUtils.getMessage(JWallpaperChanger.class, "tray.hoverMessage");
    }
}
