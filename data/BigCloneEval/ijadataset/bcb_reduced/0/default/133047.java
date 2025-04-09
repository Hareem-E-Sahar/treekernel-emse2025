import java.io.*;
import java.util.*;
import javax.swing.JTextArea;

/**
 * Holds the static executable methods for the cohan program
 * 
 * @author Andrew Warner 
 * @version 1.0
 */
public class Execs {

    private File pcrerror;

    private JTextArea log;

    private NarrWriter narr;

    private String cmd;

    private String ext;

    /**
     * Detects the Operating System that this class is running on,
     * and executes the native fortran applications.
     * 
     * @param log   JTextArea to write to.
     * @param narr  NarrWriter to write to.
     */
    public Execs(JTextArea log, NarrWriter narr) {
        this.log = log;
        this.narr = narr;
        this.pcrerror = new File("pcrerror.dat");
        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();
        String osVersion = System.getProperty("os.version");
        String workingDir = System.getProperty("user.dir");
        if (osName.contains("windows")) {
            cmd = "cmd /c " + workingDir + "\\";
            ext = ".exe";
        } else if (osName.contains("linux")) {
            cmd = workingDir + "/";
            if (osArch.contains("i386")) {
                ext = ".i386";
            } else if (osArch.contains("amd64")) {
                ext = ".amd64";
            } else {
                log.append("Unsupported Linux architecture, contact the developers.\n");
                log.append("Architecture detected: " + osArch + "\n");
                narr.println("Unsupported Linux architecure, contact the developers.");
                narr.println("Architecture detected: " + osArch);
            }
        } else {
            log.append("Unsupported OS, contact the developers.\n");
            log.append("OS detected: " + osName + "\n");
            log.append("Architecture detected: " + osArch + "\n");
            narr.println("Unsupported OS, contact the developers.");
            narr.println("OS detected: " + osName);
            narr.println("Architecture detected: " + osArch);
        }
    }

    /**
     * Runs the provided application, and waits for it to finish.
     * 
     * @param file String containing the name of the application to execute.
     * @return Integer exit value.
     * @throws java.lang.InterruptedException
     */
    private int runApplication(String file) throws InterruptedException {
        return runApplication(file, "", true);
    }

    /**
     * Runs the provided application with the provided args, and waits for it to finish.
     * 
     * @param file String containing the name of the application to execute.
     * @param args String containing any arguments to provide to the application.
     * @param wait Boolean set to TRUE to wait for application to exit.
     * @return Integer exit value.
     * @throws java.lang.InterruptedException
     */
    private int runApplication(String file, String args) throws InterruptedException {
        return runApplication(file, args, true);
    }

    /**
     * Runs the provided application with the provided args.
     * If the wait boolean is set, waits for the application to finish.
     * 
     * @param file String containing the name of the application to execute.
     * @param args String containing any arguments to provide to the application.
     * @param wait Boolean set to TRUE to wait for application to exit.
     * @return Integer exit value.
     * @throws java.lang.InterruptedException
     */
    private int runApplication(String file, String args, Boolean wait) throws InterruptedException {
        int exitVal = 0;
        log.append("Execs: " + file + "\n");
        StreamGobbler errorGobbler, outputGobbler;
        try {
            Process p;
            p = Runtime.getRuntime().exec(file + " " + args);
            errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
            outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT");
            errorGobbler.start();
            outputGobbler.start();
            if (wait) {
                exitVal = p.waitFor();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return exitVal;
    }

    /**
     * Runs the initial Fred method to find a good value with which to do
     * hill climbing.
     *
     * @return Integer exit value, -1 if there was an error.
     * @throws java.lang.InterruptedException
     */
    public int runFred() throws InterruptedException {
        return runApplication(cmd + "fredMethod" + ext);
    }

    /**
     * Runs the binning process.
     * 
     * Pre: sequencesfasta and numbers.dat are formatted correctly.  Also
     * the programs removegaps.exe, readsynec.exe, etc must all be in the
     * root folder.
     * 
     * Post: output.dat contains the binning data
     * 
     * @return Integer exit value.
     * @throws java.lang.InterruptedException
     */
    protected int runBinning() throws InterruptedException {
        int exitVal;
        makeRandPCRError();
        exitVal = runApplication(cmd + "removegaps" + ext);
        exitVal += runApplication(cmd + "readsynec" + ext);
        exitVal += runApplication(cmd + "correctpcr" + ext);
        exitVal += runApplication(cmd + "divergencematrix" + ext);
        exitVal += runApplication(cmd + "binningdanny" + ext);
        return exitVal;
    }

    /**
     * Runs the hillclimb program.
     *
     * @return Integer exit value.
     * @throws java.lang.InterruptedException
     */
    public int runHillClimb() throws InterruptedException {
        return runApplication(cmd + "hillclimb" + ext);
    }

    /**
     * Opens the tree using NJPlot so that the user can see it to start
     * analysis.
     * 
     * @param treeFile String containing the tree file to open in NJPlot.
     * @return Integer exit value.
     */
    public int openTree(String treeFile) {
        int exitVal = -1;
        try {
            exitVal = runApplication("njplot", treeFile, false);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return exitVal;
    }

    /**
     * Runs the dnapars application
     * 
     * @return Integer exit value.
     */
    public int runDNAPars() {
        int exitVal = -1;
        try {
            exitVal = runApplication("phylip dnapars", "<input.dat> screenout &");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return exitVal;
    }

    /**
     * Runs the dnadist application.
     * 
     * @return Integer exit value.
     */
    public int runDNADist() {
        int exitVal = -1;
        try {
            exitVal = runApplication("phylip dnadist", "<input.dat> screenout &");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return exitVal;
    }

    /**
     * Runs the Neighbor application.
     * 
     * @return Integer exit value.
     */
    public int runNJ() {
        int exitVal = -1;
        try {
            exitVal = runApplication("phylip neighbor", "<input.dat> screenout &");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return exitVal;
    }

    /**
     * Runs the Drift Confidence Interval application.
     * 
     * @return Integer exit value.
     */
    public int runDriftCI() {
        int exitVal = -1;
        try {
            exitVal = runApplication(cmd + "driftCI" + ext);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return exitVal;
    }

    /**
     * Runs the Npop Confidence Interval application.
     * 
     * @return Integer exit value.
     */
    public int runNpopCI() {
        int exitVal = -1;
        try {
            exitVal = runApplication(cmd + "npopCI" + ext);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return exitVal;
    }

    /**
     * Runs the Omega Confidence Interval application.
     * 
     * @return Integer exit value.
     */
    public int runOmegaCI() {
        int exitVal = -1;
        try {
            exitVal = runApplication(cmd + "omegaCI" + ext);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return exitVal;
    }

    /**
     * Runs the Sigma Confidence Interval application.
     * 
     * @return Integer exit value.
     */
    public int runSigmaCI() {
        int exitVal = -1;
        try {
            exitVal = runApplication(cmd + "sigmaCI" + ext);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return exitVal;
    }

    /**
     * Generates a random number for the pcrerror
     * file each time binning is run.
     */
    private void makeRandPCRError() {
        try {
            BufferedReader input = new BufferedReader(new FileReader(this.pcrerror));
            String pcrerrorVal = input.readLine();
            input.close();
            long randNumSeed = (long) (100000000 * Math.random());
            if (randNumSeed % 2 == 0) {
                randNumSeed++;
            }
            String randSeedLine = randNumSeed + "     random number seed odd integer";
            BufferedWriter output = new BufferedWriter(new FileWriter(this.pcrerror));
            output.write(pcrerrorVal);
            output.newLine();
            output.write(randSeedLine);
            output.newLine();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Change the PCRError value in the pcrerror.dat file.
     * 
     * @param newVal the new value to be assigned to pcrerror.
     */
    public void changePCRError(double newVal) {
        try {
            BufferedReader input = new BufferedReader(new FileReader(this.pcrerror));
            input.readLine();
            String randValue = input.readLine();
            input.close();
            BufferedWriter output = new BufferedWriter(new FileWriter(this.pcrerror));
            output.write(newVal + "  pcrerror.dat");
            output.newLine();
            output.write(randValue);
            output.newLine();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
