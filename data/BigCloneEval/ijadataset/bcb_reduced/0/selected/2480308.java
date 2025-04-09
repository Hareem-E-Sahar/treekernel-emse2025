package com.continuent.tungsten.replicator.extractor.mysql;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import com.continuent.tungsten.commons.mysql.MySQLConstants;
import com.continuent.tungsten.commons.mysql.MySQLIOs;
import com.continuent.tungsten.commons.mysql.MySQLPacket;
import com.continuent.tungsten.replicator.extractor.ExtractorException;

/**
 * Defines a client to extract binlog events and store them in local relay files
 * in a fashion similar to MySQL.
 * <p>
 * Public methods are synchronized to ensure a consistent view of client data
 * across threads.
 * 
 * @author <a href="mailto:robert.hodges@continuent.com">Robert Hodges</a>
 */
public class RelayLogClient {

    private static Logger logger = Logger.getLogger(RelayLogClient.class);

    private static byte[] magic = { (byte) 0xfe, 0x62, 0x69, 0x6e };

    private String url = "jdbc:mysql:thin://localhost:3306/";

    private String login = "tungsten";

    private String password = "secret";

    private String binlog = null;

    private String binlogPrefix = "mysql-bin";

    private long offset = 4;

    private String binlogDir = ".";

    private boolean autoClean = true;

    private File relayLog;

    private File relayDir;

    private File binlogIndex;

    private OutputStream relayOutput;

    private long relayBytes;

    private RelayLogPosition logPosition = new RelayLogPosition();

    private Connection conn;

    private InputStream input = null;

    private OutputStream output = null;

    /** Create new relay log client instance. */
    public RelayLogClient() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBinlog() {
        return binlog;
    }

    public void setBinlog(String binlog) {
        this.binlog = binlog;
    }

    public String getBinlogPrefix() {
        return binlogPrefix;
    }

    public void setBinlogPrefix(String binlogPrefix) {
        this.binlogPrefix = binlogPrefix;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String getBinlogDir() {
        return binlogDir;
    }

    public void setBinlogDir(String out) {
        this.binlogDir = out;
    }

    public boolean isAutoClean() {
        return autoClean;
    }

    public void setAutoClean(boolean autoClean) {
        this.autoClean = autoClean;
    }

    /** Connect to MySQL and start pulling down data. */
    public static void main(String[] args) {
        RelayLogClient relayClient = new RelayLogClient();
        String curArg = null;
        try {
            int argIndex = 0;
            while (argIndex < args.length) {
                curArg = args[argIndex++];
                if ("-url".equals(curArg)) relayClient.setUrl(args[argIndex++]); else if ("-login".equals(curArg)) relayClient.setLogin(args[argIndex++]); else if ("-password".equals(curArg)) relayClient.setPassword(args[argIndex++]); else if ("-binlog".equals(curArg)) relayClient.setBinlog(args[argIndex++]); else if ("-offset".equals(curArg)) relayClient.setOffset(new Integer(args[argIndex++])); else if ("-binlogdir".equals(curArg)) relayClient.setBinlogDir(args[argIndex++]); else if ("-autoclean".equals(curArg)) relayClient.setAutoClean(new Boolean(args[argIndex++])); else if ("-help".equals(curArg)) {
                    printUsage();
                    System.exit(0);
                } else {
                    System.out.println("Unrecognized option: " + curArg);
                    printUsage();
                    System.exit(1);
                }
            }
        } catch (Exception e) {
            System.out.println("Invalid or missing data for argument: " + curArg);
            printUsage();
            System.exit(1);
        }
        try {
            relayClient.connect();
            while (true) relayClient.processEvent();
        } catch (Exception e) {
            logger.fatal("Relay client failed with unexpected exception: " + e.getMessage(), e);
        } finally {
            relayClient.disconnect();
        }
        logger.info("Done!");
    }

    private static void printUsage() {
        System.out.println("Usage: BinlogRelayClient2 options");
        System.out.println("Options:");
        System.out.println("  -host <host>         Default: localhost");
        System.out.println("  -port <port>         Default: 3306");
        System.out.println("  -login <login>       Default: tungsten");
        System.out.println("  -password <password> Default: secret");
        System.out.println("  -binlog <binlog>     Default: (1st binlog file)");
        System.out.println("  -offset <offset>     Default: 0");
        System.out.println("  -binlogdir <dir>     Default: current directory");
        System.out.println("  -autoclean <true|false> Default: true");
        System.out.println("  -help                Print help message");
    }

    /**
     * Connect to database and set up relay log transfer. If successful we are
     * ready to transfer binlogs.
     */
    public void connect() throws ExtractorException {
        try {
            logger.info("Connecting to master MySQL server: url=" + url);
            Class.forName("org.drizzle.jdbc.DrizzleDriver");
            conn = DriverManager.getConnection(url, login, password);
        } catch (ClassNotFoundException e) {
            throw new ExtractorException("Unable to load JDBC driver", e);
        } catch (SQLException e) {
            throw new ExtractorException("Unable to connect", e);
        }
        try {
            MySQLIOs io = MySQLIOs.getMySQLIOs(conn);
            input = io.getInput();
            output = io.getOutput();
        } catch (Exception e) {
            throw new ExtractorException("Unable to access IO streams for connection", e);
        }
        this.relayDir = new File(binlogDir);
        if (!relayDir.isDirectory()) throw new ExtractorException("Relay log directory not a directory or does not exist: " + relayDir.getAbsolutePath()); else if (!relayDir.canWrite()) throw new ExtractorException("Relay log directory is not writable: " + relayDir.getAbsolutePath());
        binlogIndex = new File(relayDir, binlogPrefix + ".index");
        if (autoClean) {
            if (binlogIndex.delete()) logger.info("Cleaned up binlog index file: " + binlogIndex.getAbsolutePath());
            String baseLog;
            if (this.binlog == null) baseLog = ""; else baseLog = binlog;
            for (File child : relayDir.listFiles()) {
                if (!child.isFile()) continue; else if (!child.getName().startsWith(this.binlogPrefix)) continue; else if (child.getName().compareTo(baseLog) < 0) continue;
                if (child.delete()) logger.info("Cleaned up binlog file: " + child.getAbsolutePath());
            }
        }
        try {
            logger.info("Requesting binlog data from master: " + binlog + ":" + offset);
            sendBinlogDumpPacket(output);
        } catch (IOException e) {
            throw new ExtractorException("Error sending request to dump binlog", e);
        }
    }

    /**
     * Process next event packet from MySQL.
     */
    public void processEvent() throws ExtractorException, InterruptedException {
        MySQLPacket packet = MySQLPacket.readPacket(input);
        int length = packet.getDataLength();
        int number = packet.getPacketNumber();
        short type = packet.getUnsignedByte();
        if (logger.isDebugEnabled()) {
            logger.debug("Received packet: number=" + number + " length=" + length + " type=" + type);
        }
        switch(type) {
            case 0:
                try {
                    processBinlogEvent(packet);
                } catch (IOException e) {
                    throw new ExtractorException("Error processing binlog: " + e.getMessage(), e);
                }
                break;
            case 0xFE:
                throw new ExtractorException("EOF packet received");
            case 0xFF:
                int errno = packet.getShort();
                packet.getByte();
                String sqlState = packet.getString(5);
                String errMsg = packet.getString();
                String msg = "Error packet received: errno=" + errno + " sqlstate=" + sqlState + " error=" + errMsg;
                throw new ExtractorException(msg);
            default:
                throw new ExtractorException("Unexpected response while fetching binlog data: packet=" + packet.toString());
        }
    }

    /**
     * Clean up after termination.
     */
    public void disconnect() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.warn("Unable to close connection", e);
            }
        }
        try {
            closeBinlog();
        } catch (IOException e) {
            logger.warn("Unable to close binlog", e);
        }
    }

    /**
     * Returns the current relay log position.
     */
    public RelayLogPosition getPosition() {
        return logPosition.clone();
    }

    /**
     * Sends a binlog dump request to server.
     * 
     * @param out Output stream on which to write packet to server
     */
    private void sendBinlogDumpPacket(OutputStream out) throws IOException {
        MySQLPacket packet = new MySQLPacket(200, (byte) 0);
        packet.putByte((byte) MySQLConstants.COM_BINLOG_DUMP);
        packet.putInt32((int) offset);
        packet.putInt16(0);
        packet.putInt32(25);
        if (binlog != null) packet.putString(binlog);
        packet.write(out);
        out.flush();
    }

    /**
     * Process a binlog event using light parsing to detect the binlog position,
     * timestamp, and the event type. We need to detect ROTATE_LOG events.
     * 
     * @param packet
     * @throws IOException
     */
    private void processBinlogEvent(MySQLPacket packet) throws IOException {
        long timestamp = packet.getUnsignedInt32();
        int typeCode = packet.getUnsignedByte();
        long serverId = packet.getUnsignedInt32();
        long eventLength = packet.getUnsignedInt32();
        long nextPosition = packet.getUnsignedInt32();
        int flags = packet.getUnsignedShort();
        if (logger.isDebugEnabled()) {
            StringBuffer sb = new StringBuffer("Reading binlog event:");
            sb.append(" timestamp=").append(timestamp);
            sb.append(" type_code=").append(typeCode);
            sb.append(" server_id=").append(serverId);
            sb.append(" event_length=").append(eventLength);
            sb.append(" next_position=").append(nextPosition);
            sb.append(" flags=").append(flags);
            logger.debug(sb.toString());
        }
        if (typeCode == MysqlBinlog.ROTATE_EVENT) {
            offset = packet.getLong();
            binlog = packet.getString();
            if (logger.isDebugEnabled()) {
                StringBuffer sb2 = new StringBuffer("ROTATE_EVENT:");
                sb2.append(" next_start_offset=").append(offset);
                sb2.append(" next_binlog_name=").append(binlog);
                logger.debug(sb2.toString());
            }
            if (this.relayOutput != null) {
                writePacketToRelayLog(packet);
                closeBinlog();
            }
        } else {
            writePacketToRelayLog(packet);
        }
    }

    private void writePacketToRelayLog(MySQLPacket packet) throws IOException {
        if (relayOutput == null) openBinlog();
        blindlyWriteToRelayLog(packet, false);
        while (packet.getDataLength() >= MySQLPacket.MAX_LENGTH) {
            packet = MySQLPacket.readPacket(input);
            if (logger.isDebugEnabled()) {
                logger.debug("Read extended packet: number=" + packet.getPacketNumber() + " length=" + packet.getDataLength());
            }
            blindlyWriteToRelayLog(packet, true);
        }
    }

    /**
     * Writes data into the relay log file.
     * 
     * @param packet Network packet to be written
     * @param extended If false, this is a base packet with 5 byte header
     *            including type; if true, this is a follow-on packet with 4
     *            byte header (not including type field)
     * @throws IOException
     */
    private void blindlyWriteToRelayLog(MySQLPacket packet, boolean extended) throws IOException {
        byte[] bytes = packet.getByteBuffer();
        int header;
        if (extended) header = 4; else header = 5;
        int writeLength = bytes.length - header;
        if (logger.isDebugEnabled()) {
            logger.debug("Writing packet to binlog: bytesLength=" + bytes.length + " writeLength=" + writeLength);
        }
        relayOutput.write(bytes, header, writeLength);
        relayOutput.flush();
        relayBytes += writeLength;
        logPosition.setPosition(relayLog, relayBytes);
    }

    private void openBinlog() throws IOException {
        relayLog = new File(relayDir, binlog);
        logger.info("Opening relay log: name=" + relayLog.getAbsolutePath());
        try {
            this.relayOutput = new FileOutputStream(relayLog);
        } catch (FileNotFoundException e) {
            logger.error("Unable to open file for output: " + relayLog.getAbsolutePath(), e);
            return;
        }
        relayOutput.write(magic);
        relayOutput.flush();
        relayBytes = 4;
        logger.info("Adding relay log to binlog index: " + binlogIndex.getAbsolutePath());
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(binlogIndex, true);
            OutputStreamWriter writer = new OutputStreamWriter(fos);
            PrintWriter printer = new PrintWriter(writer);
            printer.println(relayLog);
            printer.flush();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
        }
        this.logPosition.setPosition(relayLog, relayBytes);
    }

    private void closeBinlog() throws IOException {
        if (relayOutput != null) {
            logger.info("Closing relay log: name=" + relayLog.getAbsolutePath() + " bytes=" + relayBytes);
            relayOutput.flush();
            relayOutput.close();
            relayOutput = null;
            relayLog = null;
        }
    }
}
