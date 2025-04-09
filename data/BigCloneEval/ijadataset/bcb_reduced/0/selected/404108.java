package org.isportal.portlet.maillist;

import org.isportal.db.tables.Kategorija;
import org.isportal.db.tables.User;
import org.isportal.portlet.IPortlet;
import org.isportal.portlet.Portlets;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;

/**
 * Portlet za posiljanje elektronskih obvestil
 * Uporabi vmesnik MailMan!!
 * 
 * @author Skupina za posiljanje elektronskih obvestil
 * 
 * @see org.isportal.portlet.IPortlet
 */
public class MailListPortlet implements IPortlet, MailMan {

    public static final int PUBLIC = 0;

    public static final int PRIVATE = 1;

    private String smtp = Portlets.getPortalConfig("smtp");

    private Integer privacy = MailListPortlet.PUBLIC;

    public String getSmtp() {
        return smtp;
    }

    public void setSmtp(String smtp) {
        this.smtp = smtp;
    }

    public Integer getPrivacy() {
        return privacy;
    }

    public void setPrivacy(Integer privacy) {
        this.privacy = privacy;
    }

    public String getAuthor() {
        return "Skupina za pošiljanje elektronskih obvestil";
    }

    public String getTitle() {
        return "Pošiljanje elektronskih obvestil";
    }

    public String getJSPPath() {
        return "maillist/";
    }

    public void sendMail(String recipient, String subject, String message, String from) throws MessagingException {
        sendMailOne(recipient, subject, message, from);
    }

    public void sendMail(Set recipients, String subject, String message, String from) throws MessagingException {
        sendMailMany(recipients, subject, message, from);
    }

    public void sendMail(List recipients, String subject, String message, String from) throws MessagingException {
        Set recset = new HashSet();
        if (recipients == null) {
            return;
        }
        recset.addAll(recipients);
        sendMailMany(recset, subject, message, from);
    }

    public void sendMail(Kategorija kategorija, String subject, String message, String from) throws MessagingException {
        List recipients = SubscriptionDAO.getKategorijaUsers(kategorija);
        sendMail(recipients, subject, message, from);
    }

    private void sendMailOne(String recipient, String subject, String message, String from) throws MessagingException {
        boolean debug = false;
        Properties props = new Properties();
        props.put("mail.smtp.host", Portlets.getPortalConfig("smtp"));
        props.put("mail.mime.charset", Portlets.getPortalConfig("charset"));
        Session session = Session.getDefaultInstance(props, null);
        session.setDebug(debug);
        Message msg = new MimeMessage(session);
        InternetAddress addressFrom = new InternetAddress(from);
        msg.setFrom(addressFrom);
        InternetAddress addressTo = new InternetAddress(recipient);
        if (privacy.compareTo(MailListPortlet.PUBLIC) == 0) {
            msg.setRecipient(Message.RecipientType.TO, addressTo);
        } else if (privacy.compareTo(MailListPortlet.PRIVATE) == 0) {
            msg.setRecipient(Message.RecipientType.BCC, addressTo);
        } else {
            System.out.println("Napačen tip vidnosti naslovov!");
        }
        msg.setSubject(subject);
        msg.setContent(message, "text/plain; charset=" + Portlets.getPortalConfig("charset"));
        Transport.send(msg);
    }

    private void sendMailMany(Set recipients, String subject, String message, String from) throws MessagingException {
        boolean debug = false;
        List emails = new ArrayList();
        for (Iterator iter = recipients.iterator(); iter.hasNext(); ) {
            User user = (User) iter.next();
            emails.add(user.getEmail());
        }
        String[] rec = new String[emails.size()];
        for (int i = 0; i < emails.size(); i++) {
            rec[i] = (String) ((emails.toArray())[i]);
        }
        Properties props = new Properties();
        props.put("mail.smtp.host", Portlets.getPortalConfig("smtp"));
        props.put("mail.mime.charset", Portlets.getPortalConfig("charset"));
        Session session = Session.getDefaultInstance(props, null);
        session.setDebug(debug);
        Message msg = new MimeMessage(session);
        InternetAddress addressFrom = new InternetAddress(from);
        msg.setFrom(addressFrom);
        InternetAddress[] addressTo = new InternetAddress[rec.length];
        for (int i = 0; i < rec.length; i++) {
            addressTo[i] = new InternetAddress(rec[i]);
        }
        if (privacy.compareTo(MailListPortlet.PUBLIC) == 0) {
            msg.setRecipients(Message.RecipientType.TO, addressTo);
        } else if (privacy.compareTo(MailListPortlet.PRIVATE) == 0) {
            msg.setRecipients(Message.RecipientType.BCC, addressTo);
        } else {
            System.out.println("Napačen tip vidnosti naslovov!");
        }
        msg.setSubject(subject);
        msg.setContent(message, "text/plain; charset=" + Portlets.getPortalConfig("charset"));
        Transport.send(msg);
    }
}
