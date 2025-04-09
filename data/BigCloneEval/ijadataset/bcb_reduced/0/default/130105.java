import java.io.*;
import java.lang.Process;
import java.lang.Runtime;
import java.util.*;

public class ProcessTest {

    public static void main(String[] args) {
        ArrayList<String> firstTest = new ArrayList<String>();
        firstTest.add("pwd");
        runCommand(firstTest);
        ArrayList<String> secondTest = new ArrayList<String>();
        secondTest.add("ls");
        secondTest.add("-l");
        runCommand(secondTest);
    }

    public static void runCommand(ArrayList<String> cmd) {
        System.out.println("Running " + cmd);
        try {
            Process process = new ProcessBuilder().command(cmd).redirectErrorStream(true).start();
            try {
                InputStream in = process.getInputStream();
                OutputStream out = process.getOutputStream();
                BufferedReader bfrd = new BufferedReader(new InputStreamReader(in));
                String prnt = bfrd.readLine();
                while (prnt != null) {
                    System.out.println(prnt);
                    prnt = bfrd.readLine();
                }
            } catch (Exception ex) {
                System.out.println("Error.");
            } finally {
                process.destroy();
            }
        } catch (IOException io) {
            System.out.println("I/O Error.");
        }
    }
}
