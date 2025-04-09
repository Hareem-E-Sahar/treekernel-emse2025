import java.io.*;
import java.util.*;

/**
 * Holds the static executable methods for the cohan program
 * 
 * @author Andrew Warner 
 * @version 1.0
 */
public class Execs {

    private static File pcrerror = new File("pcrerror.dat");

    /**
     * Only used from static context; no constructor
     */
    public Execs() {
    }

    /**
     * Runs the initial Fred method to find a good value with which to do
     * hill climbing.
     * @post acinaspolz has been run
     */
    public static void runFred() throws InterruptedException {
        StreamGobbler errorGobbler, outputGobbler;
        String acinaspolz = "cmd /c fredMethod.exe";
        try {
            Process p;
            p = Runtime.getRuntime().exec(acinaspolz);
            errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
            outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT");
            errorGobbler.start();
            outputGobbler.start();
            int exitVal = p.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Runs the binning process. (previously the batch file from the
     * "Binning Program" folder was used for this purpose
     * @pre sequencesfasta and numbers.dat are formatted correctly.  Also
     * the programs removegaps3000.exe, readsynec.exe, etc must all be in the
     * root folder
     * @post output.dat contains the binning data
     */
    protected static void runBinning() throws InterruptedException {
        StreamGobbler errorGobbler;
        StreamGobbler outputGobbler;
        makeRandPCRError();
        String removeGaps = "cmd /c removegaps3000.exe";
        try {
            Process a;
            a = Runtime.getRuntime().exec(removeGaps);
            errorGobbler = new StreamGobbler(a.getErrorStream(), "ERROR");
            outputGobbler = new StreamGobbler(a.getInputStream(), "OUTPUT");
            errorGobbler.start();
            outputGobbler.start();
            int exitVal = a.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String readSynec = "cmd /c readsynec3000.exe";
        try {
            Process b;
            b = Runtime.getRuntime().exec(readSynec);
            errorGobbler = new StreamGobbler(b.getErrorStream(), "ERROR");
            outputGobbler = new StreamGobbler(b.getInputStream(), "OUTPUT");
            errorGobbler.start();
            outputGobbler.start();
            int exitVal = b.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String correctPcr = "cmd /c correctpcr3000.exe";
        try {
            Process c;
            c = Runtime.getRuntime().exec(correctPcr);
            errorGobbler = new StreamGobbler(c.getErrorStream(), "ERROR");
            outputGobbler = new StreamGobbler(c.getInputStream(), "OUTPUT");
            errorGobbler.start();
            outputGobbler.start();
            int exitVal = c.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String divergencematrix = "cmd /c divergencematrix3000.exe";
        try {
            Process d;
            d = Runtime.getRuntime().exec(divergencematrix);
            errorGobbler = new StreamGobbler(d.getErrorStream(), "ERROR");
            outputGobbler = new StreamGobbler(d.getInputStream(), "OUTPUT");
            errorGobbler.start();
            outputGobbler.start();
            int exitVal = d.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String binningdanny = "cmd /c binningdanny.exe";
        try {
            Process e;
            e = Runtime.getRuntime().exec(binningdanny);
            errorGobbler = new StreamGobbler(e.getErrorStream(), "ERROR");
            outputGobbler = new StreamGobbler(e.getInputStream(), "OUTPUT");
            errorGobbler.start();
            outputGobbler.start();
            int exitVal = e.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Runs the hillclimb program, hillclimb.exe
     */
    public static void runHillClimb() throws InterruptedException {
        StreamGobbler errorGobbler, outputGobbler;
        String hillclimb = "cmd /c hillclimb.exe";
        try {
            Process p;
            p = Runtime.getRuntime().exec(hillclimb);
            errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
            outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT");
            errorGobbler.start();
            outputGobbler.start();
            int exitVal = p.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the tree using NJPlot so that the user can see it to start
     * analysis
     * @param treeFile the tree file to open in NJPlot
     */
    public static void openTree(String treeFile) {
        StreamGobbler errorGobbler, outputGobbler;
        String NJPlotCMD = "cmd /c njplot \"" + treeFile + "\"";
        try {
            Process p;
            p = Runtime.getRuntime().exec(NJPlotCMD);
            errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
            outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT");
            errorGobbler.start();
            outputGobbler.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Runs the dnapars program, dnapars.exe, assuming that input is in
     * "infile,"; output will be in "outtree"
     */
    public static void runDNAPars() {
        StreamGobbler errorGobbler, outputGobbler;
        String DNAPars = "cmd /c rundnapars";
        try {
            Process p;
            p = Runtime.getRuntime().exec(DNAPars);
            errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
            outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT");
            errorGobbler.start();
            outputGobbler.start();
            int exitVal = p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Runs the dnadist program, dnadist.exe, assuming that input is in
     * "infile,"; output will be in "outtree"
     */
    public static void runDNADist() {
        StreamGobbler errorGobbler, outputGobbler;
        String DNADist = "cmd /c rundnadist";
        try {
            Process p;
            p = Runtime.getRuntime().exec(DNADist);
            errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
            outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT");
            errorGobbler.start();
            outputGobbler.start();
            int exitVal = p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Runs the neighbor program, neighbor.exe, assuming that input is in
     * "infile,"; output will be in "outtree"
     */
    public static void runNJ() {
        StreamGobbler errorGobbler, outputGobbler;
        String neighbor = "cmd /c runneighbor";
        try {
            Process p;
            p = Runtime.getRuntime().exec(neighbor);
            errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
            outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT");
            errorGobbler.start();
            outputGobbler.start();
            int exitVal = p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates a random number for the pcrerror file each time binning is
     * run
     */
    private static void makeRandPCRError() {
        try {
            BufferedReader input = new BufferedReader(new FileReader(pcrerror));
            String pcrerrorVal = input.readLine();
            long randNumSeed = (long) (100000000 * Math.random());
            if (randNumSeed % 2 == 0) randNumSeed++;
            String randSeedLine = randNumSeed + "     random number seed " + "odd integer";
            input.close();
            BufferedWriter output = new BufferedWriter(new FileWriter(pcrerror));
            output.write(pcrerrorVal);
            output.newLine();
            output.write(randSeedLine);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Change the PCRError value in the pcrerror.dat file
     * @param newVal the new value to be assigned to pcrerror
     */
    public static void changePCRError(double newVal) {
        try {
            BufferedReader input = new BufferedReader(new FileReader(pcrerror));
            input.readLine();
            String randValue = input.readLine();
            input.close();
            BufferedWriter output = new BufferedWriter(new FileWriter(pcrerror));
            output.write(newVal + "  pcrerror.dat");
            output.newLine();
            output.write(randValue);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
