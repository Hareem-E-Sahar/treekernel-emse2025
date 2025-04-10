import java.lang.reflect.Method;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

/**
 * An interactive debugger that runs inside the virtual machine.
 * This thread is normally dormant and only scheduled for execution
 * by VM_Thread.threadSwitch() following receipt of a debug request 
 * signal (SIGQUIT).
 *
 * @author Derek Lieber
 * @date 28 April 1999 
 */
class DebuggerThread extends VM_Thread implements VM_Uninterruptible {

    DebuggerThread() {
        makeDaemon(true);
    }

    public String toString() {
        return "DebuggerThread";
    }

    public void run() {
        for (; ; ) {
            try {
                VM.sysWrite("debug> ");
                eval(readTokens());
            } catch (Exception e) {
                VM.sysWrite("oops: " + e + "\n");
            }
        }
    }

    private static final char EOF = 0xFFFF;

    private static String[] previousTokens;

    private static void eval(String[] tokens) throws Exception {
        char command = tokens == null ? EOF : tokens.length == 0 ? ' ' : tokens[0].charAt(0);
        switch(command) {
            case ' ':
                if (previousTokens != null) eval(previousTokens);
                return;
            case '*':
                if (previousTokens != null) for (VM_Scheduler.debugRequested = false; VM_Scheduler.debugRequested == false; ) {
                    VM.sysWrite("\033[H\033[2J");
                    eval(previousTokens);
                    sleep(1000);
                }
                return;
        }
        previousTokens = tokens;
        switch(command) {
            case 't':
                if (tokens.length == 1) {
                    for (int i = 0, col = 0; i < VM_Scheduler.threads.length; ++i) {
                        VM_Thread thread = VM_Scheduler.threads[i];
                        if (thread == null) continue;
                        VM.sysWrite(rightJustify(thread.getIndex() + " ", 4) + leftJustify(thread.toString(), 40) + getThreadState(thread) + "\n");
                    }
                } else if (tokens.length == 2) {
                    int threadIndex = Integer.valueOf(tokens[1]).intValue();
                    VM_Thread thread = VM_Scheduler.threads[threadIndex];
                    VM.sysWrite(thread.getIndex() + " " + thread + " " + getThreadState(thread) + "\n");
                    VM_Address fp = (thread == VM_Thread.getCurrentThread()) ? VM_Magic.getFramePointer() : thread.contextRegisters.getInnermostFramePointer();
                    VM_Processor.getCurrentProcessor().disableThreadSwitching();
                    VM_Scheduler.dumpStack(fp);
                    VM_Processor.getCurrentProcessor().enableThreadSwitching();
                } else VM.sysWrite("please specify a thread id\n");
                return;
            case 'p':
                if (tokens.length == 2) {
                    int addr = Integer.parseInt(tokens[1], 16);
                    VM.sysWrite("Object at addr 0x");
                    VM.sysWriteHex(addr);
                    VM.sysWrite(": ");
                    VM_ObjectModel.describeObject(VM_Address.fromInt(addr));
                    VM.sysWriteln();
                } else VM.sysWriteln("Please specify an address\n");
                return;
            case 'd':
                VM_Scheduler.dumpVirtualMachine();
                return;
            case 'e':
                if (VM.BuildForEventLogging) {
                    VM.EventLoggingEnabled = !VM.EventLoggingEnabled;
                    VM.sysWrite("event logging " + (VM.EventLoggingEnabled ? "enabled\n" : "disabled\n"));
                } else VM.sysWrite("sorry, not built for event logging\n");
                return;
            case 'l':
                if (VM.BuildForEventLogging) {
                    String fileName = "temp.eventLog";
                    try {
                        FileOutputStream out = new FileOutputStream(fileName);
                        VM_EventLogger.dump(out);
                        out.close();
                        VM.sysWrite("event log written to \"" + fileName + "\"\n");
                    } catch (IOException e) {
                        VM.sysWrite("couldn't write \"" + fileName + "\": " + e + "\n");
                    }
                } else VM.sysWrite("sorry, not built for event logging\n");
                return;
            case 'c':
                VM_Scheduler.debugRequested = false;
                VM_Scheduler.debuggerMutex.lock();
                yield(VM_Scheduler.debuggerQueue, VM_Scheduler.debuggerMutex);
                return;
            case 'q':
                VM.sysWrite("terminating execution\n");
                VM.sysExit(0);
                return;
            case EOF:
                VM_Scheduler.writeString("\n-- Stacks --\n");
                for (int i = 1; i < VM_Scheduler.threads.length; ++i) {
                    VM_Thread t = VM_Scheduler.threads[i];
                    if (t != null) {
                        VM_Scheduler.writeString("\n Thread: ");
                        t.dump();
                        VM_Processor.getCurrentProcessor().disableThreadSwitching();
                        VM_Scheduler.dumpStack(t.contextRegisters.getInnermostFramePointer());
                        VM_Processor.getCurrentProcessor().enableThreadSwitching();
                    }
                }
                VM_Scheduler.writeString("\n");
                VM_Scheduler.dumpVirtualMachine();
                VM_Scheduler.debugRequested = false;
                VM_Scheduler.debuggerMutex.lock();
                yield(VM_Scheduler.debuggerQueue, VM_Scheduler.debuggerMutex);
                return;
            default:
                if (tokens.length == 1) {
                    VM.sysWrite("Try one of:\n");
                    VM.sysWrite("   t                - display all threads\n");
                    VM.sysWrite("   t <threadIndex>  - display specified thread\n");
                    VM.sysWrite("   p <hex addr>     - print (describe) object at given address\n");
                    VM.sysWrite("   d                - dump virtual machine state\n");
                    VM.sysWrite("   e                - enable/disable event logging\n");
                    VM.sysWrite("   l                - write event log to a file\n");
                    VM.sysWrite("   c                - continue execution\n");
                    VM.sysWrite("   q                - terminate execution\n");
                    VM.sysWrite("   <class>.<method> - call a method\n");
                    VM.sysWrite("Or:\n");
                    VM.sysWrite("   <enter>          - repeat previous command once\n");
                    VM.sysWrite("   *                - repeat previous command once per second until SIGQUIT is received\n");
                    return;
                }
                if (tokens.length != 3 || !tokens[1].equals(".")) VM.sysWrite("please specify <class>.<method>\n"); else {
                    Class cls = Class.forName(tokens[0]);
                    Class[] signature = new Class[0];
                    Method method = cls.getMethod(tokens[2], signature);
                    Object[] args = new Object[0];
                    method.invoke(null, args);
                }
                return;
        }
    }

    private static String getThreadState(VM_Thread t) {
        for (int i = 0; i < VM_Scheduler.processors.length; ++i) {
            VM_Processor p = VM_Scheduler.processors[i];
            if (p == null) continue;
            if (p.transferQueue.contains(t)) return "runnable (incoming) on processor " + i;
            if (p.readyQueue.contains(t)) return "runnable on processor " + i;
            if (p.ioQueue.contains(t)) return "waitingForIO (fd=" + t.waitFdRead + " ready=" + t.waitFdReady + ") on processor " + i;
            if (p.idleQueue.contains(t)) return "waitingForIdleWork on processor " + i;
        }
        if (VM_Scheduler.wakeupQueue.contains(t)) return "sleeping";
        if (VM_Scheduler.debuggerQueue.contains(t)) return "waitingForDebuggerWork";
        if (VM_Scheduler.gcWaitQueue.contains(t)) return "waitingForCollectorWork";
        if (VM_Scheduler.collectorQueue.contains(t)) return "waitingForCollectorWork";
        if (VM_Scheduler.deadQueue.contains(t)) return "waitingToBeReaped";
        for (int i = 0; i < VM_Scheduler.locks.length; ++i) {
            VM_Lock l = VM_Scheduler.locks[i];
            if (l == null || !l.active) continue;
            if (l.entering.contains(t)) return "waitingForLock";
            if (l.waiting.contains(t)) return "waitingForNotification";
        }
        for (int i = 0; i < VM_Scheduler.processors.length; ++i) {
            VM_Processor p = VM_Scheduler.processors[i];
            if (p == null) continue;
            if (p.activeThread == t) return "running on processor " + i;
        }
        return "unknown";
    }

    private static final int STDIN = 0;

    private static String[] readTokens() {
        String line = new String();
        int bb = VM_FileSystem.readByte(STDIN);
        if (bb < 0) return null;
        for (; bb >= 0 && bb != '\n'; bb = VM_FileSystem.readByte(STDIN)) line += (char) bb;
        Vector tokens = new Vector();
        for (int i = 0, n = line.length(); i < n; ++i) {
            char ch = line.charAt(i);
            if (isLetter(ch) || isDigit(ch)) {
                String alphaNumericToken = new String();
                while (isLetter(ch) || isDigit(ch)) {
                    alphaNumericToken += ch;
                    if (++i == n) break;
                    ch = line.charAt(i);
                }
                --i;
                tokens.addElement(alphaNumericToken);
                continue;
            }
            if (ch != ' ' && ch != '\r' && ch != '\t') {
                String symbol = new String();
                symbol += ch;
                tokens.addElement(symbol);
                continue;
            }
        }
        String[] results = new String[tokens.size()];
        for (int i = 0, n = results.length; i < n; ++i) results[i] = (String) tokens.elementAt(i);
        return results;
    }

    private static boolean isDigit(char ch) {
        return '0' <= ch && ch <= '9';
    }

    private static boolean isLetter(char ch) {
        return ('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z');
    }

    private static String leftJustify(String s, int width) {
        while (s.length() < width) s = s + " ";
        return s;
    }

    private static String rightJustify(String s, int width) {
        while (s.length() < width) s = " " + s;
        return s;
    }
}
