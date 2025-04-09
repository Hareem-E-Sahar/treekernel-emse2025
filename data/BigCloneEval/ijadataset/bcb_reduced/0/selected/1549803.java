package com.abra.j2xb.beans.model;

import com.abra.j2xb.beans.exceptions.MOBeanInstansationException;
import com.abra.j2xb.beans.exceptions.MOBeansException;
import com.abra.j2xb.beans.exceptions.MOBeansConstructionDefinitionException;
import com.abra.j2xb.annotations.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.beans.IntrospectionException;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Yoav Abrahami
 * @version 1.0, May 1, 2008
 * @since   JDK1.5
 */
public class MOConstructorFacade {

    private MOConstructionDescriptor constructionDescriptor;

    private MOBeanDescriptor beanDescriptor;

    private MOBeanDescriptor parentBeanDescriptor;

    private static final MOConstructorArg[] EMPTY_CONSTRUCTOR_ARGS = {};

    private static final MOInitializerProperty[] EMPTY_INITIALIZER_PROPERTIES = {};

    public MOConstructorFacade(MOBeanDescriptor beanDescriptor, AnnotatedElement theBeanClass) {
        this(beanDescriptor, theBeanClass, null, null);
    }

    MOConstructorFacade(MOBeanDescriptor beanDescriptor, AnnotatedElement theBeanClass, MOBeanDescriptor parentBeanDescriptor, AnnotatedElement theParentProperty) {
        this.beanDescriptor = beanDescriptor;
        this.parentBeanDescriptor = parentBeanDescriptor;
        if (theParentProperty != null) {
            constructionDescriptor = theParentProperty.getAnnotation(MOConstructionDescriptor.class);
        }
        if (constructionDescriptor == null) {
            constructionDescriptor = theBeanClass.getAnnotation(MOConstructionDescriptor.class);
        }
    }

    /**
	 * constucts a new instance and initializes it as specified in the construction description member.
	 * @param parentInstance - the parent instance is used to initialize implied properties of a component object
	 * (can be null).
	 * @param providedArgValues - values for use in the constructor, that thier source are properties of the constructed
	 * instance. 
	 * @param concreteBeanDescriptor - the bean descriptor of the actual bean class to create. In most cases, this
	 * will be the same as the {@link #beanDescriptor} member. However, in cases of substitution groups of inherited
	 * beans in collections or bean properties, this bean descriptor can be a descriptor of a subclass of the
	 * @return the constructed bean instance.
	 * @throws MOBeanInstansationException - describing a possible failure to instantiate the bean
	 */
    public Object newInstance(Object parentInstance, MOConstructorFacadeParamValues providedArgValues, MOBeanDescriptor concreteBeanDescriptor) throws MOBeanInstansationException {
        Object obj = createInstance(parentInstance, providedArgValues, concreteBeanDescriptor);
        initializeProperties(obj, parentInstance);
        return obj;
    }

    private void initializeProperties(Object instance, Object parentInstance) throws MOBeanInstansationException {
        try {
            Object propertyValue;
            for (int i = 0; i < getInitializerProperties().length; i++) {
                propertyValue = getParentArgValue(parentInstance, getInitializerProperties()[i].sourceProperty());
                setPropertyValue(instance, getInitializerProperties()[i].targetProperty(), propertyValue);
            }
        } catch (MOBeansException e) {
            throw new MOBeanInstansationException(e, beanDescriptor.getBeanClass());
        }
    }

    private Object createInstance(Object parentInstance, MOConstructorFacadeParamValues providedArgValues, MOBeanDescriptor concreteBeanDescriptor) throws MOBeanInstansationException {
        try {
            MOConstructorArg[] constructorArgs = getConstructorArgs();
            Class<?>[] argTypes = new Class<?>[constructorArgs.length];
            Object[] argValues = new Object[constructorArgs.length];
            for (int i = 0; i < constructorArgs.length; i++) {
                if (constructorArgs[i].sourcePropertyOf() == SourcePropertyOf.parent) {
                    argTypes[i] = getArgType(parentBeanDescriptor, constructorArgs[i].sourceProperty());
                    argValues[i] = getParentArgValue(parentInstance, constructorArgs[i].sourceProperty());
                } else {
                    argTypes[i] = getArgType(beanDescriptor, constructorArgs[i].sourceProperty());
                    argValues[i] = providedArgValues.getValue(constructorArgs[i].sourceProperty());
                }
            }
            if (constructionDescriptor == null) {
                return concreteBeanDescriptor.getBeanClass().newInstance();
            } else if (!hasFactoryMethod()) {
                return concreteBeanDescriptor.getBeanClass().getConstructor(argTypes).newInstance(argValues);
            } else if (hasFactoryClass()) {
                return constructionDescriptor.factoryClass().getMethod(constructionDescriptor.factoryMethod(), argTypes).invoke(null, argValues);
            } else {
                return parentInstance.getClass().getMethod(constructionDescriptor.factoryMethod(), argTypes).invoke(parentInstance, argValues);
            }
        } catch (InstantiationException e) {
            throw new MOBeanInstansationException(e, concreteBeanDescriptor.getBeanClass());
        } catch (IllegalAccessException e) {
            throw new MOBeanInstansationException(e, concreteBeanDescriptor.getBeanClass());
        } catch (InvocationTargetException e) {
            throw new MOBeanInstansationException(e, concreteBeanDescriptor.getBeanClass());
        } catch (NoSuchMethodException e) {
            throw new MOBeanInstansationException(e, concreteBeanDescriptor.getBeanClass());
        } catch (MOBeansException e) {
            throw new MOBeanInstansationException(e, concreteBeanDescriptor.getBeanClass());
        }
    }

    private void setPropertyValue(Object instance, String propertyPath, Object propertyValue) throws MOBeansException {
        beanDescriptor.setValue(instance, propertyValue, propertyPath);
    }

    private Class<?> getArgType(MOBeanDescriptor beanDescriptor, String propertyPath) throws MOBeansException {
        return beanDescriptor.getType(propertyPath);
    }

    private Object getParentArgValue(Object parentInstance, String propertyPath) throws MOBeansException {
        return parentBeanDescriptor.getValue(parentInstance, propertyPath);
    }

    public MOConstructorArg[] getConstructorArgs() {
        if (constructionDescriptor != null) return constructionDescriptor.constructorArgs(); else return EMPTY_CONSTRUCTOR_ARGS;
    }

    public MOInitializerProperty[] getInitializerProperties() {
        if (constructionDescriptor != null) return constructionDescriptor.InitializerProperties(); else return EMPTY_INITIALIZER_PROPERTIES;
    }

    public MOConstructionDescriptor getConstructionDescriptor() {
        return constructionDescriptor;
    }

    public MOBeanDescriptor getParentBeanDescriptor() {
        return parentBeanDescriptor;
    }

    public MOBeanDescriptor getBeanDescriptor() {
        return beanDescriptor;
    }

    public void validateDefinitions() throws MOBeansException, IntrospectionException {
        if (beanDescriptor.isPersistentDependentBean() && parentBeanDescriptor == null && constructionDescriptor == null) return;
        if (parentBeanDescriptor == null) {
            for (MOConstructorArg constructorArg : getConstructorArgs()) {
                if (constructorArg.sourcePropertyOf() == SourcePropertyOf.parent) MOBeansConstructionDefinitionException.createInvalidUsageOfSourceProeprtyOf(beanDescriptor.getBeanClass(), constructorArg.sourceProperty(), constructorArg);
            }
        }
        List<Class<?>> constructorParameters = new ArrayList<Class<?>>();
        for (MOConstructorArg constructorArg : getConstructorArgs()) {
            if (constructorArg.sourcePropertyOf() == SourcePropertyOf.parent) {
                if (parentBeanDescriptor.getJavaPropertyDescriptor(constructorArg.sourceProperty()) == null) throw MOBeansConstructionDefinitionException.createPropertyNotFound(constructorArg.sourceProperty(), parentBeanDescriptor.getBeanClass(), constructorArg);
                constructorParameters.add(parentBeanDescriptor.getJavaPropertyDescriptor(constructorArg.sourceProperty()).getPropertyType());
            } else {
                if (beanDescriptor.getJavaPropertyDescriptor(constructorArg.sourceProperty()) == null) throw MOBeansConstructionDefinitionException.createPropertyNotFound(constructorArg.sourceProperty(), beanDescriptor.getBeanClass(), constructorArg);
                constructorParameters.add(beanDescriptor.getJavaPropertyDescriptor(constructorArg.sourceProperty()).getPropertyType());
            }
        }
        for (MOInitializerProperty initializerProperty : getInitializerProperties()) {
            if (parentBeanDescriptor.getJavaPropertyDescriptor(initializerProperty.sourceProperty()) == null) throw MOBeansConstructionDefinitionException.createPropertyNotFound(initializerProperty.sourceProperty(), parentBeanDescriptor.getBeanClass(), initializerProperty);
            if (beanDescriptor.getJavaPropertyDescriptor(initializerProperty.targetProperty()) == null) throw MOBeansConstructionDefinitionException.createPropertyNotFound(initializerProperty.targetProperty(), beanDescriptor.getBeanClass(), initializerProperty);
        }
        Class[] argTypes = constructorParameters.toArray(new Class[constructorParameters.size()]);
        if (!hasFactoryMethod()) {
            if (hasFactoryClass()) throw MOBeansConstructionDefinitionException.createFactoryClassDefinedWithoutFactoryMethodName(beanDescriptor.getBeanClass());
            try {
                if (beanDescriptor.getBeanClass().isMemberClass() && !Modifier.isStatic(beanDescriptor.getBeanClass().getModifiers())) {
                    argTypes = insertOuterClassToParams(beanDescriptor.getBeanClass().getDeclaringClass(), argTypes);
                    beanDescriptor.getBeanClass().getConstructor(argTypes);
                } else {
                    beanDescriptor.getBeanClass().getConstructor(argTypes);
                }
            } catch (NoSuchMethodException e) {
                throw MOBeansConstructionDefinitionException.createConstructorNotFoundException(beanDescriptor.getBeanClass(), argTypes);
            }
        } else if (hasFactoryClass()) {
            try {
                Method m = constructionDescriptor.factoryClass().getMethod(constructionDescriptor.factoryMethod(), argTypes);
                if (!Modifier.isStatic(m.getModifiers())) throw MOBeansConstructionDefinitionException.createFactoryStaticException(constructionDescriptor.factoryClass(), argTypes, "static", "not static");
            } catch (NoSuchMethodException e) {
                throw MOBeansConstructionDefinitionException.createFactoryNotFoundException(constructionDescriptor.factoryClass(), argTypes);
            }
        } else {
            try {
                if (parentBeanDescriptor == null) throw MOBeansConstructionDefinitionException.createInstanceFactoryMiusedException(beanDescriptor.getBeanClass());
                Method m = parentBeanDescriptor.getBeanClass().getMethod(constructionDescriptor.factoryMethod(), argTypes);
                if (!Modifier.isStatic(m.getModifiers())) throw MOBeansConstructionDefinitionException.createFactoryStaticException(parentBeanDescriptor.getBeanClass(), argTypes, "not static", "static");
            } catch (NoSuchMethodException e) {
                throw MOBeansConstructionDefinitionException.createFactoryNotFoundException(parentBeanDescriptor.getBeanClass(), argTypes);
            }
        }
    }

    private Class[] insertOuterClassToParams(Class<?> declaringClass, Class[] argTypes) {
        Class[] newArgTypes = new Class[argTypes.length + 1];
        newArgTypes[0] = declaringClass;
        System.arraycopy(argTypes, 0, newArgTypes, 1, argTypes.length);
        return newArgTypes;
    }

    private boolean hasFactoryMethod() {
        return constructionDescriptor != null && !constructionDescriptor.factoryMethod().equals("");
    }

    private boolean hasFactoryClass() {
        return constructionDescriptor != null && constructionDescriptor.factoryClass() != Object.class;
    }
}
