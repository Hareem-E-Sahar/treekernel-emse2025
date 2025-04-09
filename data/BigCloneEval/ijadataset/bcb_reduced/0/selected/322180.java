package gate.util.ant;

import gate.util.persistence.PersistenceManager;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.LogOptions;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.retrieve.RetrieveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.util.DefaultMessageLogger;
import org.apache.ivy.util.Message;
import org.apache.ivy.util.filter.Filter;
import org.apache.ivy.util.filter.FilterHelper;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Copy;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

/**
 * An ANT task that takes a CREOLE plugin and adds local copies of Ivy managed
 * dependencies. This involves copying JAR files into the plugin directory as
 * well as updating the creole.xml to substitute the IVY elements with
 * appropriate JAR elements.
 */
public class ExpandIvy extends Task {

    private XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

    private File dir, settings;

    private boolean verbose = false;

    private boolean fully = false;

    /**
   * Get the CREOLE plugin directory being processed.
   * 
   * @return the CREOLE plugin directory being processed.
   */
    public File getDir() {
        return dir;
    }

    /**
   * Set the CREOLE plugin directory to be processed.
   * 
   * @param dir
   *          the CREOLE plugin directory to be processed.
   */
    public void setDir(File dir) {
        this.dir = dir;
    }

    /**
   * Get the Ivy settings file used to control dependency resolution.
   * 
   * @return the Ivy settings file used to control dependency resolution, or
   *         null if the default settings are being used.
   */
    public File getSettings() {
        return dir;
    }

    /**
   * Specifies the settings file used to control dependency resolution.
   * 
   * @param settings
   *          the settings file used to control dependency resolution, or null
   *          to use the default settings.
   */
    public void setSettings(File settings) {
        this.settings = settings;
    }

    /**
   * If true then Ivy will spit out lots of messages while resolving
   * dependencies.
   * 
   * @return if true then Ivy will spit out lots of messages while resolving
   *         dependencies.
   */
    public boolean getVerbose() {
        return verbose;
    }

    /**
   * Controls the log level of Ivy.
   * 
   * @param verbose
   *          if true then Ivy will spit out lots of messages while resolving
   *          dependencies.
   */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
   * Should we fully remove the link to Ivy by removing the dependency XML
   * files.
   * 
   * @return if true Ivy files referenced in creole.xml will be removed after
   *         they have been processed.
   */
    public boolean getFully() {
        return fully;
    }

    /**
   * If true Ivy files referenced in creole.xml will be removed after they have
   * been processed.
   * 
   * @param fully
   *          if true Ivy files referenced in creole.xml will be removed after
   *          they have been processed.
   */
    public void setFully(boolean fully) {
        this.fully = fully;
    }

    @Override
    public void execute() throws BuildException {
        if (dir == null) throw new BuildException("Please specify a directory", getLocation());
        if (!dir.exists() || !dir.isDirectory()) throw new BuildException("Specified directory doesn't exist", getLocation());
        File creoleXml = new File(dir, "creole.xml");
        if (!creoleXml.exists()) throw new BuildException("Supplied directory isn't a CREOLE plugin");
        try {
            SAXBuilder builder = new SAXBuilder();
            Document creoleDoc = builder.build(creoleXml);
            List<Element> ivyElts = getIvyElements(creoleDoc);
            if (ivyElts.size() > 0) {
                Ivy ivy = getIvy(settings != null ? settings.toURI().toURL() : getSettingsURL(), dir);
                Filter filter = FilterHelper.getArtifactTypeFilter(new String[] { "jar" });
                ResolveOptions resolveOptions = new ResolveOptions();
                resolveOptions.setArtifactFilter(filter);
                if (!verbose) resolveOptions.setLog(LogOptions.LOG_QUIET);
                RetrieveOptions retrieveOptions = new RetrieveOptions();
                retrieveOptions.setArtifactFilter(filter);
                if (!verbose) retrieveOptions.setLog(LogOptions.LOG_QUIET);
                Copy copyTask;
                for (Element e : ivyElts) {
                    File ivyFile = getIvyFile(e, creoleXml);
                    if (!ivyFile.exists()) throw new BuildException("Referenced ivy file does not exist: " + ivyFile, getLocation());
                    Element parent = e.getParentElement();
                    parent.removeContent(e);
                    ResolveReport report = ivy.resolve(ivyFile.toURI().toURL(), resolveOptions);
                    if (report.getAllProblemMessages().size() > 0) throw new BuildException("Unable to resolve all IVY dependencies", getLocation());
                    @SuppressWarnings("unchecked") Map<ArtifactDownloadReport, Set<String>> toCopy = ivy.getRetrieveEngine().determineArtifactsToCopy(report.getModuleDescriptor().getModuleRevisionId(), ivy.getSettings().substitute(ivy.getSettings().getVariable("ivy.retrieve.pattern")), retrieveOptions);
                    for (Map.Entry<ArtifactDownloadReport, Set<String>> entry : toCopy.entrySet()) {
                        ArtifactDownloadReport dlReport = (ArtifactDownloadReport) entry.getKey();
                        for (String destPath : entry.getValue()) {
                            File destFile = new File(destPath);
                            destFile.getParentFile().mkdirs();
                            copyTask = new Copy();
                            copyTask.setProject(getProject());
                            copyTask.setLocation(getLocation());
                            copyTask.setTaskName(getTaskName());
                            copyTask.setFile(dlReport.getLocalFile());
                            copyTask.setTofile(destFile);
                            copyTask.init();
                            copyTask.perform();
                            Element jarElement = new Element("JAR").setText(PersistenceManager.getRelativePath(dir.toURI().toURL(), destFile.toURI().toURL()));
                            parent.addContent(jarElement);
                        }
                        if (fully && !ivyFile.delete()) ivyFile.deleteOnExit();
                    }
                }
                outputter.output(creoleDoc, new FileWriter(creoleXml));
            }
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    /**
   * Processes the specified creole.xml file to extract all the &lt;IVY&gt;
   * elements
   * 
   * @param creoleXML
   *          the URL of the creole.xml file to process
   * @return a list of the &lt;IVY&gt; XML elements
   */
    public static List<Element> getIvyElements(URL creoleXML) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(creoleXML);
        return getIvyElements(doc);
    }

    /**
   * Processes the specified XML document file to extract all the &lt;IVY&gt;
   * elements
   * 
   * @param doc
   *          the XML document to process
   * @return a list of the &lt;IVY&gt; XML elements
   */
    @SuppressWarnings("unchecked")
    public static List<Element> getIvyElements(Document doc) throws JDOMException {
        XPath jarXPath = XPath.newInstance("//*[translate(local-name(), 'ivy', 'IVY') = 'IVY']");
        return jarXPath.selectNodes(doc);
    }

    /**
   * Turns an &lt;IVY&gt; XML element into a File instance by resolving relative
   * to the creole.xml file.
   * 
   * @param element
   *          the &lt;IVY&gt; element to convert
   * @param creoleXML
   *          the creole.xml file to resolve relative to
   * @return a File instance pointing to the Ivy file specified by the XML
   *         element
   */
    public static File getIvyFile(Element element, File creoleXML) {
        return new File(creoleXML.getParentFile(), getIvyPath(element));
    }

    /**
   * Retrieve the path to the Ivy file as specified in the XML element. If no
   * path is given use the default of 'ivy.xml'.
   * 
   * @param element
   *          the &lt;IVY&gt; XML element to process
   * @return the path to the Ivy file as specified in the XML element, defaults
   *         to 'ivy.xml'.
   */
    public static String getIvyPath(Element element) {
        String ivyText = element.getTextTrim();
        if (ivyText == null || ivyText.equals("")) ivyText = "ivy.xml";
        return ivyText;
    }

    public static Ivy getIvy() throws ParseException, IOException {
        return getIvy(null, null);
    }

    public static Ivy getIvy(File dir) throws ParseException, IOException {
        return getIvy(null, dir);
    }

    public static Ivy getIvy(URL settings) throws ParseException, IOException {
        return getIvy(settings, null);
    }

    public static Ivy getIvy(URL settings, File dir) throws ParseException, IOException {
        IvySettings ivySettings = new IvySettings();
        if (settings != null) ivySettings.load(settings); else ivySettings.loadDefault();
        if (dir != null) ivySettings.setBaseDir(dir);
        return Ivy.newInstance(ivySettings);
    }

    /**
   * Attempts to find a custom Ivy settings file to use instead of the default
   * configuration. This looks first for a system property
   * <code>ivy.settings.file</code> and then <code>ivy.settings.url</code>. If
   * neither exist or can be converted to a valid URL then the method returns
   * null.
   * 
   * @return the URL of the settings file to use or null if one was not
   *         specified or could not be correctly converted.
   */
    public static URL getSettingsURL() {
        String val = System.getProperty("ivy.settings.file");
        if (val != null) {
            try {
                File file = new File(val);
                if (file.exists() && file.isFile() && file.canRead()) return file.toURI().toURL();
            } catch (Exception e) {
                System.err.println("Ivalid ivy.settings.file will be ignored: " + val);
            }
        }
        val = System.getProperty("ivy.settings.url");
        if (val != null) {
            try {
                return new URL(val);
            } catch (Exception e) {
                System.err.println("Ivalid ivy.settings.url will be ignored: " + val);
            }
        }
        return null;
    }

    static {
        Message.setDefaultLogger(new DefaultMessageLogger(Message.MSG_ERR));
    }
}
