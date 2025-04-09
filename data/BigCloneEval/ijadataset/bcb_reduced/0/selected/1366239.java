package org.nightlabs.nightlyconfig.eclipse;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.SAXException;

public class ConfigCreator {

    private static String ARCHIVE_FORMAT = ".zip";

    private static String PLUGINS_SUBDIR = "plugins";

    private Collection<File> eclipseInputDirs;

    private File eclipseOutputDir;

    private File eclipseOutputPluginsDir;

    private String eclipseSourceRepositoryModulePrefix;

    private String eclipseLocationPrefix;

    private String outputFile;

    /**
	 * @param eclipseInputDirs
	 * @param eclipseOutputDir
	 * @param eclipseSourceRepositoryModulePrefix Usually "eclipse/plugins/" or whatever the directory within the source-repository is.
	 */
    public ConfigCreator(String configFileName, Collection<File> eclipseInputDirs, File eclipseOutputDir, String eclipseSourceRepositoryModulePrefix, String eclipseLocationPrefix, boolean writeOnlyProvides, boolean writeOnlyExcludes, String outputFile) {
        this.configFileName = configFileName;
        this.eclipseInputDirs = eclipseInputDirs;
        this.eclipseOutputDir = eclipseOutputDir;
        this.eclipseOutputPluginsDir = new File(eclipseOutputDir, PLUGINS_SUBDIR);
        this.eclipseSourceRepositoryModulePrefix = eclipseSourceRepositoryModulePrefix;
        this.eclipseLocationPrefix = eclipseLocationPrefix;
        this.writeNightlyEclipsePlatformXML = writeOnlyProvides;
        this.writeOnlyExcludes = writeOnlyExcludes;
        this.outputFile = outputFile;
    }

    private List<Plugin> plugins = new ArrayList<Plugin>();

    private Map<String, Plugin> pluginsById = new HashMap<String, Plugin>();

    /**
	 * Contains instances of {@link String}; one item excludes one plugin.
	 */
    private Set<String> excludedPluginIDs = new HashSet<String>();

    protected static class ProvidesAllEntry {

        public ProvidesAllEntry(String pathTemplate) {
            this.pathTemplate = pathTemplate;
        }

        private String pathTemplate;

        public String getPathTemplate() {
            return pathTemplate;
        }
    }

    protected static class ProvidesEntry {

        public ProvidesEntry(String pathTemplate, String locationTemplate) {
            this.pathTemplate = pathTemplate;
            this.locationTemplate = locationTemplate;
        }

        private String pathTemplate;

        private String locationTemplate;

        public String getPathTemplate() {
            return pathTemplate;
        }

        public String getLocationTemplate() {
            return locationTemplate;
        }
    }

    protected static class DeployEntry {

        public static final String PLATFORM_ALLPLATFORMS = "allplatforms";

        private String platform;

        public DeployEntry(String platform) {
            this.platform = platform;
        }

        public String getPlatform() {
            return platform;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }
    }

    protected static class IncludeEntry {

        protected IncludeEntry() {
        }

        public IncludeEntry(XDOMParser parser, Node n) throws TransformerException {
            extractOutput = Boolean.parseBoolean(XDOMParser.getAttributeValue(n, "extractOutput", "false"));
            NodeIterator niP = parser.getXPathAPI().selectNodeIterator(n, "provides");
            Node nP = niP.nextNode();
            while (nP != null) {
                String pathTemplate = XDOMParser.getAttributeValue(nP, "path-template");
                String locationTemplate = XDOMParser.getAttributeValue(nP, "location-template");
                providesEntries.add(new ProvidesEntry(pathTemplate, locationTemplate));
                nP = niP.nextNode();
            }
            niP.detach();
            NodeIterator niPA = parser.getXPathAPI().selectNodeIterator(n, "provides-all");
            Node nPA = niPA.nextNode();
            while (nPA != null) {
                String pathTemplate = XDOMParser.getAttributeValue(nPA, "path-template");
                providesAllEntries.add(new ProvidesAllEntry(pathTemplate));
                nPA = niPA.nextNode();
            }
            niPA.detach();
            niPA = parser.getXPathAPI().selectNodeIterator(n, "deploy");
            nPA = niPA.nextNode();
            while (nPA != null) {
                String platform = XDOMParser.getAttributeValue(nPA, "platform", DeployEntry.PLATFORM_ALLPLATFORMS);
                deployEntries.add(new DeployEntry(platform));
                nPA = niPA.nextNode();
            }
            niPA.detach();
            niPA = parser.getXPathAPI().selectNodeIterator(n, "product");
            nPA = niPA.nextNode();
            while (nPA != null) {
                String productName = XDOMParser.getAttributeValue(nPA, "name", DeployEntry.PLATFORM_ALLPLATFORMS);
                productNames.add(productName);
                nPA = niPA.nextNode();
            }
            niPA.detach();
        }

        private List<ProvidesAllEntry> providesAllEntries = new LinkedList<ProvidesAllEntry>();

        private List<ProvidesEntry> providesEntries = new LinkedList<ProvidesEntry>();

        private List<DeployEntry> deployEntries = new LinkedList<DeployEntry>();

        private Set<String> productNames = new HashSet<String>();

        private boolean extractOutput = false;

        public List<ProvidesAllEntry> getProvidesAllEntries() {
            return providesAllEntries;
        }

        public List<ProvidesEntry> getProvidesEntries() {
            return providesEntries;
        }

        public List<DeployEntry> getDeployEntries() {
            return deployEntries;
        }

        public Set<String> getProductNames() {
            return productNames;
        }

        public boolean isExtractOutput() {
            return extractOutput;
        }

        public void setExtractOutput(boolean extractOutput) {
            this.extractOutput = extractOutput;
        }
    }

    protected static class IncludeAllEntry extends IncludeEntry {

        public IncludeAllEntry(XDOMParser parser, Node n) throws TransformerException {
            super(parser, n);
            patternString = XDOMParser.getAttributeValue(n, "pattern");
        }

        private String patternString;

        private Pattern pattern = null;

        public String getPatternString() {
            return patternString;
        }

        public void setPatternString(String patternString) {
            this.patternString = patternString;
            this.pattern = null;
        }

        public Pattern getPattern() {
            if (pattern == null) pattern = Pattern.compile(patternString);
            return pattern;
        }
    }

    protected static class JavadocEntry {

        public JavadocEntry(XDOMParser parser, Node n) throws TransformerException {
            patternString = XDOMParser.getAttributeValue(n, "pattern");
            href = XDOMParser.getAttributeValue(n, "href");
        }

        private String patternString;

        private Pattern pattern = null;

        private String href;

        public String getPatternString() {
            return patternString;
        }

        public void setPatternString(String patternString) {
            this.patternString = patternString;
            this.pattern = null;
        }

        public Pattern getPattern() {
            if (pattern == null) pattern = Pattern.compile(patternString);
            return pattern;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }
    }

    /**
	 * key: String pluginID<br/>
	 * value: IncludeEntry includeEntry
	 */
    private Map<String, IncludeEntry> includeEntries = new HashMap<String, IncludeEntry>();

    private List<IncludeAllEntry> includeAllEntries = new LinkedList<IncludeAllEntry>();

    private List<JavadocEntry> javadocEntries = new LinkedList<JavadocEntry>();

    /**
	 * Contains instances of {@link Pattern}; each item is a regular expression and every plugin-id that matches
	 * will cause the plugin to be excluded.
	 */
    private Set<Pattern> excludedPluginIDPatterns = new HashSet<Pattern>();

    private String configFileName;

    protected void loadConfig() throws SAXException, IOException, TransformerException {
        excludedPluginIDs.clear();
        excludedPluginIDPatterns.clear();
        includeEntries.clear();
        includeAllEntries.clear();
        javadocEntries.clear();
        FileInputStream in = new FileInputStream(configFileName);
        XDOMParser parser = new XDOMParser(in, configFileName);
        NodeIterator ni = parser.getXPathAPI().selectNodeIterator(parser.getDocument(), "//config/exclude");
        Node n = ni.nextNode();
        while (n != null) {
            String id = XDOMParser.getAttributeValue(n, "id");
            excludedPluginIDs.add(id);
            n = ni.nextNode();
        }
        ni.detach();
        ni = parser.getXPathAPI().selectNodeIterator(parser.getDocument(), "//config/include");
        n = ni.nextNode();
        while (n != null) {
            String id = XDOMParser.getAttributeValue(n, "id");
            IncludeEntry ie = new IncludeEntry(parser, n);
            includeEntries.put(id, ie);
            n = ni.nextNode();
        }
        ni.detach();
        ni = parser.getXPathAPI().selectNodeIterator(parser.getDocument(), "//config/include-all");
        n = ni.nextNode();
        while (n != null) {
            IncludeAllEntry iae = new IncludeAllEntry(parser, n);
            includeAllEntries.add(iae);
            n = ni.nextNode();
        }
        ni.detach();
        ni = parser.getXPathAPI().selectNodeIterator(parser.getDocument(), "//config/exclude-all");
        n = ni.nextNode();
        while (n != null) {
            String pattern = XDOMParser.getAttributeValue(n, "pattern");
            excludedPluginIDPatterns.add(Pattern.compile(pattern));
            n = ni.nextNode();
        }
        ni = parser.getXPathAPI().selectNodeIterator(parser.getDocument(), "//config/javadoc");
        n = ni.nextNode();
        while (n != null) {
            JavadocEntry je = new JavadocEntry(parser, n);
            javadocEntries.add(je);
            n = ni.nextNode();
        }
        ni.detach();
        in.close();
    }

    protected void scanPlugins() throws IOException, SAXException, TransformerException {
        plugins.clear();
        pluginsById.clear();
        for (File eclipseInputDir : eclipseInputDirs) {
            File eclipseInputPluginsDir = new File(eclipseInputDir, PLUGINS_SUBDIR);
            if (!eclipseInputPluginsDir.exists()) throw new FileNotFoundException(eclipseInputPluginsDir.getAbsolutePath());
            File[] pluginFiles = eclipseInputPluginsDir.listFiles();
            for (int i = 0; i < pluginFiles.length; i++) {
                File pluginFile = pluginFiles[i];
                Plugin plugin = new Plugin(eclipseInputDir, pluginFile);
                String pluginID = plugin.getId();
                if (pluginsById.containsKey(pluginID)) continue;
                if (excludedPluginIDs.contains(pluginID)) continue;
                boolean exclude = false;
                for (Pattern pattern : excludedPluginIDPatterns) {
                    if (pattern.matcher(pluginID).matches()) {
                        exclude = true;
                        break;
                    }
                }
                if (exclude) continue;
                if (!includeEntries.containsKey(pluginID)) {
                    for (IncludeAllEntry iae : includeAllEntries) {
                        if (iae.getPattern().matcher(pluginID).matches()) {
                            includeEntries.put(pluginID, iae);
                            break;
                        }
                    }
                }
                plugins.add(plugin);
                pluginsById.put(pluginID, plugin);
            }
        }
    }

    /**
	 * This method is copied from project NightLabsBase, class org.nightlabs.util.Utils
	 */
    private static boolean deleteDirectoryRecursively(File dir) {
        if (!dir.exists()) return true;
        if (dir.isDirectory()) {
            File[] content = dir.listFiles();
            for (int i = 0; i < content.length; ++i) {
                File f = content[i];
                if (f.isDirectory()) deleteDirectoryRecursively(f); else f.delete();
            }
        }
        return dir.delete();
    }

    protected void createOutput() throws IOException {
        if (eclipseOutputPluginsDir.exists()) deleteDirectoryRecursively(eclipseOutputPluginsDir);
        for (Plugin plugin : plugins) {
            File eclipseInputPluginsDir = new File(plugin.getEclipseInputDir(), PLUGINS_SUBDIR);
            if (!eclipseOutputPluginsDir.exists()) eclipseOutputPluginsDir.mkdirs();
            File pluginInFile = new File(eclipseInputPluginsDir, plugin.getFile().getName().replace('\\', '/'));
            if (pluginInFile.isDirectory()) {
                File pluginOutFile = new File(eclipseOutputPluginsDir, plugin.getFile().getName().replace('\\', '/') + ARCHIVE_FORMAT);
                System.out.println("directory \"" + pluginInFile.getAbsolutePath() + "\" => zip into \"" + pluginOutFile.getAbsolutePath() + "\"");
                pluginOutFile.getParentFile().mkdirs();
                FileOutputStream fo = new FileOutputStream(pluginOutFile);
                ZipOutputStream zipStream = new ZipOutputStream(fo);
                zipStream.setLevel(9);
                zipDir(zipStream, eclipseInputPluginsDir, plugin.getFile().getName().replace('\\', '/'));
                zipStream.finish();
                zipStream.close();
                IncludeEntry ie = includeEntries.get(plugin.getId());
                if (ie != null) {
                    if (ie.isExtractOutput()) {
                        extract(pluginOutFile, pluginOutFile.getParentFile());
                    }
                }
            } else if (pluginInFile.isFile()) {
                File pluginOutFile = new File(eclipseOutputPluginsDir, plugin.getFile().getName().replace('\\', '/'));
                System.out.println("file \"" + pluginInFile.getAbsolutePath() + "\" => copy to \"" + pluginOutFile.getAbsolutePath() + "\"");
                FileInputStream in = new FileInputStream(pluginInFile);
                FileOutputStream out = new FileOutputStream(pluginOutFile);
                copy(in, out);
                out.close();
                in.close();
                pluginOutFile.setLastModified(pluginInFile.lastModified());
            }
        }
    }

    protected static void extract(File archive, File destinationDir) throws ZipException, IOException {
        ZipFile zipFile = new ZipFile(archive);
        for (Enumeration<? extends ZipEntry> en = zipFile.entries(); en.hasMoreElements(); ) {
            ZipEntry zipEntry = en.nextElement();
            File f = new File(destinationDir, zipEntry.getName());
            f.getParentFile().mkdirs();
            InputStream in = zipFile.getInputStream(zipEntry);
            FileOutputStream out = new FileOutputStream(f);
            copy(in, out);
            out.close();
            in.close();
            f.setLastModified(zipEntry.getTime());
        }
    }

    protected static String addFinalSeparator(String s) {
        if (s.endsWith(File.separator)) return s; else return s + File.separatorChar;
    }

    protected void zipDir(ZipOutputStream zos, File baseDir, String relativeSubDir) throws IOException {
        File dir = new File(baseDir, relativeSubDir);
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isFile()) {
                FileInputStream in = new FileInputStream(file);
                ZipEntry entry = new ZipEntry(addFinalSeparator(relativeSubDir) + file.getName());
                entry.setTime(file.lastModified());
                entry.setSize(file.length());
                zos.putNextEntry(entry);
                copy(in, zos);
                zos.flush();
                zos.closeEntry();
                in.close();
            } else if (file.isDirectory()) {
                zipDir(zos, baseDir, addFinalSeparator(relativeSubDir) + file.getName());
            }
        }
    }

    protected static void copy(InputStream in, OutputStream out) throws IOException {
        int bytesRead;
        byte[] buf = new byte[65535];
        do {
            bytesRead = in.read(buf);
            if (bytesRead > 0) out.write(buf, 0, bytesRead);
        } while (bytesRead >= 0);
    }

    /**
	 * This method adds &lt;javadoc href="..."/&gt; entries to the nightly.xml
	 * for all {@link JavadocEntry}s that have a matching pattern. Hence,
	 * there can be many entries for every plugin.
	 */
    protected void writeJavadoc(Writer w, Plugin plugin) throws IOException {
        String pluginID = plugin.getId();
        for (JavadocEntry javadocEntry : javadocEntries) {
            if (javadocEntry.getPattern().matcher(pluginID).matches()) {
                String href = javadocEntry.getHref();
                if (href != null && !"".equals(href)) {
                    w.write("    <javadoc href=\"" + href + "\"/>\n");
                }
            }
        }
    }

    private boolean writeOnlyExcludes;

    private boolean writeNightlyEclipsePlatformXML;

    protected void writeNightlyXml() throws IOException {
        System.out.println("Writing " + outputFile + "...");
        FileOutputStream fo = new FileOutputStream(outputFile);
        try {
            BufferedOutputStream out = new BufferedOutputStream(fo);
            try {
                Writer w = new OutputStreamWriter(out);
                try {
                    if (!writeOnlyExcludes) {
                        w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                        w.write("<!DOCTYPE nightlybuild PUBLIC \"-//NightLabs//DTD NightlyBuild V 0.14//EN\" \"http://www.nightlabs.de/dtd/nightlybuild_0_14.dtd\">\n");
                        w.write("<nightlybuild>\n\n");
                    }
                    for (Plugin plugin : plugins) {
                        if (writeOnlyExcludes) {
                            w.write("  <exclude id=\"" + plugin.getId() + "\" />\n");
                            continue;
                        }
                        if (!writeOnlyExcludes) {
                            w.write("  <project name=\"" + plugin.getId() + "\">\n");
                        }
                        if (!writeOnlyExcludes) w.write("    <plugin dir=\"" + eclipseLocationPrefix + plugin.getFile().getName().replace('\\', '/') + "\"/>\n");
                        IncludeEntry includeEntry = includeEntries.get(plugin.getId());
                        if (plugin.getFile().isDirectory()) {
                            if (!writeOnlyExcludes) {
                                w.write("    <source repository=\"NightLabsLib\" module=\"" + eclipseSourceRepositoryModulePrefix + plugin.getFile().getName().replace('\\', '/') + ARCHIVE_FORMAT + "\" unpack=\"true\"/>\n");
                            }
                            boolean hasProvides = false;
                            if (includeEntry != null) {
                                for (ProvidesEntry providesEntry : includeEntry.getProvidesEntries()) {
                                    hasProvides = true;
                                    Map<String, String> variables = new HashMap<String, String>();
                                    if (!writeNightlyEclipsePlatformXML && !writeOnlyExcludes) {
                                        variables.put("${pluginBaseDir}", eclipseLocationPrefix);
                                        variables.put("${plugin}", plugin.getFile().getName().replace('\\', '/'));
                                    }
                                    if (!writeOnlyExcludes) w.write("    <provides path=\"" + buildPath(providesEntry.getPathTemplate(), variables) + "\" location=\"" + buildPath(providesEntry.getLocationTemplate(), variables) + "\"/>\n");
                                }
                            }
                            for (Iterator it2 = plugin.getProvides().iterator(); it2.hasNext(); ) {
                                hasProvides = true;
                                String providesEntry = (String) it2.next();
                                if (!writeOnlyExcludes) w.write("    <provides path=\"" + plugin.getProvidePath() + "\" location=\"" + eclipseLocationPrefix + plugin.getFile().getName().replace('\\', '/') + '/' + providesEntry.replace('\\', '/') + "\"/>\n");
                                if (includeEntry != null) {
                                    for (ProvidesAllEntry pae : includeEntry.getProvidesAllEntries()) {
                                        Map<String, String> variables = new HashMap<String, String>();
                                        variables.put("${provides.entry.full}", plugin.getFile().getName().replace('\\', '/') + '/' + providesEntry);
                                        variables.put("${provides.entry}", providesEntry);
                                        variables.put("${plugin}", plugin.getFile().getName().replace('\\', '/'));
                                        if (!writeOnlyExcludes) w.write("    <provides path=\"" + buildPath(pae.getPathTemplate(), variables) + "\" location=\"" + eclipseLocationPrefix + plugin.getFile().getName().replace('\\', '/') + '/' + providesEntry + "\"/>\n");
                                    }
                                }
                            }
                            if (!writeOnlyExcludes) {
                                if (includeEntry != null) {
                                    for (DeployEntry de : includeEntry.getDeployEntries()) {
                                        w.write("    <ant>\n");
                                        w.write("      <![CDATA[\n");
                                        w.write("        <property file=\"build.properties\"/>\n");
                                        w.write("        <property file=\"build-nightlabs.properties\"/>\n");
                                        w.write("        <property file=\"build-nightlabs-path.properties\"/>\n");
                                        w.write("        <copy preservelastmodified=\"true\" toDir=\"${rcp.deploy." + de.getPlatform() + ".dir}/plugins/" + plugin.getFile().getName().replace('\\', '/') + "\">\n");
                                        w.write("          <fileset dir=\"" + eclipseLocationPrefix + plugin.getFile().getName().replace('\\', '/') + "\" includes=\"**/*\"/>\n");
                                        w.write("        </copy>\n");
                                        w.write("      ]]>\n");
                                        w.write("    </ant>\n");
                                    }
                                } else {
                                    w.write("    <ant>\n");
                                    w.write("      <![CDATA[\n");
                                    w.write("        <property file=\"build.properties\"/>\n");
                                    w.write("        <property file=\"build-nightlabs.properties\"/>\n");
                                    w.write("        <property file=\"build-nightlabs-path.properties\"/>\n");
                                    w.write("        <copy preservelastmodified=\"true\" toDir=\"${rcp.deploy." + DeployEntry.PLATFORM_ALLPLATFORMS + ".dir}/plugins/" + plugin.getFile().getName().replace('\\', '/') + "\">\n");
                                    w.write("          <fileset dir=\"" + eclipseLocationPrefix + plugin.getFile().getName().replace('\\', '/') + "\" includes=\"**/*\"/>\n");
                                    w.write("        </copy>\n");
                                    w.write("      ]]>\n");
                                    w.write("    </ant>\n");
                                }
                            }
                            if (!hasProvides) {
                                if (!writeOnlyExcludes) w.write("    <provides path=\"" + plugin.getId() + "\" location=\"" + eclipseLocationPrefix + plugin.getFile().getName().replace('\\', '/') + "\"/>\n");
                            }
                        } else {
                            if (!writeOnlyExcludes) {
                                w.write("    <source repository=\"NightLabsLib\" module=\"" + eclipseSourceRepositoryModulePrefix + plugin.getFile().getName().replace('\\', '/') + "\" unpack=\"false\"/>\n");
                            }
                            if (!writeOnlyExcludes) w.write("    <provides path=\"" + plugin.getProvidePath() + "\" location=\"" + eclipseLocationPrefix + plugin.getFile().getName().replace('\\', '/') + "\"/>\n");
                            if (includeEntry != null) {
                                for (ProvidesAllEntry pae : includeEntry.getProvidesAllEntries()) {
                                    Map<String, String> variables = new HashMap<String, String>();
                                    variables.put("${provides.entry.full}", plugin.getFile().getName().replace('\\', '/'));
                                    variables.put("${provides.entry}", ".");
                                    variables.put("${plugin}", plugin.getFile().getName().replace('\\', '/'));
                                    if (!writeOnlyExcludes) w.write("    <provides path=\"" + buildPath(pae.getPathTemplate(), variables) + "\" location=\"" + eclipseLocationPrefix + plugin.getFile().getName().replace('\\', '/') + "\"/>\n");
                                }
                                if (!writeOnlyExcludes) {
                                    for (DeployEntry de : includeEntry.getDeployEntries()) {
                                        w.write("    <ant>\n");
                                        w.write("      <![CDATA[\n");
                                        w.write("        <property file=\"build.properties\"/>\n");
                                        w.write("        <property file=\"build-nightlabs.properties\"/>\n");
                                        w.write("        <property file=\"build-nightlabs-path.properties\"/>\n");
                                        w.write("        <copy preservelastmodified=\"true\" file=\"" + eclipseLocationPrefix + plugin.getFile().getName().replace('\\', '/') + "\" toDir=\"${rcp.deploy." + de.getPlatform() + ".dir}/plugins\" />\n");
                                        w.write("      ]]>\n");
                                        w.write("    </ant>\n");
                                    }
                                }
                            }
                        }
                        if (!writeOnlyExcludes) {
                            if (includeEntry != null) {
                                for (String productName : includeEntry.getProductNames()) {
                                    w.write("    <product name=\"" + productName + "\" />\n");
                                }
                            }
                        }
                        writeJavadoc(w, plugin);
                        if (!writeOnlyExcludes) {
                            w.write("  </project>\n\n");
                        }
                    }
                    if (!writeOnlyExcludes) {
                        w.write("</nightlybuild>\n");
                    }
                } finally {
                    w.close();
                }
            } finally {
                out.close();
            }
        } finally {
            fo.close();
        }
        System.out.println("Writing nightly.xml done!");
    }

    private static String buildPath(String pathTemplate, Map<String, String> variables) {
        String res = pathTemplate;
        for (Map.Entry<String, String> me : variables.entrySet()) {
            String regex = me.getKey().replaceAll("\\$", "\\\\\\$");
            regex = regex.replaceAll("\\.", "\\\\\\.");
            regex = regex.replaceAll("\\{", "\\\\\\{");
            regex = regex.replaceAll("\\}", "\\\\\\}");
            res = res.replaceAll(regex, me.getValue());
        }
        return res.replace('\\', '/');
    }

    public File getEclipseOutputDir() {
        return eclipseOutputDir;
    }

    public void execute() throws IOException, SAXException, TransformerException {
        loadConfig();
        scanPlugins();
        Collections.sort(plugins, new Comparator<Plugin>() {

            public int compare(Plugin p0, Plugin p1) {
                return p0.getId().compareTo(p1.getId());
            }
        });
        writeNightlyXml();
        createOutput();
        System.out.println("DONE!");
    }

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        ConfigCreatorDialog frame = new ConfigCreatorDialog();
        frame.setTitle("Config Creator");
        frame.setSize(640, 250);
        frame.setResizable(true);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
