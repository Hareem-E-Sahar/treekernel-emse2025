package org.dctmutils.common.email;

import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;
import org.dctmutils.common.ValidationHelper;
import org.dctmutils.common.email.exception.EmailException;
import org.dctmutils.common.email.object.Attachment;
import org.dctmutils.common.email.object.AttachmentList;
import org.dctmutils.common.email.object.EmailMessage;
import org.dctmutils.common.exception.MissingParameterException;
import org.dctmutils.common.exception.ValidationException;

/**
 * Sends SMTP email using the JavaMail API.
 * 
 * @author <a href="mailto:luther@dctmutils.org">Luther E. Birdzell</a>
 */
public class EmailSender {

    /**
     * 
     */
    private static Log log = LogFactory.getLog(EmailSender.class);

    /**
     * mail.smpt.host (see the javamail spec.)
     * 
     */
    public static final String MAIL_SMTP_HOST = "mail.smtp.host";

    /**
     * mail.mime.charset JavaMail attribute.
     */
    public static final String MIME_ENCODING = "mail.mime.charset";

    /**
     * UTF-8 encoding
     */
    public static final String UTF8_ENCODING = "UTF-8";

    /**
     * ISO8859_1 encoding
     */
    public static final String ISO8859_1_ENCODING = "ISO8859_1";

    /**
     * All messages are sent with this encoding (default ISO8859_1_ENCODING).
     */
    public static final String ENCODING = ISO8859_1_ENCODING;

    /**
     * 
     */
    private Session session;

    /**
     * 
     */
    private String mailSmtpHost = null;

    /**
     * Creates a new <code>EmailSender</code> instance.
     * 
     * @param mailSmtpHost
     * @throws EmailException
     */
    public EmailSender(String mailSmtpHost) throws EmailException {
        this.mailSmtpHost = mailSmtpHost;
        init(mailSmtpHost, false);
    }

    /**
     * Creates a new <code>EmailSender</code> instance.
     * 
     * @param mailSmtpHost
     * @param debug
     * @throws EmailException
     */
    public EmailSender(String mailSmtpHost, boolean debug) throws EmailException {
        this.mailSmtpHost = mailSmtpHost;
        init(mailSmtpHost, debug);
    }

    /**
     * @param mailSmtpHost
     * @param debug
     * @throws EmailException
     */
    private void init(String mailSmtpHost, boolean debug) throws EmailException {
        log.debug("init: start");
        try {
            Properties props = new Properties();
            props.put(MAIL_SMTP_HOST, mailSmtpHost);
            props.put(MIME_ENCODING, ENCODING);
            this.session = Session.getDefaultInstance(props, null);
            this.session.setDebug(debug);
        } catch (Exception e) {
            throw new EmailException(e.getMessage(), e);
        }
        log.debug("init: end");
    }

    /**
     * Send an <code>EmailMessage</code>.
     * 
     * @param messageData
     * @throws EmailException
     */
    public void send(EmailMessage messageData) throws EmailException {
        try {
            log.info("sending: " + messageData.toString());
            MimeMessage message = new MimeMessage(session);
            setFrom(message, messageData);
            setTo(message, messageData);
            message.setSubject(messageData.getSubject());
            message.setSentDate(new Date());
            if (messageData.getAttachments() == null) {
                log.debug("preparing text-only message");
                message.setText(messageData.getMessageText(), ENCODING);
            } else {
                log.debug("preparing multipart message");
                Multipart multipart = new MimeMultipart();
                MimeBodyPart textPart = new MimeBodyPart();
                textPart.setText(messageData.getMessageText(), ENCODING);
                multipart.addBodyPart(textPart);
                AttachmentList attachments = messageData.getAttachments();
                Attachment attachment = null;
                MimeBodyPart attachmentPart = null;
                while (attachments.hasMoreAttachments()) {
                    attachment = attachments.next();
                    attachmentPart = new MimeBodyPart();
                    attachmentPart.setDataHandler(attachment.getDataHandler());
                    attachmentPart.setFileName(attachment.getFileName());
                    multipart.addBodyPart(attachmentPart);
                }
                message.setContent(multipart);
            }
            Transport.send(message);
            log.info("message sent");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new EmailException(e.getMessage(), e);
        }
    }

    /**
     * Send an HTML <code>EmailMessage</code>.
     * 
     * @param message
     * @throws EmailException
     */
    public void sendHtmlMessage(EmailMessage message) throws EmailException {
        try {
            log.info("Sending HTML Message: " + message.toString());
            HtmlEmail email = new HtmlEmail();
            email.setHostName(mailSmtpHost);
            String[] toAddresses = StringUtils.split(message.getToAddress(), ",");
            String[] toNames = StringUtils.split(message.getToName(), ",");
            if (toAddresses == null) {
                throw new EmailException("To Address must be set!");
            }
            for (int i = 0; i < toAddresses.length; i++) {
                email.addTo(toAddresses[i], toNames[i]);
            }
            email.setFrom(message.getFromAddress(), message.getFromName());
            email.setSubject(message.getSubject());
            email.setHtmlMsg(message.getMessageText());
            email.setSentDate(new Date());
            AttachmentList attachments = message.getAttachments();
            Attachment duAttachment = null;
            if (attachments != null) {
                for (int i = 0; i < attachments.size(); i++) {
                    duAttachment = attachments.get(i);
                    EmailAttachment commonsAttachment = new EmailAttachment();
                    commonsAttachment.setPath(duAttachment.getFilePath());
                    commonsAttachment.setName(duAttachment.getFileName());
                    commonsAttachment.setDescription(duAttachment.getDescription());
                    commonsAttachment.setDisposition(EmailAttachment.ATTACHMENT);
                    email.attach(commonsAttachment);
                }
            }
            email.send();
            log.info("HTML message sent");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new EmailException(e.getMessage(), e);
        }
    }

    /**
     * Set the From of the message.
     * 
     * @param message
     * @param messageData
     * @throws EmailException
     */
    protected static void setFrom(MimeMessage message, EmailMessage messageData) throws EmailException {
        try {
            InternetAddress fromAddress = new InternetAddress();
            fromAddress.setAddress(messageData.getFromAddress());
            String fromName = messageData.getFromName();
            if (StringUtils.isNotBlank(fromName)) {
                fromAddress.setPersonal(messageData.getFromName());
            }
            message.setFrom(fromAddress);
        } catch (Exception e) {
            throw new EmailException(e.getMessage(), e);
        }
    }

    /**
     * Set the Recipients of the message.
     * 
     * @param message
     * @param messageData
     * @throws EmailException
     */
    protected static void setTo(MimeMessage message, EmailMessage messageData) throws EmailException {
        try {
            log.debug("setting toName = " + messageData.getToName() + ", toAddress = " + messageData.getToAddress());
            InternetAddress[] to = InternetAddress.parse(messageData.getToAddress());
            String toName = messageData.getToName();
            if (StringUtils.isNotBlank(toName)) {
                if (to.length == 1) {
                    to[0].setPersonal(toName);
                } else {
                    StringTokenizer st = new StringTokenizer(toName, ",");
                    int i = 0;
                    String toNameToken = null;
                    while (st.hasMoreTokens()) {
                        toNameToken = st.nextToken();
                        log.debug("toName[" + i + "] = " + toNameToken);
                        to[i].setPersonal(toNameToken);
                        i++;
                    }
                }
            }
            message.setRecipients(Message.RecipientType.TO, to);
        } catch (Exception e) {
            throw new EmailException(e.getMessage(), e);
        }
    }

    /**
     * Validate that the message is not null and that the required parameters
     * are set.
     * 
     * @param message
     * @throws ValidationException
     */
    public void validate(EmailMessage message) throws ValidationException {
        if (message == null) {
            throw new MissingParameterException("message");
        }
        List validationResults = ValidationHelper.validate(message);
        if (!validationResults.isEmpty()) {
            throw new ValidationException(validationResults.toString());
        }
    }
}
