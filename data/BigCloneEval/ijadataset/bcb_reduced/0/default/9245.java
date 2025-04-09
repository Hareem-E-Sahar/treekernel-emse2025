import sun.misc.CVM;
import java.lang.reflect.*;

public class runNamedTest {

    static String[] shift(String[] in) {
        String out[] = new String[in.length - 1];
        System.arraycopy(in, 1, out, 0, out.length);
        return out;
    }

    public static void main(String[] args) {
        boolean doRun = true;
        boolean doRunCompiled = true;
        String targetName;
        Class targetClass;
        Method targetMain = null;
        String[] kbItems = { "-f", "classesToCompile.txt" };
        if (args.length > 0 && args[0].equals("-n")) {
            doRun = false;
            args = shift(args);
        }
        if (args.length > 0 && args[0].equals("-c")) {
            doRunCompiled = false;
            args = shift(args);
        }
        targetName = args[0];
        args = shift(args);
        try {
            targetClass = Class.forName(targetName);
            targetMain = targetClass.getDeclaredMethod("main", new Class[] { kbItems.getClass() });
        } catch (Exception e) {
            System.err.println("Lookup of " + targetName + ".main got an exception:");
            e.printStackTrace();
            System.exit(1);
        }
        if (doRun) {
            System.err.println("Running " + targetName + " interpreted\n");
            try {
                targetMain.invoke(null, new Object[] { args });
            } catch (Exception e) {
                System.err.println(targetName + ".main got an exception:");
                e.printStackTrace();
                System.exit(1);
            }
        }
        System.err.println("Running GC\n");
        System.gc();
        System.err.println("Compiling named classes...\n");
        CompilerTest.main(kbItems);
        System.err.println("Running GC\n");
        System.gc();
        if (doRunCompiled) {
            System.err.println("Running " + targetName + " compiled\n");
            try {
                targetMain.invoke(null, new Object[] { args });
            } catch (Exception e) {
                System.err.println(targetName + ".main got an exception:");
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}
