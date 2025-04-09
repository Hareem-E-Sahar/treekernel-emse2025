package org.dcm4che2.tool.dcmrcv;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.dcm4che2.util.CloseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nick Evans (http://www.nickevans.me.uk)
 * @version $Revision: 1 $ $Date: 2011-09-11 12:57:21 +0000 (Sun, 11 Sept 2011) $
 * @since Sept 11, 2011
 */
public class ZipFileSender extends Thread {

    static int threadId = 1;

    static Logger LOG = LoggerFactory.getLogger(ZipFileSender.class);

    private static Hashtable<String, ZipFileSender> zipFileDictionary = new Hashtable<String, ZipFileSender>();

    public static synchronized void addFileFromInputStream(String seriesInstanceUID, String iuid, EmailParams zipFileSubjectParams, InputStream stream, File tempDirectory) throws IOException {
        String filename = iuid.replace('.', '-') + ".dcm";
        ZipFileSender existingZip = zipFileDictionary.get(seriesInstanceUID);
        if (!(existingZip != null && existingZip.writeFile(filename, stream))) {
            ZipFileSender zip = new ZipFileSender(seriesInstanceUID, zipFileSubjectParams, tempDirectory);
            zipFileDictionary.put(seriesInstanceUID, zip);
            zip.writeFile(filename, stream);
        }
    }

    private static boolean sendZip = false;

    private static int zipTimeout = 5000;

    private static String mailTo = null;

    private static String mailFrom = null;

    private static String mailHost = null;

    private static String mailUsername = null;

    private static String mailPassword = null;

    public static final void setSend(boolean send) {
        sendZip = send;
    }

    public static final void setZipTimeout(int timeout) {
        zipTimeout = timeout;
    }

    public static final void setEmailTo(String emailToAddress) {
        mailTo = emailToAddress;
    }

    public static final void setEmailFrom(String emailFromAddress) {
        mailFrom = emailFromAddress;
    }

    public static final void setSMTPServer(String smtpServer) {
        mailHost = smtpServer;
    }

    public static final void setSMTPUsername(String smtpUsername) {
        mailUsername = smtpUsername;
    }

    public static final void setSMTPPassword(String smtpPassword) {
        mailPassword = smtpPassword;
    }

    EmailParams zipFileSubjectParams = null;

    File zipFile;

    ZipOutputStream out = null;

    boolean fileWritten = false;

    boolean fileSent = false;

    String seriesInstanceUID;

    String filename;

    public void run() {
        while (out != null && !fileSent) {
            if (!fileWritten) {
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    continue;
                }
            } else {
                try {
                    sleep(zipTimeout);
                    sendEmail();
                } catch (InterruptedException e) {
                    continue;
                }
            }
        }
    }

    public ZipFileSender(String seriesInstanceUID, EmailParams subjectParams, File tempDirectory) throws IOException {
        super("ZIPFILESENDER-" + threadId++);
        this.seriesInstanceUID = seriesInstanceUID;
        try {
            zipFile = new File(tempDirectory, this.getName() + "_" + seriesInstanceUID.replace('.', '-') + ".dmz");
            zipFileSubjectParams = subjectParams;
            out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
            out.setMethod(ZipOutputStream.DEFLATED);
            this.start();
            LOG.info("M-ZIP-OPEN {}", zipFile);
        } catch (IOException e) {
            LOG.error("Error creating zip file '" + zipFile + "'", e);
            CloseUtils.safeClose(out);
            throw e;
        }
    }

    static final int BUFFER = 2048;

    public synchronized boolean writeFile(String filename, InputStream stream) throws IOException {
        if (fileSent) return false;
        BufferedInputStream origin = new BufferedInputStream(stream, BUFFER);
        try {
            byte data[] = new byte[BUFFER];
            ZipEntry entry = new ZipEntry(filename);
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            origin.close();
            fileWritten = true;
            interrupt();
            return true;
        } catch (IOException e) {
            LOG.error("Error writing to zip file '" + zipFile + "'", e);
            interrupt();
            throw e;
        } finally {
            CloseUtils.safeClose(origin);
        }
    }

    private synchronized void sendEmail() {
        fileSent = true;
        if (out != null) {
            CloseUtils.safeClose(out);
            out = null;
            zipFileDictionary.remove(seriesInstanceUID);
            LOG.info("M-ZIP-CLOSE {}", zipFile);
            if (sendZip) {
                String msgText1 = "Sending a file.\n";
                String subject = zipFileSubjectParams.getParamString();
                Properties props = System.getProperties();
                props.put("mail.smtp.host", mailHost);
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.port", "587");
                Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {

                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(mailUsername, mailPassword);
                    }
                });
                try {
                    MimeMessage msg = new MimeMessage(session);
                    msg.setFrom(new InternetAddress(mailFrom));
                    InternetAddress[] address = { new InternetAddress(mailTo) };
                    msg.setRecipients(Message.RecipientType.TO, address);
                    msg.setSubject(subject);
                    MimeBodyPart mbp1 = new MimeBodyPart();
                    mbp1.setText(msgText1);
                    MimeBodyPart mbp2 = new MimeBodyPart();
                    FileDataSource fds = new FileDataSource(zipFile);
                    mbp2.setDataHandler(new DataHandler(fds));
                    mbp2.setFileName(seriesInstanceUID.replace('.', '-') + ".dmz");
                    Multipart mp = new MimeMultipart();
                    mp.addBodyPart(mbp1);
                    mp.addBodyPart(mbp2);
                    msg.setContent(mp);
                    msg.setSentDate(zipFileSubjectParams.getSeriesDateTime());
                    Transport.send(msg);
                    LOG.info("M-EMAIL {}", zipFileSubjectParams.getParamString());
                    if (zipFile.delete()) {
                        LOG.info("M-DELETE {}", zipFile);
                        zipFile = null;
                    }
                } catch (Exception e) {
                    LOG.error("Error e-mailing file '" + zipFile + "' with subject '" + zipFileSubjectParams.getParamString() + "'", e);
                    System.out.println();
                    System.out.println((new Date()).toString());
                    System.out.println("   E-mail send failed:");
                    System.out.println("      " + e.getMessage().replaceAll("\n", "\r\n      "));
                    if (e.getCause() != null) {
                        System.out.println("      " + e.getCause().getMessage().replaceAll("\n", "\r\n      "));
                    }
                    System.out.println("   Please manually e-mail the following:");
                    System.out.println("    - Attachment:   " + zipFile);
                    System.out.println("    - Subject Line: " + zipFileSubjectParams.getParamString());
                    System.out.println("    - To:           " + mailTo);
                }
            }
        }
    }
}
