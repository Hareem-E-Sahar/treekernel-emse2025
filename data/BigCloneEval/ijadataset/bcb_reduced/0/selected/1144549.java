package org.jabusuite.mail;

import java.util.Date;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.jabusuite.core.utils.JbsObject;
import org.jabusuite.logging.Logger;

public class JbsMail extends JbsObject {

    Logger logger = Logger.getLogger(JbsMail.class);

    private String subject;

    private String text;

    private JbsMailAccount account;

    public JbsMail() {
    }

    public JbsMail(JbsMailAccount account) {
        this.account = account;
    }

    public JbsMail(String subject, String text) {
        this.subject = subject;
        this.text = text;
    }

    public JbsMail(JbsMailAccount account, String subject, String text) {
        this.account = account;
        this.subject = subject;
        this.text = text;
    }

    /**
     * Dieser Construkter erzeugt eine vollständige JbsMail und Vers
     * 
     * 
     * @param account
     * @param mailAddress
     * @param subject
     * @param text
     */
    public JbsMail(JbsMailAccount account, String mailAddress, String subject, String text) throws AddressException, MessagingException, EJbsMail {
        this.account = account;
        this.subject = subject;
        this.text = text;
    }

    /**@TODO
     * Die Klasse JbsUser braucht ein Feld JbsMailAccount!   
     * 
     * @param mailAddress
     * @throws org.jabusuite.mail.EJbsMail
     */
    public void send(String mailAddress) throws EJbsMail, AddressException, MessagingException {
        if (getAccount() == null) throw new EJbsMail(EJbsMail.ET_NOACCOUNT);
        if ((mailAddress == null) || (mailAddress.trim().equals(""))) throw new EJbsMail(EJbsMail.ET_NORECIVER);
        if ((getSubject() == null) || (getSubject().trim().equals(""))) throw new EJbsMail(EJbsMail.ET_NOSUBJECT);
        if ((getText() == null) || (getText().trim().equals(""))) throw new EJbsMail(EJbsMail.ET_NOTEXT);
        JbsMailAuthenticator auth = new JbsMailAuthenticator(getAccount().getName(), getAccount().getPasswort());
        Properties properties = new Properties();
        properties.put("mail.smtp.host", getAccount().getMailHost());
        properties.put("mail.smtp.auth", "true");
        Session session = Session.getInstance(properties, auth);
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(account.getMailAddress()));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailAddress, false));
        msg.setSubject(subject);
        msg.setText(text);
        msg.setHeader("X-Mailer", "OpenJBS-Mailer <http://openjbs.gs-networks.de>");
        msg.setSentDate(new Date());
        Transport.send(msg);
        logger.info("Send Mail to " + mailAddress + " with Subject: " + subject);
    }

    public JbsMailAccount getAccount() {
        return account;
    }

    public void setAccount(JbsMailAccount account) {
        this.account = account;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
