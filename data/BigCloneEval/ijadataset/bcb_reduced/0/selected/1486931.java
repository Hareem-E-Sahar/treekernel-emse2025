package org.maverickdbms.database.lh;

import java.io.RandomAccessFile;
import java.io.IOException;
import java.util.Iterator;
import org.maverickdbms.basic.mvArray;
import org.maverickdbms.basic.mvConstants;
import org.maverickdbms.basic.mvConstantString;
import org.maverickdbms.basic.mvException;
import org.maverickdbms.basic.Factory;
import org.maverickdbms.basic.File;
import org.maverickdbms.basic.LockedException;
import org.maverickdbms.basic.Key;
import org.maverickdbms.basic.Program;
import org.maverickdbms.basic.mvString;
import org.maverickdbms.basic.util.Tree;

/**
* lhFile provides a representation of a Linear Hash type file.
*
* Format was determined from this newsgroup posting:
* http://www.sprezzatura.com/revmedia/v3i1a1.htm
*
*/
public class lhFile implements File {

    protected Factory factory;

    private RandomAccessFile lk;

    private RandomAccessFile ov;

    private int framesize = lhFrame.DEFAULT_FRAME_SIZE;

    private lhFrame frame0;

    private long lklength;

    private long ovlength;

    private int split_threshold = 0xcc;

    private int merge_threshold = split_threshold - 25;

    private lhHash hash;

    private lhFrame[] spareFrames = new lhFrame[20];

    private int spareFrameCount = 0;

    private lhFrame[] overflowList = null;

    private Tree programs;

    private Tree records;

    private lhGroup cacheGroup = null;

    private Resolver resolver;

    lhFile(Resolver resolver, Factory f, lhHash h, RandomAccessFile lk, RandomAccessFile ov) throws IOException {
        this.resolver = resolver;
        factory = f;
        hash = h;
        this.lk = lk;
        this.ov = ov;
        lk.seek(0);
        frame0 = getFrame();
        frame0.read(lk);
        int framesize0 = framesize;
        framesize = frame0.getFramesize();
        if (framesize != framesize0) {
            lk.seek(0);
            frame0 = getFrame();
            frame0.read(lk);
        }
        lklength = lk.length();
        split_threshold = frame0.getThreshold() & 0xFF;
        merge_threshold = split_threshold - 25;
        programs = factory.getTree(Tree.TYPE_AVL);
        records = factory.getTree(Tree.TYPE_AVL);
    }

    void addInUse(int amount) throws IOException {
        frame0.addInUse(amount);
    }

    synchronized void addProgram(Program program) throws mvException {
        ProgramNode pn = new ProgramNode();
        pn.program = program;
        pn = (ProgramNode) programs.probe(pn);
        pn.refCount++;
        if (lk == null || ov == null) {
            throw new mvException(0, "lk/ov is null!");
        }
    }

    void addRecordCount(int amount) throws IOException {
        frame0.addRecordCount(amount);
    }

    public synchronized mvConstantString CLEARFILE(Program program, mvString status, boolean locked) throws mvException {
        try {
            if (cacheGroup != null) {
                cacheGroup.close();
            }
            int modulo = frame0.getModulo();
            while (modulo % 2 == 0) modulo >>= 1;
            frame0.setModulo(modulo);
            frame0.setRecordCount(0);
            frame0.setInUse(0);
            for (int i = 0; i < modulo; i++) {
                lhGroup g = getGroup(i);
                g.clear();
                g.write();
                g.close();
            }
            lklength = modulo * framesize;
            lk.setLength(lklength);
            ov.setLength(0);
            overflowList = null;
            return RETURN_SUCCESS;
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
            throw new mvException(0, ioe);
        }
    }

    public synchronized mvConstantString CLOSE(Program program, mvString status) throws mvException {
        ProgramNode pn = new ProgramNode();
        pn.program = program;
        if (programs.containsKey(pn)) {
            pn = (ProgramNode) programs.probe(pn);
            if (--pn.refCount <= 0) {
                programs.remove(pn);
            }
        } else {
            throw new mvException(0, "Program does not have file opened!");
        }
        if (pn.refCount <= 0) {
            synchronized (records) {
                Iterator iter = records.iterator();
                while (iter.hasNext()) {
                    RecordNode rn = (RecordNode) iter.next();
                    if (program == rn.program) {
                        iter.remove();
                        records.notifyAll();
                    }
                }
            }
        }
        if (programs.isEmpty() && lk != null && ov != null) {
            try {
                if (frame0.isDirty()) {
                    lk.seek(0);
                    frame0.write(lk);
                }
                if (overflowList != null) {
                    int position = 0;
                    for (int i = 0; i < overflowList.length; i++) {
                        if (overflowList[i].isDirty()) {
                            ov.seek(position * framesize);
                            overflowList[i].write(ov);
                        }
                        position = overflowList[i].getNextList();
                    }
                }
                lk.close();
                ov.close();
                lk = null;
                ov = null;
            } catch (IOException ioe) {
                throw new mvException(0, ioe);
            }
        }
        resolver.closeFile(program, this);
        return RETURN_SUCCESS;
    }

    private synchronized mvConstantString del(mvConstantString record) throws mvException {
        lhGroup group = null;
        try {
            group = getGroup(record);
            mvConstantString rtn = group.deleteRecord(record);
            if (rtn == RETURN_SUCCESS) {
                group.write();
                if (frame0.isDirty()) {
                    lk.seek(0);
                    frame0.write(lk);
                }
                long inuse = frame0.getInUse();
                long threshold = inuse * 255;
                threshold /= lklength;
                if (threshold < merge_threshold) {
                    group.close();
                    group = null;
                    merge();
                }
            }
            return rtn;
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
            throw new mvException(0, ioe);
        } finally {
            if (group != null) group.close();
        }
    }

    public mvConstantString DELETE(Program program, mvConstantString record, mvString status, boolean locked) throws LockedException, mvException {
        RecordNode rn = new RecordNode();
        rn.program = program;
        rn.record = factory.getString();
        rn.record.set(record);
        synchronized (records) {
            while (((RecordNode) records.probe(rn)).program != program) {
                if (locked) {
                    throw new LockedException(0);
                } else {
                    try {
                        records.wait();
                    } catch (InterruptedException ie) {
                    }
                }
            }
        }
        try {
            return del(record);
        } finally {
            synchronized (records) {
                rn = (RecordNode) records.remove(rn);
                if (rn != null) {
                    records.notifyAll();
                }
            }
        }
    }

    public mvConstantString DELETEU(Program program, mvConstantString record, mvString status, boolean locked) throws LockedException, mvException {
        RecordNode rn = new RecordNode();
        rn.program = program;
        rn.record = factory.getString();
        rn.record.set(record);
        synchronized (records) {
            while (((RecordNode) records.probe(rn)).program != program) {
                if (locked) {
                    throw new LockedException(0);
                } else {
                    try {
                        records.wait();
                    } catch (InterruptedException ie) {
                    }
                }
            }
        }
        return del(record);
    }

    public mvString FILEINFO(mvString result, mvConstantString var, mvString status) throws mvException {
        result.clear();
        throw new mvException(0, "Sorry FILEINFO has not been implemented.");
    }

    lhFrame getFrame() {
        if (spareFrameCount > 0) {
            lhFrame frame = spareFrames[--spareFrameCount];
            spareFrames[spareFrameCount] = null;
            return frame;
        } else {
            return new lhFrame(framesize);
        }
    }

    synchronized boolean isClosed() {
        return (programs.isEmpty() || lk == null || ov == null);
    }

    public synchronized mvConstantString MATREAD(mvArray arr, mvConstantString record, mvString status) throws mvException {
        lhGroup group = null;
        try {
            group = getGroup(record);
            return group.getRecord(arr, record);
        } catch (IOException ioe) {
            throw new mvException(0, ioe);
        } finally {
            if (group != null) group.close();
        }
    }

    public mvConstantString MATREADU(Program program, mvArray arr, mvConstantString record, mvString status, boolean locked) throws LockedException, mvException {
        RecordNode rn = new RecordNode();
        rn.program = program;
        rn.record = factory.getString();
        rn.record.set(record);
        synchronized (records) {
            while (((RecordNode) records.probe(rn)).program != program) {
                if (locked) {
                    throw new LockedException(0);
                } else {
                    try {
                        records.wait();
                    } catch (InterruptedException ie) {
                    }
                }
            }
        }
        return MATREAD(arr, record, status);
    }

    public mvConstantString MATWRITE(Program program, mvArray arr, mvConstantString record, mvString status, boolean locked) throws mvException {
        mvString[] a = arr.toStringArray();
        int len = arr.size();
        switch(len) {
            case 0:
                return WRITE(program, mvConstantString.EMPTY, record, status, locked);
            case 1:
                return WRITE(program, a[1], record, status, locked);
            default:
                mvString var = factory.getString();
                var.set(a[1]);
                for (int i = 2; i < len; i++) {
                    var.append(mvConstants.FM);
                    var.append(a[i]);
                }
                return WRITE(program, var, record, status, locked);
        }
    }

    void putFrame(lhFrame frame) {
        if (spareFrameCount < spareFrames.length && frame != null) {
            spareFrames[spareFrameCount++] = frame;
        }
    }

    public synchronized mvConstantString READ(mvString var, mvConstantString record, mvString status) throws mvException {
        lhGroup group = null;
        try {
            group = getGroup(record);
            return group.getRecord(var, record);
        } catch (IOException ioe) {
            throw new mvException(0, ioe);
        } finally {
            if (group != null) group.close();
        }
    }

    public mvConstantString READT(mvString var, mvConstantString record) throws mvException {
        throw new mvException(0, "Sorry READT has not been implemented.");
    }

    public mvConstantString READU(Program program, mvString var, mvConstantString record, mvString status, boolean locked) throws LockedException, mvException {
        RecordNode rn = new RecordNode();
        rn.program = program;
        rn.record = factory.getString();
        rn.record.set(record);
        synchronized (records) {
            while (((RecordNode) records.probe(rn)).program != program) {
                if (locked) {
                    throw new LockedException(0);
                } else {
                    try {
                        records.wait();
                    } catch (InterruptedException ie) {
                    }
                }
            }
        }
        return READ(var, record, status);
    }

    public synchronized mvConstantString READV(mvString var, mvConstantString record, mvConstantString attrib, mvString status) throws mvException {
        lhGroup group = null;
        try {
            group = getGroup(record);
            return group.getAttribute(var, record, attrib.intValue());
        } catch (IOException ioe) {
            throw new mvException(0, ioe);
        } finally {
            if (group != null) group.close();
        }
    }

    public mvConstantString READVU(Program program, mvString var, mvConstantString record, mvConstantString attribute, mvString status, boolean locked) throws LockedException, mvException {
        RecordNode rn = new RecordNode();
        rn.program = program;
        rn.record = factory.getString();
        rn.record.set(record);
        synchronized (records) {
            while (((RecordNode) records.probe(rn)).program != program) {
                if (locked) {
                    throw new LockedException(0);
                } else {
                    try {
                        records.wait();
                    } catch (InterruptedException ie) {
                    }
                }
            }
        }
        return READV(var, record, attribute, status);
    }

    public void RELEASE(Program program, mvString status) throws mvException {
        synchronized (records) {
            Iterator iter = records.iterator();
            while (iter.hasNext()) {
                RecordNode rn = (RecordNode) iter.next();
                if (program == rn.program) {
                    iter.remove();
                    records.notifyAll();
                }
            }
        }
    }

    public void RELEASE(Program program, mvConstantString record, mvString status) throws mvException {
        synchronized (records) {
            Iterator iter = records.iterator();
            while (iter.hasNext()) {
                RecordNode rn = (RecordNode) iter.next();
                if (program == rn.program && record.equals(rn.record)) {
                    iter.remove();
                    records.notifyAll();
                }
            }
        }
    }

    public synchronized void SELECT(Program program, mvString var, Key key) throws mvException {
        try {
            lhList list = new lhList(program, factory, this, key);
            if (key.isImmediate()) list.eval();
            var.setList(list);
        } catch (IOException ioe) {
            throw new mvException(0, ioe);
        }
    }

    /**
    * Gets an overflow frame from the free pool
    */
    int getFreeOverflow() throws IOException {
        lhFrame[] ovs = getFreeOverflowList();
        int size = ovs[0].getSize() - 1;
        byte[] ff = ovs[0].getFrame();
        int index = ovs[0].getDataOffset();
        int ovindex = (ff[index] & 0xFF) | ((ff[index + 1] & 0xFF) << 8) | ((ff[index + 2] & 0xFF) << 16) | ((ff[index + 3] & 0xFF) << 24);
        if (size > 0) {
            byte b1 = 0;
            byte b2 = 0;
            byte b3 = 0;
            byte b4 = 0;
            byte b5, b6, b7, b8;
            for (int i = ovs.length - 1; i >= 0; i--) {
                ff = ovs[i].getFrame();
                index = ovs[i].getDataOffset();
                b5 = ff[index];
                b6 = ff[index + 1];
                b7 = ff[index + 2];
                b8 = ff[index + 3];
                int len = framesize - index - 4;
                System.arraycopy(ff, index + 4, ff, index, len);
                ovs[i].setSize(size);
                ff[framesize - 4] = b1;
                ff[framesize - 3] = b2;
                ff[framesize - 2] = b3;
                ff[framesize - 1] = b4;
                b1 = b5;
                b2 = b6;
                b3 = b7;
                b4 = b8;
            }
        } else {
            ff[index] = (byte) ((ovindex + 1) & 0xFF);
            ff[index + 1] = (byte) (((ovindex + 1) >> 8) & 0xFF);
            ff[index + 2] = (byte) (((ovindex + 1) >> 16) & 0xFF);
            ff[index + 3] = (byte) (((ovindex + 1) >> 24) & 0xFF);
        }
        return ovindex;
    }

    /**
    * Get the free overflow frame list from the overflow file
    */
    private lhFrame[] getFreeOverflowList() throws IOException {
        if (overflowList != null) return overflowList;
        lhFrame[] ovs = new lhFrame[10];
        ov.seek(0);
        int ovindex = 0;
        ovs[ovindex] = getFrame();
        if (ov.length() == 0) {
            byte[] data = { 2, 0, 0, 0 };
            ovs[ovindex].setOverflowFreeListFrame(1, 0, framesize, data, 0, data.length);
            ovs[ovindex].write(ov);
        } else {
            ovs[ovindex].read(ov);
        }
        int next = ovs[ovindex++].getNextList();
        while (next != 0) {
            ovs[ovindex] = getFrame();
            ov.seek(next * framesize - framesize);
            ovs[ovindex].read(ov);
            next = ovs[ovindex++].getNextList();
            if (ovindex >= ovs.length) {
                lhFrame[] tmp = ovs;
                ovs = new lhFrame[tmp.length + (tmp.length >> 1)];
                System.arraycopy(tmp, 0, ovs, 0, tmp.length);
            }
        }
        overflowList = new lhFrame[ovindex];
        System.arraycopy(ovs, 0, overflowList, 0, ovindex);
        return overflowList;
    }

    int getGroupCount() throws IOException {
        return (int) (lklength / framesize);
    }

    lhGroup getGroup(int groupno) throws IOException {
        if (cacheGroup != null) {
            if (groupno == cacheGroup.getGroup() && cacheGroup.refCount > 0) {
                cacheGroup.refCount++;
                return cacheGroup;
            }
            cacheGroup.close();
        }
        if (groupno == 0) {
            cacheGroup = new lhGroup(this, lk, ov, groupno, frame0);
        } else {
            cacheGroup = new lhGroup(this, lk, ov, groupno, framesize);
        }
        cacheGroup.refCount++;
        return cacheGroup;
    }

    lhGroup getGroup(mvConstantString key) throws IOException {
        int framecount = (int) (lklength / framesize);
        int modulo = frame0.getModulo();
        int groupno2 = (modulo > 0) ? hash.hash(key, modulo) : 0;
        if (groupno2 >= framecount) {
            groupno2 = hash.hash(key, modulo >> 1);
        }
        if (groupno2 >= framecount) {
            throw new IOException("bad group number " + groupno2);
        }
        return getGroup(groupno2);
    }

    private void merge() throws IOException {
        int modulo = frame0.getModulo();
        if (modulo % 2 != 0) return;
        int framecount = (int) (lklength / framesize);
        int newframecount = framecount;
        int srcoffset = 0;
        if (modulo == framecount) {
            newframecount = (modulo >> 1) + (modulo >> 2);
            srcoffset = modulo >> 2;
        } else {
            modulo >>= 1;
            newframecount = modulo;
        }
        for (int i = newframecount; i < framecount; i++) {
            lhGroup g1 = getGroup(i);
            lhGroup g2 = getGroup(srcoffset + i - newframecount);
            g2.merge(g1);
            g2.write();
            g2.close();
            g1.clear();
            g1.write();
            g1.close();
        }
        lklength = newframecount * framesize;
        lk.setLength(lklength);
        frame0.setModulo(modulo);
        lk.seek(0);
        frame0.write(lk);
    }

    void putFreeOverflow(int frameno) throws IOException {
        overflowList = getFreeOverflowList();
        int size = overflowList[0].getSize();
        int offset = overflowList[0].getDataOffset();
        int capacity = (framesize - offset) * overflowList.length;
        if ((++size << 2) > capacity) {
            lhFrame[] tmp = overflowList;
            overflowList = new lhFrame[tmp.length + 1];
            System.arraycopy(tmp, 0, overflowList, 0, tmp.length);
            overflowList[tmp.length - 1].setNextList(frameno);
            byte[] data = new byte[0];
            overflowList[tmp.length] = getFrame();
            overflowList[tmp.length].setOverflowFreeListFrame(size, 0, framesize, data, 0, data.length);
            return;
        }
        byte b1 = (byte) (frameno & 0xFF);
        byte b2 = (byte) ((frameno >> 8) & 0xFF);
        byte b3 = (byte) ((frameno >> 16) & 0xFF);
        byte b4 = (byte) ((frameno >> 24) & 0xFF);
        byte b5, b6, b7, b8;
        for (int i = 0; i < overflowList.length; i++) {
            byte[] b = overflowList[i].getFrame();
            b5 = b[framesize - 4];
            b6 = b[framesize - 3];
            b7 = b[framesize - 2];
            b8 = b[framesize - 1];
            int offset2 = overflowList[i].getDataOffset();
            int len = framesize - offset2 - 4;
            System.arraycopy(b, offset2, b, offset2 + 4, len);
            overflowList[i].setSize(size);
            b[offset] = b1;
            b[offset + 1] = b2;
            b[offset + 2] = b3;
            b[offset + 3] = b4;
            b1 = b5;
            b2 = b6;
            b3 = b7;
            b4 = b8;
        }
        return;
    }

    private void split() throws IOException {
        int framecount = (int) (lklength / framesize);
        int modulo = frame0.getModulo();
        int newframecount = framecount;
        int srcoffset = 0;
        if (modulo == framecount) {
            modulo <<= 1;
            newframecount += framecount >> 1;
            if (newframecount == framecount) newframecount++;
        } else {
            if (framecount > modulo) {
                throw new IOException("framecount = " + framecount + " modulo = " + modulo);
            }
            newframecount = modulo;
            srcoffset = modulo >> 2;
        }
        for (int i = framecount; i < newframecount; i++) {
            lhGroup g = getGroup(srcoffset + i - framecount);
            int reccount = g.getRecordCount();
            byte[] data = new byte[0];
            lhFrame frame = getFrame();
            frame.setPrimaryFrame(0, 0, modulo, framesize, data, 0, data.length);
            lhGroup ngroup = new lhGroup(this, lk, ov, i, frame);
            g.split(factory, hash, ngroup, modulo);
            g.write();
            g.close();
            ngroup.write();
            ngroup.close();
        }
        frame0.setModulo(modulo);
        lk.seek(0);
        frame0.write(lk);
        lklength = newframecount * framesize;
    }

    private synchronized mvConstantString write(mvConstantString var, mvConstantString record) throws mvException {
        lhGroup group = null;
        try {
            long beforeinuse = frame0.getInUse();
            group = getGroup(record);
            group.putRecord(record, var);
            group.write();
            if (frame0.isDirty()) {
                lk.seek(0);
                frame0.write(lk);
            }
            long inuse = frame0.getInUse();
            long threshold = inuse * 255;
            threshold /= lklength;
            if (inuse > beforeinuse && threshold > split_threshold) {
                group.close();
                group = null;
                split();
            } else if (inuse < beforeinuse && threshold < merge_threshold) {
                group.close();
                group = null;
                merge();
            }
            if (overflowList != null) {
                int position = 0;
                for (int i = 0; i < overflowList.length; i++) {
                    if (overflowList[i].isDirty()) {
                        ov.seek(position * framesize);
                        overflowList[i].write(ov);
                    }
                    position = overflowList[i].getNextList();
                }
            }
            return RETURN_SUCCESS;
        } catch (IOException ioe) {
            throw new mvException(0, ioe);
        } finally {
            if (group != null) group.close();
        }
    }

    public mvConstantString WRITE(Program program, mvConstantString var, mvConstantString record, mvString status, boolean locked) throws mvException {
        try {
            return write(var, record);
        } finally {
            RecordNode rn = new RecordNode();
            rn.program = program;
            rn.record = factory.getString();
            rn.record.set(record);
            synchronized (records) {
                rn = (RecordNode) records.remove(rn);
                if (rn != null) {
                    records.notifyAll();
                }
            }
        }
    }

    public mvConstantString WRITET(mvConstantString var, mvConstantString record) throws mvException {
        throw new mvException(0, "Sorry WRITET has not been implemented.");
    }

    public mvConstantString WRITEU(Program program, mvConstantString var, mvConstantString record, mvString status, boolean locked) throws LockedException, mvException {
        RecordNode rn = new RecordNode();
        rn.program = program;
        rn.record = factory.getString();
        rn.record.set(record);
        synchronized (records) {
            while (((RecordNode) records.probe(rn)).program != program) {
                if (locked) {
                    throw new LockedException(0);
                } else {
                    try {
                        records.wait();
                    } catch (InterruptedException ie) {
                    }
                }
            }
        }
        return write(var, record);
    }

    public mvConstantString WRITEV(Program program, mvConstantString var, mvConstantString record, mvConstantString attrib, mvString status, boolean locked) throws mvException {
        throw new mvException(0, "Sorry WRITEV has not been implemented.");
    }

    public mvConstantString WRITEVU(Program program, mvConstantString var, mvConstantString record, mvConstantString attrib, mvString status, boolean locked) throws mvException {
        throw new mvException(0, "Sorry WRITEVU has not been implemented.");
    }
}
