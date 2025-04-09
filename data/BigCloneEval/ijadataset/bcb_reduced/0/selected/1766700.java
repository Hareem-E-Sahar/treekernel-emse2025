package editor.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author mitja
 */
public class ExternalCommand {

    private String command;

    private StringBuffer input = new StringBuffer();

    private StringBuffer error = new StringBuffer();

    /** Creates a new instance of Javac */
    public ExternalCommand(String s) {
        command = s;
    }

    public ExternalCommand() {
    }

    public String getInput() {
        return input.toString();
    }

    public String getError() {
        return error.toString();
    }

    public void runCommand() {
        input = null;
        error = null;
        try {
            Runtime a = Runtime.getRuntime();
            java.lang.Process p = a.exec(command);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            System.out.println("Here is the standard output of the command:\n");
            while ((command = stdInput.readLine()) != null) {
                input.append(command);
            }
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((command = stdError.readLine()) != null) {
                error.append(command);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void runCommand(String s) {
        input.setLength(0);
        error.setLength(0);
        try {
            command = s;
            Runtime a = Runtime.getRuntime();
            java.lang.Process p = a.exec(command);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((command = stdInput.readLine()) != null) {
                System.out.println(command);
                input.append(command + "\n");
            }
            while ((command = stdError.readLine()) != null) {
                System.out.println(command);
                error.append(command + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
