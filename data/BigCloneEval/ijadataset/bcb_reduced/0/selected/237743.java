package nuts.core.net;

import java.util.List;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.NamingException;
import nuts.core.lang.PrivateAccessUtils;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;

/**
 * a class for send mail
 */
public class SendMail {

    /**
	 * @param email email
	 * @throws EmailException if an error occurs
	 */
    @SuppressWarnings("unchecked")
    public static void send(Email email) throws EmailException {
        try {
            List<InternetAddress> toList = (List<InternetAddress>) PrivateAccessUtils.getFieldValue(email, "toList");
            send(toList, email);
            List<InternetAddress> ccList = (List<InternetAddress>) PrivateAccessUtils.getFieldValue(email, "ccList");
            send(ccList, email);
            List<InternetAddress> bccList = (List<InternetAddress>) PrivateAccessUtils.getFieldValue(email, "bccList");
            send(bccList, email);
        } catch (EmailException e) {
            throw e;
        } catch (Throwable t) {
            throw new EmailException(t);
        }
    }

    /**
	 * send email to the specified address list
	 *
	 * @param ias address list
	 * @param email email
	 * @throws EmailException if an error occurs
	 */
    public static void send(List<InternetAddress> ias, Email email) throws EmailException {
        if (ias != null && !ias.isEmpty()) {
            for (InternetAddress ia : ias) {
                send(ia, email);
            }
        }
    }

    /**
	 * send email to the specified address
	 *
	 * @param ia address
	 * @param email email
	 * @throws EmailException if an error occurs
	 */
    public static void send(InternetAddress ia, Email email) throws EmailException {
        EmailException ee = new EmailException("Invalid email address: " + ia.getAddress());
        String[] ss = ia.getAddress().split("@");
        if (ss.length != 2) {
            throw ee;
        }
        List<String> hosts;
        try {
            hosts = MXLookup.lookup(ss[1]);
        } catch (NamingException e) {
            throw new EmailException(e);
        }
        for (String host : hosts) {
            try {
                PrivateAccessUtils.setFieldValue(email, "session", null);
            } catch (Exception e) {
                throw new EmailException("failed to clear session", e);
            }
            try {
                email.setHostName(host);
                email.buildMimeMessage();
                MimeMessage message = email.getMimeMessage();
                try {
                    Transport.send(message, new InternetAddress[] { ia });
                } catch (Throwable t) {
                    String msg = "Sending the email to the following server failed : " + email.getHostName() + ":" + email.getSmtpPort();
                    throw new EmailException(msg, t);
                }
                return;
            } catch (EmailException e) {
                ee = e;
            }
        }
        throw ee;
    }
}
