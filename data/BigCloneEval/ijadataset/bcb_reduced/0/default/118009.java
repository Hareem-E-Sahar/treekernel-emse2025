import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.lang.reflect.*;
import java.net.*;

/**
 * A class to make java archives executeable. <p>
 *
 * The usual way to run java programs is to create an archive that
 * contains all classes for the app, add this archive to the classpath and run
 * the jvm whith an argument that specifies a class in the archive. <p>
 *
 * How to modify the classpath is platform dependent and on some systems
 * not that easy (eg. mac). <p>
 *
 * To make an archive "executeable" we place a class file in front of the archive.
 * The user only needs to run the java interpreter on that class. <p>
 *
 * To use this class you need to 
 * <ul>
 * <li>rename this class to whatever name the resulting class should have
 *      if you want the resulting archive not to have the name "Install.class".
 *      Note that you can not create a subclass of this class because this is
 *      the ClassLoader that you need to load this class ...
 *
 * <li>change the name of the class to run. The class to run must be a
 *     member of the archive. Its name is given in the Field "classToRun".
 *     The class you want to run needs a "main" method.
 *
 * <li>compile this class.
 *
 * <li>concatenate the compiled class and your java archive.
 *     The resulting archive must have the same name as this class, the name must end
 *     with ".class". <p>
 *     On unix you can do that with somethinhg like
 *     <code>cat Install.class &lt;java-archive&gt &gt; /tmp/Install.class</code>.
 *
 * <li>as a last step you need to fix the archive directory. On unix you can do
 *     that with <code>zip -F /tmp/Install.class</code>. You can not use <code>jar</code>
 *     for this step.
 * </ul>
 *
 *
 * This class acts as a class loader for the self extracting archive.<p>
 *
 * <b>Note</b><p>
 *
 * If you want to use this class for your own applications, note that this code depends
 * on an <code>URLStreamHandlerFactory</code> with the name 
 * <code>installer/source/ResURLHandlerFactory</code> to be present in the archive.
 * This handler factory will be loaded and installed in the "main" Method of this
 * class to support the <code>getResource</code> Method in this class. If your
 * own application needs its own handler factory you need to include the code from
 * <code>installer/source/ResURLHandlerFactory.java</code> in your own code.
 *
 * @author Andreas Hofmeister
 * @version $Revision: 1.2 $
 *
 */
public class Install extends ClassLoader {

    public static final String classToRun = "net.sourceforge.liftoff.installer.Install2";

    private Hashtable loadedClasses;

    private ZipFile archive;

    private int offset = 0;

    private static Class uhfc = null;

    private static URLStreamHandlerFactory uhf = null;

    /**
     * create a new loader object. 
     * Remember to Change the name of this constructor if you renamed this class.
     */
    public Install() {
        loadedClasses = new Hashtable();
        try {
            openArchive();
        } catch (IOException e) {
            archive = null;
        }
        if (archive == null) {
            System.err.println("can not open archive");
        }
    }

    /**
     * opent this class file as an java archive. This method assumes that the
     * archive is in the current directory and has then same name as this class
     * whith a ".class" appended.
     */
    public void openArchive() throws IOException {
        String classpath = System.getProperty("java.class.path");
        if (classpath == null) {
            System.err.println("Ooops, classpath not set ? Will search current dir");
            classpath = ".";
        }
        String pwd = System.getProperty("user.dir");
        if (pwd != null) {
            classpath = pwd + File.pathSeparator + classpath;
        }
        StringTokenizer tok = new StringTokenizer(classpath, File.pathSeparator);
        while (tok.hasMoreTokens()) {
            String pnow = tok.nextToken();
            if (!pnow.endsWith(File.separator)) {
                pnow = pnow + File.separator;
            }
            System.err.println("try path " + pnow);
            File arfile = new File(pnow + this.getClass().getName() + ".class");
            if (!arfile.exists()) {
                continue;
            }
            if (!arfile.canRead()) {
                System.err.println("can not read the file " + arfile);
                continue;
            }
            archive = new ZipFile(arfile);
            if (archive == null) {
                System.err.println("can not open the archive " + arfile);
            }
            return;
        }
        System.err.println("can not find the archive.");
        throw new FileNotFoundException();
    }

    /**
     * try to load the class data from the archive.
     *
     * @param name the class to load.
     */
    private byte[] loadClassData(String name) {
        if (archive == null) {
            System.err.println("can not read from archive");
            return null;
        }
        String fname = name.replace('.', '/') + ".class";
        ZipEntry ze = archive.getEntry(fname);
        if (ze == null) {
            return null;
        }
        try {
            byte[] result = new byte[(int) ze.getSize()];
            InputStream is = archive.getInputStream(ze);
            int got = 0;
            int want = result.length;
            while (got < result.length) {
                int now = is.read(result, got, want);
                if (now < 0) {
                    System.err.println("can not get data from zip entry");
                    return null;
                }
                got += now;
                want -= now;
            }
            return result;
        } catch (Exception e) {
            System.err.println("Exception while loading class " + e);
            return null;
        }
    }

    /**
     * load a class from the archive.
     *
     * @param name of the class to load.
     * @param resolve ?
     *
     * @return a loaded class.
     */
    public synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class result;
        if ((result = (Class) loadedClasses.get(name)) == null) {
            byte data[] = loadClassData(name);
            if (data == null) {
                result = findSystemClass(name);
            } else {
                result = defineClass(name, data, 0, data.length);
            }
            loadedClasses.put(name, result);
        }
        if (resolve) resolveClass(result);
        return result;
    }

    /**
     * return a resource from the archive as an InputStream.
     * If the resource is not present in the archive, this method searches
     * for a system resource.
     *
     * @param name the name of the resource.
     *
     * @param return an InputStream or null on error.
     */
    public InputStream getResourceAsStream(String name) {
        ZipEntry ze = null;
        if (archive != null) {
            ze = archive.getEntry(name);
        }
        if (ze == null) {
            return getSystemResourceAsStream(name);
        }
        try {
            InputStream istream = archive.getInputStream(ze);
            if ("true".equals(System.getProperty("directread"))) return istream;
            int len = (int) ze.getSize();
            byte[] buffer = new byte[len];
            int off = 0;
            while (len > 0) {
                int l = istream.read(buffer, off, buffer.length - off);
                if (l < 0) return null;
                off += l;
                len -= l;
            }
            istream.close();
            return new ByteArrayInputStream(buffer);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * get the URL for a resource in the archive. If no such resource is
     * present, return a system resource.
     *
     * @param name name of the resource.
     * @return an URL for the named resource or null on error.
     */
    public URL getResource(String name) {
        URL result = null;
        try {
            result = new URL("selfexres:" + name);
            if (result == null) return getSystemResource(name);
            return result;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * the main method.
     *
     * This method creates a new classLoader, loads
     * the class given in "classToLoad" whith this new loader and
     * invokes the "main"-method of the loaded class.
     */
    public static void main(String[] args) {
        Install ld = new Install();
        try {
            uhfc = ld.loadClass("net.sourceforge.liftoff.installer.source.ResURLFactory");
            uhf = (URLStreamHandlerFactory) uhfc.newInstance();
            URL.setURLStreamHandlerFactory(uhf);
        } catch (Exception e) {
            if (e instanceof ClassNotFoundException) {
                System.err.println("can not load class  net.sourceforge.liftoff.installer.source.ResURLFactory :" + e);
                System.exit(1);
            }
        }
        try {
            Class cl = ld.loadClass(classToRun, true);
            Class[] margsClasses = { args.getClass() };
            Object[] margs = { args };
            Method m = cl.getMethod("main", margsClasses);
            m.invoke(null, margs);
        } catch (Exception e) {
            if (e instanceof ClassNotFoundException) {
                System.err.println("can not load class " + classToRun + " : " + e);
                System.exit(1);
            }
            if (e instanceof NoSuchMethodException) {
                System.err.println("can not find method main in " + classToRun + " : " + e);
                System.exit(1);
            }
            if (e instanceof InvocationTargetException) {
                System.err.println("exception in called method " + classToRun + ".main");
                Throwable tr = ((InvocationTargetException) e).getTargetException();
                tr.printStackTrace();
            }
            System.err.println(e);
            System.exit(1);
        }
    }
}
