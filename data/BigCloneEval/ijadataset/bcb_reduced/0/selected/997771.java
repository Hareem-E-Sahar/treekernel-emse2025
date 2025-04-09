package com.xpresso.utils.email;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import com.xpresso.utils.exceptions.XpressoException;

public class SimpleEmailProvider implements EmailProvider {

    protected Properties mailConfig;

    public void configure(EmailConfiguration configuration) {
        mailConfig = new Properties();
        mailConfig.setProperty("mail.host", configuration.getHost());
        mailConfig.setProperty("mail.user", configuration.getUser());
        mailConfig.setProperty("mail.pass", configuration.getPass());
        mailConfig.put("mail.smtp.auth", configuration.isAuthentication() + "");
    }

    public void sendEmail(EmailMessage email) throws XpressoException {
        Authenticator auth = new SMTPAuthenticator();
        Session session = Session.getInstance(mailConfig, auth);
        MimeMessage message = new MimeMessage(session);
        try {
            Transport transport = session.getTransport("smtp");
            transport.connect(mailConfig.getProperty("mail.host"), mailConfig.getProperty("mail.user"), mailConfig.getProperty("mail.pass"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email.getTo()[0]));
            message.setFrom(new InternetAddress(email.getFrom()));
            message.setSubject(email.getSubject());
            message.setText(email.getBody());
            transport.send(message);
        } catch (MessagingException ex) {
            throw new XpressoException("Error sending email." + ex, ex);
        }
    }

    private class SMTPAuthenticator extends javax.mail.Authenticator {

        public PasswordAuthentication getPasswordAuthentication() {
            String username = mailConfig.getProperty("mail.user");
            String password = mailConfig.getProperty("mail.pass");
            return new PasswordAuthentication(username, password);
        }
    }
}
