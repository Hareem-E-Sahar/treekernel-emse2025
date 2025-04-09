package net.sf.lavabeans.read;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.lavabeans.util.LavaBeanUtils;
import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.enums.Enum;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Encodes some standard rules which generally apply. If you want to use the default behavior implemented in this class, invoke the superclass
 * methods after checking your custom override.
 */
public class DefaultRuleBase implements RuleBase {

    static {
        ConvertUtils.register(new DateConverter(), Date.class);
        ConvertUtils.register(new EnumConverter(), Enum.class);
    }

    private static final Log LOG = LogFactory.getLog(DefaultRuleBase.class);

    private BeanInfoRepository beanInfo;

    private Object rootBean = new ArrayList();

    private boolean lowerCaseAcronyms = false;

    private final Set failedClassNames = new HashSet();

    private final List beanPatterns = new ArrayList();

    public void initalize(BeanInfoRepository beanInfo) {
        this.beanInfo = beanInfo;
    }

    public void setLowerCaseAcronyms(boolean lowerCaseAcronyms) {
        this.lowerCaseAcronyms = lowerCaseAcronyms;
    }

    /**
     * Adds a new bean pattern. When the rule base is processing elements, it applies
     * each to the pattern strings. A pattern string should include the token <code>${shortName}</code>, 
     * for example <code>com.praxeon.bean.impl.${shortName}Impl</code>. The shortName may be determined from
     * the element name, or it may be determined by looking at the properties on the parent bean
     * that match the element name. If a class with the resulting
     * name can be found and instantiated, it is returned from the {@link #newBean(DocumentPath, BeanPath)} method.
     */
    public void addBeanPattern(String pattern) {
        beanPatterns.add(pattern);
    }

    public void setRootBean(Object rootBean) {
        this.rootBean = rootBean;
    }

    /**
     * Maps String content to a <code>text</code> property.
     */
    public String getContentPropertyName(Class cls, Class valueClass) {
        if (String.class.isAssignableFrom(valueClass)) return "text";
        return null;
    }

    /**
     * Instantates a bean by matching the element name against the bean patterns.
     * The root element is converted to an ArrayList.
     * 
     * @see #addBeanPattern(String)
     */
    public Object newBean(DocumentPath path, BeanPath beanPath) {
        if (path.getElementCount() == 1) {
            return copyBean(rootBean);
        } else {
            Element element = path.peekElement();
            return newBean(element.getJavaName(), beanPath.peek(), new Object[0], new Class[0]);
        }
    }

    /**
     * Copies a prototype bean, first trying the <code>clone</code> method, and then
     * trying the default constructor.
     */
    protected Object copyBean(Object bean) {
        try {
            return MethodUtils.invokeMethod(bean, "clone", null);
        } catch (Exception e) {
            String message = "Unable to clone rootBean " + bean;
            try {
                return ConstructorUtils.invokeConstructor(bean.getClass(), null);
            } catch (Exception e1) {
                throw new NestableRuntimeException(message, e);
            }
        }
    }

    /**
     * If there is exactly one bean in the parent stack that matches the data type
     * of a parent property, populate that parent property.
     */
    public void setParents(Object bean, BeanPath beanPath, DocumentPath docPath) {
        Property[] pds = beanInfo.getParentProperties(bean.getClass());
        for (int i = 0; i < pds.length; i++) {
            Property pd = pds[i];
            List parents = beanPath.findBeansForClass(pd.getType());
            if (parents.size() == 1) {
                Object parent = parents.get(0);
                if (!pd.setValue(docPath, bean, parent)) {
                    LOG.warn("Unable to set parent property\n" + "Bean Class  : " + bean.getClass() + "\n" + "Bean        : " + bean + "\n" + "Property    : " + pd + "\n" + "Value       : " + parent + "\n" + "Value Class : " + parent.getClass());
                }
            }
        }
    }

    public String makePropertyName(String propertyName) {
        return LavaBeanUtils.toLCC(propertyName, lowerCaseAcronyms);
    }

    protected Object newBean(String localName, Object parent, Object[] args, Class[] argTypes) {
        String propertyName = LavaBeanUtils.toLCC(localName, lowerCaseAcronyms);
        Constructor ctor = findConstructor(propertyName, argTypes);
        if (ctor == null) {
            Property[] pds = beanInfo.getProperties(parent.getClass(), propertyName);
            for (int i = 0; ctor == null && i < pds.length; i++) {
                Property prop = pds[i];
                ctor = getConstructor(prop.getType(), argTypes);
                if (ctor == null) ctor = findConstructor(LavaBeanUtils.getShortName(prop.getType()), argTypes);
            }
        }
        if (ctor != null) {
            return beanInfo.newInstance(ctor, args);
        } else {
            if (LOG.isDebugEnabled()) LOG.debug("Not able to construct a bean for " + localName);
            return null;
        }
    }

    private Constructor findConstructor(String shortName, Class[] argTypes) {
        for (Iterator i = beanPatterns.iterator(); i.hasNext(); ) {
            String pattern = (String) i.next();
            String className = StringUtils.replace(pattern, "${shortName}", LavaBeanUtils.toUCC(shortName));
            Class cls = null;
            if (!failedClassNames.contains(className)) {
                try {
                    cls = Thread.currentThread().getContextClassLoader().loadClass(className);
                } catch (ClassNotFoundException e) {
                }
            }
            if (cls != null) {
                return getConstructor(cls, argTypes);
            }
            failedClassNames.add(className);
        }
        return null;
    }

    private Constructor getConstructor(Class cls, Class[] argTypes) {
        if (!allowClass(cls)) return null;
        if (!failedClassNames.contains(cls.getName())) {
            Constructor method = null;
            try {
                if (cls == List.class || (cls.isArray() && !cls.getComponentType().isPrimitive())) {
                    method = ArrayList.class.getConstructor(argTypes);
                } else if (cls == Set.class) {
                    method = HashSet.class.getConstructor(argTypes);
                } else if (cls == Map.class) {
                    method = HashMap.class.getConstructor(argTypes);
                } else {
                    method = cls.getConstructor(argTypes);
                }
                return method;
            } catch (SecurityException e) {
            } catch (NoSuchMethodException e) {
            }
        }
        failedClassNames.add(cls.getName());
        return null;
    }

    /**
     * Whether to allow a class to be constructed. By default, primitive wrapper types, String, Date
     * classes are not constructed.
     */
    protected boolean allowClass(Class cls) {
        return !Number.class.isAssignableFrom(cls) && cls != Character.class && cls != Boolean.class && cls != String.class && cls != java.util.Date.class && cls != java.util.Calendar.class && cls != java.sql.Date.class;
    }

    public void unmappedValue(DocumentPath docPath, BeanPath beanPath, String propertyName, Object value) {
        throw new XBindException(docPath, "No action taken for path " + docPath + "\n" + "Bean class   : " + beanPath.peek().getClass() + "\n" + "Bean         : " + beanPath.peek() + "\n" + "PropertyName : " + propertyName + "\n" + (value != null ? "Value class  : " + value.getClass() + "\n" : "") + "Value        : " + value + "\n");
    }

    public boolean skipProperty(DocumentPath docPath, BeanPath beanPath, String propertyName) {
        return false;
    }

    public String getIdAttributeName(DocumentPath docPath) {
        return "beanId";
    }

    public String getRefIdAttributeName(DocumentPath docPath) {
        return "beanRefId";
    }
}
