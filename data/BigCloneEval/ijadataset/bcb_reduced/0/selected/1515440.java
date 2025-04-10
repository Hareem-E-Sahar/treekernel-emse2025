package java.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import org.apache.harmony.luni.util.Msg;

/**
 * Service loader is a service-provider loading utility class.
 * @param <S> 
 * 
 * @since 1.6
 */
public final class ServiceLoader<S> implements Iterable<S> {

    private static final String META_INF_SERVICES = "META-INF/services/";

    private Set<URL> services;

    private Class<S> service;

    private ClassLoader loader;

    private ServiceLoader() {
    }

    /**
     * reloads the services
     * 
     */
    public void reload() {
        internalLoad(this, service, loader);
    }

    /**
     * Answers the iterator of this ServiceLoader
     * 
     * @return the iterator of this ServiceLoader
     */
    public Iterator<S> iterator() {
        return new ServiceIterator(this);
    }

    /**
     * Constructs a serviceloader.
     * 
     * @param service
     *            the given service class or interface
     * @param loader
     *            the given class loader
     * @return a new ServiceLoader
     */
    public static <S> ServiceLoader<S> load(Class<S> service, ClassLoader loader) {
        ServiceLoader<S> sl = new ServiceLoader<S>();
        sl.service = service;
        sl.loader = loader;
        sl.services = new HashSet<URL>();
        internalLoad(sl, service, loader);
        return sl;
    }

    private static void internalLoad(ServiceLoader<?> sl, Class<?> service, ClassLoader loader) {
        Enumeration<URL> profiles = null;
        if (null == service) {
            sl.services.add(null);
            return;
        }
        try {
            if (null == loader) {
                profiles = ClassLoader.getSystemResources(META_INF_SERVICES + service.getName());
            } else {
                profiles = loader.getResources(META_INF_SERVICES + service.getName());
            }
        } catch (IOException e) {
            return;
        }
        if (null != profiles) {
            while (profiles.hasMoreElements()) {
                URL url = profiles.nextElement();
                sl.services.add(url);
            }
        }
    }

    /**
     * Constructs a serviceloader.
     * 
     * @param service
     *            the given service class or interface
     * @return a new ServiceLoader
     */
    public static <S> ServiceLoader<S> load(Class<S> service) {
        return ServiceLoader.load(service, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Constructs a serviceloader with extension class loader.
     * 
     * @param service
     *            the given service class or interface
     * @return a new ServiceLoader
     */
    public static <S> ServiceLoader<S> loadInstalled(Class<S> service) {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        if (null != cl) {
            while (null != cl.getParent()) {
                cl = cl.getParent();
            }
        }
        return ServiceLoader.load(service, cl);
    }

    /**
     * Answers a string that indicate the information of this ServiceLoader
     * 
     * @return a string that indicate the information of this ServiceLoader
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ServiceLoader of ");
        sb.append(service.getName());
        return sb.toString();
    }

    private class ServiceIterator implements Iterator<S> {

        private static final String SINGAL_SHARP = "#";

        private ClassLoader cl;

        private Class<S> service;

        private Set<URL> services;

        private BufferedReader reader = null;

        private boolean isRead = false;

        private Queue<String> que;

        public ServiceIterator(ServiceLoader<S> sl) {
            cl = sl.loader;
            service = sl.service;
            services = sl.services;
        }

        public boolean hasNext() {
            if (!isRead) {
                readClass();
            }
            if (null != que && !que.isEmpty()) {
                return true;
            }
            return false;
        }

        @SuppressWarnings("unchecked")
        public S next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            String clsName = que.remove();
            try {
                S ret;
                if (null == cl) {
                    ret = service.cast(Class.forName(clsName).newInstance());
                } else {
                    ret = service.cast(cl.loadClass(clsName).newInstance());
                }
                return ret;
            } catch (Exception e) {
                throw new ServiceConfigurationError(Msg.getString("KB005", clsName), e);
            }
        }

        private void readClass() {
            if (null == services) {
                isRead = true;
                return;
            }
            Iterator<URL> iter = services.iterator();
            que = new LinkedList<String>();
            while (iter.hasNext()) {
                URL url = iter.next();
                if (null == url) {
                    throw new NullPointerException();
                }
                try {
                    reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
                    String str;
                    while (true) {
                        str = reader.readLine();
                        if (null == str) {
                            break;
                        }
                        String[] strs = str.trim().split(SINGAL_SHARP);
                        if (0 != strs.length) {
                            str = strs[0].trim();
                            if (!(str.startsWith(SINGAL_SHARP) || 0 == str.length())) {
                                char[] namechars = str.toCharArray();
                                for (int i = 0; i < namechars.length; i++) {
                                    if (!(Character.isJavaIdentifierPart(namechars[i]) || namechars[i] == '.')) {
                                        throw new ServiceConfigurationError(Msg.getString("KB006", namechars[i]));
                                    }
                                }
                                if (!que.contains(str)) {
                                    que.add(str);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new ServiceConfigurationError(Msg.getString("KB006", url), e);
                }
            }
            isRead = true;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
