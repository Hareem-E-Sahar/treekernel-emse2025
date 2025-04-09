package edu.upmc.opi.caBIG.common;

import org.apache.log4j.Logger;

/**
 * The Class ObjectSizer.
 * 
 * Measures the approximate size of an object in memory, given a Class which
 * has a no-argument constructor.
 * 
 * @author mitchellkj@upmc.edu
 * @version $Id: ObjectSizer.java,v 1.2 2010/01/13 15:36:41 yiningzhao Exp $
 * @since 1.4.2_04
 */
public final class ObjectSizer {

    private static Logger logger = Logger.getLogger(ObjectSizer.class);

    private static int fSAMPLE_SIZE = 100;

    private static long fSLEEP_INTERVAL = 100;

    /**
     * Return the approximate size in bytes, and return zero if the class has no
     * default constructor.
     * 
     * @param aClass refers to a class which has a no-argument constructor.
     * 
     * @return the object size
     */
    public static long getObjectSize(Class aClass) {
        long result = 0;
        try {
            aClass.getConstructor(new Class[] {});
        } catch (NoSuchMethodException ex) {
            logger.fatal(aClass + " does not have a no-argument constructor.");
            return result;
        }
        Object[] objects = new Object[fSAMPLE_SIZE];
        try {
            Object throwAway = aClass.newInstance();
            long startMemoryUse = getMemoryUse();
            for (int idx = 0; idx < objects.length; ++idx) {
                objects[idx] = aClass.newInstance();
            }
            long endMemoryUse = getMemoryUse();
            float approximateSize = (endMemoryUse - startMemoryUse) / 100f;
            result = Math.round(approximateSize);
        } catch (Exception ex) {
            logger.fatal("Cannot create object using " + aClass);
        }
        return result;
    }

    public static long getMemoryUse() {
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
            Thread.currentThread().sleep(fSLEEP_INTERVAL);
            System.runFinalization();
            Thread.currentThread().sleep(fSLEEP_INTERVAL);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * First and only argument is the package-qualified name of a class which
     * has a no-argument constructor.
     * 
     * @param aArguments the a arguments
     */
    public static void main(String[] aArguments) {
        Class theClass = null;
        try {
            theClass = Class.forName(aArguments[0]);
        } catch (Exception ex) {
            logger.fatal("Cannot build a Class object: " + aArguments[0]);
            logger.fatal("Use a package-qualified name, and check classpath.");
        }
        long size = ObjectSizer.getObjectSize(theClass);
        System.out.println("Approximate size of " + theClass + " objects :" + size);
    }
}
