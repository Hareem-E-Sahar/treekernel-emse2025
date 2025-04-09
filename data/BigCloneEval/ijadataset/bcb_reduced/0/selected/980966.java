package ru.adv.util;

import javax.mail.MessagingException;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * User: roma
 * Date: 08.11.2005
 * Time: 12:00:28
 */
public class Mail {

    public static void sendMailMessage(final MailParams params, String message) throws MessagingException {
        sendMailMessage(params.getFrom(), params.getTo(), params.getSubject(), params.getEncoding(), params.getContentType(), message, params.getServer());
    }

    public static void sendMailMessage(final String from, final String to, String subject, final String encoding, final String contentType, final String txtMessage, final String server) throws MessagingException {
        Properties mailProps = new Properties();
        mailProps.put("mail.transport.protocol", "smtp");
        mailProps.put("mail.smtp.host", server == null || server.length() == 0 ? "localhost" : server);
        javax.mail.Session session = javax.mail.Session.getInstance(mailProps);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        setAddress(to, Message.RecipientType.TO, message);
        if (encoding == null) {
            message.setSubject(subject);
        } else {
            message.setSubject(subject, encoding);
        }
        MimeBodyPart mbp1 = new MimeBodyPart();
        if ("text/plain".equals(contentType)) {
            mbp1.setText(txtMessage, encoding);
        } else {
            mbp1.setContent(txtMessage, contentType);
        }
        MimeMultipart mp = new MimeMultipart();
        mp.addBodyPart(mbp1);
        message.setContent(mp);
        message.setSentDate(new Date());
        Transport.send(message);
    }

    private static void setAddress(String email, Message.RecipientType type, MimeMessage message) throws AddressException, MessagingException {
        StringTokenizer st = new StringTokenizer(email, ",");
        boolean first = true;
        while (st.hasMoreTokens()) {
            String tmp = st.nextToken().trim();
            if (first) {
                message.setRecipient(type, new InternetAddress(tmp));
                first = false;
            } else {
                message.addRecipient(type, new InternetAddress(tmp));
            }
        }
    }
}
