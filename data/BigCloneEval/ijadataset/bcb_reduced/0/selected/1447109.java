package eu.isas.searchgui.processbuilders;

import eu.isas.searchgui.gui.WaitingDialog;
import java.io.File;

/**
 * This class takes care of starting the Makeblastdb Process. Mandatory when using omssa.
 *
 * @author Ravi Tharakan
 * @author Lennart Martens
 * @author Marc Vaudel
 */
public class MakeblastdbProcessBuilder extends SearchGUIProcessBuilder {

    /**
     * Default mac folder.
     */
    private static final String DEFALUT_MAC_FOLDER = "resources/makeblastdb" + File.separator + "mac";

    /**
     * Default windows folder.
     */
    private static final String DEFALUT_WINDOWS_FOLDER = "resources/makeblastdb" + File.separator + "windows";

    /**
     * Default linux folder.
     */
    private static final String DEFALUT_LINUX_FOLDER = "resources/makeblastdb" + File.separator + "linux";

    /**
     * The FASTA sequence database file to process.
     */
    private File iDatabaseFile = null;

    /**
     * Boolean indicating that the process is canceled
     */
    private boolean isCanceled = false;

    /**
     * Constructor.
     *
     * @param pathToJarFile     the path to the jar file
     * @param aDatabaseFile     File with the DB file to be formatted
     * @param waitingDialog     the waiting dialog
     */
    public MakeblastdbProcessBuilder(String pathToJarFile, File aDatabaseFile, WaitingDialog waitingDialog) {
        this.waitingDialog = waitingDialog;
        try {
            File makeBlastDb = new File(pathToJarFile + File.separator + getMakeblastdbFolder() + File.separator + "makeblastdb");
            makeBlastDb.setExecutable(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        process_name_array.add(pathToJarFile + File.separator + getMakeblastdbFolder() + File.separator + "makeblastdb");
        process_name_array.add("-in");
        process_name_array.add(aDatabaseFile.getName());
        System.out.println("\n\nmakeblastdb command: ");
        for (int i = 0; i < process_name_array.size(); i++) {
            System.out.print(process_name_array.get(i) + " ");
        }
        System.out.println("\n");
        pb = new ProcessBuilder(process_name_array);
        pb.directory(aDatabaseFile.getParentFile());
        pb.redirectErrorStream(true);
        iDatabaseFile = aDatabaseFile;
    }

    /**
    * Returns the folder containing the makeblastdb script. Depends on
    * the operation system.
    *
    * @return the folder containing the makeblastdb script
    */
    public String getMakeblastdbFolder() {
        String operating_system = System.getProperty("os.name").toLowerCase();
        if (operating_system.contains("mac os")) {
            return DEFALUT_MAC_FOLDER;
        } else if (operating_system.contains("windows")) {
            return DEFALUT_WINDOWS_FOLDER;
        } else {
            return DEFALUT_LINUX_FOLDER;
        }
    }

    /**
     * Checks if makeblastdb has already been run on this file.
     *
     * @return boolean returns true if Makeblastdb has been run
     */
    public boolean needsFormatting() {
        boolean result = true;
        String[] list = iDatabaseFile.getParentFile().list();
        String name = iDatabaseFile.getName();
        boolean phr = false;
        boolean pin = false;
        boolean psq = false;
        for (int i = 0; i < list.length; i++) {
            String s = list[i];
            if (s.equals(name + ".phr")) {
                phr = true;
            }
            if (s.equals(name + ".pin")) {
                pin = true;
            }
            if (s.equals(name + ".psq")) {
                psq = true;
            }
        }
        if (phr && pin && psq) {
            result = false;
        }
        return result;
    }

    /**
     * Starts the process of a process builder, gets the inputstream from the
     * process and shows it in a JTextArea. Does not close until the process
     * is completed.
     */
    public void startProcess() {
        super.startProcess();
        if (isCanceled) {
            File tempFile = new File(iDatabaseFile.getAbsolutePath() + ".phr");
            deleteFile(tempFile);
            tempFile = new File(iDatabaseFile.getAbsolutePath() + ".pin");
            deleteFile(tempFile);
            tempFile = new File(iDatabaseFile.getAbsolutePath() + ".psq");
            deleteFile(tempFile);
        }
    }

    /**
     * Cancels the process.
     */
    public void endProcess() {
        isCanceled = true;
        super.endProcess();
    }

    /**
     * Tries to the delete the given file.
     *
     * @param aFile the file to delete
     */
    private void deleteFile(File aFile) {
        int count = 0;
        boolean deleteOK = true;
        while (!aFile.delete()) {
            count++;
            if (count > 5) {
                deleteOK = false;
                break;
            }
        }
        if (!deleteOK) {
            System.err.println(" *** Failed to deleted file " + aFile.getAbsolutePath() + "! ***");
        }
    }

    /**
     * Returns the type of the process.
     *
     * @return the type of the process
     */
    public String getType() {
        return "Database Formating Process";
    }

    /**
     * Returns the file name of the currently processed file.
     *
     * @return the file name of the currently processed file
     */
    public String getCurrentlyProcessedFileName() {
        return iDatabaseFile.getName();
    }
}
