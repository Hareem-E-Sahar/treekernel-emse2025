package org.t2framework.lucy.aop.proxy.meta.impl;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import org.t2framework.commons.exception.ConstructorNotFoundRuntimeException;
import org.t2framework.commons.meta.BeanDesc;
import org.t2framework.commons.meta.ConstructorDesc;
import org.t2framework.commons.meta.InstanceFactory;
import org.t2framework.commons.util.Reflections.ClassLoaderUtil;
import org.t2framework.commons.util.Reflections.ConstructorUtil;
import org.t2framework.lucy.aop.InterceptorDesc;
import org.t2framework.lucy.aop.proxy.ProxyInvocationHandler;

/**
 * 
 * <#if locale="en">
 * <p>
 * 
 * </p>
 * <#else>
 * <p>
 * {@code Proxy}によってインスタンスを置き換えるための{@code InstanceFactory}です.
 * </p>
 * </#if>
 * 
 * @author c9katayama
 * @author shot
 * 
 * @param <T>
 */
public class ProxyInstanceFactoryImpl<T> implements InstanceFactory<T> {

    protected static final InvocationHandler noOpInvocationHandler = new InvocationHandler() {

        public Object invoke(Object target, Method method, Object[] args) throws Throwable {
            return null;
        }
    };

    protected Map<AccessibleObject, InterceptorDesc> proxyInterceptorMap;

    public ProxyInstanceFactoryImpl(Map<AccessibleObject, InterceptorDesc> proxyInterceptorMap) {
        this.proxyInterceptorMap = proxyInterceptorMap;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T newInstance(BeanDesc<T> beanDesc, Object[] args) {
        Class<?> originalClass = beanDesc.getComponentClass();
        Object thisInstance;
        if (originalClass.isInterface()) {
            thisInstance = Proxy.newProxyInstance(ClassLoaderUtil.getClassLoader(originalClass), new Class[] { originalClass }, noOpInvocationHandler);
        } else {
            ConstructorDesc<T> cd = beanDesc.getConstructorDesc();
            Constructor<T> c = cd.getSuitableConstructor(args);
            if (c == null) {
                throw new ConstructorNotFoundRuntimeException(beanDesc.getConcreteClass().getName());
            }
            thisInstance = ConstructorUtil.newInstance(c, args);
        }
        Class<?> proxyClass = beanDesc.getEnhancedComponentClass();
        ProxyInvocationHandler proxyHandler = new ProxyInvocationHandler(proxyInterceptorMap, thisInstance);
        Constructor<?> proxyConstructor = ConstructorUtil.getConstructor(proxyClass, new Class[] { InvocationHandler.class });
        return (T) ConstructorUtil.newInstance(proxyConstructor, new Object[] { proxyHandler });
    }
}
