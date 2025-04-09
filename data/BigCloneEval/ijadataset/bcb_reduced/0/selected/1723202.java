package gnu.hylafax.util;

import gnu.hylafax.job.ReceiveEvent;
import gnu.hylafax.job.ReceiveListener;
import gnu.hylafax.job.SendEvent;
import gnu.hylafax.job.SendListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.Message;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class implements an example fax job listener. It emails the status
 * updates to a given mailbox.
 * 
 * @author $Author: sjardine $
 * @version $Id: MailListener.java 162 2009-03-26 21:42:09Z sjardine $
 * @see gnu.hylafax.job.SendNotifier
 * @see gnu.hylafax.job.SendEvent
 * @see gnu.hylafax.job.ReceiveNotifier
 * @see gnu.hylafax.job.ReceiveEvent
 * @see gnu.hylafax.util.Notifier
 */
public class MailListener implements SendListener, ReceiveListener {

    static final SimpleDateFormat rfc822df = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z");

    static final String KEY_TO = "notifier.to";

    static final String KEY_FROM = "notifier.from";

    private Properties properties;

    private static final Log log = LogFactory.getLog(MailListener.class);

    public MailListener() {
        properties = new Properties(System.getProperties());
    }

    /**
     * 
     * 
     */
    public void onSendEvent(SendEvent event) {
        try {
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * This method is called when a fax-received event occurs. It composes an
     * email, attaching relevant files and mails it to a target mailbox.
     */
    public void onReceiveEvent(ReceiveEvent event) {
        String subject;
        String body;
        String cid = null;
        Date now = new Date();
        subject = "Facsimile received";
        if ((event.getCidName() != null) && (!"".equals(event.getCidName()))) {
            cid = event.getCidName();
        } else if ((event.getCidNumber() != null) && (!"".equals(event.getCidNumber()))) {
            cid = event.getCidNumber();
        }
        try {
            File f = new File(event.getFilename());
            if (!f.exists()) {
                f = new File("log" + File.separator + "c" + event.getCommunicationIdentifier());
                subject = "Facsimile failed";
                body = "A facsimile failed to be received at " + now + "\n\n" + "See the attached log file for session details.\n";
            } else {
                body = "The attached facsimile was received " + now + "\n";
            }
            if ((event.getMessage() != null) && (!"".equals(event.getMessage()))) {
                body += "The server's message is:\n\n\t" + event.getMessage();
            }
            if (cid != null) subject += " from " + cid;
            Session s = Session.getDefaultInstance(properties);
            MimeMessage msg = new MimeMessage(s);
            msg.addRecipients(Message.RecipientType.TO, properties.getProperty(KEY_TO));
            msg.setSubject(subject);
            msg.addHeader("From", properties.getProperty(KEY_FROM));
            msg.addHeader("Date", rfc822df.format(now));
            msg.addHeader("X-MailListener", "$Id: MailListener.java 162 2009-03-26 21:42:09Z sjardine $");
            MimeBodyPart part0 = new MimeBodyPart();
            part0.setText(body);
            FileDataSource fds = new FileDataSource(f);
            fds.setFileTypeMap(new MimetypesFileTypeMap());
            DataHandler fdh = new DataHandler(fds);
            MimeBodyPart part1 = new MimeBodyPart();
            part1.setDataHandler(fdh);
            part1.setDisposition(Part.INLINE);
            part1.setFileName(f.getName());
            MimeMultipart mp = new MimeMultipart();
            mp.addBodyPart(part0);
            mp.addBodyPart(part1);
            msg.setContent(mp);
            Transport.send(msg);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
