package org.springframework.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Default implementation of the BeanWrapper interface that should be sufficient
 * for all typical use cases. Caches introspection results for efficiency.
 *
 * <p>Note: This class never tries to load a class by name, as this can pose
 * class loading problems in J2EE applications with multiple deployment modules.
 * The caller is responsible for loading a target class.
 *
 * <p>Note: Auto-registers default property editors from the
 * <code>org.springframework.beans.propertyeditors</code> package, which apply
 * in addition to the JDK's standard PropertyEditors. Applications can call
 * the <code>registerCustomEditor</code> method to register an editor for a
 * particular instance (i.e. they're not shared across the application).
 * See the base class PropertyEditorRegistrySupport for details.
 *
 * <p>BeanWrapperImpl will convert collection and array values to the
 * corresponding target collections or arrays, if necessary. Custom property
 * editors that deal with collections or arrays can either be written via
 * PropertyEditor's <code>setValue</code>, or against a comma-delimited String
 * via <code>setAsText</code>, as String arrays are converted in such a format
 * if the array itself is not assignable.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 15 April 2001
 * @see #registerCustomEditor
 * @see #setPropertyValues
 * @see #setPropertyValue
 * @see #getPropertyValue
 * @see #getPropertyType
 * @see BeanWrapper
 * @see PropertyEditorRegistrySupport
 */
public class BeanWrapperImpl extends PropertyEditorRegistrySupport implements BeanWrapper {

    /**
	 * We'll create a lot of these objects, so we don't want a new logger every time
	 */
    private static final Log logger = LogFactory.getLog(BeanWrapperImpl.class);

    /** The wrapped object */
    private Object object;

    private String nestedPath = "";

    private Object rootObject;

    private boolean extractOldValueForEditor = false;

    /**
	 * Cached introspections results for this object, to prevent encountering
	 * the cost of JavaBeans introspection every time.
	 */
    private CachedIntrospectionResults cachedIntrospectionResults;

    /**
	 * Map with cached nested BeanWrappers: nested path -> BeanWrapper instance.
	 */
    private Map nestedBeanWrappers;

    /**
	 * Create new empty BeanWrapperImpl. Wrapped instance needs to be set afterwards.
	 * Registers default editors.
	 * @see #setWrappedInstance
	 */
    public BeanWrapperImpl() {
        this(true);
    }

    /**
	 * Create new empty BeanWrapperImpl. Wrapped instance needs to be set afterwards.
	 * @param registerDefaultEditors whether to register default editors
	 * (can be suppressed if the BeanWrapper won't need any type conversion)
	 * @see #setWrappedInstance
	 */
    public BeanWrapperImpl(boolean registerDefaultEditors) {
        if (registerDefaultEditors) {
            registerDefaultEditors();
        }
    }

    /**
	 * Create new BeanWrapperImpl for the given object.
	 * @param object object wrapped by this BeanWrapper
	 */
    public BeanWrapperImpl(Object object) {
        this();
        setWrappedInstance(object);
    }

    /**
	 * Create new BeanWrapperImpl, wrapping a new instance of the specified class.
	 * @param clazz class to instantiate and wrap
	 */
    public BeanWrapperImpl(Class clazz) {
        this();
        setWrappedInstance(BeanUtils.instantiateClass(clazz));
    }

    /**
	 * Create new BeanWrapperImpl for the given object,
	 * registering a nested path that the object is in.
	 * @param object object wrapped by this BeanWrapper
	 * @param nestedPath the nested path of the object
	 * @param rootObject the root object at the top of the path
	 */
    public BeanWrapperImpl(Object object, String nestedPath, Object rootObject) {
        this();
        setWrappedInstance(object, nestedPath, rootObject);
    }

    /**
	 * Create new BeanWrapperImpl for the given object,
	 * registering a nested path that the object is in.
	 * @param object object wrapped by this BeanWrapper
	 * @param nestedPath the nested path of the object
	 * @param superBw the containing BeanWrapper (must not be <code>null</code>)
	 */
    private BeanWrapperImpl(Object object, String nestedPath, BeanWrapperImpl superBw) {
        setWrappedInstance(object, nestedPath, superBw.getWrappedInstance());
    }

    /**
	 * Switch the target object, replacing the cached introspection results only
	 * if the class of the new object is different to that of the replaced object.
	 * @param object new target
	 */
    public void setWrappedInstance(Object object) {
        setWrappedInstance(object, "", null);
    }

    /**
	 * Switch the target object, replacing the cached introspection results only
	 * if the class of the new object is different to that of the replaced object.
	 * @param object new target
	 * @param nestedPath the nested path of the object
	 * @param rootObject the root object at the top of the path
	 */
    public void setWrappedInstance(Object object, String nestedPath, Object rootObject) {
        if (object == null) {
            throw new IllegalArgumentException("Cannot set BeanWrapperImpl target to a null object");
        }
        this.object = object;
        this.nestedPath = (nestedPath != null ? nestedPath : "");
        this.rootObject = (!"".equals(this.nestedPath) ? rootObject : object);
        this.nestedBeanWrappers = null;
        setIntrospectionClass(object.getClass());
    }

    public Object getWrappedInstance() {
        return this.object;
    }

    public Class getWrappedClass() {
        return this.object.getClass();
    }

    /**
	 * Return the nested path of the object wrapped by this BeanWrapper.
	 */
    public String getNestedPath() {
        return this.nestedPath;
    }

    /**
	 * Return the root object at the top of the path of this BeanWrapper.
	 * @see #getNestedPath
	 */
    public Object getRootInstance() {
        return this.rootObject;
    }

    /**
	 * Return the class of the root object at the top of the path of this BeanWrapper.
	 * @see #getNestedPath
	 */
    public Class getRootClass() {
        return (this.rootObject != null ? this.rootObject.getClass() : null);
    }

    /**
	 * Set the class to introspect.
	 * Needs to be called when the target object changes.
	 * @param clazz the class to introspect
	 */
    protected void setIntrospectionClass(Class clazz) {
        if (this.cachedIntrospectionResults == null || !this.cachedIntrospectionResults.getBeanClass().equals(clazz)) {
            this.cachedIntrospectionResults = CachedIntrospectionResults.forClass(clazz);
        }
    }

    public void setExtractOldValueForEditor(boolean extractOldValueForEditor) {
        this.extractOldValueForEditor = extractOldValueForEditor;
    }

    /**
	 * Get the last component of the path. Also works if not nested.
	 * @param bw BeanWrapper to work on
	 * @param nestedPath property path we know is nested
	 * @return last component of the path (the property on the target bean)
	 */
    private String getFinalPath(BeanWrapper bw, String nestedPath) {
        if (bw == this) {
            return nestedPath;
        }
        return nestedPath.substring(PropertyAccessorUtils.getLastNestedPropertySeparatorIndex(nestedPath) + 1);
    }

    /**
	 * Recursively navigate to return a BeanWrapper for the nested property path.
	 * @param propertyPath property property path, which may be nested
	 * @return a BeanWrapper for the target bean
	 */
    protected BeanWrapperImpl getBeanWrapperForPropertyPath(String propertyPath) throws BeansException {
        int pos = PropertyAccessorUtils.getFirstNestedPropertySeparatorIndex(propertyPath);
        if (pos > -1) {
            String nestedProperty = propertyPath.substring(0, pos);
            String nestedPath = propertyPath.substring(pos + 1);
            BeanWrapperImpl nestedBw = getNestedBeanWrapper(nestedProperty);
            return nestedBw.getBeanWrapperForPropertyPath(nestedPath);
        } else {
            return this;
        }
    }

    /**
	 * Retrieve a BeanWrapper for the given nested property.
	 * Create a new one if not found in the cache.
	 * <p>Note: Caching nested BeanWrappers is necessary now,
	 * to keep registered custom editors for nested properties.
	 * @param nestedProperty property to create the BeanWrapper for
	 * @return the BeanWrapper instance, either cached or newly created
	 */
    private BeanWrapperImpl getNestedBeanWrapper(String nestedProperty) throws BeansException {
        if (this.nestedBeanWrappers == null) {
            this.nestedBeanWrappers = new HashMap();
        }
        PropertyTokenHolder tokens = getPropertyNameTokens(nestedProperty);
        String canonicalName = tokens.canonicalName;
        Object propertyValue = getPropertyValue(tokens);
        if (propertyValue == null) {
            throw new NullValueInNestedPathException(getRootClass(), this.nestedPath + canonicalName);
        }
        BeanWrapperImpl nestedBw = (BeanWrapperImpl) this.nestedBeanWrappers.get(canonicalName);
        if (nestedBw == null || nestedBw.getWrappedInstance() != propertyValue) {
            if (logger.isDebugEnabled()) {
                logger.debug("Creating new nested BeanWrapper for property '" + canonicalName + "'");
            }
            nestedBw = newNestedBeanWrapper(propertyValue, this.nestedPath + canonicalName + NESTED_PROPERTY_SEPARATOR);
            copyDefaultEditorsTo(nestedBw);
            copyCustomEditorsTo(nestedBw, canonicalName);
            this.nestedBeanWrappers.put(canonicalName, nestedBw);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Using cached nested BeanWrapper for property '" + canonicalName + "'");
            }
        }
        return nestedBw;
    }

    /**
	 * Create a new nested BeanWrapper instance.
	 * <p>Default implementation creates a BeanWrapperImpl instance.
	 * Can be overridden in subclasses to create a BeanWrapperImpl subclass.
	 * @param object object wrapped by this BeanWrapper
	 * @param nestedPath the nested path of the object
	 * @return the nested BeanWrapper instance
	 * @see #BeanWrapperImpl(Object, String, BeanWrapperImpl)
	 */
    protected BeanWrapperImpl newNestedBeanWrapper(Object object, String nestedPath) {
        return new BeanWrapperImpl(object, nestedPath, this);
    }

    /**
	 * Parse the given property name into the corresponding property name tokens.
	 * @param propertyName the property name to parse
	 * @return representation of the parsed property tokens
	 */
    private PropertyTokenHolder getPropertyNameTokens(String propertyName) {
        PropertyTokenHolder tokens = new PropertyTokenHolder();
        String actualName = null;
        List keys = new ArrayList(2);
        int searchIndex = 0;
        while (searchIndex != -1) {
            int keyStart = propertyName.indexOf(PROPERTY_KEY_PREFIX, searchIndex);
            searchIndex = -1;
            if (keyStart != -1) {
                int keyEnd = propertyName.indexOf(PROPERTY_KEY_SUFFIX, keyStart + PROPERTY_KEY_PREFIX.length());
                if (keyEnd != -1) {
                    if (actualName == null) {
                        actualName = propertyName.substring(0, keyStart);
                    }
                    String key = propertyName.substring(keyStart + PROPERTY_KEY_PREFIX.length(), keyEnd);
                    if ((key.startsWith("'") && key.endsWith("'")) || (key.startsWith("\"") && key.endsWith("\""))) {
                        key = key.substring(1, key.length() - 1);
                    }
                    keys.add(key);
                    searchIndex = keyEnd + PROPERTY_KEY_SUFFIX.length();
                }
            }
        }
        tokens.actualName = (actualName != null ? actualName : propertyName);
        tokens.canonicalName = tokens.actualName;
        if (!keys.isEmpty()) {
            tokens.canonicalName += PROPERTY_KEY_PREFIX + StringUtils.collectionToDelimitedString(keys, PROPERTY_KEY_SUFFIX + PROPERTY_KEY_PREFIX) + PROPERTY_KEY_SUFFIX;
            tokens.keys = (String[]) keys.toArray(new String[keys.size()]);
        }
        return tokens;
    }

    public Object getPropertyValue(String propertyName) throws BeansException {
        BeanWrapperImpl nestedBw = getBeanWrapperForPropertyPath(propertyName);
        PropertyTokenHolder tokens = getPropertyNameTokens(getFinalPath(nestedBw, propertyName));
        return nestedBw.getPropertyValue(tokens);
    }

    private Object getPropertyValue(PropertyTokenHolder tokens) throws BeansException {
        String propertyName = tokens.canonicalName;
        String actualName = tokens.actualName;
        PropertyDescriptor pd = getPropertyDescriptorInternal(tokens.actualName);
        if (pd == null || pd.getReadMethod() == null) {
            throw new NotReadablePropertyException(getRootClass(), this.nestedPath + propertyName);
        }
        if (logger.isDebugEnabled()) logger.debug("About to invoke read method [" + pd.getReadMethod() + "] on object of class [" + this.object.getClass().getName() + "]");
        try {
            Object value = pd.getReadMethod().invoke(this.object, (Object[]) null);
            if (tokens.keys != null) {
                for (int i = 0; i < tokens.keys.length; i++) {
                    String key = tokens.keys[i];
                    if (value == null) {
                        throw new NullValueInNestedPathException(getRootClass(), this.nestedPath + propertyName, "Cannot access indexed value of property referenced in indexed " + "property path '" + propertyName + "': returned null");
                    } else if (value.getClass().isArray()) {
                        value = Array.get(value, Integer.parseInt(key));
                    } else if (value instanceof List) {
                        List list = (List) value;
                        value = list.get(Integer.parseInt(key));
                    } else if (value instanceof Set) {
                        Set set = (Set) value;
                        int index = Integer.parseInt(key);
                        if (index < 0 || index >= set.size()) {
                            throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName, "Cannot get element with index " + index + " from Set of size " + set.size() + ", accessed using property path '" + propertyName + "'");
                        }
                        Iterator it = set.iterator();
                        for (int j = 0; it.hasNext(); j++) {
                            Object elem = it.next();
                            if (j == index) {
                                value = elem;
                                break;
                            }
                        }
                    } else if (value instanceof Map) {
                        Map map = (Map) value;
                        value = map.get(key);
                    } else {
                        throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName, "Property referenced in indexed property path '" + propertyName + "' is neither an array nor a List nor a Set nor a Map; returned value was [" + value + "]");
                    }
                }
            }
            return value;
        } catch (InvocationTargetException ex) {
            throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName, "Getter for property '" + actualName + "' threw exception", ex);
        } catch (IllegalAccessException ex) {
            throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName, "Illegal attempt to get property '" + actualName + "' threw exception", ex);
        } catch (IndexOutOfBoundsException ex) {
            throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName, "Index of out of bounds in property path '" + propertyName + "'", ex);
        } catch (NumberFormatException ex) {
            throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName, "Invalid index in property path '" + propertyName + "'", ex);
        }
    }

    public void setPropertyValue(String propertyName, Object value) throws BeansException {
        BeanWrapperImpl nestedBw = null;
        try {
            nestedBw = getBeanWrapperForPropertyPath(propertyName);
        } catch (NotReadablePropertyException ex) {
            throw new NotWritablePropertyException(getRootClass(), this.nestedPath + propertyName, "Nested property in path '" + propertyName + "' does not exist", ex);
        }
        PropertyTokenHolder tokens = getPropertyNameTokens(getFinalPath(nestedBw, propertyName));
        nestedBw.setPropertyValue(tokens, value);
    }

    private void setPropertyValue(PropertyTokenHolder tokens, Object newValue) throws BeansException {
        String propertyName = tokens.canonicalName;
        if (tokens.keys != null) {
            PropertyTokenHolder getterTokens = new PropertyTokenHolder();
            getterTokens.canonicalName = tokens.canonicalName;
            getterTokens.actualName = tokens.actualName;
            getterTokens.keys = new String[tokens.keys.length - 1];
            System.arraycopy(tokens.keys, 0, getterTokens.keys, 0, tokens.keys.length - 1);
            Object propValue = null;
            try {
                propValue = getPropertyValue(getterTokens);
            } catch (NotReadablePropertyException ex) {
                throw new NotWritablePropertyException(getRootClass(), this.nestedPath + propertyName, "Cannot access indexed value in property referenced " + "in indexed property path '" + propertyName + "'", ex);
            }
            String key = tokens.keys[tokens.keys.length - 1];
            if (propValue == null) {
                throw new NullValueInNestedPathException(getRootClass(), this.nestedPath + propertyName, "Cannot access indexed value in property referenced " + "in indexed property path '" + propertyName + "': returned null");
            } else if (propValue.getClass().isArray()) {
                Class requiredType = propValue.getClass().getComponentType();
                int arrayIndex = Integer.parseInt(key);
                Object oldValue = null;
                try {
                    if (this.extractOldValueForEditor) {
                        oldValue = Array.get(propValue, arrayIndex);
                    }
                    Object convertedValue = doTypeConversionIfNecessary(propertyName, propertyName, oldValue, newValue, requiredType);
                    Array.set(propValue, Integer.parseInt(key), convertedValue);
                } catch (IllegalArgumentException ex) {
                    PropertyChangeEvent pce = new PropertyChangeEvent(this.rootObject, this.nestedPath + propertyName, oldValue, newValue);
                    throw new TypeMismatchException(pce, requiredType, ex);
                } catch (IndexOutOfBoundsException ex) {
                    throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName, "Invalid array index in property path '" + propertyName + "'", ex);
                }
            } else if (propValue instanceof List) {
                List list = (List) propValue;
                int index = Integer.parseInt(key);
                Object oldValue = null;
                if (this.extractOldValueForEditor && index < list.size()) {
                    oldValue = list.get(index);
                }
                Object convertedValue = doTypeConversionIfNecessary(propertyName, propertyName, oldValue, newValue, null);
                if (index < list.size()) {
                    list.set(index, convertedValue);
                } else if (index >= list.size()) {
                    for (int i = list.size(); i < index; i++) {
                        try {
                            list.add(null);
                        } catch (NullPointerException ex) {
                            throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName, "Cannot set element with index " + index + " in List of size " + list.size() + ", accessed using property path '" + propertyName + "': List does not support filling up gaps with null elements");
                        }
                    }
                    list.add(convertedValue);
                }
            } else if (propValue instanceof Map) {
                Map map = (Map) propValue;
                Object oldValue = null;
                if (this.extractOldValueForEditor) {
                    oldValue = map.get(key);
                }
                Object convertedValue = doTypeConversionIfNecessary(propertyName, propertyName, oldValue, newValue, null);
                map.put(key, convertedValue);
            } else {
                throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName, "Property referenced in indexed property path '" + propertyName + "' is neither an array nor a List nor a Map; returned value was [" + newValue + "]");
            }
        } else {
            PropertyDescriptor pd = getPropertyDescriptorInternal(propertyName);
            if (pd == null || pd.getWriteMethod() == null) {
                throw new NotWritablePropertyException(getRootClass(), this.nestedPath + propertyName);
            }
            Method readMethod = pd.getReadMethod();
            Method writeMethod = pd.getWriteMethod();
            Object oldValue = null;
            if (this.extractOldValueForEditor && readMethod != null) {
                try {
                    oldValue = readMethod.invoke(this.object, new Object[0]);
                } catch (Exception ex) {
                    logger.debug("Could not read previous value of property '" + this.nestedPath + propertyName, ex);
                }
            }
            try {
                Object convertedValue = doTypeConversionIfNecessary(propertyName, propertyName, oldValue, newValue, pd.getPropertyType());
                if (pd.getPropertyType().isPrimitive() && (convertedValue == null || "".equals(convertedValue))) {
                    throw new IllegalArgumentException("Invalid value [" + newValue + "] for property '" + pd.getName() + "' of primitive type [" + pd.getPropertyType() + "]");
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("About to invoke write method [" + writeMethod + "] on object of class [" + this.object.getClass().getName() + "]");
                }
                writeMethod.invoke(this.object, new Object[] { convertedValue });
                if (logger.isDebugEnabled()) {
                    logger.debug("Invoked write method [" + writeMethod + "] with value of type [" + pd.getPropertyType().getName() + "]");
                }
            } catch (InvocationTargetException ex) {
                PropertyChangeEvent propertyChangeEvent = new PropertyChangeEvent(this.rootObject, this.nestedPath + propertyName, oldValue, newValue);
                if (ex.getTargetException() instanceof ClassCastException) {
                    throw new TypeMismatchException(propertyChangeEvent, pd.getPropertyType(), ex.getTargetException());
                } else {
                    throw new MethodInvocationException(propertyChangeEvent, ex.getTargetException());
                }
            } catch (IllegalArgumentException ex) {
                PropertyChangeEvent pce = new PropertyChangeEvent(this.rootObject, this.nestedPath + propertyName, oldValue, newValue);
                throw new TypeMismatchException(pce, pd.getPropertyType(), ex);
            } catch (IllegalAccessException ex) {
                PropertyChangeEvent pce = new PropertyChangeEvent(this.rootObject, this.nestedPath + propertyName, oldValue, newValue);
                throw new MethodInvocationException(pce, ex);
            }
        }
    }

    public void setPropertyValue(PropertyValue pv) throws BeansException {
        setPropertyValue(pv.getName(), pv.getValue());
    }

    /**
	 * Bulk update from a Map.
	 * Bulk updates from PropertyValues are more powerful: this method is
	 * provided for convenience.
	 * @param map map containing properties to set, as name-value pairs.
	 * The map may include nested properties.
	 * @throws BeansException if there's a fatal, low-level exception
	 */
    public void setPropertyValues(Map map) throws BeansException {
        setPropertyValues(new MutablePropertyValues(map));
    }

    public void setPropertyValues(PropertyValues pvs) throws BeansException {
        setPropertyValues(pvs, false);
    }

    public void setPropertyValues(PropertyValues propertyValues, boolean ignoreUnknown) throws BeansException {
        List propertyAccessExceptions = new ArrayList();
        PropertyValue[] pvs = propertyValues.getPropertyValues();
        for (int i = 0; i < pvs.length; i++) {
            try {
                setPropertyValue(pvs[i]);
            } catch (NotWritablePropertyException ex) {
                if (!ignoreUnknown) {
                    throw ex;
                }
            } catch (PropertyAccessException ex) {
                propertyAccessExceptions.add(ex);
            }
        }
        if (!propertyAccessExceptions.isEmpty()) {
            Object[] paeArray = propertyAccessExceptions.toArray(new PropertyAccessException[propertyAccessExceptions.size()]);
            throw new PropertyAccessExceptionsException(this, (PropertyAccessException[]) paeArray);
        }
    }

    private PropertyChangeEvent createPropertyChangeEvent(String propertyName, Object oldValue, Object newValue) {
        return new PropertyChangeEvent((this.rootObject != null ? this.rootObject : "constructor"), (propertyName != null ? this.nestedPath + propertyName : null), oldValue, newValue);
    }

    /**
	 * Convert the value to the required type (if necessary from a String).
	 * <p>Conversions from String to any type use the <code>setAsText</code> method
	 * of the PropertyEditor class. Note that a PropertyEditor must be registered
	 * for the given class for this to work; this is a standard JavaBeans API.
	 * A number of PropertyEditors are automatically registered by BeanWrapperImpl.
	 * @param newValue proposed change value
	 * @param requiredType the type we must convert to
	 * @return the new value, possibly the result of type conversion
	 * @throws TypeMismatchException if type conversion failed
	 * @see java.beans.PropertyEditor#setAsText(String)
	 * @see java.beans.PropertyEditor#getValue()
	 */
    public Object doTypeConversionIfNecessary(Object newValue, Class requiredType) throws TypeMismatchException {
        return doTypeConversionIfNecessary(null, null, null, newValue, requiredType);
    }

    /**
	 * Convert the value to the required type (if necessary from a String),
	 * for the specified property.
	 * @param propertyName name of the property
	 * @param oldValue previous value, if available (may be <code>null</code>)
	 * @param newValue proposed change value
	 * @param requiredType the type we must convert to
	 * (or <code>null</code> if not known, for example in case of a collection element)
	 * @return the new value, possibly the result of type conversion
	 * @throws TypeMismatchException if type conversion failed
	 */
    protected Object doTypeConversionIfNecessary(String propertyName, String fullPropertyName, Object oldValue, Object newValue, Class requiredType) throws TypeMismatchException {
        Object convertedValue = newValue;
        if (convertedValue != null) {
            PropertyEditor pe = findCustomEditor(requiredType, fullPropertyName);
            if (pe != null || (requiredType != null && (requiredType.isArray() || !requiredType.isAssignableFrom(convertedValue.getClass())))) {
                if (requiredType != null) {
                    if (pe == null) {
                        pe = (PropertyEditor) getDefaultEditor(requiredType);
                        if (pe == null) {
                            pe = PropertyEditorManager.findEditor(requiredType);
                        }
                    }
                }
                if (pe != null && !(convertedValue instanceof String)) {
                    try {
                        pe.setValue(convertedValue);
                        Object newConvertedValue = pe.getValue();
                        if (newConvertedValue != convertedValue) {
                            convertedValue = newConvertedValue;
                            pe = null;
                        }
                    } catch (IllegalArgumentException ex) {
                        throw new TypeMismatchException(createPropertyChangeEvent(fullPropertyName, oldValue, newValue), requiredType, ex);
                    }
                }
                if (requiredType != null && !requiredType.isArray() && convertedValue instanceof String[]) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Converting String array to comma-delimited String [" + convertedValue + "]");
                    }
                    convertedValue = StringUtils.arrayToCommaDelimitedString((String[]) convertedValue);
                }
                if (pe != null && convertedValue instanceof String) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Converting String to [" + requiredType + "] using property editor [" + pe + "]");
                    }
                    try {
                        pe.setValue(oldValue);
                        pe.setAsText((String) convertedValue);
                        convertedValue = pe.getValue();
                    } catch (IllegalArgumentException ex) {
                        throw new TypeMismatchException(createPropertyChangeEvent(fullPropertyName, oldValue, newValue), requiredType, ex);
                    }
                }
                if (requiredType != null) {
                    if (requiredType.isArray()) {
                        Class componentType = requiredType.getComponentType();
                        if (convertedValue instanceof Collection) {
                            Collection coll = (Collection) convertedValue;
                            Object result = Array.newInstance(componentType, coll.size());
                            int i = 0;
                            for (Iterator it = coll.iterator(); it.hasNext(); i++) {
                                Object value = doTypeConversionIfNecessary(propertyName, propertyName + PROPERTY_KEY_PREFIX + i + PROPERTY_KEY_SUFFIX, null, it.next(), componentType);
                                Array.set(result, i, value);
                            }
                            return result;
                        } else if (convertedValue != null && convertedValue.getClass().isArray()) {
                            int arrayLength = Array.getLength(convertedValue);
                            Object result = Array.newInstance(componentType, arrayLength);
                            for (int i = 0; i < arrayLength; i++) {
                                Object value = doTypeConversionIfNecessary(propertyName, propertyName + PROPERTY_KEY_PREFIX + i + PROPERTY_KEY_SUFFIX, null, Array.get(convertedValue, i), componentType);
                                Array.set(result, i, value);
                            }
                            return result;
                        } else {
                            Object result = Array.newInstance(componentType, 1);
                            Object value = doTypeConversionIfNecessary(propertyName, propertyName + PROPERTY_KEY_PREFIX + 0 + PROPERTY_KEY_SUFFIX, null, convertedValue, componentType);
                            Array.set(result, 0, value);
                            return result;
                        }
                    }
                    if (convertedValue != null && !requiredType.isPrimitive() && !requiredType.isAssignableFrom(convertedValue.getClass())) {
                        if (convertedValue instanceof String) {
                            try {
                                Field enumField = requiredType.getField((String) convertedValue);
                                return enumField.get(null);
                            } catch (Exception ex) {
                                logger.debug("Field [" + convertedValue + "] isn't an enum value", ex);
                            }
                        }
                        throw new TypeMismatchException(createPropertyChangeEvent(fullPropertyName, oldValue, newValue), requiredType);
                    }
                }
            }
        }
        return convertedValue;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        return this.cachedIntrospectionResults.getBeanInfo().getPropertyDescriptors();
    }

    public PropertyDescriptor getPropertyDescriptor(String propertyName) throws BeansException {
        if (propertyName == null) {
            throw new IllegalArgumentException("Can't find property descriptor for <code>null</code> property");
        }
        PropertyDescriptor pd = getPropertyDescriptorInternal(propertyName);
        if (pd != null) {
            return pd;
        } else {
            throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName, "No property '" + propertyName + "' found");
        }
    }

    /**
	 * Internal version of getPropertyDescriptor:
	 * Returns null if not found rather than throwing an exception.
	 */
    protected PropertyDescriptor getPropertyDescriptorInternal(String propertyName) throws BeansException {
        Assert.state(this.object != null, "BeanWrapper does not hold a bean instance");
        BeanWrapperImpl nestedBw = getBeanWrapperForPropertyPath(propertyName);
        return nestedBw.cachedIntrospectionResults.getPropertyDescriptor(getFinalPath(nestedBw, propertyName));
    }

    public boolean isReadableProperty(String propertyName) {
        if (propertyName == null) {
            throw new IllegalArgumentException("Can't find readability status for <code>null</code> property");
        }
        try {
            PropertyDescriptor pd = getPropertyDescriptorInternal(propertyName);
            if (pd != null) {
                if (pd.getReadMethod() != null) {
                    return true;
                }
            } else {
                getPropertyValue(propertyName);
                return true;
            }
        } catch (InvalidPropertyException ex) {
        }
        return false;
    }

    public boolean isWritableProperty(String propertyName) {
        if (propertyName == null) {
            throw new IllegalArgumentException("Can't find writability status for <code>null</code> property");
        }
        try {
            PropertyDescriptor pd = getPropertyDescriptorInternal(propertyName);
            if (pd != null) {
                if (pd.getWriteMethod() != null) {
                    return true;
                }
            } else {
                getPropertyValue(propertyName);
                return true;
            }
        } catch (InvalidPropertyException ex) {
        }
        return false;
    }

    public Class getPropertyType(String propertyName) throws BeansException {
        try {
            PropertyDescriptor pd = getPropertyDescriptorInternal(propertyName);
            if (pd != null) {
                return pd.getPropertyType();
            } else {
                Object value = getPropertyValue(propertyName);
                if (value != null) {
                    return value.getClass();
                }
                Class editorType = guessPropertyTypeFromEditors(propertyName);
                if (editorType != null) {
                    return editorType;
                }
            }
        } catch (InvalidPropertyException ex) {
        }
        return null;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("BeanWrapperImpl: wrapping class [");
        sb.append(getWrappedClass().getName()).append("]");
        return sb.toString();
    }

    private static class PropertyTokenHolder {

        private String canonicalName;

        private String actualName;

        private String[] keys;
    }
}
