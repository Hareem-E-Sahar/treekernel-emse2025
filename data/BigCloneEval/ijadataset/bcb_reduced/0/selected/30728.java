package org.apache.xml.security.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * This class is extremely useful for loading resources and classes in a fault
 * tolerant manner that works across different applications servers. Do not
 * touch this unless you're a grizzled classloading guru veteran who is going to
 * verify any change on 6 different application servers.
 */
public final class ClassLoaderUtils {

    /** {@link org.apache.commons.logging} logging facility */
    private static final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(ClassLoaderUtils.class);

    private ClassLoaderUtils() {
    }

    /**
     * Load a given resource. <p/> This method will try to load the resource
     * using the following methods (in order):
     * <ul>
     * <li>From Thread.currentThread().getContextClassLoader()
     * <li>From ClassLoaderUtil.class.getClassLoader()
     * <li>callingClass.getClassLoader()
     * </ul>
     * 
     * @param resourceName The name of the resource to load
     * @param callingClass The Class object of the calling object
     */
    public static URL getResource(String resourceName, Class<?> callingClass) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(resourceName);
        if (url == null && resourceName.startsWith("/")) {
            url = Thread.currentThread().getContextClassLoader().getResource(resourceName.substring(1));
        }
        ClassLoader cluClassloader = ClassLoaderUtils.class.getClassLoader();
        if (cluClassloader == null) {
            cluClassloader = ClassLoader.getSystemClassLoader();
        }
        if (url == null) {
            url = cluClassloader.getResource(resourceName);
        }
        if (url == null && resourceName.startsWith("/")) {
            url = cluClassloader.getResource(resourceName.substring(1));
        }
        if (url == null) {
            ClassLoader cl = callingClass.getClassLoader();
            if (cl != null) {
                url = cl.getResource(resourceName);
            }
        }
        if (url == null) {
            url = callingClass.getResource(resourceName);
        }
        if ((url == null) && (resourceName != null) && (resourceName.charAt(0) != '/')) {
            return getResource('/' + resourceName, callingClass);
        }
        return url;
    }

    /**
     * Load a given resources. <p/> This method will try to load the resources
     * using the following methods (in order):
     * <ul>
     * <li>From Thread.currentThread().getContextClassLoader()
     * <li>From ClassLoaderUtil.class.getClassLoader()
     * <li>callingClass.getClassLoader()
     * </ul>
     * 
     * @param resourceName The name of the resource to load
     * @param callingClass The Class object of the calling object
     */
    public static List<URL> getResources(String resourceName, Class<?> callingClass) {
        List<URL> ret = new ArrayList<URL>();
        Enumeration<URL> urls = new Enumeration<URL>() {

            public boolean hasMoreElements() {
                return false;
            }

            public URL nextElement() {
                return null;
            }
        };
        try {
            urls = Thread.currentThread().getContextClassLoader().getResources(resourceName);
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.debug(e);
            }
        }
        if (!urls.hasMoreElements() && resourceName.startsWith("/")) {
            try {
                urls = Thread.currentThread().getContextClassLoader().getResources(resourceName.substring(1));
            } catch (IOException e) {
                if (log.isDebugEnabled()) {
                    log.debug(e);
                }
            }
        }
        ClassLoader cluClassloader = ClassLoaderUtils.class.getClassLoader();
        if (cluClassloader == null) {
            cluClassloader = ClassLoader.getSystemClassLoader();
        }
        if (!urls.hasMoreElements()) {
            try {
                urls = cluClassloader.getResources(resourceName);
            } catch (IOException e) {
                if (log.isDebugEnabled()) {
                    log.debug(e);
                }
            }
        }
        if (!urls.hasMoreElements() && resourceName.startsWith("/")) {
            try {
                urls = cluClassloader.getResources(resourceName.substring(1));
            } catch (IOException e) {
                if (log.isDebugEnabled()) {
                    log.debug(e);
                }
            }
        }
        if (!urls.hasMoreElements()) {
            ClassLoader cl = callingClass.getClassLoader();
            if (cl != null) {
                try {
                    urls = cl.getResources(resourceName);
                } catch (IOException e) {
                    if (log.isDebugEnabled()) {
                        log.debug(e);
                    }
                }
            }
        }
        if (!urls.hasMoreElements()) {
            URL url = callingClass.getResource(resourceName);
            if (url != null) {
                ret.add(url);
            }
        }
        while (urls.hasMoreElements()) {
            ret.add(urls.nextElement());
        }
        if (ret.isEmpty() && (resourceName != null) && (resourceName.charAt(0) != '/')) {
            return getResources('/' + resourceName, callingClass);
        }
        return ret;
    }

    /**
     * This is a convenience method to load a resource as a stream. <p/> The
     * algorithm used to find the resource is given in getResource()
     * 
     * @param resourceName The name of the resource to load
     * @param callingClass The Class object of the calling object
     */
    public static InputStream getResourceAsStream(String resourceName, Class<?> callingClass) {
        URL url = getResource(resourceName, callingClass);
        try {
            return (url != null) ? url.openStream() : null;
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.debug(e);
            }
            return null;
        }
    }

    /**
     * Load a class with a given name. <p/> It will try to load the class in the
     * following order:
     * <ul>
     * <li>From Thread.currentThread().getContextClassLoader()
     * <li>Using the basic Class.forName()
     * <li>From ClassLoaderUtil.class.getClassLoader()
     * <li>From the callingClass.getClassLoader()
     * </ul>
     * 
     * @param className The name of the class to load
     * @param callingClass The Class object of the calling object
     * @throws ClassNotFoundException If the class cannot be found anywhere.
     */
    public static Class<?> loadClass(String className, Class<?> callingClass) throws ClassNotFoundException {
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl != null) {
                return cl.loadClass(className);
            }
        } catch (ClassNotFoundException e) {
            if (log.isDebugEnabled()) {
                log.debug(e);
            }
        }
        return loadClass2(className, callingClass);
    }

    private static Class<?> loadClass2(String className, Class<?> callingClass) throws ClassNotFoundException {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
            try {
                if (ClassLoaderUtils.class.getClassLoader() != null) {
                    return ClassLoaderUtils.class.getClassLoader().loadClass(className);
                }
            } catch (ClassNotFoundException exc) {
                if (callingClass != null && callingClass.getClassLoader() != null) {
                    return callingClass.getClassLoader().loadClass(className);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug(ex);
            }
            throw ex;
        }
    }
}
