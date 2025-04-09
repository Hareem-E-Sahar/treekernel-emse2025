package ge.telasi.tasks.controller;

import ge.telasi.tasks.model.BinaryData;
import ge.telasi.tasks.model.TaskAttachment;
import ge.telasi.tasks.model.User;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
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
import javax.mail.util.ByteArrayDataSource;
import javax.persistence.EntityManager;

public class EmailController extends Controller {

    /**
     * Get admin mail session.
     */
    public Session getAdminSession() {
        boolean debug = false;
        Properties props = new Properties();
        props.put("mail.smtp.host", "92.241.77.33");
        props.put("mail.smtps.port", "465");
        props.put("mail.smtps.socketFactory.port", "25");
        props.put("mail.smtps.socketFactory.fallback", "false");
        props.put("mail.smtps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        Session session = Session.getInstance(props, new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("tasks.noreply", "no868");
            }
        });
        session.setDebug(debug);
        return session;
    }

    public InternetAddress getAdminAddress() throws MessagingException {
        InternetAddress addressFrom = new InternetAddress("tasks.noreply@telasi.ge");
        return addressFrom;
    }

    public void sendMessage(EntityManager em, User to, String subject, String messageText) throws MessagingException {
        sendMessage(em, to, subject, messageText, null);
    }

    /**
     * Sending e-mail message to the given user from admin mail account.
     */
    public void sendMessage(EntityManager em, User to, String subject, String messageText, List<TaskAttachment> attachemnts) throws MessagingException {
        String toEmail = to.getEmail();
        if (!to.isNotifyByEmail() || to.getEmail() == null) return;
        Message message = new MimeMessage(getAdminSession());
        message.setFrom(getAdminAddress());
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
        message.setSubject(subject);
        Multipart multipart = new MimeMultipart();
        BodyPart part = new MimeBodyPart();
        part.setText(messageText);
        multipart.addBodyPart(part);
        if (attachemnts != null) {
            for (TaskAttachment attachment : attachemnts) {
                BinaryData data = em.find(BinaryData.class, attachment.getBinaryId());
                BodyPart attPart = new MimeBodyPart();
                DataSource source = new ByteArrayDataSource(data.getContent(), "application/octet-stream ");
                attPart.setDataHandler(new DataHandler(source));
                attPart.setFileName(attachment.getName());
                multipart.addBodyPart(attPart);
            }
        }
        message.setContent(multipart);
        Transport.send(message);
    }
}
