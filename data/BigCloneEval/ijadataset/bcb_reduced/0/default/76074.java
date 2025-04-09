import java.lang.*;
import java.io.*;

public class BioSapThread extends Thread {

    String workingDir;

    String blastCmd = "/package/genome/blast/blastall";

    BioSapThread() {
        workingDir = null;
    }

    BioSapThread(String workingDir) {
        this.workingDir = workingDir;
        if (workingDir.endsWith("/")) {
            this.workingDir = workingDir.substring(0, workingDir.length() - 1);
        }
    }

    public void run() {
        String command = "featurama -m " + workingDir + "/featurama.params";
        System.out.println(execCmd(command));
        Runtime blastingMachine = Runtime.getRuntime();
        int availableProcessors = blastingMachine.availableProcessors();
        String blastParams = "-I -p blastn -a " + availableProcessors + " -i " + workingDir + "/results.fasta";
        String blastParamLine;
        String blastLib;
        try {
            BufferedReader brBlastParams = new BufferedReader(new FileReader(workingDir + "/blast.params"));
            blastParamLine = brBlastParams.readLine();
            while (blastParamLine != null) {
                if ("blast_library=".equals(blastParamLine.substring(0, 14))) {
                    blastLib = blastParamLine.substring(14);
                    blastParams += " -d " + blastLib;
                }
                blastParamLine = brBlastParams.readLine();
            }
            blastParams += " -e " + "1e-4";
        } catch (Exception e) {
            System.err.println("Problem Reading BLAST params. Exception: " + e.toString());
        }
        try {
            FileWriter fwOutput = new FileWriter(workingDir + "/blastResults.ml");
            System.out.println(runBlast(blastCmd + " " + blastParams, new BufferedWriter(fwOutput)));
            fwOutput.close();
        } catch (java.io.IOException e) {
            System.err.println("Problem running/filtering BLAST: " + e.toString());
        }
        FileWriter fwOutput = null;
        try {
            fwOutput = new FileWriter(workingDir + "/BioSapOut.xml");
            fwOutput.write("<?xml version=\"1.0\"?>\n");
            fwOutput.write("<BioSapRun comments=\"");
        } catch (Exception e) {
            System.err.println("Problem writing BioSap xml output: " + e.toString());
        }
        BufferedReader inFile = null;
        String line = null;
        try {
            inFile = new BufferedReader(new FileReader(workingDir + "/comments"));
            line = null;
            line = inFile.readLine();
            while (line != null) {
                fwOutput.write(line.replace('"', '\''));
                line = inFile.readLine();
                if (line != null) {
                    fwOutput.write("\n");
                }
            }
            inFile.close();
        } catch (Exception e) {
            System.err.println("WARNING: comments file not found.");
        } finally {
            try {
                fwOutput.write("\">\n");
                inFile = new BufferedReader(new FileReader(workingDir + "/featurama.ml"));
                while ((line = inFile.readLine()) != null) {
                    fwOutput.write(line + "\n");
                }
                inFile = new BufferedReader(new FileReader(workingDir + "/blastResults.ml"));
                while ((line = inFile.readLine()) != null) {
                    fwOutput.write(line + "\n");
                }
                fwOutput.write("</BioSapRun>\n");
                fwOutput.flush();
                fwOutput.close();
            } catch (Exception ioe) {
                System.err.println("Problem writing BioSap xml output:" + ioe.toString());
            }
        }
        command = "load_BioSapOut.pl --quiet " + workingDir + "/BioSapOut.xml ";
        System.out.println(execCmd(command));
        return;
    }

    private String execCmd(String cmd) {
        String msg = new String("");
        boolean cmdOK = true;
        Process proc = null;
        Runtime runtime = Runtime.getRuntime();
        try {
            System.out.println("COMMAND> " + cmd);
            proc = runtime.exec("nice " + cmd);
            InputStreamReader isrError = new InputStreamReader(proc.getErrorStream());
            BufferedReader brError = new BufferedReader(isrError);
            String line = null;
            while ((line = brError.readLine()) != null) {
                System.out.println("ERROR MSG>" + line);
            }
            InputStreamReader isrOutput = new InputStreamReader(proc.getInputStream());
            BufferedReader brOutput = new BufferedReader(isrOutput);
            line = null;
            while ((line = brOutput.readLine()) != null) {
                System.out.println("OUTPUT>" + line);
            }
            int exitVal = proc.waitFor();
            System.out.println("ExitValue: " + exitVal);
        } catch (Exception e) {
            cmdOK = false;
            msg += "Problem executing command: " + cmd + "  DAMN. Exception " + e.toString();
            System.err.println(msg);
        }
        if (cmdOK) {
            msg += "Command: " + cmd + " OK.";
        }
        return msg;
    }

    public String runBlast(String cmd, BufferedWriter mlBuffWtr) {
        String msg = new String("");
        boolean cmdOK = true;
        Process proc = null;
        Runtime runtime = Runtime.getRuntime();
        try {
            System.out.println("COMMAND> " + cmd);
            proc = runtime.exec("nice " + cmd);
            BlastToXMLConverter blastParser = new BlastToXMLConverter();
            BufferedWriter blastOutWtr = new BufferedWriter(new FileWriter(workingDir + "/blast.out"));
            InputStreamReader isrOutput = new InputStreamReader(proc.getInputStream());
            BufferedReader brOutput = new BufferedReader(isrOutput);
            blastParser.parse(brOutput, mlBuffWtr, blastOutWtr);
            InputStreamReader isrError = new InputStreamReader(proc.getErrorStream());
            BufferedReader brError = new BufferedReader(isrError);
            String line = null;
            while ((line = brError.readLine()) != null) {
                System.out.println("ERROR MSG>" + line);
            }
            int exitVal = proc.waitFor();
            System.out.println("ExitValue: " + exitVal);
        } catch (Exception e) {
            cmdOK = false;
            msg += "Problem executing command: " + cmd + "  DAMN. Exception " + e.toString();
            System.err.println(msg);
        }
        if (cmdOK) {
            msg += "Command: " + cmd + " OK.";
        }
        return msg;
    }
}

;
