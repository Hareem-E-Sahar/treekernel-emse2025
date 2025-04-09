package marquee.xmlrpc.objectcomm;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.WeakHashMap;
import marquee.xmlrpc.XmlRpcCustomSerializer;
import marquee.xmlrpc.XmlRpcException;
import marquee.xmlrpc.XmlRpcSerializer;

/**
 *  This XmlRpcCustomSerializer serializes any Java object as an Xml-Rpc struct
 *  with a special member named "#ClassName". This member contains the qualified
 *  classname of the serialized type plus an object identifier which is used to
 *  track objects which have already been serialized for this serialization process.<p>
 *
 *  The format of the member is "qualified.class.name.of.Type,oid" where
 *  oid is an integer number identifying the object being serialized.
 *  The serializer continues to serialize the content of the object if and only if
 *  the object being serialized has not already been serialized in this
 *  serialization process. That means that the content of an object is only
 *  serialized once, following references to the same object consist only of the
 *  "#ClassName" member with the same object identifier as the first occurence.
 *
 *  @author  Rainer Bischof
 *  @version $Revision: 1.2 $
 *  @since   JDK 1.3
 *  @see     marquee.xmlrpc.XmlRpcCustomSerializer
 */
public class Serializer implements XmlRpcCustomSerializer {

    /** Name of the member that identifies the classname in a serialized object struct. */
    static final String OBJECT_TYPE = "#Type";

    /** Registries used to track serialized objects during the serialization process */
    private static Hashtable registries = new Hashtable();

    /** Indicates if transferred objects to have to implement java.io.Serializable */
    private static boolean requireSerializeable;

    /**
     *  Returns the class of java.lang.Object as this serializer may serialze any object
     */
    public Class getSupportedClass() {
        return requireSerializeable ? java.io.Serializable.class : Object.class;
    }

    /**
     *  Sets if the Serilizer should require transferred objects to implement java.io.Serializable.
     *
     *  @param A boolean indicating if to require Serializeable
     */
    public static void setRequireSerializeable(boolean requireSerializeable) {
        Serializer.requireSerializeable = requireSerializeable;
    }

    /**
     *  Serializes <code>value</code> to <code>output</code>.
     *
     *  @throws XmlRpcException in case anything goes wrong during serializing.
     */
    public void serialize(Object value, StringBuffer output) throws XmlRpcException {
        int hierarchyLevel = 0;
        ObjectRegistry reg = getObjectRegistry();
        try {
            try {
                Constructor c = value.getClass().getConstructor(new Class[0]);
            } catch (NoSuchMethodException exc) {
                throw new XmlRpcException("No default constructor defined in " + "object to be serialized: " + value);
            }
            boolean isAlreadySent = reg.objectRegistered(value);
            output.append("<struct>");
            output.append("<member><name>");
            output.append(OBJECT_TYPE);
            output.append("</name><value><string>");
            output.append(value.getClass().getName());
            output.append(",");
            output.append(reg.getObjectId(value));
            output.append("</string></value>");
            output.append("</member>");
            if (!isAlreadySent) {
                Class cl = value.getClass();
                while (!cl.isAssignableFrom(Object.class)) {
                    Field[] fields = cl.getDeclaredFields();
                    Object fieldValue;
                    for (int i = 0; i < fields.length; ++i) {
                        Field f = fields[i];
                        f.setAccessible(true);
                        if (!Modifier.isTransient(f.getModifiers()) && !Modifier.isFinal(f.getModifiers())) {
                            try {
                                fieldValue = fields[i].get(value);
                            } catch (java.lang.IllegalAccessException e) {
                                throw new XmlRpcException("Field " + f.getName() + " cannot be accessed by the object serializer.");
                            }
                            if (fieldValue != null) {
                                output.append("<member><name>");
                                if (hierarchyLevel != 0) {
                                    output.append(hierarchyLevel);
                                    output.append(":");
                                }
                                output.append(f.getName());
                                output.append("</name>");
                                XmlRpcSerializer.serialize(fieldValue, output);
                                output.append("</member>");
                            }
                        }
                    }
                    cl = cl.getSuperclass();
                    hierarchyLevel++;
                }
            }
            output.append("</struct>");
        } finally {
            freeObjectRegistry(reg);
        }
    }

    /**
     *  Rebuilds an object which has been de-serialized into a hashtable.
     *  The Hashtable must contain a key "#ClassName" which maps to a String
     *  that contains the qualified classname of the object and an object
     *  identifier in the format "qualified.class.name.of.Type,oid" .
     *
     *  @returns the rebuild object
     *
     *  @throws XmlRpcException if anything goes wrong while re-building the object
     */
    static Object buildObject(Hashtable ht, Class expectedType) throws XmlRpcException {
        Object o;
        ObjectRegistry reg = getObjectRegistry();
        try {
            String className = (String) ht.remove(OBJECT_TYPE);
            int oid = Integer.parseInt(className.substring(className.indexOf(",") + 1));
            o = reg.getObject(oid);
            if (o == null) {
                className = className.substring(0, className.indexOf(","));
                Class cl = Class.forName(className);
                if (!expectedType.isAssignableFrom(cl)) {
                    throw new XmlRpcException("Field type does not match. Cannot convert " + cl.getName() + " into " + expectedType.getName());
                }
                Constructor constructor = cl.getConstructor(new Class[0]);
                constructor.setAccessible(true);
                o = constructor.newInstance(new Object[0]);
                reg.registerObject(o, oid);
                populateObject(ht, o);
            }
        } catch (Exception e) {
            throw new XmlRpcException("Unable to reconstruct object: " + e.getClass().getName() + ": " + e.getMessage());
        } finally {
            freeObjectRegistry(reg);
        }
        return o;
    }

    /**
     *  Builds the object of type <code>className</code> that is stored in the provided Hashtable
     */
    private static void populateObject(Hashtable ht, Object o) throws Exception {
        String key;
        String fieldName;
        int pos;
        Field f;
        Class cl;
        List hierarchy = new ArrayList();
        List definedFields = new ArrayList();
        cl = o.getClass();
        while (!cl.isAssignableFrom(Object.class)) {
            hierarchy.add(cl);
            definedFields.addAll(Arrays.asList(cl.getDeclaredFields()));
            cl = cl.getSuperclass();
        }
        Iterator it = ht.keySet().iterator();
        while (it.hasNext()) {
            key = (String) it.next();
            pos = key.indexOf(":");
            if (pos < 0) {
                cl = (Class) hierarchy.get(0);
                fieldName = key;
            } else if (pos > 0 && pos < key.length() - 1) {
                cl = (Class) hierarchy.get(Integer.parseInt(key.substring(0, pos)));
                fieldName = key.substring(pos + 1);
            } else {
                throw new XmlRpcException("Invalid field name format: " + key);
            }
            f = cl.getDeclaredField(fieldName);
            f.setAccessible(true);
            definedFields.remove(f);
            if (!Modifier.isTransient(f.getModifiers()) && !Modifier.isFinal(f.getModifiers())) {
                Object value = ht.get(key);
                value = checkType(f.getType(), value);
                f.set(o, value);
            }
        }
        it = definedFields.iterator();
        while (it.hasNext()) {
            f = (Field) it.next();
            f.setAccessible(true);
            if (!Modifier.isTransient(f.getModifiers()) && !Modifier.isFinal(f.getModifiers())) {
                f.set(o, null);
            }
        }
    }

    /**
     *  Checks if the return value is an instance of the given class.
     *  In several special cases the passed value will be altered to another type:
     *
     *  <ul>
     *      <li>Objects encoded in hashtables will be reassembled and returned</li>
     *      <li>Hashtables may be altered to HashMap, WeakHashMap, or SortedMap</li>
     *      <li>Vectors may be altered to LinkedList, ArrayList, HashSet, Treeset,
     *          or an aray of objects or primitives.</li>
     *  </ul>
     *
     *  @returns an instance of the given class representing the passed value
     *
     *  @throws ClassNotFoundException if the Class of an encoded object cannot be found
     *
     *  @throws XmlRpcException if the passed value cannot be returned as an Instance of
     *          the given Class or an Exception occured while reassembling an object
     *          encoded in a Hashtable.
     */
    static Object checkType(Class expectedType, Object value) throws XmlRpcException {
        if (value instanceof Boolean && (Boolean.TYPE.isAssignableFrom(expectedType) || Boolean.class.isAssignableFrom(expectedType))) {
            return value;
        } else if (value instanceof Integer) {
            if (Integer.TYPE.isAssignableFrom(expectedType) || Integer.class.isAssignableFrom(expectedType)) {
                return value;
            } else if (Byte.TYPE.isAssignableFrom(expectedType) || Byte.class.isAssignableFrom(expectedType)) {
                return new Byte((byte) ((Integer) value).intValue());
            } else if (Long.TYPE.isAssignableFrom(expectedType) || Long.class.isAssignableFrom(expectedType)) {
                return new Long(((Integer) value).intValue());
            } else if (Short.TYPE.isAssignableFrom(expectedType) || Short.class.isAssignableFrom(expectedType)) {
                return new Short((short) ((Integer) value).intValue());
            }
        } else if (value instanceof Double) {
            if (Float.TYPE.isAssignableFrom(expectedType) || Float.class.isAssignableFrom(expectedType)) {
                Float tmp = new Float(((Double) value).doubleValue());
                return tmp;
            } else if (Double.TYPE.isAssignableFrom(expectedType) || Double.class.isAssignableFrom(expectedType)) {
                return value;
            }
        } else if (value instanceof String) {
            if (Character.TYPE.isAssignableFrom(expectedType) || Character.class.isAssignableFrom(expectedType)) {
                return new Character(((String) value).charAt(0));
            }
        } else if (value instanceof Hashtable) {
            if (containsObject(value)) {
                return buildObject((Hashtable) value, expectedType);
            } else {
                if (Hashtable.class.isAssignableFrom(expectedType)) {
                    checkForContainedObjects((Map) value);
                    return value;
                } else if (Map.class.isAssignableFrom(expectedType)) {
                    checkForContainedObjects((Map) value);
                    if (expectedType.isAssignableFrom(HashMap.class)) {
                        return new HashMap((Map) value);
                    } else if (expectedType.isAssignableFrom(WeakHashMap.class)) {
                        return new WeakHashMap((Map) value);
                    } else if (expectedType.isAssignableFrom(TreeMap.class)) {
                        return new TreeMap((Map) value);
                    }
                }
            }
        } else if (value instanceof Vector) {
            if (expectedType.isAssignableFrom(Vector.class)) {
                checkForContainedObjects((Vector) value);
                return value;
            } else if (Collection.class.isAssignableFrom(expectedType)) {
                checkForContainedObjects((Vector) value);
                if (expectedType.isAssignableFrom(LinkedList.class)) {
                    return new LinkedList((Collection) value);
                } else if (expectedType.isAssignableFrom(ArrayList.class)) {
                    return new ArrayList((Collection) value);
                } else if (expectedType.isAssignableFrom(HashSet.class)) {
                    return new HashSet((Collection) value);
                } else if (expectedType.isAssignableFrom(TreeSet.class)) {
                    return new TreeSet((Collection) value);
                }
            } else if (expectedType.isArray()) {
                Vector v = (Vector) value;
                Object array = Array.newInstance(expectedType.getComponentType(), v.size());
                for (int i = 0; i < v.size(); ++i) {
                    Array.set(array, i, v.elementAt(i));
                }
                value = array;
            }
        }
        if (value == null || expectedType.isInstance(value)) {
            return value;
        }
        throw new XmlRpcException("Field type does not match. Cannot convert " + value.getClass().getName() + " into " + expectedType.getName());
    }

    /**
     *  Checks a Map for objects encoded in Hashtables and replaces them with the object.
     */
    private static void checkForContainedObjects(Map orig) throws XmlRpcException {
        Object key, value;
        Map m = new HashMap(orig);
        orig.clear();
        Iterator it = m.keySet().iterator();
        while (it.hasNext()) {
            key = it.next();
            value = m.get(key);
            if (containsObject(value)) {
                value = buildObject((Hashtable) value, Object.class);
            }
            orig.put(key, value);
        }
    }

    /**
     *  Checks a List for objects encoded in Hashtables and replaces them with the object.
     */
    private static void checkForContainedObjects(List orig) throws XmlRpcException {
        Object value;
        List l = new ArrayList(orig);
        orig.clear();
        Iterator it = l.iterator();
        while (it.hasNext()) {
            value = it.next();
            if (containsObject(value)) {
                value = buildObject((Hashtable) value, Object.class);
            }
            orig.add(value);
        }
    }

    /**
     *  Returns true if the provided object is an object encoded in a hashtable.
     *
     *  @param o The object to check.
     *
     *  @returns true if the p�rovided object is an object encodedin a hashtable.
     */
    static boolean containsObject(Object o) {
        return o instanceof Hashtable && ((Hashtable) o).containsKey(OBJECT_TYPE);
    }

    /**
     *  Returns the ObjectRegistry used by this serialization / deserializatzion process.
     *  The process is identified by it's executing Thread. If no ObjectRegistry exists for
     *  that Thread a new one is created and associated with the Thread. The ObjectRegistry
     *  uses an internal counter to keep track of the number of method calls that use this
     *  registry.
     *
     *  @returns The ObjectRegistry to be used for this Thread.
     */
    private static synchronized ObjectRegistry getObjectRegistry() {
        ObjectRegistry reg = (ObjectRegistry) registries.get(Thread.currentThread());
        if (reg == null) {
            reg = new ObjectRegistry();
            registries.put(Thread.currentThread(), reg);
        }
        reg.increaseUsage();
        return reg;
    }

    /**
     *  Frees the ObjectRegistry. If no other method call uses this ObjectRegistry it is discarded.
     */
    private static synchronized void freeObjectRegistry(ObjectRegistry reg) {
        if (reg.decreaseUsage()) {
            registries.remove(Thread.currentThread());
        }
    }

    /**
     *  This class is used to keep track of objects already serialized or deserialized.
     */
    private static class ObjectRegistry {

        /**
         *  Increases the number of method calls that use this OR.
         */
        public void increaseUsage() {
            usage++;
        }

        /**
         *  Decreases the number of method calls that use this OR.
         *
         *  @returns true if no other calls use this OR.
         */
        public boolean decreaseUsage() {
            return --usage == 0;
        }

        /**
         *  Returns the object ID of the provided object. If this method has been
         *  called before with the same object this returns the same ID, otherwise
         *  a new ID is generated
         *
         *  @param Object o The object to retrieve/generate the ID for.
         *
         *  @returns the object ID.
         */
        public int getObjectId(Object o) {
            if (o == null) {
                return 0;
            } else {
                if (!o2i.containsKey(o)) {
                    registerObject(o, ++seq);
                    return seq;
                } else {
                    Integer key = (Integer) o2i.get(o);
                    return key.intValue();
                }
            }
        }

        /**
         *  Returns the object for the given object ID.
         *
         *  @param int oid The object ID to retrieve the object for.
         *
         *  @returns the object with the given ID or null if not found.
         */
        public Object getObject(int oid) {
            return i2o.get(new Integer(oid));
        }

        /**
         *  Registers a new object with the provided object ID.
         *
         *  @param Object o The object to register.
         *
         *  @param int oid The object ID to register this object with.
         */
        public void registerObject(Object o, int oid) {
            Integer key = new Integer(oid);
            i2o.put(key, o);
            o2i.put(o, key);
        }

        /**
         *  Returns if this object has been registered before.
         *
         *  @param Object o The Object to check the registry status for.
         *
         *  @returns true if this object has an object ID associated with it.
         */
        public boolean objectRegistered(Object o) {
            return o2i.containsKey(o);
        }

        private int usage;

        private int seq = 0;

        private Hashtable i2o = new Hashtable();

        private Hashtable o2i = new Hashtable();
    }
}
