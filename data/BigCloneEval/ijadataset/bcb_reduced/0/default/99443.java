import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Properties;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * The class Ping.java acts as a controller.
 * Depending on the OS the client runs on it will create an Object of
 * PingTestLinux or PingTestWindows and store it in the abstract Object PingTest
 * 
 * Only the run() method which handles the actuall ping and the parsing of the 
 * output are different in these classes. 
 */
public class Ping implements iTest {

    PingTest $selectedTest = null;

    Properties $sysprop = System.getProperties();

    HashMap $params = new HashMap();

    ArrayList<String> $output = new ArrayList();

    public Ping(HashMap paramMap, Properties sysprop) {
        $params = paramMap;
        $sysprop = sysprop;
        defaultParam("count", "4");
        defaultParam("packetSize", "1000");
        defaultParam("target", "127.0.0.1");
        setPingTest();
    }

    public Ping(HashMap paramMap) {
        this(paramMap, System.getProperties());
    }

    public String getParam(String param) {
        return (String) $params.get(param);
    }

    public void defaultParam(String param, String value) {
        if (getParam(param) == null) $params.put(param, value);
    }

    /**
     * Creates an instance of PingTestLinux or PingTestWindows and assigns it to a Object of
     * type PingTest depending on the Operating System.
     */
    public void setPingTest() {
        PingTest[] pingtests = { new PingTestCustom($params), new PingTestLinux($params), new PingTestMac($params), new PingTestWindows($params) };
        Debug.log("Ping test", "Checking what PingTest to run on this platform...");
        for (int i = 0; i < pingtests.length; i++) {
            PingTest pingtest = pingtests[i];
            Debug.log("Ping test", "    Trying " + pingtest);
            if (pingtest.onCorrectPlatform($sysprop)) {
                Debug.log("Ping test", "PingTest '" + pingtest + "' said it can run on this platform");
                $selectedTest = pingtest;
                break;
            }
        }
        Debug.log("Ping test", "Checking done");
    }

    /**
     * Executes the test. Depending on what type of object was assigned to pingTest
     * the run method of either PingTestLinux or WingPing is executed.
     */
    public void run() {
        if ($selectedTest != null) {
            if ($selectedTest.returnsCommand()) {
                Debug.log("Ping test", "Test requires command to be executed.");
                String cmd = $selectedTest.getCommand();
                Debug.log("Ping test", "Executing '" + cmd + "'");
                try {
                    Process p = Runtime.getRuntime().exec(cmd);
                    BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line = "";
                    $output = new ArrayList();
                    while ((line = in.readLine()) != null) {
                        Debug.log("Ping test", "    " + line);
                        $output.add(line);
                    }
                    Debug.log("Ping test", "Test done executing");
                } catch (Exception e) {
                    Debug.log("Ping test", "Execution failed");
                }
            } else {
                Debug.log("Ping test", "Test can run on its own");
                $selectedTest.run();
            }
        } else {
            Debug.log("Ping test", "No test selected, can't run");
        }
    }

    /**
     * 
     * @return a String containing the target of the test
     */
    public String getTarget() {
        return getParam("target");
    }

    /**
     * 
     * @return a HashMap containing the result of the test.
     *  ( the key is the name of the test and the value is the actual result )
     */
    public HashMap getResult() {
        if ($selectedTest != null) {
            if ($selectedTest.returnsCommand()) {
                Debug.log("Ping test", "Test requires command output to be parsed. Trying outputparsers:");
                PingTestOutputParser[] parsers = { new PingTestOutputParser_Windows_XP_NL(), new PingTestOutputParser_Windows_XP_EN(), new PingTestOutputParser_Windows_XP_NO(), new PingTestOutputParser_Windows_Vista_EN(), new PingTestOutputParser_MacOSX_Leopard(), new PingTestOutputParser_Windows_Unknown_EN(), new PingTestOutputParser_Linux() };
                HashMap result = null;
                for (int i = 0; i < parsers.length; i++) {
                    PingTestOutputParser parser = parsers[i];
                    Debug.log("Ping test", "    Trying " + parser);
                    result = parser.parse($output, $sysprop);
                    if (result != null) {
                        Debug.log("Ping test", "Parser was successful, returning result");
                        return result;
                    }
                }
                Debug.log("Ping test", "No parser was able to parse ping output. Please contact the author!");
                return null;
            } else {
                Debug.log("Ping test", "Test ran on its own, asking it for the result");
                return $selectedTest.getResult();
            }
        } else {
            Debug.log("Ping test", "getResult can't return result from non-existant test");
            return null;
        }
    }

    public String toString() {
        return "[Ping test to " + getTarget() + "]";
    }

    public static void main(String args[]) {
        Debug.init();
        Debug.enable();
        Debug.logEnvironment();
        HashMap params = new HashMap();
        Properties p = System.getProperties();
        Ping ping = new Ping(params, p);
        Debug.log("Ping main", ping.toString());
        ping.run();
        ping.getResult();
    }
}
