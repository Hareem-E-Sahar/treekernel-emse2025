package org.teaframework.util.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

/**
 * A class to simplify access to resources through the classloader.
 * 
 * @author <a href="mailto:founder_chen@yahoo.com.cn">Peter Cheng </a>
 * @author <a href="mailto:clinton.begin@ibatis.com">Clinton Begin </a>
 * @version $Revision: 1.6 $ $Date: 2005/05/22 06:50:55 $
 * @version Revision: 1.0
 */
public class ResourceHelper {

    private static ClassLoader defaultClassLoader;

    public ResourceHelper() {
    }

    /**
     * Returns the default classloader (may be null).
     * 
     * @return The default classloader
     */
    public static ClassLoader getDefaultClassLoader() {
        return defaultClassLoader;
    }

    /**
     * Sets the default classloader
     * 
     * @param defaultClassLoader - the new default ClassLoader
     */
    public static void setDefaultClassLoader(ClassLoader defaultClassLoader) {
        ResourceHelper.defaultClassLoader = defaultClassLoader;
    }

    /**
     * Returns the URL of the resource on the classpath
     * 
     * @param resource The resource to find
     * @return The resource
     * @throws java.io.IOException If the resource cannot be found or read
     */
    public static URL getResourceURL(String resource) throws IOException {
        return getResourceURL(getClassLoader(), resource);
    }

    /**
     * Returns the URL of the resource on the classpath
     * 
     * @param loader The classloader used to load the resource
     * @param resource The resource to find
     * @return The resource
     * @throws java.io.IOException If the resource cannot be found or read
     */
    public static URL getResourceURL(ClassLoader loader, String resource) throws IOException {
        URL url = null;
        if (loader != null) {
            url = loader.getResource(resource);
        }
        if (url == null) {
            url = ClassLoader.getSystemResource(resource);
        }
        if (url == null) {
            throw new IOException("Could not find resource " + resource);
        }
        return url;
    }

    /**
     * Returns a resource on the classpath as a Stream object
     * 
     * @param resource The resource to find
     * @return The resource
     * @throws java.io.IOException If the resource cannot be found or read
     */
    public static InputStream getResourceAsStream(String resource) throws IOException {
        return getResourceAsStream(getClassLoader(), resource);
    }

    /**
     * Returns a resource on the classpath as a Stream object
     * 
     * @param loader The classloader used to load the resource
     * @param resource The resource to find
     * @return The resource
     * @throws java.io.IOException If the resource cannot be found or read
     */
    public static InputStream getResourceAsStream(ClassLoader loader, String resource) throws IOException {
        InputStream in = null;
        if (loader != null) {
            in = loader.getResourceAsStream(resource);
        }
        if (in == null) {
            in = ClassLoader.getSystemResourceAsStream(resource);
        }
        if (in == null) {
            throw new IOException("Could not find resource " + resource);
        }
        return in;
    }

    /**
     * Returns a resource on the classpath as a Properties object
     * 
     * @param resource The resource to find
     * @return The resource
     * @throws java.io.IOException If the resource cannot be found or read
     */
    public static Properties getResourceAsProperties(String resource) throws IOException {
        Properties props = new Properties();
        InputStream in = null;
        String propfile = resource;
        in = getResourceAsStream(propfile);
        props.load(in);
        in.close();
        return props;
    }

    /**
     * Returns a resource on the classpath as a Properties object
     * 
     * @param loader The classloader used to load the resource
     * @param resource The resource to find
     * @return The resource
     * @throws java.io.IOException If the resource cannot be found or read
     */
    public static Properties getResourceAsProperties(ClassLoader loader, String resource) throws IOException {
        Properties props = new Properties();
        InputStream in = null;
        String propfile = resource;
        in = getResourceAsStream(loader, propfile);
        props.load(in);
        in.close();
        return props;
    }

    /**
     * Returns a resource on the classpath as a Reader object
     * 
     * @param resource The resource to find
     * @return The resource
     * @throws java.io.IOException If the resource cannot be found or read
     */
    public static Reader getResourceAsReader(String resource) throws IOException {
        return new InputStreamReader(getResourceAsStream(resource));
    }

    /**
     * Returns a resource on the classpath as a Reader object
     * 
     * @param loader The classloader used to load the resource
     * @param resource The resource to find
     * @return The resource
     * @throws java.io.IOException If the resource cannot be found or read
     */
    public static Reader getResourceAsReader(ClassLoader loader, String resource) throws IOException {
        return new InputStreamReader(getResourceAsStream(loader, resource));
    }

    /**
     * Returns a resource on the classpath as a File object
     * 
     * @param resource The resource to find
     * @return The resource
     * @throws java.io.IOException If the resource cannot be found or read
     */
    public static File getResourceAsFile(String resource) throws IOException {
        return new File(getResourceURL(resource).getFile());
    }

    /**
     * Returns a resource on the classpath as a File object
     * 
     * @param loader - the classloader used to load the resource
     * @param resource - the resource to find
     * @return The resource
     * @throws java.io.IOException If the resource cannot be found or read
     */
    public static File getResourceAsFile(ClassLoader loader, String resource) throws IOException {
        return new File(getResourceURL(loader, resource).getFile());
    }

    /**
     * Gets a URL as an input stream
     * 
     * @param urlString - the URL to get
     * @return An input stream with the data from the URL
     * @throws java.io.IOException If the resource cannot be found or read
     */
    public static InputStream getUrlAsStream(String urlString) throws IOException {
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        return conn.getInputStream();
    }

    /**
     * Gets a URL as a Reader
     * 
     * @param urlString - the URL to get
     * @return A Reader with the data from the URL
     * @throws java.io.IOException If the resource cannot be found or read
     */
    public static Reader getUrlAsReader(String urlString) throws IOException {
        return new InputStreamReader(getUrlAsStream(urlString));
    }

    /**
     * Gets a URL as a Properties object
     * 
     * @param urlString - the URL to get
     * @return A Properties object with the data from the URL
     * @throws java.io.IOException If the resource cannot be found or read
     */
    public static Properties getUrlAsProperties(String urlString) throws IOException {
        Properties props = new Properties();
        InputStream in = null;
        String propfile = urlString;
        in = getUrlAsStream(propfile);
        props.load(in);
        in.close();
        return props;
    }

    /**
     * Loads a class
     * 
     * @param className - the class to load
     * @return The loaded class
     * @throws ClassNotFoundException If the class cannot be found (duh!)
     */
    public static Class classForName(String className) throws ClassNotFoundException {
        Class clazz = null;
        clazz = getClassLoader().loadClass(className);
        if (clazz == null) {
            clazz = Class.forName(className);
        }
        return clazz;
    }

    /**
     * Creates an instance of a class
     * 
     * @param className - the class to create
     * @return An instance of the class
     * @throws ClassNotFoundException If the class cannot be found (duh!)
     * @throws InstantiationException If the class cannot be instantiaed
     * @throws IllegalAccessException If the class is not public, or other
     *             access problems arise
     */
    public static Object instantiate(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        return instantiate(classForName(className));
    }

    /**
     * Creates an instance of a class
     * 
     * @param clazz - the class to create
     * @return An instance of the class
     * @throws InstantiationException If the class cannot be instantiaed
     * @throws IllegalAccessException If the class is not public, or other
     *             access problems arise
     */
    public static Object instantiate(Class clazz) throws InstantiationException, IllegalAccessException {
        return clazz.newInstance();
    }

    private static ClassLoader getClassLoader() {
        if (defaultClassLoader != null) {
            return defaultClassLoader;
        } else {
            return Thread.currentThread().getContextClassLoader();
        }
    }
}
