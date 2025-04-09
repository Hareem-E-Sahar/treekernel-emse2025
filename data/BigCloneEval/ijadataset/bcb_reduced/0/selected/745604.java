package at.fhjoanneum.aim.sdi.project.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import at.fhjoanneum.aim.sdi.project.exceptions.ApacheFSAccessDenyException;
import at.fhjoanneum.aim.sdi.project.exceptions.ApacheSVNRuntimeException;

public class SystemCommander {

    public static void executeCommand() throws ApacheSVNRuntimeException {
        Runtime myRun = Runtime.getRuntime();
        try {
            URL temp = new SystemCommander().getClass().getResource("");
            String help = temp.getPath();
            help = help.substring(0, help.indexOf("classes/")) + "scripts/restart_apache.bat";
            if (System.getProperty("os.name").contains("Win")) {
                Process process = myRun.exec(help);
                InputStream in = process.getInputStream();
                process.getInputStream();
                process.getOutputStream();
                process.getErrorStream();
                InputStreamReader reader = new InputStreamReader(in);
                in.close();
                reader.close();
            } else if (System.getProperty("os.name").contains("nux")) {
                Process process = myRun.exec(help);
                InputStream in = process.getInputStream();
                process.getInputStream();
                process.getOutputStream();
                process.getErrorStream();
                InputStreamReader reader = new InputStreamReader(in);
                in.close();
                reader.close();
            }
        } catch (IOException e) {
            String message = e.getClass().getCanonicalName() + " " + e.getLocalizedMessage();
            for (StackTraceElement element : e.getStackTrace()) {
                message += element.toString();
            }
            GlobalProperties.getMyLogger().severe(message);
            throw new ApacheSVNRuntimeException("Failed to restart Apache Server! Restart manually!\n" + e.getMessage());
        }
    }

    public static void chmodUserToRepo(String repoName) throws ApacheFSAccessDenyException {
        Runtime myRun = Runtime.getRuntime();
        try {
            if (System.getProperty("os.name").contains("Win")) {
                Process process = myRun.exec("");
                process.getInputStream().close();
                process.getOutputStream().close();
                process.getErrorStream().close();
            } else if (System.getProperty("os.name").contains("nux")) {
                Process process = myRun.exec("chown " + SVNPropertyLoader.getApacheUser() + ":" + SVNPropertyLoader.getApacheUser() + repoName);
                process.getInputStream().close();
                process.getOutputStream().close();
                process.getErrorStream().close();
            }
        } catch (IOException e) {
            String message = e.getClass().getCanonicalName() + " " + e.getLocalizedMessage();
            for (StackTraceElement element : e.getStackTrace()) {
                message += element.toString();
            }
            GlobalProperties.getMyLogger().severe(message);
            throw new ApacheFSAccessDenyException(e.getMessage());
        }
    }
}
