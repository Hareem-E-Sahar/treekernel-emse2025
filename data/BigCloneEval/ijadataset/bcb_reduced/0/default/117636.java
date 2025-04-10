import java.io.*;

public class TestExec {

    public static void main(String args[]) {
        if (args.length < 1) {
            System.out.println("USAGE: java TestExec \"cmd\"");
            System.exit(1);
        }
        try {
            String cmd = args[0];
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(cmd);
            StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERR");
            StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUT");
            errorGobbler.start();
            outputGobbler.start();
            int exitVal = proc.waitFor();
            System.out.println("ExitValue: " + exitVal);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}

class StreamGobbler extends Thread {

    InputStream is;

    String type;

    OutputStream os;

    StreamGobbler(InputStream is, String type) {
        this(is, type, null);
    }

    StreamGobbler(InputStream is, String type, OutputStream redirect) {
        this.is = is;
        this.type = type;
        this.os = redirect;
    }

    public void run() {
        try {
            PrintWriter pw = null;
            if (os != null) pw = new PrintWriter(os);
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                if (pw != null) pw.println(line);
                System.out.println(type + ">" + line);
            }
            if (pw != null) pw.flush();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
