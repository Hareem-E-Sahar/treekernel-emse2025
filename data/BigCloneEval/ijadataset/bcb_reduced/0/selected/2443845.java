package com.acct001.properties;

import java.lang.reflect.*;
import java.util.*;

public class Properties {

    @SuppressWarnings("unchecked")
    public static <T> T createGetSetBean(final Object o, Class<T> iType) {
        InvocationHandler handler = new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String methodName = method.getName();
                if (methodName.startsWith("get") || methodName.startsWith("is")) {
                    String pName = methodName.substring(3);
                    if (pName.length() >= 1) {
                        Object pValue = o.getClass().getField(pName).get(o);
                        if (pValue instanceof IReadProperty<?>) {
                            return ((IReadProperty<?>) pValue).get();
                        }
                        return pValue;
                    }
                } else if (methodName.startsWith("set")) {
                    String pName = methodName.substring(3);
                    if (pName.length() >= 1) {
                        Field pField = o.getClass().getField(pName);
                        if (IWriteProperty.class.isAssignableFrom(pField.getType())) {
                            ((IWriteProperty<Object>) pField.get(o)).set(args[0]);
                        } else {
                            pField.set(o, args[0]);
                        }
                        return null;
                    }
                }
                return o.getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(o, args);
            }
        };
        Class<?> proxyClass = Proxy.getProxyClass(o.getClass().getClassLoader(), iType);
        try {
            final T instance = (T) proxyClass.getConstructor(new Class[] { InvocationHandler.class }).newInstance(new Object[] { handler });
            return instance;
        } catch (Throwable e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static <T extends IProperty> T[] getSame(T... properties) {
        return properties;
    }

    @SuppressWarnings("rawtypes")
    public static IReadProperty[] getReadables(IReadProperty... properties) {
        return properties;
    }

    @SuppressWarnings("rawtypes")
    public static IWriteProperty[] getWritables(IWriteProperty... properties) {
        return properties;
    }

    @SuppressWarnings("rawtypes")
    public static IReadWriteProperty[] getReadWrite(IReadWriteProperty... properties) {
        return properties;
    }

    @SuppressWarnings("rawtypes")
    public static IReadProperty[] listReadable(Object o) {
        return list(o, IReadProperty.class);
    }

    @SuppressWarnings("rawtypes")
    public static IReadWriteProperty[] listReadWrite(Object o) {
        return list(o, IReadWriteProperty.class);
    }

    @SuppressWarnings("rawtypes")
    public static IWriteProperty[] listWritable(Object o) {
        return list(o, IWriteProperty.class);
    }

    @SuppressWarnings("unchecked")
    private static <T extends IProperty> T[] list(Object o, Class<T> type) {
        Map<String, T> map = new LinkedHashMap<String, T>();
        for (Field f : o.getClass().getFields()) {
            if ((f.getModifiers() & Modifier.STATIC) == 0) {
                if (type.isAssignableFrom(f.getType())) {
                    try {
                        T property = (T) f.get(o);
                        if (false == (null == property)) {
                            if (false == (null == property.getName())) {
                                if (false == map.containsKey(property.getName())) {
                                    map.put(property.getName(), property);
                                }
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
        T[] array = (T[]) Array.newInstance(type, map.size());
        int i = 0;
        for (T p : map.values()) {
            array[i++] = p;
        }
        return array;
    }
}
