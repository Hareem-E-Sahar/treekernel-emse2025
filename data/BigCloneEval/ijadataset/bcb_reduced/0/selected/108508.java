package es.caib.zkib.jxpath.util;

import java.beans.IndexedPropertyDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import es.caib.zkib.jxpath.Container;
import es.caib.zkib.jxpath.DynamicPropertyHandler;
import es.caib.zkib.jxpath.JXPathException;

/**
 * Collection and property access utilities.
 *
 * @author Dmitri Plotnikov
 * @version $Revision: 1.1 $ $Date: 2009-04-03 08:13:15 $
 */
public class ValueUtils {

    private static Map dynamicPropertyHandlerMap = new HashMap();

    private static final int UNKNOWN_LENGTH_MAX_COUNT = 16000;

    /**
     * Returns true if the object is an array or a Collection.
     * @param value to test
     * @return boolean
     */
    public static boolean isCollection(Object value) {
        value = getValue(value);
        if (value == null) {
            return false;
        }
        if (value.getClass().isArray()) {
            return true;
        }
        if (value instanceof Collection) {
            return true;
        }
        return false;
    }

    /**
     * Returns 1 if the type is a collection,
     * -1 if it is definitely not
     * and 0 if it may be a collection in some cases.
     * @param clazz to test
     * @return int
     */
    public static int getCollectionHint(Class clazz) {
        if (clazz.isArray()) {
            return 1;
        }
        if (Collection.class.isAssignableFrom(clazz)) {
            return 1;
        }
        if (clazz.isPrimitive()) {
            return -1;
        }
        if (clazz.isInterface()) {
            return 0;
        }
        if (Modifier.isFinal(clazz.getModifiers())) {
            return -1;
        }
        return 0;
    }

    /**
     * If there is a regular non-indexed read method for this property,
     * uses this method to obtain the collection and then returns its
     * length.
     * Otherwise, attempts to guess the length of the collection by
     * calling the indexed get method repeatedly.  The method is supposed
     * to throw an exception if the index is out of bounds.
     * @param object collection
     * @param pd IndexedPropertyDescriptor
     * @return int
     */
    public static int getIndexedPropertyLength(Object object, IndexedPropertyDescriptor pd) {
        if (pd.getReadMethod() != null) {
            return getLength(getValue(object, pd));
        }
        Method readMethod = pd.getIndexedReadMethod();
        if (readMethod == null) {
            throw new JXPathException("No indexed read method for property " + pd.getName());
        }
        for (int i = 0; i < UNKNOWN_LENGTH_MAX_COUNT; i++) {
            try {
                readMethod.invoke(object, new Object[] { new Integer(i) });
            } catch (Throwable t) {
                return i;
            }
        }
        throw new JXPathException("Cannot determine the length of the indexed property " + pd.getName());
    }

    /**
     * Returns the length of the supplied collection. If the supplied object
     * is not a collection, returns 1. If collection is null, returns 0.
     * @param collection to check
     * @return int
     */
    public static int getLength(Object collection) {
        if (collection == null) {
            return 0;
        }
        collection = getValue(collection);
        if (collection.getClass().isArray()) {
            return Array.getLength(collection);
        }
        if (collection instanceof Collection) {
            return ((Collection) collection).size();
        }
        return 1;
    }

    /**
     * Returns an iterator for the supplied collection. If the argument
     * is null, returns an empty iterator. If the argument is not
     * a collection, returns an iterator that produces just that one object.
     * @param collection to iterate
     * @return Iterator
     */
    public static Iterator iterate(Object collection) {
        if (collection == null) {
            return Collections.EMPTY_LIST.iterator();
        }
        if (collection.getClass().isArray()) {
            int length = Array.getLength(collection);
            if (length == 0) {
                return Collections.EMPTY_LIST.iterator();
            }
            ArrayList list = new ArrayList();
            for (int i = 0; i < length; i++) {
                list.add(Array.get(collection, i));
            }
            return list.iterator();
        }
        if (collection instanceof Collection) {
            return ((Collection) collection).iterator();
        }
        return Collections.singletonList(collection).iterator();
    }

    /**
     * Grows the collection if necessary to the specified size. Returns
     * the new, expanded collection.
     * @param collection to expand
     * @param size desired size
     * @return collection or array
     */
    public static Object expandCollection(Object collection, int size) {
        if (collection == null) {
            return null;
        }
        if (size < getLength(collection)) {
            throw new JXPathException("adjustment of " + collection + " to size " + size + " is not an expansion");
        }
        if (collection.getClass().isArray()) {
            Object bigger = Array.newInstance(collection.getClass().getComponentType(), size);
            System.arraycopy(collection, 0, bigger, 0, Array.getLength(collection));
            return bigger;
        }
        if (collection instanceof Collection) {
            while (((Collection) collection).size() < size) {
                ((Collection) collection).add(null);
            }
            return collection;
        }
        throw new JXPathException("Cannot turn " + collection.getClass().getName() + " into a collection of size " + size);
    }

    /**
     * Remove the index'th element from the supplied collection.
     * @param collection to edit
     * @param index int
     * @return the resulting collection
     */
    public static Object remove(Object collection, int index) {
        collection = getValue(collection);
        if (collection == null) {
            return null;
        }
        if (index >= getLength(collection)) {
            throw new JXPathException("No such element at index " + index);
        }
        if (collection.getClass().isArray()) {
            int length = Array.getLength(collection);
            Object smaller = Array.newInstance(collection.getClass().getComponentType(), length - 1);
            if (index > 0) {
                System.arraycopy(collection, 0, smaller, 0, index);
            }
            if (index < length - 1) {
                System.arraycopy(collection, index + 1, smaller, index, length - index - 1);
            }
            return smaller;
        }
        if (collection instanceof List) {
            int size = ((List) collection).size();
            if (index < size) {
                ((List) collection).remove(index);
            }
            return collection;
        }
        if (collection instanceof Collection) {
            Iterator it = ((Collection) collection).iterator();
            for (int i = 0; i < index; i++) {
                if (!it.hasNext()) {
                    break;
                }
                it.next();
            }
            if (it.hasNext()) {
                it.next();
                it.remove();
            }
            return collection;
        }
        throw new JXPathException("Cannot remove " + collection.getClass().getName() + "[" + index + "]");
    }

    /**
     * Returns the index'th element of the supplied collection.
     * @param collection to read
     * @param index int
     * @return collection[index]
     */
    public static Object getValue(Object collection, int index) {
        collection = getValue(collection);
        Object value = collection;
        if (collection != null) {
            if (collection.getClass().isArray()) {
                if (index < 0 || index >= Array.getLength(collection)) {
                    return null;
                }
                value = Array.get(collection, index);
            } else if (collection instanceof List) {
                if (index < 0 || index >= ((List) collection).size()) {
                    return null;
                }
                value = ((List) collection).get(index);
            } else if (collection instanceof Collection) {
                int i = 0;
                Iterator it = ((Collection) collection).iterator();
                for (; i < index; i++) {
                    it.next();
                }
                if (it.hasNext()) {
                    value = it.next();
                } else {
                    value = null;
                }
            }
        }
        return value;
    }

    /**
     * Modifies the index'th element of the supplied collection.
     * Converts the value to the required type if necessary.
     * @param collection to edit
     * @param index to replace
     * @param value new value
     */
    public static void setValue(Object collection, int index, Object value) {
        collection = getValue(collection);
        if (collection != null) {
            if (collection.getClass().isArray()) {
                Array.set(collection, index, convert(value, collection.getClass().getComponentType()));
            } else if (collection instanceof List) {
                ((List) collection).set(index, value);
            } else if (collection instanceof Collection) {
                throw new UnsupportedOperationException("Cannot set value of an element of a " + collection.getClass().getName());
            }
        }
    }

    /**
     * Returns the value of the bean's property represented by
     * the supplied property descriptor.
     * @param bean to read
     * @param propertyDescriptor indicating what to read
     * @return Object value
     */
    public static Object getValue(Object bean, PropertyDescriptor propertyDescriptor) {
        Object value;
        try {
            Method method = getAccessibleMethod(propertyDescriptor.getReadMethod());
            if (method == null) {
                throw new JXPathException("No read method");
            }
            value = method.invoke(bean, new Object[0]);
        } catch (Exception ex) {
            throw new JXPathException("Cannot access property: " + (bean == null ? "null" : bean.getClass().getName()) + "." + propertyDescriptor.getName(), ex);
        }
        return value;
    }

    /**
     * Modifies the value of the bean's property represented by
     * the supplied property descriptor.
     * @param bean to read
     * @param propertyDescriptor indicating what to read
     * @param value to set
     */
    public static void setValue(Object bean, PropertyDescriptor propertyDescriptor, Object value) {
        try {
            Method method = getAccessibleMethod(propertyDescriptor.getWriteMethod());
            if (method == null) {
                throw new JXPathException("No write method");
            }
            value = convert(value, propertyDescriptor.getPropertyType());
            method.invoke(bean, new Object[] { value });
        } catch (Exception ex) {
            throw new JXPathException("Cannot modify property: " + (bean == null ? "null" : bean.getClass().getName()) + "." + propertyDescriptor.getName(), ex);
        }
    }

    /**
     * Convert value to type.
     * @param value Object
     * @param type destination
     * @return conversion result
     */
    private static Object convert(Object value, Class type) {
        try {
            return TypeUtils.convert(value, type);
        } catch (Exception ex) {
            throw new JXPathException("Cannot convert value of class " + (value == null ? "null" : value.getClass().getName()) + " to type " + type, ex);
        }
    }

    /**
     * Returns the index'th element of the bean's property represented by
     * the supplied property descriptor.
     * @param bean to read
     * @param propertyDescriptor indicating what to read
     * @param index int
     * @return Object
     */
    public static Object getValue(Object bean, PropertyDescriptor propertyDescriptor, int index) {
        if (propertyDescriptor instanceof IndexedPropertyDescriptor) {
            try {
                IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor) propertyDescriptor;
                Method method = ipd.getIndexedReadMethod();
                if (method != null) {
                    return method.invoke(bean, new Object[] { new Integer(index) });
                }
            } catch (InvocationTargetException ex) {
                Throwable t = ex.getTargetException();
                if (t instanceof IndexOutOfBoundsException) {
                    return null;
                }
                throw new JXPathException("Cannot access property: " + propertyDescriptor.getName(), t);
            } catch (Throwable ex) {
                throw new JXPathException("Cannot access property: " + propertyDescriptor.getName(), ex);
            }
        }
        return getValue(getValue(bean, propertyDescriptor), index);
    }

    /**
     * Modifies the index'th element of the bean's property represented by
     * the supplied property descriptor. Converts the value to the required
     * type if necessary.
     * @param bean to edit
     * @param propertyDescriptor indicating what to set
     * @param index int
     * @param value to set
     */
    public static void setValue(Object bean, PropertyDescriptor propertyDescriptor, int index, Object value) {
        if (propertyDescriptor instanceof IndexedPropertyDescriptor) {
            try {
                IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor) propertyDescriptor;
                Method method = ipd.getIndexedWriteMethod();
                if (method != null) {
                    method.invoke(bean, new Object[] { new Integer(index), convert(value, ipd.getIndexedPropertyType()) });
                    return;
                }
            } catch (Exception ex) {
                throw new RuntimeException("Cannot access property: " + propertyDescriptor.getName() + ", " + ex.getMessage());
            }
        }
        Object collection = getValue(bean, propertyDescriptor);
        if (isCollection(collection)) {
            setValue(collection, index, value);
        } else if (index == 0) {
            setValue(bean, propertyDescriptor, value);
        } else {
            throw new RuntimeException("Not a collection: " + propertyDescriptor.getName());
        }
    }

    /**
     * If the parameter is a container, opens the container and
     * return the contents.  The method is recursive.
     * @param object to read
     * @return Object
     */
    public static Object getValue(Object object) {
        while (object instanceof Container) {
            object = ((Container) object).getValue();
        }
        return object;
    }

    /**
     * Returns a shared instance of the dynamic property handler class
     * returned by <code>getDynamicPropertyHandlerClass()</code>.
     * @param clazz to handle
     * @return DynamicPropertyHandler
     */
    public static DynamicPropertyHandler getDynamicPropertyHandler(Class clazz) {
        DynamicPropertyHandler handler = (DynamicPropertyHandler) dynamicPropertyHandlerMap.get(clazz);
        if (handler == null) {
            try {
                handler = (DynamicPropertyHandler) clazz.newInstance();
            } catch (Exception ex) {
                throw new JXPathException("Cannot allocate dynamic property handler of class " + clazz.getName(), ex);
            }
            dynamicPropertyHandlerMap.put(clazz, handler);
        }
        return handler;
    }

    /**
     * Return an accessible method (that is, one that can be invoked via
     * reflection) that implements the specified Method.  If no such method
     * can be found, return <code>null</code>.
     *
     * @param method The method that we wish to call
     * @return Method
     */
    public static Method getAccessibleMethod(Method method) {
        if (method == null) {
            return (null);
        }
        if (!Modifier.isPublic(method.getModifiers())) {
            return (null);
        }
        Class clazz = method.getDeclaringClass();
        if (Modifier.isPublic(clazz.getModifiers())) {
            return (method);
        }
        String name = method.getName();
        Class[] parameterTypes = method.getParameterTypes();
        while (clazz != null) {
            Method aMethod = getAccessibleMethodFromInterfaceNest(clazz, name, parameterTypes);
            if (aMethod != null) {
                return aMethod;
            }
            clazz = clazz.getSuperclass();
            if (clazz != null && Modifier.isPublic(clazz.getModifiers())) {
                try {
                    return clazz.getDeclaredMethod(name, parameterTypes);
                } catch (NoSuchMethodException e) {
                }
            }
        }
        return null;
    }

    /**
     * Return an accessible method (that is, one that can be invoked via
     * reflection) that implements the specified method, by scanning through
     * all implemented interfaces and subinterfaces.  If no such Method
     * can be found, return <code>null</code>.
     *
     * @param clazz Parent class for the interfaces to be checked
     * @param methodName Method name of the method we wish to call
     * @param parameterTypes The parameter type signatures
     * @return Method
     */
    private static Method getAccessibleMethodFromInterfaceNest(Class clazz, String methodName, Class[] parameterTypes) {
        Method method = null;
        Class[] interfaces = clazz.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (!Modifier.isPublic(interfaces[i].getModifiers())) {
                continue;
            }
            try {
                method = interfaces[i].getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
            }
            if (method != null) {
                break;
            }
            method = getAccessibleMethodFromInterfaceNest(interfaces[i], methodName, parameterTypes);
            if (method != null) {
                break;
            }
        }
        return (method);
    }
}
