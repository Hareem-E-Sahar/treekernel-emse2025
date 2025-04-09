package com.utils;

import java.lang.reflect.*;
import org.apache.log4j.*;

/**
 * Utilities to work with reflection.
 *
 * @author Erich Roncarolo
 * @version 0.1 - 2004-09-07
 */
public class ClassUtil {

    private ClassUtil() {
    }

    /**
     * Returns a new instance of an object using constructor
     * without arguments.
     *
     * @param name class name
     * @return a new object instance or null if some error occours (exceptions are logged)
     */
    public static Object newInstance(String name) {
        return newInstance(name, null, null, false);
    }

    /**
     * Returns a new instance of an object using constructor
     * with arguments' running classes as parameter types.
     *
     * @param name class name
     * @param args constructor initialization arguments
     *
     * @return a new object instance or null if some error occours (exceptions are logged)
     */
    public static Object newInstance(String name, Object args[]) {
        return newInstance(name, null, args, false);
    }

    /**
     * Returns a new instance of an object using constructor with
     * specified parameter types and arguments.
     *
     * @param name class name
     * @param param constructor parameter types
     * @param args constructor initialization arguments
     *
     * @return a new object instance or null if some error occours (exceptions are logged)
     */
    public static Object newInstance(String name, Class param[], Object args[]) {
        return newInstance(name, param, args, false);
    }

    /**
     * Returns a new instance of an object using constructor with
     * specified parameter types and arguments.
     *
     * @param name class name
     * @param param constructor parameter types
     * @param args constructor initialization arguments
     * @param logException constructor initialization arguments
     *
     * @return a new object instance or null if some error occours (exceptions are logged)
     */
    public static Object newInstance(String name, Class param[], Object args[], boolean logException) {
        if (param == null && args != null) {
            param = new Class[args.length];
            for (int i = 0; i < param.length; i++) {
                if (args[i] != null) {
                    param[i] = args[i].getClass();
                } else {
                    param[i] = null;
                }
            }
        }
        Constructor c = null;
        Object obj = null;
        try {
            c = Class.forName(name).getConstructor(param);
            if (c != null) {
                obj = c.newInstance(args);
            }
        } catch (Exception x) {
            if (logException) {
                Logger log = Logger.getLogger(ClassUtil.class);
                log.error("", x);
            }
            return null;
        }
        return obj;
    }

    /**
     * Returns a new instance of an object using constructor
     * without arguments.
     *
     * @param clazz object class
     * @return a new object instance or null if some error occours (exceptions are logged)
     */
    public static Object newInstance(Class clazz) {
        return newInstance(clazz, null, null);
    }

    /**
     * Returns a new instance of an object using constructor
     * with arguments' running classes as parameter types.
     *
     * @param clazz object class
     * @param args constructor initialization arguments
     *
     * @return a new object instance or null if some error occours (exceptions are logged)
     */
    public static Object newInstance(Class clazz, Object args[]) {
        return newInstance(clazz, null, args);
    }

    /**
     * Returns a new instance of an object using constructor with
     * specified parameter types and arguments.
     *
     * @param clazz object class
     * @param param constructor parameter types
     * @param args constructor initialization arguments
     *
     * @return a new object instance or null if some error occours (exceptions are logged)
     */
    public static Object newInstance(Class clazz, Class param[], Object args[]) {
        if (clazz == null) {
            return null;
        }
        String name = clazz.getName();
        return newInstance(name, param, args);
    }

    /**
     * Invokes the object's method with specified parameter types and arguments.
     *
     * @param obj the object the specified method is invoked from (use a Class object if method is static)
     * @param name method name
     * @param param constructor parameter types
     * @param args constructor initialization arguments
     *
     * @return a new object instance or null if some error occours (exceptions are logged)
     */
    public static Object invokeMethod(Object obj, String name, Class param[], Object args[]) {
        if (param == null && args != null) {
            param = new Class[args.length];
            for (int i = 0; i < param.length; i++) {
                if (args[i] != null) {
                    param[i] = args[i].getClass();
                } else {
                    param[i] = null;
                }
            }
        }
        Method m = null;
        Object ret = null;
        try {
            if (obj instanceof Class) {
                m = ((Class) obj).getMethod(name, param);
            } else {
                m = obj.getClass().getMethod(name, param);
            }
            if (m != null) {
                ret = m.invoke(obj, args);
            }
        } catch (NoSuchMethodException x) {
            return null;
        } catch (Exception x) {
            Logger log = Logger.getLogger(ClassUtil.class);
            log.error("", x);
            return null;
        }
        return ret;
    }

    /**
     * Invokes the object's method with specified arguments.
     *
     * @param obj the object the specified method is invoked from (use a Class object if method is static)
     * @param name method name
     * @param args constructor initialization arguments
     *
     * @return a new object instance or null if some error occours (exceptions are logged)
     */
    public static Object invokeMethod(Object obj, String name, Object args[]) {
        return invokeMethod(obj, name, null, args);
    }

    /**
     * Invokes the object's method with no arguments.
     *
     * @param obj the object the specified method is invoked from (use a Class object if method is static)
     * @param name method name
     *
     * @return a new object instance or null if some error occours (exceptions are logged)
     */
    public static Object invokeMethod(Object obj, String name) {
        return invokeMethod(obj, name, null, null);
    }
}
