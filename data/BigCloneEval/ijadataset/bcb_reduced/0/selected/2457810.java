package net.sf.joafip.store.service.copier;

import java.lang.reflect.Array;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;
import net.sf.joafip.reflect.HelperReflect;
import net.sf.joafip.reflect.ReflectException;
import net.sf.joafip.reflect.ReflectFailedSetException;
import net.sf.joafip.reflect.ReflectInvalidClassException;
import net.sf.joafip.store.entity.classinfo.ClassInfo;
import net.sf.joafip.store.entity.classinfo.FieldInfo;
import net.sf.joafip.store.service.binary.HelperBinaryConversion;
import net.sf.joafip.store.service.classinfo.ClassInfoFactory;
import net.sf.joafip.store.service.proxy.ProxyManager2;

/**
 * create object instance by copy
 * 
 * @author luc peuvrier
 * 
 */
public final class Copier {

    private static final Copier INSTANCE = new Copier();

    private static final HelperReflect helperReflect = HelperReflect.getInstance();

    private static final Object marker = new Object();

    private final Map<Object, Object> objectCopyMap = new IdentityHashMap<Object, Object>();

    private final Deque<Object> objectToCopyQueue = new LinkedList<Object>();

    private final Map<Object, Object> copiedObjectMap = new IdentityHashMap<Object, Object>();

    public static Copier getInstance() {
        return INSTANCE;
    }

    private Copier() {
        super();
    }

    public Object copy(final Object sourceObject, final ClassInfoFactory classInfoFactory, final HelperBinaryConversion helperBinaryConversion) throws CopierException {
        final Object sourceCopy;
        try {
            clear();
            addObjectToCopy(sourceObject);
            Object objectToCopy = objectToCopyQueue.pollFirst();
            while (objectToCopy != null) {
                try {
                    ProxyManager2.forceLoad(objectToCopy);
                } catch (Exception exception) {
                    throw new CopierException(exception);
                }
                addCopied(objectToCopy);
                createCopy(objectToCopy, classInfoFactory, helperBinaryConversion);
                objectToCopy = objectToCopyQueue.pollFirst();
            }
            sourceCopy = getObjectCopy(sourceObject, classInfoFactory);
        } finally {
            clear();
        }
        return sourceCopy;
    }

    private void createCopy(final Object sourceObject, final ClassInfoFactory classInfoFactory, final HelperBinaryConversion helperBinaryConversion) throws CopierException {
        final Object objectCopy = getObjectCopy(sourceObject, classInfoFactory);
        if (sourceObject.getClass().isArray()) {
            createArrayCopy(sourceObject, objectCopy, classInfoFactory, helperBinaryConversion);
        } else {
            createNotArrayCopy(sourceObject, objectCopy, classInfoFactory, helperBinaryConversion);
        }
    }

    private void createNotArrayCopy(final Object sourceObject, final Object objectCopy, final ClassInfoFactory classInfoFactory, final HelperBinaryConversion helperBinaryConversion) throws CopierException {
        try {
            final ClassInfo classForField = ProxyManager2.classOfObject(sourceObject, classInfoFactory);
            final FieldInfo[] fieldsInfo = classForField.allDeclaredFields(true);
            for (FieldInfo fieldInfo : fieldsInfo) {
                final Object fieldValue = helperReflect.getFieldValue(sourceObject, fieldInfo);
                final Object fieldCopyValue;
                if (fieldValue == null) {
                    fieldCopyValue = fieldValue;
                } else {
                    final Class<?> fieldValueClass = fieldValue.getClass();
                    if (fieldValueClass.equals(String.class)) {
                        fieldCopyValue = fieldValue;
                    } else if (haveConverter(fieldValueClass, classInfoFactory, helperBinaryConversion)) {
                        fieldCopyValue = fieldValue;
                    } else {
                        addObjectToCopy(fieldValue);
                        fieldCopyValue = getObjectCopy(fieldValue, classInfoFactory);
                    }
                }
                helperReflect.setFieldValue(objectCopy, fieldInfo, fieldCopyValue);
            }
        } catch (ReflectException exception) {
            throw new CopierException(exception);
        } catch (ReflectFailedSetException exception) {
            throw new CopierException(exception);
        }
    }

    private boolean haveConverter(final Class clazz, final ClassInfoFactory classInfoFactory, final HelperBinaryConversion helperBinaryConversion) {
        final ClassInfo classInfo = classInfoFactory.getClassInfo(clazz);
        return helperBinaryConversion.haveConverter(classInfo);
    }

    private void createArrayCopy(final Object sourceArray, final Object arrayCopy, final ClassInfoFactory classInfoFactory, final HelperBinaryConversion helperBinaryConversion) throws CopierException {
        try {
            final int arrayLength = Array.getLength(sourceArray);
            final Class<?> componentType = sourceArray.getClass().getComponentType();
            if (componentType.equals(StringBuilder.class) || haveConverter(componentType, classInfoFactory, helperBinaryConversion)) {
                for (int index = 0; index < arrayLength; index++) {
                    final Object elementObject = helperReflect.getArrayElement(sourceArray, index);
                    helperReflect.setArrayElement(arrayCopy, index, elementObject);
                }
            } else {
                for (int index = 0; index < arrayLength; index++) {
                    final Object elementObject = helperReflect.getArrayElement(sourceArray, index);
                    addObjectToCopy(elementObject);
                    final Object elemetCopy = getObjectCopy(elementObject, classInfoFactory);
                    helperReflect.setArrayElement(arrayCopy, index, elemetCopy);
                }
            }
        } catch (ReflectException exception) {
            throw new CopierException(exception);
        } catch (ReflectFailedSetException exception) {
            throw new CopierException(exception);
        }
    }

    private void addObjectToCopy(final Object object) {
        if (object != null && !copiedObjectMap.containsKey(object)) {
            objectToCopyQueue.add(object);
        }
    }

    private void addCopied(final Object object) {
        copiedObjectMap.put(object, marker);
    }

    private void clear() {
        objectCopyMap.clear();
        objectToCopyQueue.clear();
        copiedObjectMap.clear();
    }

    private Object getObjectCopy(final Object sourceObject, final ClassInfoFactory classInfoFactory) throws CopierException {
        Object objectCopy;
        if (sourceObject == null) {
            objectCopy = null;
        } else {
            objectCopy = objectCopyMap.get(sourceObject);
            if (objectCopy == null) {
                if (sourceObject.getClass().isArray()) {
                    final Class<?> arrayComponentType = sourceObject.getClass().getComponentType();
                    final int arrayLength = Array.getLength(sourceObject);
                    objectCopy = Array.newInstance(arrayComponentType, arrayLength);
                } else {
                    final ClassInfo objectClassInfo = ProxyManager2.classOfObject(sourceObject, classInfoFactory);
                    objectCopy = newInstanceNoConstruction(objectClassInfo.getObjectClass());
                }
                objectCopyMap.put(sourceObject, objectCopy);
            }
        }
        return objectCopy;
    }

    /**
	 * create a new instance without calling constructor
	 * 
	 * @param objectClass
	 * @return
	 * @throws CopierException
	 */
    private Object newInstanceNoConstruction(final Class objectClass) throws CopierException {
        try {
            return helperReflect.newInstanceNoConstruction(objectClass);
        } catch (ReflectException exception) {
            throw new CopierException(exception);
        } catch (ReflectInvalidClassException exception) {
            throw new CopierException(exception);
        }
    }
}
