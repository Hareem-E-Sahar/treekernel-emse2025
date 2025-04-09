import java.sql.*;
import java.io.Reader;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.Reader;
import java.io.CharArrayReader;
import javax.sql.rowset.serial.SerialException;
import javax.sql.rowset.serial.SerialBlob;

public class TestJDBC {

    public static void main(String args[]) {
        System.out.println("JDBC BLOB Streaming Test");
        System.out.println("java.class.path: " + System.getProperty("java.class.path"));
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
        }
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/test?user=root&password=&enableBlobStreaming=true");
            execute(conn, "CREATE TABLE IF NOT EXISTS mybs_test_tab (n_id int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT, n_text LONGBLOB) ENGINE=PBXT");
            funcTest(conn);
            bigDataTest(conn);
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            ex.printStackTrace();
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e2) {
                }
            }
        }
    }

    static final int SET_ASCII_STREAM_TEST = 1;

    static final int SET_BINARY_STREAM_TEST = 2;

    static final int SET_BLOB_TEST = 3;

    static final int SET_BLOB_STREAM_TEST = 4;

    static final int SET_CHAR_STREAM_TEST = 5;

    static final int SET_CLOB_TEST = 6;

    static final int SET_CLOB_READER_TEST = 7;

    public static class BigByteStream extends InputStream {

        int size;

        int pos;

        public BigByteStream(int s) {
            size = s;
            pos = 0;
        }

        public int read() {
            if (pos == size) return -1;
            int ch = pos % 256;
            pos++;
            return ch;
        }

        public int getLength() {
            return size;
        }
    }

    public static class LocalBlob extends SerialBlob {

        public LocalBlob(byte[] b) throws SerialException, SQLException {
            super(b);
        }

        public byte[] getBytes(long pos, int length) throws SerialException {
            if (pos >= 1 && length == 0) return new byte[0];
            return super.getBytes(pos, length);
        }
    }

    public static class StreamBuffer {

        public int type;

        public byte buffer[];

        public StreamBuffer(int typ, byte buf[]) {
            type = typ;
            buffer = buf;
        }

        public InputStream getStream() {
            return new ByteArrayInputStream(buffer);
        }

        public long getLength() {
            return (long) buffer.length;
        }

        public Blob getBlob() throws SerialException, SQLException {
            return new LocalBlob(buffer);
        }

        public char[] getChars() {
            char chars[] = new char[buffer.length];
            for (int i = 0; i < buffer.length; i++) chars[i] = (char) buffer[i];
            return chars;
        }

        public Clob getClob() throws SerialException, SQLException {
            return new javax.sql.rowset.serial.SerialClob(getChars());
        }

        public Reader getReader() {
            return new CharArrayReader(getChars());
        }
    }

    public static class BigDownload extends Thread {

        public void run() {
            Connection conn = null;
            try {
                conn = DriverManager.getConnection("jdbc:mysql://localhost/test?user=root&password=&enableBlobStreaming=true");
                ResultSet rs = executeQuery(conn, "select n_text from mybs_test_tab order by n_id");
                while (rs.next()) {
                    Blob blob = rs.getBlob(1);
                    InputStream in = blob.getBinaryStream();
                    try {
                        for (int i = 0; i < blob.length(); i++) {
                            int ch = in.read();
                            if (ch != (i % 256)) throw new IOException("Error reading stream");
                        }
                    } finally {
                        in.close();
                    }
                }
            } catch (SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
                ex.printStackTrace();
            } catch (Exception e2) {
                e2.printStackTrace();
            } finally {
                try {
                    if (conn != null) conn.close();
                } catch (Exception e3) {
                    e3.printStackTrace();
                }
            }
        }
    }

    public static void bigDataTest(Connection conn) throws IOException, SQLException {
        System.out.println("BIG DATA TEST:");
        execute(conn, "delete from mybs_test_tab");
        Object list[] = new Object[1];
        for (int i = 0; i < 10; i++) {
            list[0] = new BigByteStream(100000);
            execute(conn, "insert mybs_test_tab(n_text) values(?)", list);
        }
        ResultSet rs = executeQuery(conn, "select n_text from mybs_test_tab order by n_id");
        while (rs.next()) {
            Blob blob = rs.getBlob(1);
            InputStream in = blob.getBinaryStream();
            try {
                for (int i = 0; i < blob.length(); i++) {
                    int ch = in.read();
                    if (ch != (i % 256)) throw new IOException("Error reading stream");
                }
            } finally {
                in.close();
            }
        }
        System.out.println("Getting data using multiple threads...");
        int threads = 5;
        BigDownload t[] = new BigDownload[threads];
        for (int i = 0; i < threads; i++) t[i] = new BigDownload();
        try {
            for (int i = 0; i < threads; i++) t[i].start();
            for (int i = 0; i < threads; i++) t[i].join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Done OK");
    }

    public static void funcTest(Connection conn) throws IOException, SQLException {
        System.out.println("FUNCTIONALITY TEST:");
        execute(conn, "delete from mybs_test_tab");
        Object list[] = new Object[1];
        list[0] = new StreamBuffer(SET_ASCII_STREAM_TEST, "ABC This is a test stream DEF".getBytes());
        execute(conn, "insert mybs_test_tab(n_text) values(?)", list);
        list[0] = new StreamBuffer(SET_BINARY_STREAM_TEST, "ABC This is a test stream DEF".getBytes());
        execute(conn, "insert mybs_test_tab(n_text) values(?)", list);
        list[0] = new StreamBuffer(SET_BLOB_TEST, "ABC This is a test stream DEF".getBytes());
        execute(conn, "insert mybs_test_tab(n_text) values(?)", list);
        list[0] = new StreamBuffer(SET_ASCII_STREAM_TEST, "".getBytes());
        execute(conn, "insert mybs_test_tab(n_text) values(?)", list);
        list[0] = new StreamBuffer(SET_BINARY_STREAM_TEST, "".getBytes());
        execute(conn, "insert mybs_test_tab(n_text) values(?)", list);
        list[0] = new StreamBuffer(SET_BLOB_TEST, "".getBytes());
        execute(conn, "insert mybs_test_tab(n_text) values(?)", list);
        list[0] = null;
        execute(conn, "insert mybs_test_tab(n_text) values(?)", list);
        list[0] = null;
        execute(conn, "insert mybs_test_tab(n_text) values(?)", list);
        list[0] = null;
        execute(conn, "insert mybs_test_tab(n_text) values(?)", list);
        System.out.println("getBinaryStream():");
        execute(conn, "select n_id, n_text from mybs_test_tab order by n_id", GET_BINARY_STREAM_TEST);
        System.out.println("getString():");
        execute(conn, "select n_id, n_text from mybs_test_tab order by n_id", GET_STRING_TEST);
        System.out.println("getBytes():");
        execute(conn, "select n_id, n_text from mybs_test_tab order by n_id", GET_BYTES_TEST);
        System.out.println("getAsciiStream():");
        execute(conn, "select n_id, n_text from mybs_test_tab order by n_id", GET_ASCII_STREAM_TEST);
        System.out.println("getBlob():");
        execute(conn, "select n_id, n_text from mybs_test_tab order by n_id", GET_BLOB_TEST);
        System.out.println("getCharacterStream():");
        execute(conn, "select n_id, n_text from mybs_test_tab order by n_id", GET_CHAR_STREAM_TEST);
        System.out.println("getClob():");
        execute(conn, "select n_id, n_text from mybs_test_tab order by n_id", GET_CLOB_TEST);
        System.out.println("getObject():");
        execute(conn, "select n_id, n_text from mybs_test_tab order by n_id", GET_OBJECT_TEST);
    }

    static final int GET_ASCII_STREAM_TEST = 1;

    static final int GET_BINARY_STREAM_TEST = 2;

    static final int GET_BLOB_TEST = 3;

    static final int GET_BYTES_TEST = 4;

    static final int GET_CHAR_STREAM_TEST = 5;

    static final int GET_CLOB_TEST = 6;

    static final int GET_STRING_TEST = 7;

    static final int GET_OBJECT_TEST = 8;

    public static class ByteBuffer {

        int pos = 0;

        byte buffer[] = null;

        public void append(int val) {
            if (buffer == null) buffer = new byte[10]; else if (pos == buffer.length) {
                byte new_buffer[];
                new_buffer = new byte[buffer.length + 10];
                System.arraycopy(buffer, 0, new_buffer, 0, buffer.length);
                buffer = new_buffer;
            }
            buffer[pos] = (byte) val;
            pos++;
        }

        byte[] getBytes() {
            byte new_buffer[];
            if (buffer == null) return null;
            new_buffer = new byte[pos];
            System.arraycopy(buffer, 0, new_buffer, 0, pos);
            return new_buffer;
        }
    }

    public static void printValue(Object obj) throws SQLException, IOException {
        if (obj instanceof Blob) {
            obj = ((Blob) obj).getBinaryStream();
        } else if (obj instanceof Clob) {
            obj = ((Clob) obj).getCharacterStream();
        }
        if (obj instanceof InputStream) {
            ByteBuffer buf = new ByteBuffer();
            try {
                int ch;
                for (; ; ) {
                    ch = ((InputStream) obj).read();
                    if (ch == -1) break;
                    buf.append(ch);
                }
            } finally {
                ((InputStream) obj).close();
            }
            obj = buf.getBytes();
        }
        if (obj instanceof Reader) {
            try {
                int ch;
                for (; ; ) {
                    ch = ((Reader) obj).read();
                    if (ch == -1) break;
                    System.out.print((char) ch);
                }
                System.out.flush();
            } finally {
                ((Reader) obj).close();
            }
        } else if (obj instanceof String) {
            System.out.print((String) obj);
        } else if (obj instanceof byte[]) {
            byte buffer[] = (byte[]) obj;
            boolean binary = false;
            for (int i = 0; i < buffer.length; i++) {
                if (buffer[i] != '\n' && buffer[i] != '\r' && !(buffer[i] >= ' ' && buffer[i] <= 127)) {
                    binary = true;
                    break;
                }
            }
            if (binary) {
                int ch;
                for (int i = 0; i < buffer.length; i++) {
                    ch = buffer[i] >> 4;
                    if (ch >= 0 && ch <= 9) System.out.print((char) ('0' + ch)); else System.out.print((char) ('A' + ch - 10));
                    ch = buffer[i] & 0xF;
                    if (ch >= 0 && ch <= 9) System.out.print((char) ('0' + ch)); else System.out.print((char) ('A' + ch - 10));
                }
            } else {
                for (int i = 0; i < buffer.length; i++) System.out.print((char) buffer[i]);
            }
        } else if (obj == null) System.out.print("null"); else System.out.print(obj.toString());
        System.out.flush();
    }

    public static void printResult(ResultSet rs, int testType) throws SQLException, IOException, SerialException {
        ResultSetMetaData md = rs.getMetaData();
        int colsPerRow = md.getColumnCount();
        int rows = 0;
        int cnt = 1;
        while (rs.next()) {
            rows++;
            for (int i = 1; i <= colsPerRow; i++) {
                String name = md.getTableName(i) + "." + md.getColumnName(i);
                String type = md.getColumnTypeName(i) + "(" + md.getScale(i) + "," + md.getPrecision(i) + ")";
                System.out.print(i + "> " + name + " " + type + " ");
                Object obj = null;
                switch(testType) {
                    case GET_ASCII_STREAM_TEST:
                        obj = rs.getAsciiStream(i);
                        break;
                    case GET_BINARY_STREAM_TEST:
                        obj = rs.getBinaryStream(i);
                        break;
                    case GET_BLOB_TEST:
                        obj = rs.getBlob(i);
                        break;
                    case GET_BYTES_TEST:
                        obj = rs.getBytes(i);
                        break;
                    case GET_CHAR_STREAM_TEST:
                        obj = rs.getCharacterStream(i);
                        break;
                    case GET_CLOB_TEST:
                        obj = rs.getClob(i);
                        break;
                    case GET_STRING_TEST:
                        obj = rs.getString(i);
                        break;
                    case GET_OBJECT_TEST:
                        obj = rs.getObject(i);
                        break;
                }
                if (rs.wasNull()) System.out.println("NULL"); else {
                    printValue(obj);
                    System.out.println("");
                }
            }
            System.out.println("");
        }
        System.out.println("Row count = " + rows);
    }

    public static void printAll(Statement stat, boolean more, int testType) throws SQLException, IOException, SerialException {
        ResultSet rs;
        for (; ; ) {
            if (more) {
                rs = stat.getResultSet();
                printResult(rs, testType);
            } else {
                int cnt = stat.getUpdateCount();
                if (cnt == -1) break;
                System.out.println("Update count = " + cnt);
            }
            more = stat.getMoreResults();
        }
        System.out.println("Execution completed.");
    }

    public static void execute(Connection conn, String sql, int testType, Object list[]) throws SQLException, IOException, SerialException {
        Statement st;
        boolean more;
        if (list == null) {
            st = conn.createStatement();
            more = st.execute(sql);
        } else {
            PreparedStatement ps;
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < list.length; i++) {
                if (list[i] instanceof StreamBuffer) {
                    StreamBuffer bb = (StreamBuffer) list[i];
                    switch(bb.type) {
                        case SET_ASCII_STREAM_TEST:
                            ps.setAsciiStream(i + 1, bb.getStream(), (int) bb.getLength());
                            break;
                        case SET_BINARY_STREAM_TEST:
                            ps.setBinaryStream(i + 1, bb.getStream(), (int) bb.getLength());
                            break;
                        case SET_BLOB_TEST:
                            ps.setBlob(i + 1, bb.getBlob());
                            break;
                        case SET_BLOB_STREAM_TEST:
                            break;
                        case SET_CHAR_STREAM_TEST:
                            ps.setCharacterStream(i + 1, bb.getReader(), (int) bb.getLength());
                            break;
                        case SET_CLOB_TEST:
                            ps.setClob(i + 1, bb.getClob());
                            break;
                        case SET_CLOB_READER_TEST:
                            break;
                    }
                } else if (list[i] instanceof BigByteStream) ps.setBinaryStream(i + 1, (InputStream) list[i], (int) ((BigByteStream) list[i]).getLength()); else ps.setObject(i + 1, list[i]);
            }
            more = ps.execute();
            st = ps;
        }
        try {
            printAll(st, more, testType);
        } finally {
            st.close();
        }
    }

    public static void insertBLOB(Connection conn, InputStream in, int length) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement("insert mybs_test_tab(n_text) values(?)");
            ps.setBinaryStream(1, in, (int) length);
            int rows = ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
        }
    }

    public static InputStream selectBLOB(Connection conn, int id) throws SQLException {
        Statement st = null;
        InputStream in = null;
        try {
            st = conn.createStatement();
            ResultSet rs = st.executeQuery("select n_text from mybs_test_tab where n_id = " + id);
            rs.next();
            in = rs.getBinaryStream("n_text");
        } finally {
            if (st != null) st.close();
        }
        return in;
    }

    public static void execute(Connection conn, String sql, int testType) throws SQLException, IOException {
        execute(conn, sql, testType, null);
    }

    public static void execute(Connection conn, String sql) throws SQLException, IOException {
        execute(conn, sql, GET_STRING_TEST, null);
    }

    public static void execute(Connection conn, String sql, Object list[]) throws SQLException, IOException {
        execute(conn, sql, GET_STRING_TEST, list);
    }

    public static ResultSet executeQuery(Connection conn, String sql) throws SQLException, IOException {
        Statement st;
        st = conn.createStatement();
        return st.executeQuery(sql);
    }
}
