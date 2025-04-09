package net.sf.cryptoluggage.util;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import net.sf.cryptoluggage.gui.KeySizeChooser;

/**
 * This class provides a user friendly API for commonly used methods.
 *
 * @author Miguel Hernández <mhernandez314@gmail.com>
 */
public class MiscUtil {

    private static final String stringBundle = "strings";

    /**
     * Check wether two char[] arrays are the same
     * @return true if they're the same, false otherwise
     */
    public static boolean areEqual(char[] array1, char[] array2) {
        if (array1.length != array2.length) {
            return false;
        }
        for (int i = 0; i < array2.length; i++) {
            if (array1[i] != array2[i]) {
                return false;
            }
        }
        return true;
    }

    public static String bytesToHumanReadable(long originalFileSize) {
        String[] units = { "bytes", "Kb", "MB", "GB", "TB" };
        int unitIndex = 0;
        while (originalFileSize >= 1024 && unitIndex < units.length) {
            originalFileSize /= 1024;
            unitIndex++;
        }
        return originalFileSize + " " + units[unitIndex];
    }

    /**
     * Returns the internationalized string for the given key.
     * Code and idea has been taken from AC antiplagiarism project
     * (visit http://tangow.ii.uam.es/trac/ac for more info)
     * 
     * @param key the key for the internationalized string
     * @return the internationalized string for the given key
     */
    public static String getI18nString(String key) {
        return ResourceBundle.getBundle(stringBundle).getString(key);
    }

    /**
     * Get version string
     * @return this version string
     */
    public static String getVersionString() {
        BufferedReader reader = null;
        try {
            String versionPath = "/version.txt";
            InputStream in = versionPath.getClass().getResourceAsStream(versionPath);
            reader = new BufferedReader(new InputStreamReader(in));
            return reader.readLine();
        } catch (IOException ex) {
            Logger.getLogger(MiscUtil.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                Logger.getLogger(MiscUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    /**
     * Get a default Hyperlink Listener that opens link in browser
     * @return a default Hyperlink Listener that opens link in browser
     */
    public static HyperlinkListener getDefaultLinkListener() {
        return new HyperlinkListener() {

            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (Desktop.isDesktopSupported() && e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        Desktop desktop = Desktop.getDesktop();
                        desktop.browse(e.getURL().toURI());
                    } catch (IOException ex) {
                        Logger.getLogger(KeySizeChooser.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(KeySizeChooser.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
    }
}
