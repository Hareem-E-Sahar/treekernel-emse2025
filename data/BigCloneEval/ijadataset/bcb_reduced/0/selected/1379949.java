package gnos.info.frame;

import gnos.util.Conversion;
import java.io.*;
import java.util.*;
import java.util.zip.*;

/** Frames are the unit of indexing and search.
 *
 * A Frame is a set of segments.  Each segment is a set of attributes. 
 * Each attribute has a name and a textual value.
 * An attribute may be {@link Attribute#isStored() stored} with the segment, 
 * in which case it is returned with search hits on the document.  
 * Thus each document should typically contain one or more segments and stored 
 * attributes which uniquely identify it.
 *
 * <p>Note that attributes which are <i>not</i> {@link Attribute#isStored() stored} are
 * <i>not</i> available in documents retrieved from the index, e.g. with {@link
 * Hits#doc(int)}, {@link Searcher#doc(int)} or {@link IndexReader#frame(int)}.
 */
public class Segment implements Serializable {

    public int segmentx;

    public String tag;

    public TreeMap attributes = new TreeMap();

    public boolean isStored = false;

    public File file = null;

    public long charSize = 0;

    public long maxCharSize = 65536;

    public int lastAttribx = 0;

    public long counter = 0;

    public Frame parent_frame = null;

    public TreeMap terms = new TreeMap();

    public Segment(Frame frame, int segmentx, String tag) {
        this.segmentx = segmentx;
        this.tag = tag;
        this.parent_frame = frame;
    }

    public Segment(Frame frame, String tag) {
        this.tag = tag;
        this.parent_frame = frame;
    }

    public Segment(Frame frame, int segmentx, Attrib attrib) {
        this.segmentx = segmentx;
        addAttrib(attrib);
        this.parent_frame = frame;
    }

    public final void addAttrib(Attrib attrib) {
        attrib.attribx = ++lastAttribx;
        attributes.put(new Integer(attrib.attribx), attrib);
    }

    public final void setFile(String path) {
        file = new File(path + "/#_" + tag + ".zip");
    }

    public final void putZipElt(ZipOutputStream out, byte rid, String str) throws Exception {
        short len = (short) str.length();
        byte[] buf = new byte[len + 3];
        buf[0] = rid;
        byte[] vli = Conversion.convertToBytes(len);
        buf[1] = vli[0];
        buf[2] = vli[1];
        byte[] bstr = str.getBytes("UTF-8");
        for (int i = 0; i < bstr.length; i++) buf[i + 3] = bstr[i];
        out.write(buf, 0, buf.length);
    }

    public final void writeAttrib() throws Exception {
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
        out.putNextEntry(new ZipEntry("datagram"));
        Set keys = attributes.keySet();
        Iterator keyIterator = keys.iterator();
        while (keyIterator.hasNext()) {
            Integer key = (Integer) keyIterator.next();
            Attrib attrib = (Attrib) attributes.get(key);
            Set propkeys = attrib.fields.keySet();
            Iterator propIterator = propkeys.iterator();
            while (propIterator.hasNext()) {
                String propkey = (String) propIterator.next();
                String prop = (String) attrib.fields.get(propkey);
                putZipElt(out, (byte) 11, propkey);
                putZipElt(out, (byte) 12, prop);
            }
            putZipElt(out, (byte) 41, attrib.datagram.stringValue);
        }
        out.close();
    }

    public final byte getEltRid(ZipInputStream inp) throws Exception {
        byte[] bytes = new byte[1];
        int n = inp.read(bytes, 0, 1);
        if (n == -1) return -1;
        if (n != 1) System.out.println(" ZipInputStream returned value: " + n);
        return bytes[0];
    }

    public final short getEltLen(ZipInputStream inp) throws Exception {
        byte[] bytes = new byte[2];
        int offset = 0;
        short size = 2;
        while (offset < size) {
            offset += inp.read(bytes, offset, size - offset);
        }
        short len = Conversion.convertToShort(bytes);
        return len;
    }

    public final String getElt(ZipInputStream inp, short len) throws Exception {
        byte[] bytes = new byte[len];
        int n = 0;
        String elt = "";
        int offset = 0;
        while (offset < len) {
            offset += inp.read(bytes, offset, len - offset);
        }
        elt = new String(bytes, 0, len);
        return elt;
    }

    public final void readAttrib() throws Exception {
        System.out.println("<--- " + file.getCanonicalPath());
        ZipInputStream inp = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)));
        ZipEntry entry = inp.getNextEntry();
        boolean stop = false;
        int nn = 0, ii = 0, jj = 0;
        String field = null, prop = null;
        TreeMap fields = null;
        byte rid = 0;
        boolean firstime = true;
        while (!stop) {
            if (!stop) {
                rid = getEltRid(inp);
                if (rid == 11 || rid == 12 || rid == 41) {
                    short len = getEltLen(inp);
                    String stringValue = getElt(inp, len);
                    switch(rid) {
                        case 11:
                            if (firstime) {
                                fields = new TreeMap();
                                firstime = false;
                            }
                            field = new String(stringValue);
                            ii++;
                            break;
                        case 12:
                            prop = new String(stringValue);
                            fields.put(field, prop);
                            jj++;
                            break;
                        case 41:
                            nn++;
                            Attrib attrib = new Attrib(this, fields, stringValue);
                            addAttrib(attrib);
                            firstime = true;
                            break;
                    }
                } else {
                    stop = true;
                }
            }
        }
        System.out.println(ii + "\t" + jj + "\t" + nn + "\t" + rid + "\t" + stop);
        inp.close();
    }

    public void dump() throws Exception {
        System.out.println(" -------- Segment: " + tag + " dump ----------");
        Set keys = attributes.keySet();
        Iterator keyIterator = keys.iterator();
        while (keyIterator.hasNext()) {
            Integer attrix = (Integer) keyIterator.next();
            System.out.println("Attrib Index " + attrix);
            Attrib attrib = (Attrib) attributes.get(attrix);
            attrib.list();
        }
    }

    public void dump(int attrib_index) throws Exception {
        Integer attrix = new Integer(attrib_index);
        System.out.println("---------- Attrib Index " + attrix + " -----------");
        if (attributes.containsKey(attrix)) {
            Attrib attrib = (Attrib) attributes.get(attrix);
            attrib.list_terms();
        }
    }
}
