import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.StringTokenizer;
import fi.hip.gb.data.EventData;
import fi.hip.gb.data.TextualEventData;
import fi.hip.gb.mobile.*;
import fi.hip.gb.mobile.AgentApi;
import fi.hip.gb.mobile.Combiner;
import fi.hip.gb.mobile.DefaultProcessor;
import fi.hip.gb.mobile.Job;
import fi.hip.gb.mobile.Observer;
import fi.hip.gb.mobile.Processor;

/**
 * Agent for executing shell command and returning it's outputs. Uses
 * java's Runtime-class for execution.
 * <p>
 * <b>Description of flags:</b><br>
 * DIRECTORY=directory where commands are executed, if null the
 * default working directory is used
 * <p>
 * <b>Description of parameters:</b><br>
 * Every parameter must be a valid shell command which prints out
 * the the wanted results into console.
 * <p>
 * Currently works only with Linux/Unix systems. 
 * 
 * @author Juho Karppinen
 * @version $Id: Shell.java 102 2004-11-12 14:31:37Z jkarppin $
 */
public final class Shell extends DefaultProcessor implements Job {

    /**
	 * @see fi.hip.gb.mobile.DefaultProcessor#DefaultProcessor(AgentApi)
	 */
    public Shell(AgentApi api) {
        super(api);
    }

    /**
	 * Executes and captures the output of shell command
	 * 
	 * @see fi.hip.gb.mobile.Processor#processEvent(fi.hip.gb.data.EventData)
	 */
    public void processEvent(final EventData d) throws RemoteException, Exception {
        TextualEventData se = (TextualEventData) d;
        StringBuffer result = new StringBuffer();
        String parameters = se.getData();
        StringTokenizer st = new StringTokenizer(parameters, " ", false);
        String[] cmd = new String[st.countTokens()];
        int pos = 0;
        while (st.hasMoreTokens()) cmd[pos++] = st.nextToken();
        if (cmd.length == 0) throw new RemoteException("No command given");
        String[] env = new String[] {};
        String defaultPath = se.getFlags().getProperty("PATH", _api.getDirectory());
        File workDir = new File(defaultPath);
        if (workDir.exists() == false) workDir = new File(_api.getDirectory());
        String outLine;
        Process p = Runtime.getRuntime().exec(cmd, env, workDir);
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((outLine = input.readLine()) != null) {
            result.append(outLine).append("\n");
        }
        input.close();
        int index = -1;
        if ((index = parameters.indexOf(" ")) != -1) parameters = parameters.substring(0, index);
        parameters = parameters.replaceAll("/", "_");
        _results.insertResult(parameters, "Parameters were: " + se.getData(), result.toString());
    }

    public String getDescription() {
        return "Executes shell commands and returns their outputs.";
    }

    public String[] getSupportedParameters() {
        return new String[] { "a valid shell command" };
    }

    public Object[][] getSupportedFlags() {
        return new Object[][] { { "DIRECTORY", (Object) new String[] { "" }, "directory where commands are executed" } };
    }

    /**
	 * @see fi.hip.gb.mobile.Job#getProcessor()
	 */
    public Processor getProcessor() {
        return this;
    }

    /**
	 * @see fi.hip.gb.mobile.Job#getObserver()
	 */
    public Observer getObserver() {
        return new TextObserver(_api);
    }

    /**
	 * @see fi.hip.gb.mobile.Job#getCombiner()
	 */
    public Combiner getCombiner() {
        return new DefaultCombiner(_api);
    }
}
