package org.tunesremote_se;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.libtunesremote_se.LibrarySelector;
import org.libtunesremote_se.PairingDatabase;
import org.libtunesremote_se.PairingDialog;
import org.libtunesremote_se.TunesService;
import net.firefly.client.Version;
import net.firefly.client.controller.ConfigurationManager;
import net.firefly.client.gui.context.Context;
import net.firefly.client.gui.swing.dialog.AboutDialog;
import net.firefly.client.gui.swing.dialog.ConfigLocationDialog;
import net.firefly.client.model.configuration.Configuration;
import net.firefly.client.tools.FireflyClientException;
import java.io.File;
import java.lang.reflect.*;
import java.awt.Image;
import java.awt.Toolkit;
import android.util.Log;
import apple.dts.samplecode.osxadapter.OSXAdapter;

public class TunesRemoteSE {

    public static final String TAG = TunesRemoteSE.class.toString();

    private static String configRootDirectory;

    public static boolean MAC_OS_X = (System.getProperty("os.name").toLowerCase().startsWith("mac os x"));

    protected AboutDialog aboutDialog = null;

    private static Configuration config = null;

    private static Image icon;

    public static void main(String[] args) throws ClassNotFoundException {
        Log.version(TAG, "Launching " + Version.getLongApplicationName() + " ...");
        icon = Toolkit.getDefaultToolkit().getImage(TunesRemoteSE.class.getResource("/net/firefly/client/resources/images/app.png"));
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            Log.e(TAG, "Look and Feel Error : " + ex.getMessage());
        }
        if (MAC_OS_X) {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "TunesRemote SE");
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            try {
                Class<?> applicationClass = Class.forName("com.apple.eawt.Application");
                Object macOSXApplication = applicationClass.getConstructor((Class[]) null).newInstance((Object[]) null);
                Method setDockIconImage = applicationClass.getDeclaredMethod("setDockIconImage", java.awt.Image.class);
                setDockIconImage.invoke(macOSXApplication, icon);
            } catch (ClassNotFoundException cnfe) {
                Log.e(TAG, "This version of Mac OS X does not support the Apple EAWT. (" + cnfe + ")");
            } catch (Exception ex) {
                Log.e(TAG, "Mac OS X Adapter could not talk to EAWT:", ex);
            }
        }
        ConfigurationManager.loadDefaultConfiguration();
        final boolean configAlreadyExists;
        if (ConfigurationManager.isConfigurationDirectoryValid(ConfigurationManager.getConfigRootDirectoryApp())) {
            configRootDirectory = ConfigurationManager.getConfigRootDirectoryApp();
            configAlreadyExists = true;
        } else if (ConfigurationManager.isConfigurationDirectoryValid(ConfigurationManager.getConfigRootDirectoryUser())) {
            configRootDirectory = ConfigurationManager.getConfigRootDirectoryUser();
            configAlreadyExists = true;
        } else {
            configAlreadyExists = false;
            Context temporaryContext = new Context(Configuration.getInstance());
            ConfigLocationDialog cld = new ConfigLocationDialog(temporaryContext, null);
            cld.setVisible(true);
            configRootDirectory = cld.getConfigRootDirectory();
            if (configRootDirectory == null) {
                System.exit(0);
            }
        }
        boolean configValid = ConfigurationManager.isConfigurationDirectoryValid(configRootDirectory);
        if (!configValid) {
            try {
                ConfigurationManager.createConfigurationDirectory(configRootDirectory);
            } catch (FireflyClientException e) {
                Log.e(TAG, "Error creating config directory", e);
            }
        }
        Log.setLogFile(configRootDirectory + File.separator + "Session.log");
        if (configAlreadyExists) {
            try {
                config = ConfigurationManager.loadSavedConfiguration(configRootDirectory);
            } catch (FireflyClientException e) {
                config = Configuration.getInstance();
            }
        } else {
            config = Configuration.getInstance();
        }
        config.setConfigRootDirectory(configRootDirectory);
        for (int i = 0; i < args.length - 1; i += 2) {
            String arg = args[i];
            String value = args[i + 1];
            if (arg.equals("-loglevel")) {
                try {
                    int level = Integer.parseInt(value);
                    Log.i(TAG, "Setting -loglevel to " + value);
                    android.util.Log.setLogLevel(level);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Illegal value for -loglevel : " + value);
                }
            }
        }
        System.setProperty("http.agent", TunesRemoteSE.class.getSimpleName());
        UIManager.put("Tree.expandedIcon", new ImageIcon(TunesRemoteSE.class.getResource("/net/firefly/client/resources/images/tree-down.png")));
        UIManager.put("Tree.collapsedIcon", new ImageIcon(TunesRemoteSE.class.getResource("/net/firefly/client/resources/images/tree-right.png")));
        new TunesRemoteSE();
    }

    private TunesRemoteSE() {
        if (MAC_OS_X) {
            try {
                OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("quit", (Class[]) null));
                OSXAdapter.setAboutHandler(this, getClass().getDeclaredMethod("about", (Class[]) null));
            } catch (Exception e) {
                Log.e(TAG, "Error while loading the OSXAdapter:", e);
            }
        }
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                PairingDatabase.setConfigDirectory(configRootDirectory);
                TunesService.startService(Version.APPLICATION_NAME);
                PairingDialog.setIcon(icon);
                LibrarySelector librarySelector = new LibrarySelector(new NewSessionCallback(), null);
                librarySelector.setIconImage(icon);
                librarySelector.setVisible(true);
            }
        });
    }

    public boolean quit() {
        try {
            if (config != null) {
                ConfigurationManager.saveConfiguration(config);
            }
        } catch (FireflyClientException e) {
            Log.e(TAG, "Error saving configuration", e);
        }
        System.exit(0);
        return true;
    }

    public boolean about() {
        if (aboutDialog == null) {
            aboutDialog = new AboutDialog(config, null);
        }
        if (aboutDialog != null) {
            aboutDialog.setVisible(true);
        }
        return true;
    }

    public boolean preferences() {
        return false;
    }
}
