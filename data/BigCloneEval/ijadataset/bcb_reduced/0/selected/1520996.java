package org.plazmaforge.framework.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.plazmaforge.framework.core.exception.ApplicationException;

public class ClassUtils {

    private static ClassUtils instance;

    private static ClassLoader externalLoader;

    private static Map<String, Class> classMap = new HashMap<String, Class>();

    static {
        classMap.put(Boolean.TYPE.getName(), Boolean.TYPE);
        classMap.put(Byte.TYPE.getName(), Byte.TYPE);
        classMap.put(Short.TYPE.getName(), Short.TYPE);
        classMap.put(Integer.TYPE.getName(), Integer.TYPE);
        classMap.put(Long.TYPE.getName(), Long.TYPE);
        classMap.put(Float.TYPE.getName(), Float.TYPE);
        classMap.put(Double.TYPE.getName(), Double.TYPE);
        classMap.put(Boolean.class.getName(), Boolean.class);
        classMap.put(Byte.class.getName(), Byte.class);
        classMap.put(Short.class.getName(), Short.class);
        classMap.put(Integer.class.getName(), Integer.class);
        classMap.put(Long.class.getName(), Long.class);
        classMap.put(Float.class.getName(), Float.class);
        classMap.put(Double.class.getName(), Double.class);
        classMap.put(BigInteger.class.getName(), BigInteger.class);
        classMap.put(BigDecimal.class.getName(), BigDecimal.class);
        classMap.put(AtomicInteger.class.getName(), AtomicInteger.class);
        classMap.put(AtomicLong.class.getName(), AtomicLong.class);
        classMap.put(Date.class.getName(), Date.class);
        classMap.put(java.sql.Date.class.getName(), java.sql.Date.class);
        classMap.put(Time.class.getName(), Time.class);
        classMap.put(Timestamp.class.getName(), Timestamp.class);
        classMap.put(String.class.getName(), String.class);
    }

    private ClassUtils() {
    }

    public static synchronized ClassUtils getInstance() {
        if (instance == null) {
            instance = new ClassUtils();
        }
        return instance;
    }

    /**
     * Return simple class by name
     * @param className
     * @return
     */
    public static Class getSimpleClass(String className) {
        return classMap.get(className);
    }

    public static Class forName(String className) throws ClassNotFoundException {
        ClassLoader contextClassLoader = null;
        try {
            contextClassLoader = Thread.currentThread().getContextClassLoader();
            return Class.forName(className, true, contextClassLoader);
        } catch (ClassNotFoundException ex) {
            return Class.forName(className);
        }
    }

    /**
     * Returns the class by name
     * 
     * @param className
     * @return
     * @throws ApplicationException
     */
    public static Class getClass(String className) throws ApplicationException {
        try {
            return forName(className);
        } catch (Exception e) {
            throw new ApplicationException(e);
        }
    }

    /**
     * Returns an instance of the class  by class
     * @param klass
     * @return
     * @throws ApplicationException
     */
    public static Object getClassInstance(Class klass) throws ApplicationException {
        try {
            if (klass == null) {
                return null;
            }
            return klass.newInstance();
        } catch (Exception e) {
            throw new ApplicationException(e);
        }
    }

    /**
     * Returns an instance of the class  by class name
     * 
     * @param className
     * @return
     * @throws ApplicationException
     */
    public static Object getClassInstance(String className) throws ApplicationException {
        Class klass = getClass(className);
        return getClassInstance(klass);
    }

    /**
     * Returns an instance of the class  by class name and argumnet of constructor
     * 
     * @param className
     * @param arg
     * @return
     * @throws ApplicationException
     */
    public static Object getClassInstance(String className, Object arg) throws ApplicationException {
        if (arg == null) {
            return getClassInstance(className);
        }
        try {
            Class klass = getClass(className);
            Class[] ca = new Class[1];
            ca[0] = arg.getClass();
            return klass.getConstructor(ca).newInstance(new Object[] { arg });
        } catch (Exception e) {
            throw new ApplicationException(e);
        }
    }

    /**
     * Retuns <code>BeanInfo</code> by class
     * @param klass
     * @return
     * @throws ApplicationException
     */
    public static BeanInfo getBeanInfo(Class klass) throws ApplicationException {
        checkClass(klass);
        try {
            return Introspector.getBeanInfo(klass);
        } catch (IntrospectionException ex) {
            throw new ApplicationException(ex);
        }
    }

    public static PropertyDescriptor[] getBeanProperties(Class klass) throws ApplicationException {
        checkClass(klass);
        return getBeanInfo(klass).getPropertyDescriptors();
    }

    /**
     * Returns array of <code>PropertyDescriptor</code> by class and property type
     * @param klass
     * @param type
     * @return
     * @throws ApplicationException
     */
    public static PropertyDescriptor[] getBeanPropertiesByType(Class klass, Class type) throws ApplicationException {
        PropertyDescriptor[] props = getBeanProperties(klass);
        if (props == null || props.length == 0) {
            return new PropertyDescriptor[0];
        }
        List<PropertyDescriptor> result = new ArrayList<PropertyDescriptor>();
        for (int i = 0; i < props.length; i++) {
            PropertyDescriptor desc = props[i];
            Class propertyType = desc.getPropertyType();
            if (type.isAssignableFrom(propertyType)) {
                result.add(desc);
            }
        }
        return result.toArray(new PropertyDescriptor[0]);
    }

    /**
     * Returns array of <code>PropertyDescriptor</code> by class and collection type
     * @param klass
     * @return
     * @throws ApplicationException
     */
    public static PropertyDescriptor[] getBeanCollectionProperties(Class klass) throws ApplicationException {
        return getBeanPropertiesByType(klass, Collection.class);
    }

    /**
     * Returns <code>PropertyDescriptor</code> by class and property name
     * @param klass
     * @param propertyName
     * @return
     * @throws ApplicationException
     */
    public static PropertyDescriptor getBeanProperty(Class klass, String propertyName) throws ApplicationException {
        checkClass(klass);
        checkPropertyName(propertyName);
        propertyName = propertyName.trim();
        PropertyDescriptor[] properties = getBeanProperties(klass);
        if (properties == null || properties.length == 0) {
            checkPropertyDescriptor(null);
        }
        PropertyDescriptor property = null;
        for (PropertyDescriptor prop : properties) {
            if (propertyName.equals(prop.getName())) {
                property = prop;
                break;
            }
        }
        checkPropertyDescriptor(property);
        return property;
    }

    public static Class getBeanPropertyTypeWithNull(Class klass, String propertyName) {
        try {
            return getBeanPropertyType(klass, propertyName);
        } catch (ApplicationException ex) {
            return null;
        }
    }

    public static Class getBeanPropertyType(Class klass, String propertyName) throws ApplicationException {
        PropertyDescriptor property = getBeanProperty(klass, propertyName);
        checkPropertyDescriptor(property, propertyName);
        return property.getPropertyType();
    }

    public static Method getBeanReadMethod(Class klass, String propertyName) throws ApplicationException {
        PropertyDescriptor property = getBeanProperty(klass, propertyName);
        checkPropertyDescriptor(property, propertyName);
        Method method = property.getReadMethod();
        checkMethod(method, "Read", propertyName);
        return method;
    }

    public static Method getBeanWriteMethod(Class klass, String propertyName) throws ApplicationException {
        PropertyDescriptor property = getBeanProperty(klass, propertyName);
        checkPropertyDescriptor(property, propertyName);
        Method method = property.getWriteMethod();
        checkMethod(method, "Write", propertyName);
        return method;
    }

    public static String getBeanReadMethodName(Class klass, String propertyName) throws ApplicationException {
        checkClass(klass);
        checkPropertyName(propertyName);
        Method method = getBeanReadMethod(klass, propertyName);
        checkMethod(method, "Read", propertyName);
        return method.getName();
    }

    public static String getBeanWriteMethodName(Class klass, String propertyName) throws ApplicationException {
        checkClass(klass);
        checkPropertyName(propertyName);
        Method method = getBeanWriteMethod(klass, propertyName);
        checkMethod(method, "Write", propertyName);
        return method.getName();
    }

    public static Object getBeanValue(Class klass, String propertyName, Object obj) throws ApplicationException {
        checkClass(klass);
        checkPropertyName(propertyName);
        Method method = getBeanReadMethod(klass, propertyName);
        checkMethod(method, "Read", propertyName);
        return invoke(method, obj);
    }

    public static void setBeanValue(Class klass, String propertyName, Object obj, Object value) throws ApplicationException {
        checkClass(klass);
        checkPropertyName(propertyName);
        Method method = getBeanWriteMethod(klass, propertyName);
        checkMethod(method, "Write", propertyName);
        invoke(method, obj, value);
    }

    public static Object getBeanValue(Class klass, PropertyDescriptor propertyDescriptor, Object obj) throws ApplicationException {
        checkClass(klass);
        checkPropertyDescriptor(propertyDescriptor);
        Method method = propertyDescriptor.getReadMethod();
        checkMethod(method, "Read", propertyDescriptor.getName());
        return invoke(method, obj);
    }

    public static void setBeanValue(Class klass, PropertyDescriptor propertyDescriptor, Object obj, Object value) throws ApplicationException {
        checkClass(klass);
        checkPropertyDescriptor(propertyDescriptor);
        Method method = propertyDescriptor.getWriteMethod();
        checkMethod(method, "Write", propertyDescriptor.getName());
        invoke(method, obj, value);
    }

    public static Object getBeanValue(Method method, Object obj) throws ApplicationException {
        checkMethod(method);
        return invoke(method, obj);
    }

    public static void setBeanValue(Method method, Object obj, Object value) throws ApplicationException {
        checkMethod(method);
        invoke(method, obj, value);
    }

    public static Method getMethod(Class klass, String methodName, Class... parameterTypes) throws ApplicationException {
        checkClass(klass);
        try {
            return klass.getMethod(methodName, parameterTypes);
        } catch (Exception ex) {
            throw new ApplicationException(ex);
        }
    }

    public static Object invoke(Method method, Object obj, Object... args) throws ApplicationException {
        checkMethod(method);
        try {
            return method.invoke(obj, args);
        } catch (Exception ex) {
            throw new ApplicationException(ex);
        }
    }

    public static Object invoke(Class klass, String methodName, Class[] parameterTypes, Object obj, Object... args) throws ApplicationException {
        Method method = getMethod(klass, methodName, parameterTypes);
        return invoke(method, obj, args);
    }

    public static Object invoke(Class klass, String methodName, Class parameterType, Object obj, Object... args) throws ApplicationException {
        return invoke(klass, methodName, new Class[] { parameterType }, obj, args);
    }

    private static void checkClass(Class klass) throws ApplicationException {
        if (klass == null) {
            throw new ApplicationException("Class must be not null");
        }
    }

    private static void checkMethod(Method method) throws ApplicationException {
        if (method == null) {
            throw new ApplicationException("Method must be not null");
        }
    }

    private static void checkMethod(Method method, String type, String propertyName) throws ApplicationException {
        if (method == null) {
            throw new ApplicationException(type + " method of property [" + propertyName + "] not found");
        }
    }

    private static void checkPropertyName(String propertyName) throws ApplicationException {
        if (propertyName == null) {
            throw new ApplicationException("Property Name must be not null");
        }
    }

    private static void checkPropertyDescriptor(PropertyDescriptor propertyDescriptor) throws ApplicationException {
        if (propertyDescriptor == null) {
            throw new ApplicationException("Property Descriptor must be not null");
        }
    }

    private static void checkPropertyDescriptor(PropertyDescriptor propertyDescriptor, String propertyName) throws ApplicationException {
        if (propertyDescriptor == null) {
            throw new ApplicationException("Property Descriptor of property [" + propertyName + "] not found");
        }
    }

    public static boolean isBoolean(Class klass) {
        if (klass == null) {
            return false;
        }
        return (klass.equals(Boolean.class) || klass.equals(Boolean.TYPE));
    }

    public static boolean isNumber(Class klass) {
        if (klass == null) {
            return false;
        }
        return isClass(klass, Number.class) || isPrimitiveNumber(klass);
    }

    public static boolean isDecimalNumber(Class klass) {
        if (klass == null) {
            return false;
        }
        return isClass(klass, Float.class) || isClass(klass, Double.class) || isClass(klass, BigDecimal.class) || isDecimalPrimitiveNumber(klass);
    }

    public static boolean isIntegerNumber(Class klass) {
        if (klass == null) {
            return false;
        }
        return isClass(klass, Byte.class) || isClass(klass, Short.class) || isClass(klass, Integer.class) || isClass(klass, Long.class) || isClass(klass, BigInteger.class) || isClass(klass, AtomicInteger.class) || isClass(klass, AtomicLong.class) || isIntegerPrimitiveNumber(klass);
    }

    public static boolean isPrimitiveNumber(Class klass) {
        if (klass == null) {
            return false;
        }
        return (isClass(klass, Byte.TYPE) || isClass(klass, Short.TYPE) || isClass(klass, Integer.TYPE) || isClass(klass, Long.TYPE) || isClass(klass, Float.TYPE) || isClass(klass, Double.TYPE));
    }

    public static boolean isIntegerPrimitiveNumber(Class klass) {
        if (klass == null) {
            return false;
        }
        return (isClass(klass, Byte.TYPE) || isClass(klass, Short.TYPE) || isClass(klass, Integer.TYPE) || isClass(klass, Long.TYPE));
    }

    public static boolean isDecimalPrimitiveNumber(Class klass) {
        if (klass == null) {
            return false;
        }
        return (isClass(klass, Float.TYPE) || isClass(klass, Double.TYPE));
    }

    public static boolean isString(Class klass) {
        return isClass(klass, String.class);
    }

    public static boolean isDate(Class klass) {
        return isClass(klass, Date.class);
    }

    public static boolean isTime(Class klass) {
        return isClass(klass, Time.class);
    }

    public static boolean isTimestamp(Class klass) {
        return isClass(klass, Timestamp.class);
    }

    public static boolean isClass(Class klass, Class superClass) {
        if (klass == null || superClass == null) {
            return false;
        }
        return superClass.isAssignableFrom(klass);
    }

    public static ClassLoader getExternalLoader() {
        return externalLoader;
    }

    public static void setExternalLoader(ClassLoader externalLoader) {
        ClassUtils.externalLoader = externalLoader;
    }
}
