package com.versant.core.jdo;

import com.versant.core.jdo.sco.*;
import com.versant.core.jdo.sco.detached.DetachSCOFactoryRegistry;
import com.versant.core.metadata.*;
import com.versant.core.common.OID;
import com.versant.core.common.State;
import com.versant.core.common.*;
import javax.jdo.spi.JDOImplHelper;
import javax.jdo.spi.PersistenceCapable;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

/**
 * This keeps track of State's, FetchGroup's, OID's and PCs for the detach
 * code. It provides a list of State's and FetchGroup's for a breadth first
 * traversal of a State graph. It also provides hashed access to the State
 * and PC for a given OID.
 */
public class DetachStateContainer {

    private static final int INITIAL_SIZE = 16;

    private final JDOImplHelper jdoImplHelper = JDOImplHelper.getInstance();

    private int size;

    private int sizeFG;

    private int sizeRefs;

    private int capacity = INITIAL_SIZE;

    private int capacityFG = INITIAL_SIZE;

    private int capacityRefs = INITIAL_SIZE * 2;

    private int posFG = -1;

    private State[] states = new State[capacity];

    private PersistenceCapable[] pcs = new PersistenceCapable[capacity];

    private OID[] oids = new OID[capacity];

    private FetchGroup[][] stateFG = new FetchGroup[capacity][];

    private FetchGroup[] fetchGroups = new FetchGroup[capacityFG];

    private int[] stateIndexs = new int[capacityFG];

    private State[] stateRefs = new State[capacityRefs];

    private Map oidMap = new HashMap();

    private final VersantDetachStateManager stateManager = new VersantDetachStateManager();

    private final VersantDetachedStateManager detachedStateManager = new VersantDetachedStateManager();

    private final VersantPersistenceManagerImp pm;

    private final DetachSCOFactoryRegistry scoFactoryRegistry = new DetachSCOFactoryRegistry();

    private final ModelMetaData modelMetaData;

    public DetachStateContainer(VersantPersistenceManagerImp pm) {
        this.pm = pm;
        modelMetaData = pm.modelMetaData;
        stateManager.setDsc(this);
    }

    /**
     * Add a oid and state to the container. If this oid is allready in the
     * container we just copy the values from the new state into the container's
     * state.
     * The state can later be looked up in by using the oid.
     */
    public int add(OID oid, State newState) {
        addState(newState);
        Integer integer = (Integer) oidMap.get(oid);
        if (integer == null) {
            if (size == capacity) {
                capacity = (capacity * 3) / 2 + 1;
                State[] ns = new State[capacity];
                System.arraycopy(states, 0, ns, 0, size);
                states = ns;
                PersistenceCapable[] npcs = new PersistenceCapable[capacity];
                System.arraycopy(pcs, 0, npcs, 0, size);
                pcs = npcs;
                OID[] noids = new OID[capacity];
                System.arraycopy(oids, 0, noids, 0, size);
                oids = noids;
                FetchGroup[][] nStateFG = new FetchGroup[capacity][];
                System.arraycopy(stateFG, 0, nStateFG, 0, size);
                stateFG = nStateFG;
            }
            states[size] = newState;
            oids[size] = oid;
            oidMap.put(oid, new Integer(size));
            return size++;
        } else {
            int i = integer.intValue();
            State realState = states[i];
            if (realState == null) {
                states[i] = newState;
            } else if (realState != newState) {
                if (newState != null) {
                    realState.updateNonFilled(newState);
                }
                newState = realState;
            }
            return i;
        }
    }

    /**
     * Link the FetchGroup to this state.
     */
    public void add(OID oid, State newState, FetchGroup fg) {
        int stateIndex = add(oid, newState);
        if (fg == null) return;
        if (newState != null) {
            fg = fg.resolve(newState.getClassMetaData(modelMetaData));
        }
        for (FetchGroup superFG = fg; superFG != null; superFG = superFG.superFetchGroup) {
            addImpl(stateIndex, superFG);
        }
    }

    public void addImpl(int stateIndex, FetchGroup fg) {
        FetchGroup[] sfg = stateFG[stateIndex];
        if (sfg == null) {
            sfg = new FetchGroup[4];
            stateFG[stateIndex] = sfg;
        }
        int length = sfg.length;
        boolean add = true;
        for (int i = 0; i < length; i++) {
            FetchGroup fetchGroup = sfg[i];
            if (fetchGroup == fg) {
                return;
            } else if (fetchGroup == null) {
                sfg[i] = fg;
                add = false;
                break;
            }
        }
        if (add) {
            int oldSize = sfg.length;
            int newSize = oldSize * 2;
            FetchGroup[] nfg = new FetchGroup[newSize];
            System.arraycopy(sfg, 0, nfg, 0, oldSize);
            sfg = nfg;
            sfg[oldSize] = fg;
            stateFG[stateIndex] = sfg;
        }
        if (sizeFG == capacityFG) {
            capacityFG = (capacityFG * 3) / 2 + 1;
            FetchGroup[] ng = new FetchGroup[capacityFG];
            System.arraycopy(fetchGroups, 0, ng, 0, sizeFG);
            fetchGroups = ng;
            int[] nsi = new int[capacityFG];
            System.arraycopy(stateIndexs, 0, nsi, 0, sizeFG);
            stateIndexs = nsi;
        }
        fetchGroups[sizeFG] = fg;
        stateIndexs[sizeFG] = stateIndex;
        sizeFG++;
    }

    /**
     * We keep a ref to these states so they will not be gc'ed out of the cache
     */
    private void addState(State newState) {
        if (sizeRefs == capacityRefs) {
            capacityRefs = (capacityRefs * 3) / 2 + 1;
            State[] ns = new State[capacityRefs];
            System.arraycopy(stateRefs, 0, ns, 0, sizeRefs);
            stateRefs = ns;
        }
        stateRefs[sizeRefs++] = newState;
    }

    /**
     * todo
     */
    public void createPcClasses(ModelMetaData jmd) {
        if (pcs[0] != null) return;
        for (int i = 0; i < size; i++) {
            PersistenceCapable pc = jdoImplHelper.newInstance(states[i].getClassMetaData(jmd).cls, stateManager);
            if (!(pc instanceof VersantDetachable)) {
                throw BindingSupportImpl.getInstance().runtime("'" + pc.getClass().getName() + "' is not detachable please enhance " + "classes with the detach option set to true");
            }
            VersantDetachable detachable = (VersantDetachable) pc;
            Object externalOID = pm.getExternalOID(oids[i]);
            oidMap.put(externalOID, new Integer(i));
            detachable.versantSetOID(externalOID);
            detachable.versantSetVersion(states[i].getOptimisticLockingValue());
            pcs[i] = detachable;
        }
        for (int i = 0; i < size; i++) {
            PersistenceCapable pc = pcs[i];
            ClassMetaData cmd = states[i].getClassMetaData(jmd);
            FieldMetaData[] pkFields = cmd.top.pkFields;
            if (pkFields != null) {
                for (int j = 0; j < pkFields.length; j++) {
                    FieldMetaData fmd = cmd.pkFields[j];
                    int managedFieldNo = fmd.managedFieldNo;
                    if (!stateManager.isLoaded(pc, managedFieldNo)) {
                        pc.jdoReplaceField(managedFieldNo);
                    }
                }
            }
        }
        for (int i = 0; i < size; i++) {
            PersistenceCapable pc = pcs[i];
            FetchGroup[] sfg = stateFG[i];
            int sfgLength = sfg.length;
            for (int cfg = 0; cfg < sfgLength; cfg++) {
                FetchGroup fg = sfg[cfg];
                if (fg == null) {
                    break;
                }
                boolean defaultFG = fg == fg.classMetaData.fetchGroups[0];
                for (int j = 0; j < fg.fields.length; j++) {
                    FetchGroupField field = fg.fields[j];
                    FieldMetaData fmd = field.fmd;
                    if (fmd.fake) continue;
                    if (fmd.scoField) continue;
                    if (defaultFG && !fmd.isJDODefaultFetchGroup()) continue;
                    int managedFieldNo = fmd.managedFieldNo;
                    if (stateManager.isLoaded(pc, managedFieldNo)) continue;
                    pc.jdoReplaceField(managedFieldNo);
                }
            }
        }
        for (int i = 0; i < size; i++) {
            PersistenceCapable pc = pcs[i];
            FetchGroup[] sfg = stateFG[i];
            int sfgLength = sfg.length;
            for (int cfg = 0; cfg < sfgLength; cfg++) {
                FetchGroup fg = sfg[cfg];
                if (fg == null) {
                    break;
                }
                boolean defaultFG = fg == fg.classMetaData.fetchGroups[0];
                for (int j = 0; j < fg.fields.length; j++) {
                    FetchGroupField field = fg.fields[j];
                    FieldMetaData fmd = field.fmd;
                    if (fmd.fake) continue;
                    if (!fmd.scoField) continue;
                    if (defaultFG && !fmd.isJDODefaultFetchGroup()) continue;
                    int managedFieldNo = fmd.managedFieldNo;
                    if (stateManager.isLoaded(pc, managedFieldNo)) continue;
                    pc.jdoReplaceField(managedFieldNo);
                }
            }
        }
        for (int i = 0; i < size; i++) {
            pcs[i].jdoReplaceStateManager(detachedStateManager);
        }
    }

    /**
     * Are there more FetchGroups to be processed?
     */
    public boolean hasNextFetchGroup() {
        posFG++;
        return posFG < sizeFG;
    }

    /**
     * Get the state for the current fetch group.
     */
    public State getNextFetchGroupState() {
        return states[stateIndexs[posFG]];
    }

    /**
     * Get the fetch group.
     */
    public FetchGroup getNextFetchGroup() {
        return fetchGroups[posFG];
    }

    /**
     * Get the OID for the current fetch group.
     */
    public OID getNextFetchGroupOID() {
        return oids[stateIndexs[posFG]];
    }

    /**
     * Do we have stuff for the oid?
     */
    public boolean contains(OID oid) {
        if (oid == null) return false;
        return oidMap.containsKey(oid);
    }

    /**
     * Get the PC for the oid.
     */
    public PersistenceCapable getPC(OID oid) {
        return pcs[((Integer) oidMap.get(oid)).intValue()];
    }

    public State getState(PersistenceCapable pc) {
        Object oid = ((VersantDetachable) pc).versantGetOID();
        Integer integer = (Integer) oidMap.get(oid);
        if (integer == null) {
            return null;
        }
        return states[integer.intValue()];
    }

    public Object getObjectField(VersantDetachable pc, int fieldNo) {
        State state = getState(pc);
        ClassMetaData cmd = state.getClassMetaData();
        FieldMetaData fmd = cmd.stateFields[cmd.absToRel[fieldNo]];
        int category = fmd.category;
        Object o = state.getInternalObjectFieldAbs(fieldNo);
        switch(category) {
            case MDStatics.CATEGORY_COLLECTION:
                if (o != null) {
                    VersantSCOCollectionFactory factory = scoFactoryRegistry.getJDOGenieSCOCollectionFactory(fmd);
                    if (o instanceof VersantSCOCollection) {
                        CollectionData collectionData = new CollectionData();
                        ((VersantSCOCollection) o).fillCollectionData(collectionData);
                        collectionData.valueCount = getDetachCopy(collectionData.values, collectionData.valueCount);
                        return factory.createSCOCollection(pc, pm, detachedStateManager, fmd, collectionData);
                    } else {
                        CollectionData collectionData = new CollectionData();
                        Object[] values = (Object[]) o;
                        int length = values.length;
                        collectionData.values = new Object[length];
                        System.arraycopy(values, 0, collectionData.values, 0, length);
                        collectionData.valueCount = collectionData.values.length;
                        collectionData.valueCount = getDetachCopy(collectionData.values, collectionData.valueCount);
                        return factory.createSCOCollection(pc, pm, detachedStateManager, fmd, collectionData);
                    }
                }
                break;
            case MDStatics.CATEGORY_ARRAY:
                if (o != null) {
                    if (!o.getClass().isArray()) return o;
                    Class type = o.getClass().getComponentType();
                    int length = Array.getLength(o);
                    Object newArray = Array.newInstance(type, length);
                    System.arraycopy(o, 0, newArray, 0, length);
                    if (fmd.isElementTypePC()) {
                        getDetachCopy((Object[]) newArray, length);
                    }
                    return newArray;
                }
                break;
            case MDStatics.CATEGORY_MAP:
                if (o != null) {
                    VersantSCOMapFactory factory = scoFactoryRegistry.getJDOGenieSCOMapFactory(fmd);
                    if (o instanceof VersantSCOMap) {
                        MapData mapData = new MapData();
                        ((VersantSCOMap) o).fillMapData(mapData);
                        mapData.entryCount = getDetachCopy(mapData.keys, mapData.entryCount);
                        mapData.entryCount = getDetachCopy(mapData.values, mapData.entryCount);
                        return factory.createSCOHashMap(pc, pm, detachedStateManager, fmd, mapData);
                    } else {
                        MapEntries entries = (MapEntries) o;
                        MapData mapData = new MapData();
                        mapData.entryCount = entries.keys.length;
                        Object[] keys = new Object[mapData.entryCount];
                        System.arraycopy(entries.keys, 0, keys, 0, mapData.entryCount);
                        Object[] values = new Object[mapData.entryCount];
                        System.arraycopy(entries.values, 0, values, 0, mapData.entryCount);
                        mapData.keys = keys;
                        mapData.values = values;
                        mapData.entryCount = getDetachCopy(mapData.keys, mapData.entryCount);
                        mapData.entryCount = getDetachCopy(mapData.values, mapData.entryCount);
                        return factory.createSCOHashMap(pc, pm, detachedStateManager, fmd, mapData);
                    }
                }
                break;
            case MDStatics.CATEGORY_EXTERNALIZED:
                if (o != null) {
                    if (state.isResolvedForClient(fieldNo)) {
                        o = fmd.externalizer.toExternalForm(pm, o);
                    }
                    o = fmd.externalizer.fromExternalForm(pm, o);
                }
                return o;
            case MDStatics.CATEGORY_REF:
            case MDStatics.CATEGORY_POLYREF:
                if (o != null) {
                    if (fmd.scoField) {
                        VersantSCOFactory factory = scoFactoryRegistry.getJdoGenieSCOFactory(fmd);
                        return factory.createSCO(pc, pm, detachedStateManager, fmd, o);
                    } else {
                        return getDetachCopy(o);
                    }
                }
                break;
        }
        return o;
    }

    public PersistenceCapable getDetachCopy(OID oid) {
        Integer integer = (Integer) oidMap.get(oid);
        int index = integer == null ? -1 : integer.intValue();
        if (index < 0) {
            throw BindingSupportImpl.getInstance().exception("Could not find detached copy of object with oid='" + oid + "'");
        }
        return pcs[index];
    }

    public PersistenceCapable getDetachCopy(PersistenceCapable pc) {
        PCStateMan internalSM = pm.getInternalSM(pc);
        return getDetachCopy(internalSM.oid);
    }

    public int getDetachCopy(Object[] objects, int count) {
        count = Math.min(count, objects.length);
        for (int i = 0; i < count; i++) {
            Object o = objects[i];
            if (o != null) {
                objects[i] = getDetachCopy(o);
            } else {
                return i;
            }
        }
        return count;
    }

    public Object getDetachCopy(Object o) {
        if (o instanceof OID) {
            return getDetachCopy((OID) o);
        } else if (o instanceof PersistenceCapable) {
            return getDetachCopy((PersistenceCapable) o);
        }
        return o;
    }

    public VersantDetachStateManager getStateManager() {
        return stateManager;
    }
}
