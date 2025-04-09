package de.jformular.util.mail;

import java.util.Date;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Class declaration
 * @author Frank Dolibois, fdolibois@itzone.de, http://www.itzone.de
 * @version $Id: SmtpMailClient.java,v 1.7 2002/10/14 14:02:26 fdolibois Exp $
 */
public class SmtpMailClient {

    private String msg = "";

    private String to = "";

    private String bcc = "";

    private String from = "";

    private String subject = "";

    private String mailhost = "";

    /**
     * Constructor declaration
     */
    public SmtpMailClient() {
    }

    /**
     */
    public void setTo(String s) {
        to = s;
    }

    /**
     */
    public void setBcc(String s) {
        bcc = s;
    }

    /**
     */
    public void setFrom(String s) {
        from = s;
    }

    /**
     */
    public void setSubject(String s) {
        subject = s;
    }

    /**
     *
     */
    public void setMessage(String s) {
        msg = s;
    }

    /**
     */
    public void setMailhost(String s) {
        mailhost = s;
    }

    /**
     */
    public boolean send() {
        try {
            Properties props = System.getProperties();
            if (!mailhost.equals("")) {
                props.put("mail.smtp.host", mailhost);
            }
            Session session = Session.getDefaultInstance(props, null);
            MimeMessage m = new MimeMessage(session);
            m.setFrom(new InternetAddress(from));
            InternetAddress[] adr_to = new InternetAddress[1];
            adr_to[0] = new InternetAddress(to);
            m.setRecipients(Message.RecipientType.TO, adr_to);
            if ((bcc != null) && (bcc.length() > 0)) {
                InternetAddress[] adr_bcc = new InternetAddress[1];
                adr_bcc[0] = new InternetAddress(bcc);
                m.setRecipients(Message.RecipientType.BCC, adr_bcc);
            }
            m.setSubject(subject);
            m.setSentDate(new Date());
            m.setContent(msg, "text/plain");
            Transport.send(m);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     */
    public void setMailProperties(String to, String bcc, String from, String subject, String message, String mailhost) {
        setTo(to);
        setBcc(bcc);
        setFrom(from);
        setSubject(subject);
        setMessage(message);
        setMailhost(mailhost);
    }
}
