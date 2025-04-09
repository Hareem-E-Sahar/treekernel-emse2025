package mecca.service;

import java.util.Date;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * @author Shamsul Bahrin Abd Mutalib
 * @version 1.01
 */
public class MailService {

    String msgText = "This is a message body.\nHere's the second line.";

    String to = "sbahrin@in-fusion.com.my";

    String cc = "";

    String bcc = "";

    String from = "Portal Messenger Service";

    String host = "mail.in-fusion.com.my";

    String subject = "JAVAMAIL API TEST";

    public static void main(String args[]) {
        MailService mail = new MailService();
        mail.setBody("Test test test");
        mail.setTo("sbahrin@in-fusion.com.my");
        mail.setFrom("admin@in-fusion.com.my");
        mail.setHost("mail.in-fusion.com.my");
        mail.send();
    }

    public void setSubject(String s) {
        subject = s;
    }

    public void setBody(String s) {
        msgText = s;
    }

    public void setTo(String s) {
        to = s;
    }

    public void setCc(String s) {
        cc = s;
    }

    public void setBcc(String s) {
        bcc = s;
    }

    public void setFrom(String s) {
        from = s;
    }

    public void setHost(String s) {
        host = s;
    }

    public void send() {
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        Session session = Session.getDefaultInstance(props, null);
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            if (cc != null || !"".equals(cc)) msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc, false));
            if (bcc != null || !"".equals(bcc)) msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc, false));
            msg.setSubject(subject);
            msg.setSentDate(new Date());
            msg.setText(msgText);
            Transport.send(msg);
        } catch (MessagingException mex) {
            System.out.println("\n--Exception handling in msgsendsample.java");
            mex.printStackTrace();
            System.out.println();
            Exception ex = mex;
            do {
                if (ex instanceof SendFailedException) {
                    SendFailedException sfex = (SendFailedException) ex;
                    Address[] invalid = sfex.getInvalidAddresses();
                    if (invalid != null) {
                        System.out.println("    ** Invalid Addresses");
                        if (invalid != null) {
                            for (int i = 0; i < invalid.length; i++) System.out.println("         " + invalid[i]);
                        }
                    }
                    Address[] validUnsent = sfex.getValidUnsentAddresses();
                    if (validUnsent != null) {
                        System.out.println("    ** ValidUnsent Addresses");
                        if (validUnsent != null) {
                            for (int i = 0; i < validUnsent.length; i++) System.out.println("         " + validUnsent[i]);
                        }
                    }
                    Address[] validSent = sfex.getValidSentAddresses();
                    if (validSent != null) {
                        System.out.println("    ** ValidSent Addresses");
                        if (validSent != null) {
                            for (int i = 0; i < validSent.length; i++) System.out.println("         " + validSent[i]);
                        }
                    }
                }
                System.out.println();
                if (ex instanceof MessagingException) ex = ((MessagingException) ex).getNextException(); else ex = null;
            } while (ex != null);
        }
    }
}
