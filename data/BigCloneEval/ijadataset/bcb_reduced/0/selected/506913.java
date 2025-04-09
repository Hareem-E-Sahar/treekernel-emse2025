package org.mikha.utils.io;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

/**
 * Collection of synchronous I/O-related utility functions.
 * @author mikha
 * @author evgenia
 */
public class IoUtils {

    /** Contains result of execution */
    public static final class ExecutionResult {

        private final int exitCode;

        private final List<String> output;

        private final List<String> error;

        public ExecutionResult(int exitCode, List<String> output, List<String> error) {
            this.exitCode = exitCode;
            this.output = output;
            this.error = error;
        }

        public int getExitCode() {
            return this.exitCode;
        }

        public List<String> getOutput() {
            return this.output;
        }

        public List<String> getError() {
            return this.error;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("exitCode=").append(exitCode);
            sb.append("\noutput=[\n");
            for (String s : output) {
                sb.append(s);
                sb.append('\n');
            }
            sb.append("]\nerror=[\n");
            for (String s : error) {
                sb.append(s);
                sb.append('\n');
            }
            sb.append("]\n");
            return sb.toString();
        }
    }

    /** Helper class to capture input stream into list of strings */
    public static final class IndependentStreamReader extends Thread {

        /** stream to read from */
        private final InputStream input;

        /** list that captures result */
        private final List<String> result = new LinkedList<String>();

        /** is reading finished? */
        private boolean finished = false;

        /**
         * Constructor.
         * @param input
         */
        public IndependentStreamReader(InputStream input) {
            super(input.toString());
            this.input = input;
            setDaemon(true);
            start();
        }

        @Override
        public void run() {
            BufferedReader r = new BufferedReader(new InputStreamReader(input));
            try {
                String s;
                while ((s = r.readLine()) != null) {
                    result.add(s);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                silentClose(r);
            }
            synchronized (this) {
                finished = true;
                notifyAll();
            }
        }

        /**
         * Wait till this reader finishes reading (if necessary) and returns
         * list of read strings.
         * @return list of read strings
         * @throws InterruptedException if interrupted
         */
        public synchronized List<String> getResult() throws InterruptedException {
            while (!finished) {
                wait();
            }
            return result;
        }
    }

    /**
     * Closes given {@link Closeable} object, suppresses exceptions.
     * @param c object to close, can be <code>null</code>
     */
    public static void silentClose(Closeable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (IOException ex) {
        }
    }

    /**
     * Executes given command and waits for completion.
     * @param dir directory to execute in
     * @param cmd command to execute
     * @param args command line arguments
     * @return result object
     * @throws IOException if failed to execute
     */
    public static ExecutionResult execute(File dir, String cmd, String... args) throws IOException {
        String[] command = new String[1 + args.length];
        command[0] = cmd;
        System.arraycopy(args, 0, command, 1, args.length);
        return execute(dir, command);
    }

    /**
     * Executes given command and waits for completion.
     * @param dir directory to execute in
     * @param args command line arguments
     * @return result object
     * @throws IOException if failed to execute
     */
    public static ExecutionResult execute(File dir, String... args) throws IOException {
        Process p = Runtime.getRuntime().exec(args, null, dir);
        IndependentStreamReader os = new IndependentStreamReader(p.getInputStream());
        IndependentStreamReader es = new IndependentStreamReader(p.getErrorStream());
        try {
            int code = p.waitFor();
            return new ExecutionResult(code, os.getResult(), es.getResult());
        } catch (InterruptedException e) {
            IOException ioe = new IOException("Unexpecetd interruption");
            ioe.initCause(e);
            throw ioe;
        }
    }

    /**
     * Reads file to the byte array.
     * @param file file to read
     * @return array of bytes
     * @throws IOException if an error occurs
     */
    public static byte[] readFileToByteArray(File file) throws IOException {
        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            throw new IOException(String.format("Cannot read file \"%s\": %d bytes is way too much", file.getName(), length));
        }
        byte[] bytes = new byte[(int) length];
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) > 0) {
                offset += numRead;
            }
            if (offset < bytes.length) {
                throw new IOException(String.format("Cannot read file \"%s\": read only %d bytes of %d", file.getName(), offset, length));
            }
            return bytes;
        } finally {
            silentClose(is);
        }
    }

    /**
     * Read file into one string using system-default charset.
     * @param file file to read
     * @return file contents as one string
     * @throws IOException if failed to read file
     */
    public static String readFileToString(File file) throws IOException {
        return new String(readFileToByteArray(file));
    }
}
