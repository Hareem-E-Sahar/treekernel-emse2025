package org.efs.openreports.providers;

import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.Message.RecipientType;
import javax.mail.internet.*;
import org.apache.log4j.Logger;
import org.efs.openreports.engine.output.ReportEngineOutput;
import org.efs.openreports.objects.MailMessage;
import org.efs.openreports.objects.ORProperty;
import org.efs.openreports.util.ByteArrayDataSource;
import org.efs.openreports.util.SMTPAuthenticator;

public class MailProvider {

    protected static Logger log = Logger.getLogger(MailProvider.class.getName());

    private PropertiesProvider propertiesProvider;

    private String mailHost;

    private boolean useMailAuthenticator;

    private String userName;

    private String password;

    private Session mailSession;

    public MailProvider(PropertiesProvider propertiesProvider) throws ProviderException {
        this.propertiesProvider = propertiesProvider;
        init();
    }

    protected void init() throws ProviderException {
        mailHost = null;
        useMailAuthenticator = false;
        userName = null;
        password = null;
        ORProperty property = propertiesProvider.getProperty(ORProperty.MAIL_SMTP_HOST);
        if (property != null) mailHost = property.getValue();
        property = propertiesProvider.getProperty(ORProperty.MAIL_SMTP_AUTH);
        if (property != null) useMailAuthenticator = Boolean.valueOf(property.getValue()).booleanValue();
        property = propertiesProvider.getProperty(ORProperty.MAIL_AUTH_USER);
        if (property != null) userName = property.getValue();
        property = propertiesProvider.getProperty(ORProperty.MAIL_AUTH_PASSWORD);
        if (property != null) password = property.getValue();
        log.info("Created: Use Mail Authenticator = " + useMailAuthenticator);
    }

    public void sendMail(MailMessage mail) throws ProviderException {
        try {
            if (mailSession == null) {
                Properties mailProps = new Properties();
                mailProps.put("mail.smtp.host", mailHost);
                if (mail.getBounceAddress() != null && mail.getBounceAddress().trim().length() > 0) {
                    mailProps.setProperty("mail.smtp.from", mail.getBounceAddress());
                }
                if (useMailAuthenticator) {
                    mailSession = Session.getInstance(mailProps, new SMTPAuthenticator(userName, password));
                } else {
                    mailSession = Session.getInstance(mailProps, null);
                }
            }
            Multipart multipart = new MimeMultipart();
            if (mail.getText() != null && (mail.getByteArrayDataSource() == null || !mail.getByteArrayDataSource().getContentType().equals(ReportEngineOutput.CONTENT_TYPE_TEXT))) {
                MimeBodyPart mbpText = new MimeBodyPart();
                mbpText.setText(mail.getText());
                multipart.addBodyPart(mbpText);
            }
            ArrayList<String> attachments = mail.getAttachments();
            for (int i = 0; i < attachments.size(); i++) {
                String fileAttachment = attachments.get(i);
                FileDataSource source = new FileDataSource(fileAttachment);
                MimeBodyPart mbpAttachment = new MimeBodyPart();
                mbpAttachment.setDataHandler(new DataHandler(source));
                mbpAttachment.setFileName(fileAttachment);
                multipart.addBodyPart(mbpAttachment);
            }
            if (mail.getByteArrayDataSource() != null) {
                String contentType = mail.getByteArrayDataSource().getContentType();
                if (contentType != null && (contentType.equals(ReportEngineOutput.CONTENT_TYPE_HTML) || contentType.equals(ReportEngineOutput.CONTENT_TYPE_TEXT))) {
                    Multipart htmlMP = new MimeMultipart("related");
                    MimeBodyPart htmlBP = new MimeBodyPart();
                    htmlBP.setDataHandler(new DataHandler(mail.getByteArrayDataSource()));
                    htmlMP.addBodyPart(htmlBP);
                    ArrayList<ByteArrayDataSource> images = mail.getHtmlImageDataSources();
                    for (int i = 0; i < images.size(); i++) {
                        DataSource imageDS = images.get(i);
                        MimeBodyPart imageBodyPart = new MimeBodyPart();
                        imageBodyPart.setFileName(imageDS.getName());
                        imageBodyPart.setText(imageDS.getName());
                        imageBodyPart.setDataHandler(new DataHandler(imageDS));
                        imageBodyPart.setHeader("Content-ID", "<" + imageDS.getName() + ">");
                        imageBodyPart.setDisposition(javax.mail.Part.INLINE);
                        htmlMP.addBodyPart(imageBodyPart);
                    }
                    BodyPart completeHtmlBP = new MimeBodyPart();
                    completeHtmlBP.setContent(htmlMP);
                    multipart.addBodyPart(completeHtmlBP);
                } else {
                    MimeBodyPart mbpAttachment = new MimeBodyPart();
                    mbpAttachment.setDataHandler(new DataHandler(mail.getByteArrayDataSource()));
                    mbpAttachment.setFileName(mail.getByteArrayDataSource().getName());
                    multipart.addBodyPart(mbpAttachment);
                }
            }
            Message msg = new MimeMessage(mailSession);
            msg.setFrom(new InternetAddress(mail.getSender()));
            msg.setSubject(mail.getSubject());
            msg.setContent(multipart);
            msg.setSentDate(new Date());
            ArrayList<String> recipients = mail.getRecipients();
            for (int i = 0; i < recipients.size(); i++) {
                RecipientType recipientType = RecipientType.TO;
                StringTokenizer tokenizer = new StringTokenizer(recipients.get(i), ":");
                if (tokenizer.countTokens() == 2) {
                    String type = tokenizer.nextToken();
                    if ("TO".equalsIgnoreCase(type)) {
                        recipientType = RecipientType.TO;
                    } else if ("CC".equalsIgnoreCase(type)) {
                        recipientType = RecipientType.CC;
                    } else if ("BCC".equalsIgnoreCase(type)) {
                        recipientType = RecipientType.BCC;
                    }
                }
                msg.addRecipient(recipientType, new InternetAddress(tokenizer.nextToken()));
            }
            Transport.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            throw new ProviderException(e.getMessage());
        }
    }

    public void setMailHost(String mailHost) {
        this.mailHost = mailHost;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUseMailAuthenticator(boolean useMailAuthenticator) {
        this.useMailAuthenticator = useMailAuthenticator;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPropertiesProvider(PropertiesProvider propertiesProvider) {
        this.propertiesProvider = propertiesProvider;
    }

    public void setMailSession(Session mailSession) {
        this.mailSession = mailSession;
    }
}
