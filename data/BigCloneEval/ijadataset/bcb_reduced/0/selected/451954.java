package org.xebra.scp.db.persist;

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import javax.swing.event.EventListenerList;
import org.xebra.scp.db.exception.InvalidParameterException;
import org.xebra.scp.db.exception.PersistException;
import org.xebra.scp.db.model.HxTiObject;
import org.xebra.scp.db.sql.SqlClientContainer;
import org.xebra.scp.db.sql.XMLMethodMap;
import org.xebra.scp.db.sql.event.DatabaseAccessEvent;
import org.xebra.scp.db.sql.event.DatabaseAccessListener;

/**
 * Adds methods which simplify the development of subclasses.
 * 
 * @author Rafael Chargel
 * @version $Revision: 1.2 $
 */
abstract class AbstractPersister<T extends HxTiObject> implements Persister<T> {

    private static final EventListenerList EVENT_LIST = new EventListenerList();

    public void addDatabaseAccessListener(DatabaseAccessListener listener) {
        EVENT_LIST.add(DatabaseAccessListener.class, listener);
    }

    public void removeDatabaseAccessListener(DatabaseAccessListener listener) {
        EVENT_LIST.remove(DatabaseAccessListener.class, listener);
    }

    /**
	 * Deletes a single line item from the database.
	 * @param objectType The object type to delete.
	 * @param uid The UID of the object to delete.
	 * 
	 * @throws PersistException
	 * @throws InvalidParameterException
	 */
    void deleteByUID(Class<T> objectType, String uid) throws PersistException {
        LOG.trace("Deleting " + objectType.getSimpleName() + " by " + uid);
        DatabaseAccessEvent event = new DatabaseAccessEvent(this, objectType);
        if (uid == null || uid.length() == 0) {
            InvalidParameterException exc = new InvalidParameterException(this.getClass().getName() + ".deleteByUID(" + objectType.getName() + "): A UID must be entered to perform a delete function");
            event.setException(exc);
            event.setMessage(DatabaseAccessEvent.INVALID_PARAMETERS);
            fireDeleteEvent(event);
            throw exc;
        }
        event.setUidAccessed("UID: " + uid);
        XMLMethodMap methodMap = XMLMethodMap.getInstance();
        try {
            SqlClientContainer.sql().startTransaction();
            if (checkForGrandChildContext(objectType)) {
                LOG.trace("Deleting grandchildren");
                String xmlMethodId = methodMap.getXMLMethodId(objectType, XMLMethodMap.DELETE_GRAND_CHILD);
                LOG.trace("XML Method ID: " + xmlMethodId);
                SqlClientContainer.sql().delete(xmlMethodId, uid);
            }
            if (checkForChildContext(objectType)) {
                LOG.trace("Deleting children");
                String xmlMethodId = methodMap.getXMLMethodId(objectType, XMLMethodMap.DELETE_CHILD);
                LOG.trace("XML Method ID: " + xmlMethodId);
                SqlClientContainer.sql().delete(xmlMethodId, uid);
            }
            String xmlMethodId = methodMap.getXMLMethodId(objectType, XMLMethodMap.DELETE);
            LOG.trace("XML Method ID: " + xmlMethodId);
            SqlClientContainer.sql().delete(xmlMethodId, uid);
            SqlClientContainer.sql().commitTransaction();
            SqlClientContainer.sql().endTransaction();
            event.setMessage(DatabaseAccessEvent.ACTION_SUCCESSFULL);
            fireDeleteEvent(event);
        } catch (Throwable exc) {
            PersistException pExc = new PersistException(this.getClass().getName() + ".deleteById(" + objectType.getName() + "): " + exc.getMessage(), exc);
            event.setException(pExc);
            event.setMessage(DatabaseAccessEvent.PERSIST_ERR);
            fireDeleteEvent(event);
            throw pExc;
        }
    }

    /**
	 * Persists an object to the database.  If the object doesn't already
	 * exist it is inserted, otherwise it is simply updated.
	 * 
	 * @param objectType The type of object to persist.
	 * @param object The object to persist.
	 * 
	 * @throws PersistException Thrown if there is a sql error.
	 * @throws InvalidParameterException Thrown if the parent UID is not valid.
	 */
    void persist(Class<T> objectType, T object) throws PersistException {
        LOG.trace("Persisting " + objectType.getSimpleName());
        DatabaseAccessEvent event = new DatabaseAccessEvent(this, objectType);
        if (object == null || !object.getClass().equals(objectType) || object.getUid() == null || object.getUid().length() == 0) {
            InvalidParameterException exc = new InvalidParameterException(this.getClass().getName() + ".persist(): Parameter is null, has no UID, or is not a subclass of " + objectType.getName());
            event.setException(exc);
            event.setMessage(DatabaseAccessEvent.INVALID_PARAMETERS);
            fireWriteEvent(event);
            throw exc;
        }
        event.setUidAccessed("UID: " + object.getUid());
        if (!checkParent(object)) {
            InvalidParameterException exc = new InvalidParameterException(this.getClass().getName() + ".persist(): Parameter object has either a missing or invalid parent UID - " + object.getParentUid());
            event.setException(exc);
            event.setMessage(DatabaseAccessEvent.INVALID_PARAMETERS);
            fireWriteEvent(event);
            throw exc;
        }
        XMLMethodMap methodMap = XMLMethodMap.getInstance();
        if (checkObject(object)) {
            LOG.trace("Object exists, updating");
            try {
                String xmlMethodId = methodMap.getXMLMethodId(objectType, XMLMethodMap.PERSIST);
                LOG.trace("XML Method ID: " + xmlMethodId);
                SqlClientContainer.sql().update(xmlMethodId, object);
                event.setMessage(DatabaseAccessEvent.ACTION_SUCCESSFULL);
                fireUpdateEvent(event);
            } catch (SQLException exc) {
                PersistException pExc = new PersistException(this.getClass().getName() + ".persist(" + objectType.getName() + "): " + exc.getMessage(), exc);
                event.setException(exc);
                event.setMessage(DatabaseAccessEvent.PERSIST_ERR);
                fireUpdateEvent(event);
                throw pExc;
            } catch (Throwable exc) {
                PersistException pExc = new PersistException(this.getClass().getName() + ".persist(" + objectType.getName() + "): " + exc.getMessage(), exc);
                event.setException(exc);
                event.setMessage(DatabaseAccessEvent.PERSIST_ERR);
                fireUpdateEvent(event);
                throw pExc;
            }
        } else {
            LOG.trace("Object does not exist, inserting");
            try {
                String xmlMethodId = methodMap.getXMLMethodId(objectType, XMLMethodMap.INSERT);
                LOG.trace("XML Method ID: " + xmlMethodId);
                SqlClientContainer.sql().insert(xmlMethodId, object);
                event.setMessage(DatabaseAccessEvent.ACTION_SUCCESSFULL);
                fireInsertEvent(event);
            } catch (SQLException exc) {
                PersistException pExc = new PersistException(this.getClass().getName() + ".persist(" + objectType.getName() + "): " + exc.getMessage(), exc);
                event.setException(exc);
                event.setMessage(DatabaseAccessEvent.PERSIST_ERR);
                fireInsertEvent(event);
                throw pExc;
            } catch (Throwable exc) {
                PersistException pExc = new PersistException(this.getClass().getName() + ".persist(" + objectType.getName() + "): " + exc.getMessage(), exc);
                event.setException(exc);
                event.setMessage(DatabaseAccessEvent.PERSIST_ERR);
                fireInsertEvent(event);
                throw pExc;
            }
        }
    }

    private boolean checkForChildContext(Class objectClass) throws Exception {
        LOG.trace("Checking for child context");
        Constructor constructor = objectClass.getConstructor((Class[]) null);
        HxTiObject object = (HxTiObject) constructor.newInstance((Object[]) null);
        return object.hasParentContext();
    }

    private boolean checkForGrandChildContext(Class objectClass) throws Exception {
        LOG.trace("Checking for grandchild context");
        Constructor constructor = objectClass.getConstructor((Class[]) null);
        HxTiObject object = (HxTiObject) constructor.newInstance((Object[]) null);
        if (!object.hasParentContext()) return false;
        return object.getParentObject().hasParentContext();
    }

    private boolean checkObject(HxTiObject object) {
        LOG.trace("Checking if object exists");
        Loader loader = LoaderFactory.getLoader(object.getClass());
        try {
            loader.loadByUID(object.getUid());
        } catch (Throwable t) {
            return false;
        }
        return true;
    }

    private boolean checkParent(HxTiObject object) {
        LOG.trace("Checking for parent context");
        if (!object.hasParentContext()) {
            return true;
        }
        if (object.getParentUid() == null || object.getParentUid().length() == 0) {
            return false;
        }
        Loader loader = LoaderFactory.getLoader(object.getParentObject().getClass());
        try {
            loader.loadByUID(object.getParentUid());
        } catch (Throwable exc) {
            LOG.error("Could not find a parent for this object", exc);
            return false;
        }
        return true;
    }

    void fireDeleteEvent(DatabaseAccessEvent event) {
        Object[] listeners = EVENT_LIST.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i].equals(DatabaseAccessListener.class)) {
                DatabaseAccessListener listener = (DatabaseAccessListener) listeners[i + 1];
                listener.databaseWriteAttempt(event);
                listener.deleteAttempt(event);
                if (!event.isSuccessfull()) {
                    listener.persistErrorThrown(event);
                }
            }
        }
    }

    void fireWriteEvent(DatabaseAccessEvent event) {
        Object[] listeners = EVENT_LIST.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i].equals(DatabaseAccessListener.class)) {
                DatabaseAccessListener listener = (DatabaseAccessListener) listeners[i + 1];
                listener.databaseWriteAttempt(event);
                if (!event.isSuccessfull()) {
                    listener.persistErrorThrown(event);
                }
            }
        }
    }

    void fireInsertEvent(DatabaseAccessEvent event) {
        Object[] listeners = EVENT_LIST.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i].equals(DatabaseAccessListener.class)) {
                DatabaseAccessListener listener = (DatabaseAccessListener) listeners[i + 1];
                listener.databaseWriteAttempt(event);
                listener.insertAttempt(event);
                if (!event.isSuccessfull()) {
                    listener.persistErrorThrown(event);
                }
            }
        }
    }

    void fireUpdateEvent(DatabaseAccessEvent event) {
        Object[] listeners = EVENT_LIST.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i].equals(DatabaseAccessListener.class)) {
                DatabaseAccessListener listener = (DatabaseAccessListener) listeners[i + 1];
                listener.databaseWriteAttempt(event);
                listener.updateAttempt(event);
                if (!event.isSuccessfull()) {
                    listener.persistErrorThrown(event);
                }
            }
        }
    }
}
