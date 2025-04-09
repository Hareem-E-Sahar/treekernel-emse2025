package com.unice.miage.oobdoo.helpfull;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author Robert
 */
public class EmailSender {

    private String smtpServer = "smtp.gmail.com";

    private String port = "465";

    private String user = "oobdoo.project@gmail.com";

    private String password = "qsd654s123";

    private String auth = "true";

    private String from = "oobdoo.project@gmail.com";

    public EmailSender() {
    }

    private Properties prepareProperties() {
        Properties props = new Properties();
        props.setProperty("mail.smtp.host", smtpServer);
        props.setProperty("mail.smtp.auth", auth);
        props.setProperty("mail.smtp.port", port);
        props.setProperty("mail.smtp.user", user);
        props.setProperty("mail.smtp.password", password);
        props.setProperty("mail.smtp.socketFactory.port", port);
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.smtp.socketFactory.fallback", "false");
        return props;
    }

    private MimeMessage prepareMessage(Session mailSession, String charset, String from, String subject, String HtmlMessage, String recipient) {
        MimeMessage message = null;
        try {
            message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(from));
            message.setSubject(subject);
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setContent(HtmlMessage, "text/html; charset=\"" + charset + "\"");
        } catch (Exception ex) {
            Logger.getLogger(EmailSender.class.getName()).log(Level.SEVERE, null, ex);
        }
        return message;
    }

    public void sendEmail(String subject, String HtmlMessage, String to) {
        Transport transport = null;
        try {
            Properties props = prepareProperties();
            Session mailSession = Session.getInstance(props, new SMTPAuthenticator(from, password, true));
            transport = mailSession.getTransport("smtp");
            MimeMessage message = prepareMessage(mailSession, "ISO-8859-2", from, subject, HtmlMessage, to);
            System.out.println("PREPARE MESSSAGE OK");
            transport.connect();
            System.out.println("CONNECT OK");
            Transport.send(message);
            System.out.println("SEND OK");
        } catch (Exception ex) {
            System.out.println("exception == " + ex.getMessage());
        } finally {
            try {
                transport.close();
            } catch (MessagingException ex) {
                Logger.getLogger(EmailSender.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
