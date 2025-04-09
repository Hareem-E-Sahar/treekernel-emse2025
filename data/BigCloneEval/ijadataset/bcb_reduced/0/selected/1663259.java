package pl.edu.amu.xtr.util;

import pl.edu.amu.xtr.xml.Variables;

/**
 * Measures the approximate size of an object in memory, given a Class which has a no-argument constructor. Downloaded from:
 * http://www.javapractices.com/topic/TopicAction.do?Id=83
 */
public final class ObjectSizer {

    /**
	 * First and only argument is the package-qualified name of a class which has a no-argument constructor.
	 */
    public static void main(String... aArguments) {
        Class theClass = Variables.class;
        long size = ObjectSizer.getObjectSize(theClass);
        System.out.println("Approximate size of " + theClass + " objects :" + size);
    }

    /**
	 * Return the approximate size in bytes, and return zero if the class has no default constructor.
	 * 
	 * @param aClass
	 *            refers to a class which has a no-argument constructor.
	 */
    public static long getObjectSize(Class aClass) {
        long result = 0;
        try {
            aClass.getConstructor(new Class[] {});
        } catch (NoSuchMethodException ex) {
            System.err.println(aClass + " does not have a no-argument constructor.");
            return result;
        }
        Object[] objects = new Object[fSAMPLE_SIZE];
        try {
            @SuppressWarnings("unused") Object throwAway = aClass.newInstance();
            long startMemoryUse = getMemoryUse();
            for (int idx = 0; idx < objects.length; ++idx) {
                objects[idx] = aClass.newInstance();
            }
            long endMemoryUse = getMemoryUse();
            float approximateSize = (endMemoryUse - startMemoryUse) / 100f;
            result = Math.round(approximateSize);
        } catch (Exception ex) {
            System.err.println("Cannot create object using " + aClass);
        }
        return result;
    }

    private static int fSAMPLE_SIZE = 100;

    private static long fSLEEP_INTERVAL = 100;

    private static long getMemoryUse() {
        putOutTheGarbage();
        long totalMemory = Runtime.getRuntime().totalMemory();
        putOutTheGarbage();
        long freeMemory = Runtime.getRuntime().freeMemory();
        return (totalMemory - freeMemory);
    }

    private static void putOutTheGarbage() {
        collectGarbage();
        collectGarbage();
    }

    private static void collectGarbage() {
        try {
            System.gc();
            Thread.sleep(fSLEEP_INTERVAL);
            System.runFinalization();
            Thread.sleep(fSLEEP_INTERVAL);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
