package org.maverickdbms.database.lh;

import java.io.RandomAccessFile;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import org.maverickdbms.basic.Array;
import org.maverickdbms.basic.ConstantString;
import org.maverickdbms.basic.MaverickException;
import org.maverickdbms.basic.Factory;
import org.maverickdbms.basic.LockedException;
import org.maverickdbms.basic.Key;
import org.maverickdbms.basic.Program;
import org.maverickdbms.basic.MaverickString;
import org.maverickdbms.basic.util.Tree;

/**
* File provides a representation of a Linear Hash type file.
*
* Format was determined from this newsgroup posting:
* http://www.sprezzatura.com/revmedia/v3i1a1.htm
*
*/
public class File implements org.maverickdbms.basic.File {

    protected Factory factory;

    private RandomAccessFile lk;

    private RandomAccessFile ov;

    private int framesize = lhFrame.DEFAULT_FRAME_SIZE;

    private lhFrame frame0;

    private long lklength;

    private int split_threshold = 0xcc;

    private int merge_threshold = split_threshold - 25;

    private lhHash hash;

    private lhFrame[] spareFrames = new lhFrame[20];

    private int spareFrameCount = 0;

    private OverflowList overflowList;

    private Tree programs;

    private Tree records;

    private Group cacheGroup = null;

    private Resolver resolver;

    private Charset charset;

    File(Resolver resolver, Factory f, lhHash h, RandomAccessFile lk, RandomAccessFile ov, Charset charset) throws IOException {
        this.resolver = resolver;
        factory = f;
        hash = h;
        this.lk = lk;
        this.ov = ov;
        this.charset = charset;
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

    synchronized void addProgram(Program program) throws MaverickException {
        ProgramNode pn = new ProgramNode();
        pn.program = program;
        pn = (ProgramNode) programs.probe(pn);
        pn.refCount++;
        if (lk == null || ov == null) {
            throw new MaverickException(0, "lk/ov is null!");
        }
    }

    public synchronized ConstantString CLEARFILE(Program program, MaverickString status, boolean locked) throws MaverickException {
        try {
            if (cacheGroup != null) {
                cacheGroup.close();
            }
            int modulo = frame0.getModulo();
            while (modulo % 2 == 0) modulo >>= 1;
            byte[] data = { Group.GROUP_TERMINATOR };
            int inuse = data.length * modulo;
            int size = 0;
            int count = 0;
            frame0.setFirstPrimaryFrame(0, 0, modulo, framesize, inuse, (byte) split_threshold, size, count, data, 0, data.length);
            lhFrame frame = getFrame();
            frame.setPrimaryFrame(0, 0, modulo, framesize, data, 0, data.length);
            lk.seek(0);
            frame0.write(lk);
            for (int i = 1; i < modulo; i++) {
                frame.write(lk);
            }
            lklength = modulo * framesize;
            lk.setLength(lklength);
            ov.setLength(0);
            overflowList = null;
            return ConstantString.RETURN_SUCCESS;
        } catch (IOException ioe) {
            throw new MaverickException(0, ioe);
        }
    }

    public synchronized ConstantString CLOSE(Program program, MaverickString status) throws MaverickException {
        ProgramNode pn = new ProgramNode();
        pn.program = program;
        if (programs.containsKey(pn)) {
            pn = (ProgramNode) programs.probe(pn);
            if (--pn.refCount <= 0) {
                programs.remove(pn);
            }
        } else {
            throw new MaverickException(0, "Program does not have file opened!");
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
                getOverflowList().write(ov);
                lk.close();
                ov.close();
                lk = null;
                ov = null;
            } catch (IOException ioe) {
                throw new MaverickException(0, ioe);
            }
        }
        resolver.closeFile(program, this);
        return ConstantString.RETURN_SUCCESS;
    }

    private synchronized ConstantString del(ConstantString record) throws MaverickException {
        Group group = null;
        try {
            group = getGroup(record);
            int beforePrimaryLength = group.getPrimaryLength();
            ConstantString rtn = group.deleteRecord(record);
            if (rtn == ConstantString.RETURN_SUCCESS) {
                group.write(lk, ov);
                long inuse = frame0.addInUse(group.getPrimaryLength() - beforePrimaryLength);
                frame0.addRecordCount(-1);
                if (frame0.isDirty()) {
                    lk.seek(0);
                    frame0.write(lk);
                }
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
            throw new MaverickException(0, ioe);
        } finally {
            if (group != null) group.close();
        }
    }

    public ConstantString DELETE(Program program, ConstantString record, MaverickString status, boolean locked) throws LockedException, MaverickException {
        try {
            return del(record);
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

    public ConstantString DELETEU(Program program, ConstantString record, MaverickString status, boolean locked) throws LockedException, MaverickException {
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

    public MaverickString FILEINFO(MaverickString result, ConstantString var, MaverickString status) throws MaverickException {
        result.clear();
        throw new MaverickException(0, "Sorry FILEINFO has not been implemented.");
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

    OverflowList getOverflowList() throws IOException {
        if (overflowList == null) {
            overflowList = new OverflowList(this);
            overflowList.read(ov, framesize);
        }
        return overflowList;
    }

    synchronized boolean isClosed() {
        return (programs.isEmpty() || lk == null || ov == null);
    }

    public synchronized ConstantString MATREAD(Array arr, ConstantString record, boolean overflowLast, MaverickString status) throws MaverickException {
        Group group = null;
        try {
            group = getGroup(record);
            return group.getRecord(arr, record, overflowLast);
        } catch (IOException ioe) {
            throw new MaverickException(0, ioe);
        } finally {
            if (group != null) group.close();
        }
    }

    public ConstantString MATREADU(Program program, Array arr, ConstantString record, boolean overflowLast, MaverickString status, boolean locked) throws LockedException, MaverickException {
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
        return MATREAD(arr, record, overflowLast, status);
    }

    public ConstantString MATWRITE(Program program, Array arr, ConstantString record, MaverickString status, boolean locked) throws MaverickException {
        int len = arr.size();
        switch(len) {
            case 0:
                return WRITE(program, ConstantString.EMPTY, record, status, locked);
            case 1:
                return WRITE(program, arr.get(ConstantString.ONE), record, status, locked);
            default:
                MaverickString var = factory.getString();
                arr.MATBUILD(var, ConstantString.ONE, factory.getConstant(len), ConstantString.FM);
                return WRITE(program, var, record, status, locked);
        }
    }

    public ConstantString MATWRITEU(Program program, Array arr, ConstantString record, MaverickString status, boolean locked) throws LockedException, MaverickException {
        int len = arr.size();
        switch(len) {
            case 0:
                return WRITEU(program, ConstantString.EMPTY, record, status, locked);
            case 1:
                return WRITEU(program, arr.get(ConstantString.ONE), record, status, locked);
            default:
                MaverickString var = factory.getString();
                arr.MATBUILD(var, ConstantString.ZERO, ConstantString.ZERO, ConstantString.FM);
                return WRITEU(program, var, record, status, locked);
        }
    }

    void putFrame(lhFrame frame) {
        if (spareFrameCount < spareFrames.length && frame != null) {
            spareFrames[spareFrameCount++] = frame;
        }
    }

    public synchronized ConstantString READ(MaverickString var, ConstantString record, MaverickString status) throws MaverickException {
        Group group = null;
        try {
            group = getGroup(record);
            return group.getRecord(var, record);
        } catch (IOException ioe) {
            throw new MaverickException(0, ioe);
        } finally {
            if (group != null) group.close();
        }
    }

    void readOverflowFrame(lhFrame frame, long offset) throws IOException {
        ov.seek(offset);
        frame.read(ov);
        if (frame.getType() != lhFrame.TYPE_SUBSEQUENT_OVERFLOW) {
            throw new IOException("Frame is not of subsequent overflow type.  Offset: " + offset);
        }
    }

    public ConstantString READT(MaverickString var, ConstantString record) throws MaverickException {
        throw new MaverickException(0, "Sorry READT has not been implemented.");
    }

    public ConstantString READU(Program program, MaverickString var, ConstantString record, MaverickString status, boolean locked) throws LockedException, MaverickException {
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

    public synchronized ConstantString READV(MaverickString var, ConstantString record, ConstantString attribute, MaverickString status) throws MaverickException {
        Group group = null;
        try {
            group = getGroup(record);
            return group.getAttribute(var, record, attribute.intValue());
        } catch (IOException ioe) {
            throw new MaverickException(0, ioe);
        } finally {
            if (group != null) group.close();
        }
    }

    public ConstantString READVU(Program program, MaverickString var, ConstantString record, ConstantString attribute, MaverickString status, boolean locked) throws LockedException, MaverickException {
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

    public void RELEASE(Program program, MaverickString status) throws MaverickException {
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

    public void RELEASE(Program program, ConstantString record, MaverickString status) throws MaverickException {
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

    public synchronized void SELECT(Program program, MaverickString var, Key key) throws MaverickException {
        try {
            List list = new List(program, factory, this, key);
            if (key.isImmediate()) list.eval();
            var.setList(list);
        } catch (IOException ioe) {
            throw new MaverickException(0, ioe);
        }
    }

    int getGroupCount() throws IOException {
        return (int) (lklength / framesize);
    }

    Group getGroup(int groupno) throws IOException {
        if (cacheGroup != null) {
            if (groupno == cacheGroup.getGroup() && cacheGroup.refCount > 0) {
                cacheGroup.refCount++;
                return cacheGroup;
            }
            cacheGroup.close();
        }
        if (groupno == 0) {
            cacheGroup = new Group(this, groupno, frame0, charset);
        } else {
            cacheGroup = new Group(this, lk, ov, groupno, framesize, charset);
        }
        cacheGroup.refCount++;
        return cacheGroup;
    }

    Group getGroup(ConstantString key) throws IOException {
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
        int inuse = 0;
        for (int i = newframecount; i < framecount; i++) {
            Group g1 = getGroup(i);
            Group g2 = getGroup(srcoffset + i - newframecount);
            inuse += g2.merge(g1);
            g2.write(lk, ov);
            g2.close();
            g1.clear();
            g1.write(lk, ov);
            g1.close();
        }
        lklength = newframecount * framesize;
        lk.setLength(lklength);
        frame0.addInUse(inuse);
        frame0.setModulo(modulo);
        lk.seek(0);
        frame0.write(lk);
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
        int inuse = 0;
        for (int i = framecount; i < newframecount; i++) {
            Group g = getGroup(srcoffset + i - framecount);
            int reccount = g.getRecordCount();
            byte[] data = { Group.GROUP_TERMINATOR };
            lhFrame frame = getFrame();
            frame.setPrimaryFrame(0, 0, modulo, framesize, data, 0, data.length);
            Group ngroup = new Group(this, i, frame, charset);
            inuse += g.split(factory, hash, ngroup, modulo);
            g.write(lk, ov);
            g.close();
            ngroup.write(lk, ov);
            ngroup.close();
        }
        frame0.addInUse((int) inuse);
        frame0.setModulo(modulo);
        lk.seek(0);
        frame0.write(lk);
        lklength = newframecount * framesize;
    }

    private synchronized ConstantString write(ConstantString var, ConstantString record) throws MaverickException {
        Group group = null;
        try {
            long beforeinuse = frame0.getInUse();
            group = getGroup(record);
            int beforeRecordCount = group.getRecordCount();
            long inuse = group.putRecord(record, var);
            group.write(lk, ov);
            inuse = frame0.addInUse((int) inuse);
            frame0.addRecordCount(group.getRecordCount() - beforeRecordCount);
            if (frame0.isDirty()) {
                lk.seek(0);
                frame0.write(lk);
            }
            long threshold = inuse * 255 / lklength;
            if (inuse >= beforeinuse && threshold >= split_threshold) {
                group.close();
                group = null;
                split();
            } else if (inuse < beforeinuse && threshold <= merge_threshold) {
                group.close();
                group = null;
                merge();
            }
            getOverflowList().write(ov);
            return ConstantString.RETURN_SUCCESS;
        } catch (IOException ioe) {
            throw new MaverickException(0, ioe);
        } finally {
            if (group != null) group.close();
        }
    }

    public ConstantString WRITE(Program program, ConstantString var, ConstantString record, MaverickString status, boolean locked) throws MaverickException {
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

    public ConstantString WRITET(ConstantString var, ConstantString record) throws MaverickException {
        throw new MaverickException(0, "Sorry WRITET has not been implemented.");
    }

    public ConstantString WRITEU(Program program, ConstantString var, ConstantString record, MaverickString status, boolean locked) throws LockedException, MaverickException {
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

    private synchronized ConstantString writev(ConstantString var, ConstantString record, ConstantString attribute) throws MaverickException {
        Group group = null;
        try {
            MaverickString rec = factory.getString();
            group = getGroup(record);
            if (group.getRecord(rec, record) == ConstantString.RETURN_SUCCESS) {
            } else {
            }
            rec.REPLACE(attribute, ConstantString.ZERO, ConstantString.ZERO, var);
            return write(rec, record);
        } catch (IOException ioe) {
            throw new MaverickException(0, ioe);
        } finally {
            if (group != null) group.close();
        }
    }

    public ConstantString WRITEV(Program program, ConstantString var, ConstantString record, ConstantString attribute, MaverickString status, boolean locked) throws MaverickException {
        try {
            return writev(var, record, attribute);
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

    public ConstantString WRITEVU(Program program, ConstantString var, ConstantString record, ConstantString attribute, MaverickString status, boolean locked) throws LockedException, MaverickException {
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
        return writev(var, record, attribute);
    }
}
