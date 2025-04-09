package ro.codemart.installer.extractor;

import ro.codemart.installer.extractor.util.ExtractorUtils;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.zip.ZipException;
import java.util.jar.JarInputStream;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLClassLoader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Extracts the installer
 */
public class InstallerExtractor {

    private static final String EXPLODED_DIR = "exploded";

    public static final String TMP_DIR_PREFIX = "tmp_nestedjar";

    private static final String LIB_DIR_3RD_PARTY = "/lib/3rd-party/";

    private static final String LIB_DIR = "/lib/";

    private static final String RESOURCES = "/resources/";

    private static final String JAR_FILE = "jar:file:";

    private static final String JAR_HTTP = "jar:http:";

    private List<File> libraries = new ArrayList<File>();

    private File explodedDir;

    private File tmpDir;

    private String appMainClass;

    public static void main(String[] args) throws Exception {
        final InstallerExtractor extractor = new InstallerExtractor();
        extractor.expand();
        Runtime.getRuntime().addShutdownHook(new Thread() {

            public void run() {
                ExtractorUtils.delete(extractor.explodedDir);
            }
        });
        extractor.executeApplicationMainClass(args);
    }

    /**
     * Finds the enclosing JAR file
     *
     * @return the enclosing jar file
     * @throws java.io.IOException if app could not open jar stream
     */
    private InputStream getEnclosingJar() throws IOException {
        String fileName = "/" + this.getClass().getName().replace(".", "/") + ".class";
        URL url = this.getClass().getResource(fileName);
        String urlFile = url.toString();
        String delim = "!/";
        int index;
        if (!urlFile.startsWith("jar:")) {
            throw new RuntimeException("The protocol doesn't start with jar:");
        } else if ((index = urlFile.indexOf(delim)) == -1) {
            throw new RuntimeException("Can't find the enclosing JAR");
        }
        if (urlFile.startsWith(JAR_FILE)) {
            String jarPath = urlFile.substring(JAR_FILE.length(), index);
            jarPath = URLDecoder.decode(jarPath, "UTF-8");
            File jar = new File(jarPath);
            if (!jar.exists()) {
                throw new RuntimeException("The jar file : " + jarPath + " doesn't exist");
            }
            return new FileInputStream(jar);
        } else if (urlFile.startsWith(JAR_HTTP)) {
            String jarPath = urlFile.substring("jar:".length(), index);
            jarPath = URLDecoder.decode(jarPath, "UTF-8");
            URL jarUrl = new URL(jarPath);
            return jarUrl.openStream();
        } else {
            throw new RuntimeException("Could not process JAR url: " + urlFile);
        }
    }

    /**
     * Expands the enclosing jar
     *
     * @throws Exception if the jar/zip cannot be found
     */
    public void expand() throws Exception {
        JarInputStream jarInputStream = null;
        explodedDir = ExtractorUtils.createTempFolder(EXPLODED_DIR, true);
        explodedDir.deleteOnExit();
        try {
            jarInputStream = new JarInputStream(getEnclosingJar());
            getApplicationMainClass(jarInputStream);
            int available = jarInputStream.available();
            tmpDir = ExtractorUtils.createTempFolder(explodedDir, TMP_DIR_PREFIX);
            tmpDir.deleteOnExit();
            JarEntry jarEntry = jarInputStream.getNextJarEntry();
            if (available > 0 && jarEntry == null) {
                throw new ZipException("No zip/jar entry found. Invalid jar file.");
            }
            while (jarEntry != null) {
                File file = new File(tmpDir, jarEntry.getName());
                if (jarEntry.isDirectory()) {
                    file.mkdirs();
                }
                addJarsToLibs(jarInputStream, file);
                jarInputStream.closeEntry();
                jarEntry = jarInputStream.getNextJarEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (jarInputStream != null) {
                try {
                    jarInputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Add the jar files to the list of jar libraries
     *
     * @param jarInputStream jarInputStream
     * @param file           the directory with jars or jar file to be added to libraries list
     * @throws IOException if something goes wrong
     */
    private void addJarsToLibs(JarInputStream jarInputStream, File file) throws IOException {
        if (file.isDirectory()) {
            if (!file.exists()) {
                file.mkdirs();
            }
            for (File someFile : file.listFiles()) {
                addJarsToLibs(jarInputStream, someFile);
            }
            file.deleteOnExit();
        } else {
            file.deleteOnExit();
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.getParentFile().deleteOnExit();
            }
            BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));
            ExtractorUtils.copyStream(jarInputStream, os);
            os.close();
            if (file.getName().endsWith(".jar")) {
                libraries.add(file);
            }
        }
    }

    /**
     * Finds the application's main class
     *
     * @param jarInputStream jar input stream
     * @throws IOException cannot find the jar containing the manifest file to get the main class
     */
    private void getApplicationMainClass(JarInputStream jarInputStream) throws IOException {
        Manifest manifest = jarInputStream.getManifest();
        if (manifest != null) {
            Attributes attr = manifest.getMainAttributes();
            if (attr != null) {
                appMainClass = attr.getValue("Application-Main-Class");
            }
        }
    }

    /**
     * Delegates the execution to the application's main class
     *
     * @param args the arguments
     */
    public void executeApplicationMainClass(String[] args) {
        Field sysPathsField = null;
        Object oldFieldValue = null;
        boolean accessible = false;
        try {
            for (File lib : libraries) {
                addToClasspath(lib);
            }
            String s = System.getProperty("java.library.path");
            if (s != null) {
                s += File.pathSeparatorChar + tmpDir.getCanonicalPath() + LIB_DIR_3RD_PARTY + File.pathSeparatorChar + tmpDir.getCanonicalPath() + LIB_DIR;
            } else {
                s = tmpDir.getCanonicalPath() + LIB_DIR_3RD_PARTY + File.pathSeparatorChar + tmpDir.getCanonicalPath() + LIB_DIR;
            }
            s += File.pathSeparator + tmpDir.getCanonicalPath() + RESOURCES;
            sysPathsField = getSysPathsField();
            if (sysPathsField != null) {
                accessible = sysPathsField.isAccessible();
                if (!accessible) {
                    sysPathsField.setAccessible(true);
                }
                oldFieldValue = sysPathsField.get(ClassLoader.class);
                sysPathsField.set(ClassLoader.class, null);
            }
            System.setProperty("java.library.path", s);
            Class clazz = Class.forName(appMainClass);
            Method m = clazz.getMethod("main", new Class[] { args.getClass() });
            m.invoke(null, new Object[] { args });
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Can't load the main class", e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Can't find the main() method of the main class", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (sysPathsField != null) {
                try {
                    sysPathsField.set(ClassLoader.class, oldFieldValue);
                } catch (IllegalAccessException e) {
                }
                sysPathsField.setAccessible(accessible);
            }
        }
    }

    /**
     * Returns the sys_paths field of the ClassLoader or null if it doesn't exist
     *
     * @return the {@code Field} object for sys_paths field
     */
    private Field getSysPathsField() {
        try {
            return ClassLoader.class.getDeclaredField("sys_paths");
        } catch (NoSuchFieldException e) {
            System.out.println("Field sys_paths was not found : " + e.getMessage());
        }
        return null;
    }

    /**
     * Adds the jar to the list of URLs of the class loader
     *
     * @param jarFile the jar
     */
    private void addToClasspath(File jarFile) {
        try {
            URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Method m = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
            m.setAccessible(true);
            m.invoke(urlClassLoader, jarFile.toURI().toURL());
            String cp = System.getProperty("java.class.path");
            if (cp != null) {
                cp += File.pathSeparatorChar + jarFile.toURI().getPath();
            } else {
                cp = jarFile.toURI().getPath();
            }
            System.setProperty("java.class.path", cp);
        } catch (Exception e) {
            throw new RuntimeException("Can't add jar " + jarFile.getName() + " to classpath.", e);
        }
    }
}
