/**
 * This class manages finalization.  When an object is created
 * if its class has a finalize() method, addElement below is 
 * called, and a VM_FinalizerListElement (see VM_FinalizerListElement)
 * is created for it.  While the object is old, the integer field of 
 * the list element holds its value (this does not keep the object 
 * live during gc.  At the end of gc, the list of VM_FinalizerListElements
 * is scanned for objects which have become garbage.  Those which have
 * are made live again, by setting the ref field of the VM_FLE to the
 * object's address, and the VM_FLE is moved from the live objects list
 * to the to-be-finalized objects list.  A distinguished thread, the
 * Finalizer thread (see FinalizerThread.java) enqueues itself on 
 * the VM_Scheduler finalizerQueue.  At the end of gc, if VM_FLEs have 
 * been added to the to-be-finalized list, and if the VM_Scheduler
 * finalizerQueue is not empty, the finalizer thread is scheduled to be
 * run when gc is completed.
 *
 * @author Dick Attanasio
 * @author Stephen Smith
 */
public class VM_Finalizer {

    public static boolean finalizeOnExit = false;

    static VM_FinalizerListElement live_head;

    static VM_FinalizerListElement finalize_head;

    static int live_count;

    static int finalize_count;

    private static VM_Synchronizer locker;

    static boolean foundFinalizableObject;

    static int foundFinalizableCount = 0;

    private static final boolean TRACE = false;

    private static final boolean TRACE_DETAIL = false;

    private static final boolean PRINT_FINALIZABLE_COUNT = false;

    private static final boolean COUNT_BY_TYPES = false;

    public static void setup() {
        locker = new VM_Synchronizer();
    }

    static final void addElement(Object item) {
        VM_Magic.pragmaNoInline();
        synchronized (locker) {
            live_count++;
            if (TRACE_DETAIL) VM_Scheduler.trace(" VM_Finalizer: ", " addElement	called, count = ", live_count);
            VM_FinalizerListElement le = new VM_FinalizerListElement(item);
            VM_FinalizerListElement old = live_head;
            live_head = le;
            le.next = old;
        }
    }

    /**
   * Called from the mutator thread: return the first object queued 
   * on the finalize list, or null if none
   */
    static final Object get() {
        VM_FinalizerListElement temp = finalize_head;
        if (temp == null) return null;
        finalize_head = temp.next;
        finalize_count--;
        if (TRACE_DETAIL) {
            VM_Scheduler.trace(" VM_Finalizer: ", "get returning ", VM_Magic.objectAsAddress(temp.pointer));
            VM_Scheduler.trace(" VM_Finalizer: ", "finalize count is ", finalize_count);
        }
        return temp.pointer;
    }

    /**
   * return true if there are objects with finalizers
   * potentially needing to be finalized: called by
   * gcs at the end of a collection to determine whether 	
   * to call moveToFinalizable()
   */
    static final boolean existObjectsWithFinalizers() {
        return (live_head != null);
    }

    /** 
   * Move all finalizable objects to the to-be-finalized queue
   * Called on shutdown
  */
    static final void finalizeAll() {
        VM_FinalizerListElement le = live_head;
        VM_FinalizerListElement from = live_head;
        while (le != null) {
            live_count--;
            finalize_count++;
            if (TRACE) VM_Scheduler.trace("\n in finalizeall:", "le.value =", VM_Magic.objectAsAddress(le.pointer));
            VM_FinalizerListElement current = le.next;
            if (le == live_head) live_head = current; else from.next = current;
            VM_FinalizerListElement temp = finalize_head;
            finalize_head = le;
            le.next = temp;
            le = current;
        }
        if (!VM_Scheduler.finalizerQueue.isEmpty()) {
            VM_Thread tt = VM_Scheduler.finalizerQueue.dequeue();
            VM_Processor.getCurrentProcessor().scheduleThread(tt);
        }
    }

    /**
   * Scan the array for objects which have become garbage
   * and move them to the Finalizable class
   */
    static final void moveToFinalizable() {
        if (TRACE) VM_Scheduler.trace(" VM_Finalizer: ", " move to finalizable ");
        boolean added = false;
        boolean is_live = false;
        foundFinalizableObject = false;
        foundFinalizableCount = 0;
        int initial_finalize_count = finalize_count;
        VM_FinalizerListElement le = live_head;
        VM_FinalizerListElement from = live_head;
        while (le != null) {
            is_live = VM_Allocator.processFinalizerListElement(le);
            if (is_live) {
                from = le;
                le = le.next;
                continue;
            } else {
                added = true;
                live_count--;
                finalize_count++;
                if (TRACE) VM_Scheduler.trace("\n moving to finalizer:", "le.value =", VM_Magic.objectAsAddress(le.pointer));
                VM_FinalizerListElement current = le.next;
                if (le == live_head) live_head = current; else from.next = current;
                VM_FinalizerListElement temp = finalize_head;
                finalize_head = le;
                le.next = temp;
                le = current;
            }
        }
        if (added) {
            if (TRACE) VM_Scheduler.trace(" VM_Finalizer: ", " added was true");
            foundFinalizableObject = true;
            foundFinalizableCount = finalize_count - initial_finalize_count;
            if (!VM_Scheduler.finalizerQueue.isEmpty()) {
                VM_Thread tt = VM_Scheduler.finalizerQueue.dequeue();
                VM_Processor.getCurrentProcessor().scheduleThread(tt);
            }
        }
        if (PRINT_FINALIZABLE_COUNT && VM_Allocator.verbose >= 1) {
            VM.sysWrite("<GC ");
            VM.sysWrite(VM_Collector.collectionCount(), false);
            VM.sysWrite(" moveToFinalizable: finalize_count: before = ");
            VM.sysWrite(initial_finalize_count, false);
            VM.sysWrite(" after = ");
            VM.sysWrite(finalize_count, false);
            VM.sysWrite(">\n");
        }
    }

    static void schedule() {
        if ((finalize_head != null) && !VM_Scheduler.finalizerQueue.isEmpty()) {
            VM_Thread t = VM_Scheduler.finalizerQueue.dequeue();
            VM_Processor.getCurrentProcessor().scheduleThread(t);
        }
    }

    static int countHasFinalizer() {
        int count = 0;
        VM_FinalizerListElement le = live_head;
        while (le != null) {
            count++;
            le = le.next;
        }
        return count;
    }

    static int countToBeFinalized() {
        int count = 0;
        VM_FinalizerListElement le = finalize_head;
        while (le != null) {
            count++;
            le = le.next;
        }
        return count;
    }

    /**
   * A debugging routine: print out the type of each object in live_list
   */
    static final void dump_live() {
        VM_Scheduler.trace(" VM_Finalizer.dump_live", "cnt is ", live_count);
        VM.sysWrite("\n");
        VM_FinalizerListElement le = live_head;
        while (le != null) {
            VM.sysWrite(" In live_list: object type is ");
            VM_GCUtil.printclass(le.value);
            VM.sysWrite(" at ");
            VM.sysWrite(le.value);
            VM.sysWrite("\n");
            le = le.next;
        }
    }

    /** 
   * A debugging routine: print out the type of each object to be finalized
   */
    static final void dump_finalize() {
        VM_Scheduler.trace(" VM_Finalizer.dump_finalize", "cnt is ", finalize_count);
        VM.sysWrite("\n");
        VM_FinalizerListElement le = finalize_head;
        while (le != null) {
            VM.sysWrite(" In finalize_list: object type is ");
            VM_GCUtil.printclass(le.value);
            VM.sysWrite(" at ");
            VM.sysWrite(le.value);
            VM.sysWrite("\n");
            le = le.next;
        }
    }
}
