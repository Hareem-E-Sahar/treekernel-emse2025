package org.jsserv.resource;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.log4j.Logger;

/**
 * Loads and caches the javascript files and other resources.   
 * @author shelmberger
 */
public class URLResourceLoader<T extends URLResourceHolder> {

    protected static Logger log = Logger.getLogger(URLResourceLoader.class);

    private ConcurrentMap<URL, T> cached = new ConcurrentHashMap<URL, T>();

    private Class<T> cls;

    public URLResourceLoader(Class<T> cls) {
        if (!URLResourceHolder.class.isAssignableFrom(cls)) {
            throw new IllegalArgumentException(cls + " is no " + URLResourceHolder.class);
        }
        this.cls = cls;
    }

    public T load(URL url, boolean canCache) throws IOException, SecurityException, IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        T holder = createHolder(url);
        T existing = null;
        if (canCache) {
            existing = cached.putIfAbsent(url, holder);
        }
        if (existing == null) {
            if (log.isDebugEnabled()) {
                log.debug("return new holder" + holder);
            }
            return holder;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("return existing holder" + holder);
            }
            return existing;
        }
    }

    public T createHolder(URL url) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Constructor<T> ctor = cls.getConstructor(URL.class);
        return ctor.newInstance(url);
    }
}
