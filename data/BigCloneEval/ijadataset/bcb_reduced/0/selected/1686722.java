package org.openmolgrid.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.openmolgrid.cli.resources.CLIResourceReader;
import org.openmolgrid.cli.resources.CLIServiceReader;
import org.openmolgrid.cli.resources.CLIServiceRegistry;
import org.openmolgrid.cli.util.CommonTools;
import com.fujitsu.arcon.servlet.Identity;
import com.pallas.unicore.resourcemanager.ResourceManager;

/**
 * Command Line API (CLAPI) can be used by a programm to access CLI features. At 
 * startup CLAPI initializes all <br> necessary server and local resources and 
 * caches them internally. It provides a method <br> 
 * {@link #execute(String command, String[] options) execute} to call CLI commands.
 * <p>
 * Here is a short example of the CLAPI usage: <br><br>
 * CLAPI cli = new CLAPI(); // create an instance of CLAPI<br>
 * String command = new String("build_ajo"); // specify a command to execute <br> //
 * create a String array with required parameters (note that authentification <br> //
 *  information is provided by the user defaults file!) <br>
 * String[] options = new String[]("-in", "my_workflow.xml", "-out", "my_ajo.ajo"); 
 * <br>
 * cli.execute(command, options); // execute command <br><br>
 * 
 * @author Lidia Kirtchakova, Research Center Juelich
 * @version $Id: CLAPI.java,v 1.1.1.1 2005/03/02 13:09:13 bschuller Exp $
 */
public class CLAPI {

    private CLIResourceReader resReader;

    private CLIServiceRegistry serviceRegistry;

    private CLIServiceReader serviceReader;

    private Properties props;

    private Properties defaultsProps;

    private String keystore;

    private String password;

    private Identity identity;

    static Logger logger = Logger.getLogger("org.openmolgrid");

    /**
	 * returns a CLAPI object. By this time all necessary server and 
	 * local resources are collected and cached. 
	 * <p>
	 * CLAPI uses a file with user defaults to get an access to user's keystore and 
	 * the corresponding password.
	 * 
	 * @throws Exception
	 */
    public CLAPI() throws Exception {
        String configFile = System.getProperty("org.openmolgrid.cli.config");
        if (configFile == null) configFile = "cli_config.txt";
        props = CommonTools.getCLIProperties(configFile);
        if (props == null) {
            throw new Exception("Error: Could not get CLI config file!");
        }
        String defaultsFile = System.getProperty("org.openmolgrid.cli.defaults");
        if (defaultsFile == null) {
            defaultsFile = "userdefaults.txt";
        }
        defaultsProps = CommonTools.getCLIProperties(defaultsFile);
        if (defaultsProps == null) {
            throw new Exception("Error: Could not get CLI defaults file!");
        }
        try {
            FileHandler fileHandler = new FileHandler(defaultsProps.getProperty("logger"));
            fileHandler.setFormatter(new SimpleFormatter());
            logger.setLevel(Level.INFO);
            logger.addHandler(fileHandler);
            logger.info("Logging started.");
            String logLevel = defaultsProps.getProperty("logging_level");
            Level level = null;
            try {
                level = Level.parse(logLevel);
            } catch (Exception ex) {
                logger.warning("Unrecognised loglevel, going to INFO");
                level = Level.INFO;
                logLevel = "INFO (fallback)";
            }
            logger.setLevel(level);
            logger.info("New log level " + logLevel);
        } catch (Exception ex) {
            throw new Exception("Could not create CLI logging: " + ex.getMessage());
        }
        keystore = defaultsProps.getProperty("keystore");
        if (keystore == null) {
            logger.severe("No keystore found!");
            throw new Exception("No keystore found!");
        }
        if (defaultsProps.containsKey("password")) {
            password = defaultsProps.getProperty("password");
        } else {
            String passwdPath = defaultsProps.getProperty("passwdfile");
            if (passwdPath == null) {
                logger.severe("No password found!");
                throw new Exception("No password found!");
            }
            try {
                String line = new String();
                File passwdFile = new File(passwdPath);
                BufferedReader r = new BufferedReader(new FileReader(passwdFile));
                while ((line += r.readLine()) != null) ;
                password = line.trim();
            } catch (IOException ex) {
                logger.severe("Error occured while retrieving password: " + ex.getMessage());
                throw new Exception("Error occured while retrieving password!");
            }
        }
        String unicoreDir = defaultsProps.getProperty("userDefaultsDir");
        System.setProperty("org.openmolgrid.cli.unicoredir", unicoreDir);
        if (!initResources()) {
            logger.severe("Error occured while initializing resources!");
            throw new Exception("Error occured while initializing resources!");
        }
    }

    private boolean initResources() {
        try {
            identity = new Identity(new File(keystore), password.toCharArray());
            String email = defaultsProps.getProperty(("userEmail"));
            if (email == null) email = "";
            identity.getUser().setEmailAddress(email);
            logger.info(("Setting user email adress to '" + email + "'"));
            resReader = new CLIResourceReader(identity, defaultsProps);
            ResourceManager.init(resReader);
            String pluginDir = System.getProperty("org.openmolgrid.cli.plugindir");
            if (pluginDir == null) pluginDir = defaultsProps.getProperty("pluginDir");
            if (pluginDir == null) {
                pluginDir = ".";
            }
            logger.info("Searching for plugins in " + pluginDir);
            serviceRegistry = new CLIServiceRegistry();
            serviceReader = new CLIServiceReader();
            serviceReader.setPluginDirectory(pluginDir);
            serviceReader.setRegistry(serviceRegistry);
            serviceReader.refresh(serviceRegistry);
        } catch (Exception ex) {
            logger.severe("Error: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private AbstractClientOperation loadClass(String command) {
        Object o;
        String classname = props.getProperty(command);
        if (classname == null || classname.equals("")) {
            logger.severe("Unknown operation " + command);
            return null;
        }
        try {
            Class c = Class.forName(classname);
            o = c.newInstance();
        } catch (Exception ex) {
            logger.severe("Could not load class " + classname);
            logger.severe(ex.getMessage());
            return null;
        }
        if (!(o instanceof AbstractClientOperation)) {
            logger.severe("Wrong class type: " + classname);
            return null;
        }
        return (AbstractClientOperation) o;
    }

    /**
	 * 
	 * @param command - command to be executed
	 * @param args - options required for this command
	 * example:
	 * command - "submit"
	 * args - ["-in", "ajo filepath", "-dir", "outcome_dir"]
	 */
    public void execute(String command, String[] args) throws Exception {
        logger.info("starting executing command...");
        AbstractClientOperation c = loadClass(command);
        if (c == null) {
            logger.severe("Command " + command + " can not be executed!");
            return;
        }
        logger.info("setting keystore and password...");
        c.setKeystore(keystore);
        c.setPassword(password);
        c.setIdentity(identity);
        c.setOptions(args);
        if (!(c instanceof BuildAJO)) {
            c.process();
        } else {
            BuildAJO bajo = (BuildAJO) c;
            bajo.setResourceInfoProvider(serviceRegistry);
            bajo.setUser(resReader.getUser());
            bajo.processFromAPI();
        }
    }
}
