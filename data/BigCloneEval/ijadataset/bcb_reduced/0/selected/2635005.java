package javadz.beanutils;

import javadz.beans.IntrospectionException;
import javadz.beans.PropertyDescriptor;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * A MappedPropertyDescriptor describes one mapped property.
 * Mapped properties are multivalued properties like indexed properties
 * but that are accessed with a String key instead of an index.
 * Such property values are typically stored in a Map collection.
 * For this class to work properly, a mapped value must have
 * getter and setter methods of the form
 * <p><code>get<strong>Property</strong>(String key)<code> and
 * <p><code>set<strong>Property</strong>(String key, Object value)<code>,
 * <p>where <code><strong>Property</strong></code> must be replaced
 * by the name of the property.
 * @see java.beans.PropertyDescriptor
 *
 * @author Rey Francois
 * @author Gregor Rayman
 * @version $Revision: 806915 $ $Date: 2009-08-23 01:50:23 +0100 (Sun, 23 Aug 2009) $
 */
public class MappedPropertyDescriptor extends PropertyDescriptor {

    /**
     * The underlying data type of the property we are describing.
     */
    private Reference mappedPropertyTypeRef;

    /**
     * The reader method for this property (if any).
     */
    private MappedMethodReference mappedReadMethodRef;

    /**
     * The writer method for this property (if any).
     */
    private MappedMethodReference mappedWriteMethodRef;

    /**
     * The parameter types array for the reader method signature.
     */
    private static final Class[] STRING_CLASS_PARAMETER = new Class[] { String.class };

    /**
     * Constructs a MappedPropertyDescriptor for a property that follows
     * the standard Java convention by having getFoo and setFoo
     * accessor methods, with the addition of a String parameter (the key).
     * Thus if the argument name is "fred", it will
     * assume that the writer method is "setFred" and the reader method
     * is "getFred".  Note that the property name should start with a lower
     * case character, which will be capitalized in the method names.
     *
     * @param propertyName The programmatic name of the property.
     * @param beanClass The Class object for the target bean.  For
     *        example sun.beans.OurButton.class.
     *
     * @exception IntrospectionException if an exception occurs during
     *              introspection.
     */
    public MappedPropertyDescriptor(String propertyName, Class beanClass) throws IntrospectionException {
        super(propertyName, null, null);
        if (propertyName == null || propertyName.length() == 0) {
            throw new IntrospectionException("bad property name: " + propertyName + " on class: " + beanClass.getClass().getName());
        }
        setName(propertyName);
        String base = capitalizePropertyName(propertyName);
        Method mappedReadMethod = null;
        Method mappedWriteMethod = null;
        try {
            try {
                mappedReadMethod = getMethod(beanClass, "get" + base, STRING_CLASS_PARAMETER);
            } catch (IntrospectionException e) {
                mappedReadMethod = getMethod(beanClass, "is" + base, STRING_CLASS_PARAMETER);
            }
            Class[] params = { String.class, mappedReadMethod.getReturnType() };
            mappedWriteMethod = getMethod(beanClass, "set" + base, params);
        } catch (IntrospectionException e) {
        }
        if (mappedReadMethod == null) {
            mappedWriteMethod = getMethod(beanClass, "set" + base, 2);
        }
        if ((mappedReadMethod == null) && (mappedWriteMethod == null)) {
            throw new IntrospectionException("Property '" + propertyName + "' not found on " + beanClass.getName());
        }
        mappedReadMethodRef = new MappedMethodReference(mappedReadMethod);
        mappedWriteMethodRef = new MappedMethodReference(mappedWriteMethod);
        findMappedPropertyType();
    }

    /**
     * This constructor takes the name of a mapped property, and method
     * names for reading and writing the property.
     *
     * @param propertyName The programmatic name of the property.
     * @param beanClass The Class object for the target bean.  For
     *        example sun.beans.OurButton.class.
     * @param mappedGetterName The name of the method used for
     *          reading one of the property values.  May be null if the
     *          property is write-only.
     * @param mappedSetterName The name of the method used for writing
     *          one of the property values.  May be null if the property is
     *          read-only.
     *
     * @exception IntrospectionException if an exception occurs during
     *              introspection.
     */
    public MappedPropertyDescriptor(String propertyName, Class beanClass, String mappedGetterName, String mappedSetterName) throws IntrospectionException {
        super(propertyName, null, null);
        if (propertyName == null || propertyName.length() == 0) {
            throw new IntrospectionException("bad property name: " + propertyName);
        }
        setName(propertyName);
        Method mappedReadMethod = null;
        Method mappedWriteMethod = null;
        mappedReadMethod = getMethod(beanClass, mappedGetterName, STRING_CLASS_PARAMETER);
        if (mappedReadMethod != null) {
            Class[] params = { String.class, mappedReadMethod.getReturnType() };
            mappedWriteMethod = getMethod(beanClass, mappedSetterName, params);
        } else {
            mappedWriteMethod = getMethod(beanClass, mappedSetterName, 2);
        }
        mappedReadMethodRef = new MappedMethodReference(mappedReadMethod);
        mappedWriteMethodRef = new MappedMethodReference(mappedWriteMethod);
        findMappedPropertyType();
    }

    /**
     * This constructor takes the name of a mapped property, and Method
     * objects for reading and writing the property.
     *
     * @param propertyName The programmatic name of the property.
     * @param mappedGetter The method used for reading one of
     *          the property values.  May be be null if the property
     *          is write-only.
     * @param mappedSetter The method used for writing one the
     *          property values.  May be null if the property is read-only.
     *
     * @exception IntrospectionException if an exception occurs during
     *              introspection.
     */
    public MappedPropertyDescriptor(String propertyName, Method mappedGetter, Method mappedSetter) throws IntrospectionException {
        super(propertyName, mappedGetter, mappedSetter);
        if (propertyName == null || propertyName.length() == 0) {
            throw new IntrospectionException("bad property name: " + propertyName);
        }
        setName(propertyName);
        mappedReadMethodRef = new MappedMethodReference(mappedGetter);
        mappedWriteMethodRef = new MappedMethodReference(mappedSetter);
        findMappedPropertyType();
    }

    /**
     * Gets the Class object for the property values.
     *
     * @return The Java type info for the property values.  Note that
     * the "Class" object may describe a built-in Java type such as "int".
     * The result may be "null" if this is a mapped property that
     * does not support non-keyed access.
     * <p>
     * This is the type that will be returned by the mappedReadMethod.
     */
    public Class getMappedPropertyType() {
        return (Class) mappedPropertyTypeRef.get();
    }

    /**
     * Gets the method that should be used to read one of the property value.
     *
     * @return The method that should be used to read the property value.
     * May return null if the property can't be read.
     */
    public Method getMappedReadMethod() {
        return mappedReadMethodRef.get();
    }

    /**
     * Sets the method that should be used to read one of the property value.
     *
     * @param mappedGetter The mapped getter method.
     * @throws IntrospectionException If an error occurs finding the
     * mapped property
     */
    public void setMappedReadMethod(Method mappedGetter) throws IntrospectionException {
        mappedReadMethodRef = new MappedMethodReference(mappedGetter);
        findMappedPropertyType();
    }

    /**
     * Gets the method that should be used to write one of the property value.
     *
     * @return The method that should be used to write one of the property value.
     * May return null if the property can't be written.
     */
    public Method getMappedWriteMethod() {
        return mappedWriteMethodRef.get();
    }

    /**
     * Sets the method that should be used to write the property value.
     *
     * @param mappedSetter The mapped setter method.
     * @throws IntrospectionException If an error occurs finding the
     * mapped property
     */
    public void setMappedWriteMethod(Method mappedSetter) throws IntrospectionException {
        mappedWriteMethodRef = new MappedMethodReference(mappedSetter);
        findMappedPropertyType();
    }

    /**
     * Introspect our bean class to identify the corresponding getter
     * and setter methods.
     */
    private void findMappedPropertyType() throws IntrospectionException {
        try {
            Method mappedReadMethod = getMappedReadMethod();
            Method mappedWriteMethod = getMappedWriteMethod();
            Class mappedPropertyType = null;
            if (mappedReadMethod != null) {
                if (mappedReadMethod.getParameterTypes().length != 1) {
                    throw new IntrospectionException("bad mapped read method arg count");
                }
                mappedPropertyType = mappedReadMethod.getReturnType();
                if (mappedPropertyType == Void.TYPE) {
                    throw new IntrospectionException("mapped read method " + mappedReadMethod.getName() + " returns void");
                }
            }
            if (mappedWriteMethod != null) {
                Class[] params = mappedWriteMethod.getParameterTypes();
                if (params.length != 2) {
                    throw new IntrospectionException("bad mapped write method arg count");
                }
                if (mappedPropertyType != null && mappedPropertyType != params[1]) {
                    throw new IntrospectionException("type mismatch between mapped read and write methods");
                }
                mappedPropertyType = params[1];
            }
            mappedPropertyTypeRef = new SoftReference(mappedPropertyType);
        } catch (IntrospectionException ex) {
            throw ex;
        }
    }

    /**
     * Return a capitalized version of the specified property name.
     *
     * @param s The property name
     */
    private static String capitalizePropertyName(String s) {
        if (s.length() == 0) {
            return s;
        }
        char[] chars = s.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    /**
     * Find a method on a class with a specified number of parameters.
     */
    private static Method internalGetMethod(Class initial, String methodName, int parameterCount) {
        for (Class clazz = initial; clazz != null; clazz = clazz.getSuperclass()) {
            Method[] methods = clazz.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method == null) {
                    continue;
                }
                int mods = method.getModifiers();
                if (!Modifier.isPublic(mods) || Modifier.isStatic(mods)) {
                    continue;
                }
                if (method.getName().equals(methodName) && method.getParameterTypes().length == parameterCount) {
                    return method;
                }
            }
        }
        Class[] interfaces = initial.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            Method method = internalGetMethod(interfaces[i], methodName, parameterCount);
            if (method != null) {
                return method;
            }
        }
        return null;
    }

    /**
     * Find a method on a class with a specified number of parameters.
     */
    private static Method getMethod(Class clazz, String methodName, int parameterCount) throws IntrospectionException {
        if (methodName == null) {
            return null;
        }
        Method method = internalGetMethod(clazz, methodName, parameterCount);
        if (method != null) {
            return method;
        }
        throw new IntrospectionException("No method \"" + methodName + "\" with " + parameterCount + " parameter(s)");
    }

    /**
     * Find a method on a class with a specified parameter list.
     */
    private static Method getMethod(Class clazz, String methodName, Class[] parameterTypes) throws IntrospectionException {
        if (methodName == null) {
            return null;
        }
        Method method = MethodUtils.getMatchingAccessibleMethod(clazz, methodName, parameterTypes);
        if (method != null) {
            return method;
        }
        int parameterCount = (parameterTypes == null) ? 0 : parameterTypes.length;
        throw new IntrospectionException("No method \"" + methodName + "\" with " + parameterCount + " parameter(s) of matching types.");
    }

    /**
     * Holds a {@link Method} in a {@link SoftReference} so that it
     * it doesn't prevent any ClassLoader being garbage collected, but
     * tries to re-create the method if the method reference has been
     * released.
     *
     * See http://issues.apache.org/jira/browse/BEANUTILS-291
     */
    private static class MappedMethodReference {

        private String className;

        private String methodName;

        private Reference methodRef;

        private Reference classRef;

        private Reference writeParamTypeRef0;

        private Reference writeParamTypeRef1;

        private String[] writeParamClassNames;

        MappedMethodReference(Method m) {
            if (m != null) {
                className = m.getDeclaringClass().getName();
                methodName = m.getName();
                methodRef = new SoftReference(m);
                classRef = new WeakReference(m.getDeclaringClass());
                Class[] types = m.getParameterTypes();
                if (types.length == 2) {
                    writeParamTypeRef0 = new WeakReference(types[0]);
                    writeParamTypeRef1 = new WeakReference(types[1]);
                    writeParamClassNames = new String[2];
                    writeParamClassNames[0] = types[0].getName();
                    writeParamClassNames[1] = types[1].getName();
                }
            }
        }

        private Method get() {
            if (methodRef == null) {
                return null;
            }
            Method m = (Method) methodRef.get();
            if (m == null) {
                Class clazz = (Class) classRef.get();
                if (clazz == null) {
                    clazz = reLoadClass();
                    if (clazz != null) {
                        classRef = new WeakReference(clazz);
                    }
                }
                if (clazz == null) {
                    throw new RuntimeException("Method " + methodName + " for " + className + " could not be reconstructed - class reference has gone");
                }
                Class[] paramTypes = null;
                if (writeParamClassNames != null) {
                    paramTypes = new Class[2];
                    paramTypes[0] = (Class) writeParamTypeRef0.get();
                    if (paramTypes[0] == null) {
                        paramTypes[0] = reLoadClass(writeParamClassNames[0]);
                        if (paramTypes[0] != null) {
                            writeParamTypeRef0 = new WeakReference(paramTypes[0]);
                        }
                    }
                    paramTypes[1] = (Class) writeParamTypeRef1.get();
                    if (paramTypes[1] == null) {
                        paramTypes[1] = reLoadClass(writeParamClassNames[1]);
                        if (paramTypes[1] != null) {
                            writeParamTypeRef1 = new WeakReference(paramTypes[1]);
                        }
                    }
                } else {
                    paramTypes = STRING_CLASS_PARAMETER;
                }
                try {
                    m = clazz.getMethod(methodName, paramTypes);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException("Method " + methodName + " for " + className + " could not be reconstructed - method not found");
                }
                methodRef = new SoftReference(m);
            }
            return m;
        }

        /**
         * Try to re-load the class
         */
        private Class reLoadClass() {
            return reLoadClass(className);
        }

        /**
         * Try to re-load the class
         */
        private Class reLoadClass(String name) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader != null) {
                try {
                    return classLoader.loadClass(name);
                } catch (ClassNotFoundException e) {
                }
            }
            classLoader = MappedPropertyDescriptor.class.getClassLoader();
            try {
                return classLoader.loadClass(name);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
    }
}
