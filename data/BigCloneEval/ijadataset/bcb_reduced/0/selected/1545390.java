package org.eclipse.osgi.launch;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.*;
import java.util.*;
import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.osgi.framework.*;
import org.osgi.framework.launch.Framework;

/**
 * The System Bundle implementation for the Equinox Framework.
 * 
 * @since 3.5
 */
public class Equinox implements Framework {

    private static final String implName = "org.eclipse.osgi.framework.internal.core.EquinoxLauncher";

    /**@GuardedBy this*/
    private Framework impl;

    private final boolean useSeparateCL;

    private final Map configuration;

    public Equinox(Map configuration) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(new AllPermission());
        useSeparateCL = FrameworkProperties.inUse();
        this.configuration = configuration;
    }

    private Framework createImpl() {
        if (System.getSecurityManager() == null) return createImpl0();
        return (Framework) AccessController.doPrivileged(new PrivilegedAction() {

            public Object run() {
                return createImpl0();
            }
        });
    }

    Framework createImpl0() {
        try {
            Class implClazz = getImplClass();
            Constructor constructor = implClazz.getConstructor(new Class[] { Map.class });
            return (Framework) constructor.newInstance(new Object[] { configuration });
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(implName);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage());
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        } catch (InstantiationException e) {
            throw new RuntimeException(e.getMessage());
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private Class getImplClass() throws ClassNotFoundException {
        ClassLoader thisCL = this.getClass().getClassLoader();
        if (!(useSeparateCL && (thisCL instanceof URLClassLoader))) return Class.forName(implName);
        URL[] cp = ((URLClassLoader) thisCL).getURLs();
        EquinoxFWClassLoader fwCL = new EquinoxFWClassLoader(cp, thisCL);
        return fwCL.loadClass(implName);
    }

    private synchronized Framework getImpl() {
        if (impl == null) impl = createImpl();
        return impl;
    }

    public void init() throws BundleException {
        getImpl().init();
    }

    public FrameworkEvent waitForStop(long timeout) throws InterruptedException {
        return getImpl().waitForStop(timeout);
    }

    public Enumeration findEntries(String path, String filePattern, boolean recurse) {
        return getImpl().findEntries(path, filePattern, recurse);
    }

    public BundleContext getBundleContext() {
        return getImpl().getBundleContext();
    }

    public long getBundleId() {
        return getImpl().getBundleId();
    }

    public URL getEntry(String path) {
        return getImpl().getEntry(path);
    }

    public Enumeration getEntryPaths(String path) {
        return getImpl().getEntryPaths(path);
    }

    public Dictionary getHeaders() {
        return getImpl().getHeaders();
    }

    public Dictionary getHeaders(String locale) {
        return getImpl().getHeaders(locale);
    }

    public long getLastModified() {
        return getImpl().getLastModified();
    }

    public String getLocation() {
        return getImpl().getLocation();
    }

    public ServiceReference[] getRegisteredServices() {
        return getImpl().getRegisteredServices();
    }

    public URL getResource(String name) {
        return getImpl().getResource(name);
    }

    public Enumeration getResources(String name) throws IOException {
        return getImpl().getResources(name);
    }

    public ServiceReference[] getServicesInUse() {
        return getImpl().getServicesInUse();
    }

    public int getState() {
        return getImpl().getState();
    }

    public String getSymbolicName() {
        return getImpl().getSymbolicName();
    }

    public boolean hasPermission(Object permission) {
        return getImpl().hasPermission(permission);
    }

    public Class loadClass(String name) throws ClassNotFoundException {
        return getImpl().loadClass(name);
    }

    public void start(int options) throws BundleException {
        getImpl().start(options);
    }

    public void start() throws BundleException {
        getImpl().start();
    }

    public void stop(int options) throws BundleException {
        getImpl().stop(options);
    }

    public void stop() throws BundleException {
        getImpl().stop();
    }

    public void uninstall() throws BundleException {
        getImpl().uninstall();
    }

    public void update() throws BundleException {
        getImpl().update();
    }

    public void update(InputStream in) throws BundleException {
        getImpl().update(in);
    }

    public Map getSignerCertificates(int signersType) {
        return getImpl().getSignerCertificates(signersType);
    }

    public Version getVersion() {
        return getImpl().getVersion();
    }
}
