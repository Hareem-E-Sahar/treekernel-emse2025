package net.sf.bluecove.se;

import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import net.sf.bluecove.Configuration;
import org.bluecove.tester.log.Logger;
import org.bluecove.tester.util.CLDCStub;
import org.bluecove.tester.util.IOUtils;
import org.bluecove.tester.util.RuntimeDetect;

public class JavaSECommon implements CLDCStub {

    private static boolean initialized = false;

    public static void initOnce() {
        if (initialized) {
            return;
        }
        initialized = true;
        Configuration.logTimeStamp = true;
        Logger.addAppender(new LoggerJavaSEAppender());
        if (Configuration.serverAcceptWhileConnectedOnJavaSE) {
            Configuration.serverAcceptWhileConnected.setValue(true);
        }
        RuntimeDetect.cldcStub = new JavaSECommon();
    }

    public boolean canInterruptThread() {
        return true;
    }

    public void interruptThread(Thread t) {
        if (t != null) {
            t.interrupt();
        }
    }

    public Thread createNamedThread(Runnable target, String name) {
        return new Thread(target, name);
    }

    public void setThreadLocalBluetoothStack(Object id) {
        LocalDeviceManager.setThreadLocalBluetoothStack(id);
    }

    public static boolean isJava5() {
        try {
            return java5Function();
        } catch (Throwable e) {
            return false;
        }
    }

    static boolean java5Function() {
        return (Thread.currentThread().getStackTrace() != null);
    }

    public static void threadDump() {
        SimpleDateFormat fmt = new SimpleDateFormat("MM-dd_HH-mm-ss");
        OutputStreamWriter out = null;
        try {
            File file = new File("ThreadDump-" + fmt.format(new Date()) + ".log");
            out = new FileWriter(file);
            Map traces = Thread.getAllStackTraces();
            for (Iterator iterator = traces.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Thread thread = (Thread) entry.getKey();
                out.write("Thread= " + thread.getName() + " " + (thread.isDaemon() ? "daemon" : "") + " prio=" + thread.getPriority() + "id=" + thread.getId() + " " + thread.getState());
                out.write("\n");
                StackTraceElement[] ste = (StackTraceElement[]) entry.getValue();
                for (int i = 0; i < ste.length; i++) {
                    out.write("\t");
                    out.write(ste[i].toString());
                    out.write("\n");
                }
                out.write("---------------------------------\n");
            }
            out.close();
            out = null;
            Logger.info("Full ThreadDump created " + file.getAbsolutePath());
        } catch (Throwable ignore) {
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    public static void logSystemProperties() {
        StringBuffer sysProperties = new StringBuffer();
        Properties properties = System.getProperties();
        Vector list2sort = new Vector();
        int max_key = 0;
        for (Iterator iterator = properties.keySet().iterator(); iterator.hasNext(); ) {
            Object keyObj = iterator.next();
            String key = keyObj.toString();
            list2sort.add(key);
            int len = key.length();
            if (len > max_key) {
                max_key = len;
            }
        }
        Collections.sort(list2sort);
        if (max_key > 41) {
            max_key = 41;
        }
        for (Iterator iterator = list2sort.iterator(); iterator.hasNext(); ) {
            String key = (String) iterator.next();
            StringBuffer key_p = new StringBuffer(key);
            while (key_p.length() < max_key) {
                key_p.append(" ");
            }
            String value = (String) properties.get(key);
            if (value == null) {
                value = "{null}";
            }
            StringBuffer value_p = new StringBuffer(value);
            value_p.append("]");
            while (value_p.length() < 60) {
                value_p.append(" ");
            }
            sysProperties.append(key_p.toString() + " = [" + value_p.toString() + "\n");
        }
        Logger.debug("System Properties:\n" + sysProperties.toString());
    }
}
