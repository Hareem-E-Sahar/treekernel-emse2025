package ipmss.services.messages;

import ipmss.data.messages.Email;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * The Class MessagesBean.
 *
 * @author Michał Czarnik
 * All about messaging
 */
@Named
public class MessagesBean implements Messages {

    /** The Constant logger. */
    private static final Logger logger = Logger.getLogger(MessagesBean.class.getName());

    /** The session. */
    private Session session;

    /**
     * Instantiates a new messages bean.
     */
    public MessagesBean() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        session = Session.getInstance(props, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("darmoweaplikacjejava", "mniedej12");
            }
        });
    }

    /**
     * Send Email using SSL connection.
     *
     * @param email the email
     * @param address the address
     * @throws MessagingException the messaging exception
     */
    public void sendEmail(Email email, String address) throws MessagingException {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("darmoweaplikacjejava@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(address));
            message.setSubject(email.getSubject());
            message.setText(email.getContent());
            Transport.send(message);
        } catch (MessagingException e) {
            logger.log(Level.WARNING, e.toString());
            throw e;
        }
    }

    /**
     * Send Email using SSL connection.
     *
     * @param email the email
     * @param address the address
     */
    public void sendEmails(Email email, List<String> address) {
    }
}
