package panda.index.bplustree;

import panda.file.Block;
import panda.record.*;
import panda.query.struct.*;
import static panda.file.Page.BLOCK_SIZE;
import static panda.file.Page.INT_SIZE;
import static java.sql.Types.*;
import panda.transaction.Transaction;

public class TreePage {

    private Block currentBlock;

    private int slotsize;

    private TableToken tbl;

    private Transaction tx;

    public int nextLeafPageBlockNum = -1;

    public static int maxlevel = -1;

    TreePage(Block blk, TableToken tb, Transaction t) {
        currentBlock = blk;
        tbl = tb;
        slotsize = tbl.getTupleLength();
        tx = t;
        tx.pin(currentBlock);
    }

    public int getLevel() {
        int templevel = tx.getInt(currentBlock, 0);
        if (templevel > maxlevel) {
            maxlevel = templevel;
        }
        return templevel;
    }

    public int findSlotBefore(Constant searchkey) {
        int slot = 0;
        while (slot < getRecordNum() && getData(slot).compareTo(searchkey) < 0) {
            slot++;
        }
        return slot - 1;
    }

    private int binarySearchGet(int down, int up, Constant searchkey) {
        if (getData(getRecordNum() - 1).compareTo(searchkey) < 0) {
            return getRecordNum() - 1;
        }
        int mid = (down + up) / 2;
        int ans = getData(mid).compareTo(searchkey);
        if (ans == 0) return mid;
        if (ans > 0) {
            if (getData(up).compareTo(searchkey) <= 0) return mid; else return binarySearchGet(down, mid - 1, searchkey);
        } else {
            if (getData(down).compareTo(searchkey) >= 0) return mid; else return binarySearchGet(mid + 1, up, searchkey);
        }
    }

    public int findSlotBefore2(Constant searchkey) {
        return (binarySearchGet(0, getRecordNum() - 1, searchkey));
    }

    public boolean isFull() {
        return slotPosition(getRecordNum() + 1) >= BLOCK_SIZE;
    }

    public void close() {
        if (currentBlock != null) {
            tx.unpin(currentBlock);
        }
        currentBlock = null;
    }

    public void insertDir(int slot, Constant v, int blockno) {
        insert(slot);
        setValue(slot, "dataval", v);
        setInt(slot, "blocknum", blockno);
    }

    public Block append(int level) {
        return tx.append(tbl.getFilename(), new FileHeader(tbl, level));
    }

    public Block split(int splitpos, int flag) {
        Block newblk = append(flag);
        TreePage newpage = new TreePage(newblk, tbl, tx);
        transferRecord(splitpos, newpage);
        newpage.setLevel(flag);
        newpage.close();
        return newblk;
    }

    public Constant getData(int slot) {
        return getDataVal(slot, "dataval");
    }

    public void print() {
        for (int i = 0; i < getRecordNum(); i++) {
            System.out.println("dataval : " + getDataVal(i, "dataval").getContentValue());
            System.out.println("blocknum : " + getDataVal(i, "blocknum").getContentValue());
            System.out.println("id : " + getDataVal(i, "id").getContentValue());
            System.out.println("*************************");
        }
    }

    public int getChildBlockNum(int slot) {
        return getInt(slot, "blocknum");
    }

    public void moveTo(Block blk) {
        TreePage newpage = new TreePage(blk, tbl, tx);
        transferRecord(0, newpage);
        newpage.close();
    }

    public void setLevel(int l) {
        tx.setInt(currentBlock, 0, l);
    }

    public int getRecordNum() {
        return tx.getInt(currentBlock, INT_SIZE);
    }

    public TupleToken getTupleToken(int slot) {
        return new TupleToken(getInt(slot, "blocknum"), getInt(slot, "id"));
    }

    public void deleteTupleToken(int slot) {
        int i = 0;
        for (i = slot + 1; i < getRecordNum(); i++) {
            copyTupleToken(i, i - 1);
        }
        setRecordNum(getRecordNum() - 1);
    }

    public void insertLeaf(int slot, Constant searchkey, TupleToken tt) {
        insert(slot);
        setValue(slot, "dataval", searchkey);
        setInt(slot, "blocknum", tt.getBlockNum());
        setInt(slot, "id", tt.getOffSet());
    }

    private int slotPosition(int slot) {
        return INT_SIZE + INT_SIZE + (slot * slotsize);
    }

    private void insert(int slot) {
        int i = 0;
        for (i = getRecordNum(); i > slot; i--) {
            copyTupleToken(i - 1, i);
        }
        setRecordNum(getRecordNum() + 1);
    }

    private boolean binarySearch(int down, int up, Constant searchkey) {
        if (down > up) return false;
        int mid = (down + up) / 2;
        int ans = getData(mid).compareTo(searchkey);
        if (ans == 0) return true;
        if (ans > 0) return binarySearch(down, mid - 1, searchkey); else return binarySearch(mid + 1, up, searchkey);
    }

    public boolean isInLeaf(Constant searchkey) {
        return binarySearch(0, getRecordNum() - 1, searchkey);
    }

    private void setValue(int slot, String field, Constant val) {
        int type = tbl.getSchema().getType(field);
        if (type == INTEGER) {
            setInt(slot, field, (Integer) val.getContentValue());
        } else if (type == VARCHAR || type == CHAR) {
            setString(slot, field, (String) val.getContentValue());
        }
    }

    private void setInt(int slot, String field, int val) {
        int pos = getPos(slot, field);
        tx.setInt(currentBlock, pos, val);
    }

    private void setString(int slot, String field, String val) {
        int pos = getPos(slot, field);
        tx.setString(currentBlock, pos, val);
    }

    private void copyTupleToken(int from, int to) {
        Schema sch = tbl.getSchema();
        for (String field : sch.getAllAttributeNames()) {
            setValue(to, field, getDataVal(from, field));
        }
    }

    private void setRecordNum(int num) {
        tx.setInt(currentBlock, INT_SIZE, num);
    }

    private void transferRecord(int pos, TreePage dest) {
        int destpos = 0;
        while (pos < getRecordNum()) {
            dest.insert(destpos);
            Schema sch = tbl.getSchema();
            for (String fldname : sch.getAllAttributeNames()) {
                dest.setValue(destpos, fldname, getDataVal(pos, fldname));
            }
            deleteRecord(pos);
            destpos++;
        }
    }

    private void deleteRecord(int slot) {
        int i = 0;
        for (i = slot + 1; i < getRecordNum(); i++) {
            copyTupleToken(i, i - 1);
        }
        setRecordNum(getRecordNum() - 1);
    }

    private void onceDeleteRecord(int pos, int length) {
        tx.memCpy(currentBlock, pos + 1, pos, length);
        setRecordNum(getRecordNum() - 1);
    }

    private Constant getDataVal(int slot, String fldname) {
        int ty = tbl.getSchema().getType(fldname);
        if (ty == INTEGER) {
            return new IntConstant(getInt(slot, fldname));
        } else if (ty == VARCHAR) {
            return new StringConstant(getString(slot, fldname));
        } else {
            return new FloatConstant(getFloat(slot, fldname));
        }
    }

    private Float getFloat(int slot, String fldname) {
        int pos = getPos(slot, fldname);
        return tx.getFloat(currentBlock, pos);
    }

    private String getString(int slot, String fldname) {
        int pos = getPos(slot, fldname);
        return tx.getString(currentBlock, pos);
    }

    private int getInt(int slot, String fldname) {
        int pos = getPos(slot, fldname);
        return tx.getInt(currentBlock, pos);
    }

    private int getPos(int slot, String fldname) {
        int offset = tbl.offsetOf(fldname);
        return slotPosition(slot) + offset;
    }
}
