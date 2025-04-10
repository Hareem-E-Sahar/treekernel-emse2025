package javax.microedition.rms;

/**
 * This class implements the RecordEnumeration interface.
 */
class RecordEnumerationImpl implements RecordEnumeration, RecordListener {

    /** The associated record store for this enumeration */
    private RecordStore recordStore;

    /** The record filter this enumeration should use, or null if none */
    private RecordFilter filter;

    /** The record comparator this enumeration should use, or null if none */
    private RecordComparator comparator;

    /** True if this should listen to <code>recordStore</code> for changes */
    private boolean beObserver;

    /** Current pos within the enumeration */
    private int index;

    /** Array of recordId's of records included in the enumeration */
    private int[] records;

    /**
     * A constant recordId indicating the splice point between the
     * last and first records in the enumeration. Returned by
     * <code>nextElement()</code> and <code>prevElement()</code>
     * when the next or prev element does not exist.
     */
    private static final int NO_SUCH_RECORD = -1;

    /** 
     * Apps must use <code>RecordStore.enumerateRecords()</code> to get 
     * a <code>RecordEnumeration</code> object. If this constructor 
     * is not declared (as private scope), Javadoc (and Java) 
     * will assume a public constructor. 
     */
    private RecordEnumerationImpl() {
    }

    /**
     * Builds an enumeration to traverse a set of records in the
     * given record store in an optionally specified order.<p>
     *
     * The filter, if non-null, will be used to determine what
     * subset of the record store records will be used.<p>
     *
     * The comparator, if non-null, will be used to determine the
     * order in which the records are returned.<p>
     *
     * If both the filter and comparator are null, the enumeration
     * will traverse all records in the record store in an undefined
     * order. This is the most efficient way to traverse all of the
     * records in a record store.
     *
     * @param recordStore the RecordStore to enumerate.
     * @param filter if non-null, will be used to determine what
     *        subset of the record store records will be used.
     * @param comparator if non-null, will be used to determine the
     *        order in which the records are returned.
     * @param keepUpdated if true, the enumerator will keep its enumeration
     *        current with any changes in the records of the record store. 
     *        Use with caution as there are performance consequences.
     *
     * @see #rebuild
     */
    RecordEnumerationImpl(RecordStore recordStore, RecordFilter filter, RecordComparator comparator, boolean keepUpdated) {
        this.recordStore = recordStore;
        this.filter = filter;
        this.comparator = comparator;
        records = new int[0];
        keepUpdated(keepUpdated);
        if (!keepUpdated) {
            rebuild();
        }
    }

    /**
     * Returns the number of records available in this enumeration's
     * set. That is, the number of records that have matched the
     * filter criterion. Note that this forces the RecordEnumeration
     * to fully build the enumeration by applying the filter to all
     * records, which may take a non-trivial amount
     * of time if there are a lot of records in the record store.
     *
     * @return the number of records available in this enumeration's
     *         set. That is, the number of records that have matched 
     *         the filter criterion.
     */
    public synchronized int numRecords() {
        checkDestroyed();
        return records.length;
    }

    /**
     * Returns a copy of the <i>next</i> record in this enumeration,
     * where <i>next</i> is defined by the comparator and/or filter
     * supplied in the constructor of this enumerator. The byte array
     * returned is a copy of the record. Any changes made to this array
     * will NOT be reflected in the record store. After calling
     * this method, the enumeration is advanced to the next available
     * record.
     *
     * @exception InvalidRecordIDException no more records are available
     *
     * @return the next record in this enumeration.
     */
    public synchronized byte[] nextRecord() throws InvalidRecordIDException, RecordStoreNotOpenException, RecordStoreException {
        checkDestroyed();
        return recordStore.getRecord(nextRecordId());
    }

    /**
     * Returns the recordId of the <i>next</i> record in this enumeration,
     * where <i>next</i> is defined by the comparator and/or filter
     * supplied in the constructor of this enumerator. After calling
     * this method, the enumeration is advanced to the next available
     * record.
     *
     * @exception InvalidRecordIDException no more records are available.
     *
     * @return the recordId of the next record in this enumeration.
     */
    public synchronized int nextRecordId() throws InvalidRecordIDException {
        checkDestroyed();
        if (index == records.length - 1) {
            throw new InvalidRecordIDException();
        }
        if (index == NO_SUCH_RECORD) {
            index = 0;
        } else {
            index++;
        }
        return records[index];
    }

    /**
     * Returns a copy of the <i>previous</i> record in this enumeration,
     * where <i>previous</i> is defined by the comparator and/or filter
     * supplied in the constructor of this enumerator. The byte array
     * returned is a copy of the record. Any changes made to this array
     * will NOT be reflected in the record store. After calling
     * this method, the enumeration is advanced to the next (previous)
     * available record.
     *
     * @exception InvalidRecordIDException no more records are available.
     *
     * @return the previous record in this enumeration.
     */
    public synchronized byte[] previousRecord() throws InvalidRecordIDException, RecordStoreNotOpenException, RecordStoreException {
        checkDestroyed();
        return recordStore.getRecord(previousRecordId());
    }

    /**
     * Returns the recordId of the <i>previous</i> record in this enumeration,
     * where <i>previous</i> is defined by the comparator and/or filter
     * supplied in the constructor of this enumerator. After this method
     * is called, the enumeration is advanced to the next (previous)
     * available record.
     *
     * @exception InvalidRecordIDException when no more records are available.
     *
     * @return the recordId of the previous record in this enumeration.
     */
    public synchronized int previousRecordId() throws InvalidRecordIDException {
        checkDestroyed();
        if (index == 0 || records.length == 0) {
            throw new InvalidRecordIDException();
        }
        if (index == NO_SUCH_RECORD) {
            index = records.length - 1;
        } else {
            index--;
        }
        return records[index];
    }

    /**
     * Returns true if more elements exist in the <i>next</i> direction.
     *
     * @return true if more elements exist in the <i>next</i> direction.
     */
    public boolean hasNextElement() {
        checkDestroyed();
        if (recordStore.isOpen() == false) {
            return false;
        }
        return (index != records.length - 1);
    }

    /**
     * Returns true if more elements exist in the <i>previous</i> direction.
     *
     * @return true if more elements exist in the <i>previous</i> direction.
     */
    public boolean hasPreviousElement() {
        checkDestroyed();
        if (records.length == 0 || recordStore.isOpen() == false) {
            return false;
        }
        return (index != 0);
    }

    /**
     * Returns the index point of the enumeration to the beginning.
     */
    public void reset() {
        checkDestroyed();
        index = NO_SUCH_RECORD;
    }

    /**
     * Request that the enumeration be updated to reflect the current
     * record set. Useful for when an application makes a number of 
     * changes to the record store, and then wants an existing 
     * RecordEnumeration to enumerate the new changes.
     *
     * @see #keepUpdated
     */
    public void rebuild() {
        checkDestroyed();
        synchronized (recordStore.rsLock) {
            int[] tmp = recordStore.getRecordIDs();
            reFilterSort(tmp);
        }
    }

    /**
     * Used to set whether the enumeration should be registered
     * as a listener of the record store, and rebuild its internal
     * index with every record addition/deletion in the record store.
     * Note that this should be used carefully due to the potential 
     * performance cost associated with maintaining the 
     * enumeration with every change.
     *
     * @param keepUpdated if true, the enumerator will keep its enumeration
     *        current with any changes in the records of the record store.
     *        Use with caution as there are possible performance consequences.
     *        If false, the enumeration will not be kept current and may 
     *        return recordIds for records that have been deleted or miss 
     *        records that are added later. It may also return records out
     *        of order that have been modified after the enumeration was 
     *        built.
     *
     * @see #rebuild
     */
    public void keepUpdated(boolean keepUpdated) {
        checkDestroyed();
        if (keepUpdated != beObserver) {
            beObserver = keepUpdated;
            if (keepUpdated) {
                recordStore.addRecordListener(this);
                rebuild();
            } else {
                recordStore.removeRecordListener(this);
            }
        }
    }

    /**
     * Returns true if the enumeration keeps its enumeration
     * current with any changes in the records.
     *
     * @return true if the enumeration keeps its enumeration
     *         current with any changes in the records
     */
    public boolean isKeptUpdated() {
        checkDestroyed();
        return beObserver;
    }

    /**
     * From the RecordListener interface.  This method is called if
     * a record is added to <code>recordStore</code>.
     *
     * @param recordStore the record store to which a record was added
     * @param recordId the record ID of the new record
     */
    public synchronized void recordAdded(RecordStore recordStore, int recordId) {
        checkDestroyed();
        synchronized (recordStore.rsLock) {
            filterAdd(recordId);
        }
    }

    /**
     * From the RecordListener interface.  This method is called if
     * a record in <code>recordStor</code> is modified.
     *
     * @param recordStore the record store in which a record was modified
     * @param recordId the record ID of the modified record.
     */
    public synchronized void recordChanged(RecordStore recordStore, int recordId) {
        checkDestroyed();
        int recIndex = findIndexOfRecord(recordId);
        if (recIndex < 0) {
            return;
        }
        removeRecordAtIndex(recIndex);
        synchronized (recordStore.rsLock) {
            filterAdd(recordId);
        }
    }

    /**
     * From the RecordListener interface.  This method is called when a
     * record in <code>recordStore</code> is deleted.
     *
     * @param recordStore the record store from which a record was deleted
     * @param recordId the record id of the deleted record
     */
    public synchronized void recordDeleted(RecordStore recordStore, int recordId) {
        checkDestroyed();
        int recIndex = findIndexOfRecord(recordId);
        if (recIndex < 0) {
            return;
        }
        removeRecordAtIndex(recIndex);
    }

    /**
     * Implements RecordEnumeration.destroy() interface.  Called
     * to signal that this enumeration will no longer be used, and that
     * its resources may be collected.
     */
    public synchronized void destroy() {
        checkDestroyed();
        if (beObserver == true) {
            recordStore.removeRecordListener(this);
        }
        filter = null;
        comparator = null;
        records = null;
        recordStore = null;
    }

    /**
     * Helper method that checks if this enumeration can be used.
     * If this enumeration has been destroyed, an exception is thrown.
     *
     * @exception IllegalStateException if RecordEnumeration has been 
     *            destroyed.
     */
    private void checkDestroyed() {
        if (recordStore == null) {
            throw new IllegalStateException();
        }
    }

    /**
     * Used to add a record to an already filtered and sorted
     * <code>records</code> array.  More efficient than 
     * <code>reFilterSort</code> because it relys on 
     * <code>records</code> being in sorted order.
     *
     * First ensures that record <code>recordId</code> 
     * meets this enumeration's filter criteria.
     * If it does it is added to records as array element
     * 0.  If a comparator is defined for this enumeration,
     * the helper method <code>sortAdd</code> is called to 
     * properly position <code>recordId</code> within the ordered
     * <code>records</code> array.
     *
     * Should be called from within a 
     * synchronized (recordStore.rsLock) block.
     *
     * @param recordId the record to add to this enumeration
     */
    private void filterAdd(int recordId) {
        int insertPoint = -1;
        if (filter != null) {
            try {
                if (!filter.matches(recordStore.getRecord(recordId))) {
                    return;
                }
            } catch (RecordStoreException rse) {
                return;
            }
        }
        int[] newrecs = new int[records.length + 1];
        newrecs[0] = recordId;
        System.arraycopy(records, 0, newrecs, 1, records.length);
        records = newrecs;
        if (comparator != null) {
            try {
                insertPoint = sortInsert();
            } catch (RecordStoreException rse) {
                System.out.println("Unexpected exception in filterAdd");
            }
        }
        if (index != NO_SUCH_RECORD && insertPoint != -1 && insertPoint < index) {
            index++;
        }
    }

    /**
     * Helper method called by <code>filterAdd</code>.
     * Moves the posibly unsorted element zero in the
     * <code>records</code> array to its sorted position
     * within the array.
     *
     * @return index of inserted element.
     * @exception RecordStoreException if an error occurs
     *            in the comparator function.
     */
    private int sortInsert() throws RecordStoreException {
        int tmp;
        int i;
        int j;
        for (i = 0, j = 1; i < records.length - 1; i++, j++) {
            if (comparator.compare(recordStore.getRecord(records[i]), recordStore.getRecord(records[j])) == RecordComparator.FOLLOWS) {
                tmp = records[i];
                records[i] = records[j];
                records[j] = tmp;
            } else {
                break;
            }
        }
        return i;
    }

    /**
     * Find the index in records of record <code>recordId</code> 
     * and return it.  
     *
     * @param recordId the record index to find
     * @return the index of the record, or -1.
     */
    private int findIndexOfRecord(int recordId) {
        int idx;
        int recIndex = -1;
        for (idx = records.length - 1; idx >= 0; idx--) {
            if (records[idx] == recordId) {
                recIndex = idx;
                break;
            }
        }
        return recIndex;
    }

    /**
     * Internal helper method which 
     * removes the array element at index <code>recIndex</code>
     * from the internal <code>records</code> array.  
     *
     * <code>recIndex</code> should be non negative.
     *
     * @param recIndex the array element to remove.
     */
    private void removeRecordAtIndex(int recIndex) {
        int[] tmp = new int[records.length - 1];
        if (recIndex < records.length) {
            System.arraycopy(records, 0, tmp, 0, recIndex);
            System.arraycopy(records, recIndex + 1, tmp, recIndex, (records.length - recIndex) - 1);
        } else {
            System.arraycopy(records, 0, tmp, 0, records.length - 1);
        }
        records = tmp;
        if (index != NO_SUCH_RECORD && recIndex < index) {
            index--;
        } else if (index == records.length) {
            index--;
        }
    }

    /**
     * Internal helper method for filtering and sorting records if
     * necessary. Called from rebuild().
     *
     * Should be called from within a synchronized(recordStore.rsLock) block
     *
     * @param filtered array of record stores to filter and sort.
     */
    private void reFilterSort(int[] filtered) {
        int filteredIndex = 0;
        if (filter == null) {
            records = filtered;
        } else {
            for (int i = 0; i < filtered.length; i++) {
                try {
                    if (filter.matches(recordStore.getRecord(filtered[i]))) {
                        if (filteredIndex != i) {
                            filtered[filteredIndex++] = filtered[i];
                        } else {
                            filteredIndex++;
                        }
                    }
                } catch (RecordStoreException rse) {
                }
            }
            records = new int[filteredIndex];
            System.arraycopy(filtered, 0, records, 0, filteredIndex);
        }
        if (comparator != null) {
            try {
                QuickSort(records, 0, records.length - 1, comparator);
            } catch (RecordStoreException de) {
                System.out.println("Unexpected exception in " + "reFilterSort");
            }
        }
        reset();
    }

    /**
     * Quicksort helper function for sorting the records.
     *
     * @param a the array of recordId's to sort using comparator.
     * @param lowIndex the low bound of the range to sort.
     * @param highIndex the hight bound of the range to sort.
     * @param comparator the RecordComparator to use to compare records.
     */
    private void QuickSort(int a[], int lowIndex, int highIndex, RecordComparator comparator) throws RecordStoreException {
        int left = lowIndex;
        int right = highIndex;
        if (highIndex > lowIndex) {
            int ind = (lowIndex + highIndex) / 2;
            int pivotIndex = a[ind];
            byte[] pivotData = recordStore.getRecord(pivotIndex);
            while (left <= right) {
                while ((left < highIndex) && (comparator.compare(recordStore.getRecord(a[left]), pivotData) == RecordComparator.PRECEDES)) {
                    left++;
                }
                while ((right > lowIndex) && (comparator.compare(recordStore.getRecord(a[right]), pivotData) == RecordComparator.FOLLOWS)) {
                    right--;
                }
                if (left <= right) {
                    int tmp = a[left];
                    a[left] = a[right];
                    a[right] = tmp;
                    left++;
                    right--;
                }
            }
            if (lowIndex < right) {
                QuickSort(a, lowIndex, right, comparator);
            }
            if (left < highIndex) {
                QuickSort(a, left, highIndex, comparator);
            }
        }
    }
}
