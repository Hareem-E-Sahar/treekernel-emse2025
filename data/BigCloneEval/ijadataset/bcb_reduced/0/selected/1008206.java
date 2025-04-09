package bueu.bexl.utils;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import bueu.bexl.BexlException;

/**
 * Solo un nivel de lista
 * 
 * @author javierfri
 *
 */
public class Property {

    private static final Map<String, Object> INSTANCES = new HashMap<String, Object>();

    private final String name;

    private final Method get;

    private final Method set;

    private final Class<?> type;

    private final Converter converter;

    private final Class<?> componentType;

    private final boolean simple;

    protected Property(PropertyDescriptor pd) throws ClassNotFoundException {
        this.name = pd.getName();
        this.get = pd.getReadMethod();
        this.set = pd.getWriteMethod();
        this.type = pd.getPropertyType();
        if (this.type.isArray()) {
            this.componentType = this.type.getComponentType();
            this.converter = ConverterRepository.INSTANCE.find(this.componentType);
        } else {
            if (Collection.class.isAssignableFrom(this.type)) {
                String type = this.get.getGenericReturnType().toString();
                int index = type.indexOf('<');
                if (index != -1) {
                    String className = type.substring(index + 1, type.length() - 1);
                    this.componentType = Class.forName(className);
                    this.converter = ConverterRepository.INSTANCE.find(this.componentType);
                } else {
                    this.componentType = null;
                    this.converter = null;
                }
            } else {
                this.converter = ConverterRepository.INSTANCE.find(this.type);
                this.componentType = null;
            }
        }
        this.simple = TypeUtils.isSimple(this.type);
    }

    public final String getName() {
        return this.name;
    }

    public final Class<?> getType() {
        return this.type;
    }

    public final Class<?> getComponentType() {
        return this.componentType;
    }

    @SuppressWarnings("unchecked")
    public void set(final Object bean, final Integer index, final Object value) {
        try {
            if (index == null) {
                this.set.invoke(bean, convert(this.type, this.componentType, value));
            } else {
                Object array = get(bean, index, true, false);
                if (array instanceof List) {
                    ((List<Object>) array).set(index, convert(this.componentType, null, value));
                } else {
                    Array.set(array, index, value);
                }
            }
        } catch (IllegalArgumentException e) {
            throw new BexlException(e);
        } catch (IllegalAccessException e) {
            throw new BexlException(e);
        } catch (InvocationTargetException e) {
            throw new BexlException(e);
        } catch (SecurityException e) {
            throw new BexlException(e);
        }
    }

    public void set(final Object bean, final Object value) {
        try {
            this.set.invoke(bean, convert(this.type, this.componentType, value));
        } catch (IllegalArgumentException e) {
            throw new BexlException(e);
        } catch (IllegalAccessException e) {
            throw new BexlException(e);
        } catch (InvocationTargetException e) {
            throw new BexlException(e);
        } catch (SecurityException e) {
            throw new BexlException(e);
        }
    }

    public boolean isSimple() {
        return this.simple;
    }

    public Object get(final Object bean) {
        return get(bean, null, false, true);
    }

    public Object get(final Object bean, final Integer index, boolean createIfNeeded) {
        return get(bean, index, createIfNeeded, true);
    }

    @SuppressWarnings("unchecked")
    public Object get(final Object bean, final Integer index, boolean createIfNeeded, boolean next) {
        try {
            Object value = this.get.invoke(bean);
            if (value == null && createIfNeeded) {
                value = create(this.type, this.componentType, index);
                if (value != null) {
                    this.set.invoke(bean, value);
                }
            }
            if (value != null && index != null) {
                if (value instanceof List) {
                    List<Object> list = (List<Object>) value;
                    final int size = list.size();
                    if (size > index) {
                        value = list.get(index);
                    } else {
                        value = null;
                        if (createIfNeeded) {
                            for (int i = size; i < index + 1; i++) {
                                list.add(null);
                            }
                        }
                    }
                    if (value == null && createIfNeeded) {
                        if (next) {
                            value = createComponent();
                            list.set(index, value);
                        } else {
                            value = list;
                        }
                    }
                } else if (value.getClass().isArray()) {
                    final int length = Array.getLength(value);
                    Object array = value;
                    value = length > index ? Array.get(value, index) : null;
                    if (value == null && createIfNeeded) {
                        if (length <= index) {
                            Object newArray = Array.newInstance(array.getClass().getComponentType(), index + 1);
                            System.arraycopy(array, 0, newArray, 0, length);
                            array = newArray;
                            this.set.invoke(bean, array);
                        }
                        if (next) {
                            value = createComponent();
                            Array.set(array, index, value);
                        }
                    }
                }
            }
            return value;
        } catch (IllegalArgumentException e) {
            throw new BexlException(e);
        } catch (IllegalAccessException e) {
            throw new BexlException(e);
        } catch (InvocationTargetException e) {
            throw new BexlException(e);
        } catch (SecurityException e) {
            throw new BexlException(e);
        }
    }

    private final Object createComponent() {
        final Class<?> type = this.componentType;
        if (!type.isInterface() && ((type.getModifiers() & Modifier.ABSTRACT) == 0)) {
            try {
                return type.newInstance();
            } catch (Exception ex) {
                return null;
            }
        }
        return null;
    }

    private final Object create(Class<?> type, Class<?> componentType, final Integer index) {
        if (type.isArray()) {
            return Array.newInstance(componentType, index == null ? 0 : index + 1);
        }
        if (type == List.class || type == Collection.class) {
            return new ArrayList<Object>();
        }
        if (type == Set.class) {
            return new HashSet<Object>();
        }
        if (!type.isInterface() && ((type.getModifiers() & Modifier.ABSTRACT) == 0)) {
            try {
                return type.newInstance();
            } catch (Exception ex) {
                return null;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private final Object convert(final Class<?> toType, final Class<?> componentType, final Object value) {
        if (toType == Object.class) {
            return value;
        }
        if (value == null) {
            if (toType.isPrimitive()) {
                return ((ConverterRepository.PrimitiveConverter) this.converter).zero;
            }
            return null;
        }
        final Class<?> fromType = value.getClass();
        if (fromType == toType || toType.isAssignableFrom(fromType)) {
            return value;
        }
        if (fromType.isArray()) {
            final int length = Array.getLength(value);
            if (toType.isArray()) {
                final Object arrayRes = Array.newInstance(componentType, length);
                for (int i = 0; i < length; i++) {
                    Array.set(arrayRes, i, convert(componentType, null, Array.get(value, i)));
                }
                return arrayRes;
            }
            if (Collection.class.isAssignableFrom(toType)) {
                Collection<Object> collection = (Collection<Object>) create(toType, componentType, 0);
                for (int i = 0; i < length; i++) {
                    collection.add(convert(componentType, null, Array.get(value, i)));
                }
                return collection;
            }
            if (length > 0) {
                return this.converter.convert(toType, Array.get(value, 0));
            }
            return null;
        }
        if (value instanceof Collection) {
            final Object[] array = ((Collection<?>) value).toArray();
            final int length = array.length;
            if (toType.isArray()) {
                final Object arrayRes = Array.newInstance(componentType, length);
                for (int i = 0; i < length; i++) {
                    Array.set(arrayRes, i, convert(componentType, null, array[i]));
                }
                return arrayRes;
            }
            if (Collection.class.isAssignableFrom(toType)) {
                Collection<Object> collection = (Collection<Object>) create(toType, componentType, 0);
                for (int i = 0; i < length; i++) {
                    collection.add(convert(componentType, null, array[i]));
                }
                return collection;
            }
            if (length > 0) {
                return this.converter.convert(toType, array[0]);
            }
            return null;
        }
        if (toType.isArray()) {
            final Object arrayRes = Array.newInstance(componentType, 1);
            Array.set(arrayRes, 0, convert(componentType, null, value));
            return arrayRes;
        }
        if (Collection.class.isAssignableFrom(toType)) {
            Collection<Object> collection = (Collection<Object>) create(toType, componentType, 0);
            collection.add(convert(componentType, null, value));
            return collection;
        }
        if (toType == String.class) {
            return value.toString();
        }
        if (this.converter != null) {
            return this.converter.convert(toType, value);
        }
        return value;
    }

    public static final Property get(Class<?> type, String property) throws BexlException {
        final StringBuilder sb = new StringBuilder(type.getName());
        sb.append('.');
        final String sufix = sb.toString();
        sb.append(property);
        final String key = sb.toString();
        Property prop = (Property) INSTANCES.get(key);
        if (prop == null) {
            if (INSTANCES.get(sufix) == null) {
                load(sufix, type);
                prop = (Property) INSTANCES.get(key);
            }
        }
        return prop;
    }

    private static final synchronized void load(String sufix, Class<?> type) {
        if (INSTANCES.get(sufix) == null) {
            try {
                for (PropertyDescriptor pd : Introspector.getBeanInfo(type).getPropertyDescriptors()) {
                    INSTANCES.put(sufix + pd.getName(), new Property(pd));
                }
                INSTANCES.put(sufix, Boolean.TRUE);
            } catch (IntrospectionException e) {
                throw new BexlException(e);
            } catch (ClassNotFoundException e) {
                throw new BexlException(e);
            }
        }
    }
}
