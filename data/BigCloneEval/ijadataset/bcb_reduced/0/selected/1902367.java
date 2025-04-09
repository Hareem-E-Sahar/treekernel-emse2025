package fiswidgets.fisutils;

import java.util.*;
import java.io.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import javax.swing.*;

/**
 * FisEmail is a class that can be used to send email to users through a SMTP server.  
 * In order to use FisEmail jav-1.0.1 must be installled and its activation.jar must be in the CLASSPATH and 
 * the javamail-1.1.2 must be installed and its mail.jar must be in the CLASSPATH.
 *
 * FisEmail will check the current system properties for EMAIL_TO, EMAIL_FROM, and SMTP_SERVER.  If these 
 * properties are not defined then the programmer can specify them.
 *
 */
public class FisEmail {

    private String mailto = "none";

    private String mailfrom = "none";

    private String mailsubject = "none";

    private String mailsmtp = "none";

    private String mailbody = "none";

    private String[] attachments;

    /**
     * This is the constructor that takes a subject and a body as arguments.  The constructor will try and 
     * load in the EMAIL_TO, EMAIL_FROM, and SMTP_SERVER from the current system properties.
     * @param subject the subject of the email to be sent.
     * @param body the body of the email to be sent.
     */
    public FisEmail(String subject, String body) {
        Properties props = System.getProperties();
        mailto = props.getProperty("EMAIL_TO", "none");
        mailfrom = props.getProperty("EMAIL_FROM", "none");
        mailsmtp = props.getProperty("SMTP_SERVER", "none");
        mailsubject = subject;
        mailbody = body;
    }

    /**
     * The send method is used to send the FisEmail that has been created.  If errors occur then a dialog will
     * appear with the appropriate errors.
     */
    public void send() throws FileNotFoundException, SendFailedException, MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", mailsmtp);
        Session session = Session.getDefaultInstance(props, null);
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(mailfrom));
        InternetAddress all = new InternetAddress();
        InternetAddress[] addresses = all.parse(mailto);
        msg.setRecipients(Message.RecipientType.TO, addresses);
        msg.setSubject(mailsubject);
        msg.setSentDate(new Date());
        msg.setText(mailbody);
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(mailbody);
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        if (attachments != null) {
            for (int i = 0; i < attachments.length; i++) {
                messageBodyPart = new MimeBodyPart();
                File file = new File(attachments[i]);
                if (file.canRead()) {
                    DataSource source = new FileDataSource(file);
                    messageBodyPart.setDataHandler(new DataHandler(source));
                    messageBodyPart.setFileName(attachments[i]);
                    multipart.addBodyPart(messageBodyPart);
                }
            }
        }
        msg.setContent(multipart);
        Transport.send(msg);
    }

    /**
     * getMailTo returns the email address of the to field for the email.  This email address can be 
     * multiple email addresses seperated by commas.
     */
    public String getMailTo() {
        return mailto;
    }

    /**
     * getMailFrom returns the email address of the from field for the email.
     */
    public String getMailFrom() {
        return mailfrom;
    }

    /**
     * getMailSubject returns the subject of the email.
     */
    public String getMailSubject() {
        return mailsubject;
    }

    /**
     * getSMTPServer returns the SMTP server that will be used to send the email through.
     */
    public String getSMTPServer() {
        return mailsmtp;
    }

    /**
     * Get file attachment.
     * @return attachment.
     */
    public String[] getAttachments() {
        return attachments;
    }

    /**
     * setMailTo can be used to overwrite the EMAIL_TO system property.
     * @param to the email address for the to field of the email.  This can be multiple email addresses seperated by commas.
     */
    public void setMailTo(String to) {
        mailto = to;
    }

    /**
     * setMailFrom can be used to overwrite the EMAIL_FROM system property.
     * @param from is the email address that will be in the from field of the email.
     */
    public void setMailFrom(String from) {
        mailfrom = from;
    }

    /**
     * setMailSubject can be used to overwrite the email's subject from the constructor.
     * @param subject will be the subject field of the email.
     */
    public void setMailSubject(String subject) {
        mailsubject = subject;
    }

    /**
     * setSMTPServer can be used to overwrite the SMTP_SERVER system property.
     * @param smtp is the SMTP server that the email should be sent to.
     */
    public void setSMTPServer(String smtp) {
        mailsmtp = smtp;
    }

    /**
     * setMailBody can be used to overwrite the body of the constructor.
     * @param body will be the body of the email.
     */
    public void setMailBody(String body) {
        mailbody = body;
    }

    /**
     * Set attachment to string .
     * @param String [] attachments.
     */
    public void setAttachments(String[] a) {
        attachments = a;
    }

    /**
     * Set attachment to string .
     * @param attachment.
     */
    public void setAttachment(String a) {
        setAttachments(new String[] { a });
    }
}
