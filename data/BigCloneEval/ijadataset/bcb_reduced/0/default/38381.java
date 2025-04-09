import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.xmlbeans.Filer;
import org.apache.xmlbeans.ResourceLoader;
import org.apache.xmlbeans.SchemaCodePrinter;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.SystemProperties;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.common.IOUtil;
import org.apache.xmlbeans.impl.common.JarHelper;
import org.apache.xmlbeans.impl.common.ResolverUtil;
import org.apache.xmlbeans.impl.common.XmlErrorPrinter;
import org.apache.xmlbeans.impl.common.XmlErrorWatcher;
import org.apache.xmlbeans.impl.config.BindingConfigImpl;
import org.apache.xmlbeans.impl.schema.PathResourceLoader;
import org.apache.xmlbeans.impl.schema.SchemaTypeLoaderImpl;
import org.apache.xmlbeans.impl.schema.SchemaTypeSystemCompiler;
import org.apache.xmlbeans.impl.schema.SchemaTypeSystemImpl;
import org.apache.xmlbeans.impl.schema.StscState;
import org.apache.xmlbeans.impl.tool.CodeGenUtil;
import org.apache.xmlbeans.impl.tool.CommandLine;
import org.apache.xmlbeans.impl.tool.Extension;
import org.apache.xmlbeans.impl.tool.SchemaCodeGenerator;
import org.apache.xmlbeans.impl.tool.SchemaCompilerExtension;
import org.apache.xmlbeans.impl.util.FilerImpl;
import org.apache.xmlbeans.impl.values.XmlListImpl;
import org.apache.xmlbeans.impl.xb.xmlconfig.ConfigDocument;
import org.apache.xmlbeans.impl.xb.xmlconfig.Extensionconfig;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.xml.sax.EntityResolver;
import repackage.Repackager;

public class MainSchemaCompiler {

    public static void main(String[] args) {
        String[] a = new String[] { "-src", "src/java", "-javasource", "1.4", "-verbose", "-out", "lib/xml-pardo.jar", "src/java/pardoconfig.xsdconfig", "src/java/application-schema.xsd" };
        doSchemaCompiler(a);
        a = new String[] { "-src", "src/java", "-javasource", "1.4", "-verbose", "-out", "lib/xml-dao-pardo.jar", "src/java/queriesconfig.xsdconfig", "src/java/org/skins/dao2/config/etc/queries2.xsd" };
        doSchemaCompiler(a);
    }

    public static void printUsage() {
        System.out.println("Compiles a schema into XML Bean classes and metadata.");
        System.out.println("Usage: scomp [opts] [dirs]* [schema.xsd]* [service.wsdl]* [config.xsdconfig]*");
        System.out.println("Options include:");
        System.out.println("    -cp [a;b;c] - classpath");
        System.out.println("    -d [dir] - target binary directory for .class and .xsb files");
        System.out.println("    -src [dir] - target directory for generated .java files");
        System.out.println("    -srconly - do not compile .java files or jar the output.");
        System.out.println("    -out [xmltypes.jar] - the name of the output jar");
        System.out.println("    -dl - permit network downloads for imports and includes (default is off)");
        System.out.println("    -noupa - do not enforce the unique particle attribution rule");
        System.out.println("    -nopvr - do not enforce the particle valid (restriction) rule");
        System.out.println("    -noann - ignore annotations");
        System.out.println("    -novdoc - do not validate contents of <documentation>");
        System.out.println("    -noext - ignore all extension (Pre/Post and Interface) found in .xsdconfig files");
        System.out.println("    -compiler - path to external java compiler");
        System.out.println("    -javasource [version] - generate java source compatible for a Java version (1.4 or 1.5)");
        System.out.println("    -ms - initial memory for external java compiler (default '" + CodeGenUtil.DEFAULT_MEM_START + "')");
        System.out.println("    -mx - maximum memory for external java compiler (default '" + CodeGenUtil.DEFAULT_MEM_MAX + "')");
        System.out.println("    -debug - compile with debug symbols");
        System.out.println("    -quiet - print fewer informational messages");
        System.out.println("    -verbose - print more informational messages");
        System.out.println("    -version - prints version information");
        System.out.println("    -license - prints license information");
        System.out.println("    -allowmdef \"[ns] [ns] [ns]\" - ignores multiple defs in given namespaces (use ##local for no-namespace)");
        System.out.println("    -catalog [file] -  catalog file for org.apache.xml.resolver.tools.CatalogResolver. (Note: needs resolver.jar from http://xml.apache.org/commons/components/resolver/index.html)");
        System.out.println();
    }

    public static void doSchemaCompiler(String[] args) {
        if (args.length == 0) {
            printUsage();
            System.exit(0);
            return;
        }
        Set flags = new HashSet();
        flags.add("h");
        flags.add("help");
        flags.add("usage");
        flags.add("license");
        flags.add("quiet");
        flags.add("verbose");
        flags.add("version");
        flags.add("dl");
        flags.add("noupa");
        flags.add("nopvr");
        flags.add("noann");
        flags.add("novdoc");
        flags.add("noext");
        flags.add("srconly");
        flags.add("debug");
        Set opts = new HashSet();
        opts.add("out");
        opts.add("name");
        opts.add("src");
        opts.add("d");
        opts.add("cp");
        opts.add("compiler");
        opts.add("javasource");
        opts.add("jar");
        opts.add("ms");
        opts.add("mx");
        opts.add("repackage");
        opts.add("schemaCodePrinter");
        opts.add("extension");
        opts.add("extensionParms");
        opts.add("allowmdef");
        opts.add("catalog");
        CommandLine cl = new CommandLine(args, flags, opts);
        if (cl.getOpt("h") != null || cl.getOpt("help") != null || cl.getOpt("usage") != null) {
            printUsage();
            System.exit(0);
            return;
        }
        String[] badopts = cl.getBadOpts();
        if (badopts.length > 0) {
            for (int i = 0; i < badopts.length; i++) System.out.println("Unrecognized option: " + badopts[i]);
            printUsage();
            System.exit(0);
            return;
        }
        if (cl.getOpt("license") != null) {
            CommandLine.printLicense();
            System.exit(0);
            return;
        }
        if (cl.getOpt("version") != null) {
            CommandLine.printVersion();
            System.exit(0);
            return;
        }
        args = cl.args();
        boolean verbose = (cl.getOpt("verbose") != null);
        boolean quiet = (cl.getOpt("quiet") != null);
        if (verbose) quiet = false;
        if (verbose) CommandLine.printVersion();
        String outputfilename = cl.getOpt("out");
        String repackage = cl.getOpt("repackage");
        String codePrinterClass = cl.getOpt("schemaCodePrinter");
        SchemaCodePrinter codePrinter = null;
        if (codePrinterClass != null) {
            try {
                codePrinter = (SchemaCodePrinter) Class.forName(codePrinterClass).newInstance();
            } catch (Exception e) {
                System.err.println("Failed to load SchemaCodePrinter class " + codePrinterClass + "; proceeding with default printer");
            }
        }
        String name = cl.getOpt("name");
        boolean download = (cl.getOpt("dl") != null);
        boolean noUpa = (cl.getOpt("noupa") != null);
        boolean noPvr = (cl.getOpt("nopvr") != null);
        boolean noAnn = (cl.getOpt("noann") != null);
        boolean noVDoc = (cl.getOpt("novdoc") != null);
        boolean noExt = (cl.getOpt("noext") != null);
        boolean nojavac = (cl.getOpt("srconly") != null);
        boolean debug = (cl.getOpt("debug") != null);
        String allowmdef = cl.getOpt("allowmdef");
        Set mdefNamespaces = (allowmdef == null ? Collections.EMPTY_SET : new HashSet(Arrays.asList(XmlListImpl.split_list(allowmdef))));
        List extensions = new ArrayList();
        if (cl.getOpt("extension") != null) {
            try {
                Extension e = new Extension();
                e.setClassName(Class.forName(cl.getOpt("extension"), false, Thread.currentThread().getContextClassLoader()));
                extensions.add(e);
            } catch (ClassNotFoundException e) {
                System.err.println("Could not find extension class: " + cl.getOpt("extension") + "  Is it on your classpath?");
                System.exit(1);
            }
        }
        if (extensions.size() > 0) {
            if (cl.getOpt("extensionParms") != null) {
                Extension e = (Extension) extensions.get(0);
                StringTokenizer parmTokens = new StringTokenizer(cl.getOpt("extensionParms"), ";");
                while (parmTokens.hasMoreTokens()) {
                    String nvPair = parmTokens.nextToken();
                    int index = nvPair.indexOf('=');
                    if (index < 0) {
                        System.err.println("extensionParms should be name=value;name=value");
                        System.exit(1);
                    }
                    String n = nvPair.substring(0, index);
                    String v = nvPair.substring(index + 1);
                    Extension.Param param = e.createParam();
                    param.setName(n);
                    param.setValue(v);
                }
            }
        }
        String classesdir = cl.getOpt("d");
        File classes = null;
        if (classesdir != null) classes = new File(classesdir);
        String srcdir = cl.getOpt("src");
        File src = null;
        if (srcdir != null) src = new File(srcdir);
        if (nojavac && srcdir == null && classes != null) src = classes;
        File tempdir = null;
        if (src == null || classes == null) {
            try {
                tempdir = SchemaCodeGenerator.createTempDir();
            } catch (java.io.IOException e) {
                System.err.println("Error creating temp dir " + e);
                System.exit(1);
            }
        }
        File jarfile = null;
        if (outputfilename == null && classes == null && !nojavac) outputfilename = "xmltypes.jar";
        if (outputfilename != null) jarfile = new File(outputfilename);
        if (src == null) src = IOUtil.createDir(tempdir, "src");
        if (classes == null) classes = IOUtil.createDir(tempdir, "classes");
        File[] classpath = null;
        String cpString = cl.getOpt("cp");
        if (cpString != null) {
            String[] cpparts = cpString.split(File.pathSeparator);
            List cpList = new ArrayList();
            for (int i = 0; i < cpparts.length; i++) cpList.add(new File(cpparts[i]));
            classpath = (File[]) cpList.toArray(new File[cpList.size()]);
        } else {
            classpath = CodeGenUtil.systemClasspath();
        }
        String javasource = cl.getOpt("javasource");
        String compiler = cl.getOpt("compiler");
        String jar = cl.getOpt("jar");
        if (verbose && jar != null) System.out.println("The 'jar' option is no longer supported.");
        String memoryInitialSize = cl.getOpt("ms");
        String memoryMaximumSize = cl.getOpt("mx");
        File[] xsdFiles = cl.filesEndingWith(".xsd");
        File[] wsdlFiles = cl.filesEndingWith(".wsdl");
        File[] javaFiles = cl.filesEndingWith(".java");
        File[] configFiles = cl.filesEndingWith(".xsdconfig");
        URL[] urlFiles = cl.getURLs();
        if (xsdFiles.length + wsdlFiles.length + urlFiles.length == 0) {
            System.out.println("Could not find any xsd or wsdl files to process.");
            System.exit(0);
        }
        File baseDir = cl.getBaseDir();
        URI baseURI = baseDir == null ? null : baseDir.toURI();
        XmlErrorPrinter err = new XmlErrorPrinter(verbose, baseURI);
        String catString = cl.getOpt("catalog");
        Parameters params = new Parameters();
        params.setBaseDir(baseDir);
        params.setXsdFiles(xsdFiles);
        params.setWsdlFiles(wsdlFiles);
        params.setJavaFiles(javaFiles);
        params.setConfigFiles(configFiles);
        params.setUrlFiles(urlFiles);
        params.setClasspath(classpath);
        params.setOutputJar(jarfile);
        params.setName(name);
        params.setSrcDir(src);
        params.setClassesDir(classes);
        params.setCompiler(compiler);
        params.setJavaSource(javasource);
        params.setMemoryInitialSize(memoryInitialSize);
        params.setMemoryMaximumSize(memoryMaximumSize);
        params.setNojavac(nojavac);
        params.setQuiet(quiet);
        params.setVerbose(verbose);
        params.setDownload(download);
        params.setNoUpa(noUpa);
        params.setNoPvr(noPvr);
        params.setNoAnn(noAnn);
        params.setNoVDoc(noVDoc);
        params.setNoExt(noExt);
        params.setDebug(debug);
        params.setErrorListener(err);
        params.setRepackage(repackage);
        params.setExtensions(extensions);
        params.setMdefNamespaces(mdefNamespaces);
        params.setCatalogFile(catString);
        params.setSchemaCodePrinter(codePrinter);
        boolean result = compile(params);
        if (tempdir != null) SchemaCodeGenerator.tryHardToDelete(tempdir);
    }

    public static class Parameters {

        private File baseDir;

        private File[] xsdFiles;

        private File[] wsdlFiles;

        private File[] javaFiles;

        private File[] configFiles;

        private URL[] urlFiles;

        private File[] classpath;

        private File outputJar;

        private String name;

        private File srcDir;

        private File classesDir;

        private String memoryInitialSize;

        private String memoryMaximumSize;

        private String compiler;

        private String javasource;

        private boolean nojavac;

        private boolean quiet;

        private boolean verbose;

        private boolean download;

        private Collection errorListener;

        private boolean noUpa;

        private boolean noPvr;

        private boolean noAnn;

        private boolean noVDoc;

        private boolean noExt;

        private boolean debug;

        private boolean incrementalSrcGen;

        private String repackage;

        private List extensions = Collections.EMPTY_LIST;

        private Set mdefNamespaces = Collections.EMPTY_SET;

        private String catalogFile;

        private SchemaCodePrinter schemaCodePrinter;

        private EntityResolver entityResolver;

        public File getBaseDir() {
            return baseDir;
        }

        public void setBaseDir(File baseDir) {
            this.baseDir = baseDir;
        }

        public File[] getXsdFiles() {
            return xsdFiles;
        }

        public void setXsdFiles(File[] xsdFiles) {
            this.xsdFiles = xsdFiles;
        }

        public File[] getWsdlFiles() {
            return wsdlFiles;
        }

        public void setWsdlFiles(File[] wsdlFiles) {
            this.wsdlFiles = wsdlFiles;
        }

        public File[] getJavaFiles() {
            return javaFiles;
        }

        public void setJavaFiles(File[] javaFiles) {
            this.javaFiles = javaFiles;
        }

        public File[] getConfigFiles() {
            return configFiles;
        }

        public void setConfigFiles(File[] configFiles) {
            this.configFiles = configFiles;
        }

        public URL[] getUrlFiles() {
            return urlFiles;
        }

        public void setUrlFiles(URL[] urlFiles) {
            this.urlFiles = urlFiles;
        }

        public File[] getClasspath() {
            return classpath;
        }

        public void setClasspath(File[] classpath) {
            this.classpath = classpath;
        }

        public File getOutputJar() {
            return outputJar;
        }

        public void setOutputJar(File outputJar) {
            this.outputJar = outputJar;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public File getSrcDir() {
            return srcDir;
        }

        public void setSrcDir(File srcDir) {
            this.srcDir = srcDir;
        }

        public File getClassesDir() {
            return classesDir;
        }

        public void setClassesDir(File classesDir) {
            this.classesDir = classesDir;
        }

        public boolean isNojavac() {
            return nojavac;
        }

        public void setNojavac(boolean nojavac) {
            this.nojavac = nojavac;
        }

        public boolean isQuiet() {
            return quiet;
        }

        public void setQuiet(boolean quiet) {
            this.quiet = quiet;
        }

        public boolean isVerbose() {
            return verbose;
        }

        public void setVerbose(boolean verbose) {
            this.verbose = verbose;
        }

        public boolean isDownload() {
            return download;
        }

        public void setDownload(boolean download) {
            this.download = download;
        }

        public boolean isNoUpa() {
            return noUpa;
        }

        public void setNoUpa(boolean noUpa) {
            this.noUpa = noUpa;
        }

        public boolean isNoPvr() {
            return noPvr;
        }

        public void setNoPvr(boolean noPvr) {
            this.noPvr = noPvr;
        }

        public boolean isNoAnn() {
            return noAnn;
        }

        public void setNoAnn(boolean noAnn) {
            this.noAnn = noAnn;
        }

        public boolean isNoVDoc() {
            return noVDoc;
        }

        public void setNoVDoc(boolean newNoVDoc) {
            this.noVDoc = newNoVDoc;
        }

        public boolean isNoExt() {
            return noExt;
        }

        public void setNoExt(boolean newNoExt) {
            this.noExt = newNoExt;
        }

        public boolean isIncrementalSrcGen() {
            return incrementalSrcGen;
        }

        public void setIncrementalSrcGen(boolean incrSrcGen) {
            this.incrementalSrcGen = incrSrcGen;
        }

        public boolean isDebug() {
            return debug;
        }

        public void setDebug(boolean debug) {
            this.debug = debug;
        }

        public String getMemoryInitialSize() {
            return memoryInitialSize;
        }

        public void setMemoryInitialSize(String memoryInitialSize) {
            this.memoryInitialSize = memoryInitialSize;
        }

        public String getMemoryMaximumSize() {
            return memoryMaximumSize;
        }

        public void setMemoryMaximumSize(String memoryMaximumSize) {
            this.memoryMaximumSize = memoryMaximumSize;
        }

        public String getCompiler() {
            return compiler;
        }

        public void setCompiler(String compiler) {
            this.compiler = compiler;
        }

        public String getJavaSource() {
            return javasource;
        }

        public void setJavaSource(String javasource) {
            this.javasource = javasource;
        }

        /** @deprecated */
        public String getJar() {
            return null;
        }

        /** @deprecated */
        public void setJar(String jar) {
        }

        public Collection getErrorListener() {
            return errorListener;
        }

        public void setErrorListener(Collection errorListener) {
            this.errorListener = errorListener;
        }

        public String getRepackage() {
            return repackage;
        }

        public void setRepackage(String newRepackage) {
            repackage = newRepackage;
        }

        public List getExtensions() {
            return extensions;
        }

        public void setExtensions(List extensions) {
            this.extensions = extensions;
        }

        public Set getMdefNamespaces() {
            return mdefNamespaces;
        }

        public void setMdefNamespaces(Set mdefNamespaces) {
            this.mdefNamespaces = mdefNamespaces;
        }

        public String getCatalogFile() {
            return catalogFile;
        }

        public void setCatalogFile(String catalogPropFile) {
            this.catalogFile = catalogPropFile;
        }

        public SchemaCodePrinter getSchemaCodePrinter() {
            return schemaCodePrinter;
        }

        public void setSchemaCodePrinter(SchemaCodePrinter schemaCodePrinter) {
            this.schemaCodePrinter = schemaCodePrinter;
        }

        public EntityResolver getEntityResolver() {
            return entityResolver;
        }

        public void setEntityResolver(EntityResolver entityResolver) {
            this.entityResolver = entityResolver;
        }
    }

    private static SchemaTypeSystem loadTypeSystem(String name, File[] xsdFiles, File[] wsdlFiles, URL[] urlFiles, File[] configFiles, File[] javaFiles, ResourceLoader cpResourceLoader, boolean download, boolean noUpa, boolean noPvr, boolean noAnn, boolean noVDoc, boolean noExt, Set mdefNamespaces, File baseDir, Map sourcesToCopyMap, Collection outerErrorListener, File schemasDir, EntityResolver entResolver, File[] classpath, String javasource) {
        XmlErrorWatcher errorListener = new XmlErrorWatcher(outerErrorListener);
        StscState state = StscState.start();
        state.setErrorListener(errorListener);
        SchemaTypeLoader loader = XmlBeans.typeLoaderForClassLoader(SchemaDocument.class.getClassLoader());
        ArrayList scontentlist = new ArrayList();
        if (xsdFiles != null) {
            for (int i = 0; i < xsdFiles.length; i++) {
                try {
                    XmlOptions options = new XmlOptions();
                    options.setLoadLineNumbers();
                    options.setLoadMessageDigest();
                    options.setEntityResolver(entResolver);
                    XmlObject schemadoc = loader.parse(xsdFiles[i], null, options);
                    if (!(schemadoc instanceof SchemaDocument)) {
                        StscState.addError(errorListener, XmlErrorCodes.INVALID_DOCUMENT_TYPE, new Object[] { xsdFiles[i], "schema" }, schemadoc);
                    } else {
                        addSchema(xsdFiles[i].toString(), (SchemaDocument) schemadoc, errorListener, noVDoc, scontentlist);
                    }
                } catch (XmlException e) {
                    errorListener.add(e.getError());
                } catch (Exception e) {
                    StscState.addError(errorListener, XmlErrorCodes.CANNOT_LOAD_FILE, new Object[] { "xsd", xsdFiles[i], e.getMessage() }, xsdFiles[i]);
                }
            }
        }
        if (wsdlFiles != null) {
            for (int i = 0; i < wsdlFiles.length; i++) {
                try {
                    XmlOptions options = new XmlOptions();
                    options.setLoadLineNumbers();
                    options.setLoadSubstituteNamespaces(Collections.singletonMap("http://schemas.xmlsoap.org/wsdl/", "http://www.apache.org/internal/xmlbeans/wsdlsubst"));
                    options.setEntityResolver(entResolver);
                    XmlObject wsdldoc = loader.parse(wsdlFiles[i], null, options);
                    if (!(wsdldoc instanceof org.apache.xmlbeans.impl.xb.substwsdl.DefinitionsDocument)) StscState.addError(errorListener, XmlErrorCodes.INVALID_DOCUMENT_TYPE, new Object[] { wsdlFiles[i], "wsdl" }, wsdldoc); else {
                        addWsdlSchemas(wsdlFiles[i].toString(), (org.apache.xmlbeans.impl.xb.substwsdl.DefinitionsDocument) wsdldoc, errorListener, noVDoc, scontentlist);
                    }
                } catch (XmlException e) {
                    errorListener.add(e.getError());
                } catch (Exception e) {
                    StscState.addError(errorListener, XmlErrorCodes.CANNOT_LOAD_FILE, new Object[] { "wsdl", wsdlFiles[i], e.getMessage() }, wsdlFiles[i]);
                }
            }
        }
        if (urlFiles != null) {
            for (int i = 0; i < urlFiles.length; i++) {
                try {
                    XmlOptions options = new XmlOptions();
                    options.setLoadLineNumbers();
                    options.setLoadSubstituteNamespaces(Collections.singletonMap("http://schemas.xmlsoap.org/wsdl/", "http://www.apache.org/internal/xmlbeans/wsdlsubst"));
                    options.setEntityResolver(entResolver);
                    XmlObject urldoc = loader.parse(urlFiles[i], null, options);
                    if ((urldoc instanceof org.apache.xmlbeans.impl.xb.substwsdl.DefinitionsDocument)) {
                        addWsdlSchemas(urlFiles[i].toString(), (org.apache.xmlbeans.impl.xb.substwsdl.DefinitionsDocument) urldoc, errorListener, noVDoc, scontentlist);
                    } else if ((urldoc instanceof SchemaDocument)) {
                        addSchema(urlFiles[i].toString(), (SchemaDocument) urldoc, errorListener, noVDoc, scontentlist);
                    } else {
                        StscState.addError(errorListener, XmlErrorCodes.INVALID_DOCUMENT_TYPE, new Object[] { urlFiles[i], "wsdl or schema" }, urldoc);
                    }
                } catch (XmlException e) {
                    errorListener.add(e.getError());
                } catch (Exception e) {
                    StscState.addError(errorListener, XmlErrorCodes.CANNOT_LOAD_FILE, new Object[] { "url", urlFiles[i], e.getMessage() }, urlFiles[i]);
                }
            }
        }
        SchemaDocument.Schema[] sdocs = (SchemaDocument.Schema[]) scontentlist.toArray(new SchemaDocument.Schema[scontentlist.size()]);
        ArrayList cdoclist = new ArrayList();
        if (configFiles != null) {
            if (noExt) System.out.println("Pre/Post and Interface extensions will be ignored.");
            for (int i = 0; i < configFiles.length; i++) {
                try {
                    XmlOptions options = new XmlOptions();
                    options.put(XmlOptions.LOAD_LINE_NUMBERS);
                    options.setEntityResolver(entResolver);
                    options.setLoadSubstituteNamespaces(MAP_COMPATIBILITY_CONFIG_URIS);
                    XmlObject configdoc = loader.parse(configFiles[i], null, options);
                    if (!(configdoc instanceof ConfigDocument)) StscState.addError(errorListener, XmlErrorCodes.INVALID_DOCUMENT_TYPE, new Object[] { configFiles[i], "xsd config" }, configdoc); else {
                        StscState.addInfo(errorListener, "Loading config file " + configFiles[i]);
                        if (configdoc.validate(new XmlOptions().setErrorListener(errorListener))) {
                            ConfigDocument.Config config = ((ConfigDocument) configdoc).getConfig();
                            cdoclist.add(config);
                            if (noExt) {
                                config.setExtensionArray(new Extensionconfig[] {});
                            }
                        }
                    }
                } catch (XmlException e) {
                    errorListener.add(e.getError());
                } catch (Exception e) {
                    StscState.addError(errorListener, XmlErrorCodes.CANNOT_LOAD_FILE, new Object[] { "xsd config", configFiles[i], e.getMessage() }, configFiles[i]);
                }
            }
        }
        ConfigDocument.Config[] cdocs = (ConfigDocument.Config[]) cdoclist.toArray(new ConfigDocument.Config[cdoclist.size()]);
        SchemaTypeLoader linkTo = SchemaTypeLoaderImpl.build(null, cpResourceLoader, null);
        URI baseURI = null;
        if (baseDir != null) baseURI = baseDir.toURI();
        XmlOptions opts = new XmlOptions();
        if (download) opts.setCompileDownloadUrls();
        if (noUpa) opts.setCompileNoUpaRule();
        if (noPvr) opts.setCompileNoPvrRule();
        if (noAnn) opts.setCompileNoAnnotations();
        if (mdefNamespaces != null) opts.setCompileMdefNamespaces(mdefNamespaces);
        opts.setCompileNoValidation();
        opts.setEntityResolver(entResolver);
        if (javasource != null) opts.setGenerateJavaVersion(javasource);
        SchemaTypeSystemCompiler.Parameters params = new SchemaTypeSystemCompiler.Parameters();
        params.setName(name);
        params.setSchemas(sdocs);
        params.setConfig(BindingConfigImpl.forConfigDocuments(cdocs, javaFiles, classpath));
        params.setLinkTo(linkTo);
        params.setOptions(opts);
        params.setErrorListener(errorListener);
        params.setJavaize(true);
        params.setBaseURI(baseURI);
        params.setSourcesToCopyMap(sourcesToCopyMap);
        params.setSchemasDir(schemasDir);
        return SchemaTypeSystemCompiler.compile(params);
    }

    private static void addSchema(String name, SchemaDocument schemadoc, XmlErrorWatcher errorListener, boolean noVDoc, List scontentlist) {
        StscState.addInfo(errorListener, "Loading schema file " + name);
        XmlOptions opts = new XmlOptions().setErrorListener(errorListener);
        if (noVDoc) opts.setValidateTreatLaxAsSkip();
        if (schemadoc.validate(opts)) scontentlist.add((schemadoc).getSchema());
    }

    private static void addWsdlSchemas(String name, org.apache.xmlbeans.impl.xb.substwsdl.DefinitionsDocument wsdldoc, XmlErrorWatcher errorListener, boolean noVDoc, List scontentlist) {
        if (wsdlContainsEncoded(wsdldoc)) StscState.addWarning(errorListener, "The WSDL " + name + " uses SOAP encoding. SOAP encoding is not compatible with literal XML Schema.", XmlErrorCodes.GENERIC_ERROR, wsdldoc);
        StscState.addInfo(errorListener, "Loading wsdl file " + name);
        XmlOptions opts = new XmlOptions().setErrorListener(errorListener);
        if (noVDoc) opts.setValidateTreatLaxAsSkip();
        XmlObject[] types = wsdldoc.getDefinitions().getTypesArray();
        int count = 0;
        for (int j = 0; j < types.length; j++) {
            XmlObject[] schemas = types[j].selectPath("declare namespace xs=\"http://www.w3.org/2001/XMLSchema\" xs:schema");
            if (schemas.length == 0) {
                StscState.addWarning(errorListener, "The WSDL " + name + " did not have any schema documents in namespace 'http://www.w3.org/2001/XMLSchema'", XmlErrorCodes.GENERIC_ERROR, wsdldoc);
                continue;
            }
            for (int k = 0; k < schemas.length; k++) {
                if (schemas[k] instanceof SchemaDocument.Schema && schemas[k].validate(opts)) {
                    count++;
                    scontentlist.add(schemas[k]);
                }
            }
        }
        StscState.addInfo(errorListener, "Processing " + count + " schema(s) in " + name);
    }

    public static boolean compile(Parameters params) {
        File baseDir = params.getBaseDir();
        File[] xsdFiles = params.getXsdFiles();
        File[] wsdlFiles = params.getWsdlFiles();
        URL[] urlFiles = params.getUrlFiles();
        File[] javaFiles = params.getJavaFiles();
        File[] configFiles = params.getConfigFiles();
        File[] classpath = params.getClasspath();
        File outputJar = params.getOutputJar();
        String name = params.getName();
        File srcDir = params.getSrcDir();
        File classesDir = params.getClassesDir();
        String compiler = params.getCompiler();
        String javasource = params.getJavaSource();
        String memoryInitialSize = params.getMemoryInitialSize();
        String memoryMaximumSize = params.getMemoryMaximumSize();
        boolean nojavac = params.isNojavac();
        boolean debug = params.isDebug();
        boolean verbose = params.isVerbose();
        boolean quiet = params.isQuiet();
        boolean download = params.isDownload();
        boolean noUpa = params.isNoUpa();
        boolean noPvr = params.isNoPvr();
        boolean noAnn = params.isNoAnn();
        boolean noVDoc = params.isNoVDoc();
        boolean noExt = params.isNoExt();
        boolean incrSrcGen = params.isIncrementalSrcGen();
        Collection outerErrorListener = params.getErrorListener();
        String repackage = params.getRepackage();
        if (repackage != null) {
            SchemaTypeLoaderImpl.METADATA_PACKAGE_LOAD = SchemaTypeSystemImpl.METADATA_PACKAGE_GEN;
            String stsPackage = SchemaTypeSystem.class.getPackage().getName();
            Repackager repackager = new Repackager(repackage);
            SchemaTypeSystemImpl.METADATA_PACKAGE_GEN = repackager.repackage(new StringBuffer(stsPackage)).toString().replace('.', '_');
            System.out.println("\n\n\n" + stsPackage + ".SchemaCompiler  Metadata LOAD:" + SchemaTypeLoaderImpl.METADATA_PACKAGE_LOAD + " GEN:" + SchemaTypeSystemImpl.METADATA_PACKAGE_GEN);
        }
        SchemaCodePrinter codePrinter = params.getSchemaCodePrinter();
        List extensions = params.getExtensions();
        Set mdefNamespaces = params.getMdefNamespaces();
        EntityResolver cmdLineEntRes = params.getEntityResolver() == null ? ResolverUtil.resolverForCatalog(params.getCatalogFile()) : params.getEntityResolver();
        if (srcDir == null || classesDir == null) throw new IllegalArgumentException("src and class gen directories may not be null.");
        long start = System.currentTimeMillis();
        if (baseDir == null) baseDir = new File(SystemProperties.getProperty("user.dir"));
        ResourceLoader cpResourceLoader = null;
        Map sourcesToCopyMap = new HashMap();
        if (classpath != null) cpResourceLoader = new PathResourceLoader(classpath);
        boolean result = true;
        File schemasDir = IOUtil.createDir(classesDir, "schema" + SchemaTypeSystemImpl.METADATA_PACKAGE_GEN + "/src");
        XmlErrorWatcher errorListener = new XmlErrorWatcher(outerErrorListener);
        SchemaTypeSystem system = loadTypeSystem(name, xsdFiles, wsdlFiles, urlFiles, configFiles, javaFiles, cpResourceLoader, download, noUpa, noPvr, noAnn, noVDoc, noExt, mdefNamespaces, baseDir, sourcesToCopyMap, errorListener, schemasDir, cmdLineEntRes, classpath, javasource);
        if (errorListener.hasError()) result = false;
        long finish = System.currentTimeMillis();
        if (!quiet) System.out.println("Time to build schema type system: " + ((double) (finish - start) / 1000.0) + " seconds");
        if (result && system != null) {
            start = System.currentTimeMillis();
            Repackager repackager = (repackage == null ? null : new Repackager(repackage));
            FilerImpl filer = new FilerImpl(classesDir, srcDir, repackager, verbose, incrSrcGen);
            XmlOptions options = new XmlOptions();
            if (codePrinter != null) options.setSchemaCodePrinter(codePrinter);
            if (javasource != null) options.setGenerateJavaVersion(javasource);
            system.save(filer);
            result &= SchemaTypeSystemCompiler.generateTypes(system, filer, options);
            if (incrSrcGen) {
                SchemaCodeGenerator.deleteObsoleteFiles(srcDir, srcDir, new HashSet(filer.getSourceFiles()));
            }
            if (result) {
                finish = System.currentTimeMillis();
                if (!quiet) System.out.println("Time to generate code: " + ((double) (finish - start) / 1000.0) + " seconds");
            }
            if (result && !nojavac) {
                start = System.currentTimeMillis();
                List sourcefiles = filer.getSourceFiles();
                if (javaFiles != null) sourcefiles.addAll(java.util.Arrays.asList(javaFiles));
                if (!CodeGenUtil.externalCompile(sourcefiles, classesDir, classpath, debug, compiler, javasource, memoryInitialSize, memoryMaximumSize, quiet, verbose)) result = false;
                finish = System.currentTimeMillis();
                if (result && !params.isQuiet()) System.out.println("Time to compile code: " + ((double) (finish - start) / 1000.0) + " seconds");
                if (result && outputJar != null) {
                    try {
                        new JarHelper().jarDir(classesDir, outputJar);
                    } catch (IOException e) {
                        System.err.println("IO Error " + e);
                        result = false;
                    }
                    if (result && !params.isQuiet()) System.out.println("Compiled types to: " + outputJar);
                }
            }
        }
        if (!result && !quiet) {
            System.out.println("BUILD FAILED");
        } else {
            runExtensions(extensions, system, classesDir);
        }
        if (cpResourceLoader != null) cpResourceLoader.close();
        return result;
    }

    private static void runExtensions(List extensions, SchemaTypeSystem system, File classesDir) {
        if (extensions != null && extensions.size() > 0) {
            SchemaCompilerExtension sce = null;
            Iterator i = extensions.iterator();
            Map extensionParms = null;
            String classesDirName = null;
            try {
                classesDirName = classesDir.getCanonicalPath();
            } catch (java.io.IOException e) {
                System.out.println("WARNING: Unable to get the path for schema jar file");
                classesDirName = classesDir.getAbsolutePath();
            }
            while (i.hasNext()) {
                Extension extension = (Extension) i.next();
                try {
                    sce = (SchemaCompilerExtension) extension.getClassName().newInstance();
                } catch (InstantiationException e) {
                    System.out.println("UNABLE to instantiate schema compiler extension:" + extension.getClassName().getName());
                    System.out.println("EXTENSION Class was not run");
                    break;
                } catch (IllegalAccessException e) {
                    System.out.println("ILLEGAL ACCESS Exception when attempting to instantiate schema compiler extension: " + extension.getClassName().getName());
                    System.out.println("EXTENSION Class was not run");
                    break;
                }
                System.out.println("Running Extension: " + sce.getExtensionName());
                extensionParms = new HashMap();
                Iterator parmsi = extension.getParams().iterator();
                while (parmsi.hasNext()) {
                    Extension.Param p = (Extension.Param) parmsi.next();
                    extensionParms.put(p.getName(), p.getValue());
                }
                extensionParms.put("classesDir", classesDirName);
                sce.schemaCompilerExtension(system, extensionParms);
            }
        }
    }

    private static boolean wsdlContainsEncoded(XmlObject wsdldoc) {
        XmlObject[] useAttrs = wsdldoc.selectPath("declare namespace soap='http://schemas.xmlsoap.org/wsdl/soap/' " + ".//soap:body/@use|.//soap:header/@use|.//soap:fault/@use");
        for (int i = 0; i < useAttrs.length; i++) {
            if ("encoded".equals(((SimpleValue) useAttrs[i]).getStringValue())) return true;
        }
        return false;
    }

    private static final String CONFIG_URI = "http://xml.apache.org/xmlbeans/2004/02/xbean/config";

    private static final String COMPATIBILITY_CONFIG_URI = "http://www.bea.com/2002/09/xbean/config";

    private static final Map MAP_COMPATIBILITY_CONFIG_URIS;

    static {
        MAP_COMPATIBILITY_CONFIG_URIS = new HashMap();
        MAP_COMPATIBILITY_CONFIG_URIS.put(COMPATIBILITY_CONFIG_URI, CONFIG_URI);
    }

    public static class SchemaCodeGenerator {

        /**
		 * Saves a SchemaTypeSystem to the specified directory.
		 * 
		 * @param system
		 *            the <code>SchemaTypeSystem</code> to save
		 * @param classesDir
		 *            the destination directory for xsb's
		 * @param sourceFile
		 *            if present, the TypeSystemHolder source will be generated
		 *            in this file for subsequent compilation, if null then the
		 *            source will be generated in a temp directory and then
		 *            compiled to the destination dir
		 * @param repackager
		 *            the repackager to use when generating the holder class
		 * @param options
		 *            options. Can be null
		 * @deprecated Use {@link SchemaTypeSystem.save()} instead.
		 */
        public static void saveTypeSystem(SchemaTypeSystem system, File classesDir, File sourceFile, Repackager repackager, XmlOptions options) throws IOException {
            Filer filer = new FilerImpl(classesDir, null, repackager, false, false);
            system.save(filer);
        }

        static void deleteObsoleteFiles(File rootDir, File srcDir, Set seenFiles) {
            if (!(rootDir.isDirectory() && srcDir.isDirectory())) throw new IllegalArgumentException();
            String absolutePath = srcDir.getAbsolutePath();
            if (absolutePath.length() <= 5) return;
            if (absolutePath.startsWith("/home/") && (absolutePath.indexOf("/", 6) >= absolutePath.length() - 1 || absolutePath.indexOf("/", 6) < 0)) return;
            File[] files = srcDir.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) deleteObsoleteFiles(rootDir, files[i], seenFiles); else if (seenFiles.contains(files[i])) ; else {
                    deleteXmlBeansFile(files[i]);
                    deleteDirRecursively(rootDir, files[i].getParentFile());
                }
            }
        }

        private static void deleteXmlBeansFile(File file) {
            if (file.getName().endsWith(".java")) file.delete();
        }

        private static void deleteDirRecursively(File root, File dir) {
            String[] list = dir.list();
            while (list != null && list.length == 0 && !dir.equals(root)) {
                dir.delete();
                dir = dir.getParentFile();
                list = dir.list();
            }
        }

        protected static File createTempDir() throws IOException {
            try {
                File tmpDirFile = new File(SystemProperties.getProperty("java.io.tmpdir"));
                tmpDirFile.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
            File tmpFile = File.createTempFile("xbean", null);
            String path = tmpFile.getAbsolutePath();
            if (!path.endsWith(".tmp")) throw new IOException("Error: createTempFile did not create a file ending with .tmp");
            path = path.substring(0, path.length() - 4);
            File tmpSrcDir = null;
            for (int count = 0; count < 100; count++) {
                String name = path + ".d" + (count == 0 ? "" : Integer.toString(count++));
                tmpSrcDir = new File(name);
                if (!tmpSrcDir.exists()) {
                    boolean created = tmpSrcDir.mkdirs();
                    break;
                }
            }
            tmpFile.deleteOnExit();
            return tmpSrcDir;
        }

        protected static void tryHardToDelete(File dir) {
            tryToDelete(dir);
            if (dir.exists()) tryToDeleteLater(dir);
        }

        private static void tryToDelete(File dir) {
            if (dir.exists()) {
                if (dir.isDirectory()) {
                    String[] list = dir.list();
                    if (list != null) for (int i = 0; i < list.length; i++) tryToDelete(new File(dir, list[i]));
                }
                if (!dir.delete()) return;
            }
        }

        private static Set deleteFileQueue = new HashSet();

        private static int triesRemaining = 0;

        private static boolean tryNowThatItsLater() {
            List files;
            synchronized (deleteFileQueue) {
                files = new ArrayList(deleteFileQueue);
                deleteFileQueue.clear();
            }
            List retry = new ArrayList();
            for (Iterator i = files.iterator(); i.hasNext(); ) {
                File file = (File) i.next();
                tryToDelete(file);
                if (file.exists()) retry.add(file);
            }
            synchronized (deleteFileQueue) {
                if (triesRemaining > 0) triesRemaining -= 1;
                if (triesRemaining <= 0 || retry.size() == 0) triesRemaining = 0; else deleteFileQueue.addAll(retry);
                return (triesRemaining <= 0);
            }
        }

        private static void giveUp() {
            synchronized (deleteFileQueue) {
                deleteFileQueue.clear();
                triesRemaining = 0;
            }
        }

        private static void tryToDeleteLater(File dir) {
            synchronized (deleteFileQueue) {
                deleteFileQueue.add(dir);
                if (triesRemaining == 0) {
                    new Thread() {

                        public void run() {
                            try {
                                for (; ; ) {
                                    if (tryNowThatItsLater()) return;
                                    Thread.sleep(1000 * 3);
                                }
                            } catch (InterruptedException e) {
                                giveUp();
                            }
                        }
                    };
                }
                if (triesRemaining < 10) triesRemaining = 10;
            }
        }
    }
}
