package org.objectstyle.cayenne.access;

import java.util.Iterator;
import java.util.List;
import org.objectstyle.art.Artist;
import org.objectstyle.art.Painting;
import org.objectstyle.cayenne.DataObject;
import org.objectstyle.cayenne.DataObjectUtils;
import org.objectstyle.cayenne.ObjectId;
import org.objectstyle.cayenne.PersistenceState;
import org.objectstyle.cayenne.Persistent;
import org.objectstyle.cayenne.query.SelectQuery;
import org.objectstyle.cayenne.query.ObjectIdQuery;
import org.objectstyle.cayenne.unit.CayenneTestCase;

public class NestedDataContextReadTst extends CayenneTestCase {

    public void testCreateChildDataContext() {
        DataContext parent = createDataContext();
        parent.setValidatingObjectsOnCommit(true);
        DataContext child1 = parent.createChildDataContext();
        assertNotNull(child1);
        assertSame(parent, child1.getChannel());
        assertTrue(child1.isValidatingObjectsOnCommit());
        parent.setValidatingObjectsOnCommit(false);
        DataContext child2 = parent.createChildDataContext();
        assertNotNull(child2);
        assertSame(parent, child2.getChannel());
        assertFalse(child2.isValidatingObjectsOnCommit());
        DataContext child21 = child2.createChildDataContext();
        assertNotNull(child21);
        assertSame(child2, child21.getChannel());
        assertFalse(child2.isValidatingObjectsOnCommit());
    }

    public void testLocalObjectSynchronize() throws Exception {
        deleteTestData();
        createTestData("testArtists");
        DataContext context = createDataContext();
        DataContext childContext = context.createChildDataContext();
        DataObject _new = context.createAndRegisterNewObject(Artist.class);
        Persistent hollow = context.localObject(new ObjectId("Artist", Artist.ARTIST_ID_PK_COLUMN, 33001), null);
        DataObject committed = (DataObject) DataObjectUtils.objectForQuery(context, new ObjectIdQuery(new ObjectId("Artist", Artist.ARTIST_ID_PK_COLUMN, 33002)));
        int modifiedId = 33003;
        Artist modified = (Artist) DataObjectUtils.objectForQuery(context, new ObjectIdQuery(new ObjectId("Artist", Artist.ARTIST_ID_PK_COLUMN, modifiedId)));
        modified.setArtistName("M1");
        DataObject deleted = (DataObject) DataObjectUtils.objectForQuery(context, new ObjectIdQuery(new ObjectId("Artist", Artist.ARTIST_ID_PK_COLUMN, 33004)));
        context.deleteObject(deleted);
        assertEquals(PersistenceState.HOLLOW, hollow.getPersistenceState());
        assertEquals(PersistenceState.COMMITTED, committed.getPersistenceState());
        assertEquals(PersistenceState.MODIFIED, modified.getPersistenceState());
        assertEquals(PersistenceState.DELETED, deleted.getPersistenceState());
        assertEquals(PersistenceState.NEW, _new.getPersistenceState());
        blockQueries();
        try {
            Persistent newPeer = childContext.localObject(_new.getObjectId(), _new);
            assertEquals(_new.getObjectId(), newPeer.getObjectId());
            assertEquals(PersistenceState.COMMITTED, newPeer.getPersistenceState());
            assertSame(childContext, newPeer.getObjectContext());
            assertSame(context, _new.getDataContext());
            Persistent hollowPeer = childContext.localObject(hollow.getObjectId(), hollow);
            assertEquals(PersistenceState.HOLLOW, hollowPeer.getPersistenceState());
            assertEquals(hollow.getObjectId(), hollowPeer.getObjectId());
            assertSame(childContext, hollowPeer.getObjectContext());
            assertSame(context, hollow.getObjectContext());
            Persistent committedPeer = childContext.localObject(committed.getObjectId(), committed);
            assertEquals(PersistenceState.COMMITTED, committedPeer.getPersistenceState());
            assertEquals(committed.getObjectId(), committedPeer.getObjectId());
            assertSame(childContext, committedPeer.getObjectContext());
            assertSame(context, committed.getDataContext());
            Artist modifiedPeer = (Artist) childContext.localObject(modified.getObjectId(), modified);
            assertEquals(PersistenceState.COMMITTED, modifiedPeer.getPersistenceState());
            assertEquals(modified.getObjectId(), modifiedPeer.getObjectId());
            assertEquals("M1", modifiedPeer.getArtistName());
            assertSame(childContext, modifiedPeer.getDataContext());
            assertSame(context, modified.getDataContext());
            Persistent deletedPeer = childContext.localObject(deleted.getObjectId(), deleted);
            assertEquals(PersistenceState.COMMITTED, deletedPeer.getPersistenceState());
            assertEquals(deleted.getObjectId(), deletedPeer.getObjectId());
            assertSame(childContext, deletedPeer.getObjectContext());
            assertSame(context, deleted.getDataContext());
        } finally {
            unblockQueries();
        }
    }

    public void testLocalObjectsNoOverride() throws Exception {
        deleteTestData();
        createTestData("testArtists");
        DataContext context = createDataContext();
        DataContext childContext = context.createChildDataContext();
        int modifiedId = 33003;
        Artist modified = (Artist) DataObjectUtils.objectForQuery(context, new ObjectIdQuery(new ObjectId("Artist", Artist.ARTIST_ID_PK_COLUMN, modifiedId)));
        Artist peerModified = (Artist) DataObjectUtils.objectForQuery(childContext, new ObjectIdQuery(new ObjectId("Artist", Artist.ARTIST_ID_PK_COLUMN, modifiedId)));
        modified.setArtistName("M1");
        peerModified.setArtistName("M2");
        assertEquals(PersistenceState.MODIFIED, modified.getPersistenceState());
        assertEquals(PersistenceState.MODIFIED, peerModified.getPersistenceState());
        blockQueries();
        try {
            Persistent peerModified2 = childContext.localObject(modified.getObjectId(), modified);
            assertSame(peerModified, peerModified2);
            assertEquals(PersistenceState.MODIFIED, peerModified2.getPersistenceState());
            assertEquals("M2", peerModified.getArtistName());
            assertEquals("M1", modified.getArtistName());
        } finally {
            unblockQueries();
        }
    }

    public void testSelect() throws Exception {
        deleteTestData();
        createTestData("testArtists");
        DataContext parent = createDataContext();
        DataContext child = parent.createChildDataContext();
        DataObject _new = parent.createAndRegisterNewObject(Artist.class);
        Persistent hollow = parent.localObject(new ObjectId("Artist", Artist.ARTIST_ID_PK_COLUMN, 33001), null);
        DataObject committed = (DataObject) DataObjectUtils.objectForQuery(parent, new ObjectIdQuery(new ObjectId("Artist", Artist.ARTIST_ID_PK_COLUMN, 33002)));
        int modifiedId = 33003;
        Artist modified = (Artist) DataObjectUtils.objectForQuery(parent, new ObjectIdQuery(new ObjectId("Artist", Artist.ARTIST_ID_PK_COLUMN, modifiedId)));
        modified.setArtistName("MODDED");
        DataObject deleted = (DataObject) DataObjectUtils.objectForQuery(parent, new ObjectIdQuery(new ObjectId("Artist", Artist.ARTIST_ID_PK_COLUMN, 33004)));
        parent.deleteObject(deleted);
        assertEquals(PersistenceState.HOLLOW, hollow.getPersistenceState());
        assertEquals(PersistenceState.COMMITTED, committed.getPersistenceState());
        assertEquals(PersistenceState.MODIFIED, modified.getPersistenceState());
        assertEquals(PersistenceState.DELETED, deleted.getPersistenceState());
        assertEquals(PersistenceState.NEW, _new.getPersistenceState());
        List objects = child.performQuery(new SelectQuery(Artist.class));
        assertEquals("All but NEW object must have been included", 4, objects.size());
        Iterator it = objects.iterator();
        while (it.hasNext()) {
            DataObject next = (DataObject) it.next();
            assertEquals(PersistenceState.COMMITTED, next.getPersistenceState());
            int id = DataObjectUtils.intPKForObject(next);
            if (id == modifiedId) {
                assertEquals("MODDED", next.readProperty(Artist.ARTIST_NAME_PROPERTY));
            }
        }
    }

    public void testReadToOneRelationship() throws Exception {
        deleteTestData();
        createTestData("testReadRelationship");
        DataContext parent = createDataContext();
        DataContext child = parent.createChildDataContext();
        int hollowTargetSrcId = 33001;
        int modifiedTargetSrcId = 33002;
        int deletedTargetSrcId = 33003;
        int committedTargetSrcId = 33004;
        int newTargetSrcId = 33005;
        Painting hollowTargetSrc = (Painting) DataObjectUtils.objectForPK(parent, Painting.class, hollowTargetSrcId);
        Artist hollowTarget = hollowTargetSrc.getToArtist();
        Painting modifiedTargetSrc = (Painting) DataObjectUtils.objectForPK(parent, Painting.class, modifiedTargetSrcId);
        Artist modifiedTarget = modifiedTargetSrc.getToArtist();
        modifiedTarget.setArtistName("M1");
        Painting deletedTargetSrc = (Painting) DataObjectUtils.objectForPK(parent, Painting.class, deletedTargetSrcId);
        Artist deletedTarget = deletedTargetSrc.getToArtist();
        deletedTargetSrc.setToArtist(null);
        parent.deleteObject(deletedTarget);
        Painting committedTargetSrc = (Painting) DataObjectUtils.objectForPK(parent, Painting.class, committedTargetSrcId);
        Artist committedTarget = committedTargetSrc.getToArtist();
        committedTarget.getArtistName();
        Painting newTargetSrc = (Painting) DataObjectUtils.objectForPK(parent, Painting.class, newTargetSrcId);
        Artist newTarget = (Artist) parent.createAndRegisterNewObject(Artist.class);
        newTarget.setArtistName("N1");
        newTargetSrc.setToArtist(newTarget);
        assertEquals(PersistenceState.COMMITTED, hollowTargetSrc.getPersistenceState());
        assertEquals(PersistenceState.COMMITTED, modifiedTargetSrc.getPersistenceState());
        assertEquals(PersistenceState.MODIFIED, deletedTargetSrc.getPersistenceState());
        assertEquals(PersistenceState.COMMITTED, committedTargetSrc.getPersistenceState());
        assertEquals(PersistenceState.MODIFIED, newTargetSrc.getPersistenceState());
        assertEquals(PersistenceState.HOLLOW, hollowTarget.getPersistenceState());
        assertEquals(PersistenceState.MODIFIED, modifiedTarget.getPersistenceState());
        assertEquals(PersistenceState.DELETED, deletedTarget.getPersistenceState());
        assertEquals(PersistenceState.COMMITTED, committedTarget.getPersistenceState());
        assertEquals(PersistenceState.NEW, newTarget.getPersistenceState());
        SelectQuery q = new SelectQuery(Painting.class);
        q.addOrdering(Painting.PAINTING_TITLE_PROPERTY, true);
        List childSources = child.performQuery(q);
        assertEquals(5, childSources.size());
        blockQueries();
        try {
            Painting childHollowTargetSrc = (Painting) childSources.get(0);
            assertSame(child, childHollowTargetSrc.getObjectContext());
            Artist childHollowTarget = childHollowTargetSrc.getToArtist();
            assertNotNull(childHollowTarget);
            assertEquals(PersistenceState.HOLLOW, childHollowTarget.getPersistenceState());
            assertSame(child, childHollowTarget.getObjectContext());
            Artist childModifiedTarget = ((Painting) childSources.get(1)).getToArtist();
            assertEquals(PersistenceState.COMMITTED, childModifiedTarget.getPersistenceState());
            assertSame(child, childModifiedTarget.getObjectContext());
            assertEquals("M1", childModifiedTarget.getArtistName());
            Painting childDeletedTargetSrc = (Painting) childSources.get(2);
            assertEquals(deletedTargetSrc.getObjectId(), childDeletedTargetSrc.getObjectId());
            Artist childDeletedTarget = childDeletedTargetSrc.getToArtist();
            assertNull(childDeletedTarget);
            Artist childCommittedTarget = ((Painting) childSources.get(3)).getToArtist();
            assertEquals(PersistenceState.COMMITTED, childCommittedTarget.getPersistenceState());
            assertSame(child, childCommittedTarget.getObjectContext());
            Painting childNewTargetSrc = (Painting) childSources.get(4);
            assertEquals(newTargetSrc.getObjectId(), childNewTargetSrc.getObjectId());
            Artist childNewTarget = childNewTargetSrc.getToArtist();
            assertNotNull(childNewTarget);
            assertEquals(PersistenceState.COMMITTED, childNewTarget.getPersistenceState());
            assertSame(child, childNewTarget.getObjectContext());
            assertEquals("N1", childNewTarget.getArtistName());
        } finally {
            unblockQueries();
        }
    }

    public void testPrefetchingToOne() throws Exception {
        deleteTestData();
        createTestData("testPrefetching");
        DataContext parent = createDataContext();
        DataContext child = parent.createChildDataContext();
        ObjectId prefetchedId = new ObjectId("Artist", Artist.ARTIST_ID_PK_COLUMN, new Integer(33001));
        SelectQuery q = new SelectQuery(Painting.class);
        q.addOrdering(Painting.PAINTING_TITLE_PROPERTY, true);
        q.addPrefetch(Painting.TO_ARTIST_PROPERTY);
        List results = child.performQuery(q);
        blockQueries();
        try {
            assertEquals(2, results.size());
            Iterator it = results.iterator();
            while (it.hasNext()) {
                Painting o = (Painting) it.next();
                assertEquals(PersistenceState.COMMITTED, o.getPersistenceState());
                assertSame(child, o.getObjectContext());
                Artist o1 = o.getToArtist();
                assertNotNull(o1);
                assertEquals(PersistenceState.COMMITTED, o1.getPersistenceState());
                assertSame(child, o1.getObjectContext());
                assertEquals(prefetchedId, o1.getObjectId());
            }
        } finally {
            unblockQueries();
        }
    }

    public void testPrefetchingToMany() throws Exception {
        deleteTestData();
        createTestData("testPrefetching");
        DataContext parent = createDataContext();
        DataContext child = parent.createChildDataContext();
        SelectQuery q = new SelectQuery(Artist.class);
        q.addOrdering(Artist.ARTIST_NAME_PROPERTY, true);
        q.addPrefetch(Artist.PAINTING_ARRAY_PROPERTY);
        List results = child.performQuery(q);
        blockQueries();
        try {
            Artist o1 = (Artist) results.get(0);
            assertEquals(PersistenceState.COMMITTED, o1.getPersistenceState());
            assertSame(child, o1.getObjectContext());
            List children1 = o1.getPaintingArray();
            assertEquals(2, children1.size());
            Iterator it = children1.iterator();
            while (it.hasNext()) {
                Painting o = (Painting) it.next();
                assertEquals(PersistenceState.COMMITTED, o.getPersistenceState());
                assertSame(child, o.getObjectContext());
                assertEquals(o1, o.getToArtist());
            }
            Artist o2 = (Artist) results.get(1);
            assertEquals(PersistenceState.COMMITTED, o2.getPersistenceState());
            assertSame(child, o2.getObjectContext());
            List children2 = o2.getPaintingArray();
            assertEquals(0, children2.size());
        } finally {
            unblockQueries();
        }
    }
}
