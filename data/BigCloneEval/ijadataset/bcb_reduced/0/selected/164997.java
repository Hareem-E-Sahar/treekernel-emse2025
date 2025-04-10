package org.jpos.util;

import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;

/**
 * TPS can be used to measure Transactions Per Second (or transactions during other period of time).
 *
 * <p>It can operate in two different modes:
 * <ul>
 *  <li>Auto update</li>
 *  <li>Manual update</li>
 * </ul></p>
 *
 * <p>When operating in <b>auto update</b> mode, a Timer is created and the number of transactions (calls to tick())
 * is automatically calculated for every period. Under this mode, user has to call the
 * <b>stop()</b> method when this TPS object is no longer needed, otherwise it will keep a Thread
 * lingering around.</p>
 *
 * <p>When operating in <b>manual update</b> mode, user has to call one of its
 * floatValue() or intValue() method at regular intervals. The returned value will be the average
 * TPS for the given period since the last call</p>.
 * 
 * @author Alejandro Revilla, Jeronimo Paolleti and Thiago Moretto
 * @since 1.6.7 r2912
 */
@SuppressWarnings("unused")
public class TPS implements Loggeable {

    volatile int count;

    volatile long start;

    int peak;

    long peakWhen;

    static final long FROM_NANOS = 1000000L;

    long period;

    float tps;

    float avg;

    Timer timer;

    boolean autoupdate;

    public TPS() {
        this(1000L, false);
    }

    /**
     *
     * @param autoupdate
     */
    public TPS(boolean autoupdate) {
        this(1000L, autoupdate);
    }

    /**
     * @param period in millis
     */
    public TPS(final long period, boolean autoupdate) {
        super();
        this.period = period;
        this.autoupdate = autoupdate;
        start = System.nanoTime() / FROM_NANOS;
        if (autoupdate) {
            timer = new Timer();
            timer.schedule(new TimerTask() {

                public void run() {
                    calcTPS(period);
                }
            }, period, period);
        }
    }

    public void tick() {
        count++;
    }

    public float floatValue() {
        return autoupdate ? tps : calcTPS();
    }

    public int intValue() {
        return Math.round(floatValue());
    }

    public float getAvg() {
        return avg;
    }

    public int getPeak() {
        return peak;
    }

    public long getPeakWhen() {
        return peakWhen;
    }

    /**
     * resets average and peak
     */
    public synchronized void reset() {
        avg = 0f;
        peak = 0;
        peakWhen = 0L;
    }

    public long getPeriod() {
        return period;
    }

    public long getElapsed() {
        return System.nanoTime() - start;
    }

    public String toString() {
        return String.format("tps=%d, peak=%d, avg=%.2f", intValue(), getPeak(), getAvg());
    }

    public synchronized void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            autoupdate = false;
        }
    }

    public void dump(PrintStream p, String indent) {
        p.println(indent + "<tps" + (autoupdate ? " auto='true'>" : ">") + this.toString() + "</tps>");
    }

    private synchronized float calcTPS(long interval) {
        tps = (float) period * count / interval;
        avg = (avg + tps) / 2;
        if (tps > peak) {
            peak = Math.round(tps);
            peakWhen = System.currentTimeMillis();
        }
        count = 0;
        return tps;
    }

    private synchronized float calcTPS() {
        long now = System.nanoTime() / FROM_NANOS;
        long interval = now - start;
        if (interval >= period) {
            calcTPS(interval);
            start = now;
        }
        return tps;
    }
}
