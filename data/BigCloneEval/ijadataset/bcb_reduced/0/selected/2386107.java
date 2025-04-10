package org.h2.index;

import java.lang.ref.SoftReference;
import java.sql.SQLException;
import java.util.Arrays;
import org.h2.constant.ErrorCode;
import org.h2.constant.SysProperties;
import org.h2.engine.Session;
import org.h2.message.Message;
import org.h2.result.Row;
import org.h2.store.Data;
import org.h2.store.DataPage;
import org.h2.store.Page;
import org.h2.store.PageStore;

/**
 * A leaf page that contains data of one or multiple rows. Format:
 * <ul>
 * <li>page type: byte (0)</li>
 * <li>checksum: short (1-2)</li>
 * <li>parent page id (0 for root): int (3-6)</li>
 * <li>table id: varInt</li>
 * <li>column count: varInt</li>
 * <li>entry count: short</li>
 * <li>with overflow: the first overflow page id: int</li>
 * <li>list of key / offset pairs (key: varLong, offset: shortInt)</li>
 * <li>data</li>
 * </ul>
 */
public class PageDataLeaf extends PageData {

    /**
     * The row offsets.
     */
    private int[] offsets;

    /**
     * The rows.
     */
    private Row[] rows;

    /**
     * For pages with overflow: the soft reference to the row
     */
    private SoftReference<Row> rowRef;

    /**
     * The page id of the first overflow page (0 if no overflow).
     */
    private int firstOverflowPageId;

    /**
     * The start of the data area.
     */
    private int start;

    /**
     * The size of the row in bytes for large rows.
     */
    private int overflowRowSize;

    private int columnCount;

    private int memorySize;

    private PageDataLeaf(PageDataIndex index, int pageId, Data data) {
        super(index, pageId, data);
    }

    /**
     * Create a new page.
     *
     * @param index the index
     * @param pageId the page id
     * @param parentPageId the parent
     * @return the page
     */
    static PageDataLeaf create(PageDataIndex index, int pageId, int parentPageId) throws SQLException {
        PageDataLeaf p = new PageDataLeaf(index, pageId, index.getPageStore().createData());
        index.getPageStore().logUndo(p, p.data);
        p.parentPageId = parentPageId;
        p.columnCount = index.getTable().getColumns().length;
        p.writeHead();
        p.start = p.data.length();
        return p;
    }

    /**
     * Read a data leaf page.
     *
     * @param index the index
     * @param data the data
     * @param pageId the page id
     * @return the page
     */
    public static Page read(PageDataIndex index, Data data, int pageId) throws SQLException {
        PageDataLeaf p = new PageDataLeaf(index, pageId, data);
        p.read();
        return p;
    }

    private void read() throws SQLException {
        data.reset();
        int type = data.readByte();
        data.readShortInt();
        this.parentPageId = data.readInt();
        int tableId = data.readVarInt();
        if (tableId != index.getId()) {
            throw Message.getSQLException(ErrorCode.FILE_CORRUPTED_1, "page:" + getPos() + " expected table:" + index.getId() + " got:" + tableId + " type:" + type);
        }
        columnCount = data.readVarInt();
        entryCount = data.readShortInt();
        offsets = new int[entryCount];
        keys = new long[entryCount];
        rows = new Row[entryCount];
        if (type == Page.TYPE_DATA_LEAF) {
            if (entryCount != 1) {
                Message.throwInternalError("entries: " + entryCount);
            }
            firstOverflowPageId = data.readInt();
        }
        for (int i = 0; i < entryCount; i++) {
            keys[i] = data.readVarLong();
            offsets[i] = data.readShortInt();
        }
        start = data.length();
        written = true;
    }

    private int getRowLength(Row row) throws SQLException {
        int size = 0;
        for (int i = 0; i < columnCount; i++) {
            size += data.getValueLen(row.getValue(i));
        }
        return size;
    }

    int addRowTry(Row row) throws SQLException {
        index.getPageStore().logUndo(this, data);
        int rowLength = getRowLength(row);
        int pageSize = index.getPageStore().getPageSize();
        int last = entryCount == 0 ? pageSize : offsets[entryCount - 1];
        int keyOffsetPairLen = 2 + data.getVarLongLen(row.getKey());
        if (entryCount > 0 && last - rowLength < start + keyOffsetPairLen) {
            int x = find(row.getKey());
            if (entryCount > 1) {
                if (entryCount < 5) {
                    return entryCount / 2;
                }
                int third = entryCount / 3;
                return x < third ? third : x >= 2 * third ? 2 * third : x;
            }
            return x;
        }
        index.getPageStore().logUndo(this, data);
        int offset = last - rowLength;
        int[] newOffsets = new int[entryCount + 1];
        long[] newKeys = new long[entryCount + 1];
        Row[] newRows = new Row[entryCount + 1];
        int x;
        if (entryCount == 0) {
            x = 0;
        } else {
            readAllRows();
            x = find(row.getKey());
            if (x < keys.length && keys[x] == row.getKey()) {
                throw index.getDuplicateKeyException();
            }
            System.arraycopy(offsets, 0, newOffsets, 0, x);
            System.arraycopy(keys, 0, newKeys, 0, x);
            System.arraycopy(rows, 0, newRows, 0, x);
            if (x < entryCount) {
                for (int j = x; j < entryCount; j++) {
                    newOffsets[j + 1] = offsets[j] - rowLength;
                }
                System.arraycopy(keys, x, newKeys, x + 1, entryCount - x);
                System.arraycopy(rows, x, newRows, x + 1, entryCount - x);
            }
        }
        written = false;
        last = x == 0 ? pageSize : offsets[x - 1];
        offset = last - rowLength;
        entryCount++;
        start += keyOffsetPairLen;
        newOffsets[x] = offset;
        newKeys[x] = row.getKey();
        newRows[x] = row;
        memorySize += row.getMemorySize();
        offsets = newOffsets;
        keys = newKeys;
        rows = newRows;
        index.getPageStore().update(this);
        if (offset < start) {
            if (entryCount > 1) {
                Message.throwInternalError();
            }
            start += 4;
            int remaining = rowLength - (pageSize - start);
            offset = start;
            offsets[x] = offset;
            int previous = getPos();
            int dataOffset = pageSize;
            int page = index.getPageStore().allocatePage();
            firstOverflowPageId = page;
            this.overflowRowSize = pageSize + rowLength;
            write();
            rowRef = new SoftReference<Row>(rows[0]);
            rows[0] = null;
            Data all = index.getPageStore().createData();
            all.checkCapacity(data.length());
            all.write(data.getBytes(), 0, data.length());
            data.truncate(index.getPageStore().getPageSize());
            do {
                int type, size, next;
                if (remaining <= pageSize - PageDataOverflow.START_LAST) {
                    type = Page.TYPE_DATA_OVERFLOW | Page.FLAG_LAST;
                    size = remaining;
                    next = 0;
                } else {
                    type = Page.TYPE_DATA_OVERFLOW;
                    size = pageSize - PageDataOverflow.START_MORE;
                    next = index.getPageStore().allocatePage();
                }
                PageDataOverflow overflow = PageDataOverflow.create(index.getPageStore(), page, type, previous, next, all, dataOffset, size);
                index.getPageStore().update(overflow);
                dataOffset += size;
                remaining -= size;
                previous = page;
                page = next;
            } while (remaining > 0);
        }
        return -1;
    }

    private void removeRow(int i) throws SQLException {
        index.getPageStore().logUndo(this, data);
        written = false;
        readAllRows();
        Row r = rows[i];
        if (r != null) {
            memorySize += r.getMemorySize();
        }
        entryCount--;
        if (entryCount < 0) {
            Message.throwInternalError();
        }
        firstOverflowPageId = 0;
        overflowRowSize = 0;
        rowRef = null;
        int keyOffsetPairLen = 2 + data.getVarLongLen(keys[i]);
        int[] newOffsets = new int[entryCount];
        long[] newKeys = new long[entryCount];
        Row[] newRows = new Row[entryCount];
        System.arraycopy(offsets, 0, newOffsets, 0, i);
        System.arraycopy(keys, 0, newKeys, 0, i);
        System.arraycopy(rows, 0, newRows, 0, i);
        int startNext = i > 0 ? offsets[i - 1] : index.getPageStore().getPageSize();
        int rowLength = startNext - offsets[i];
        for (int j = i; j < entryCount; j++) {
            newOffsets[j] = offsets[j + 1] + rowLength;
        }
        System.arraycopy(keys, i + 1, newKeys, i, entryCount - i);
        System.arraycopy(rows, i + 1, newRows, i, entryCount - i);
        start -= keyOffsetPairLen;
        offsets = newOffsets;
        keys = newKeys;
        rows = newRows;
    }

    Cursor find(Session session, long min, long max, boolean multiVersion) {
        int x = find(min);
        return new PageDataCursor(session, this, x, max, multiVersion);
    }

    /**
     * Get the row at the given index.
     *
     * @param at the index
     * @return the row
     */
    Row getRowAt(int at) throws SQLException {
        Row r = rows[at];
        if (r == null) {
            if (firstOverflowPageId == 0) {
                data.setPos(offsets[at]);
                r = index.readRow(data, columnCount);
            } else {
                if (rowRef != null) {
                    r = rowRef.get();
                    if (r != null) {
                        return r;
                    }
                }
                PageStore store = index.getPageStore();
                Data buff = store.createData();
                int pageSize = store.getPageSize();
                int offset = offsets[at];
                buff.write(data.getBytes(), offset, pageSize - offset);
                int next = firstOverflowPageId;
                do {
                    PageDataOverflow page = index.getPageOverflow(next);
                    next = page.readInto(buff);
                } while (next != 0);
                overflowRowSize = pageSize + buff.length();
                buff.setPos(0);
                r = index.readRow(buff, columnCount);
            }
            r.setKey(keys[at]);
            if (firstOverflowPageId != 0) {
                rowRef = new SoftReference<Row>(r);
            } else {
                rows[at] = r;
                memorySize += r.getMemorySize();
            }
        }
        return r;
    }

    int getEntryCount() {
        return entryCount;
    }

    PageData split(int splitPoint) throws SQLException {
        int newPageId = index.getPageStore().allocatePage();
        PageDataLeaf p2 = PageDataLeaf.create(index, newPageId, parentPageId);
        for (int i = splitPoint; i < entryCount; ) {
            int split = p2.addRowTry(getRowAt(splitPoint));
            if (split != -1) {
                Message.throwInternalError("split " + split);
            }
            removeRow(splitPoint);
        }
        return p2;
    }

    long getLastKey() throws SQLException {
        if (entryCount == 0) {
            return 0;
        }
        return getRowAt(entryCount - 1).getKey();
    }

    PageDataLeaf getNextPage() throws SQLException {
        if (parentPageId == PageData.ROOT) {
            return null;
        }
        PageDataNode next = (PageDataNode) index.getPage(parentPageId, -1);
        return next.getNextPage(keys[entryCount - 1]);
    }

    PageDataLeaf getFirstLeaf() {
        return this;
    }

    protected void remapChildren(int old) throws SQLException {
        if (firstOverflowPageId == 0) {
            return;
        }
        PageDataOverflow overflow = index.getPageOverflow(firstOverflowPageId);
        overflow.setParentPageId(getPos());
        index.getPageStore().update(overflow);
    }

    boolean remove(long key) throws SQLException {
        int i = find(key);
        if (keys[i] != key) {
            throw Message.getSQLException(ErrorCode.ROW_NOT_FOUND_WHEN_DELETING_1, index.getSQL() + ": " + key);
        }
        index.getPageStore().logUndo(this, data);
        if (entryCount == 1) {
            freeChildren();
            return true;
        }
        removeRow(i);
        index.getPageStore().update(this);
        return false;
    }

    void freeChildren() throws SQLException {
        if (firstOverflowPageId != 0) {
            PageStore store = index.getPageStore();
            int next = firstOverflowPageId;
            do {
                PageDataOverflow page = index.getPageOverflow(next);
                store.free(next, false);
                next = page.getNextOverflow();
            } while (next != 0);
        }
    }

    Row getRow(long key) throws SQLException {
        int index = find(key);
        return getRowAt(index);
    }

    int getRowCount() {
        return entryCount;
    }

    void setRowCountStored(int rowCount) {
    }

    public int getByteCount(DataPage dummy) {
        return index.getPageStore().getPageSize();
    }

    public void write(DataPage buff) throws SQLException {
        write();
        index.getPageStore().writePage(getPos(), data);
        data.truncate(index.getPageStore().getPageSize());
    }

    private void readAllRows() throws SQLException {
        for (int i = 0; i < entryCount; i++) {
            getRowAt(i);
        }
    }

    private void writeHead() {
        data.reset();
        int type;
        if (firstOverflowPageId == 0) {
            type = Page.TYPE_DATA_LEAF | Page.FLAG_LAST;
        } else {
            type = Page.TYPE_DATA_LEAF;
        }
        data.writeByte((byte) type);
        data.writeShortInt(0);
        if (SysProperties.CHECK2) {
            if (data.length() != START_PARENT) {
                Message.throwInternalError();
            }
        }
        data.writeInt(parentPageId);
        data.writeVarInt(index.getId());
        data.writeVarInt(columnCount);
        data.writeShortInt(entryCount);
    }

    private void write() throws SQLException {
        if (written) {
            return;
        }
        if (SysProperties.CHECK && firstOverflowPageId != 0 && rows[0] == null) {
            Message.throwInternalError(toString());
        }
        readAllRows();
        writeHead();
        if (firstOverflowPageId != 0) {
            data.writeInt(firstOverflowPageId);
            data.checkCapacity(overflowRowSize);
        }
        for (int i = 0; i < entryCount; i++) {
            data.writeVarLong(keys[i]);
            data.writeShortInt(offsets[i]);
        }
        for (int i = 0; i < entryCount; i++) {
            data.setPos(offsets[i]);
            Row r = getRowAt(i);
            for (int j = 0; j < columnCount; j++) {
                data.writeValue(r.getValue(j));
            }
        }
        written = true;
    }

    public String toString() {
        return "page[" + getPos() + "] data leaf table:" + index.getId() + " entries:" + entryCount + " parent:" + parentPageId + (firstOverflowPageId == 0 ? "" : " overflow:" + firstOverflowPageId) + " keys:" + Arrays.toString(keys) + " offsets:" + Arrays.toString(offsets);
    }

    public void moveTo(Session session, int newPos) throws SQLException {
        PageStore store = index.getPageStore();
        store.logUndo(this, data);
        PageDataLeaf p2 = PageDataLeaf.create(index, newPos, parentPageId);
        readAllRows();
        p2.keys = keys;
        p2.overflowRowSize = overflowRowSize;
        p2.firstOverflowPageId = firstOverflowPageId;
        p2.rowRef = rowRef;
        p2.rows = rows;
        if (firstOverflowPageId != 0) {
            p2.rows[0] = getRowAt(0);
        }
        p2.entryCount = entryCount;
        p2.offsets = offsets;
        p2.start = start;
        p2.remapChildren(getPos());
        p2.write();
        p2.data.truncate(index.getPageStore().getPageSize());
        store.update(p2);
        store.free(getPos(), true);
        if (parentPageId == ROOT) {
            index.setRootPageId(session, newPos);
        } else {
            PageDataNode p = (PageDataNode) store.getPage(parentPageId);
            p.moveChild(getPos(), newPos);
        }
    }

    void setOverflow(int overflow) throws SQLException {
        index.getPageStore().logUndo(this, data);
        firstOverflowPageId = overflow;
        if (written) {
            writeHead();
            data.writeInt(firstOverflowPageId);
        }
        index.getPageStore().update(this);
    }

    public int getMemorySize() {
        return index.getMemorySizePerPage();
    }
}
