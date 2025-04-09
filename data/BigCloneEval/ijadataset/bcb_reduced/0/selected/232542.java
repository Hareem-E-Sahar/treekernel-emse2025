package dplayer.ext.linux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.log4j.Logger;
import dplayer.ext.ExtException;
import dplayer.gui.i18n.I18N;

public class LinuxExt {

    protected static final Logger logger = Logger.getLogger(LinuxExt.class);

    public static ExecResult exec(final String command) throws ExtException {
        try {
            logger.debug("Running [" + command + "]...");
            final Process p = Runtime.getRuntime().exec(command);
            final StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream());
            final StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream());
            errorGobbler.start();
            outputGobbler.start();
            final int exitCode = p.waitFor();
            logger.debug("  command terminated with exit code " + exitCode);
            return new ExecResult(outputGobbler.getBuffer(), errorGobbler.getBuffer(), exitCode);
        } catch (Throwable t) {
            logger.debug(t);
            throw new ExtException(I18N.get("ERROR_LINUX_EXEC_FAILED", "Error while running command '{0}'.", new String[] { command }), t);
        }
    }

    public static boolean isRunning() throws ExtException {
        final ExecResult er = exec("ps aux");
        return er.getExitCode() == 0 && er.getOutput().contains("dplayer.jar");
    }

    public static class ExecResult {

        private String mOutput;

        private String mError;

        private int mExitCode;

        ExecResult(final String output, final String error, final int ec) {
            mOutput = output;
            mError = error;
            mExitCode = ec;
        }

        public String getOutput() {
            return mOutput;
        }

        public String getError() {
            return mError;
        }

        public int getExitCode() {
            return mExitCode;
        }
    }

    private static class StreamGobbler extends Thread {

        private InputStream mInputStream;

        private StringBuffer mBuffer;

        StreamGobbler(final InputStream is) {
            mInputStream = is;
            mBuffer = new StringBuffer();
        }

        public void run() {
            try {
                final BufferedReader br = new BufferedReader(new InputStreamReader(mInputStream));
                String line = null;
                while ((line = br.readLine()) != null) {
                    mBuffer.append(line);
                    mBuffer.append('\n');
                }
            } catch (IOException ioe) {
            }
        }

        String getBuffer() {
            while (isAlive()) {
                try {
                    sleep(10);
                } catch (InterruptedException e) {
                }
            }
            return mBuffer.toString();
        }
    }
}
