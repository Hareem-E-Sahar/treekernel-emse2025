import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;

public class Installer {

    private static final String version = "0.3";

    private static final String FILESEPERATOR = System.getProperty("file.separator");

    private static final String LINESEPERATOR = System.getProperty("line.separator");

    private static File file;

    private static BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

    private static String JAVA_PATH, KN_PATH, PATH_TO_MKN, CONF_FOLDER, EXECUTABLE, INIT_SCRIPT, USER, LOG;

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        if (args.length > 1) {
            usage();
            System.exit(0);
        }
        if (args.length == 1) {
            if (args[0].compareTo("--help") == 0 || args[0].compareTo("-h") == 0) {
                usage();
                System.exit(0);
            }
            readConfig(args[0]);
            System.exit(0);
        }
        try {
            String in;
            if (System.getProperty("os.name").compareToIgnoreCase("Linux") != 0) {
                System.out.print("This installer currently supports only Linux, do you wish still to use it?\n<y/n>\n> ");
                while (true) {
                    in = stdIn.readLine();
                    if ((in.compareTo("y") == 0) || (in.compareTo("yes") == 0)) {
                        System.out.println("Continuing");
                        break;
                    }
                    if ((in.compareTo("n") == 0) || (in.compareTo("no") == 0)) {
                        System.exit(1);
                    }
                    System.out.printf("Unknown option %s%nContinue anyway <y/n>%n> ", in);
                }
            }
            System.out.printf("This programm will install MyKeyNote v%s %nTo exit type quit or press enter to continue%n> ", version);
            in = stdIn.readLine();
            if (quiting(in)) {
                System.exit(0);
            }
            while (true) {
                System.out.printf("Please specify your Java path, for example /usr/bin/java%n> ", version);
                in = stdIn.readLine();
                if (quiting(in)) {
                    System.exit(0);
                }
                JAVA_PATH = in;
                System.out.printf("Please specify where the keynote executable resides, for example /usr/bin/keynote%n> ", version);
                in = stdIn.readLine();
                if (quiting(in)) {
                    System.exit(0);
                }
                KN_PATH = in;
                System.out.printf("Please specify where you want to install MKN class files, for example /opt/mkn/%n> ", version);
                in = stdIn.readLine();
                if (quiting(in)) {
                    System.exit(0);
                }
                PATH_TO_MKN = in;
                System.out.printf("Please specify where the configuration folder should be, for example /etc/mykeynote/%n> ", version);
                in = stdIn.readLine();
                if (quiting(in)) {
                    System.exit(0);
                }
                CONF_FOLDER = in;
                System.out.printf("Please specify where the executable file should be installed, for example /usr/bin/%n> ", version);
                in = stdIn.readLine();
                if (quiting(in)) {
                    System.exit(0);
                }
                EXECUTABLE = in;
                System.out.printf("Please specify where the init script should be installed, for example /etc/init.d/%n> ", version);
                in = stdIn.readLine();
                if (quiting(in)) {
                    System.exit(0);
                }
                INIT_SCRIPT = in;
                System.out.printf("Please specify as which user the init script should be executed, for example mkn:users%n> ", version);
                in = stdIn.readLine();
                if (quiting(in)) {
                    System.exit(0);
                }
                USER = in;
                System.out.printf("Please specify the logging directory, for example /var/log/%n> ", version);
                in = stdIn.readLine();
                if (quiting(in)) {
                    System.exit(0);
                }
                LOG = in;
                System.out.printf("The current configuration is:%nJava path:\t%s%nMyKeyNote:\t%s\nConfig folder:\t%s\nInit script:\t%s\nExecutable:\t%s\nUser:\t%s\nAccept <y/n>\n> ", JAVA_PATH, PATH_TO_MKN, CONF_FOLDER, INIT_SCRIPT, EXECUTABLE, USER);
                boolean run = true;
                while (run) {
                    in = stdIn.readLine();
                    if ((in.compareTo("y") == 0) || (in.compareTo("yes") == 0)) {
                        initiate();
                        run = false;
                    }
                    if ((in.compareTo("n") == 0) || (in.compareTo("no") == 0)) {
                        System.out.println("Install aborted");
                        System.exit(0);
                    } else {
                        System.out.printf("Unknown option %s%nInstall <y/n>%n> ", in);
                    }
                }
                saveConfigFile();
                System.out.println("Deleting temp files.");
                deleteRecursive(new File("etc"));
                deleteRecursive(new File("META-INF"));
                deleteRecursive(new File("usr"));
                deleteRecursive(new File("var"));
                System.exit(0);
            }
        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }
    }

    private static boolean quiting(String in) throws IOException {
        if (in.compareTo("quit") == 0) {
            System.out.print("Are you sure you want to quit this installer? <y/n>\n> ");
            while (true) {
                in = stdIn.readLine();
                if ((in.compareTo("y") == 0) || (in.compareTo("yes") == 0)) return true;
                if ((in.compareTo("n") == 0) || (in.compareTo("no") == 0)) {
                    System.out.println("Continuing");
                    return false;
                }
                System.out.printf("Unknown option %s%nQuit <y/n>%n> ", in);
            }
        } else return false;
    }

    private static void initiate() throws FileNotFoundException, IOException {
        File file;
        file = new File(CONF_FOLDER);
        if (!file.isDirectory()) {
            if (!file.mkdir()) {
                System.err.printf("Unable to create directory %s. Please create it after the install.\n", file.getAbsolutePath());
            } else {
                System.out.printf("Directory %s created.\n", file.getAbsolutePath());
            }
        }
        file = new File(CONF_FOLDER, "public_key");
        if (!file.isDirectory()) {
            if (!file.mkdir()) {
                System.err.printf("Unable to create directory %s. Please create it after the install.\n", file.getAbsolutePath());
            } else {
                System.out.printf("Directory %s created.\n", file.getAbsolutePath());
            }
        }
        file = new File(CONF_FOLDER, "private_key");
        if (!file.isDirectory()) {
            if (!file.mkdir()) {
                System.err.printf("Unable to create directory %s. Please create it after the install.\n", file.getAbsolutePath());
            } else {
                System.out.printf("Directory %s created.\n", file.getAbsolutePath());
            }
        }
        file = new File(CONF_FOLDER, "keynames");
        if (!file.isDirectory()) {
            if (!file.mkdir()) {
                System.err.printf("Unable to create directory %s. Please create it after the install.\n", file.getAbsolutePath());
            } else {
                System.out.printf("Directory %s created.\n", file.getAbsolutePath());
            }
        }
        file = new File(CONF_FOLDER, "sessions");
        if (!file.isDirectory()) {
            if (!file.mkdir()) {
                System.err.printf("Unable to create directory %s. Please create it after the install.\n", file.getAbsolutePath());
            } else {
                System.out.printf("Directory %s created.\n", file.getAbsolutePath());
            }
        }
        file = new File(PATH_TO_MKN);
        if (file.isDirectory()) {
            System.out.println("Older version of MKN detected, deleting folder " + file.getAbsolutePath());
            deleteRecursive(file);
        }
        if (!file.mkdir()) {
            System.err.printf("Unable to create directory %s. Aborting.\n", file.getAbsoluteFile());
            System.exit(1);
        } else {
            System.out.printf("Directory %s created.\n", file.getAbsolutePath());
        }
        file = new File(INIT_SCRIPT);
        if (!file.isDirectory()) {
            if (!file.mkdir()) {
                System.err.printf("Unable to create directory %s. Please create it after the install.\n", file.getAbsolutePath());
            } else {
                System.out.printf("Directory %s created.\n", file.getAbsolutePath());
            }
        }
        file = new File(LOG);
        if (!file.isDirectory()) {
            if (!file.mkdir()) {
                System.err.printf("Unable to create directory %s. Please create it after the install.\n", file.getAbsolutePath());
            } else {
                System.out.printf("Directory %s created.\n", file.getAbsolutePath());
            }
        }
        file = new File(LOG, "mykeynote");
        if (!file.isDirectory()) {
            if (!file.mkdir()) {
                System.err.printf("Unable to create directory %s. Please create it after the install.\n", file.getAbsolutePath());
            } else {
                System.out.printf("Directory %s created.\n", file.getAbsolutePath());
            }
        }
        String temp = "";
        String line = "";
        BufferedReader read;
        PrintWriter out;
        file = new File(INIT_SCRIPT, "mykeynote");
        if (!file.isFile()) {
            System.out.println("Creating the init script");
            read = new BufferedReader(new FileReader(String.format("etc%sinit.d%smykeynote.template", FILESEPERATOR, FILESEPERATOR)));
            out = new PrintWriter(file);
            while ((line = read.readLine()) != null) temp += line + LINESEPERATOR;
            temp = String.format(temp, PATH_TO_MKN, JAVA_PATH, CONF_FOLDER, USER);
            out.write(temp);
            out.flush();
            out.close();
            read.close();
            if (file.setExecutable(true)) {
                System.err.printf("Unable to set %s as executable. Please do it manualy.\n", file.getAbsoluteFile());
            }
        }
        file = new File(CONF_FOLDER, "MyKeyNote.conf");
        if (!file.exists()) {
            System.out.println("Creating the configuration file");
            read = new BufferedReader(new FileReader(String.format("etc%smykeynote%sMyKeyNote.conf.template", FILESEPERATOR, FILESEPERATOR)));
            out = new PrintWriter(file);
            temp = "";
            while ((line = read.readLine()) != null) temp += line + "\n";
            temp = String.format(temp, LOG, KN_PATH, CONF_FOLDER, CONF_FOLDER, CONF_FOLDER);
            out.write(temp);
            out.flush();
            out.close();
            read.close();
        }
        file = new File(EXECUTABLE, "mykeynote");
        if (!file.exists()) {
            System.out.println("Creating the executable script");
            read = new BufferedReader(new FileReader(String.format("usr%sbin%smykeynote.template", FILESEPERATOR, FILESEPERATOR)));
            out = new PrintWriter(file);
            temp = "";
            while ((line = read.readLine()) != null) temp += line + LINESEPERATOR;
            temp = String.format(temp, JAVA_PATH, PATH_TO_MKN, CONF_FOLDER);
            out.write(temp);
            out.flush();
            out.close();
            if (!file.setExecutable(true)) System.err.printf("Unable to set the %s file to be executable. Please repair the  is done", file.getAbsoluteFile());
            read.close();
        }
        file = new File("mykeynote");
        File dest = new File(PATH_TO_MKN, "mykeynote");
        if (dest.exists()) {
            System.out.println("Deleting the older instance of MyKeyNote " + dest.getParentFile());
            deleteRecursive(dest.getParentFile());
        }
        dest = dest.getParentFile();
        try {
            if (!file.renameTo(dest)) {
                System.err.printf("Unable to copy the program from %s to %s directory. Maybe it is on another parition?" + "\nTrying to copy the files recursively.%n", file.getAbsoluteFile(), dest.getAbsoluteFile());
                if (!copyDirectory(file, dest)) System.err.printf("Please copy the directory from %s to %s manually.", file.getAbsoluteFile(), dest.getAbsoluteFile());
            } else {
                System.out.printf("MyKeyNote program intalled to %s\n", file.getAbsoluteFile());
            }
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
        }
    }

    private static void readConfig(String filename) {
        String ant;
        File file = new File(filename);
        java.util.Properties config = new java.util.Properties();
        String version_this = "error";
        String[] options = { "JAVA_PATH", "KN_PATH", "PATH_TO_MKN", "CONF_FOLDER", "EXECUTABLE", "INIT_SCRIPT", "USER", "LOG", "version" };
        String[] optionsObject = { JAVA_PATH, KN_PATH, PATH_TO_MKN, CONF_FOLDER, EXECUTABLE, INIT_SCRIPT, USER, LOG, version_this };
        try {
            FileInputStream in = new FileInputStream(file.getAbsoluteFile());
            config.load(in);
        } catch (IOException e) {
            System.err.println("Error while trying to parse the file, exiting");
            System.exit(1);
        }
        for (int i = 0; i < options.length; i++) {
            if ((ant = config.getProperty(options[i])) != null) {
                optionsObject[i] = ant;
            } else {
                System.err.printf("Option %s is not set. Aborting.\n", options[i]);
                System.exit(0);
            }
        }
        JAVA_PATH = optionsObject[0];
        KN_PATH = optionsObject[1];
        PATH_TO_MKN = optionsObject[2];
        CONF_FOLDER = optionsObject[3];
        EXECUTABLE = optionsObject[4];
        INIT_SCRIPT = optionsObject[5];
        USER = optionsObject[6];
        LOG = optionsObject[7];
        version_this = optionsObject[8];
        if (version_this.equals("0")) {
            System.out.println("Initial installation from file.");
            try {
                initiate();
                System.exit(0);
            } catch (FileNotFoundException e) {
                System.err.printf("FileNotFoundException hit:\n%s\nExiting");
                System.exit(1);
            } catch (IOException e) {
                System.err.println("IOException while trying to save a file. Exiting!");
                e.printStackTrace();
                System.exit(1);
            }
        }
        if (version_this.equals(version)) {
            System.out.printf("Version %s is being reinstalled, all configuration files are not to be deleted.\n", version_this);
            File dest = new File(PATH_TO_MKN, "mykeynote");
            if (dest.exists()) {
                System.out.println("Deleting the older instance of MyKeyNote.");
                System.out.println(dest.getParent());
                if (!deleteRecursive(dest.getParentFile())) System.exit(1);
            }
            try {
                initiate();
                System.out.println("Reinstall complete.");
                System.exit(0);
            } catch (FileNotFoundException e) {
                System.err.println(e);
                System.exit(99);
            } catch (IOException e) {
                System.err.println(e);
                System.exit(19);
            }
        }
        System.err.printf("Unknown or too new version %s.\n", version_this);
        System.exit(1);
    }

    private static void usage() {
        System.out.printf("<no parameter>\tUse the installer in the interactive mode,\n--help\t\tReturns this menu\n<file name>\tSpecify the file, where the installation configuration file resides\n");
    }

    private static boolean deleteRecursive(File dir) {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                if (!deleteRecursive(file)) return false;
            } else {
                if (file.delete()) {
                } else {
                    System.out.printf("Unable to delete the file %s\n", file.getAbsoluteFile());
                    return false;
                }
            }
        }
        if (!dir.delete()) {
            System.out.println("Unable to delete directory " + dir.getAbsolutePath());
        } else {
        }
        return true;
    }

    private static void saveConfigFile() throws IOException {
        String in;
        while (true) {
            System.out.printf("Would you like to save the options in a file, which later can be used for reinstalling and updating.\n<y/n>\n> ");
            in = stdIn.readLine();
            if (in.compareToIgnoreCase("n") == 0 || in.compareToIgnoreCase("no") == 0) {
                System.out.printf("Installation of MyKeyNote %s was successfull\n", version);
                System.exit(0);
            }
            if (in.compareToIgnoreCase("y") == 0 || in.compareToIgnoreCase("yes") == 0) {
                File file = new File(CONF_FOLDER + "/.installation");
                if (file.exists()) {
                    if (!file.delete()) {
                        System.err.println("Unable to delete the old configuration file " + file.getAbsolutePath());
                        System.exit(1);
                    }
                }
                String config = String.format("JAVA_PATH = %s\nKN_PATH = %s\nPATH_TO_MKN = %s\nCONF_FOLDER = %s\nEXECUTABLE = %s\nINIT_SCRIPT = %s\nUSER = %s\nLOG = %s\nversion = %s", JAVA_PATH, KN_PATH, PATH_TO_MKN, CONF_FOLDER, EXECUTABLE, INIT_SCRIPT, USER, LOG, version);
                PrintWriter out;
                try {
                    out = new PrintWriter(file);
                    out.write(config);
                    out.flush();
                    out.close();
                } catch (FileNotFoundException e) {
                    System.err.println("Unable to create the installation file.\n" + "Please copy the following information to " + file.getAbsolutePath() + config);
                    System.exit(1);
                }
                System.out.printf("Installation of MyKeyNote %s was successfull\n", version);
            }
            return;
        }
    }

    private static boolean copyDirectory(File source, File destination) {
        if (!source.isDirectory()) {
            System.err.printf("%s is not a directory, aborting%n", source.getAbsoluteFile());
            return false;
        }
        if (!destination.isDirectory()) {
            System.err.printf("%s is not a directory, aborting%n", source.getAbsoluteFile());
        }
        if (!destination.exists()) destination.mkdir();
        String[] files = source.list();
        for (String fileS : files) {
            file = new File(source, fileS);
            if (file.isDirectory()) {
                if (!copyDirectory(new File(source, fileS), new File(destination, fileS))) return false;
            } else {
                InputStream in = null;
                OutputStream out = null;
                try {
                    in = new FileInputStream(file);
                    out = new FileOutputStream(new File(destination, fileS));
                } catch (FileNotFoundException e) {
                    System.err.println("Unexpected error, exiting!");
                    System.exit(1);
                }
                byte[] buf = new byte[1024];
                int len;
                try {
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                } catch (IOException e) {
                    System.err.println("IOException hit while copying file " + file.getAbsolutePath());
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return true;
    }
}
