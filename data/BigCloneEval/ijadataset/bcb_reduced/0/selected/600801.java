package com.versant.core.jdo;

import com.versant.core.common.MapEntries;
import com.versant.core.common.OID;
import com.versant.core.common.State;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.metadata.MDStaticUtils;
import com.versant.core.metadata.MDStatics;
import javax.jdo.PersistenceManager;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateManager;
import java.lang.reflect.Array;
import java.util.*;
import com.versant.core.common.BindingSupportImpl;

/**
 *
 */
public class AttachCopyStateManager implements VersantStateManager {

    private VersantPersistenceManagerImp pm;

    private State state;

    private FieldMetaData fmd;

    public AttachCopyStateManager(VersantPersistenceManagerImp pm) {
        this.pm = pm;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setFieldMetaData(FieldMetaData fmd) {
        this.fmd = fmd;
    }

    public byte replacingFlags(PersistenceCapable persistenceCapable) {
        return 0;
    }

    public StateManager replacingStateManager(PersistenceCapable persistenceCapable, StateManager stateManager) {
        return stateManager;
    }

    public boolean isDirty(PersistenceCapable persistenceCapable) {
        return ((VersantDetachable) persistenceCapable).versantIsDirty();
    }

    public boolean isTransactional(PersistenceCapable persistenceCapable) {
        return false;
    }

    public boolean isPersistent(PersistenceCapable persistenceCapable) {
        return false;
    }

    public boolean isNew(PersistenceCapable persistenceCapable) {
        return getPcOID((VersantDetachable) persistenceCapable).isNew();
    }

    public boolean isDeleted(PersistenceCapable persistenceCapable) {
        return false;
    }

    public PersistenceManager getPersistenceManager(PersistenceCapable persistenceCapable) {
        return null;
    }

    public void makeDirty(PersistenceCapable persistenceCapable, String s) {
        ((VersantDetachable) persistenceCapable).versantMakeDirty(s);
    }

    public Object getObjectId(PersistenceCapable persistenceCapable) {
        return getPcOID((VersantDetachable) persistenceCapable);
    }

    public Object getTransactionalObjectId(PersistenceCapable persistenceCapable) {
        return null;
    }

    public boolean isLoaded(PersistenceCapable persistenceCapable, int i) {
        return ((VersantDetachable) persistenceCapable).versantIsLoaded(i);
    }

    public void preSerialize(PersistenceCapable persistenceCapable) {
    }

    public boolean getBooleanField(PersistenceCapable persistenceCapable, int i, boolean b) {
        return b;
    }

    public char getCharField(PersistenceCapable persistenceCapable, int i, char c) {
        return c;
    }

    public byte getByteField(PersistenceCapable persistenceCapable, int i, byte b) {
        return b;
    }

    public short getShortField(PersistenceCapable persistenceCapable, int i, short i1) {
        return i1;
    }

    public int getIntField(PersistenceCapable persistenceCapable, int i, int i1) {
        return i1;
    }

    public long getLongField(PersistenceCapable persistenceCapable, int i, long l) {
        return l;
    }

    public float getFloatField(PersistenceCapable persistenceCapable, int i, float v) {
        return v;
    }

    public double getDoubleField(PersistenceCapable persistenceCapable, int i, double v) {
        return v;
    }

    public String getStringField(PersistenceCapable persistenceCapable, int i, String s) {
        return s;
    }

    public Object getObjectField(PersistenceCapable persistenceCapable, int i, Object o) {
        return o;
    }

    public void setBooleanField(PersistenceCapable persistenceCapable, int i, boolean b, boolean b1) {
    }

    public void setCharField(PersistenceCapable persistenceCapable, int i, char c, char c1) {
    }

    public void setByteField(PersistenceCapable persistenceCapable, int i, byte b, byte b1) {
    }

    public void setShortField(PersistenceCapable persistenceCapable, int i, short i1, short i2) {
    }

    public void setIntField(PersistenceCapable persistenceCapable, int i, int i1, int i2) {
    }

    public void setLongField(PersistenceCapable persistenceCapable, int i, long l, long l1) {
    }

    public void setFloatField(PersistenceCapable persistenceCapable, int i, float v, float v1) {
    }

    public void setDoubleField(PersistenceCapable persistenceCapable, int i, double v, double v1) {
    }

    public void setStringField(PersistenceCapable persistenceCapable, int i, String s, String s1) {
    }

    public void setObjectField(PersistenceCapable persistenceCapable, int i, Object o, Object o1) {
    }

    public void providedBooleanField(PersistenceCapable persistenceCapable, int i, boolean b) {
        state.setBooleanFieldAbs(i, b);
    }

    public void providedCharField(PersistenceCapable persistenceCapable, int i, char c) {
        state.setCharFieldAbs(i, c);
    }

    public void providedByteField(PersistenceCapable persistenceCapable, int i, byte b) {
        state.setByteFieldAbs(i, b);
    }

    public void providedShortField(PersistenceCapable persistenceCapable, int i, short i1) {
        state.setShortFieldAbs(i, i1);
    }

    public void providedIntField(PersistenceCapable persistenceCapable, int i, int i1) {
        state.setIntFieldAbs(i, i1);
    }

    public void providedLongField(PersistenceCapable persistenceCapable, int i, long l) {
        state.setLongFieldAbs(i, l);
    }

    public void providedFloatField(PersistenceCapable persistenceCapable, int i, float v) {
        state.setFloatFieldAbs(i, v);
    }

    public void providedDoubleField(PersistenceCapable persistenceCapable, int i, double v) {
        state.setDoubleFieldAbs(i, v);
    }

    public void providedStringField(PersistenceCapable persistenceCapable, int i, String s) {
        state.setStringFieldAbs(i, s);
    }

    public void providedObjectField(PersistenceCapable persistenceCapable, int i, Object o) {
        if (o == null) {
            state.setObjectFieldAbs(i, o);
        } else {
            state.setObjectFieldAbs(i, getUnresolved(o));
        }
    }

    private Object getUnresolved(Object o) {
        switch(fmd.category) {
            case MDStatics.CATEGORY_COLLECTION:
                if (o instanceof Collection) {
                    Collection col = (Collection) o;
                    ArrayList oids = new ArrayList(col.size());
                    if (fmd.isElementTypePC()) {
                        for (Iterator it = col.iterator(); it.hasNext(); ) {
                            VersantDetachable detachable = (VersantDetachable) it.next();
                            oids.add(getPC(detachable));
                        }
                    } else {
                        oids.addAll(col);
                    }
                    col = createNewCol();
                    col.addAll(oids);
                    return col;
                }
                break;
            case MDStatics.CATEGORY_ARRAY:
                if (!o.getClass().isArray()) return o;
                Class type = o.getClass().getComponentType();
                int length = Array.getLength(o);
                Object newArray = Array.newInstance(type, length);
                System.arraycopy(o, 0, newArray, 0, length);
                if (fmd.isElementTypePC()) {
                    Object[] objects = (Object[]) newArray;
                    for (int i = 0; i < objects.length; i++) {
                        objects[i] = getPC((VersantDetachable) objects[i]);
                    }
                }
                return newArray;
            case MDStatics.CATEGORY_MAP:
                if (o instanceof Map) {
                    MapEntries entries = new MapEntries();
                    Map map = (Map) o;
                    int size = map.size();
                    entries.keys = new Object[size];
                    entries.values = new Object[size];
                    int x = 0;
                    for (Iterator it = map.keySet().iterator(); it.hasNext(); ) {
                        Object o1 = (Object) it.next();
                        if (fmd.isKeyTypePC()) {
                            entries.keys[x] = getPC((VersantDetachable) o1);
                        } else {
                            entries.keys[x] = o1;
                        }
                        if (fmd.isElementTypePC()) {
                            entries.values[x] = getPC((VersantDetachable) map.get(o1));
                        } else {
                            entries.values[x] = map.get(o1);
                        }
                        x++;
                    }
                    map = createNewMap();
                    length = entries.keys.length;
                    for (int i = 0; i < length; i++) {
                        Object key = entries.keys[i];
                        Object value = entries.values[i];
                        map.put(key, value);
                    }
                    return map;
                }
                break;
            case MDStatics.CATEGORY_REF:
            case MDStatics.CATEGORY_POLYREF:
                VersantDetachable detachable = (VersantDetachable) o;
                return getPC(detachable);
        }
        return o;
    }

    private Object getPC(VersantDetachable detachable) {
        OID pcOID = getPcOID(detachable);
        return pm.getObjectById(pcOID, false);
    }

    private Map createNewMap() {
        switch(fmd.typeCode) {
            case MDStatics.MAP:
            case MDStatics.HASHMAP:
                return new HashMap();
            case MDStatics.HASHTABLE:
                return new Hashtable();
            case MDStatics.TREEMAP:
            case MDStatics.SORTEDMAP:
                return new TreeMap();
            default:
                throw BindingSupportImpl.getInstance().notImplemented("Creating a Map instance for field " + fmd.getName() + " of type " + MDStaticUtils.toSimpleName(fmd.typeCode) + " is not supported");
        }
    }

    private Collection createNewCol() {
        switch(fmd.typeCode) {
            case MDStatics.HASHSET:
            case MDStatics.SET:
                return new HashSet();
            case MDStatics.TREESET:
            case MDStatics.SORTEDSET:
                return new TreeSet();
            case MDStatics.COLLECTION:
            case MDStatics.LIST:
            case MDStatics.ARRAYLIST:
                return new ArrayList();
            case MDStatics.LINKEDLIST:
                return new LinkedList();
            case MDStatics.VECTOR:
                return new Vector();
            default:
                throw BindingSupportImpl.getInstance().notImplemented("Creating a Collection instance for field " + fmd.getName() + " of type " + MDStaticUtils.toSimpleName(fmd.typeCode) + " is not supported");
        }
    }

    public boolean replacingBooleanField(PersistenceCapable persistenceCapable, int i) {
        return false;
    }

    public char replacingCharField(PersistenceCapable persistenceCapable, int i) {
        return 0;
    }

    public byte replacingByteField(PersistenceCapable persistenceCapable, int i) {
        return 0;
    }

    public short replacingShortField(PersistenceCapable persistenceCapable, int i) {
        return 0;
    }

    public int replacingIntField(PersistenceCapable persistenceCapable, int i) {
        return 0;
    }

    public long replacingLongField(PersistenceCapable persistenceCapable, int i) {
        return 0;
    }

    public float replacingFloatField(PersistenceCapable persistenceCapable, int i) {
        return 0;
    }

    public double replacingDoubleField(PersistenceCapable persistenceCapable, int i) {
        return 0;
    }

    public String replacingStringField(PersistenceCapable persistenceCapable, int i) {
        return null;
    }

    public Object replacingObjectField(PersistenceCapable persistenceCapable, int i) {
        return null;
    }

    public void fillNewAppPKField(int fieldNo) {
    }

    public void makeDirty(PersistenceCapable pc, int managedFieldNo) {
        ((VersantDetachable) pc).versantMakeDirty(managedFieldNo);
    }

    private OID getPcOID(VersantDetachable persistenceCapable) {
        return pm.getOID(persistenceCapable);
    }

    public OID getOID() {
        return null;
    }

    public PersistenceCapable getPersistenceCapable() {
        return null;
    }
}
