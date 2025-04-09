package uk.ac.ebi.rhea.updater;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.log4j.Logger;

/**
 * Messenger for notifications via e-mail.
 * 
 * @author Rafael Alcántara, adapted from Michael Darsow's
 *         <code>IntEnzMessenger</code>
 * @deprecated use org.apache.log4j.net.SMTPAppender instead
 */
public class Mailer {

    public static final Logger LOGGER = Logger.getLogger(Mailer.class);

    private final Properties mailProperties;

    /**
	 * Basic constructor. The configuration is read from a file in the classpath.
	 * The mandatory properties in the file are:
	 * <ul>
	 * 	<li><code>mailer.mail.from</code>: sender address</li>
	 * 	<li><code>mailer.mail.to</code>: comma-separated list of recipient
	 * 		addresses.</li>
	 * </ul>
	 * And additionally:
	 * <ul>
	 * 	<li><code>mail.smtp.host</code>: can be set to override its
	 * 		default value.</li>
	 * 	<li>Any other custom properties whose value can be retrieved using
	 * 		{@link #getCustomProperty(String)} method. These are useful to
	 * 		define formats in the properties file to be used within the
	 * 		message body.</li>
	 * </ul>
	 * @param configFile name of the properties configuration file
	 * 		(excluding the <code>.properties</code> extension).
	 * @throws IOException
	 */
    public Mailer(String configFile) throws IOException {
        mailProperties = new Properties();
        mailProperties.load(Mailer.class.getClassLoader().getResourceAsStream(configFile + ".properties"));
    }

    public void sendMessage(String subject, String messageBody) {
        Date today = new Date();
        Session session = Session.getDefaultInstance(mailProperties, null);
        Message message = new MimeMessage(session);
        try {
            InternetAddress fromAddress = new InternetAddress(mailProperties.getProperty("mailer.mail.from"));
            message.setFrom(fromAddress);
            InternetAddress[] toAddress = InternetAddress.parse(mailProperties.getProperty("mailer.mail.to"), false);
            message.setRecipients(Message.RecipientType.TO, toAddress);
            message.setSubject(subject);
            message.setText(messageBody);
            message.setSentDate(today);
            Transport.send(message);
            LOGGER.info("Sent mail to " + mailProperties.getProperty("mailer.mail.to"));
        } catch (MessagingException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
	 * Retrieves a custom property from the configuration file to use it
	 * in message body formatting.
	 * @param propKey
	 * @return
	 */
    public String getCustomProperty(String propKey) {
        return mailProperties.getProperty(propKey);
    }
}
