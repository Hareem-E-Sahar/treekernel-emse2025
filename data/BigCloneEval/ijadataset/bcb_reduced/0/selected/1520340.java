package com.freeture.frmwk.net.client.mail;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.log4j.Logger;

public class SMTPClient {

    private String server;

    public String sender;

    public String to;

    public String cc;

    public String bcc;

    public String sujet;

    public String body;

    private MimeMessage message;

    private String contentType = "text/html; charset=ISO-8859-1";

    static Logger logger = Logger.getLogger(SMTPClient.class);

    /**
     * Initialise l'objet Mailer
     *
     * @param serveur nom du serveur smtp
     */
    public SMTPClient(String serveur, String sender) {
        Properties props = System.getProperties();
        props.put("mail.smtp.host", this.server);
        Session session = Session.getDefaultInstance(props, null);
        setMessage(new MimeMessage(session));
        this.sender = sender;
    }

    private void setMessage(MimeMessage mimeMessage) {
        this.message = mimeMessage;
    }

    public void send() throws IOException {
        try {
            this.message.setFrom(new InternetAddress(this.sender));
            String[] emailToArray = this.to.split(",");
            for (int i = 0; i < emailToArray.length; i++) {
                this.message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailToArray[i]));
            }
            String[] emailToArrayCC = this.cc.split(",");
            for (int i = 0; i < emailToArrayCC.length; i++) {
                this.message.addRecipient(Message.RecipientType.CC, new InternetAddress(emailToArrayCC[i]));
            }
            this.message.setSubject(this.sujet);
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setContent(this.body, this.contentType);
            Multipart mp = new MimeMultipart();
            mp.addBodyPart(textPart);
            this.message.setContent(mp);
            Transport.send(this.message);
            logger.info("Envoi du mail");
        } catch (AddressException e) {
            logger.error("Adresse mail mal configur�");
            e.printStackTrace();
        } catch (MessagingException e) {
            logger.info("Serveur mail non configur�");
            e.printStackTrace();
        }
    }

    /**
     * @param email
     * @return
     * @throws MessagingException
     */
    public Message addFile(String strAttachment) throws MessagingException {
        File attachment;
        attachment = new File(strAttachment);
        BodyPart messageBodyPart = new MimeBodyPart();
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(this.body, this.contentType);
        Multipart mp = new MimeMultipart();
        mp.addBodyPart(textPart);
        messageBodyPart.setContent(mp);
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        messageBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(attachment);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(attachment.getAbsolutePath());
        multipart.addBodyPart(messageBodyPart);
        this.message.setContent(multipart);
        return this.message;
    }

    public void addTo(String to) {
        this.to.concat(",").concat(to);
    }

    public void addCc(String cc) {
        this.cc.concat(",").concat(cc);
    }

    public void addBcc(String bcc) {
        this.bcc.concat(",").concat(bcc);
    }

    public void setSujet(String sujet) {
        this.sujet = sujet;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
