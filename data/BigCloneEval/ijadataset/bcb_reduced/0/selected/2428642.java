package org.tigr.htc.server;

import java.io.File;
import java.lang.reflect.Constructor;
import org.apache.log4j.Logger;
import org.tigr.htc.cmd.Command;
import org.tigr.htc.cmd.CommandSet;
import org.tigr.htc.cmd.CommandType;
import org.tigr.htc.cmd.ICommand;
import org.tigr.htc.common.HTCConfig;
import org.tigr.htc.server.condor.CondorRunner;
import org.tigr.htc.server.mw.Runner;
import org.tigr.htc.server.sge.SgeRunner;

public class RunnerFactory {

    private RunnerFactory() {
    }

    private static Logger logger = Logger.getLogger(RunnerFactory.class);

    /**
	 * The method <code>newRunner</code> creates a new runner class to launch the command
	 * on the grid.
	 * 
	 * @param cmd <code>ICommand</code> class that is to be executed on the grid
	 * @param cwd <code>File</code> class representing the current working directory
	 * 
	 * @return <code>IRunner</code> that can launch the job on the grid
	 * @throws Exception
	 */
    public static IRunner newRunner(ICommand cmd, File cwd) throws Exception {
        logger.debug("Creating runner for Command: " + cmd.getID());
        if (cmd.getType().equals(CommandType.HTC) || cmd.getType().equals(CommandType.INTERNAL)) {
            String className = HTCConfig.getProperty("grid.runner");
            logger.debug("Read grid runner class name: " + className);
            Class[] paramTypes = { Command.class, File.class };
            Class runnerClass = Class.forName(className);
            logger.debug("Obtained class: " + runnerClass.getName());
            Constructor constructor = runnerClass.getConstructor(paramTypes);
            logger.debug("Obtained constructor: " + constructor);
            Object[] params = { (Command) cmd, cwd };
            IRunner runner = (IRunner) constructor.newInstance(params);
            return runner;
        } else if (cmd.getType().equals(CommandType.SSH)) {
            return (IRunner) new SSHRunner((Command) cmd, cwd);
        } else if (cmd.getType().equals(CommandType.SGE)) {
            return (IRunner) new SgeRunner((Command) cmd, cwd);
        } else if (cmd.getType().equals(CommandType.CONDOR)) {
            return (IRunner) new CondorRunner((Command) cmd, cwd);
        } else if (cmd.getType().equals(CommandType.COMPOSITE)) {
            return (IRunner) new CompositeRunner((CommandSet) cmd, cwd);
        } else if (cmd.getType().equals(CommandType.MW)) {
            return (IRunner) new Runner((Command) cmd, cwd);
        } else {
            throw new Exception("unknown request/command type " + cmd.getType());
        }
    }
}
