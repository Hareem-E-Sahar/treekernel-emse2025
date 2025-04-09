package org.extwind.osgi.tapestry.internal.ioc;

import static java.lang.String.format;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import org.apache.tapestry5.ioc.AdvisorDef;
import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.ServiceBuilderResources;
import org.apache.tapestry5.ioc.ServiceLifecycle2;
import org.apache.tapestry5.ioc.ServiceResources;
import org.apache.tapestry5.ioc.def.ContributionDef;
import org.apache.tapestry5.ioc.def.DecoratorDef;
import org.apache.tapestry5.ioc.def.ModuleDef;
import org.apache.tapestry5.ioc.def.ModuleDef2;
import org.apache.tapestry5.ioc.def.ServiceDef;
import org.apache.tapestry5.ioc.def.ServiceDef2;
import org.apache.tapestry5.ioc.internal.AdvisorStackBuilder;
import org.apache.tapestry5.ioc.internal.EagerLoadServiceProxy;
import org.apache.tapestry5.ioc.internal.InterceptorStackBuilder;
import org.apache.tapestry5.ioc.internal.InternalRegistry;
import org.apache.tapestry5.ioc.internal.LifecycleWrappedServiceCreator;
import org.apache.tapestry5.ioc.internal.Module;
import org.apache.tapestry5.ioc.internal.ObjectLocatorImpl;
import org.apache.tapestry5.ioc.internal.OperationTrackingObjectCreator;
import org.apache.tapestry5.ioc.internal.RecursiveServiceCreationCheckWrapper;
import org.apache.tapestry5.ioc.internal.ServiceActivityTracker;
import org.apache.tapestry5.ioc.internal.ServiceResourcesImpl;
import org.apache.tapestry5.ioc.internal.services.JustInTimeObjectCreator;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.ConcurrentBarrier;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.internal.util.InjectionResources;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.MapInjectionResources;
import org.apache.tapestry5.ioc.services.AspectDecorator;
import org.apache.tapestry5.ioc.services.ClassFab;
import org.apache.tapestry5.ioc.services.ClassFactory;
import org.apache.tapestry5.ioc.services.MethodSignature;
import org.apache.tapestry5.ioc.services.Status;
import org.slf4j.Logger;

/**
 * @author Donf Yang
 */
public class DynamicModuleImpl implements DynamicModule {

    protected final InternalRegistry registry;

    protected final ServiceActivityTracker tracker;

    protected final ModuleDef2 moduleDef;

    protected final ClassFactory classFactory;

    private final Logger logger;

    /**
	 * Lazily instantiated. Access is guarded by BARRIER.
	 */
    protected Object moduleInstance;

    protected boolean insideConstructor;

    /**
	 * Keyed on fully qualified service id; values are instantiated services
	 * (proxies). Guarded by BARRIER.
	 */
    protected final Map<String, Object> services = CollectionFactory.newCaseInsensitiveMap();

    protected final Map<String, DynamicObjectCreator> serviceCreators = CollectionFactory.newCaseInsensitiveMap();

    protected final Map<String, ServiceDef2> serviceDefs = CollectionFactory.newCaseInsensitiveMap();

    /**
	 * The barrier is shared by all modules, which means that creation of *any*
	 * service for any module is single threaded.
	 */
    protected static final ConcurrentBarrier BARRIER = new ConcurrentBarrier();

    public DynamicModuleImpl(InternalRegistry registry, ServiceActivityTracker tracker, ModuleDef moduleDef, ClassFactory classFactory, Logger logger) {
        this.registry = registry;
        this.tracker = tracker;
        this.moduleDef = InternalUtils.toModuleDef2(moduleDef);
        this.classFactory = classFactory;
        this.logger = logger;
        for (String id : moduleDef.getServiceIds()) {
            ServiceDef sd = moduleDef.getServiceDef(id);
            ServiceDef2 sd2 = InternalUtils.toServiceDef2(sd);
            serviceDefs.put(id, sd2);
        }
    }

    public <T> T getService(String serviceId, Class<T> serviceInterface) {
        Defense.notBlank(serviceId, "serviceId");
        Defense.notNull(serviceInterface, "serviceInterface");
        ServiceDef2 def = getServiceDef(serviceId);
        assert def != null;
        Object service = findOrCreate(def, null);
        try {
            return serviceInterface.cast(service);
        } catch (ClassCastException ex) {
            throw new RuntimeException(ProxyIOCMessages.serviceWrongInterface(serviceId, def.getServiceInterface(), serviceInterface));
        }
    }

    public Set<DecoratorDef> findMatchingDecoratorDefs(ServiceDef serviceDef) {
        Set<DecoratorDef> result = CollectionFactory.newSet();
        for (DecoratorDef def : moduleDef.getDecoratorDefs()) {
            if (def.matches(serviceDef)) result.add(def);
        }
        return result;
    }

    public Set<AdvisorDef> findMatchingServiceAdvisors(ServiceDef serviceDef) {
        Set<AdvisorDef> result = CollectionFactory.newSet();
        for (AdvisorDef def : moduleDef.getAdvisorDefs()) {
            if (def.matches(serviceDef)) result.add(def);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public Collection<String> findServiceIdsForInterface(Class serviceInterface) {
        Defense.notNull(serviceInterface, "serviceInterface");
        Collection<String> result = CollectionFactory.newList();
        for (ServiceDef2 def : serviceDefs.values()) {
            Class clz = def.getServiceInterface();
            if (serviceInterface.isAssignableFrom(def.getServiceInterface())) result.add(def.getServiceId());
        }
        return result;
    }

    /**
	 * Locates the service proxy for a particular service (from the service
	 * definition).
	 * 
	 * @param def
	 *            defines the service
	 * @param eagerLoadProxies
	 *            collection into which proxies for eager loaded services are
	 *            added (or null)
	 * @return the service proxy
	 */
    protected Object findOrCreate(final ServiceDef2 def, final Collection<EagerLoadServiceProxy> eagerLoadProxies) {
        final String key = def.getServiceId();
        final Invokable create = new Invokable() {

            public Object invoke() {
                Object result = services.get(key);
                if (result == null) {
                    result = create(def, eagerLoadProxies);
                    services.put(key, result);
                }
                return result;
            }
        };
        Invokable find = new Invokable() {

            public Object invoke() {
                Object result = services.get(key);
                String serviceId = key;
                if (result == null) result = BARRIER.withWrite(create);
                return result;
            }
        };
        return BARRIER.withRead(find);
    }

    public void collectEagerLoadServices(final Collection<EagerLoadServiceProxy> proxies) {
        Runnable work = new Runnable() {

            public void run() {
                for (ServiceDef2 def : serviceDefs.values()) {
                    if (def.isEagerLoad()) findOrCreate(def, proxies);
                }
            }
        };
        registry.run("Eager loading services", work);
    }

    /**
	 * Creates the service and updates the cache of created services.
	 * 
	 * @param eagerLoadProxies
	 *            a list into which any eager loaded proxies should be added
	 */
    protected Object create(final ServiceDef2 def, final Collection<EagerLoadServiceProxy> eagerLoadProxies) {
        final String serviceId = def.getServiceId();
        final Logger logger = registry.getServiceLogger(serviceId);
        String description = ProxyIOCMessages.creatingService(serviceId);
        if (logger.isDebugEnabled()) logger.debug(description);
        final Module module = this;
        Invokable operation = new Invokable() {

            public Object invoke() {
                try {
                    final ServiceBuilderResources resources = new ServiceResourcesImpl(registry, module, def, classFactory, logger);
                    Invokable<ObjectCreator> delegateOperation = new Invokable<ObjectCreator>() {

                        public ObjectCreator invoke() {
                            return createDelegate(resources, serviceId, def);
                        }
                    };
                    DynamicObjectCreatorImpl DynamicObjectCreatorImpl = new DynamicObjectCreatorImpl(registry, delegateOperation, serviceId);
                    serviceCreators.put(serviceId, DynamicObjectCreatorImpl);
                    Object proxy = createProxy(resources, DynamicObjectCreatorImpl);
                    registry.addRegistryShutdownListener(DynamicObjectCreatorImpl);
                    if (def.isEagerLoad() && eagerLoadProxies != null) eagerLoadProxies.add(DynamicObjectCreatorImpl);
                    tracker.setStatus(serviceId, Status.VIRTUAL);
                    return proxy;
                } catch (Exception ex) {
                    throw new RuntimeException(ProxyIOCMessages.errorBuildingService(serviceId, def, ex), ex);
                }
            }
        };
        return registry.invoke(description, operation);
    }

    protected ObjectCreator createDelegate(ServiceBuilderResources resources, String serviceId, final ServiceDef2 def) {
        final Logger logger = registry.getServiceLogger(serviceId);
        ObjectCreator creator = def.createServiceCreator(resources);
        Class serviceInterface = def.getServiceInterface();
        ServiceLifecycle2 lifecycle = registry.getServiceLifecycle(def.getServiceScope());
        if (!serviceInterface.isInterface()) {
            return new ObjectCreator() {

                public Object createObject() {
                    return new IllegalArgumentException(String.format("Service scope '%s' requires a proxy, but the service does not have a service interface (necessary to create a proxy). Provide a service interface or select a different service scope.", def.getServiceScope()));
                }
            };
        }
        creator = new OperationTrackingObjectCreator(registry, "Invoking " + creator.toString(), creator);
        creator = new LifecycleWrappedServiceCreator(lifecycle, resources, creator);
        if (!def.isPreventDecoration()) {
            creator = new AdvisorStackBuilder(def, creator, getAspectDecorator(), registry);
            creator = new InterceptorStackBuilder(def, creator, registry);
        }
        creator = new RecursiveServiceCreationCheckWrapper(def, creator, logger);
        creator = new OperationTrackingObjectCreator(registry, "Realizing service " + serviceId, creator);
        JustInTimeObjectCreator delegate = new JustInTimeObjectCreator(tracker, creator, serviceId);
        return delegate;
    }

    protected AspectDecorator getAspectDecorator() {
        return registry.invoke("Obtaining AspectDecorator service", new Invokable<AspectDecorator>() {

            public AspectDecorator invoke() {
                return registry.getService(AspectDecorator.class);
            }
        });
    }

    protected final Runnable instantiateModule = new Runnable() {

        public void run() {
            moduleInstance = registry.invoke("Constructing module class " + moduleDef.getBuilderClass().getName(), new Invokable() {

                public Object invoke() {
                    return instantiateModuleInstance();
                }
            });
        }
    };

    protected final Invokable provideModuleInstance = new Invokable<Object>() {

        public Object invoke() {
            if (moduleInstance == null) BARRIER.withWrite(instantiateModule);
            return moduleInstance;
        }
    };

    public Object getModuleBuilder() {
        return BARRIER.withRead(provideModuleInstance);
    }

    protected Object instantiateModuleInstance() {
        Class moduleClass = moduleDef.getBuilderClass();
        Constructor[] constructors = moduleClass.getConstructors();
        if (constructors.length == 0) throw new RuntimeException(ProxyIOCMessages.noPublicConstructors(moduleClass));
        if (constructors.length > 1) {
            Comparator<Constructor> comparator = new Comparator<Constructor>() {

                public int compare(Constructor c1, Constructor c2) {
                    return c2.getParameterTypes().length - c1.getParameterTypes().length;
                }
            };
            Arrays.sort(constructors, comparator);
            logger.warn(ProxyIOCMessages.tooManyPublicConstructors(moduleClass, constructors[0]));
        }
        Constructor constructor = constructors[0];
        if (insideConstructor) throw new RuntimeException(ProxyIOCMessages.recursiveModuleConstructor(moduleClass, constructor));
        ObjectLocator locator = new ObjectLocatorImpl(registry, this);
        Map<Class, Object> resourcesMap = CollectionFactory.newMap();
        resourcesMap.put(Logger.class, logger);
        resourcesMap.put(ObjectLocator.class, locator);
        resourcesMap.put(OperationTracker.class, registry);
        InjectionResources resources = new MapInjectionResources(resourcesMap);
        Throwable fail = null;
        try {
            insideConstructor = true;
            Object[] parameterValues = InternalUtils.calculateParameters(locator, resources, constructor.getParameterTypes(), constructor.getGenericParameterTypes(), constructor.getParameterAnnotations(), registry);
            Object result = constructor.newInstance(parameterValues);
            InternalUtils.injectIntoFields(result, locator, resources, registry);
            return result;
        } catch (InvocationTargetException ex) {
            fail = ex.getTargetException();
        } catch (Exception ex) {
            fail = ex;
        } finally {
            insideConstructor = false;
        }
        throw new RuntimeException(ProxyIOCMessages.instantiateBuilderError(moduleClass, fail), fail);
    }

    protected Object createProxy(ServiceResources resources, ObjectCreator creator) {
        String serviceId = resources.getServiceId();
        Class serviceInterface = resources.getServiceInterface();
        String toString = format("<Proxy for %s(%s)>", serviceId, serviceInterface.getName());
        return createProxyInstance(creator, serviceId, serviceInterface, toString);
    }

    protected Object createProxyInstance(ObjectCreator creator, String serviceId, Class serviceInterface, String description) {
        ProxyServiceProxyToken token = ProxySerializationSupport.createToken(serviceId);
        ClassFab classFab = registry.newClass(serviceInterface);
        classFab.addField("creator", Modifier.PRIVATE | Modifier.FINAL, ObjectCreator.class);
        classFab.addField("token", Modifier.PRIVATE | Modifier.FINAL, ProxyServiceProxyToken.class);
        classFab.addConstructor(new Class[] { ObjectCreator.class, ProxyServiceProxyToken.class }, null, "{ creator = $1; token = $2; }");
        classFab.addInterface(Serializable.class);
        classFab.addInterface(serviceInterface);
        MethodSignature writeReplaceSig = new MethodSignature(Object.class, "writeReplace", null, new Class[] { ObjectStreamException.class });
        classFab.addMethod(Modifier.PRIVATE, writeReplaceSig, "return token;");
        String body = format("return (%s) creator.createObject();", serviceInterface.getName());
        MethodSignature sig = new MethodSignature(serviceInterface, "delegate", null, null);
        classFab.addMethod(Modifier.PRIVATE, sig, body);
        classFab.proxyMethodsToDelegate(serviceInterface, "delegate()", description);
        Class proxyClass = classFab.createClass();
        try {
            return proxyClass.getConstructors()[0].newInstance(creator, token);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public Set<ContributionDef> getContributorDefsForService(String serviceId) {
        Set<ContributionDef> result = CollectionFactory.newSet();
        for (ContributionDef def : moduleDef.getContributionDefs()) {
            if (def.getServiceId().equals(serviceId)) result.add(def);
        }
        return result;
    }

    public ServiceDef2 getServiceDef(String serviceId) {
        return serviceDefs.get(serviceId);
    }

    public String getLoggerName() {
        return moduleDef.getLoggerName();
    }

    @Override
    public String toString() {
        return String.format("ModuleImpl[%s]", moduleDef.getLoggerName());
    }

    public void dirty(String serviceId) {
        services.remove(serviceId);
        DynamicObjectCreator creator = serviceCreators.get(serviceId);
        if (creator != null) {
            creator.dirty();
        }
    }
}
