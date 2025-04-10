package org.objectstyle.cayenne;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.objectstyle.cayenne.event.EventManager;
import org.objectstyle.cayenne.graph.GraphDiff;
import org.objectstyle.cayenne.graph.GraphManager;
import org.objectstyle.cayenne.map.EntityResolver;
import org.objectstyle.cayenne.map.ObjEntity;
import org.objectstyle.cayenne.property.ClassDescriptor;
import org.objectstyle.cayenne.query.ObjectIdQuery;
import org.objectstyle.cayenne.query.Query;
import org.objectstyle.cayenne.util.EventUtil;

/**
 * A default generic implementation of ObjectContext suitable for accessing Cayenne from
 * either an ORM or a client tiers. Communicates with Cayenne via a
 * {@link org.objectstyle.cayenne.DataChannel}.
 * 
 * @since 1.2
 * @author Andrus Adamchik
 */
public class CayenneContext implements ObjectContext {

    protected transient DataChannel channel;

    protected EntityResolver entityResolver;

    CayenneContextGraphManager graphManager;

    CayenneContextGraphAction graphAction;

    CayenneContextMergeHandler mergeHandler;

    /**
     * Creates a new CayenneContext with no channel and disabled graph events.
     */
    public CayenneContext() {
        this(null);
    }

    /**
     * Creates a new CayenneContext, initializaing it with a channel instance.
     * CayenneContext created using this constructor WILL NOT broadcast graph change
     * events.
     */
    public CayenneContext(DataChannel channel) {
        this(channel, false, false);
    }

    /**
     * Creates a new CayenneContext, initializaing it with a channel. If
     * <code>graphEventsEnabled</code> is true, this context will broadcast GraphEvents
     * using ObjectContext.GRAPH_CHANGE_SUBJECT.
     */
    public CayenneContext(DataChannel channel, boolean changeEventsEnabled, boolean syncEventsEnabled) {
        this.graphAction = new CayenneContextGraphAction(this);
        this.graphManager = new CayenneContextGraphManager(this, changeEventsEnabled, syncEventsEnabled);
        setChannel(channel);
    }

    public DataChannel getChannel() {
        return channel;
    }

    /**
     * Sets the context channel, setting up a listener for channel events.
     */
    public void setChannel(DataChannel channel) {
        if (this.channel != channel) {
            if (this.mergeHandler != null) {
                this.mergeHandler.active = false;
                this.mergeHandler = null;
            }
            this.channel = channel;
            EventManager eventManager = (channel != null) ? channel.getEventManager() : null;
            if (eventManager != null) {
                this.mergeHandler = new CayenneContextMergeHandler(this);
                EventUtil.listenForChannelEvents(channel, mergeHandler);
            }
        }
    }

    /**
     * Returns true if this context posts individual object modification events. Subject
     * used for these events is <code>ObjectContext.GRAPH_CHANGED_SUBJECT</code>.
     */
    public boolean isChangeEventsEnabled() {
        return graphManager.changeEventsEnabled;
    }

    /**
     * Returns true if this context posts lifecycle events. Subjects used for these events
     * are
     * <code>ObjectContext.GRAPH_COMMIT_STARTED_SUBJECT, ObjectContext.GRAPH_COMMITTED_SUBJECT,
     * ObjectContext.GRAPH_COMMIT_ABORTED_SUBJECT, ObjectContext.GRAPH_ROLLEDBACK_SUBJECT.</code>.
     */
    public boolean isLifecycleEventsEnabled() {
        return graphManager.lifecycleEventsEnabled;
    }

    /**
     * Returns an EntityResolver that provides mapping information needed for
     * CayenneContext operation. If EntityResolver is not set, this method would obtain
     * and cache one from the underlying DataChannel.
     */
    public EntityResolver getEntityResolver() {
        if (entityResolver == null) {
            synchronized (this) {
                if (entityResolver == null) {
                    setEntityResolver(channel.getEntityResolver());
                }
            }
        }
        return entityResolver;
    }

    public void setEntityResolver(EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
    }

    public GraphManager getGraphManager() {
        return graphManager;
    }

    CayenneContextGraphManager internalGraphManager() {
        return graphManager;
    }

    CayenneContextGraphAction internalGraphAction() {
        return graphAction;
    }

    /**
     * Commits changes to uncommitted objects. First checks if there are changes in this
     * context and if any changes are detected, sends a commit message to remote Cayenne
     * service via an internal instance of CayenneConnector.
     */
    public void commitChanges() {
        doCommitChanges();
    }

    GraphDiff doCommitChanges() {
        GraphDiff commitDiff = null;
        synchronized (graphManager) {
            if (graphManager.hasChanges()) {
                graphManager.graphCommitStarted();
                try {
                    commitDiff = channel.onSync(this, graphManager.getDiffsSinceLastFlush(), DataChannel.FLUSH_CASCADE_SYNC);
                } catch (Throwable th) {
                    graphManager.graphCommitAborted();
                    if (th instanceof CayenneRuntimeException) {
                        throw (CayenneRuntimeException) th;
                    } else {
                        throw new CayenneRuntimeException("Commit error", th);
                    }
                }
                graphManager.graphCommitted(commitDiff);
            }
        }
        return commitDiff;
    }

    public void rollbackChanges() {
        synchronized (graphManager) {
            if (graphManager.hasChanges()) {
                GraphDiff diff = graphManager.getDiffs();
                graphManager.graphReverted();
                channel.onSync(this, diff, DataChannel.ROLLBACK_CASCADE_SYNC);
            }
        }
    }

    public void commitChangesToParent() {
        synchronized (graphManager) {
            if (graphManager.hasChangesSinceLastFlush()) {
                GraphDiff diff = graphManager.getDiffsSinceLastFlush();
                graphManager.graphFlushed();
                channel.onSync(this, diff, DataChannel.FLUSH_NOCASCADE_SYNC);
            }
        }
    }

    public void rollbackChangesLocally() {
        synchronized (graphManager) {
            if (graphManager.hasChanges()) {
                graphManager.graphReverted();
            }
        }
    }

    /**
     * Deletes an object locally, scheduling it for future deletion from the external data
     * store.
     */
    public void deleteObject(Persistent object) {
        new ObjectContextDeleteAction(this).performDelete(object);
    }

    /**
     * Creates and registers a new Persistent object instance.
     */
    public Persistent newObject(Class persistentClass) {
        if (persistentClass == null) {
            throw new NullPointerException("Persistent class can't be null.");
        }
        ObjEntity entity = getEntityResolver().lookupObjEntity(persistentClass);
        if (entity == null) {
            throw new CayenneRuntimeException("No entity mapped for class: " + persistentClass);
        }
        synchronized (graphManager) {
            return createNewObject(new ObjectId(entity.getName()));
        }
    }

    /**
     * Runs a query, returning result as list.
     */
    public List performQuery(Query query) {
        List result = onQuery(this, query).firstList();
        return result != null ? result : new ArrayList(1);
    }

    public QueryResponse performGenericQuery(Query query) {
        return onQuery(this, query);
    }

    QueryResponse onQuery(ObjectContext context, Query query) {
        return new CayenneContextQueryAction(this, context, query).execute();
    }

    /**
     * Converts a list of Persistent objects registered in some other ObjectContext to a
     * list of objects local to this ObjectContext.
     * <p>
     * <i>Current limitation: all objects in the source list must be either in COMMITTED
     * or in HOLLOW state.</i>
     * </p>
     */
    public Persistent localObject(ObjectId id, Persistent prototype) {
        if (id == null) {
            throw new IllegalArgumentException("Null ObjectId");
        }
        ClassDescriptor descriptor = getEntityResolver().getClassDescriptor(id.getEntityName());
        Persistent cachedObject = (Persistent) getGraphManager().getNode(id);
        if (cachedObject != null) {
            if (cachedObject != prototype && cachedObject.getPersistenceState() != PersistenceState.MODIFIED && cachedObject.getPersistenceState() != PersistenceState.DELETED) {
                descriptor.injectValueHolders(cachedObject);
                if (prototype != null && prototype.getPersistenceState() != PersistenceState.HOLLOW) {
                    descriptor.shallowMerge(prototype, cachedObject);
                    if (cachedObject.getPersistenceState() == PersistenceState.HOLLOW) {
                        cachedObject.setPersistenceState(PersistenceState.COMMITTED);
                    }
                }
            }
            return cachedObject;
        } else {
            Persistent localObject = (Persistent) descriptor.createObject();
            localObject.setObjectContext(this);
            localObject.setObjectId(id);
            getGraphManager().registerNode(id, localObject);
            if (prototype != null) {
                localObject.setPersistenceState(PersistenceState.COMMITTED);
                descriptor.injectValueHolders(localObject);
                descriptor.shallowMerge(prototype, localObject);
            } else {
                localObject.setPersistenceState(PersistenceState.HOLLOW);
            }
            return localObject;
        }
    }

    /**
     * Resolves an object if it is HOLLOW.
     */
    public void prepareForAccess(Persistent object, String property) {
        if (object.getPersistenceState() == PersistenceState.HOLLOW) {
            ObjectId gid = object.getObjectId();
            List objects = performQuery(new ObjectIdQuery(gid));
            if (objects.size() == 0) {
                throw new FaultFailureException("Error resolving fault, no matching row exists in the database for GlobalID: " + gid);
            } else if (objects.size() > 1) {
                throw new FaultFailureException("Error resolving fault, more than one row exists in the database for GlobalID: " + gid);
            }
        }
    }

    public void propertyChanged(Persistent object, String property, Object oldValue, Object newValue) {
        graphAction.handlePropertyChange(object, property, oldValue, newValue);
    }

    public Collection uncommittedObjects() {
        synchronized (graphManager) {
            return graphManager.dirtyNodes();
        }
    }

    public Collection deletedObjects() {
        synchronized (graphManager) {
            return graphManager.dirtyNodes(PersistenceState.DELETED);
        }
    }

    public Collection modifiedObjects() {
        synchronized (graphManager) {
            return graphManager.dirtyNodes(PersistenceState.MODIFIED);
        }
    }

    public Collection newObjects() {
        synchronized (graphManager) {
            return graphManager.dirtyNodes(PersistenceState.NEW);
        }
    }

    Persistent createNewObject(ObjectId id) {
        ClassDescriptor descriptor = getEntityResolver().getClassDescriptor(id.getEntityName());
        Persistent object = (Persistent) descriptor.createObject();
        object.setPersistenceState(PersistenceState.NEW);
        object.setObjectContext(this);
        object.setObjectId(id);
        descriptor.injectValueHolders(object);
        graphManager.registerNode(object.getObjectId(), object);
        graphManager.nodeCreated(object.getObjectId());
        return object;
    }

    Persistent createFault(ObjectId id) {
        ClassDescriptor descriptor = getEntityResolver().getClassDescriptor(id.getEntityName());
        Persistent object = (Persistent) descriptor.createObject();
        object.setPersistenceState(PersistenceState.HOLLOW);
        object.setObjectContext(this);
        object.setObjectId(id);
        descriptor.injectValueHolders(object);
        graphManager.registerNode(id, object);
        return object;
    }
}
