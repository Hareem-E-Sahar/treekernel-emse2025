import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import com.kni.etl.dbutils.ResourcePool;
import com.kni.util.ExternalJarLoader;

/**
 * The Class KETLBootStrap.
 */
public class KETLBootStrap {

    /**
     * The main method.
     * 
     * @param args the args
     */
    public static void main(String[] args) {
        String ketldir = System.getenv("KETLDIR");
        if (ketldir == null) {
            ResourcePool.LogMessage(Thread.currentThread(), ResourcePool.WARNING_MESSAGE, "KETLDIR not set, defaulting to working dir");
            ketldir = ".";
        }
        ExternalJarLoader.loadJars(new File(ketldir + File.separator + "conf" + File.separator + "Extra.Libraries"), "ketlextralibs", ";");
        if ((args.length == 3) && args[2].equalsIgnoreCase("FOREGROUND")) {
            KETLBootStrap.startProcess(args[1], args[0], false);
        } else if (args.length == 2) {
            KETLBootStrap.startProcess(args[1], args[0], true);
        } else if (args.length == 1) {
            KETLBootStrap.startProcess(null, args[0], true);
        } else {
            System.out.println("Syntax Error: <Command> {Working directory} {BACKGROUND|FOREGROUND}");
        }
    }

    /**
     * Start process.
     * 
     * @param strWorkingDirectory the str working directory
     * @param pProcessCommand the process command
     * @param pBackground the background
     * 
     * @return true, if successful
     */
    public static boolean startProcess(String strWorkingDirectory, String pProcessCommand, boolean pBackground) {
        Process pProcess = null;
        boolean bSuccess = true;
        if (strWorkingDirectory == null) {
            strWorkingDirectory = System.getProperty("user.dir");
        }
        try {
            String osName = System.getProperty("os.name");
            String strExecStmt;
            if (osName.startsWith("Windows")) {
                strExecStmt = "cmd.exe /c " + pProcessCommand;
            } else {
                strExecStmt = pProcessCommand;
            }
            File x = new File(strWorkingDirectory);
            pProcess = Runtime.getRuntime().exec(strExecStmt, null, x);
        } catch (Exception e) {
            System.out.println("Error running exec(): " + e.getMessage());
            return false;
        }
        try {
            if (pBackground == false) {
                BufferedReader in = new BufferedReader(new InputStreamReader(pProcess.getInputStream()));
                String currentLine = null;
                while ((currentLine = in.readLine()) != null) System.out.println(currentLine);
                BufferedReader err = new BufferedReader(new InputStreamReader(pProcess.getErrorStream()));
                while ((currentLine = err.readLine()) != null) System.out.println(currentLine);
                int iReturnValue = pProcess.waitFor();
                if (iReturnValue != 0) {
                    bSuccess = false;
                }
            } else {
                bSuccess = true;
            }
        } catch (Exception e) {
            System.out.println("Error in process: " + e.getMessage());
            return false;
        }
        return bSuccess;
    }
}
