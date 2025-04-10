package java.lang;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.AccessControlContext;
import java.security.CodeSource;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.Map;
import java.util.Vector;
import sun.misc.ClassFileTransformer;
import sun.misc.CompoundEnumeration;
import sun.misc.Resource;
import sun.misc.URLClassPath;
import sun.security.util.SecurityConstants;
import sun.misc.CVM;

/**
 * A class loader is an object that is responsible for loading classes. The
 * class <tt>ClassLoader</tt> is an abstract class.  Given the name of a
 * class, a class loader should attempt to locate or generate data that
 * constitutes a definition for the class.  A typical strategy is to transform
 * the name into a file name and then read a "class file" of that name
 * from a file system.
 *
 * <p> Every {@link Class <tt>Class</tt>} object contains a {@link
 * Class#getClassLoader() reference} to the <tt>ClassLoader</tt> that defined
 * it.
 *
 * <p> <tt>Class</tt> objects for array classes are not created by class
 * loaders, but are created automatically as required by the Java runtime.
 * The class loader for an array class, as returned by {@link
 * Class#getClassLoader()} is the same as the class loader for its element
 * type; if the element type is a primitive type, then the array class has no
 * class loader.
 *
 * <p> Applications implement subclasses of <tt>ClassLoader</tt> in order to
 * extend the manner in which the Java virtual machine dynamically loads
 * classes.
 *
 * <p> Class loaders may typically be used by security managers to indicate
 * security domains.
 *
 * <p> The <tt>ClassLoader</tt> class uses a delegation model to search for
 * classes and resources.  Each instance of <tt>ClassLoader</tt> has an
 * associated parent class loader.  When requested to find a class or
 * resource, a <tt>ClassLoader</tt> instance will delegate the search for the
 * class or resource to its parent class loader before attempting to find the
 * class or resource itself.  The virtual machine's built-in class loader,
 * called the "bootstrap class loader", does not itself have a parent but may
 * serve as the parent of a <tt>ClassLoader</tt> instance.
 *
 * <p> Normally, the Java virtual machine loads classes from the local file
 * system in a platform-dependent manner.  For example, on UNIX systems, the
 * virtual machine loads classes from the directory defined by the
 * <tt>CLASSPATH</tt> environment variable.
 *
 * <p> However, some classes may not originate from a file; they may originate
 * from other sources, such as the network, or they could be constructed by an
 * application.  The method {@link #defineClass(String, byte[], int, int)
 * <tt>defineClass</tt>} converts an array of bytes into an instance of class
 * <tt>Class</tt>. Instances of this newly defined class can be created using
 * {@link Class#newInstance <tt>Class.newInstance</tt>}.
 *
 * <p> The methods and constructors of objects created by a class loader may
 * reference other classes.  To determine the class(es) referred to, the Java
 * virtual machine invokes the {@link #loadClass <tt>loadClass</tt>} method of
 * the class loader that originally created the class.
 *
 * <p> For example, an application could create a network class loader to
 * download class files from a server.  Sample code might look like:
 *
 * <blockquote><pre>
 *   ClassLoader loader&nbsp;= new NetworkClassLoader(host,&nbsp;port);
 *   Object main&nbsp;= loader.loadClass("Main", true).newInstance();
 *	 &nbsp;.&nbsp;.&nbsp;.
 * </pre></blockquote>
 *
 * <p> The network class loader subclass must define the methods {@link
 * #findClass <tt>findClass</tt>} and <tt>loadClassData</tt> to load a class
 * from the network.  Once it has downloaded the bytes that make up the class,
 * it should use the method {@link #defineClass <tt>defineClass</tt>} to
 * create a class instance.  A sample implementation is:
 *
 * <blockquote><pre>
 *     class NetworkClassLoader extends ClassLoader {
 *         String host;
 *         int port;
 *
 *         public Class findClass(String name) {
 *             byte[] b = loadClassData(name);
 *             return defineClass(name, b, 0, b.length);
 *         }
 *
 *         private byte[] loadClassData(String name) {
 *             // load the class data from the connection
 *             &nbsp;.&nbsp;.&nbsp;.
 *         }
 *     }
 * </pre></blockquote>
 *
 * @version  1.163, 10/10/06
 * @see      #resolveClass(Class)
 * @since    1.0
 */
public abstract class ClassLoader {

    private boolean initialized = false;

    private ClassLoader parent;

    private Hashtable package2certs = new Hashtable(11);

    java.security.cert.Certificate[] nocerts;

    private int loaderGlobalRoot;

    private Vector classes = new Vector();

    private Set domains = new HashSet();

    void addClass(Class c) {
        if (CVM.checkDebugFlags(CVM.DEBUGFLAG_TRACE_CLASSLOADING) != 0) {
            System.err.println("CL: addClass() called for <" + c + "," + this + ">");
        }
        classes.addElement(c);
    }

    private void removeClass(Class c) {
        if (CVM.checkDebugFlags(CVM.DEBUGFLAG_TRACE_CLASSLOADING) != 0) {
            System.err.println("CL: removeClass() called for <" + c + "," + this + ">");
        }
        classes.removeElement(c);
    }

    private HashMap packages = new HashMap();

    /**
     * Creates a new class loader using the specified parent class loader for
     * delegation.
     *
     * <p> If there is a security manager, its {@link
     * SecurityManager#checkCreateClassLoader()
     * <tt>checkCreateClassLoader</tt>} method is invoked.  This may result in
     * a security exception.  </p>
     *
     * @param  parent
     *         The parent class loader
     *
     * @throws  SecurityException
     *          If a security manager exists and its
     *          <tt>checkCreateClassLoader</tt> method doesn't allow creation
     *          of a new class loader.
     *
     * @since  1.2
     */
    protected ClassLoader(ClassLoader parent) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        InitializeLoaderGlobalRoot();
        this.parent = parent;
        initialized = true;
    }

    /**
     * Creates a new class loader using the <tt>ClassLoader</tt> returned by
     * the method {@link #getSystemClassLoader()
     * <tt>getSystemClassLoader()</tt>} as the parent class loader.
     *
     * <p> If there is a security manager, its {@link
     * SecurityManager#checkCreateClassLoader()
     * <tt>checkCreateClassLoader</tt>} method is invoked.  This may result in
     * a security exception.  </p>
     *
     * @throws  SecurityException
     *          If a security manager exists and its
     *          <tt>checkCreateClassLoader</tt> method doesn't allow creation
     *          of a new class loader.
     */
    protected ClassLoader() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        InitializeLoaderGlobalRoot();
        this.parent = getSystemClassLoader();
        initialized = true;
    }

    private native void InitializeLoaderGlobalRoot();

    /**
     * Loads the class with the specified name.  This method searches for
     * classes in the same manner as the {@link #loadClass(String, boolean)}
     * method.  It is invoked by the Java virtual machine to resolve class
     * references.  Invoking this method is equivalent to invoking {@link
     * #loadClass(String, boolean) <tt>loadClass(name, false)</tt>}.  </p>
     *
     * @param  name
     *         The name of the class
     *
     * @return  The resulting <tt>Class</tt> object
     *
     * @throws  ClassNotFoundException
     *          If the class was not found
     */
    public Class loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    /**
     * Loads the class with the specified name.  The default implementation
     * of this method searches for classes in the following order:
     *
     * <p><ol>
     *
     *   <li><p> Invoke {@link #findLoadedClass(String)} to check if the class
     *   has already been loaded.  </p></li>
     *
     *   <li><p> Invoke the {@link #loadClass(String) <tt>loadClass</tt>} method
     *   on the parent class loader.  If the parent is <tt>null</tt> the class
     *   loader built-in to the virtual machine is used, instead.  </p></li>
     *
     *   <li><p> Invoke the {@link #findClass(String)} method to find the
     *   class.  </p></li>
     *
     * </ol>
     *
     * <p> If the class was found using the above steps, and the
     * <tt>resolve</tt> flag is true, this method will then invoke the {@link
     * #resolveClass(Class)} method on the resulting <tt>Class</tt> object.
     *
     * <p> Subclasses of <tt>ClassLoader</tt> are encouraged to override {@link
     * #findClass(String)}, rather than this method.  </p>
     *
     * @param  name
     *         The name of the class
     *
     * @param  resolve
     *         If <tt>true</tt> then resolve the class
     *
     * @return  The resulting <tt>Class</tt> object
     *
     * @throws  ClassNotFoundException
     *          If the class could not be found
     */
    protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class c = findLoadedClass(name);
        if (c == null) {
            try {
                if (parent != null) {
                    c = parent.loadClass(name, false);
                } else {
                    check();
                    c = loadBootstrapClassOrNull(name);
                }
            } catch (ClassNotFoundException e) {
                c = null;
            }
            if (c == null) {
                c = findClass(name);
            } else {
                c.addToLoaderCache(this);
            }
        }
        if (resolve) {
            resolveClass(c);
        }
        return c;
    }

    private synchronized Class loadClassInternal(String name) throws ClassNotFoundException {
        return loadClass(name);
    }

    private void checkPackageAccess(Class cls, ProtectionDomain pd) {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            final String name = cls.getName();
            final int i = name.lastIndexOf('.');
            if (i != -1) {
                AccessController.doPrivileged(new PrivilegedAction() {

                    public Object run() {
                        sm.checkPackageAccess(name.substring(0, i));
                        return null;
                    }
                }, new AccessControlContext(new ProtectionDomain[] { pd }));
            }
        }
        domains.add(pd);
    }

    /**
     * Finds the specified class.  This method should be overridden by class
     * loader implementations that follow the delegation model for loading
     * classes, and will be invoked by the {@link #loadClass
     * <tt>loadClass</tt>} method after checking the parent class loader for
     * the requested class.  The default implementation throws a
     * <tt>ClassNotFoundException</tt>.  </p>
     *
     * @param  name
     *         The name of the class
     *
     * @return  The resulting <tt>Class</tt> object
     *
     * @throws  ClassNotFoundException
     *          If the class could not be found
     *
     * @since  1.2
     */
    protected Class findClass(String name) throws ClassNotFoundException {
        throw new ClassNotFoundException(name);
    }

    /**
     * Converts an array of bytes into an instance of class <tt>Class</tt>.
     * Before the <tt>Class</tt> can be used it must be resolved.
     *
     * <p> This method assigns a default {@link java.security.ProtectionDomain
     * <tt>ProtectionDomain</tt>} to the newly defined class.  The
     * <tt>ProtectionDomain</tt> is effectively granted the same set of
     * permissions returned when {@link
     * java.security.Policy#getPermissions(java.security.CodeSource)
     * <tt>Policy.getPolicy().getPermissions(new CodeSource(null, null))</tt>}
     * is invoked.  The default domain is created on the first invocation of
     * {@link #defineClass(String, byte[], int, int) <tt>defineClass</tt>},
     * and re-used on subsequent invocations.
     *
     * <p> To assign a specific <tt>ProtectionDomain</tt> to the class, use
     * the {@link #defineClass(String, byte[], int, int,
     * java.security.ProtectionDomain) <tt>defineClass</tt>} method that takes a
     * <tt>ProtectionDomain</tt> as one of its arguments.  </p>
     *
     * @param  name
     *         The expected name of the class, or <tt>null</tt>
     *         if not known, using '<tt>.</tt>' and not '<tt>/</tt>' as the
     *         separator and without a trailing <tt>.class</tt> suffix.
     *
     * @param  b
     *         The bytes that make up the class data.  The bytes in positions
     *         <tt>off</tt> through <tt>off+len-1</tt> should have the format
     *         of a valid class file as defined by the <a
     *         href="http://java.sun.com/docs/books/vmspec/">Java Virtual
     *         Machine Specification</a>.
     *
     * @param  off
     *         The start offset in <tt>b</tt> of the class data
     *
     * @param  len
     *         The length of the class data
     *
     * @return  The <tt>Class</tt> object that was created from the specified
     *          class data.
     *
     * @throws  ClassFormatError
     *          If the data did not contain a valid class
     *
     * @throws  IndexOutOfBoundsException
     *          If either <tt>off</tt> or <tt>len</tt> is negative, or if
     *          <tt>off+len</tt> is greater than <tt>b.length</tt>.
     *
     * @throws  SecurityException
     *          If an attempt is made to add this class to a package that
     *          contains classes that were signed by a different set of
     *          certificates than this class (which is unsigned), or if the
     *          class name begins with "<tt>java.</tt>".
     *
     * @see  #loadClass(String, boolean)
     * @see  #resolveClass(Class)
     * @see  java.security.CodeSource
     * @see  java.security.SecureClassLoader
     *
     * @since  1.1
     */
    protected final Class defineClass(String name, byte[] b, int off, int len) throws ClassFormatError {
        return defineClass(name, b, off, len, null);
    }

    /**
     * Converts an array of bytes into an instance of class <tt>Class</tt>,
     * with an optional <tt>ProtectionDomain</tt>.  If the domain is
     * <tt>null</tt>, then a default domain will be assigned to the class as
     * specified in the documentation for {@link #defineClass(String, byte[],
     * int, int)}.  Before the class can be used it must be resolved.
     *
     * <p> The first class defined in a package determines the exact set of
     * certificates that all subsequent classes defined in that package must
     * contain.  The set of certificates for a class is obtained from the
     * {@link java.security.CodeSource <tt>CodeSource</tt>} within the
     * <tt>ProtectionDomain</tt> of the class.  Any classes added to that
     * package must contain the same set of certificates or a
     * <tt>SecurityException</tt> will be thrown.  Note that if the
     * <tt>name</tt> argument is <tt>null</tt>, this check is not performed.
     * You should always pass in the name of the class you are defining as
     * well as the bytes.  This ensures that the class you are defining is
     * indeed the class you think it is.
     *
     * <p> The specified class name cannot begin with "<tt>java.</tt>", since
     * all classes in the "<tt>java.*</tt> packages can only be defined by the
     * bootstrap class loader. If the name parameter is not <tt>null</tt>, it
     * must be equal to the name of the class specified by the byte array
     * "<tt>b</tt>", otherwise a {@link <tt>NoClassDefFoundError</tt>} will be
     * thrown.  </p>
     *
     * @param  name
     *         The expected name of the class, or <tt>null</tt> if not known,
     *         using '<tt>.</tt>' and not '<tt>/</tt>' as the separator and
     *         without a trailing "<tt>.class</tt>" suffix.
     *
     * @param  b
     *         The bytes that make up the class data. The bytes in positions
     *         <tt>off</tt> through <tt>off+len-1</tt> should have the format
     *         of a valid class file as defined by the <a
     *         href="http://java.sun.com/docs/books/vmspec/">Java Virtual
     *         Machine Specification</a>.
     *
     * @param  off
     *         The start offset in <tt>b</tt> of the class data
     *
     * @param  len
     *         The length of the class data
     *
     * @param  protectionDomain
     *         The ProtectionDomain of the class
     *
     * @return  The <tt>Class</tt> object created from the data,
     *          and optional <tt>ProtectionDomain</tt>.
     *
     * @throws  ClassFormatError
     *          If the data did not contain a valid class
     *
     * @throws  NoClassDefFoundError
     *          If <tt>name</tt> is not equal to the name of the class
     *          specified by <tt>b</tt>
     *
     * @throws  IndexOutOfBoundsException
     *          If either <tt>off</tt> or <tt>len</tt> is negative, or if
     *          <tt>off+len</tt> is greater than <tt>b.length</tt>.
     *
     * @throws  SecurityException
     *          If an attempt is made to add this class to a package that
     *          contains classes that were signed by a different set of
     *          certificates than this class, or if the class name begins with
     *          "<tt>java.</tt>".
     */
    protected final Class defineClass(String name, byte[] b, int off, int len, ProtectionDomain protectionDomain) throws ClassFormatError {
        check();
        if ((name != null) && name.startsWith("java.")) {
            throw new SecurityException("Prohibited package name: " + name.substring(0, name.lastIndexOf('.')));
        }
        if (protectionDomain == null) {
            protectionDomain = getDefaultDomain();
        }
        if (name != null) checkCerts(name, protectionDomain.getCodeSource());
        if (!checkName(name)) throw new NoClassDefFoundError("Illegal name: " + name);
        Class c = null;
        try {
            c = defineClass0(name, b, off, len, protectionDomain);
        } catch (ClassFormatError cfe) {
            Object[] transformers = ClassFileTransformer.getTransformers();
            for (int i = 0; transformers != null && i < transformers.length; i++) {
                try {
                    byte[] tb = ((ClassFileTransformer) transformers[i]).transform(b, off, len);
                    c = defineClass0(name, tb, 0, tb.length, protectionDomain);
                    break;
                } catch (ClassFormatError cfe2) {
                }
            }
            if (c == null) throw cfe;
        }
        c.loadSuperClasses();
        if (protectionDomain.getCodeSource() != null) {
            java.security.cert.Certificate certs[] = protectionDomain.getCodeSource().getCertificates();
            if (certs != null) setSigners(c, certs);
        }
        return c;
    }

    private static boolean checkName(String name) {
        if (name == null || name.length() == 0) return true;
        if (name.indexOf('/') != -1) return false;
        if (name.charAt(0) == '[') return false;
        return true;
    }

    private native Class defineClass0(String name, byte[] b, int off, int len, ProtectionDomain pd);

    private synchronized void checkCerts(String name, CodeSource cs) {
        int i = name.lastIndexOf('.');
        String pname = (i == -1) ? "" : name.substring(0, i);
        java.security.cert.Certificate[] pcerts = (java.security.cert.Certificate[]) package2certs.get(pname);
        if (pcerts == null) {
            if (cs != null) {
                pcerts = cs.getCertificates();
            }
            if (pcerts == null) {
                if (nocerts == null) nocerts = new java.security.cert.Certificate[0];
                pcerts = nocerts;
            }
            package2certs.put(pname, pcerts);
        } else {
            java.security.cert.Certificate[] certs = null;
            if (cs != null) {
                certs = cs.getCertificates();
            }
            if (!compareCerts(pcerts, certs)) {
                throw new SecurityException("class \"" + name + "\"'s signer information does not match signer information of other classes in the same package");
            }
        }
    }

    /**
     * check to make sure the certs for the new class (certs) are the same as
     * the certs for the first class inserted in the package (pcerts)
     */
    private boolean compareCerts(java.security.cert.Certificate[] pcerts, java.security.cert.Certificate[] certs) {
        if ((certs == null) || (certs.length == 0)) {
            return pcerts.length == 0;
        }
        if (certs.length != pcerts.length) return false;
        boolean match;
        for (int i = 0; i < certs.length; i++) {
            match = false;
            for (int j = 0; j < pcerts.length; j++) {
                if (certs[i].equals(pcerts[j])) {
                    match = true;
                    break;
                }
            }
            if (!match) return false;
        }
        for (int i = 0; i < pcerts.length; i++) {
            match = false;
            for (int j = 0; j < certs.length; j++) {
                if (pcerts[i].equals(certs[j])) {
                    match = true;
                    break;
                }
            }
            if (!match) return false;
        }
        return true;
    }

    /**
     * Links the specified class.  This (misleadingly named) method may be
     * used by a class loader to link a class.  If the class <tt>c</tt> has
     * already been linked, then this method simply returns. Otherwise, the
     * class is linked as described in the "Execution" chapter of the <a
     * href="http://java.sun.com/docs/books/jls/">Java Language Specification</a>.
     * </p>
     *
     * @param  c
     *         The class to link
     *
     * @throws  NullPointerException
     *          If <tt>c</tt> is <tt>null</tt>.
     *
     * @see  #defineClass(String, byte[], int, int)
     */
    protected final void resolveClass(Class c) {
        check();
        resolveClass0(c);
    }

    private native void resolveClass0(Class c);

    /**
     * Finds a class with the specified name, loading it if necessary.
     *
     * <p> This method loads the class through the system class loader (see
     * {@link #getSystemClassLoader()}).  The <tt>Class</tt> object returned
     * might have more than one <tt>ClassLoader</tt> associated with it.
     * Subclasses of <tt>ClassLoader</tt> need not usually invoke this method,
     * because most class loaders need to override just {@link
     * #findClass(String)}.  </p>
     *
     * @param  name
     *         The name of the class that is to be found
     *
     * @return  The <tt>Class</tt> object for the specified <tt>name</tt>
     *
     * @throws  ClassNotFoundException
     *          If the class could not be found
     *
     * @see  #ClassLoader(ClassLoader)
     * @see  #getParent()
     */
    protected final Class findSystemClass(String name) throws ClassNotFoundException {
        check();
        ClassLoader system = getSystemClassLoader();
        if (system == null) {
            return loadBootstrapClass(name);
        }
        return system.loadClass(name);
    }

    /**
     * Used only by profiling code to load classes via reflections
     */
    private Class findBootstrapClass(String name) throws ClassNotFoundException {
        return loadBootstrapClass(name);
    }

    /**
     * Returns a bootstrap Class, or throws a ClassNotFoundException
     */
    static Class loadBootstrapClass(String name) throws ClassNotFoundException {
        Class c = loadBootstrapClassOrNull(name);
        if (c == null) throw new ClassNotFoundException(name);
        return c;
    }

    private static Class loadBootstrapClassOrNull(String name) throws ClassNotFoundException {
        if (!checkName(name)) throw new ClassNotFoundException(name);
        synchronized (ClassLoader.class) {
            Class c = loadBootstrapClass0(name);
            if (c != null && !c.superClassesLoaded()) {
                c.loadSuperClasses();
            }
            return c;
        }
    }

    private static native Class loadBootstrapClass0(String name) throws ClassNotFoundException;

    private void check() {
        if (!initialized) {
            throw new SecurityException("ClassLoader object not initialized");
        }
    }

    /**
     * Returns the class with the given name if this loader has been recorded
     * by the Java virtual machine as an initiating loader of a class with
     * that name.  Otherwise <tt>null</tt> is returned.  </p>
     *
     * @param  name
     *         The class name
     *
     * @return  The <tt>Class</tt> object, or <tt>null</tt> if the class has
     *          not been loaded
     *
     * @since  1.1
     */
    protected final Class findLoadedClass(String name) {
        check();
        if (!checkName(name)) return null;
        return findLoadedClass0(name);
    }

    private final native Class findLoadedClass0(String name);

    /**
     * Sets the signers of a class.  This should be invoked after defining a
     * class.  </p>
     *
     * @param  c
     *         The <tt>Class</tt> object
     *
     * @param  signers
     *         The signers for the class
     *
     * @since  1.1
     */
    protected final void setSigners(Class c, Object[] signers) {
        check();
        c.setSigners(signers);
    }

    /**
     * Finds the resource with the given name.  A resource is some data
     * (images, audio, text, etc) that can be accessed by class code in a way
     * that is independent of the location of the code.
     *
     * <p> The name of a resource is a '<tt>/</tt>'-separated path name that
     * identifies the resource.
     *
     * <p> This method will first search the parent class loader for the
     * resource; if the parent is <tt>null</tt> the path of the class loader
     * built-in to the virtual machine is searched.  That failing, this method
     * will invoke {@link #findResource(String)} to find the resource.  </p>
     *
     * @param  name
     *         The resource name
     *
     * @return  A <tt>URL</tt> object for reading the resource, or
     *          <tt>null</tt> if the resource could not be found or the invoker
     *          doesn't have adequate  privileges to get the resource.
     *
     * @since  1.1
     */
    public URL getResource(String name) {
        URL url;
        if (parent != null) {
            url = parent.getResource(name);
        } else {
            url = getBootstrapResource(name);
        }
        if (url == null) {
            url = findResource(name);
        }
        return url;
    }

    /**
     * Finds all the resources with the given name. A resource is some data
     * (images, audio, text, etc) that can be accessed by class code in a way
     * that is independent of the location of the code.
     *
     * <p>The name of a resource is a <tt>/</tt>-separated path name that
     * identifies the resource.
     *
     * <p> The search order is described in the documentation for {@link
     * #getResource(String)}.  </p>
     *
     * @param  name
     *         The resource name
     *
     * @return  An enumeration of {@link java.net.URL <tt>URL</tt>} objects for
     *          the resource.  If no resources could  be found, the enumeration
     *          will be empty.  Resources that the class loader doesn't have
     *          access to will not be in the enumeration.
     *
     * @throws  IOException
     *          If I/O errors occur
     *
     * @see  #findResources(String)
     *
     * @since  1.2
     */
    public final Enumeration getResources(String name) throws IOException {
        Enumeration[] tmp = new Enumeration[2];
        if (parent != null) {
            tmp[0] = parent.getResources(name);
        } else {
            tmp[0] = getBootstrapResources(name);
        }
        tmp[1] = findResources(name);
        return new CompoundEnumeration(tmp);
    }

    /**
     * Finds the resource with the given name. Class loader implementations
     * should override this method to specify where to find resources.  </p>
     *
     * @param  name
     *         The resource name
     *
     * @return  A <tt>URL</tt> object for reading the resource, or
     *          <tt>null</tt> if the resource could not be found
     *
     * @since  1.2
     */
    protected URL findResource(String name) {
        return null;
    }

    /**
     * Returns an enumeration of {@link java.net.URL <tt>URL</tt>} objects
     * representing all the resources with the given name. Class loader
     * implementations should override this method to specify where to load
     * resources from.  </p>
     *
     * @param  name
     *         The resource name
     *
     * @return  An enumeration of {@link java.net.URL <tt>URL</tt>} objects for
     *          the resources
     *
     * @throws  IOException
     *          If I/O errors occur
     *
     * @since  1.2
     */
    protected Enumeration findResources(String name) throws IOException {
        return new CompoundEnumeration(new Enumeration[0]);
    }

    /**
     * Find a resource of the specified name from the search path used to load
     * classes.  This method locates the resource through the system class
     * loader (see {@link #getSystemClassLoader()}).  </p>
     *
     * @param  name
     *         The resource name
     *
     * @return  A {@link java.net.URL <tt>URL</tt>} object for reading the
     *          resource, or <tt>null</tt> if the resource could not be found
     *
     * @since  1.1
     */
    public static URL getSystemResource(String name) {
        ClassLoader system = getSystemClassLoader();
        if (system == null) {
            return getBootstrapResource(name);
        }
        return system.getResource(name);
    }

    /**
     * Finds all resources of the specified name from the search path used to
     * load classes.  The resources thus found are returned as an
     * {@link java.util.Enumeration <tt>Enumeration</tt>} of {@link
     * java.net.URL <tt>URL</tt>} objects.
     *
     * <p> The search order is described in the documentation for {@link
     * #getSystemResource(String)}.  </p>
     *
     * @param  name
     *         The resource name
     *
     * @return  An enumeration of resource {@link java.net.URL <tt>URL</tt>}
     *          objects
     *
     * @throws  IOException
     *          If I/O errors occur

     * @since  1.2
     */
    public static Enumeration getSystemResources(String name) throws IOException {
        ClassLoader system = getSystemClassLoader();
        if (system == null) {
            return getBootstrapResources(name);
        }
        return system.getResources(name);
    }

    /**
     * Find resources from the VM's built-in classloader.
     */
    private static URL getBootstrapResource(String name) {
        URLClassPath ucp = getBootstrapClassPath();
        Resource res = ucp.getResource(name);
        return res != null ? res.getURL() : null;
    }

    /**
     * Find resources from the VM's built-in classloader.
     */
    private static Enumeration getBootstrapResources(String name) throws IOException {
        final Enumeration e = getBootstrapClassPath().getResources(name);
        return new Enumeration() {

            public Object nextElement() {
                return ((Resource) e.nextElement()).getURL();
            }

            public boolean hasMoreElements() {
                return e.hasMoreElements();
            }
        };
    }

    static URLClassPath getBootstrapClassPath() {
        return sun.misc.Launcher.getBootstrapClassPath();
    }

    /**
     * Returns an input stream for reading the specified resource.
     *
     * <p> The search order is described in the documentation for {@link
     * #getResource(String)}.  </p>
     *
     * @param  name
     *         The resource name
     *
     * @return  An input stream for reading the resource, or <tt>null</tt>
     *          if the resource could not be found
     *
     * @since  1.1
     */
    public InputStream getResourceAsStream(String name) {
        URL url = getResource(name);
        try {
            return url != null ? url.openStream() : null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Open for reading, a resource of the specified name from the search path
     * used to load classes.  This method locates the resource through the
     * system class loader (see {@link #getSystemClassLoader()}).  </p>
     *
     * @param  name
     *         The resource name
     *
     * @return  An input stream for reading the resource, or <tt>null</tt>
     * 	        if the resource could not be found
     *
     * @since  1.1
     */
    public static InputStream getSystemResourceAsStream(String name) {
        URL url = getSystemResource(name);
        try {
            return url != null ? url.openStream() : null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Returns the parent class loader for delegation. Some implementations may
     * use <tt>null</tt> to represent the bootstrap class loader. This method
     * will return <tt>null</tt> in such implementations if this class loader's
     * parent is the bootstrap class loader.
     *
     * <p> If a security manager is present, and the invoker's class loader is
     * not <tt>null</tt> and is not an ancestor of this class loader, then this
     * method invokes the security manager's {@link
     * SecurityManager#checkPermission(java.security.Permission)
     * <tt>checkPermission</tt>} method with a {@link
     * RuntimePermission#RuntimePermission(String)
     * <tt>RuntimePermission("getClassLoader")</tt>} permission to verify
     * access to the parent class loader is permitted.  If not, a
     * <tt>SecurityException</tt> will be thrown.  </p>
     *
     * @return  The parent <tt>ClassLoader</tt>
     *
     * @throws  SecurityException
     *          If a security manager exists and its <tt>checkPermission</tt>
     *          method doesn't allow access to this class loader's parent class
     *          loader.
     *
     * @since  1.2
     */
    public final ClassLoader getParent() {
        if (parent == null) return null;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            ClassLoader ccl = getCallerClassLoader();
            if (ccl != null && !isAncestor(ccl)) {
                sm.checkPermission(SecurityConstants.GET_CLASSLOADER_PERMISSION);
            }
        }
        return parent;
    }

    /**
     * Returns the system class loader for delegation.  This is the default
     * delegation parent for new <tt>ClassLoader</tt> instances, and is
     * typically the class loader used to start the application.
     *
     * <p> This method is first invoked early in the runtime's startup
     * sequence, at which point it creates the system class loader and sets it
     * as the context class loader of the invoking <tt>Thread</tt>.
     *
     * <p> The default system class loader is an implementation-dependent
     * instance of this class.
     *
     * <p> If the system property "<tt>java.system.class.loader</tt>" is defined
     * when this method is first invoked then the value of that property is
     * taken to be the name of a class that will be returned as the system
     * class loader.  The class is loaded using the default system class loader
     * and must define a public constructor that takes a single parameter of
     * type <tt>ClassLoader</tt> which is used as the delegation parent.  An
     * instance is then created using this constructor with the default system
     * class loader as the parameter.  The resulting class loader is defined
     * to be the system class loader.
     *
     * <p> If a security manager is present, and the invoker's class loader is
     * not <tt>null</tt> and the invoker's class loader is not the same as or
     * an ancestor of the system class loader, then this method invokes the
     * security manager's {@link
     * SecurityManager#checkPermission(java.security.Permission)
     * <tt>checkPermission</tt>} method with a {@link
     * RuntimePermission#RuntimePermission(String)
     * <tt>RuntimePermission("getClassLoader")</tt>} permission to verify
     * access to the system class loader.  If not, a
     * <tt>SecurityException</tt> will be thrown.  </p>
     *
     * @return  The system <tt>ClassLoader</tt> for delegation, or
     *          <tt>null</tt> if none
     *
     * @throws  SecurityException
     *          If a security manager exists and its <tt>checkPermission</tt>
     *          method doesn't allow access to the system class loader.
     *
     * @throws  IllegalStateException
     *          If invoked recursively during the construction of the class
     *          loader specified by the "<tt>java.system.class.loader</tt>"
     *          property.
     *
     * @throws  Error
     *          If the system property "<tt>java.system.class.loader</tt>"
     *          is defined but the named class could not be loaded, the
     *          provider class does not define the required constructor, or an
     *          exception is thrown by that constructor when it is invoked. The
     *          underlying cause of the error can be retrieved via the
     *          {@link Throwable#getCause()} method.
     *
     * @revised  1.4
     */
    public static ClassLoader getSystemClassLoader() {
        initSystemClassLoader();
        if (scl == null) {
            return null;
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            ClassLoader ccl = getCallerClassLoader();
            if (ccl != null && ccl != scl && !scl.isAncestor(ccl)) {
                sm.checkPermission(SecurityConstants.GET_CLASSLOADER_PERMISSION);
            }
        }
        return scl;
    }

    private static synchronized void initSystemClassLoader() {
        if (!sclSet) {
            if (scl != null) throw new IllegalStateException("recursive invocation");
            sun.misc.Launcher l = sun.misc.Launcher.getLauncher();
            if (l != null) {
                Throwable oops = null;
                scl = l.getClassLoader();
                try {
                    PrivilegedExceptionAction a;
                    a = new SystemClassLoaderAction(scl);
                    scl = (ClassLoader) AccessController.doPrivileged(a);
                } catch (PrivilegedActionException pae) {
                    oops = pae.getCause();
                    if (oops instanceof InvocationTargetException) {
                        oops = oops.getCause();
                    }
                }
                if (oops != null) {
                    if (oops instanceof Error) {
                        throw (Error) oops;
                    } else {
                        throw new Error(oops);
                    }
                }
            }
            sclSet = true;
        }
    }

    boolean isAncestor(ClassLoader cl) {
        ClassLoader acl = this;
        do {
            acl = acl.parent;
            if (cl == acl) {
                return true;
            }
        } while (acl != null);
        return false;
    }

    static native ClassLoader getCallerClassLoader();

    private static ClassLoader scl;

    private static boolean sclSet;

    /**
     * Defines a package by name in this <tt>ClassLoader</tt>.  This allows
     * class loaders to define the packages for their classes. Packages must
     * be created before the class is defined, and package names must be
     * unique within a class loader and cannot be redefined or changed once
     * created.  </p>
     *
     * @param  name
     *         The package name
     *
     * @param  specTitle
     *         The specification title
     *
     * @param  specVersion
     *         The specification version
     *
     * @param  specVendor
     *         The specification vendor
     *
     * @param  implTitle
     *         The implementation title
     *
     * @param  implVersion
     *         The implementation version
     *
     * @param  implVendor
     *         The implementation vendor
     *
     * @param  sealBase
     *         If not <tt>null</tt>, then this package is sealed with
     *         respect to the given code source {@link java.net.URL
     *         <tt>URL</tt>}  object.  Otherwise, the package is not sealed.
     *
     * @return  The newly defined <tt>Package</tt> object
     *
     * @throws  IllegalArgumentException
     *          If package name duplicates an existing package either in this
     *          class loader or one of its ancestors
     *
     * @since  1.2
     */
    protected Package definePackage(String name, String specTitle, String specVersion, String specVendor, String implTitle, String implVersion, String implVendor, URL sealBase) throws IllegalArgumentException {
        synchronized (packages) {
            Package pkg = getPackage(name);
            if (pkg != null) {
                throw new IllegalArgumentException(name);
            }
            pkg = new Package(name, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor, sealBase);
            packages.put(name, pkg);
            return pkg;
        }
    }

    /**
     * Returns a <tt>Package</tt> that has been defined by this class loader
     * or any of its ancestors.  </p>
     *
     * @param  name
     *         The package name
     *
     * @return  The <tt>Package</tt> corresponding to the given name, or
     *          <tt>null</tt> if not found
     *
     * @since  1.2
     */
    protected Package getPackage(String name) {
        synchronized (packages) {
            Package pkg = (Package) packages.get(name);
            if (pkg == null) {
                if (parent != null) {
                    pkg = parent.getPackage(name);
                } else {
                    pkg = Package.getSystemPackage(name);
                }
                if (pkg != null) {
                    packages.put(name, pkg);
                }
            }
            return pkg;
        }
    }

    /**
     * Returns all of the <tt>Packages</tt> defined by this class loader and
     * its ancestors.  </p>
     *
     * @return  The array of <tt>Package</tt> objects defined by this
     *          <tt>ClassLoader</tt>
     *
     * @since  1.2
     */
    protected Package[] getPackages() {
        Map map;
        synchronized (packages) {
            map = (Map) packages.clone();
        }
        Package[] pkgs;
        if (parent != null) {
            pkgs = parent.getPackages();
        } else {
            pkgs = Package.getSystemPackages();
        }
        if (pkgs != null) {
            for (int i = 0; i < pkgs.length; i++) {
                String pkgName = pkgs[i].getName();
                if (map.get(pkgName) == null) {
                    map.put(pkgName, pkgs[i]);
                }
            }
        }
        return (Package[]) map.values().toArray(new Package[map.size()]);
    }

    /**
     * Returns the absolute path name of a native library.  The VM invokes this
     * method to locate the native libraries that belong to classes loaded with
     * this class loader. If this method returns <tt>null</tt>, the VM
     * searches the library along the path specified as the
     * "<tt>java.library.path</tt>" property.  </p>
     *
     * @param  libname
     *         The library name
     *
     * @return  The absolute path of the native library
     *
     * @see  System#loadLibrary(String)
     * @see  System#mapLibraryName(String)
     *
     * @since  1.2
     */
    protected String findLibrary(String libname) {
        return null;
    }

    /**
     * The inner class NativeLibrary denotes a loaded native library instance.
     * Every classloader contains a vector of loaded native libraries in the
     * private field <tt>nativeLibraries</tt>.  The native libraries loaded
     * into the system are entered into the <tt>systemNativeLibraries</tt>
     * vector.
     *
     * <p> Every native library reuqires a particular version of JNI. This is
     * denoted by the private <tt>jniVersion</tt> field.  This field is set by
     * the VM when it loads the library, and used by the VM to pass the correct
     * version of JNI to the native methods.  </p>
     *
     * @version  1.163 10/10/06
     * @see      ClassLoader
     * @since    1.2
     */
    static class NativeLibrary {

        long handle;

        private int jniVersion;

        private Class fromClass;

        String name;

        boolean isXrunLibrary;

        boolean isBuiltin;

        native void load(String name);

        native long find(String name);

        native void unload();

        public NativeLibrary(Class fromClass, String name, boolean isXrunLibrary, boolean isBuiltin) {
            this.name = name;
            this.fromClass = fromClass;
            this.isXrunLibrary = isXrunLibrary;
            this.isBuiltin = isBuiltin;
        }

        protected void finalize() {
            synchronized (loadedLibraryNames) {
                if ((isXrunLibrary || fromClass.getClassLoader() != null) && handle != 0) {
                    if (!isXrunLibrary) {
                        int size = loadedLibraryNames.size();
                        for (int i = 0; i < size; i++) {
                            if (name.equals(loadedLibraryNames.elementAt(i))) {
                                loadedLibraryNames.removeElementAt(i);
                                break;
                            }
                        }
                    }
                    ClassLoader.nativeLibraryContext.push(this);
                    try {
                        unload();
                    } finally {
                        ClassLoader.nativeLibraryContext.pop();
                    }
                }
            }
        }

        static Class getFromClass() {
            return ((NativeLibrary) (ClassLoader.nativeLibraryContext.peek())).fromClass;
        }

        private static native boolean initIDs();

        static {
            if (!initIDs()) {
                throw new RuntimeException("NativeLibrary initIDs() failed");
            }
        }
    }

    private ProtectionDomain defaultDomain = null;

    private synchronized ProtectionDomain getDefaultDomain() {
        if (defaultDomain == null) {
            CodeSource cs = new CodeSource(null, null);
            defaultDomain = new ProtectionDomain(cs, null, this, null);
        }
        return defaultDomain;
    }

    private static Vector loadedLibraryNames = new Vector();

    private static Vector systemNativeLibraries = new Vector();

    private static Vector xrunNativeLibraries = new Vector();

    private Vector nativeLibraries = new Vector();

    private static Stack nativeLibraryContext = new Stack();

    private static String usr_paths[];

    private static String sys_paths[];

    static void loadLibrary(Class fromClass, String name, boolean isAbsolute) {
        loadLibraryInternal(fromClass, name, isAbsolute, false);
    }

    /** This intermediate routine is the contents of loadLibrary,
	above, in the JDK, and is used to assist in the loading of
	so-called "JVM helper libraries" like the JDWP and HPROF and
	the invoking of the "JVM_OnLoad" function defined therein.
	Called from VM initialization code via the JNI (to get past
	access protection.) Returns an internal NativeLibrary object,
	which is dangerous; this should only be called for the purpose
	of loading these helper libraries. Note that findNative,
	below, is not appropriate for this task since we need to find
	this symbol in a specific native library. */
    private static Object loadLibraryInternal(Class fromClass, String name, boolean isAbsolute, boolean isXrunLibrary) {
        Object libObj;
        ClassLoader loader = (fromClass == null) ? null : fromClass.getClassLoader();
        usr_paths = CVM.getUserLibrarySearchPaths();
        sys_paths = CVM.getSystemLibrarySearchPaths();
        if (isAbsolute) {
            libObj = loadLibrary0(fromClass, new File(name), isXrunLibrary);
            if (libObj != null) {
                return libObj;
            }
            throw new UnsatisfiedLinkError("Can't load library: " + name);
        }
        if (loader != null) {
            String libfilename = loader.findLibrary(name);
            if (libfilename != null) {
                File libfile = new File(libfilename);
                if (!libfile.isAbsolute()) {
                    throw new UnsatisfiedLinkError("ClassLoader.findLibrary failed to return an absolute path: " + libfilename);
                }
                libObj = loadLibrary0(fromClass, libfile, false);
                if (libObj != null) {
                    return libObj;
                }
                throw new UnsatisfiedLinkError("Can't load " + libfilename);
            }
        }
        boolean libraryFound = false;
        String builtin = System.getProperty("java.library.builtin." + name);
        if (builtin != null) {
            libraryFound = true;
        } else {
            String[] builtins = CVM.getBuiltinLibrarySearchPaths();
            for (int i = 0; i < builtins.length; i++) {
                if (name.equals(builtins[i])) {
                    libraryFound = true;
                    break;
                }
            }
        }
        if (libraryFound) {
            return loadLibrary0(fromClass, name, true, isXrunLibrary);
        }
        String mappedLibraryName = System.mapLibraryName(name);
        for (int i = 0; i < sys_paths.length; i++) {
            File libfile = new File(sys_paths[i], mappedLibraryName);
            libObj = loadLibrary0(fromClass, libfile, isXrunLibrary);
            if (libObj != null) {
                return libObj;
            }
        }
        if (loader == null) {
            throw new UnsatisfiedLinkError("no " + name + " in sun.boot.library.path");
        }
        for (int i = 0; i < usr_paths.length; i++) {
            File libfile = new File(usr_paths[i], mappedLibraryName);
            libObj = loadLibrary0(fromClass, libfile, false);
            if (libObj != null) {
                return libObj;
            }
        }
        throw new UnsatisfiedLinkError("no " + name + " in java.library.path");
    }

    /** Changed from the JDK. This now returns the NativeLibrary
        object to allow the VM initialization code to reuse the
        Java-based dynamic linking code when loading "JVM helper
        libraries"; see loadLibraryInternal, above. Now returns null
        where the JDK version would have returned false. */
    private static Object loadLibrary0(Class fromClass, String name, boolean builtin, boolean isXrunLibrary) {
        ClassLoader loader = (fromClass == null) ? null : fromClass.getClassLoader();
        Vector libs;
        if (isXrunLibrary) {
            libs = xrunNativeLibraries;
        } else if (loader == null) {
            libs = systemNativeLibraries;
        } else {
            libs = loader.nativeLibraries;
        }
        synchronized (libs) {
            int size = libs.size();
            for (int i = 0; i < size; i++) {
                NativeLibrary lib = (NativeLibrary) libs.elementAt(i);
                if (name.equals(lib.name)) {
                    return lib;
                }
            }
            synchronized (loadedLibraryNames) {
                if (!isXrunLibrary && loadedLibraryNames.contains(name)) {
                    throw new UnsatisfiedLinkError("Native Library " + name + " already loaded in another classloader");
                }
                int n = nativeLibraryContext.size();
                for (int i = 0; i < n; i++) {
                    NativeLibrary lib = (NativeLibrary) nativeLibraryContext.elementAt(i);
                    if (name.equals(lib.name) && (lib.isXrunLibrary == isXrunLibrary)) {
                        if (loader == lib.fromClass.getClassLoader()) {
                            return lib;
                        } else {
                            throw new UnsatisfiedLinkError("Native Library " + name + " is being loaded in another classloader");
                        }
                    }
                }
                NativeLibrary lib = new NativeLibrary(fromClass, name, isXrunLibrary, builtin);
                if (!builtin) {
                    nativeLibraryContext.push(lib);
                    try {
                        lib.load(name);
                    } finally {
                        nativeLibraryContext.pop();
                    }
                }
                if (lib.handle != 0 || builtin) {
                    if (!isXrunLibrary) {
                        loadedLibraryNames.addElement(name);
                    }
                    libs.addElement(lib);
                    return lib;
                }
                return null;
            }
        }
    }

    private static Object loadLibrary0(Class fromClass, final File file, boolean isXrunLibrary) {
        Boolean exists = (Boolean) AccessController.doPrivileged(new PrivilegedAction() {

            public Object run() {
                return new Boolean(file.exists());
            }
        });
        if (!exists.booleanValue()) {
            return null;
        }
        String name;
        try {
            name = file.getCanonicalPath();
        } catch (IOException e) {
            return null;
        }
        return loadLibrary0(fromClass, name, false, isXrunLibrary);
    }

    static long findNative(ClassLoader loader, String name) {
        Vector libs = loader != null ? loader.nativeLibraries : systemNativeLibraries;
        synchronized (libs) {
            int size = libs.size();
            for (int i = 0; i < size; i++) {
                NativeLibrary lib = (NativeLibrary) libs.elementAt(i);
                if (lib.isBuiltin) {
                    continue;
                }
                long entry = lib.find(name);
                if (entry != 0) return entry;
            }
        }
        return 0;
    }

    private boolean defaultAssertionStatus = false;

    private Map packageAssertionStatus = null;

    Map classAssertionStatus = null;

    /**
     * Sets the default assertion status for this class loader.  This setting
     * determines whether classes loaded by this class loader and initialized
     * in the future will have assertions enabled or disabled by default.
     * This setting may be overridden on a per-package or per-class basis by
     * invoking {@link #setPackageAssertionStatus(String, boolean)} or {@link
     * #setClassAssertionStatus(String, boolean)}.  </p>
     *
     * @param  enabled
     *         <tt>true</tt> if classes loaded by this class loader will
     *         henceforth have assertions enabled by default, <tt>false</tt>
     *         if they will have assertions disabled by default.
     *
     * @since  1.4
     */
    public synchronized void setDefaultAssertionStatus(boolean enabled) {
        if (classAssertionStatus == null) initializeJavaAssertionMaps();
        defaultAssertionStatus = enabled;
    }

    /**
     * Sets the package default assertion status for the named package.  The
     * package default assertion status determines the assertion status for
     * classes initialized in the future that belong to the named package or
     * any of its "subpackages".
     *
     * <p> A subpackage of a package named p is any package whose name begins
     * with "<tt>p.</tt>".  For example, <tt>javax.swing.text</tt> is a
     * subpackage of <tt>javax.swing</tt>, and both <tt>java.util</tt> and
     * <tt>java.lang.reflect</tt> are subpackages of <tt>java</tt>.
     *
     * <p> In the event that multiple package defaults apply to a given class,
     * the package default pertaining to the most specific package takes
     * precedence over the others.  For example, if <tt>javax.lang</tt> and
     * <tt>javax.lang.reflect</tt> both have package defaults associated with
     * them, the latter package default applies to classes in
     * <tt>javax.lang.reflect</tt>.
     *
     * <p> Package defaults take precedence over the class loader's default
     * assertion status, and may be overridden on a per-class basis by invoking
     * {@link #setClassAssertionStatus(String, boolean)}.  </p>
     *
     * @param  packageName
     *         The name of the package whose package default assertion status
     *         is to be set. A <tt>null</tt> value indicates the unnamed
     *         package that is "current" (<a *
     *         href="http://java.sun.com/docs/books/jls/">Java Language
     *         Specification</a>, section 7.4.2).
     *
     * @param  enabled
     *         <tt>true</tt> if classes loaded by this classloader and
     *         belonging to the named package or any of its subpackages will
     *         have assertions enabled by default, <tt>false</tt> if they will
     *         have assertions disabled by default.
     *
     * @since  1.4
     */
    public synchronized void setPackageAssertionStatus(String packageName, boolean enabled) {
        if (packageAssertionStatus == null) initializeJavaAssertionMaps();
        packageAssertionStatus.put(packageName, Boolean.valueOf(enabled));
    }

    /**
     * Sets the desired assertion status for the named top-level class in this
     * class loader and any nested classes contained therein.  This setting
     * takes precedence over the class loader's default assertion status, and
     * over any applicable per-package default.  This method has no effect if
     * the named class has already been initialized.  (Once a class is
     * initialized, its assertion status cannot change.)
     *
     * <p> If the named class is not a top-level class, this invocation will
     * have no effect on the actual assertion status of any class, and its
     * return value is undefined.  </p>
     *
     * @param  className
     *         The fully qualified class name of the top-level class whose
     *         assertion status is to be set.
     *
     * @param  enabled
     *         <tt>true</tt> if the named class is to have assertions
     *         enabled when (and if) it is initialized, <tt>false</tt> if the
     *         class is to have assertions disabled.
     *
     * @since  1.4
     */
    public synchronized void setClassAssertionStatus(String className, boolean enabled) {
        if (classAssertionStatus == null) initializeJavaAssertionMaps();
        classAssertionStatus.put(className, Boolean.valueOf(enabled));
    }

    /**
     * Sets the default assertion status for this class loader to
     * <tt>false</tt> and discards any package defaults or class assertion
     * status settings associated with the class loader.  This method is
     * provided so that class loaders can be made to ignore any command line or
     * persistent assertion status settings and "start with a clean slate."
     * </p>
     *
     * @since  1.4
     */
    public synchronized void clearAssertionStatus() {
        classAssertionStatus = new HashMap();
        packageAssertionStatus = new HashMap();
        defaultAssertionStatus = false;
    }

    /**
     * Returns the assertion status that would be assigned to the specified
     * class if it were to be initialized at the time this method is invoked.
     * If the named class has had its assertion status set, the most recent
     * setting will be returned; otherwise, if any package default assertion
     * status pertains to this class, the most recent setting for the most
     * specific pertinent package default assertion status is returned;
     * otherwise, this class loader's default assertion status is returned.
     * </p>
     *
     * @param  className
     *         The fully qualified class name of the class whose desired
     *         assertion status is being queried.
     *
     * @return  The desired assertion status of the specified class.
     *
     * @see  #setClassAssertionStatus(String, boolean)
     * @see  #setPackageAssertionStatus(String, boolean)
     * @see  #setDefaultAssertionStatus(boolean)
     *
     * @since  1.4
     */
    synchronized boolean desiredAssertionStatus(String className) {
        Boolean result;
        result = (Boolean) classAssertionStatus.get(className);
        if (result != null) return result.booleanValue();
        int dotIndex = className.lastIndexOf(".");
        if (dotIndex < 0) {
            result = (Boolean) packageAssertionStatus.get(null);
            if (result != null) return result.booleanValue();
        }
        while (dotIndex > 0) {
            className = className.substring(0, dotIndex);
            result = (Boolean) packageAssertionStatus.get(className);
            if (result != null) return result.booleanValue();
            dotIndex = className.lastIndexOf(".", dotIndex - 1);
        }
        return defaultAssertionStatus;
    }

    private void initializeJavaAssertionMaps() {
        classAssertionStatus = new HashMap();
        packageAssertionStatus = new HashMap();
        AssertionStatusDirectives directives = retrieveDirectives();
        for (int i = 0; i < directives.classes.length; i++) classAssertionStatus.put(directives.classes[i], Boolean.valueOf(directives.classEnabled[i]));
        for (int i = 0; i < directives.packages.length; i++) packageAssertionStatus.put(directives.packages[i], Boolean.valueOf(directives.packageEnabled[i]));
        defaultAssertionStatus = directives.deflt;
    }

    private static native AssertionStatusDirectives retrieveDirectives();
}

class SystemClassLoaderAction implements PrivilegedExceptionAction {

    private ClassLoader parent;

    SystemClassLoaderAction(ClassLoader parent) {
        this.parent = parent;
    }

    public Object run() throws Exception {
        ClassLoader sys;
        Constructor ctor;
        Class c;
        Class cp[] = { ClassLoader.class };
        Object params[] = { parent };
        String cls = System.getProperty("java.system.class.loader");
        if (cls == null) {
            return parent;
        }
        c = Class.forName(cls, true, parent);
        ctor = c.getDeclaredConstructor(cp);
        sys = (ClassLoader) ctor.newInstance(params);
        Thread.currentThread().setContextClassLoader(sys);
        return sys;
    }
}
