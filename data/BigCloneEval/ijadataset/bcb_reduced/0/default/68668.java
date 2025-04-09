import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import net.pulseproject.mkinector.josceleton.api.OsceletonProcessStarter;

public class OsceletonProcessStarterImpl implements OsceletonProcessStarter {

    public static void main(final String[] args) {
        new OsceletonProcessStarterImpl().start();
    }

    @Override
    public final void start() {
        executeCommand("/apps/bin/osceleton");
    }

    private static int executeCommand(final String commandName) {
        return executeCommand(commandName, Collections.<String>emptyList());
    }

    private static int executeCommand(final String commandName, final List<String> arguments) {
        System.out.println("executing [" + commandName.toString() + "] with arguments: " + Arrays.toString(arguments.toArray()));
        try {
            final List<String> pbArgs = new LinkedList<String>(arguments);
            pbArgs.add(0, commandName);
            final ProcessBuilder pb = new ProcessBuilder(pbArgs);
            System.out.println("starting ...");
            final Process process = pb.start();
            InputStream inputStream = process.getInputStream();
            InputStream errorStream = process.getErrorStream();
            ThreadedStreamHandler outputStreamHandler = ThreadedStreamHandler.newSimple("OUT", inputStream);
            ThreadedStreamHandler errorStreamHandler = ThreadedStreamHandler.newSimple("ERR", errorStream);
            outputStreamHandler.start();
            errorStreamHandler.start();
            System.out.println("command running");
            final int exitValue = process.waitFor();
            outputStreamHandler.interrupt();
            errorStreamHandler.interrupt();
            outputStreamHandler.join();
            errorStreamHandler.join();
            System.out.println("finished. exitValue: " + exitValue);
            return exitValue;
        } catch (final Exception e) {
            throw new RuntimeException("executing command failed", e);
        }
    }

    private static class ThreadedStreamHandler extends Thread {

        private final String name;

        private final InputStream inputStream;

        private final String interactionInputString;

        private final StringBuilder outputBuffer = new StringBuilder();

        private final OutputStream outputStream;

        private ThreadedStreamHandler(String name, InputStream inputStream, OutputStream outputStream, String interactionInputString) {
            this.name = name;
            this.inputStream = inputStream;
            this.outputStream = outputStream;
            this.interactionInputString = interactionInputString;
        }

        static ThreadedStreamHandler newWithInteraction(String name, InputStream inputStream, OutputStream outputStream, String interactionInputString) {
            return new ThreadedStreamHandler(name, inputStream, outputStream, interactionInputString);
        }

        static ThreadedStreamHandler newSimple(String name, InputStream inputStream) {
            return new ThreadedStreamHandler(name, inputStream, null, null);
        }

        public void run() {
            if (interactionInputString != null) {
                System.out.println(">> [" + this.name + "] SUDO REQUESTED, entering password");
                final PrintWriter outputStreamWriter = new PrintWriter(outputStream);
                outputStreamWriter.println(interactionInputString);
                outputStreamWriter.flush();
            }
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = null;
                System.out.println("[" + this.name + "] Waiting for input ...");
                while ((line = bufferedReader.readLine()) != null) {
                    System.out.println("[" + this.name + "]: " + line);
                    outputBuffer.append(line + "\n");
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                }
            }
        }

        private void doSleep(long millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
            }
        }

        public StringBuilder getOutputBuffer() {
            return outputBuffer;
        }
    }
}
