package org.archive.crawler.datamodel.settings;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import org.archive.crawler.datamodel.CrawlOrder;
import org.archive.util.FileUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/** A SettingsHandler which uses XML files as persistent storage.
 *
 * @author John Erik Halse
 */
public class XMLSettingsHandler extends SettingsHandler {

    private static Logger logger = Logger.getLogger("org.archive.crawler.datamodel.settings.XMLSettingsHandler");

    protected static final String XML_SCHEMA = "heritrix_settings.xsd";

    protected static final String XML_ROOT_ORDER = "crawl-order";

    protected static final String XML_ROOT_HOST_SETTINGS = "crawl-settings";

    protected static final String XML_ELEMENT_CONTROLLER = "controller";

    protected static final String XML_ELEMENT_META = "meta";

    protected static final String XML_ELEMENT_NAME = "name";

    protected static final String XML_ELEMENT_DESCRIPTION = "description";

    protected static final String XML_ELEMENT_DATE = "date";

    protected static final String XML_ELEMENT_OBJECT = "object";

    protected static final String XML_ELEMENT_NEW_OBJECT = "newObject";

    protected static final String XML_ATTRIBUTE_NAME = "name";

    protected static final String XML_ATTRIBUTE_CLASS = "class";

    private File orderFile;

    private static final String settingsFilename = "settings.xml";

    /** Create a new XMLSettingsHandler object.
     *
     * @param orderFile where the order file is located.
     * @throws InvalidAttributeValueException
     */
    public XMLSettingsHandler(File orderFile) throws InvalidAttributeValueException {
        super();
        this.orderFile = orderFile;
    }

    /** Initialize the SettingsHandler.
     *
     * This method builds the settings data structure and initializes it with
     * settings from the order file given to the constructor.
     */
    public void initialize() {
        super.initialize();
    }

    /** Initialize the SettingsHandler from a source.
     *
     * This method builds the settings data structure and initializes it with
     * settings from the order file given as a parameter. The intended use is
     * to create a new order file based on a default (template) order file.
     *
     * @param source the order file to initialize from.
     */
    public void initialize(File source) {
        File tmpOrderFile = orderFile;
        orderFile = source;
        this.initialize();
        orderFile = tmpOrderFile;
    }

    private File getSettingsDirectory() {
        String settingsDirectoryName = null;
        try {
            settingsDirectoryName = (String) getOrder().getAttribute(CrawlOrder.ATTR_SETTINGS_DIRECTORY);
        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        } catch (MBeanException e) {
            e.printStackTrace();
        } catch (ReflectionException e) {
            e.printStackTrace();
        }
        return getPathRelativeToWorkingDirectory(settingsDirectoryName);
    }

    /** Resolves a scope (host/domain) into a file path.
     *
     * It will also create the directory structure leading to this file
     * if it doesn't exist.
     *
     * @param scope the host or domain to get file path for.
     * @return the file path for this scope.
     */
    protected final File scopeToFile(String scope) {
        File settingsDirectory = getSettingsDirectory();
        File file;
        if (scope == null || scope.equals("")) {
            return orderFile;
        } else {
            String elements[] = scope.split("\\.");
            if (elements.length == 0) {
                return orderFile;
            }
            StringBuffer path = new StringBuffer();
            for (int i = elements.length - 1; i > 0; i--) {
                path.append(elements[i]);
                path.append(File.separatorChar);
            }
            path.append(elements[0]);
            file = new File(settingsDirectory, path.toString());
        }
        file = new File(file, settingsFilename);
        return file;
    }

    public final void writeSettingsObject(CrawlerSettings settings) {
        File filename = scopeToFile(settings.getScope());
        writeSettingsObject(settings, filename);
    }

    /** Write a CrawlerSettings object to a specified file.
     *
     * This method is similar to {@link #writeSettingsObject(CrawlerSettings)}
     * except that it uses the submitted File object instead of trying to
     * resolve where the file should be written.
     *
     * @param settings the settings object to be serialized.
     * @param filename the file to which the settings object should be written.
     */
    public final void writeSettingsObject(CrawlerSettings settings, File filename) {
        logger.fine("Writing " + filename.getAbsolutePath());
        filename.getParentFile().mkdirs();
        try {
            StreamResult result = new StreamResult(new BufferedOutputStream(new FileOutputStream(filename)));
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            Source source = new CrawlSettingsSAXSource(settings);
            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Read the CrawlerSettings object from a specific file.
     *
     * @param settings the settings object to be updated with data from the
     *                 persistent storage.
     * @param filename the file to read from.
     * @return the updated settings object or null if there was no data for this
     *         in the persistent storage.
     */
    protected final CrawlerSettings readSettingsObject(CrawlerSettings settings, File filename) {
        if (filename.exists()) {
            logger.fine("Reading " + filename.getAbsolutePath());
            try {
                XMLReader parser = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
                InputStream file = new BufferedInputStream(new FileInputStream(filename));
                parser.setContentHandler(new CrawlSettingsSAXHandler(settings));
                InputSource source = new InputSource(file);
                source.setSystemId(filename.toURL().toExternalForm());
                parser.parse(source);
            } catch (SAXParseException e) {
                logger.warning(e.getMessage() + " in '" + e.getSystemId() + "', line: " + e.getLineNumber() + ", column: " + e.getColumnNumber());
            } catch (SAXException e) {
                logger.warning(e.getMessage() + ": " + e.getException().getMessage());
            } catch (ParserConfigurationException e) {
                logger.warning(e.getMessage() + ": " + e.getCause().getMessage());
            } catch (FactoryConfigurationError e) {
                logger.warning(e.getMessage() + ": " + e.getException().getMessage());
            } catch (IOException e) {
                logger.warning("Could not access file '" + filename.getAbsolutePath() + "': " + e.getMessage());
            }
        } else {
            settings = null;
        }
        return settings;
    }

    protected final CrawlerSettings readSettingsObject(CrawlerSettings settings) {
        File filename = scopeToFile(settings.getScope());
        return readSettingsObject(settings, filename);
    }

    /** Get the <code>File</code> object pointing to the order file.
     *
     * @return File object for the order file.
     */
    public File getOrderFile() {
        return orderFile;
    }

    /** Creates a replica of the settings file structure in another directory
     * (fully recursive, includes all per host settings). The SettingsHandler
     * will then refer to the new files.
     *
     * Observe that this method should only be called after the SettingsHandler
     * has been initialized.
     *
     * @param newOrderFileName where the new order file should be saved.
     * @param newSettingsDirectory the top level directory of the per host/domain
     *                          settings files.
     * @throws IOException
     */
    public void copySettings(File newOrderFileName, String newSettingsDirectory) throws IOException {
        File oldSettingsDirectory = getSettingsDirectory();
        orderFile = newOrderFileName;
        try {
            getOrder().setAttribute(new Attribute(CrawlOrder.ATTR_SETTINGS_DIRECTORY, newSettingsDirectory));
        } catch (Exception e) {
            throw new IOException("Could not update settings with new location: " + e.getMessage());
        }
        writeSettingsObject(getSettingsObject(null));
        File newDir = getPathRelativeToWorkingDirectory(newSettingsDirectory);
        FileUtils.copyFiles(oldSettingsDirectory, newDir);
    }

    /**
     * Transforms a relative path so that it is relative to the location of the
     * order file. If an absolute path is given, it will be returned unchanged.<p>
     * The location of it's order file is always considered as the 'working'
     * directory for any given settings.
     * @param path A relative path to a file (or directory)
     * @return The same path modified so that it is relative to the file level
     *         location of the order file for the settings handler.
     */
    public File getPathRelativeToWorkingDirectory(String path) {
        File f = new File(path);
        if (!f.isAbsolute()) {
            f = new File(this.getOrderFile().getParent(), path);
        }
        return f;
    }

    public ArrayList getDomainOverrides(String rootDomain) {
        File settingsDir = getSettingsDirectory();
        ArrayList domains = new ArrayList();
        while (rootDomain != null && rootDomain.length() > 0) {
            if (rootDomain.indexOf('.') < 0) {
                domains.add(rootDomain);
                break;
            } else {
                domains.add(rootDomain.substring(0, rootDomain.indexOf('.')));
                rootDomain = rootDomain.substring(rootDomain.indexOf('.') + 1);
            }
        }
        StringBuffer subDir = new StringBuffer();
        for (int i = (domains.size() - 1); i >= 0; i--) {
            subDir.append(File.separator + domains.get(i));
        }
        settingsDir = new File(settingsDir.getPath() + subDir);
        ArrayList confirmedSubDomains = new ArrayList();
        if (settingsDir.exists()) {
            File[] possibleSubDomains = settingsDir.listFiles();
            for (int i = 0; i < possibleSubDomains.length; i++) {
                if (possibleSubDomains[i].isDirectory() && isOverride(possibleSubDomains[i])) {
                    confirmedSubDomains.add(possibleSubDomains[i].getName());
                }
            }
        }
        return confirmedSubDomains;
    }

    /**
     * Checks if a file is a a 'per host' override or if it's a directory if it
     * or it's subdirectories  contains a 'per host' override file.
     * @param f The file or directory to check
     * @return True if the file is an override or it's a directory that contains
     *         such a file.
     */
    private boolean isOverride(File f) {
        if (f.isDirectory()) {
            File[] subs = f.listFiles();
            for (int i = 0; i < subs.length; i++) {
                if (isOverride(subs[i])) {
                    return true;
                }
            }
        } else if (f.getName().equals(settingsFilename)) {
            return true;
        }
        return false;
    }

    /** Delete a settings object from persistent storage.
     *
     * Deletes the file represented by the submitted settings object. All empty
     * directories that are parents to the files path are also deleted.
     *
     * @param settings the settings object to delete.
     */
    public void deleteSettingsObject(CrawlerSettings settings) {
        super.deleteSettingsObject(settings);
        File settingsDirectory = getSettingsDirectory();
        File settingsFile = scopeToFile(settings.getScope());
        settingsFile.delete();
        settingsFile = settingsFile.getParentFile();
        while (settingsFile.isDirectory() && settingsFile.list().length == 0 && !settingsFile.equals(settingsDirectory)) {
            settingsFile.delete();
            settingsFile = settingsFile.getParentFile();
        }
    }
}
