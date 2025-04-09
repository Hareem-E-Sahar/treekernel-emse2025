package org.ourgrid.common.executor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import br.edu.ufcg.lsd.commune.container.logging.CommuneLogger;

public class Win32Executor extends AbstractExecutor {

    private static final long serialVersionUID = 33L;

    /** The processes which results were not collected yet. */
    private Map<ExecutorHandle, Process> processes = new TreeMap<ExecutorHandle, Process>();

    /**
	 * Logger to store log messages
	 */
    private final transient CommuneLogger logger;

    /**
	 * The native process abstract representation. It is used to control the
	 * execution of command.
	 */
    private Process process = null;

    /** This is the next handle can be issued. */
    private int nextHandle = 0;

    public Win32Executor(CommuneLogger logger) {
        this.logger = logger;
    }

    public void beginAllocation() throws ExecutorException {
    }

    public void chmod(File file, String mode) throws ExecutorException {
        return;
    }

    /**
	 * Execute a command like a OS native script. This a method used by the
	 * Executor class in order to provide some facilities provided by shell. For
	 * example, wild cards expansion.
	 * 
	 * @param command The command must be executed
	 * @param dirName The directory where the execution will be started.
	 * @return The result (stdout and stderr) of the command execution
	 * @throws ExecutorException If the execution could not be performed.
	 */
    public ExecutorHandle execute(String dirName, String command) throws ExecutorException {
        return execute(dirName, command, new HashMap());
    }

    public ExecutorHandle execute(String dirName, String command, Map envVars) throws ExecutorException {
        ExecutorHandle handle;
        handle = this.getNextHandle();
        File script = createScript(command, dirName, envVars);
        logger.debug("About to invoke cmd /C " + script.getPath() + " command:  " + command);
        try {
            process = Runtime.getRuntime().exec("cmd /C \"" + script.getPath() + "\"");
            this.includeInProcesses(handle, process);
        } catch (IOException e) {
            throw new ExecutorException(command, e);
        }
        script.deleteOnExit();
        return handle;
    }

    public void kill(ExecutorHandle handle) {
        Process processToKill;
        processToKill = this.getProcess(handle);
        if (processToKill != null) {
            processToKill.destroy();
        } else {
            logger.debug("Command kill for handle " + handle.toString() + " is not necessary because this process is already finished.");
        }
    }

    /**
	 * Yet to be implemented... (non-Javadoc)
	 * 
	 * @see org.ourgrid.common.executor.Executor#getResult(org.ourgrid.common.executor.ExecutorHandle)
	 */
    public ExecutorResult getResult(ExecutorHandle handle) throws ExecutorException {
        ExecutorResult result = null;
        Process processToWait = null;
        try {
            processToWait = this.getProcess(handle);
            result = this.catchOutput(processToWait);
            this.removeFromProcesses(handle);
        } catch (InterruptedException e) {
            throw new ExecutorException("Cannot get the result of command execution.", e);
        }
        return result;
    }

    public void finishExecution() throws ExecutorException {
    }

    protected File createScript(String command, String dirName, Map envVars) throws ExecutorException {
        File dir;
        dirName = convert2WinStyle(dirName);
        command = convert2WinStyle(command);
        logger.debug("Creating script on dir..." + dirName + " for command " + command);
        File temporaryScript;
        BufferedWriter bwTemp = null;
        Iterator keys;
        String setCommand = "set ";
        dir = new File(dirName);
        logger.debug("Will create file on dir " + dir + " is Directory: " + dir.isDirectory());
        if (!dirName.equals(".") && !dir.isDirectory()) {
            throw new ExecutorException(command, new FileNotFoundException(dir.getAbsolutePath()));
        }
        logger.debug("Will create file on dir " + dir + " command: " + command);
        new File(dir, command);
        try {
            temporaryScript = File.createTempFile("broker", ".bat", dir);
            temporaryScript.deleteOnExit();
            bwTemp = new BufferedWriter(new FileWriter(temporaryScript));
            bwTemp.write("@echo off");
            bwTemp.newLine();
            if (envVars != null) {
                keys = envVars.keySet().iterator();
                if (keys.hasNext()) {
                    while (keys.hasNext()) {
                        String theKey = (String) keys.next();
                        bwTemp.write(setCommand + " " + theKey + "=" + envVars.get(theKey) + "");
                        bwTemp.newLine();
                    }
                    bwTemp.newLine();
                }
            }
            bwTemp.write("cd " + dirName);
            bwTemp.newLine();
            bwTemp.write(command);
            bwTemp.newLine();
            bwTemp.flush();
            bwTemp.close();
            return temporaryScript;
        } catch (IOException ioe) {
            throw new ExecutorException(command, ioe);
        } finally {
            try {
                if (bwTemp != null) {
                    bwTemp.close();
                }
            } catch (IOException e) {
                logger.debug("Failed to close stream on Exception.");
            }
        }
    }

    protected File createScript(String command, String dirName) throws ExecutorException {
        return this.createScript(command, dirName, null);
    }

    /**
	 * This method is responsible to convert some linux variables and separators
	 * styles to Windows' ones
	 * 
	 * @param inn String containing the text that should be processed and if
	 *        necessary converted to windows style
	 * @return The string converted for windows' format
	 */
    public static String convert2WinStyle(String inn) {
        StringBuffer sb = new StringBuffer(inn);
        Pattern p = Pattern.compile("\\${1}[1-9a-zA-Z]+");
        Matcher m = p.matcher(inn);
        int increased = 0;
        while (m.find()) {
            sb.replace(m.start() + increased, m.start() + increased + 1, "%");
            sb.insert(m.end() + increased, "%");
            increased++;
        }
        return sb.toString().replace('/', '\\');
    }

    /**
	 * This method provide a Thread safe implementation for Map management. The
	 * idea is to protect the cuncurrent modification in the Map.
	 * 
	 * @param handle The handle that identifies the process must be removed from
	 *        Map.
	 */
    private synchronized void removeFromProcesses(ExecutorHandle handle) {
        Process p = this.processes.remove(handle);
        p.destroy();
    }

    /**
	 * This method provides a synchronized access to the Map containning the
	 * Processes.
	 * 
	 * @param handle An identificator for the Process in the Map.
	 * @return An instance of Process identified by the Map.
	 */
    private synchronized Process getProcess(ExecutorHandle handle) {
        return this.processes.get(handle);
    }

    /**
	 * Adds a process into the set of the ones which results were not collected
	 * yet.
	 * 
	 * @param handle The handle for the process
	 * @param process The process to be included at the group
	 */
    protected synchronized void includeInProcesses(ExecutorHandle handle, Process process) {
        this.processes.put(handle, process);
    }

    /**
	 * This method manage the handles issued for each command execution
	 * 
	 * @return A handle to be used by the client to identify its execution
	 */
    protected synchronized ExecutorHandle getNextHandle() {
        IntegerExecutorHandle newHandle = new IntegerExecutorHandle(nextHandle);
        this.nextHandle++;
        return newHandle;
    }
}
