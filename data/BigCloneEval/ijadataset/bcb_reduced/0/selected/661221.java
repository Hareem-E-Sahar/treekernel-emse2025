package au.edu.uq.itee.eresearch.dimer.webapp.app.util;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import au.edu.uq.itee.eresearch.dimer.webapp.app.FileMonitorJob;
import au.edu.uq.itee.eresearch.dimer.webapp.app.GlobalProperties;

public class EmailUtils {

    private static String fromEmail = GlobalProperties.properties.getProperty("email.fromAddress");

    private static String hostName = GlobalProperties.properties.getProperty("email.serverName");

    private static Logger log = LoggerFactory.getLogger(FileMonitorJob.class);

    public static void sendMail(String recipient, String subject, String message) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", hostName);
            Session session = Session.getDefaultInstance(props, null);
            session.setDebug(true);
            Message msg = new MimeMessage(session);
            InternetAddress addressFrom;
            addressFrom = new InternetAddress(fromEmail);
            msg.setFrom(addressFrom);
            InternetAddress[] addressTo = new InternetAddress[1];
            addressTo[0] = new InternetAddress(recipient);
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            msg.setSubject(subject);
            msg.setContent(message, "text/plain");
            Transport.send(msg);
        } catch (AddressException e) {
            log.error("Error encountered sending email.", e);
        } catch (MessagingException e) {
            log.error("Error encountered sending email.", e);
        }
    }
}
