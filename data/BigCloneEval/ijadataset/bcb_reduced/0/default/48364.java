import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sun.awt.windows.ThemeReader;

/**
 * Simple Launcher that automatically builds up the Classpath and invokes the
 * main class. This negates the need to write a bat/cmd/sh script for each
 * platform.
 * 
 * You invoke it by
 * 
 * <pre>
 * # java ScriptingFrameworkLauncher [OPTIONS]
 * </pre>
 * 
 * @author seans
 * 
 */
public class ScriptingFrameworkLauncher {

    public static final String DEFAULT_MAIN_CLASS = "sagex.ScriptRunner";

    public static final String HOME_DIR_VAR = "SAGE_SCRIPTING_FRAMEWORK_HOME";

    public static boolean debug = false;

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        debug = Boolean.getBoolean("SageScriptingFramework.debug");
        String mainClass = System.getProperty("jdna.MainClass", DEFAULT_MAIN_CLASS);
        if (debug) System.out.println("Using main class: " + mainClass);
        String home = System.getenv(HOME_DIR_VAR);
        if (home == null) {
            home = ".";
        }
        File homeDir = null;
        try {
            homeDir = new File(home).getCanonicalFile();
        } catch (IOException e2) {
            System.out.println("Failed to set home dir: " + home);
            System.exit(1);
        }
        if (debug) System.out.println("Assuming Home Dir: " + homeDir.getAbsolutePath());
        if (!homeDir.exists() && !homeDir.isDirectory()) {
            System.out.printf("Home Dir: %s does not exist, or is not a valid directory.\n", home);
            System.exit(1);
        }
        java.util.List<URL> urls = new ArrayList<URL>();
        File defClassDir = new File(homeDir, "bin");
        if (defClassDir.exists()) {
            if (debug) System.out.println("Adding Bin dir: " + defClassDir.getAbsolutePath());
            try {
                urls.add(defClassDir.toURI().toURL());
            } catch (MalformedURLException e1) {
                if (debug) e1.printStackTrace();
            }
        }
        File libDir = new File(homeDir, "JARs");
        File libs[] = libDir.listFiles();
        for (File lib : libs) {
            if (lib.getName().endsWith(".jar")) {
                if (debug) System.out.println("Adding Lib: " + lib.getAbsolutePath());
                urls.add(lib.toURI().toURL());
            }
        }
        urls.add(new File("scripts").toURI().toURL());
        urls.add(new File("scriptsLib").toURI().toURL());
        URLClassLoader cl = new URLClassLoader(urls.toArray(new URL[urls.size()]), ScriptingFrameworkLauncher.class.getClassLoader());
        if (debug) {
            for (URL u : cl.getURLs()) {
                System.out.printf("ClassLoader Url: %s\n", u.toExternalForm());
            }
        }
        Thread.currentThread().setContextClassLoader(cl);
        Class run = null;
        try {
            run = cl.loadClass(mainClass);
        } catch (ClassNotFoundException e) {
            System.out.printf("Failed to Find Class: %s\nPlease set the %s environment variable.\n", mainClass, HOME_DIR_VAR);
            System.exit(1);
        }
        Method m = null;
        try {
            m = run.getMethod("main", new Class[] { String[].class });
        } catch (Exception e) {
            System.out.println("Failed to find a valid main() method for class: " + mainClass);
            System.exit(1);
        }
        try {
            if (debug) System.out.printf("Invoking with %d args\n", args.length);
            m.invoke(null, (Object) args);
        } catch (Exception e) {
            System.out.println("Error Launching Class: " + mainClass);
            e.printStackTrace();
            System.exit(1);
        }
        if (debug) System.out.println("Command completed without error.");
        System.exit(0);
    }

    private static void checkDuplicate(String pattern, File libs[]) throws Exception {
        Pattern p = Pattern.compile(pattern);
        int hits = 0;
        for (File lib : libs) {
            if (lib.getName().endsWith(".jar")) {
                Matcher m = p.matcher(lib.toURI().toASCIIString());
                if (m.find()) {
                    hits++;
                }
            }
        }
        if (hits == 0) {
            throw new Exception("Missing required jar: " + pattern);
        }
        if (hits > 1) {
            System.err.println("You have too many(" + hits + ") jars in your libs directory with the following pattern: " + pattern);
            System.err.println("You need to clean the libs directory and ensure that you only have a single version of each jar file.");
            System.exit(1);
        }
    }
}
