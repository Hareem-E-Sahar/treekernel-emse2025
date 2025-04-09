package middlegen.plugins.xmi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;
import middlegen.FileProducer;
import middlegen.MiddlegenException;
import middlegen.javax.JavaPlugin;

/**
 * This plugin generates an XMI document from the schema in a SQL database. <p>
 *
 * Two output formats are available:
 * <ol>
 *   <li> xmi<br>
 *   Create a single XMI file
 *   <li> poseidon<br>
 *   Create a .zuml file for use in the Poseidon UML application.
 * </ul>
 * The plugin can be invoked form within a middlegen ant task: <code>
 *          &lt;xmi fileprefix="${name}" package="${name}"  format="poseidon"
 *           destination="${build.xmi.dir}"
 *        /&gt;
 * </code> <p>
 *
 * The fileprefix parameter is used as the root of the generated file and the
 * format parameter determines both the output file format and the file suffix.
 *
 * @author <a href="mailto:mhinca@mac.com?Subject=middlegen.plugins.xmi.XmiPlugin.java">
 *      Mike Henderson</a>
 * @created November 27, 2003
 * @middlegen.plugin name="xmi"
 */
public class XmiPlugin extends JavaPlugin {

    /**
    * The fileprefix parameter passed to the plugin. This value will be used to
    * construct the output filename based upon the requested format. <p>
    *
    * For example a value of "example" with a format of "xmi" will generate a
    * file named "example.xmi" while a format value of "poseidon" will generate
    * a file in the Poseidon format named "example.zuml"
    */
    private String _fileprefix = null;

    /**
    * The format of the XMI output to be produced by the plugin. The default is
    * "xmi", plain XMI output.
    */
    private String _format = XMI_FORMAT;

    /**
    * @todo-javadoc Describe the field
    */
    private boolean _generateaccessors = false;

    /**
    * @todo-javadoc Describe the field
    */
    private HashMap _modeltables = new HashMap();

    /**
    * @todo-javadoc Describe the field
    */
    private HashMap _diagrams = new HashMap();

    /**
    * The format value used to specify that the plugin should produce a Poseidon
    * model file.
    */
    private static final String POSEIDON_FORMAT = "poseidon";

    /**
    * The format value used to specify that the plugin should produce a plain
    * XMI file.
    */
    private static final String XMI_FORMAT = "xmi";

    /**
    * @todo-javadoc Describe the field
    */
    private static org.apache.log4j.Category _log = org.apache.log4j.Category.getInstance(XmiPlugin.class.getName());

    /**
    */
    public XmiPlugin() {
        super();
    }

    /**
    * Sets the Fileprefix attribute of the XmiPlugin object
    *
    * @param value The new Fileprefix value
    */
    public void setFileprefix(String value) {
        _fileprefix = value;
    }

    /**
    * Sets the Format attribute of the XmiPlugin object
    *
    * @param value The new Format value
    */
    public void setFormat(String value) {
        _format = value;
    }

    /**
    * Sets the Generateaccessors attribute of the XmiPlugin object
    *
    * @param value The new Generateaccessors value
    */
    public void setGenerateaccessors(boolean value) {
        _generateaccessors = true;
    }

    /**
    * Gets the Filename attribute of the XmiPlugin object
    *
    * @return The Filename value
    */
    public String getFileprefix() {
        return _fileprefix;
    }

    /**
    * Gets the Generateaccessors attribute of the XmiPlugin object
    *
    * @return The Generateaccessors value
    */
    public boolean getGenerateaccessors() {
        return _generateaccessors;
    }

    /**
    * @return The Generatediagrams value
    */
    public boolean getGeneratediagrams() {
        _log.debug("  _diagrams.size() = " + _diagrams.size());
        return (_diagrams.size() > 0);
    }

    /**
    * Gets the ClassDiagrams attribute of the XmiPlugin object
    *
    * @return The ClassDiagrams value
    */
    public Collection getClassdiagrams() {
        return _diagrams.values();
    }

    /**
    * Gets the ModelTables attribute of the XmiPlugin object
    *
    * @return The ModelTables value
    */
    public Collection getModelTables() {
        return _modeltables.values();
    }

    /**
    * This method checks the plugin parameters before the output is generated to
    * ensure that the paramters are valid.
    *
    * @exception MiddlegenException An exception indicating which parameters are
    *      not valid.
    */
    public void validate() throws MiddlegenException {
        super.validate();
        if (!_format.equals(XMI_FORMAT) && !_format.equals(POSEIDON_FORMAT)) {
            throw new MiddlegenException("Unrecognized XMI format, must be one of(" + XMI_FORMAT + ", " + POSEIDON_FORMAT + ")");
        }
        if (_fileprefix == null) {
            throw new MiddlegenException("A fileprefix must be supplied");
        }
    }

    /**
    * Describe the method
    *
    * @param diagram Describe the method parameter
    * @todo-javadoc Describe the method
    * @todo-javadoc Describe the method parameter
    */
    public void addConfiguredClassdiagram(ClassdiagramElement diagram) {
        _log.debug(" XmiPlugin.addConfiguredClassdiagram(" + diagram.getName() + ")");
        _diagrams.put(diagram.getName(), diagram);
    }

    /**
    * Describe the method
    *
    * @param table Describe the method parameter
    * @todo-javadoc Describe the method
    * @todo-javadoc Describe the method parameter
    */
    public void addConfiguredTable(TableElement table) {
        _log.debug("    XmiPlugin.addConfiguredTable(" + table.getName() + ")");
        _modeltables.put(table.getName(), table);
    }

    /**
    * Describe what the method does
    *
    * @param msg Describe what the parameter does
    * @todo-javadoc Write javadocs for method
    * @todo-javadoc Write javadocs for method parameter
    */
    public void println(String msg) {
        _log.debug(msg);
    }

    /**
    * Register the file producers for this plugin
    */
    protected void registerFileProducers() {
        addConfiguredFileproducer(new FileProducer(getDestinationDir(), getXmiFilename(), getClass().getResource("xmi.vm")));
        if (_format.equals(POSEIDON_FORMAT)) {
            addConfiguredFileproducer(new FileProducer(getDestinationDir(), getProjFilename(), getClass().getResource("poseidon-proj.vm")));
        }
    }

    /**
    * This method generates the output files specified by the plugin parameters.
    *
    * @exception MiddlegenException Any exception occurring duiring the
    *      generation process.
    */
    protected void generate() throws MiddlegenException {
        if (getGeneratediagrams()) {
            calculateDiagramDimensions();
        }
        super.generate();
        if (_format.equals(POSEIDON_FORMAT)) {
            createPoseidonFile(getFileprefix() + ".zuml");
        }
    }

    /**
    * Gets the XmiFilename attribute of the XmiPlugin object
    *
    * @return The XmiFilename value
    */
    private String getXmiFilename() {
        return getFileprefix() + ".xmi";
    }

    /**
    * Gets the ProjFilename attribute of the XmiPlugin object
    *
    * @return The ProjFilename value
    */
    private String getProjFilename() {
        return getFileprefix() + ".proj";
    }

    /**
    * This method merges the .proj and .xmi files into a .zuml zip file for use
    * by the Poseidon UML editor.
    *
    * @param zipFilename The name of the Poseidon .zuml file to create.
    * @exception MiddlegenException Any exception which occurs during the IO
    *      process.
    */
    private void createPoseidonFile(String zipFilename) throws MiddlegenException {
        try {
            File dir = getDestinationDir();
            String xmiFileName = getXmiFilename();
            String projFileName = getProjFilename();
            File zipFile = new File(dir, zipFilename);
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
            ZipEntry zipEntry = new ZipEntry(projFileName);
            out.putNextEntry(zipEntry);
            File projFile = new File(dir, projFileName);
            FileInputStream in = new FileInputStream(projFile);
            writeInToOut(in, out);
            in.close();
            zipEntry = new ZipEntry(xmiFileName);
            out.putNextEntry(zipEntry);
            File xmiFile = new File(dir, xmiFileName);
            in = new FileInputStream(xmiFile);
            writeInToOut(in, out);
            in.close();
            out.close();
            projFile.delete();
        } catch (Exception ex) {
            throw new MiddlegenException(ex.getMessage());
        }
    }

    /**
    * Writes the data on the InputStream srgument to the OutputStream argument
    *
    * @param in The InputStream to read from
    * @param out The OutputStream to write to
    * @exception IOException Describe the exception
    * @todo-javadoc Write javadocs for exception
    */
    private void writeInToOut(InputStream in, OutputStream out) throws IOException {
        try {
            byte[] bytes = new byte[1024];
            int byteCount;
            while ((byteCount = in.read(bytes)) != -1) {
                out.write(bytes, 0, byteCount);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
    * Describe what the method does
    *
    * @todo-javadoc Write javadocs for method
    */
    private void calculateDiagramDimensions() {
        Iterator iterator = getClassdiagrams().iterator();
        while (iterator.hasNext()) {
            ClassdiagramElement cde = (ClassdiagramElement) iterator.next();
            cde.calculateDimensions(this);
        }
    }
}
