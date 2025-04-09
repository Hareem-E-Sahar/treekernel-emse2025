import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * Remote control server.
 */
class Remote extends LocalServer {

    /** Maps commands to execs. */
    private final Map<String, String> commands2execs = new HashMap<String, String>();

    protected boolean showIP() {
        return true;
    }

    /** Executes a command. */
    private class CommandHandler extends AbstractHandler {

        public String handle(Map<String, String> args) {
            String cmd = demand(args, "command");
            String exe = commands2execs.get(cmd.toLowerCase());
            if (Util.isEmpty(exe)) {
                return NO;
            }
            Process procTmp = null;
            try {
                procTmp = Runtime.getRuntime().exec(exe);
            } catch (IOException e) {
                Remote.this.handle(e);
            }
            final Process proc = procTmp;
            final BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            Thread t = new Thread(new Runnable() {

                public void run() {
                    boolean going = true;
                    String line;
                    try {
                        while ((line = in.readLine()) != null) {
                            System.out.println(in.readLine());
                        }
                    } catch (Exception e) {
                        Remote.this.handle(e);
                    }
                    try {
                        System.out.println("trying to kill process");
                        proc.destroy();
                        System.out.println("killed process");
                    } catch (Exception e) {
                        Remote.this.handle(e);
                    }
                }
            });
            t.start();
            try {
                proc.waitFor();
                proc.exitValue();
                t.join();
            } catch (InterruptedException e) {
                Remote.this.handle(e);
            }
            return OK;
        }
    }

    Handler[] getHandlers() {
        return new Handler[] { new GetCommandsHandler(), new CommandHandler() };
    }

    /** Returns '|' delimited list of commands. */
    private class GetCommandsHandler extends AbstractHandler {

        public String handle(Map<String, String> args) {
            note("Showing commands...");
            StringBuffer sb = new StringBuffer();
            for (String cmd : commands2execs.keySet()) {
                if (sb.length() > 0) sb.append("|");
                cmd = cmd.toLowerCase();
                if (cmd.length() == 0) continue;
                char first = Character.toUpperCase(cmd.charAt(0));
                String rest = "";
                if (cmd.length() > 1) {
                    rest = cmd.substring(1);
                }
                cmd = first + rest;
                note("- " + cmd);
                sb.append(cmd);
            }
            return sb.toString();
        }
    }

    protected void processArgs(Iterable<String> args) {
        boolean haveArgs = false;
        for (String arg : args) {
            haveArgs |= addCommandFile(arg);
        }
        if (!haveArgs) {
            addCommandFile(System.getProperty("user.home") + "/.iwebapp");
        }
    }

    private boolean addCommandFile(String fileName) {
        File f = new File(fileName);
        if (!f.exists()) {
            warn(f + " doesn't exist");
            return false;
        }
        if (f.isDirectory()) {
            warn(f + " is a directory");
            return false;
        }
        note("Reading command file " + f);
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(f));
            String line;
            while ((line = in.readLine()) != null) {
                int icomment = line.indexOf("#");
                if (icomment != -1) {
                    line = line.substring(icomment + 1);
                }
                line = line.trim();
                if ("".equals(line)) continue;
                String[] parts = line.split("\\|");
                if (parts.length < 2) continue;
                String cmd = parts[0].toLowerCase();
                String exe = parts[1];
                note("Adding command " + cmd + " -> " + exe);
                commands2execs.put(cmd, exe);
            }
        } catch (IOException e) {
            handle(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ee) {
                    handle(ee);
                }
            }
        }
        return true;
    }

    public static void main(String args[]) {
        new Remote().realMain(args);
    }
}
