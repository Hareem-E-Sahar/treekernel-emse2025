import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Executor {

    class ReaderThread extends Thread {

        private BufferedReader in;

        private PrintWriter logfile;

        private String name;

        private boolean done = false;

        /**
         * Creates a new ReaderThread object. redirects the I/O streams of the console call to a logfile in a different
         * thread.
         * 
         * @param name
         *           kind of stream "ERROR" or "OUT"
         * @param in
         *           stream to the java class, will be redirected
         * @param logfile
         *           to this logfile
         */
        ReaderThread(String name, InputStream in, PrintWriter logfile) {
            this.in = new BufferedReader(new InputStreamReader(in));
            this.logfile = logfile;
            this.name = name;
        }

        /**
         * DOCUMENT ME!
         */
        public void run() {
            synchronized (name) {
                done = false;
                try {
                    String line;
                    while (true) {
                        line = in.readLine();
                        if (line == null) {
                            break;
                        }
                        logfile.println(line);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace(logfile);
                }
                logfile.flush();
                done = true;
                name.notify();
            }
        }

        public void waitFor() throws InterruptedException {
            synchronized (name) {
                if (!done) name.wait();
            }
        }
    }

    String baseDir = System.getProperty("user.dir");

    String command;

    private List env = new ArrayList();

    private boolean includeDel;

    private PrintStream out = System.out;

    /**
     * @param baseDir
     * @param command
     * @param out
     */
    public Executor(String baseDir, String command, PrintStream out) {
        super();
        this.baseDir = baseDir;
        this.command = command;
        this.out = out;
    }

    public Executor(String command) {
        super();
        this.command = command;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public String[] getEnvForExec() {
        return (String[]) env.toArray(new String[0]);
    }

    /**
     * @return Returns the commandline.
     */
    public String getCommandline() {
        return command;
    }

    protected String[] getCommandLineTokens() {
        String line = getCommandline();
        List list = new ArrayList();
        int idx = 0;
        boolean in = false;
        StringBuffer sb = new StringBuffer();
        char c;
        String token;
        for (int i = 0; i < line.length(); i++) {
            c = line.charAt(i);
            if (c == '\"' || c == '\'') {
                if (includeDel) sb.append(c);
                if (in) {
                    list.add(sb.toString());
                    sb.setLength(0);
                    in = false;
                } else in = true;
            } else if ((Character.isSpaceChar(c) || Character.isWhitespace(c)) && !in) {
                token = sb.toString().trim();
                if (token.length() > 0) {
                    list.add(token);
                }
                sb.setLength(0);
            } else sb.append(c);
        }
        token = sb.toString().trim();
        if (token.length() > 0) {
            list.add(token);
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    public int exec() throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(getCommandline());
        PrintWriter logfile = new PrintWriter(out);
        ReaderThread err = new ReaderThread("ERROR", process.getErrorStream(), logfile);
        ReaderThread out = new ReaderThread("OUT", process.getInputStream(), logfile);
        err.start();
        out.start();
        int exitVal = process.waitFor();
        err.waitFor();
        out.waitFor();
        return exitVal;
    }
}
