package edu.indiana.extreme.www.xgws.eventNotifier.utility;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.security.Security;
import java.util.Properties;

public class MailSender {

    private String sender;

    private String password;

    public MailSender(String sender, String password) {
        this.sender = sender;
        this.password = password;
    }

    public synchronized void sendMail(String subject, String body, String recipients) throws Exception {
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        InputStream input = this.getClass().getClassLoader().getResourceAsStream("resources/MailSender.properties");
        if (input != null) {
            Properties props = new Properties();
            props.load(input);
            Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {

                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(sender, password);
                }
            });
            MimeMessage message = new MimeMessage(session);
            message.setSender(new InternetAddress(sender));
            message.setSubject(subject, "text/plain");
            message.setContent(body, "text/plain");
            if (recipients.indexOf(',') > 0) message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients)); else message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
            Transport.send(message);
        } else System.out.println("MailSender Properties File not found!");
    }
}
