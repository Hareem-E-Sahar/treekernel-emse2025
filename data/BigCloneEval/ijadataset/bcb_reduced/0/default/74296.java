import iwork.eheap2.*;
import java.net.*;
import java.io.*;

public class EHtoAppleScript {

    private EventHeap eheap;

    public String hostname;

    public static String SCRIPT_FILE_NAME = "script.txt";

    private Event latest = null;

    public EHtoAppleScript(String eheapName) throws UnknownHostException {
        this(eheapName, (InetAddress.getLocalHost()).getHostName());
    }

    public EHtoAppleScript(String eheapName, String machineName) {
        eheap = new EventHeap(eheapName);
        hostname = machineName;
        System.out.println("Launching EHtoAppleScript on " + hostname);
    }

    public boolean verifyPermission(Event e) {
        return true;
    }

    public void processEvents() {
        try {
            Event e = new Event("AppleScript");
            e.addField("script", FieldType.STRING, FieldValueTypes.FORMAL, FieldValueTypes.FORMAL);
            e.addField("Target", hostname);
            Event[] events = new Event[1];
            events[0] = e;
            eheap.registerForEvents(events, new EHListener());
            while (true) {
                if (latest != null) {
                    synchronized (eheap) {
                        if (verifyPermission(latest)) {
                            execute(latest.getPostValueString("script"));
                        }
                        latest = null;
                    }
                } else {
                    Thread.sleep(100);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute(String script) {
        File scriptFile = new File(SCRIPT_FILE_NAME);
        try {
            BufferedWriter bufWrtr = new BufferedWriter(new FileWriter(scriptFile));
            String line = null;
            bufWrtr.write(script);
            bufWrtr.close();
            String exec = "osascript " + SCRIPT_FILE_NAME;
            System.out.println(exec);
            Process proc = Runtime.getRuntime().exec(exec);
            DataInputStream in = new DataInputStream(proc.getInputStream());
            String str;
            while ((str = in.readLine()) != null) {
                System.out.println(str);
            }
            proc.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class EHListener extends Thread implements EventCallback {

        public boolean returnEvent(Event retEvents[]) {
            synchronized (eheap) {
                latest = retEvents[0];
            }
            return true;
        }
    }

    public static void main(String[] args) {
        EHtoAppleScript as;
        if (args.length == 1) {
            try {
                as = new EHtoAppleScript(args[0]);
                as.processEvents();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (args.length == 2) {
            as = new EHtoAppleScript(args[0], args[1]);
            as.processEvents();
        } else {
            System.out.println("Usage: EHtoAppleScript <event heap name> [machine name]");
            System.exit(-1);
        }
    }
}
