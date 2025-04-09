package jvc.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class ReflectUtils {

    private ReflectUtils() {
    }

    public static Class forName(String name) {
        try {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            if (contextClassLoader != null) {
                return contextClassLoader.loadClass(name);
            } else {
                return Class.forName(name);
            }
        } catch (Exception e) {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException cnfEx) {
                return null;
            }
        }
    }

    public static Object newInstance(Class clazz, Class[] types, Object[] initargs) {
        try {
            Constructor cr = clazz.getConstructor(types);
            return cr.newInstance(initargs);
        } catch (Exception e) {
            return null;
        }
    }

    public static Object newInstance(Class clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    public static Object invoke(Object obj, String methodName, Class[] types, Object[] args) {
        try {
            Method method = obj.getClass().getDeclaredMethod(methodName, types);
            return method.invoke(obj, args);
        } catch (Exception e) {
            return null;
        }
    }

    public static Object invoke(Object obj, String methodName) {
        try {
            Method method = obj.getClass().getDeclaredMethod(methodName, null);
            return method.invoke(obj, null);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isPublic(Class clazz, Member member) {
        return Modifier.isPublic(member.getModifiers()) && Modifier.isPublic(clazz.getModifiers());
    }

    public static boolean isAbstract(Class clazz) {
        int modifier = clazz.getModifiers();
        return Modifier.isAbstract(modifier);
    }

    public static boolean isInterface(Class clazz) {
        int modifier = clazz.getModifiers();
        return Modifier.isInterface(modifier);
    }
}
