package com.hs.mail.imap.message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import javax.mail.Flags;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.field.address.Mailbox;
import com.hs.mail.container.config.Config;

/**
 * 
 * @author Won Chul Doh
 * @since May 5, 2010
 *
 */
public class MailMessage extends FetchData {

    private MessageHeader header;

    private File file;

    private MailMessage(File file) {
        super();
        this.file = file;
    }

    public MessageHeader getHeader() {
        return header;
    }

    public void setHeader(MessageHeader header) {
        this.header = header;
    }

    public String getSubject() {
        return header.getSubject();
    }

    public String getFrom() {
        return header.getFrom().getAddress();
    }

    public String getReplyTo() {
        Mailbox m = header.getReplyTo();
        if (m == null) return getFrom(); else return header.getReplyTo().getAddress();
    }

    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    public void save(boolean deleteSrc) throws IOException {
        File dest = Config.getDataFile(getInternalDate(), getPhysMessageID());
        if (deleteSrc) {
            FileUtils.moveFile(file, dest);
        } else {
            FileUtils.copyFile(file, dest);
        }
    }

    public void save(InputStream is) throws IOException {
        File dest = Config.getDataFile(getInternalDate(), getPhysMessageID());
        OutputStream os = null;
        try {
            os = new FileOutputStream(dest);
            IOUtils.copyLarge(is, os);
        } finally {
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);
        }
    }

    public static MailMessage createMailMessage(File file) throws IOException {
        return createMailMessage(file, new Date(), null);
    }

    public static MailMessage createMailMessage(File file, Date internalDate, Flags flags) throws IOException {
        MailMessage message = new MailMessage(file);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            MessageHeader header = new MessageHeader(fis);
            message.setPhysMessageID(0);
            message.setHeader(header);
            message.setSize(file.length());
            message.setInternalDate(internalDate);
            message.setFlags(flags);
            return message;
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }
}
