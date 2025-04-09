package org.eclipse.update.internal.provisional;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.update.core.IIncludedFeatureReference;
import org.eclipse.update.core.IncludedFeatureReference;
import org.eclipse.update.core.model.DefaultSiteParser;
import org.eclipse.update.core.model.FeatureModel;
import org.eclipse.update.core.model.FeatureModelFactory;
import org.eclipse.update.core.model.FeatureReferenceModel;
import org.eclipse.update.core.model.ImportModel;
import org.eclipse.update.core.model.PluginEntryModel;
import org.eclipse.update.core.model.SiteModel;
import org.eclipse.update.core.model.URLEntryModel;
import org.eclipse.update.internal.core.ExtendedSiteURLFactory;
import org.eclipse.update.internal.core.Messages;
import org.eclipse.update.internal.core.UpdateManagerUtils;
import org.eclipse.update.internal.jarprocessor.JarProcessor;
import org.eclipse.update.internal.jarprocessor.JarProcessorExecutor;
import org.eclipse.update.internal.jarprocessor.Main;
import org.xml.sax.SAXException;

/**
 * The application class used to perform update site optimizations.
 * <p>
 * This class can only be referenced from <code>org.eclipse.runtime.applications</code>
 * extension point. It should not be extended or instantiated.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still
 * under development and expected to change significantly before reaching
 * stability. It is being made available at this early stage to solicit feedback
 * from pioneering adopters on the understanding that any code that uses this
 * API will almost certainly be broken (repeatedly) as the API evolves.
 * </p>
 * 
 * @since 3.2
 */
public class SiteOptimizerApplication implements IPlatformRunnable {

    public static final Integer EXIT_ERROR = new Integer(1);

    public static final String JAR_PROCESSOR = "-jarProcessor";

    public static final String DIGEST_BUILDER = "-digestBuilder";

    public static final String INPUT = "input";

    public static final String OUTPUT_DIR = "-outputDir";

    public static final String VERBOSE = "-verbose";

    public static final String JAR_PROCESSOR_PACK = "-pack";

    public static final String JAR_PROCESSOR_UNPACK = "-unpack";

    public static final String JAR_PROCESSOR_REPACK = "-repack";

    public static final String JAR_PROCESSOR_SIGN = "-sign";

    public static final String JAR_PROCESSOR_PROCESS_ALL = "-processAll";

    public static final String SITE_XML = "-siteXML";

    public static final String SITE_ATTRIBUTES_FILE = "siteAttributes.txt";

    public static final String DIGEST_OUTPUT_DIR = "-digestOutputDir";

    /**
	 * Parses the command line in the form: [-key [value]]* [inputvalue] If the
	 * last argument does not start with a "-" then it is taken as the input
	 * value and not the value for a preceding -key
	 * 
	 * @param args
	 * @return
	 */
    private Map parseCmdLine(String[] args) {
        Map cmds = new HashMap();
        for (int i = 0; i < args.length; i++) {
            if (i == args.length - 1 && !args[i].startsWith("-")) {
                cmds.put(INPUT, args[i]);
            } else {
                String key = args[i];
                String val = null;
                if (i < args.length - 2 && !args[i + 1].startsWith("-")) {
                    val = args[++i];
                }
                if (key.startsWith(SITE_XML)) {
                    val = key.substring(key.indexOf("=") + 1);
                    cmds.put(SITE_XML, val);
                } else if (key.startsWith(DIGEST_OUTPUT_DIR)) {
                    val = key.substring(key.indexOf("=") + 1);
                    cmds.put(DIGEST_OUTPUT_DIR, val);
                } else {
                    cmds.put(key, val);
                }
            }
        }
        return cmds;
    }

    private boolean runJarProcessor(Map params) {
        Main.Options options = new Main.Options();
        options.pack = params.containsKey(JAR_PROCESSOR_PACK);
        options.unpack = params.containsKey(JAR_PROCESSOR_UNPACK);
        options.repack = params.containsKey(JAR_PROCESSOR_REPACK);
        options.processAll = params.containsKey(JAR_PROCESSOR_PROCESS_ALL);
        options.verbose = params.containsKey(VERBOSE);
        options.signCommand = (String) params.get(JAR_PROCESSOR_SIGN);
        options.outputDir = (String) params.get(OUTPUT_DIR);
        String problem = null;
        String input = (String) params.get(INPUT);
        if (input == null) problem = Messages.SiteOptimizer_inputNotSpecified; else {
            File inputFile = new File(input);
            if (inputFile.exists()) options.input = inputFile; else problem = NLS.bind(Messages.SiteOptimizer_inputFileNotFound, new String[] { input });
        }
        if (options.unpack) {
            if (!JarProcessor.canPerformUnpack()) {
                problem = Messages.JarProcessor_unpackNotFound;
            } else if (options.pack || options.repack || options.signCommand != null) {
                problem = Messages.JarProcessor_noPackUnpack;
            }
        } else if ((options.pack || options.repack) && !JarProcessor.canPerformPack()) {
            problem = Messages.JarProcessor_packNotFound;
        }
        if (problem != null) {
            System.out.println(problem);
            return false;
        }
        new JarProcessorExecutor().runJarProcessor(options);
        return true;
    }

    private boolean runDigestBuilder(Map params) {
        List featureList = getFeatureList(params);
        if ((featureList == null) || featureList.isEmpty()) {
            System.out.println("no features to process");
            return false;
        }
        Map perFeatureLocales = new HashMap();
        Map availableLocales = getAvailableLocales(featureList, perFeatureLocales);
        try {
            openInputStremas(availableLocales);
        } catch (IOException e1) {
            e1.printStackTrace();
            System.out.println("Can not create file in output direcotry");
            return false;
        }
        for (int i = 0; i < featureList.size(); i++) {
            String featureJarFileName = (String) featureList.get(i);
            if (featureJarFileName.endsWith("jar")) {
                System.out.println("Processing... " + featureJarFileName);
            } else {
                System.out.println("Skipping... " + featureJarFileName);
                continue;
            }
            JarFile featureJar = null;
            try {
                featureJar = new JarFile(featureJarFileName);
            } catch (IOException e) {
                System.out.println("Problem with opening jar: " + featureJarFileName);
                e.printStackTrace();
                return false;
            }
            FeatureModelFactory fmf = new FeatureModelFactory();
            try {
                ZipEntry featureXMLEntry = featureJar.getEntry("feature.xml");
                Map featureProperties = loadProperties(featureJar, featureJarFileName, perFeatureLocales);
                FeatureModel featureModel = fmf.parseFeature(featureJar.getInputStream(featureXMLEntry));
                featureList = addFeaturesToList((String) params.get(SITE_XML), featureList, featureModel.getFeatureIncluded(), availableLocales, perFeatureLocales);
                Iterator availableLocalesIterator = availableLocales.values().iterator();
                while (availableLocalesIterator.hasNext()) {
                    ((AvailableLocale) availableLocalesIterator.next()).writeFeatureDigests(featureModel, featureProperties);
                }
            } catch (SAXException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (CoreException e) {
                e.printStackTrace();
                return false;
            }
        }
        Iterator availableLocalesIterator = availableLocales.values().iterator();
        String outputDirectory = (String) params.get(DIGEST_OUTPUT_DIR);
        outputDirectory = outputDirectory.substring(outputDirectory.indexOf("=") + 1);
        if (!outputDirectory.endsWith(File.separator)) {
            outputDirectory = outputDirectory + File.separator;
        }
        while (availableLocalesIterator.hasNext()) {
            try {
                ((AvailableLocale) availableLocalesIterator.next()).finishDigest(outputDirectory);
            } catch (IOException e) {
                System.out.println("Can not write in digest output directory: " + outputDirectory);
                e.printStackTrace();
                return false;
            }
        }
        System.out.println("Done");
        return true;
    }

    private List addFeaturesToList(String siteXML, List featureList, IIncludedFeatureReference[] iIncludedFeatureReferences, Map availableLocales, Map perFeatureLocales) throws CoreException {
        String directoryName = (new File(siteXML)).getParent();
        if (!directoryName.endsWith(File.separator)) {
            directoryName = directoryName + File.separator;
        }
        directoryName = directoryName + "features" + File.separator;
        for (int i = 0; i < iIncludedFeatureReferences.length; i++) {
            String featureURL = directoryName + iIncludedFeatureReferences[i].getVersionedIdentifier() + ".jar";
            if (!(isFeatureAlreadyInList(featureList, featureURL))) {
                try {
                    System.out.println("Extracting locales from included feature " + featureURL);
                    processLocalesInJar(availableLocales, featureURL, perFeatureLocales, true);
                } catch (IOException e) {
                    if (iIncludedFeatureReferences[i].isOptional()) continue;
                    System.out.println("Error while extracting locales from included feature " + featureURL);
                    e.printStackTrace();
                    throw new CoreException(new Status(IStatus.ERROR, "", IStatus.OK, "Error while extracting locales from included feature " + featureURL, e));
                }
                featureList.add(featureURL);
            }
        }
        return featureList;
    }

    private boolean isFeatureAlreadyInList(List featureList, String featureURL) {
        for (int i = 0; i < featureList.size(); i++) {
            String currentFeatureURL = (String) featureList.get(i);
            if (currentFeatureURL.equals(featureURL)) {
                return true;
            }
        }
        return false;
    }

    private Map loadProperties(JarFile featureJar, String featureJarFileName, Map perFeatureLocales) {
        Iterator it = ((List) perFeatureLocales.get(featureJarFileName)).iterator();
        Map result = new HashMap();
        while (it.hasNext()) {
            String propertyFileName = (String) it.next();
            ZipEntry featurePropertiesEntry = featureJar.getEntry(propertyFileName);
            Properties featureProperties = new Properties();
            if (featurePropertiesEntry != null) {
                try {
                    featureProperties.load(featureJar.getInputStream(featurePropertiesEntry));
                    String localeString = null;
                    if (propertyFileName.endsWith("feature.properties")) {
                        localeString = "";
                    } else {
                        localeString = propertyFileName.substring(8, propertyFileName.indexOf('.'));
                    }
                    result.put(localeString, featureProperties);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private void openInputStremas(Map availableLocales) throws IOException {
        Iterator locales = availableLocales.values().iterator();
        while (locales.hasNext()) {
            AvailableLocale availableLocale = (AvailableLocale) locales.next();
            availableLocale.openLocalizedOutputStream();
        }
    }

    private Map getAvailableLocales(List featureList, Map perFeatureLocales) {
        Iterator features = featureList.iterator();
        Map locales = new HashMap();
        while (features.hasNext()) {
            String feature = (String) features.next();
            try {
                System.out.println("Extracting locales from " + feature);
                processLocalesInJar(locales, feature, perFeatureLocales, false);
            } catch (IOException e) {
                System.out.println("Error while extracting locales from " + feature);
                e.printStackTrace();
                return null;
            }
        }
        return locales;
    }

    private void processLocalesInJar(Map locales, String feature, Map perFeatureLocales, boolean ignoreNewLocales) throws IOException {
        JarFile jar = new JarFile(feature);
        Enumeration files = jar.entries();
        List localesTemp = new ArrayList();
        perFeatureLocales.put(feature, localesTemp);
        while (files.hasMoreElements()) {
            ZipEntry file = (ZipEntry) files.nextElement();
            String localeString = null;
            String name = file.getName();
            if (name.startsWith("feature") && name.endsWith(".properties")) {
                localesTemp.add(name);
                if (name.endsWith("feature.properties")) {
                    localeString = "";
                } else {
                    localeString = name.substring(8, name.indexOf('.'));
                }
                if (!ignoreNewLocales && !locales.containsKey(localeString)) {
                    locales.put(localeString, new AvailableLocale(localeString));
                }
                if (locales.containsKey(localeString)) {
                    AvailableLocale currentLocale = (AvailableLocale) locales.get(localeString);
                    currentLocale.addFeatures(feature);
                }
            }
        }
    }

    private List getFeatureList(Map params) {
        if (params.containsKey(SITE_XML) && (fileExists((String) params.get(SITE_XML)))) {
            return getFeatureListFromSiteXML((String) params.get(SITE_XML));
        } else if (params.containsKey(INPUT) && isDirectory((String) params.get(SiteOptimizerApplication.INPUT))) {
            return getFeatureListFromDirectory((String) params.get(INPUT));
        }
        return null;
    }

    private boolean fileExists(String fileName) {
        File file = new File(fileName);
        if ((file != null) && file.exists()) return true;
        return false;
    }

    private List getFeatureListFromDirectory(String directoryName) {
        List featuresURLs = new ArrayList();
        File directory = new File(directoryName);
        String[] featureJarFileNames = directory.list();
        for (int i = 0; i < featureJarFileNames.length; i++) {
            featuresURLs.add(directoryName + File.separator + featureJarFileNames[i]);
        }
        return featuresURLs;
    }

    private boolean isDirectory(String fileName) {
        File directory = new File(fileName);
        if ((directory != null) && directory.exists() && directory.isDirectory()) return true;
        return false;
    }

    private List getFeatureListFromSiteXML(String siteXML) {
        List featuresURLs = new ArrayList();
        String directoryName = (new File(siteXML)).getParent();
        if (!directoryName.endsWith(File.separator)) {
            directoryName = directoryName + File.separator;
        }
        DefaultSiteParser siteParser = new DefaultSiteParser();
        siteParser.init(new ExtendedSiteURLFactory());
        try {
            SiteModel site = siteParser.parse(new FileInputStream(siteXML));
            if (site.getFeatureReferenceModels().length > 0) {
                site.getFeatureReferenceModels()[0].getURLString();
                FeatureReferenceModel[] featureReferenceModel = site.getFeatureReferenceModels();
                for (int i = 0; i < featureReferenceModel.length; i++) {
                    featuresURLs.add(directoryName + featureReferenceModel[i].getURLString());
                }
            }
            return featuresURLs;
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
            e.printStackTrace();
        } catch (SAXException e) {
            System.out.println("Parsing problem: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Problem while parsing: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public Object run(Object args) throws Exception {
        Platform.endSplash();
        if (args == null) return EXIT_ERROR;
        if (args instanceof String[]) {
            Map params = parseCmdLine((String[]) args);
            if (params.containsKey(JAR_PROCESSOR)) {
                if (!runJarProcessor(params)) return EXIT_ERROR;
            }
            if (params.containsKey(DIGEST_BUILDER)) {
                if (!runDigestBuilder(params)) return EXIT_ERROR;
            }
        }
        return IPlatformRunnable.EXIT_OK;
    }

    private class AvailableLocale {

        private String PREFIX = "temp";

        private String locale;

        private Map features = new HashMap();

        private PrintWriter localizedPrintWriter;

        private File tempDigestDirectory;

        public Map availableLocales;

        public Map getAvailableLocales() {
            return availableLocales;
        }

        public void finishDigest(String outputDirectory) throws IOException {
            localizedPrintWriter.println("</digest>");
            if (localizedPrintWriter != null) {
                localizedPrintWriter.close();
            }
            File digest = new File(outputDirectory + File.separator + "digest" + (locale == null || locale.equals("") ? "" : "_" + locale) + ".zip");
            System.out.println(digest.getAbsolutePath());
            System.out.println(digest.getName());
            if (digest.exists()) {
                digest.delete();
            }
            digest.createNewFile();
            OutputStream os = new FileOutputStream(digest);
            JarOutputStream jos = new JarOutputStream(os);
            jos.putNextEntry(new ZipEntry("digest.xml"));
            InputStream is = new FileInputStream(tempDigestDirectory);
            byte[] b = new byte[4096];
            int bytesRead = 0;
            do {
                bytesRead = is.read(b);
                if (bytesRead > 0) {
                    jos.write(b, 0, bytesRead);
                }
            } while (bytesRead > 0);
            jos.closeEntry();
            jos.close();
            os.close();
            is.close();
            tempDigestDirectory.delete();
        }

        public void setAvailableLocales(Map availableLocales) {
            this.availableLocales = availableLocales;
        }

        public AvailableLocale(String locale) {
            this.locale = locale;
        }

        public Map getFeatures() {
            return features;
        }

        public void addFeatures(String feature) {
            features.put(feature, feature);
        }

        public String getLocale() {
            return locale;
        }

        public PrintWriter getLocalizedPrintWriter() {
            return localizedPrintWriter;
        }

        public void openLocalizedOutputStream() throws IOException {
            tempDigestDirectory = File.createTempFile(PREFIX, null);
            FileOutputStream fstream = new FileOutputStream(tempDigestDirectory);
            localizedPrintWriter = new PrintWriter(new OutputStreamWriter(fstream, "UTF-8"));
            localizedPrintWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n <digest>");
            tempDigestDirectory.deleteOnExit();
        }

        public int hashCode() {
            return locale.hashCode();
        }

        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            final AvailableLocale other = (AvailableLocale) obj;
            if (locale == null) {
                if (other.locale != null) return false;
            } else if (!locale.equals(other.locale)) return false;
            return true;
        }

        public void writeFeatureDigests(FeatureModel featureModel, Map featureProperties) {
            if (this.locale.equals("")) {
                writeFeatureDigest(localizedPrintWriter, featureModel, (Properties) featureProperties.get(""));
                return;
            }
            Properties temp = new Properties();
            if (locale.indexOf("_") < 0) {
                temp = combineProperties((Properties) featureProperties.get(""), (Properties) featureProperties.get(locale), null);
                writeFeatureDigest(localizedPrintWriter, featureModel, temp);
            } else {
                temp = combineProperties((Properties) featureProperties.get(""), (Properties) featureProperties.get(locale.substring(0, locale.indexOf("_"))), (Properties) featureProperties.get(locale));
                writeFeatureDigest(localizedPrintWriter, featureModel, temp);
            }
        }

        /**
		 * @param defaults	Should be lang_country
		 * @param secondary	Should be lang
		 * @param preferred	Should be default
		 * @return
		 */
        private Properties combineProperties(Properties defaults, Properties secondary, Properties preferred) {
            return new CombinedProperties(preferred, secondary, defaults);
        }
    }

    public static void writeFeatureDigest(PrintWriter digest, FeatureModel featureModel, Properties featureProperties) {
        String label = null;
        String provider = null;
        String description = null;
        String license = null;
        String copyright = null;
        if ((featureProperties != null) && featureModel.getLabel().startsWith("%")) {
            label = featureProperties.getProperty(featureModel.getLabel().substring(1));
        } else {
            label = featureModel.getLabel();
        }
        if ((featureProperties != null) && (featureModel.getDescriptionModel() != null) && featureModel.getDescriptionModel().getAnnotation().startsWith("%")) {
            description = featureProperties.getProperty(featureModel.getDescriptionModel().getAnnotation().substring(1));
        } else {
            URLEntryModel descriptionModel = featureModel.getDescriptionModel();
            if (descriptionModel == null) description = ""; else description = descriptionModel.getAnnotation();
        }
        String pvalue = featureModel.getProvider();
        if ((featureProperties != null) && pvalue != null && pvalue.startsWith("%")) {
            provider = featureProperties.getProperty(featureModel.getProvider().substring(1));
        } else {
            provider = pvalue;
        }
        if (provider == null) provider = "";
        if (((featureProperties != null) && featureModel.getCopyrightModel() != null) && featureModel.getCopyrightModel().getAnnotation().startsWith("%")) {
            copyright = featureProperties.getProperty(featureModel.getCopyrightModel().getAnnotation().substring(1));
        } else {
            if (featureModel.getCopyrightModel() != null) {
                copyright = featureModel.getCopyrightModel().getAnnotation();
            } else {
                copyright = null;
            }
        }
        if ((featureProperties != null) && (featureModel.getLicenseModel() != null) && featureModel.getLicenseModel().getAnnotation().startsWith("%")) {
            license = featureProperties.getProperty(featureModel.getLicenseModel().getAnnotation().substring(1));
        } else {
            license = featureModel.getLicenseModel().getAnnotation();
        }
        digest.print("<feature ");
        digest.print("label=\"" + getUTF8String(label) + "\" ");
        digest.print("provider-name=\"" + provider + "\" ");
        digest.print("id=\"" + featureModel.getFeatureIdentifier() + "\" ");
        digest.print("version=\"" + featureModel.getFeatureVersion() + "\" ");
        if (featureModel.getOS() != null) digest.print("os=\"" + featureModel.getOS() + "\" ");
        if (featureModel.getNL() != null) digest.print("nl=\"" + featureModel.getNL() + "\" ");
        if (featureModel.getWS() != null) digest.print("ws=\"" + featureModel.getWS() + "\" ");
        if (featureModel.getOSArch() != null) digest.print("arch=\"" + featureModel.getOSArch() + "\" ");
        if (featureModel.isExclusive()) digest.print("exclusive=\"" + featureModel.isExclusive() + "\" ");
        if (((featureModel.getImportModels() == null) || (featureModel.getImportModels().length == 0)) && ((featureModel.getDescriptionModel() == null) || (featureModel.getDescriptionModel().getAnnotation() == null) || (featureModel.getDescriptionModel().getAnnotation().trim().length() == 0)) && ((featureModel.getCopyrightModel() == null) || (featureModel.getCopyrightModel().getAnnotation() == null) || (featureModel.getCopyrightModel().getAnnotation().trim().length() == 0)) && ((featureModel.getLicenseModel() == null) || (featureModel.getLicenseModel().getAnnotation() == null) || (featureModel.getLicenseModel().getAnnotation().trim().length() == 0)) && ((featureModel.getFeatureIncluded() == null) || (featureModel.getFeatureIncluded().length == 0))) {
            digest.println("/> ");
        } else {
            digest.println("> ");
            if (featureModel.getImportModels().length > 0) {
                digest.println("\t<requires> ");
                ImportModel[] imports = featureModel.getImportModels();
                for (int j = 0; j < imports.length; j++) {
                    digest.print("\t\t<import ");
                    if (imports[j].isFeatureImport()) {
                        digest.print("feature=\"");
                    } else {
                        digest.print("plugin=\"");
                    }
                    digest.print(imports[j].getIdentifier() + "\" ");
                    digest.print("version=\"");
                    digest.print(imports[j].getVersion() + "\" ");
                    digest.print("match=\"");
                    digest.print(imports[j].getMatchingRuleName() + "\" ");
                    if (imports[j].isPatch()) {
                        digest.print("patch=\"true\" ");
                    }
                    digest.println(" />");
                }
                digest.println("\t</requires>");
            }
            if ((featureModel.getDescriptionModel() != null) && (featureModel.getDescriptionModel().getAnnotation() != null) && (featureModel.getDescriptionModel().getAnnotation().trim().length() != 0)) {
                digest.println("\t<description>");
                digest.println("\t\t" + UpdateManagerUtils.getWritableXMLString(description));
                digest.println("\t</description>");
            }
            if (featureModel.getCopyrightModel() != null) {
                if (featureModel.getCopyrightModel().getAnnotation() != null) {
                    digest.println("\t<copyright>");
                    digest.println("\t\t" + UpdateManagerUtils.getWritableXMLString(copyright));
                    digest.println("\t</copyright>");
                }
            }
            if ((featureModel.getLicenseModel() != null) && (featureModel.getLicenseModel().getAnnotation() != null) && (featureModel.getLicenseModel().getAnnotation().trim().length() != 0)) {
                digest.println("\t<license>");
                digest.println("\t\t" + UpdateManagerUtils.getWritableXMLString(license));
                digest.println("\t</license>");
            }
            PluginEntryModel[] plugins = featureModel.getPluginEntryModels();
            if ((plugins != null) && (plugins.length != 0)) {
                for (int i = 0; i < plugins.length; i++) {
                    digest.print("\t<plugin ");
                    digest.print("id=\"" + plugins[i].getPluginIdentifier() + "\" ");
                    digest.print("version=\"" + plugins[i].getPluginVersion() + "\" ");
                    if (plugins[i].getOS() != null) digest.print("os=\"" + plugins[i].getOS() + "\" ");
                    if (plugins[i].getNL() != null) digest.print("nl=\"" + plugins[i].getNL() + "\" ");
                    if (plugins[i].getWS() != null) digest.print("ws=\"" + plugins[i].getWS() + "\" ");
                    if (plugins[i].getOSArch() != null) digest.print("arch=\"" + plugins[i].getOSArch() + "\" ");
                    if (plugins[i].getDownloadSize() > 0) digest.print("download-size=\"" + plugins[i].getDownloadSize() + "\" ");
                    if (plugins[i].getInstallSize() > 0) digest.print("install-size=\"" + plugins[i].getInstallSize() + "\" ");
                    if (!plugins[i].isUnpack()) digest.print("unpack=\"" + plugins[i].isUnpack() + "\" ");
                    digest.println("/> ");
                }
            }
            IIncludedFeatureReference[] includedFeatures = featureModel.getFeatureIncluded();
            if ((includedFeatures != null) && (includedFeatures.length != 0)) {
                for (int i = 0; i < includedFeatures.length; i++) {
                    try {
                        digest.print("\t<includes ");
                        digest.print("id=\"" + includedFeatures[i].getVersionedIdentifier().getIdentifier() + "\" ");
                        digest.print("version=\"" + includedFeatures[i].getVersionedIdentifier().getVersion() + "\" ");
                        if (includedFeatures[i].getOS() != null) digest.print("os=\"" + includedFeatures[i].getOS() + "\" ");
                        if (includedFeatures[i].getNL() != null) digest.print("nl=\"" + includedFeatures[i].getNL() + "\" ");
                        if (includedFeatures[i].getWS() != null) digest.print("ws=\"" + includedFeatures[i].getWS() + "\" ");
                        if (includedFeatures[i].getOSArch() != null) digest.print("arch=\"" + includedFeatures[i].getOSArch() + "\" ");
                        if ((includedFeatures[i] instanceof IncludedFeatureReference) && (((IncludedFeatureReference) includedFeatures[i]).getLabel() != null)) digest.print("name=\"" + includedFeatures[i].getName() + "\" ");
                        if (includedFeatures[i].isOptional()) digest.print("optional=\"true\" ");
                        digest.print("search-location=\"" + includedFeatures[i].getSearchLocation() + "\" ");
                        digest.println("/> ");
                    } catch (CoreException e) {
                        e.printStackTrace();
                    }
                }
            }
            digest.println("</feature>");
        }
    }

    private class CombinedProperties extends Properties {

        private Properties properties1;

        private Properties properties2;

        private Properties properties3;

        /**
		 * @param preferred preferred, such as lang_country
		 * @param secondary secondary, such as lang
		 * @param defaults default,
		 */
        public CombinedProperties(Properties preferred, Properties secondary, Properties defaults) {
            this.properties1 = preferred;
            this.properties2 = secondary;
            this.properties3 = defaults;
        }

        /**
		 * 
		 */
        private static final long serialVersionUID = 1L;

        public String getProperty(String key) {
            String result = null;
            if (properties3 != null && properties3.containsKey(key)) result = properties3.getProperty(key);
            if (properties2 != null && properties2.containsKey(key)) result = properties2.getProperty(key);
            if (properties1 != null && properties1.containsKey(key)) result = properties1.getProperty(key);
            return result;
        }
    }

    public static final String getUTF8String(String s) {
        if (s == null) return "";
        return UpdateManagerUtils.getWritableXMLString(s);
    }
}
