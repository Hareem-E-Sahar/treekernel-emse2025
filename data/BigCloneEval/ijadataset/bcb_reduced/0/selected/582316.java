package net.sourceforge.pojosync;

import java.io.IOException;
import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import net.sourceforge.pojosync.client.Objects;
import net.sourceforge.pojosync.client.Synchronizer;
import net.sourceforge.pojosync.client.Objects.ArrayOfReferences;
import net.sourceforge.pojosync.client.Objects.Reference;
import net.sourceforge.pojosync.internal.Access;
import net.sourceforge.pojosync.internal.Classes;
import net.sourceforge.pojosync.internal.IAccessCollection;
import org.apache.log4j.Logger;

public class Delta implements IDelta {

    private static Logger logger = Logger.getLogger(Delta.class);

    static Object NULL_VALUE = new Object() {

        @Override
        public String toString() {
            return "NULL_VALUE";
        }
    };

    private int id;

    private Class<?> iface;

    private boolean virgin;

    private static int nextId = -1;

    private Map map;

    public interface Serializer {

        void add(String key, Object value) throws IOException;

        void addReference(String key, String interfaceName, int id) throws IOException;

        void addReferenceArray(String key, String interfaceName, int[] idArr) throws IOException;

        void set(int id, String interfaceName, boolean virgin) throws IOException;
    }

    private Delta(Class<?> iface, int id, boolean virgin) {
        this.iface = iface;
        this.id = id;
        this.virgin = virgin;
        map = new Map();
    }

    public Delta(Class<?> iface) {
        this(iface, nextId--, true);
    }

    private Delta(Delta delta) {
        this(delta.getInterface(), delta.getId(), delta.isVirgin());
        this.map = new Map(delta.map);
    }

    @Override
    public String[] getKeys() {
        return map.getKeys();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        virgin = false;
    }

    public boolean isVirgin() {
        return virgin;
    }

    public Class<?> getInterface() {
        return iface;
    }

    public boolean isSet(String key) {
        return map.containsKey(key);
    }

    private boolean isSupportedClass(Class<?> clazz) {
        if ("java.lang".equals(clazz.getPackage().getName())) {
            return true;
        }
        if (clazz == Timestamp.class) {
            return true;
        }
        return false;
    }

    public void set(IAccessCollection objects, String key, Object object, Object originalValue) {
        if (object == originalValue) {
            return;
        }
        if (object != null && object.equals(originalValue)) {
            return;
        }
        set(objects, key, object);
    }

    private void set(IAccessCollection objects, String key, Object object) {
        if (object == null) {
            map.put(key, NULL_VALUE);
            return;
        }
        if (object.getClass().isArray()) {
            Class<?> componentClass = object.getClass().getComponentType();
            if (componentClass.isPrimitive()) {
                map.put(key, copyArray(object));
                return;
            } else {
                Object[] objArr = (Object[]) object;
                Class<?> interfaze = Classes.getInstance().getSynchronizableInterface(componentClass);
                ArrayOfReferences references = new ArrayOfReferences(interfaze, objArr.length);
                for (int i = 0; i < objArr.length; i++) {
                    Access access = (Access) objArr[i];
                    Object delegate = access.getDelegate();
                    int identifier = Classes.getInstance().getObjectIdentifier().getId(delegate);
                    references.set(i, identifier);
                    if (objects != null && !objects.contains(access)) {
                        objects.add(access);
                    }
                }
                map.put(key, references);
            }
        } else {
            if (isSupportedClass(object.getClass())) {
                map.put(key, object);
                return;
            }
            if (object instanceof ArrayOfReferences) {
                map.put(key, object);
                return;
            }
            Class<?> interfaze = Classes.getInstance().getSynchronizableInterface(object.getClass());
            if (interfaze == null) {
                throw new PojoSyncException("unknown type: " + object.getClass());
            }
            if (interfaze != null) {
                Access access = (Access) object;
                Object delegate = access.getDelegate();
                int identifier = Classes.getInstance().getObjectIdentifier().getId(delegate);
                Reference reference = new Reference(interfaze, identifier);
                if (objects != null && !objects.contains(access)) {
                    objects.add(access);
                }
                map.put(key, reference);
                return;
            }
        }
    }

    @Override
    public boolean isEmpty(Objects objects) {
        return map.isEmpty();
    }

    public Object getReference(IAccessCollection objects, String key, Object originalValue) {
        if (originalValue == null) {
            return null;
        }
        int id = Classes.getInstance().getObjectIdentifier().getId(originalValue);
        Class<?> clazz = Classes.getInstance().getSynchronizableInterface(originalValue.getClass());
        Access access = (Access) objects.get(clazz, id);
        if (access == null) {
            access = Classes.getInstance().createAccessForObject(objects, originalValue);
        }
        Reference ref = new Reference(clazz, access.getDelta().getId());
        map.put(key, ref);
        return access;
    }

    public Object get(IAccessCollection objects, String key) {
        Object value = map.get(key);
        if (value == NULL_VALUE) {
            return null;
        }
        if (value instanceof Reference) {
            Reference reference = (Reference) value;
            value = objects.get(reference.getInterface(), reference.getId());
            if (value == null) {
                throw new PojoSyncException("failed to solve reference: " + reference);
            }
        } else if (value instanceof ArrayOfReferences) {
            ArrayOfReferences references = (ArrayOfReferences) value;
            Class<?> iface = references.getInterface();
            int[] refIds = references.getReferences();
            value = Array.newInstance(iface, refIds.length);
            for (int i = 0; i < refIds.length; i++) {
                Access access = (Access) objects.get(iface, refIds[i]);
                if (access == null) {
                    throw new PojoSyncException("failed to solve reference " + iface.getName() + ":" + refIds[i]);
                }
                Array.set(value, i, access);
            }
        }
        return value;
    }

    public static Object copyArray(Object array) {
        int len = Array.getLength(array);
        Object copy = Array.newInstance(array.getClass().getComponentType(), len);
        System.arraycopy(array, 0, copy, 0, len);
        return copy;
    }

    private Object prepareArray(IAccessCollection objects, Object array) {
        if (array == null) {
            return NULL_VALUE;
        } else if (array.getClass().getComponentType().isPrimitive()) {
            return array;
        } else {
            Class<?> interfaze = Classes.getInstance().getSynchronizableInterface(array.getClass().getComponentType());
            Object[] accessArr = (Object[]) array;
            ArrayOfReferences refArr = new ArrayOfReferences(interfaze, accessArr.length);
            for (int i = 0; i < accessArr.length; i++) {
                Access access;
                int accessId;
                if (accessArr[i] instanceof Access) {
                    access = (Access) accessArr[i];
                    accessId = access.getDelta().getId();
                } else {
                    accessId = Classes.getInstance().getObjectIdentifier().getId(accessArr[i]);
                    access = objects.getAccess(interfaze, accessId);
                }
                if (access == null) {
                    access = Classes.getInstance().createAccessForObject(objects, accessArr[i]);
                    objects.add(access);
                    accessId = access.getDelta().getId();
                }
                refArr.set(i, accessId);
            }
            return refArr;
        }
    }

    public void setArray(IAccessCollection objects, String key, Object newArray, Object originalArray) {
        map.putArray(key, prepareArray(objects, newArray));
        if (!map.containsOriginalArray(key)) {
            if (originalArray == null) {
                map.putOriginalArray(key, NULL_VALUE);
            } else {
                map.putOriginalArray(key, prepareArray(objects, originalArray));
            }
        }
    }

    public Object getArray(IAccessCollection objects, String key, Object originalArray, Class<?> elementType) {
        if (!isSet(key)) {
            if (originalArray != null && !originalArray.getClass().isArray()) {
                throw new PojoSyncException("array expected");
            }
            if (elementType.isPrimitive()) {
                map.put(key, prepareArray(objects, originalArray));
            }
            map.putArray(key, prepareArray(objects, originalArray));
            if (originalArray == null) {
                map.putOriginalArray(key, NULL_VALUE);
            } else if (originalArray.getClass().getComponentType().isPrimitive()) {
                map.putOriginalArray(key, copyArray(originalArray));
            } else {
                map.putOriginalArray(key, prepareArray(objects, originalArray));
            }
        }
        return get(objects, key);
    }

    public boolean arrayModified(String key) {
        return !Map.arraysEqual(map.getArray(key), map.getOriginalArray(key));
    }

    private static Object mergeArrays(Object thisArray, Object otherArray) {
        if (thisArray == null) {
            return otherArray;
        }
        if (thisArray instanceof ArrayOfReferences && otherArray instanceof ArrayOfReferences) {
            ArrayOfReferences arr1 = (ArrayOfReferences) thisArray;
            ArrayOfReferences arr2 = (ArrayOfReferences) otherArray;
            if (arr1.getReferences().length == arr2.getReferences().length) {
                mergeArrays(arr1.getReferences(), arr2.getReferences());
                return thisArray;
            } else {
                return otherArray;
            }
        }
        int lenThis = Array.getLength(thisArray);
        int lenOther = Array.getLength(otherArray);
        if (lenThis != lenOther) {
            return otherArray;
        }
        for (int i = 0; i < lenThis; i++) {
            Object otherObject = Array.get(otherArray, i);
            Array.set(thisArray, i, otherObject);
        }
        return thisArray;
    }

    public void merge(IAccessCollection objects, IDelta delta) {
        logger.debug("merging delta (" + delta + ") into this (" + this + ")...");
        for (String key : delta.getKeys()) {
            Object value = delta.get(objects, key);
            if (value != null && value.getClass().isArray()) {
                Object thisArray = map.getArray(key);
                Object otherArray = ((Delta) (delta)).map.get(key);
                Object mergedArray = mergeArrays(thisArray, otherArray);
                if (thisArray != null && mergedArray != thisArray) {
                    logger.error("conflicting array. key=" + key + "; solving by overwriting");
                    set(objects, key, mergedArray);
                } else {
                    set(objects, key, mergedArray);
                }
            } else if (isSet(key)) {
                logger.error("conflict: key=" + key + "; solving by overwriting");
                set(objects, key, value);
            } else {
                set(objects, key, value);
            }
        }
    }

    public Collection<Reference> getReferences() {
        ArrayList<Reference> refs = new ArrayList<Reference>();
        for (Object value : map.values()) {
            if (value instanceof Reference) {
                refs.add(new Reference((Reference) value));
            } else if (value instanceof ArrayOfReferences) {
                ArrayOfReferences array = (ArrayOfReferences) value;
                Class<?> iface = array.getInterface();
                for (int id : array.getReferences()) {
                    refs.add(new Reference(iface, id));
                }
            }
        }
        return refs;
    }

    public void fixReferences(Synchronizer.IIdMapping mapping) {
        map.fixReferences(mapping);
    }

    @Override
    public void serialize(Serializer serializer) throws IOException {
        serializer.set(id, iface.getName(), virgin);
        for (String key : map.getKeys()) {
            Object value = map.get(key);
            if (value instanceof Reference) {
                Reference ref = (Reference) value;
                serializer.addReference(key, ref.getInterface().getName(), ref.getId());
            } else if (value instanceof ArrayOfReferences) {
                ArrayOfReferences refArr = (ArrayOfReferences) value;
                serializer.addReferenceArray(key, refArr.getInterface().getName(), refArr.getReferences());
            } else if (value == NULL_VALUE) {
                serializer.add(key, null);
            } else {
                serializer.add(key, value);
            }
        }
    }

    @Override
    public Serializer deserialize() throws IOException {
        map.clear();
        return new Serializer() {

            @Override
            public void add(String key, Object value) {
                if (value == null) {
                    map.put(key, NULL_VALUE);
                } else {
                    map.put(key, value);
                }
            }

            @Override
            public void set(int id, String interfaceName, boolean virgin) {
                Delta.this.id = id;
                Delta.this.virgin = virgin;
                try {
                    Delta.this.iface = Class.forName(interfaceName);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void addReference(String key, String interfaceName, int id) {
                Class<?> iface;
                try {
                    iface = Class.forName(interfaceName);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                map.put(key, new Reference(iface, id));
            }

            @Override
            public void addReferenceArray(String key, String interfaceName, int[] idArr) {
                Class<?> iface;
                try {
                    iface = Class.forName(interfaceName);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                ArrayOfReferences refArr = new ArrayOfReferences(iface, idArr.length);
                for (int i = 0; i < idArr.length; i++) {
                    refArr.set(i, idArr[i]);
                }
                map.put(key, refArr);
            }
        };
    }

    @Override
    public Delta clone() {
        return new Delta(this);
    }

    public void reset() {
        map.clear();
        for (String key : map.getArrayKeys()) {
            Object array = map.getArray(key);
            if (array.getClass().isArray()) {
                map.putOriginalArray(key, copyArray(array));
            } else if (array instanceof ArrayOfReferences) {
                ArrayOfReferences refArr = (ArrayOfReferences) array;
                map.putOriginalArray(key, new ArrayOfReferences(refArr));
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(iface.getName()).append(":").append(id);
        builder.append("[map: ");
        builder.append(map);
        builder.append("]");
        return builder.toString();
    }
}
