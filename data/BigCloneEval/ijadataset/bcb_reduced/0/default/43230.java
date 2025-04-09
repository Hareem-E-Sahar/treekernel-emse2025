import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * This is essentially a java replacement for a shell script that sets up
 * the classpath, etc for running Ant.
 * 
 * @author peterd
 */
public class RunAnt {

    static final Properties props;

    static final File jreHome;

    static final File javaHome;

    static final File userDir;

    static final File toolsJar;

    static File[] classPath;

    static String cpString;

    static File jpfHome = null;

    static File buildToolsLib = null;

    static {
        props = System.getProperties();
        jreHome = new File(props.getProperty("java.home"));
        if (!jreHome.isDirectory()) {
            throw new InternalError("java.home property not a valid directory!");
        }
        userDir = new File(props.getProperty("user.dir"));
        if (!userDir.isDirectory()) {
            throw new InternalError("user.dir property not a valid directory!");
        }
        javaHome = jreHome.getParentFile();
        toolsJar = new File(new File(javaHome, "lib"), "tools.jar");
        cpString = "";
        updateClassPathInfo();
    }

    private static void updateClassPathInfo() {
        String newCp = props.getProperty("java.class.path");
        if (newCp.equals(cpString)) return;
        cpString = newCp;
        StringTokenizer cpToks = new StringTokenizer(cpString, File.pathSeparator);
        ArrayList<File> cpArr = new ArrayList<File>();
        while (cpToks.hasMoreTokens()) {
            cpArr.add(new File(cpToks.nextToken()));
        }
        classPath = new File[cpArr.size()];
        cpArr.toArray(classPath);
    }

    public static String getArg(String[] args, String pattern, String defValue, boolean consume) {
        String s = defValue;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg != null) {
                if (arg.matches(pattern)) {
                    int idx = arg.indexOf('=');
                    if (idx > 0) {
                        s = arg.substring(idx + 1);
                        if (consume) {
                            args[i] = null;
                        }
                    } else if (i < args.length - 1) {
                        s = args[i + 1];
                        if (consume) {
                            args[i] = null;
                            args[i + 1] = null;
                        }
                    }
                    break;
                }
            }
        }
        return s;
    }

    /**
	 * Determines JPF's root directory.  It trys each of the following
	 * until it finds one that contains 'env/jpf':  jpf.basedir property,
	 * +jpf.basedir= parameter on command line (if command line is passed
	 * in), the current working directory, and then all the directories in
	 * the classpath.
	 * <p>
	 * The result is stored in jpfHome or a RuntimeException is thrown.
	 * 
	 * @param args arguments to main if we want to search for matching
	 * argument.
	 */
    public static void computeJpfHome(String[] args) {
        ArrayList<File> possibleJpfHomes = new ArrayList<File>();
        String prop = props.getProperty("jpf.basedir");
        if (prop != null) {
            possibleJpfHomes.add(new File(prop));
        }
        prop = getArg(args, "[+]jpf[.]basedir(=.+)?", null, false);
        if (prop != null) {
            possibleJpfHomes.add(new File(prop));
        }
        possibleJpfHomes.add(userDir);
        possibleJpfHomes.addAll(Arrays.asList(classPath));
        for (File poss : possibleJpfHomes) {
            if (poss.isDirectory()) {
                if (new File(new File(poss, "env"), "jpf").isDirectory()) {
                    jpfHome = poss;
                    buildToolsLib = new File(new File(jpfHome, "build-tools"), "lib");
                    return;
                } else if (poss.getName().equals("jpf") && poss.getParentFile() != null && poss.getParentFile().getName().equals("build")) {
                    jpfHome = poss.getParentFile().getParentFile();
                    buildToolsLib = new File(new File(jpfHome, "build-tools"), "lib");
                    return;
                }
            }
        }
        throw new RuntimeException("Unable to find JPF home dir.  Either " + "make current directory, or put in classpath.");
    }

    public static void addToClassPath(List<File> paths) {
        updateClassPathInfo();
        StringBuilder cpBuilder = new StringBuilder(cpString);
        for (int i = 0; i < paths.size(); i++) {
            cpBuilder.append(File.pathSeparatorChar);
            cpBuilder.append(paths.get(i));
        }
        props.setProperty("java.class.path", cpBuilder.toString());
        updateClassPathInfo();
    }

    public static void removeFromClassPath(File path) {
        updateClassPathInfo();
        String canonical;
        try {
            canonical = path.getCanonicalPath();
        } catch (IOException e) {
            canonical = path.getAbsolutePath();
        }
        ArrayList<File> newPaths = new ArrayList<File>(classPath.length);
        for (int i = 0; i < classPath.length; i++) {
            String canonical2;
            try {
                canonical2 = classPath[i].getCanonicalPath();
            } catch (IOException ioe) {
                canonical2 = classPath[i].getAbsolutePath();
            }
            if (!canonical.equals(canonical2)) {
                newPaths.add(classPath[i]);
            }
        }
        StringBuilder cpBuilder = new StringBuilder();
        for (int i = 0; i < newPaths.size(); i++) {
            if (i > 0) cpBuilder.append(File.pathSeparatorChar);
            cpBuilder.append(newPaths.get(i));
        }
        props.setProperty("java.class.path", cpBuilder.toString());
        updateClassPathInfo();
    }

    /**
	 * Returns a list of jar/zip files within a directory.
	 * 
	 * @param parent directory to decend from.
	 * @param recursive whether to decent subdirectories.
	 * @return the jar/zip files found.
	 */
    public static List<File> findJars(File parent, boolean recursive) {
        List<File> ret = new ArrayList<File>();
        addJars(parent, ret, recursive);
        return ret;
    }

    /**
	 * Add all jar/zip files within a directory to a list.
	 * @param parent directory to decend from.
	 * @param paths list to add to.
	 * @param recursive whether to search subdirectories.
	 */
    public static void addJars(File parent, List<File> paths, boolean recursive) {
        File[] children = parent.listFiles();
        if (children == null) return;
        for (File child : children) {
            if (child.isDirectory()) {
                if (recursive) addJars(child, paths, true);
            } else if (child.isFile() && child.canRead()) {
                String name = child.getName();
                if (name.endsWith(".jar") || name.endsWith(".zip")) {
                    paths.add(child);
                }
            }
        }
    }

    /**
	 * Determines whether the class for directly invoking Javac is present
	 * in the current classpath.
	 */
    public static boolean javacPresent() {
        try {
            return Class.forName("com.sun.tools.javac.Main") != null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
	 * Returns a ClassLoader with the specified parent and searching the
	 * specified paths.
	 */
    public static ClassLoader getClassLoader(List<File> paths, ClassLoader parent) {
        URL[] urls = new URL[paths.size()];
        for (int i = 0; i < paths.size(); i++) {
            try {
                urls[i] = paths.get(i).toURI().toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                System.exit(2);
            }
        }
        return new URLClassLoader(urls);
    }

    /**
	 * Returns a ClassLoader for the updated classpath in the static fields.
	 */
    public static ClassLoader getClassPathClassLoader() {
        return getClassLoader(Arrays.asList(classPath), Object.class.getClassLoader());
    }

    /**
	 * Reflectively invokes the main method of the specified class with
	 * the given args.
	 */
    public static void invokeMain(Class<?> clazz, String[] args) throws NoSuchMethodException, InvocationTargetException {
        Class<?>[] params = new Class[] { String[].class };
        Method main = clazz.getMethod("main", params);
        if (!Modifier.isStatic(main.getModifiers())) {
            throw new NoSuchMethodException("main method in " + clazz + " not static!");
        }
        try {
            main.invoke(null, new Object[] { args });
        } catch (IllegalAccessException e) {
            throw new NoSuchMethodException("main method in " + clazz + " not accessible!");
        }
    }

    /**
   * Removes some reflection nastiness from printed stack traces.
   */
    public static void printTrimmedStackTrace(Throwable e) {
        StackTraceElement[] st = e.getStackTrace();
        LinkedList<StackTraceElement> newStList = new LinkedList<StackTraceElement>();
        int minStuff = Thread.currentThread().getStackTrace().length + 2;
        int i = 0;
        boolean internal = true;
        for (; i < st.length && (i <= minStuff || internal); i++) {
            StackTraceElement tmp = st[st.length - 1 - i];
            String cname = tmp.getClassName();
            internal = cname.startsWith("java.lang.") || cname.startsWith("sun.reflect.");
            if (!internal) {
                newStList.addFirst(tmp);
            }
        }
        for (; i < st.length; i++) {
            StackTraceElement tmp = st[st.length - 1 - i];
            newStList.addFirst(tmp);
        }
        StackTraceElement[] newSt = newStList.toArray(new StackTraceElement[newStList.size()]);
        e.setStackTrace(newSt);
        e.printStackTrace();
    }

    /**
	 * Runs ant in the JPF install tree.  The primary job here is
	 * setting up all the classpath stuff into a classloader.
	 */
    public static void main(String[] args) throws Throwable {
        computeJpfHome(new String[] {});
        ArrayList<File> paths = new ArrayList<File>();
        addJars(buildToolsLib, paths, true);
        if (!javacPresent()) {
            paths.add(toolsJar);
        }
        addToClassPath(paths);
        try {
            String antClassName = "org.apache.tools.ant.Main";
            ClassLoader cl = getClassPathClassLoader();
            Class<?> antMainClass = cl.loadClass(antClassName);
            invokeMain(antMainClass, args);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("Make sure Ant jars are available in build-tools/lib.");
            System.exit(1);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (InvocationTargetException e) {
            printTrimmedStackTrace(e.getTargetException());
            System.exit(1);
        }
    }
}
