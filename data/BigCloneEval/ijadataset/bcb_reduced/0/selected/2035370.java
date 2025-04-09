package org.hardtokenmgmt.tools.reportgen;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.hardtokenmgmt.common.Constants;
import org.hardtokenmgmt.core.settings.BasicGlobalSettings;
import org.hardtokenmgmt.tools.reportgen.signtoken.DefaultJKSSignToken;

/**
 * 
 * Class in-charge of managing configuration and property files related to the 
 * report generator tools.
 * 
 * @author Philip Vendil 1 Jul 2010
 *
 * @version $Id$
 */
public class Config {

    private static final Logger log = Logger.getLogger(Config.class);

    private static final String DEFAULT_CONFIGFILE_LOCATION = "config.properties";

    public static final String SETTING_CACHE_DIRECTORY = "cache.dir";

    public static final String DEFAULT_CACHE_DIRECTORY = "./orgresources";

    public static final String SETTING_LANGUGAGE_DIRECTORY = "lang.dir";

    public static final String DEFAULT_LANGUAGE_DIRECTORY = "./languages";

    public static final String SETTING_DB_DRIVER = "db.driver.name";

    public static final String SETTING_DB_DRIVER_LOCATION = "db.driver.location";

    public static final String SETTING_DB_CONNECTURL = "db.connecturl";

    public static final String SETTING_DB_USERNAME = "db.username";

    public static final String SETTING_DB_PASSWORD = "db.password";

    public static final String SETTING_USE_ENCRYPTION = "encrypt.use";

    public static final boolean DEFAULT_USE_ENCRYPTION = false;

    public static final String SETTING_ENCRYPT_PASSWORD = "encrypt.password";

    public static final String SETTING_USE_DIGITALSIGNATURE = "digitalsignature.use";

    public static final boolean DEFAULT_USE_DIGITALSIGNATURE = false;

    public static final String SETTING_SIGNTOKEN = "digitalsignature.signtoken";

    public static final String DEFAULT_SIGNTOKEN = DefaultJKSSignToken.class.getName();

    public static final String SETTING_DEFAULTSIGNTOKEN_KEYSTOREPATH = "digitalsignature.defaultsignengine.keystorepath";

    public static final String SETTING_DEFAULTSIGNTOKEN_PASSWORD = "digitalsignature.defaultsignengine.password";

    public static final String SETTING_DEFAULTSIGNTOKEN_ALIAS = "digitalsignature.defaultsignengine.alias";

    public static final String SETTING_PDFSIGNATURE_REASON = "digitalsignature.pdf.reason";

    public static final String SETTING_PDFSIGNATURE_LOCATION = "digitalsignature.pdf.location";

    public static final String SETTING_PDFSIGNATURE_VISIBLESIGNATURE = "digitalsignature.pdf.visiblearea";

    /**
	 * A ";" separated string containing all the report formats to generate the reports in for the given organization
	 * <p>
	 * Default is PDF only.
	 */
    public static final String SETTING_REPORTFORMATS = "reportformats";

    public static final String DEFAULT_REPORTFORMAT = Constants.REPORT_FORMAT_PDF;

    /**
	 * A ";" separated string containing all the report filenames (including .jrxml) to generate for organization.
	 */
    public static final String SETTING_GENERATEREPORTS = "generatereports";

    private Properties configProperties = new Properties();

    private byte[] configData = null;

    private static Config configuration = new Config();

    public static Config getInstance() {
        return configuration;
    }

    private Config() {
    }

    /**
	 * Initialization method that should be called before any properties are retrieved
	 * from the configuration.
	 * 
	 * @param fileLocation the full path the config.properties file
	 * @throws IOException
	 */
    public void init(String fileLocation) throws IOException {
        if (fileLocation == null) {
            fileLocation = DEFAULT_CONFIGFILE_LOCATION;
        }
        File configFile = new File(fileLocation);
        if (!configFile.exists() || !configFile.isFile() || !configFile.canRead()) {
            throw new IOException("Error cannot read config file '" + fileLocation + "', check that it exist and is readable.");
        }
        configProperties.load(new FileInputStream(configFile));
        PropertyConfigurator.configure(configProperties);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        configProperties.store(baos, null);
        configData = baos.toByteArray();
    }

    /**
	 * Init method used for testing purposes.
	 * 
	 * @param props test properties
	 */
    public void init(Properties props) throws IOException {
        configProperties = props;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        configProperties.store(baos, null);
        configData = baos.toByteArray();
    }

    /**
	 * @return Returns the properties in the config.properties file and not any settings from
	 * organization global.properties.
	 */
    public Properties getConfigProperties() {
        return configProperties;
    }

    /**
	 * Should be called after ReportResourcesManager have downloaded all
	 * the organizations resources.
	 * <p>
	 * Takes the local configuration in config.properties and overloads the
	 * organizations global.properties
	 * 
	 * @return all properties related to the organization
	 */
    public BasicGlobalSettings getOrgProperties() throws IOException {
        Properties retval = new Properties();
        retval.load(new ByteArrayInputStream(configData));
        String cacheDirectoryPath = configProperties.getProperty(SETTING_CACHE_DIRECTORY, DEFAULT_CACHE_DIRECTORY);
        File orgProperties = new File(cacheDirectoryPath + "/global.properties");
        if (!orgProperties.exists() || !orgProperties.isFile() || !orgProperties.canRead()) {
            throw new IOException("Error reading global.properties file '" + orgProperties.getAbsolutePath() + "', check that the directory is writable.");
        }
        FileInputStream fis = new FileInputStream(orgProperties);
        retval.load(fis);
        fis.close();
        return new BasicGlobalSettings(retval);
    }

    /** 
	 * Help method used to fetch report names for given organization.
	 * @param orgProps organizational properties
	 * @return an array of reports to generate, never null.
	 */
    public static String[] getReportNames(String orgId, BasicGlobalSettings orgProps) {
        String reportValue = orgProps.getProperty(SETTING_GENERATEREPORTS);
        if (reportValue == null) {
            log.debug("No reports configured to be generated for organization : " + orgId);
            return new String[0];
        }
        return reportValue.split(";");
    }

    public static String[] getReportFormats(BasicGlobalSettings orgProps) {
        String reportFormatValue = orgProps.getProperty(SETTING_REPORTFORMATS, DEFAULT_REPORTFORMAT);
        return reportFormatValue.split(";");
    }

    /**
	 * Returns true if organization is configured to digitally sign the reports is supported by report type.
	 * @param orgProperties the organizational properties
	 * @return true if reports should be signed.
	 */
    public static boolean getSignReports(BasicGlobalSettings orgProperties) {
        return orgProperties.getPropertyAsBoolean(Config.SETTING_USE_DIGITALSIGNATURE, Config.DEFAULT_USE_DIGITALSIGNATURE);
    }

    /**
	 * Returns true if organization is configured to encrypt the reports is supported by report type.
	 * @param orgProperties the organizational properties
	 * @return true if reports should be encrypted.
	 */
    public static boolean getEncryptReports(BasicGlobalSettings orgProperties) {
        return orgProperties.getPropertyAsBoolean(Config.SETTING_USE_ENCRYPTION, Config.DEFAULT_USE_ENCRYPTION);
    }
}
