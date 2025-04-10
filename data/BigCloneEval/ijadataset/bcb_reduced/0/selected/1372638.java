package org.apache.catalina.ha.deploy;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

/**
 * This factory is used to read files and write files by splitting them up into
 * smaller messages. So that entire files don't have to be read into memory.
 * <BR>
 * The factory can be used as a reader or writer but not both at the same time.
 * When done reading or writing the factory will close the input or output
 * streams and mark the factory as closed. It is not possible to use it after
 * that. <BR>
 * To force a cleanup, call cleanup() from the calling object. <BR>
 * This class is not thread safe.
 * 
 * @author Filip Hanik
 * @version 1.0
 */
public class FileMessageFactory {

    public static org.apache.juli.logging.Log log = org.apache.juli.logging.LogFactory.getLog(FileMessageFactory.class);

    /**
     * The number of bytes that we read from file
     */
    public static final int READ_SIZE = 1024 * 10;

    /**
     * The file that we are reading/writing
     */
    protected File file = null;

    /**
     * True means that we are writing with this factory. False means that we are
     * reading with this factory
     */
    protected boolean openForWrite;

    /**
     * Once the factory is used, it can not be reused.
     */
    protected boolean closed = false;

    /**
     * When openForWrite=false, the input stream is held by this variable
     */
    protected FileInputStream in;

    /**
     * When openForWrite=true, the output stream is held by this variable
     */
    protected FileOutputStream out;

    /**
     * The number of messages we have read or written
     */
    protected int nrOfMessagesProcessed = 0;

    /**
     * The total size of the file
     */
    protected long size = 0;

    /**
     * The total number of packets that we split this file into
     */
    protected long totalNrOfMessages = 0;

    /**
     * The bytes that we hold the data in, not thread safe.
     */
    protected byte[] data = new byte[READ_SIZE];

    /**
     * Private constructor, either instantiates a factory to read or write. <BR>
     * When openForWrite==true, then a the file, f, will be created and an
     * output stream is opened to write to it. <BR>
     * When openForWrite==false, an input stream is opened, the file has to
     * exist.
     * 
     * @param f
     *            File - the file to be read/written
     * @param openForWrite
     *            boolean - true means we are writing to the file, false means
     *            we are reading from the file
     * @throws FileNotFoundException -
     *             if the file to be read doesn't exist
     * @throws IOException -
     *             if the system fails to open input/output streams to the file
     *             or if it fails to create the file to be written to.
     */
    private FileMessageFactory(File f, boolean openForWrite) throws FileNotFoundException, IOException {
        this.file = f;
        this.openForWrite = openForWrite;
        if (log.isDebugEnabled()) log.debug("open file " + f + " write " + openForWrite);
        if (openForWrite) {
            if (!file.exists()) file.createNewFile();
            out = new FileOutputStream(f);
        } else {
            size = file.length();
            totalNrOfMessages = (size / READ_SIZE) + 1;
            in = new FileInputStream(f);
        }
    }

    /**
     * Creates a factory to read or write from a file. When opening for read,
     * the readMessage can be invoked, and when opening for write the
     * writeMessage can be invoked.
     * 
     * @param f
     *            File - the file to be read or written
     * @param openForWrite
     *            boolean - true, means we are writing to the file, false means
     *            we are reading from it
     * @throws FileNotFoundException -
     *             if the file to be read doesn't exist
     * @throws IOException -
     *             if it fails to create the file that is to be written
     * @return FileMessageFactory
     */
    public static FileMessageFactory getInstance(File f, boolean openForWrite) throws FileNotFoundException, IOException {
        return new FileMessageFactory(f, openForWrite);
    }

    /**
     * Reads file data into the file message and sets the size, totalLength,
     * totalNrOfMsgs and the message number <BR>
     * If EOF is reached, the factory returns null, and closes itself, otherwise
     * the same message is returned as was passed in. This makes sure that not
     * more memory is ever used. To remember, neither the file message or the
     * factory are thread safe. dont hand off the message to one thread and read
     * the same with another.
     * 
     * @param f
     *            FileMessage - the message to be populated with file data
     * @throws IllegalArgumentException -
     *             if the factory is for writing or is closed
     * @throws IOException -
     *             if a file read exception occurs
     * @return FileMessage - returns the same message passed in as a parameter,
     *         or null if EOF
     */
    public FileMessage readMessage(FileMessage f) throws IllegalArgumentException, IOException {
        checkState(false);
        int length = in.read(data);
        if (length == -1) {
            cleanup();
            return null;
        } else {
            f.setData(data, length);
            f.setTotalLength(size);
            f.setTotalNrOfMsgs(totalNrOfMessages);
            f.setMessageNumber(++nrOfMessagesProcessed);
            return f;
        }
    }

    /**
     * Writes a message to file. If (msg.getMessageNumber() ==
     * msg.getTotalNrOfMsgs()) the output stream will be closed after writing.
     * 
     * @param msg
     *            FileMessage - message containing data to be written
     * @throws IllegalArgumentException -
     *             if the factory is opened for read or closed
     * @throws IOException -
     *             if a file write error occurs
     * @return returns true if the file is complete and outputstream is closed,
     *         false otherwise.
     */
    public boolean writeMessage(FileMessage msg) throws IllegalArgumentException, IOException {
        if (!openForWrite) throw new IllegalArgumentException("Can't write message, this factory is reading.");
        if (log.isDebugEnabled()) log.debug("Message " + msg + " data " + msg.getData() + " data length " + msg.getDataLength() + " out " + out);
        if (out != null) {
            out.write(msg.getData(), 0, msg.getDataLength());
            nrOfMessagesProcessed++;
            out.flush();
            if (msg.getMessageNumber() == msg.getTotalNrOfMsgs()) {
                out.close();
                cleanup();
                return true;
            }
        } else {
            if (log.isWarnEnabled()) log.warn("Receive Message again -- Sender ActTimeout to short [ path: " + msg.getContextPath() + " war: " + msg.getFileName() + " data: " + msg.getData() + " data length: " + msg.getDataLength() + " ]");
        }
        return false;
    }

    /**
     * Closes the factory, its streams and sets all its references to null
     */
    public void cleanup() {
        if (in != null) try {
            in.close();
        } catch (Exception ignore) {
        }
        if (out != null) try {
            out.close();
        } catch (Exception ignore) {
        }
        in = null;
        out = null;
        size = 0;
        closed = true;
        data = null;
        nrOfMessagesProcessed = 0;
        totalNrOfMessages = 0;
    }

    /**
     * Check to make sure the factory is able to perform the function it is
     * asked to do. Invoked by readMessage/writeMessage before those methods
     * proceed.
     * 
     * @param openForWrite
     *            boolean
     * @throws IllegalArgumentException
     */
    protected void checkState(boolean openForWrite) throws IllegalArgumentException {
        if (this.openForWrite != openForWrite) {
            cleanup();
            if (openForWrite) throw new IllegalArgumentException("Can't write message, this factory is reading."); else throw new IllegalArgumentException("Can't read message, this factory is writing.");
        }
        if (this.closed) {
            cleanup();
            throw new IllegalArgumentException("Factory has been closed.");
        }
    }

    /**
     * Example usage.
     * 
     * @param args
     *            String[], args[0] - read from filename, args[1] write to
     *            filename
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Usage: FileMessageFactory fileToBeRead fileToBeWritten");
        System.out.println("Usage: This will make a copy of the file on the local file system");
        FileMessageFactory read = getInstance(new File(args[0]), false);
        FileMessageFactory write = getInstance(new File(args[1]), true);
        FileMessage msg = new FileMessage(null, args[0], args[0]);
        msg = read.readMessage(msg);
        System.out.println("Expecting to write " + msg.getTotalNrOfMsgs() + " messages.");
        int cnt = 0;
        while (msg != null) {
            write.writeMessage(msg);
            cnt++;
            msg = read.readMessage(msg);
        }
        System.out.println("Actually wrote " + cnt + " messages.");
    }

    public File getFile() {
        return file;
    }
}
