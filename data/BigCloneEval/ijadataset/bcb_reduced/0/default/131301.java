import coopnetclient.Client;
import coopnetclient.Globals;
import coopnetclient.enums.LogTypes;
import coopnetclient.utils.Logger;
import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;

public class Main {

    public static void main(String[] args) {
        try {
            System.getProperty("os.name");
        } catch (SecurityException e) {
            JOptionPane.showMessageDialog(null, "An error occured while trying to detect your operating system!" + "\nPlease make sure that your security policy in java is not set too tight." + "\nException message: " + e.getLocalizedMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        checkArgs(args);
        Logger.log(LogTypes.LOG, "Starting ...");
        cleanUpdater();
        Client.startup();
    }

    private static void checkArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--server")) {
                if (args.length < i + 1 || args[i].indexOf(":") == -1) {
                    try {
                        String ip = args[i + 1].substring(0, args[i + 1].indexOf(":"));
                        Globals.setServerIP(ip);
                        int port = Integer.parseInt(args[i + 1].substring(args[i + 1].indexOf(":") + 1));
                        Globals.setServerPort(port);
                        i++;
                    } catch (NumberFormatException e) {
                        System.out.println("ERROR: invalid value for <PORT>, number expected");
                    } catch (java.lang.StringIndexOutOfBoundsException e) {
                        System.out.println("ERROR: invalid value for <PORT>, number expected");
                    }
                } else {
                    System.out.println("ERROR: --server expects data in the form of \"127.0.0.1:6667\"");
                    printHelp();
                }
            } else if (args[i].equals("--debug")) {
                Globals.enableDebug();
            } else if (args[i].equals("--help")) {
                printHelp();
            } else {
                printHelp();
            }
        }
    }

    private static void printHelp() {
        System.out.println("\nCoopnetClient " + Globals.getClientVersion() + " usage:\n" + "    java -jar CoopnetClient.jar [--server <IP>:<PORT>] [--debug]\n" + "\n" + "    --server   ip and port of the server to connect to\n" + "    --debug    print debug messages during operation\n" + "    --help     print this help and exit\n");
        System.exit(1);
    }

    private static void cleanUpdater() {
        final File tmpDir = new File("./UPDATER_TMP");
        final File updaterFile = new File("./CoopnetUpdater.jar");
        if (tmpDir.exists() || updaterFile.exists()) {
            Logger.log(LogTypes.LOG, "Updater files queued for deletion ...");
            new Thread() {

                @Override
                public void run() {
                    try {
                        sleep(10);
                    } catch (InterruptedException ex) {
                    }
                    try {
                        if (tmpDir.exists()) {
                            Logger.log(LogTypes.LOG, "Deleting ./UPDATER_TMP recursively");
                            delete(tmpDir);
                        }
                    } catch (IOException e) {
                    }
                    try {
                        if (updaterFile.exists()) {
                            Logger.log(LogTypes.LOG, "Deleting ./CoopnetUpdater.jar");
                            delete(updaterFile);
                        }
                    } catch (IOException e) {
                    }
                }
            }.start();
        }
    }

    private static boolean delete(File resource) throws IOException {
        if (resource.isDirectory()) {
            File[] childFiles = resource.listFiles();
            for (File child : childFiles) {
                delete(child);
            }
        }
        return resource.delete();
    }
}
