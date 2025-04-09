import java.util.*;
import java.io.*;
import java.nio.charset.*;

public class SystemWrapper {

    private LinkedList<String> _noutput;

    private int _retval;

    private byte _eol;

    public static String stringArrayToString(String[] sa) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < sa.length; i++) sb.append(sa[i] + " ");
        return sb.toString();
    }

    public SystemWrapper(String commandstring) {
        try {
            String cmdline = stringArrayToString(commandstring.split("\\[\\]"));
            (Megatron.getLog()).debug("Trying to invoke: " + cmdline + "\n");
            Process p = (Runtime.getRuntime()).exec(commandstring.split("\\[\\]"));
            String line;
            _noutput = new LinkedList<String>();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = r.readLine()) != null) _noutput.add(line);
            boolean done = false;
            while (!done) {
                try {
                    _retval = p.waitFor();
                    done = true;
                    (Megatron.getLog()).debug("Process finished: " + cmdline + "\n");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SystemWrapper(String commandstring, String path) {
        try {
            String cmdline = stringArrayToString(commandstring.split("\\[\\]"));
            (Megatron.getLog()).debug("Trying to invoke: " + cmdline + "\n");
            Process p = (Runtime.getRuntime()).exec(commandstring.split("\\[\\]"), null, new File(path));
            String line;
            _noutput = new LinkedList<String>();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = r.readLine()) != null) _noutput.add(line);
            boolean done = false;
            while (!done) {
                try {
                    _retval = p.waitFor();
                    done = true;
                    (Megatron.getLog()).debug("Process finished: " + cmdline + "\n");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean boolval() {
        if (_retval > 0) return false; else return true;
    }

    public int intval() {
        return _retval;
    }

    public String output() {
        return _noutput.pollFirst();
    }

    public LinkedList<String> noutput() {
        return _noutput;
    }
}

;
