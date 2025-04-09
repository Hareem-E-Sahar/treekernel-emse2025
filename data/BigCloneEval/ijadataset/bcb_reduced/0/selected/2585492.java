package net.sourceforge.chimeralibrary.ioc.context;

import java.lang.reflect.Constructor;
import java.util.Map;
import net.sourceforge.chimeralibrary.ioc.Construct;
import net.sourceforge.chimeralibrary.ioc.IoCConstants;
import net.sourceforge.chimeralibrary.ioc.bean.Bean;
import net.sourceforge.chimeralibrary.ioc.factorybean.FactoryBean;
import net.sourceforge.chimeralibrary.oxm.ElementHandler;
import net.sourceforge.chimeralibrary.oxm.OXContainer;
import net.sourceforge.chimeralibrary.oxm.util.OXUtility;
import org.dom4j.Element;

/**
 * Utility class for IoC support.
 * 
 * @author Christian Cruz
 * @version 1.0.000
 * @since 1.0.000
 */
public class ApplicationContextUtility implements IoCConstants {

    /**
	 * Returns a bean object within the map of beans given the bean id.
	 * 
	 * @param map the map of beans
	 * @param beanId the bean id to search within the map
	 * @return a bean object within the map of beans given the bean id
	 */
    public static final Bean getBean(final Map<Bean, Element> map, final String beanId) {
        for (final Bean bean : map.keySet()) {
            if (beanId.equals(bean.getId())) {
                return bean;
            }
        }
        throw new IllegalArgumentException("Bean not found for reference: '" + beanId + "'");
    }

    /**
	 * Creates a bean constructor using the XML root element.
	 * 
	 * @param root the XML root element that defines the bean constructor
	 * @param elementHandler the custom element handler
	 * @return a bean constructor using the XML root element
	 */
    public static final Construct getConstructor(final Element root, final ElementHandler elementHandler) {
        if (root != null) {
            final Element element = OXUtility.getElement(CONSTRUCTOR, root);
            if (element != null) {
                final Construct construct = OXContainer.getObject(element, Construct.class, elementHandler);
                root.remove(element);
                return construct;
            }
        }
        return null;
    }

    /**
	 * Initializes the bean and instantiates the encapsulated object.
	 * 
	 * @param applicationContext the ApplicationContext
	 * @param bean the Bean object
	 * @return the object defined by the bean
	 * @throws Exception If exception occurred during bean creation
	 */
    public static final Object getObject(final ApplicationContext applicationContext, final Bean bean) throws Exception {
        return getObject(applicationContext, bean, null);
    }

    /**
	 * Initializes the bean and instantiates the encapsulated object.
	 * 
	 * @param applicationContext the ApplicationContext
	 * @param bean the Bean object
	 * @param construct the bean constructor
	 * @return the object defined by the bean
	 * @throws Exception If exception occurred during bean creation
	 */
    public static final Object getObject(final ApplicationContext applicationContext, final Bean bean, final Construct construct) throws Exception {
        final Class<?> clazz = bean.getType();
        Object object = null;
        Object[] args = null;
        Integer constructArgsLength = 0;
        if ((construct != null) && (construct.getArguments() != null)) {
            args = construct.getArguments().toArray();
            constructArgsLength = args.length;
        }
        for (final Constructor<?> constructor : clazz.getConstructors()) {
            if (constructor.getParameterTypes().length == constructArgsLength) {
                object = constructor.newInstance(args == null ? new Object[0] : args);
                if (bean.getStaticField() != null) {
                    object = object.getClass().getField(bean.getStaticField()).get(object);
                }
                break;
            }
        }
        if ((object == null) && (bean.getFactoryMethod() != null)) {
            object = bean.getType().getMethod(bean.getFactoryMethod()).invoke(new Object[0]);
        }
        if (object != null) {
            bean.setCreated(true);
            if (object instanceof FactoryBean<?>) {
                final FactoryBean<?> factoryBean = (FactoryBean<?>) object;
                applicationContext.addObject(bean.getId() == null ? null : AMPERSAND.concat(bean.getId()), factoryBean);
                applicationContext.addObject(bean.getId(), factoryBean.getObject());
            } else {
                applicationContext.addObject(bean.getId(), object);
            }
            return object;
        }
        throw new ApplicationContextException("Could not instantiate: " + bean);
    }

    /**
	 * Invokes the underlying method represented by the method name of a bean object and if applicable, returns the object value.
	 * 
	 * @param applicationContext the ApplicationContext object
	 * @param beanId the bean id
	 * @param method the method name to be invoked
	 * @return the result of dispatching the method
	 */
    public static final Object invokeBeanMethod(final ApplicationContext applicationContext, final String beanId, final String method) {
        if ((beanId != null) && (method != null)) {
            try {
                final Object object = applicationContext.getObject(beanId);
                if (object != null) {
                    return object.getClass().getMethod(method, new Class[0]).invoke(object, new Object[0]);
                }
            } catch (final Exception e) {
                throw new ApplicationContextException(e);
            }
        }
        return null;
    }
}
