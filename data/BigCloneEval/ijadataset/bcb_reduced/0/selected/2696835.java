package com.extentech.toolkit;

import java.lang.reflect.Method;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.io.*;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.zip.*;
import com.extentech.naming.InitialContextImpl;
import java.util.Properties;

/** Resource Loader which implements a basic JNDI Context and performs:

    <li> Classloading mapped to variable names in properties files
    	 allows for easy abstraction of implementation classes
    <li> Configuration strings loaded from properties files
    <li> Arbitrary resource binding
    
	* @author John McMahon -- Copyright &copy;2010 <a href = "http://www.extentech.com">Extentech Inc.</a>
	* @version 1.0
	* @since 1.3

*/
public class ResourceLoader extends InitialContextImpl implements Serializable, javax.naming.Context {

    /** 
	 * 
	 * @author John [ Apr 5, 2007 ]
	 * 
	 */
    private static final long serialVersionUID = 12345245254L;

    private String resloc = "";

    private File propsfile = null;

    public static boolean DEBUG = false;

    private Properties resources = new Properties();

    public String toString() {
        return "Extentech ResourceLoader v." + ResourceLoader.getVersion();
    }

    public static String getVersion() {
        return "##VERSION##";
    }

    public Enumeration getKeys() {
        if (!snagged) return resources.keys();
        return env.keys();
    }

    public Object getObject(String key) {
        if (!snagged) return resources.get(key);
        return env.get(key);
    }

    private boolean snagged = false;

    /** put the properties file vals in the ResourceLoader
     * 
     *
     */
    private void snagVals() {
        snagged = true;
        Enumeration a = resources.keys();
        while (a.hasMoreElements()) {
            String mystr = (String) a.nextElement();
            env.put(mystr, resources.get(mystr));
        }
    }

    /** Constructor which takes a path to the properties
     *  file containing the initial ResourceLoader values.
     *  
     *  Uses the resources from the proper locale.
     * 
     * @param s
     */
    public ResourceLoader(String s) {
        super();
        if (true) {
            if (s.indexOf("resources/") == -1) s = "resources/" + s;
        }
        Logger.logInfo("ResourceLoader INIT: " + s);
        resloc = s;
        try {
            try {
                propsfile = new File(s + ".properties");
                FileInputStream fis = new FileInputStream(propsfile);
                resources.load(fis);
            } catch (Exception e) {
                try {
                    propsfile.createNewFile();
                    FileInputStream fis = new FileInputStream(propsfile);
                    resources.load(fis);
                } catch (Exception ex) {
                    Logger.logWarn("Could not init Resourceloader from: " + propsfile.getAbsolutePath());
                }
            }
            boolean hidevals = false;
            try {
                if (resources.get("public") != null) if (resources.get("visibility").equals("private")) hidevals = true;
            } catch (MissingResourceException mre) {
            }
            if (!hidevals) this.snagVals();
        } catch (MissingResourceException mre) {
            Logger.logErr("ResourceLoader getting resources failed: " + mre.toString());
        }
    }

    public ResourceLoader() {
        super();
    }

    /** Returns a String from the properties file
	 * 
	 * @param nm
	 * @return
	 */
    public String getResourceString(String nm) {
        String str;
        try {
            str = resources.get(nm).toString();
        } catch (Exception mre) {
            str = "";
        }
        return str;
    }

    /** Sets a String value in the properties file
	 * 
	 * @param nm
	 * @return
	 */
    public void setResourceString(String nm, String v) {
        try {
            resources.setProperty(nm, v);
            FileOutputStream fos = new FileOutputStream(propsfile);
            resources.store(fos, null);
            fos.flush();
            fos.close();
        } catch (Exception mre) {
            Logger.logWarn("Resource string: " + nm + " could not be set to " + v + " in:" + this.resloc);
        }
    }

    /** Returns an Array of Objects which are class
        loaded based on a comma-delimited list of
        class names listed in the properties file.
    */
    public Object[] getObjects(String propname) {
        String objnames = getResourceString(propname);
        if (objnames != null) {
            Object[] obj = new Object[1];
            obj[0] = loadClass(objnames);
            return obj;
        }
        return null;
    }

    /** Load a Class by name
	 * 
	 * @param className
	 * @return
	 */
    public static Object loadClass(String className) {
        ExtenClassLoader cl = new ExtenClassLoader();
        Object obj = null;
        try {
            Class c = cl.loadClass(className, true);
            obj = c.newInstance();
            return obj;
        } catch (ClassFormatError t) {
            Logger.logErr(t.toString());
            return null;
        } catch (ClassNotFoundException t) {
            Logger.logErr(t);
            return null;
        } catch (ClassCastException t) {
            Logger.logErr(t);
            return null;
        } catch (InstantiationException t) {
            Logger.logErr(t);
            return null;
        } catch (IllegalAccessException t) {
            Logger.logErr(t);
            return null;
        }
    }

    /** Enables the retrieval of a byte array from a jar file.   The classpath is searched
        for the file and if it exists within a jar the file is returned as a byte array.
     * @param filepath
     * @return
     * @throws Exception
     */
    public static byte[] getBytesFromJar(String filepath) throws Exception {
        if (DEBUG) Logger.logInfo("Resourceloader.getBytesFromJar: The initial filepath is: " + filepath);
        byte[] b = null;
        String zipstring = getFilePathForResource(filepath);
        if (zipstring == null) return null;
        if (System.getProperty("com.extentech.extenxls.jarloc") != null) zipstring = System.getProperty("com.extentech.extenxls.jarloc");
        String filepathNoSlash = filepath.substring(1);
        if (DEBUG) Logger.logInfo("Resourceloader.getBytesFromJar: The filepath without slash is: " + filepathNoSlash);
        if (DEBUG) Logger.logInfo("Resourceloader.getBytesFromJar: The zipstring is: " + zipstring);
        File f = new File(zipstring);
        try {
            ZipFile pkg = new ZipFile(f);
            ZipEntry z = pkg.getEntry(filepathNoSlash);
            InputStream is = pkg.getInputStream(z);
            long len = z.getSize();
            b = new byte[(int) len];
            for (int a = 0; a < b.length; a++) {
                b[a] = (byte) is.read();
            }
            pkg.close();
        } catch (ZipException e) {
            return InFile.getBytesFromFile(f);
        }
        return b;
    }

    /** Returns the file system-specific path to a given resource
     *  in the classpath for the VM.
     * 
     * @param resource
     * @return
     */
    public static String getFilePathForResource(String resource) {
        URL u = new ResourceLoader().getClass().getResource(resource);
        if (u == null) {
            Logger.logErr("ResourceLoader.getFilePathForResource: " + resource + " not found.");
            return null;
        }
        if (DEBUG) Logger.logInfo("ResourceLoader.getFilePathForResource() got:" + u.toString());
        String s = u.getFile();
        if (DEBUG) Logger.logInfo("ResourceLoader.getFilePathForResource Decoding:" + s);
        s = ResourceLoader.Decode(s);
        if (DEBUG) Logger.logInfo("ResourceLoader.getFilePathForResource Decoded:" + s);
        int i = s.indexOf("!");
        if (i > -1) {
            String zipstring = s.substring(0, i);
            int begin = zipstring.indexOf(":");
            begin += 1;
            zipstring = zipstring.substring(begin);
            if (zipstring.indexOf(":") != -1) {
                if (zipstring.indexOf("/") == 0) {
                    zipstring = zipstring.substring(1);
                }
            }
            if (DEBUG) Logger.logInfo("Resourceloader.getFilePathForResource(): Successfully obtained " + zipstring);
            return zipstring;
        } else {
            if (DEBUG) Logger.logErr("ResourceLoader.getFilePathForResource(): File is not in jar:" + s);
            return s;
        }
    }

    /**
	 * extract resource from jar (.WAR,.JAR or .ZIP)
	 * Strips resource from filename pattern 
	 * @param jar
	 * @param resource
	 * @return	inputStream or null if resource can't be located or other error
	 */
    public static InputStream getInputStreamFromJar(String jarandResource) {
        String[] tmp = extractJarAndResourceName(jarandResource);
        return getInputStreamFromJar(tmp[0], tmp[1]);
    }

    /**
	 * write file f to jar referenced by jarandresource ( <jar><resource> ) and set path/name to resource
	 * @param jarandResource
	 * @param f
	 */
    public static void addFileToJar(String jarandResource, String f) {
        String[] tmp = extractJarAndResourceName(jarandResource);
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(tmp[0]));
            ZipInputStream fin = new ZipInputStream(new FileInputStream(f));
            out.putNextEntry(new ZipEntry(tmp[1]));
            byte[] buf = new byte[fin.available()];
            fin.read(buf);
            out.write(buf);
            out.flush();
            out.closeEntry();
            out.close();
        } catch (Exception e) {
            Logger.logErr("addFileToJar: Jar: " + tmp[0] + " File: " + tmp[1] + " : " + e.toString());
        }
    }

    /**
	 * returns truth of "file is a jar/archive file"
	 * @param f
	 * @return
	 */
    public static boolean isJarFile(String f) {
        f = f.toLowerCase();
        int i = f.indexOf(".war");
        if (i < 0) i = f.indexOf(".jar");
        if (i < 0) i = f.indexOf(".rar");
        if (i < 0) i = f.indexOf(".zip");
        return (i > -1);
    }

    /**
	 * separate and return the jar portion and resource portion of a jar and resource string: <jar (.war/.zip/.jar/.rar)><resource>
	 * @param jarAndResource
	 * @return String[2]
	 */
    public static String[] extractJarAndResourceName(String jarAndResource) {
        jarAndResource = jarAndResource.toLowerCase();
        int i = jarAndResource.indexOf(".war");
        if (i < 0) i = jarAndResource.indexOf(".jar");
        if (i < 0) i = jarAndResource.indexOf(".rar");
        if (i < 0) i = jarAndResource.indexOf(".zip");
        return new String[] { jarAndResource.substring(0, i + 4), jarAndResource.substring(i + 5) };
    }

    /**
	 * extract resource from jar (.WAR,.JAR or .ZIP)
	 * @param jar
	 * @param resource
	 * @return	inputStream or null if resource can't be located or other error
	 */
    public static InputStream getInputStreamFromJar(String jar, String resource) {
        try {
            java.util.zip.ZipFile z = new ZipFile(jar);
            if (z != null) {
                return z.getInputStream(new ZipEntry(resource));
            }
        } catch (Exception e) {
            Logger.logErr("getInputStreamFromJar: " + e.toString());
        }
        return null;
    }

    /** Get the path to a directory by locating the jar
	 *  file in the classpath containing the given resource name.
	 * 
	 * @param resource
	 * @return
	 */
    public String getWorkingDirectoryFromJar(String resource) {
        String s;
        if (System.getProperty("com.extentech.extenxls.jarloc") != null) {
            s = System.getProperty("com.extentech.extenxls.jarloc") + "!";
        } else {
            URL u = getClass().getResource(resource);
            s = u.getFile();
        }
        if (DEBUG) Logger.logInfo("Resource: " + resource + " found in: " + s);
        int begin = -1;
        begin = s.indexOf("file:");
        if (begin < 0) {
            begin = s.indexOf(":");
            begin += 1;
        } else begin += 5;
        s = s.substring(begin);
        if (s.indexOf(":") != -1) {
            if (s.indexOf("/") == 0) s = s.substring(1);
        }
        if (DEBUG) Logger.logInfo("ResourceLoader() after stripping:" + s);
        int i = s.indexOf("!");
        if (i > -1) {
            String zipstring = s.substring(0, i);
            i = zipstring.lastIndexOf("/");
            if (i == -1) {
                i = zipstring.lastIndexOf("\\");
            }
            zipstring = zipstring.substring(0, i);
            if (DEBUG) Logger.logInfo("ResourceLoader() returning zipstring Final Working Directory Setting: " + zipstring);
            return zipstring;
        } else {
            if (DEBUG) Logger.logInfo("ResourceLoader() returning Final Working Directory Setting: " + s);
            return s;
        }
    }

    private static URLDecoder decodr = new URLDecoder();

    /** Decode a URL String, if supported by the JDK version in use
		 *  this method will utilize the 
		 * @param s
		 * @return
		 */
    public static String Decode(String s) {
        String[] tmpstr = { s, "ISO-8859-1" };
        String ret = s;
        ret = (String) ResourceLoader.executeIfSupported(decodr, tmpstr, "decode");
        if (ret == null) try {
            ret = URLDecoder.decode(s, "ISO-8859-1");
        } catch (Exception e) {
            Logger.logErr("ResourceLoader.Decode resource failed: " + e.toString());
        }
        return ret;
    }

    /** Decode a URL String, if supported by the JDK version in use
	 *  this method will utilize the non-deprecated method of decoding.
	 * @param s, string to decode
	 * @param encoding, the encoding type to use
	 * @return
	 */
    public static String Decode(String s, String encoding) {
        String[] tmpstr = { s, "Encoding" };
        String ret = s;
        ret = (String) ResourceLoader.executeIfSupported(decodr, tmpstr, "decode");
        if (ret == null) try {
            ret = URLDecoder.decode(s);
        } catch (Exception e) {
            Logger.logErr("ResourceLoader.Decode resource failed: " + e.toString());
        }
        return ret;
    }

    /** Attempt to execute a Method on an Object
		 * 
		 * @param ob the Object which contains the method you want to execute
		 * @param args an array of arguments to the Method, null if none
		 * @param methname the name of the Method you are executing
		 * @return the return value of the method if any
		 */
    public static Object executeIfSupported(Object ob, Object[] args, String methname) {
        try {
            Object retob = null;
            Method[] mt = ob.getClass().getMethods();
            int t = 0;
            for (; t < mt.length; t++) {
                int numparms = 0, numargs = 0;
                if (args != null) numargs = args.length;
                if (mt[t].getParameterTypes() != null) numparms = mt[t].getParameterTypes().length;
                String nm = mt[t].getName();
                if ((nm.equals(methname)) && (numparms == numargs)) {
                    try {
                        Method mx = mt[t];
                        retob = mx.invoke(ob, args);
                        break;
                    } catch (Exception e) {
                        ;
                        if (false) Logger.logWarn("ResourceLoader.executeIfSupported() Method NOT supported: " + methname + " in " + ob.getClass().getName() + " for arguments " + StringTool.arrayToString(args));
                        return null;
                    }
                }
            }
            if (false) if (t == mt.length) Logger.logWarn("ResourceLoader.executeIfSupported() Method NOT found: " + methname + " in " + ob.getClass().getName() + " for arguments " + StringTool.arrayToString(args));
            return retob;
        } catch (NoSuchMethodError e) {
            return null;
        }
    }

    /** Execute a Method on an Object
		 * 
		 * @param ob the Object which contains the method you want to execute
		 * @param args an array of arguments to the Method, null if none
		 * @param methname the name of the Method you are executing
		 * @return the return value of the method if any
		 */
    public static Object execute(Object ob, Object[] args, String methname) throws Exception {
        Class[] pc = new Class[args.length];
        for (int r = 0; r < args.length; r++) {
            pc[r] = args[r].getClass();
        }
        Method mt = null;
        try {
            mt = ob.getClass().getMethod(methname, pc);
        } catch (NoSuchMethodException e) {
            return executeIfSupported(ob, args, methname);
        }
        try {
            return mt.invoke(ob, args);
        } catch (Exception e) {
            Logger.logErr("ResourceLoader.execute " + methname + " on " + ob.getClass().getName() + " failed: " + e.toString());
            e.printStackTrace();
            return null;
        }
    }

    /** Sets the debugging level for the ResourceLoader
	 * @param b
	 */
    public void setDebug(boolean b) {
        DEBUG = b;
    }
}
