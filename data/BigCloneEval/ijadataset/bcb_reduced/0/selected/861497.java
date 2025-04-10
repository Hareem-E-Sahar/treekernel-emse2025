package joeq.Scheduler;

import joeq.Allocator.CodeAllocator;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_InstanceMethod;
import joeq.Memory.CodeAddress;
import joeq.Memory.HeapAddress;
import joeq.Memory.StackAddress;
import joeq.Runtime.Debug;
import joeq.Runtime.SystemInterface;
import joeq.Runtime.Unsafe;
import jwutil.util.Assert;

public class jq_InterrupterThread extends Thread {

    public static boolean TRACE = false;

    public static final boolean STATISTICS = true;

    jq_InterrupterThread(jq_NativeThread other_nt) {
        this.other_nt = other_nt;
        if (TRACE) SystemInterface.debugwriteln("Initialized timer interrupt for native thread " + other_nt);
        myself = ThreadUtils.getJQThread(this);
        myself.disableThreadSwitch();
        this.tid = SystemInterface.create_thread(_run.getDefaultCompiledVersion().getEntrypoint(), HeapAddress.addressOf(this));
        jq_NativeThread my_nt = new jq_NativeThread(myself);
        my_nt.getCodeAllocator().init();
        my_nt.getHeapAllocator().init();
        SystemInterface.resume_thread(this.tid);
    }

    private int tid, pid;

    private jq_NativeThread other_nt;

    private jq_Thread myself;

    private int enabledCount;

    private int disabledCount;

    public void dumpStatistics() {
        Debug.write("enabled=");
        Debug.write(enabledCount);
        Debug.write(" disabled=");
        Debug.writeln(disabledCount);
    }

    public static int QUANTA = 10;

    public void run() {
        this.pid = SystemInterface.init_thread();
        Unsafe.setThreadBlock(this.myself);
        SystemInterface.set_thread_priority(this.tid, SystemInterface.THREAD_PRIORITY_TIME_CRITICAL);
        for (; ; ) {
            SystemInterface.msleep(QUANTA);
            other_nt.suspend();
            jq_Thread javaThread = other_nt.getCurrentJavaThread();
            if (javaThread.isThreadSwitchEnabled()) {
                if (STATISTICS) ++enabledCount;
                if (TRACE) SystemInterface.debugwriteln("TICK! " + other_nt + " Java Thread = " + javaThread);
                javaThread.disableThreadSwitch();
                Assert._assert(other_nt.getCurrentJavaThread() == javaThread);
                jq_RegisterState regs = javaThread.getRegisterState();
                regs.setContextFlags(jq_RegisterState.CONTEXT_CONTROL | jq_RegisterState.CONTEXT_INTEGER | jq_RegisterState.CONTEXT_FLOATING_POINT);
                boolean b = other_nt.getContext(regs);
                if (!b) {
                    if (TRACE) SystemInterface.debugwriteln("Failed to get thread context for " + other_nt);
                } else {
                    if (TRACE) SystemInterface.debugwriteln(other_nt + " : " + javaThread + " ip=" + regs.getEip().stringRep() + " sp=" + regs.getEsp().stringRep() + " cc=" + CodeAllocator.getCodeContaining(regs.getEip()));
                    regs.setEsp((StackAddress) regs.getEsp().offset(-HeapAddress.size()));
                    regs.getEsp().poke(HeapAddress.addressOf(other_nt));
                    regs.setEsp((StackAddress) regs.getEsp().offset(-CodeAddress.size()));
                    regs.getEsp().poke(regs.getEip());
                    regs.setEip(jq_NativeThread._threadSwitch.getDefaultCompiledVersion().getEntrypoint());
                    regs.setContextFlags(jq_RegisterState.CONTEXT_CONTROL);
                    b = other_nt.setContext(regs);
                    if (!b) {
                        if (TRACE) SystemInterface.debugwriteln("Failed to set thread context for " + other_nt);
                    } else {
                        if (TRACE) SystemInterface.debugwriteln(other_nt + " : simulating a call to threadSwitch");
                    }
                }
            } else {
                if (STATISTICS) ++disabledCount;
            }
            other_nt.resume();
        }
    }

    public static final jq_Class _class;

    public static final jq_InstanceMethod _run;

    static {
        _class = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Scheduler/jq_InterrupterThread;");
        _run = _class.getOrCreateInstanceMethod("run", "()V");
    }
}
