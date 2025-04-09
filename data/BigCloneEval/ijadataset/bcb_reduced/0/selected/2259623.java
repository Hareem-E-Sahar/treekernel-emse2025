package uk.co.lakesidetech.spxforms.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.util.TypeUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.NotReadablePropertyException;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.beans.PropertyAccessException;
import org.springframework.beans.PropertyAccessExceptionsException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.TypeMismatchException;

/**
 * TODO deal with xpath errors better
 * and throw PropertyAccessExceptionsException
 * @author Stuart Eccles
 */
public class JXPathBeanWrapper implements BeanWrapper {

    private JXPathContext jxpathcontext;

    /**
     * 
     */
    public JXPathBeanWrapper(JXPathContext jxpathcontext) {
        this.jxpathcontext = jxpathcontext;
    }

    /**
     * @return Returns the jxpathcontext.
     */
    public JXPathContext getJxpathcontext() {
        return jxpathcontext;
    }

    /**
     * @see org.springframework.beans.BeanWrapper#setWrappedInstance(java.lang.Object)
     * @param obj
     */
    public void setWrappedInstance(Object obj) {
        JXPathContext newcontext = JXPathContext.newContext(jxpathcontext, obj);
        jxpathcontext = newcontext;
    }

    /**
     * @see org.springframework.beans.BeanWrapper#getWrappedInstance()
     * @return
     */
    public Object getWrappedInstance() {
        return jxpathcontext.getContextBean();
    }

    /**
     * @see org.springframework.beans.BeanWrapper#getWrappedClass()
     * @return
     */
    public Class getWrappedClass() {
        return jxpathcontext.getContextBean().getClass();
    }

    /**
     * @see org.springframework.beans.BeanWrapper#registerCustomEditor(java.lang.Class, java.beans.PropertyEditor)
     * @param requiredType
     * @param propertyEditor
     */
    public void registerCustomEditor(Class requiredType, PropertyEditor propertyEditor) {
    }

    /**
     * @see org.springframework.beans.BeanWrapper#registerCustomEditor(java.lang.Class, java.lang.String, java.beans.PropertyEditor)
     * @param requiredType
     * @param propertyPath
     * @param propertyEditor
     */
    public void registerCustomEditor(Class requiredType, String propertyPath, PropertyEditor propertyEditor) {
    }

    /**
     * @see org.springframework.beans.BeanWrapper#findCustomEditor(java.lang.Class, java.lang.String)
     * @param requiredType
     * @param propertyPath
     * @return
     */
    public PropertyEditor findCustomEditor(Class requiredType, String propertyPath) {
        return null;
    }

    /**
     * @see org.springframework.beans.BeanWrapper#getPropertyDescriptors()
     * @return
     * @throws org.springframework.beans.BeansException
     */
    public PropertyDescriptor[] getPropertyDescriptors() throws BeansException {
        return null;
    }

    /**
     * @see org.springframework.beans.BeanWrapper#getPropertyDescriptor(java.lang.String)
     * @param propertyName
     * @return
     * @throws org.springframework.beans.BeansException
     */
    public PropertyDescriptor getPropertyDescriptor(String propertyName) throws BeansException {
        return null;
    }

    /**
     * @see org.springframework.beans.BeanWrapper#getPropertyType(java.lang.String)
     * @param propertyName
     * @return
     * @throws org.springframework.beans.BeansException
     */
    public Class getPropertyType(String propertyName) throws BeansException {
        try {
            return jxpathcontext.getValue(propertyName).getClass();
        } catch (JXPathException e) {
            throw new NotReadablePropertyException(getWrappedClass(), propertyName);
        }
    }

    /**
     * @see org.springframework.beans.BeanWrapper#isReadableProperty(java.lang.String)
     * @param propertyName
     * @return
     * @throws org.springframework.beans.BeansException
     */
    public boolean isReadableProperty(String propertyName) throws BeansException {
        try {
            jxpathcontext.getValue(propertyName);
            return true;
        } catch (JXPathException e) {
            return false;
        }
    }

    /**
     * @see org.springframework.beans.BeanWrapper#isWritableProperty(java.lang.String)
     * @param propertyName
     * @return
     * @throws org.springframework.beans.BeansException
     */
    public boolean isWritableProperty(String propertyName) throws BeansException {
        return true;
    }

    /**
     * @see org.springframework.beans.PropertyAccessor#getPropertyValue(java.lang.String)
     * @param propertyName
     * @return
     * @throws org.springframework.beans.BeansException
     */
    public Object getPropertyValue(String propertyName) throws BeansException {
        try {
            return jxpathcontext.getValue(propertyName);
        } catch (JXPathException e) {
            try {
                jxpathcontext.createPath(propertyName);
                return jxpathcontext.getValue(propertyName);
            } catch (JXPathException e1) {
                throw new NotReadablePropertyException(getWrappedClass(), propertyName);
            }
        }
    }

    public Double getPropertyNodeSetCount(String propertyName) throws BeansException {
        try {
            return (Double) jxpathcontext.getValue("count(" + propertyName + ")");
        } catch (JXPathException e) {
            return null;
        }
    }

    /**
     * @see org.springframework.beans.PropertyAccessor#setPropertyValue(java.lang.String, java.lang.Object)
     * @param propertyName
     * @param value
     * @throws org.springframework.beans.BeansException
     */
    public void setPropertyValue(String propertyName, Object value) throws BeansException {
        try {
            if (isAllowedXBinding(propertyName)) {
                boolean createFirst = (jxpathcontext.getFactory() != null);
                if (createFirst) {
                    jxpathcontext.createPath(propertyName);
                }
                Pointer pointer = jxpathcontext.getPointer(propertyName);
                Object property = pointer.getValue();
                if (value.getClass().isArray()) {
                    Object[] values = (Object[]) value;
                    if (property != null && property.getClass().isArray()) {
                        Class componentType = property.getClass().getComponentType();
                        property = java.lang.reflect.Array.newInstance(componentType, values.length);
                        java.lang.System.arraycopy(values, 0, property, 0, values.length);
                        pointer.setValue(property);
                    } else if (property instanceof Collection) {
                        Collection cl = (Collection) property;
                        cl.clear();
                        cl.addAll(java.util.Arrays.asList(values));
                    }
                } else {
                    if (TypeUtils.canConvert(value, property.getClass())) {
                        pointer.setValue(value);
                    } else {
                        throw new TypeMismatchException(createPropertyChangeEvent(propertyName, property, value), property.getClass());
                    }
                }
            }
        } catch (JXPathException e) {
            throw new NotWritablePropertyException(getWrappedClass(), propertyName, e.getMessage(), e);
        }
    }

    /**
     * @see org.springframework.beans.PropertyAccessor#setPropertyValue(org.springframework.beans.PropertyValue)
     * @param pv
     * @throws org.springframework.beans.BeansException
     */
    public void setPropertyValue(PropertyValue pv) throws BeansException {
        setPropertyValue(pv.getName(), pv.getValue());
    }

    /**
     * @see org.springframework.beans.PropertyAccessor#setPropertyValues(java.util.Map)
     * @param map
     * @throws org.springframework.beans.BeansException
     */
    public void setPropertyValues(Map map) throws BeansException {
        setPropertyValues(new MutablePropertyValues(map));
    }

    /**
     * @see org.springframework.beans.PropertyAccessor#setPropertyValues(org.springframework.beans.PropertyValues)
     * @param pvs
     * @throws org.springframework.beans.BeansException
     */
    public void setPropertyValues(PropertyValues propertyValues) throws BeansException {
        setPropertyValues(propertyValues, false);
    }

    /**
     * @see org.springframework.beans.PropertyAccessor#setPropertyValues(org.springframework.beans.PropertyValues, boolean)
     * @param pvs
     * @param ignoreUnknown
     * @throws org.springframework.beans.BeansException
     */
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

    protected boolean isAllowedXBinding(String propertyName) {
        if (propertyName.startsWith("/")) {
            return true;
        }
        return false;
    }

    private PropertyChangeEvent createPropertyChangeEvent(String propertyName, Object oldValue, Object newValue) {
        return new PropertyChangeEvent((this.jxpathcontext.getContextBean() != null ? this.jxpathcontext.getContextBean() : "constructor"), (propertyName != null ? propertyName : null), oldValue, newValue);
    }
}
