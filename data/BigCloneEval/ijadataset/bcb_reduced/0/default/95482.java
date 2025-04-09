import java.io.File;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Helper class to start the native daemon.
 *
 * @author <a href = "mailto:juha@juhalindfors.com">Juha Lindfors</a>
 */
public class Daemon {

    /**
   * I/O daemon executable version identifier
   */
    public static final String VERSION = "1.0.0";

    /**
   * Using a different port from regular default, just so to isolate test runs with
   * I/O daemon.
   */
    public static final int TEST_DAEMON_PORT = 19999;

    private static Process process;

    /**
   * Attempts to start the native I/O Daemon for testing.
   *
   */
    public static void start() {
        File workdir = new File(System.getProperty("user.dir"));
        File outputdir = new File(workdir, "output");
        if (!outputdir.exists()) {
            System.out.println("***********************************************************************");
            System.out.println("");
            System.out.println("  Directory " + outputdir + " does not exist.");
            System.out.println("");
            System.out.println("  Make sure you've built the native executable first before running ");
            System.out.println("  these tests.");
            System.out.println("");
            System.out.println("***********************************************************************");
            System.exit(-1);
        }
        String osname = System.getProperty("os.name");
        File iodaemon = new File("");
        if (osname.startsWith("Linux")) {
            throw new Error("TODO: IMPLEMENT LINUX IODAEMON PATH");
        } else if (osname.startsWith("Windows")) {
            iodaemon = new File(outputdir, "cygwin");
            iodaemon = new File(iodaemon, "iodaemon-" + VERSION + ".exe");
        } else if (osname.startsWith("Mac")) {
            throw new Error("TODO: IMPLEMENT MACOS IODAEMON PATH");
        }
        if (!iodaemon.exists()) {
            System.out.println("***********************************************************************");
            System.out.println("");
            System.out.println("  " + iodaemon.getAbsolutePath() + " does not exist.");
            System.out.println("");
            System.out.println("  Make sure you've built the native executable first before running ");
            System.out.println("  these tests.");
            System.out.println("");
            System.out.println("***********************************************************************");
            System.exit(-1);
        }
        ProcessBuilder builder = new ProcessBuilder(iodaemon.getAbsolutePath(), "--port=" + TEST_DAEMON_PORT);
        try {
            process = builder.start();
            readInputStream(process.getInputStream());
            readErrorStream(process.getErrorStream());
            try {
                int exitValue = process.exitValue();
                System.out.println("***********************************************************************");
                System.out.println("");
                System.out.println("  I/O Daemon did not start normally (exit value: " + exitValue + ").");
                System.out.println("");
                System.out.println("***********************************************************************");
                System.exit(-1);
            } catch (IllegalThreadStateException e) {
            }
        } catch (Exception e) {
            System.out.println("***********************************************************************");
            System.out.println("");
            System.out.println("  Cannot start I/O daemon!");
            System.out.println("");
            System.out.println("  " + e.toString());
            System.out.println("");
            System.out.println("***********************************************************************");
            System.exit(-1);
        }
    }

    /**
   * Sends a ping request to I/O daemon. Daemon should be started first.
   *
   * @throws IOException    if something unexpected happens
   */
    public static void ping() throws IOException {
        Socket socket = null;
        BufferedOutputStream output = null;
        BufferedInputStream input = null;
        try {
            socket = new Socket("localhost", TEST_DAEMON_PORT);
            output = new BufferedOutputStream(socket.getOutputStream());
            input = new BufferedInputStream(socket.getInputStream());
            output.write("DCONTROL0X0000000DARE YOU THERE".getBytes());
            output.flush();
            final String RESPONSE = "I AM HERE";
            int len = RESPONSE.length();
            byte[] buffer = new byte[len];
            int readbytes = input.read(buffer);
            if (len != readbytes) {
                System.out.println("*********************************************************************");
                System.out.println("");
                System.out.println("  I/O Daemon sent an unexpected response to ping request.");
                System.out.println("  Expected " + len + " bytes, received " + readbytes + " bytes instead.");
                System.out.println("  (" + new String(buffer) + ")");
                System.out.println("");
                System.out.println("*********************************************************************");
                throw new IOException("I/O Daemon sent an unexpected response to ping request. " + "Expected " + len + " bytes, received " + readbytes + " bytes instead.");
            }
            if (!new String(buffer).equals(RESPONSE)) {
                System.out.println("*********************************************************************");
                System.out.println("");
                System.out.println("  I/O Daemon sent an unexpected response to ping request.");
                System.out.println("  Expected '" + RESPONSE + "', received " + new String(buffer) + "'.");
                System.out.println("");
                System.out.println("*********************************************************************");
                throw new IOException("I/O Daemon sent an unexpected response to ping request. " + "Expected '" + RESPONSE + "', received '" + new String(buffer) + "'.");
            }
        } finally {
            if (socket != null) socket.close();
        }
    }

    /**
   * Try to shut down the daemon nicely with a shutdown command.
   *
   * @throws IOException  if there's an IOException or I/O daemon returns an unexpected
   *                      response to stop request
   */
    public static void stop() throws IOException {
        Socket socket = null;
        BufferedOutputStream output = null;
        BufferedInputStream input = null;
        try {
            socket = new Socket("localhost", TEST_DAEMON_PORT);
            output = new BufferedOutputStream(socket.getOutputStream());
            input = new BufferedInputStream(socket.getInputStream());
            output.write("DCONTROL0X00000009D1ED1ED1E".getBytes());
            output.flush();
            final String RESPONSE = "GOODBYE CRUEL WORLD";
            int len = RESPONSE.length();
            byte[] buffer = new byte[len];
            int readbytes = input.read(buffer);
            if (len != readbytes) {
                System.out.println("*********************************************************************");
                System.out.println("");
                System.out.println("  I/O Daemon sent an unexpected response to stop request.");
                System.out.println("  Expected " + len + " bytes, received " + readbytes + " bytes instead.");
                System.out.println("  (" + new String(buffer) + ")");
                System.out.println("");
                System.out.println("*********************************************************************");
                throw new IOException("I/O Daemon sent an unexpected response to stop request. " + "Expected " + len + " bytes, received " + readbytes + " bytes instead.");
            }
            if (!new String(buffer).equals(RESPONSE)) {
                System.out.println("*********************************************************************");
                System.out.println("");
                System.out.println("  I/O Daemon sent an unexpected response to stop request.");
                System.out.println("  Expected '" + RESPONSE + "', received " + new String(buffer) + "'.");
                System.out.println("");
                System.out.println("*********************************************************************");
                throw new IOException("I/O Daemon sent an unexpected response to stop request. " + "Expected '" + RESPONSE + "', received '" + new String(buffer) + "'.");
            }
        } finally {
            if (socket != null) socket.close();
        }
    }

    /**
   * Not so nice, should only be used if stop fails.
   */
    public static void kill() {
        process.destroy();
        process = null;
    }

    /**
   * Sends an arbitrary byte array to daemon without waiting for response
   *
   * @param bytes   bytes to send
   *
   * @throws IOException  if something unexpected happens
   */
    public static void sendBytes(byte[] bytes) throws IOException {
        Socket socket = null;
        BufferedOutputStream output = null;
        try {
            socket = new Socket("localhost", TEST_DAEMON_PORT);
            output = new BufferedOutputStream(socket.getOutputStream());
            output.write(bytes);
            output.flush();
        } finally {
            if (socket != null) socket.close();
        }
    }

    private static void readInputStream(final InputStream input) {
        Runnable runnable = new Runnable() {

            public void run() {
                BufferedReader in = new BufferedReader(new InputStreamReader(input));
                boolean running = true;
                while (running) {
                    try {
                        String readLine;
                        while ((readLine = in.readLine()) != null) {
                            System.out.println("[I/O DAEMON] " + readLine);
                        }
                    } catch (Throwable t) {
                        System.out.println("[I/O DAEMON] " + t.toString());
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.start();
    }

    private static void readErrorStream(final InputStream error) {
        Runnable runnable = new Runnable() {

            public void run() {
                BufferedReader in = new BufferedReader(new InputStreamReader(error));
                boolean running = true;
                while (running) {
                    try {
                        String readLine;
                        while ((readLine = in.readLine()) != null) {
                            System.out.println("[I/O DAEMON] " + readLine);
                        }
                    } catch (Throwable t) {
                        System.out.println("[I/O DAEMON] " + t.toString());
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.start();
    }
}
