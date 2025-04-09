import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

/**
 * Colander is an application that parses an assignment file containing
 * instructions on accessing various internet resources with various tasks
 * contained in a task directory.
 * <p>
 * This is the main class that executes a run for a given user (account).
 * It is normally called by the ColanderServer class, which provides
 * remote account management and security policies.
 */
class Colander implements Runnable {

    /** The username of the account which invoked this class. */
    String username;

    /** The assignment to be run. */
    String assignmentName;

    /** The name of the file to which run information should be sent. */
    String logFile;

    /** Flag, true if this class is invoked from ColanderLocal. */
    boolean localRun;

    /** Local newline sequence. */
    private String newline;

    /**
    * Initialises the user's logfile and set's the user's account name.
    * <p>
    * A new logfile is overwrites the old logfile on each run.
    */
    Colander(String invoker, String assign, boolean localFlag) {
        newline = System.getProperty("line.separator");
        username = invoker;
        assignmentName = assign;
        localRun = localFlag;
        int endPos = assignmentName.lastIndexOf(".");
        if (endPos == -1) endPos = assignmentName.length();
        logFile = "accounts" + File.separator + username + File.separator + assignmentName.substring(0, endPos) + ".log";
        try {
            String text = new Date() + " commencing run" + newline;
            Files.write(username, logFile, text, "UTF-8", false, true);
        } catch (IOException ex) {
            Files.log("Colander.constructor: " + ex.toString());
        }
    }

    /**
    * Performs the required operations for the Execute task, which
    * is not allowed sufficient security permissions.
    * <p>
    * @param commandLine An Object array containing a single entry, which is
    *    a String of the command line given for execution.
    * @return An Object array containing a String of the resultant standard
    *    output, and a String of the command line given.
    */
    private Object[] exec(Object[] commandLine) {
        Object[] result = new Object[2];
        result[0] = "No command specified";
        result[1] = "Execute";
        if (commandLine == null || commandLine.length < 1) return result;
        String command = (String) commandLine[0];
        result[1] = command;
        String whitelist;
        if (Files.exists("Colander.whitelist") == false) {
            result[0] = "Execution of native commands is disabled";
            return result;
        }
        try {
            whitelist = Files.read("Colander.whitelist", "UTF-8");
        } catch (IOException ex) {
            result[0] = "Error accessing whitelist: " + ex.toString();
            return result;
        }
        boolean match = false;
        StringTokenizer st = new StringTokenizer(whitelist, "\n\r");
        String token;
        while (st.hasMoreTokens()) {
            token = st.nextToken();
            if (token.compareTo(command) == 0) {
                match = true;
                break;
            }
        }
        if (match == false) {
            result[0] = "The specified command is not allowed on this server";
            return result;
        }
        StringBuffer outputString = new StringBuffer();
        try {
            Runtime runtime = Runtime.getRuntime();
            Process p = runtime.exec(command);
            BufferedReader cmdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            BufferedReader cmdOutput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String sErr = cmdError.readLine();
            String sOut = cmdOutput.readLine();
            while (sErr != null || sOut != null) {
                if (sErr != null) {
                    outputString.append(sErr + newline);
                    sErr = cmdError.readLine();
                }
                if (sOut != null) {
                    outputString.append(sOut + newline);
                    sOut = cmdOutput.readLine();
                }
            }
            cmdError.close();
            cmdOutput.close();
        } catch (IOException ex) {
            outputString = new StringBuffer("Execute error: " + ex.toString());
        }
        result[0] = outputString;
        return result;
    }

    /**
    * Appends a text String to the user's current logfile.
    */
    void log(String text) {
        try {
            Files.write(username, logFile, text + newline, "UTF-8", true, true);
        } catch (IOException ex) {
            Files.log("Colander.log: " + ex.toString());
        }
    }

    /**
    * This method runs all of the tasks with the actions and parameters
    * specified in the user's assignment file.
    */
    public void run() {
        Class c;
        Class[] actionTypes, parameterTypes;
        Constructor cc;
        int parameterCount;
        Method method;
        Object[] parameterObjects;
        String actionName, taskName, logText;
        String[] stringParameters;
        StringBuffer logBuffer;
        Task task;
        Assignment asgn = null;
        Document doc = null;
        try {
            asgn = new Assignment(username, assignmentName);
        } catch (IOException ex) {
            Throwable cause = ex.getCause();
            log("   There was a problem accessing your files.");
            log("   Assignment: " + ex.toString());
            if (cause != null) log("      Cause: " + cause.toString());
            return;
        }
        Actions acts = new Actions(this);
        Accounts.setRunStatus(username, "Assignment parsed");
        int taskCount = asgn.getTaskCount();
        for (int tasknum = 1; tasknum <= taskCount; tasknum++) {
            if (Thread.interrupted() == true) return;
            taskName = asgn.getTaskName(tasknum);
            logBuffer = new StringBuffer(taskName + "( ");
            parameterObjects = asgn.getParameters(tasknum, -1);
            parameterCount = parameterObjects.length;
            for (int p = 0; p < parameterCount; p++) {
                logBuffer.append(parameterObjects[p].toString() + ", ");
            }
            logBuffer.append(")");
            log("");
            log(new String(logBuffer));
            if (localRun) System.out.println("   " + logBuffer.toString());
            if (taskName.compareTo("Execute") == 0) {
                parameterCount++;
                parameterObjects = exec(parameterObjects);
            }
            parameterTypes = new Class[parameterCount];
            for (int j = 0; j < parameterCount; j++) parameterTypes[j] = String.class;
            try {
                logText = null;
                c = Class.forName(taskName);
                cc = c.getConstructor(parameterTypes);
                task = (Task) cc.newInstance(parameterObjects);
                doc = task.getResult();
                if (task.getSuccessFlag() == false) logText = "Last status: " + task.getStatus();
            } catch (ClassNotFoundException ex) {
                logText = "Could not find the task";
                log(logText);
                log(ex.toString());
            } catch (InstantiationException ex) {
                logText = "The task couldn't be created";
                log(logText);
                log(ex.toString());
            } catch (NoSuchMethodException ex) {
                logText = "The task tried to call a non-existent method";
                log(logText);
                log(ex.toString());
            } catch (SecurityException ex) {
                logText = "The task tried to do something it isn't allowed to";
                log(logText);
                log(ex.toString());
            } catch (IllegalAccessException ex) {
                logText = "The server is incorrectly configured";
                log(logText);
                log(ex.toString());
            } catch (InvocationTargetException ex) {
                logText = "The task caused a target error";
                log(logText);
                log(ex.toString());
            } catch (RuntimeException ex) {
                logText = "The task generated a runtime exception";
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                log(logText);
                log(ex.toString());
                log("");
                log(sw.toString());
            }
            if (logText != null) {
                try {
                    ResultHelper xml = new ResultHelper("Failed: " + logText, taskName, "text/plain", "Colander Error", "Colander");
                    doc = xml.getDocument();
                } catch (ParserConfigurationException ex) {
                    continue;
                }
                log(logText);
            }
            try {
                acts.setDocument(doc);
                c = acts.getClass();
                if (ServerProperties.isDebugModeSet()) {
                    if (Thread.interrupted() == true) return;
                    String[] sArray = { "debug_" + tasknum + "_result.log" };
                    Class[] cArray = new Class[1];
                    cArray[0] = String.class;
                    method = c.getMethod("toFile", cArray);
                    method.invoke(acts, (Object[]) sArray);
                }
                int actionCount = asgn.getActionCount(tasknum);
                for (int action = 1; action <= actionCount; action++) {
                    if (Thread.interrupted() == true) return;
                    actionName = asgn.getActionName(tasknum, action);
                    stringParameters = asgn.getParameters(tasknum, action);
                    actionTypes = new Class[stringParameters.length];
                    logBuffer = new StringBuffer("   " + actionName + "( ");
                    for (int apc = 0; apc < stringParameters.length; apc++) {
                        actionTypes[apc] = String.class;
                        logBuffer.append(stringParameters[apc] + ", ");
                    }
                    logBuffer.append(")");
                    log(new String(logBuffer));
                    method = c.getMethod(actionName, actionTypes);
                    method.invoke(acts, (Object[]) stringParameters);
                }
            } catch (NoSuchMethodException ex) {
                log("The assignment tried to call a non-existent action");
                log(ex.toString());
            } catch (SecurityException ex) {
                log("The action tried to do something it isn't allowed to");
                log(ex.toString());
            } catch (IllegalAccessException ex) {
                log("The server is incorrectly configured");
                log(ex.toString());
            } catch (InvocationTargetException ex) {
                log("The action caused the following error:");
                log("thrown by: " + ex.getCause().toString());
            } catch (RuntimeException ex) {
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                log("The action generated a runtime exception");
                log(ex.toString());
                log("");
                log(sw.toString());
            }
            Accounts.setRunStatus(username, "Completed " + tasknum + " of " + taskCount + " tasks");
        }
        log("");
        log("All tasks have been run.");
    }
}
