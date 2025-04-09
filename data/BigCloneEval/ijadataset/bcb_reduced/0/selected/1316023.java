package org.nextframework.bean;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.WeakHashMap;
import javax.persistence.Id;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LazyInitializationException;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.nextframework.core.standard.Next;
import org.nextframework.exception.NotParameterizedTypeException;
import org.nextframework.persistence.QueryBuilder;
import org.nextframework.util.ReflectionCache;
import org.nextframework.util.ReflectionCacheFactory;
import org.nextframework.util.Util;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.MethodInvocationException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.NotReadablePropertyException;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.beans.NullValueInNestedPathException;
import org.springframework.beans.PropertyAccessException;
import org.springframework.beans.PropertyBatchUpdateException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.propertyeditors.ByteArrayPropertyEditor;
import org.springframework.beans.propertyeditors.CharacterEditor;
import org.springframework.beans.propertyeditors.ClassEditor;
import org.springframework.beans.propertyeditors.CustomBooleanEditor;
import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.beans.propertyeditors.FileEditor;
import org.springframework.beans.propertyeditors.InputStreamEditor;
import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.beans.propertyeditors.PropertiesEditor;
import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.beans.propertyeditors.URLEditor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceArrayPropertyEditor;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.util.StringUtils;

/**
 * C�pia do beanWrapper do Spring
 * @author rogelgarcia
 * @since 23/01/2006
 * @version 1.1
 */
@SuppressWarnings("unchecked")
public class PropertyDescriptorBeanWrapper {

    /**
	 * Path separator for nested properties.
	 * Follows normal Java conventions: getFoo().getBar() would be "foo.bar".
	 */
    static final String NESTED_PROPERTY_SEPARATOR = ".";

    static final char NESTED_PROPERTY_SEPARATOR_CHAR = '.';

    /**
	 * Marker that indicates the start of a property key for an
	 * indexed or mapped property like "person.addresses[0]".
	 */
    static final String PROPERTY_KEY_PREFIX = "[";

    static final char PROPERTY_KEY_PREFIX_CHAR = '[';

    /**
	 * Marker that indicates the end of a property key for an
	 * indexed or mapped property like "person.addresses[0]".
	 */
    static final String PROPERTY_KEY_SUFFIX = "]";

    static final char PROPERTY_KEY_SUFFIX_CHAR = ']';

    /**
	 * We'll create a lot of these objects, so we don't want a new logger every time
	 */
    private static final Log logger = LogFactory.getLog(PropertyDescriptorBeanWrapper.class);

    /**
	 * The wrapped object
	 */
    private PropertyInfo propertyInfo;

    private String nestedPath = "";

    private PropertyInfo rootObject;

    private boolean extractOldValueForEditor = false;

    private final Map defaultEditors;

    private Map customEditors;

    private IndexValueResolver indexValueResolver;

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
	 * Create new empty PropertyDescriptorBeanWrapper. Wrapped instance needs to be set afterwards.
	 * Registers default editors.
	 * @see #setWrappedInstance
	 */
    public PropertyDescriptorBeanWrapper() {
        this(true);
    }

    /**
	 * Create new empty PropertyDescriptorBeanWrapper. Wrapped instance needs to be set afterwards.
	 * @param registerDefaultEditors whether to register default editors
	 * (can be suppressed if the BeanWrapper won't need any type conversion)
	 * @see #setWrappedInstance
	 */
    public PropertyDescriptorBeanWrapper(boolean registerDefaultEditors) {
        if (registerDefaultEditors) {
            this.defaultEditors = new HashMap(32);
            registerDefaultEditors();
        } else {
            this.defaultEditors = Collections.EMPTY_MAP;
        }
    }

    /**
	 * Create new PropertyDescriptorBeanWrapper for the given object.
	 * @param object object wrapped by this BeanWrapper
	 */
    public PropertyDescriptorBeanWrapper(PropertyInfo object) {
        this();
        setWrappedInstance(object);
    }

    /**
	 * Create new PropertyDescriptorBeanWrapper, wrapping a new instance of the specified class.
	 * @param clazz class to instantiate and wrap
	 */
    public PropertyDescriptorBeanWrapper(Class clazz) {
        this();
        Object instantiateClass = BeanUtils.instantiateClass(clazz);
        PropertyInfo info = new PropertyInfo();
        info.type = clazz;
        info.clazz = clazz;
        info.value = instantiateClass;
        setWrappedInstance(info);
    }

    /**
	 * Create new PropertyDescriptorBeanWrapper, wrapping a new instance of the specified class.
	 * @param clazz class to instantiate and wrap
	 */
    public PropertyDescriptorBeanWrapper(Class clazz, Object wrappedInstance) {
        this();
        if (clazz == null) {
            throw new NullPointerException("Classe nula ao criar propertyDescriptorBeanWrapper");
        }
        PropertyInfo info = new PropertyInfo();
        info.type = clazz;
        info.clazz = clazz;
        info.value = wrappedInstance;
        setWrappedInstance(info, "", null);
    }

    /**
	 * Create new PropertyDescriptorBeanWrapper for the given object,
	 * registering a nested path that the object is in.
	 * @param object object wrapped by this BeanWrapper.
	 * @param nestedPath the nested path of the object
	 * @param rootPropertyInfo the root object at the top of the path
	 */
    public PropertyDescriptorBeanWrapper(PropertyInfo object, String nestedPath, PropertyInfo rootPropertyInfo) {
        this();
        setWrappedInstance(object, nestedPath, rootPropertyInfo);
    }

    /**
	 * Create new PropertyDescriptorBeanWrapper for the given object,
	 * registering a nested path that the object is in.
	 * @param object object wrapped by this BeanWrapper.
	 * @param nestedPath the nested path of the object
	 * @param superBw the containing BeanWrapper (must not be <code>null</code>)
	 */
    private PropertyDescriptorBeanWrapper(PropertyInfo object, String nestedPath, PropertyDescriptorBeanWrapper superBw) {
        this.defaultEditors = superBw.defaultEditors;
        setWrappedInstance(object, nestedPath, superBw.propertyInfo);
    }

    /**
	 * Register default editors in this class, for restricted environments.
	 * We're not using the JRE's PropertyEditorManager to avoid potential
	 * SecurityExceptions when running in a SecurityManager.
	 * <p>Registers a <code>CustomNumberEditor</code> for all primitive number types,
	 * their corresponding wrapper types, <code>BigInteger</code> and <code>BigDecimal</code>.
	 */
    private void registerDefaultEditors() {
        this.defaultEditors.put(byte[].class, new ByteArrayPropertyEditor());
        this.defaultEditors.put(Class.class, new ClassEditor());
        this.defaultEditors.put(File.class, new FileEditor());
        this.defaultEditors.put(InputStream.class, new InputStreamEditor());
        this.defaultEditors.put(Locale.class, new LocaleEditor());
        this.defaultEditors.put(Properties.class, new PropertiesEditor());
        this.defaultEditors.put(Resource[].class, new ResourceArrayPropertyEditor());
        this.defaultEditors.put(String[].class, new StringArrayPropertyEditor());
        this.defaultEditors.put(URL.class, new URLEditor());
        this.defaultEditors.put(Collection.class, new CustomCollectionEditor(Collection.class));
        this.defaultEditors.put(Set.class, new CustomCollectionEditor(Set.class));
        this.defaultEditors.put(SortedSet.class, new CustomCollectionEditor(SortedSet.class));
        this.defaultEditors.put(List.class, new CustomCollectionEditor(List.class));
        PropertyEditor characterEditor = new CharacterEditor(false);
        PropertyEditor booleanEditor = new CustomBooleanEditor(false);
        this.defaultEditors.put(char.class, characterEditor);
        this.defaultEditors.put(Character.class, characterEditor);
        this.defaultEditors.put(boolean.class, booleanEditor);
        this.defaultEditors.put(Boolean.class, booleanEditor);
        PropertyEditor byteEditor = new CustomNumberEditor(Byte.class, false);
        PropertyEditor shortEditor = new CustomNumberEditor(Short.class, false);
        PropertyEditor integerEditor = new CustomNumberEditor(Integer.class, false);
        PropertyEditor longEditor = new CustomNumberEditor(Long.class, false);
        PropertyEditor floatEditor = new CustomNumberEditor(Float.class, false);
        PropertyEditor doubleEditor = new CustomNumberEditor(Double.class, false);
        this.defaultEditors.put(byte.class, byteEditor);
        this.defaultEditors.put(Byte.class, byteEditor);
        this.defaultEditors.put(short.class, shortEditor);
        this.defaultEditors.put(Short.class, shortEditor);
        this.defaultEditors.put(int.class, integerEditor);
        this.defaultEditors.put(Integer.class, integerEditor);
        this.defaultEditors.put(long.class, longEditor);
        this.defaultEditors.put(Long.class, longEditor);
        this.defaultEditors.put(float.class, floatEditor);
        this.defaultEditors.put(Float.class, floatEditor);
        this.defaultEditors.put(double.class, doubleEditor);
        this.defaultEditors.put(Double.class, doubleEditor);
        this.defaultEditors.put(BigDecimal.class, new CustomNumberEditor(BigDecimal.class, false));
        this.defaultEditors.put(BigInteger.class, new CustomNumberEditor(BigInteger.class, false));
    }

    /**
	 * Switch the target object, replacing the cached introspection results only
	 * if the class of the new object is different to that of the replaced object.
	 * @param object new target
	 */
    public void setWrappedInstance(Object object) {
        PropertyInfo info = new PropertyInfo();
        info.value = object;
        if (object == null) {
            throw new NullPointerException("PropertyDescriptorBeanWrapper n�o pode ser inicializado. Objeto nulo.");
        }
        info.clazz = object.getClass();
        info.type = object.getClass();
        setWrappedInstance(info, "", null);
    }

    /**
	 * Switch the target object, replacing the cached introspection results only
	 * if the class of the new object is different to that of the replaced object.
	 * @param propertyInfo new target
	 * @param nestedPath the nested path of the object
	 * @param rootPropertyInfo the root object at the top of the path
	 */
    public void setWrappedInstance(PropertyInfo propertyInfo, String nestedPath, PropertyInfo rootPropertyInfo) {
        if (propertyInfo == null) {
            throw new IllegalArgumentException("Cannot set PropertyDescriptorBeanWrapper target to a null object");
        }
        this.propertyInfo = propertyInfo;
        this.nestedPath = (nestedPath != null ? nestedPath : "");
        this.rootObject = (!"".equals(this.nestedPath) ? rootPropertyInfo : propertyInfo);
        this.nestedBeanWrappers = null;
        setIntrospectionClass(propertyInfo.clazz);
    }

    public Object getWrappedInstance() {
        return this.propertyInfo.value;
    }

    public PropertyInfo getWrappedPropertyInfo() {
        return this.propertyInfo;
    }

    public Class getWrappedClass() {
        return this.propertyInfo.clazz;
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
    public PropertyInfo getRootInstance() {
        return this.rootObject;
    }

    /**
	 * Return the class of the root object at the top of the path of this BeanWrapper.
	 * @see #getNestedPath
	 */
    public Class getRootClass() {
        return (this.rootObject != null ? this.rootObject.clazz : propertyInfo.clazz);
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

    public void registerCustomEditor(Class requiredType, PropertyEditor propertyEditor) {
        registerCustomEditor(requiredType, null, propertyEditor);
    }

    public void registerCustomEditor(Class requiredType, String propertyPath, PropertyEditor propertyEditor) {
        if (requiredType == null && propertyPath == null) {
            throw new IllegalArgumentException("Either requiredType or propertyPath is required");
        }
        if (this.customEditors == null) {
            this.customEditors = new HashMap();
        }
        if (propertyPath != null) {
            this.customEditors.put(propertyPath, new CustomEditorHolder(propertyEditor, requiredType));
        } else {
            this.customEditors.put(requiredType, propertyEditor);
        }
    }

    public PropertyEditor findCustomEditor(Class requiredType, String propertyPath) {
        if (this.customEditors == null) {
            return null;
        }
        if (propertyPath != null) {
            PropertyEditor editor = getCustomEditor(propertyPath, requiredType);
            if (editor == null) {
                List strippedPaths = new LinkedList();
                addStrippedPropertyPaths(strippedPaths, "", propertyPath);
                for (Iterator it = strippedPaths.iterator(); it.hasNext() && editor == null; ) {
                    String strippedPath = (String) it.next();
                    editor = getCustomEditor(strippedPath, requiredType);
                }
            }
            if (editor != null) {
                return editor;
            } else if (requiredType == null) {
                requiredType = getPropertyType(propertyPath);
            }
        }
        return getCustomEditor(requiredType);
    }

    /**
	 * Get custom editor that has been registered for the given property.
	 * @return the custom editor, or <code>null</code> if none specific for this property
	 */
    private PropertyEditor getCustomEditor(String propertyName, Class requiredType) {
        CustomEditorHolder holder = (CustomEditorHolder) this.customEditors.get(propertyName);
        return (holder != null ? holder.getPropertyEditor(requiredType) : null);
    }

    /**
	 * Get custom editor for the given type. If no direct match found,
	 * try custom editor for superclass (which will in any case be able
	 * to render a value as String via <code>getAsText</code>).
	 * @return the custom editor, or <code>null</code> if none found for this type
	 * @see java.beans.PropertyEditor#getAsText
	 */
    private PropertyEditor getCustomEditor(Class requiredType) {
        if (requiredType != null) {
            PropertyEditor editor = (PropertyEditor) this.customEditors.get(requiredType);
            if (editor == null) {
                for (Iterator it = this.customEditors.keySet().iterator(); it.hasNext(); ) {
                    Object key = it.next();
                    if (key instanceof Class && ((Class) key).isAssignableFrom(requiredType)) {
                        editor = (PropertyEditor) this.customEditors.get(key);
                    }
                }
            }
            return editor;
        }
        return null;
    }

    /**
	 * Add property paths with all variations of stripped keys and/or indexes.
	 * Invokes itself recursively with nested paths
	 * @param strippedPaths the result list to add to
	 * @param nestedPath the current nested path
	 * @param propertyPath the property path to check for keys/indexes to strip
	 */
    private void addStrippedPropertyPaths(List strippedPaths, String nestedPath, String propertyPath) {
        int startIndex = propertyPath.indexOf(PROPERTY_KEY_PREFIX_CHAR);
        if (startIndex != -1) {
            int endIndex = propertyPath.indexOf(PROPERTY_KEY_SUFFIX_CHAR);
            if (endIndex != -1) {
                String prefix = propertyPath.substring(0, startIndex);
                String key = propertyPath.substring(startIndex, endIndex + 1);
                String suffix = propertyPath.substring(endIndex + 1, propertyPath.length());
                strippedPaths.add(nestedPath + prefix + suffix);
                addStrippedPropertyPaths(strippedPaths, nestedPath + prefix, suffix);
                addStrippedPropertyPaths(strippedPaths, nestedPath + prefix + key, suffix);
            }
        }
    }

    private int getNestedPropertySeparatorIndex(String propertyPath, boolean last) {
        boolean inKey = false;
        int i = (last ? propertyPath.length() - 1 : 0);
        while ((last && i >= 0) || i < propertyPath.length()) {
            switch(propertyPath.charAt(i)) {
                case PROPERTY_KEY_PREFIX_CHAR:
                case PROPERTY_KEY_SUFFIX_CHAR:
                    inKey = !inKey;
                    break;
                case NESTED_PROPERTY_SEPARATOR_CHAR:
                    if (!inKey) {
                        return i;
                    }
            }
            if (last) i--; else i++;
        }
        return -1;
    }

    /**
	 * Get the last component of the path. Also works if not nested.
	 * @param bw BeanWrapper to work on
	 * @param nestedPath property path we know is nested
	 * @return last component of the path (the property on the target bean)
	 */
    private String getFinalPath(PropertyDescriptorBeanWrapper bw, String nestedPath) {
        if (bw == this) {
            return nestedPath;
        }
        return nestedPath.substring(getNestedPropertySeparatorIndex(nestedPath, true) + 1);
    }

    /**
	 * Recursively navigate to return a BeanWrapper for the nested property path.
	 * @param propertyPath property property path, which may be nested
	 * @return a BeanWrapper for the target bean
	 */
    public PropertyDescriptorBeanWrapper getBeanWrapperForPropertyPath(String propertyPath) throws BeansException {
        int pos = getNestedPropertySeparatorIndex(propertyPath, false);
        if (pos > -1) {
            String nestedProperty = propertyPath.substring(0, pos);
            String nestedPath = propertyPath.substring(pos + 1);
            PropertyDescriptorBeanWrapper nestedBw = getNestedBeanWrapper(nestedProperty);
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
    protected PropertyDescriptorBeanWrapper getNestedBeanWrapper(String nestedProperty) throws BeansException {
        if (this.nestedBeanWrappers == null) {
            this.nestedBeanWrappers = new HashMap();
        }
        PropertyTokenHolder tokens = getPropertyNameTokens(nestedProperty);
        PropertyInfo propertyInfo = getPropertyValue(tokens);
        Object propertyValue = propertyInfo.value;
        String canonicalName = tokens.canonicalName;
        String propertyName = tokens.actualName;
        PropertyDescriptorBeanWrapper nestedBw = (PropertyDescriptorBeanWrapper) this.nestedBeanWrappers.get(canonicalName);
        if (nestedBw == null || nestedBw.getWrappedInstance() != propertyValue) {
            if (logger.isDebugEnabled()) {
                logger.debug("Creating new nested BeanWrapper for property '" + canonicalName + "'");
            }
            nestedBw = new PropertyDescriptorBeanWrapper(propertyInfo, this.nestedPath + canonicalName + NESTED_PROPERTY_SEPARATOR, this);
            nestedBw.indexValueResolver = this.indexValueResolver;
            if (this.customEditors != null) {
                for (Iterator it = this.customEditors.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry entry = (Map.Entry) it.next();
                    if (entry.getKey() instanceof Class) {
                        Class requiredType = (Class) entry.getKey();
                        PropertyEditor editor = (PropertyEditor) entry.getValue();
                        nestedBw.registerCustomEditor(requiredType, editor);
                    } else if (entry.getKey() instanceof String) {
                        String editorPath = (String) entry.getKey();
                        int pos = getNestedPropertySeparatorIndex(editorPath, false);
                        if (pos != -1) {
                            String editorNestedProperty = editorPath.substring(0, pos);
                            String editorNestedPath = editorPath.substring(pos + 1);
                            if (editorNestedProperty.equals(canonicalName) || editorNestedProperty.equals(propertyName)) {
                                CustomEditorHolder editorHolder = (CustomEditorHolder) entry.getValue();
                                nestedBw.registerCustomEditor(editorHolder.getRegisteredType(), editorNestedPath, editorHolder.getPropertyEditor());
                            }
                        }
                    }
                }
            }
            this.nestedBeanWrappers.put(canonicalName, nestedBw);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Using cached nested BeanWrapper for property '" + canonicalName + "'");
            }
        }
        return nestedBw;
    }

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

    public Object getPropertyValue(String propertyName) {
        return getPropertyInfo(propertyName).value;
    }

    public PropertyInfo getPropertyInfo(String propertyName) throws BeansException {
        PropertyDescriptorBeanWrapper nestedBw = getBeanWrapperForPropertyPath(propertyName);
        PropertyTokenHolder tokens = getPropertyNameTokens(getFinalPath(nestedBw, propertyName));
        return nestedBw.getPropertyValue(tokens);
    }

    protected PropertyInfo getPropertyValue(PropertyTokenHolder tokens) throws BeansException {
        String propertyName = tokens.canonicalName;
        String actualName = tokens.actualName;
        PropertyDescriptor pd = getPropertyDescriptorInternal(tokens.actualName);
        if (pd == null || pd.getReadMethod() == null) {
            throw new NotReadablePropertyException(getRootClass(), this.nestedPath + propertyName);
        }
        Method readMethod = pd.getReadMethod();
        if (logger.isDebugEnabled()) logger.debug("About to invoke read method [" + readMethod + "] on object of class [" + propertyInfo.type + "]");
        try {
            ReflectionCache reflectionCache = ReflectionCacheFactory.getReflectionCache();
            PropertyInfo propertyInfo = new PropertyInfo();
            propertyInfo.lastPropertyGetterName = readMethod.getName();
            propertyInfo.type = readMethod.getGenericReturnType();
            propertyInfo.clazz = readMethod.getReturnType();
            propertyInfo.propertyName = nestedPath + actualName;
            propertyInfo.errorMessages.addAll(this.propertyInfo.errorMessages);
            propertyInfo.annotations = reflectionCache.getAnnotations(readMethod);
            propertyInfo.ownerType = readMethod.getDeclaringClass();
            propertyInfo.parent = this.propertyInfo;
            if (this.propertyInfo.value != null) {
                Object value = getLazyValue(propertyInfo);
                propertyInfo.value = readMethod.invoke(value);
            } else {
                propertyInfo.value = null;
            }
            if (tokens.keys != null) {
                for (int i = 0; i < tokens.keys.length; i++) {
                    String key = tokens.keys[i];
                    if (propertyInfo.value == null && indexValueResolver != null) {
                        Object resolveName = indexValueResolver.resolveName(key, Object.class);
                        if (resolveName == null) {
                            propertyInfo.addErrorMessage("N�o foi poss�vel achar nenhum valor para a chave: '" + key + "' properiedade: " + this.nestedPath + propertyName + " (obs.: " + propertyInfo.propertyName + " � nulo)");
                        }
                    }
                    if (propertyInfo.clazz.isArray()) {
                        Integer index;
                        if (indexValueResolver != null) {
                            index = indexValueResolver.resolveName(key, Integer.class);
                        } else {
                            logger.warn("IndexValueResolver n�o informado");
                            index = Integer.parseInt(key);
                        }
                        if (propertyInfo.value != null) {
                            if (index != null && index < ((Object[]) propertyInfo.value).length) {
                                propertyInfo.value = Array.get(propertyInfo.value, index);
                            } else {
                                if (index == null) {
                                    propertyInfo.addErrorMessage("N�o foi poss�vel achar nenhum valor para a chave: " + key);
                                }
                                propertyInfo.value = null;
                            }
                        }
                        propertyInfo.propertyName = resolveIndexedPropertyName(propertyInfo.propertyName, index, key);
                    } else if (List.class.isAssignableFrom(propertyInfo.clazz)) {
                        if (!(propertyInfo.type instanceof ParameterizedType)) {
                            throw new NotParameterizedTypeException("Path direciona a um List n�o parameterizado com generics. " + " Propriedade '" + this.nestedPath + propertyName + "' da classe [" + this.rootObject.clazz.getName() + "]");
                        }
                        ParameterizedType parameterizedType = ((ParameterizedType) propertyInfo.type);
                        Type collectionType = parameterizedType.getActualTypeArguments()[0];
                        Class rawType = collectionType instanceof Class ? (Class) collectionType : (Class) ((ParameterizedType) collectionType).getRawType();
                        propertyInfo.clazz = rawType;
                        propertyInfo.type = collectionType;
                        Integer index;
                        if (indexValueResolver != null) {
                            index = indexValueResolver.resolveName(key, Integer.class);
                        } else {
                            logger.warn("IndexValueResolver n�o informado");
                            index = Integer.parseInt(key);
                        }
                        if (propertyInfo.value != null) {
                            List list = (List) propertyInfo.value;
                            try {
                                propertyInfo.value = list.get(index);
                            } catch (Exception e) {
                                if (index == null) {
                                    propertyInfo.addErrorMessage("N�o foi poss�vel achar nenhum valor para a chave: '" + key + "' properiedade: " + this.nestedPath + propertyName);
                                }
                                propertyInfo.value = null;
                            }
                        }
                        propertyInfo.propertyName = resolveIndexedPropertyName(propertyInfo.propertyName, index, key);
                    } else if (Set.class.isAssignableFrom(propertyInfo.clazz)) {
                        if (!(propertyInfo.type instanceof ParameterizedType)) {
                            throw new NotParameterizedTypeException("Path direciona a um Set n�o parameterizado com generics. " + " Propriedade '" + this.nestedPath + propertyName + "' da classe [" + this.rootObject.clazz.getName() + "]");
                        }
                        ParameterizedType parameterizedType = ((ParameterizedType) propertyInfo.type);
                        Type collectionType = parameterizedType.getActualTypeArguments()[0];
                        Class rawType = collectionType instanceof Class ? (Class) collectionType : (Class) ((ParameterizedType) collectionType).getRawType();
                        propertyInfo.clazz = rawType;
                        propertyInfo.type = collectionType;
                        Integer index;
                        if (indexValueResolver != null) {
                            index = indexValueResolver.resolveName(key, Integer.class);
                        } else {
                            logger.warn("IndexValueResolver n�o informado");
                            index = Integer.parseInt(key);
                        }
                        if (propertyInfo.value != null) {
                            Set set = (Set) propertyInfo.value;
                            if (index != null && index < set.size()) {
                                Iterator it = set.iterator();
                                for (int j = 0; it.hasNext(); j++) {
                                    Object elem = it.next();
                                    if (j == index) {
                                        propertyInfo.value = elem;
                                        break;
                                    }
                                }
                            } else {
                                if (index == null) {
                                    propertyInfo.addErrorMessage("N�o foi poss�vel achar nenhum valor para a chave: '" + key + "' properiedade: " + this.nestedPath + propertyName);
                                }
                                propertyInfo.value = null;
                            }
                        }
                        propertyInfo.propertyName = resolveIndexedPropertyName(propertyInfo.propertyName, index, key);
                    } else if (Map.class.isAssignableFrom(propertyInfo.clazz)) {
                        if (!(propertyInfo.type instanceof ParameterizedType)) {
                            throw new NotParameterizedTypeException("Path direciona a um Map n�o parameterizado com generics. " + " Propriedade '" + this.nestedPath + propertyName + "' da classe [" + this.rootObject.clazz.getName() + "]");
                        }
                        ParameterizedType parameterizedType = ((ParameterizedType) propertyInfo.type);
                        Type mapKeyType = parameterizedType.getActualTypeArguments()[0];
                        Type mapValueType = parameterizedType.getActualTypeArguments()[1];
                        Class rawType = mapValueType instanceof Class ? (Class) mapValueType : (Class) ((ParameterizedType) mapValueType).getRawType();
                        Class rawKeyType = mapKeyType instanceof Class ? (Class) mapKeyType : (Class) ((ParameterizedType) mapKeyType).getRawType();
                        propertyInfo.clazz = rawType;
                        propertyInfo.type = mapValueType;
                        Object index;
                        if (indexValueResolver != null) {
                            index = indexValueResolver.resolveName(key, rawKeyType);
                        } else {
                            logger.warn("IndexValueResolver n�o informado");
                            index = key;
                        }
                        if (propertyInfo.value != null) {
                            Map map = (Map) propertyInfo.value;
                            if (index != null) {
                                propertyInfo.value = map.get(index);
                            } else {
                                propertyInfo.addErrorMessage("N�o foi poss�vel achar nenhum valor para a chave: '" + key + "' properiedade: " + this.nestedPath + propertyName);
                                propertyInfo.value = null;
                            }
                        } else {
                            propertyInfo.value = null;
                        }
                        propertyInfo.propertyName = resolveIndexedPropertyName(propertyInfo.propertyName, index, key);
                    } else {
                        throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName, "Property referenced in indexed property path '" + propertyName + "' is neither an array nor a List nor a Set nor a Map; returned value was [" + propertyInfo.value + "]");
                    }
                }
            }
            return propertyInfo;
        } catch (InvocationTargetException ex) {
            if (ex.getTargetException() instanceof LazyInitializationException) {
                throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName, "Getter for property '" + actualName + "' threw exception. LazyInitializationException: objeto da classe " + propertyInfo.clazz.getName() + " n�o inicializado", ex);
            } else {
                throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName, "Getter for property '" + actualName + "' threw exception", ex);
            }
        } catch (IllegalAccessException ex) {
            throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName, "Illegal attempt to get property '" + actualName + "' threw exception", ex);
        } catch (IndexOutOfBoundsException ex) {
            throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName, "Index of out of bounds in property path '" + propertyName + "'", ex);
        } catch (NumberFormatException ex) {
            throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName, "Invalid index in property path '" + propertyName + "'", ex);
        }
    }

    /**
	 * Carrega o valor do property info mesmo se o valor for lazy.
	 * � necessario existir um HibernateTemplate na aplica��o
	 * @param propertyInfo
	 * @return
	 */
    public Object getLazyValue(PropertyInfo propertyInfo) {
        Object value = this.propertyInfo.value;
        for (Annotation ann : propertyInfo.annotations) {
            if (Id.class.isAssignableFrom(ann.annotationType())) {
                return value;
            }
        }
        value = Util.hibernate.getLazyValue(value);
        if (value != this.propertyInfo.value) {
            logger.warn(value.getClass().getSimpleName() + "." + propertyInfo.propertyName + " carregado sob demanda.");
        }
        return value;
    }

    private String resolveIndexedPropertyName(String propertyName, Object index, String key) {
        Object newKey = index == null ? key : index;
        return propertyName + "[" + newKey + "]";
    }

    public void setPropertyValue(String propertyName, Object value) throws BeansException {
        PropertyDescriptorBeanWrapper nestedBw = null;
        try {
            nestedBw = getBeanWrapperForPropertyPath(propertyName);
        } catch (NotReadablePropertyException ex) {
            throw new NotWritablePropertyException(getRootClass(), this.nestedPath + propertyName, "Nested property in path '" + propertyName + "' does not exist", ex);
        }
        PropertyTokenHolder tokens = getPropertyNameTokens(getFinalPath(nestedBw, propertyName));
        nestedBw.setPropertyValue(tokens, value);
    }

    protected void setPropertyValue(PropertyTokenHolder tokens, Object newValue) throws BeansException {
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
                    PropertyChangeEvent pce = new PropertyChangeEvent(this.rootObject.value, this.nestedPath + propertyName, oldValue, newValue);
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
                    oldValue = readMethod.invoke(this.propertyInfo, new Object[0]);
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
                    logger.debug("About to invoke write method [" + writeMethod + "] on object of class [" + this.propertyInfo.getClass().getName() + "]");
                }
                writeMethod.invoke(this.propertyInfo, new Object[] { convertedValue });
                if (logger.isDebugEnabled()) {
                    logger.debug("Invoked write method [" + writeMethod + "] with value of type [" + pd.getPropertyType().getName() + "]");
                }
            } catch (InvocationTargetException ex) {
                PropertyChangeEvent propertyChangeEvent = new PropertyChangeEvent(this.rootObject.value, this.nestedPath + propertyName, oldValue, newValue);
                if (ex.getTargetException() instanceof ClassCastException) {
                    throw new TypeMismatchException(propertyChangeEvent, pd.getPropertyType(), ex.getTargetException());
                } else {
                    throw new MethodInvocationException(propertyChangeEvent, ex.getTargetException());
                }
            } catch (IllegalArgumentException ex) {
                PropertyChangeEvent pce = new PropertyChangeEvent(this.rootObject.value, this.nestedPath + propertyName, oldValue, newValue);
                throw new TypeMismatchException(pce, pd.getPropertyType(), ex);
            } catch (IllegalAccessException ex) {
                PropertyChangeEvent pce = new PropertyChangeEvent(this.rootObject.value, this.nestedPath + propertyName, oldValue, newValue);
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
            throw new PropertyBatchUpdateException((PropertyAccessException[]) paeArray);
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
	 * A number of PropertyEditors are automatically registered by PropertyDescriptorBeanWrapper.
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
                        pe = (PropertyEditor) this.defaultEditors.get(requiredType);
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
        PropertyDescriptorBeanWrapper nestedBw = getBeanWrapperForPropertyPath(propertyName);
        return nestedBw.cachedIntrospectionResults.getPropertyDescriptor(getFinalPath(nestedBw, propertyName));
    }

    public Class getPropertyType(String propertyName) throws BeansException {
        try {
            PropertyDescriptor pd = getPropertyDescriptorInternal(propertyName);
            if (pd != null) {
                return pd.getPropertyType();
            } else {
                Object value = getPropertyInfo(propertyName);
                if (value != null) {
                    return value.getClass();
                }
                if (this.customEditors != null) {
                    CustomEditorHolder editorHolder = (CustomEditorHolder) this.customEditors.get(propertyName);
                    if (editorHolder == null) {
                        List strippedPaths = new LinkedList();
                        addStrippedPropertyPaths(strippedPaths, "", propertyName);
                        for (Iterator it = strippedPaths.iterator(); it.hasNext() && editorHolder == null; ) {
                            String strippedName = (String) it.next();
                            editorHolder = (CustomEditorHolder) this.customEditors.get(strippedName);
                        }
                    }
                    if (editorHolder != null) {
                        return editorHolder.getRegisteredType();
                    }
                }
            }
        } catch (InvalidPropertyException ex) {
        }
        return null;
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
                getPropertyInfo(propertyName);
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
                getPropertyInfo(propertyName);
                return true;
            }
        } catch (InvalidPropertyException ex) {
        }
        return false;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("PropertyDescriptorBeanWrapper: wrapping class [");
        sb.append(getWrappedClass().getName()).append("]");
        return sb.toString();
    }

    /**
	 * Holder for a registered custom editor with property name.
	 * Keeps the PropertyEditor itself plus the type it was registered for.
	 */
    private static class CustomEditorHolder {

        private final PropertyEditor propertyEditor;

        private final Class registeredType;

        private CustomEditorHolder(PropertyEditor propertyEditor, Class registeredType) {
            this.propertyEditor = propertyEditor;
            this.registeredType = registeredType;
        }

        private PropertyEditor getPropertyEditor() {
            return propertyEditor;
        }

        private Class getRegisteredType() {
            return registeredType;
        }

        private PropertyEditor getPropertyEditor(Class requiredType) {
            return null;
        }
    }

    private static class PropertyTokenHolder {

        private String canonicalName;

        private String actualName;

        private String[] keys;
    }

    public Type getType() {
        return propertyInfo.type;
    }

    public Annotation[] getAnnotations() {
        return propertyInfo.annotations;
    }

    public void setType(Class beanClass) {
        if (this.propertyInfo == null) {
            this.propertyInfo = new PropertyInfo();
        }
        propertyInfo.type = beanClass;
        propertyInfo.clazz = beanClass;
        propertyInfo.value = null;
        setIntrospectionClass(beanClass);
    }

    public IndexValueResolver getIndexValueResolver() {
        return indexValueResolver;
    }

    public void setIndexValueResolver(IndexValueResolver valueindexValueResolver) {
        this.indexValueResolver = valueindexValueResolver;
    }
}

/**
 * Copia do spring
 * @author rogelgarcia
 *
 */
@SuppressWarnings("unchecked")
final class CachedIntrospectionResults {

    private static final Log logger = LogFactory.getLog(CachedIntrospectionResults.class);

    /**
	 * Map keyed by class containing CachedIntrospectionResults.
	 * Needs to be a WeakHashMap with WeakReferences as values to allow
	 * for proper garbage collection in case of multiple class loaders.
	 */
    private static final Map classCache = Collections.synchronizedMap(new WeakHashMap());

    /**
	 * We might use this from the EJB tier, so we don't want to use synchronization.
	 * Object references are atomic, so we can live with doing the occasional
	 * unnecessary lookup at startup only.
	 */
    static CachedIntrospectionResults forClass(Class clazz) throws BeansException {
        CachedIntrospectionResults results = null;
        Object value = classCache.get(clazz);
        if (value instanceof Reference) {
            Reference ref = (Reference) value;
            results = (CachedIntrospectionResults) ref.get();
        } else {
            results = (CachedIntrospectionResults) value;
        }
        if (results == null) {
            results = new CachedIntrospectionResults(clazz);
            boolean cacheSafe = isCacheSafe(clazz);
            if (logger.isDebugEnabled()) {
                logger.debug("Class [" + clazz.getName() + "] is " + (!cacheSafe ? "not " : "") + "cache-safe");
            }
            if (cacheSafe) {
                classCache.put(clazz, results);
            } else {
                classCache.put(clazz, new WeakReference(results));
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Using cached introspection results for class [" + clazz.getName() + "]");
            }
        }
        return results;
    }

    /**
	 * Check whether the given class is cache-safe,
	 * i.e. whether it is loaded by the same class loader as the
	 * CachedIntrospectionResults class or a parent of it.
	 * <p>Many thanks to Guillaume Poirier for pointing out the
	 * garbage collection issues and for suggesting this solution.
	 * @param clazz the class to analyze
	 * @return whether the given class is thread-safe
	 */
    private static boolean isCacheSafe(Class clazz) {
        ClassLoader cur = CachedIntrospectionResults.class.getClassLoader();
        ClassLoader target = clazz.getClassLoader();
        if (target == null || cur == target) {
            return true;
        }
        while (cur != null) {
            cur = cur.getParent();
            if (cur == target) {
                return true;
            }
        }
        return false;
    }

    private final BeanInfo beanInfo;

    /** Property descriptors keyed by property name */
    private final Map propertyDescriptorCache;

    /**
	 * Create new CachedIntrospectionResults instance fot the given class.
	 */
    private CachedIntrospectionResults(Class clazz) throws BeansException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Getting BeanInfo for class [" + clazz.getName() + "]");
            }
            this.beanInfo = Introspector.getBeanInfo(clazz);
            Class classToFlush = clazz;
            do {
                Introspector.flushFromCaches(classToFlush);
                classToFlush = classToFlush.getSuperclass();
            } while (classToFlush != null);
            if (logger.isDebugEnabled()) {
                logger.debug("Caching PropertyDescriptors for class [" + clazz.getName() + "]");
            }
            this.propertyDescriptorCache = new HashMap();
            PropertyDescriptor[] pds = this.beanInfo.getPropertyDescriptors();
            for (int i = 0; i < pds.length; i++) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Found property '" + pds[i].getName() + "'" + (pds[i].getPropertyType() != null ? " of type [" + pds[i].getPropertyType().getName() + "]" : "") + (pds[i].getPropertyEditorClass() != null ? "; editor [" + pds[i].getPropertyEditorClass().getName() + "]" : ""));
                }
                Method readMethod = pds[i].getReadMethod();
                if (readMethod != null && !Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                    readMethod.setAccessible(true);
                }
                Method writeMethod = pds[i].getWriteMethod();
                if (writeMethod != null && !Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                    writeMethod.setAccessible(true);
                }
                this.propertyDescriptorCache.put(pds[i].getName(), pds[i]);
            }
        } catch (IntrospectionException ex) {
            throw new FatalBeanException("Cannot get BeanInfo for object of class [" + clazz.getName() + "]", ex);
        }
    }

    BeanInfo getBeanInfo() {
        return this.beanInfo;
    }

    Class getBeanClass() {
        return this.beanInfo.getBeanDescriptor().getBeanClass();
    }

    PropertyDescriptor getPropertyDescriptor(String propertyName) {
        return (PropertyDescriptor) this.propertyDescriptorCache.get(propertyName);
    }
}
