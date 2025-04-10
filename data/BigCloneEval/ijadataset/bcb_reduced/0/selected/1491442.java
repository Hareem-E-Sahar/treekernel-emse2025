package net.sf.gilead.loading.proxy;

import java.util.HashMap;
import java.util.Map;
import net.sf.gilead.loading.annotations.LoadingInterface;
import net.sf.gilead.loading.proxy.wrapper.LoadingList;
import net.sf.gilead.loading.proxy.wrapper.LoadingWrapper;
import net.sf.gilead.proxy.JavassistProxyGenerator;
import net.sf.gilead.proxy.xml.AdditionalCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

/**
 * Additional code generator for loading interfaces.
 * @author bruno.marchesson
 */
public class LoadingProxyManager {

    /**
	 * Logger channel.
	 */
    private static Logger _log = LoggerFactory.getLogger(LoadingProxyManager.class);

    /**
	 * Proxy map.
	 * Key is the loading interface, value is the associated wrapper.
	 */
    private Map<Class<?>, Class<?>> _proxyMap;

    /**
	 * Unique instance of singleton
	 */
    private static LoadingProxyManager _instance;

    /**
	 * @return the unique instance of the singleton
	 */
    public static LoadingProxyManager getInstance() {
        if (_instance == null) {
            _instance = new LoadingProxyManager();
        }
        return _instance;
    }

    /**
	 * Ctor
	 */
    protected LoadingProxyManager() {
        _proxyMap = new HashMap<Class<?>, Class<?>>();
    }

    /**
	 * Returns the wrapper association with the loading interface
	 * @param loadingInterface
	 * @return
	 */
    public Class<?> getWrapper(Class<?> loadingInterface) {
        Class<?> wrapperClass = _proxyMap.get(loadingInterface);
        if (wrapperClass != null) {
            return wrapperClass;
        }
        _proxyMap.put(loadingInterface, getClass());
        AdditionalCode additionalCode = LoadingProxyCreator.generateProxyFor(loadingInterface);
        JavassistProxyGenerator generator = new JavassistProxyGenerator();
        wrapperClass = generator.generateProxyFor(LoadingWrapper.class, additionalCode);
        _proxyMap.put(loadingInterface, wrapperClass);
        return wrapperClass;
    }

    /**
	 * Wrap the persistent entity with the loading interface relevant wrapper
	 * @param entity
	 * @param loadingInterface
	 * @return
	 */
    public Object wrapAs(Object entity, Class<?> loadingInterface) {
        if (entity == null) {
            return null;
        }
        try {
            Class<?> wrapperClass = getWrapper(loadingInterface);
            java.lang.reflect.Constructor<?> constructor = wrapperClass.getConstructor(entity.getClass());
            return constructor.newInstance(entity);
        } catch (Exception e) {
            throw new RuntimeException("Wrapper creation exception", e);
        }
    }

    /**
	 * Wrap the persistent entity collection with the loading interface relevant wrapper
	 * @param collection
	 * @param loadingInterface
	 * @return
	 */
    public Object wrapCollectionAs(Object collection, Class<?> loadingInterface) {
        if (collection == null) {
            return null;
        }
        if (List.class.isAssignableFrom(collection.getClass())) {
        }
        return null;
    }
}
