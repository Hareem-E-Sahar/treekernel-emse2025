import java.io.*;
import java.util.*;

public class ConsoleEmulator extends Thread {

    private String commandToExecute;

    private String[] arguments = null;

    private static String[] executor = null;

    private Vector<String> commandLine = new Vector<String>();

    private boolean useConsoleInterpreter = false;

    private ProcessMaster myMaster = null;

    public ConsoleEmulator(ProcessMaster cm, String command) {
        commandToExecute = command;
        myMaster = cm;
        if (executor == null) executor = getConsoleInterpreter();
    }

    public ConsoleEmulator(ProcessMaster cm, String command, String[] args) {
        commandToExecute = command;
        myMaster = cm;
        if (executor == null) executor = getConsoleInterpreter();
        arguments = args;
    }

    public ConsoleEmulator(String command, String[] args) {
        commandToExecute = command;
        if (executor == null) executor = getConsoleInterpreter();
        arguments = args;
    }

    public void setCommandToExecute(String newCommand) {
        commandToExecute = newCommand;
    }

    public void setArguments(String[] newArgs) {
        arguments = newArgs;
    }

    public void useCommandInterpreter(boolean iWantToUseIt) {
        useConsoleInterpreter = iWantToUseIt;
    }

    public void run() {
        String outConsole = "";
        try {
            Runtime runtime = Runtime.getRuntime();
            String[] cl = buildCommandLine();
            if (!PatchManager.mute) {
                for (int i = 0; i < cl.length; i++) System.out.println(cl[i] + "__");
                System.out.println("starting the process...");
            }
            final Process process = runtime.exec(cl);
            InputStreamReader in = new InputStreamReader(process.getInputStream());
            in = new InputStreamReader(process.getInputStream(), in.getEncoding());
            BufferedReader reader = new BufferedReader(in);
            String line = "";
            try {
                while ((line = reader.readLine()) != null) {
                    if (myMaster != null) myMaster.writeOnLog("Console : " + line); else if (!PatchManager.mute) System.out.println("Console : " + line);
                    outConsole += line + "\n";
                }
            } finally {
                reader.close();
            }
        } catch (Exception ioe) {
            if (myMaster != null) myMaster.writeOnLog("Console : " + ioe.toString()); else System.out.println("Console : " + ioe.toString());
        }
        if (myMaster != null) {
            myMaster.releaseToken();
            myMaster.writeOnLog("I released a token for having proceeded : " + arguments[0]);
        }
    }

    private String[] buildCommandLine() {
        commandLine.removeAllElements();
        if (useConsoleInterpreter) {
            for (int i = 0; i < executor.length; i++) {
                commandLine.add(executor[i]);
            }
        }
        commandLine.add(commandToExecute);
        if (arguments != null) for (int i = 0; i < arguments.length; i++) commandLine.add(arguments[i]);
        String[] cl = new String[commandLine.size()];
        for (int i = 0; i < cl.length; i++) cl[i] = commandLine.get(i);
        return cl;
    }

    private String[] getConsoleInterpreter() {
        boolean found;
        int possibility = 1;
        String[] res = null;
        Runtime runtime = Runtime.getRuntime();
        do {
            try {
                found = true;
                switch(possibility) {
                    case 1:
                        res = new String[] { "cmd", "/C" };
                        runtime.exec(res);
                        break;
                    case 2:
                        res = new String[] { "command.com", "/C" };
                        runtime.exec(res);
                        break;
                    case 3:
                        res = new String[] { "/bin/sh", "-c" };
                        runtime.exec(res);
                        break;
                    default:
                        possibility = 0;
                }
            } catch (IOException ioe) {
                found = false;
                possibility++;
                res = null;
            }
        } while (!found && possibility != 0);
        return res;
    }
}
