package org.voxmail.mail;

import java.util.Date;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.log4j.Category;
import org.voxmail.types.Contact;

/**
 *
 * @author  Rick
 */
public class MailSender {

    static Category logger = Category.getInstance(MailSender.class.getName());

    /** Creates a new instance of VM_Create */
    public MailSender() {
    }

    public boolean sendMail(String mailurl, String filePath, String callerID, Contact contact) {
        return this.sendMail(mailurl, filePath, callerID, contact, null, null);
    }

    public boolean sendMail(String mailurl, String filePath, String callerID, Contact contact, String smtpHost, String smtpUserAccount) {
        logger.debug("MailSender::sendMail - attempting delivery at: " + mailurl);
        boolean bSUCCESS = false;
        boolean isSMTP = false;
        try {
            Session s = null;
            if (mailurl.toUpperCase().startsWith("POP")) {
                isSMTP = true;
                Properties props = new Properties();
                props.put("mail.smtp.host", smtpHost);
                s = Session.getInstance(props);
            } else {
                s = Session.getInstance(new Properties());
            }
            MimeMessage message = new MimeMessage(s);
            message.setFrom(new InternetAddress(contact.getEmail(), "voicemail"));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(contact.getEmail(), contact.getEmail()));
            if (callerID == null) callerID = "";
            message.addHeader("x-caller-id", callerID);
            message.addHeader("x-message-path", filePath);
            String subject = "Voicemail Message";
            if (!"".equals(callerID) && !"unknown".equalsIgnoreCase(callerID)) subject += " From " + callerID;
            message.setSubject(subject);
            Multipart multipart = new MimeMultipart();
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("voicemail message is attached...");
            multipart.addBodyPart(messageBodyPart);
            messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(filePath);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName("message.wav");
            multipart.addBodyPart(messageBodyPart);
            messageBodyPart.setDisposition("CRITICAL");
            message.setContent(multipart);
            message.setSentDate(new Date());
            if (isSMTP) {
                Transport trans = s.getTransport("smtp");
                message.setFrom(new InternetAddress(smtpUserAccount, "voicemail"));
                try {
                    trans.connect();
                    Transport.send(message);
                } catch (Exception e) {
                    logger.debug("MailSender::sendMail() - failed to send via SMTP: " + e.getMessage());
                } finally {
                    trans.close();
                }
            } else {
                MailConnection mail = null;
                ;
                try {
                    mail = new MailConnection(mailurl);
                    Folder inbox = mail.getInbox();
                    inbox.appendMessages(new Message[] { (Message) message });
                    bSUCCESS = true;
                } catch (Exception e) {
                    logger.debug("MailSender::sendMail() - failed to append message: " + e.getMessage());
                } finally {
                    mail.closeInbox();
                }
            }
            logger.debug("MailSender::sendMail() - SUCCESSFULLY DELIVERED!");
        } catch (javax.mail.MessagingException me) {
            logger.error("MailSender::sendMail() - MessagingException: " + me.getMessage());
        } catch (Exception e) {
            logger.error("MailSender::sendMail() - Exception: " + e.getMessage());
        }
        return bSUCCESS;
    }
}
