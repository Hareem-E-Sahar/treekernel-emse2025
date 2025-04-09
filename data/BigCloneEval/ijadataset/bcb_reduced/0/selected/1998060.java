package fi.mmmtike.tiira.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.springframework.context.support.GenericApplicationContext;
import fi.mmmtike.tiira.cache.TiiraCache;
import fi.mmmtike.tiira.context.service.TiiraLocalServiceFactory;
import fi.mmmtike.tiira.context.service.TiiraServiceContext;
import fi.mmmtike.tiira.context.service.call.TiiraCallParam;
import fi.mmmtike.tiira.context.service.resolver.TiiraService;
import fi.mmmtike.tiira.exception.TiiraException;

/**
 * Reflection helper methods. 
 
 * @author Tomi Tuomainen
 *
 */
@SuppressWarnings("unchecked")
public class TiiraReflectionUtils {

    private static final Class<?>[] EMPTY_CLASS_PARAMS = new Class<?>[0];

    private static final Object[] EMPTY_PARAMS = new Object[0];

    private TiiraCache serviceImplCache = new TiiraCache(TiiraLocalServiceFactory.class.getName());

    private TiiraBeanUtils beanUtils = new TiiraBeanUtils();

    /**
	  * Creates a new instance of given class using no argument constructor.
	  * @param aClass	a class
	  * @return			instance of the class
	  */
    public Object createNewObject(Class aClass) {
        return createNewObject(aClass, EMPTY_CLASS_PARAMS, EMPTY_PARAMS);
    }

    /**
    * Creates a new instance of given class using constructor matching parameter classes.
    * @param aClass a class
    * @param paramClasses   
    * @param params     
    * @return     instance of the class
    */
    public Object createNewObject(Class aClass, Class<?>[] paramClasses, Object[] params) {
        try {
            Constructor c = aClass.getConstructor(paramClasses);
            Object obj = c.newInstance(params);
            return obj;
        } catch (Exception ex) {
            throw new TiiraException(ex);
        }
    }

    /**
		 * 
		 * @param interfaceClass	service interface
		 * @return					implementation class
		 */
    public Class fetchImplClass(Class interfaceClass, TiiraServiceContext serviceContext) {
        TiiraService service = serviceContext.getServiceResolver().fetchServicesMap().get(interfaceClass.getName());
        if (service != null) {
            return service.getServiceImpl();
        }
        return null;
    }

    /**
		  * Fetches service implementation for service interface. 
		  * 
		  * @param serviceInterface	
		  * @return	instance of service implementation
		  */
    private Object fetchServiceImpl(Class serviceInterface, TiiraServiceContext tiiraServiceContext) {
        Class implClass = fetchImplClass(serviceInterface, tiiraServiceContext);
        if (implClass == null) {
            throw new TiiraException("Service implementation class not found for interface " + serviceInterface.getName());
        }
        GenericApplicationContext appContext = tiiraServiceContext.getApplicationContext();
        if (appContext != null) {
            return beanUtils.fetchBean(appContext, implClass);
        } else {
            return fetchReflectionServiceImpl(implClass, tiiraServiceContext);
        }
    }

    /**
		  * 
		  * @param implClass	service implementation class
		  * @return				singleton instance, created with reflection
		  */
    private Object fetchReflectionServiceImpl(Class implClass, TiiraServiceContext tiiraServiceContext) {
        Object serviceImpl = getServiceImplFromCache(implClass);
        if (serviceImpl == null) {
            serviceImpl = createNewObject(implClass);
            putServiceImplToCache(implClass, serviceImpl);
        }
        return serviceImpl;
    }

    /**
		  * 
		  * @param implClass	 service impl class
		  * @return				cached instance of the class, null if not found
		  */
    private Object getServiceImplFromCache(Class implClass) {
        Object key = createCacheKey(implClass);
        return serviceImplCache.get(key);
    }

    /**
		  * 
		  * @param implClass		service impl class
		  * @param serviceImpl		service impl to cache
		  */
    private void putServiceImplToCache(Class implClass, Object serviceImpl) {
        Object key = createCacheKey(implClass);
        serviceImplCache.put(key, serviceImpl);
    }

    /**
		  * @param implClass	service implementation class
		  * @return				key for caching
		  */
    private Object createCacheKey(Class implClass) {
        return implClass.getName();
    }

    /**
		 * Invokes service implementation via reflection.
		 * 
		 * @param context		the call context
		 * @return				method result
		 */
    public Object invokeServiceImpl(TiiraCallParam context, TiiraServiceContext tiiraServiceContext) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Object serviceImpl = fetchServiceImpl(context.getServiceInterface(), tiiraServiceContext);
        Class implClass = serviceImpl.getClass();
        Method m = implClass.getMethod(context.getMethod(), context.getParamTypes());
        return m.invoke(serviceImpl, context.getParams());
    }

    /**
		  * 
		  * @param aClass
		  * @return class name without package prefix
		  */
    public String resolveShortName(Class aClass) {
        String fullName = aClass.getName();
        String s = fullName.substring(fullName.lastIndexOf(".") + 1);
        return s;
    }
}
