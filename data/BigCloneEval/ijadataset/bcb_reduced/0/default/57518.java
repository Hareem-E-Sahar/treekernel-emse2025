import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import fi.hip.gb.mobile.AgentApi;
import fi.hip.gb.mobile.MobileAgent;

/**
 * Agent for executing shell command and returning it's outputs. Uses
 * java's Runtime-class for execution. Every command must be a valid shell 
 * command which prints out the the wanted results into console.
 * <p>
 * The runtime execution is not suited for all kind of processes. For
 * example, interactive output streams are not supported, such as top command.
 * <p>
 * Example commands:
 * <ul>
 * <li>ls -la  [prints the current working directory on linux]
 * <li>cmd.exe -C dir
 * </ul> 
 * 
 * @author Juho Karppinen
 * @version $Id: Shell.java 1296 2007-01-29 13:12:08Z jkarppin $
 */
@MobileAgent(disabled = false)
public class Shell {

    public Shell() {
    }

    /**
	 * Executes and captures the output of shell command
     * @param command command to execute
     * @param path path to execute the command, null path uses the current working directory
     * @return output from the command
	 */
    public String execute(String command, String path) throws RemoteException, Exception {
        String[] cmd = command.split(" ");
        if (cmd.length == 0) throw new RemoteException("No command given");
        String[] env = new String[] {};
        File workDir = null;
        if (path == null || new File(path).exists() == false) workDir = new File(AgentApi.getAPI().getDirectory()); else workDir = new File(path);
        Process p = Runtime.getRuntime().exec(cmd, env, workDir);
        StreamGobbler stdout = new StreamGobbler(p.getInputStream());
        StreamGobbler stderr = new StreamGobbler(p.getErrorStream());
        stdout.start();
        stderr.start();
        int exitVal = p.waitFor();
        stdout.join(2000);
        stderr.join(2000);
        String result = stdout.sb.toString();
        if (exitVal != 0) {
            result += "\n\nEXITCODE: " + exitVal + "\n";
        }
        if (stderr.sb.length() > 0) {
            result += "\n\nSTDERR:\n" + stderr.sb.toString();
        }
        return result.toString();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("********** Shell *********");
        Shell shell = new Shell();
        if (args.length > 0) {
            String cmd = "";
            for (String a : args) {
                cmd += a + " ";
            }
            System.out.println("Output for " + cmd + "\n\n'" + shell.execute(cmd, null) + "'");
        } else {
            System.out.println("Working directory:\n\n='" + shell.execute("cmd.exe /C dir", null) + "'");
        }
    }

    /**
     * Thread for reading output streams.
     */
    class StreamGobbler extends Thread {

        InputStream is;

        StringBuffer sb = new StringBuffer();

        StreamGobbler(InputStream is) {
            this.is = is;
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) sb.append(line).append("\n");
            } catch (IOException ioe) {
                sb.append("Error while reading output " + ioe.getMessage());
            }
        }
    }
}
