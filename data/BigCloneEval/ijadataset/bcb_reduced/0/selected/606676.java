package org.softnetwork.io;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author $Author: smanciot $
 *
 * @version $Revision: 84 $
 */
public class LibraryClassLoader extends ClassLoader {

    private Hashtable classes = new Hashtable();

    private List libraries, classPathes;

    private boolean debug;

    /**
	 * Constructor for LibraryLoader.
	 * @param parent
	 * @param classPath
	 */
    public LibraryClassLoader(ClassLoader parent, String classPath) {
        this(parent, classPath, false);
    }

    /**
	 * Constructor for LibraryLoader.
	 * @param parent
	 */
    public LibraryClassLoader(ClassLoader parent, String classPath, boolean _debug) {
        super(parent);
        this.debug = _debug;
        libraries = new ArrayList();
        classPathes = new ArrayList();
        if (classPath.indexOf(";") >= 0) {
            StringTokenizer str = new StringTokenizer(classPath, ";");
            while (str.hasMoreTokens()) {
                String token = str.nextToken();
                if (IOTools.getInstance().isDirectory(token)) {
                    classPathes.add(token);
                } else if (IOTools.getInstance().isLibrary(token)) {
                    libraries.add(token);
                }
            }
        } else {
            if (IOTools.getInstance().isDirectory(classPath)) {
                classPathes.add(classPath);
            } else if (IOTools.getInstance().isLibrary(classPath)) {
                libraries.add(classPath);
            }
        }
        if (debug) {
            Iterator it = libraries.iterator();
            while (it.hasNext()) {
                it.next();
                org.softnetwork.log.Log4jConnector.getConsole().debug(it.next());
            }
            it = classPathes.iterator();
            while (it.hasNext()) {
                org.softnetwork.log.Log4jConnector.getConsole().debug(it.next());
            }
        }
    }

    /**
	 * This is a simple version for external clients since they
	 * will always want the class resolved before it is returned
	 * to them.
	 */
    public synchronized Class loadClass(String className) throws ClassNotFoundException {
        return (loadClass(className, true));
    }

    /**
	 * This is the required version of loadClass which is called
	 * both from loadClass above and from the internal function
	 * FindClassFromClass.
	 */
    public synchronized Class loadClass(String className, boolean resolveIt) throws ClassNotFoundException {
        Class result = null;
        byte classData[];
        if (debug) org.softnetwork.log.Log4jConnector.getConsole().debug("        >>>>>> Load class : " + className);
        result = (Class) classes.get(className);
        if (result != null) {
            if (debug) org.softnetwork.log.Log4jConnector.getConsole().debug("        >>>>>> returning cached result.");
            return result;
        }
        try {
            result = super.findSystemClass(className);
            if (debug) org.softnetwork.log.Log4jConnector.getConsole().debug("        >>>>>> returning system class (in CLASSPATH).");
            return result;
        } catch (ClassNotFoundException e) {
            if (debug) org.softnetwork.log.Log4jConnector.getConsole().debug("        >>>>>> Not a system class.");
        }
        if (className.startsWith("java.") || className.startsWith("javax.")) throw new ClassNotFoundException();
        if ((classData = getClassImplFromClassPath(className)) == null) {
            classData = getClassImplFromLibraries(className);
        }
        if (classData == null) {
            throw new ClassNotFoundException();
        }
        result = defineClass(className, classData, 0, classData.length);
        if (result == null) {
            throw new ClassFormatError();
        }
        if (resolveIt) {
            resolveClass(result);
        }
        classes.put(className, result);
        if (debug) org.softnetwork.log.Log4jConnector.getConsole().debug("        >>>>>> Returning newly loaded class.");
        return result;
    }

    private byte[] getClassImplFromLibraries(String className) {
        try {
            String zipName = className.replace('.', '/') + ".class";
            Iterator it = libraries.iterator();
            ZipFile zip;
            ZipEntry entry;
            while (it.hasNext()) {
                String path = (String) it.next();
                zip = new ZipFile(path);
                if ((entry = zip.getEntry(zipName)) != null) {
                    if (debug) org.softnetwork.log.Log4jConnector.getConsole().debug("        >>>>>> Fetching the implementation of " + className + " from " + path);
                    return IOTools.getInstance().copyInputStream(zip.getInputStream(entry)).toByteArray();
                }
            }
            return null;
        } catch (Exception ex) {
            org.softnetwork.log.Log4jConnector.getConsole().error(ex.getMessage(), ex);
            return null;
        }
    }

    private byte[] getClassImplFromClassPath(String className) {
        try {
            String fileName = className.replace('.', IOTools.fs);
            Iterator it = classPathes.iterator();
            File f;
            while (it.hasNext()) {
                String classPath = (String) it.next();
                fileName = classPath + fileName + ".class";
                f = new File(fileName);
                if (f.exists()) {
                    if (debug) org.softnetwork.log.Log4jConnector.getConsole().debug("        >>>>>> Fetching the implementation of " + className);
                    return IOTools.getInstance().copyInputStream(new FileInputStream(f)).toByteArray();
                }
            }
            return null;
        } catch (Exception ex) {
            org.softnetwork.log.Log4jConnector.getConsole().error(ex.getMessage(), ex);
            return null;
        }
    }
}
