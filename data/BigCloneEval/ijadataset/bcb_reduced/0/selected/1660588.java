package com.db4o.internal.fileheader;

import com.db4o.*;
import com.db4o.internal.*;

/**
 * @exclude
 */
public class FileHeader2 extends NewFileHeaderBase {

    private static final int BLOCKSIZE_OFFSET = ACCESS_TIME_OFFSET + Const4.LONG_LENGTH;

    public static final int HEADER_LENGTH = BLOCKSIZE_OFFSET + (Const4.INT_LENGTH * 5) + 1;

    private int _transactionPointerAddress = 0;

    @Override
    public int length() {
        return HEADER_LENGTH;
    }

    @Override
    protected void read(LocalObjectContainer container, ByteArrayBuffer reader) {
        newTimerFileLock(container);
        oldEncryptionOff(container);
        checkThreadFileLock(container, reader);
        reader.seek(BLOCKSIZE_OFFSET);
        container.blockSizeReadFromFile(reader.readInt());
        SystemData systemData = container.systemData();
        systemData.classCollectionID(reader.readInt());
        container.systemData().idSystemType(reader.readByte());
        _variablePart = createVariablePart(container);
        int variablePartAddress = reader.readInt();
        int variablePartLength = reader.readInt();
        _variablePart.read(variablePartAddress, variablePartLength);
        _transactionPointerAddress = reader.readInt();
        if (_transactionPointerAddress != 0) {
            ByteArrayBuffer buffer = new ByteArrayBuffer(TRANSACTION_POINTER_LENGTH);
            buffer.read(container, _transactionPointerAddress, 0);
            systemData.transactionPointer1(buffer.readInt());
            systemData.transactionPointer2(buffer.readInt());
        }
    }

    @Override
    public void writeFixedPart(LocalObjectContainer file, boolean startFileLockingThread, boolean shuttingDown, StatefulBuffer writer, int blockSize) {
        SystemData systemData = file.systemData();
        writer.append(SIGNATURE);
        writer.writeByte(version());
        writer.writeInt((int) timeToWrite(_timerFileLock.openTime(), shuttingDown));
        writer.writeLong(timeToWrite(_timerFileLock.openTime(), shuttingDown));
        writer.writeLong(timeToWrite(System.currentTimeMillis(), shuttingDown));
        writer.writeInt(blockSize);
        writer.writeInt(systemData.classCollectionID());
        writer.writeByte(systemData.idSystemType());
        writer.writeInt(((FileHeaderVariablePart2) _variablePart).address());
        writer.writeInt(((FileHeaderVariablePart2) _variablePart).length());
        writer.writeInt(_transactionPointerAddress);
        if (Debug4.xbytes) {
            writer.checkXBytes(false);
        }
        writer.write();
        if (shuttingDown) {
            writeVariablePart(file, true);
        } else {
            file.syncFiles();
        }
        if (startFileLockingThread) {
            file.threadPool().start("db4o lock thread", _timerFileLock);
        }
    }

    @Override
    public void writeTransactionPointer(Transaction systemTransaction, int transactionPointer) {
        if (_transactionPointerAddress == 0) {
            LocalObjectContainer file = ((LocalTransaction) systemTransaction).localContainer();
            _transactionPointerAddress = file.allocateSafeSlot(TRANSACTION_POINTER_LENGTH).address();
            file.writeHeader(false, false);
        }
        writeTransactionPointer(systemTransaction, transactionPointer, _transactionPointerAddress, 0);
    }

    @Override
    protected byte version() {
        return (byte) 2;
    }

    @Override
    protected NewFileHeaderBase createNew() {
        return new FileHeader2();
    }

    @Override
    public FileHeaderVariablePart createVariablePart(LocalObjectContainer file) {
        return new FileHeaderVariablePart2(file);
    }
}
