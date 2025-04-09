package org.jtools.service;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.jpattern.condition.Condition;
import org.jpattern.condition.True;

public final class ServiceUtils {

    private static final class InfoImpl<T, R> implements ServiceInfo<T, R> {

        private final ClassLoader classLoader;

        private final Condition<String[]> filter;

        private final Class<R> resultClass;

        private final Class<T> targetClass;

        private final boolean testResult;

        public InfoImpl(final ClassLoader classLoader, final Condition<String[]> filter, final Class<R> resultClass, final Class<T> targetClass, final boolean testResult) {
            this.classLoader = classLoader;
            this.filter = filter == null ? True.<String[]>getInstance() : filter;
            this.resultClass = resultClass;
            this.targetClass = targetClass;
            this.testResult = testResult;
        }

        public ClassLoader getClassLoader() {
            return classLoader;
        }

        public Condition<String[]> getFilter() {
            return filter;
        }

        public Class<R> getResultClass() {
            return resultClass;
        }

        public Class<T> getServiceClass() {
            return targetClass;
        }

        public boolean isTestResult() {
            return testResult;
        }
    }

    /**
     * Creates a new service loader for the given service type and class loader.
     *
     * @param service The interface or abstract class representing the service
     *
     * @param loader The class loader to be used to load provider-configuration files and provider classes, or
     *            <tt>null</tt> if the system class loader (or, failing that, the bootstrap class loader) is to be
     *            used
     *
     * @return A new service loader
     */
    @SuppressWarnings("unchecked")
    public static <S, R> ServiceLoader<R> load(Class<S> service, Class<R> result, ClassLoader loader, Condition<String[]> filter) {
        Service s = service.getAnnotation(Service.class);
        Class<? extends ServiceLoader> sl = null;
        if (s != null) {
            sl = s.loader();
            if (ServiceLoader.class.equals(sl)) sl = null;
        }
        if (sl == null) sl = SimpleServiceLoader.class;
        try {
            return sl.getConstructor(ServiceInfo.class).newInstance(new InfoImpl<S, R>(loader, filter, result, service, false));
        } catch (Exception e) {
            throw new RuntimeException("Error invoking loader " + sl.getName() + " for service " + service.getName(), e);
        }
    }

    public static <S> ServiceLoader<S> load(Class<S> service, ClassLoader loader, Condition<String[]> filter) {
        return load(service, service, loader, filter);
    }

    public static <S extends Annotation> ServiceLoader<Annotation> loadAnnotation(Class<S> service, ClassLoader loader, Condition<String[]> filter) {
        return load(service, Annotation.class, loader, filter);
    }

    /**
     * Creates a new service loader for the given service type, using the current thread's
     * {@linkplain java.lang.Thread#getContextClassLoader context class loader}.
     *
     * <p>
     * An invocation of this convenience method of the form
     *
     * <blockquote>
     *
     * <pre>
     *  ServiceLoader.load(&lt;i&gt;service&lt;/i&gt;)
     * </pre>
     *
     * </blockquote>
     *
     * is equivalent to
     *
     * <blockquote>
     *
     * <pre>
     *  ServiceLoader.load(&lt;i&gt;service&lt;/i&gt;,
     *                     Thread.currentThread().getContextClassLoader())
     * </pre>
     *
     * </blockquote>
     *
     * @param service The interface or abstract class representing the service
     *
     * @return A new service loader
     */
    public static <S, R> ServiceLoader<R> load(Class<S> service, Class<R> result, Condition<String[]> filter) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return load(service, result, cl, filter);
    }

    public static <S> ServiceLoader<S> load(Class<S> service, Condition<String[]> filter) {
        return load(service, service, filter);
    }

    public static <S extends Annotation> ServiceLoader<Annotation> loadAnnotation(Class<S> service, Condition<String[]> filter) {
        return load(service, Annotation.class, filter);
    }

    /**
     * Creates a new service loader for the given service type, using the extension class loader.
     *
     * <p>
     * This convenience method simply locates the extension class loader, call it <tt><i>extClassLoader</i></tt>,
     * and then returns
     *
     * <blockquote>
     *
     * <pre>
     *  ServiceLoader.load(&lt;i&gt;service&lt;/i&gt;, &lt;i&gt;extClassLoader&lt;/i&gt;)
     * </pre>
     *
     * </blockquote>
     *
     * <p>
     * If the extension class loader cannot be found then the system class loader is used; if there is no system class
     * loader then the bootstrap class loader is used.
     *
     * <p>
     * This method is intended for use when only installed providers are desired. The resulting service will only find
     * and load providers that have been installed into the current Java virtual machine; providers on the application's
     * class path will be ignored.
     *
     * @param service The interface or abstract class representing the service
     *
     * @return A new service loader
     */
    public static <S, R> ServiceLoader<R> loadInstalled(Class<S> service, Class<R> result, Condition<String[]> filter) {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        ClassLoader prev = null;
        while (cl != null) {
            prev = cl;
            cl = cl.getParent();
        }
        return load(service, result, prev, filter);
    }

    public static <S, R> ServiceLoader<R> loadInstalled(Class<S> service, Class<R> result) {
        return loadInstalled(service, result, null);
    }

    public static <S> ServiceLoader<S> loadInstalled(Class<S> service, Condition<String[]> filter) {
        return loadInstalled(service, service, filter);
    }

    public static <S extends Annotation> ServiceLoader<Annotation> loadInstalledAnnotation(Class<S> service, Condition<String[]> filter) {
        return loadInstalled(service, Annotation.class, filter);
    }

    @SuppressWarnings("unchecked")
    public static <R> Collection<R> instantiate(Class<R> srvClass) {
        if (srvClass.isAssignableFrom(Enum.class)) return Arrays.asList(srvClass.getEnumConstants());
        try {
            return Collections.singletonList((R) srvClass.getMethod("getInstance").invoke(null));
        } catch (NoSuchMethodException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) throw (RuntimeException) e.getCause();
            throw new RuntimeException(e);
        }
        try {
            return Collections.singletonList(srvClass.newInstance());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public static <R> Collection<R> instantiateAll(ServiceLoader<R> services) {
        ArrayList<R> result = new ArrayList<R>();
        for (Class<R> svc : services) result.addAll(instantiate(svc));
        return result;
    }
}
