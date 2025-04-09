package net.sf.joafip.store.service.copier;

import java.lang.reflect.Array;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;
import net.sf.joafip.NotStorableClass;
import net.sf.joafip.logger.JoafipLogger;
import net.sf.joafip.ograph.GraphException;
import net.sf.joafip.ograph.ObjectGraph;
import net.sf.joafip.ograph.ObjectGraphCreator;
import net.sf.joafip.reflect.HelperReflect;
import net.sf.joafip.reflect.ReflectException;
import net.sf.joafip.reflect.ReflectFailedSetException;
import net.sf.joafip.store.entity.classinfo.ClassInfo;
import net.sf.joafip.store.entity.classinfo.FieldInfo;
import net.sf.joafip.store.service.classinfo.ClassInfoException;
import net.sf.joafip.store.service.classinfo.ClassInfoFactory;
import net.sf.joafip.store.service.objectio.ObjectIOClassNotFoundException;
import net.sf.joafip.store.service.objectio.ObjectIODataCorruptedException;
import net.sf.joafip.store.service.objectio.ObjectIODataRecordNotFoundException;
import net.sf.joafip.store.service.objectio.ObjectIOException;
import net.sf.joafip.store.service.objectio.ObjectIOInvalidClassException;
import net.sf.joafip.store.service.objectio.ObjectIONotSerializableException;
import net.sf.joafip.store.service.proxy.IProxyManagerForObjectIO;
import net.sf.joafip.store.service.proxy.ProxyException;
import net.sf.joafip.store.service.proxy.ProxyManager2;

/**
 * 
 * @author luc peuvrier
 */
@NotStorableClass
public abstract class AbstractDeepCopy {

    protected final JoafipLogger logger = JoafipLogger.getLogger(getClass());

    private static final String ELEMENT = "element ";

    private static final String NULL = "null";

    private static final String OF = ", of ";

    protected static final HelperReflect HELPER_REFLECT = HelperReflect.getInstance();

    /** constant value of map entry to use map as a set */
    private static final Object MARKER = new Object();

    /** map of object copy by source object */
    protected final Map<Object, Object> objectCopyMap = new IdentityHashMap<Object, Object>();

    /** object to copy, not already copied but instance created */
    private final Deque<Object> objectToCopyQueue = new LinkedList<Object>();

    /** set ( implemented using map ) of already copied object */
    private final Map<Object, Object> copiedObjectSet = new IdentityHashMap<Object, Object>();

    protected transient Object rootSourceObject;

    protected transient ClassInfoFactory classInfoFactory;

    protected transient IDeepCopyServiceDelegate deepCopyServiceDelagate;

    protected AbstractDeepCopy() {
        super();
    }

    /**
	 * 
	 * @param rootSourceObject
	 *            the source object
	 * @param deepCopyServiceDelagate
	 * @param forceLoad
	 *            true if force proxy load
	 * @return deep copy of the source object
	 * @throws CopierException
	 */
    protected Object deepCopy(final Object rootSourceObject, final IDeepCopyServiceDelegate deepCopyServiceDelagate, final boolean forceLoad) throws CopierException {
        this.rootSourceObject = rootSourceObject;
        this.classInfoFactory = deepCopyServiceDelagate.getClassInfoFactory();
        this.deepCopyServiceDelagate = deepCopyServiceDelagate;
        final IProxyManagerForObjectIO proxyManager2 = deepCopyServiceDelagate.getProxyManager2();
        final Object sourceCopy;
        try {
            clear();
            addObjectToCopyQueue(rootSourceObject);
            Object objectToCopy = objectToCopyQueue.pollFirst();
            while (objectToCopy != null) {
                if (forceLoad) {
                    forceLoadImpl(objectToCopy);
                }
                addCopied(objectToCopy);
                final ClassInfo objectToCopyClassInfo = proxyManager2.classInfoOfObject(objectToCopy);
                createCopy(objectToCopy, objectToCopyClassInfo);
                objectToCopy = objectToCopyQueue.pollFirst();
            }
            final ClassInfo sourceObjectClassInfo = proxyManager2.classInfoOfObject(rootSourceObject);
            sourceCopy = getOrCreateObjectCopy(rootSourceObject, sourceObjectClassInfo);
            deepCopyDone();
        } catch (ProxyException exception) {
            throw new CopierException(exception);
        } catch (ObjectIOException exception) {
            throw new CopierException(exception);
        } catch (ObjectIODataRecordNotFoundException exception) {
            throw new CopierException(exception);
        } catch (ObjectIOInvalidClassException exception) {
            throw new CopierException(exception);
        } catch (ObjectIOClassNotFoundException exception) {
            throw new CopierException(exception);
        } catch (ObjectIODataCorruptedException exception) {
            throw new CopierException(exception);
        } catch (ObjectIONotSerializableException exception) {
            throw new CopierException(exception);
        } finally {
            clear();
        }
        return sourceCopy;
    }

    protected abstract void deepCopyDone() throws CopierException;

    protected abstract void forceLoadImpl(Object objectToCopy) throws ObjectIOException, ObjectIODataRecordNotFoundException, ObjectIOInvalidClassException, ObjectIOClassNotFoundException, ObjectIODataCorruptedException, ObjectIONotSerializableException, ProxyException;

    private void clear() {
        objectCopyMap.clear();
        objectToCopyQueue.clear();
        copiedObjectSet.clear();
    }

    private void addObjectToCopyQueue(final Object objectToCopy) {
        if (objectToCopy != null && !copiedObjectSet.containsKey(objectToCopy)) {
            objectToCopyQueue.add(objectToCopy);
        }
    }

    private void createCopy(final Object sourceObject, final ClassInfo sourceObjectClassInfo) throws CopierException {
        final Object objectCopy = getOrCreateObjectCopy(sourceObject, sourceObjectClassInfo);
        assert objectCopy != null : "object copy can not be null";
        if (sourceObject != objectCopy) {
            if (sourceObjectClassInfo.isArrayType()) {
                arrayCopy(sourceObject, objectCopy);
            } else {
                notArrayCopy(sourceObject, sourceObjectClassInfo, objectCopy);
            }
        }
        if (logger.debugEnabled) {
            logger.debug("source " + sourceObject.getClass().getName() + "@" + System.identityHashCode(sourceObject) + " copied to " + objectCopy.getClass().getName() + "@" + System.identityHashCode(objectCopy));
        }
    }

    private void notArrayCopy(final Object sourceObject, final ClassInfo sourceObjectClassInfo, final Object objectCopy) throws CopierException {
        assert sourceObject != null : "can not copy from null";
        assert sourceObjectClassInfo != null && sourceObjectClassInfo != ClassInfo.NULL : "can not copy: null class information";
        FieldInfo[] fieldsInfo;
        try {
            fieldsInfo = sourceObjectClassInfo.allDeclaredFieldsWithTransientWithoutStatic();
        } catch (ClassInfoException exception) {
            throw new CopierException(exception);
        }
        assert assertHasFieldInfo(fieldsInfo, sourceObjectClassInfo);
        for (FieldInfo fieldInfo : fieldsInfo) {
            Object fieldValue;
            try {
                fieldValue = HELPER_REFLECT.getFieldValue(sourceObject, fieldInfo, true);
            } catch (ReflectException exception) {
                throw new CopierException("for field " + fieldInfo.toString() + OF + identityString(sourceObject), exception);
            }
            final Object fieldCopyValue;
            if (fieldValue == null || !fieldInfo.isPersisted()) {
                fieldCopyValue = fieldValue;
            } else {
                final Class<?> fieldValueClass = fieldValue.getClass();
                ClassInfo fieldValueClassInfo;
                try {
                    fieldValueClassInfo = classInfoFactory.getNoProxyClassInfo(fieldValueClass);
                } catch (ClassInfoException exception) {
                    throw new CopierException(exception);
                }
                if (fieldValueClass.equals(String.class)) {
                    fieldCopyValue = fieldValue;
                } else if (fieldValueClassInfo.isBasicType()) {
                    fieldCopyValue = fieldValue;
                } else {
                    addObjectToCopyQueue(fieldValue);
                    try {
                        fieldCopyValue = getOrCreateObjectCopy(fieldValue, fieldValueClassInfo);
                    } catch (CopierException exception) {
                        throw new CopierException("field " + fieldValue.getClass() + ", field info " + fieldInfo.toString() + OF + sourceObjectClassInfo, exception);
                    }
                }
            }
            try {
                HELPER_REFLECT.setFieldValue(objectCopy, fieldInfo, fieldCopyValue);
            } catch (ReflectException exception) {
                throw new CopierException("for field " + (fieldValue == null ? NULL : fieldValue.getClass().toString()) + ", field info " + fieldInfo.toString() + OF + identityString(objectCopy), exception);
            } catch (ReflectFailedSetException exception) {
                throw new CopierException("for field " + (fieldValue == null ? NULL : fieldValue.getClass().toString()) + ", field info " + fieldInfo.toString() + OF + identityString(objectCopy), exception);
            }
        }
    }

    private boolean assertHasFieldInfo(final FieldInfo[] fieldsInfo, final ClassInfo sourceObjectClassInfo) {
        if (fieldsInfo == null) {
            throw new AssertionError("no fields information for " + sourceObjectClassInfo.toString());
        }
        return true;
    }

    private void arrayCopy(final Object sourceArray, final Object arrayCopy) throws CopierException {
        final int arrayLength = Array.getLength(sourceArray);
        final Class<?> componentType = sourceArray.getClass().getComponentType();
        ClassInfo componentTypeClassInfo;
        try {
            componentTypeClassInfo = classInfoFactory.getNoProxyClassInfo(componentType);
        } catch (ClassInfoException exception) {
            throw new CopierException(exception);
        }
        if (componentTypeClassInfo.isStringType() || componentTypeClassInfo.isBasicType()) {
            for (int index = 0; index < arrayLength; index++) {
                final Object elementObject;
                try {
                    elementObject = HELPER_REFLECT.getArrayElement(sourceArray, index);
                } catch (ReflectException exception) {
                    throw new CopierException("element of " + identityString(sourceArray), exception);
                }
                try {
                    HELPER_REFLECT.setArrayElement(arrayCopy, index, elementObject);
                } catch (ReflectException exception) {
                    throw new CopierException(ELEMENT + elementObject.getClass() + OF + identityString(sourceArray), exception);
                } catch (ReflectFailedSetException exception) {
                    throw new CopierException(ELEMENT + elementObject.getClass() + OF + identityString(sourceArray), exception);
                }
            }
        } else {
            for (int index = 0; index < arrayLength; index++) {
                Object elementObject;
                try {
                    elementObject = HELPER_REFLECT.getArrayElement(sourceArray, index);
                } catch (ReflectException exception) {
                    throw new CopierException("element of " + sourceArray, exception);
                }
                addObjectToCopyQueue(elementObject);
                final Object elemetCopy;
                if (elementObject == null) {
                    elemetCopy = elementObject;
                } else {
                    ClassInfo elementObjectClassInfo;
                    try {
                        elementObjectClassInfo = classInfoFactory.getNoProxyClassInfo(elementObject.getClass());
                    } catch (ClassInfoException exception) {
                        throw new CopierException(exception);
                    }
                    if (elementObjectClassInfo.isStringType() || elementObjectClassInfo.isBasicType()) {
                        elemetCopy = elementObject;
                    } else {
                        try {
                            elemetCopy = getOrCreateObjectCopy(elementObject, elementObjectClassInfo);
                        } catch (CopierException exception) {
                            throw new CopierException(ELEMENT + elementObject.getClass() + OF + sourceArray, exception);
                        }
                    }
                }
                try {
                    HELPER_REFLECT.setArrayElement(arrayCopy, index, elemetCopy);
                } catch (ReflectException exception) {
                    throw new CopierException(ELEMENT + (elementObject == null ? NULL : elementObject.getClass().toString()) + OF + sourceArray, exception);
                } catch (ReflectFailedSetException exception) {
                    throw new CopierException(ELEMENT + (elementObject == null ? NULL : elementObject.getClass().toString()) + OF + sourceArray, exception);
                }
            }
        }
    }

    private void addCopied(final Object objectToCopy) {
        copiedObjectSet.put(objectToCopy, MARKER);
    }

    private Object getOrCreateObjectCopy(final Object sourceObject, final ClassInfo sourceObjectClassInfo) throws CopierException {
        Object objectCopy;
        if (sourceObject == null) {
            objectCopy = null;
        } else {
            objectCopy = objectCopyMap.get(sourceObject);
            if (objectCopy == null) {
                if (mustBeCopied(sourceObject, sourceObjectClassInfo)) {
                    forceLoad(sourceObject);
                    if (sourceObject.getClass().isArray()) {
                        final Class<?> arrayComponentType = sourceObject.getClass().getComponentType();
                        final int arrayLength = Array.getLength(sourceObject);
                        objectCopy = Array.newInstance(arrayComponentType, arrayLength);
                    } else {
                        try {
                            objectCopy = newInstanceForObjectCopy(sourceObject, sourceObjectClassInfo);
                        } catch (CopierException exception) {
                            logger.fatal(pathFromRootToObject(sourceObject), exception);
                            throw exception;
                        }
                    }
                } else {
                    objectCopy = sourceObject;
                }
                objectCopyMap.put(sourceObject, objectCopy);
            }
        }
        return objectCopy;
    }

    private void forceLoad(final Object sourceObject) throws CopierException {
        try {
            ProxyManager2.forceLoad(sourceObject);
        } catch (ProxyException exception) {
            throw new CopierException(exception);
        }
    }

    protected abstract boolean mustBeCopied(Object objectToCopy, ClassInfo classInfo) throws CopierException;

    /**
	 * create a new instance copy of source object<br>
	 * the new instance must be done without calling constructor since state
	 * will be set by field value copy<br>
	 * 
	 * @param sourceObject
	 *            the source object
	 * @param sourceObjectClassInfo
	 *            the class information of the object to create
	 * @return the new instance or the source object
	 * @throws CopierException
	 */
    protected abstract Object newInstanceForObjectCopy(Object sourceObject, ClassInfo sourceObjectClassInfo) throws CopierException;

    private String identityString(final Object object) {
        return object.getClass().getName() + "@" + System.identityHashCode(object);
    }

    private String pathFromRootToObject(final Object sourceObject) {
        final ObjectGraphCreator objectGraphCreator = ObjectGraphCreator.getInstance();
        String pathFromRootToObject;
        try {
            final ObjectGraph objectGraph = objectGraphCreator.create(rootSourceObject);
            pathFromRootToObject = objectGraphCreator.pathToObject(objectGraph, sourceObject);
        } catch (GraphException exception) {
            pathFromRootToObject = "failed create object graph " + exception.getMessage();
        }
        return pathFromRootToObject;
    }
}
