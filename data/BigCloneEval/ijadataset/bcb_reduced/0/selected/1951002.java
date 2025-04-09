package de.ios.framework.db2;

import java.util.*;
import java.lang.reflect.*;
import de.ios.framework.basic.*;

/**
 * Baseclass for access to datenbase tables.
 * Use <i>getAttributes</i> to retrive all public members of the object. Modify or read values from
 * the returned array. If the object uses primitive datatypes or if you have assigned complet new
 * instances, use <i>setAttributes</i> to write back all data in the object.<br>
 * Remember:
 * <ul>
 *  <li> Only public members are handled.
 *  <li> All primitive members are represented by their wrapper-classes.
 *  <li> All datatypes can be used. Even static members are retrived and set on this level.
 *  <li> oid  identify an object and all subobject. This attribute is not retrieved or set
 *       with the set-/get functions. To set it use "setOId".
 *  <li> DBObjectServer will only handle DBAttribute-members.
 * </ul>
 * In normal case you must define only the attributes of the object.
 * No constructor or methods must be defined. Initialisiation can be done with <i>initInstance</i>.
 * @see #initInstance()
 * @see #getAttributes()
 * @see #setAttributes() 
 */
public class DBObject implements Cloneable, java.io.Serializable, de.ios.framework.db2.log.TransactionLogable {

    public static final Class attrClass = getClassForName("de.ios.framework.db2.DBAttribute");

    public static final Class objClass = getClassForName("de.ios.framework.db2.DBObject");

    public static final Class containClass = getClassForName("de.ios.framework.db2.DBContainedObject");

    public static final Class referClass = getClassForName("de.ios.framework.db2.DBReferencedObject");

    /**
   * A virtual attribute for selecting the objectid in whereclauses. 
   * The value of the objectid CAN NOT BE AFFECTED by this attribute!
   */
    public static final DBLongAttr OID = new DBLongAttr();

    /**
   * The field that represents the OID-member. 
   */
    public static final Field OIDF = new DBObject().getField("OID");

    /**
   * Called by class-initialisation. 
   */
    protected static final Class getClassForName(String name) {
        try {
            return Class.forName(name);
        } catch (Exception e) {
            Debug.printException("de.ios.framework.db2.DBObject", e);
            return null;
        }
    }

    /**
   * The private object-identifactation. To identify the object and all subobjects in the database. 
   * Use as primary key only if object is not a element of an array of an other object.
   */
    protected long oid = -1;

    /**
   * Timestamp. Counter for updates at this object. Used for validations during updates.
   */
    protected long ts = -1;

    /**
   * Reste the object-id so that the object is handled as new object.
   */
    public void resetOId() {
        oid = -1;
        ts = -1;
    }

    /** 
   * Get object-identification.
   */
    public final long getOId() {
        return oid;
    }

    /** 
   * Get object-identification.
   */
    public final long getStamp() {
        return ts;
    }

    /**
   * Constructor.
   * Call this only if ALL datamembers are instanciated elsewhere!
   */
    public DBObject() {
    }

    /**
   * Constructor.
   * Must be called from extentions.
   * ONly members of type DBAttribute will be instanciated.
   * @param checkNull If false, all DBAttributes are instanciated. If true, only members equal to null will be instanciated.
   */
    public DBObject(boolean checkNull) {
        initInstance(checkNull);
    }

    /**
   * Implements the cloneable-interface (returns null, if creating a 'new instance()' failed with a non-RuntimeException).
   */
    public Object clone() {
        DBObject other = null;
        try {
            other = (DBObject) getClass().newInstance();
            other.oid = oid;
            other.ts = ts;
            Class fType;
            int modifiers;
            Object o, ao;
            Field f;
            Field fields[] = this.getClass().getFields();
            int i, j, s;
            for (i = 0; i < fields.length; i++) {
                f = fields[i];
                modifiers = f.getModifiers();
                if (!(Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers))) {
                    o = f.get(this);
                    fType = f.getType();
                    if (o == null) f.set(other, null); else if (fType.isArray()) {
                        s = Array.getLength(o);
                        ao = Array.newInstance(fType.getComponentType(), s);
                        for (j = s - 1; j >= 0; j--) Array.set(ao, j, ((DBObject) Array.get(o, j)).clone());
                        f.set(other, ao);
                    } else {
                        try {
                            f.set(other, ((DBAttribute) o).clone());
                        } catch (ClassCastException e) {
                            try {
                                f.set(other, ((DBObject) o).clone());
                            } catch (ClassCastException e2) {
                                f.set(other, o);
                            }
                        }
                    }
                }
            }
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            Debug.printException(this, e);
            other = null;
        }
        return other;
    }

    /**
   * Gets Array of Attributes.
   * If primitive datatypes are used, the members are represented by wrapper-classes.
   * @return All public attributes are returned.
   */
    public Object[] getAttributes() {
        Field fields[] = getFields();
        Object a[] = new Object[fields.length];
        for (int i = 0; i < fields.length; i++) {
            try {
                a[i] = fields[i].get(this);
            } catch (Exception e) {
                Debug.printException(this, e);
            }
        }
        return a;
    }

    /**
   * Assign all Attributes. 
   */
    public void setAttributes(Object[] objs) {
        Field fields[] = getFields();
        for (int i = 0; i < fields.length; i++) {
            try {
                fields[i].set(this, objs[i]);
            } catch (Exception e) {
                Debug.printException(this, e);
            }
        }
    }

    /**
   * Get a field by his name.
   * @return The field or null if it is not found or not accessible.
   */
    public Field getField(String name) {
        try {
            return getClass().getField(name);
        } catch (Throwable t) {
            Debug.printThrowable(this, t);
            return null;
        }
    }

    /**
   * Gets Array of Fields.
   * Shortcut for "getClass().getFields()".
   * @return All public attributes are returned.
   */
    public final Field[] getFields() {
        return this.getClass().getFields();
    }

    /**
   * Initialise Instance. 
   * Creates Instances for all public DBAttribute-Members.
   * Arrays are creates with size 0.
   * Calls initInitance( false ).
   */
    public final void initInstance() {
        initInstance(false);
    }

    /**
   * Initialise Instance. 
   * Creates Instances for all public Members.
   * Arrays are creates with size 0.
   *
   * @param checkNull If true, only fields equal to null are set.
   */
    public final void initInstance(boolean checkNull) {
        Field fields[] = this.getClass().getFields();
        Class fType;
        Field f;
        if (checkNull) {
            for (int i = 0; i < fields.length; i++) {
                try {
                    f = fields[i];
                    if (f.get(this) == null) {
                        fType = f.getType();
                        if (attrClass.isAssignableFrom(fType)) {
                            f.set(this, fType.newInstance());
                        } else {
                            if (fType.isArray()) f.set(this, Array.newInstance(fType.getComponentType(), 0));
                        }
                    }
                } catch (Throwable e) {
                    Debug.printThrowable(this, e);
                }
            }
        } else {
            for (int i = 0; i < fields.length; i++) {
                try {
                    fType = (f = fields[i]).getType();
                    if (attrClass.isAssignableFrom(fType)) {
                        if (!f.equals(OIDF)) f.set(this, fType.newInstance());
                    } else {
                        if (fType.isArray()) f.set(this, Array.newInstance(fType.getComponentType(), 0));
                    }
                } catch (Throwable e) {
                    Debug.printThrowable(this, e);
                }
            }
        }
    }

    /**
   * Reset all modified-flags.
   * Sub-DBObjects must implement DBContainedObject to be affected.
   */
    public final void resetModified() {
        Debug.println(Debug.INFO, this, "resetModified");
        Field fields[] = getFields();
        Field ff;
        Object obj;
        Object aobj;
        Class fc;
        int i;
        int ai;
        int an;
        for (i = 0; i < fields.length; i++) {
            ff = fields[i];
            try {
                obj = ff.get(this);
            } catch (IllegalAccessException e) {
                Debug.printThrowable(this, e);
                obj = null;
            }
            if (obj != null) {
                if (obj instanceof DBAttribute) ((DBAttribute) obj).resetModified(); else if (obj instanceof DBContainedObject) ((DBObject) obj).resetModified(); else {
                    fc = obj.getClass();
                    if (fc.isArray()) {
                        if (objClass.isAssignableFrom(fc.getComponentType())) {
                            an = Array.getLength(obj);
                            for (ai = 0; ai < an; ai++) {
                                aobj = Array.get(obj, ai);
                                if (aobj instanceof DBContainedObject) ((DBObject) aobj).resetModified();
                            }
                        }
                    }
                }
            }
        }
    }

    /**
   * Get name of an attribute.
   */
    public final String getNameOf(DBAttribute a) {
        Field af[] = getFields();
        for (int i = 0; i < af.length; i++) {
            try {
                if (af[i].get(this) == a) return af[i].getName();
            } catch (Exception e) {
                Debug.printException(this, e);
            }
        }
        return null;
    }

    /**
   * Creates a new Array with the given Object added.
   * Don't forget to assign the return-value!
   *
   * @param array Source to resize (will not be modified!).
   * @param item   New element to add at the end.
   * @return New created Array with contains all elements from the source and the new one.
   */
    public static final Object addElementToArray(Object array, Object item) {
        Class fc = array.getClass().getComponentType();
        int n = Array.getLength(array);
        Object newArray = Array.newInstance(fc, n + 1);
        if (n > 0) System.arraycopy(array, 0, newArray, 0, n);
        Array.set(newArray, n, item);
        return newArray;
    }

    /**
   * Creates a new Array with the given index removed.
   * (Don't forget to assign the return-value!)
   *
   * @param array Source to resize (will not be modified!).
   * @param index Index of element to remove.
   * @return New created Array with contains all elements from the source except the removed one. 
   */
    public static final Object removeElementFromArray(Object array, int index) {
        Class fc = array.getClass().getComponentType();
        int n = Array.getLength(array);
        Object newArray = Array.newInstance(fc, n - 1);
        if (index > 0) System.arraycopy(array, 0, newArray, 0, index);
        if (index < (n - 1)) System.arraycopy(array, index + 1, newArray, index, n - index - 1);
        return newArray;
    }
}

;
