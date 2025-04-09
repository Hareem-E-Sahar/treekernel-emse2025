package net.hawk.digiextractor.GUI;

import java.awt.Component;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import net.hawk.digiextractor.util.Helper;

/**
 * The Class WebUpdater.
 * This class implements a simple check if a newer Version of the Software is
 * available. This works by opening the file 'version' in the root directory
 * of the webserver. This File contains a string that identifies the current
 * version.
 */
public class WebUpdater extends Thread {

    /** The Constant logger. */
    private static final Logger LOGGER = Logger.getLogger(WebUpdater.class.getName());

    /** The parent frame. */
    private Component parentFrame;

    /** Flag indicating if operation should be silent. */
    private boolean silentMode;

    /**
	 * Instantiates a new web updater.
	 * 
	 * @param parent the parent
	 * @param silent decides if a message box should be displayed when the
	 * version is current. If true no message box will be displayed if no newer
	 * version is available
	 */
    public WebUpdater(final JFrame parent, final boolean silent) {
        parentFrame = parent;
        silentMode = silent;
    }

    /**
	 * check if Server has newer version.
	 * 
	 * @param serverString the version string reported by the server.
	 * 
	 * @return true, if a newer version is available
	 */
    private boolean serverHasNewerVersion(final String serverString) {
        try {
            StringTokenizer st = new StringTokenizer(serverString, ".");
            int majorServer = Integer.parseInt(st.nextToken());
            int minorServer = Integer.parseInt(st.nextToken());
            int patchServer = Integer.parseInt(st.nextToken());
            st = new StringTokenizer(Helper.getVersionString(), ".-");
            int majorLocal = Integer.parseInt(st.nextToken());
            int minorLocal = Integer.parseInt(st.nextToken());
            int patchLocal = Integer.parseInt(st.nextToken());
            return ((majorServer > majorLocal) || (minorServer > minorLocal) || (patchServer > patchLocal));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error comparing Version strings", e);
            return false;
        }
    }

    /**
	 * Check for update.
	 */
    public final void run() {
        String serverVersion = "";
        try {
            URL version = new URL("http://www.digiextractor.de/version");
            BufferedReader in = new BufferedReader(new InputStreamReader(version.openStream()));
            serverVersion = in.readLine();
            in.close();
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, "Error getting Version from Server", ioe);
            return;
        }
        if (!serverVersion.matches("\\d+\\.\\d+\\.\\d+.*")) {
            if (!silentMode) {
                JOptionPane.showMessageDialog(parentFrame, Messages.getString("WebUpdater.1"), Messages.getString("WebUpdater.2"), JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
        if (!serverHasNewerVersion(serverVersion)) {
            if (!silentMode) {
                JOptionPane.showMessageDialog(parentFrame, Messages.getString("WebUpdater.4"));
            }
        } else {
            Object[] options = { Messages.getString("WebUpdater.5"), Messages.getString("WebUpdater.6") };
            int n = JOptionPane.showOptionDialog(parentFrame, String.format(Messages.getString("WebUpdater.7"), "", serverVersion), Messages.getString("WebUpdater.9"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if (n == JOptionPane.YES_OPTION) {
                try {
                    URI site = new URI("http://www.digiextractor.de");
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(site);
                    } else {
                        JOptionPane.showMessageDialog(parentFrame, Messages.getString("WebUpdater.11"), Messages.getString("WebUpdater.12"), JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error opening Browser", e);
                }
            }
        }
    }
}
