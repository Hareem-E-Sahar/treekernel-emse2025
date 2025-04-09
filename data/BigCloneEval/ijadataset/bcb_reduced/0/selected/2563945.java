package org.neodatis.odb.core.layers.layer1;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.neodatis.odb.ODBRuntimeException;
import org.neodatis.odb.OID;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.OdbConfiguration;
import org.neodatis.odb.core.CoreProvider;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.layers.layer2.meta.AbstractObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.ArrayObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.AtomicNativeObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.AttributeIdentification;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfo;
import org.neodatis.odb.core.layers.layer2.meta.CollectionObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.EnumNativeObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.MapObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeNullObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.NullNativeObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.ODBType;
import org.neodatis.odb.core.layers.layer2.meta.ObjectInfoHeader;
import org.neodatis.odb.core.layers.layer2.meta.ObjectReference;
import org.neodatis.odb.core.layers.layer4.OidProvider;
import org.neodatis.odb.core.oid.OIDTypes;
import org.neodatis.odb.core.session.Cache;
import org.neodatis.odb.core.session.Session;
import org.neodatis.tool.wrappers.OdbReflection;
import org.neodatis.tool.wrappers.list.IOdbList;
import org.neodatis.tool.wrappers.map.OdbHashMap;

/**
 * The local implementation of the Object Introspector.
 * 
 * @author osmadja
 * 
 */
public class ObjectIntrospectorImpl implements ObjectIntrospector {

    protected ClassIntrospector classIntrospector;

    protected Session session;

    protected OidProvider oidProvider;

    public ObjectIntrospectorImpl(Session session, ClassIntrospector classIntrospector, OidProvider oidProvider) {
        this.session = session;
        this.classIntrospector = classIntrospector;
        this.oidProvider = oidProvider;
    }

    public NonNativeObjectInfo getMetaRepresentation(Object object, IntrospectionCallback callback) {
        return (NonNativeObjectInfo) getObjectInfoInternal(object, new HashMap<Object, NonNativeObjectInfo>(), callback);
    }

    /**
	 * Build a meta representation of an object
	 * 
	 * <pre>
	 * warning: When an object has two fields with the same name (a private field with the same name in a parent class, the deeper field (of the parent) is ignored!)
	 * </pre>
	 * 
	 * @param nnoi
	 *            The NonNativeObjectInfo to fill
	 * @param object
	 * @param ci
	 * @param recursive
	 * @return The ObjectInfo
	 */
    protected AbstractObjectInfo getObjectInfoInternal(Object object, Map<Object, NonNativeObjectInfo> alreadyReadObjects, IntrospectionCallback callback) {
        Object value = null;
        if (object == null) {
            return NullNativeObjectInfo.getInstance();
        }
        Class clazz = object.getClass();
        ODBType type = ODBType.getFromClass(clazz);
        String className = clazz.getName();
        if (type.isNative()) {
            return getNativeObjectInfoInternal(type, object, alreadyReadObjects, callback);
        }
        ClassInfo ci = getClassInfo(className);
        NonNativeObjectInfo mainAoi = buildNnoi(object, ci, null, null);
        boolean isRootObject = false;
        if (alreadyReadObjects == null) {
            alreadyReadObjects = new OdbHashMap<Object, NonNativeObjectInfo>();
            isRootObject = true;
        }
        if (object != null) {
            NonNativeObjectInfo cachedNnoi = alreadyReadObjects.get(object);
            if (cachedNnoi != null) {
                ObjectReference or = new ObjectReference(cachedNnoi);
                return or;
            }
            objectFound(object, mainAoi.getOid(), callback);
        }
        alreadyReadObjects.put(object, mainAoi);
        IOdbList<Field> fields = classIntrospector.getAllFields(className);
        AbstractObjectInfo aoi = null;
        int attributeId = -1;
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            try {
                value = field.get(object);
                attributeId = ci.getAttributeId(field.getName());
                if (attributeId == -1) {
                    throw new ODBRuntimeException(NeoDatisError.OBJECT_INTROSPECTOR_NO_FIELD_WITH_NAME.addParameter(ci.getFullClassName()).addParameter(field.getName()));
                }
                ODBType valueType = null;
                if (value == null) {
                    valueType = ODBType.getFromClass(field.getType());
                } else {
                    valueType = ODBType.getFromClass(value.getClass());
                }
                if (valueType.isNative()) {
                    aoi = getNativeObjectInfoInternal(valueType, value, alreadyReadObjects, callback);
                    mainAoi.setAttributeValue(attributeId, aoi);
                } else {
                    ClassInfo clai = getClassInfo(valueType.getName());
                    if (value == null) {
                        aoi = new NonNativeNullObjectInfo();
                        mainAoi.setAttributeValue(attributeId, aoi);
                    } else {
                        aoi = getObjectInfoInternal(value, alreadyReadObjects, callback);
                        mainAoi.setAttributeValue(attributeId, aoi);
                    }
                }
            } catch (IllegalArgumentException e) {
                throw new ODBRuntimeException(NeoDatisError.INTERNAL_ERROR.addParameter("in getObjectInfoInternal"), e);
            } catch (IllegalAccessException e) {
                throw new ODBRuntimeException(NeoDatisError.INTERNAL_ERROR.addParameter("getObjectInfoInternal"), e);
            }
        }
        if (isRootObject) {
            alreadyReadObjects.clear();
            alreadyReadObjects = null;
        }
        return mainAoi;
    }

    protected void objectFound(Object object, ObjectOid objectOid, IntrospectionCallback callback) {
        if (callback != null) {
            callback.objectFound(object, objectOid);
        }
    }

    public NonNativeObjectInfo buildNnoi(Object object, ClassInfo classInfo, AbstractObjectInfo[] values, AttributeIdentification[] attributesIdentification) {
        NonNativeObjectInfo nnoi = new NonNativeObjectInfo(object, classInfo, values, attributesIdentification);
        if (session != null) {
            Cache cache = session.getCache();
            ObjectOid oid = (ObjectOid) cache.getOid(object, false);
            if (oid != null) {
                nnoi.setOid(oid);
                ObjectInfoHeader oih = null;
                if (oih != null) {
                    nnoi.getHeader().setObjectVersion(oih.getObjectVersion());
                    nnoi.getHeader().setUpdateDate(oih.getUpdateDate());
                    nnoi.getHeader().setCreationDate(oih.getCreationDate());
                }
            } else {
                oid = getNextObjectOid(classInfo);
                oid.setIsNew();
                nnoi.setOid(oid);
                cache.addObject(oid, object);
            }
        }
        return nnoi;
    }

    /**
	 * @param classInfo
	 * @return
	 */
    private ObjectOid getNextObjectOid(ClassInfo classInfo) {
        if (oidProvider == null) {
            oidProvider = session.getEngine().getLayer4().getOidProvider();
        }
        return oidProvider.getNextObjectOid(classInfo.getOid());
    }

    /**
	 * 
	 * @param type
	 *            The odb type of the object
	 * @param object
	 *            The object to be introspected
	 * @param alreadyReadObjects
	 *            All the objects that has already been read
	 * @param callback
	 * @return
	 */
    protected AbstractObjectInfo getNativeObjectInfoInternal(ODBType type, Object object, Map<Object, NonNativeObjectInfo> alreadyReadObjects, IntrospectionCallback callback) {
        AbstractObjectInfo aoi = null;
        if (type.isAtomicNative()) {
            if (object == null) {
                return new NullNativeObjectInfo(type.getId());
            }
            return new AtomicNativeObjectInfo(object, type.getId());
        }
        if (type.isCollection()) {
            return introspectCollection((Collection) object, alreadyReadObjects, type, callback);
        }
        if (type.isArray()) {
            if (object == null) {
                return new ArrayObjectInfo(null);
            }
            String realArrayClassName = object.getClass().getComponentType().getName();
            ArrayObjectInfo aroi = null;
            aroi = introspectArray(object, alreadyReadObjects, type, callback);
            aroi.setRealArrayComponentClassName(realArrayClassName);
            return aroi;
        }
        if (type.isMap()) {
            if (object == null) {
                return new MapObjectInfo(null, type, type.getDefaultInstanciationClass().getName());
            }
            MapObjectInfo moi = introspectMap((Map) object, alreadyReadObjects, callback);
            if (moi.getRealMapClassName().indexOf("$") != -1) {
                moi.setRealMapClassName(type.getDefaultInstanciationClass().getName());
            }
            return moi;
        }
        if (type.isEnum()) {
            Enum enumObject = (Enum) object;
            if (enumObject == null) {
                return new NullNativeObjectInfo(type.getSize());
            }
            String enumClassName = enumObject == null ? null : enumObject.getClass().getName();
            ClassInfo ci = getClassInfo(enumClassName);
            String enumValue = enumObject == null ? null : enumObject.name();
            return new EnumNativeObjectInfo(ci, enumValue);
        }
        throw new ODBRuntimeException(NeoDatisError.INTERNAL_ERROR.addParameter(String.format("Unsupported type %s", type.getId())));
    }

    private CollectionObjectInfo introspectCollection(Collection collection, Map<Object, NonNativeObjectInfo> alreadyReadObjects, ODBType type, IntrospectionCallback callback) {
        if (collection == null) {
            return new CollectionObjectInfo();
        }
        Collection<AbstractObjectInfo> collectionCopy = new ArrayList<AbstractObjectInfo>(collection.size());
        Collection<NonNativeObjectInfo> nonNativesObjects = new ArrayList<NonNativeObjectInfo>(collection.size());
        AbstractObjectInfo aoi = null;
        Iterator iterator = collection.iterator();
        while (iterator.hasNext()) {
            Object o = iterator.next();
            ClassInfo ci = null;
            if (o != null) {
                aoi = getObjectInfoInternal(o, alreadyReadObjects, callback);
                collectionCopy.add(aoi);
                if (aoi.isNonNativeObject()) {
                    nonNativesObjects.add((NonNativeObjectInfo) aoi);
                }
            }
        }
        CollectionObjectInfo coi = new CollectionObjectInfo(collectionCopy, nonNativesObjects);
        String realCollectionClassName = collection.getClass().getName();
        if (realCollectionClassName.indexOf("$") != -1) {
            coi.setRealCollectionClassName(type.getDefaultInstanciationClass().getName());
        } else {
            coi.setRealCollectionClassName(realCollectionClassName);
        }
        return coi;
    }

    /**
	 * Introspect a map
	 * 
	 * @param map
	 * @param alreadyReadObjects
	 * @param callback
	 * @return
	 */
    private MapObjectInfo introspectMap(Map map, Map<Object, NonNativeObjectInfo> alreadyReadObjects, IntrospectionCallback callback) {
        Map<AbstractObjectInfo, AbstractObjectInfo> mapCopy = new OdbHashMap<AbstractObjectInfo, AbstractObjectInfo>();
        Collection<NonNativeObjectInfo> nonNativeObjects = new ArrayList<NonNativeObjectInfo>(map.size() * 2);
        Collection keySet = map.keySet();
        Iterator keys = keySet.iterator();
        ClassInfo ciKey = null;
        ClassInfo ciValue = null;
        AbstractObjectInfo aoiForKey = null;
        AbstractObjectInfo aoiForValue = null;
        while (keys.hasNext()) {
            Object key = keys.next();
            Object value = map.get(key);
            if (key != null) {
                ciKey = getClassInfo(key.getClass().getName());
                if (value != null) {
                    ciValue = getClassInfo(value.getClass().getName());
                }
                aoiForKey = getObjectInfoInternal(key, alreadyReadObjects, callback);
                aoiForValue = getObjectInfoInternal(value, alreadyReadObjects, callback);
                mapCopy.put(aoiForKey, aoiForValue);
                if (aoiForKey.isNonNativeObject()) {
                    nonNativeObjects.add((NonNativeObjectInfo) aoiForKey);
                }
                if (aoiForValue.isNonNativeObject()) {
                    nonNativeObjects.add((NonNativeObjectInfo) aoiForValue);
                }
            }
        }
        MapObjectInfo mapObjectInfo = new MapObjectInfo(mapCopy, map.getClass().getName());
        mapObjectInfo.setNonNativeObjects(nonNativeObjects);
        return mapObjectInfo;
    }

    private ClassInfo getClassInfo(String fullClassName) {
        return session.getClassInfo(fullClassName);
    }

    private ArrayObjectInfo introspectArray(Object array, Map<Object, NonNativeObjectInfo> alreadyReadObjects, ODBType valueType, IntrospectionCallback callback) {
        int length = OdbReflection.getArrayLength(array);
        Class elementType = array.getClass().getComponentType();
        ODBType type = ODBType.getFromClass(elementType);
        if (type.isAtomicNative()) {
            return intropectAtomicNativeArray(array, type);
        }
        AbstractObjectInfo[] arrayCopy = new AbstractObjectInfo[length];
        Collection<NonNativeObjectInfo> nonNativeObjects = new ArrayList<NonNativeObjectInfo>(length);
        for (int i = 0; i < length; i++) {
            Object o = OdbReflection.getArrayElement(array, i);
            ClassInfo ci = null;
            if (o != null) {
                AbstractObjectInfo aoi = getObjectInfoInternal(o, alreadyReadObjects, callback);
                arrayCopy[i] = aoi;
                if (aoi.isNonNativeObject()) {
                    nonNativeObjects.add((NonNativeObjectInfo) aoi);
                }
            } else {
                arrayCopy[i] = new NonNativeNullObjectInfo();
                nonNativeObjects.add((NonNativeObjectInfo) arrayCopy[i]);
            }
        }
        ArrayObjectInfo arrayOfAoi = new ArrayObjectInfo(arrayCopy, valueType, type.getId());
        arrayOfAoi.setNonNativeObjects(nonNativeObjects);
        return arrayOfAoi;
    }

    private ArrayObjectInfo intropectAtomicNativeArray(Object array, ODBType type) {
        int length = OdbReflection.getArrayLength(array);
        AtomicNativeObjectInfo anoi = null;
        AbstractObjectInfo[] arrayCopy = new AbstractObjectInfo[length];
        int typeId = 0;
        for (int i = 0; i < length; i++) {
            Object o = OdbReflection.getArrayElement(array, i);
            if (o != null) {
                typeId = ODBType.getFromClass(o.getClass()).getId();
                anoi = new AtomicNativeObjectInfo(o, typeId);
                arrayCopy[i] = anoi;
            } else {
                arrayCopy[i] = new NullNativeObjectInfo(type.getId());
            }
        }
        ArrayObjectInfo aoi = new ArrayObjectInfo(arrayCopy, ODBType.ARRAY, type.getId());
        return aoi;
    }

    public void clear() {
    }

    public ClassIntrospector getClassIntrospector() {
        return classIntrospector;
    }
}
