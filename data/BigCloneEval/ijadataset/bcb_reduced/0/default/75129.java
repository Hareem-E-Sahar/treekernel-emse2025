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
        System.out.println("Used memory " + used + " heap " + heap + " free " + free);
    }

    public static void printActiveThreadList() {
        int count = Thread.activeCount();
        Thread[] threads = new Thread[count + 1];
        Thread.enumerate(threads);
        for (int x = 0; x < threads.length; x++) {
            Thread current = threads[x];
            if (current == null) continue;
            System.out.println("Thread " + x + " is " + current.getName() + " " + current.toString() + " class " + current.getClass().toString());
        }
    }

    public static void showSwingProperties() {
        String[] keys = sort(UIManager.getDefaults().keySet());
        for (int x = 0; x < keys.length; x++) {
            String key = keys[x];
            String value = System.getProperty(key);
            System.out.println("Key: " + key + " Value: " + value);
        }
    }

    public static void showSystemProperties() {
        String[] keys = sort(System.getProperties().keySet());
        for (int x = 0; x < keys.length; x++) {
            String key = keys[x];
            String value = System.getProperty(key);
            System.out.println("Key: " + key + " Value: " + value);
        }
    }

    static String staticField = "Default Value";

    public static void waitOnKey() throws Exception {
        System.out.println("Press enter on keyboard!");
        System.in.read();
    }

    public static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {

            public void run() {
                System.out.println("SHUTTING DOWN!!");
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
            System.out.print((char) c);
        }
        in.close();
    }

    public static void main(String[] args) throws Exception {
        System.out.println();
        System.out.println("You have called a Dummy class!");
        System.out.println("I was loaded from : " + args.getClass().getResource("/JVMStarter.class"));
        for (int x = 0; x < args.length; x++) {
            System.out.println("args[" + x + "]=" + args[x]);
        }
        System.out.println("The static field is: " + JVMStarter.staticField);
        if (args.length > 0) JVMStarter.staticField = args[0];
        if (args.length > 5) throw new Exception("Too much parameters!!");
        if (args.length == 0) exec(); else {
            showSystemProperties();
            printActiveThreadList();
            if (args[0].equalsIgnoreCase("wait")) waitOnKey(); else if (args[0].equalsIgnoreCase("sleep")) Thread.currentThread().sleep(Long.parseLong(args[1]));
        }
    }
}
