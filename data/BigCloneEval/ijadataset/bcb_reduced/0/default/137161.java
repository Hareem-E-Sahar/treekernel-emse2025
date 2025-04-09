import java.lang.reflect.*;

/**
 * This java file starts benchmarks and measures run time. 
 * @keyword XXX_harness
 */
public class PerfCheck {

    public static void main(String[] args) {
        String test;
        if (args.length > 0) {
            test = args[0];
            runTest(test);
        } else {
            System.out.println("Please enter test to run!");
        }
    }

    public static boolean runTest(String test) {
        trace("The benchmark is: " + test);
        try {
            Class test_class = Class.forName(test);
            Method main = test_class.getMethod("main", new Class[] { String[].class });
            long itime = System.currentTimeMillis();
            main.invoke(null, new Object[] { new String[] {} });
            System.out.println("The test run: " + (System.currentTimeMillis() - itime) + " ms");
        } catch (Throwable e) {
            System.out.println(test + " failed, " + e);
            return false;
        }
        return true;
    }

    public static void trace(Object o) {
        System.out.println(o);
        System.out.flush();
    }
}
