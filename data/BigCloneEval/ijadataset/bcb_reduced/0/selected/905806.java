package net.sf.dozer.util.mapping;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.sf.dozer.util.mapping.config.GlobalSettings;
import net.sf.dozer.util.mapping.jmx.DozerAdminController;
import net.sf.dozer.util.mapping.jmx.DozerStatisticsController;
import net.sf.dozer.util.mapping.util.InitLogger;
import net.sf.dozer.util.mapping.util.MapperConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Internal class that performs one time Dozer initializations. Only intended for internal use.
 * 
 * @author tierney.matt
 */
public class DozerInitializer {

    private static final Log log = LogFactory.getLog(DozerInitializer.class);

    private static boolean isInitialized = false;

    public static void init() {
        if (isInitialized) {
            return;
        }
        InitLogger.log(log, "Initializing Dozer.  Version: " + MapperConstants.CURRENT_VERSION + ", Thread Name:" + Thread.currentThread().getName() + ", Is this JDK 1.5.x?:" + GlobalSettings.getInstance().isJava5());
        if (GlobalSettings.getInstance().isAutoregisterJMXBeans()) {
            if (!areJMXMgmtClassesAvailable()) {
                InitLogger.log(log, "jdk1.5 management classes unavailable.  Dozer JMX MBeans will not be auto registered.");
            } else {
                try {
                    registerJMXBeans();
                } catch (Throwable t) {
                    log.warn("Unable to register Dozer JMX MBeans with the PlatformMBeanServer.  Dozer will still function " + "normally, but management via JMX may not be available", t);
                }
            }
        }
        isInitialized = true;
    }

    private static boolean areJMXMgmtClassesAvailable() {
        boolean result = false;
        try {
            Class.forName("java.lang.management.ManagementFactory");
            Class.forName("javax.management.ObjectName");
            Class.forName("javax.management.MBeanServer");
            result = true;
        } catch (Throwable t) {
            result = false;
        }
        return result;
    }

    protected static boolean isInitialized() {
        return isInitialized;
    }

    private static void registerJMXBeans() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        registerJMXBean("net.sf.dozer.util.mapping.jmx:type=DozerStatisticsController", new DozerStatisticsController());
        registerJMXBean("net.sf.dozer.util.mapping.jmx:type=DozerAdminController", new DozerAdminController());
    }

    private static void registerJMXBean(String mbeanName, Object mbean) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        Class mgmtFactoryClass = Class.forName("java.lang.management.ManagementFactory");
        Class objectNameClass = Class.forName("javax.management.ObjectName");
        Class mbsClass = Class.forName("javax.management.MBeanServer");
        Constructor objectNameConstructor = objectNameClass.getConstructor(new Class[] { String.class });
        Object mbeanObjectName = objectNameConstructor.newInstance(new Object[] { mbeanName });
        Object mbs = mgmtFactoryClass.getMethod("getPlatformMBeanServer", null).invoke(null, null);
        Method isMBeanRegisteredMethod = mbsClass.getMethod("isRegistered", new Class[] { objectNameClass });
        Boolean isMBeanRegistered = (Boolean) isMBeanRegisteredMethod.invoke(mbs, new Object[] { mbeanObjectName });
        if (isMBeanRegistered.booleanValue()) {
            InitLogger.log(log, "Dozer JMX MBean [" + mbeanName + "] already registered.  Unregistering the existing MBean.");
            Method unregisterMBeanMethod = mbsClass.getMethod("unregisterMBean", new Class[] { objectNameClass });
            unregisterMBeanMethod.invoke(mbs, new Object[] { mbeanObjectName });
        }
        Method registerMBeanMethod = mbsClass.getMethod("registerMBean", new Class[] { Object.class, objectNameClass });
        registerMBeanMethod.invoke(mbs, new Object[] { mbean, mbeanObjectName });
        InitLogger.log(log, "Dozer JMX MBean [" + mbeanName + "] auto registered with the Platform MBean Server");
    }
}
