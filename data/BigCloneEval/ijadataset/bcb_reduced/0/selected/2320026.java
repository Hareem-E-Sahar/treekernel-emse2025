package org.inria.genouest.opal.tools.typedwsdl.generator;

import org.apache.log4j.Logger;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.w3c.dom.Document;

/**
 * This class is class is the netry point of an application that perform generation of typed WSDL files.
 */
public class OpalTypedWSDLGenerator {

    private static Logger logger = Logger.getLogger(OpalTypedWSDLGenerator.class.getName());

    /** The first part of opal services urls. */
    static String servicesUrl;

    /** Path where opal program config files are located. */
    static String configFilesPath;

    /** Path where the generated WSDL file will be stored. */
    static String targetWSDLPath;

    /** The first part of the generated WSDL url. */
    static String targetWSDLUrl;

    /** Tmp path to store a copy of the original WSDL file. */
    static File tmpOriginalWSDLFile;

    /** The config file path. */
    private static String configFilePath;

    /**
     * The main method.
     * 
     * @param args the args
     */
    public static void main(String[] args) {
        loadConfig();
        CommandLine line = createProgramOptions(args);
        InputStream xsltFile = checkXSLTPath();
        prepareConfigFile(line);
        String serviceUrl = downloadOriginalWSDL(line);
        String targetFullWSDLUrl = targetWSDLUrl + line.getOptionValue("s") + ".wsdl";
        String targetFullWSDLPath = targetWSDLPath + line.getOptionValue("s") + ".wsdl";
        applyXSLTTransformation(xsltFile, targetFullWSDLPath);
        showResult(serviceUrl, targetFullWSDLUrl, targetFullWSDLPath);
    }

    /**
     * Load config.
     */
    private static void loadConfig() {
        String buildConfigPath = "build.properties";
        String opalConfigPath = "etc" + File.separator + "opal.properties";
        try {
            Properties buildConfigFile = new Properties();
            InputStream buildConfigContent = new FileInputStream(buildConfigPath);
            buildConfigFile.load(buildConfigContent);
            Properties opalConfigFile = new Properties();
            InputStream opalConfigContent = new FileInputStream(opalConfigPath);
            opalConfigFile.load(opalConfigContent);
            String tomcatUrl = opalConfigFile.getProperty("tomcat.url");
            String catalina = buildConfigFile.getProperty("catalina.home");
            String typedDir = buildConfigFile.getProperty("typedservices.dir");
            if ((tomcatUrl == null) || (catalina == null) || (typedDir == null)) throw new Exception("Missing value in config file. Aborting typed service generation.");
            servicesUrl = tomcatUrl + "/opal2/services/";
            configFilesPath = "configs" + File.separator;
            targetWSDLPath = catalina + File.separator + "webapps" + File.separator + typedDir + File.separator;
            File td = new File(targetWSDLPath);
            if (!td.exists()) {
                td.mkdir();
            }
            targetWSDLUrl = tomcatUrl + "/" + typedDir + "/";
            tmpOriginalWSDLFile = File.createTempFile("typedwsdlgenerator_tmp_original_WSDL_file", ".xml");
            tmpOriginalWSDLFile.deleteOnExit();
        } catch (Exception e) {
            logger.error("Couldn't load some config file (" + buildConfigPath + "; " + opalConfigPath + ").");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Check xslt path.
     * 
     * @return the input stream
     */
    private static InputStream checkXSLTPath() {
        InputStream xsltStream = OpalTypedWSDLGenerator.class.getResourceAsStream("/xslt/opalWSDL2TypedWSDL.xsl");
        if (xsltStream == null) {
            logger.error("The XSLT file could not be found.");
            System.exit(1);
        }
        return xsltStream;
    }

    /**
     * Prepare config file.
     * 
     * @param line the line
     */
    private static void prepareConfigFile(CommandLine line) {
        String fileName = line.getOptionValue("c");
        configFilePath = configFilesPath + fileName;
        if (fileName.indexOf(File.separator) != -1) {
            configFilePath = fileName;
        }
        File configFile = new File(configFilePath);
        if (!configFile.canRead()) {
            logger.error("The given config file (" + configFilePath + ") does not exist or is not readable.");
            System.exit(1);
        }
    }

    /**
     * Download original wsdl.
     * 
     * @param line the line
     * 
     * @return the string
     */
    private static String downloadOriginalWSDL(CommandLine line) {
        String serviceUrl = servicesUrl + line.getOptionValue("s") + "?wsdl";
        try {
            downloadFile(serviceUrl, tmpOriginalWSDLFile);
        } catch (MalformedURLException e) {
            logger.error("Could not download service WSDL file (" + serviceUrl + ") to tmp dir (" + tmpOriginalWSDLFile.getAbsolutePath() + "). Problem with Url.");
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            logger.error("Could not download service WSDL file (" + serviceUrl + ") to tmp dir (" + tmpOriginalWSDLFile.getAbsolutePath() + ").");
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            logger.error("Could not download service WSDL file (" + serviceUrl + ") to tmp dir (" + tmpOriginalWSDLFile.getAbsolutePath() + ").");
            e.printStackTrace();
            System.exit(1);
        }
        return serviceUrl;
    }

    /**
     * Apply xslt transformation.
     * 
     * @param xsltFile the xslt file
     * @param targetFullWSDLPath the target full wsdl path
     * 
     * @throws TransformerFactoryConfigurationError the transformer factory configuration error
     */
    private static void applyXSLTTransformation(InputStream xsltFile, String targetFullWSDLPath) throws TransformerFactoryConfigurationError {
        try {
            TransformerFactory transfoFact = TransformerFactory.newInstance();
            StreamSource stylesource = new StreamSource(xsltFile);
            Transformer transformer = transfoFact.newTransformer(stylesource);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setParameter("configPath", configFilePath);
            DocumentBuilderFactory docBFact = DocumentBuilderFactory.newInstance();
            docBFact.setNamespaceAware(true);
            DocumentBuilder docBuilder = docBFact.newDocumentBuilder();
            Document document = docBuilder.parse(tmpOriginalWSDLFile);
            Source source = new DOMSource(document);
            File fileWSDL = new File(targetFullWSDLPath);
            Result result = new StreamResult(fileWSDL);
            transformer.transform(source, result);
        } catch (Exception e) {
            logger.error("Error while applying XSLT stylesheet.");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Show result.
     * 
     * @param serviceUrl the service url
     * @param targetFullWSDLUrl the target full wsdl url
     * @param targetFullWSDLPath the target full wsdl path
     */
    private static void showResult(String serviceUrl, String targetFullWSDLUrl, String targetFullWSDLPath) {
        logger.info("Autogenerated WSDL file is available at: " + targetFullWSDLUrl);
        logger.info("It has been created on file at : " + targetFullWSDLPath);
        logger.info("The untyped WSDL file is still available at: " + serviceUrl);
    }

    /**
     * Generates options for this program.
     * 
     * @param args the args
     * 
     * @return CommandLine object
     */
    private static CommandLine createProgramOptions(String[] args) {
        Options options = new Options();
        options.addOption(OptionBuilder.withArgName("service_name").isRequired().withDescription("Deployed service name.").hasArg().create("s"));
        options.addOption(OptionBuilder.withArgName("config_filename").isRequired().withDescription("The config file name (it must be located in " + configFilesPath + ").").hasArg().create("c"));
        logger.debug("Reading command line arguments");
        CommandLineParser parser = new GnuParser();
        CommandLine line = null;
        try {
            line = parser.parse(options, args);
        } catch (Exception e) {
            logger.error(e.toString());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java org.inria.genouest.opal.tools.typedwsdl.generator.OpalTypedWSDLGenerator", options);
            System.exit(1);
        }
        return line;
    }

    /**
     * Download file.
     * 
     * @param filePath the file path
     * @param destination the destination
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MalformedURLException the malformed url exception
     */
    private static void downloadFile(String filePath, File destination) throws IOException, MalformedURLException {
        URLConnection connection = null;
        InputStream is = null;
        FileOutputStream destinationFile = null;
        URL url = new URL(filePath);
        connection = url.openConnection();
        destinationFile = new FileOutputStream(destination);
        is = new BufferedInputStream(connection.getInputStream());
        int currentBit = 0;
        while ((currentBit = is.read()) != -1) {
            destinationFile.write(currentBit);
        }
        is.close();
        destinationFile.flush();
        destinationFile.close();
    }
}
