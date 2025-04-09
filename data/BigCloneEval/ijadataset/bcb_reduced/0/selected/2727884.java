package de.peathal.util;

import de.peathal.resource.L;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.security.AccessControlException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * This class servers as base class for the property classes which should
 * be able to load/save its content from/to file.
 *
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public abstract class AbstractProperties extends FireTo implements Serializable {

    private static final long serialVersionUID = 7972371872720088133L;

    public static final String RESTORED_FROM_TMP = "restoreFromTmp";

    public static final String STORED_TO_TMP_EVENT = "storeToTmp";

    private static final String couldnotLoad = L.tr("Couldn't_load_object_from_file_-_");

    private static final String zipEntryName = "xml";

    private static final String xmlFileEnding = ".xml";

    private transient HashMap<String, Object> tmp;

    private transient boolean tmpChanged;

    private transient boolean stored = false;

    private transient Map<String, Object> defaultProperties;

    private Map<String, Object> thisProperty;

    /**
     * This variable indicates if thisProperty has changes. (then changed = true)
     */
    private boolean changed;

    protected AbstractProperties() {
        changed = false;
        setDefault();
    }

    /**
     * This method loads a singleton specified by its classname.
     */
    public static Object loadSingleton(Class propertyClass) {
        IDirHelper dh = Lookup.get().getImplObject(IDirHelper.class);
        File xmlFile = new File(dh.getDir() + propertyClass.getSimpleName() + xmlFileEnding);
        return loadFromFile(xmlFile);
    }

    public static Object loadFromFile(File xmlFile) {
        ISerializer xs = Lookup.get().getImplObject(ISerializer.class);
        Object obj = null;
        try {
            InputStream is = new FileInputStream(xmlFile);
            try {
                if (xmlFile.getName().endsWith(".gz")) {
                    is = new GZIPInputStream(is);
                } else if (xmlFile.getName().endsWith(".zip")) {
                    is = new ZipFile(xmlFile).getInputStream(new ZipEntry(zipEntryName));
                }
                obj = xs.fromXML(is);
            } finally {
                is.close();
            }
        } catch (FileNotFoundException e) {
            GLog.log(couldnotLoad + L.tr("File_not_found:_") + xmlFile.getAbsolutePath());
        } catch (IOException e) {
            GLog.warn(couldnotLoad + L.tr("May_be_the_gzipped_file_is_currupt_or_it_is_the_wrong_file_format."), e);
        } catch (AccessControlException e) {
            GLog.warn(couldnotLoad + L.tr("Please_specify_this_in_your_security_policy_file!"), e);
        } catch (Exception e) {
            GLog.warn(couldnotLoad + L.tr("May_be_it_isn't_the_correct_file_version!"), e);
        }
        return obj;
    }

    /**
     * This method overwrites all non null values of this 
     * AbstractProperties object from the specified ap if this object
     * already contains such a key.
     */
    public void getExistingValuesFrom(AbstractProperties ap) {
        Map<String, Object> proper = ap.getProperties();
        for (String key : getProperties().keySet()) {
            Object obj = proper.get(key);
            if (obj != null) {
                set(key, obj);
            }
        }
    }

    /**
     * This method save the specified object to a file.
     * It uses the same filename to save and to load the object.
     */
    protected void saveSingleton(Object obj) {
        IDirHelper dh = Lookup.get().getImplObject(IDirHelper.class);
        File xmlFile = new File(dh.getDir() + obj.getClass().getSimpleName() + xmlFileEnding);
        saveToFile(obj, xmlFile);
    }

    public static void saveToFile(Object obj, File xmlFile) {
        ISerializer xs = Lookup.get().getImplObject(ISerializer.class);
        OutputStream os;
        try {
            os = new FileOutputStream(xmlFile);
            try {
                if (xmlFile.getName().endsWith(".zip")) {
                    ZipOutputStream zip = new ZipOutputStream(os);
                    zip.putNextEntry(new ZipEntry(zipEntryName));
                    os = zip;
                } else if (xmlFile.getName().endsWith(".gz")) {
                    GZIPOutputStream zip = new GZIPOutputStream(os);
                    os = zip;
                }
                xs.toXML(obj, os);
            } finally {
                os.close();
            }
        } catch (SerializeException e) {
            GLog.warn(L.tr("XML_Write_Exception:"), e);
        } catch (MalformedURLException e) {
            GLog.warn(L.tr("Malformed_URL:"), e);
        } catch (FileNotFoundException ex) {
            GLog.warn(L.tr("Can't_find_file!"));
        } catch (IOException e) {
            GLog.warn(L.tr("Can't_gzip_the_file:_IOException:"), e);
        }
        GLog.log(L.tr("Saved_object_to:") + " " + xmlFile.getAbsolutePath());
    }

    protected synchronized Object get(String key) {
        return getProperties().get(key);
    }

    /**
     * This method overwrites specified key with defaultValue.
     *
     * @see set(String, Object, boolean)
     */
    protected Object set(String key, Object value) {
        return set(key, value, true);
    }

    /**
     * This method sets the specified propertyKey to the associated value
     * and it fires a property change if this is appropriated.
     *
     * @param overwriteAlways is false if this method writes the specified
     * value only if no previous value is associated with specified key.
     * That means it will write only if get(key) == null. Otherwise if it is
     * true it will write the value always.
     *
     * @return the old value associated to key
     */
    protected synchronized Object set(String key, Object value, boolean overwriteAlways) {
        if (key == null || value != null && value.equals(getProperties().get(key))) {
            return null;
        }
        if (!overwriteAlways && get(key) != null) {
            return get(key);
        }
        Object ret;
        synchronized (this) {
            ret = getProperties().put(key, value);
        }
        changed = true;
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

    /**
     * Override this method to initialize properties with some good standard values.
     * But only if they where not saved to the restored file.
     * This method will be called in constructor and while reading from file.
     */
    protected final void setDefault() {
        revertChange();
        if (defaultProperties == null) {
            defaultProperties = new HashMap<String, Object>(30);
        }
        initDefault();
    }

    public void setDefault(String s, Object o) {
        defaultProperties.put(s, o);
        set(s, o, false);
    }

    /**
     * Subclasses can use this method to put some {key, value} pairs via
     * setDefault in the default property map.
     */
    protected void initDefault() {
    }

    public final void resetToDefaultValues() {
        change();
        for (Entry<String, Object> e : defaultProperties.entrySet()) {
            set(e.getKey(), e.getValue());
        }
    }

    /**
     * This method saves the current application.properties to a File.
     */
    public synchronized void save() {
        saveSingleton(this);
        changed = false;
    }

    /**
     * @return a map containing all properties stored in this object.
     */
    public synchronized Map<String, Object> getProperties() {
        if (thisProperty == null) {
            thisProperty = new HashMap<String, Object>(30);
        }
        return thisProperty;
    }

    public synchronized void storeToTmp() {
        tmp = new HashMap<String, Object>(getProperties());
        tmpChanged = changed;
        stored = true;
        fireChangeEvent(new PropertyChangeEvent(this, STORED_TO_TMP_EVENT, null, null));
    }

    public synchronized void restoreFromTmp() {
        if (stored) {
            Entry<String, Object> e;
            Iterator<Entry<String, Object>> iter = tmp.entrySet().iterator();
            while (iter.hasNext()) {
                e = iter.next();
                set(e.getKey(), e.getValue());
            }
            changed = tmpChanged;
            stored = false;
            fireChangeEvent(new PropertyChangeEvent(this, RESTORED_FROM_TMP, null, null));
        }
    }

    synchronized void change() {
        changed = true;
    }

    /**
     * This method only removes the changed flag from this properties.
     * It does not revert the changes that were made to it.
     */
    protected synchronized void revertChange() {
        changed = false;
    }

    /**
     * This method returns true, if there was changes through set(),
     * change() or any other explicit setter.
     */
    public boolean hasChanges() {
        return changed;
    }
}
