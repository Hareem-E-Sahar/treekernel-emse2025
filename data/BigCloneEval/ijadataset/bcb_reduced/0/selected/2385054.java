package com.lightminds.map.tileserver.scheduler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

class EmailSender {

    private Session session;

    private ResourceBundle logSenderConfig = ResourceBundle.getBundle("logsender_conf");

    void send(String recipient, final File zipFile) {
        try {
            javax.naming.InitialContext ctx = new javax.naming.InitialContext();
            session = (Session) ctx.lookup(logSenderConfig.getString("mailResourceJNDI"));
            Message msg = new MimeMessage(session);
            InternetAddress[] recipientAddress = new InternetAddress[1];
            recipientAddress[0] = new InternetAddress(recipient);
            msg.addRecipients(Message.RecipientType.TO, recipientAddress);
            msg.setSubject(logSenderConfig.getString("mail.subject"));
            msg.setSentDate(new Date());
            msg.setFrom(new InternetAddress(session.getProperty("mail.from")));
            MimeBodyPart mbp = new MimeBodyPart();
            mbp.setText(logSenderConfig.getString("mail.body"));
            MimeMultipart mp = new MimeMultipart();
            MimeBodyPart mbpAttachment = new MimeBodyPart();
            mbpAttachment.setFileName(zipFile.getName());
            mbpAttachment.setDataHandler(new DataHandler(new DataSource() {

                public String getContentType() {
                    return "application/zip";
                }

                public InputStream getInputStream() throws IOException {
                    return new FileInputStream(zipFile);
                }

                public String getName() {
                    return "LogSenderDataSource";
                }

                public OutputStream getOutputStream() throws IOException {
                    return null;
                }
            }));
            mp.addBodyPart(mbp);
            mp.addBodyPart(mbpAttachment);
            msg.setContent(mp);
            Transport.send(msg);
            System.out.println("Message with statistics log file " + zipFile.getName() + " sent to " + recipient);
        } catch (Exception ex) {
            Logger.getLogger(EmailSender.class.getName()).log(Level.SEVERE, "Problem with the e-mail message sending.", ex);
        }
    }
}
