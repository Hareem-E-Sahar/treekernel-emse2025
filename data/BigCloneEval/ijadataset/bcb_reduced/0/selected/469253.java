package openvend.lang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import openvend.main.OvLog;
import org.apache.commons.lang.text.StrBuilder;
import org.apache.commons.logging.Log;

/**
 * Executes shell commands.<p/>
 * 
 * @author Thomas Weckert
 * @version $Revision: 1.8 $
 * @see http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=4
 * @since 1.0
 */
public class OvExternalProcess {

    private static Log log = OvLog.getLog(OvExternalProcess.class);

    private String errorOutput;

    private String commandOutput;

    private int exitCode;

    public void exec(String args[], boolean saveOutput) {
        StreamListener errorListener = null;
        StreamListener outputListener = null;
        try {
            if (log.isDebugEnabled()) {
                StrBuilder buf = new StrBuilder();
                for (int i = 0; i < args.length; i++) {
                    buf.append(args[i]);
                    if (i < args.length - 1) {
                        buf.append(" ");
                    }
                }
                log.debug("Executing " + buf.toString());
            }
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(args);
            errorListener = new StreamListener(process.getErrorStream(), "ERROR", saveOutput);
            outputListener = new StreamListener(process.getInputStream(), "OUTPUT", saveOutput);
            errorListener.start();
            outputListener.start();
            exitCode = process.waitFor();
            errorOutput = errorListener.getOutput();
            commandOutput = outputListener.getOutput();
        } catch (Throwable t) {
            if (log.isErrorEnabled()) {
                log.error("Error executing external process!", t);
            }
        } finally {
            errorListener = null;
            outputListener = null;
        }
    }

    public String getCommandOutput() {
        return commandOutput;
    }

    public String getErrorOutput() {
        return errorOutput;
    }

    public int getExitCode() {
        return exitCode;
    }

    class StreamListener extends Thread {

        private InputStream input;

        private String type;

        private boolean saveOutput;

        private StrBuilder output;

        StreamListener(InputStream is, String type, boolean saveOutput) {
            super();
            this.input = is;
            this.type = type;
            this.saveOutput = saveOutput;
            if (this.saveOutput) {
                this.output = new StrBuilder();
            }
        }

        public void run() {
            try {
                InputStreamReader inputReader = new InputStreamReader(input);
                BufferedReader bufferedReader = new BufferedReader(inputReader);
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    if (saveOutput) {
                        output.append(line);
                        output.append("\n");
                    }
                    if (OvExternalProcess.log.isInfoEnabled()) {
                        OvExternalProcess.log.info(type + "> " + line);
                    }
                }
            } catch (IOException e) {
                if (OvExternalProcess.log.isErrorEnabled()) {
                    OvExternalProcess.log.error(e);
                }
            }
        }

        public String getOutput() {
            if (saveOutput) {
                return output.toString();
            }
            return null;
        }
    }
}
