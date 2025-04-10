package org.tolven.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;

/**
 * This class augements IzPack for Tolven installation.
 * 
 * @author Joseph Isaac
 * 
 */
public class TolvenInstaller {

    public static final String CMD_LINE_CONF_OPTION = "conf";

    public static final String ENV_CONF = "TOLVEN_CONFIG_DIR";

    public static final String CMD_LINE_PLUGINSXMLTEMPLATE_OPTION = "pluginsXMLTemplate";

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        try {
            System.out.println("Starting Tolven installation...");
            CommandLine commandLine = getCommandLine(args);
            String configDirname = getCommandLineConfigDir(commandLine);
            File configDir = new File(configDirname);
            System.out.println("Selected tolven-config Directory:\t" + configDir.getPath());
            File binDirectory = new File(System.getProperty("user.dir"));
            File installDir = binDirectory.getParentFile();
            String templatePluginsXML = getTemplatePluginsXML(commandLine);
            File bootPropertiesTemplate = new File(installDir, "installer/template-tolven-config/boot.properties");
            if (!bootPropertiesTemplate.exists()) {
                throw new RuntimeException("Template boot.properties not found: " + bootPropertiesTemplate.getPath());
            }
            System.out.println("Template boot.properties:\t" + bootPropertiesTemplate.getPath());
            File repositoryLocalTemplate = new File(installDir, "installer/template-tolven-config/repositoryLocal");
            if (!repositoryLocalTemplate.exists()) {
                throw new RuntimeException("Template repositoryLocal not found: " + repositoryLocalTemplate.getPath());
            }
            System.out.println("Template repositoryLocal:\t" + repositoryLocalTemplate.getPath());
            upgradeConfigDir(configDir, installDir, templatePluginsXML, bootPropertiesTemplate, repositoryLocalTemplate);
            File templateBinDir = new File(binDirectory.getParent(), "installer/bin");
            updateScripts(binDirectory, templateBinDir, configDir);
            System.out.println("\n*** Installation successful ***");
        } catch (AbandonInstallationException ex) {
            System.out.println(ex.getMessage());
            return;
        }
    }

    private static CommandLine getCommandLine(String[] args) {
        GnuParser parser = new GnuParser();
        try {
            return parser.parse(getCommandOptions(), args);
        } catch (ParseException ex) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(TolvenInstaller.class.getName(), getCommandOptions());
            throw new RuntimeException("Could not parse command line for: " + TolvenInstaller.class.getName(), ex);
        }
    }

    private static Options getCommandOptions() {
        Options cmdLineOptions = new Options();
        Option confOption = new Option(CMD_LINE_CONF_OPTION, CMD_LINE_CONF_OPTION, true, "configuration directory");
        cmdLineOptions.addOption(confOption);
        Option pluginsXMLSourceOption = new Option(CMD_LINE_PLUGINSXMLTEMPLATE_OPTION, CMD_LINE_PLUGINSXMLTEMPLATE_OPTION, true, "source plugins.xml url");
        pluginsXMLSourceOption.setRequired(true);
        cmdLineOptions.addOption(pluginsXMLSourceOption);
        return cmdLineOptions;
    }

    private static String getCommandLineConfigDir(CommandLine commandLine) throws AbandonInstallationException, IOException {
        String configDir = commandLine.getOptionValue(CMD_LINE_CONF_OPTION);
        if (configDir == null) {
            configDir = System.getenv(ENV_CONF);
            if (configDir == null) {
                String configDirPath = null;
                if (System.getProperty("os.name").toLowerCase().indexOf("windows") == -1) {
                    configDirPath = "/usr/local/tolven/tolven-config";
                } else {
                    configDirPath = "c:\\tolven\\tolven-config";
                }
                System.out.print("\nPlease enter the Configuration Directory (hit return for the  default:  " + configDirPath + "): ");
                BufferedReader input = new BufferedReader(new InputStreamReader(System.in, Charset.forName("UTF-8")));
                String promptedConfigDirPath = input.readLine();
                if (promptedConfigDirPath == null) {
                    throw new AbandonInstallationException("Installation abandoned");
                }
                if (promptedConfigDirPath.length() == 0) {
                    promptedConfigDirPath = configDirPath;
                }
                return promptedConfigDirPath.trim();
            }
        }
        return configDir;
    }

    private static String getTemplatePluginsXML(CommandLine commandLine) {
        String urlString = commandLine.getOptionValue(CMD_LINE_PLUGINSXMLTEMPLATE_OPTION);
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Could not convert to URL: '" + urlString + "'", ex);
        }
        String templatePluginsXML = null;
        try {
            InputStream in = null;
            try {
                in = url.openStream();
                templatePluginsXML = IOUtils.toString(in);
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("Could not load plugins metadata from: " + url.toExternalForm(), ex);
        }
        if (templatePluginsXML == null || templatePluginsXML.trim().length() == 0) {
            throw new RuntimeException("Template plugins.xml has no content: " + url.toExternalForm());
        }
        System.out.println("Template plugins XML:\t" + url.toExternalForm());
        return templatePluginsXML;
    }

    private static void updateScripts(File binDirectory, File templateBinDir, File selectedConfigDir) throws IOException {
        String extension = null;
        if (System.getProperty("os.name").toLowerCase().indexOf("windows") == -1) {
            extension = ".sh";
        } else {
            extension = ".bat";
        }
        Collection<File> scripts = FileUtils.listFiles(templateBinDir, new SuffixFileFilter(extension), null);
        for (File script : scripts) {
            File dest = new File(binDirectory, script.getName());
            if (dest.exists()) {
                dest.delete();
            }
            System.out.println("move: " + script.getPath() + " to: " + binDirectory.getPath());
            FileUtils.moveFileToDirectory(script, binDirectory, false);
            if (script.getName().equals("tpfenv" + extension)) {
                List<String> lines = FileUtils.readLines(dest);
                List<String> sub_lines = new ArrayList<String>();
                boolean tpfenvRequiresUpdate = false;
                for (String line : lines) {
                    String replacedLine = line.replace("$TOLVEN_CONFIG", selectedConfigDir.getPath());
                    if (!replacedLine.equals(line)) {
                        tpfenvRequiresUpdate = true;
                    }
                    sub_lines.add(replacedLine);
                }
                if (tpfenvRequiresUpdate) {
                    System.out.println("updated: " + dest.getPath());
                    FileUtils.writeLines(dest, sub_lines);
                }
            }
        }
    }

    private static void upgradeConfigDir(File configDir, File installDir, String templatePluginsXML, File bootPropertiesTemplate, File repositoryLocalTemplate) throws IOException {
        File pluginsXML = new File(configDir, "plugins.xml");
        if (pluginsXML.exists()) {
            System.out.println(pluginsXML + " exists, and will NOT be overwritten");
        } else {
            templatePluginsXML = templatePluginsXML.replace("your-installationDir", installDir.getPath().replace("\\", "/"));
            templatePluginsXML = templatePluginsXML.replace("your-tolven-configDir", configDir.getPath().replace("\\", "/"));
            System.out.println("Writing template plugins.xml to: " + pluginsXML);
            FileUtils.writeStringToFile(pluginsXML, templatePluginsXML);
        }
        File bootProperties = new File(configDir, bootPropertiesTemplate.getName());
        if (bootProperties.exists()) {
            System.out.println(bootProperties + " exists, and will NOT be replaced by: " + bootPropertiesTemplate);
        } else {
            System.out.println("copy: " + bootPropertiesTemplate.getPath() + " to: " + bootProperties.getPath());
            FileUtils.copyFile(bootPropertiesTemplate, bootProperties);
        }
        File repositoryLocal = new File(configDir, "repositoryLocal");
        if (repositoryLocal.exists()) {
            System.out.println(repositoryLocal + " exists, and will NOT be replaced by: " + repositoryLocalTemplate);
        } else {
            System.out.println("copy: " + repositoryLocalTemplate.getPath() + " to: " + repositoryLocal.getPath());
            FileUtils.copyDirectory(repositoryLocalTemplate, repositoryLocal);
            File repositoryPluginsDir = new File(repositoryLocal, "plugins");
            repositoryPluginsDir.mkdirs();
        }
    }
}

class AbandonInstallationException extends Exception {

    private static final long serialVersionUID = 1L;

    AbandonInstallationException(Exception ex) {
        super(ex);
    }

    AbandonInstallationException(String ex) {
        super(ex);
    }
}
