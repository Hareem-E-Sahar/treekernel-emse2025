package net.rptools.chartool.model.property;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.rptools.chartool.model.db.PropertyTable;
import net.rptools.chartool.model.db.PropertyTableXML;
import net.rptools.chartool.model.property.MultipleScriptOwner.ScriptDescriptor;
import net.rptools.chartool.model.xml.ConverterSupport;
import net.rptools.chartool.ui.component.RPIcon;
import net.rptools.chartool.ui.component.Utilities;
import net.rptools.lib.FileUtil;
import net.rptools.lib.io.PackedFile;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;

/**
 * A packed file that contains the property definition, database, and named and invoked script files used by 
 * properties.  
 * 
 * @author jgorrell
 * @version $Revision$ $Date$ $Author$
 */
public class PropertySettingsFile {

    /**
   * The packed file that is read and modified by this property settings file.
   */
    private PackedFile packed;

    /**
   * Flag indicating that this settings file is being validated. May only perform loads when validating
   */
    private boolean validating;

    /**
   * The directory in the zip file that contains the property data.
   */
    public static final String PROPERTY_DIR = "property/";

    /**
   * The file that contains the {@link PropertyDescriptorSet} XML.
   */
    public static final String PROPERTY_DESCRIPTOR_FILE = PROPERTY_DIR + "propertyDescriptorSet.xml";

    /**
   * The file that contains the {@link Properties} which map the MT property names to the CT property names.
   */
    public static final String MT_PROPERTY_MAP_FILE = PROPERTY_DIR + "mtPropertyMap.properties";

    /**
   * The directory that contains all of the rpdat database files for the properties.
   */
    public static final String DATABASE_DIR = PROPERTY_DIR + "database/";

    /**
   * The directory that contains all of the named scripts XML.
   */
    public static final String NAMED_SCRIPT_DIR = "namedScripts/";

    /**
   * The directory that contains all of the print files and their descriptor.
   */
    public static final String PRINT_DIR = "print/";

    /**
   * The file that contains the {@link PropertyDescriptorSet} XML.
   */
    public static final String PRINT_DESCRIPTOR_FILE = PRINT_DIR + "printDescriptor.xml";

    /**
   * The directory that contains all of the font files.
   */
    public static final String FONTS_DIR = "fonts/";

    /**
   * The property in the packed file that contains the game name.
   */
    public static final String GAME_NAME_PROP_NAME = "gameName";

    /**
   * The property in the packed file that contains the name of a source. If
   * this value is not <code>null</code> then the file is considered a source file.
   */
    public static final String SOURCE_PROP_NAME = "source";

    /**
   * The directory in the zip file that contains all of the forms.
   */
    public static final String FORMS_DIR = "forms/";

    /**
   * The directory in the zip file that contains all of the forms.
   */
    public static final String IMAGES_DIR = "images/";

    /**
   * The directory that contains all of the java class files.
   */
    public static final String JAVA_DIR = "java/";

    /**
   * Logger instance for this class.
   */
    private static final Logger LOGGER = Logger.getLogger(PropertySettingsFile.class.getName());

    /**
   * Create the property settings file for the passed zip file.
   * 
   * @param file Zip file containing the property settings.
   */
    public PropertySettingsFile(PackedFile file) {
        packed = file;
    }

    /** @return Getter for packed */
    public PackedFile getPacked() {
        return packed;
    }

    /**
   * Get the list of all of the paths starting at a certain directory. This includes all the 
   * file paths in descendant directories well as the ones in the passed directory.
   * 
   * @param dir Find the paths starting from this directory.
   * @return The list of file paths found
   */
    public List<String> getDirPaths(String dir) {
        List<String> paths = new ArrayList<String>();
        try {
            for (String path : packed.getPaths()) if (path.startsWith(dir) && !path.equals(dir)) paths.add(path);
            return paths;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Problem reading " + dir + " paths.", e);
            throw new IllegalStateException("Invalid property file: " + packed.getPackedFile().getAbsolutePath());
        }
    }

    /**
   * Create and register a font for use by character sheets.
   * 
   * @param path The path to the font file. Only True type fonts are supported.
   */
    public void loadFont(String path) {
        InputStream is = null;
        try {
            is = packed.getFile(path);
            Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            if (validating) return;
            if (!GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font)) LOGGER.log(Level.FINE, "Unable to register the font in file: " + path);
        } catch (FontFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid font: " + path, e);
            throw new IllegalArgumentException("Invalid font file: " + path, e);
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.WARNING, "Unknown file: " + path, e);
            throw new IllegalArgumentException("Invalid resource: " + path);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Invalid font file: " + path, e);
            throw new IllegalArgumentException("Invalid font file: " + path, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Ignoring font close exception", e);
                }
            }
        }
    }

    /**
   * Generic routine to read an XML object from a file in the packed file.
   * 
   * @param xstream Configured XStream
   * @param path Path that should be read.
   * @param fileMessage Message displayed to users.
   * @param baseObject The base object used to receive the xml info.
   * @return The object read from the file. 
   */
    protected Object readXMLObject(XStream xstream, String path, String fileMessage, Object baseObject) {
        Reader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(packed.getFile(path)));
            return xstream.fromXML(reader, baseObject);
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.WARNING, "Couldn't open: '" + path + "'", e);
            throw new IllegalStateException("Couldn't open the " + fileMessage + ".", e);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Problem reading: '" + path + "'", e);
            throw new IllegalStateException("Could not read the " + fileMessage + ".", e);
        } catch (ConversionException e) {
            LOGGER.log(Level.WARNING, "Invalid XML in path: '" + path + "'", e);
            throw new IllegalStateException("Invalid " + fileMessage + " data.", e);
        } catch (RuntimeException e1) {
            LOGGER.log(Level.WARNING, "Unexepected problem reading: '" + path + "'", e1);
            throw new IllegalStateException("Unexepected problem reading the " + fileMessage + " file.", e1);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    LOGGER.log(Level.INFO, "Ignoring close exception: '" + path + "'", e1);
                }
            }
        }
    }

    /**
   * Generic routine to write a Java Object as an XML file in the packed file.
   * 
   * @param xstream Configured XStream
   * @param path Path that should be written.
   * @param fileMessage Message displayed to users on error.
   * @param baseObject The base object used to receive the xml info.
   */
    protected void writeXMLObject(XStream xstream, String path, String fileMessage, Object baseObject) {
        Writer writer = null;
        try {
            writer = new StringWriter();
            xstream.toXML(baseObject, writer);
            packed.putFile(path, writer.toString().getBytes());
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.WARNING, "Couldn't open: '" + path + "'", e);
            throw new IllegalStateException("Couldn't open the " + fileMessage + ".", e);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Problem writing: '" + path + "'", e);
            throw new IllegalStateException("Could not write the " + fileMessage + ".", e);
        } catch (RuntimeException e1) {
            LOGGER.log(Level.WARNING, "Unexepected problem writing: '" + path + "'", e1);
            throw new IllegalStateException("Unexepected problem writing the " + fileMessage + " file.", e1);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e1) {
                    LOGGER.log(Level.INFO, "Ignoring close exception on '" + path + "'.", e1);
                }
            }
        }
    }

    /**
   * Write a single database to a file.
   * 
   * @param database Write a table this database
   * @param table Write this table
   * @param path To this path.
   */
    protected void saveDatabase(String database, String table, String path) {
        OutputStream os = null;
        try {
            if (table == null) {
                PropertyDescriptorSet db = PropertyTableXML.getInstance().readDescriptor(packed, path);
                table = db.getType();
            }
            os = packed.getOutputStream(path);
            PropertyTable pt = PropertyTable.getPropertyTable(database, table);
            PropertyTableXML.getInstance().writeDatabaseFile(pt, new OutputStreamWriter(os));
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Problem opening output stream for " + database + "." + table + " to path '" + path + ".", e);
            throw new IllegalArgumentException("Problem opening output stream for  " + database + "." + table + " to path '" + path + ".");
        } catch (IllegalStateException e) {
            LOGGER.log(Level.WARNING, "Problem writing database " + database + "." + table + " to path '" + path + ".", e);
            throw new IllegalArgumentException("Problem writing database " + database + "." + table + " to path '" + path + ".");
        } finally {
            try {
                if (os != null) os.close();
            } catch (IOException e) {
            }
        }
    }

    /**
   * Read the data from XML and place it into the database.
   * 
   * @param data The path to the XML data
   * @param database The name of the database being updated.
   * @param table The table being written.
   * @return The database that was imported.
   */
    protected PropertyTable loadDatabase(String data, String database, String table) {
        if (validating) return null;
        try {
            if (!packed.hasFile(data)) return null;
            if (table != null) {
                return PropertyTableXML.getInstance().createAndLoadPropertyTable(data, database, table, packed);
            } else {
                return PropertyTableXML.getInstance().loadDatabaseFile(database, data, packed);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unable to install the table '" + data + "'", e);
            throw new IllegalArgumentException("Unable to install the table '" + data + "'");
        }
    }

    /**
   * Internal routine to remove a database and it's descriptor file from the settings file.
   * 
   * @param path Full path to the file that describes the database and its data.
   * @param database The database containing the data.
   */
    protected void removeDatabaseImpl(String path, String database) {
        PropertyDescriptorSet table = null;
        try {
            if (!packed.hasFile(path)) throw new IllegalArgumentException("Path does not exist: " + path);
            table = PropertyTableXML.getInstance().readDescriptor(packed, path);
            PropertyTable.deletePropertyTable(database, table.getType());
            getPacked().removeFile(path);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to find the table name from path: " + path);
            throw new IllegalArgumentException("Unable to find the table name from path: " + path);
        }
    }

    /**
   * Read an events script object from the property file.
   * 
   * @param path Path of the file w/in the property file
   * @param fileMessage Message displayed in errors.
   * @param sd Descriptors for the event scripts objects
   * @return The new event scripts object.
   */
    protected EventScripts readEventScriptsObject(String path, String fileMessage, ScriptDescriptor[] sd) {
        EventScripts scripts = new EventScripts(sd);
        try {
            if (!packed.hasFile(path)) return scripts;
        } catch (IOException e) {
            throw new IllegalStateException("Bad settings file: " + packed.getPackedFile().getAbsolutePath());
        }
        XStream xstream = ConverterSupport.getXStream(EventScripts.class);
        return (EventScripts) readXMLObject(xstream, path, fileMessage, scripts);
    }

    /**
   * Read an events script object from the property file.
   * 
   * @param path Path of the file w/in the property file
   * @param fileMessage Message displayed in errors.
   * @param scripts Scripts written to XML.
   */
    protected void writeEventScriptsObject(String path, String fileMessage, EventScripts scripts) {
        XStream xstream = ConverterSupport.getXStream(EventScripts.class);
        writeXMLObject(xstream, path, fileMessage, scripts);
    }

    /**
   * Import a file into the settings file
   * 
   * @param path The path of the file in the packed file.
   * @param file The file to import.
   */
    public void importFile(String path, File file) {
        OutputStream os = null;
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(file));
            os = packed.getOutputStream(path);
            FileUtil.copy(is, os);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to write the form file '" + file.getAbsolutePath() + "' to the packed file " + packed.getPackedFile().getAbsolutePath(), e);
            throw new IllegalArgumentException("Unable to write the form file '" + file.getAbsolutePath() + "' to the packed file " + packed.getPackedFile().getAbsolutePath());
        } finally {
            try {
                if (os != null) os.close();
            } catch (IOException e) {
                LOGGER.log(Level.INFO, "Ignoring exception closing output file", e);
            }
            try {
                if (is != null) is.close();
            } catch (IOException e) {
                LOGGER.log(Level.INFO, "Ignoring exception closing inpyt file", e);
            }
        }
    }

    /**
   * Copy a path into a temporary file.
   * 
   * @param path The path to export.
   * @param type The file type for the temporary file.
   * @param exportFile An optional file name where the path is exported. If <code>null</code>
   * the file is exported to a temporary file of the given type.
   * @return The new temporary file containing the form.
   */
    public File exportFile(String path, String type, File exportFile) {
        OutputStream os = null;
        InputStream is = null;
        File file = exportFile;
        try {
            if (file == null) file = File.createTempFile(path, type);
            os = new BufferedOutputStream(new FileOutputStream(file));
            is = packed.getFile(path);
            FileUtil.copy(is, os);
        } catch (IOException e) {
            if (file != null) file.delete();
            LOGGER.log(Level.WARNING, "Unable to read the form file from the packed file '" + packed.getPackedFile().getAbsolutePath() + "' to the temp file " + file.getAbsolutePath(), e);
            throw new IllegalArgumentException("Unable to read the form file from the packed file '" + packed.getPackedFile().getAbsolutePath() + "' to the temp file " + file.getAbsolutePath());
        } finally {
            try {
                if (os != null) os.close();
            } catch (IOException e) {
                LOGGER.log(Level.INFO, "Ignoring exception closing output file", e);
            }
            try {
                if (is != null) is.close();
            } catch (IOException e) {
                LOGGER.log(Level.INFO, "Ignoring exception closing input file", e);
            }
        }
        return file;
    }

    /**
   * Get the list of all of the database paths in this property file.
   * 
   * @return The list of database paths in this property file.
   */
    public List<String> getDatabasePaths() {
        return getDirPaths(DATABASE_DIR);
    }

    /**
   * Get the database data mapping for the files currently loaded in the settings file.
   * 
   * @return The mapping between data type names and their data descriptions
   */
    public Map<String, PropertyDescriptorSet> getDatabaseDataMapping() {
        Map<String, PropertyDescriptorSet> databaseDataMapping = new HashMap<String, PropertyDescriptorSet>();
        for (String file : getDatabasePaths()) {
            PropertyDescriptorSet set = PropertyTableXML.getInstance().readDescriptor(packed, file);
            databaseDataMapping.put(set.getType(), set);
        }
        return databaseDataMapping;
    }

    /**
   * Load all of the databases currently in the property settings file.
   * 
   * @param database Import to this database instance
   * @return The databases that were imported.
   */
    public List<PropertyTable> loadDatabases(String database) {
        List<PropertyTable> tables = new ArrayList<PropertyTable>();
        List<String> paths = getDatabasePaths();
        for (String string : paths) tables.add(loadDatabase(string, database, null));
        return tables;
    }

    /**
   * Save all of the databases to their files in the property settings file.
   * 
   * @param database Import to this database instance
   */
    public void saveDatabases(String database) {
        List<String> files = getDatabasePaths();
        for (String file : files) saveDatabase(database, null, file);
    }

    /**
   * Import a new data file into the database
   * 
   * @param name Name of the new data file in the property settings file.
   * @param database The database where the data is loaded.
   * @param data The actual data file to be imported.
   * @return The property table that was loaded.
   */
    public PropertyTable importDatabase(String name, String database, File data) {
        if (name == null) name = data.getName();
        name = DATABASE_DIR + name;
        importFile(name, data);
        return loadDatabase(name, database, null);
    }

    /**
   * Export a database to an external file.
   * 
   * @param name The path to the data file for the database that is saved.
   * @param database The database containing the table of data
   * @return The temporary file that contains the data from the table.
   */
    public File exportDatabase(String name, String database) {
        name = DATABASE_DIR + name;
        saveDatabase(database, null, name);
        return exportFile(name, "rpdat", null);
    }

    /**
   * Remove a property database and it's file from the property settings. 
   * 
   * @param name The path to the data file for the database that is saved.
   * @param database The database containing the table of data
   */
    public void removeDatabase(String name, String database) {
        name = DATABASE_DIR + name;
        removeDatabaseImpl(name, database);
    }

    /**
   * Read the property descriptors from the file.
   * 
   * @return The property descriptor set 
   */
    public PropertyDescriptorSet getPropertyDescriptors() {
        XStream xstream = ConverterSupport.getXStream(PropertyDescriptor.class, RPIcon.class, PropertyDescriptorSet.class, PropertyDescriptorMap.class);
        return (PropertyDescriptorSet) readXMLObject(xstream, PROPERTY_DESCRIPTOR_FILE, "property descriptor", null);
    }

    /**
   * Write a new property descriptor set to the file.
   * 
   * @param set New property descriptors
   */
    public void setPropertyDescriptors(PropertyDescriptorSet set) {
        XStream xstream = ConverterSupport.getXStream(PropertyDescriptor.class, RPIcon.class, PropertyDescriptorSet.class, PropertyDescriptorMap.class);
        writeXMLObject(xstream, PROPERTY_DESCRIPTOR_FILE, "property descriptor", set);
        try {
            packed.setProperty(GAME_NAME_PROP_NAME, set.getGame());
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Problem setting the game name property", e);
            throw new IllegalStateException("Unable to save the game name property", e);
        }
    }

    /**
   * Get property mapping
   * 
   * @return A mapping of MT Property names in the keys to CT property names in the values. Any 
   * value that begins with a * is written to MT properties, but not read from them.
   */
    public Properties getMTPropertyMap() {
        try {
            if (!packed.hasFile(MT_PROPERTY_MAP_FILE)) return null;
            Properties props = new Properties();
            props.load(packed.getFile(MT_PROPERTY_MAP_FILE));
            return props;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to read the MT/CT property mapping file.", e);
            throw new IllegalStateException("Unable to read the MT/CT property mapping file.", e);
        }
    }

    /**
   * Set the property mapping
   * 
   * @param props The MT/CT property mappings being saved.
   */
    public void setMTPropertyMap(Properties props) {
        try {
            if (props == null && packed.hasFile(MT_PROPERTY_MAP_FILE)) {
                packed.removeFile(MT_PROPERTY_MAP_FILE);
            } else {
                props.store(packed.getOutputStream(MT_PROPERTY_MAP_FILE), null);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to write the MT/CT property mapping file.", e);
            throw new IllegalStateException("Unable to write the MT/CT property mapping file.", e);
        }
    }

    /** @return Getter for printFiles */
    public Map<String, String> getPrintFiles() {
        try {
            if (!packed.hasFile(PRINT_DESCRIPTOR_FILE)) return null;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to read the print mapping file.", e);
            throw new IllegalStateException("Unable to read the print mapping file.", e);
        }
        return (Map<String, String>) readXMLObject(new XStream(), PRINT_DESCRIPTOR_FILE, "print descriptor", new LinkedHashMap<String, String>());
    }

    /** @param aPrintFiles Setter for printFiles */
    public void setPrintFiles(Map<String, String> aPrintFiles) {
        try {
            if (aPrintFiles == null && packed.hasFile(PRINT_DESCRIPTOR_FILE)) {
                packed.removeFile(PRINT_DESCRIPTOR_FILE);
            } else {
                writeXMLObject(new XStream(), PRINT_DESCRIPTOR_FILE, "print descriptor", aPrintFiles);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to write the print mapping file.", e);
            throw new IllegalStateException("Unable to write the print mapping file.", e);
        }
    }

    /**
   * Read all of the named scripts into the passed map.
   * 
   * @return The named scripts from the file.
   */
    public Map<String, InvokableScript> getNamedScripts() {
        List<String> namedScriptPaths = getDirPaths(NAMED_SCRIPT_DIR);
        Map<String, InvokableScript> owner = new HashMap<String, InvokableScript>();
        for (String path : namedScriptPaths) {
            try {
                String[] nt = getScriptNameAndType(path);
                InvokableScript iScript = loadScript(path, packed.getFileData(path), nt[1]);
                owner.put(nt[0], iScript);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Problem loading named script file: '" + path + "'", e);
                throw new IllegalStateException("Problem loading named script file: '" + path + "'", e);
            }
        }
        return owner;
    }

    /**
   * Get the script content, attach the type if needed and create the script
   * 
   * @param path Path to the script file
   * @param data The data for the script file.
   * @param type File type of the script file
   * @return The loaded script file
   */
    public InvokableScript loadScript(String path, byte[] data, String type) {
        String script = new String(data);
        if (type != null && !script.contains("::")) script = type + "::" + script;
        InvokableScript iScript = (InvokableScript) AbstractScript.createScript(script, true);
        iScript.setPath(path);
        return iScript;
    }

    /**
   * Get the script name and type from its file name
   * 
   * @param path Path to the script file
   * @return The name in element 0 and the file type in element 1.
   */
    public String[] getScriptNameAndType(String path) {
        String[] nt = new String[2];
        int index = path.lastIndexOf('/');
        nt[0] = path;
        if (index >= 0) nt[0] = path.substring(index + 1);
        index = nt[0].indexOf('.');
        nt[1] = null;
        if (index >= 0) {
            nt[1] = nt[0].substring(index + 1);
            nt[0] = nt[0].substring(0, index);
        }
        return nt;
    }

    /**
   * Write all of the named scripts to a file.
   * 
   * @param owner Owner of the named scripts.
   */
    public void setNamedScripts(Map<String, InvokableScript> owner) {
        try {
            for (InvokableScript script : owner.values()) {
                assert script.getPath() != null : "Script without a path";
                StringBuilder builder = new StringBuilder();
                if (!script.getPath().endsWith("." + script.getScriptType())) {
                    builder.append(script.getScriptType());
                    builder.append("::");
                }
                builder.append(script.getScript());
                packed.putFile(script.getPath(), builder.toString().getBytes());
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Problem writing script", e);
            throw new IllegalStateException("Unable to write named scripts");
        }
    }

    /**
   * Get the list of all of the form paths in this property file.
   * 
   * @return The list of form paths in this property file.
   */
    public List<String> getFormPaths() {
        return getDirPaths(FORMS_DIR);
    }

    /**
   * Put a new form in the list of forms.
   * 
   * @param name The name of the new form. If <code>null</code> the name of the file is used.
   * @param form The new form file.
   */
    public void importForm(String name, File form) {
        if (name == null) name = form.getName();
        name = FORMS_DIR + name;
        importFile(name, form);
    }

    /**
   * Copy a form into a temporary file.
   * 
   * @param name The name of the form.
   * @return The new temporary file containing the form.
   */
    public File exportForm(String name) {
        name = FORMS_DIR + name;
        return exportFile(name, ".jfrm", null);
    }

    /**
   * Remove a form from the settings file.
   * 
   * @param name The name of the form being removed.
   */
    public void removeForm(String name) {
        packed.removeFile(FORMS_DIR + name);
    }

    /**
   * Get the list of all of the image paths in this property file.
   * 
   * @return The list of image paths in this property file.
   */
    public List<String> getImagePaths() {
        return getDirPaths(IMAGES_DIR);
    }

    /**
   * Get the list of all of the named script paths in this property file.
   * 
   * @return The list of named script paths in this property file.
   */
    public List<String> getNamedScriptPaths() {
        return getDirPaths(NAMED_SCRIPT_DIR);
    }

    /**
   * Put a new image in the images directory.
   * 
   * @param name The name of the new image. If <code>null</code> the name of the file is used.
   * @param image The new image file.
   */
    public void importImage(String name, File image) {
        if (name == null) name = image.getName();
        name = IMAGES_DIR + name;
        importFile(name, image);
    }

    /**
   * Copy an image into a temporary file.
   * 
   * @param name The name of the image.
   * @return The new temporary file containing the image.
   */
    public File exportImage(String name) {
        name = IMAGES_DIR + name;
        int index = name.lastIndexOf(".");
        String type = ".unknownImage";
        if (index >= 0) type = name.substring(index);
        return exportFile(name, type, null);
    }

    /**
   * Remove an image from the settings file.
   * 
   * @param name The name of the image being removed.
   */
    public void removeImage(String name) {
        packed.removeFile(IMAGES_DIR + name);
    }

    /**
   * Load all of the fonts listed in the file.
   */
    public void loadFonts() {
        List<String> paths = getDirPaths(FONTS_DIR);
        for (String path : paths) {
            loadFont(path);
        }
    }

    /**
   * Copy all of the files in a packed file directory to the file system.
   * 
   * @param from The directory path in the packed file.
   * @param to The directory on the file system where the files are copied.
   */
    protected void copyDirectory(String from, File to) {
        List<String> paths = getDirPaths(from);
        for (String path : paths) {
            if (path.endsWith("/")) continue;
            File file = new File(to, path);
            File parent = file.getParentFile();
            if (!parent.exists()) parent.mkdirs();
            try {
                FileUtil.copy(packed.getFile(path), new BufferedOutputStream(new FileOutputStream(file)));
            } catch (FileNotFoundException e) {
                assert false : "This should never happen, the paths were read from the packed file.";
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Problem copying " + path + " path to " + file.getAbsolutePath(), e);
                throw new IllegalStateException("Invalid property file: " + packed.getPackedFile().getAbsolutePath());
            }
        }
    }

    /**
   * Install property game settings into the a given instance and database.
   * 
   * @param settings Load the settings into here
   * @param cacheDir The directory where cached files are stored.
   */
    protected void installPropertySettings(PropertySettings settings, File cacheDir) {
        if (validating) throw new IllegalStateException("Can not install while validating.");
        PropertyDescriptorSet pds = getPropertyDescriptors();
        String databaseName = pds.getDatabaseName() == null ? pds.getGame() : pds.getDatabaseName();
        settings.setDatabaseName(databaseName);
        Utilities.incrementProgressModel(1);
        PropertyTable.deleteAllPropertyTables(databaseName);
        Utilities.incrementProgressModel(1);
        PropertyTable.clearPropertyTableCache();
        Utilities.incrementProgressModel(1);
        loadDatabases(databaseName);
        Utilities.incrementProgressModel(1);
        Utilities.deleteFiles(cacheDir, true);
        Utilities.incrementProgressModel(1);
        cacheDir.mkdirs();
        Utilities.incrementProgressModel(1);
        copyDirectory(FORMS_DIR, cacheDir);
        Utilities.incrementProgressModel(1);
        copyDirectory(IMAGES_DIR, cacheDir);
        Utilities.incrementProgressModel(1);
        copyDirectory(JAVA_DIR, cacheDir);
        Utilities.incrementProgressModel(1);
    }

    /**
   * Load previously installed property settings in the given instance
   * 
   * @param settings Load the settings into here
   * @param cacheDir The directory where cached files are stored. If <code>null</code>, then
   * the class loader isn't placed in the settings.
   */
    protected void loadPropertySettings(PropertySettings settings, File cacheDir) {
        loadFonts();
        Utilities.incrementProgressModel(1);
        settings.setDatabaseDataMapping(getDatabaseDataMapping());
        Utilities.incrementProgressModel(1);
        settings.setNamedScripts(getNamedScripts());
        Utilities.incrementProgressModel(1);
        PropertyDescriptorSet pds = getPropertyDescriptors();
        settings.setCustomPropertySet(pds);
        Utilities.incrementProgressModel(1);
        String databaseName = pds.getDatabaseName() == null ? pds.getGame() : pds.getDatabaseName();
        settings.setDatabaseName(databaseName);
        Utilities.incrementProgressModel(1);
        settings.setFormPaths(getFormPaths());
        Utilities.incrementProgressModel(1);
        settings.setImagePaths(getImagePaths());
        Utilities.incrementProgressModel(1);
        settings.setMTPropertyMap(getMTPropertyMap());
        Utilities.incrementProgressModel(1);
        settings.setPrintFiles(getPrintFiles());
        Utilities.incrementProgressModel(1);
        settings.getSourceNames().clear();
        Utilities.incrementProgressModel(1);
        if (cacheDir != null) {
            try {
                cacheDir.mkdirs();
                File javaDir = new File(cacheDir, "java");
                javaDir.mkdirs();
                settings.setClassLoader(new URLClassLoader(new URL[] { cacheDir.toURI().toURL(), javaDir.toURI().toURL() }));
            } catch (MalformedURLException e) {
                throw new IllegalStateException("Unable to convert the cache directory " + cacheDir.getAbsolutePath() + " into a URL.");
            }
        }
        Utilities.incrementProgressModel(1);
    }

    /**
   * Install property game settings into the a given instance and database.
   * 
   * @param settings Load the settings into here
   * @param database Load the data into here
   * @param cacheDir The directory where cached files are stored.
   */
    protected void installGameSource(PropertySettings settings, String database, File cacheDir) {
        if (validating) throw new IllegalStateException("Can not install while validating.");
        Map<String, PropertyDescriptorSet> dataMappings = settings.getDatabaseDataMapping();
        Utilities.incrementProgressModel(1);
        List<String> paths = getDatabasePaths();
        for (String string : paths) {
            PropertyDescriptorSet pds = PropertyTableXML.getInstance().readDescriptor(getPacked(), string);
            if (dataMappings.get(pds.getType()) == null) {
                loadDatabase(string, database, null);
            } else {
                PropertyTableXML.getInstance().updatePropertyTable(string, database, pds.getType(), getPacked());
            }
        }
        Utilities.incrementProgressModel(1);
        copyDirectory(FORMS_DIR, cacheDir);
        Utilities.incrementProgressModel(1);
        copyDirectory(IMAGES_DIR, cacheDir);
        Utilities.incrementProgressModel(1);
        copyDirectory(JAVA_DIR, cacheDir);
        Utilities.incrementProgressModel(1);
    }

    /**
   * Load previously installed game source file into the given instance
   * 
   * @param settings Load the settings into here
   * @param cacheDir The directory where cached files are stored. If <code>null</code>, then
   * the class loader isn't placed in the settings.
   */
    protected void loadGameSource(PropertySettings settings, File cacheDir) {
        loadFonts();
        Utilities.incrementProgressModel(1);
        Map<String, PropertyDescriptorSet> sourceMapping = getDatabaseDataMapping();
        Utilities.incrementProgressModel(1);
        Map<String, PropertyDescriptorSet> mapping = settings.getDatabaseDataMapping();
        for (String type : sourceMapping.keySet()) {
            if (!mapping.containsKey(type)) mapping.put(type, sourceMapping.get(type));
        }
        Utilities.incrementProgressModel(1);
        settings.getNamedScripts().putAll(getNamedScripts());
        Utilities.incrementProgressModel(1);
        try {
            if (packed.hasFile(PROPERTY_DESCRIPTOR_FILE)) {
                PropertyDescriptorMap sourcePDMap = getPropertyDescriptors().getProperties();
                if (!validating) {
                    PropertyDescriptorMap pdMap = settings.getCustomPropertySet().getProperties();
                    for (String prop : sourcePDMap.keySet()) {
                        if (!pdMap.containsPropertyName(prop)) pdMap.put(sourcePDMap.get(prop));
                    }
                    settings.getCustomPropertySet().setProperties(pdMap);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error determining if the property descriptor file exists in the source file: " + packed.getPackedFile().getAbsolutePath(), e);
            throw new IllegalStateException("Error determining if the property descriptor file exists in the source file: " + packed.getPackedFile().getAbsolutePath(), e);
        }
        Utilities.incrementProgressModel(1);
        Set<String> paths = new HashSet<String>(validating ? Collections.EMPTY_SET : settings.getFormPaths());
        paths.addAll(getFormPaths());
        settings.setFormPaths(new ArrayList<String>(paths));
        Utilities.incrementProgressModel(1);
        paths = new HashSet<String>(validating ? Collections.EMPTY_SET : settings.getImagePaths());
        paths.addAll(getImagePaths());
        settings.setImagePaths(new ArrayList<String>(paths));
        Utilities.incrementProgressModel(1);
        Properties mtProps = getMTPropertyMap();
        if (mtProps != null) settings.setMTPropertyMap(mtProps);
        Utilities.incrementProgressModel(1);
        try {
            String source = (String) packed.getProperty(SOURCE_PROP_NAME);
            if (source == null) {
                LOGGER.log(Level.WARNING, "Source file '" + packed.getPackedFile().getAbsolutePath() + "' does not have a source name.");
                throw new IllegalStateException("Source file '" + packed.getPackedFile().getAbsolutePath() + "' does not have a source name.");
            }
            if (!settings.getSourceNames().add(source)) {
                LOGGER.log(Level.WARNING, "Source file '" + packed.getPackedFile().getAbsolutePath() + "' has the same name as another source '" + source + "'");
                throw new IllegalStateException("Source file '" + packed.getPackedFile().getAbsolutePath() + "' has the same name as another source '" + source + "'");
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error reading the soure name in the file: " + packed.getPackedFile().getAbsolutePath(), e);
            throw new IllegalStateException("Error reading the soure name in the file: " + packed.getPackedFile().getAbsolutePath(), e);
        }
    }

    /**
   * Save all of the settings referenced in this file. InitTool's, CharTool's, and shared PropertySettings all get saved.
   * 
   * @param settings Settings begin saved
   * @param database Database containing tables being saved.
   */
    public void savePropertySettings(PropertySettings settings, String database) {
        saveDatabases(database);
        setPropertyDescriptors(settings.getCustomPropertySet());
        setNamedScripts(settings.getNamedScripts());
        setMTPropertyMap(settings.getMTPropertyMap());
        setPrintFiles(settings.getPrintFiles());
    }

    /** @return Getter for validating */
    public boolean isValidating() {
        return validating;
    }

    /** @param aValidating Setter for validating */
    public void setValidating(boolean aValidating) {
        validating = aValidating;
    }

    /**
   * The number of times the progress count is incremented for installation of the game settings.
   * 
   * @return The number of increments.
   */
    protected static int installPropertySettingsProgressCount() {
        return 9;
    }

    /**
   * The number of times the progress count is incremented for loading of the game settings.
   * 
   * @return The number of increments.
   */
    protected static int loadPropertySettingsProgressCount() {
        return 11;
    }

    /**
   * The number of times the progress count is incremented for installation of a single game source file.
   * 
   * @return The number of increments.
   */
    protected static int installGameSourceProgressCount() {
        return 5;
    }

    /**
   * The number of times the progress count is incremented for loading of a single game source file.
   * 
   * @return The number of increments.
   */
    protected static int loadGameSourceProgressCount() {
        return 8;
    }
}
