package asc_fridayemail;

import java.util.*;
import java.io.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

/**
 * Emails a PDF file using SMTP
 * @author Simon Thompson MEng MIET
 * @version 2.0
 */
public class emailPDFFile {

    String smtpServer;

    String from;

    String to;

    String subject;

    String body;

    String filename;

    parseConfigFile config;

    /**
         * @param filename filename of the PDF file to be emailed
         * @param config Config File Parser which provides:
                       * getEmailSmtpServerStr()
                       * getEmailFromAdrressStr()
                       * getEmailToAdrressStr()
                       * getEmailSubjectStr()
                       * getEmailBodyTextStr()
         */
    public emailPDFFile(parseConfigFile config, String filename) {
        this.filename = filename;
        this.config = config;
        smtpServer = config.getEmailSmtpServerStr();
        from = config.getEmailFromAdrressStr();
        to = config.getEmailToAdrressStr();
        subject = config.getEmailSubjectStr();
        body = config.getEmailBodyTextStr();
    }

    /**
     * Creates the email message object and sends it via SMTP
     * @throws MessagingException caught and piped to std err
     */
    public void sendEmail() {
        System.out.println("...Emailling document");
        System.out.println("......getting email environment settings");
        Properties props = System.getProperties();
        props.put("mail.smtp.host", smtpServer);
        Session session = Session.getInstance(props, null);
        try {
            System.out.println("......creating message");
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            InternetAddress[] address = { new InternetAddress(to) };
            msg.setRecipients(Message.RecipientType.TO, address);
            msg.setSubject(subject);
            MimeBodyPart mbp1 = new MimeBodyPart();
            mbp1.setText(body);
            MimeBodyPart mbp2 = new MimeBodyPart();
            FileDataSource fds = new FileDataSource(filename);
            mbp2.setDataHandler(new DataHandler(fds));
            mbp2.setFileName(fds.getName());
            Multipart mp = new MimeMultipart();
            mp.addBodyPart(mbp1);
            mp.addBodyPart(mbp2);
            msg.setContent(mp);
            msg.setSentDate(new Date());
            System.out.println("......sending message");
            Transport.send(msg);
            System.out.println("......connection closed");
        } catch (MessagingException mex) {
            System.err.println("MESSAGE EXCEPTION CREATED");
        }
    }
}
