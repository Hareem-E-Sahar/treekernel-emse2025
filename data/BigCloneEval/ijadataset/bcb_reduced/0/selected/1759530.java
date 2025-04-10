package com.teletalk.jserver.util;

/**
 * The ReaderWriterLock class implements a thread synchronization mechanism that follows a "multiple reader/single writer" 
 * pattern. This means that multiple threads that require read access can own the lock at the same time, but in order to grant 
 * a thread write access, no other readers or writers can own the lock.<br>
 * <br>
 * This class can be useful when a resource needs to be protected by a lock, but there is no reason to prevent several threads 
 * form "reading"  the resource simultaneously. Permitting several threads to read at the same time may improve performance.<br>
 * <br>
 * To obtain a lock the method {@link #acquireReaderLock()} should be called for read access and the method {@link #acquireWriterLock()} 
 * for write access. When a lock has been obtained it <b>MUST ALWAYS</b> be relinquished by calling either {@link #releaseReaderLock()} 
 * or {@link #releaseWriterLock()} depending on wheter a reader or writer lock was originally obtained. The easiest way of ensuring that 
 * the lock gets relinquished is to use a try-finally statement like the one below:<br>
 * <br>
 * <code><b>try</b><br>
 * {<br>
 * &nbsp;&nbsp;&nbsp;rwLock.acquireReaderLock();<br>
 * &nbsp;&nbsp;&nbsp;// Read from resource<br>
 * &nbsp;&nbsp;&nbsp;...<br>
 * }<br>
 * <b>finally</b><br>
 * {<br>
 * &nbsp;&nbsp;&nbsp;rwLock.releaseReaderLock();<br>
 * }</code>
 * 
 * @author Tobias L�fstrand
 * 
 * @since 1.1 Build 545
 */
public class ReaderWriterLock {

    /**
	 * Linked list wait queue object.
	 */
    private static final class WaitObject {

        public WaitObject() {
        }

        public boolean isItMyTurn = false;

        public boolean isWriter = false;

        public WaitObject next = null;
    }

    private int queueSize;

    private WaitObject first = null;

    private WaitObject last = null;

    /**
	 * Adds a wait object to the queue.
	 */
    private void addToQueue(final WaitObject wo) {
        if (this.last != null) this.last.next = wo; else this.first = wo;
        this.last = wo;
        wo.next = null;
        this.queueSize++;
    }

    /**
	 * Gets the size of the queue.
	 */
    private int getQueueSize() {
        return this.queueSize;
    }

    /**
	 * Gets the first wait object in the queue.
	 */
    private WaitObject getFirstInQueue() {
        return this.first;
    }

    /**
	 * Gets and removes the first wait object in the queue.
	 */
    private WaitObject removeFirstInQueue() {
        final WaitObject wo = this.first;
        if (wo != null) {
            this.first = wo.next;
            if (wo == this.last) this.last = null;
            this.queueSize--;
        }
        return wo;
    }

    private int numberOfActiveReaders;

    private int numberOfWaitingWriters;

    private Thread writerLockOwner;

    /**
	 * Constructs a new ReaderWriterLock.
	 */
    public ReaderWriterLock() {
        this.queueSize = 0;
        this.numberOfActiveReaders = 0;
        this.numberOfWaitingWriters = 0;
        this.writerLockOwner = null;
    }

    /**
	 * Wakes up the next waiting thread.
	 */
    private void wakeUpNextInQueue(final boolean canWakeUpWriter) {
        if (this.getQueueSize() > 0) {
            WaitObject next = (WaitObject) this.getFirstInQueue();
            if ((next != null) && (!next.isWriter || (next.isWriter && canWakeUpWriter && (this.numberOfActiveReaders == 0)))) {
                this.removeFirstInQueue();
                next.isItMyTurn = true;
                synchronized (next) {
                    next.notify();
                }
            }
        }
    }

    /**
	 * Acquires a lock with read access. <br>
	 * <br>
	 * To relinquish the lock the method {@link #releaseReaderLock()} must be called. This must be done even if a 
	 * <code>InterruptedException</code> is thrown by this mehtod.
	 * 
	 * @exception InterruptedException if the thread was interrupted while waiting for the read lock to become available.
	 */
    public void acquireReaderLock() throws InterruptedException {
        WaitObject wo = null;
        synchronized (this) {
            if (this.writerLockOwner == Thread.currentThread()) {
                numberOfActiveReaders++;
                return;
            }
            if ((this.numberOfWaitingWriters > 0) || (this.writerLockOwner != null)) {
                wo = new WaitObject();
                this.addToQueue(wo);
            }
        }
        try {
            if (wo != null) {
                synchronized (wo) {
                    while (!wo.isItMyTurn) wo.wait();
                }
            }
        } finally {
            synchronized (this) {
                if (wo != null) {
                    this.wakeUpNextInQueue(false);
                }
                numberOfActiveReaders++;
            }
        }
    }

    /**
	 * Releases a lock with read access.
	 */
    public synchronized void releaseReaderLock() {
        numberOfActiveReaders--;
        this.wakeUpNextInQueue(true);
    }

    /**
	 * Checks if the lock is currently owned by any "reading" threads. 
	 * 
	 * @return <code>true</code> if the lock is currently owned by at least one "reading" thread, otherwise <code>false</code>. 
	 */
    public synchronized boolean isReaderLockAcquired() {
        return (numberOfActiveReaders > 0);
    }

    /**
	 * Acquires a lock with write access. <br>
	 * <br>
	 * To relinquish the lock the method {@link #releaseWriterLock()} must be called. This must be done even if a 
	 * <code>InterruptedException</code> is thrown by this mehtod.
	 * 
	 * @exception InterruptedException if the thread was interrupted while waiting for the write lock to become available.
	 */
    public void acquireWriterLock() throws InterruptedException {
        WaitObject wo = null;
        synchronized (this) {
            if (this.writerLockOwner == Thread.currentThread()) return;
            this.numberOfWaitingWriters++;
            if ((this.writerLockOwner != null) || (numberOfActiveReaders > 0)) {
                wo = new WaitObject();
                wo.isWriter = true;
                this.addToQueue(wo);
            }
        }
        try {
            if (wo != null) {
                synchronized (wo) {
                    while (!wo.isItMyTurn) wo.wait();
                }
            }
        } finally {
            synchronized (this) {
                this.writerLockOwner = Thread.currentThread();
                this.numberOfWaitingWriters--;
            }
        }
    }

    /**
	 * Releases a lock with write access.
	 */
    public synchronized void releaseWriterLock() {
        this.writerLockOwner = null;
        this.wakeUpNextInQueue(true);
    }

    /**
	 * Checks if the lock is currently owned by a "writing" thread. 
	 * 
	 * @return <code>true</code> if the lock is currently owned by a "writing" thread, otherwise <code>false</code>. 
	 */
    public synchronized boolean isWriterLockAcquired() {
        return (this.writerLockOwner != null);
    }
}
