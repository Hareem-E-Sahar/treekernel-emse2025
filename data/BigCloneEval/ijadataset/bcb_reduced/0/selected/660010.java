package org.grailrtls.solver.notifications;

import java.util.ArrayList;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Class that uses open-source JavaMail API method to send an email 
 *  via Gmail SMTP server, using an SSL connection.
 *  
	 *  from: http://www.mkyong.com/java/javamail-api-sending-email-via-gmail-smtp-example/
	 *  
	 * @author mkyong, Sumedh Sawant
 * 
 * Greatly modified by Sumedh Sawant for GRAIL purposes, used to send SMS and email notifications, 
 * as well to update the twitter feed. 
 *
 */
public class SendMessageSSL {

    private ArrayList<String> recipients;

    private static final Logger log = LoggerFactory.getLogger(SendMessageSSL.class);

    private String username = null;

    private String password = null;

    public SendMessageSSL(ArrayList<String> recpients) {
        this.recipients = recpients;
    }

    public void sendMessage(String msg) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SendMessageSSL.this.username, SendMessageSSL.this.password);
            }
        });
        try {
            for (String recipient : this.recipients) {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress("from@no-spam.com"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
                message.setSubject("");
                message.setText(msg);
                Transport.send(message);
            }
            log.info("Message was sent!");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendRemovalMessage(String recipient) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SendMessageSSL.this.username, SendMessageSSL.this.password);
            }
        });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("from@no-spam.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject("Removal Confirmation");
            message.setText("You have been removed from the WINLAB notification system.");
            Transport.send(message);
            System.out.println("Done");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        log.info("Send removal message to recipient.");
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
