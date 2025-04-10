package edu.rice.cs.plt.concurrent;

import java.util.LinkedList;

/**
 * This class implements synchronization primitives to solve the classic
 * readers/writers problem without allowing deadlock or starvation.
 * 
 * <p>
 * Problem: Suppose multiple threads want to read and write to a resource.
 * Multiple readers can be active at a time, but only a single writer can
 * be active at a time, and no readers can be active while the writer is
 * active.
 * </p>
 * 
 * <p>
 * We must be careful to avoid starvation in our solution, so that a steady
 * flow of reader threads cannot block waiting writer threads indefinitely.
 * This can be achieved by imposing an ordering on the incoming readers
 * and writers.
 * </p>
 * 
 * <p>
 * To use this class, instantiate a ReaderWriterLock in the class holding
 * the shared resource.  Any methods which read from the resource must call
 * startRead before reading and endRead after reading, and must not be
 * synchronized themselves.  Similarly, any methods which write to the resource
 * must not be synchronized and must call startWrite before writing and endWrite
 * after writing.
 * </p>
 * 
 * <p>
 * This class enforces that any readers and writers that are forced to wait
 * are allowed access to the shared resource in the order in which they arrive.
 * Groups of readers are allowed to proceed simultaneously, where each group
 * contains all waiting readers that arrived before the next waiting writer.
 * </p>
 * 
 * <p>
 * This class is loosely adapted from the "Starvation-Free Readers and Writers
 * Monitor" available from Stephen J. Hartley (Drexel University) at:
 * http://www.mcs.drexel.edu/~shartley/ConcProgJava/monitors.html
 * We have imposed an ordering on the pending waiting readers and writers
 * using an ordered queue.
 * </p>
 * 
 * @version $Id: ReaderWriterLock.java 5175 2010-01-20 08:46:32Z mgricken $
 */
public class ReaderWriterLock {

    /** The number of readers currently reading. */
    private volatile int _numActiveReaders = 0;

    /** The number of writers currently writing (ie. 0 or 1). */
    private volatile int _numActiveWriters = 0;

    /** The number of readers waiting to read. */
    private volatile int _numWaitingReaders = 0;

    /** The number of writers waiting to write. */
    private volatile int _numWaitingWriters = 0;

    /**
   * Queue of all waiting reader and writer threads.  The front of the queue
   * (first element of the list) represents the thread which arrived first;
   * new waiting threads are added at the end.
   * "Groups" on the queue refer to several sequential Readers or a
   * single Writer.  (We can wake up the front group on the queue each
   * time a writer finishes and each time the last reader finishes.)
   */
    private final LinkedList<ReaderWriterThread> _waitQueue;

    /**
   * A list of the Threads that are currently reading or writing.
   * We maintain this list to prevent the deadlock which would occur if
   * a Thread which is reading or writing attempted to read or write again.
   * (If that happens, we throw an IllegalStateException.)
   */
    private final LinkedList<Thread> _runningThreads;

    /** Creates a new ReaderWriterLock. */
    public ReaderWriterLock() {
        _waitQueue = new LinkedList<ReaderWriterThread>();
        _runningThreads = new LinkedList<Thread>();
    }

    /** Must be called by each reader thread before starting to read.  The calling method must <i>not</i> be 
   *  synchronized.  This method blocks the reader if there are current active or waiting writers, until those 
   *  writers have finished.
   *  @throws IllegalStateException if the thread is already a reader or writer
   */
    public synchronized void startRead() {
        if (!_alreadyReading()) {
            _ensureNotAlreadyRunning();
            if (_numWaitingWriters > 0 || _numActiveWriters > 0) {
                _numWaitingReaders++;
                Reader r = new Reader();
                r.startWaiting();
                _numWaitingReaders--;
            }
        }
        _numActiveReaders++;
        _runningThreads.add(Thread.currentThread());
    }

    /** Must be called by each reader thread after it is finished reading.  The calling method must <i>not</i> be 
   *  synchronized. This method wakes up a waiting writer if there are no remaining reader threads actively reading.
   * 
   *  @throws IllegalStateException if the thread is already a reader or writer
   */
    public synchronized void endRead() {
        if (_numActiveReaders == 0) {
            throw new IllegalStateException("Trying to end a read with no active readers!");
        }
        _numActiveReaders--;
        _ensureAlreadyRunning();
        _runningThreads.remove(Thread.currentThread());
        assert _numActiveWriters == 0 : "A writer was active during the read";
        if (_numActiveReaders == 0) {
            _wakeFrontGroupOfWaitQueue();
        }
    }

    /** Must be called by each writer thread before starting to write.  The calling method must <i>not</i> be 
   *  synchronized. This method blocks the writer if there are any active readers or writers, and prevents any new 
   *  readers from starting to read until this writer gets a chance to write.
   * 
   *  @throws IllegalStateException if the thread is already a reader or writer
   */
    public synchronized void startWrite() {
        _ensureNotAlreadyRunning();
        if ((_numActiveReaders > 0 || _numActiveWriters > 0) || (_numWaitingReaders > 0 || _numWaitingWriters > 0)) {
            _numWaitingWriters++;
            Writer w = new Writer();
            w.startWaiting();
            _numWaitingWriters--;
        }
        _numActiveWriters++;
        _runningThreads.add(Thread.currentThread());
    }

    /** Must be called by each writer thread after it is finished writing.  The calling method must <i>not</i> be 
   *  synchronized. This method wakes up any waiting readers and writers.  If there are waiting readers, they read 
   *  before the next writer, but any new readers (after this call) wait until the next waiting writer writes.
   * 
   *  @throws IllegalStateException if the thread is already a reader or writer
   */
    public synchronized void endWrite() {
        if (_numActiveWriters != 1) {
            throw new IllegalStateException("Trying to end a write with " + _numActiveWriters + " active writers!");
        }
        _numActiveWriters--;
        _ensureAlreadyRunning();
        _runningThreads.remove(Thread.currentThread());
        assert _numActiveWriters == 0 && _numActiveReaders == 0 : "Multiple readers/writers were active during a write";
        _wakeFrontGroupOfWaitQueue();
    }

    /** Checks if the current thread is already a reader. */
    private boolean _alreadyReading() {
        return _numActiveReaders > 0 && _runningThreads.contains(Thread.currentThread());
    }

    /** Ensures that the current thread is not already considered a reader or writer.  This prevents the deadlock which 
   *  would occur if a reader thread tries to write (or vice versa).
   *  @throws IllegalStateException if the thread is already a reader or writer
   */
    private void _ensureNotAlreadyRunning() {
        if (_runningThreads.contains(Thread.currentThread())) {
            throw new IllegalStateException("Same thread cannot read or write multiple times!  (Would cause deadlock.)");
        }
    }

    /**  Ensures that the current thread is not already considered a reader or writer.  This prevents the deadlock which 
   *   would occur if a reader thread tries to write (or vice versa).
   *   @throws IllegalStateException if the thread is already a reader or writer
   */
    private void _ensureAlreadyRunning() {
        if (!_runningThreads.contains(Thread.currentThread())) {
            throw new IllegalStateException("Current thread did not initiate a read or write!");
        }
    }

    /** Wakes up either the writer or all sequential readers before a writer at the front of the waitQueue. */
    private synchronized void _wakeFrontGroupOfWaitQueue() {
        if (!_waitQueue.isEmpty()) {
            ReaderWriterThread front = _waitQueue.getFirst();
            front.stopWaiting();
            if (front.isReader()) {
                while (!_waitQueue.isEmpty()) {
                    front = _waitQueue.getFirst();
                    if (front.isReader()) {
                        front.stopWaiting();
                    } else {
                        break;
                    }
                }
            }
        }
    }

    /** Represents a thread waiting to either read or write.  Instances of this class are placed in a queue to enforce
   *  the correct order when allowing new threads to read or write.  The waiting thread must call wait() on this object,
   *  allowing it to be notified when it reaches the front of the queue.  This object will remain on the queue until the
   *  thread completes its read or write, allowing us to check for and prevent deadlock if the same thread tries to both 
   *  read and write at the same time.
   */
    public abstract class ReaderWriterThread {

        private volatile boolean _isWaiting = true;

        /** Returns whether this ReaderWriter is a writer. */
        public abstract boolean isWriter();

        /** Returns whether this ReaderWriter is a reader. */
        public abstract boolean isReader();

        /** Causes this ReaderWriterThread to wait until stopWaiting is called. While it's waiting, it is on the waitQueue.
     */
        public void startWaiting() {
            synchronized (ReaderWriterLock.this) {
                _isWaiting = true;
                _waitQueue.addLast(this);
                while (_isWaiting) {
                    try {
                        ReaderWriterLock.this.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

        /** Wakes up this ReaderWriterThread, removing it from the waitQueue. */
        public void stopWaiting() {
            synchronized (ReaderWriterLock.this) {
                _isWaiting = false;
                _waitQueue.remove(this);
                ReaderWriterLock.this.notifyAll();
            }
        }
    }

    /** Object representing a reader thread which is waiting for read access on the queue of waiting threads. */
    public class Reader extends ReaderWriterThread {

        public boolean isReader() {
            return true;
        }

        public boolean isWriter() {
            return false;
        }
    }

    /** Object representing a writer thread which is waiting for write access on the queue of waiting threads. */
    public class Writer extends ReaderWriterThread {

        public boolean isReader() {
            return false;
        }

        public boolean isWriter() {
            return true;
        }
    }
}
