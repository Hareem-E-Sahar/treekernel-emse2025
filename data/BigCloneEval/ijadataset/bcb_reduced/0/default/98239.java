import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * Invokes JPF after setting up classpath, etc.
 * This class replaces a shell script that used to do all this stuff, but
 * JPF is run in the same virtual machine as the one that RunJPF is run
 * in, so if you want a large heap use something like
 * <blockquote>
 * java -Xmx1024M RunJPF +vm.classpath=/somewhere MyModel
 * </blockquote>
 * If you want to run MyModel using the regular virtual machine but you need
 * JPF stuff in your classpath (like Verify), use this:
 * <blockquote>
 * java RunJava +vm.classpath=/somewhere MyModel
 * </blockquote>
 * Have fun!
 * @author peterd
 */
public class RunJPF extends RunAnt {

    /**
	 * Adds to the classpath stored in RunAnt all the directories
	 * and jars needed to run JPF.  Also includes test cases,
	 * extensions, etc. for good measure.
	 */
    public static void addJpfClassPath() {
        ArrayList<File> paths = new ArrayList<File>();
        File buildDir = new File(jpfHome, "build");
        paths.add(new File(buildDir, "jpf"));
        paths.add(new File(buildDir, "test"));
        addJars(new File(jpfHome, "lib"), paths, true);
        addJars(new File(jpfHome, "extensions"), paths, true);
        paths.add(new File(new File(buildDir, "env"), "jvm"));
        paths.add(new File(buildDir, "examples"));
        addToClassPath(paths);
    }

    public static void main(String[] args) throws Throwable {
        if (args.length == 0) {
            for (String s : usage) {
                System.err.println(s);
            }
            System.exit(1);
        }
        computeJpfHome(args);
        addJpfClassPath();
        if (System.getenv("VERBOSE") != null) {
            System.err.println("CLASSPATH: " + cpString);
        }
        String jpfClassName = "gov.nasa.jpf.JPF";
        String[] targetArgs = new String[args.length + 1];
        targetArgs[0] = "+jpf.basedir=" + jpfHome;
        System.arraycopy(args, 0, targetArgs, 1, args.length);
        if (System.getenv("VERBOSE") != null) {
            System.err.print("main & args: " + jpfClassName);
            for (int i = 0; i < targetArgs.length; i++) {
                System.err.print(" ");
                System.err.print(targetArgs[i]);
            }
            System.err.println();
        }
        try {
            ClassLoader cl = getClassPathClassLoader();
            Class<?> jpfClass = cl.loadClass(jpfClassName);
            Constructor<?> jpfConst = jpfClass.getConstructor(new Class[] { String[].class });
            if (jpfConst == null || !Runnable.class.isAssignableFrom(jpfClass)) {
                throw new ClassNotFoundException("Invalid/out of date " + jpfClassName + " class");
            }
            Runnable jpf = (Runnable) jpfConst.newInstance(new Object[] { targetArgs });
            jpf.run();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("Make sure you have compiled JPF into build/jpf.");
            System.err.println("Try this:  java RunAnt");
            System.exit(1);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (InvocationTargetException e) {
            printTrimmedStackTrace(e.getTargetException());
            System.exit(1);
        }
    }

    static String[] usage = { "This is a wrapper for executing Java PathFinder, which starts with the class", "gov.nasa.jpf.JPF.  This wrapper takes care of locating the JPF install root", "and setting up an appropriate classpath.", "", "Usage: java [<vm-options>..] RunJPF [<jpf-args>...]", "", "For more information on <vm-options>, run \"java -help\".", "For more information on <jpf-args>, run \"java RunJPF -help\".", "", "Examples:", "  From the JPF root directory:", "    java -Xmx1024m RunJPF +vm.classpath=path/to/model MyModel", "  From directory with model classes:", "    java -Xmx1024m -cp path/to/jpf RunJPF +vm.classpath=. MyModel ", "  or (more concise, less elegant):", "    java -Xmx1024m -cp path/to/jpf:. RunJPF MyModel ", "" };
}
