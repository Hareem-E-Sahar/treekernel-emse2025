package org.damour.base.server;

import java.security.Security;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class GmailHelper implements IEmailService {

    private static final String SMTP_HOST_NAME = "smtp.gmail.com";

    private static final String SMTP_PORT = "465";

    private static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

    private static GmailHelper instance = new GmailHelper();

    public GmailHelper() {
        Logger.log("GmailHelper instanced");
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
    }

    public static GmailHelper getInstance() {
        return instance;
    }

    public void emailException(Throwable t) {
        String trace = Logger.convertStringToHTML(Logger.convertThrowableToString(t));
        String from = BaseSystem.getAdminEmailAddress();
        String to = from;
        String subject = "A critical server error has occurred.";
        String message = "<BR/>" + t.getMessage() + "<BR/>" + trace;
        sendMessage(BaseSystem.getSmtpHost(), from, from, to, subject, message);
    }

    public void sendDebugMessage(String text) {
        String from = BaseSystem.getAdminEmailAddress();
        String to = from;
        String subject = BaseSystem.getDomainName() + " DEBUG";
        String message = "<BR/>" + text + "<BR/>";
        sendMessage(BaseSystem.getSmtpHost(), from, from, to, subject, message);
    }

    public boolean sendMessage(String smtpHost, String fromAddress, String fromName, String to, String subject, String text) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", SMTP_HOST_NAME);
            props.put("mail.smtp.auth", "true");
            props.put("mail.debug", "true");
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.socketFactory.port", SMTP_PORT);
            props.put("mail.smtp.socketFactory.class", SSL_FACTORY);
            props.put("mail.smtp.socketFactory.fallback", "false");
            Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {

                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(BaseSystem.getSettings().getProperty("gmail.userid"), BaseSystem.getSettings().getProperty("gmail.password"));
                }
            });
            Message msg = new MimeMessage(session);
            InternetAddress addressFrom = new InternetAddress(fromAddress);
            msg.setFrom(addressFrom);
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            msg.setSubject(subject);
            msg.setContent(text, "text/html");
            Transport.send(msg);
            return true;
        } catch (Exception e) {
            Logger.log(e);
            return false;
        }
    }
}
