package org.piuframework.context;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.piuframework.context.impl.XMLApplicationContext;
import org.piuframework.util.ClassUtils;

/**
 * Helper class with static utility methods that can be used in standalone applications.
 *
 * @author Dirk Mascher
 */
public class ApplicationContextHelper {

    public static final String SYSTEM_PROPERTY_CONTEXT_CLASS = "piuframework.context.class";

    private static final Log log = LogFactory.getLog(ApplicationContextHelper.class);

    private static ApplicationContext context;

    private static ThreadLocal contextInited = new ThreadLocal();

    private static void initStaticContext() throws ApplicationContextException {
        try {
            String contextClassName = System.getProperty(SYSTEM_PROPERTY_CONTEXT_CLASS);
            if (contextClassName == null) {
                context = new XMLApplicationContext(System.getProperties());
            } else {
                Class contextClass = ClassUtils.forName(contextClassName);
                Constructor constructor = contextClass.getConstructor(new Class[] { Map.class });
                context = (ApplicationContext) constructor.newInstance(new Object[] { System.getProperties() });
            }
        } catch (InvocationTargetException e) {
            log.error("failed to create ApplicationContext", e.getTargetException());
            throw new ApplicationContextException("failed to create ApplicationContext", e.getTargetException());
        } catch (Throwable t) {
            log.error("failed to create ApplicationContext", t);
            throw new ApplicationContextException("failed to create ApplicationContext", t);
        }
    }

    public static ApplicationContext getApplicationContext() throws ApplicationContextException {
        if (contextInited.get() == null) {
            synchronized (ApplicationContextHelper.class) {
                if (context == null) {
                    initStaticContext();
                }
            }
            contextInited.set(Boolean.TRUE);
        }
        return context;
    }

    /**
     * package scope method (only needed for JUnit testcase)
     */
    static void resetContext() {
        context = null;
        contextInited.set(null);
    }
}
