import java.util.*;
import java.io.*;
import javax.swing.*;

public class JVMStarter {

    static String[] sort(Set set) {
        int size = set.size();
        String[] keys = new String[size];
        set.toArray(keys);
        Arrays.sort(keys);
        return keys;
    }

    public static void printMemoryInfo() {
        Runtime run = Runtime.getRuntime();
        long heap = run.totalMemory() / 1024;
        long free = run.freeMemory() / 1024;
        long used = run.totalMemory() / 1024 - free;
        out.println("Used memory " + used + " heap " + heap + " free " + free);
    }

    public static void printActiveThreadList() {
        int count = Thread.activeCount();
        Thread[] threads = new Thread[count + 1];
        Thread.enumerate(threads);
        for (int x = 0; x < threads.length; x++) {
            Thread current = threads[x];
            if (current == null) continue;
            out.println("Thread " + x + " is " + current.getName() + " " + current.toString() + " class " + current.getClass().toString());
        }
    }

    public static void showSwingProperties() {
        String[] keys = sort(UIManager.getDefaults().keySet());
        for (int x = 0; x < keys.length; x++) {
            String key = keys[x];
            String value = System.getProperty(key);
            out.println("Key: " + key + " Value: " + value);
        }
    }

    public static void showSystemProperties() {
        String[] keys = sort(System.getProperties().keySet());
        for (int x = 0; x < keys.length; x++) {
            String key = keys[x];
            String value = System.getProperty(key);
            out.println("Key: " + key + " Value: " + value);
        }
    }

    static String staticField = "Default Value";

    public static void waitOnKey() throws Exception {
        out.println("Press enter on keyboard!");
        System.in.read();
    }

    public static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {

            public void run() {
                out.println("SHUTTING DOWN!!");
            }
        });
    }

    public static void exec() throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();
        String[] params;
        if (System.getProperty("path.separator", ":").equals(":")) {
            params = new String[] { "sh", "-c", "set" };
        } else {
            params = new String[] { "cmd", "/C", "set" };
        }
        Process p = rt.exec(params);
        InputStream in = p.getInputStream();
        int c;
        while ((c = in.read()) != -1) {
            out.print((char) c);
        }
        in.close();
    }

    public static void printArgs(String[] args) {
        if (args.length > 0) {
            System.out.println("main arguments:");
            for (int x = 0; x < args.length; x++) {
                out.println("args[" + x + "]=" + args[x]);
            }
        } else System.out.println("No main arguments.");
    }

    public static PrintStream out;

    public static void printInfo(String[] args) throws Exception {
        out.println();
        out.println("You have called a Test class!");
        out.println("I was loaded from : " + args.getClass().getResource("/JVMStarter.class"));
        printArgs(args);
        out.println("The static field is: " + JVMStarter.staticField);
        showSystemProperties();
        printActiveThreadList();
    }

    public static void main(String[] args) throws Exception {
        out = System.out;
        if (args.length > 0) {
            JVMStarter.staticField = args[0];
            if (args[0].equalsIgnoreCase("-exec")) exec();
            if (args[0].equalsIgnoreCase("-exception")) throw new Exception("Too much parameters!!");
            if (args[0].equalsIgnoreCase("-wait")) waitOnKey(); else if (args[0].equalsIgnoreCase("-sleep")) Thread.sleep(Long.parseLong(args[1]));
        } else {
            printInfo(args);
        }
    }
}
