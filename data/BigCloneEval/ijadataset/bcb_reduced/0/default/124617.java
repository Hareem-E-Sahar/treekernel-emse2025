import java.io.*;
import fibs.FibsAction;

public class TestHeartBeatFibsAction implements fibs.FibsAction {

    private String COMMAND = null;

    private String[] ret;

    private String greeting = "";

    public TestHeartBeatFibsAction() {
        this.COMMAND = "You're away. Please type 'back'";
        this.ret = new String[2];
        this.greeting = System.getProperty("greeting", "");
    }

    public String getGrepString() {
        return this.COMMAND;
    }

    private static String fortuneString = null;

    public String[] action(String command, OutputStream logStream) throws java.io.IOException {
        String logString = "\tHeartBeatAction.action:acknowledged by FIBS\n";
        synchronized (logStream) {
            logStream.write(logString.getBytes(), 0, logString.length());
            logStream.flush();
        }
        try {
            Process p = (Runtime.getRuntime()).exec("fortune");
            BufferedReader in = new BufferedReader((Reader) new InputStreamReader(p.getInputStream()));
            fortuneString = "";
        } catch (IOException ioe) {
        }
        ret[0] = "back";
        if (fortuneString != null) ret[1] = fortuneString; else {
            ret[1] = "tell repbot thump";
        }
        return (ret);
    }
}
