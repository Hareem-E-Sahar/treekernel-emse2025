package edu.oswego.cs.dl.util.concurrent;

import java.util.*;

/**
 * SyncMaps wrap Sync-based control around java.util.Maps.
 * They operate in the same way as SyncCollection.
 * <p>
 * Reader operations are
 * <ul>
 *  <li> size
 *  <li> isEmpty
 *  <li> get
 *  <li> containsKey
 *  <li> containsValue
 *  <li> keySet
 *  <li> entrySet
 *  <li> values
 * </ul>
 * Writer operations are:
 * <ul>
 *  <li> put
 *  <li> putAll
 *  <li> remove
 *  <li> clear
 * </ul>
 *  
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> Introduction to this package. </a>]
 * @see SyncCollection
**/
public class SyncMap implements Map {

    protected final Map c_;

    protected final Sync rd_;

    protected final Sync wr_;

    protected final SynchronizedLong syncFailures_ = new SynchronizedLong(0);

    private transient Set keySet_ = null;

    private transient Set entrySet_ = null;

    private transient Collection values_ = null;

    /**
   * Create a new SyncMap protecting the given map,
   * and using the given ReadWriteLock to control reader and writer methods.
   **/
    public SyncMap(Map map, ReadWriteLock rwl) {
        this(map, rwl.readLock(), rwl.writeLock());
    }

    /**
   * Create a new SyncMap protecting the given map,
   * and using the given sync to control both reader and writer methods.
   * Common, reasonable choices for the sync argument include
   * Mutex, ReentrantLock, and Semaphores initialized to 1.
   **/
    public SyncMap(Map map, Sync sync) {
        this(map, sync, sync);
    }

    /**
   * Create a new SyncMap protecting the given map,
   * and using the given pair of locks to control reader and writer methods.
   **/
    public SyncMap(Map map, Sync readLock, Sync writeLock) {
        c_ = map;
        rd_ = readLock;
        wr_ = writeLock;
    }

    /** Clean up after a reader operation **/
    protected void afterRead(boolean wasInterrupted) {
        if (wasInterrupted) {
            Thread.currentThread().interrupt();
        } else rd_.release();
    }

    /** Try to acquire sync before a reader operation; record failure **/
    protected boolean beforeRead() {
        try {
            rd_.acquire();
            return false;
        } catch (InterruptedException ex) {
            syncFailures_.increment();
            return true;
        }
    }

    public void clear() {
        try {
            wr_.acquire();
            try {
                c_.clear();
            } finally {
                wr_.release();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new UnsupportedOperationException();
        }
    }

    public boolean containsKey(Object o) {
        boolean wasInterrupted = beforeRead();
        try {
            return c_.containsKey(o);
        } finally {
            afterRead(wasInterrupted);
        }
    }

    public boolean containsValue(Object o) {
        boolean wasInterrupted = beforeRead();
        try {
            return c_.containsValue(o);
        } finally {
            afterRead(wasInterrupted);
        }
    }

    public Set entrySet() {
        boolean wasInterrupted = beforeRead();
        try {
            if (entrySet_ == null) entrySet_ = new SyncSet(c_.entrySet(), rd_, wr_);
            return entrySet_;
        } finally {
            afterRead(wasInterrupted);
        }
    }

    public boolean equals(Object o) {
        boolean wasInterrupted = beforeRead();
        try {
            return c_.equals(o);
        } finally {
            afterRead(wasInterrupted);
        }
    }

    public Object get(Object key) {
        boolean wasInterrupted = beforeRead();
        try {
            return c_.get(key);
        } finally {
            afterRead(wasInterrupted);
        }
    }

    public int hashCode() {
        boolean wasInterrupted = beforeRead();
        try {
            return c_.hashCode();
        } finally {
            afterRead(wasInterrupted);
        }
    }

    public boolean isEmpty() {
        boolean wasInterrupted = beforeRead();
        try {
            return c_.isEmpty();
        } finally {
            afterRead(wasInterrupted);
        }
    }

    public Set keySet() {
        boolean wasInterrupted = beforeRead();
        try {
            if (keySet_ == null) keySet_ = new SyncSet(c_.keySet(), rd_, wr_);
            return keySet_;
        } finally {
            afterRead(wasInterrupted);
        }
    }

    public Object put(Object key, Object value) {
        try {
            wr_.acquire();
            try {
                return c_.put(key, value);
            } finally {
                wr_.release();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new UnsupportedOperationException();
        }
    }

    public void putAll(Map coll) {
        try {
            wr_.acquire();
            try {
                c_.putAll(coll);
            } finally {
                wr_.release();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new UnsupportedOperationException();
        }
    }

    /** 
   * Return the Sync object managing read-only operations
   **/
    public Sync readerSync() {
        return rd_;
    }

    public Object remove(Object key) {
        try {
            wr_.acquire();
            try {
                return c_.remove(key);
            } finally {
                wr_.release();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new UnsupportedOperationException();
        }
    }

    public int size() {
        boolean wasInterrupted = beforeRead();
        try {
            return c_.size();
        } finally {
            afterRead(wasInterrupted);
        }
    }

    /**
   * Return the number of synchronization failures for read-only operations
   **/
    public long syncFailures() {
        return syncFailures_.get();
    }

    public Collection values() {
        boolean wasInterrupted = beforeRead();
        try {
            if (values_ == null) values_ = new SyncCollection(c_.values(), rd_, wr_);
            return values_;
        } finally {
            afterRead(wasInterrupted);
        }
    }

    /** 
   * Return the Sync object managing mutative operations
   **/
    public Sync writerSync() {
        return wr_;
    }
}
