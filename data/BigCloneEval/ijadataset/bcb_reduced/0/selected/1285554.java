package org.charvolant.tmsnet.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.zip.CRC32;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.charvolant.tmsnet.command.CloseFile;
import org.charvolant.tmsnet.command.CloseFileResponse;
import org.charvolant.tmsnet.command.CreateFile;
import org.charvolant.tmsnet.command.OpenFile;
import org.charvolant.tmsnet.command.OpenFileResponse;
import org.charvolant.tmsnet.command.SendFile;
import org.charvolant.tmsnet.model.Directory;
import org.charvolant.tmsnet.model.FileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A transaction to send a file to the PVR.
 *
 * @author Doug Palmer &lt;doug@charvolant.org&gt;
 *
 */
@XmlRootElement()
@XmlAccessorType(XmlAccessType.NONE)
public class SendFileTransaction extends Transaction<Transactable> {

    /** The logger for this transaction */
    private static final Logger logger = LoggerFactory.getLogger(SendFileTransaction.class);

    /** The skip trigger */
    public static final String SKIP = "skip";

    /** The directory to send the file to */
    private Directory remote;

    /** The remote path */
    private String path;

    /** The base local file to read from */
    private File local;

    /** The file descriptor for the remote file */
    private int fd;

    /** The input stream for the file */
    private InputStream stream;

    /** The number of bytes sent */
    private long sent;

    /**
   * Construct an empty fetch directory transaction
   *
   */
    public SendFileTransaction() {
        super();
        this.fd = -1;
    }

    /**
   * Construct a transaction for a specific file and local file.
   * <p>
   * The local file is the base local file.
   * Extra files have extensions added as required.
   * 
   * @param remote The remote directory to send to
   * @param local The local file
   */
    public SendFileTransaction(Directory remote, File local) {
        super();
        this.remote = remote;
        this.local = local;
        this.fd = -1;
    }

    /**
   * Start the ball rolling by creating the remote file and opening
   * the local file.
   *
   * @see org.charvolant.tmsnet.client.Transaction#onExecute()
   */
    @Override
    protected void onExecute() {
        String name;
        int ext = 1;
        super.onExecute();
        name = this.local.getName();
        while (this.remote.find(name) != null) {
            name = this.local.getName() + "-" + (ext++);
        }
        this.path = this.remote.getPath() + name;
        try {
            this.stream = new FileInputStream(this.local);
            this.sent = 0;
            this.queue(new CreateFile(this.path));
        } catch (Exception ex) {
            this.event(ex);
        }
    }

    /**
   * Then open the file.
   */
    protected void onOpenFile() {
        this.queue(new OpenFile(this.path));
    }

    /**
   * Respond to an open file.
   * <p>
   * Get the file descriptor associated with the open.
   * 
   * @param response The response
   */
    protected void onOpenFileResponse(OpenFileResponse response) {
        this.fd = response.getFd();
    }

    /**
   * Get a segment from the PVR.
   */
    protected void onSendSegment() {
        byte[] buffer = new byte[13100];
        int n;
        try {
            n = this.stream.read(buffer);
            if (n <= 0) this.event(this.SKIP); else {
                CRC32 crc = new CRC32();
                byte[] send = Arrays.copyOf(buffer, n);
                this.sent += n;
                crc.update(send);
                this.logger.debug("Send " + n + " bytes CRC32=" + crc.getValue());
                this.queue(new SendFile(this.fd, send));
            }
        } catch (Exception ex) {
            this.event(ex);
        }
    }

    /**
   * Close any open files that we have.
   */
    protected void onCloseFile() {
        try {
            if (this.stream != null) this.stream.close();
        } catch (IOException ex) {
        } finally {
            this.stream = null;
        }
        if (this.fd > 0) this.queue(new CloseFile(this.fd)); else this.event(this.SKIP);
    }

    /**
   * React to a close file response
   * @param response
   */
    protected void onCloseFileResponse(CloseFileResponse response) {
        this.fd = -1;
    }

    /**
   * When we complete, add the file to the local file set
   *
   * @see org.charvolant.tmsnet.client.Transaction#onCommitted()
   */
    @Override
    protected void onCommitted() {
        FileInfo file = new FileInfo(this.local.getName());
        file.setSize(this.sent);
        file.setModified(new Date());
        this.getClient().getState().addFiles(this.remote, Collections.singleton(file));
        super.onCommitted();
    }
}
