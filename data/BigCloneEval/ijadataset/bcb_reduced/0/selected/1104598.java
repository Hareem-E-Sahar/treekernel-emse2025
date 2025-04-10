package org.exist.storage.lock;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deadlock detection for resource and collection locks. The static methods in this class
 * keep track of all waiting threads, which are currently waiting on a resource or collection
 * lock. In some scenarios (e.g. a complex XQuery which modifies resources), a single thread
 * may acquire different read/write locks on resources in a collection. The locks can be arbitrarily
 * nested. For example, a thread may first acquire a read lock on a collection, then a read lock on
 * a resource and later acquires a write lock on the collection to remove the resource.
 *
 * Since we have locks on both, collections and resources, deadlock situations are sometimes
 * unavoidable. For example, imagine the following scenario:
 *
 * <ul>
 *  <li>T1 owns write lock on resource</li>
 *  <li>T2 owns write lock on collection</li>
 *  <li>T2 wants to acquire write lock on resource locked by T1</li>
 *  <li>T1 tries to acquire write lock on collection currently locked by T2</li>
 *  <li>DEADLOCK</li>
 * </ul>
 *
 * The code should probably be redesigned to avoid this kind of crossed collection-resource
 * locking, which easily leads to circular wait conditions. However, this needs to be done with care. In
 * the meantime, DeadlockDetection is used to detect deadlock situations as the one described
 * above. The lock classes can
 * then try to resolve the deadlock by suspending one thread.
 */
public class DeadlockDetection {

    private static final Object latch = new Object();

    private static final Map<Thread, WaitingThread> waitForResource = new HashMap<Thread, WaitingThread>();

    private static final Map<Thread, Lock> waitForCollection = new HashMap<Thread, Lock>();

    /**
     * Register a thread as waiting for a resource lock.
     *
     * @param thread the thread
     * @param waiter the WaitingThread object which wraps around the thread
     */
    public static void addResourceWaiter(Thread thread, WaitingThread waiter) {
        synchronized (latch) {
            waitForResource.put(thread, waiter);
        }
    }

    /**
     * Deregister a waiting thread.
     *  
     * @param thread
     * @return lock
     */
    public static Lock clearResourceWaiter(Thread thread) {
        synchronized (latch) {
            WaitingThread waiter = waitForResource.remove(thread);
            if (waiter != null) return waiter.getLock();
            return null;
        }
    }

    public static WaitingThread getResourceWaiter(Thread thread) {
        synchronized (latch) {
            return waitForResource.get(thread);
        }
    }

    /**
     * Check if there's a risk for a circular wait between threadA and threadB. The method tests if
     * threadB is currently waiting for a resource lock (read or write). It then checks
     * if threadA holds a lock on this resource. If yes, the {@link org.exist.storage.lock.WaitingThread}
     * object for threadB is returned. This object can be used to suspend the waiting thread
     * in order to temporarily yield the lock to threadA.
     *
     * @param threadA
     * @param threadB
     * @return waiting thread
     */
    public static WaitingThread deadlockCheckResource(Thread threadA, Thread threadB) {
        synchronized (latch) {
            WaitingThread waitingThread = waitForResource.get(threadB);
            if (waitingThread != null) {
                return waitingThread.getLock().hasLock(threadA) ? waitingThread : null;
            }
            return null;
        }
    }

    /**
     * Check if the second thread is currently waiting for a resource lock and
     * is blocked by the first thread.
     *
     * @param threadA the thread whose lock might be blocking threadB
     * @param threadB the thread to check
     * @return true if threadB is currently blocked by a lock held by threadA
     */
    public static boolean isBlockedBy(Thread threadA, Thread threadB) {
        synchronized (latch) {
            WaitingThread waitingThread = waitForResource.get(threadB);
            if (waitingThread != null) {
                return waitingThread.getLock().hasLock(threadA);
            }
            return false;
        }
    }

    public static boolean wouldDeadlock(Thread waiter, Thread owner, List<WaitingThread> waiters) {
        synchronized (latch) {
            WaitingThread wt = waitForResource.get(owner);
            if (wt != null) {
                if (waiters.contains(wt)) {
                    return false;
                }
                waiters.add(wt);
                Lock l = wt.getLock();
                Thread t = ((MultiReadReentrantLock) l).getWriteLockedThread();
                if (t == owner) {
                    return false;
                }
                if (t != null) {
                    if (t == waiter) return true;
                    return wouldDeadlock(waiter, t, waiters);
                }
                return false;
            }
            Lock l = waitForCollection.get(owner);
            if (l != null) {
                Thread t = ((ReentrantReadWriteLock) l).getOwner();
                if (t == owner) {
                    return false;
                }
                if (t != null) {
                    if (t == waiter) return true;
                    return wouldDeadlock(waiter, t, waiters);
                }
            }
            return false;
        }
    }

    /**
     * Register a thread as waiting for a resource lock.
     *
     * @param waiter the thread
     * @param lock the lock object
     */
    public static void addCollectionWaiter(Thread waiter, Lock lock) {
        synchronized (latch) {
            waitForCollection.put(waiter, lock);
        }
    }

    public static Lock clearCollectionWaiter(Thread waiter) {
        synchronized (latch) {
            return waitForCollection.remove(waiter);
        }
    }

    public static Lock isWaitingFor(Thread waiter) {
        synchronized (latch) {
            return waitForCollection.get(waiter);
        }
    }

    public static Map<String, LockInfo> getWaitingThreads() {
        Map<String, LockInfo> table = new HashMap<String, LockInfo>();
        for (WaitingThread waitingThread : waitForResource.values()) {
            table.put(waitingThread.getThread().getName(), waitingThread.getLock().getLockInfo());
        }
        for (Map.Entry<Thread, Lock> entry : waitForCollection.entrySet()) {
            table.put(entry.getKey().getName(), entry.getValue().getLockInfo());
        }
        return table;
    }

    public static void debug(String name, LockInfo info) {
        StringWriter sout = new StringWriter();
        PrintWriter writer = new PrintWriter(sout);
        debug(writer, name, info);
        writer.flush();
        writer.close();
        System.out.println(sout.toString());
    }

    public static void debug(PrintWriter writer, String name, LockInfo info) {
        writer.println("Thread: " + name);
        if (info != null) {
            writer.format("%20s: %s\n", "Lock type", info.getLockType());
            writer.format("%20s: %s\n", "Lock mode", info.getLockMode());
            writer.format("%20s: %s\n", "Lock id", info.getId());
            writer.format("%20s: %s\n", "Held by", Arrays.toString(info.getOwners()));
            writer.format("%20s: %s\n", "Held by", Arrays.toString(info.getOwners()));
            writer.format("%20s: %s\n", "Held by", Arrays.toString(info.getOwners()));
            writer.format("%20s: %s\n", "Waiting for read", Arrays.toString(info.getWaitingForRead()));
            writer.format("%20s: %s\n\n", "Waiting for write", Arrays.toString(info.getWaitingForWrite()));
        }
    }

    public static void debug(PrintWriter writer) {
        writer.println("Threads currently waiting for a lock:");
        writer.println("=====================================");
        Map<String, LockInfo> threads = getWaitingThreads();
        for (Map.Entry<String, LockInfo> entry : threads.entrySet()) {
            debug(writer, entry.getKey().toString(), entry.getValue());
        }
    }

    public static String arrayToString(Object[] array) {
        if (array == null) return "null";
        if (array.length == 0) return "[]";
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < array.length; i++) {
            if (i == 0) buf.append('['); else buf.append(", ");
            buf.append(array[i] == null ? "null" : array[i].toString());
        }
        buf.append("]");
        return buf.toString();
    }
}
