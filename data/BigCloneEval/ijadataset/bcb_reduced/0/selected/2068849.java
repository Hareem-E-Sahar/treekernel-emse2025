package org.apache.poi.poifs.filesystem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.poifs.dev.POIFSViewable;
import org.apache.poi.poifs.property.DocumentProperty;
import org.apache.poi.poifs.property.Property;
import org.apache.poi.poifs.storage.BlockWritable;
import org.apache.poi.poifs.storage.DataInputBlock;
import org.apache.poi.poifs.storage.DocumentBlock;
import org.apache.poi.poifs.storage.ListManagedBlock;
import org.apache.poi.poifs.storage.RawDataBlock;
import org.apache.poi.poifs.storage.SmallDocumentBlock;
import org.apache.poi.util.HexDump;

/**
 * This class manages a document in the POIFS filesystem.
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */
public final class POIFSDocument implements BATManaged, BlockWritable, POIFSViewable {

    private static final DocumentBlock[] EMPTY_BIG_BLOCK_ARRAY = {};

    private static final SmallDocumentBlock[] EMPTY_SMALL_BLOCK_ARRAY = {};

    private DocumentProperty _property;

    private int _size;

    private SmallBlockStore _small_store;

    private BigBlockStore _big_store;

    /**
	 * Constructor from large blocks
	 *
	 * @param name the name of the POIFSDocument
	 * @param blocks the big blocks making up the POIFSDocument
	 * @param length the actual length of the POIFSDocument
	 */
    public POIFSDocument(String name, RawDataBlock[] blocks, int length) throws IOException {
        _size = length;
        _big_store = new BigBlockStore(convertRawBlocksToBigBlocks(blocks));
        _property = new DocumentProperty(name, _size);
        _small_store = new SmallBlockStore(EMPTY_SMALL_BLOCK_ARRAY);
        _property.setDocument(this);
    }

    private static DocumentBlock[] convertRawBlocksToBigBlocks(ListManagedBlock[] blocks) throws IOException {
        DocumentBlock[] result = new DocumentBlock[blocks.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = new DocumentBlock((RawDataBlock) blocks[i]);
        }
        return result;
    }

    private static SmallDocumentBlock[] convertRawBlocksToSmallBlocks(ListManagedBlock[] blocks) {
        if (blocks instanceof SmallDocumentBlock[]) {
            return (SmallDocumentBlock[]) blocks;
        }
        SmallDocumentBlock[] result = new SmallDocumentBlock[blocks.length];
        System.arraycopy(blocks, 0, result, 0, blocks.length);
        return result;
    }

    /**
	 * Constructor from small blocks
	 *
	 * @param name the name of the POIFSDocument
	 * @param blocks the small blocks making up the POIFSDocument
	 * @param length the actual length of the POIFSDocument
	 */
    public POIFSDocument(String name, SmallDocumentBlock[] blocks, int length) {
        _size = length;
        _big_store = new BigBlockStore(EMPTY_BIG_BLOCK_ARRAY);
        _property = new DocumentProperty(name, _size);
        _small_store = new SmallBlockStore(blocks);
        _property.setDocument(this);
    }

    /**
	 * Constructor from small blocks
	 *
	 * @param name the name of the POIFSDocument
	 * @param blocks the small blocks making up the POIFSDocument
	 * @param length the actual length of the POIFSDocument
	 */
    public POIFSDocument(String name, ListManagedBlock[] blocks, int length) throws IOException {
        _size = length;
        _property = new DocumentProperty(name, _size);
        _property.setDocument(this);
        if (Property.isSmall(_size)) {
            _big_store = new BigBlockStore(EMPTY_BIG_BLOCK_ARRAY);
            _small_store = new SmallBlockStore(convertRawBlocksToSmallBlocks(blocks));
        } else {
            _big_store = new BigBlockStore(convertRawBlocksToBigBlocks(blocks));
            _small_store = new SmallBlockStore(EMPTY_SMALL_BLOCK_ARRAY);
        }
    }

    /**
	 * Constructor
	 *
	 * @param name the name of the POIFSDocument
	 * @param stream the InputStream we read data from
	 */
    public POIFSDocument(String name, InputStream stream) throws IOException {
        List<DocumentBlock> blocks = new ArrayList<DocumentBlock>();
        _size = 0;
        while (true) {
            DocumentBlock block = new DocumentBlock(stream);
            int blockSize = block.size();
            if (blockSize > 0) {
                blocks.add(block);
                _size += blockSize;
            }
            if (block.partiallyRead()) {
                break;
            }
        }
        DocumentBlock[] bigBlocks = blocks.toArray(new DocumentBlock[blocks.size()]);
        _big_store = new BigBlockStore(bigBlocks);
        _property = new DocumentProperty(name, _size);
        _property.setDocument(this);
        if (_property.shouldUseSmallBlocks()) {
            _small_store = new SmallBlockStore(SmallDocumentBlock.convert(bigBlocks, _size));
            _big_store = new BigBlockStore(new DocumentBlock[0]);
        } else {
            _small_store = new SmallBlockStore(EMPTY_SMALL_BLOCK_ARRAY);
        }
    }

    /**
	 * Constructor
	 *
	 * @param name the name of the POIFSDocument
	 * @param size the length of the POIFSDocument
	 * @param path the path of the POIFSDocument
	 * @param writer the writer who will eventually write the document contents
	 */
    public POIFSDocument(String name, int size, POIFSDocumentPath path, POIFSWriterListener writer) {
        _size = size;
        _property = new DocumentProperty(name, _size);
        _property.setDocument(this);
        if (_property.shouldUseSmallBlocks()) {
            _small_store = new SmallBlockStore(path, name, size, writer);
            _big_store = new BigBlockStore(EMPTY_BIG_BLOCK_ARRAY);
        } else {
            _small_store = new SmallBlockStore(EMPTY_SMALL_BLOCK_ARRAY);
            _big_store = new BigBlockStore(path, name, size, writer);
        }
    }

    /**
	 * @return array of SmallDocumentBlocks; may be empty, cannot be null
	 */
    public BlockWritable[] getSmallBlocks() {
        return _small_store.getBlocks();
    }

    /**
	 * @return size of the document
	 */
    public int getSize() {
        return _size;
    }

    /**
	 * read data from the internal stores
	 *
	 * @param buffer the buffer to write to
	 * @param offset the offset into our storage to read from
	 * This method is currently (Oct 2008) only used by test code. Perhaps it can be deleted
	 */
    void read(byte[] buffer, int offset) {
        int len = buffer.length;
        DataInputBlock currentBlock = getDataInputBlock(offset);
        int blockAvailable = currentBlock.available();
        if (blockAvailable > len) {
            currentBlock.readFully(buffer, 0, len);
            return;
        }
        int remaining = len;
        int writePos = 0;
        int currentOffset = offset;
        while (remaining > 0) {
            boolean blockIsExpiring = remaining >= blockAvailable;
            int reqSize;
            if (blockIsExpiring) {
                reqSize = blockAvailable;
            } else {
                reqSize = remaining;
            }
            currentBlock.readFully(buffer, writePos, reqSize);
            remaining -= reqSize;
            writePos += reqSize;
            currentOffset += reqSize;
            if (blockIsExpiring) {
                if (currentOffset == _size) {
                    if (remaining > 0) {
                        throw new IllegalStateException("reached end of document stream unexpectedly");
                    }
                    currentBlock = null;
                    break;
                }
                currentBlock = getDataInputBlock(currentOffset);
                blockAvailable = currentBlock.available();
            }
        }
    }

    /**
	 * @return <code>null</code> if <tt>offset</tt> points to the end of the document stream
	 */
    DataInputBlock getDataInputBlock(int offset) {
        if (offset >= _size) {
            if (offset > _size) {
                throw new RuntimeException("Request for Offset " + offset + " doc size is " + _size);
            }
            return null;
        }
        if (_property.shouldUseSmallBlocks()) {
            return SmallDocumentBlock.getDataInputBlock(_small_store.getBlocks(), offset);
        }
        return DocumentBlock.getDataInputBlock(_big_store.getBlocks(), offset);
    }

    /**
	 * @return the instance's DocumentProperty
	 */
    DocumentProperty getDocumentProperty() {
        return _property;
    }

    /**
	 * Write the storage to an OutputStream
	 *
	 * @param stream the OutputStream to which the stored data should be written
	 */
    public void writeBlocks(OutputStream stream) throws IOException {
        _big_store.writeBlocks(stream);
    }

    /**
	 * Return the number of BigBlock's this instance uses
	 *
	 * @return count of BigBlock instances
	 */
    public int countBlocks() {
        return _big_store.countBlocks();
    }

    /**
	 * Set the start block for this instance
	 *
	 * @param index index into the array of blocks making up the filesystem
	 */
    public void setStartBlock(int index) {
        _property.setStartBlock(index);
    }

    /**
	 * Get an array of objects, some of which may implement POIFSViewable
	 *
	 * @return an array of Object; may not be null, but may be empty
	 */
    public Object[] getViewableArray() {
        Object[] results = new Object[1];
        String result;
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            BlockWritable[] blocks = null;
            if (_big_store.isValid()) {
                blocks = _big_store.getBlocks();
            } else if (_small_store.isValid()) {
                blocks = _small_store.getBlocks();
            }
            if (blocks != null) {
                for (int k = 0; k < blocks.length; k++) {
                    blocks[k].writeBlocks(output);
                }
                byte[] data = output.toByteArray();
                if (data.length > _property.getSize()) {
                    byte[] tmp = new byte[_property.getSize()];
                    System.arraycopy(data, 0, tmp, 0, tmp.length);
                    data = tmp;
                }
                output = new ByteArrayOutputStream();
                HexDump.dump(data, 0, output, 0);
                result = output.toString();
            } else {
                result = "<NO DATA>";
            }
        } catch (IOException e) {
            result = e.getMessage();
        }
        results[0] = result;
        return results;
    }

    /**
	 * Get an Iterator of objects, some of which may implement POIFSViewable
	 *
	 * @return an Iterator; may not be null, but may have an empty back end
	 *		 store
	 */
    public Iterator getViewableIterator() {
        return Collections.EMPTY_LIST.iterator();
    }

    /**
	 * Give viewers a hint as to whether to call getViewableArray or
	 * getViewableIterator
	 *
	 * @return <code>true</code> if a viewer should call getViewableArray,
	 *		 <code>false</code> if a viewer should call getViewableIterator
	 */
    public boolean preferArray() {
        return true;
    }

    /**
	 * Provides a short description of the object, to be used when a
	 * POIFSViewable object has not provided its contents.
	 *
	 * @return short description
	 */
    public String getShortDescription() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Document: \"").append(_property.getName()).append("\"");
        buffer.append(" size = ").append(getSize());
        return buffer.toString();
    }

    private static final class SmallBlockStore {

        private SmallDocumentBlock[] _smallBlocks;

        private final POIFSDocumentPath _path;

        private final String _name;

        private final int _size;

        private final POIFSWriterListener _writer;

        /**
		 * Constructor
		 *
		 * @param blocks blocks to construct the store from
		 */
        SmallBlockStore(SmallDocumentBlock[] blocks) {
            _smallBlocks = blocks.clone();
            this._path = null;
            this._name = null;
            this._size = -1;
            this._writer = null;
        }

        /**
		 * Constructor for a small block store that will be written later
		 *
		 * @param path path of the document
		 * @param name name of the document
		 * @param size length of the document
		 * @param writer the object that will eventually write the document
		 */
        SmallBlockStore(POIFSDocumentPath path, String name, int size, POIFSWriterListener writer) {
            _smallBlocks = new SmallDocumentBlock[0];
            this._path = path;
            this._name = name;
            this._size = size;
            this._writer = writer;
        }

        /**
		 * @return <code>true</code> if this store is a valid source of data
		 */
        boolean isValid() {
            return _smallBlocks.length > 0 || _writer != null;
        }

        /**
		 * @return the SmallDocumentBlocks
		 */
        SmallDocumentBlock[] getBlocks() {
            if (isValid() && _writer != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream(_size);
                DocumentOutputStream dstream = new DocumentOutputStream(stream, _size);
                _writer.processPOIFSWriterEvent(new POIFSWriterEvent(dstream, _path, _name, _size));
                _smallBlocks = SmallDocumentBlock.convert(stream.toByteArray(), _size);
            }
            return _smallBlocks;
        }
    }

    private static final class BigBlockStore {

        private DocumentBlock[] bigBlocks;

        private final POIFSDocumentPath _path;

        private final String _name;

        private final int _size;

        private final POIFSWriterListener _writer;

        /**
		 * Constructor
		 *
		 * @param blocks the blocks making up the store
		 */
        BigBlockStore(DocumentBlock[] blocks) {
            bigBlocks = blocks.clone();
            _path = null;
            _name = null;
            _size = -1;
            _writer = null;
        }

        /**
		 * Constructor for a big block store that will be written later
		 *
		 * @param path path of the document
		 * @param name name of the document
		 * @param size length of the document
		 * @param writer the object that will eventually write the document
		 */
        BigBlockStore(POIFSDocumentPath path, String name, int size, POIFSWriterListener writer) {
            bigBlocks = new DocumentBlock[0];
            _path = path;
            _name = name;
            _size = size;
            _writer = writer;
        }

        /**
		 * @return <code>true</code> if this store is a valid source of data
		 */
        boolean isValid() {
            return bigBlocks.length > 0 || _writer != null;
        }

        /**
		 * @return the DocumentBlocks
		 */
        DocumentBlock[] getBlocks() {
            if (isValid() && _writer != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream(_size);
                DocumentOutputStream dstream = new DocumentOutputStream(stream, _size);
                _writer.processPOIFSWriterEvent(new POIFSWriterEvent(dstream, _path, _name, _size));
                bigBlocks = DocumentBlock.convert(stream.toByteArray(), _size);
            }
            return bigBlocks;
        }

        /**
		 * write the blocks to a stream
		 *
		 * @param stream the stream to which the data is to be written
		 */
        void writeBlocks(OutputStream stream) throws IOException {
            if (isValid()) {
                if (_writer != null) {
                    DocumentOutputStream dstream = new DocumentOutputStream(stream, _size);
                    _writer.processPOIFSWriterEvent(new POIFSWriterEvent(dstream, _path, _name, _size));
                    dstream.writeFiller(countBlocks() * POIFSConstants.BIG_BLOCK_SIZE, DocumentBlock.getFillByte());
                } else {
                    for (int k = 0; k < bigBlocks.length; k++) {
                        bigBlocks[k].writeBlocks(stream);
                    }
                }
            }
        }

        /**
		 * @return number of big blocks making up this document
		 */
        int countBlocks() {
            if (isValid()) {
                if (_writer == null) {
                    return bigBlocks.length;
                }
                return (_size + POIFSConstants.BIG_BLOCK_SIZE - 1) / POIFSConstants.BIG_BLOCK_SIZE;
            }
            return 0;
        }
    }
}
