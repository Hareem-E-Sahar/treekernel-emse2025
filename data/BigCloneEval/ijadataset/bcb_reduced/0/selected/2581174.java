package uk.co.marcoratto.apache.ssh;

import java.io.File;
import java.io.IOException;
import java.io.EOFException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import uk.co.marcoratto.scp.listeners.Listener;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;

/**
 * A helper object representing an scp download.
 */
public class ScpFromMessage extends AbstractSshMessage {

    private static final int HUNDRED_KILOBYTES = 102400;

    private static final byte LINE_FEED = 0x0a;

    private static final int BUFFER_SIZE = 1024;

    private String remoteFile;

    private File localFile;

    private boolean isRecursive = false;

    private boolean preserveLastModified = false;

    /**
     * Constructor for ScpFromMessage
     * @param session the ssh session to use
     */
    public ScpFromMessage(Session session) {
        super(session);
    }

    /**
     * Constructor for ScpFromMessage
     * @param verbose if true do verbose logging
     * @param session the ssh session to use
     * @since Ant 1.7
     */
    public ScpFromMessage(boolean verbose, Session session) {
        super(verbose, session);
    }

    /**
     * Constructor for ScpFromMessage.
     * @param verbose if true log extra information
     * @param session the Scp session to use
     * @param aRemoteFile the remote file name
     * @param aLocalFile  the local file
     * @param recursive   if true use recursion (-r option to scp)
     * @since Ant 1.6.2
     */
    public ScpFromMessage(boolean verbose, Session session, String aRemoteFile, File aLocalFile, boolean recursive) {
        this(false, session, aRemoteFile, aLocalFile, recursive, false);
    }

    /**
     * Constructor for ScpFromMessage.
     * @param session the Scp session to use
     * @param aRemoteFile the remote file name
     * @param aLocalFile  the local file
     * @param recursive   if true use recursion (-r option to scp)
     */
    public ScpFromMessage(Session session, String aRemoteFile, File aLocalFile, boolean recursive) {
        this(false, session, aRemoteFile, aLocalFile, recursive);
    }

    /**
     * Constructor for ScpFromMessage.
     * @param verbose if true log extra information
     * @param session the Scp session to use
     * @param aRemoteFile the remote file name
     * @param aLocalFile  the local file
     * @param recursive   if true use recursion (-r option to scp)
     * @param preserveLastModified whether to preserve file
     * modification times
     * @since Ant 1.8.0
     */
    public ScpFromMessage(boolean verbose, Session session, String aRemoteFile, File aLocalFile, boolean recursive, boolean preserveLastModified) {
        super(verbose, session);
        this.remoteFile = aRemoteFile;
        this.localFile = aLocalFile;
        this.isRecursive = recursive;
        this.preserveLastModified = preserveLastModified;
    }

    /**
     * Carry out the transfer.
     * @throws IOException on i/o errors
     * @throws JSchException on errors detected by scp
     * @throws ScpException 
     */
    public void execute() throws IOException, JSchException, ScpException {
        String command = "scp -f ";
        if (isRecursive) {
            command += "-r ";
        }
        command += remoteFile;
        Channel channel = openExecChannel(command);
        try {
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();
            channel.connect();
            sendAck(out);
            startRemoteCpProtocol(in, out, localFile);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
        log("done\n");
    }

    protected boolean getPreserveLastModified() {
        return preserveLastModified;
    }

    private void startRemoteCpProtocol(InputStream in, OutputStream out, File localFile) throws IOException, JSchException, ScpException {
        File startFile = localFile;
        while (true) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            while (true) {
                int read = in.read();
                if (read < 0) {
                    return;
                }
                if ((byte) read == LINE_FEED) {
                    break;
                }
                stream.write(read);
            }
            String serverResponse = stream.toString("UTF-8");
            if (serverResponse.charAt(0) == 'C') {
                parseAndFetchFile(serverResponse, startFile, out, in);
            } else if (serverResponse.charAt(0) == 'D') {
                startFile = parseAndCreateDirectory(serverResponse, startFile);
                sendAck(out);
            } else if (serverResponse.charAt(0) == 'E') {
                startFile = startFile.getParentFile();
                sendAck(out);
            } else if (serverResponse.charAt(0) == '\01' || serverResponse.charAt(0) == '\02') {
                throw new IOException(serverResponse.substring(1));
            }
        }
    }

    private File parseAndCreateDirectory(String serverResponse, File localFile) {
        int start = serverResponse.indexOf(" ");
        start = serverResponse.indexOf(" ", start + 1);
        String directoryName = serverResponse.substring(start + 1);
        if (localFile.isDirectory()) {
            File dir = new File(localFile, directoryName);
            dir.mkdir();
            log("Creating: " + dir);
            return dir;
        }
        return null;
    }

    private void parseAndFetchFile(String serverResponse, File localFile, OutputStream out, InputStream in) throws IOException, JSchException, ScpException {
        int start = 0;
        int end = serverResponse.indexOf(" ", start + 1);
        start = end + 1;
        end = serverResponse.indexOf(" ", start + 1);
        long filesize = Long.parseLong(serverResponse.substring(start, end));
        String filename = serverResponse.substring(end + 1);
        log("Receiving: " + filename + " : " + filesize);
        File transferFile = (localFile.isDirectory()) ? new File(localFile, filename) : localFile;
        fetchFile(transferFile, filesize, out, in);
        waitForAck(in);
        sendAck(out);
    }

    private void fetchFile(File localFile, long filesize, OutputStream out, InputStream in) throws IOException, JSchException {
        byte[] buf = new byte[BUFFER_SIZE];
        sendAck(out);
        FileOutputStream fos = new FileOutputStream(localFile);
        int length;
        long totalLength = 0;
        long startTime = System.currentTimeMillis();
        boolean trackProgress = getVerbose() && filesize > HUNDRED_KILOBYTES;
        long initFilesize = filesize;
        int percentTransmitted = 0;
        try {
            while (true) {
                length = in.read(buf, 0, (BUFFER_SIZE < filesize) ? BUFFER_SIZE : (int) filesize);
                if (length < 0) {
                    throw new EOFException("Unexpected end of stream.");
                }
                fos.write(buf, 0, length);
                filesize -= length;
                totalLength += length;
                if (filesize == 0) {
                    break;
                }
                if (trackProgress) {
                    percentTransmitted = trackProgress(initFilesize, totalLength, percentTransmitted);
                }
            }
        } finally {
            long endTime = System.currentTimeMillis();
            logStats(startTime, endTime, totalLength);
            fos.flush();
            fos.close();
        }
        if (getPreserveLastModified()) {
            setLastModified(localFile);
        }
    }

    /**
     * @param localFile
     * @throws JSchException
     */
    private void setLastModified(File localFile) throws JSchException {
        SftpATTRS fileAttributes = null;
        ChannelSftp channel = openSftpChannel();
        channel.connect();
        try {
            fileAttributes = channel.lstat(remoteDir(remoteFile) + localFile.getName());
        } catch (SftpException e) {
            throw new JSchException("failed to stat remote file", e);
        }
    }

    /**
     * returns the directory part of the remote file, if any.
     */
    private static String remoteDir(String remoteFile) {
        int index = remoteFile.lastIndexOf("/");
        if (index < 0) {
            index = remoteFile.lastIndexOf("\\");
        }
        return index > -1 ? remoteFile.substring(0, index + 1) : "";
    }
}
