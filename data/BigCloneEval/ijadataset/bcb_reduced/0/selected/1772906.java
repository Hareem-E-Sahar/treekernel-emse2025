package fi.hip.gb.onejar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 * Loads classes from pre-defined locations inside the jar file containing this
 * class.  Classes will be loaded from jar files contained in the following 
 * locations within the main jar file (on the classpath of the application 
 * actually, which when running with the "java -jar" command works out to be
 * the same thing).
 * <ul>
 * <li>
 *   /lib	Used to contain library jars.
 * </li>
 * <li>
 *   /main	Used to contain a default main jar.
 * </li>
 * <p>
 * Changes made to the original version:
 * - allows expanding of plain files.
 * 
 * </ul> 
 * @author simon@simontuffs.com (<a href="http://www.simontuffs.com">http://www.simontuffs.com</a>)
 */
public class JarClassLoader extends ClassLoader {

    public static final String JAVA_CLASS_PATH = "java.class.path";

    public static final String LIB_PREFIX = "lib/";

    public static final String MAIN_PREFIX = "main/";

    public static final String RECORDING = "recording";

    public static final String TMP = "tmp";

    public static final String UNPACK = "unpack";

    public static final String EXPAND = "One-Jar-Expand";

    public static final String ENV = "One-Jar-Env";

    public static final String CLASSDIR = "One-Jar-Classdir";

    public static final String CLASS = ".class";

    public static final String JAVA_PROTOCOL_HANDLER = "java.protocol.handler.pkgs";

    protected String name;

    protected String classdir;

    static {
        String handlerPackage = System.getProperty(JAVA_PROTOCOL_HANDLER);
        if (handlerPackage == null) handlerPackage = "";
        if (handlerPackage.length() > 0) handlerPackage = "|" + handlerPackage;
        handlerPackage = "fi.hip.gb" + handlerPackage;
        System.setProperty(JAVA_PROTOCOL_HANDLER, handlerPackage);
    }

    protected String PREFIX() {
        return "JarClassLoader: ";
    }

    protected String NAME() {
        return (name != null ? "'" + name + "' " : "");
    }

    protected void VERBOSE(String message) {
        if (verbose) System.out.println(PREFIX() + NAME() + message);
    }

    protected void WARNING(String message) {
        System.err.println(PREFIX() + "Warning: " + NAME() + message);
    }

    protected void INFO(String message) {
        if (info) System.out.println(PREFIX() + "Info: " + NAME() + message);
    }

    protected Map<String, ByteCode> byteCode = new HashMap<String, ByteCode>();

    @SuppressWarnings("unchecked")
    protected Map pdCache = Collections.synchronizedMap(new HashMap());

    protected boolean record = false, flatten = false, unpackFindResource = false;

    protected boolean verbose = false, info = false;

    protected String recording = RECORDING;

    protected String jarName, mainJar, wrapDir;

    protected boolean delegateToParent;

    protected class ByteCode {

        public ByteCode(String $name, String $original, byte $bytes[], String $codebase) {
            name = $name;
            original = $original;
            bytes = $bytes;
            codebase = $codebase;
        }

        public byte bytes[];

        public String name, original, codebase;
    }

    /**
	 * Create a non-delegating but jar-capable classloader for bootstrap
	 * purposes.
	 * @param $wrap  The directory in the archive from which to load a 
	 * wrapping classloader.
	 */
    public JarClassLoader(String $wrap) {
        wrapDir = $wrap;
        delegateToParent = wrapDir == null;
    }

    /**
	 * The main constructor for the Jar-capable classloader.
	 * @param parent	If true, the JarClassLoader will record all used classes
	 * 					into a recording directory (called 'recording' by default)
	 *				 	The name of each jar file will be used as a directory name
	 *					for the recorded classes..
	 * 
	 * Example: Given the following layout of the one-jar.jar file
	 * <pre>
	 *    /
	 *    /META-INF
	 *    | MANIFEST.MF
	 *    /com
	 *      /simontuffs
	 *        /onejar
	 *          Boot.class
	 *          JarClassLoader.class
	 *    /main
	 *        main.jar
	 *        /com
	 *          /main
	 *            Main.class 
	 *    /lib
	 *        util.jar
	 *          /com
	 *            /util
	 *              Util.clas
	 * </pre>
	 * The recording directory will look like this:
	 * <ul>
	 * <li>flatten=false</li>
	 * <pre>
	 *   /recording
	 *     /main.jar
	 *       /com
	 *         /main
	 *            Main.class
	 *     /util.jar
	 *       /com
	 *         /util
	 *            Util.class
	 * </pre>
	 *
	 * <li>flatten = true</li>
	 * <pre>
	 *   /recording
	 *     /com
	 *       /main
	 *          Main.class
	 *       /util
	 *          Util.class
	 *   
	 * </ul>
	 * Flatten mode is intended for when you want to create a super-jar which can
	 * be launched directly without using one-jar's launcher.  Run your application
	 * under all possible scenarios to collect the actual classes which are loaded,
	 * then jar them all up, and point to the main class with a "Main-Class" entry
	 * in the manifest.  
	 *       
	 */
    public JarClassLoader(ClassLoader parent) {
        super(parent);
        delegateToParent = true;
    }

    public String load(String mainClass) {
        return load(mainClass, null);
    }

    @SuppressWarnings("unchecked")
    public String load(String mainClass, String jarName) {
        if (record) {
            new File(recording).mkdirs();
        }
        try {
            if (jarName == null) {
                jarName = System.getProperty(JAVA_CLASS_PATH);
            }
            if (jarName.indexOf(":") != -1) {
                jarName = jarName.substring(0, jarName.indexOf(":"));
            }
            JarFile jarFile = new JarFile(jarName);
            Enumeration<JarEntry> e = jarFile.entries();
            Manifest manifest = jarFile.getManifest();
            String expandPaths[] = null;
            String expand = manifest.getMainAttributes().getValue(EXPAND);
            if (expand != null) {
                VERBOSE(EXPAND + "=" + expand);
                expandPaths = expand.split(",");
            }
            while (e.hasMoreElements()) {
                JarEntry entry = (JarEntry) e.nextElement();
                if (entry.isDirectory()) continue;
                boolean expanded = false;
                String name = entry.getName();
                if (expandPaths != null) {
                    for (int i = 0; i < expandPaths.length; i++) {
                        if (name.startsWith(expandPaths[i])) {
                            File dest = new File(name);
                            if (!dest.exists() || dest.lastModified() < entry.getTime()) {
                                INFO("Expanding " + name);
                                if (dest.exists()) INFO("Update because lastModified=" + new Date(dest.lastModified()) + ", entry=" + new Date(entry.getTime()));
                                if (dest.getParentFile() != null) {
                                    dest.getParentFile().mkdirs();
                                }
                                VERBOSE("using jarFile.getInputStream(" + entry + ")");
                                InputStream is = jarFile.getInputStream(entry);
                                FileOutputStream os = new FileOutputStream(dest);
                                copy(is, os);
                                is.close();
                                os.close();
                            } else {
                                VERBOSE(name + " already expanded");
                            }
                            expanded = true;
                            break;
                        }
                    }
                }
                if (expanded) continue;
                String jar = entry.getName();
                if (wrapDir != null && jar.startsWith(wrapDir) || jar.startsWith(LIB_PREFIX) || jar.startsWith(MAIN_PREFIX)) {
                    if (wrapDir != null && !entry.getName().startsWith(wrapDir)) continue;
                    INFO("caching " + jar);
                    VERBOSE("using jarFile.getInputStream(" + entry + ")");
                    {
                        InputStream is = jarFile.getInputStream(entry);
                        if (is == null) throw new IOException("Unable to load resource /" + jar + " using " + this);
                        loadByteCode(is, jar);
                    }
                    if (jar.startsWith(MAIN_PREFIX)) {
                        if (mainClass == null) {
                            JarInputStream jis = new JarInputStream(jarFile.getInputStream(entry));
                            mainClass = jis.getManifest().getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
                            mainJar = jar;
                        } else if (mainJar != null) {
                            WARNING("A main class is defined in multiple jar files inside " + MAIN_PREFIX + mainJar + " and " + jar);
                            WARNING("The main class " + mainClass + " from " + mainJar + " will be used");
                        }
                    }
                } else if (wrapDir == null && name.startsWith(UNPACK)) {
                    InputStream is = this.getClass().getResourceAsStream("/" + jar);
                    if (is == null) throw new IOException(jar);
                    File dir = new File(TMP);
                    File sentinel = new File(dir, jar.replace('/', '.'));
                    if (!sentinel.exists()) {
                        INFO("unpacking " + jar + " into " + dir.getCanonicalPath());
                        loadByteCode(is, jar, TMP);
                        sentinel.getParentFile().mkdirs();
                        sentinel.createNewFile();
                    }
                } else if (name.endsWith(CLASS)) {
                    loadBytes(entry, jarFile.getInputStream(entry), "/", null);
                }
            }
            if (new File(Boot.MAIN_JAR_EXT).exists()) {
                InputStream mainIs = new FileInputStream(Boot.MAIN_JAR_EXT + Boot.MAIN_JAR);
                if (mainIs != null) {
                    loadByteCode(mainIs, Boot.MAIN_JAR_EXT + Boot.MAIN_JAR);
                }
            }
            this.classdir = manifest.getMainAttributes().getValue(CLASSDIR);
            if (this.classdir != null) {
                File clDir = new File(this.classdir);
                if (clDir.exists()) {
                    for (String file : clDir.list()) {
                        byteCode.put(this.classdir + "/" + file, new ByteCode(null, null, null, null));
                    }
                }
            }
            if (mainClass == null) {
                mainClass = jarFile.getManifest().getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
            }
            String env = manifest.getMainAttributes().getValue(ENV);
            String envs[] = new String[0];
            if (env != null) {
                VERBOSE(ENV + "=" + env);
                envs = env.split(",");
            }
            for (int i = 0; i < envs.length; i++) {
                String[] element = envs[i].split("=");
                System.setProperty(element[0], element[1]);
            }
        } catch (IOException iox) {
            System.err.println("Unable to load resource: " + iox);
            iox.printStackTrace(System.err);
        }
        return mainClass;
    }

    protected void loadByteCode(InputStream is, String jar) throws IOException {
        loadByteCode(is, jar, null);
    }

    protected void loadByteCode(InputStream is, String jar, String tmp) throws IOException {
        JarInputStream jis = new JarInputStream(is);
        JarEntry entry = null;
        Attributes attr = null;
        if (jis.getManifest() != null) {
            Manifest mf = jis.getManifest();
            if (mf != null) attr = mf.getMainAttributes();
        }
        while ((entry = jis.getNextJarEntry()) != null) {
            if (entry.isDirectory()) {
                if (attr != null) {
                    String name = entry.getName();
                    name = name.substring(0, name.length() - 1);
                    name = name.replaceAll("/", ".");
                    if (name.indexOf(".") != -1 && !name.startsWith("META-INF")) {
                        try {
                            this.definePackage(name, attr.getValue(Attributes.Name.SPECIFICATION_TITLE), attr.getValue(Attributes.Name.SPECIFICATION_VERSION), attr.getValue(Attributes.Name.SPECIFICATION_VENDOR), attr.getValue(Attributes.Name.IMPLEMENTATION_TITLE), attr.getValue(Attributes.Name.IMPLEMENTATION_VERSION), attr.getValue(Attributes.Name.IMPLEMENTATION_VENDOR), null);
                        } catch (IllegalArgumentException iae) {
                        }
                    }
                }
            } else {
                loadBytes(entry, jis, jar, tmp);
            }
        }
    }

    protected void loadBytes(JarEntry entry, InputStream is, String jar, String tmp) throws IOException {
        String entryName = entry.getName().replace('/', '.');
        int index = entryName.lastIndexOf('.');
        String type = entryName.substring(index + 1);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copy(is, baos);
        if (tmp != null) {
            File file = new File(tmp, entry.getName());
            file.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baos.toByteArray());
            fos.close();
        } else {
            byte[] bytes = baos.toByteArray();
            if (type.equals("class")) {
                if (alreadyCached(entryName, jar, bytes)) return;
                byteCode.put(entryName, new ByteCode(entryName, entry.getName(), bytes, jar));
                VERBOSE("cached bytes for class " + entryName);
            } else {
                String localname = jar + "/" + entryName;
                byteCode.put(localname, new ByteCode(localname, entry.getName(), bytes, jar));
                VERBOSE("cached bytes for local name " + localname);
                if (alreadyCached(entryName, jar, bytes)) return;
                byteCode.put(entryName, new ByteCode(entryName, entry.getName(), bytes, jar));
                VERBOSE("cached bytes for entry name " + entryName);
            }
        }
    }

    protected boolean classPool = false;

    /**
	 * Locate the named class in a jar-file, contained inside the
	 * jar file which was used to load <u>this</u> class.
	 */
    @SuppressWarnings("unchecked")
    protected Class findClass(String name) throws ClassNotFoundException {
        Class cls = findLoadedClass(name);
        if (cls != null) return cls;
        VERBOSE("findClass(" + name + ")");
        String cache = name.replace('/', '.') + ".class";
        ByteCode bytecode = byteCode.get(cache);
        if (bytecode != null) {
            VERBOSE("found " + name + " in codebase '" + bytecode.codebase + "'");
            if (record) {
                record(bytecode);
            }
            ProtectionDomain pd = (ProtectionDomain) pdCache.get(bytecode.codebase);
            if (pd == null) {
                ProtectionDomain cd = JarClassLoader.class.getProtectionDomain();
                URL url = cd.getCodeSource().getLocation();
                try {
                    url = new URL("jar:" + url + "!/" + bytecode.codebase);
                } catch (MalformedURLException mux) {
                    mux.printStackTrace(System.out);
                }
                CodeSource source = new CodeSource(url, (Certificate[]) null);
                pd = new ProtectionDomain(source, null, this, null);
                pdCache.put(bytecode.codebase, pd);
            }
            byte bytes[] = bytecode.bytes;
            return defineClass(name, bytes, pd);
        }
        VERBOSE(name + " not found");
        throw new ClassNotFoundException(name);
    }

    protected Class defineClass(String name, byte[] bytes, ProtectionDomain pd) throws ClassFormatError {
        return defineClass(name, bytes, 0, bytes.length, pd);
    }

    protected void record(ByteCode bytecode) {
        String fileName = bytecode.original;
        File dir = new File(recording, flatten ? "" : bytecode.codebase);
        File file = new File(dir, fileName);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            VERBOSE("" + file);
            try {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bytecode.bytes);
                fos.close();
            } catch (IOException iox) {
                System.err.println(PREFIX() + "unable to record " + file + ": " + iox);
            }
        }
    }

    /**
	 * Overriden to return resources from the appropriate codebase.
	 * There are basically two ways this method will be called: most commonly
	 * it will be called through the class of an object which wishes to 
	 * load a resource, i.e. this.getClass().getResourceAsStream().  Before
	 * passing the call to us, java.lang.Class mangles the name.  It 
	 * converts a file path such as foo/bar/Class.class into a name like foo.bar.Class, 
	 * and it strips leading '/' characters e.g. converting '/foo' to 'foo'.
	 * All of which is a nuisance, since we wish to do a lookup on the original
	 * name of the resource as present in the One-Jar jar files.  
	 * The other way is more direct, i.e. this.getClass().getClassLoader().getResourceAsStream().
	 * Then we get the name unmangled, and can deal with it directly. 
	 *
	 * The problem is this: if one resource is called /foo/bar/data, and another 
	 * resource is called /foo.bar.data, both will have the same mangled name, 
	 * namely 'foo.bar.data' and only one of them will be visible.  Perhaps the
	 * best way to deal with this is to store the lookup names in mangled form, and
	 * simply issue warnings if collisions occur.  This is not very satisfactory,
	 * but is consistent with the somewhat limiting design of the resource name mapping
	 * strategy in Java today.
	 */
    public InputStream getByteStream(String resource) {
        InputStream result = null;
        ByteCode bytecode = (ByteCode) byteCode.get(resource);
        if (bytecode == null) {
            bytecode = (ByteCode) byteCode.get(resolve(resource));
        }
        if (bytecode != null) result = new ByteArrayInputStream(bytecode.bytes);
        if (result == null && delegateToParent) {
            result = ((JarClassLoader) getParent()).getByteStream(resource);
        }
        VERBOSE("getByteStream(" + resource + ") -> " + result);
        return result;
    }

    /**
	 * Resolve a resource name.  Look first in jar-relative, then in global scope.
	 * @param $resource resource to resolve
	 * @return resource
	 */
    protected String resolve(String $resource) {
        if ($resource.startsWith("/")) $resource = $resource.substring(1);
        $resource = $resource.replace('/', '.');
        String resource = null;
        String caller = getCaller();
        ByteCode callerCode = (ByteCode) byteCode.get(caller + ".class");
        if (callerCode != null) {
            String tmp = callerCode.codebase + "/" + $resource;
            if (byteCode.get(tmp) != null) {
                resource = tmp;
            }
        }
        if (resource == null) {
            if (byteCode.get($resource) == null) {
                String tmp = this.classdir + "/" + $resource;
                if (byteCode.get(tmp) != null) {
                    resource = tmp;
                } else {
                    resource = null;
                }
            } else {
                resource = $resource;
            }
        }
        VERBOSE("resource " + $resource + " resolved to " + resource);
        return resource;
    }

    protected boolean alreadyCached(String name, String jar, byte[] bytes) {
        ByteCode existing = (ByteCode) byteCode.get(name);
        if (existing != null) {
            if (!Arrays.equals(existing.bytes, bytes) && !name.startsWith("/META-INF")) {
                INFO(existing.name + " in " + jar + " is hidden by " + existing.codebase + " (with different bytecode)");
            } else {
                VERBOSE(existing.name + " in " + jar + " is hidden by " + existing.codebase + " (with same bytecode)");
            }
            return true;
        }
        return false;
    }

    protected String getCaller() {
        StackTraceElement[] stack = new Throwable().getStackTrace();
        String caller = null;
        for (int i = 0; i < stack.length; i++) {
            if (byteCode.get(stack[i].getClassName() + ".class") != null) {
                caller = stack[i].getClassName();
                break;
            }
        }
        return caller;
    }

    /**
     * Sets the name of the used  classes recording directory.
     * 
     * @param $recording A value of "" will use the current working directory 
     * (not recommended).  A value of 'null' will use the default directory, which
     * is called 'recording' under the launch directory (recommended).
     */
    public void setRecording(String $recording) {
        recording = $recording;
        if (recording == null) recording = RECORDING;
    }

    public String getRecording() {
        return recording;
    }

    public void setRecord(boolean $record) {
        record = $record;
    }

    public boolean getRecord() {
        return record;
    }

    public void setFlatten(boolean $flatten) {
        flatten = $flatten;
    }

    public boolean isFlatten() {
        return flatten;
    }

    public void setVerbose(boolean $verbose) {
        verbose = $verbose;
        info = verbose;
    }

    public boolean getVerbose() {
        return verbose;
    }

    public void setInfo(boolean $info) {
        info = $info;
    }

    public boolean getInfo() {
        return info;
    }

    protected URL findResource(String $resource) {
        try {
            INFO("findResource(" + $resource + ")");
            String resource = resolve($resource);
            if (resource != null) {
                INFO("findResource() found: " + $resource);
                return new URL(Handler.PROTOCOL + ":" + resource);
            }
            INFO("findResource(): unable to locate " + $resource);
            return null;
        } catch (MalformedURLException mux) {
            WARNING("unable to locate " + $resource + " due to " + mux);
        }
        return null;
    }

    /**
     * Utility to assist with copying InputStream to OutputStream.  All
     * bytes are copied, but both streams are left open.
     * @param in Source of bytes to copy.
     * @param out Destination of bytes to copy.
     * @throws IOException
     */
    protected void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[1024];
        while (true) {
            int len = in.read(buf);
            if (len < 0) break;
            out.write(buf, 0, len);
        }
    }

    public String toString() {
        return super.toString() + (name != null ? "(" + name + ")" : "");
    }

    /**
     * Returns name of the classloader.
     * @return name of the classloader.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name of the classloader.  Default is null.
     * @param string name of the classloader.
     */
    public void setName(String string) {
        name = string;
    }
}
