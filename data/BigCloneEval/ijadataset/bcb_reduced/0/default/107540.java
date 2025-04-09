import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;

/**
 * DexFile tests (Dalvik-specific).
 */
public class Main {

    private static final String CLASS_PATH = "test-ex.jar";

    private static final String ODEX_DIR = "/sdcard";

    private static final String ODEX_ALT = "/tmp";

    private static final String LIB_DIR = "/nowhere/nothing/";

    /**
     * Prep the environment then run the test.
     */
    public static void main(String[] args) {
        Process p;
        try {
            ProcessBuilder pb = new ProcessBuilder("cat", "/dev/random");
            p = pb.start();
        } catch (IOException ioe) {
            System.err.println("cmd failed: " + ioe.getMessage());
            p = null;
        }
        try {
            testDexClassLoader();
        } finally {
            if (p != null) p.destroy();
            try {
                Thread.sleep(500);
            } catch (Exception ex) {
            }
        }
        System.out.println("done");
    }

    /**
     * Create a class loader, explicitly specifying the source DEX and
     * the location for the optimized DEX.
     */
    private static void testDexClassLoader() {
        ClassLoader dexClassLoader = getDexClassLoader();
        Class anotherClass;
        try {
            anotherClass = dexClassLoader.loadClass("Another");
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException("Another?");
        }
        Object another;
        try {
            another = anotherClass.newInstance();
        } catch (IllegalAccessException ie) {
            throw new RuntimeException("new another", ie);
        } catch (InstantiationException ie) {
            throw new RuntimeException("new another", ie);
        }
        dexClassLoader.getResource("nonexistent");
    }

    private static ClassLoader getDexClassLoader() {
        String odexDir;
        File test = new File(ODEX_DIR);
        if (test.isDirectory()) odexDir = ODEX_DIR; else odexDir = ODEX_ALT;
        ClassLoader myLoader = Main.class.getClassLoader();
        Class dclClass;
        try {
            dclClass = myLoader.loadClass("dalvik.system.DexClassLoader");
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException("dalvik.system.DexClassLoader not found");
        }
        Constructor ctor;
        try {
            ctor = dclClass.getConstructor(String.class, String.class, String.class, ClassLoader.class);
        } catch (NoSuchMethodException nsme) {
            throw new RuntimeException("DCL ctor", nsme);
        }
        Object dclObj;
        try {
            dclObj = ctor.newInstance(CLASS_PATH, odexDir, LIB_DIR, myLoader);
        } catch (Exception ex) {
            throw new RuntimeException("DCL newInstance", ex);
        }
        return (ClassLoader) dclObj;
    }
}
