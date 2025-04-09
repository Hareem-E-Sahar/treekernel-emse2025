package dovetaildb.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.WrappedException;
import dovetaildb.bytes.Bytes;

public class Util {

    public static final byte[] EMPTY_BYTES = new byte[] {};

    public static <T> ArrayList<T> singletonList(T item) {
        ArrayList<T> l = new ArrayList<T>();
        l.add(item);
        return l;
    }

    public static <K, V> HashMap<V, Map<K, V>> indexBy(List<Map<K, V>> rows, K key) {
        HashMap<V, Map<K, V>> results = new HashMap<V, Map<K, V>>();
        for (Map<K, V> row : rows) {
            results.put(row.get(key), row);
        }
        return results;
    }

    public static <K, V> HashSet<V> findUniqueValues(List<Map<K, V>> rows, K key) {
        HashSet<V> results = new HashSet<V>();
        for (Map<K, V> row : rows) {
            results.add(row.get(key));
        }
        return results;
    }

    public static <K, V> LiteralHashMap literalMap() {
        return new LiteralHashMap<K, V>();
    }

    public static <T> LiteralHashSet literalSet() {
        return new LiteralHashSet<T>();
    }

    public static <T> LiteralList literalList() {
        return new LiteralList<T>();
    }

    public static String genUUID() {
        return UUID.generate();
    }

    public static <K, V1, V2> Map<V1, V2> getMapsHashMap(Map<K, Map<V1, V2>> map, K key) {
        if (map.containsKey(key)) return map.get(key);
        Map<V1, V2> ret = new HashMap<V1, V2>();
        map.put(key, ret);
        return ret;
    }

    public static String socketToString(Socket socket) {
        return "(" + socket.getLocalSocketAddress().toString() + socket.getRemoteSocketAddress().toString() + ")";
    }

    public static String jsStackTrace(Throwable t) {
        StringBuffer trace = new StringBuffer();
        for (StackTraceElement e : t.getStackTrace()) {
            String fileName = e.getFileName();
            if (fileName != null && fileName.endsWith(".js")) trace.append(e.toString() + "\n");
        }
        return trace.toString();
    }

    public static RhinoException ensureRhino(Exception e) {
        if (e instanceof RhinoException) return (RhinoException) e; else return new WrappedException(e);
    }

    public static long sizeDir(File file) {
        if (file.isFile()) return file.length();
        File[] files = file.listFiles();
        long size = 0;
        if (files != null) {
            for (int i = 0; i < files.length; i++) size += sizeDir(files[i]);
        }
        return size;
    }

    public static String readFully(File file) {
        try {
            return readFully(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readFully(InputStream resultStream) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(resultStream));
            String line;
            StringBuffer buf = new StringBuffer();
            while ((line = r.readLine()) != null) {
                buf.append(line);
            }
            return buf.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int objToInt(Object obj, int defaultValue) {
        if (obj == null) return defaultValue; else if (obj instanceof Number) return ((Number) obj).intValue(); else if (obj instanceof String) return Integer.parseInt((String) obj); else throw new RuntimeException("Value not an integer: " + obj);
    }

    public static String getTraces(int i1, int i2) {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        StringBuffer b = new StringBuffer();
        for (int i = i1; i <= i2; i++) {
            if (i >= elements.length) break;
            b.append(elements[i].toString());
            b.append('\n');
        }
        return b.toString();
    }

    public static String encodeBytes(byte[] bytes) {
        try {
            return new String(bytes, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] decodeString(String s) {
        try {
            return s.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String bytesAsString(byte[] bytes) {
        StringBuffer buf = new StringBuffer();
        for (byte b : bytes) buf.append((char) b);
        return buf.toString();
    }

    public static String bytesAsString(ByteBuffer tsf, int bufIdxMin, int bufIdxMax) {
        StringBuffer buf = new StringBuffer();
        for (int i = bufIdxMin; i < bufIdxMax; i++) buf.append((char) tsf.get(i));
        return buf.toString();
    }

    public static int compareBytes(byte[] b1, byte[] b2) {
        int max = Math.min(b1.length, b2.length);
        for (int i = 0; i < max; i++) {
            int byte1 = (b1[i] & 0xff);
            int byte2 = (b2[i] & 0xff);
            int diff = byte1 - byte2;
            if (diff != 0) return diff;
        }
        return b1.length - b2.length;
    }

    public static int compareBytes(byte[] b1, byte[] b2, int o1, int o2, int l1, int l2) {
        int max = Math.min(l1, l2);
        for (int l = 0; l < max; l++) {
            int byte1 = (b1[o1 + l] & 0xff);
            int byte2 = (b2[o2 + l] & 0xff);
            int diff = byte1 - byte2;
            if (diff != 0) return diff;
        }
        return l1 - l2;
    }

    public static int leBytesToInt(byte[] a, int i) {
        return ((a[i] & 0xFF) << 8 * 0) | ((a[i + 1] & 0xFF) << 8 * 1) | ((a[i + 2] & 0xFF) << 8 * 2) | ((a[i + 3] & 0xFF) << 8 * 3);
    }

    public static long leBytesToUInt(byte[] a, int i) {
        return ((a[i] & 0xFFL) << 8 * 0) | ((a[i + 1] & 0xFFL) << 8 * 1) | ((a[i + 2] & 0xFFL) << 8 * 2) | ((a[i + 3] & 0xFFL) << 8 * 3);
    }

    public static int leBytesToUShort(byte[] a, int i) {
        return ((a[i] & 0xFF) << 8 * 0) | ((a[i + 1] & 0xFF) << 8 * 1);
    }

    public static void leIntToBytes(int i, byte[] a, int offset) {
        a[offset] = (byte) ((i >>> 8 * 0) & 0xFF);
        a[offset + 1] = (byte) ((i >>> 8 * 1) & 0xFF);
        a[offset + 2] = (byte) ((i >>> 8 * 2) & 0xFF);
        a[offset + 3] = (byte) ((i >>> 8 * 3) & 0xFF);
    }

    public static void leUIntToBytes(long i, byte[] a, int offset) {
        a[offset] = (byte) ((i >>> 8 * 0) & 0xFF);
        a[offset + 1] = (byte) ((i >>> 8 * 1) & 0xFF);
        a[offset + 2] = (byte) ((i >>> 8 * 2) & 0xFF);
        a[offset + 3] = (byte) ((i >>> 8 * 3) & 0xFF);
    }

    public static void leShortToBytes(int i, byte[] a, int offset) {
        a[offset] = (byte) ((i >>> 8 * 0) & 0xFF);
        a[offset + 1] = (byte) ((i >>> 8 * 1) & 0xFF);
    }

    public static void beLongToBytes(long i, byte[] a, int offset) {
        a[offset + 7] = (byte) ((i >>> 8 * 0) & 0xFF);
        a[offset + 6] = (byte) ((i >>> 8 * 1) & 0xFF);
        a[offset + 5] = (byte) ((i >>> 8 * 2) & 0xFF);
        a[offset + 4] = (byte) ((i >>> 8 * 3) & 0xFF);
        a[offset + 3] = (byte) ((i >>> 8 * 0) & 0xFF);
        a[offset + 2] = (byte) ((i >>> 8 * 1) & 0xFF);
        a[offset + 1] = (byte) ((i >>> 8 * 2) & 0xFF);
        a[offset + 0] = (byte) ((i >>> 8 * 3) & 0xFF);
    }

    public static long beBytesToLong(byte[] a, int i) {
        return (((long) a[0] & 0xFF) << 8 * 7) | (((long) a[1] & 0xFF) << 8 * 6) | (((long) a[2] & 0xFF) << 8 * 5) | (((long) a[3] & 0xFF) << 8 * 4) | (((long) a[4] & 0xFF) << 8 * 3) | (((long) a[5] & 0xFF) << 8 * 2) | (((long) a[6] & 0xFF) << 8 * 1) | (((long) a[7] & 0xFF) << 8 * 0);
    }

    public static boolean incrementBinary(byte[] a) {
        for (int i = a.length - 1; i >= 0; i--) {
            int v = a[i] & 0xff;
            if (v < 255) {
                a[i] = (byte) (v + 1);
                return true;
            } else {
                a[i] = 0;
            }
        }
        return false;
    }

    public static File createTempDirectory(String name) {
        try {
            final File temp;
            temp = File.createTempFile(name, null);
            if (!(temp.delete())) {
                throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
            }
            if (!(temp.mkdir())) {
                throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
            }
            return temp;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    public static byte[] appendByte(byte[] prefix, byte suffix) {
        byte[] newBytes = new byte[prefix.length + 1];
        System.arraycopy(prefix, 0, newBytes, 0, prefix.length);
        newBytes[prefix.length] = suffix;
        return newBytes;
    }

    public static Object jsonDecode(String json) {
        JSONParser parser = new JSONParser();
        try {
            return parser.parse(json);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static void jsonEncode(Writer wtr, Object o) {
        try {
            JSONValue.writeJSONString(o, wtr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
