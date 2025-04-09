package qp.operators;

import java.io.*;
import qp.operators.*;
import java.util.*;
import qp.optimizer.BufferManager;
import qp.utils.*;

public class ExternalSort extends Operator {

    private static int numBuff;

    Operator opt;

    Vector PageList;

    Vector scan_runs;

    int[] sortkeyIndex;

    int batchsize;

    int filenum;

    int pass;

    int start;

    int position;

    boolean eos;

    int nodeindex;

    int parent;

    public ExternalSort(Operator opt, Vector KeyAttrs, int key, int type, int nodeindex, int parent) {
        super(key);
        this.opt = opt;
        this.parent = parent;
        PageList = new Vector();
        schema = opt.getSchema();
        sortkeyIndex = new int[KeyAttrs.size()];
        for (int i = 0; i < KeyAttrs.size(); i++) {
            Attribute attr = (Attribute) KeyAttrs.elementAt(i);
            int index = schema.indexOf(attr);
            sortkeyIndex[i] = index;
        }
        batchsize = Batch.getPageSize() / schema.getTupleSize();
    }

    public Tuple getTuple(Vector a, int i) {
        Batch tmp = (Batch) a.elementAt(i / batchsize);
        Tuple tup = (Tuple) tmp.elementAt(i % batchsize);
        return tup;
    }

    private boolean cmp(int i, int j) {
        Tuple t1 = getTuple(PageList, i);
        Tuple t2 = getTuple(PageList, j);
        return cmp(t1, t2);
    }

    private boolean cmp(Tuple t1, Tuple t2) {
        for (int i = 0; i < sortkeyIndex.length; i++) {
            Object Value1 = t1.dataAt(sortkeyIndex[i]);
            Object Value2 = t2.dataAt(sortkeyIndex[i]);
            int dataType = schema.typeOf(sortkeyIndex[i]);
            if (dataType == Attribute.INT) {
                int Val1 = ((Integer) Value1).intValue();
                int Val2 = ((Integer) Value2).intValue();
                if (Val2 > Val1) return true; else if (Val2 < Val1) return false; else if (Val2 == Val1) continue;
            }
            if (dataType == Attribute.STRING) {
                String Val1 = (String) Value1;
                String Val2 = (String) Value2;
                int flag = Val1.compareTo(Val2);
                if (flag < 0) return true; else if (flag > 0) return false; else if (flag == 0) continue;
            }
            if (dataType == Attribute.REAL) {
                float Val1 = ((Float) Value1).floatValue();
                float Val2 = ((Float) Value2).floatValue();
                if (Val2 > Val1) return true; else if (Val2 < Val1) return false; else if (Val2 == Val1) continue;
            }
        }
        return false;
    }

    private void swap(int i, int j, Vector a) {
        Batch b1 = (Batch) a.elementAt(i / batchsize);
        Batch b2 = (Batch) a.elementAt(j / batchsize);
        Tuple t = b1.elementAt(i % batchsize);
        b1.setElementAt(b2.elementAt(j % batchsize), i % batchsize);
        b2.setElementAt(t, j % batchsize);
    }

    public void quicksort(int b, int e, Vector a) {
        int i = b, j = e, x = (b + e) / 2;
        do {
            while (cmp(i, x)) i++;
            while (cmp(x, j)) j--;
            if (i <= j) swap(i++, j--, a);
        } while (i < j);
        if (i < e) quicksort(i, e, a);
        if (j > b) quicksort(b, j, a);
    }

    public void printTuple(Tuple t, PrintWriter out) {
        for (int i = 0; i < schema.getNumCols(); i++) {
            Object data = t.dataAt(i);
            if (data instanceof Integer) {
                out.print(((Integer) data).intValue() + "\t");
            } else if (data instanceof Float) {
                out.print(((Float) data).floatValue() + "\t");
            } else {
                out.print(((String) data) + "\t");
            }
        }
        out.println();
    }

    public boolean GenNextRun() {
        if (PageList.isEmpty()) return true;
        batchsize = ((Batch) PageList.elementAt(0)).size();
        Batch last = (Batch) PageList.elementAt(PageList.size() - 1);
        int numTuple = (PageList.size() - 1) * batchsize + last.size();
        if (numTuple == 0) return true;
        quicksort(0, numTuple - 1, PageList);
        String rfname = "";
        if (parent == OpType.INTERSECT) rfname = "in";
        rfname = rfname + String.valueOf(nodeindex) + "j" + String.valueOf(position) + "s" + "0pass_runs_" + String.valueOf(filenum) + ".tbl";
        try {
            int i;
            ObjectOutputStream out2 = new ObjectOutputStream(new FileOutputStream(rfname));
            i = 0;
            while (i < PageList.size()) {
                for (int j = 0; j < ((Batch) PageList.elementAt(i)).size(); j++) {
                    Tuple t = ((Batch) PageList.elementAt(i)).elementAt(j);
                    out2.writeObject(t);
                }
                i++;
            }
            out2.close();
        } catch (IOException io) {
            System.out.println("GentNextRun:writing the temporay file error");
            return false;
        }
        filenum++;
        return true;
    }

    public boolean deleterunsfile(String filename) {
        File f = new File(filename);
        if (!f.exists()) throw new IllegalArgumentException("Delete: no such file or directory: " + filename);
        if (!f.canWrite()) throw new IllegalArgumentException("Delete: write protected: " + filename);
        boolean success = f.delete();
        return success;
    }

    public boolean open() {
        start = 0;
        eos = false;
        int runs;
        String tbname;
        numBuff = BufferManager.getBuffersPerJoin();
        scan_runs = new Vector();
        if (!opt.open()) return false;
        filenum = 0;
        while (true) {
            int flag = 0;
            for (int i = 0; i < numBuff; i++) {
                Batch tmp = opt.next();
                if (tmp == null || tmp.size() == 0) {
                    GenNextRun();
                    PageList.clear();
                    flag = 1;
                    break;
                }
                PageList.add(tmp);
            }
            if (flag == 1) break;
            GenNextRun();
            PageList.clear();
        }
        opt.close();
        batchsize = Batch.getPageSize() / schema.getTupleSize();
        pass = 0;
        if (filenum <= numBuff - 1) return true;
        do {
            runs = filenum;
            int current_run = 0;
            Vector scans = new Vector(numBuff);
            filenum = 0;
            while (current_run < runs) {
                int i = 0;
                while (current_run < runs && i < numBuff - 1) {
                    tbname = "";
                    if (parent == OpType.INTERSECT) tbname = "in";
                    tbname = String.valueOf(nodeindex) + "j" + String.valueOf(position) + "s" + String.valueOf(pass) + "Pass_runs_" + String.valueOf(current_run);
                    current_run++;
                    i++;
                    Scan tmp = new Scan(tbname, OpType.SCAN);
                    tmp.setSchema(schema);
                    tmp.open();
                    scans.add(tmp);
                }
                for (int j = 0; j < scans.size(); j++) {
                    PageList.add(((Scan) scans.elementAt(j)).next());
                }
                Batch output = new Batch(batchsize);
                while (true) {
                    Tuple mintuple = ((Batch) PageList.elementAt(0)).elementAt(0);
                    int minPageList = 0;
                    Tuple cmptmp;
                    for (int j = 1; j < scans.size(); j++) {
                        cmptmp = ((Batch) PageList.elementAt(0)).elementAt(0);
                        if (cmp(cmptmp, mintuple)) {
                            mintuple = cmptmp;
                            minPageList = j;
                        }
                    }
                    Batch tmp;
                    tmp = (Batch) PageList.elementAt(minPageList);
                    output.add(tmp.elementAt(0));
                    if (output.isFull()) {
                        String rfname;
                        rfname = "";
                        if (parent == OpType.INTERSECT) rfname = "in";
                        try {
                            rfname = rfname + String.valueOf(nodeindex) + "j" + String.valueOf(position) + "s" + String.valueOf(pass + 1) + "pass_runs_" + String.valueOf(filenum) + ".tbl";
                            ObjectOutputStream out2 = new ObjectOutputStream(new FileOutputStream(rfname));
                            for (int j = 0; j < output.size(); j++) out2.writeObject((Tuple) output.elementAt(j));
                            out2.close();
                        } catch (IOException io) {
                            System.out.println("MergeSort:writing the temporay file error");
                            return false;
                        }
                        output.clear();
                    }
                    tmp.remove(0);
                    if (tmp.isEmpty()) {
                        tmp = ((Scan) scans.elementAt(minPageList)).next();
                        if (tmp != null && tmp.size() != 0) PageList.setElementAt(((Scan) scans.elementAt(minPageList)).next(), minPageList); else {
                            PageList.remove(minPageList);
                            ((Scan) scans.elementAt(minPageList)).close();
                            if (!deleterunsfile(((Scan) scans.elementAt(minPageList)).getTabName() + ".tbl")) {
                                System.out.println("Sort:writing the temporay file error");
                                return false;
                            }
                            scans.remove(minPageList);
                            if (PageList.size() == 0) break;
                        }
                    }
                }
                filenum++;
            }
            pass++;
        } while (filenum > numBuff);
        return true;
    }

    public Batch next() {
        if (eos) {
            close();
            return null;
        }
        if (start == 0) {
            int runs = filenum;
            int current_run = 0;
            String tbname;
            while (current_run < runs) {
                tbname = "";
                if (parent == OpType.INTERSECT) tbname = "in";
                tbname = tbname + String.valueOf(nodeindex) + "j" + String.valueOf(position) + "s" + String.valueOf(pass) + "pass_runs_" + String.valueOf(current_run);
                current_run++;
                Scan tmp = new Scan(tbname, OpType.SCAN);
                tmp.setSchema(schema);
                if (!tmp.open()) {
                    System.exit(1);
                }
                scan_runs.add(tmp);
            }
            for (int j = 0; j < scan_runs.size(); j++) {
                PageList.add(((Scan) scan_runs.elementAt(j)).next());
            }
            start = 1;
        }
        Batch output = new Batch(batchsize);
        while (!output.isFull()) {
            Tuple mintuple = ((Batch) PageList.elementAt(0)).elementAt(0);
            int minPageList = 0;
            Tuple cmptmp;
            for (int j = 1; j < scan_runs.size(); j++) {
                cmptmp = ((Batch) PageList.elementAt(0)).elementAt(0);
                if (cmp(cmptmp, mintuple)) {
                    mintuple = cmptmp;
                    minPageList = j;
                }
            }
            Batch tmp;
            tmp = (Batch) PageList.elementAt(minPageList);
            output.add(tmp.elementAt(0));
            tmp.remove(0);
            if (tmp.isEmpty()) {
                tmp = ((Scan) scan_runs.elementAt(minPageList)).next();
                if (tmp != null && tmp.size() != 0) PageList.setElementAt(tmp, minPageList); else {
                    PageList.remove(minPageList);
                    if (!((Scan) scan_runs.elementAt(minPageList)).close()) {
                        System.out.println("failed to close the scans");
                        return output;
                    }
                    if (!deleterunsfile(((Scan) scan_runs.elementAt(minPageList)).getTabName() + ".tbl")) System.out.println("Sort:Delete the temporay file error");
                    scan_runs.remove(minPageList);
                    if (PageList.size() == 0) {
                        eos = true;
                        return output;
                    }
                }
            }
        }
        return output;
    }
}
