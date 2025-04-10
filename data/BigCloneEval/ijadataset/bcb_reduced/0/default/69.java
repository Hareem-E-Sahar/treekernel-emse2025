import java.lang.reflect.Constructor;
import java.lang.reflect.Constructor;

/**
 * Test instance creation.
 */
public class Main {

    public static void main(String[] args) {
        testClassNewInstance();
        testConstructorNewInstance();
    }

    /**
     * Tests Class.newInstance().
     */
    static void testClassNewInstance() {
        try {
            Class c = Class.forName("LocalClass");
            Object obj = c.newInstance();
            System.out.println("LocalClass succeeded");
        } catch (Exception ex) {
            System.err.println("LocalClass failed");
            ex.printStackTrace();
        }
        try {
            Class c = Class.forName("otherpackage.PackageAccess");
            Object obj = c.newInstance();
            System.err.println("ERROR: PackageAccess succeeded unexpectedly");
        } catch (IllegalAccessException iae) {
            System.out.println("Got expected PackageAccess complaint");
        } catch (Exception ex) {
            System.err.println("Got unexpected PackageAccess failure");
            ex.printStackTrace();
        }
        LocalClass3.main();
        try {
            MaybeAbstract ma = new MaybeAbstract();
            System.err.println("ERROR: MaybeAbstract succeeded unexpectedly");
        } catch (InstantiationError ie) {
            System.out.println("Got expected InstantationError");
        } catch (Exception ex) {
            System.err.println("Got unexpected MaybeAbstract failure");
        }
    }

    /**
     * Tests Constructor.newInstance().
     */
    static void testConstructorNewInstance() {
        try {
            Class c = Class.forName("LocalClass");
            Constructor cons = c.getConstructor(new Class[0]);
            System.err.println("Cons LocalClass succeeded unexpectedly");
        } catch (NoSuchMethodException nsme) {
            System.out.println("Cons LocalClass failed as expected");
        } catch (Exception ex) {
            System.err.println("Cons LocalClass failed strangely");
            ex.printStackTrace();
        }
        try {
            Class c = Class.forName("LocalClass2");
            Constructor cons = c.getConstructor((Class[]) null);
            Object obj = cons.newInstance();
            System.out.println("Cons LocalClass2 succeeded");
        } catch (Exception ex) {
            System.err.println("Cons LocalClass2 failed");
            ex.printStackTrace();
        }
        try {
            Class c = Class.forName("otherpackage.PackageAccess");
            Constructor cons = c.getConstructor(new Class[0]);
            System.err.println("ERROR: Cons PackageAccess succeeded unexpectedly");
        } catch (NoSuchMethodException nsme) {
            System.out.println("Cons got expected PackageAccess complaint");
        } catch (Exception ex) {
            System.err.println("Cons got unexpected PackageAccess failure");
            ex.printStackTrace();
        }
        try {
            Class c = Class.forName("MaybeAbstract");
            Constructor cons = c.getConstructor(new Class[0]);
            Object obj = cons.newInstance();
            System.err.println("ERROR: Cons MaybeAbstract succeeded unexpectedly");
        } catch (InstantiationException ie) {
            System.out.println("Cons got expected InstantationException");
        } catch (Exception ex) {
            System.err.println("Cons got unexpected MaybeAbstract failure");
            ex.printStackTrace();
        }
    }
}

class LocalClass {
}

class LocalClass2 {

    public LocalClass2() {
    }
}

class LocalClass3 {

    public static void main() {
        try {
            CC.newInstance();
            System.out.println("LocalClass3 succeeded");
        } catch (Exception ex) {
            System.err.println("Got unexpected LocalClass3 failure");
            ex.printStackTrace();
        }
    }

    static class CC {

        private CC() {
        }

        static Object newInstance() {
            try {
                Class c = CC.class;
                return c.newInstance();
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }
}
