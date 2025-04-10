package com.sun.jmx.snmp.agent;

import java.io.Serializable;
import java.util.Date;
import java.util.Vector;
import java.util.Enumeration;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.NotificationBroadcaster;
import javax.management.MBeanNotificationInfo;
import javax.management.ListenerNotFoundException;
import com.sun.jmx.snmp.SnmpOid;
import com.sun.jmx.snmp.SnmpValue;
import com.sun.jmx.snmp.SnmpInt;
import com.sun.jmx.snmp.SnmpVarBind;
import com.sun.jmx.snmp.SnmpStatusException;
import com.sun.jmx.snmp.EnumRowStatus;
import com.sun.jmx.trace.Trace;

/**
 * This class is the base class for SNMP table metadata.
 * <p>
 * Its responsibility is to manage a sorted array of OID indexes
 * according to the SNMP indexing scheme over the "real" table.
 * Each object of this class can be bound to an
 * {@link com.sun.jmx.snmp.agent.SnmpTableEntryFactory} to which it will
 * forward remote entry creation requests, and invoke callbacks
 * when an entry has been successfully added to / removed from
 * the OID index array.
 * </p>
 *
 * <p>
 * For each table defined in the MIB, mibgen will generate a specific
 * class called Table<i>TableName</i> that will implement the 
 * SnmpTableEntryFactory interface, and a corresponding 
 * <i>TableName</i>Meta class that will extend this class. <br>
 * The Table<i>TableName</i> class corresponds to the MBean view of the
 * table while the <i>TableName</i>Meta class corresponds to the
 * MIB metadata view of the same table.
 * </p>
 *
 * <p>
 * Objects of this class are instantiated by the generated 
 * whole MIB class extending {@link com.sun.jmx.snmp.agent.SnmpMib}
 * You should never need to instantiate this class directly.
 * </p>
 *
 * <p><b>This API is a Sun Microsystems internal API  and is subject 
 * to change without notice.</b></p>
 * @see com.sun.jmx.snmp.agent.SnmpMib
 * @see com.sun.jmx.snmp.agent.SnmpMibEntry
 * @see com.sun.jmx.snmp.agent.SnmpTableEntryFactory
 * @see com.sun.jmx.snmp.agent.SnmpTableSupport
 *
 * @version     4.59     04/07/06
 * @author      Sun Microsystems, Inc
 */
public abstract class SnmpMibTable extends SnmpMibNode implements NotificationBroadcaster, Serializable {

    /**
     * Create a new <CODE>SnmpMibTable</CODE> metadata node.
     *
     * <p>
     * @param mib The SNMP MIB to which the metadata will be linked.
     */
    public SnmpMibTable(SnmpMib mib) {
        this.theMib = mib;
        setCreationEnabled(false);
    }

    /**
     * This method is invoked when the creation of a new entry is requested
     * by a remote SNMP manager.
     * <br>By default, remote entry creation is disabled - and this method 
     * will not be called. You can dynamically switch the entry creation
     * policy by calling <code>setCreationEnabled(true)</code> and <code>
     * setCreationEnabled(false)</code> on this object.
     * <p><b><i>
     * This method is called internally by the SNMP runtime and you 
     * should never need to call it directly. </b></i>However you might want 
     * to extend it in order to implement your own specific application 
     * behaviour, should the default behaviour not be at your convenience.
     * </p>
     * <p>
     * @param req   The SNMP  subrequest requesting this creation
     * @param rowOid  The OID indexing the conceptual row (entry) for which 
     *                the creation was requested.
     * @param depth The position of the columnar object arc in the OIDs
     *              from the varbind list.
     *
     * @exception SnmpStatusException if the entry cannot be created.
     */
    public abstract void createNewEntry(SnmpMibSubRequest req, SnmpOid rowOid, int depth) throws SnmpStatusException;

    /**
     * Tell whether the specific version of this metadata generated
     * by <code>mibgen</code> requires entries to be registered with
     * the MBeanServer. In this case an ObjectName will have to be 
     * passed to addEntry() in order for the table to behave correctly 
     * (case of the generic metadata).
     * <p>
     * If that version of the metadata does not require entry to be
     * registered, then passing an ObjectName becomes optional (null
     * can be passed instead).
     *
     * @return <code>true</code> if registration is required by this
     *         version of the metadata.
     */
    public abstract boolean isRegistrationRequired();

    /**
     * Tell whether a new entry should be created when a SET operation
     * is received for an entry that does not exist yet.
     * 
     * @return true if a new entry must be created, false otherwise.<br>
     *         [default: returns <CODE>false</CODE>]
     **/
    public boolean isCreationEnabled() {
        return creationEnabled;
    }

    /**
     * This method lets you dynamically switch the creation policy.
     *
     * <p>
     * @param remoteCreationFlag Tells whether remote entry creation must
     *        be enabled or disabled.
     * <ul><li>
     * <CODE>setCreationEnabled(true)</CODE> will enable remote entry
     *      creation via SET operations.</li>
     * <li>
     * <CODE>setCreationEnabled(false)</CODE> will disable remote entry
     *      creation via SET operations.</li>
     * <p> By default remote entry creation via SET operation is disabled.
     * </p>
     * </ul>
     **/
    public void setCreationEnabled(boolean remoteCreationFlag) {
        creationEnabled = remoteCreationFlag;
    }

    /**
     * Return <code>true</code> if the conceptual row contains a columnar
     * object used to control creation/deletion of rows in this table.
     * <p>
     * This  columnar object can be either a variable with RowStatus
     * syntax as defined by RFC 2579, or a plain variable whose
     * semantics is table specific.
     * <p>
     * By default, this function returns <code>false</code>, and it is
     * assumed that the table has no such control variable.<br>
     * When <code>mibgen</code> is used over SMIv2 MIBs, it will generate
     * an <code>hasRowStatus()</code> method returning <code>true</code>
     * for each table containing an object with RowStatus syntax.
     * <p>
     * When this method returns <code>false</code> the default mechanism
     * for remote entry creation is used.
     * Otherwise, creation/deletion is performed as specified
     * by the control variable (see getRowAction() for more details).
     * <p>
     * This method is called internally when a SET request involving
     * this table is processed.
     * <p>
     * If you need to implement a control variable which do not use
     * the RowStatus convention as defined by RFC 2579, you should
     * subclass the generated table metadata class in order to redefine
     * this method and make it returns <code>true</code>.<br>
     * You will then have to redefine the isRowStatus(), mapRowStatus(), 
     * isRowReady(), and setRowStatus() methods to suit your specific
     * implementation.
     * <p>
     * @return <li><code>true</code> if this table contains a control 
     *         variable (eg: a variable with RFC 2579 RowStatus syntax),
     *         </li>
     *         <li><code>false</code> if this table does not contain
     *         any control variable.</li>
     *
     **/
    public boolean hasRowStatus() {
        return false;
    }

    /**
     * Generic handling of the <CODE>get</CODE> operation.
     * <p> The default implementation of this method is to
     * <ul>
     * <li> check whether the entry exists, and if not register an 
     *      exception for each varbind in the list.
     * <li> call the generated 
     *      <CODE>get(req,oid,depth+1)</CODE> method. </li>
     * </ul> 
     * <p>
     * <pre>
     * public void get(SnmpMibSubRequest req, int depth)
     *	  throws SnmpStatusException {
     *    boolean         isnew  = req.isNewEntry();
     *
     * 	  // if the entry does not exists, then registers an error for
     *    // each varbind involved (nb: this should not happen, since 
     *    // the error should already have been detected earlier)
     *    //
     *    if (isnew) {
     *        SnmpVarBind     var = null;
     *        for (Enumeration e= req.getElements(); e.hasMoreElements();) {
     *            var = (SnmpVarBind) e.nextElement(); 
     *            req.registerGetException(var,noSuchNameException);
     *        }
     *    }
     *
     *    final SnmpOid oid = req.getEntryOid();
     *    get(req,oid,depth+1);
     * }
     * </pre>
     * <p> You should not need to override this method in any cases, because
     * it will eventually call 
     * <CODE>get(SnmpMibSubRequest req, int depth)</CODE> on the generated 
     * derivative of <CODE>SnmpMibEntry</CODE>. If you need to implement
     * specific policies for minimizing the accesses made to some remote
     * underlying resources, or if you need to implement some consistency
     * checks between the different values provided in the varbind list,
     * you should then rather override 
     * <CODE>get(SnmpMibSubRequest req, int depth)</CODE> on the generated 
     * derivative of <CODE>SnmpMibEntry</CODE>.
     * <p>
     *
     */
    public void get(SnmpMibSubRequest req, int depth) throws SnmpStatusException {
        final boolean isnew = req.isNewEntry();
        final SnmpMibSubRequest r = req;
        if (isnew) {
            SnmpVarBind var = null;
            for (Enumeration e = r.getElements(); e.hasMoreElements(); ) {
                var = (SnmpVarBind) e.nextElement();
                r.registerGetException(var, noSuchInstanceException);
            }
        }
        final SnmpOid oid = r.getEntryOid();
        get(req, oid, depth + 1);
    }

    /**
     * Generic handling of the <CODE>check</CODE> operation.
     * <p> The default implementation of this method is to
     * <ul> 
     * <li> check whether a new entry must be created, and if remote
     *      creation of entries is enabled, create it. </li>
     * <li> call the generated 
     *      <CODE>check(req,oid,depth+1)</CODE> method. </li>
     * </ul> 
     * <p>
     * <pre>
     * public void check(SnmpMibSubRequest req, int depth)
     *	  throws SnmpStatusException {
     *    final SnmpOid     oid    = req.getEntryOid();
     *    final int         action = getRowAction(req,oid,depth+1);
     *
     *    beginRowAction(req,oid,depth+1,action);
     *    check(req,oid,depth+1);
     * }
     * </pre>
     * <p> You should not need to override this method in any cases, because
     * it will eventually call 
     * <CODE>check(SnmpMibSubRequest req, int depth)</CODE> on the generated 
     * derivative of <CODE>SnmpMibEntry</CODE>. If you need to implement
     * specific policies for minimizing the accesses made to some remote
     * underlying resources, or if you need to implement some consistency
     * checks between the different values provided in the varbind list,
     * you should then rather override 
     * <CODE>check(SnmpMibSubRequest req, int depth)</CODE> on the generated 
     * derivative of <CODE>SnmpMibEntry</CODE>.
     * <p>
     *
     */
    public void check(SnmpMibSubRequest req, int depth) throws SnmpStatusException {
        final SnmpOid oid = req.getEntryOid();
        final int action = getRowAction(req, oid, depth + 1);
        final boolean dbg = isDebugOn();
        if (dbg) debug("check", "Calling beginRowAction");
        beginRowAction(req, oid, depth + 1, action);
        if (dbg) debug("check", "Calling check for " + req.getSize() + " varbinds.");
        check(req, oid, depth + 1);
        if (dbg) debug("check", "check finished");
    }

    /**
     * Generic handling of the <CODE>set</CODE> operation.
     * <p> The default implementation of this method is to
     * call the generated 
     * <CODE>set(req,oid,depth+1)</CODE> method.
     * <p>
     * <pre>
     * public void set(SnmpMibSubRequest req, int depth)
     *	  throws SnmpStatusException {
     *    final SnmpOid oid = req.getEntryOid();
     *    final int  action = getRowAction(req,oid,depth+1);
     *
     *    set(req,oid,depth+1);
     *    endRowAction(req,oid,depth+1,action);
     * }
     * </pre>
     * <p> You should not need to override this method in any cases, because
     * it will eventually call 
     * <CODE>set(SnmpMibSubRequest req, int depth)</CODE> on the generated 
     * derivative of <CODE>SnmpMibEntry</CODE>. If you need to implement
     * specific policies for minimizing the accesses made to some remote
     * underlying resources, or if you need to implement some consistency
     * checks between the different values provided in the varbind list,
     * you should then rather override 
     * <CODE>set(SnmpMibSubRequest req, int depth)</CODE> on the generated 
     * derivative of <CODE>SnmpMibEntry</CODE>.
     * <p>
     *
     */
    public void set(SnmpMibSubRequest req, int depth) throws SnmpStatusException {
        final boolean dbg = isDebugOn();
        if (dbg) debug("set", "Entering set.");
        final SnmpOid oid = req.getEntryOid();
        final int action = getRowAction(req, oid, depth + 1);
        if (dbg) debug("set", "Calling set for " + req.getSize() + "varbinds.");
        set(req, oid, depth + 1);
        if (dbg) debug("set", "Calling endRowAction");
        endRowAction(req, oid, depth + 1, action);
        if (dbg) debug("set", "RowAction finished");
    }

    public void addEntry(SnmpOid rowOid, Object entry) throws SnmpStatusException {
        addEntry(rowOid, null, entry);
    }

    public synchronized void addEntry(SnmpOid oid, ObjectName name, Object entry) throws SnmpStatusException {
        if (isRegistrationRequired() == true && name == null) throw new SnmpStatusException(SnmpStatusException.badValue);
        if (size == 0) {
            insertOid(0, oid);
            if (entries != null) entries.addElement(entry);
            if (entrynames != null) entrynames.addElement(name);
            size++;
            if (factory != null) {
                try {
                    factory.addEntryCb(0, oid, name, entry, this);
                } catch (SnmpStatusException x) {
                    removeOid(0);
                    if (entries != null) entries.removeElementAt(0);
                    if (entrynames != null) entrynames.removeElementAt(0);
                    throw x;
                }
            }
            sendNotification(SnmpTableEntryNotification.SNMP_ENTRY_ADDED, (new Date()).getTime(), entry, name);
            return;
        }
        int pos = 0;
        pos = getInsertionPoint(oid, true);
        if (pos == size) {
            insertOid(tablecount, oid);
            if (entries != null) entries.addElement(entry);
            if (entrynames != null) entrynames.addElement(name);
            size++;
        } else {
            try {
                insertOid(pos, oid);
                if (entries != null) entries.insertElementAt(entry, pos);
                if (entrynames != null) entrynames.insertElementAt(name, pos);
                size++;
            } catch (ArrayIndexOutOfBoundsException e) {
            }
        }
        if (factory != null) {
            try {
                factory.addEntryCb(pos, oid, name, entry, this);
            } catch (SnmpStatusException x) {
                removeOid(pos);
                if (entries != null) entries.removeElementAt(pos);
                if (entrynames != null) entrynames.removeElementAt(pos);
                throw x;
            }
        }
        sendNotification(SnmpTableEntryNotification.SNMP_ENTRY_ADDED, (new Date()).getTime(), entry, name);
    }

    /**
     * Remove the specified entry from the table.
     * Also triggers the removeEntryCB() callback of the 
     * {@link com.sun.jmx.snmp.agent.SnmpTableEntryFactory} interface
     * if this node is bound to a factory.
     *
     * <p>
     * @param rowOid The <CODE>SnmpOid</CODE> identifying the table
     *               row to remove.
     *
     * @param entry The entry to be removed. This parameter is not used
     *              internally, it is simply passed along to the 
     *              removeEntryCB() callback.
     *
     * @exception SnmpStatusException if the specified entry couldn't
     *            be removed (if the given <code>rowOid</code> is not
     *            valid for instance).
     */
    public synchronized void removeEntry(SnmpOid rowOid, Object entry) throws SnmpStatusException {
        int pos = findObject(rowOid);
        if (pos == -1) return;
        removeEntry(pos, entry);
    }

    /**
     * Remove the specified entry from the table.
     * Also triggers the removeEntryCB() callback of the 
     * {@link com.sun.jmx.snmp.agent.SnmpTableEntryFactory} interface
     * if this node is bound to a factory.
     *
     * <p>
     * @param rowOid The <CODE>SnmpOid</CODE> identifying the table
     *               row to remove.
     *
     * @exception SnmpStatusException if the specified entry couldn't
     *            be removed (if the given <code>rowOid</code> is not
     *            valid for instance).
     */
    public void removeEntry(SnmpOid rowOid) throws SnmpStatusException {
        int pos = findObject(rowOid);
        if (pos == -1) return;
        removeEntry(pos, null);
    }

    /**
     * Remove the specified entry from the table.
     * Also triggers the removeEntryCB() callback of the 
     * {@link com.sun.jmx.snmp.agent.SnmpTableEntryFactory} interface
     * if this node is bound to a factory.
     *
     * <p>
     * @param pos The position of the entry in the table.
     *
     * @param entry The entry to be removed. This parameter is not used
     *              internally, it is simply passed along to the 
     *              removeEntryCB() callback.
     *
     * @exception SnmpStatusException if the specified entry couldn't
     *            be removed.
     */
    public synchronized void removeEntry(int pos, Object entry) throws SnmpStatusException {
        if (pos == -1) return;
        if (pos >= size) return;
        Object obj = entry;
        if (entries != null && entries.size() > pos) {
            obj = entries.elementAt(pos);
            entries.removeElementAt(pos);
        }
        ObjectName name = null;
        if (entrynames != null && entrynames.size() > pos) {
            name = (ObjectName) entrynames.elementAt(pos);
            entrynames.removeElementAt(pos);
        }
        final SnmpOid rowOid = tableoids[pos];
        removeOid(pos);
        size--;
        if (obj == null) obj = entry;
        if (factory != null) factory.removeEntryCb(pos, rowOid, name, obj, this);
        sendNotification(SnmpTableEntryNotification.SNMP_ENTRY_REMOVED, (new Date()).getTime(), obj, name);
    }

    /**
     * Get the entry corresponding to the specified rowOid.
     *
     * <p>
     * @param rowOid The <CODE>SnmpOid</CODE> identifying the
     *        row to be retrieved.
     *
     * @return The entry.
     *
     * @exception SnmpStatusException There is no entry with the specified 
     *      <code>rowOid</code> in the table.
     */
    public synchronized Object getEntry(SnmpOid rowOid) throws SnmpStatusException {
        int pos = findObject(rowOid);
        if (pos == -1) throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        return entries.elementAt(pos);
    }

    /**
     * Get the ObjectName of the entry corresponding to the 
     * specified rowOid.
     * The result of this method is only meaningful if 
     * isRegistrationRequired() yields true.
     *
     * <p>
     * @param rowOid The <CODE>SnmpOid</CODE> identifying the table
     *        row whose ObjectName we want to retrieve.
     *
     * @return The object name of the entry.
     *
     * @exception SnmpStatusException There is no entry with the specified 
     *      <code>rowOid</code> in the table.
     */
    public synchronized ObjectName getEntryName(SnmpOid rowOid) throws SnmpStatusException {
        int pos = findObject(rowOid);
        if (entrynames == null) return null;
        if (pos == -1 || pos >= entrynames.size()) throw new SnmpStatusException(SnmpStatusException.noSuchInstance);
        return (ObjectName) entrynames.elementAt(pos);
    }

    /**
     * Return the entries stored in this table <CODE>SnmpMibTable</CODE>.
     * <p> 
     * If the subclass generated by mibgen uses the generic way to access
     * the entries (i.e. if it goes through the MBeanServer) then some of
     * the entries may be <code>null</code>. It all depends whether a non
     * <code>null</code> entry was passed to addEntry().<br>
     * Otherwise, if it uses the standard way (access the entry directly
     * through their standard MBean interface) this array will contain all
     * the entries.
     * <p>
     * @return The entries array.
     */
    public Object[] getBasicEntries() {
        Object[] array = new Object[size];
        entries.copyInto(array);
        return array;
    }

    /**
     * Get the size of the table.
     *
     * @return The number of entries currently registered in this table.
     */
    public int getSize() {
        return size;
    }

    /**
     * Enable to add an SNMP entry listener to this 
     * <CODE>SnmpMibTable</CODE>.
     *
     * <p>
     * @param listener The listener object which will handle the 
     *    notifications emitted by the registered MBean.
     *
     * @param filter The filter object. If filter is null, no filtering 
     *    will be performed before handling notifications.
     *
     * @param handback The context to be sent to the listener when a 
     *    notification is emitted.
     *
     * @exception IllegalArgumentException Listener parameter is null.
     */
    public synchronized void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) {
        if (listener == null) {
            throw new java.lang.IllegalArgumentException("Listener can't be null");
        }
        java.util.Vector handbackList = (java.util.Vector) handbackTable.get(listener);
        java.util.Vector filterList = (java.util.Vector) filterTable.get(listener);
        if (handbackList == null) {
            handbackList = new java.util.Vector();
            filterList = new java.util.Vector();
            handbackTable.put(listener, handbackList);
            filterTable.put(listener, filterList);
        }
        handbackList.addElement(handback);
        filterList.addElement(filter);
    }

    /**
     * Enable to remove an SNMP entry listener from this 
     * <CODE>SnmpMibTable</CODE>.
     *
     * @param listener The listener object which will handle the 
     *    notifications emitted by the registered MBean.
     *    This method will remove all the information related to this 
     *    listener.
     *
     * @exception ListenerNotFoundException The listener is not registered 
     *    in the MBean.
     */
    public synchronized void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
        java.util.Vector handbackList = (java.util.Vector) handbackTable.get(listener);
        java.util.Vector filterList = (java.util.Vector) filterTable.get(listener);
        if (handbackList == null) {
            throw new ListenerNotFoundException("listener");
        }
        handbackTable.remove(listener);
        filterTable.remove(listener);
    }

    /**    
     * Return a <CODE>NotificationInfo</CODE> object containing the 
     * notification class and the notification type sent by the 
     * <CODE>SnmpMibTable</CODE>.  
     */
    public MBeanNotificationInfo[] getNotificationInfo() {
        String[] types = { SnmpTableEntryNotification.SNMP_ENTRY_ADDED, SnmpTableEntryNotification.SNMP_ENTRY_REMOVED };
        MBeanNotificationInfo[] notifsInfo = { new MBeanNotificationInfo(types, "com.sun.jmx.snmp.agent.SnmpTableEntryNotification", "Notifications sent by the SnmpMibTable") };
        return notifsInfo;
    }

    /**
     * Register the factory through which table entries should
     * be created when remote entry creation is enabled.
     *
     * <p>
     * @param factory The 
     *        {@link com.sun.jmx.snmp.agent.SnmpTableEntryFactory} through
     *        which entries will be created when a remote SNMP manager
     *        request the creation of a new entry via an SNMP SET request.
     */
    public void registerEntryFactory(SnmpTableEntryFactory factory) {
        this.factory = factory;
    }

    /**
     * Return true if the columnar object identified by <code>var</code>
     * is used to control the addition/deletion of rows in this table.
     *
     * <p>
     * By default, this method assumes that there is no control variable 
     * and always return <code>false</code>
     * <p>
     * If this table was defined using SMIv2, and if it contains a
     * control variable with RowStatus syntax, <code>mibgen</code>
     * will generate a non default implementation for this method
     * that will identify the RowStatus control variable.
     * <p>
     * You will have to redefine this method if you need to implement
     * control variables that do not conform to RFC 2579 RowStatus
     * TEXTUAL-CONVENTION.
     * <p>
     * @param rowOid The <CODE>SnmpOid</CODE> identifying the table
     *               row involved in the operation.
     *
     * @param var The OID arc identifying the involved columnar object.
     *
     * @param userData A contextual object containing user-data.
     *        This object is allocated through the <code>
     *        {@link com.sun.jmx.snmp.agent.SnmpUserDataFactory}</code>
     *        for each incoming SNMP request.
     * 
     **/
    protected boolean isRowStatus(SnmpOid rowOid, long var, Object userData) {
        return false;
    }

    /**
     * Return the RowStatus code value specified in this request.
     * <p>
     * The RowStatus code value should be one of the values defined
     * by {@link com.sun.jmx.snmp.EnumRowStatus}. These codes correspond
     * to RowStatus codes as defined in RFC 2579, plus the <i>unspecified</i>
     * value which is SNMP Runtime specific.
     * <p>
     *
     * @param req    The sub-request that must be handled by this node.
     *
     * @param rowOid The <CODE>SnmpOid</CODE> identifying the table
     *               row involved in the operation.
     *
     * @param depth  The depth reached in the OID tree.
     *
     * @return The RowStatus code specified in this request, if any:
     * <ul>
     * <li>If the specified row does not exist and this table do
     *     not use any variable to control creation/deletion of
     *     rows, then default creation mechanism is assumed and 
     *     <i>createAndGo</i> is returned</li>
     * <li>Otherwise, if the row exists and this table do not use any 
     *     variable to control creation/deletion of rows,
     *     <i>unspecified</i> is returned.</li>
     * <li>Otherwise, if the request does not contain the control variable,
     *     <i>unspecified</i> is returned.</li>
     * <li>Otherwise, mapRowStatus() is called to extract the RowStatus
     *     code from the SnmpVarBind that contains the control variable.</li>
     * </ul>
     *
     * @exception SnmpStatusException if the value of the control variable
     *            could not be mapped to a RowStatus code.
     *
     * @see com.sun.jmx.snmp.EnumRowStatus
     **/
    protected int getRowAction(SnmpMibSubRequest req, SnmpOid rowOid, int depth) throws SnmpStatusException {
        final boolean isnew = req.isNewEntry();
        final SnmpVarBind vb = req.getRowStatusVarBind();
        if (vb == null) {
            if (isnew && !hasRowStatus()) return EnumRowStatus.createAndGo; else return EnumRowStatus.unspecified;
        }
        try {
            return mapRowStatus(rowOid, vb, req.getUserData());
        } catch (SnmpStatusException x) {
            checkRowStatusFail(req, x.getStatus());
        }
        return EnumRowStatus.unspecified;
    }

    /**
     * Map the value of the <code>vbstatus</code> varbind to the
     * corresponding RowStatus code defined in 
     * {@link com.sun.jmx.snmp.EnumRowStatus}.
     * These codes correspond to RowStatus codes as defined in RFC 2579, 
     * plus the <i>unspecified</i> value which is SNMP Runtime specific.
     * <p>
     * By default, this method assumes that the control variable is 
     * an Integer, and it simply returns its value without further
     * analysis.
     * <p>
     * If this table was defined using SMIv2, and if it contains a
     * control variable with RowStatus syntax, <code>mibgen</code>
     * will generate a non default implementation for this method.
     * <p>
     * You will have to redefine this method if you need to implement
     * control variables that do not conform to RFC 2579 RowStatus
     * TEXTUAL-CONVENTION.
     *
     * <p>
     * @param rowOid The <CODE>SnmpOid</CODE> identifying the table
     *               row involved in the operation.
     *
     * @param vbstatus The SnmpVarBind containing the value of the control
     *           variable, as identified by the isRowStatus() method.
     *
     * @param userData A contextual object containing user-data.
     *        This object is allocated through the <code>
     *        {@link com.sun.jmx.snmp.agent.SnmpUserDataFactory}</code>
     *        for each incoming SNMP request.
     * 
     * @return The RowStatus code mapped from the value contained
     *     in <code>vbstatus</code>.
     *
     * @exception SnmpStatusException if the value of the control variable
     *            could not be mapped to a RowStatus code.
     *
     * @see com.sun.jmx.snmp.EnumRowStatus
     **/
    protected int mapRowStatus(SnmpOid rowOid, SnmpVarBind vbstatus, Object userData) throws SnmpStatusException {
        final SnmpValue rsvalue = vbstatus.value;
        if (rsvalue instanceof SnmpInt) return ((SnmpInt) rsvalue).intValue(); else throw new SnmpStatusException(SnmpStatusException.snmpRspInconsistentValue);
    }

    /**
     * Set the control variable to the specified <code>newStatus</code>
     * value.
     *
     * <p>
     * This method maps the given <code>newStatus</code> to the appropriate
     * value for the control variable, then sets the control variable in
     * the entry identified by <code>rowOid</code>. It returns the new 
     * value of the control variable.
     * <p>
     * By default, it is assumed that there is no control variable so this
     * method does nothing and simply returns <code>null</code>.
     * <p>
     * If this table was defined using SMIv2, and if it contains a
     * control variable with RowStatus syntax, <code>mibgen</code>
     * will generate a non default implementation for this method.
     * <p>
     * You will have to redefine this method if you need to implement
     * control variables that do not conform to RFC 2579 RowStatus
     * TEXTUAL-CONVENTION.
     *
     * <p>
     * @param rowOid The <CODE>SnmpOid</CODE> identifying the table
     *               row involved in the operation.
     *
     * @param newStatus The new status for the row: one of the
     *        RowStatus code defined in 
     *        {@link com.sun.jmx.snmp.EnumRowStatus}. These codes 
     *        correspond to RowStatus codes as defined in RFC 2579, 
     *        plus the <i>unspecified</i> value which is SNMP Runtime specific.
     *        
     * @param userData A contextual object containing user-data.
     *        This object is allocated through the <code>
     *        {@link com.sun.jmx.snmp.agent.SnmpUserDataFactory}</code>
     *        for each incoming SNMP request.
     * 
     * @return The new value of the control variable (usually 
     *         <code>new SnmpInt(newStatus)</code>) or <code>null</code>
     *         if the table do not have any control variable.
     * 
     * @exception SnmpStatusException If the given <code>newStatus</code> 
     *            could not be set on the specified entry, or if the
     *            given <code>newStatus</code> is not valid.
     *
     * @see com.sun.jmx.snmp.EnumRowStatus
     **/
    protected SnmpValue setRowStatus(SnmpOid rowOid, int newStatus, Object userData) throws SnmpStatusException {
        return null;
    }

    /**
     * Tell whether the specified row is ready and can be put in the
     * <i>notInService</i> state.
     * <p>
     * This method is called only once, after all the varbind have been
     * set on a new entry for which <i>createAndWait</i> was specified.
     * <p>
     * If the entry is not yet ready, this method should return false.
     * It will then be the responsibility of the entry to switch its
     * own state to <i>notInService</i> when it becomes ready.
     * No further call to <code>isRowReady()</code> will be made.
     * <p>
     * By default, this method always return true. <br>
     * <code>mibgen</code> will not generate any specific implementation
     * for this method - meaning that by default, a row created using
     * <i>createAndWait</i> will always be placed in <i>notInService</i>
     * state at the end of the request.
     * <p>
     * If this table was defined using SMIv2, and if it contains a
     * control variable with RowStatus syntax, <code>mibgen</code>
     * will generate an implementation for this method that will
     * delegate the work to the metadata class modelling the conceptual 
     * row, so that you can override the default behaviour by subclassing
     * that metadata class.
     * <p>
     * You will have to redefine this method if this default mechanism
     * does not suit your needs.
     * 
     * <p>
     * @param rowOid The <CODE>SnmpOid</CODE> identifying the table
     *               row involved in the operation.
     *
     * @param userData A contextual object containing user-data.
     *        This object is allocated through the <code>
     *        {@link com.sun.jmx.snmp.agent.SnmpUserDataFactory}</code>
     *        for each incoming SNMP request.
     * 
     * @return <code>true</code> if the row can be placed in 
     *         <i>notInService</i> state.
     *
     * @exception SnmpStatusException An error occured while trying
     *            to retrieve the row status, and the operation should 
     *            be aborted.
     *
     * @see com.sun.jmx.snmp.EnumRowStatus
     **/
    protected boolean isRowReady(SnmpOid rowOid, Object userData) throws SnmpStatusException {
        return true;
    }

    /**
     * Check whether the control variable of the given row can be
     * switched to the new specified <code>newStatus</code>.
     * <p>
     * This method is called during the <i>check</i> phase of a SET
     * request when the control variable specifies <i>active</i> or
     * <i>notInService</i>.
     * <p>
     * By default it is assumed that nothing prevents putting the
     * row in the requested state, and this method does nothing. 
     * It is simply provided as a hook so that specific checks can 
     * be implemented.
     * <p>
     * Note that if the actual row deletion fails afterward, the
     * atomicity of the request is no longer guaranteed.
     *
     * <p>
     * @param req    The sub-request that must be handled by this node.
     *
     * @param rowOid The <CODE>SnmpOid</CODE> identifying the table
     *               row involved in the operation.
     *
     * @param depth  The depth reached in the OID tree.
     *
     * @param newStatus The new status for the row: one of the
     *        RowStatus code defined in 
     *        {@link com.sun.jmx.snmp.EnumRowStatus}. These codes 
     *        correspond to RowStatus codes as defined in RFC 2579, 
     *        plus the <i>unspecified</i> value which is SNMP Runtime specific.
     *        
     * @exception SnmpStatusException if switching to this new state
     *            would fail.
     *
     **/
    protected void checkRowStatusChange(SnmpMibSubRequest req, SnmpOid rowOid, int depth, int newStatus) throws SnmpStatusException {
    }

    /**
     * Check whether the specified row can be removed from the table.
     * <p>
     * This method is called during the <i>check</i> phase of a SET
     * request when the control variable specifies <i>destroy</i>
     * <p>
     * By default it is assumed that nothing prevents row deletion
     * and this method does nothing. It is simply provided as a hook
     * so that specific checks can be implemented.
     * <p>
     * Note that if the actual row deletion fails afterward, the
     * atomicity of the request is no longer guaranteed.
     *
     * <p>
     * @param req    The sub-request that must be handled by this node.
     *
     * @param rowOid The <CODE>SnmpOid</CODE> identifying the table
     *               row involved in the operation.
     *
     * @param depth  The depth reached in the OID tree.
     *
     * @exception SnmpStatusException if the row deletion must be
     *            rejected.
     **/
    protected void checkRemoveTableRow(SnmpMibSubRequest req, SnmpOid rowOid, int depth) throws SnmpStatusException {
    }

    /**
     * Remove a table row upon a remote manager request.
     *
     * This method is called internally when <code>getRowAction()</code>
     * yields <i>destroy</i> - i.e.: it is only called when a remote 
     * manager requests the removal of a table row.<br>
     * You should never need to call this function directly.
     * <p>
     * By default, this method simply calls <code>removeEntry(rowOid)
     * </code>.
     * <p>
     * You can redefine this method if you need to implement some 
     * specific behaviour when a remote row deletion is invoked.
     * <p>
     * Note that specific checks should not be implemented in this
     * method, but rather in <code>checkRemoveTableRow()</code>. 
     * If <code>checkRemoveTableRow()</code> succeeds and this method 
     * fails afterward, the atomicity of the original SET request can no 
     * longer be guaranteed.
     * <p>
     *
     * @param req    The sub-request that must be handled by this node.
     *
     * @param rowOid The <CODE>SnmpOid</CODE> identifying the table
     *               row involved in the operation.
     *
     * @param depth  The depth reached in the OID tree.
     *
     * @exception SnmpStatusException if the actual row deletion fails.
     *            This should not happen since it would break the 
     *            atomicity of the SET request. Specific checks should
     *            be implemented in <code>checkRemoveTableRow()</code>
     *            if needed. If the entry does not exists, no exception
     *            is generated and the method simply returns.
     *
     **/
    protected void removeTableRow(SnmpMibSubRequest req, SnmpOid rowOid, int depth) throws SnmpStatusException {
        removeEntry(rowOid);
    }

    /**
     * This method takes care of initial RowStatus handling during the
     * check() phase of a SET request.
     *
     * In particular it will:
     * <ul><li>check that the given <code>rowAction</code> returned by 
     *         <code>getRowAction()</code> is valid.</li>
     * <li>Then depending on the <code>rowAction</code> specified it will:
     *     <ul><li>either call <code>createNewEntry()</code> (<code>
     *         rowAction = <i>createAndGo</i> or <i>createAndWait</i>
     *         </code>),</li>
     *     <li>or call <code>checkRemoveTableRow()</code> (<code>
     *         rowAction = <i>destroy</i></code>),</li>
     *     <li>or call <code>checkRowStatusChange()</code> (<code>
     *         rowAction = <i>active</i> or <i>notInService</i></code>),</li>
     *     <li>or generate a SnmpStatusException if the passed <code>
     *         rowAction</code> is not correct.</li>
     * </ul></li></ul>
     * <p>
     * In principle, you should not need to redefine this method.
     * <p>
     * <code>beginRowAction()</code> is called during the check phase
     * of a SET request, before actual checking on the varbind list
     * is performed.
     * 
     * <p>
     * @param req    The sub-request that must be handled by this node.
     *
     * @param rowOid The <CODE>SnmpOid</CODE> identifying the table
     *               row involved in the operation.
     *
     * @param depth  The depth reached in the OID tree.
     *
     * @param rowAction The requested action as returned by <code>
     *        getRowAction()</code>: one of the RowStatus codes defined in 
     *        {@link com.sun.jmx.snmp.EnumRowStatus}. These codes 
     *        correspond to RowStatus codes as defined in RFC 2579, 
     *        plus the <i>unspecified</i> value which is SNMP Runtime specific.
     *        
     * @exception SnmpStatusException if the specified <code>rowAction</code>
     *            is not valid or cannot be executed.
     *            This should not happen since it would break the 
     *            atomicity of the SET request. Specific checks should
     *            be implemented in <code>beginRowAction()</code> if needed.
     *
     * @see com.sun.jmx.snmp.EnumRowStatus
     **/
    protected synchronized void beginRowAction(SnmpMibSubRequest req, SnmpOid rowOid, int depth, int rowAction) throws SnmpStatusException {
        final boolean isnew = req.isNewEntry();
        final SnmpOid oid = rowOid;
        final int action = rowAction;
        switch(action) {
            case EnumRowStatus.unspecified:
                if (isnew) {
                    if (isDebugOn()) debug("beginRowAction", "Failed to create row[" + rowOid + "] : RowStatus = unspecified");
                    checkRowStatusFail(req, SnmpStatusException.snmpRspNoAccess);
                }
                break;
            case EnumRowStatus.createAndGo:
            case EnumRowStatus.createAndWait:
                if (isnew) {
                    if (isCreationEnabled()) {
                        if (isDebugOn()) debug("beginRowAction", "Creating row[" + rowOid + "] : RowStatus = createAndGo | createAndWait");
                        createNewEntry(req, oid, depth);
                    } else {
                        if (isDebugOn()) debug("beginRowAction", "Can't create row[" + rowOid + "] : RowStatus = createAndGo | createAndWait" + " but creation is disabled");
                        checkRowStatusFail(req, SnmpStatusException.snmpRspNoAccess);
                    }
                } else {
                    if (isDebugOn()) debug("beginRowAction", "Can't create row[" + rowOid + "] : RowStatus = createAndGo | createAndWait" + " but row already exists");
                    checkRowStatusFail(req, SnmpStatusException.snmpRspInconsistentValue);
                }
                break;
            case EnumRowStatus.destroy:
                if (isnew) {
                    if (isDebugOn()) debug("beginRowAction", "Warning: can't destroy row[" + rowOid + "] : RowStatus = destroy" + " but row does not exist");
                } else if (!isCreationEnabled()) {
                    if (isDebugOn()) debug("beginRowAction", "Can't destroy row[" + rowOid + "] : RowStatus = destroy " + " but creation is disabled");
                    checkRowStatusFail(req, SnmpStatusException.snmpRspNoAccess);
                }
                checkRemoveTableRow(req, rowOid, depth);
                break;
            case EnumRowStatus.active:
            case EnumRowStatus.notInService:
                if (isnew) {
                    if (isDebugOn()) debug("beginRowAction", "Can't switch state of row[" + rowOid + "] : specified RowStatus = active | notInService" + " but row does not exist");
                    checkRowStatusFail(req, SnmpStatusException.snmpRspInconsistentValue);
                }
                checkRowStatusChange(req, rowOid, depth, action);
                break;
            case EnumRowStatus.notReady:
            default:
                if (isDebugOn()) debug("beginRowAction", "Invalid RowStatus value for row[" + rowOid + "] : specified RowStatus = " + action);
                checkRowStatusFail(req, SnmpStatusException.snmpRspInconsistentValue);
        }
    }

    /**
     * This method takes care of final RowStatus handling during the
     * set() phase of a SET request.
     *
     * In particular it will:
     *     <ul><li>either call <code>setRowStatus(<i>active</i>)</code>
     *         (<code> rowAction = <i>createAndGo</i> or <i>active</i>
     *         </code>),</li>
     *     <li>or call <code>setRowStatus(<i>notInService</i> or <i>
     *         notReady</i>)</code> depending on the result of <code>
     *         isRowReady()</code> (<code>rowAction = <i>createAndWait</i>
     *         </code>),</li>
     *     <li>or call <code>setRowStatus(<i>notInService</i>)</code> 
     *         (<code> rowAction = <i>notInService</i></code>),
     *     <li>or call <code>removeTableRow()</code> (<code>
     *         rowAction = <i>destroy</i></code>),</li>
     *     <li>or generate a SnmpStatusException if the passed <code>
     *         rowAction</code> is not correct. This should be avoided
     *         since it would break SET request atomicity</li>
     *     </ul>
     * <p>
     * In principle, you should not need to redefine this method.
     * <p>
     * <code>endRowAction()</code> is called during the set() phase
     * of a SET request, after the actual set() on the varbind list
     * has been performed. The varbind containing the control variable
     * is updated with the value returned by setRowStatus() (if it is
     * not <code>null</code>).
     * 
     * <p>
     * @param req    The sub-request that must be handled by this node.
     *
     * @param rowOid The <CODE>SnmpOid</CODE> identifying the table
     *               row involved in the operation.
     *
     * @param depth  The depth reached in the OID tree.
     *
     * @param rowAction The requested action as returned by <code>
     *        getRowAction()</code>: one of the RowStatus codes defined in 
     *        {@link com.sun.jmx.snmp.EnumRowStatus}. These codes 
     *        correspond to RowStatus codes as defined in RFC 2579, 
     *        plus the <i>unspecified</i> value which is SNMP Runtime specific.
     *        
     * @exception SnmpStatusException if the specified <code>rowAction</code>
     *            is not valid.
     *
     * @see com.sun.jmx.snmp.EnumRowStatus
     **/
    protected void endRowAction(SnmpMibSubRequest req, SnmpOid rowOid, int depth, int rowAction) throws SnmpStatusException {
        final boolean isnew = req.isNewEntry();
        final SnmpOid oid = rowOid;
        final int action = rowAction;
        final Object data = req.getUserData();
        SnmpValue value = null;
        switch(action) {
            case EnumRowStatus.unspecified:
                break;
            case EnumRowStatus.createAndGo:
                if (isDebugOn()) debug("endRowAction", "Setting RowStatus to `active'" + " for row[" + rowOid + "] : requested RowStatus = " + "createAndGo");
                value = setRowStatus(oid, EnumRowStatus.active, data);
                break;
            case EnumRowStatus.createAndWait:
                if (isRowReady(oid, data)) {
                    if (isDebugOn()) debug("endRowAction", "Setting RowStatus to `notInService'" + " for row[" + rowOid + "] : requested RowStatus = " + "createAndWait");
                    value = setRowStatus(oid, EnumRowStatus.notInService, data);
                } else {
                    if (isDebugOn()) debug("endRowAction", "Setting RowStatus to `notReady'" + " for row[" + rowOid + "] : requested RowStatus = " + "createAndWait");
                    value = setRowStatus(oid, EnumRowStatus.notReady, data);
                }
                break;
            case EnumRowStatus.destroy:
                if (isnew) {
                    if (isDebugOn()) debug("endRowAction", "Warning: " + " requested RowStatus = destroy," + "but row[" + rowOid + "] does not exist.");
                } else {
                    if (isDebugOn()) debug("endRowAction", "destroying row[" + rowOid + "] : requested RowStatus = destroy");
                }
                removeTableRow(req, oid, depth);
                break;
            case EnumRowStatus.active:
                if (isDebugOn()) debug("endRowAction", "Setting RowStatus to `active'" + " for row[" + rowOid + "] : requested RowStatus = " + "active");
                value = setRowStatus(oid, EnumRowStatus.active, data);
                break;
            case EnumRowStatus.notInService:
                if (isDebugOn()) debug("endRowAction", "Setting RowStatus to `notInService'" + " for row[" + rowOid + "] : requested RowStatus = " + "notInService");
                value = setRowStatus(oid, EnumRowStatus.notInService, data);
                break;
            case EnumRowStatus.notReady:
            default:
                if (isDebugOn()) debug("endRowAction", "Invalid RowStatus value for row[" + rowOid + "] : specified RowStatus = " + action);
                setRowStatusFail(req, SnmpStatusException.snmpRspInconsistentValue);
        }
        if (value != null) {
            final SnmpVarBind vb = req.getRowStatusVarBind();
            if (vb != null) vb.value = value;
        }
    }

    /**
     * Return the next OID arc corresponding to a readable columnar 
     * object in the underlying entry OBJECT-TYPE, possibly skipping over
     * those objects that must not or cannot be returned.
     * Calls {@link 
     * #getNextVarEntryId(com.sun.jmx.snmp.SnmpOid,long,java.lang.Object)},
     * until
     * {@link #skipEntryVariable(com.sun.jmx.snmp.SnmpOid,long,
     * java.lang.Object,int)} returns false.
     *
     * 
     * @param rowOid The OID index of the row involved in the operation.
     *               
     * @param var Id of the variable we start from, looking for the next.
     *
     * @param userData A contextual object containing user-data.
     *        This object is allocated through the <code>
     *        {@link com.sun.jmx.snmp.agent.SnmpUserDataFactory}</code>
     *        for each incoming SNMP request.
     *
     * @param pduVersion Protocol version of the original request PDU.
     *
     * @return The next columnar object id which can be returned using
     *         the given PDU's protocol version.
     *
     * @exception SnmpStatusException If no id is found after the given id.
     *
     **/
    protected long getNextVarEntryId(SnmpOid rowOid, long var, Object userData, int pduVersion) throws SnmpStatusException {
        long varid = var;
        do {
            varid = getNextVarEntryId(rowOid, varid, userData);
        } while (skipEntryVariable(rowOid, varid, userData, pduVersion));
        return varid;
    }

    /**
     * Hook for subclasses. 
     * The default implementation of this method is to always return
     * false. Subclasses should redefine this method so that it returns
     * true when:
     * <ul><li>the variable is a leaf that is not instantiated,</li>
     * <li>or the variable is a leaf whose type cannot be returned by that
     *     version of the protocol (e.g. an Counter64 with SNMPv1).</li>
     * </ul>
     *
     * @param rowOid The OID index of the row involved in the operation.
     *               
     * @param var Id of the variable we start from, looking for the next.
     *
     * @param userData A contextual object containing user-data.
     *        This object is allocated through the <code>
     *        {@link com.sun.jmx.snmp.agent.SnmpUserDataFactory}</code>
     *        for each incoming SNMP request.
     *
     * @param pduVersion Protocol version of the original request PDU.
     *
     * @return true if the variable must be skipped by the get-next
     *         algorithm.
     */
    protected boolean skipEntryVariable(SnmpOid rowOid, long var, Object userData, int pduVersion) {
        return false;
    }

    /**
     * Get the <CODE>SnmpOid</CODE> index of the row that follows 
     * the given <CODE>oid</CODE> in the table. The given <CODE>
     * oid</CODE> does not need to be a valid row OID index.
     *
     * <p>
     * @param oid The OID from which the search will begin.
     *
     * @param userData A contextual object containing user-data.
     *        This object is allocated through the <code>
     *        {@link com.sun.jmx.snmp.agent.SnmpUserDataFactory}</code>
     *        for each incoming SNMP request.
     *
     * @return The next <CODE>SnmpOid</CODE> index.
     *
     * @exception SnmpStatusException There is no index following the 
     *     specified <CODE>oid</CODE> in the table.
     */
    protected SnmpOid getNextOid(SnmpOid oid, Object userData) throws SnmpStatusException {
        if (size == 0) throw noSuchInstanceException;
        final SnmpOid resOid = oid;
        SnmpOid last = tableoids[tablecount - 1];
        if (last.equals(resOid)) {
            throw noSuchInstanceException;
        }
        final int newPos = getInsertionPoint(resOid, false);
        if (newPos > -1 && newPos < size) {
            try {
                last = tableoids[newPos];
            } catch (ArrayIndexOutOfBoundsException e) {
                throw noSuchInstanceException;
            }
        } else {
            throw noSuchInstanceException;
        }
        return last;
    }

    /**
     * Return the first entry OID registered in the table.
     * 
     * <p>
     * @param userData A contextual object containing user-data.
     *        This object is allocated through the <code>
     *        {@link com.sun.jmx.snmp.agent.SnmpUserDataFactory}</code>
     *        for each incoming SNMP request.
     *
     * @return The <CODE>SnmpOid</CODE> of the first entry in the table.
     * 
     * @exception SnmpStatusException If the table is empty.
     */
    protected SnmpOid getNextOid(Object userData) throws SnmpStatusException {
        if (size == 0) throw noSuchInstanceException;
        return tableoids[0];
    }

    /**
     * This method is used internally and is implemented by the 
     * <CODE>SnmpMibTable</CODE> subclasses generated by <CODE>mibgen</CODE>.
     *
     * <p> Return the next OID arc corresponding to a readable columnar 
     *     object in the underlying entry OBJECT-TYPE.</p> 
     *
     * <p>
     * @param rowOid The OID index of the row involved in the operation.
     *               
     * @param var Id of the variable we start from, looking for the next.
     *
     * @param userData A contextual object containing user-data.
     *        This object is allocated through the <code>
     *        {@link com.sun.jmx.snmp.agent.SnmpUserDataFactory}</code>
     *        for each incoming SNMP request.
     *
     * @return The next columnar object id.
     *
     * @exception SnmpStatusException If no id is found after the given id.
     *
     **/
    protected abstract long getNextVarEntryId(SnmpOid rowOid, long var, Object userData) throws SnmpStatusException;

    /**
     * This method is used internally and is implemented by the 
     * <CODE>SnmpMibTable</CODE> subclasses generated by <CODE>mibgen</CODE>.
     *
     * <p>
     * @param rowOid The OID index of the row involved in the operation.
     *               
     * @param var The var we want to validate.
     *
     * @param userData A contextual object containing user-data.
     *        This object is allocated through the <code>
     *        {@link com.sun.jmx.snmp.agent.SnmpUserDataFactory}</code>
     *        for each incoming SNMP request.
     *
     * @exception SnmpStatusException If this id is not valid.
     *
     */
    protected abstract void validateVarEntryId(SnmpOid rowOid, long var, Object userData) throws SnmpStatusException;

    /**
     *
     * This method is used internally and is implemented by the 
     * <CODE>SnmpMibTable</CODE> subclasses generated by <CODE>mibgen</CODE>.
     *
     * <p>
     * @param rowOid The OID index of the row involved in the operation.
     *               
     * @param var The OID arc.
     *
     * @param userData A contextual object containing user-data.
     *        This object is allocated through the <code>
     *        {@link com.sun.jmx.snmp.agent.SnmpUserDataFactory}</code>
     *        for each incoming SNMP request.
     *
     * @exception SnmpStatusException If this id is not valid.
     *
     */
    protected abstract boolean isReadableEntryId(SnmpOid rowOid, long var, Object userData) throws SnmpStatusException;

    /**
     * This method is used internally and is implemented by the 
     * <CODE>SnmpMibTable</CODE> subclasses generated by <CODE>mibgen</CODE>.
     */
    protected abstract void get(SnmpMibSubRequest req, SnmpOid rowOid, int depth) throws SnmpStatusException;

    /**
     * This method is used internally and is implemented by the 
     * <CODE>SnmpMibTable</CODE> subclasses generated by <CODE>mibgen</CODE>.
     */
    protected abstract void check(SnmpMibSubRequest req, SnmpOid rowOid, int depth) throws SnmpStatusException;

    /**
     * This method is used internally and is implemented by the 
     * <CODE>SnmpMibTable</CODE> subclasses generated by <CODE>mibgen</CODE>.
     */
    protected abstract void set(SnmpMibSubRequest req, SnmpOid rowOid, int depth) throws SnmpStatusException;

    /**
     * Get the <CODE>SnmpOid</CODE> index of the row that follows the 
     * index extracted from the specified OID array. 
     * Builds the SnmpOid corresponding to the row OID and calls 
     * <code>getNextOid(oid,userData)</code>;
     *
     * <p>
     * @param oid The OID array.
     *
     * @param pos The position in the OID array at which the index starts.
     *
     * @param userData A contextual object containing user-data.
     *        This object is allocated through the <code>
     *        {@link com.sun.jmx.snmp.agent.SnmpUserDataFactory}</code>
     *        for each incoming SNMP request.
     *
     * @return The next <CODE>SnmpOid</CODE>.
     *
     * @exception SnmpStatusException There is no index following the 
     *     specified one in the table.
     */
    SnmpOid getNextOid(long[] oid, int pos, Object userData) throws SnmpStatusException {
        final SnmpOid resOid = new SnmpEntryOid(oid, pos);
        return getNextOid(resOid, userData);
    }

    static final void checkRowStatusFail(SnmpMibSubRequest req, int errorStatus) throws SnmpStatusException {
        final SnmpVarBind statusvb = req.getRowStatusVarBind();
        final SnmpStatusException x = new SnmpStatusException(errorStatus);
        req.registerCheckException(statusvb, x);
    }

    static final void setRowStatusFail(SnmpMibSubRequest req, int errorStatus) throws SnmpStatusException {
        final SnmpVarBind statusvb = req.getRowStatusVarBind();
        final SnmpStatusException x = new SnmpStatusException(errorStatus);
        req.registerSetException(statusvb, x);
    }

    final synchronized void findHandlingNode(SnmpVarBind varbind, long[] oid, int depth, SnmpRequestTree handlers) throws SnmpStatusException {
        final int length = oid.length;
        if (handlers == null) throw new SnmpStatusException(SnmpStatusException.snmpRspGenErr);
        if (depth >= length) throw new SnmpStatusException(SnmpStatusException.noAccess);
        if (oid[depth] != nodeId) throw new SnmpStatusException(SnmpStatusException.noAccess);
        if (depth + 2 >= length) throw new SnmpStatusException(SnmpStatusException.noAccess);
        final SnmpOid entryoid = new SnmpEntryOid(oid, depth + 2);
        final Object data = handlers.getUserData();
        final boolean hasEntry = contains(entryoid, data);
        if (!hasEntry) {
            if (!handlers.isCreationAllowed()) throw noSuchInstanceException; else if (!isCreationEnabled()) throw new SnmpStatusException(SnmpStatusException.snmpRspNoAccess);
        }
        final long var = oid[depth + 1];
        if (hasEntry) {
            validateVarEntryId(entryoid, var, data);
        }
        if (handlers.isSetRequest() && isRowStatus(entryoid, var, data)) handlers.add(this, depth, entryoid, varbind, (!hasEntry), varbind); else handlers.add(this, depth, entryoid, varbind, (!hasEntry));
    }

    final synchronized long[] findNextHandlingNode(SnmpVarBind varbind, long[] oid, int pos, int depth, SnmpRequestTree handlers, AcmChecker checker) throws SnmpStatusException {
        int length = oid.length;
        if (handlers == null) throw noSuchObjectException;
        final Object data = handlers.getUserData();
        final int pduVersion = handlers.getRequestPduVersion();
        long var = -1;
        if (pos >= length) {
            oid = new long[1];
            oid[0] = nodeId;
            pos = 0;
            length = 1;
        } else if (oid[pos] > nodeId) {
            throw noSuchObjectException;
        } else if (oid[pos] < nodeId) {
            oid = new long[1];
            oid[0] = nodeId;
            pos = 0;
            length = 0;
        } else if ((pos + 1) < length) {
            var = oid[pos + 1];
        }
        SnmpOid entryoid = null;
        if (pos == (length - 1)) {
            entryoid = getNextOid(data);
            var = getNextVarEntryId(entryoid, var, data, pduVersion);
        } else if (pos == (length - 2)) {
            entryoid = getNextOid(data);
            if (skipEntryVariable(entryoid, var, data, pduVersion)) {
                var = getNextVarEntryId(entryoid, var, data, pduVersion);
            }
        } else {
            try {
                entryoid = getNextOid(oid, pos + 2, data);
                if (skipEntryVariable(entryoid, var, data, pduVersion)) throw noSuchObjectException;
            } catch (SnmpStatusException se) {
                entryoid = getNextOid(data);
                var = getNextVarEntryId(entryoid, var, data, pduVersion);
            }
        }
        return findNextAccessibleOid(entryoid, varbind, oid, depth, handlers, checker, data, var);
    }

    private long[] findNextAccessibleOid(SnmpOid entryoid, SnmpVarBind varbind, long[] oid, int depth, SnmpRequestTree handlers, AcmChecker checker, Object data, long var) throws SnmpStatusException {
        final int pduVersion = handlers.getRequestPduVersion();
        while (true) {
            if (entryoid == null || var == -1) throw noSuchObjectException;
            try {
                if (!isReadableEntryId(entryoid, var, data)) throw noSuchObjectException;
                final long[] etable = entryoid.longValue(false);
                final int elength = etable.length;
                final long[] result = new long[depth + 2 + elength];
                result[0] = -1;
                java.lang.System.arraycopy(etable, 0, result, depth + 2, elength);
                result[depth] = nodeId;
                result[depth + 1] = var;
                checker.add(depth, result, depth, elength + 2);
                try {
                    checker.checkCurrentOid();
                    handlers.add(this, depth, entryoid, varbind, false);
                    return result;
                } catch (SnmpStatusException e) {
                    entryoid = getNextOid(entryoid, data);
                } finally {
                    checker.remove(depth, elength + 2);
                }
            } catch (SnmpStatusException e) {
                entryoid = getNextOid(data);
                var = getNextVarEntryId(entryoid, var, data, pduVersion);
            }
            if (entryoid == null || var == -1) throw noSuchObjectException;
        }
    }

    /**
     * Validate the specified OID.
     *
     * <p>
     * @param oid The OID array.
     *
     * @param pos The position in the array.
     *
     * @exception SnmpStatusException If the validation fails.
     */
    final void validateOid(long[] oid, int pos) throws SnmpStatusException {
        final int length = oid.length;
        if (pos + 2 >= length) throw noSuchInstanceException;
        if (oid[pos] != nodeId) throw noSuchObjectException;
    }

    /**
     * Enable this <CODE>SnmpMibTable</CODE> to send a notification.
     *   
     * <p>
     * @param notification The notification to send.
     */
    private synchronized void sendNotification(Notification notification) {
        for (java.util.Enumeration k = handbackTable.keys(); k.hasMoreElements(); ) {
            NotificationListener listener = (NotificationListener) k.nextElement();
            java.util.Vector handbackList = (java.util.Vector) handbackTable.get(listener);
            java.util.Vector filterList = (java.util.Vector) filterTable.get(listener);
            java.util.Enumeration f = filterList.elements();
            for (java.util.Enumeration h = handbackList.elements(); h.hasMoreElements(); ) {
                Object handback = h.nextElement();
                NotificationFilter filter = (NotificationFilter) f.nextElement();
                if ((filter == null) || ((filter != null) && (filter.isNotificationEnabled(notification)))) {
                    listener.handleNotification(notification, handback);
                }
            }
        }
    }

    /**
     * This method is used by the SnmpMibTable to create and send a table 
     * entry notification to all the listeners registered for this kind of 
     * notification.
     *
     * <p>
     * @param type The notification type.
     *
     * @param timeStamp The notification emission date.
     *
     * @param entry The entry object.
     */
    private void sendNotification(String type, long timeStamp, Object entry, ObjectName name) {
        synchronized (this) {
            sequenceNumber = sequenceNumber + 1;
        }
        SnmpTableEntryNotification notif = new SnmpTableEntryNotification(type, this, sequenceNumber, timeStamp, entry, name);
        this.sendNotification(notif);
    }

    /**
     * Return true if the entry identified by the given OID index
     * is contained in this table.
     * <p>
     * <b>Do not call this method directly</b>. 
     * <p>
     * This method is provided has a hook for subclasses. 
     * It is called when a get/set request is received in order to
     * determine whether the specified entry is contained in the table.
     * You may want to override this method if you need to perform e.g. 
     * lazy evaluation of tables (you need to update the table when a
     * request is received) or if your table is virtual.
     * <p>
     * Note that this method is called by the Runtime from within a
     * synchronized block.
     *
     * @param oid The index part of the OID we're looking for.
     * @param userData A contextual object containing user-data.
     *        This object is allocated through the <code>
     *        {@link com.sun.jmx.snmp.agent.SnmpUserDataFactory}</code>
     *        for each incoming SNMP request.
     * 
     * @return <code>true</code> if the entry is found, <code>false</code> 
     *         otherwise.
     *
     * @since 1.5
     **/
    protected boolean contains(SnmpOid oid, Object userData) {
        return (findObject(oid) > -1);
    }

    /**
     * Look for the given oid in the OID table (tableoids) and returns
     * its position.
     *
     * <p>
     * @param oid The OID we're looking for.
     *
     * @return The position of the OID in the table. -1 if the given
     *         OID was not found.
     *
     **/
    private final int findObject(SnmpOid oid) {
        int low = 0;
        int max = size - 1;
        SnmpOid pos;
        int comp;
        int curr = low + (max - low) / 2;
        while (low <= max) {
            pos = tableoids[curr];
            comp = oid.compareTo(pos);
            if (comp == 0) return curr;
            if (oid.equals(pos) == true) {
                return curr;
            }
            if (comp > 0) {
                low = curr + 1;
            } else {
                max = curr - 1;
            }
            curr = low + (max - low) / 2;
        }
        return -1;
    }

    /**
     * Search the position at which the given oid should be inserted
     * in the OID table (tableoids).
     *
     * <p>
     * @param oid The OID we would like to insert.
     *
     * @return The position at which the OID should be inserted in 
     *         the table.
     *
     * @exception SnmpStatusException if the OID is already present in the
     *            table.
     *
     **/
    private final int getInsertionPoint(SnmpOid oid) throws SnmpStatusException {
        return getInsertionPoint(oid, true);
    }

    /**
     * Search the position at which the given oid should be inserted
     * in the OID table (tableoids).
     *
     * <p>
     * @param oid The OID we would like to insert.
     *
     * @param fail Tells whether a SnmpStatusException must be generated
     *             if the given OID is already present in the table.
     *
     * @return The position at which the OID should be inserted in 
     *         the table. When the OID is found, it returns the next
     *         position. Note that it is not valid to insert twice the
     *         same OID. This feature is only an optimization to improve
     *         the getNextOid() behaviour.
     *
     * @exception SnmpStatusException if the OID is already present in the
     *            table and <code>fail</code> is <code>true</code>.
     *
     **/
    private final int getInsertionPoint(SnmpOid oid, boolean fail) throws SnmpStatusException {
        final int failStatus = SnmpStatusException.snmpRspNotWritable;
        int low = 0;
        int max = size - 1;
        SnmpOid pos;
        int comp;
        int curr = low + (max - low) / 2;
        while (low <= max) {
            pos = tableoids[curr];
            comp = oid.compareTo(pos);
            if (comp == 0) {
                if (fail) throw new SnmpStatusException(failStatus, curr); else return curr + 1;
            }
            if (comp > 0) {
                low = curr + 1;
            } else {
                max = curr - 1;
            }
            curr = low + (max - low) / 2;
        }
        return curr;
    }

    /**
     * Remove the OID located at the given position.
     *
     * <p>
     * @param pos The position at which the OID to be removed is located.
     *
     **/
    private final void removeOid(int pos) {
        if (pos >= tablecount) return;
        if (pos < 0) return;
        final int l1 = --tablecount - pos;
        tableoids[pos] = null;
        if (l1 > 0) java.lang.System.arraycopy(tableoids, pos + 1, tableoids, pos, l1);
        tableoids[tablecount] = null;
    }

    /**
     * Insert an OID at the given position.
     *
     * <p>
     * @param oid The OID to be inserted in the table 
     * @param pos The position at which the OID to be added is located.
     *
     **/
    private final void insertOid(int pos, SnmpOid oid) {
        if (pos >= tablesize || tablecount == tablesize) {
            final SnmpOid[] olde = tableoids;
            tablesize += Delta;
            tableoids = new SnmpOid[tablesize];
            if (pos > tablecount) pos = tablecount;
            if (pos < 0) pos = 0;
            final int l1 = pos;
            final int l2 = tablecount - pos;
            if (l1 > 0) java.lang.System.arraycopy(olde, 0, tableoids, 0, l1);
            if (l2 > 0) java.lang.System.arraycopy(olde, l1, tableoids, l1 + 1, l2);
        } else if (pos < tablecount) {
            java.lang.System.arraycopy(tableoids, pos, tableoids, pos + 1, tablecount - pos);
        }
        tableoids[pos] = oid;
        tablecount++;
    }

    private static final boolean isDebugOn() {
        return Trace.isSelected(Trace.LEVEL_DEBUG, Trace.INFO_ADAPTOR_SNMP);
    }

    private final void debug(String func, String info) {
        Trace.send(Trace.LEVEL_DEBUG, Trace.INFO_ADAPTOR_SNMP, getClass().getName(), func, info);
    }

    /**
     * The id of the contained entry object.
     * @serial
     */
    protected int nodeId = 1;

    /**
     * The MIB to which the metadata is linked.
     * @serial
     */
    protected SnmpMib theMib;

    /**
     * <CODE>true</CODE> if remote creation of entries via SET operations
     * is enabled.
     * [default value is <CODE>false</CODE>]
     * @serial
     */
    protected boolean creationEnabled = false;

    /**
     * The entry factory
     */
    protected SnmpTableEntryFactory factory = null;

    /**
     * The number of elements in the table.
     * @serial
     */
    private int size = 0;

    private static final int Delta = 16;

    private int tablecount = 0;

    private int tablesize = Delta;

    private SnmpOid tableoids[] = new SnmpOid[tablesize];

    /**
     * The list of entries.
     * @serial
     */
    private final Vector entries = new Vector();

    /**
     * The list of object names.
     * @serial
     */
    private final Vector entrynames = new Vector();

    /**
     * Listener hastable containing the hand-back objects.
     */
    private java.util.Hashtable handbackTable = new java.util.Hashtable();

    /**
     * Listener hastable containing the filter objects.
     */
    private java.util.Hashtable filterTable = new java.util.Hashtable();

    /**
     * SNMP table sequence number.
     * The default value is set to 0.
     */
    transient long sequenceNumber = 0;
}
