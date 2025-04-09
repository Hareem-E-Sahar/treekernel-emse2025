package org.synthful.mail;

import java.util.Properties;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Date;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * sendmail Class.
 */
public class sendmail {

    /**
	 * Send.
	 * 
	 * @param msginfo
	 * @param debug
	 */
    public static void send(Hashtable msginfo, boolean debug) {
        String from = "" + msginfo.get("from");
        Object recip = msginfo.get("recipient");
        String host = "" + msginfo.get("host");
        String subject = "" + msginfo.get("subject");
        String message = "" + msginfo.get("message");
        if (recip == null) return;
        if (recip instanceof Hashtable) send(from, (Hashtable) recip, host, subject, message, debug); else send(from, "" + recip, host, subject, message, debug);
    }

    /**
	 * Send.
	 * Initiate sending a mail message.
	 * 
	 * @param from
	 * @param to
	 * @param host
	 * @param subject
	 * @param msgText
	 * @param debug
	 */
    public static void send(String from, String to, String host, String subject, String msgText, boolean debug) {
        Properties props = System.getProperties();
        props.put("mail.smtp.host", host);
        Session session = Session.getInstance(props, null);
        session.setDebug(debug);
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            InternetAddress[] address = { new InternetAddress(to) };
            msg.setRecipients(Message.RecipientType.TO, address);
            msg.setSubject(subject);
            msg.setText(msgText);
            msg.setSentDate(new Date());
            Transport.send(msg);
        } catch (MessagingException mex) {
            mex.printStackTrace();
            Exception ex = null;
            if ((ex = mex.getNextException()) != null) {
                ex.printStackTrace();
            }
        }
    }

    /**
	 * Send.
	 * 
	 * @param from
	 * @param recipient
	 * @param host
	 * @param subject
	 * @param msgText
	 * @param debug
	 */
    public static void send(String from, Hashtable recipient, String host, String subject, String msgText, boolean debug) {
        Properties props = System.getProperties();
        props.put("mail.smtp.host", host);
        Session session = Session.getInstance(props, null);
        session.setDebug(debug);
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            Enumeration renum = recipient.keys();
            while (renum.hasMoreElements()) {
                Object rkey = renum.nextElement();
                if (rkey instanceof Message.RecipientType) {
                    Message.RecipientType rtype = (Message.RecipientType) rkey;
                    Object rvo = recipient.get(rkey);
                    InternetAddress[] addresses = null;
                    if (rvo instanceof String[]) {
                        String[] rvs = (String[]) rvo;
                        addresses = new InternetAddress[rvs.length];
                        for (int rr = 0; rr < rvs.length; rr++) addresses[rr] = new InternetAddress(rvs[rr]);
                    } else if (rvo instanceof InternetAddress[]) {
                        addresses = (InternetAddress[]) rvo;
                    } else if (rvo instanceof InternetAddress) {
                        InternetAddress[] addrs = { (InternetAddress) rvo };
                        addresses = addrs;
                    } else if (rvo instanceof String) {
                        InternetAddress[] addrs = { new InternetAddress("" + rvo) };
                        addresses = addrs;
                    }
                    msg.setRecipients(rtype, addresses);
                }
            }
            msg.setSubject(subject);
            msg.setText(msgText);
            msg.setSentDate(new Date());
            Transport.send(msg);
        } catch (MessagingException mex) {
            mex.printStackTrace();
            Exception ex = null;
            if ((ex = mex.getNextException()) != null) {
                ex.printStackTrace();
            }
        }
    }
}
