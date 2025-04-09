package com.antlersoft.odb.diralloc;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;
import com.antlersoft.odb.DiskAllocatorException;
import com.antlersoft.odb.KeyGenerator;
import com.antlersoft.odb.ObjectStoreException;

class Index {

    IndexEntry entry;

    StreamPair streams;

    DirectoryAllocator manager;

    private IndexPageKey topKey;

    Index(DirectoryAllocator m, IndexEntry e, StreamPair s) {
        manager = m;
        entry = e;
        streams = s;
        topKey = new IndexPageKey(this, entry.startPageOffset, null);
    }

    IndexPage getChildPage(IndexPage parent, int index) {
        IndexPageKey key = new IndexPageKey(this, parent.nextOffsetArray[index], parent.thisPage);
        return manager.getIndexPageByKey(key);
    }

    DAKey findObject(Comparable key) throws ObjectStoreException {
        manager.startIndexPageNoFlush();
        streams.enterProtected();
        try {
            Comparable origKey = key;
            if (!entry.unique) key = new UniqueKey(key, entry.descending ? UniqueKey.largestDAKey : UniqueKey.smallestDAKey);
            FindResult fr = findKey(key);
            if (!keyMatches(fr, origKey)) return null;
            return new DAKey(fr.page.nextOffsetArray[fr.offset], fr.page.reuseArray[fr.offset]);
        } finally {
            streams.leaveProtected();
            manager.endIndexPageNoFlush();
        }
    }

    IndexIterator getIterator(Comparable key) throws ObjectStoreException {
        manager.startIndexPageNoFlush();
        streams.enterProtected();
        try {
            Comparable origKey = key;
            if (!entry.unique) key = new UniqueKey(key, entry.descending ? UniqueKey.largestDAKey : UniqueKey.smallestDAKey);
            FindResult fr = findKey(key);
            return entry.unique ? new IndexIterator(this, fr, keyMatches(fr, origKey)) : new NonUniqueIndexIterator(this, fr, keyMatches(fr, origKey), origKey);
        } finally {
            streams.leaveProtected();
            manager.endIndexPageNoFlush();
        }
    }

    /** Must be called when classChangeLock is protected */
    void removeKey(DAKey objectRef, Object toRemove) throws ObjectStoreException {
        Comparable key = entry.generator.generateKey(toRemove);
        if (key == null) return;
        if (!entry.unique) {
            key = new UniqueKey(key, objectRef);
        }
        manager.startIndexPageNoFlush();
        streams.enterCritical();
        try {
            removeKey(objectRef, key);
        } finally {
            streams.leaveCritical();
            manager.endIndexPageNoFlush();
        }
    }

    /** Must be called when classChangeLock is protected */
    void insertKey(DAKey objectRef, Object toInsert) throws ObjectStoreException {
        Comparable key = entry.generator.generateKey(toInsert);
        if (key == null) return;
        if (!entry.unique) {
            key = new UniqueKey(key, objectRef);
        }
        manager.startIndexPageNoFlush();
        streams.enterCritical();
        try {
            insertKey(objectRef, key);
        } finally {
            streams.leaveCritical();
            manager.endIndexPageNoFlush();
        }
    }

    /** Must be called when class change lock is protected */
    void updateKey(DAKey objectRef, Object oldVersion, Object newVersion) throws ObjectStoreException {
        Comparable oldKey = entry.generator.generateKey(oldVersion);
        Comparable newKey = entry.generator.generateKey(newVersion);
        if (oldKey == null && newKey == null) return;
        if (oldKey != null && newKey != null && oldKey.compareTo(newKey) == 0) return;
        manager.startIndexPageNoFlush();
        streams.enterCritical();
        try {
            if (oldKey != null) {
                Comparable removeKey = oldKey;
                if (!entry.unique) {
                    removeKey = new UniqueKey(oldKey, objectRef);
                }
                removeKey(objectRef, removeKey);
            }
            if (newKey != null) {
                if (!entry.unique) {
                    newKey = new UniqueKey(newKey, objectRef);
                }
                insertKey(objectRef, newKey);
            }
        } finally {
            streams.leaveCritical();
            manager.endIndexPageNoFlush();
        }
    }

    int binarySearch(Comparable[] a, int low, int high, Comparable key) {
        int cmp;
        while (low <= high) {
            int mid = (low + high) / 2;
            Comparable midVal = a[mid];
            cmp = midVal.compareTo(key);
            if (entry.descending) cmp = -cmp;
            if (cmp < 0) low = mid + 1; else if (cmp > 0) high = mid - 1; else return mid;
        }
        return -(low + 1);
    }

    static IndexEntry createIndexEntry(String indexName, KeyGenerator keyGen, boolean descending, boolean unique, int entriesPerPage, DirectoryAllocator manager, StreamPair streams) throws ObjectStoreException {
        IndexEntry entry = new IndexEntry();
        entry.descending = descending;
        entry.generator = keyGen;
        entry.indexName = indexName;
        entry.unique = unique;
        entry.pageCount = entriesPerPage;
        IndexPage startPage = new IndexPage();
        startPage.size = 0;
        startPage.nextOffsetArray = new int[entry.pageCount];
        startPage.modified = false;
        startPage.keyArray = new Comparable[entry.pageCount];
        startPage.reuseArray = new int[entry.pageCount];
        try {
            entry.startPageOffset = streams.writeImmovableObject(startPage, 0);
        } catch (IOException ioe) {
            throw new ObjectStoreException("I/O error creating index start page", ioe);
        } catch (DiskAllocatorException dae) {
            throw new ObjectStoreException("Error creating index start page:", dae);
        }
        entry.index = new Index(manager, entry, streams);
        startPage.thisPage = entry.index.getTopKey();
        manager.addNewIndexPageToCache(startPage);
        return entry;
    }

    IndexStatistics getStatistics() throws ObjectStoreException {
        IndexStatistics result = new IndexStatistics();
        result.m_entriesPerPage = entry.pageCount;
        try {
            streams.enterProtected();
            evaluatePageStatistics(result, entry.startPageOffset);
        } catch (ClassNotFoundException cnfe) {
            throw new ObjectStoreException("getStatistics ", cnfe);
        } catch (DiskAllocatorException dae) {
            throw new ObjectStoreException("getStatistics allocator error ", dae);
        } catch (IOException ioe) {
            throw new ObjectStoreException("getStatistic io error", ioe);
        } finally {
            streams.leaveProtected();
        }
        return result;
    }

    private void evaluatePageStatistics(IndexStatistics stats, int offset) throws IOException, DiskAllocatorException, ClassNotFoundException {
        byte[] overflowOffsetBuffer = streams.allocator.read(offset, 4);
        int overflowOffset = com.antlersoft.util.NetByte.quadToInt(overflowOffsetBuffer, 0);
        IndexPage page = (IndexPage) streams.readImmovableObject(offset);
        IndexStatistics.Counts counts = (overflowOffset == 0 ? stats.m_regular : stats.m_overflow);
        counts.m_pages++;
        counts.m_totalKeys += page.size;
        counts.m_totalSize += streams.writeObject(page).length;
        if (page.reuseArray == null) {
            for (int i = 0; i < page.size; ++i) {
                evaluatePageStatistics(stats, page.nextOffsetArray[i]);
            }
        }
    }

    void validate(Set<Integer> freeSet) throws ObjectStoreException {
        try {
            streams.enterProtected();
            validateIndexPage(freeSet, entry.startPageOffset, null);
        } catch (ClassNotFoundException cnfe) {
            throw new ObjectStoreException("validate ", cnfe);
        } catch (DiskAllocatorException dae) {
            throw new ObjectStoreException("validate allocator error ", dae);
        } catch (IOException ioe) {
            throw new ObjectStoreException("validate io error", ioe);
        } finally {
            streams.leaveProtected();
        }
    }

    private void validateIndexPage(Set<Integer> freeSet, int offset, Comparable parentKey) throws ObjectStoreException, ClassNotFoundException, DiskAllocatorException, IOException {
        IndexPage page = (IndexPage) streams.readImmovableObject(offset);
        Comparable previous = null;
        if (parentKey != null) {
            if (parentKey.compareTo(page.keyArray[0]) != 0) throw new ObjectStoreException("Key in parent page doesn't match first key in child");
        }
        for (int i = 0; i < page.size; ++i) {
            Comparable next = page.keyArray[i];
            if (previous != null) {
                if (previous.compareTo(next) >= 0) throw new ObjectStoreException("Keys in wrong order in index page");
            }
            previous = next;
            if (page.reuseArray == null) {
                validateIndexPage(freeSet, page.nextOffsetArray[i], previous);
            } else if (freeSet != null) {
                DAKey objKey = new DAKey(page.nextOffsetArray[i], page.reuseArray[i]);
                Serializable obj = manager.retrieve(objKey);
                Comparable compareKey = entry.generator.generateKey(obj);
                if (!entry.unique) {
                    compareKey = new UniqueKey(compareKey, objKey);
                }
                if (compareKey.compareTo(previous) != 0) throw new ObjectStoreException("Keys fail to compare");
                if (freeSet.contains(objKey.index)) {
                    throw new ObjectStoreException("Free set contains object index for object " + obj.toString());
                }
            }
        }
    }

    /**
     * These private methods live in a happy world where all indexes are
     * unique, no index pages are ever flushed, and no other thread is
     * accessing the index.
     */
    private void insertKey(DAKey objectRef, Comparable key) throws ObjectStoreException {
        FindResult fr = findKey(key);
        if (fr.keyMatched) throw new ObjectStoreException("Duplicate value for index: " + entry.indexName);
        if (fr.offset == 0) fixParentKey(fr.page, fr.page.keyArray[0], key);
        System.arraycopy(fr.page.nextOffsetArray, fr.offset, fr.page.nextOffsetArray, fr.offset + 1, fr.page.size - fr.offset);
        System.arraycopy(fr.page.reuseArray, fr.offset, fr.page.reuseArray, fr.offset + 1, fr.page.size - fr.offset);
        System.arraycopy(fr.page.keyArray, fr.offset, fr.page.keyArray, fr.offset + 1, fr.page.size - fr.offset);
        fr.page.size++;
        fr.page.nextOffsetArray[fr.offset] = objectRef.index;
        fr.page.reuseArray[fr.offset] = objectRef.reuseCount;
        fr.page.keyArray[fr.offset] = key;
        fr.page.modified = true;
        if (fr.page.size >= entry.pageCount) {
            splitPage(fr.page);
        }
    }

    void splitPage(IndexPage toSplit) throws ObjectStoreException {
        IndexPage parent;
        int splitParentPosition;
        if (toSplit.thisPage.parent == null) {
            parent = new IndexPage();
            parent.nextOffsetArray = new int[entry.pageCount];
            parent.keyArray = new Comparable[entry.pageCount];
            parent.reuseArray = null;
            parent.size = 1;
            parent.nextOffsetArray[0] = toSplit.thisPage.offset;
            parent.keyArray[0] = toSplit.keyArray[0];
            parent.modified = true;
            try {
                parent.thisPage = new IndexPageKey(this, streams.writeImmovableObject(parent, 0), null);
            } catch (IOException ioe) {
                throw new ObjectStoreException("I/O error creating index page", ioe);
            } catch (DiskAllocatorException dae) {
                throw new ObjectStoreException("Error creating index page:", dae);
            }
            entry.startPageOffset = parent.thisPage.offset;
            topKey = parent.thisPage;
            toSplit.thisPage.parent = parent.thisPage;
            manager.dirtyClassList();
            manager.addNewIndexPageToCache(parent);
            splitParentPosition = 1;
        } else {
            parent = manager.getIndexPageByKey(toSplit.thisPage.parent);
            splitParentPosition = binarySearch(parent.keyArray, 0, parent.size - 1, toSplit.keyArray[0]);
            if (splitParentPosition < 0) throw new ObjectStoreException("Index corrupt: splitting page");
            splitParentPosition++;
        }
        int splitPosition = toSplit.size / 2;
        IndexPage newPage = new IndexPage();
        newPage.nextOffsetArray = new int[entry.pageCount];
        System.arraycopy(toSplit.nextOffsetArray, splitPosition, newPage.nextOffsetArray, 0, toSplit.size - splitPosition);
        newPage.keyArray = new Comparable[entry.pageCount];
        System.arraycopy(toSplit.keyArray, splitPosition, newPage.keyArray, 0, toSplit.size - splitPosition);
        if (toSplit.reuseArray == null) newPage.reuseArray = null; else {
            newPage.reuseArray = new int[entry.pageCount];
            System.arraycopy(toSplit.reuseArray, splitPosition, newPage.reuseArray, 0, toSplit.size - splitPosition);
        }
        newPage.size = toSplit.size - splitPosition;
        newPage.modified = true;
        try {
            newPage.thisPage = new IndexPageKey(this, streams.writeImmovableObject(newPage, 0), toSplit.thisPage.parent);
        } catch (IOException ioe) {
            throw new ObjectStoreException("I/O error creating index page", ioe);
        } catch (DiskAllocatorException dae) {
            throw new ObjectStoreException("Error creating index page:", dae);
        }
        toSplit.size = splitPosition;
        for (int i = splitPosition; i < entry.pageCount; i++) {
            toSplit.keyArray[i] = null;
            toSplit.nextOffsetArray[i] = 0;
            if (toSplit.reuseArray != null) toSplit.reuseArray[i] = 0;
        }
        manager.addNewIndexPageToCache(newPage);
        System.arraycopy(parent.nextOffsetArray, splitParentPosition, parent.nextOffsetArray, splitParentPosition + 1, parent.size - splitParentPosition);
        System.arraycopy(parent.keyArray, splitParentPosition, parent.keyArray, splitParentPosition + 1, parent.size - splitParentPosition);
        parent.size++;
        parent.nextOffsetArray[splitParentPosition] = newPage.thisPage.offset;
        parent.keyArray[splitParentPosition] = newPage.keyArray[0];
        parent.modified = true;
        if (newPage.reuseArray == null) {
            manager.fixIndexPageParent(newPage.thisPage, newPage.nextOffsetArray, 0, newPage.size);
        }
        if (parent.size >= entry.pageCount) splitPage(parent);
    }

    private void removeKey(DAKey objectRef, Comparable key) throws ObjectStoreException {
        FindResult fr = findKey(key);
        if (!fr.keyMatched) throw new ObjectStoreException("Missing value for index: " + entry.indexName);
        if (fr.offset == 0 && fr.page.thisPage.parent != null) {
            if (fr.page.size == 1) {
                emptyPage(fr.page);
                return;
            } else {
                fixParentKey(fr.page, fr.page.keyArray[0], fr.page.keyArray[1]);
            }
        }
        if (fr.offset + 1 < fr.page.size) {
            System.arraycopy(fr.page.nextOffsetArray, fr.offset + 1, fr.page.nextOffsetArray, fr.offset, fr.page.size - fr.offset - 1);
            System.arraycopy(fr.page.reuseArray, fr.offset + 1, fr.page.reuseArray, fr.offset, fr.page.size - fr.offset - 1);
            System.arraycopy(fr.page.keyArray, fr.offset + 1, fr.page.keyArray, fr.offset, fr.page.size - fr.offset - 1);
        }
        fr.page.size--;
        fr.page.keyArray[fr.page.size] = null;
        fr.page.modified = true;
        if (fr.page.size < entry.pageCount / 4) {
            mergePage(fr.page);
        }
    }

    /**
     * Fix key value in parent pages, recursively, when initial value
     * in a child page changes
     */
    private void fixParentKey(IndexPage newInitial, Comparable oldStartValue, Comparable newStartValue) throws ObjectStoreException {
        if (newInitial.thisPage.parent != null) {
            IndexPage parent = manager.getIndexPageByKey(newInitial.thisPage.parent);
            int parentPosition = binarySearch(parent.keyArray, 0, parent.size - 1, oldStartValue);
            if (parentPosition < 0) throw new ObjectStoreException("Index corrupt when changing initial value");
            parent.keyArray[parentPosition] = newStartValue;
            parent.modified = true;
            if (parentPosition == 0) {
                fixParentKey(parent, oldStartValue, newStartValue);
            }
        }
    }

    /**
     * Called when a removal would make an IndexPage empty.  This can
     * never be called on the top page, because it is never called by
     * removeKey for a page that is top, and a non-leaf
     * top page is removed
     * by mergePage when it gets to a size of 1.
     */
    private void emptyPage(IndexPage toEmpty) throws ObjectStoreException {
        IndexPage parent = manager.getIndexPageByKey(toEmpty.thisPage.parent);
        manager.freeIndexPage(toEmpty);
        if (parent.size == 1) {
            emptyPage(parent);
        } else {
            int parentPosition = binarySearch(parent.keyArray, 0, parent.size - 1, toEmpty.keyArray[0]);
            if (parentPosition < 0) throw new ObjectStoreException("Index corrupt in emptyPage");
            if (parentPosition == 0) fixParentKey(parent, parent.keyArray[0], parent.keyArray[1]);
            if (parentPosition + 1 < parent.size) {
                System.arraycopy(parent.nextOffsetArray, parentPosition + 1, parent.nextOffsetArray, parentPosition, parent.size - parentPosition - 1);
                System.arraycopy(parent.reuseArray, parentPosition + 1, parent.reuseArray, parentPosition, parent.size - parentPosition - 1);
                System.arraycopy(parent.keyArray, parentPosition + 1, parent.keyArray, parentPosition, parent.size - parentPosition - 1);
            }
            parent.size--;
            parent.keyArray[parent.size] = null;
            parent.modified = true;
            if (parent.size < entry.pageCount / 4) {
                mergePage(parent);
            }
        }
    }

    /**
     * Called whenever a removal makes an index page smaller than the
     * low water mark (pageCount/4).  This will recursively merge pages
     * up the tree if the situation warrants it.  If the top page is non-leaf
     * and gets to a size of one, it is removed.  This is the only way levels
     * are removed from the tree.  In particular, a non-leaf page that is
     * not the top page and is size 1 is not removed until there are zero
     * index entries beneath it (by empty page).
     */
    private void mergePage(IndexPage toMerge) throws ObjectStoreException {
        if (toMerge.thisPage.parent == null) {
            if (toMerge.size == 1 && toMerge.reuseArray == null) {
                IndexPage newTop = getChildPage(toMerge, 0);
                entry.startPageOffset = newTop.thisPage.offset;
                topKey = newTop.thisPage;
                topKey.parent = null;
                manager.freeIndexPage(toMerge);
                manager.dirtyClassList();
                if (newTop.size == 1) mergePage(newTop);
            }
        } else {
            IndexPage parent = manager.getIndexPageByKey(toMerge.thisPage.parent);
            int parentPosition = binarySearch(parent.keyArray, 0, parent.size - 1, toMerge.keyArray[0]);
            if (parentPosition < 0) throw new ObjectStoreException("Index corrupt merging pages");
            IndexPage leftPage = null;
            if (parentPosition > 0) leftPage = getChildPage(parent, parentPosition - 1);
            IndexPage rightPage = null;
            if (parentPosition < parent.size - 1) rightPage = getChildPage(parent, parentPosition + 1);
            if (leftPage != null && rightPage != null) {
                if (leftPage.size <= rightPage.size) {
                    if (leftPage.size + toMerge.size < entry.pageCount) mergeLeft(leftPage, toMerge, parent, parentPosition);
                } else {
                    if (rightPage.size + toMerge.size < entry.pageCount) mergeLeft(toMerge, rightPage, parent, parentPosition + 1);
                }
            } else {
                if (leftPage != null) {
                    if (leftPage.size + toMerge.size < entry.pageCount) mergeLeft(leftPage, toMerge, parent, parentPosition);
                } else {
                    if (rightPage != null && rightPage.size + toMerge.size < entry.pageCount) mergeLeft(toMerge, rightPage, parent, parentPosition + 1);
                }
            }
        }
    }

    /**
     * Called from mergePage when it has figured out all the merge parameters
     * to merge two index pages
     */
    private void mergeLeft(IndexPage leftPage, IndexPage rightPage, IndexPage parent, int rightPosition) throws ObjectStoreException {
        leftPage.modified = true;
        System.arraycopy(rightPage.nextOffsetArray, 0, leftPage.nextOffsetArray, leftPage.size, rightPage.size);
        System.arraycopy(rightPage.keyArray, 0, leftPage.keyArray, leftPage.size, rightPage.size);
        if (leftPage.reuseArray != null) System.arraycopy(rightPage.reuseArray, 0, leftPage.reuseArray, leftPage.size, rightPage.size); else manager.fixIndexPageParent(leftPage.thisPage, rightPage.nextOffsetArray, 0, rightPage.size);
        leftPage.size += rightPage.size;
        manager.freeIndexPage(rightPage);
        parent.modified = true;
        if (rightPosition + 1 < parent.size) {
            System.arraycopy(parent.nextOffsetArray, rightPosition + 1, parent.nextOffsetArray, rightPosition, parent.size - rightPosition - 1);
            System.arraycopy(parent.keyArray, rightPosition + 1, parent.keyArray, rightPosition, parent.size - rightPosition - 1);
        }
        parent.size--;
        if (parent.size < entry.pageCount / 4) mergePage(parent);
    }

    private IndexPageKey getTopKey() {
        topKey.offset = entry.startPageOffset;
        return topKey;
    }

    private FindResult findKey(Comparable key) throws ObjectStoreException {
        IndexPage currentPage = manager.getIndexPageByKey(getTopKey());
        int difference;
        while (true) {
            difference = binarySearch(currentPage.keyArray, 0, currentPage.size - 1, key);
            if (currentPage.reuseArray != null) {
                return new FindResult(currentPage, difference);
            } else {
                if (difference < 0) {
                    difference = -difference - 2;
                    if (difference < 0) difference = 0;
                }
                currentPage = getChildPage(currentPage, difference);
            }
        }
    }

    private boolean keyMatches(FindResult fr, Comparable key) {
        if (entry.unique) return fr.keyMatched;
        if (fr.offset >= fr.page.size) return false;
        return key.compareTo(((UniqueKey) fr.page.keyArray[fr.offset]).base) == 0;
    }

    void moveToFirstKeyOfNextPage(FindResult fr) throws ObjectStoreException {
        IndexPage nextPage = fr.page;
        Comparable toFindInParent = fr.page.keyArray[0];
        while (nextPage.thisPage.parent != null) {
            IndexPage parent = manager.getIndexPageByKey(nextPage.thisPage.parent);
            int keyOffset = binarySearch(parent.keyArray, 0, parent.size - 1, toFindInParent);
            if (keyOffset < 0) throw new ObjectStoreException("moveToFirstKeyOfNextPage: parent page does not match child page initial key"); else keyOffset++;
            if (keyOffset >= parent.size) {
                nextPage = parent;
                toFindInParent = parent.keyArray[0];
            } else {
                nextPage = getChildPage(parent, keyOffset);
                while (nextPage.reuseArray == null) {
                    nextPage = getChildPage(nextPage, 0);
                }
                fr.page = nextPage;
                fr.offset = 0;
                fr.keyMatched = false;
                break;
            }
        }
    }

    static class FindResult {

        FindResult(IndexPage p, int d) {
            page = p;
            if (d < 0) offset = -d - 1; else offset = d;
            keyMatched = (d >= 0);
            if (offset >= page.size) {
                page.thisPage.index.moveToFirstKeyOfNextPage(this);
            }
        }

        IndexPage page;

        int offset;

        boolean keyMatched;
    }
}
