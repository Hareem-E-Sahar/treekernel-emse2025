package sifter.connection;

import sifter.*;
import sifter.rcfile.*;
import sifter.translation.*;
import sifter.ui.*;
import sifter.messages.*;
import net.sourceforge.jmisc.Debug;
import java.io.*;
import java.util.Vector;

/** 
    A connection through the internet.


    @author Fred Gylys-Colwell
    @version $Name:  $, $Revision: 1.5 $
*/
public class Rsh extends TwoWay {

    String command;

    Vector args;

    public Rsh(String host1, String host2) {
        super("rsh", host1, host2);
    }

    public void setCommand(String s) {
        command = s;
    }

    public void setArgs(Vector c) {
        args = c;
    }

    public Process connect() throws IOException {
        Process p = Runtime.getRuntime().exec(command);
        new Warner(p.getErrorStream());
        OutputStream in = p.getOutputStream();
        OutputStreamWriter write = new OutputStreamWriter(in);
        for (int i = 0; i < args.size(); i++) {
            String s = (String) args.elementAt(i);
            if (i + 1 == args.size()) s += "  --passive";
            write.write(s + "\n");
            if (Debug.check(Bug.PASSIVE)) Debug.println(Bug.PASSIVE, "~~~:" + s);
        }
        write.flush();
        return p;
    }

    public static class Warner extends Thread {

        InputStream err;

        String start;

        Warner(InputStream err) {
            this(err, "REMOTE STDERR>>");
        }

        Warner(InputStream err, String start) {
            this.err = err;
            this.start = start;
            start();
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(err));
                String s;
                while (null != (s = in.readLine())) {
                    if (Debug.check(Bug.PASSIVE)) {
                        Debug.println(Bug.PASSIVE, start + s);
                    } else {
                        if ((s.indexOf("Pseudo-terminal will not be allocated") < 0) && (s.indexOf("Debug stream") < 0)) {
                            Main.verify.warn("REMOTE ERROR: " + s, Verify.COMMENT);
                        }
                    }
                }
            } catch (IOException ex) {
                System.err.println("Error reading remote stream: " + ex);
            }
        }
    }
}
