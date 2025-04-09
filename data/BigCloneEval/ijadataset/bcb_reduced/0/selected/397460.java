package com.smssalama.storage.framework;

import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordListener;
import javax.microedition.rms.RecordStore;

/**
 * A stores for {@link com.smssalama.storage.framework.framework.Storable}
 * objects.
 * 
 * @author Arnold P. Minde
 */
public class ObjectStore {

    private class ObjectListenerProxy implements RecordListener {

        private void notifyStorageListeners(int recordId) {
            if (ObjectStore.this.storageListeners != null) {
                for (int i = ObjectStore.this.storageListeners.size() - 1; i >= 0; i--) {
                    WeakReference ref = (WeakReference) ObjectStore.this.storageListeners.elementAt(i);
                    StorageListener listener = (StorageListener) ref.get();
                    if (listener != null) {
                        listener.storeChanged(ObjectStore.this, recordId);
                    } else {
                        ObjectStore.this.storageListeners.removeElement(ref);
                    }
                }
            }
        }

        public void recordAdded(RecordStore arg0, int recordId) {
            synchronized (ObjectStore.this) {
                if (ObjectStore.this.objectListeners != null) {
                    for (int i = ObjectStore.this.objectListeners.size() - 1; i >= 0; i--) {
                        WeakReference ref = (WeakReference) ObjectStore.this.objectListeners.elementAt(i);
                        ObjectListener listener = (ObjectListener) ref.get();
                        if (listener != null) {
                            listener.objectAdded(ObjectStore.this, recordId);
                        } else {
                            ObjectStore.this.objectListeners.removeElement(ref);
                        }
                    }
                }
                notifyStorageListeners(recordId);
            }
        }

        public void recordChanged(RecordStore arg0, int recordId) {
            synchronized (ObjectStore.this) {
                if (ObjectStore.this.objectListeners != null) {
                    for (int i = ObjectStore.this.objectListeners.size() - 1; i >= 0; i--) {
                        WeakReference ref = (WeakReference) ObjectStore.this.objectListeners.elementAt(i);
                        ObjectListener listener = (ObjectListener) ref.get();
                        if (listener != null) {
                            listener.objectChanged(ObjectStore.this, recordId);
                        } else {
                            ObjectStore.this.objectListeners.removeElement(ref);
                        }
                    }
                }
                notifyStorageListeners(recordId);
            }
        }

        public void recordDeleted(RecordStore arg0, int recordId) {
            synchronized (ObjectStore.this) {
                if (ObjectStore.this.objectListeners != null) {
                    for (int i = ObjectStore.this.objectListeners.size() - 1; i >= 0; i--) {
                        WeakReference ref = (WeakReference) ObjectStore.this.objectListeners.elementAt(i);
                        ObjectListener listener = (ObjectListener) ref.get();
                        if (listener != null) {
                            listener.objectDeleted(ObjectStore.this, recordId);
                        } else {
                            ObjectStore.this.objectListeners.removeElement(ref);
                        }
                    }
                }
                notifyStorageListeners(recordId);
            }
        }
    }

    private RecordStore _recordStore;

    private StorageEncryptor _storageEncrytor;

    private Class type;

    private Hashtable objectCache = new Hashtable();

    public ObjectStore(String recordStoreName, StorageEncryptor storageEncryptor) throws Exception {
        this._recordStore = RecordStore.openRecordStore(recordStoreName, true, RecordStore.AUTHMODE_PRIVATE, false);
        this._storageEncrytor = storageEncryptor;
    }

    public ObjectStore(String recordStoreName, StorageEncryptor storageEncryptor, boolean openNew) throws Exception {
        super();
        if (openNew) {
            try {
                RecordStore.deleteRecordStore(recordStoreName);
            } catch (Exception e) {
            }
        }
        this._recordStore = RecordStore.openRecordStore(recordStoreName, true, RecordStore.AUTHMODE_PRIVATE, false);
        this._storageEncrytor = storageEncryptor;
    }

    public synchronized void persistStorable(Storable object) throws Exception {
        if (this.type != null) {
            if (!this.type.getClass().isAssignableFrom(object.getClass())) {
                throw new RuntimeException("This object store is for object of type " + this.type.getName() + ". Can not save " + object.getClass().getName());
            }
        }
        boolean isNew = false;
        if (object.recordId == 0) {
            isNew = true;
            object.recordId = this._recordStore.getNextRecordID();
        }
        byte[] data = object.getBytes();
        Storable.dump('S', object.recordId, data);
        if (this._storageEncrytor != null) {
            data = this._storageEncrytor.encrypt(data);
        }
        if (isNew) {
            object.recordId = this._recordStore.addRecord(data, 0, data.length);
        } else {
            this._recordStore.setRecord(object.recordId, data, 0, data.length);
        }
        objectCache.put(new Integer(object.getRecordId()), object);
    }

    public synchronized Storable getStorable(int recordId) throws Exception {
        Integer recordId_obj = new Integer(recordId);
        if (objectCache.containsKey(recordId_obj)) {
            return (Storable) objectCache.get(recordId_obj);
        }
        byte[] data = this._recordStore.getRecord(recordId);
        if (this._storageEncrytor != null) {
            data = this._storageEncrytor.decrypt(data);
        }
        Storable result = Storable.init(data);
        result.recordId = recordId;
        objectCache.put(recordId_obj, result);
        return result;
    }

    public synchronized void deleteStorable(int recordId) throws Exception {
        this._recordStore.deleteRecord(recordId);
    }

    public RecordStore getRecordStore() {
        return this._recordStore;
    }

    public ObjectEnumeration search(StorableFilter filter, StorableComparator comparator) throws Exception {
        if (comparator == null) {
        }
        Vector matches = new Vector();
        RecordEnumeration e = this._recordStore.enumerateRecords(null, null, false);
        while (e.hasNextElement()) {
            int recordId = e.nextRecordId();
            Storable storable = this.getStorable(recordId);
            if (filter == null || filter.matches(storable)) {
                matches.addElement(storable);
            }
        }
        quickSort(matches, comparator);
        return new ObjectEnumeration2(matches);
    }

    private static void swap(Vector matches, int i, int j) {
        Object tmp = matches.elementAt(i);
        matches.setElementAt(matches.elementAt(j), i);
        matches.setElementAt(tmp, j);
    }

    public static void quickSort(Vector matches, StorableComparator comparator) {
        quickSort(matches, comparator, 0, matches.size() - 1);
    }

    private static void quickSort(Vector matches, StorableComparator comparator, int startIndex, int endIndex) {
        if (comparator == null) return;
        if (startIndex < 0 || startIndex >= endIndex || endIndex >= matches.size()) return;
        int middleIndex = (startIndex + endIndex) / 2;
        Storable pivot = (Storable) matches.elementAt(middleIndex);
        swap(matches, middleIndex, endIndex);
        int pivotIndex = startIndex;
        for (int i = startIndex; i <= endIndex - 1; i++) {
            if (comparator.terminate()) return;
            int result = comparator.compare(pivot, ((Storable) matches.elementAt(i)));
            if (result >= 0) {
                swap(matches, pivotIndex, i);
                pivotIndex++;
            }
        }
        swap(matches, pivotIndex, endIndex);
        quickSort(matches, comparator, startIndex, pivotIndex - 1);
        quickSort(matches, comparator, pivotIndex + 1, endIndex);
    }

    public static void crudeSort(Vector matches, StorableComparator comparator) throws ObjectStorageException {
        if (comparator != null) {
            for (int i = 0; i < matches.size(); i++) {
                for (int j = i + 1; j < matches.size(); j++) {
                    if (comparator.terminate()) {
                        return;
                    }
                    int r = comparator.compare(((Storable) matches.elementAt(i)), ((Storable) matches.elementAt(j)));
                    if (r >= 0) {
                        swap(matches, i, j);
                    }
                }
            }
        }
    }

    private Vector objectListeners = new Vector();

    private RecordListener objectListenerProxy = null;

    private Vector storageListeners = new Vector();

    private Hashtable listeners = new Hashtable();

    public synchronized void addObjectListener(final ObjectListener listener) {
        if (this.objectListeners == null) {
            this.objectListeners = new Vector();
        }
        for (int i = 0; i < this.objectListeners.size(); i++) {
            WeakReference ref = (WeakReference) this.objectListeners.elementAt(i);
            if (ref.get() == listener) {
                return;
            }
        }
        WeakReference ref = new WeakReference(listener);
        this.objectListeners.addElement(ref);
        if (this.objectListenerProxy == null) {
            createObjectListenerProxy();
            this._recordStore.addRecordListener(this.objectListenerProxy);
        }
    }

    private void createObjectListenerProxy() {
        if (this.objectListenerProxy == null) {
            this.objectListenerProxy = new ObjectListenerProxy();
        }
    }

    public synchronized void removeObjectListener(ObjectListener listener) {
        if (this.objectListeners != null) {
            synchronized (this.objectListeners) {
                for (int i = this.objectListeners.size() - 1; i >= 0; i--) {
                    WeakReference ref = (WeakReference) this.objectListeners.elementAt(i);
                    if (ref.get() == listener) {
                        ref.clear();
                        this.objectListeners.removeElement(ref);
                    } else if (ref.get() == null) {
                        this.objectListeners.removeElement(ref);
                    }
                }
            }
        }
    }

    public synchronized void addStorageListener(final StorageListener listener) {
        if (this.storageListeners == null) {
            this.storageListeners = new Vector();
        }
        for (int i = 0; i < this.storageListeners.size(); i++) {
            WeakReference ref = (WeakReference) this.storageListeners.elementAt(i);
            if (ref.get() == listener) {
                return;
            }
        }
        WeakReference ref = new WeakReference(listener);
        this.storageListeners.addElement(ref);
        if (this.objectListenerProxy == null) {
            createObjectListenerProxy();
            this._recordStore.addRecordListener(this.objectListenerProxy);
        }
    }

    public void removeStorageListener(StorageListener listener) {
        if (this.storageListeners != null) {
            synchronized (this.storageListeners) {
                for (int i = this.storageListeners.size() - 1; i >= 0; i--) {
                    WeakReference ref = (WeakReference) this.storageListeners.elementAt(i);
                    if (ref.get() == listener) {
                        ref.clear();
                        this.storageListeners.removeElement(ref);
                    } else if (ref.get() == null) {
                        this.storageListeners.removeElement(ref);
                    }
                }
            }
        }
    }

    public class ObjectEnumeration {

        private RecordEnumeration _recordEnumeration;

        public ObjectEnumeration(RecordEnumeration recordEnumeration) {
            this._recordEnumeration = recordEnumeration;
        }

        public boolean hasNextObject() {
            return this._recordEnumeration.hasNextElement();
        }

        public Storable nextObject() throws Exception {
            int recordId = this._recordEnumeration.nextRecordId();
            return getStorable(recordId);
        }

        public int getCount() {
            return this._recordEnumeration.numRecords();
        }

        public void rebuild() {
            this._recordEnumeration.rebuild();
        }

        public void reset() {
            this._recordEnumeration.reset();
        }

        public void close() {
            this._recordEnumeration.destroy();
        }
    }

    public class ObjectEnumeration2 extends ObjectEnumeration {

        Vector objects = null;

        int nextIndex = 0;

        public ObjectEnumeration2(Vector v) {
            super(null);
            this.objects = v;
        }

        public boolean hasNextObject() {
            return (this.nextIndex >= 0) && (this.nextIndex) < this.objects.size();
        }

        public Storable nextObject() throws Exception {
            Storable n = (Storable) this.objects.elementAt(this.nextIndex);
            this.nextIndex++;
            return n;
        }

        public int getCount() {
            return this.objects.size();
        }

        public void rebuild() {
        }

        public void reset() {
            this.nextIndex = 0;
        }

        public void close() {
            this.objects.removeAllElements();
        }
    }

    public class ObjectEnumeration3 extends ObjectEnumeration {

        final RecordEnumeration recordEnumeration;

        final StorableFilter filter;

        public ObjectEnumeration3(RecordEnumeration recordEnumeration, StorableFilter filter) {
            super(recordEnumeration);
            this.recordEnumeration = recordEnumeration;
            this.filter = filter;
        }

        public boolean hasNextObject() {
            return recordEnumeration.hasNextElement();
        }

        public Storable nextObject() throws Exception {
            int recordId = recordEnumeration.nextRecordId();
            return ObjectStore.this.getStorable(recordId);
        }

        private boolean findNext() {
            if (filter == null) {
                return recordEnumeration.hasNextElement();
            }
            while (recordEnumeration.hasNextElement()) {
                int recordId;
                try {
                    recordId = recordEnumeration.nextRecordId();
                    Storable s = ObjectStore.this.getStorable(recordId);
                    if (filter.matches(s)) {
                        recordEnumeration.previousRecordId();
                        return true;
                    }
                } catch (InvalidRecordIDException e) {
                    e.printStackTrace();
                    return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return false;
        }

        public int getCount() {
            return -1;
        }

        public void rebuild() {
            recordEnumeration.rebuild();
        }

        public void reset() {
            recordEnumeration.reset();
        }

        public void close() {
            recordEnumeration.destroy();
        }
    }

    /**
	 * Changes the storage encryptor for the storage. If changeRecords is
	 * <code>true</code>, all records encryption is changed to using the new
	 * encryptor. This is useful when a user changes password, for example, and the records encrypted in the old password have to be replaced with same records encrypted using the new password.
	 * 
	 * @param encryptor
	 * @param changeRecords
	 * @throws Exception
	 */
    public synchronized void setStorageEncryptor(StorageEncryptor encryptor, boolean changeRecords) throws Exception {
        if (changeRecords) {
            RecordEnumeration e = this._recordStore.enumerateRecords(null, null, false);
            while (e.hasNextElement()) {
                int id = e.nextRecordId();
                byte[] oldR = this._recordStore.getRecord(id);
                byte[] plain = this._storageEncrytor.decrypt(oldR);
                byte[] newR = encryptor.encrypt(plain);
                this._recordStore.setRecord(id, newR, 0, newR.length);
            }
        }
        this._storageEncrytor = encryptor;
    }

    public void delete() throws Exception {
        RecordStore.deleteRecordStore(this._recordStore.getName());
    }

    public int getNumRecords() throws Exception {
        return this._recordStore.getNumRecords();
    }
}
