package gov.nasa.worldwind.formats.shapefile;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.exception.*;
import gov.nasa.worldwind.util.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

/**
 * @author Patrick Murris
 * @version $Id: DBaseFile.java 1 2011-07-16 23:22:47Z dcollins $
 */
public class DBaseFile extends AVListImpl {

    protected static final int FIXED_HEADER_LENGTH = 32;

    protected static final int FIELD_DESCRIPTOR_LENGTH = 32;

    protected static String[] DBASE_CONTENT_TYPES = { "application/dbase", "application/dbf", "application/octet-stream" };

    protected static class Header {

        public int fileCode;

        public Date lastModificationDate;

        public int numberOfRecords;

        public int headerLength;

        public int recordLength;
    }

    protected Header header;

    protected DBaseField[] fields;

    protected ReadableByteChannel channel;

    protected boolean open;

    protected int numRecordsRead;

    protected ByteBuffer recordBuffer;

    public DBaseFile(Object source) {
        if (source == null || WWUtil.isEmpty(source)) {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        try {
            this.setValue(AVKey.DISPLAY_NAME, source.toString());
            if (source instanceof File) this.initializeFromFile((File) source); else if (source instanceof URL) this.initializeFromURL((URL) source); else if (source instanceof InputStream) this.initializeFromStream((InputStream) source); else if (source instanceof String) this.initializeFromPath((String) source); else {
                String message = Logging.getMessage("generic.UnrecognizedSourceType", source);
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
        } catch (Exception e) {
            String message = Logging.getMessage("SHP.ExceptionAttemptingToReadDBase", this.getValue(AVKey.DISPLAY_NAME));
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            throw new WWRuntimeException(message, e);
        }
    }

    public DBaseFile(InputStream is) {
        if (is == null) {
            String message = Logging.getMessage("nullValue.InputStreamIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        try {
            this.setValue(AVKey.DISPLAY_NAME, is.toString());
            this.initializeFromStream(is);
        } catch (Exception e) {
            String message = Logging.getMessage("SHP.ExceptionAttemptingToReadDBase", this.getValue(AVKey.DISPLAY_NAME));
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            throw new WWRuntimeException(message, e);
        }
    }

    public Date getLastModificationDate() {
        return this.header.lastModificationDate;
    }

    public int getNumberOfRecords() {
        return this.header.numberOfRecords;
    }

    public int getHeaderLength() {
        return this.header.headerLength;
    }

    public int getRecordLength() {
        return this.header.recordLength;
    }

    public int getNumberOfFields() {
        return (this.header.headerLength - 1 - FIXED_HEADER_LENGTH) / FIELD_DESCRIPTOR_LENGTH;
    }

    public DBaseField[] getFields() {
        return this.fields;
    }

    public boolean hasNext() {
        return this.open && this.numRecordsRead < this.header.numberOfRecords;
    }

    public DBaseRecord nextRecord() {
        if (!this.open) {
            String message = Logging.getMessage("SHP.DBaseFileClosed", this.getValue(AVKey.DISPLAY_NAME));
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }
        if (this.getNumberOfRecords() <= 0 || this.numRecordsRead >= this.getNumberOfRecords()) {
            String message = Logging.getMessage("SHP.NoRecords", this.getValue(AVKey.DISPLAY_NAME));
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }
        try {
            return this.readNextRecord();
        } catch (IOException e) {
            String message = Logging.getMessage("SHP.ExceptionAttemptingToReadDBaseRecord", this.getValue(AVKey.DISPLAY_NAME));
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            throw new WWRuntimeException(message, e);
        }
    }

    public void close() {
        if (this.channel != null) {
            WWIO.closeStream(this.channel, null);
            this.channel = null;
        }
        this.open = false;
        this.recordBuffer = null;
    }

    protected void initializeFromFile(File file) throws IOException {
        if (!file.exists()) {
            String message = Logging.getMessage("generic.FileNotFound", file.getPath());
            Logging.logger().severe(message);
            throw new FileNotFoundException(message);
        }
        this.channel = Channels.newChannel(WWIO.getBufferedInputStream(new FileInputStream(file)));
        this.initialize();
    }

    protected void initializeFromURL(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        String message = this.validateURLConnection(connection, DBASE_CONTENT_TYPES);
        if (message != null) {
            throw new IOException(message);
        }
        this.channel = Channels.newChannel(WWIO.getBufferedInputStream(connection.getInputStream()));
        this.initialize();
    }

    protected void initializeFromStream(InputStream stream) throws IOException {
        this.channel = Channels.newChannel(WWIO.getBufferedInputStream(stream));
        this.initialize();
    }

    protected void initializeFromPath(String path) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            this.initializeFromFile(file);
            return;
        }
        URL url = WWIO.makeURL(path);
        if (url != null) {
            this.initializeFromURL(url);
            return;
        }
        String message = Logging.getMessage("generic.UnrecognizedSourceType", path);
        Logging.logger().severe(message);
        throw new IllegalArgumentException(message);
    }

    protected void initialize() throws IOException {
        this.header = this.readHeader();
        this.fields = this.readFields();
        this.open = true;
    }

    protected String validateURLConnection(URLConnection connection, String[] acceptedContentTypes) {
        try {
            if (connection instanceof HttpURLConnection && ((HttpURLConnection) connection).getResponseCode() != HttpURLConnection.HTTP_OK) {
                return Logging.getMessage("HTTP.ResponseCode", ((HttpURLConnection) connection).getResponseCode(), connection.getURL());
            }
        } catch (Exception e) {
            return Logging.getMessage("URLRetriever.ErrorOpeningConnection", connection.getURL());
        }
        String contentType = connection.getContentType();
        if (WWUtil.isEmpty(contentType)) return null;
        for (String type : acceptedContentTypes) {
            if (contentType.trim().toLowerCase().startsWith(type)) return null;
        }
        return Logging.getMessage("HTTP.UnexpectedContentType", contentType, Arrays.toString(acceptedContentTypes));
    }

    /**
     * Reads the {@link Header} from this DBaseFile. This file is assumed to have a header.
     *
     * @return a {@link Header} instance.
     *
     * @throws IOException if the header cannot be read for any reason.
     */
    protected Header readHeader() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(FIXED_HEADER_LENGTH);
        WWIO.readChannelToBuffer(this.channel, buffer);
        if (buffer.remaining() < FIXED_HEADER_LENGTH) {
            throw new WWRuntimeException(Logging.getMessage("generic.InvalidFileLength", buffer.remaining()));
        }
        return this.readHeaderFromBuffer(buffer);
    }

    /**
     * Reads a {@link Header} instance from the given {@link java.nio.ByteBuffer};
     * <p/>
     * The buffer current position is assumed to be set at the start of the header and will be set to the end of the
     * header after this method has completed.
     *
     * @param buffer the Header @link java.nio.ByteBuffer} to read from.
     *
     * @return a {@link Header} instances.
     */
    protected Header readHeaderFromBuffer(ByteBuffer buffer) {
        int pos = buffer.position();
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int fileCode = buffer.get();
        if (fileCode > 5) {
            throw new WWUnrecognizedException(Logging.getMessage("SHP.UnrecognizedDBaseFile", fileCode));
        }
        int yy = 0xFF & buffer.get();
        int mm = buffer.get();
        int dd = buffer.get();
        int numRecords = buffer.getInt();
        int headerLength = buffer.getShort();
        int recordLength = buffer.getShort();
        Header header = new Header();
        header.fileCode = fileCode;
        Calendar cal = Calendar.getInstance();
        cal.set(1900 + yy, mm - 1, dd);
        header.lastModificationDate = cal.getTime();
        header.numberOfRecords = numRecords;
        header.headerLength = headerLength;
        header.recordLength = recordLength;
        buffer.position(pos + FIXED_HEADER_LENGTH);
        return header;
    }

    /**
     * Reads the {@link DBaseField} descriptions from this DBaseFile. This file is assumed to have one or more fields
     * available.
     *
     * @return an array of {@link DBaseField} instances.
     *
     * @throws IOException if the fields cannot be read for any reason.
     */
    protected DBaseField[] readFields() throws IOException {
        int fieldsLength = this.header.headerLength - FIXED_HEADER_LENGTH;
        ByteBuffer buffer = ByteBuffer.allocate(fieldsLength);
        WWIO.readChannelToBuffer(this.channel, buffer);
        return this.readFieldsFromBuffer(buffer, this.getNumberOfFields());
    }

    /**
     * Reads a sequence of {@link DBaseField} descriptions from the given {@link java.nio.ByteBuffer};
     * <p/>
     * The buffer current position is assumed to be set at the start of the sequence and will be set to the end of the
     * sequence after this method has completed.
     *
     * @param buffer    the DBaseField sequence {@link java.nio.ByteBuffer} to read from.
     * @param numFields the number of DBaseFields to read.
     *
     * @return an array of {@link DBaseField} instances.
     */
    protected DBaseField[] readFieldsFromBuffer(ByteBuffer buffer, int numFields) {
        int pos = buffer.position();
        DBaseField[] fields = new DBaseField[numFields];
        for (int i = 0; i < numFields; i++) {
            fields[i] = new DBaseField(this, buffer);
        }
        int fieldsLength = this.header.headerLength - FIXED_HEADER_LENGTH;
        buffer.position(pos + fieldsLength);
        return fields;
    }

    /**
     * Reads the next {@link DBaseRecord} instance from this DBaseFile. This file is assumed to have one or more
     * remaining records available.
     *
     * @return a new {@link DBaseRecord} instance.
     *
     * @throws IOException if the record cannot be read for any reason.
     */
    protected DBaseRecord readNextRecord() throws IOException {
        if (this.recordBuffer == null) this.recordBuffer = ByteBuffer.allocate(this.getRecordLength());
        this.recordBuffer.limit(this.getRecordLength());
        this.recordBuffer.rewind();
        WWIO.readChannelToBuffer(this.channel, this.recordBuffer);
        return this.readRecordFromBuffer(this.recordBuffer, ++this.numRecordsRead);
    }

    /**
     * Reads a {@link DBaseRecord} instance from the given {@link java.nio.ByteBuffer};
     * <p/>
     * The buffer current position is assumed to be set at the start of the record and will be set to the start of the
     * next record after this method has completed.
     *
     * @param buffer       the DBase record {@link java.nio.ByteBuffer} to read from.
     * @param recordNumber the record's sequence number.
     *
     * @return a {@link DBaseRecord} instance.
     */
    protected DBaseRecord readRecordFromBuffer(ByteBuffer buffer, int recordNumber) {
        return new DBaseRecord(this, buffer, recordNumber);
    }

    protected int readZeroTerminatedString(ByteBuffer buffer, byte[] bytes, int maxLength) {
        if (maxLength <= 0) return 0;
        buffer.get(bytes, 0, maxLength);
        int length;
        for (length = 0; length < maxLength && bytes[length] != 0; length++) {
        }
        return length;
    }

    protected String decodeString(byte[] bytes, int length) {
        if (length <= 0) return null;
        try {
            return new String(bytes, 0, length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return new String(bytes, 0, length);
        }
    }

    protected boolean isStringEmpty(byte[] bytes, int length) {
        return length <= 0 || isArrayFilled(bytes, length, (byte) 0x20) || isArrayFilled(bytes, length, (byte) 0x2A);
    }

    protected static boolean isArrayFilled(byte[] bytes, int length, byte fillValue) {
        if (length <= 0) return false;
        for (int i = 0; i < length; i++) {
            if (bytes[i] != fillValue) return false;
        }
        return true;
    }
}
