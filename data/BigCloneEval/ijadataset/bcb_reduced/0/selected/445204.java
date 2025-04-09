package org.brainypdm.modules.commons.reflaction;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.brainypdm.modules.commons.log.BrainyLogger;

public class ClassReflect {

    private static BrainyLogger log = new BrainyLogger(ClassReflect.class);

    public static Constructor<?> getConstructor(String className, String[] paramsTypes) throws ClassNotFoundException, NoSuchMethodException {
        Constructor<?> out = null;
        Class<?>[] parametersTypes = getConstructorParametersTypes(paramsTypes);
        log.debug("Class name=" + className);
        out = Class.forName(className).getConstructor(parametersTypes);
        return out;
    }

    private static Class<?>[] getConstructorParametersTypes(String[] paramsTypes) throws ClassNotFoundException {
        Class<?>[] out = null;
        if (paramsTypes != null) {
            out = new Class[paramsTypes.length];
            for (int i = 0; i < paramsTypes.length; i++) {
                out[i] = Class.forName(paramsTypes[i].trim());
            }
        }
        return out;
    }

    public static Object[] newInstances(String[] types, String[] values) throws ClassNotFoundException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Object[] out = null;
        if ((types != null) && (values != null) && (types.length == values.length)) {
            out = new Object[types.length];
            for (int i = 0; i < types.length; i++) {
                out[i] = Class.forName(types[i].trim()).getConstructor(String.class).newInstance(values[i]);
            }
        }
        return out;
    }

    public static boolean checkInterface(Class<?> clazz, Class<?> aInterface) {
        boolean founded = false;
        log.debug("Checking " + clazz + " - " + aInterface);
        Class<?>[] interfaces = clazz.getInterfaces();
        for (int i = 0; (interfaces != null) && (i < interfaces.length); i++) {
            if (interfaces[i].equals(aInterface)) {
                founded = true;
            }
        }
        if (!founded) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                founded = checkInterface(superClass, aInterface);
            }
        } else {
            log.debug("Interface implemented by " + clazz);
        }
        return founded;
    }
}
