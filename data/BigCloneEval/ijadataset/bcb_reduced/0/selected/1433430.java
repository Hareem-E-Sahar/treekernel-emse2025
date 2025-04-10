package com.curl.orb.context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContext;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Application context to integrate with seasar2 framework. (http://www.seasar.org/)
 * 
 * @author Satoshi Kimura
 * @since 0.5
 */
public class Seasar2ApplicationContext extends AbstractApplicationContext {

    /**
     * Create new Seasar2ApplicationContext.
     * 
     * @param context ServletContext
     * @throws ApplicationContextException
     */
    public Seasar2ApplicationContext(ServletContext context) throws ApplicationContextException {
        try {
            Class<?> cls = Class.forName("org.seasar.framework.container.servlet.SingletonS2ContainerInitializer");
            Object obj = cls.getConstructor(new Class[0]).newInstance(new Object[0]);
            if (context.getInitParameter("configPath") != null) {
                MethodUtils.invokeMethod(obj, "setConfigPath", new Object[] { context.getInitParameter("configPath") });
            }
            MethodUtils.invokeMethod(obj, "setApplication", new Object[] { context });
            MethodUtils.invokeMethod(obj, "initialize", new Object[0]);
        } catch (IllegalArgumentException e) {
            throw new ApplicationContextException(e);
        } catch (SecurityException e) {
            throw new ApplicationContextException(e);
        } catch (InstantiationException e) {
            throw new ApplicationContextException(e);
        } catch (ClassNotFoundException e) {
            throw new ApplicationContextException(e);
        } catch (NoSuchMethodException e) {
            throw new ApplicationContextException(e);
        } catch (IllegalAccessException e) {
            throw new ApplicationContextException(e);
        } catch (InvocationTargetException e) {
            throw new ApplicationContextException(e);
        }
    }

    @Override
    public Object getObject(String name) throws ApplicationContextException {
        try {
            Class<?> cls = Class.forName("org.seasar.framework.container.factory.SingletonS2ContainerFactory");
            Object obj = MethodUtils.invokeStaticMethod(cls, "getContainer", new Object[0]);
            return MethodUtils.invokeMethod(obj, "getComponent", new Object[] { name });
        } catch (ClassNotFoundException e) {
            throw new ApplicationContextException(e);
        } catch (NoSuchMethodException e) {
            throw new ApplicationContextException(e);
        } catch (IllegalAccessException e) {
            throw new ApplicationContextException(e);
        } catch (InvocationTargetException e) {
            throw new ApplicationContextException(e);
        }
    }

    @Override
    public String[] getObjectNames() throws ApplicationContextException {
        try {
            Class<?> cls = Class.forName("org.seasar.framework.container.factory.SingletonS2ContainerFactory");
            Object container = MethodUtils.invokeStaticMethod(cls, "getContainer", new Object[0]);
            List<String> componentNames = new ArrayList<String>();
            addComponentName(container, componentNames);
            return componentNames.toArray(new String[componentNames.size()]);
        } catch (ClassNotFoundException e) {
            throw new ApplicationContextException(e);
        } catch (NoSuchMethodException e) {
            throw new ApplicationContextException(e);
        } catch (IllegalAccessException e) {
            throw new ApplicationContextException(e);
        } catch (InvocationTargetException e) {
            throw new ApplicationContextException(e);
        } catch (Exception e) {
            throw new ApplicationContextException(e);
        }
    }

    private void addComponentName(Object container, List<String> componentNames) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Integer componentSize = (Integer) MethodUtils.invokeMethod(container, "getComponentDefSize", new Object[0]);
        Method getComponentDefMethod = container.getClass().getMethod("getComponentDef", Integer.TYPE);
        for (int i = 0; i < componentSize; i++) {
            Object componentDef = getComponentDefMethod.invoke(container, new Object[] { i });
            String componentName = (String) MethodUtils.invokeMethod(componentDef, "getComponentName", new Object[0]);
            if (StringUtils.isEmpty(componentName) == false) {
                componentNames.add(componentName);
            }
        }
        Integer childSize = (Integer) MethodUtils.invokeMethod(container, "getChildSize", new Object[0]);
        for (int i = 0; i < childSize; i++) {
            Object child = MethodUtils.invokeMethod(container, "getChild", new Object[] { i });
            addComponentName(child, componentNames);
        }
    }

    @Override
    public Class<?> getObjectType(String name) throws ApplicationContextException {
        try {
            Object componentDef = getComponentDef(name);
            Object componentClass = MethodUtils.invokeMethod(componentDef, "getComponentClass", new Object[0]);
            return (Class<?>) componentClass;
        } catch (NoSuchMethodException e) {
            throw new ApplicationContextException(e);
        } catch (IllegalAccessException e) {
            throw new ApplicationContextException(e);
        } catch (InvocationTargetException e) {
            throw new ApplicationContextException(e);
        }
    }

    private Object getComponentDef(String name) throws ApplicationContextException {
        try {
            Class<?> cls = Class.forName("org.seasar.framework.container.factory.SingletonS2ContainerFactory");
            Object obj = MethodUtils.invokeStaticMethod(cls, "getContainer", new Object[0]);
            return MethodUtils.invokeMethod(obj, "getComponentDef", new Object[] { name });
        } catch (ClassNotFoundException e) {
            throw new ApplicationContextException(e);
        } catch (NoSuchMethodException e) {
            throw new ApplicationContextException(e);
        } catch (IllegalAccessException e) {
            throw new ApplicationContextException(e);
        } catch (InvocationTargetException e) {
            throw new ApplicationContextException(e);
        }
    }

    @Override
    public void setObject(String name, Object value) throws ApplicationContextException {
    }
}
