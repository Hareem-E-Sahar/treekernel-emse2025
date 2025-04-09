package de.dirkdittmar.flickr.group.comment.ui.helper;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import de.dirkdittmar.flickr.group.comment.ui.MainFrame;

/**
 * @author Dirk Dittmar
 * 
 */
public class UiHelper {

    /**
	 * 
	 */
    private static final String COULD_NOT_OPEN_BROWSER = "Could not open Browser";

    private static final String DESKTOP_INTERACTION_NOT_SUPPORTED = "Desktop interaction not supported!";

    private static final String OPEN_THE_DEFAULT_BROWSER_NOT_SUPPORTED = "Open the Default-Browser not supported on your platform!";

    private static final Logger log = Logger.getLogger(UiHelper.class);

    private UiHelper() {
    }

    public static void openBrowser(URI uri) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(uri);
                } else {
                    log.error(OPEN_THE_DEFAULT_BROWSER_NOT_SUPPORTED);
                    JOptionPane.showMessageDialog(MainFrame.getInstance(), OPEN_THE_DEFAULT_BROWSER_NOT_SUPPORTED, COULD_NOT_OPEN_BROWSER, JOptionPane.ERROR_MESSAGE);
                }
            } else {
                log.error(DESKTOP_INTERACTION_NOT_SUPPORTED);
                JOptionPane.showMessageDialog(MainFrame.getInstance(), DESKTOP_INTERACTION_NOT_SUPPORTED, COULD_NOT_OPEN_BROWSER, JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            log.error(COULD_NOT_OPEN_BROWSER, e);
            JOptionPane.showMessageDialog(MainFrame.getInstance(), COULD_NOT_OPEN_BROWSER, COULD_NOT_OPEN_BROWSER, JOptionPane.ERROR_MESSAGE);
        }
    }
}
