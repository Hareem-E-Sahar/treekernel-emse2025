package info.opencards.ui.actions;

import info.opencards.Utils;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * DOCUMENT ME!
 *
 * @author Holger Brandl
 */
public class URLAction extends AbstractAction {

    private String url;

    public URLAction(String name, Icon icon, String url) {
        super(name, icon);
        this.url = url;
    }

    public URLAction(String name, String url) {
        super(name);
        this.url = url;
    }

    public void actionPerformed(ActionEvent e) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URL(url).toURI());
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (URISyntaxException e1) {
                e1.printStackTrace();
            }
        } else {
            JOptionPane.showConfirmDialog(null, "Can not determine the default web browser.\n" + url);
        }
    }
}

class BrowserControl {

    /**
     * Display a file in the system browser.  If you want to display a file, you must include the absolute path name.
     *
     * @param url the file's url (the url must start with either "http://" or "file://").
     */
    public static void displayURL(final String url) {
        boolean windows = Utils.isWindowsPlatform();
        String cmd = null;
        try {
            if (windows) {
                cmd = WIN_PATH + " " + WIN_FLAG + " " + url;
                Process p = Runtime.getRuntime().exec(cmd);
            } else {
                new Thread() {

                    public void run() {
                        super.run();
                        try {
                            String cmd = UNIX_PATH + " " + url;
                            Process p = Runtime.getRuntime().exec(cmd);
                            int exitCode = p.waitFor();
                            if (exitCode != 0) {
                                cmd = UNIX_PATH + " " + url;
                                p = Runtime.getRuntime().exec(cmd);
                            }
                        } catch (InterruptedException x) {
                            System.err.println("Error bringing up browser, cmd=''");
                            System.err.println("Caught: " + x);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        } catch (IOException x) {
            System.err.println("Could not invoke browser, command=" + cmd);
            System.err.println("Caught: " + x);
        }
    }

    /**
     * Simple example.
     */
    public static void main(String[] args) {
        displayURL("http://www.javaworld.com");
    }

    private static final String WIN_PATH = "rundll32";

    private static final String WIN_FLAG = "url.dll,FileProtocolHandler";

    private static final String UNIX_PATH = "firefox";

    private static final String UNIX_FLAG = "-remote openURL";
}
