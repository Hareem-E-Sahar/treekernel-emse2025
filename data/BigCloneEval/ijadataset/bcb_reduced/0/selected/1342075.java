package org.jamwiki.utils;

import java.io.File;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;

/**
 * This class provides utilities for working with classpath resources
 * such as property files.
 */
public abstract class ResourceUtil {

    private static final WikiLogger logger = WikiLogger.getLogger(ResourceUtil.class.getName());

    /** Sub-directory within the JAMWiki system directory that holds resource files. */
    private static final String RESOURCES_DIR = "resources";

    /** Sub-directory within the JAMWiki webapp root that holds resource setup files. */
    private static final String RESOURCES_SETUP_DIR = "setup";

    /**
	 * This method is a wrapper for Class.forName that will attempt to load a
	 * class from both the current thread context class loader and the default
	 * class loader.
	 *
	 * @param className The full class name that is to be initialized with the
	 *  <code>Class.forName</code> call.
	 * @throws ClassNotFoundException Thrown if the class cannot be initialized
	 *  from any class loader.
	 */
    public static void forName(String className) throws ClassNotFoundException {
        try {
            Class.forName(className, true, Thread.currentThread().getContextClassLoader());
            return;
        } catch (ClassNotFoundException e) {
            logger.info("Unable to load class " + className + " using the thread class loader, now trying the default class loader");
        }
        Class.forName(className);
    }

    /**
	 * Given a file name for a file that is located somewhere in the application
	 * classpath, return a File object representing the file.
	 *
	 * @param filename The name of the file (relative to the classpath) that is
	 *  to be retrieved.
	 * @return A file object representing the requested filename.  Note that the
	 *  file name is not guaranteed to match the filename passed to this method
	 *  since (for example) the file might be found in a JAR file and thus will
	 *  need to be copied to a temporary location for reading.
	 * @throws IOException Thrown if the classloader can not be found or if
	 *  the file can not be found in the classpath.
	 */
    public static File getClassLoaderFile(String filename) throws IOException {
        Resource resource = new ClassPathResource(filename);
        try {
            return resource.getFile();
        } catch (IOException e) {
        }
        InputStream is = null;
        FileOutputStream os = null;
        try {
            String tempFilename = RandomStringUtils.randomAlphanumeric(20);
            File file = File.createTempFile(tempFilename, null);
            is = resource.getInputStream();
            os = new FileOutputStream(file);
            IOUtils.copy(is, os);
            return file;
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }

    /**
	 * Attempt to get the class loader root directory.  This method works
	 * by searching for a file that MUST exist in the class loader root
	 * and then returning its parent directory.
	 *
	 * @return Returns a file indicating the directory of the class loader.
	 * @throws IOException Thrown if the class loader can not be found,
	 *  which may occur if this class is deployed without the jamwiki-war package.
	 */
    public static File getClassLoaderRoot() throws IOException {
        File file = ResourceUtil.getClassLoaderFile("sql/sql.ansi.properties");
        if (!file.exists()) {
            throw new IOException("Unable to find class loader root");
        }
        return file.getParentFile().getParentFile();
    }

    /**
	 * Retrieve a file from the JAMWiki system resources directory.  If the file
	 * does not exist then an attempt will be made to retrieve it from the
	 * classpath setup folder.
	 *
	 * @param filename The name of the file (relative to the JAMWiki system
	 *  resource directory) that is to be retrieved.
	 * @return A file object representing the requested filename.
	 * @throws IOException Thrown if the file can not be found in the
	 *  JAMWiki system resources directory.
	 */
    public static File getJAMWikiResourceFile(String filename) throws IOException {
        File resourceFile = null;
        if (Environment.isInitialized()) {
            File resourceDirectory = new File(Environment.getValue(Environment.PROP_BASE_FILE_DIR), RESOURCES_DIR);
            resourceFile = FileUtils.getFile(resourceDirectory, filename);
            if (!resourceFile.exists()) {
                File setupFile = ResourceUtil.getClassLoaderFile(new File(RESOURCES_SETUP_DIR, filename).getPath());
                if (setupFile.exists()) {
                    FileUtils.copyFile(setupFile, resourceFile);
                }
            }
            if (!resourceFile.exists()) {
                throw new FileNotFoundException("Resource file " + filename + " not found in system directory " + resourceDirectory.getAbsolutePath());
            }
        } else {
            resourceFile = ResourceUtil.getClassLoaderFile(new File(RESOURCES_SETUP_DIR, filename).getPath());
            if (!resourceFile.exists()) {
                throw new FileNotFoundException("Resource file " + filename + " not found in system setup resources.");
            }
        }
        return resourceFile;
    }

    /**
	 * Given a String representation of a class name (for example, org.jamwiki.db.AnsiDataHandler)
	 * return an instance of the class.  The constructor for the class being instantiated must
	 * not take any arguments.
	 *
	 * @param className The name of the class being instantiated.
	 * @return A Java Object representing an instance of the specified class.
	 */
    public static Object instantiateClass(String className) {
        if (StringUtils.isBlank(className)) {
            throw new IllegalArgumentException("Cannot call instantiateClass with an empty class name");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Instantiating class: " + className);
        }
        try {
            Class clazz = ClassUtils.forName(className, ClassUtils.getDefaultClassLoader());
            Class[] parameterTypes = new Class[0];
            Constructor constructor = clazz.getConstructor(parameterTypes);
            Object[] initArgs = new Object[0];
            return constructor.newInstance(initArgs);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Invalid class name specified: " + className, e);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Specified class does not have a valid constructor: " + className, e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Specified class does not have a valid constructor: " + className, e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Specified class does not have a valid constructor: " + className, e);
        } catch (InstantiationException e) {
            throw new IllegalStateException("Specified class could not be instantiated: " + className, e);
        }
    }

    /**
	 * Utility method for reading a file from a classpath directory and returning
	 * its contents as a String.
	 *
	 * @param filename The name of the file to be read, either as an absolute file
	 *  path or relative to the classpath.
	 * @return A string representation of the file contents.
	 * @throws IOException Thrown if the file cannot be found or if an I/O exception
	 *  occurs.
	 */
    public static String readFile(String filename) throws IOException {
        File file = new File(filename);
        if (file.exists()) {
            return FileUtils.readFileToString(file, "UTF-8");
        }
        file = getClassLoaderFile(filename);
        return FileUtils.readFileToString(file, "UTF-8");
    }
}
