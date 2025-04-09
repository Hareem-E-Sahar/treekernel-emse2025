import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.HashMap;

/**
 * Uses the Ping command native to windows to collect the average latency and
 * the packetloss to a certain target.
 * 
 */
public class WinPing extends AbstractPing {

    public WinPing(HashMap paramMap) {
        super.target = (String) paramMap.get("target");
        if (paramMap.get("repeats") != null) {
            super.repeats = Integer.parseInt((String) paramMap.get("repeats"));
        }
        if (paramMap.get("packetsize") != null) {
            super.packetSize = (String) paramMap.get("packetsize");
        }
    }

    /**
     * Executes the test and stores the result in the super.result HashMap 
     */
    public void run() {
        try {
            InetAddress addr = InetAddress.getByName(super.target);
            String line = null;
            Debug.log("Windows Ping test", "Executing command '" + "ping -n " + super.repeats + " -l " + super.packetSize + " " + super.target + "'");
            Process p = Runtime.getRuntime().exec("ping -n " + super.repeats + " -l " + super.packetSize + " " + super.target);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = in.readLine()) != null) {
                Debug.log("Windows Ping test", "    " + line);
                super.output.add(line);
            }
            String packetlossLine = super.output.get(super.output.size() - 6);
            String latencyLine = super.output.get(super.output.size() - 2);
            Debug.log("Windows Ping test", "Probable packetloss line: " + packetlossLine);
            Debug.log("Windows Ping test", "Probable latency line:    " + latencyLine);
            String[] packetLossSplit = packetlossLine.split(",");
            String[] packetLossPercentage = packetLossSplit[2].split(" ");
            super.packetloss = packetLossPercentage[4].substring(1, packetLossPercentage[4].length() - 1);
            Debug.log("Windows Ping test", "Read packetloss: " + packetloss);
            String[] avgLatencySplit = latencyLine.split(" ");
            String latency = avgLatencySplit[12];
            super.avgLatency = latency.substring(0, latency.length() - 2);
            Debug.log("Windows Ping test", "Read latency: " + avgLatency);
            super.result.put("Packetloss", super.packetloss);
            super.result.put("Avg Latency", super.avgLatency);
            Debug.log("Windows Ping test", "Test OK");
        } catch (Exception e) {
            e.printStackTrace();
            super.result.put("Packetloss", "FAIL");
            super.result.put("Avg Latency", "FAIL");
            Debug.log("Windows Ping test", "Test failed");
        }
    }
}
