public class Test {    public void dumpProcessorState() throws VM_PragmaInterruptible {
        VM_Scheduler.writeString("Processor ");
        VM_Scheduler.writeDecimal(id);
        if (this == VM_Processor.getCurrentProcessor()) VM_Scheduler.writeString(" (me)");
        VM_Scheduler.writeString(" running thread");
        if (activeThread != null) activeThread.dump(); else VM_Scheduler.writeString(" NULL Active Thread");
        VM_Scheduler.writeString("\n");
        VM_Scheduler.writeString(" system thread id ");
        VM_Scheduler.writeDecimal(pthread_id);
        VM_Scheduler.writeString("\n");
        VM_Scheduler.writeString(" transferQueue:");
        if (transferQueue != null) transferQueue.dump();
        VM_Scheduler.writeString(" readyQueue:");
        if (readyQueue != null) readyQueue.dump();
        VM_Scheduler.writeString(" ioQueue:");
        if (ioQueue != null) ioQueue.dump();
        VM_Scheduler.writeString(" processWaitQueue:");
        if (processWaitQueue != null) processWaitQueue.dump();
        VM_Scheduler.writeString(" idleQueue:");
        if (idleQueue != null) idleQueue.dump();
        if (processorMode == RVM) VM_Scheduler.writeString(" mode: RVM\n"); else if (processorMode == NATIVE) VM_Scheduler.writeString(" mode: NATIVE\n"); else if (processorMode == NATIVEDAEMON) VM_Scheduler.writeString(" mode: NATIVEDAEMON\n");
        VM_Scheduler.writeString(" status: ");
        int status = vpStatus[vpStatusIndex];
        if (status == IN_NATIVE) VM_Scheduler.writeString("IN_NATIVE\n");
        if (status == IN_JAVA) VM_Scheduler.writeString("IN_JAVA\n");
        if (status == BLOCKED_IN_NATIVE) VM_Scheduler.writeString("BLOCKED_IN_NATIVE\n");
        if (status == IN_SIGWAIT) VM_Scheduler.writeString("IN_SIGWAIT\n");
        if (status == BLOCKED_IN_SIGWAIT) VM_Scheduler.writeString("BLOCKED_IN_SIGWAIT\n");
        VM_Scheduler.writeString(" threadSwitchRequested: ");
        VM_Scheduler.writeDecimal(threadSwitchRequested);
        VM_Scheduler.writeString("\n");
    }
}