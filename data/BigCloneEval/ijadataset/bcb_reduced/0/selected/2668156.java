package com.crowdsourcing.framework.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.list.SetUniqueList;
import EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap;
import com.crowdsourcing.framework.entity.EntityException;

/**
 * 
 * <CODE>ReflectionUtils</CODE> provides access to objects by reflection.
 * think of used net.sf.cglib.core.ReflectUtils
 * @author chikai
 * @link <a href="mailto:chikaiwang@hotmail.com">chikai</a>
 * @version CVS $Revision:  $ $Date:  $
 */
public final class ReflectionUtils {

    private static final Map CLASS_META_DATA = new ConcurrentReaderHashMap();

    private ReflectionUtils() {
    }

    /**
	 * Handle the given reflection exception. Should only be called if
	 * no checked exception is expected to be thrown by the target method.
	 * <p>Throws the underlying RuntimeException or Error in case of an
	 * InvocationTargetException with such a root cause. Throws an
	 * IllegalStateException with an appropriate message else.
	 * @param ex the reflection exception to handle
	 */
    public static void handleReflectionException(Exception ex) {
        if (ex instanceof NoSuchMethodException) {
            throw new IllegalStateException("Method not found: " + ex.getMessage());
        }
        if (ex instanceof IllegalAccessException) {
            throw new IllegalStateException("Could not access method: " + ex.getMessage());
        }
        if (ex instanceof InvocationTargetException) {
            handleInvocationTargetException((InvocationTargetException) ex);
        }
        throw new IllegalStateException("Unexpected reflection exception - " + ex.getClass().getName() + ": " + ex.getMessage());
    }

    /**
	 * Handle the given invocation target exception. Should only be called if
	 * no checked exception is expected to be thrown by the target method.
	 * <p>Throws the underlying RuntimeException or Error in case of such
	 * a root cause. Throws an IllegalStateException else.
	 * @param ex the invocation target exception to handle
	 */
    public static void handleInvocationTargetException(InvocationTargetException ex) {
        if (ex.getTargetException() instanceof RuntimeException) {
            throw (RuntimeException) ex.getTargetException();
        }
        if (ex.getTargetException() instanceof Error) {
            throw (Error) ex.getTargetException();
        }
        throw new IllegalStateException("Unexpected exception thrown by method - " + ex.getTargetException().getClass().getName() + ": " + ex.getTargetException().getMessage());
    }

    /**
     * Comparator that compares java field names.
     */
    private static final Comparator FIELD_COMPARATOR = new Comparator() {

        /**
         * {@inheritDoc}
         */
        public int compare(Object pObject1, Object pObject2) {
            if (!(pObject1 instanceof Field) || !(pObject2 instanceof Field)) {
                throw new IllegalStateException("This comparator only compares valid field objects.");
            }
            Field field1 = (Field) pObject1;
            Field field2 = (Field) pObject2;
            return field1.getName().compareTo(field2.getName());
        }
    };

    /**
     * (Comment for getMethod)
     * 
     * @param pClass
     * @param pMethodName
     * @return @since 1.0 S2
     */
    public static Method getMethod(Class pClass, String pMethodName) {
        return getMethod(pClass, pMethodName, (Class[]) null);
    }

    /**
     * Return the method corresponding to the method name with those parameter types.
     * 
     * @param pClass - class to search for the method.
     * @param pMethodName - method name.
     * @param pParameterTypes - parameter types.
     * @return Method - the corresponding Method if it is found and <CODE>null</CODE> if it is not
     * found.
     * @since RS4.0
     */
    public static Method getMethod(Class pClass, String pMethodName, Class[] pParameterTypes) {
        Method method;
        try {
            method = pClass.getDeclaredMethod(pMethodName, pParameterTypes);
        } catch (NoSuchMethodException e) {
            Class superclass = pClass.getSuperclass();
            if (superclass != null) {
                method = getMethod(superclass, pMethodName, pParameterTypes);
            } else {
                method = null;
            }
        }
        return method;
    }

    /**
     * (Comment for getMethods)
     * 
     * @param pClassType
     * @param pMethodName
     * @return @since 1.0 S2
     */
    @SuppressWarnings("unchecked")
    public static Method[] getMethods(Class pClassType, String pMethodName) {
        Method[] allMethods = pClassType.getDeclaredMethods();
        List methodList = new ArrayList();
        for (int i = 0; i < allMethods.length; i++) {
            Method method = allMethods[i];
            if (method.getName().equals(pMethodName)) {
                methodList.add(method);
            }
        }
        Method[] methods = new Method[methodList.size()];
        methodList.toArray(methods);
        return methods;
    }

    /**
     * description_here.
     * 
     * @param pClassType
     * @return the empty constructor for this class or null if none found
     */
    public static Constructor getConstructor(Class pClassType) {
        return getConstructor(pClassType, null);
    }

    /**
     * (Comment for getConstructor)
     * 
     * @param pClass
     * @param pParameterTypes
     * @return @since 1.0 S1
     */
    public static Constructor getConstructor(Class pClass, Class[] pParameterTypes) {
        Constructor constructor;
        try {
            constructor = pClass.getDeclaredConstructor(pParameterTypes);
        } catch (NoSuchMethodException e) {
            constructor = null;
        }
        return constructor;
    }

    /**
     * (Comment for getConstructor)
     * 
     * @param pClass
     * @param pParameterValues
     * @return @since 1.0 S1
     */
    public static Constructor getConstructor(Class pClass, Object[] pParameterValues) {
        Constructor result = null;
        Constructor[] constructors = pClass.getDeclaredConstructors();
        for (int i = 0; i < constructors.length; i++) {
            Constructor constructor = constructors[i];
            Class[] parameterTypes = constructor.getParameterTypes();
            boolean match = parameterTypes.length == pParameterValues.length;
            if (match) {
                for (int j = 0; j < parameterTypes.length; j++) {
                    if (!parameterTypes[j].isInstance(pParameterValues[j])) {
                        match = false;
                        break;
                    }
                }
            }
            if (match) {
                result = constructor;
                break;
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Field[] getDeclaredFields(Class pClasz) {
        Set fields = new HashSet();
        Class clazz = pClasz;
        do {
            CollectionUtils.mergeArrayIntoCollection(clazz.getDeclaredFields(), fields);
            clazz = clazz.getSuperclass();
        } while (!clazz.equals(Object.class));
        Field[] fieldArray = new Field[fields.size()];
        return (Field[]) fields.toArray(fieldArray);
    }

    /**
     * (Comment for getField)
     * 
     * @param pObject
     * @param pFieldName
     * @return @since 1.0 S3
     */
    public static Field getField(Object pObject, String pFieldName) {
        if (pObject == null) {
            throw new NullPointerException();
        }
        return getField(pObject.getClass(), pFieldName);
    }

    @SuppressWarnings("unchecked")
    public static List getFieldWithAnnotation(Object pObj, Class<? extends Annotation> pAnnon) {
        List result = new ArrayList();
        Field[] fields = pObj.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (field.isAnnotationPresent(pAnnon)) {
                result.add(field.getName());
            }
        }
        return result;
    }

    /**
	 * (Comment for getField)
	 * 
	 * @param pObject
	 * @param pFieldName
	 * @return
	 * @since 1.0 S3
	 */
    public static Field getField(Class pClass, String pFieldName) {
        if (pClass == null) {
            throw new NullPointerException();
        }
        Field field;
        try {
            field = pClass.getDeclaredField(pFieldName);
        } catch (SecurityException se) {
            throw new EntityException(se);
        } catch (NoSuchFieldException e) {
            try {
                field = pClass.getSuperclass().getDeclaredField(pFieldName);
            } catch (SecurityException se1) {
                throw new EntityException(se1);
            } catch (NoSuchFieldException e1) {
                throw new EntityException(e1);
            }
        }
        return field;
    }

    /**
     * (Comment for getFieldValue)
     * 
     * @param pClass
     * @param pFieldName
     * @return @since 1.0 S2
     */
    public static Object getFieldValue(Class pClass, String pFieldName) {
        return getFieldValue(pClass, null, pFieldName);
    }

    /**
     * (Comment for getFieldValue)
     * 
     * @param pObject
     * @param pFieldName
     * @return @since 1.0 S3
     */
    public static Object getFieldValue(Object pObject, String pFieldName) {
        if (pObject == null) {
            throw new IllegalArgumentException("pObject should not be null, " + "use different method");
        }
        return getFieldValue(pObject.getClass(), pObject, pFieldName);
    }

    /**
     * Return the corresponding field value if it is found.
     * 
     * @param pClass - class to search for the method.
     * @param pObject - object containing the field value.
     * @param pFieldName - field name.
     * @return Object - the corresponding field value if it is found
     * @since RS4.0
     */
    private static Object getFieldValue(Class pClass, Object pObject, String pFieldName) {
        Field field = getField(pClass, pFieldName);
        if (field == null) {
            throw new IllegalArgumentException("unknown field: " + pFieldName + ", class: " + pClass.getName());
        }
        return getFieldValue(pObject, field);
    }

    /**
     * Return the value of a static field.
     *
     * @param pField The field.
     * @return The value.
     * @since 1.1
     */
    public static Object getFieldValue(Field pField) {
        return getFieldValue(null, pField);
    }

    /**
     * Return the field value.
     * 
     * @param pObject - object containing the field value.
     * @param pField - field.
     * @return Object - the corresponding field value if it is found and <CODE>null</CODE> if it
     * is not found.
     * @since RS4.0
     */
    public static Object getFieldValue(Object pObject, Field pField) {
        Object value;
        boolean accessible = pField.isAccessible();
        try {
            if (!accessible) {
                pField.setAccessible(true);
            }
            value = pField.get(pObject);
        } catch (IllegalAccessException e) {
            throw ExceptionUtils.createIllegalStateException(e);
        } finally {
            if (!accessible) {
                pField.setAccessible(false);
            }
        }
        return value;
    }

    /**
     * Sets the value of the given field and instance.
     * 
     * @param pObject the instance to alter.
     * @param pFieldName the name of the field to alter.
     * @param pValue the value to set.
     * @since 1.0
     */
    public static void setFieldValue(Object pObject, String pFieldName, Object pValue) {
        Field field = getField(pObject.getClass(), pFieldName);
        setFieldValue(pObject, field, pValue);
    }

    /**
     * (Comment for setFieldValue)
     * 
     * @param pObject
     * @param pField
     * @param pValue
     * @since 1.0 S3
     */
    public static void setFieldValue(Object pObject, Field pField, Object pValue) {
        boolean accessible = pField.isAccessible();
        try {
            if (!accessible) {
                pField.setAccessible(true);
            }
            pField.set(pObject, pValue);
        } catch (IllegalAccessException e) {
            throw ExceptionUtils.createIllegalStateException(e);
        } finally {
            if (!accessible) {
                pField.setAccessible(false);
            }
        }
    }

    private static ClassLoader getClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = ReflectionUtils.class.getClassLoader();
        }
        return classLoader;
    }

    /**
     * Find a class from his full name.
     *
     * @param pClassName The name of the class.
     * @return The class.  Null if not found.
     * @since 1.1
     */
    public static Class findClass(String pClassName) {
        Class klass;
        try {
            klass = Class.forName(pClassName, false, getClassLoader());
        } catch (ClassNotFoundException e) {
            klass = null;
        }
        return klass;
    }

    /**
     * get a class from his full name.
     *
     * @param pClassName The name of the class.
     * @return The class.
     * @throws IllegalArgumentException if not found.
     * @since 1.1
     */
    public static Class getClass(String pClassName) {
        Class klass = findClass(pClassName);
        if (klass == null) {
            throw new IllegalArgumentException("unknown class: " + pClassName);
        }
        return klass;
    }

    /**
     * Returns all interfaces implemented/extended by the given class.
     *
     * @param pClass the class for which to get all interfaces.
     * @param pIncludeItself the flag indicating if the given class must also be returned.
     * @return all interfaces implemented/extended by the given class.
     * @since 1.2
     */
    public static Class[] getAllInterfaces(Class pClass, boolean pIncludeItself) {
        List result = new ArrayList();
        if (pIncludeItself) {
            if (!pClass.isInterface()) {
                throw new IllegalArgumentException("Cannot add " + pClass + ", it is not an interface.");
            }
            result.add(pClass);
        }
        getAllInterfaces(pClass, result);
        return (Class[]) result.toArray(new Class[result.size()]);
    }

    /**
     * Adds all interfaces implemented/extended by the given class into the given list.
     *
     * @param pClass the class for which to get all interfaces.
     * @param pAllInterfaces the list into which to add interfaces.
     * @since 1.2
     */
    private static void getAllInterfaces(Class pClass, List pAllInterfaces) {
        List allInterfaces = SetUniqueList.class.isInstance(pAllInterfaces) ? pAllInterfaces : SetUniqueList.decorate(pAllInterfaces);
        Class[] superInterfaces = pClass.getInterfaces();
        for (int i = 0; i < superInterfaces.length; i++) {
            allInterfaces.add(superInterfaces[i]);
        }
        for (int i = 0; i < superInterfaces.length; i++) {
            getAllInterfaces(superInterfaces[i], allInterfaces);
        }
    }

    /**
     * Finds for the given interface name for the given interfaces.
     *
     * @param pInterfaces the interfaces to inspect.
     * @param pInterfaceName the name of the interface to look for.
     * @return the interface class or <code>null</code> if none is found.
     * @since 1.3
     */
    public static Class findInterface(Class[] pInterfaces, String pInterfaceName) {
        if (pInterfaces == null || pInterfaceName == null) {
            throw new IllegalArgumentException("Intefaces and interface name must be specified (not null).");
        }
        Class result = null;
        for (int i = 0; i < pInterfaces.length && result == null; i++) {
            if (pInterfaces[i].getName().equals(pInterfaceName)) {
                result = pInterfaces[i];
            }
        }
        for (int i = 0; i < pInterfaces.length && result == null; i++) {
            Class[] interfaces = pInterfaces[i].getInterfaces();
            if (interfaces.length > 0) {
                result = findInterface(interfaces, pInterfaceName);
            }
        }
        return result;
    }

    /**
     * (Comment for invoke)
     * 
     * @param pMethod
     * @return @since 1.0 S2
     */
    public static Object invoke(Method pMethod) {
        return invoke(pMethod, null, null);
    }

    /**
     * (Comment for invoke)
     * 
     * @param pMethod
     * @param pInvokingObject
     * @return @since 1.0 S2
     */
    public static Object invoke(Method pMethod, Object pInvokingObject) {
        return invoke(pMethod, null, pInvokingObject);
    }

    /**
     * Invoke the method with the specified parameter values and returns 
     * the method's returned value.
     * 
     * @param pMethod - method to invoke.
     * @param pParameterValues - parameter values for the method.
     * @return Object - method's returned value.
     * @since 1.0 S2
     */
    public static Object invoke(Method pMethod, Object[] pParameterValues) {
        return invoke(pMethod, pParameterValues, null);
    }

    /**
     * Invokes the underlying method represented by <code>pMethod</code> 
     * object, on the specified object with the specified parameters.
     * <b>This method is not synchronized and changes pMethod's accessibility
     * flag to true on entry and back to its original value on exit. be 
     * careful if pMethod may be shared by multiple threads.</b>
     * 
     * @param pMethod - method to invoke. 
     * @param pParameterValues - parameter values for the method.
     * @param pInvokingObject - object to invoke. Note: This parameter can be null if the method is
     * a class method.
     * @return Object - method's returned value.
     * @since RS4.0
     */
    public static Object invoke(Method pMethod, Object[] pParameterValues, Object pInvokingObject) {
        Object result = null;
        boolean accessible = pMethod.isAccessible();
        try {
            if (!accessible) {
                pMethod.setAccessible(true);
            }
            result = pMethod.invoke(pInvokingObject, pParameterValues);
        } catch (InvocationTargetException e) {
            throw ExceptionUtils.convertInvocationTargetException(e);
        } catch (IllegalAccessException e) {
            throw ExceptionUtils.createIllegalArgumentException(e);
        } finally {
            if (!accessible) {
                pMethod.setAccessible(false);
            }
        }
        return result;
    }

    /**
     * (Comment for invoke)
     * 
     * @param pMethodName
     * @param pInvokingObject
     * @return @since 1.0 S2
     */
    public static Object invoke(String pMethodName, Object pInvokingObject) {
        return invoke(pMethodName, null, null, pInvokingObject);
    }

    /**
     * Invoke the method and return the method's returned value.
     * 
     * @param pMethodName - method name to invoke.
     * @param pParameterTypes - parameter types for the method.
     * @param pParameterValues - parameter values for the method.
     * @param pInvokingObject - object to invoke. Note: This parameter can be null if the invoking
     * method is a class method.
     * @return Object - method's returned value.
     * @since RS4.0
     */
    public static Object invoke(String pMethodName, Class[] pParameterTypes, Object[] pParameterValues, Object pInvokingObject) {
        Method method = getMethod(pInvokingObject.getClass(), pMethodName, pParameterTypes);
        if (method == null) {
            throw new IllegalArgumentException("unknown method: " + formatMethodName(pInvokingObject.getClass(), pMethodName, pParameterTypes));
        }
        return invoke(method, pParameterValues, pInvokingObject);
    }

    /**
     * Create a new instance of the class with the corresponding constructor.
     * 
     * @param pConstructor
     * @return Object - new class instance.
     */
    public static Object newInstance(Constructor pConstructor) {
        return newInstance(pConstructor, (Object[]) null);
    }

    /**
     * Create a new instance of the class with the corresponding constructor.
     * 
     * @param pConstructor
     * @param pParameterValues - parameter values for the constructor.
     * @return Object - new class instance.
     * @since RS4.0
     */
    public static Object newInstance(Constructor pConstructor, Object[] pParameterValues) {
        Object instance;
        try {
            boolean accessible = pConstructor.isAccessible();
            try {
                if (!accessible) {
                    pConstructor.setAccessible(true);
                }
                instance = pConstructor.newInstance(pParameterValues);
            } finally {
                if (!accessible) {
                    pConstructor.setAccessible(false);
                }
            }
        } catch (InvocationTargetException e) {
            throw ExceptionUtils.convertInvocationTargetException(e);
        } catch (IllegalAccessException e) {
            throw ExceptionUtils.createIllegalArgumentException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
        return instance;
    }

    /**
     * Create a new instance of the class with the corresponding constructor.
     * 
     * @param pClassType - class from which invoke the constructor.
     * @param pParameterTypes - parameter types of the constructor.
     * @param pParameterValues - parameter values for the constructor.
     * @return Object - new class instance.
     * @since RS4.0
     */
    public static Object newInstance(Class pClassType, Class[] pParameterTypes, Object[] pParameterValues) {
        Constructor constructor = getConstructor(pClassType, pParameterTypes);
        if (constructor == null) {
            throw new IllegalArgumentException("unknown constructor: " + formatMethodName(pClassType, pClassType.getName(), pParameterTypes));
        }
        return newInstance(constructor, pParameterValues);
    }

    /**
     * Create a new instance of the class with the corresponding constructor.
     * 
     * @param pClassType - class from which invoke the constructor.
     * @param pParameterValues - parameter values for the constructor.
     * @return Object - new class instance.
     */
    public static Object newInstance(Class pClassType, Object[] pParameterValues) {
        Constructor constructor = getConstructor(pClassType, pParameterValues);
        if (constructor == null) {
            Class[] parameterTypes = new Class[pParameterValues.length];
            for (int i = 0, n = pParameterValues.length; i < n; i++) {
                parameterTypes[i] = pParameterValues[i] != null ? pParameterValues[i].getClass() : null;
            }
            throw new IllegalArgumentException("unknown constructor: " + formatMethodName(pClassType, pClassType.getName(), parameterTypes));
        }
        return newInstance(constructor, pParameterValues);
    }

    /**
     * Create a new instance of the class with the corresponding constructor.
     * 
     * @param pClassType - class from which invoke the constructor.
     * @return Object - new class instance.
     * @since RS4.0
     */
    public static Object newInstance(Class pClassType) {
        return newInstance(pClassType, null, null);
    }

    /**
     * Create a new instance of the class with the corresponding constructor.
     * 
     * @param pClassName - class from which invoke the constructor.
     * @return Object - new class instance.
     * @throws IllegalArgumentException if invalid class name
     */
    public static Object newInstance(String pClassName) {
        Class classType = getClass(pClassName);
        if (classType == null) {
            throw new IllegalArgumentException("unknown class: " + pClassName);
        }
        return newInstance(classType);
    }

    /**
     * Create a new instance of the class with the corresponding constructor.
     * 
     * @param pClassName - class name from which invoke the constructor.
     * @param pParameterTypes - parameter types of the constructor.
     * @param pParameterValues - parameter values for the constructor.
     * @return Object - new class instance. NoSuchMethodException, IllegalAccessException or an
     * InstantiationException is thrown.
     * @since RS4.0
     */
    public static Object newInstance(String pClassName, Class[] pParameterTypes, Object[] pParameterValues) {
        Class classType = getClass(pClassName);
        if (classType == null) {
            throw new IllegalArgumentException("unknown class: " + pClassName);
        }
        return newInstance(classType, pParameterTypes, pParameterValues);
    }

    /**
     * Create a new instance of the class with the corresponding constructor.
     * 
     * @param pClassName - class name from which invoke the constructor.
     * @param pParameterValues - parameter values for the constructor.
     * @return Object - new class instance. NoSuchMethodException, IllegalAccessException or an
     * InstantiationException is thrown.
    */
    public static Object newInstance(String pClassName, Object[] pParameterValues) {
        Class classType = getClass(pClassName);
        if (classType == null) {
            throw new IllegalArgumentException("unknown class: " + pClassName);
        }
        return newInstance(classType, pParameterValues);
    }

    /**
     * Create a new instance of an array.
     * 
     * @param pClassType - type of class in the array.
     * @param pLenght - Lenght of the array.
     * @return Object - new Array instance.
     */
    public static Object newArray(Class pClassType, int pLenght) {
        return Array.newInstance(pClassType, pLenght);
    }

    private static String formatMethodName(Class pType, String pMethodName, Class[] pParameterTypes) {
        StringBuffer sb = new StringBuffer();
        sb.append(pMethodName);
        sb.append('(');
        if (pParameterTypes != null) {
            for (int i = 0, n = pParameterTypes.length; i < n; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                if (pParameterTypes[i] != null) {
                    sb.append(formatTypeName(pParameterTypes[i]));
                } else {
                    sb.append("!null!");
                }
            }
        }
        sb.append(')');
        return sb.toString();
    }

    private static String formatTypeName(Class pType) {
        String val;
        if (pType.isArray()) {
            Class type = pType;
            int dimensions = 0;
            while (type.isArray()) {
                dimensions++;
                type = type.getComponentType();
            }
            StringBuffer sb = new StringBuffer();
            sb.append(type.getName());
            for (int i = 0; i < dimensions; i++) {
                sb.append("[]");
            }
            val = sb.toString();
        } else {
            val = pType.getName();
        }
        return val;
    }

    /**
	 * Attempt to find a {@link Method} on the supplied type with the supplied name
	 * and parameter types. Searches all superclasses up to <code>Object</code>.
	 * Returns <code>null</code> if no {@link Method} can be found.
	 */
    public static Method findMethod(Class type, String name, Class[] paramTypes) {
        Class searchType = type;
        while (!Object.class.equals(searchType) && searchType != null) {
            Method[] methods = (searchType.isInterface() ? searchType.getMethods() : searchType.getDeclaredMethods());
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (name.equals(method.getName()) && Arrays.equals(paramTypes, method.getParameterTypes())) {
                    return method;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    /**
	 * Invoke the specified {@link Method} against the supplied target object
	 * with no arguments. The target object can be <code>null</code> when
	 * invoking a static {@link Method}.
	 * <p>Thrown exceptions are handled via a call to {@link #handleReflectionException}.
	 * @see #invokeMethod(java.lang.reflect.Method, Object, Object[])
	 */
    public static Object invokeMethod(Method method, Object target) {
        return invokeMethod(method, target, null);
    }

    /**
	 * Invoke the specified {@link Method} against the supplied target object
	 * with the supplied arguments. The target object can be <code>null</code>
	 * when invoking a static {@link Method}.
	 * <p>Thrown exceptions are handled via a call to {@link #handleReflectionException}.
	 * @see #invokeMethod(java.lang.reflect.Method, Object, Object[])
	 */
    public static Object invokeMethod(Method method, Object target, Object[] args) {
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException ex) {
            handleReflectionException(ex);
            throw new IllegalStateException("Unexpected reflection exception - " + ex.getClass().getName() + ": " + ex.getMessage());
        } catch (InvocationTargetException ex) {
            handleReflectionException(ex);
            throw new IllegalStateException("Unexpected reflection exception - " + ex.getClass().getName() + ": " + ex.getMessage());
        }
    }

    /**
	 * Determine whether the given field is a "public static final" constant.
	 * @param field the field to check
	 */
    public static boolean isPublicStaticFinal(Field field) {
        int modifiers = field.getModifiers();
        return (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers));
    }

    /**
	 * Make the given field accessible, explicitly setting it accessible if necessary.
	 * The <code>setAccessible(true)</code> method is only called when actually necessary,
	 * to avoid unnecessary conflicts with a JVM SecurityManager (if active).
	 * @param field the field to make accessible
	 * @see java.lang.reflect.Field#setAccessible
	 */
    public static void makeAccessible(Field field) {
        if (!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers())) {
            field.setAccessible(true);
        }
    }

    /**
	 * Perform the given callback operation on all matching methods of the
	 * given class and superclasses.
	 * <p>The same named method occurring on subclass and superclass will
	 * appear twice, unless excluded by a {@link MethodFilter}.
	 * @param targetClass class to start looking at
	 * @param mc the callback to invoke for each method
	 * @see #doWithMethods(Class, MethodCallback, MethodFilter)
	 */
    public static void doWithMethods(Class targetClass, MethodCallback mc) throws IllegalArgumentException {
        doWithMethods(targetClass, mc, null);
    }

    /**
	 * Perform the given callback operation on all matching methods of the
	 * given class and superclasses.
	 * <p>The same named method occurring on subclass and superclass will
	 * appear twice, unless excluded by the specified {@link MethodFilter}.
	 * @param targetClass class to start looking at
	 * @param mc the callback to invoke for each method
	 * @param mf the filter that determines the methods to apply the callback to
	 */
    public static void doWithMethods(Class targetClass, MethodCallback mc, MethodFilter mf) throws IllegalArgumentException {
        do {
            Method[] methods = targetClass.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                if (mf != null && !mf.matches(methods[i])) {
                    continue;
                }
                try {
                    mc.doWith(methods[i]);
                } catch (IllegalAccessException ex) {
                    throw new IllegalStateException("Shouldn't be illegal to access method '" + methods[i].getName() + "': " + ex);
                }
            }
            targetClass = targetClass.getSuperclass();
        } while (targetClass != null);
    }

    /**
	 * Get all declared methods on the leaf class and all superclasses.
	 * Leaf class methods are included first.
	 */
    public static Method[] getAllDeclaredMethods(Class leafClass) throws IllegalArgumentException {
        final List l = new LinkedList();
        doWithMethods(leafClass, new MethodCallback() {

            public void doWith(Method m) {
                l.add(m);
            }
        });
        return (Method[]) l.toArray(new Method[l.size()]);
    }

    /**
	 * Invoke the given callback on all private fields in the target class,
	 * going up the class hierarchy to get all declared fields.
	 * @param targetClass the target class to analyze
	 * @param fc the callback to invoke for each field
	 */
    public static void doWithFields(Class targetClass, FieldCallback fc) throws IllegalArgumentException {
        doWithFields(targetClass, fc, null);
    }

    /**
	 * Invoke the given callback on all private fields in the target class,
	 * going up the class hierarchy to get all declared fields.
	 * @param targetClass the target class to analyze
	 * @param fc the callback to invoke for each field
	 * @param ff the filter that determines the fields to apply the callback to
	 */
    public static void doWithFields(Class targetClass, FieldCallback fc, FieldFilter ff) throws IllegalArgumentException {
        do {
            Field[] fields = targetClass.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                if (ff != null && !ff.matches(fields[i])) {
                    continue;
                }
                try {
                    fc.doWith(fields[i]);
                } catch (IllegalAccessException ex) {
                    throw new IllegalStateException("Shouldn't be illegal to access field '" + fields[i].getName() + "': " + ex);
                }
            }
            targetClass = targetClass.getSuperclass();
        } while (targetClass != null && targetClass != Object.class);
    }

    /**
	 * Given the source object and the destination, which must be the same class
	 * or a subclass, copy all fields, including inherited fields. Designed to
	 * work on objects with public no-arg constructors.
	 * @throws IllegalArgumentException if the arguments are incompatible
	 */
    public static void shallowCopyFieldState(final Object src, final Object dest) throws IllegalArgumentException {
        if (src == null) {
            throw new IllegalArgumentException("Source for field copy cannot be null");
        }
        if (dest == null) {
            throw new IllegalArgumentException("Destination for field copy cannot be null");
        }
        if (!src.getClass().isAssignableFrom(dest.getClass())) {
            throw new IllegalArgumentException("Destination class [" + dest.getClass().getName() + "] must be same or subclass as source class [" + src.getClass().getName() + "]");
        }
        doWithFields(src.getClass(), new ReflectionUtils.FieldCallback() {

            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                makeAccessible(field);
                Object srcValue = field.get(src);
                field.set(dest, srcValue);
            }
        }, ReflectionUtils.COPYABLE_FIELDS);
    }

    /**
	 * Action to take on each method.
	 */
    public static interface MethodCallback {

        /**
		 * Perform an operation using the given method.
		 * @param method the method which will have been made accessible before this invocation
		 */
        void doWith(Method method) throws IllegalArgumentException, IllegalAccessException;
    }

    /**
	 * Callback optionally used to method fields to be operated on by a method callback.
	 */
    public static interface MethodFilter {

        /**
		 * Determine whether the given method matches.
		 * @param method the method to check
		 */
        boolean matches(Method method);
    }

    /**
	 * Callback interface invoked on each field in the hierarchy.
	 */
    public static interface FieldCallback {

        /**
		 * Perform an operation using the given field.
		 * @param field the field which will have been made accessible before this invocation
		 */
        void doWith(Field field) throws IllegalArgumentException, IllegalAccessException;
    }

    /**
	 * Callback optionally used to filter fields to be operated on by a field callback.
	 */
    public static interface FieldFilter {

        /**
		 * Determine whether the given field matches.
		 * @param field the field to check
		 */
        boolean matches(Field field);
    }

    /**
	 * Pre-built FieldFilter that matches all non-static, non-final fields.
	 */
    public static FieldFilter COPYABLE_FIELDS = new FieldFilter() {

        public boolean matches(Field field) {
            return !(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()));
        }
    };
}
