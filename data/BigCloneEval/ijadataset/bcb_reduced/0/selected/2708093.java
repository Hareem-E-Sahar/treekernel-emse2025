package com.db4o.internal;

import com.db4o.config.*;
import com.db4o.ext.*;
import com.db4o.foundation.*;
import com.db4o.internal.activation.*;
import com.db4o.internal.convert.*;
import com.db4o.internal.ids.*;
import com.db4o.internal.references.*;
import com.db4o.internal.slots.*;
import com.db4o.internal.weakref.*;
import com.db4o.io.*;
import com.db4o.reflect.*;
import com.db4o.types.*;

/**
 * no reading
 * no writing
 * no updates
 * no weak references
 * navigation by ID only both sides need synchronised ClassCollections and
 * MetaInformationCaches
 * 
 * @exclude
 */
public class TransportObjectContainer extends LocalObjectContainer {

    private final ObjectContainerBase _parent;

    private final MemoryBin _memoryBin;

    public TransportObjectContainer(ObjectContainerBase parent, MemoryBin memoryFile) {
        super(parent.config());
        _memoryBin = memoryFile;
        _parent = parent;
        _lock = parent.lock();
        _showInternalClasses = parent._showInternalClasses;
        open();
    }

    protected void initialize1(Configuration config) {
        _handlers = _parent._handlers;
        _classCollection = _parent.classCollection();
        _config = _parent.configImpl();
        _references = WeakReferenceSupportFactory.disabledWeakReferenceSupport();
    }

    @Override
    protected void initializeClassMetadataRepository() {
    }

    @Override
    protected void initalizeWeakReferenceSupport() {
    }

    @Override
    void initializeEssentialClasses() {
    }

    @Override
    protected void initializePostOpenExcludingTransportObjectContainer() {
    }

    @Override
    void initNewClassCollection() {
    }

    @Override
    boolean canUpdate() {
        return false;
    }

    @Override
    public ClassMetadata classMetadataForID(int id) {
        return _parent.classMetadataForID(id);
    }

    @Override
    void configureNewFile() {
    }

    @Override
    public int converterVersion() {
        return Converter.VERSION;
    }

    protected void dropReferences() {
        _config = null;
    }

    @Override
    protected void handleExceptionOnClose(Exception exc) {
    }

    @Override
    public final Transaction newTransaction(Transaction parentTransaction, ReferenceSystem referenceSystem, boolean isSystemTransaction) {
        if (null != parentTransaction) {
            return parentTransaction;
        }
        return new TransactionObjectCarrier(this, null, new TransportIdSystem(this), referenceSystem);
    }

    @Override
    public long currentVersion() {
        return 0;
    }

    @Override
    public Db4oType db4oTypeStored(Transaction a_trans, Object a_object) {
        return null;
    }

    @Override
    public boolean dispatchsEvents() {
        return false;
    }

    @Override
    protected void finalize() {
    }

    @Override
    public final void free(int a_address, int a_length) {
    }

    @Override
    public final void free(Slot slot) {
    }

    @Override
    public Slot allocateSlot(int length) {
        return appendBytes(length);
    }

    @Override
    protected boolean isValidPointer(int id) {
        return id != 0 && super.isValidPointer(id);
    }

    @Override
    public Db4oDatabase identity() {
        return ((ExternalObjectContainer) _parent).identity();
    }

    @Override
    public boolean maintainsIndices() {
        return false;
    }

    @Override
    public long generateTimeStampId() {
        return _parent.generateTimeStampId();
    }

    @Override
    void message(String msg) {
    }

    @Override
    public ClassMetadata produceClassMetadata(ReflectClass claxx) {
        return _parent.produceClassMetadata(claxx);
    }

    @Override
    public void raiseCommitTimestamp(long a_minimumVersion) {
    }

    @Override
    void readThis() {
    }

    @Override
    boolean stateMessages() {
        return false;
    }

    @Override
    public void shutdown() {
        processPendingClassUpdates();
        writeDirtyClassMetadata();
        transaction().commit();
    }

    @Override
    public final void writeHeader(boolean startFileLockingThread, boolean shuttingDown) {
    }

    public static class KnownObjectIdentity {

        public int _id;

        public KnownObjectIdentity(int id) {
            _id = id;
        }
    }

    @Override
    public int storeInternal(Transaction trans, Object obj, UpdateDepth depth, boolean checkJustSet) throws DatabaseClosedException, DatabaseReadOnlyException {
        int id = _parent.getID(null, obj);
        if (id > 0) {
            return super.storeInternal(trans, new KnownObjectIdentity(id), depth, checkJustSet);
        }
        return super.storeInternal(trans, obj, depth, checkJustSet);
    }

    @Override
    public Object getByID2(Transaction ta, int id) {
        Object obj = super.getByID2(ta, id);
        if (obj instanceof KnownObjectIdentity) {
            KnownObjectIdentity oi = (KnownObjectIdentity) obj;
            activate(oi);
            obj = _parent.getByID(null, oi._id);
        }
        return obj;
    }

    public void deferredOpen() {
        open();
    }

    @Override
    protected final void openImpl() throws OldFormatException {
        createIdSystem();
        if (_memoryBin.length() == 0) {
            configureNewFile();
            commitTransaction();
        } else {
            readThis();
        }
    }

    @Override
    public void backup(Storage targetStorage, String path) throws NotSupportedException {
        throw new NotSupportedException();
    }

    @Override
    public void blockSize(int size) {
    }

    @Override
    public void closeTransaction(Transaction transaction, boolean isSystemTransaction, boolean rollbackOnClose) {
    }

    @Override
    protected void shutdownDataStorage() {
        dropReferences();
    }

    @Override
    public long fileLength() {
        return _memoryBin.length();
    }

    @Override
    public String fileName() {
        return "Memory File";
    }

    @Override
    protected boolean hasShutDownHook() {
        return false;
    }

    @Override
    public final boolean needsLockFileThread() {
        return false;
    }

    @Override
    public void readBytes(byte[] bytes, int address, int length) {
        try {
            _memoryBin.read(address, bytes, length);
        } catch (Exception e) {
            Exceptions4.throwRuntimeException(13, e);
        }
    }

    @Override
    public void readBytes(byte[] bytes, int address, int addressOffset, int length) {
        readBytes(bytes, address + addressOffset, length);
    }

    @Override
    public void syncFiles() {
    }

    @Override
    public void writeBytes(ByteArrayBuffer buffer, int address, int addressOffset) {
        _memoryBin.write(address + addressOffset, buffer._buffer, buffer.length());
    }

    @Override
    public void overwriteDeletedBytes(int a_address, int a_length) {
    }

    @Override
    public void reserve(int byteCount) {
        throw new NotSupportedException();
    }

    @Override
    public byte blockSize() {
        return 1;
    }

    @Override
    protected void fatalStorageShutdown() {
        shutdownDataStorage();
    }

    @Override
    public ReferenceSystem createReferenceSystem() {
        return new HashcodeReferenceSystem();
    }

    @Override
    protected void createIdSystem() {
    }

    @Override
    public Runnable commitHook() {
        return Runnable4.DO_NOTHING;
    }

    public void syncFiles(Runnable runnable) {
        runnable.run();
    }
}
