package suneido.database.tools;

import static suneido.SuException.unreachable;
import static suneido.SuException.verify;
import static suneido.Suneido.errlog;
import static suneido.Suneido.fatal;
import static suneido.database.tools.DbTools.renameWithBackup;
import java.io.File;
import java.util.*;
import suneido.SuException;
import suneido.database.*;
import suneido.database.Database.TN;
import suneido.util.ByteBuf;
import suneido.util.Checksum;
import com.google.common.collect.ImmutableList;

public class DbRebuild extends DbCheck {

    private final String filename;

    private final String tempfilename;

    private Database newdb;

    private final BitSet deletes = new BitSet();

    private final Map<Long, Long> tr = new HashMap<Long, Long>();

    private int max_tblnum = -1;

    private final Map<Integer, String> tblnames = new HashMap<Integer, String>();

    private final Checksum cksum = new Checksum();

    private final int GRANULARITY = 16;

    public static void rebuildOrExit(String dbfilename) {
        File tempfile = DbTools.tempfile();
        try {
            if (!DbTools.runWithNewJvm("-rebuild:" + tempfile)) throw new SuException("rebuild failed: " + dbfilename);
        } catch (InterruptedException e) {
            throw new SuException("rebuild was interrupted");
        }
        renameWithBackup(tempfile, dbfilename);
    }

    public static void rebuild2(String dbfilename, String tempfilename) {
        DbRebuild dbr = new DbRebuild(dbfilename, tempfilename, true);
        Status status = dbr.check();
        switch(status) {
            case OK:
            case CORRUPTED:
                dbr.rebuild();
                errlog("Rebuilt " + dbfilename + " was " + status + " " + dbr.lastCommit(status));
                break;
            case UNRECOVERABLE:
                fatal("Rebuild failed " + dbfilename + " UNRECOVERABLE");
            default:
                throw unreachable();
        }
    }

    protected DbRebuild(String filename, String tempfilename, boolean print) {
        super(filename, print);
        this.filename = filename;
        this.tempfilename = tempfilename;
    }

    protected void rebuild() {
        println("Rebuilding " + filename);
        File tempfile = new File(tempfilename);
        try {
            newdb = new Database(tempfile, Mode.CREATE);
            tblnames.put(4, "views");
            copy();
            newdb.setNextTableNum(max_tblnum + 1);
            mmf.close();
            newdb.close();
            newdb = null;
            DbCheck dbc = new DbCheck(tempfile.getPath(), print);
            switch(dbc.check()) {
                case OK:
                    break;
                case CORRUPTED:
                case UNRECOVERABLE:
                    System.out.println("Rebuild FAILED still corrupt after rebuild");
                    return;
                default:
                    throw unreachable();
            }
            println(filename + " rebuilt");
        } catch (Throwable e) {
            System.out.println("Rebuild FAILED " + e);
        } finally {
            if (newdb != null) newdb.close();
        }
    }

    private boolean copy() {
        Mmfile.Iter iter = mmf.iterator();
        while (iter.next()) {
            ByteBuf buf = iter.current();
            if (iter.type() == Mmfile.OTHER || (iter.type() == Mmfile.COMMIT && isCommitOther(buf))) continue;
            long newoff = copyBlock(buf, iter.length(), iter.type());
            switch(iter.type()) {
                case Mmfile.SESSION:
                    handleSession();
                    break;
                case Mmfile.COMMIT:
                    handleCommit(newoff);
                    break;
                case Mmfile.DATA:
                    handleData(buf, iter.offset(), newoff);
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    private boolean isCommitOther(ByteBuf buf) {
        Commit commit = new Commit(buf);
        if (commit.getNCreates() != 1 || commit.getNDeletes() != 0) return false;
        long offset = commit.getCreate(0);
        return mmf.type(offset - 4) == Mmfile.OTHER;
    }

    private void handleSession() {
        cksum.reset();
    }

    private void handleData(ByteBuf buf, long oldoff, long newoff) {
        int tblnum = buf.getInt(0);
        if (tblnum != TN.TABLES && tblnum != TN.INDEXES) {
            Record r = new Record(buf.slice(4));
            cksum.add(buf.getByteBuffer(), r.bufSize() + 4);
        }
        tr.put(oldoff, newoff);
    }

    private long copyBlock(ByteBuf buf, int n, byte type) {
        long offset = newdb.alloc(n, type);
        ByteBuf newbuf = newdb.adr(offset);
        newbuf.put(0, buf.slice(0, n));
        return offset;
    }

    private void handleCommit(long newoff) {
        ByteBuf buf = newdb.adr(newoff);
        Commit commit = new Commit(buf);
        handleCommitEntries(commit);
        cksum.add(buf.getByteBuffer(), commit.sizeWithoutChecksum());
        commit.putChecksum(cksum.getValue());
        cksum.reset();
    }

    private void handleCommitEntries(Commit commit) {
        Record recFrom = handleTableRename(commit);
        for (int i = 0; i < commit.getNCreates(); ++i) {
            long oldoff = commit.getCreate(i);
            long newoff = tr.get(oldoff - 4) + 4;
            commit.putCreate(i, newoff);
            addIndexEntries(oldoff, newoff, recFrom);
        }
        for (int i = 0; i < commit.getNDeletes(); ++i) {
            long oldoff = commit.getDelete(i);
            long newoff = tr.get(oldoff - 4) + 4;
            commit.putDelete(i, newoff);
        }
    }

    private Record handleTableRename(Commit commit) {
        if (!isTableRename(commit)) return null;
        long oldoff = commit.getDelete(0);
        long newoff = tr.get(oldoff - 4) + 4;
        ByteBuf buf = newdb.adr(newoff - 4);
        int tblnum = buf.getInt(0);
        Record recFrom = new Record(buf.slice(4), newoff);
        newdb.removeIndexEntriesForRebuild(tblnum, recFrom);
        return recFrom;
    }

    private int tblnum(long offset) {
        ByteBuf buf = mmf.adr(offset - 4);
        return buf.getInt(0);
    }

    private void addIndexEntries(long oldoff, long newoff, Record renamedFrom) {
        if (isDeleted(oldoff)) return;
        ByteBuf buf = newdb.adr(newoff - 4);
        int tblnum = buf.getInt(0);
        Record rec = new Record(buf.slice(4), newoff);
        if (tblnum <= TN.INDEXES) handleSchemaRecord(tblnum, rec, newoff, renamedFrom); else {
            String tablename = tblnames.get(tblnum);
            if (tablename == null) return;
            newdb.addIndexEntriesForRebuild(tblnum, rec);
        }
    }

    private boolean isDeleted(long oldoff) {
        oldoff -= 4;
        verify(oldoff % 8 == 4);
        verify(oldoff / GRANULARITY < Integer.MAX_VALUE);
        return deletes.get((int) (oldoff / GRANULARITY));
    }

    private void handleSchemaRecord(int tblnum, Record rec, long newoff, Record renamedFrom) {
        int tn = rec.getInt(0);
        if (tn <= TN.INDEXES) return;
        newoff += 4;
        switch(tblnum) {
            case TN.TABLES:
                handleTablesRecord(rec, renamedFrom);
                break;
            case TN.COLUMNS:
                handleColumnsRecord(rec);
                break;
            case TN.INDEXES:
                handleIndexesRecord(rec);
                break;
            default:
                throw unreachable();
        }
    }

    private void handleTablesRecord(Record rec, Record renamedFrom) {
        int tblnum = rec.getInt(Table.TBLNUM);
        if (tblnum > max_tblnum) max_tblnum = tblnum;
        if (renamedFrom == null) Table.update(rec, rec.getInt(Table.NEXTFIELD), 0, 0); else Table.update(rec, renamedFrom.getInt(Table.NEXTFIELD), renamedFrom.getInt(Table.NROWS), renamedFrom.getInt(Table.TOTALSIZE));
        String tablename = rec.getString(Table.TABLE);
        tblnames.put(tblnum, tablename);
        newdb.addIndexEntriesForRebuild(TN.TABLES, rec);
        reloadTable(rec);
    }

    private void handleColumnsRecord(Record rec) {
        newdb.addIndexEntriesForRebuild(TN.COLUMNS, rec);
        reloadTable(rec.getInt(Column.TBLNUM));
    }

    private void handleIndexesRecord(Record indexes_rec) {
        newdb.addIndexEntriesForRebuild(TN.INDEXES, indexes_rec);
        BtreeIndex.rebuildCreate(newdb.dest, indexes_rec);
        reloadTable(indexes_rec.getInt(Index.TBLNUM));
        Transaction tran = newdb.readwriteTran();
        insertExistingRecords(tran, indexes_rec);
        tran.complete();
    }

    void insertExistingRecords(Transaction tran, Record indexes_rec) {
        int tblnum = indexes_rec.getInt(Index.TBLNUM);
        Table table = tran.getTable(tblnum);
        if (table.indexes.size() == 1) return;
        String columns = indexes_rec.getString(Index.COLUMNS);
        BtreeIndex btreeIndex = tran.getBtreeIndex(tblnum, columns);
        ImmutableList<Integer> colnums = table.getIndex(columns).colnums;
        Index index = table.firstIndex();
        BtreeIndex.Iter iter = tran.getBtreeIndex(index).iter(tran).next();
        for (; !iter.eof(); iter.next()) {
            Record r = newdb.input(iter.keyadr());
            Record key = r.project(colnums, iter.cur().keyadr());
            verify(btreeIndex.insert(tran, new Slot(key)));
        }
    }

    private void reloadTable(int tblnum) {
        Transaction tran = newdb.readonlyTran();
        Record table_rec = newdb.getTableRecord(tran, tblnum);
        reloadTable(tran, table_rec);
        tran.complete();
    }

    private void reloadTable(Record rec) {
        Transaction tran = newdb.readonlyTran();
        reloadTable(tran, rec);
        tran.complete();
    }

    private void reloadTable(Transaction tran, Record table_rec) {
        List<BtreeIndex> btis = new ArrayList<BtreeIndex>();
        Table table = newdb.loadTable(tran, table_rec, btis);
        newdb.updateTable(table, new TableData(table_rec));
        for (BtreeIndex bti : btis) newdb.updateBtreeIndex(bti);
    }

    @Override
    protected void process_deletes(Commit commit) {
        if (isTableRename(commit)) return;
        for (int i = 0; i < commit.getNDeletes(); ++i) {
            long del = commit.getDelete(i);
            verify(del % 8 == 0);
            del = (del - 4) / GRANULARITY;
            verify(del < Integer.MAX_VALUE);
            deletes.set((int) del);
        }
    }

    private boolean isTableRename(Commit commit) {
        return commit.getNCreates() == 1 && commit.getNDeletes() == 1 && isTableRecord(commit.getDelete(0)) && isTableRecord(commit.getCreate(0));
    }

    private boolean isTableRecord(long offset) {
        return mmf.type(offset - 4) == Mmfile.DATA && tblnum(offset) == TN.TABLES;
    }

    public static void main(String[] args) {
        rebuild2("suneido.db", "rebuilt.db");
    }
}
