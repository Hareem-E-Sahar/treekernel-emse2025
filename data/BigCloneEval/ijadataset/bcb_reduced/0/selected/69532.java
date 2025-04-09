package de.gstpl.data;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.StreamException;
import de.gstpl.Startup;
import de.gstpl.resource.language.L;
import de.gstpl.util.ConnectionHelper;
import de.gstpl.util.GDirHelper;
import de.gstpl.util.server.ServerProperties;
import de.peathal.util.FireTo;
import de.gstpl.util.GLog;
import de.peathal.util.MyFont;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * This class helps us to manage all properties we need for the application.
 * @see de.gstpl.data.DBProperties
 * @author peter karich
 */
public class AppProperties extends FireTo {

    private final Map thisProperty;

    private static AppProperties prop;

    private boolean changed;

    /**
     * Use this method to get the singleton property object.
     */
    public static synchronized AppProperties get() {
        if (prop == null) {
            XStream xs = new XStream();
            File xmlFile = _getAppPropFile();
            try {
                InputStream is = new FileInputStream(xmlFile);
                if (xmlFile.getName().endsWith(".gz")) is = new GZIPInputStream(is); else if (xmlFile.getName().endsWith(".zip")) {
                    is = new ZipFile(xmlFile).getInputStream(new ZipEntry(zipEntryName));
                }
                GLog.log(L.tr("Starting_reading_xml_file:_") + xmlFile.getAbsolutePath());
                prop = (AppProperties) xs.fromXML(is);
                if (!AppProperties.getDefaultVersion().equals(prop.get("version"))) prop = null;
                is.close();
                GLog.log(L.tr("Successfully_readed:_") + xmlFile.getAbsolutePath());
            } catch (FileNotFoundException e) {
                GLog.warn(couldnotLoad + L.tr("File_not_found:_") + xmlFile.getAbsolutePath(), e);
            } catch (IOException e) {
                GLog.warn(couldnotLoad + L.tr("May_be_the_gzipped_file_is_currupt_or_it_is_the_wrong_file_format."), e);
            } catch (AccessControlException e) {
                GLog.warn(couldnotLoad + L.tr("Please_specify_this_in_your_security_policy_file!"), e);
            } catch (Exception e) {
                GLog.warn(couldnotLoad + L.tr("May_be_it_isn't_the_correct_file_version!"), e);
            }
            if (prop == null) {
                GLog.log(L.tr("Now_fill_application_properties_with_default_values!"));
                prop = new AppProperties();
            }
        }
        return prop;
    }

    AppProperties() {
        thisProperty = new HashMap();
        setDefault();
    }

    private synchronized Object get(String key) {
        if ("version".equals(key)) return getDefaultVersion();
        return thisProperty.get(key);
    }

    private Object set(String key, Object value) {
        if (key == null || value == null) return null;
        if ("fontsize".equals(key)) {
            int size = ((Integer) value).intValue();
            if (size > 0) MyFont.setFontSize(size); else {
                GLog.warn(L.tr("Fontsize_should_be_none_negative!"));
                return null;
            }
        }
        Object ret;
        synchronized (this) {
            ret = thisProperty.put(key, value);
        }
        if (!value.equals(ret)) changed = true;
        fireChangeEvent(new PropertyChangeEvent(this, key, value, ret));
        return ret;
    }

    /**
     * This method is called after an object construction from an objectstream.
     * Here: while reading the properties from a file.
     */
    Object readResolve() throws ObjectStreamException {
        this.setDefault();
        return this;
    }

    public boolean hasChanges() {
        return changed;
    }

    private static final String defaultVersion = "0.0.6";

    /**
     * This method returns the program version.
     */
    public static String getDefaultVersion() {
        return defaultVersion;
    }

    private static final String couldnotLoad = L.tr("Couldn't_load_application_properties_from_file_-_");

    /**
     * This method saves the current application.properties to a File.
     */
    public void save() {
        AppProperties prop = AppProperties.get();
        synchronized (this) {
            saveTo(getAppPropFile(), prop);
        }
    }

    private static final String zipEntryName = "xml";

    private void saveTo(File xmlFile, Object obj) {
        XStream xs = new XStream();
        OutputStream os;
        try {
            os = new FileOutputStream(xmlFile);
            if (xmlFile.getName().endsWith(".zip")) {
                ZipOutputStream zip = new ZipOutputStream(os);
                zip.putNextEntry(new ZipEntry(zipEntryName));
                os = zip;
            } else if (xmlFile.getName().endsWith(".gz")) {
                GZIPOutputStream zip = new GZIPOutputStream(os);
                os = zip;
            }
            xs.toXML(obj, os);
            os.close();
        } catch (StreamException e) {
            GLog.warn(L.tr("XML_Write_Exception:"), e);
        } catch (MalformedURLException e) {
            GLog.warn(L.tr("Malformed_URL:"), e);
        } catch (FileNotFoundException ex) {
            GLog.warn(L.tr("Can't_find_file!"), ex);
        } catch (IOException e) {
            GLog.warn(L.tr("Can't_gzip_the_file:_IOException:"), e);
        }
        GLog.log(L.tr("Save_properties_to:_") + xmlFile.getAbsolutePath());
        changed = false;
    }

    /**
     * @return a map containing all properties stored in this object.
     */
    public Map getProperties() {
        return thisProperty;
    }

    protected void overwriteIfNull(String key, Object defaultVal) {
        if (get(key) == null) set(key, defaultVal);
    }

    /**
     * If the key is null this method will connect the key with a default value.
     */
    public void setDefault() {
        changed = false;
        overwriteIfNull("fetchlimit", new Integer(1000));
        overwriteIfNull("commands.size", new Integer(30));
        overwriteIfNull("commands." + GDB.P, new ListenerList(0));
        overwriteIfNull("commands." + GDB.S, new ListenerList(0));
        overwriteIfNull("commands." + GDB.R, new ListenerList(0));
        overwriteIfNull("commands." + GDB.TI, new ListenerList(0));
        ServerProperties sp = new ServerProperties(("localhost"), 7083);
        overwriteIfNull("server.properties", sp);
        overwriteIfNull("connection.properties", new ConnectionHelper(ConnectionHelper.DERBY_CLIENT, "jdbc:derby://" + HOST + ":" + PORT + "/sample", "admin", "secure", 1, 100));
        overwriteIfNull("saveSelection.startup", defaultStartup);
        overwriteIfNull("browser", "firefox");
        overwriteIfNull("helpLocation", new File("doc/web/userguide/index.html"));
        overwriteIfNull("quickStartLocation", new File("doc/web/quickstart/index.html"));
        overwriteIfNull("helpWantedLocation", new File("doc/web/helpwanted/index.html"));
        overwriteIfNull("aboutLocation", new File("doc/web/about.html"));
        overwriteIfNull("logCayenne", new Boolean(false));
        overwriteIfNull("logPropertiesLocation", new File(GDirHelper.get().getSystem() + "/log4j.properties"));
        overwriteIfNull("fontsize", new Integer(12));
        overwriteIfNull("application.properties", _getAppPropFile());
        overwriteIfNull("fullscreen", new Boolean(false));
        set("fontsize", get("fontsize"));
    }

    public void setFontSize(int integer) {
        set("fontsize", new Integer(integer));
    }

    public int getFontSize() {
        return ((Integer) get("fontsize")).intValue();
    }

    public void setUnparsedConnection(ConnectionHelper connectionHelper) {
        set("connection.properties", connectionHelper);
    }

    public ConnectionHelper getConnection() {
        ConnectionHelper uh = getUnparsedConnection();
        ConnectionHelper ret = new ConnectionHelper(uh.getDriverName(), parseFromText(uh.getUrl()), uh.getUserName(), uh.getPassword(), uh.getMinConnections(), uh.getMaxConnections());
        return ret;
    }

    public ConnectionHelper getUnparsedConnection() {
        return (ConnectionHelper) get("connection.properties");
    }

    public static final String HOST = "_HOST_";

    public static final String PORT = "_PORT_";

    private String parseFromText(String unparsedUrl) {
        String ret = unparsedUrl.replaceAll(HOST, getServer().getHost());
        ret = ret.replaceAll(PORT, Integer.toString(getServer().getPortID()));
        return ret;
    }

    /**
     * This is exclusive for l2fprod:
     */
    public void setAppPropFile(File file) {
        set("application.properties", file);
    }

    /**
     * This is for arguments if started from console
     */
    public void setAppPropFile(String file) {
        if (file.indexOf('/') == -1 && file.indexOf('\\') == -1) file = GDirHelper.get().getDir() + file;
        set("application.properties", file);
    }

    private static File _getAppPropFile() {
        return new File(GDirHelper.get().getDir() + "gstpl.xml");
    }

    public File getAppPropFile() {
        return (File) get("application.properties");
    }

    public void setLogCayenne(boolean bool) {
        set("logCayenne", new Boolean(bool));
    }

    public boolean getLogCayenne() {
        return ((Boolean) get("logCayenne")).booleanValue();
    }

    public void setFetchLimit(int i) {
        set("fetchlimit", new Integer(i));
    }

    public int getFetchLimit() {
        return ((Integer) get("fetchlimit")).intValue();
    }

    /**
     * This method stores the specified user selection. Always use this method
     * if you have something like: "You this message on the next startup?".
     */
    public void setSelection(String selName, Number selectionKind) {
        set("saveSelection." + selName, selectionKind);
    }

    /**
     * @return true if specified user selection was made. false if not or if
     * selName was not found. (silent exception)
     */
    public int getSelection(String selName) {
        return ((Integer) get("saveSelection." + selName)).intValue();
    }

    public void setStartedAppFromGui(boolean bool) {
        set(("startedAppFromGui"), new Boolean(bool));
    }

    public boolean getStartedAppFromGui() {
        return ((Boolean) get(("startedAppFromGui"))).booleanValue();
    }

    public List getCommands(String tableName) {
        return (List) get(("commands.") + tableName);
    }

    public int getMaxCommandListSize(String tableName) {
        return ((Integer) get(("commands.size"))).intValue();
    }

    public File getAboutLocation() {
        return (File) get("aboutLocation");
    }

    public File getQuickStartLocation() {
        return (File) get("quickStartLocation");
    }

    public File getHelpLocation() {
        return (File) get("helpLocation");
    }

    public String getBrowser() {
        return (String) get("browser");
    }

    public void setBrowser(String str) {
        set("browser", str);
    }

    public void setServer(ServerProperties sp) {
        set(("server.properties"), sp);
    }

    public ServerProperties getServer() {
        return (ServerProperties) get(("server.properties"));
    }

    private static final Integer defaultStartup = new Integer(Startup.DIALOG);

    private int lastStartup = defaultStartup.intValue();

    public void setStartupDialogVisible(boolean visible) {
        int tmp = getSelection("startup");
        if (visible) {
            if (tmp != defaultStartup.intValue()) {
                lastStartup = tmp;
                setSelection("startup", defaultStartup);
            }
        } else {
            if (tmp != lastStartup) {
                setSelection("startup", new Integer(lastStartup));
            }
        }
    }

    public boolean isStartupDialogVisible() {
        return getSelection("startup") == defaultStartup.intValue();
    }

    public void setFullScreen(boolean b) {
        set(("fullscreen"), new Boolean(b));
    }

    public boolean isFullScreen() {
        return ((Boolean) get(("fullscreen"))).booleanValue();
    }

    public File getLogPropertiesLocation() {
        return ((File) get(("logPropertiesLocation")));
    }

    class ListenerList extends ArrayList {

        public ListenerList(int init) {
            super(init);
        }

        public boolean remove(Object o) {
            if (super.remove(o)) {
                changed = true;
                return true;
            } else return false;
        }

        public boolean removeAll(Collection c) {
            if (super.removeAll(c)) {
                changed = true;
                return true;
            } else return false;
        }

        public Object remove(int index) {
            changed = true;
            return super.remove(index);
        }

        public boolean add(Object o) {
            if (super.add(o)) {
                changed = true;
                return true;
            } else return false;
        }

        public void add(int index, Object element) {
            changed = true;
            super.add(index, element);
        }

        public boolean addAll(int index, Collection c) {
            if (super.addAll(index, c)) {
                changed = true;
                return true;
            } else return false;
        }

        public boolean addAll(Collection c) {
            if (super.addAll(c)) {
                changed = true;
                return true;
            } else return false;
        }

        public void clear() {
            changed = true;
            super.clear();
        }

        public boolean retainAll(Collection c) {
            if (super.retainAll(c)) {
                changed = true;
                return true;
            } else return false;
        }

        public Object set(int index, Object element) {
            changed = true;
            return super.set(index, element);
        }
    }
}
