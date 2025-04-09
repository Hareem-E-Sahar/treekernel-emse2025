package net.sf.opendf.hades.des.util;

import java.io.Serializable;
import java.util.Vector;
import net.sf.opendf.hades.des.DiscreteEventEntity;

/**
 *  @memo time-sorted linear structure of objects
 *
 *  TimedQueues can be used for time-sorted lists, like e.g. event lists etc.
 *  They are slightly distinct from MessageQueues, in that they only combine a time stamp and an arbitrary
 *  object into a TimedEntry object.
 *
 *  @see TimedEntry
 *  @author JWJ
 */
public class TimedQueue extends Vector implements Serializable {

    private double lastTime;

    class UnexpectedTQException extends RuntimeException {
    }

    class TQEmpty extends RuntimeException {
    }

    /**
   *  @memo adds a new entry for v at time t to the queue
   */
    public void addEntry(double t, Object v) {
        if (t == Double.POSITIVE_INFINITY) return;
        int lo = 0;
        int hi = this.size();
        while (lo < hi) {
            int mid = (lo + hi) / 2;
            TimedEntry e = (TimedEntry) this.elementAt(mid);
            if (t > e.t) lo = mid + 1; else {
                if (t < e.t) hi = mid; else lo = hi = mid;
            }
        }
        try {
            TimedEntry e = new TimedEntry(t, v);
            this.insertElementAt(e, lo);
        } catch (Exception exc) {
            throw new UnexpectedTQException();
        }
    }

    /**
   *  @memo deletes an entry/all entries containing v and adds a new entry for v at time t
   *
   *  @param once if true, only one entry is deleted (if present), otherwise all are removed, and one is added
   */
    public void reschedule(double t, Object v, boolean once) {
        unschedule(v, once);
        addEntry(t, v);
    }

    /**
   * @memo removes entries for an object from the queue
   *
   * @param once if true, only one entry containing is removed from the queue, otherwise all are deleted
   */
    public void unschedule(Object v, boolean once) {
        boolean resched = false;
        int n = size();
        for (int i = 0; i < n; i++) {
            TimedEntry e = (TimedEntry) this.elementAt(i);
            if (e.v == v) {
                removeElementAt(i);
                if (once) i = n;
            }
        }
    }

    /**
   * @return true, if queue contains no entry
   */
    public boolean empty() {
        return (this.size() == 0);
    }

    /**
   *  @return time of next scheduled event - Double.POSITIVE_INFINITY if event queue is empty.
   */
    public double nextTime() {
        try {
            TimedEntry e = (TimedEntry) this.elementAt(0);
            return e.t;
        } catch (ArrayIndexOutOfBoundsException exc) {
            return Double.POSITIVE_INFINITY;
        }
    }

    /**
   *  @return time stamp of most recently removed entry.
   */
    public double now() {
        return lastTime;
    }

    /**
   *  @memo checks, whether queue has an entry {\em not after} a given time
   *  @return true if queue has an entry before or at t, false otherwise (including when queue is empty).
   */
    public boolean hasEntryBefore(double t) {
        if (this.size() == 0) return false; else return (nextTime() <= t);
    }

    /**
   * @memo produces the next entry (in time-stamp order) in the queue
   * @exception TQEmpty if there are no elements
   * @return the next entry in the queue
   */
    public TimedEntry next() {
        try {
            TimedEntry e = (TimedEntry) this.firstElement();
            this.removeElementAt(0);
            lastTime = e.t;
            return e;
        } catch (Exception exc) {
            throw new TQEmpty();
        }
    }

    /**
   * @memo initializes the queue at time 0
   */
    public TimedQueue() {
        this(0);
    }

    /**
   * @memo initializes the queue at the given time
   */
    public TimedQueue(double tm) {
        this(tm, null);
    }

    public TimedQueue(double tm, DiscreteEventEntity dee) {
        lastTime = tm;
    }
}
