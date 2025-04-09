package ru.adv.util.siteconverter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import ru.adv.db.config.DBConfig;
import ru.adv.io.InputOutputException;
import ru.adv.util.Files;
import ru.adv.util.InputOutput;
import ru.adv.util.Path;
import ru.adv.util.Strings;
import ru.adv.util.XmlUtils;
import ru.adv.xml.formatter.XMLFormatter;
import ru.adv.xml.parser.IncludableParser;
import ru.adv.xml.parser.PI;
import ru.adv.xml.parser.Parser;

/**
 * 
 * Converts Mozart-2 web-sites into Mozat-3
 * @author vic
 *
 */
public class SiteConverter {

    private static final String WEB_INF_MOZART_CONFIG = "/WEB-INF/mozart.config";

    private static final String WP = "/wp";

    private static final String TEMPLATES = "/templates";

    static final String REPOSITORY_DIR = "/opt/mozart/repository";

    static final String REPOSITORY_CFG = REPOSITORY_DIR + "/config.xml";

    static final Log LOGGER = LogFactory.getLog(SiteConverter.class);

    static final Set<String> fileExtNameWithPackegeNames = new HashSet<String>(Strings.split("xml,xgi,xsl,dtd,", ","));

    private String distrWebDirPath;

    private String distrDbDirPath;

    private String port;

    private File webhostDir;

    /**
	 * Create web-host distribution file for Mozart ver.3
	 * from web-host based om Mozart ver 2
	 */
    public void makeMozart3Distr(String webhostDirPath, String outputDirPath) throws Exception {
        this.port = readLocalRepositoryPort();
        Assert.hasText(webhostDirPath);
        webhostDirPath = Path.getNormalize(webhostDirPath);
        webhostDir = new File(webhostDirPath);
        Assert.isTrue(webhostDir.exists(), webhostDirPath + " is not exist");
        createDistrDir(outputDirPath);
        List<String> databaseNames = calculateDatabaseNames(webhostDirPath);
        for (String dbName : databaseNames) {
            collectDatabseInfo(dbName);
        }
        createWebContent();
        LOGGER.info("Make tar.gz file ... ");
        String zipFileName = makeTarGzip(webhostDirPath, outputDirPath);
        LOGGER.info("Distributive file is ready: " + zipFileName);
    }

    private String makeTarGzip(String webhostDirPath, String outputDirPath) throws Exception {
        final String filename = StringUtils.getFilename(webhostDirPath);
        Assert.hasText(filename, "Cant calulate name for tar.gz file!");
        final String gzipFilePath = new File(outputDirPath + "/" + filename + ".tgz").getAbsolutePath();
        ProcessBuilder pb = new ProcessBuilder("tar", "-czf", gzipFilePath, "-C", new File(outputDirPath).getAbsolutePath(), "ROOT", "database");
        pb.redirectErrorStream(true);
        Process p = pb.start();
        InputStream in = p.getInputStream();
        int b = in.read();
        while (b != -1) {
            System.out.write(b);
            b = in.read();
        }
        p.waitFor();
        if (p.exitValue() != 0) {
            throw new RuntimeException("Can't create tar.gzip file " + gzipFilePath);
        }
        return gzipFilePath;
    }

    private String readLocalRepositoryPort() throws Exception {
        try {
            String cfg = FileCopyUtils.copyToString(new FileReader(REPOSITORY_CFG));
            return StringUtils.split(StringUtils.split(cfg, "<port>")[1], "</port>")[0].trim();
        } catch (Throwable t) {
            throw new Exception("Can't read port that repository is listening to");
        }
    }

    private void collectDatabseInfo(String databaseName) throws Exception, IOException {
        Files.createDir(this.distrDbDirPath + "/" + databaseName + "/dumps");
        Files.createDir(this.distrDbDirPath + "/" + databaseName + "/etc");
        final String dumpDstFile = this.distrDbDirPath + "/" + databaseName + "/dumps/fulldump";
        if (new File(dumpDstFile).exists()) {
            if (ask("The previous saved dump file found " + dumpDstFile + ".\n Skip action ?")) {
                return;
            }
        }
        String dumpName = databaseName + "_fulldump";
        doCommandInShelladmin(databaseName, "dump " + dumpName + " full");
        final String databasePath = REPOSITORY_DIR + "/data/" + databaseName;
        final String dumpFilePath = databasePath + "/dumps/" + dumpName;
        File dump = new File(dumpFilePath);
        Assert.isTrue(dump.exists(), "File dump " + dump + " is not found");
        LOGGER.info("Move dump file to " + dumpDstFile);
        convertDump(dumpDstFile, dump);
        doCommandInShelladmin(databaseName, "removedump " + dumpName);
        Files.copyDirectory(databasePath + "/etc", this.distrDbDirPath + "/" + databaseName + "/etc", true);
        replaceStringInFiles(new File(this.distrDbDirPath + "/" + databaseName + "/etc"));
        FileCopyUtils.copy(new File(this.distrDbDirPath + "/" + databaseName + "/etc/base_in_use.xml"), new File(this.distrDbDirPath + "/" + databaseName + "/etc/base_in_use.xml.bak"));
        setNativeSystemTriggerAttrIntoBaseConfig(new File(this.distrDbDirPath + "/" + databaseName + "/etc/base.xml"));
    }

    public static void convertDump(final String dumpDstFile, File dump) throws Exception {
        new DumpConverter().covertDump(dump, new File(dumpDstFile), new File("/tmp"));
    }

    private void setNativeSystemTriggerAttrIntoBaseConfig(File baseXml) throws InputOutputException, FileNotFoundException {
        Document doc = new Parser().parse(InputOutput.create("file://" + baseXml.getAbsolutePath()), false).getDocument();
        Element baseElement = XmlUtils.findFirstElement(doc.getDocumentElement(), "base");
        Assert.notNull(baseElement, "base Element is not found");
        baseElement.setAttribute(DBConfig.NATIVE_SYSTEM_TRIGGERS_ATTR_NAME, "yes");
        new XMLFormatter("UTF-8").format(doc, new FileOutputStream(baseXml));
    }

    private void createDistrDir(String outputDirPath) {
        File distrDir = new File(outputDirPath);
        LOGGER.info("Creating Mozart-3 site version in " + distrDir.getAbsolutePath());
        distrWebDirPath = distrDir.getAbsoluteFile() + "/ROOT";
        distrDbDirPath = distrDir.getAbsoluteFile() + "/database";
        Files.createDir(distrDir);
        Files.createDir(distrWebDirPath);
        Files.createDir(distrDbDirPath);
    }

    private List<String> calculateDatabaseNames(String hostDir) {
        List<String> result = new ArrayList<String>();
        String dbXmlPath = hostDir + "/htdocs/WEB-INF/db.xml";
        Document doc = new Parser().parse(InputOutput.create(dbXmlPath)).getDocument();
        List<Element> elements = XmlUtils.findAllElements(doc.getDocumentElement(), "database");
        Assert.isTrue(!elements.isEmpty(), "Can't find databse element");
        for (Element elem : elements) {
            String databaseName = elem.getAttribute("name");
            Assert.hasText(databaseName, "Database name is empty");
            LOGGER.info("Site database name found: " + databaseName);
            result.add(databaseName);
        }
        return result;
    }

    private String doCommandInShelladmin(String databaseName, String cmd) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/opt/mozart/lib/shelladmin.jar", "-h", "localhost", "-p", this.port, "-U", "pers", "-P", "qwe", "-d", databaseName, "-c", cmd);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        InputStream in = p.getInputStream();
        int b = in.read();
        while (b != -1) {
            System.out.write(b);
            buff.write(b);
            b = in.read();
        }
        p.waitFor();
        if (!buff.toString().trim().endsWith("ok")) {
            throw new RuntimeException("Error on create dump");
        }
        return null;
    }

    private void createWebContent() throws Exception {
        final String htdocsDirPath = this.webhostDir.getAbsoluteFile() + "/htdocs/";
        LOGGER.info("Copy " + htdocsDirPath);
        Files.copyDirectory(htdocsDirPath, distrWebDirPath, true);
        String mozartDirPath = distrWebDirPath + "/WEB-INF/mozart";
        Files.createDir(mozartDirPath);
        final String templateDirPath = this.webhostDir.getAbsoluteFile() + TEMPLATES;
        LOGGER.info("Copy " + templateDirPath);
        Files.copyDirectory(templateDirPath, mozartDirPath + TEMPLATES, true);
        Properties mozartConfig = loadMozartConfig(htdocsDirPath);
        String wpPath = mozartConfig.getProperty("prefix.wp");
        Assert.hasText(wpPath, "wp prefix is not defined");
        wpPath += "/";
        LOGGER.info("Copy wp prefix " + wpPath + " into " + mozartDirPath + WP);
        Files.copyDirectory(wpPath, mozartDirPath + WP, true);
        fixPrefixesInMozartConfig();
        fixDbXml(distrWebDirPath);
        fixSitemapXml(distrWebDirPath);
        new File(distrWebDirPath + "/WEB-INF/web.xml").renameTo(new File(distrWebDirPath + "/WEB-INF/web.xml.backup"));
        copyResourseConf("web.xml");
        copyResourseConf("captcha.xml");
        copyResourseConf("mozartContext.xml");
        copyResourseConf("mozart-servlet.xml");
        replaceStringInFiles(new File(distrWebDirPath));
        replaceStringInSecurityFilterConfigs(new File(distrWebDirPath + "/WEB-INF/auth"));
        new File(distrWebDirPath + "/WEB-INF/authfilter-config.xml").delete();
        new File(distrWebDirPath + "/WEB-INF/dbusers").delete();
    }

    private static void replaceStringInSecurityFilterConfigs(File directory) throws IOException {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.getName().endsWith(".xml")) {
                LOGGER.info("fix: " + file);
                byte[] result = FileCopyUtils.copyToByteArray(new StreamReplacer(new FileInputStream(file), "jdbc:postgresql://tandem2.adv.ru/".getBytes(), "jdbc:postgresql://localhost/".getBytes()));
                FileCopyUtils.copy(result, file);
            }
        }
    }

    private static void replaceStringInFiles(File directory) throws FileNotFoundException, IOException {
        Assert.isTrue(directory.isDirectory(), "Expected directory");
        File[] files = directory.listFiles(new FileFilter() {

            @Override
            public boolean accept(File f) {
                String filenameExtension = StringUtils.getFilenameExtension(f.getName());
                if (filenameExtension == null) {
                    filenameExtension = "";
                }
                return f.isDirectory() || fileExtNameWithPackegeNames.contains(filenameExtension);
            }
        });
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                replaceStringInFiles(file);
            } else {
                replaceOldPackageNames(file);
            }
        }
    }

    private static void replaceOldPackageNames(File file) throws FileNotFoundException, IOException {
        Assert.isTrue(file.isFile(), "Expected plain file");
        LOGGER.info("fix: " + file);
        byte[] result = FileCopyUtils.copyToByteArray(new StreamReplacer(new FileInputStream(file), "/com/adv/".getBytes(), "/ru/adv/".getBytes()));
        result = FileCopyUtils.copyToByteArray(new StreamReplacer(new ByteArrayInputStream(result), "com.adv.".getBytes(), "ru.adv.".getBytes()));
        FileCopyUtils.copy(result, file);
    }

    private void copyResourseConf(String fileName) throws IOException, FileNotFoundException {
        FileCopyUtils.copy(SiteConverter.class.getResourceAsStream("conf/" + fileName), new FileOutputStream(distrWebDirPath + "/WEB-INF/" + fileName));
    }

    private void fixPrefixesInMozartConfig() throws IOException, FileNotFoundException {
        Properties siteConfig = new Properties();
        siteConfig.load(new FileInputStream(distrWebDirPath + WEB_INF_MOZART_CONFIG));
        siteConfig.put("prefix.src", "htdocs:///WEB-INF/mozart/src");
        siteConfig.put("prefix.wp", "htdocs:///WEB-INF/mozart/wp");
        siteConfig.put("prefix.templates", "htdocs:///WEB-INF/mozart/templates");
        siteConfig.put("prefix.cfg", "htdocs:///WEB-INF/mozart/templates");
        siteConfig.store(new FileOutputStream(distrWebDirPath + WEB_INF_MOZART_CONFIG), "Mozart config");
    }

    private Properties loadMozartConfig(final String htdocsDirPath) throws IOException, FileNotFoundException {
        Properties mozartConfig;
        mozartConfig = new Properties();
        mozartConfig.load(new FileInputStream("/opt/mozart/config"));
        mozartConfig.load(new FileInputStream(htdocsDirPath + WEB_INF_MOZART_CONFIG));
        return mozartConfig;
    }

    private boolean ask(String question) throws IOException {
        System.out.print(question + "(y/n):");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String answer = reader.readLine();
            if ("y".equalsIgnoreCase(answer)) {
                return true;
            } else if ("n".equalsIgnoreCase(answer)) {
                return false;
            }
            System.out.print("(y/n):");
        }
    }

    /**
	 * выделим фильтры в отдельный файл и подключим их через префикс htdocs://
	 * @param distrWebDirPath
	 * @throws Exception 
	 */
    private void fixDbXml(String distrWebDirPath) throws Exception {
        String dbXmlPath = distrWebDirPath + "/WEB-INF/db.xml";
        Document doc = new Parser().parse(InputOutput.create(dbXmlPath)).getDocument();
        List<Element> dbElemList = XmlUtils.findAllElements(doc.getDocumentElement(), "database");
        Assert.isTrue(!dbElemList.isEmpty(), "Can't find databse element");
        int idx = 0;
        for (Element dbElem : dbElemList) {
            Element filtersElem = XmlUtils.findFirstElement(dbElem, "filters");
            Assert.isTrue(filtersElem != null, "Can't find filters element");
            Node[] filters = XmlUtils.childrenArray(filtersElem);
            LOGGER.debug("database filters found:  " + XmlUtils.toString(filtersElem));
            Document dbFiltersXml = createDbFiltersXml(filters);
            String dbFilterFileName = (idx == 0) ? "db-filters.xml" : "db-filters" + idx + ".xml";
            writeXml(dbFiltersXml, distrWebDirPath + "/WEB-INF/" + dbFilterFileName);
            XmlUtils.removeAllChildren(filtersElem);
            filtersElem.appendChild(doc.createProcessingInstruction(IncludableParser.MOZART_INCLUDE, "href=\"htdocs:///WEB-INF/" + dbFilterFileName + "\" match=\"/root/filters/*\" "));
            idx++;
        }
        writeXml(doc, dbXmlPath);
    }

    /**
	 * remove editor from sitemap.xml
	 * @param distrWebDirPath
	 * @throws Exception 
	 */
    private void fixSitemapXml(String distrWebDirPath) throws Exception {
        String sitemapXmlPath = distrWebDirPath + "/tree/sitemap.xml";
        Document doc = new Parser().parse(InputOutput.create(sitemapXmlPath)).getDocument();
        for (ProcessingInstruction pi : XmlUtils.findAllProcessingIstructions(doc, IncludableParser.MOZART_INCLUDE)) {
            final String href = PI.getPIAttributes(pi).get("href");
            if (href != null && href.contains("/editor/")) {
                Comment comment = pi.getOwnerDocument().createComment(XmlUtils.processingInstructionToString(pi));
                pi.getParentNode().replaceChild(comment, pi);
                LOGGER.info("Modify sitemap.xml: disable editor mozart-include instraction " + XmlUtils.processingInstructionToString(pi));
            }
        }
        writeXml(doc, sitemapXmlPath);
    }

    /**
	 * write Document into file
	 * @param doc
	 * @param fileName
	 * @throws FileNotFoundException
	 * @throws TransformerFactoryConfigurationError 
	 * @throws TransformerConfigurationException 
	 */
    private void writeXml(Document doc, final String fileName) throws Exception {
        LOGGER.info("write xml to " + fileName);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Properties props = new Properties();
        props.put(OutputKeys.ENCODING, "UTF-8");
        props.put(OutputKeys.METHOD, "xml");
        props.put(OutputKeys.VERSION, "1.0");
        props.put(OutputKeys.INDENT, "yes");
        transformer.setOutputProperties(props);
        transformer.transform(new DOMSource(doc), new StreamResult(new FileOutputStream(fileName)));
    }

    private Document createDbFiltersXml(Node[] filters) {
        Document dbFiltersDoc = Parser.createEmptyDocument();
        Element dbFiltersElem = (Element) dbFiltersDoc.appendChild(dbFiltersDoc.createElement("root")).appendChild(dbFiltersDoc.createElement("filters"));
        for (int i = 0; i < filters.length; i++) {
            Node node = filters[i];
            if (XmlUtils.isElement(node)) {
                dbFiltersElem.appendChild(dbFiltersDoc.adoptNode(node));
            } else if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
                final String piData = ((ProcessingInstruction) node).getData();
                if (StringUtils.hasText(piData) && piData.contains("editor-filters.xml")) {
                    LOGGER.info("Skip editor db filters");
                    dbFiltersElem.appendChild(dbFiltersDoc.createComment("Skip editor db filters \n" + XmlUtils.toString(node)));
                } else {
                    dbFiltersElem.appendChild(dbFiltersDoc.adoptNode(node));
                }
            }
        }
        return dbFiltersDoc;
    }

    /**
	 * 
	 * @param args
	 */
    public static void main(String[] args) {
        System.getProperties().setProperty("org.apache.commons.logging.simplelog.defaultlog", "info");
        System.getProperties().setProperty("org.apache.commons.logging.LogFactory", "org.apache.commons.logging.impl.LogFactoryImpl");
        System.getProperties().setProperty("org.apache.commons.logging.simplelog.log.ru.adv.util.Files", "info");
        if (args.length != 2) {
            LOGGER.info("This program converts Mozart-2 websites files into Mozart-3 verion.");
            LOGGER.info("Usage: java -jar siteconverter.jar WEB_HOST_PATH OUTPUT_DIR");
            LOGGER.info("Usage: java -jar siteconverter.jar -fixdir DIR");
            LOGGER.info("Usage: java -jar siteconverter.jar -fixdump DUMPFILE");
            System.exit(-1);
        } else {
            try {
                if (args[0].equals("-fixdir")) {
                    replaceStringInFiles(new File(args[1]));
                } else if (args[0].equals("-fixdump")) {
                    String dumpFile = args[1];
                    SiteConverter.convertDump(StringUtils.getFilename(dumpFile) + ".fixed", new File(dumpFile));
                } else {
                    new SiteConverter().makeMozart3Distr(args[0], args[1]);
                }
            } catch (Throwable t) {
                t.printStackTrace();
                System.exit(-1);
            }
        }
    }
}
