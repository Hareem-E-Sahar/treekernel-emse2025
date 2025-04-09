import java.io.*;

public class RuntimeExec {

    public static void main(String[] args) {
        System.out.println("Current directory " + System.getProperty("user.dir"));
        String script = "./testruntimeexec.sh";
        try {
            int exitCode;
            if (args.length > 1) {
                exitCode = new Executor(script).exec();
            } else {
                exitCode = exec(script);
            }
            System.out.println("exec=" + exitCode);
            System.exit(exitCode);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(1);
    }

    public static int exec(String script) throws IOException, InterruptedException {
        Process shell;
        shell = Runtime.getRuntime().exec(script);
        BufferedReader in = new BufferedReader(new InputStreamReader(shell.getInputStream()));
        String line = null;
        while ((line = in.readLine()) != null) {
            System.out.println(line);
        }
        return shell.waitFor();
    }
}
