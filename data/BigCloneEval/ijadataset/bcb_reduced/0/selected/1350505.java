package org.conserve.tools;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.conserve.adapter.AdapterBase;
import org.conserve.connection.ConnectionWrapper;

/**
 * Wrapper that encapsulates all ObjectRepresentations for an object.
 * 
 * 
 * @author Erik Berglund
 * 
 */
public class ObjectStack {

    private ArrayList<ObjectRepresentation> representations = new ArrayList<ObjectRepresentation>();

    private AdapterBase adapter;

    public ObjectStack(AdapterBase adapter, Class<?> c) {
        this(adapter, c, null);
    }

    public ObjectStack(AdapterBase adapter, Class<?> c, Object o) {
        this(adapter, c, o, null);
    }

    public ObjectStack(AdapterBase adapter, Class<?> c, Object o, DelayedInsertionBuffer delayBuffer) {
        this.adapter = adapter;
        while (c != null) {
            ObjectRepresentation rep = null;
            if (o != null) {
                rep = new ObjectRepresentation(adapter, c, o, delayBuffer);
            } else {
                rep = new ObjectRepresentation(adapter, c, delayBuffer);
            }
            representations.add(0, rep);
            c = c.getSuperclass();
        }
        for (int x = getSize() - 1; x >= 0; x--) {
            ObjectRepresentation rep = getRepresentation(x);
            for (int p = 0; p < rep.getPropertyCount(); p++) {
                String name = rep.getPropertyName(p);
                for (int y = x - 1; y >= 0; y--) {
                    ObjectRepresentation superRep = getRepresentation(y);
                    if (superRep.hasProperty(name)) {
                        rep.removeProperty(p);
                        p--;
                        break;
                    }
                }
            }
        }
        if (this.getActualRepresentation().isImplementation(Collection.class)) {
            ObjectRepresentation lastKnownImplementor = getActualRepresentation();
            for (int x = this.getSize() - 2; x >= 0; x--) {
                if (this.getRepresentation(x).isImplementation(Collection.class)) {
                    lastKnownImplementor = this.getRepresentation(x);
                } else {
                    break;
                }
            }
            lastKnownImplementor.implementCollection();
        }
        if (this.getActualRepresentation().isImplementation(Map.class)) {
            ObjectRepresentation lastKnownImplementor = getActualRepresentation();
            for (int x = this.getSize() - 2; x >= 0; x--) {
                if (this.getRepresentation(x).isImplementation(Map.class)) {
                    lastKnownImplementor = this.getRepresentation(x);
                } else {
                    break;
                }
            }
            lastKnownImplementor.implementMap();
        }
        if (!adapter.allowsEmptyStatements()) {
            for (int x = getSize() - 1; x >= 0; x--) {
                ObjectRepresentation rep = getRepresentation(x);
                if (rep.getPropertyCount() == 0) {
                    rep.addValuePair(Defaults.DUMMY_COL_NAME, null, short.class);
                }
            }
        }
    }

    /**
	 * Get the number of representation layers. Each object has a stack that has size = N+1, where N is the size of the
	 * stack of the super-class. java.lang.Object has size = 1.
	 * 
	 * @return the number of inheritance levels in this object stack.
	 */
    public int getSize() {
        return representations.size();
    }

    /**
	 * Get the representation at a given level.
	 * 
	 * @param level
	 * @return the reperesentation of this object at the given class level.
	 */
    public ObjectRepresentation getRepresentation(int level) {
        return representations.get(level);
    }

    /**
	 * Get the representation of a given class, if it exists in this stack.
	 * 
	 * @param clazz
	 *            the class to get the representation for.
	 * @return the reperesentation of this object at the given class level.
	 */
    public ObjectRepresentation getRepresentation(Class<?> clazz) {
        int level = getLevel(clazz);
        if (level >= 0) {
            return getRepresentation(level);
        } else {
            return null;
        }
    }

    /**
	 * 
	 * Get the representational level of the given class. Returns -1 if there is no such class in the stack.
	 * 
	 * @param c
	 * @return the level (where 0 equals Object.class) of the class c within this stack.
	 */
    public int getLevel(Class<?> c) {
        if (c.isInterface()) {
            for (int x = getSize() - 1; x >= 0; x--) {
                Class<?> candidate = getRepresentation(x).getRepresentedClass();
                if (ObjectTools.implementsInterfaceIncludingSuper(candidate, c)) {
                    return x;
                }
            }
        } else {
            for (int x = 0; x < getSize(); x++) {
                if (getRepresentation(x).getRepresentedClass().equals(c)) {
                    return x;
                }
            }
        }
        return -1;
    }

    /**
	 * Save the object represented by this ObjectStack, and all referenced objects, as necessary.
	 * 
	 * @param cw
	 * @throws SQLException
	 * @throws IOException
	 */
    public void save(ConnectionWrapper cw) throws SQLException, IOException {
        String className = null;
        Long id = null;
        for (int x = getSize() - 1; x >= 0; x--) {
            ObjectRepresentation rep = getRepresentation(x);
            adapter.getPersist().getTableManager().ensureTableExists(rep, cw);
            rep.save(cw, className, id);
            if (rep.isArray()) {
                className = Defaults.ARRAY_TABLE_NAME;
            } else {
                className = ObjectTools.getSystemicName(rep.getRepresentedClass());
            }
            id = rep.getId();
        }
        ObjectRepresentation rep = this.getActualRepresentation();
        adapter.getPersist().saveToCache(rep.getTableName(), rep.getObject(), rep.getId());
    }

    /**
	 * Get the representation at the bottom of the stack, i.e. the representation that corresponds to the actual class
	 * of the object.
	 * 
	 * @return the representation of the object at the bottom of the inheritance tree.
	 */
    public ObjectRepresentation getActualRepresentation() {
        return getRepresentation(getSize() - 1);
    }
}
