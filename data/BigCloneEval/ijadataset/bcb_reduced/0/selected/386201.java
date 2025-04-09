package dovetaildb.dbservice;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;
import org.json.simple.JSONValue;
import dovetaildb.bytes.ArrayBytes;
import dovetaildb.bytes.Bytes;
import dovetaildb.bytes.CompoundBytes;
import dovetaildb.bytes.SlicedBytes;

public class DbResult implements JSONAware, JSONStreamAware {

    Bytes prefix = ArrayBytes.EMPTY_BYTES;

    Bytes suffix = null;

    SpecializableQueryNode node;

    DbResultMap asMap = null;

    DbResultList asList = null;

    HashMap<String, DbResult> byObjectKey = new HashMap<String, DbResult>();

    ArrayList<DbResult> byArrayIndex = new ArrayList<DbResult>();

    Bytes firstTerm;

    char type;

    ArrayList<String> mapKeys = null;

    public Object simplify() {
        switch(type) {
            case 'l':
                return null;
            case 's':
                return getString();
            case 'n':
                return getDouble();
            case 't':
                return Boolean.TRUE;
            case 'f':
                return Boolean.FALSE;
            case '[':
                return asList;
            case '{':
                return asMap;
        }
    }

    public Bytes[] terms = new Bytes[] { null, null, null, null };

    int numTerms = 0;

    public char getType() {
        return (char) terms[0].get(0);
    }

    private static int binarySearch(Comparable[] a, Comparable x, int highestValidIndex) {
        int low = 0;
        int high = highestValidIndex;
        int mid;
        while (low <= high) {
            mid = (low + high) / 2;
            if (a[mid].compareTo(x) < 0) low = mid + 1; else if (a[mid].compareTo(x) > 0) high = mid - 1; else return mid;
        }
        return low;
    }

    public DbResult derefByKey(String key) {
        Bytes cmpKey = new CompoundBytes(DbServiceUtil.HEADER_BYTE_MAPOPEN, new CompoundBytes(DbServiceUtil.sencodeMapKey(key), DbServiceUtil.HEADER_BYTE_COLON));
        int keyLen = cmpKey.getLength();
        int idx = binarySearch(terms, cmpKey, numTerms);
        Bytes[] subTerms = new Bytes[numTerms - idx];
        int subIdx = 0;
        for (; idx < numTerms; idx++) {
            Bytes curTerm = terms[idx];
            if (cmpKey.isPrefixOf(curTerm)) {
                subTerms[subIdx++] = curTerm.subBytes(keyLen, curTerm.getLength() - keyLen);
            } else {
                break;
            }
        }
        if (subIdx == 0) return null;
        DbResult next = new DbResult();
        next.terms = subTerms;
        next.numTerms = subIdx;
        return next;
    }

    public DbResult derefByIndex(int index) {
        int hiIdxByte = index >> 8;
        int loIdxByte = index & 0xff;
        Bytes[] subTerms = new Bytes[numTerms];
        int subIdx = 0;
        for (int termIdx = 0; termIdx < this.numTerms; termIdx++) {
            Bytes term = terms[termIdx];
            if (term.get(0) != '[') continue;
            int sz = term.getLength();
            if (term.get(sz - 1) == loIdxByte && term.get(sz - 2) == hiIdxByte) {
                subTerms[subIdx++] = SlicedBytes.make(term, 1, sz - 3);
            }
        }
        DbResult next = new DbResult();
        next.terms = subTerms;
        next.numTerms = subIdx;
        return next;
    }

    public Collection<String> getObjectKeys() {
        HashSet<String> keys = new HashSet<String>(terms.length);
        for (int termIdx = 0; termIdx < this.numTerms; termIdx++) {
            Bytes term = terms[termIdx];
            int len = term.getLength();
            for (int i = 1; i < len; i++) {
                if (term.get(i) == ':') {
                    try {
                        keys.add(new String(term.subBytes(1, i - 1).getBytes(), "utf-8"));
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                }
            }
        }
        return keys;
    }

    public int getArrayLength() {
        int maxIdx = -1;
        for (int termIdx = 0; termIdx < this.numTerms; termIdx++) {
            Bytes term = terms[termIdx];
            int sz = term.getLength();
            int idx = (term.get(sz - 2) << 8) | term.get(sz - 1);
            if (idx > maxIdx) maxIdx = idx;
        }
        return maxIdx + 1;
    }

    public String getString() {
        try {
            Bytes term = terms[0];
            byte[] bytes = new byte[term.getLength() - 1];
            term.writeBytes(1, bytes);
            return new String(bytes, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isString() {
        return terms[0].get(0) == 's';
    }

    public boolean isObject() {
        return terms[0].get(0) == '{';
    }

    public boolean isArray() {
        return terms[0].get(0) == '[';
    }

    public boolean isNull() {
        return terms[0].get(0) == 'l';
    }

    public boolean getBoolean() {
        switch(terms[0].get(0)) {
            case 't':
                return true;
            case 'f':
                return false;
            default:
                throw new RuntimeException();
        }
    }

    public double getDouble() {
        Bytes term = terms[0];
        if (term.get(0) != 'n') throw new RuntimeException();
        long bits = (((long) term.get(1)) << 8 * 7) | (((long) term.get(2) & 0xFF) << 8 * 6) | (((long) term.get(3) & 0xFF) << 8 * 5) | (((long) term.get(4) & 0xFF) << 8 * 4) | (((long) term.get(5) & 0xFF) << 8 * 3) | (((long) term.get(6) & 0xFF) << 8 * 2) | (((long) term.get(7) & 0xFF) << 8 * 1) | (((long) term.get(8) & 0xFF) << 8 * 0);
        if ((bits & 0x8000000000000000L) == 0) {
            bits ^= 0xFFFFFFFFFFFFFFFFL;
        } else {
            bits ^= 0x8000000000000000L;
        }
        return Double.longBitsToDouble(bits);
    }

    public DbResult() {
    }

    public void clearTerms() {
        numTerms = 0;
    }

    public void addTerm(Bytes bytes) {
        if (numTerms >= terms.length) {
            Bytes[] newTerms = new Bytes[(terms.length * 3) / 2];
            System.arraycopy(terms, 0, newTerms, 0, terms.length);
            terms = newTerms;
        }
        terms[numTerms] = bytes.copyInto(terms[numTerms]);
        numTerms++;
    }

    public void writeJSONString(Writer w) throws IOException {
        if (numTerms < 1) {
            throw new RuntimeException();
        }
        switch(terms[0].get(0)) {
            case 'l':
                w.write("null");
                break;
            case 't':
                w.write("true");
                break;
            case 'f':
                w.write("false");
                break;
            case 'n':
                w.write(Double.toString(getDouble()));
                break;
            case 's':
                w.write('"');
                w.write(JSONObject.escape(getString()));
                w.write('"');
                break;
            case '{':
                w.write('{');
                boolean first = true;
                for (String key : getObjectKeys()) {
                    if (first) first = false; else w.write(',');
                    w.write('"');
                    w.write(key);
                    w.write("\":");
                    derefByKey(key).writeJSONString(w);
                }
                w.write('}');
                break;
            case '[':
                w.write('[');
                for (int i = 0; i < getArrayLength(); i++) {
                    if (i > 0) w.write(',');
                    derefByIndex(i).writeJSONString(w);
                }
                w.write(']');
                break;
            default:
                throw new RuntimeException();
        }
    }

    public String toJSONString() {
        StringWriter w = new StringWriter();
        try {
            writeJSONString(w);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return w.toString();
    }
}
