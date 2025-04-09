package ecom.ibeer.managedbeans.tools.mail;

import java.rmi.RemoteException;
import java.util.Properties;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * @author Administrator
 *
 */
public class SendMail {

    private static final Logger LOGGER = Logger.getLogger("SendMail");

    /**
	 * Send mail
	 * @param mail
	 * @throws RemoteException
	 */
    public static void sendMail(Mail mail) throws RemoteException {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.orange.fr");
            Session session = Session.getDefaultInstance(props, null);
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(mail.getFromAddr()));
            InternetAddress[] tos = InternetAddress.parse(mail.getToAddr());
            msg.setRecipients(Message.RecipientType.TO, tos);
            msg.setSubject(mail.getSubject());
            msg.setText(mail.getBody());
            Transport.send(msg);
            LOGGER.info("Password sent to: " + mail.getToAddr());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
