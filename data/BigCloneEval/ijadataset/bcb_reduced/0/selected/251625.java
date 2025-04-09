package org.nakedobjects.nof.core.adapter.map;

import org.nakedobjects.noa.NakedObjectRuntimeException;
import org.nakedobjects.noa.adapter.Naked;
import org.nakedobjects.noa.adapter.NakedCollection;
import org.nakedobjects.noa.adapter.NakedObject;
import org.nakedobjects.noa.adapter.NakedObjectLoader;
import org.nakedobjects.noa.adapter.NakedReference;
import org.nakedobjects.noa.adapter.NakedValue;
import org.nakedobjects.noa.adapter.ObjectLoaderException;
import org.nakedobjects.noa.adapter.Oid;
import org.nakedobjects.noa.adapter.ResolveState;
import org.nakedobjects.noa.reflect.NakedObjectField;
import org.nakedobjects.noa.reflect.NakedObjectReflector;
import org.nakedobjects.noa.reflect.SpecObjectPair;
import org.nakedobjects.noa.spec.NakedCollectionSpecification;
import org.nakedobjects.noa.spec.NakedObjectSpecification;
import org.nakedobjects.nof.core.adapter.PojoAdapter;
import org.nakedobjects.nof.core.adapter.value.AwtImageAdapter;
import org.nakedobjects.nof.core.adapter.value.BigDecimalAdapter;
import org.nakedobjects.nof.core.adapter.value.BigIntegerAdapter;
import org.nakedobjects.nof.core.adapter.value.BooleanAdapter;
import org.nakedobjects.nof.core.adapter.value.ByteAdapter;
import org.nakedobjects.nof.core.adapter.value.CharAdapter;
import org.nakedobjects.nof.core.adapter.value.DateAdapter;
import org.nakedobjects.nof.core.adapter.value.DateTimeAdapter;
import org.nakedobjects.nof.core.adapter.value.DoubleAdapter;
import org.nakedobjects.nof.core.adapter.value.FloatAdapter;
import org.nakedobjects.nof.core.adapter.value.IntAdapter;
import org.nakedobjects.nof.core.adapter.value.LongAdapter;
import org.nakedobjects.nof.core.adapter.value.PrimitiveBooleanAdapter;
import org.nakedobjects.nof.core.adapter.value.PrimitiveByteAdapter;
import org.nakedobjects.nof.core.adapter.value.PrimitiveCharAdapter;
import org.nakedobjects.nof.core.adapter.value.PrimitiveDoubleAdapter;
import org.nakedobjects.nof.core.adapter.value.PrimitiveFloatAdapter;
import org.nakedobjects.nof.core.adapter.value.PrimitiveIntAdapter;
import org.nakedobjects.nof.core.adapter.value.PrimitiveLongAdapter;
import org.nakedobjects.nof.core.adapter.value.PrimitiveShortAdapter;
import org.nakedobjects.nof.core.adapter.value.ShortAdapter;
import org.nakedobjects.nof.core.adapter.value.StringAdapter;
import org.nakedobjects.nof.core.adapter.value.TimeAdapter;
import org.nakedobjects.nof.core.context.NakedObjectsContext;
import org.nakedobjects.nof.core.util.Assert;
import org.nakedobjects.nof.core.util.DebugInfo;
import org.nakedobjects.nof.core.util.DebugString;
import org.nakedobjects.nof.core.util.ToString;
import org.nakedobjects.nof.core.util.UnknownTypeException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Enumeration;
import java.util.Hashtable;
import org.apache.log4j.Logger;

public abstract class ObjectLoaderImpl implements NakedObjectLoader, DebugInfo {

    private static final Logger LOG = Logger.getLogger(ObjectLoaderImpl.class);

    protected PojoAdapterMap pojoAdapterMap;

    protected IdentityAdapterMap identityAdapterMap;

    private final Hashtable adapterClasses = new Hashtable();

    private Object[] services;

    public NakedObject createAdapterForTransient(final Object object) {
        return createAdapterForTransient(object, true);
    }

    /**
     * 
     * @param object
     * @param defaultProperties
     * @return
     */
    public NakedObject createAdapterForTransient(final Object object, boolean defaultProperties) {
        Oid transientOid = NakedObjectsContext.getObjectPersistor().createTransientOid(object);
        PojoAdapter adapter = (PojoAdapter) createObjectAdapter(transientOid, object);
        LOG.debug("creating adapter (transient) " + adapter);
        Assert.assertEquals(adapter, pojoAdapterMap.getPojo(object));
        if (defaultProperties) {
            NakedObjectField[] fields = adapter.getSpecification().getFields();
            for (int i = 0; i < fields.length; i++) {
                fields[i].toDefault(adapter);
            }
            adapter.getSpecification().lifecycleEvent(adapter, NakedObjectSpecification.CREATED);
        }
        adapter.changeState(ResolveState.TRANSIENT);
        return adapter;
    }

    /**
     * Creates adapter for Java primitives, or else delegates to the AdapterFactory class.
     */
    public NakedValue createAdapterForValue(final NakedObjectSpecification specification) {
        NakedValue adapter;
        Class adapterClass = (Class) adapterClasses.get(specification);
        if (adapterClass != null) {
            adapter = (NakedValue) createInstance(adapterClass);
        } else {
            adapter = NakedObjectsContext.getReflector().createValueAdapter(specification);
        }
        return adapter;
    }

    private Object createInstance(final Class adapterClass) {
        try {
            return adapterClass.newInstance();
        } catch (InstantiationException e) {
            throw new ObjectLoaderException("Failed to create instance of type " + adapterClass.getName(), e);
        } catch (IllegalAccessException e) {
            throw new ObjectLoaderException("Failed to create instance of type " + adapterClass.getName() + "; could not access constructor", e);
        }
    }

    public void addAdapterClass(Class valueClass, Class adapterClass) {
        NakedObjectSpecification valueSpecification = NakedObjectsContext.getReflector().loadSpecification(valueClass);
        adapterClasses.put(valueSpecification, adapterClass);
    }

    /**
     * Creates adapter for Java primitives, or else delegates to the AdapterFactory class.
     */
    public NakedValue createAdapterForValue(final Object value) {
        Assert.assertFalse("can't create an adapter for a NOF adapter", value instanceof Naked);
        Assert.assertFalse("can't create an adapter for a NO Specification", value instanceof NakedObjectSpecification);
        NakedValue adapter = null;
        NakedObjectReflector reflector = NakedObjectsContext.getReflector();
        NakedObjectSpecification specification = reflector.loadSpecification(value.getClass());
        Class adapterClass = (Class) adapterClasses.get(specification);
        if (adapterClass != null) {
            try {
                Constructor[] constructors = adapterClass.getConstructors();
                for (int i = 0; i < constructors.length; i++) {
                    if (constructors[i].getParameterTypes().length == 1) {
                        adapter = (NakedValue) constructors[i].newInstance(new Object[] { value });
                        break;
                    }
                }
                if (adapter == null) {
                    throw new ObjectLoaderException("Failed to find suitable constructor in " + adapterClass);
                }
            } catch (InstantiationException e) {
                throw new ObjectLoaderException("Failed to create value adapter of type " + adapterClass, e);
            } catch (IllegalAccessException e) {
                throw new ObjectLoaderException("Failed to create value adapter of type " + adapterClass + "; could not access constructor", e);
            } catch (IllegalArgumentException e) {
                throw new ObjectLoaderException("Failed to create value adapter of type " + adapterClass, e);
            } catch (InvocationTargetException e) {
                throw new ObjectLoaderException("Failed to create value adapter of type " + adapterClass, e);
            }
        } else {
            adapter = reflector.createValueAdapter(value);
        }
        return adapter;
    }

    public NakedCollection createAdapterForCollection(final Object collection, final NakedObjectSpecification specification) {
        Assert.assertFalse("Can't create an adapter for a NOF adapter", collection instanceof Naked);
        LOG.debug("creating adapter (collection) for " + collection);
        NakedCollection adapter;
        adapter = NakedObjectsContext.getReflector().createCollectionAdapter(collection, specification);
        if (adapter != null) {
            pojoAdapterMap.add(collection, adapter);
            LOG.debug("created " + adapter + " for " + collection);
            adapter.changeState(ResolveState.TRANSIENT);
            Assert.assertNotNull(adapter);
        }
        return adapter;
    }

    private Class classFor(final NakedObjectSpecification specification) {
        String className = specification.getFullName();
        Class cls;
        try {
            cls = Class.forName(className);
        } catch (ClassNotFoundException e) {
            if (className.equals("boolean")) {
                return boolean.class;
            } else if (className.equals("char")) {
                return char.class;
            } else if (className.equals("byte")) {
                return byte.class;
            } else if (className.equals("short")) {
                return short.class;
            } else if (className.equals("int")) {
                return int.class;
            } else if (className.equals("long")) {
                return long.class;
            } else if (className.equals("float")) {
                return float.class;
            } else if (className.equals("double")) {
                return double.class;
            }
            throw new NakedObjectRuntimeException(e);
        }
        return cls;
    }

    /**
     * Creates a new instance of the specified type, and then call the new objects setContainer() methods if
     * it has one.
     */
    private Object createObject(final Class cls) {
        if (cls.isArray()) {
            return Array.newInstance(cls.getComponentType(), 0);
        } else {
            if (Modifier.isAbstract(cls.getModifiers())) {
                throw new NakedObjectRuntimeException("Cannot create an instance of an abstract class: " + cls);
            }
            Object object = createInstance(cls);
            initDomainObject(object);
            return object;
        }
    }

    private Object createObject(final NakedObjectSpecification specification) {
        Class cls = classFor(specification);
        Object object = createObject(cls);
        return object;
    }

    private NakedObject createObjectAdapter(final Oid oid, final Object object) {
        Assert.assertNotNull(oid);
        Assert.assertNotNull(object);
        Assert.assertFalse("POJO Map already contains object", object, pojoAdapterMap.containsPojo(object));
        Assert.assertFalse("Can't create an adapter for a NOF adapter", object instanceof Naked);
        NakedObject nakedObject = new PojoAdapter(object, oid);
        LOG.debug("created PojoAdapter@" + Integer.toHexString(nakedObject.hashCode()) + " for " + new ToString(object));
        pojoAdapterMap.add(object, nakedObject);
        LOG.debug("adding identity " + oid + " for " + nakedObject);
        identityAdapterMap.add(oid, nakedObject);
        return nakedObject;
    }

    public NakedObject createTransientInstance(final NakedObjectSpecification specification) {
        Assert.assertTrue("must be an object", specification.getType() == NakedObjectSpecification.OBJECT);
        LOG.debug("creating transient instance of " + specification);
        Object object = createObject(specification);
        NakedObject adapter = createAdapterForTransient(object);
        return adapter;
    }

    public NakedCollection recreateCollection(final NakedObjectSpecification collectionSpecification, NakedObjectSpecification elementSpecification) {
        Assert.assertFalse("must not be an object", collectionSpecification.getType() == NakedObjectSpecification.OBJECT);
        Assert.assertFalse("must not be a value", collectionSpecification.getType() == NakedObjectSpecification.VALUE);
        LOG.debug("recreating collection " + collectionSpecification);
        Object object = createObject(collectionSpecification);
        NakedCollection adapter = createAdapterForCollection(object, elementSpecification);
        return adapter;
    }

    public NakedObject recreateTransientInstance(final Oid oid, final NakedObjectSpecification specification) {
        Assert.assertNotNull("must have an OID", oid);
        Assert.assertTrue("must be an object", specification, specification.getType() == NakedObjectSpecification.OBJECT);
        if (isIdentityKnown(oid)) {
            return getAdapterFor(oid);
        }
        LOG.debug("recreating transient instance of for " + specification);
        Object object = createObject(specification);
        NakedObject adapter = createObjectAdapter(oid, object);
        adapter.changeState(ResolveState.TRANSIENT);
        return adapter;
    }

    public NakedValue createValueInstance(final NakedObjectSpecification specification) {
        Assert.assertTrue("must be a value", specification, specification.getType() == NakedObjectSpecification.VALUE);
        return createAdapterForValue(specification);
    }

    public NakedObject getAdapterFor(final Object object) {
        Assert.assertNotNull("can't get an adapter for null", this, object);
        NakedObject adapter = (NakedObject) pojoAdapterMap.getPojo(object);
        return adapter;
    }

    public NakedObject getAdapterFor(final Oid oid) {
        Assert.assertNotNull("OID should not be null", this, oid);
        processChangedOid(oid);
        NakedObject adapter = identityAdapterMap.getAdapter(oid);
        return adapter;
    }

    public NakedCollection getAdapterForElseCreateAdapterForCollection(final NakedObject parent, final String fieldName, final NakedObjectSpecification specification, final Object collection) {
        Assert.assertNotNull("can't get an adapter for null", this, collection);
        InternalCollectionKey key = InternalCollectionKey.createKey(parent, fieldName);
        NakedCollection adapter = (NakedCollection) pojoAdapterMap.getPojo(key);
        if (adapter == null) {
            adapter = NakedObjectsContext.getReflector().createCollectionAdapter(collection, specification);
            pojoAdapterMap.add(key, adapter);
            if (parent.getResolveState().isPersistent()) {
                LOG.debug("creating adapter for persistent collection: " + collection);
                adapter.changeState(ResolveState.GHOST);
            } else {
                LOG.debug("creating adapter for transient collection: " + collection);
                adapter.changeState(ResolveState.TRANSIENT);
            }
        } else {
            Assert.assertSame(collection, adapter.getObject());
        }
        Assert.assertNotNull("should have an adapter for ", collection, adapter);
        return adapter;
    }

    public NakedObject getAdapterForElseCreateAdapterForTransient(final Object object) {
        NakedObject adapter = getAdapterFor(object);
        if (adapter == null) {
            LOG.debug("no existing adapter found; creating a transient adapter for " + new ToString(object));
            adapter = createAdapterForTransient(object);
        }
        Assert.assertNotNull("should have an adapter for ", object, adapter);
        return adapter;
    }

    public void debugData(final DebugString debug) {
        debug.appendln();
        debug.appendTitle("Services");
        for (int i = 0; i < services.length; i++) {
            debug.append(i + 1, 5);
            debug.append(" ");
            debug.append(services[i].getClass().getName(), 30);
            debug.append("   ");
            debug.appendln(services[i].toString());
        }
        debug.appendln();
        debug.appendTitle("POJO-Adapter Mappings");
        pojoAdapterMap.debugData(debug);
        debug.appendln();
        debug.appendTitle("Identity-Adapter Mappings");
        Enumeration e = identityAdapterMap.oids();
        int count = 0;
        while (e.hasMoreElements()) {
            Oid oid = (Oid) e.nextElement();
            NakedObject object = (NakedObject) identityAdapterMap.getAdapter(oid);
            debug.append(count++ + 1, 5);
            debug.append(" ");
            debug.append(oid.toString(), 12);
            debug.append("    ");
            debug.appendln(object.toString());
        }
        debug.appendln();
    }

    public String debugTitle() {
        return "Object Loader";
    }

    public Enumeration getIdentifiedObjects() {
        return pojoAdapterMap.elements();
    }

    public void init() {
        LOG.debug("initialising " + this);
        if (identityAdapterMap == null) {
            identityAdapterMap = new IdentityAdapterHashMap();
        }
        if (pojoAdapterMap == null) {
            pojoAdapterMap = new PojoAdapterHashMap();
        }
        addAdapterClass(String.class, StringAdapter.class);
        addAdapterClass(java.sql.Date.class, DateAdapter.class);
        addAdapterClass(java.sql.Time.class, TimeAdapter.class);
        addAdapterClass(java.util.Date.class, DateTimeAdapter.class);
        addAdapterClass(java.awt.Image.class, AwtImageAdapter.class);
        addAdapterClass(Boolean.class, BooleanAdapter.class);
        addAdapterClass(Character.class, CharAdapter.class);
        addAdapterClass(Byte.class, ByteAdapter.class);
        addAdapterClass(Short.class, ShortAdapter.class);
        addAdapterClass(Integer.class, IntAdapter.class);
        addAdapterClass(Long.class, LongAdapter.class);
        addAdapterClass(Float.class, FloatAdapter.class);
        addAdapterClass(Double.class, DoubleAdapter.class);
        addAdapterClass(boolean.class, PrimitiveBooleanAdapter.class);
        addAdapterClass(char.class, PrimitiveCharAdapter.class);
        addAdapterClass(byte.class, PrimitiveByteAdapter.class);
        addAdapterClass(short.class, PrimitiveShortAdapter.class);
        addAdapterClass(int.class, PrimitiveIntAdapter.class);
        addAdapterClass(long.class, PrimitiveLongAdapter.class);
        addAdapterClass(float.class, PrimitiveFloatAdapter.class);
        addAdapterClass(double.class, PrimitiveDoubleAdapter.class);
        addAdapterClass(BigDecimal.class, BigDecimalAdapter.class);
        addAdapterClass(BigInteger.class, BigIntegerAdapter.class);
    }

    public boolean isIdentityKnown(final Oid oid) {
        Assert.assertNotNull(oid);
        processChangedOid(oid);
        return identityAdapterMap.isIdentityKnown(oid);
    }

    public void start(final NakedReference object, final ResolveState state) {
        LOG.debug("start " + object + " as " + state.name());
        object.changeState(state);
    }

    public void end(final NakedReference object) {
        ResolveState endState = object.getResolveState().getEndState();
        LOG.debug("end " + object + " as " + endState.name());
        object.changeState(endState);
    }

    public void madePersistent(final NakedReference adapter) {
        Oid oid = adapter.getOid();
        identityAdapterMap.remove(oid);
        NakedObjectsContext.getObjectPersistor().convertTransientToPersistentOid(oid);
        adapter.changeState(ResolveState.RESOLVED);
        Assert.assertTrue("Adapter's pojo should exist in pojo map and return the adapter", pojoAdapterMap.getPojo(adapter.getObject()) == adapter);
        Assert.assertNull("Changed OID should not already map to a known adapter " + oid, identityAdapterMap.getAdapter(oid));
        identityAdapterMap.add(oid, adapter);
        LOG.debug("made persistent " + adapter + "; was " + oid.getPrevious());
    }

    public NakedObject recreateAdapterForPersistent(final Oid oid, final NakedObjectSpecification specification) {
        Assert.assertNotNull("must have an OID", oid);
        Assert.assertTrue("must be an object", specification.getType() == NakedObjectSpecification.OBJECT);
        if (isIdentityKnown(oid)) {
            return getAdapterFor(oid);
        }
        LOG.debug("recreating object " + specification.getFullName() + "/" + oid);
        Object object = createObject(specification);
        NakedObject adapter = createObjectAdapter(oid, object);
        adapter.changeState(ResolveState.GHOST);
        return adapter;
    }

    public NakedObject recreateAdapter(final Oid oid, final Object object) {
        Assert.assertNotNull("must have an OID", oid);
        if (isIdentityKnown(oid)) {
            return getAdapterFor(oid);
        }
        NakedObject adapter = createObjectAdapter(oid, object);
        adapter.changeState(oid.isTransient() ? ResolveState.TRANSIENT : ResolveState.GHOST);
        return adapter;
    }

    public void setIdentityAdapterMap(final IdentityAdapterMap identityAdapterMap) {
        this.identityAdapterMap = identityAdapterMap;
    }

    /**
     * Expose as a .Net property.
     * 
     * @property
     */
    public void set_IdentityAdapterMap(final IdentityAdapterMap identityAdapterMap) {
        setIdentityAdapterMap(identityAdapterMap);
    }

    public void setPojoAdapterMap(final PojoAdapterMap pojoAdapterMap) {
        this.pojoAdapterMap = pojoAdapterMap;
    }

    /**
     * Expose as a .Net property.
     * 
     * @property
     */
    public void set_PojoAdapterMap(final PojoAdapterMap pojoAdapterMap) {
        setPojoAdapterMap(pojoAdapterMap);
    }

    public Object[] getServices() {
        return services == null ? new Object[0] : services;
    }

    public void setServices(Object[] services) {
        this.services = services == null ? new Object[0] : services;
        for (int i = 0; i < this.services.length; i++) {
            Object service = services[i];
            initDomainObject(service);
        }
    }

    public void reset() {
        identityAdapterMap.reset();
        pojoAdapterMap.reset();
        InternalCollectionKey.reset();
    }

    public void shutdown() {
        LOG.info("shutting down " + this);
        identityAdapterMap.shutdown();
        identityAdapterMap = null;
        pojoAdapterMap.shutdown();
        InternalCollectionKey.reset();
    }

    public void unloaded(final NakedObject object) {
        LOG.debug("unload ignored: " + object);
        LOG.debug("removed loaded object " + object);
        Oid oid = object.getOid();
        if (oid != null) {
            identityAdapterMap.remove(oid);
        }
        pojoAdapterMap.remove(object);
    }

    /**
     * Given a new Oid (not from the adapter, but usually a reference during distribution) this method
     * extracts the original Oid, find the associated adapter and then updates the lookup so that the new Oid
     * now keys the adapter. The adapter's oid is then updated to take on the new Oid's identity.
     */
    private void processChangedOid(final Oid oid) {
        if (oid.hasPrevious()) {
            Oid previous = oid.getPrevious();
            NakedObject object = (NakedObject) identityAdapterMap.getAdapter(previous);
            if (object != null) {
                LOG.debug("updating oid " + previous + " to " + oid);
                identityAdapterMap.remove(previous);
                Oid oidFromObject = object.getOid();
                oidFromObject.copyFrom(oid);
                identityAdapterMap.add(oidFromObject, object);
            }
        }
    }

    public Naked[] getAdapters(SpecObjectPair[] pairs) {
        Naked[] nakeds = new Naked[pairs.length];
        for (int i = 0; i < pairs.length; i++) {
            nakeds[i] = getAdapter(pairs[i]);
        }
        return nakeds;
    }

    public Naked getAdapter(SpecObjectPair pair) {
        NakedObjectSpecification spec = pair.getSpecification();
        Object object = pair.getObject();
        final int specType = spec.getType();
        if (object instanceof Naked) {
            return (Naked) object;
        }
        if (specType == NakedObjectSpecification.VALUE) {
            if (object == null) {
                return createAdapterForValue(spec);
            }
            return createAdapterForValue(object);
        }
        if (specType == NakedObjectSpecification.OBJECT) {
            if (object == null) {
                return null;
            }
            return getAdapterForElseCreateAdapterForTransient(object);
        }
        if (specType == NakedObjectSpecification.COLLECTION) {
            if (object == null) {
                return null;
            }
            NakedCollectionSpecification collSpec = (NakedCollectionSpecification) spec;
            final NakedObjectSpecification elementSpec = collSpec.getElementType();
            return createAdapterForCollection(object, elementSpec);
        }
        throw new UnknownTypeException("Unable to decode NakedObjectSpecification; pair = " + pair);
    }
}
