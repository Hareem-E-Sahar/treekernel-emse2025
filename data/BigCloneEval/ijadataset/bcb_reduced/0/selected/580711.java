package org.dmpotter.mapper;

import org.dmpotter.mapper.actions.*;
import org.dmpotter.mapper.plugin.*;
import org.dmpotter.mapper.objects.*;
import org.dmpotter.util.*;
import org.dmpotter.plugin.*;
import java.awt.Color;
import java.awt.Image;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * The main application instance of the Mapper application.
 * @author dmpotter
 * @version $Revision: 1.17 $
 */
public class Mapper implements Application {

    private Image appImage;

    private IconManager iconManager;

    private MenuManager menuManager;

    private StringTable stringTable;

    private TileSetManager tileSetManager;

    private MapObjectManager mapObjectManager;

    private WindowManager windowManager;

    private File userDir, appDir;

    private JarFile appFile;

    private Properties properties;

    private PluginManager systemPlugins, userPlugins;

    /**
     * The current version of the mapper application as a Version object.
     */
    public static final Version version = new Version(0, 0, 4, 1);

    /**
     * The current version string.
     */
    public static final String versionStr = "0.0.4-1";

    public Mapper() {
        init();
        bootstrap();
    }

    /**
     * Creates a new Application, configuring local properties based on the
     * arguments given.
     * @throws RuntimeException if the application cannot start for a variety of reasons
     */
    public Mapper(String args[]) {
        init();
        bootstrap();
    }

    /**
     * Loads the current properties.
     */
    private void init() {
        properties = new Properties();
        userDir = new File(System.getProperty("user.home"), ".rpgmapper");
        if (userDir.exists()) {
            try {
                properties.load(new java.io.FileInputStream(new File(userDir, "mapper.conf")));
            } catch (IOException ioe) {
                logInfo("load", "Unable to load user configuration - using default values.");
                logException("load", ioe);
            }
        } else {
            if (!userDir.mkdir()) logWarning("load", "Unable to create user information directory.");
        }
    }

    /**
     * Loads the application.
     */
    private void bootstrap() {
        String appDirName = System.getProperty("rpgmapper.app.dir", ".");
        appDir = new File(appDirName);
        appFile = null;
        if (appDir.exists()) {
            if (appDir.isDirectory()) {
                try {
                    appFile = new JarFile(new File(appDir, "rpgmapper.jar"));
                } catch (IOException ioe) {
                    throw new RuntimeException("Could not open application file.", ioe);
                }
            } else throw new RuntimeException("Application directory (" + appDirName + ") exists but is not a directory.");
        } else throw new RuntimeException("Application directory (" + appDirName + ") does not exist.");
        if (appFile == null) throw new RuntimeException("No application file available.");
        try {
            Locale locale = null;
            String localeProp = properties.getProperty("locale");
            if (localeProp != null) {
                String lang, country = "", var = "";
                int i = localeProp.indexOf("_");
                int o = 0;
                if (i >= 0) {
                    lang = localeProp.substring(0, i);
                    o = i + 1;
                } else lang = localeProp;
                i = localeProp.indexOf("_", o);
                if (i >= 0) {
                    country = localeProp.substring(o, i);
                    o = i + 1;
                    var = localeProp.substring(o);
                }
                logInfo("localization", "Got language code of " + lang + ":" + country + ":" + var);
                locale = new Locale(lang, country, var);
            }
            stringTable = new StringTable(appFile, locale);
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to load strings.", ioe);
        }
        appImage = Utilities.loadImage(appFile, "splash.png");
        SplashWindow splash = new SplashWindow(getString("app.name"), appImage, versionStr, Color.black, 250, 155, Color.black, 36, 177);
        splash.setVisible(true);
        splash.setActionString(getString("load.gui"));
        iconManager = new IconManager(appFile, "icons");
        menuManager = new MenuManager(this);
        windowManager = new WindowManager(this);
        menuManager.addMenuItem("<file>", new NewAction(this));
        menuManager.addMenuItem("<file>", new OpenAction(this));
        menuManager.addMenuItem("<file>", null);
        menuManager.addMenuItem("<file>", new SaveAction(this));
        menuManager.addMenuItem("<file>", new SaveAsAction(this));
        menuManager.addMenuItem("<file>", null);
        menuManager.addMenuItem("<file>", new PrintAction(this));
        menuManager.addMenuItem("<file>", new PrintPreviewAction(this));
        menuManager.addMenuItem("<file>", null);
        menuManager.addMenuItem("<file>", new ExitAction(this));
        menuManager.addMenuItem("<view>", new PaletteToggleAction(this));
        menuManager.addMenuItem("<view>", new ObjectPaletteToggleAction(this));
        menuManager.addMenuItem("<view>", new FloorAction(this));
        menuManager.addMenuItem("<tools>", new EditModeAction(this, "select", "select_menu.png", MapPanel.MODE_SELECT_OBJECTS));
        menuManager.addMenuItem("<tools>", new EditModeAction(this, "tileBrush", "tileBrush_menu.png", MapPanel.MODE_PAINT_TILES));
        menuManager.addMenuItem("<tools>", new EditModeAction(this, "addObject", "insertObject_menu.png", MapPanel.MODE_ADD_OBJECT));
        menuManager.addMenuItem("<tools>", null);
        menuManager.addMenuItem("<tools>", new DeleteAction(this));
        menuManager.addMenuItem("<plugin>", new PluginAction(this));
        menuManager.addMenuItem("<help>", new HelpAction(this));
        menuManager.addMenuItem("<help>", new AboutAction(this));
        mapObjectManager = new MapObjectManager();
        mapObjectManager.addMapObjectFactory(new CommentObject.CommentObjectFactory(this));
        splash.setActionString(getString("load.plugins"));
        loadPlugins();
        splash.setActionString(getString("load.plugins.system"));
        boolean sysPluginsOK = plugIn(systemPlugins);
        splash.setActionString(getString("load.plugins.user"));
        boolean usrPluginsOK = plugIn(userPlugins);
        splash.setActionString(getString("load.tiles"));
        tileSetManager = new TileSetManager();
        ZippedTileSet set = new ZippedTileSet(appFile, "tiles");
        tileSetManager.addTileSet(set);
        set = new ZippedTileSet(appFile, "hextiles");
        tileSetManager.addTileSet(set);
        if (appDir != null) {
            File tileDir = new File(appDir, "tilesets");
            if (tileDir.exists() && tileDir.isDirectory()) {
                File list[] = tileDir.listFiles();
                for (int i = 0; i < list.length; i++) try {
                    System.out.println(list[i].getName());
                    tileSetManager.addTileSet(new ZippedTileSet(new java.util.zip.ZipFile(list[i])));
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            } else System.out.println("Tile set dir does not exist or is not a directory.");
        }
        tileSetManager.waitForLoad();
        if (!sysPluginsOK || !usrPluginsOK) {
            JOptionPane.showMessageDialog(null, getString("plugin.load.failed"), getString("plugin.load.failed.title"), JOptionPane.ERROR_MESSAGE);
        }
        new MapWindow(this, new Map(tileSetManager.getDefaultTileSet(), 10, 10));
        splash.setVisible(false);
        splash.dispose();
    }

    /**
     * Loads plugins, separate for the day when plugins can be reloaded at
     * runtime...
     */
    private void loadPlugins() {
        systemPlugins = new PluginManager();
        userPlugins = new PluginManager();
        File systemPluginDir = new File(appDir, "plugins");
        if (systemPluginDir.exists()) {
            systemPlugins.loadPlugins(systemPluginDir, this);
            if (systemPlugins.haveFailedPlugins()) logWarning("plugins.system", "Plugins have failed to load.");
        }
        File userPluginDir = new File(userDir, "plugins");
        if (userPluginDir.exists()) {
            userPlugins.loadPlugins(userPluginDir, this);
            if (userPlugins.haveFailedPlugins()) logWarning("plugins.user", "Plugins have failed to load.");
        }
    }

    /**
     * Checks all loaded plugins for mapper interfaces and runs init
     * routines.
     */
    private boolean plugIn(PluginManager manager) {
        Iterator iter = manager.getPlugins();
        boolean success = true;
        while (iter.hasNext()) {
            PluginInstance instance = (PluginInstance) iter.next();
            Plugin plugin = instance.getPlugin();
            logInfo("plugins", "Plugging in " + plugin.getName());
            if (plugin instanceof MapperPlugin) {
                try {
                    ((MapperPlugin) plugin).initMapper(this);
                } catch (Exception e) {
                    logWarning("plugin", "A plugin failed to load.");
                    logException("plugin", e);
                    success = false;
                }
            }
        }
        return success;
    }

    /**
     * Log an informational message.
     * @param context the context of the warning
     * @param message the message of the warning
     */
    public void logInfo(String context, String message) {
        System.err.print("[info] [");
        System.err.print(context);
        System.err.print("] ");
        System.err.println(message);
    }

    /**
     * Log a warning.
     * @param context the context of the warning
     * @param message the message of the warning
     */
    public void logWarning(String context, String message) {
        System.err.print("[warn] [");
        System.err.print(context);
        System.err.print("] ");
        System.err.println(message);
    }

    /**
     * Log an exception.
     * @param context the context of the exception
     * @param exception the exception
     */
    public void logException(String context, Throwable exception) {
        System.err.print("[exception] [");
        System.err.print(context);
        System.err.print("] ");
        exception.printStackTrace(System.err);
    }

    /**
     * Saves the properties to the user dir.
     */
    public void saveProperties() {
        try {
            properties.store(new java.io.FileOutputStream(new File(userDir, "mapper.conf")), "Mapper application settings");
        } catch (IOException ioe) {
            Utilities.showExceptionDialog(this, null, ioe);
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * Saves the properties and does anything needed on shutdown.
     */
    public void shutDown() {
        saveProperties();
    }

    public Image getApplicationImage() {
        return appImage;
    }

    /**
     * Gets the current icon manager.
     */
    public IconManager getIconManager() {
        return iconManager;
    }

    /**
     * Requests the icon under the given key.
     */
    public javax.swing.Icon loadIcon(String iconKey) {
        return iconManager.loadIcon(iconKey);
    }

    /**
     * Requests the image that makes up the icon under the given key.
     */
    public java.awt.Image loadImage(String iconKey) {
        return iconManager.loadImage(iconKey);
    }

    /**
     * Gets the current menu manager.
     */
    public MenuManager getMenuManager() {
        return menuManager;
    }

    /**
     * Returns the current string table being used for localization.
     */
    public StringTable getStringTable() {
        return stringTable;
    }

    /**
     * Looks up a string in the current string table, and returns
     * that value found in the table or the key itself if no value
     * was found in the table.
     */
    public String getString(String key) {
        return stringTable.getString(key);
    }

    /**
     * Looks up a string in the current string table.
     */
    public String lookupString(String key) {
        return stringTable.lookupString(key);
    }

    /**
     * Looks up a string in the current string table, and returns
     * that value found in the table or the key itself if no value
     * was found in the table.
     */
    public String getOriginalString(String key) {
        return stringTable.getOriginalString(key);
    }

    /**
     * Looks up a string in the current string table.
     */
    public String lookupOriginalString(String key) {
        return stringTable.lookupOriginalString(key);
    }

    /**
     * @deprecated replaced with {@link #getLocale()} - since the new
     * interface uses the Java Locale object, this method is being
     * phased out
     */
    public String getLanguageCode() {
        return stringTable.getLanguageCode();
    }

    /**
     * Gets the Locale being used.
     * @return locale
     */
    public Locale getLocale() {
        return stringTable.getLocale();
    }

    /**
     * Changes the string table - the new strings will not be used until the
     * application is restarted, unfortunately.
     */
    public void setLocale(Locale locale) {
        properties.setProperty("locale", locale.toString());
        try {
            stringTable = new StringTable(appFile, locale);
        } catch (IOException ioe) {
            Utilities.showExceptionDialog(this, null, ioe);
        }
    }

    /**
     * Gets the MapObjectManager.
     * @return the current MapObjectManager
     */
    public MapObjectManager getMapObjectManager() {
        return mapObjectManager;
    }

    /**
     * Gets the tile set manager.
     * @return the current tile set manager
     */
    public TileSetManager getTileSetManager() {
        return tileSetManager;
    }

    /**
     * Gets the window manager.
     * @return the current window manager
     */
    public WindowManager getWindowManager() {
        return windowManager;
    }

    /**
     * Attachs the given window to the current window manager.
     * @param w the window to attach
     */
    public void attachWindow(Window w) {
        windowManager.attachWindow(w);
    }

    /**
     * Gets the JAR file that this program was loaded from.
     */
    public JarFile getApplicationFile() {
        return appFile;
    }

    /**
     * Gets the PluginManager that is handling system plugins.
     */
    public PluginManager getSystemPluginManager() {
        return systemPlugins;
    }

    /**
     * Gets the PluginManager that is handling user plugins.
     */
    public PluginManager getUserPluginManager() {
        return userPlugins;
    }

    /**
     * Starts the application.
     * @param args arguments to the application
     */
    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            new Mapper(args);
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) throw (ThreadDeath) t;
            System.err.println("Unable to start application: an exception occured:");
            System.err.print(t.getClass().getName() + ": ");
            if (t.getMessage() != null) System.err.println(t.getMessage()); else System.err.println("no message provided.");
            t.printStackTrace(System.err);
            if (t.getCause() != null) {
                System.err.println("The root cause of this exception is:");
                t.getCause().printStackTrace(System.err);
            }
            System.exit(1);
        }
    }
}
