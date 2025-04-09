import coopnetclient.Client;
import coopnetclient.Globals;
import coopnetclient.enums.LogTypes;
import coopnetclient.utils.Logger;
import coopnetclient.utils.OnlineClientData;
import coopnetclient.utils.ipc.IPC;
import coopnetclient.utils.settings.SettingsHelper;
import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public final class Main {

    private static final String DEBUG = "debug";

    private static final String HELP = "help";

    private static final String SAFEMODE = "safemode";

    private static final String SERVER = "server";

    private static final String MULTIPLE_INSTANCES = "multipleInstances";

    private static boolean multipleInstances;

    private static boolean safemode;

    private Main() {
    }

    public static void main(String[] args) {
        try {
            System.getProperty("os.name");
        } catch (SecurityException e) {
            JOptionPane.showMessageDialog(null, "An error occured while trying to detect your operating system!" + "\nPlease make sure that your security policy in java is not set too tight." + "\nException message: " + e.getLocalizedMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        Globals.preInit();
        checkArgs(args);
        Globals.init();
        if (!multipleInstances) {
            doIPC();
        }
        if (safemode) {
            SettingsHelper.resetSettings();
        }
        Logger.log("Starting ...");
        cleanUpdater();
        Client.startup();
    }

    private static void doIPC() {
        try {
            if (IPC.isAnotherInstanceAlreadyRunning()) {
                System.out.println("Found another instance running, exiting.");
                IPC.showClientFrameOfRunningInstance();
                System.exit(0);
            } else {
                try {
                    IPC.registerAsRunningInstance();
                } catch (Exception e) {
                    Logger.log(e);
                }
            }
        } catch (Exception e) {
            Logger.log(e);
        }
    }

    private static void checkArgs(String[] args) {
        Options options = createCommandlineOptions();
        try {
            CommandLineParser parser = new GnuParser();
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption(SAFEMODE)) {
                safemode = true;
            }
            if (cmd.hasOption(HELP)) {
                printHelp(options);
                System.exit(0);
            }
            if (cmd.hasOption(SERVER)) {
                String value = cmd.getOptionValue(SERVER);
                String ip = value.substring(0, value.indexOf(OnlineClientData.IP_PORT_SEPARATOR));
                Globals.setServerIP(ip);
                int port = Integer.parseInt(value.substring(value.indexOf(OnlineClientData.IP_PORT_SEPARATOR) + 1));
                Globals.setServerPort(port);
            }
            if (cmd.hasOption(DEBUG)) {
                Globals.enableDebug();
            }
            if (cmd.hasOption(MULTIPLE_INSTANCES)) {
                multipleInstances = true;
            }
        } catch (ParseException ex) {
            System.out.println(ex.getMessage());
            System.out.println();
            printHelp(options);
            System.exit(1);
        }
    }

    @SuppressWarnings("static-access")
    private static Options createCommandlineOptions() {
        Options options = new Options();
        Option safemode = OptionBuilder.withDescription("resets all settings").withLongOpt(SAFEMODE).create();
        Option server = OptionBuilder.withDescription("ip and port of the server to connect to (e.g. 127.0.0.1:6667)").hasArg().withArgName("ip:port").withLongOpt(SERVER).create("s");
        Option debug = OptionBuilder.withDescription("print debug messages during operation").withLongOpt(DEBUG).create("d");
        Option help = OptionBuilder.withDescription("print this message").withLongOpt(HELP).create("h");
        Option multiple = OptionBuilder.withDescription("allow multiple instances").withLongOpt(MULTIPLE_INSTANCES).create("m");
        options.addOption(safemode);
        options.addOption(server);
        options.addOption(debug);
        options.addOption(multiple);
        options.addOption(help);
        return options;
    }

    private static void printHelp(Options options) {
        System.out.println("CoopnetClient, version " + Globals.getClientVersion());
        new HelpFormatter().printHelp("java -jar CoopnetClient.jar", "options:", options, "Visit our project website at \"http://coopnet.sourceforge.net\".", true);
    }

    private static void cleanUpdater() {
        final File tmpDir = new File("./UPDATER_TMP");
        final File updaterFile = new File("./CoopnetUpdater.jar");
        if (tmpDir.exists() || updaterFile.exists()) {
            Logger.log(LogTypes.LOG, "Updater files queued for deletion ...");
            new Thread() {

                private static final int UPDATER_CLOSING_SLEEP = 1000;

                @Override
                public void run() {
                    try {
                        sleep(UPDATER_CLOSING_SLEEP);
                    } catch (InterruptedException ex) {
                        Logger.log(ex);
                    }
                    try {
                        if (tmpDir.exists()) {
                            Logger.log(LogTypes.LOG, "Deleting ./UPDATER_TMP recursively");
                            deleteFile(tmpDir);
                        }
                    } catch (IOException e) {
                        Logger.log(e);
                    }
                    try {
                        if (updaterFile.exists()) {
                            Logger.log(LogTypes.LOG, "Deleting ./CoopnetUpdater.jar");
                            deleteFile(updaterFile);
                        }
                    } catch (IOException e) {
                        Logger.log(e);
                    }
                }
            }.start();
        }
    }

    private static boolean deleteFile(File resource) throws IOException {
        if (resource.isDirectory()) {
            File[] childFiles = resource.listFiles();
            for (File child : childFiles) {
                deleteFile(child);
            }
        }
        return resource.delete();
    }
}
