import java.io.*;
import java.util.*;

/**
 * A generic class to use the AMS tracker to control things.
 */
public class Control {

    private String ams = "/Applications/AMSTracker";

    private float period = .1F;

    /**
   * Sets the application name of the ASM Tracker, defaults to
   * <code>/Applications/AMSTracker</code>
   *
   * @param ams application path
   */
    public void setAMS(String ams) {
        this.ams = ams;
    }

    /**
   * @return the current AMS tracker application path
   */
    public String getAMS() {
        return this.ams;
    }

    /**
   * Sets the period for checking the AMS tracker.  defaults to
   * <code>-1<code>.
   *
   * @param period fraction of a second to wait before getting new coordinates.
   */
    public void setPeriod(float period) {
        if (period < 0) {
            throw new IllegalArgumentException("Period must be > 0");
        }
        this.period = period;
    }

    /**
   * @return the current period
   */
    public float getPeriod() {
        return this.period;
    }

    /**
   * A simple callback to recieve new coordniates.  {@link
   * #process(int,int,int)} is called every {@link #getPeriod()} seconds.
   */
    public interface Callback {

        void process(int x, int y, int z);
    }

    public static class MultiCallack implements Callback {

        private final List callbacks = new ArrayList();

        public void addCallack(Callback callback) {
            if (callback == null || callbacks.contains(callback)) return;
            callbacks.add(callback);
        }

        public void removeCallback(Callback callback) {
            if (callback == null || !callbacks.contains(callback)) return;
            callbacks.remove(callback);
        }

        public final void process(int x, int y, int z) {
            for (Iterator it = callbacks.iterator(); it.hasNext(); ) {
                ((Callback) it.next()).process(x, y, z);
            }
        }
    }

    private Thread t;

    /**
   * starts every off.  {@link Callback#process(int,int,int)} is called
   * every {@link #getPeriod()} seconds on <code>callback</code>.
   *
   * @param callback the Callback
   */
    public void start(final Callback callback) throws Exception {
        if (t != null) return;
        String[] cmd = { ams, "-u", String.valueOf(period), "-s" };
        final Process proc = Runtime.getRuntime().exec(cmd);
        final BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        t = new Thread(new Runnable() {

            public void run() {
                boolean going = true;
                while (going) {
                    try {
                        String line = in.readLine();
                        if (line.indexOf("AMS") != -1) continue;
                        StringTokenizer st = new StringTokenizer(line, " \n\t\r", false);
                        int x = Integer.parseInt(st.nextToken().trim());
                        int y = Integer.parseInt(st.nextToken().trim());
                        int z = Integer.parseInt(st.nextToken().trim());
                        callback.process(-x, y, z);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try {
                    System.out.println("trying to kill process");
                    proc.destroy();
                    System.out.println("killed process");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
        proc.waitFor();
        proc.exitValue();
        t.join();
    }

    /**
   * Stops everything
   */
    public void stop() throws Exception {
        if (t == null) return;
        t.interrupt();
        t = null;
    }
}
