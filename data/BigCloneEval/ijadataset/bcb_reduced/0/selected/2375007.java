package org.databene.commons;

import org.databene.commons.converter.AnyConverter;
import org.databene.commons.converter.ConverterManager;
import org.databene.commons.converter.ToStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.Callable;
import java.beans.Introspector;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;
import java.io.File;
import java.io.PrintWriter;

/**
 * Bundles reflection and introspection related operations.<br/><br/>
 * Created: 01.07.2006 08:44:33
 * @since 0.1
 * @author Volker Bergmann
 */
public final class BeanUtil {

    private static final Logger logger = LoggerFactory.getLogger(BeanUtil.class);

    private static final HashSet<String> NON_CLASS_NAMES = new HashSet<String>(100);

    private static Escalator escalator = new LoggerEscalator();

    private static final Map<String, PropertyDescriptor> propertyDescriptors = new HashMap<String, PropertyDescriptor>();

    /**
     * List of simple Java types.
     */
    private static final Class<?>[] simpleTypes = { String.class, long.class, Long.class, int.class, Integer.class, short.class, Short.class, byte.class, Byte.class, boolean.class, Boolean.class, char.class, Character.class, float.class, Float.class, double.class, Double.class, BigDecimal.class, BigInteger.class };

    private static final Class<?>[] integralNumberTypes = { long.class, Long.class, int.class, Integer.class, short.class, Short.class, byte.class, Byte.class, BigInteger.class };

    private static final Class<?>[] decimalNumberTypes = { float.class, Float.class, double.class, Double.class, BigDecimal.class };

    private static final PrimitiveTypeMapping[] primitiveNumberTypes = { new PrimitiveTypeMapping(long.class, Long.class), new PrimitiveTypeMapping(int.class, Integer.class), new PrimitiveTypeMapping(short.class, Short.class), new PrimitiveTypeMapping(byte.class, Byte.class), new PrimitiveTypeMapping(float.class, Float.class), new PrimitiveTypeMapping(double.class, Double.class) };

    private static final PrimitiveTypeMapping[] primitiveNonNumberTypes = { new PrimitiveTypeMapping(boolean.class, Boolean.class), new PrimitiveTypeMapping(char.class, Character.class) };

    /**
     * Map of integral Java number types
     */
    private static Map<String, Class<?>> integralNumberTypeMap;

    /**
     * Map of decimal Java number types
     */
    private static Map<String, Class<?>> decimalNumberTypeMap;

    /**
     * Map of simple Java types
     */
    private static Map<String, Class<?>> simpleTypeMap;

    /**
     * Map of primitive Java types
     */
    private static Map<String, Class<?>> primitiveTypeMap;

    /**
     * Map of primitive Java number types
     */
    private static Map<String, Class<?>> primitiveNumberTypeMap;

    static {
        simpleTypeMap = map(simpleTypes);
        integralNumberTypeMap = map(integralNumberTypes);
        decimalNumberTypeMap = map(decimalNumberTypes);
        primitiveNumberTypeMap = new HashMap<String, Class<?>>();
        primitiveTypeMap = new HashMap<String, Class<?>>();
        for (PrimitiveTypeMapping mapping : primitiveNumberTypes) {
            primitiveNumberTypeMap.put(mapping.primitiveType.getName(), mapping.wrapperType);
            primitiveTypeMap.put(mapping.primitiveType.getName(), mapping.wrapperType);
        }
        for (PrimitiveTypeMapping mapping : primitiveNonNumberTypes) primitiveTypeMap.put(mapping.primitiveType.getName(), mapping.wrapperType);
    }

    private static Map<String, Class<?>> map(Class<?>[] array) {
        Map<String, Class<?>> result = new HashMap<String, Class<?>>();
        for (Class<?> type : array) result.put(type.getName(), type);
        return result;
    }

    /** Prevents instantiation of a BeanUtil object. */
    private BeanUtil() {
    }

    public static Class<?> commonSuperType(Collection<?> objects) {
        Iterator<?> iterator = objects.iterator();
        if (!iterator.hasNext()) return null;
        Class<?> result = null;
        while (iterator.hasNext()) {
            Object candidate = iterator.next();
            if (candidate != null) {
                Class<?> candidateClass = candidate.getClass();
                if (result == null) result = candidateClass; else if (candidateClass != result && candidateClass.isAssignableFrom(result)) result = candidateClass;
            }
        }
        return result;
    }

    public static Class<?> commonSubType(Collection<?> objects) {
        Iterator<?> iterator = objects.iterator();
        if (!iterator.hasNext()) return null;
        Class<?> result = null;
        while (iterator.hasNext()) {
            Object candidate = iterator.next();
            if (candidate != null) {
                Class<?> candidateClass = candidate.getClass();
                if (result == null) result = candidateClass; else if (candidateClass != result && result.isAssignableFrom(candidateClass)) result = candidateClass;
            }
        }
        return result;
    }

    /**
     * Tells if the provided class name is the name of a simple Java type
     * @param className the name to check
     * @return true if it is a simple type, else false
     */
    public static boolean isSimpleType(String className) {
        return simpleTypeMap.containsKey(className);
    }

    public static boolean isPrimitiveType(String className) {
        return primitiveTypeMap.containsKey(className);
    }

    public static boolean isPrimitiveNumberType(String className) {
        return primitiveNumberTypeMap.containsKey(className);
    }

    public static boolean isNumberType(Class<?> type) {
        return (isIntegralNumberType(type) || isDecimalNumberType(type));
    }

    public static boolean isIntegralNumberType(Class<?> type) {
        return isIntegralNumberType(type.getName());
    }

    public static boolean isIntegralNumberType(String className) {
        return integralNumberTypeMap.containsKey(className);
    }

    public static boolean isDecimalNumberType(Class<?> type) {
        return isDecimalNumberType(type.getName());
    }

    public static boolean isDecimalNumberType(String className) {
        return decimalNumberTypeMap.containsKey(className);
    }

    public static Class<?> getWrapper(String primitiveClassName) {
        return primitiveTypeMap.get(primitiveClassName);
    }

    /**
     * Tells if the specified class is a collection type.
     * @param type the class to check
     * @return true if the class is a collection type, false otherwise
     */
    public static boolean isCollectionType(Class<?> type) {
        return Collection.class.isAssignableFrom(type);
    }

    /**
     * Returns an object's attribute value
     * @param obj the object to query
     * @param attributeName the name of the attribute
     * @return the attribute value
     */
    public static Object getAttributeValue(Object obj, String attributeName) {
        if (obj == null) throw new IllegalArgumentException("Object may not be null");
        Field field = getField(obj.getClass(), attributeName);
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw ExceptionMapper.configurationException(e, field);
        }
    }

    /**
     * Sets an attribute value of an object.
     * @param obj the object to modify
     * @param fieldName the name of the attribute to set
     * @param value the value to assign to the field
     */
    public static void setAttributeValue(Object obj, String fieldName, Object value) {
        Field field = getField(obj.getClass(), fieldName);
        setAttributeValue(obj, field, value);
    }

    /**
     * Returns a class' static attribute value
     * @param objectType the class to query
     * @param attributeName the name of the attribute
     * @return the attribute value
     */
    public static Object getStaticAttributeValue(Class<?> objectType, String attributeName) {
        Field field = getField(objectType, attributeName);
        try {
            return field.get(null);
        } catch (IllegalAccessException e) {
            throw ExceptionMapper.configurationException(e, field);
        }
    }

    /**
     * Sets a static attribute value of a class.
     * @param objectType the class to modify
     * @param fieldName the name of the attribute to set
     * @param value the value to assign to the field
     */
    public static void setStaticAttributeValue(Class<?> objectType, String fieldName, Object value) {
        Field field = getField(objectType, fieldName);
        setAttributeValue(null, field, value);
    }

    /**
     * Sets an attribute value of an object.
     * @param obj the object to modify
     * @param field the attribute to set
     * @param value the value to assign to the field
     */
    private static void setAttributeValue(Object obj, Field field, Object value) {
        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            throw ExceptionMapper.configurationException(e, field);
        }
    }

    /**
     * Returns the generic type information of an attribute.
     * @param field the field representation of the attribute.
     * @return an array of types that are used to parameterize the attribute.
     */
    public static Class<?>[] getGenericTypes(Field field) {
        Type genericFieldType = field.getGenericType();
        if (!(genericFieldType instanceof ParameterizedType)) return null;
        ParameterizedType pType = (ParameterizedType) genericFieldType;
        Type[] args = pType.getActualTypeArguments();
        Class<?>[] types = new Class[args.length];
        System.arraycopy(args, 0, types, 0, args.length);
        return types;
    }

    /**
     * Instantiates the specified class.
     * @param name the name of the class to instantiate
     * @return the Class instance
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> Class<T> forName(String name) {
        Assert.notNull(name, "class name");
        Class type = simpleTypeMap.get(name);
        if (type != null) return type; else {
            try {
                return (Class<T>) getContextClassLoader().loadClass(name);
            } catch (ClassNotFoundException e) {
                throw ExceptionMapper.configurationException(e, name);
            } catch (NullPointerException e) {
                throw ExceptionMapper.configurationException(e, name);
            }
        }
    }

    public static ClassLoader getContextClassLoader() {
        ClassLoader result = Thread.currentThread().getContextClassLoader();
        if (result == null) result = BeanUtil.class.getClassLoader();
        return result;
    }

    public static ClassLoader createJarClassLoader(File jarFile) throws MalformedURLException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (jarFile != null) classLoader = new URLClassLoader(new URL[] { jarFile.toURI().toURL() }, classLoader);
        return classLoader;
    }

    public static void runWithJarClassLoader(File jarFile, Runnable action) throws MalformedURLException {
        Thread currentThread = Thread.currentThread();
        ClassLoader contextClassLoader = currentThread.getContextClassLoader();
        try {
            currentThread.setContextClassLoader(createJarClassLoader(jarFile));
            action.run();
        } finally {
            currentThread.setContextClassLoader(contextClassLoader);
        }
    }

    public static <T> T callWithJarClassLoader(File jarFile, Callable<T> action) throws Exception {
        Thread currentThread = Thread.currentThread();
        ClassLoader contextClassLoader = currentThread.getContextClassLoader();
        try {
            currentThread.setContextClassLoader(createJarClassLoader(jarFile));
            return action.call();
        } finally {
            currentThread.setContextClassLoader(contextClassLoader);
        }
    }

    /**
     * Instantiates a class by the default constructor.
     * @param className the name of the class to instantiate
     * @return an instance of the class
     */
    public static Object newInstance(String className) {
        Class<?> type = BeanUtil.forName(className);
        return newInstanceFromDefaultConstructor(type);
    }

    /**
     * Creates an object of the specified type.
     * @param type the class to instantiate
     * @param parameters the constructor parameters
     * @return an object of the specified class
     */
    public static <T> T newInstance(Class<T> type, Object... parameters) {
        return newInstance(type, true, parameters);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T newInstance(Class<T> type, boolean strict, Object... parameters) {
        if (parameters.length == 0) return newInstanceFromDefaultConstructor(type);
        Constructor<T> constructorToUse = null;
        try {
            Constructor<T>[] constructors = (Constructor<T>[]) type.getConstructors();
            List<Constructor<T>> candidates = new ArrayList<Constructor<T>>(constructors.length);
            int paramCount = parameters.length;
            for (Constructor<T> constructor : constructors) if (constructor.getParameterTypes().length == paramCount) candidates.add(constructor);
            if (candidates.size() == 1) constructorToUse = candidates.get(0); else if (candidates.size() == 0) throw new ConfigurationError("No constructor with " + paramCount + " parameters found for " + type); else {
                Class<?>[] paramTypes = new Class[parameters.length];
                for (int i = 0; i < parameters.length; i++) paramTypes[i] = parameters[i].getClass();
                for (Constructor c : type.getConstructors()) {
                    if (typesMatch(c.getParameterTypes(), paramTypes)) {
                        constructorToUse = c;
                        break;
                    }
                }
                if (constructorToUse == null) {
                    if (strict) throw new NoSuchMethodException("No appropriate constructor found: " + type + '(' + ArrayFormat.format(", ", paramTypes) + ')');
                    Exception mostRecentException = null;
                    for (Constructor<T> candidate : candidates) {
                        try {
                            return newInstance(candidate, strict, parameters);
                        } catch (Exception e) {
                            mostRecentException = e;
                            logger.warn("Exception in constructor call: " + candidate, e);
                            continue;
                        }
                    }
                    String errMsg = (mostRecentException != null ? "None of these constructors could be called without exception: " + candidates + ", latest exception: " + mostRecentException : type + " has no appropriate constructor for the arguments " + ArrayFormat.format(", ", parameters));
                    throw new ConfigurationError(errMsg);
                }
            }
            if (!strict) parameters = convertArray(parameters, constructorToUse.getParameterTypes());
            return newInstance(constructorToUse, parameters);
        } catch (SecurityException e) {
            throw ExceptionMapper.configurationException(e, constructorToUse);
        } catch (NoSuchMethodException e) {
            throw ExceptionMapper.configurationException(e, type);
        }
    }

    /**
     * Creates a new instance of a Class.
     * @param constructor
     * @param params
     * @return a new instance of the class
     */
    public static <T> T newInstance(Constructor<T> constructor, Object... params) {
        return newInstance(constructor, true, params);
    }

    public static <T> T newInstance(Constructor<T> constructor, boolean strict, Object... parameters) {
        if (!strict) parameters = convertArray(parameters, constructor.getParameterTypes());
        Class<T> type = constructor.getDeclaringClass();
        if (deprecated(type)) escalator.escalate("Instantiating a deprecated class: " + type.getName(), BeanUtil.class, null);
        try {
            return constructor.newInstance(parameters);
        } catch (InstantiationException e) {
            throw ExceptionMapper.configurationException(e, type);
        } catch (IllegalAccessException e) {
            throw ExceptionMapper.configurationException(e, type);
        } catch (InvocationTargetException e) {
            throw ExceptionMapper.configurationException(e, type);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T clone(T object) {
        try {
            Method cloneMethod = object.getClass().getMethod("clone");
            return (T) cloneMethod.invoke(object);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Unexpected exception", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unexpected exception", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Execption occured in clone() method", e);
        }
    }

    public static <T> T[] cloneAll(T[] input) {
        T[] output = ArrayUtil.newInstance(ArrayUtil.componentType(input), input.length);
        for (int i = 0; i < input.length; i++) output[i] = clone(input[i]);
        return output;
    }

    /**
     * Finds a method by reflection. This iterates all methods of the class, comparing names and parameter types.
     * Unlike the method Class.getMethod(String, Class ...), this method is able to match primitive and wrapper types.
     * If no appropriate method is found, a ConfigurationError is raised.
     * @param type
     * @param methodName
     * @param paramTypes
     * @return a method with matching names and parameters
     */
    public static Method getMethod(Class<?> type, String methodName, Class<?>... paramTypes) {
        Method method = findMethod(type, methodName, paramTypes);
        if (method == null) throw new ConfigurationError("method not found in class " + type.getName() + ": " + methodName + '(' + ArrayFormat.format(paramTypes) + ')');
        return method;
    }

    /**
     * Finds a method by reflection. This iterates all methods of the class, comparing names and parameter types.
     * Unlike the method Class.getMethod(String, Class ...), this method is able to match primitive and wrapper types.
     * If no appropriate method is found, 'null' is returned
     * @param type
     * @param methodName
     * @param paramTypes
     * @return a method with matching names and parameters
     */
    public static Method findMethod(Class<?> type, String methodName, Class<?>... paramTypes) {
        Method result = null;
        for (Method method : type.getMethods()) {
            if (!methodName.equals(method.getName())) continue;
            if (typesMatch(paramTypes, method.getParameterTypes())) {
                result = method;
                if ((ArrayUtil.isEmpty(paramTypes) && ArrayUtil.isEmpty(method.getParameterTypes())) || paramTypes.length == method.getParameterTypes().length) return method; else result = method;
            }
        }
        return result;
    }

    public static Method[] findMethodsByName(Class<?> type, String methodName) {
        ArrayBuilder<Method> builder = new ArrayBuilder<Method>(Method.class);
        for (Method method : type.getMethods()) {
            if (methodName.equals(method.getName())) builder.add(method);
        }
        return builder.toArray();
    }

    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> findConstructor(Class<T> type, Class<?>... paramTypes) {
        Constructor<T>[] ctors = (Constructor<T>[]) type.getConstructors();
        for (Constructor<T> ctor : ctors) if (typesMatch(paramTypes, ctor.getParameterTypes())) return ctor;
        return null;
    }

    /**
     * Invokes a method on a bean.
     * @param target
     * @param methodName
     * @param args
     * @return the invoked method's return value.
     */
    public static Object invoke(Object target, String methodName, Object... args) {
        return invoke(true, target, methodName, args);
    }

    @SuppressWarnings("rawtypes")
    public static Object invoke(boolean strict, Object target, String methodName, Object... args) {
        if (target == null) throw new IllegalArgumentException("target is null");
        Class[] argTypes = null;
        if (args != null) {
            argTypes = new Class[args.length];
            for (int i = 0; i < args.length; i++) argTypes[i] = (args[i] != null ? args[i].getClass() : null);
        }
        Method method;
        if (target instanceof Class) method = getMethod((Class) target, methodName, argTypes); else method = getMethod(target.getClass(), methodName, argTypes);
        return invoke(target, method, strict, args);
    }

    public static Object invokeStatic(Class<?> targetClass, String methodName, Object... args) {
        return invokeStatic(targetClass, methodName, true, args);
    }

    public static Object invokeStatic(Class<?> targetClass, String methodName, boolean strict, Object... args) {
        if (targetClass == null) throw new IllegalArgumentException("target is null");
        Class<?>[] argClasses = new Class[args.length];
        for (int i = 0; i < args.length; i++) argClasses[i] = (args[i] != null ? args[i].getClass() : null);
        Method method = getMethod(targetClass, methodName, argClasses);
        return invoke(null, method, strict, args);
    }

    /**
     * Invokes a method on a bean
     * @param target
     * @param method
     * @param args
     * @return the invoked method's return value.
     */
    public static Object invoke(Object target, Method method, Object... args) {
        return invoke(target, method, true, args);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Object invoke(Object target, Method method, boolean strict, Object... args) {
        try {
            Object[] params;
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length == 0) {
                params = null;
            } else if (args.length == paramTypes.length) {
                if (strict) {
                    params = args;
                } else {
                    params = new Object[paramTypes.length];
                    for (int i = 0; i < paramTypes.length; i++) {
                        Object arg = args[i];
                        if (arg == null) params[i] = null; else {
                            Converter converter = ConverterManager.getInstance().createConverter(arg.getClass(), paramTypes[i]);
                            params[i] = converter.convert(arg);
                        }
                    }
                }
            } else {
                params = new Object[paramTypes.length];
                for (int i = 0; i < paramTypes.length - 1; i++) params[i] = (strict ? args[i] : AnyConverter.convert(args[i], paramTypes[i]));
                Class<?> varargsComponentType = paramTypes[paramTypes.length - 1].getComponentType();
                Object varargs = Array.newInstance(varargsComponentType, args.length - paramTypes.length + 1);
                for (int i = 0; i < args.length - paramTypes.length + 1; i++) {
                    Object param = args[paramTypes.length - 1 + i];
                    if (strict) param = AnyConverter.convert(param, varargsComponentType);
                    Array.set(varargs, i, param);
                }
                params[params.length - 1] = varargs;
            }
            return method.invoke(target, params);
        } catch (IllegalAccessException e) {
            throw ExceptionMapper.configurationException(e, method);
        } catch (InvocationTargetException e) {
            throw ExceptionMapper.configurationException(e, method);
        }
    }

    public static boolean typesMatch(Class<?>[] usedTypes, Class<?>[] expectedTypes) {
        if (ArrayUtil.isEmpty(expectedTypes)) return ArrayUtil.isEmpty(usedTypes);
        Class<?> lastExpectedType = ArrayUtil.lastElementOf(expectedTypes);
        if (lastExpectedType.isArray()) {
            if (usedTypes.length < expectedTypes.length - 1) return false;
            if (usedTypes.length == expectedTypes.length - 1) return typesMatch(usedTypes, ArrayUtil.copyOfRange(expectedTypes, 0, usedTypes.length));
            if (usedTypes.length >= expectedTypes.length) {
                Class<?> componentType = lastExpectedType.getComponentType();
                for (int i = expectedTypes.length - 1; i < usedTypes.length; i++) {
                    Class<?> foundType = usedTypes[i];
                    if (!typeMatches(foundType, componentType)) return false;
                }
                return true;
            }
        }
        if (usedTypes.length != expectedTypes.length) return false;
        if (expectedTypes.length == 0 && usedTypes.length == 0) return true;
        for (int i = 0; i < usedTypes.length; i++) {
            Class<?> expectedType = expectedTypes[i];
            Class<?> foundType = usedTypes[i];
            if (!typeMatches(foundType, expectedType)) return false;
        }
        return true;
    }

    private static boolean typeMatches(Class<?> foundType, Class<?> expectedType) {
        if (foundType == null) return true;
        if (expectedType.isAssignableFrom(foundType)) return true;
        if (isPrimitiveType(expectedType.getName()) && foundType.equals(getWrapper(expectedType.getName()))) return true;
        if (isPrimitiveType(foundType.getName()) && expectedType.equals(getWrapper(foundType.getName()))) return true;
        if (isNumberType(foundType) && isNumberType(expectedType)) return true;
        return false;
    }

    /**
     * Returns the bean property descriptor of an attribute
     * @param beanClass the class that holds the attribute
     * @param propertyName the name of the property
     * @return the attribute's property descriptor
     */
    public static PropertyDescriptor getPropertyDescriptor(Class<?> beanClass, String propertyName) {
        if (beanClass == null) throw new IllegalArgumentException("beanClass is null");
        String propertyId = beanClass.getName() + '#' + propertyName;
        PropertyDescriptor result = propertyDescriptors.get(propertyId);
        if (result != null) return result;
        int separatorIndex = propertyName.indexOf('.');
        if (separatorIndex >= 0) {
            String localProperty = propertyName.substring(0, separatorIndex);
            String remoteProperty = propertyName.substring(separatorIndex + 1);
            Class<?> localPropertyType = getPropertyDescriptor(beanClass, localProperty).getPropertyType();
            result = getPropertyDescriptor(localPropertyType, remoteProperty);
        } else {
            try {
                BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
                PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
                for (PropertyDescriptor descriptor : descriptors) {
                    String name = descriptor.getName();
                    if (name.equals(propertyName)) {
                        result = descriptor;
                        break;
                    }
                }
            } catch (IntrospectionException e) {
                throw ExceptionMapper.configurationException(e, propertyName);
            }
        }
        propertyDescriptors.put(propertyId, result);
        return result;
    }

    public static PropertyDescriptor getPropertyDescriptor(Class<?> type, String propertyName, boolean required) {
        PropertyDescriptor descriptor = getPropertyDescriptor(type, propertyName);
        if (required && descriptor == null) throw new UnsupportedOperationException(type.getName() + " does not have a property " + propertyName);
        return descriptor;
    }

    public static boolean hasProperty(Class<?> beanClass, String propertyName) {
        return (getPropertyDescriptor(beanClass, propertyName) != null);
    }

    /**
     * returns the name of a property read method.
     * @param propertyName the name of the property
     * @param propertyType the type of the property
     * @return the name of the property read method
     */
    public static String readMethodName(String propertyName, Class<?> propertyType) {
        if (boolean.class.equals(propertyType) || Boolean.class.equals(propertyType)) return "is" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1); else return "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
    }

    /**
     * returns the name of a property write method.
     * @param propertyName the name of the property
     * @return the name of the property write method
     */
    public static String writeMethodName(String propertyName) {
        return "set" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
    }

    /**
     * Finds all property descriptors of a bean class
     * @param type the class to check
     * @return all found property descriptors
     */
    public static PropertyDescriptor[] getPropertyDescriptors(Class<?> type) {
        try {
            return Introspector.getBeanInfo(type).getPropertyDescriptors();
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Queries a property value on a JavaBean instance
     * @param bean
     * @param propertyName
     * @return the property value
     */
    public static Object getPropertyValue(Object bean, String propertyName) {
        return getPropertyValue(bean, propertyName, true);
    }

    public static Object getPropertyValue(Object bean, String propertyName, boolean strict) {
        Method readMethod = null;
        try {
            PropertyDescriptor descriptor = getPropertyDescriptor(bean.getClass(), propertyName);
            if (descriptor == null) {
                if (strict) throw new ConfigurationError("Property '" + propertyName + "' not found in class " + bean.getClass()); else return null;
            }
            readMethod = descriptor.getReadMethod();
            return readMethod.invoke(bean);
        } catch (IllegalAccessException e) {
            throw ExceptionMapper.configurationException(e, readMethod);
        } catch (InvocationTargetException e) {
            throw ExceptionMapper.configurationException(e, readMethod);
        }
    }

    /**
     * sets a property value on a JavaBean instance.
     * @param bean
     * @param propertyName
     * @param propertyValue
     */
    public static void setPropertyValue(Object bean, String propertyName, Object propertyValue) {
        setPropertyValue(bean, propertyName, propertyValue, true);
    }

    public static void setPropertyValue(Object bean, String propertyName, Object propertyValue, boolean strict) {
        setPropertyValue(bean, propertyName, propertyValue, strict, !strict);
    }

    public static void setPropertyValue(Object bean, String propertyName, Object propertyValue, boolean required, boolean autoConvert) {
        Method writeMethod = null;
        try {
            Class<?> beanClass = bean.getClass();
            PropertyDescriptor propertyDescriptor = getPropertyDescriptor(beanClass, propertyName);
            if (propertyDescriptor == null) if (required) throw new ConfigurationError(beanClass + " does not have a property '" + propertyName + "'"); else return;
            writeMethod = propertyDescriptor.getWriteMethod();
            if (writeMethod == null) throw new UnsupportedOperationException("Cannot write read-only property '" + propertyDescriptor.getName() + "' of " + beanClass);
            Class<?> propertyType = propertyDescriptor.getPropertyType();
            if (propertyValue != null) {
                Class<?> argType = propertyValue.getClass();
                if (!propertyType.isAssignableFrom(argType) && !isWrapperTypeOf(propertyType, propertyValue) && !autoConvert) throw new IllegalArgumentException("ArgumentType mismatch: expected " + propertyType.getName() + ", found " + propertyValue.getClass().getName()); else propertyValue = AnyConverter.convert(propertyValue, propertyType);
            }
            writeMethod.invoke(bean, propertyValue);
        } catch (IllegalAccessException e) {
            throw ExceptionMapper.configurationException(e, writeMethod);
        } catch (InvocationTargetException e) {
            throw ExceptionMapper.configurationException(e, writeMethod);
        }
    }

    private static boolean isWrapperTypeOf(Class<?> propertyType, Object propertyValue) {
        String propertyTypeName = propertyType.getName();
        return (isPrimitiveType(propertyTypeName) && getWrapper(propertyType.getName()) == propertyValue.getClass());
    }

    @SuppressWarnings("unchecked")
    public static <BEAN, PROP_TYPE> List<PROP_TYPE> extractProperties(Collection<BEAN> beans, String propertyName) {
        List<PROP_TYPE> result = new ArrayList<PROP_TYPE>(beans.size());
        for (BEAN bean : beans) result.add((PROP_TYPE) getPropertyValue(bean, propertyName));
        return result;
    }

    /**
     * Prints information about a class' parents and methods to a PrintWriter
     * @param object
     * @param printer
     */
    public static void printClassInfo(Object object, PrintWriter printer) {
        if (object == null) {
            printer.println("null");
            return;
        }
        Class<?> type = object.getClass();
        printer.println(type);
        if (type.getSuperclass() != null) printer.println("extends " + type.getSuperclass());
        for (Class<?> interf : type.getInterfaces()) printer.println("implements " + interf);
        for (Method method : type.getMethods()) {
            printer.println(method);
        }
    }

    /**
     * Checks if a class fulfills the JavaBeans contract.
     * @param cls the class to check
     */
    public static void checkJavaBean(Class<?> cls) {
        try {
            Constructor<?> constructor = cls.getDeclaredConstructor();
            int classModifiers = cls.getModifiers();
            if (Modifier.isInterface(classModifiers)) throw new RuntimeException(cls.getName() + " is an interface");
            if (Modifier.isAbstract(classModifiers)) throw new RuntimeException(cls.getName() + " cannot be instantiated - it is an abstract class");
            int modifiers = constructor.getModifiers();
            if (!Modifier.isPublic(modifiers)) throw new RuntimeException("No public default constructor in " + cls);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("No default constructor in class " + cls);
        } catch (SecurityException e) {
            logger.error("I am not allowed to check the class by using reflection, " + "so I just can hope the class is alright and go on: ", e);
        }
    }

    /**
     * Tells if a class is deprecated.
     * @param type the class to check for deprecation
     * @return true if the class is deprecated, else false
     * @since 0.2.05
     */
    public static boolean deprecated(Class<?> type) {
        Annotation[] annotations = type.getDeclaredAnnotations();
        for (Annotation annotation : annotations) if (annotation instanceof Deprecated) return true;
        return false;
    }

    /**
     * Creates an instance of the class using the default constructor.
     * @since 0.2.06
     */
    @SuppressWarnings("cast")
    private static <T> T newInstanceFromDefaultConstructor(Class<T> type) {
        if (type == null) return null;
        if (logger.isDebugEnabled()) logger.debug("Instantiating " + type.getSimpleName());
        if (deprecated(type)) escalator.escalate("Instantiating a deprecated class: " + type.getName(), BeanUtil.class, null);
        try {
            return (T) type.newInstance();
        } catch (InstantiationException e) {
            throw ExceptionMapper.configurationException(e, type);
        } catch (IllegalAccessException e) {
            throw ExceptionMapper.configurationException(e, type);
        }
    }

    public static Object getFieldValue(Object target, String name, boolean strict) {
        Class<?> type = target.getClass();
        try {
            Field field = type.getField(name);
            return getFieldValue(field, target, strict);
        } catch (NoSuchFieldException e) {
            if (strict) throw ExceptionMapper.configurationException(e, type.getName() + '.' + name); else {
                escalator.escalate("Class '" + type + "' does not have a field '" + name + "'", type, name);
                return null;
            }
        }
    }

    public static Object getFieldValue(Field field, Object target, boolean strict) {
        try {
            if ((field.getModifiers() & Modifier.STATIC) == Modifier.STATIC) return field.get(null); else return field.get(target);
        } catch (IllegalArgumentException e) {
            throw new ConfigurationError(e);
        } catch (IllegalAccessException e) {
            throw new ConfigurationError(e);
        }
    }

    /**
     * Returns a Field object that represents an attribute of a class
     * @param type the class that holds the attribute
     * @param name the name of the attribute
     * @return a Field object that represents the attribute
     */
    public static Field getField(Class<?> type, String name) {
        try {
            return type.getField(name);
        } catch (NoSuchFieldException e) {
            throw ExceptionMapper.configurationException(e, type.getName() + '.' + name);
        }
    }

    /**
     * Represents a primitive-to-wrapper mapping.
     */
    private static final class PrimitiveTypeMapping {

        public Class<?> primitiveType;

        public Class<?> wrapperType;

        public PrimitiveTypeMapping(Class<?> primitiveType, Class<?> wrapperType) {
            this.primitiveType = primitiveType;
            this.wrapperType = wrapperType;
        }
    }

    public static Method[] findMethodsByAnnotation(Class<?> owner, Class<? extends Annotation> annotationClass) {
        Method[] methods = owner.getMethods();
        ArrayBuilder<Method> builder = new ArrayBuilder<Method>(Method.class);
        for (Method method : methods) if (method.getAnnotation(annotationClass) != null) builder.add(method);
        return builder.toArray();
    }

    public static <C, I> Type[] getGenericInterfaceParams(Class<C> checkedClass, Class<I> searchedInterface) {
        for (Type type : checkedClass.getGenericInterfaces()) {
            ParameterizedType pt = (ParameterizedType) type;
            if (searchedInterface.equals(pt.getRawType())) {
                ParameterizedType pType = ((ParameterizedType) type);
                return pType.getActualTypeArguments();
            }
        }
        if (!Object.class.equals(checkedClass.getSuperclass())) return getGenericInterfaceParams(checkedClass.getSuperclass(), searchedInterface);
        throw new ConfigurationError(checkedClass + " does not implement interface with generic parameters: " + searchedInterface);
    }

    public static String toString(Object bean) {
        return toString(bean, false);
    }

    public static String toString(Object bean, boolean simple) {
        if (bean == null) return null;
        Class<?> beanClass = bean.getClass();
        StringBuilder builder = new StringBuilder(simple ? beanClass.getSimpleName() : bean.getClass().getName());
        PropertyDescriptor[] descriptors = getPropertyDescriptors(bean.getClass());
        boolean first = true;
        for (PropertyDescriptor descriptor : descriptors) {
            String propertyName = descriptor.getName();
            if (!"class".equals(propertyName) && descriptor.getReadMethod() != null) {
                if (first) builder.append('['); else builder.append(", ");
                Object value = getPropertyValue(bean, propertyName);
                String valueString = ToStringConverter.convert(value, "null");
                builder.append(propertyName).append("=").append(valueString);
                first = false;
            }
        }
        if (!first) builder.append(']');
        return builder.toString();
    }

    public static <T> String simpleClassName(Object o) {
        if (o == null) return null;
        return (o instanceof Class ? ((Class<?>) o).getName() : o.getClass().getSimpleName());
    }

    /** Tries to convert both arguments to the same type and then compares them */
    public static boolean equalsIgnoreType(Object o1, Object o2) {
        if (NullSafeComparator.equals(o1, o2)) return true;
        if (o1 == null || o2 == null) return false;
        if (o1.getClass() == o2.getClass()) return false;
        if (o1 instanceof String && o2 instanceof Number) {
            Object tmp = o1;
            o1 = o2;
            o2 = tmp;
        }
        if (o1 instanceof Number) {
            if (o2 instanceof String) o2 = AnyConverter.convert(o2, o1.getClass());
            return (((Number) o1).doubleValue() == ((Number) o2).doubleValue());
        }
        return false;
    }

    public static boolean existsClass(String className) {
        try {
            if (NON_CLASS_NAMES.contains(className)) return false;
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            NON_CLASS_NAMES.add(className);
            return false;
        }
    }

    private static Object[] convertArray(Object[] values, Class<?>[] targetTypes) {
        Object[] result = new Object[values.length];
        for (int i = 0; i < values.length; i++) result[i] = AnyConverter.convert(values[i], targetTypes[i]);
        return result;
    }
}
