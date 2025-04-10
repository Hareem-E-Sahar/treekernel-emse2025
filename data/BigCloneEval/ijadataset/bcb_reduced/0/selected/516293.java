package org.clearsilver;

import java.lang.reflect.Constructor;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class holds static methods for getting and setting the CS and HDF
 * factory used throughout the Java Clearsilver Framework.
 * Clients are <strong>strongly encouraged</strong> to not use this class, and
 * instead directly inject {@link ClearsilverFactory} into the classes that
 * need to create {@link HDF} and {@link CS} instances.
 * For now, projects should set the {@link ClearsilverFactory} in FactoryLoader
 * and use the singleton accessor {@link #getClearsilverFactory()} if proper
 * dependency injection is not easy to implement.
 * <p>
 * Allows the default implementation to be the original JNI version without
 * requiring users that don't want to use the JNI version to have to link
 * it in.  The ClearsilverFactory object to use can be either passed into the
 * {@link #setClearsilverFactory} method or the class name can be specified
 * in the Java property {@code  org.clearsilver.defaultClearsilverFactory}.
 *
 * @author Sergio Marti (smarti@google.com)
 */
public final class FactoryLoader {

    private static final Logger logger = Logger.getLogger(FactoryLoader.class.getName());

    private static final String DEFAULT_CS_FACTORY_CLASS_PROPERTY_NAME = "org.clearsilver.defaultClearsilverFactory";

    private static final String DEFAULT_CS_FACTORY_CLASS_NAME = "org.clearsilver.jni.JniClearsilverFactory";

    private static ClearsilverFactory clearsilverFactory = null;

    private static final ReadWriteLock factoryLock = new ReentrantReadWriteLock();

    /**
   * Get the {@link org.clearsilver.ClearsilverFactory} object to be used by
   * disparate parts of the application.
   */
    public static ClearsilverFactory getClearsilverFactory() {
        factoryLock.readLock().lock();
        if (clearsilverFactory == null) {
            factoryLock.readLock().unlock();
            factoryLock.writeLock().lock();
            try {
                if (clearsilverFactory == null) {
                    clearsilverFactory = newDefaultClearsilverFactory();
                }
                factoryLock.readLock().lock();
            } finally {
                factoryLock.writeLock().unlock();
            }
        }
        ClearsilverFactory returned = clearsilverFactory;
        factoryLock.readLock().unlock();
        return returned;
    }

    /**
   * Set the {@link org.clearsilver.ClearsilverFactory} to be used by
   * the application. If parameter is {@code null}, then the default factory
   * implementation will be used the next time {@link #getClearsilverFactory()}
   * is called.
   *
   * @return the previous factory (may return {@code null})
   */
    public static ClearsilverFactory setClearsilverFactory(ClearsilverFactory clearsilverFactory) {
        factoryLock.writeLock().lock();
        try {
            ClearsilverFactory previousFactory = FactoryLoader.clearsilverFactory;
            FactoryLoader.clearsilverFactory = clearsilverFactory;
            return previousFactory;
        } finally {
            factoryLock.writeLock().unlock();
        }
    }

    private static ClearsilverFactory newDefaultClearsilverFactory() {
        String factoryClassName = System.getProperty(DEFAULT_CS_FACTORY_CLASS_PROPERTY_NAME, DEFAULT_CS_FACTORY_CLASS_NAME);
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Class<ClearsilverFactory> clazz = loadClass(factoryClassName, classLoader);
            Constructor<ClearsilverFactory> constructor = clazz.getConstructor();
            return constructor.newInstance();
        } catch (Exception e) {
            String errMsg = "Unable to load default ClearsilverFactory class: \"" + factoryClassName + "\"";
            logger.log(Level.SEVERE, errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
    }

    private static Class<ClearsilverFactory> loadClass(String className, ClassLoader classLoader) throws ClassNotFoundException {
        return (Class<ClearsilverFactory>) Class.forName(className, true, classLoader);
    }
}
