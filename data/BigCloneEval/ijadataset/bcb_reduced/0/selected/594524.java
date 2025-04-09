package com.tegsoft.tobe.util;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import org.apache.log4j.Level;
import com.tegsoft.tobe.util.message.MessageUtil;

public class MethodUtil implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * For debug, and error handling method details can be logged by this
	 * function.
	 * 
	 * @param level
	 * @param source
	 * @param name
	 * @param parameterTypes
	 */
    public static void logMethod(Level level, Class<? extends Object> source, String name, Class<?>... parameterTypes) {
        if (parameterTypes == null) {
            parameterTypes = new Class<?>[] {};
        }
        String logText = "";
        for (int i = 0; i < parameterTypes.length; i++) {
            try {
                logText += MessageUtil.getMessage(MethodUtil.class, Messages.findMethod_2, parameterTypes[i].getName());
            } catch (Exception ex) {
                UiUtil.handleException(ex);
            }
            if (i < parameterTypes.length - 1) {
                logText += ",";
            }
        }
        MessageUtil.logMessage(MethodUtil.class, level, Messages.findMethod_1, source.getName(), name, logText);
    }

    private static void convertParameterArray(Class<?> parameterTypes[], Object parameters[]) {
        if (parameters != null) {
            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameters[i] == null) {
                    parameterTypes[i] = Object.class;
                } else {
                    if ("STRING_NULL".equals(parameters[i])) {
                        parameterTypes[i] = String.class;
                        parameters[i] = null;
                    } else if ("INT_NULL".equals(parameters[i])) {
                        parameterTypes[i] = Integer.class;
                        parameters[i] = null;
                    } else if ("DATE_NULL".equals(parameters[i])) {
                        parameterTypes[i] = Timestamp.class;
                        parameters[i] = null;
                    } else {
                        parameterTypes[i] = parameters[i].getClass();
                    }
                }
            }
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> class1 = parameterTypes[i];
            if (class1.equals(Integer.class)) {
                parameterTypes[i] = int.class;
            } else if (class1.equals(Boolean.class)) {
                parameterTypes[i] = boolean.class;
            } else if (class1.equals(Double.class)) {
                parameterTypes[i] = double.class;
            }
        }
    }

    /**
	 * returns the first matching method of the given clazz with the name and
	 * parameter count.
	 * 
	 * @param clazz
	 * @param name
	 * @param paramCount
	 * @return
	 */
    public static Method getMethod(Class<?> clazz, String name, int paramCount) {
        Method[] methods = clazz.getMethods();
        for (int i = 0; i < methods.length; i++) {
            if ((methods[i].getName().equals(name) && (methods[i].getParameterTypes().length == paramCount))) {
                return methods[i];
            }
        }
        return null;
    }

    public static Object getMethodAndInvoke(Class<?> clazz, String name, Object... parameters) throws Exception {
        Class<?>[] parameterTypes = new Class[parameters.length];
        convertParameterArray(parameterTypes, parameters);
        Method method = getMethod(clazz, name, parameterTypes);
        if (method == null) {
            logMethod(Level.ERROR, clazz, name, parameterTypes);
            throw new NoSuchMethodError("check debug information above");
        }
        return method.invoke(null, parameters);
    }

    private static Object getMethodAndInvoke(boolean throwException, Object source, String name, Object... parameters) throws Exception {
        Class<?>[] parameterTypes = new Class[parameters.length];
        convertParameterArray(parameterTypes, parameters);
        Method method = getMethod(source.getClass(), name, parameterTypes);
        if (method == null) {
            String componentId = "";
            Method method2 = source.getClass().getMethod("getId");
            if (method2 != null) {
                componentId = (String) method2.invoke(source);
            }
            logMethod(Level.ERROR, source.getClass(), name, parameterTypes);
            if (throwException) {
                throw new NoSuchMethodError("check debug information above " + componentId);
            } else {
                return null;
            }
        }
        return method.invoke(source, parameters);
    }

    public static Object getMethodAndInvoke(Object source, String name, Object... parameters) throws Exception {
        return getMethodAndInvoke(true, source, name, parameters);
    }

    public static Object getMethodAndInvokeSafe(Object source, String name, Object... parameters) throws Exception {
        return getMethodAndInvoke(false, source, name, parameters);
    }

    public static Method getMethod(Object source, String name, Class<?>... parameterTypes) {
        return getMethod(source.getClass(), name, parameterTypes);
    }

    public static Object getConstructedObject(Class<?> clazz, Object... parameters) throws Exception {
        Class<?>[] parameterTypes = new Class[parameters.length];
        convertParameterArray(parameterTypes, parameters);
        Constructor<?> method = getConstructor(clazz, parameterTypes);
        if (method == null) {
            logMethod(Level.ERROR, clazz, clazz.getName(), parameterTypes);
            throw new NoSuchMethodError("check debug information above");
        }
        return method.newInstance(parameters);
    }

    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameterTypes) {
        try {
            convertParameterArray(parameterTypes, null);
            Constructor<?> method = clazz.getConstructor(parameterTypes);
            try {
                method.setAccessible(true);
            } catch (SecurityException se) {
            }
        } catch (NoSuchMethodException e) {
        }
        int paramSize = parameterTypes.length;
        Constructor<?>[] methods = clazz.getConstructors();
        for (int i = 0, size = methods.length; i < size; i++) {
            Class<?>[] methodsParams = methods[i].getParameterTypes();
            int methodParamSize = methodsParams.length;
            if (methodParamSize == paramSize) {
                boolean match = true;
                for (int n = 0; n < methodParamSize; n++) {
                    if (!isAssignmentCompatible(methodsParams[n], parameterTypes[n])) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    return methods[i];
                }
            }
        }
        return null;
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            convertParameterArray(parameterTypes, null);
            Method method = clazz.getMethod(methodName, parameterTypes);
            try {
                method.setAccessible(true);
            } catch (SecurityException se) {
            }
        } catch (NoSuchMethodException e) {
        }
        int paramSize = parameterTypes.length;
        Method[] methods = clazz.getMethods();
        for (int i = 0, size = methods.length; i < size; i++) {
            if (methods[i].getName().equals(methodName)) {
                Class<?>[] methodsParams = methods[i].getParameterTypes();
                int methodParamSize = methodsParams.length;
                if (methodParamSize == paramSize) {
                    boolean match = true;
                    for (int n = 0; n < methodParamSize; n++) {
                        if (!isAssignmentCompatible(methodsParams[n], parameterTypes[n])) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        return methods[i];
                    }
                }
            }
        }
        return null;
    }

    private static final boolean isAssignmentCompatible(Class<?> parameterType, Class<?> parameterization) {
        if (parameterType.isAssignableFrom(parameterization)) {
            return true;
        }
        if (parameterType.isPrimitive()) {
            Class<?> parameterWrapperClazz = getPrimitiveWrapper(parameterType);
            if (parameterWrapperClazz != null) {
                return parameterWrapperClazz.equals(parameterization);
            }
        }
        return false;
    }

    private static Class<?> getPrimitiveWrapper(Class<?> primitiveType) {
        if (boolean.class.equals(primitiveType)) {
            return Boolean.class;
        } else if (float.class.equals(primitiveType)) {
            return Float.class;
        } else if (long.class.equals(primitiveType)) {
            return Long.class;
        } else if (int.class.equals(primitiveType)) {
            return Integer.class;
        } else if (short.class.equals(primitiveType)) {
            return Short.class;
        } else if (byte.class.equals(primitiveType)) {
            return Byte.class;
        } else if (double.class.equals(primitiveType)) {
            return Double.class;
        } else if (char.class.equals(primitiveType)) {
            return Character.class;
        } else {
            return null;
        }
    }

    public enum Messages {

        /**
		 * Unable to locate {1} method for {0} class with parameters {2}
		 */
        findMethod_1, /**
		 * {0}
		 */
        findMethod_2
    }
}
