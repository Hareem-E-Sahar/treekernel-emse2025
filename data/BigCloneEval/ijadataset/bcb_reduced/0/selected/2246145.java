package net.afternoonsun.imaso.core;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import net.afternoonsun.imaso.ImasoMain;
import net.afternoonsun.imaso.core.database.SettingsManager;
import net.afternoonsun.imaso.core.sorter.Scheme;
import net.afternoonsun.imaso.core.sorter.SchemeXML;
import net.afternoonsun.imaso.exceptions.ImasoUncheckedException;
import net.afternoonsun.imaso.exceptions.InvalidSortingSchemeException;
import net.afternoonsun.imaso.exceptions.NoValidSchemeException;

/**
 * Acts as an important container of global settings and also as a bridge for
 * other classes in order to provide a common interface.
 *
 * @author Sergey Pisarenko aka drseergio (drseergio AT gmail DOT com)
 */
public class Environment {

    /** The folder name for images with missing meta info. */
    public static final String UNTAGGED_FOLDERNAME = "untagged";

    /** Environment variable name for enabling debug mode.  */
    public static final String ENV_DEBUG = "IMASO_DEBUG";

    /** The application version number. */
    public static final String VERSION = "0.0.6";

    /** The application copyright statement. */
    public static final String COPYRIGHT = "Copyright (C) 2009 Sergey Pisarenko.";

    /** Shows whether or not debug mode is turned on. */
    public static final boolean DEBUG = true;

    /** Stores supported image formats. */
    public static final Set<String> FORMATS;

    /** Used for retrieving settings from the DB. */
    private SettingsManager manager;

    /** Stores the sorting scheme. */
    private Scheme scheme;

    /**
     * Default constructor. Initialize the environment by accessing the settings
     * manager, retrieving values and setting default ones if none are set.
     */
    public Environment() {
        this.manager = SettingsManager.getInstance();
        loadSortingScheme();
        loadDefaultSettings();
    }

    static {
        FORMATS = new HashSet<String>();
        FORMATS.add("image/jpeg");
    }

    /**
     * Checks whether a separate folder for images with missing metainformation
     * should be used.
     *
     * @return true if separate folder, false if parent folder should be used
     */
    public boolean useUntaggedFolder() {
        return manager.getBooleanValue(SettingsManager.USE_UNTAGGED_FOLDER);
    }

    /**
     * Checks if robust mode is enabled.
     *
     * @return true if enabled, false otherwise
     */
    public boolean robustMode() {
        return manager.getBooleanValue(SettingsManager.ROBUST_MODE);
    }

    /**
     * Checks if MIME should be detected by contents or by extension.
     *
     * @return true if by contents, false if by extension.
     */
    public boolean useMime() {
        return manager.getBooleanValue(SettingsManager.USE_MIME);
    }

    /**
     * Returns the preferred name for the folder which is used for images with
     * missing meta information.
     *
     * @return the name of the untagged folder
     */
    public String getUntaggedFoldername() {
        return manager.getValue(SettingsManager.UNTAGGED_FOLDER);
    }

    /**
     * Returns the default destination path.
     *
     * @return the default final destination path
     */
    public File getDestination() {
        return new File(manager.getValue(SettingsManager.DESTINATION_PATH));
    }

    /**
     * Checks if image management is used.
     *
     * @return true if it is, false otherwise
     */
    public boolean isManagement() {
        return manager.getBooleanValue(SettingsManager.DEVICE_MANAGEMENT);
    }

    /**
     * Checks if effects are enabled.
     *
     * @return true if enabled, false otherwise
     */
    public boolean isEffectsEnabled() {
        return manager.getBooleanValue(SettingsManager.EFFECTS_ENABLED);
    }

    /**
     * Checks if open destination after sort is enabled.
     *
     * @return true if enabled, false otherwise
     */
    public boolean isOpenDestination() {
        return manager.getBooleanValue(SettingsManager.OPEN_DESTINATION);
    }

    /**
     * Returns the sorting scheme used for calculating the final path for each
     * file.
     *
     * @return sorting scheme
     * @throws NoValidSchemeException if no supported scheme is available
     */
    public Scheme getScheme() throws NoValidSchemeException {
        if ((scheme == null) || (!scheme.getVersion().equals(VERSION))) {
            throw new NoValidSchemeException();
        } else {
            return scheme;
        }
    }

    /**
     * Returns the user's home folder path.
     *
     * @return user's home folder path
     */
    public static String getHome() {
        return System.getProperty("user.home", ".") + File.separator + ".imaso";
    }

    /**
     * Returns the path where log files are to be stored.
     *
     * @return log path
     */
    public static String getLogPath() {
        return getHome() + File.separator + "logs";
    }

    /**
     * Opens the specified location using the desktop default associated file
     * manager.
     *
     * @param path file object to be opened
     * @throws IOException if something goes wrong
     */
    public static void openPath(File path) throws IOException {
        if (Desktop.isDesktopSupported()) {
            final Desktop desktop = Desktop.getDesktop();
            desktop.open(path);
        }
    }

    /**
     * Checks whether it is possible to open a path using desktop class.
     *
     * @return true if supported, false if not
     */
    public static boolean isOpenPathSupported() {
        return Desktop.isDesktopSupported();
    }

    /**
     * Sets default settings if none are set beforehand.
     */
    protected void loadDefaultSettings() {
        if (manager.getValue(SettingsManager.ROBUST_MODE) == null) manager.setValue(SettingsManager.ROBUST_MODE, true);
        if (manager.getValue(SettingsManager.USE_UNTAGGED_FOLDER) == null) manager.setValue(SettingsManager.USE_UNTAGGED_FOLDER, false);
        if (manager.getValue(SettingsManager.UNTAGGED_FOLDER) == null) manager.setValue(SettingsManager.UNTAGGED_FOLDER, UNTAGGED_FOLDERNAME);
        if (manager.getValue(SettingsManager.DESTINATION_PATH) == null) manager.setValue(SettingsManager.DESTINATION_PATH, System.getProperty("user.dir"));
        if (manager.getValue(SettingsManager.DEVICE_MANAGEMENT) == null) manager.setValue(SettingsManager.DEVICE_MANAGEMENT, true);
        if (manager.getValue(SettingsManager.USE_MIME) == null) manager.setValue(SettingsManager.USE_MIME, false);
        if (manager.getValue(SettingsManager.WINDOW_WIDTH) == null) manager.setValue(SettingsManager.WINDOW_WIDTH, 600);
        if (manager.getValue(SettingsManager.WINDOW_HEIGHT) == null) manager.setValue(SettingsManager.WINDOW_HEIGHT, 400);
        if (manager.getValue(SettingsManager.LEFT_PANEL_SHOWN) == null) manager.setValue(SettingsManager.LEFT_PANEL_SHOWN, false);
        if (manager.getValue(SettingsManager.NOTIFICATION_TIMEOUT) == null) manager.setValue(SettingsManager.NOTIFICATION_TIMEOUT, 3000);
        if (manager.getValue(SettingsManager.ORDER_ASCENDING) == null) manager.setValue(SettingsManager.ORDER_ASCENDING, true);
        if (manager.getValue(SettingsManager.SORT_CRITERIA) == null) manager.setValue(SettingsManager.SORT_CRITERIA, "NAME");
        if (manager.getValue(SettingsManager.EFFECTS_ENABLED) == null) manager.setValue(SettingsManager.EFFECTS_ENABLED, true);
        if (manager.getValue(SettingsManager.OPEN_DESTINATION) == null) manager.setValue(SettingsManager.OPEN_DESTINATION, false);
    }

    /**
     * Loads the sorting scheme.
     */
    protected void loadSortingScheme() {
        if (manager.getValue("scheme_path") != null) {
            try {
                final InputStream is = new FileInputStream(manager.getValue("scheme_path"));
                final SchemeXML loader = new SchemeXML(is);
                scheme = loader.loadXML();
            } catch (IOException e) {
                manager.deleteValue("scheme_path");
                initDefaultScheme();
            } catch (InvalidSortingSchemeException e) {
                manager.deleteValue("scheme_path");
                initDefaultScheme();
            }
        } else {
            initDefaultScheme();
        }
    }

    /**
     * Loads the default sorting scheme.
     */
    protected void initDefaultScheme() {
        SchemeXML loader = new SchemeXML(ImasoMain.class.getResourceAsStream("imaso-scheme.xml"));
        try {
            scheme = loader.loadXML();
        } catch (InvalidSortingSchemeException e) {
            throw new ImasoUncheckedException(e.getMessage());
        }
    }
}
