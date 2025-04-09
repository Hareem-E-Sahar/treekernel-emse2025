package org.primordion.xholon.io;

import java.io.File;
import java.util.Date;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.primordion.xholon.app.Application;
import org.primordion.xholon.util.StringTokenizer;

/**
 * Send email messages from an executing Xholon application.
 * The text of the message can be either plain or html.
 * One file attachment can optionally be included in the email message.
 * @author <a href="mailto:ken@primordion.com">Ken Webb</a>
 * @see <a href="http://www.primordion.com/Xholon">Xholon Project website</a>
 * @since 0.7.1 (Created on December 16, 2007)
 */
public class Mail implements IMail {

    protected String host = "localhost";

    protected String port = "25";

    protected String mimeType = "text/plain";

    protected String username = "";

    protected String password = "";

    protected String from = "";

    protected String to = "";

    protected boolean debug = false;

    /**
	 * Constructor.
	 * @param mailParams A comma-delimited string of Mail parameters.
	 */
    public Mail(String mailParams) {
        initialize(mailParams);
    }

    public void initialize(String mailParams) {
        StringTokenizer st = new StringTokenizer(mailParams, ",");
        host = st.nextToken();
        port = st.nextToken();
        mimeType = st.nextToken();
        username = st.nextToken();
        password = st.nextToken();
        from = st.nextToken();
        to = st.nextToken();
    }

    public void sendMailMessage(String subject, String text, String attachment) {
        Properties props = new Properties();
        props.setProperty("mail.smtp.host", host);
        props.setProperty("mail.smtp.port", port);
        if (debug) {
            props.put("mail.debug", new Boolean(debug));
        }
        Session session = Session.getInstance(props, null);
        session.setDebug(debug);
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            InternetAddress[] address = { new InternetAddress(to) };
            msg.setRecipients(Message.RecipientType.TO, address);
            msg.setSubject(subject);
            msg.setSentDate(new Date());
            if (attachment == null) {
                msg.setDataHandler(new DataHandler(text, mimeType));
            } else {
                MimeBodyPart msgBodyPart = new MimeBodyPart();
                Multipart multipart = new MimeMultipart();
                msgBodyPart.setDataHandler(new DataHandler(text, mimeType));
                multipart.addBodyPart(msgBodyPart);
                msgBodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource(attachment);
                msgBodyPart.setDataHandler(new DataHandler(source));
                msgBodyPart.setFileName(new File(attachment).getName());
                multipart.addBodyPart(msgBodyPart);
                msg.setContent(multipart);
            }
            Transport.send(msg);
        } catch (MessagingException e) {
            Application.getLogger().error("Mail: Unable to send message.", e);
        }
    }

    /**
	 * Test the Xholon Mail class.
	 * @param args None are expected.
	 */
    public static void main(String[] args) {
        String from = "ken@primordion.com";
        String to = "kenwebb@users.sourceforge.net";
        String mimeType = "text/html";
        String mailParams = "mail.primordion.com,26," + mimeType + ",user,pwd," + from + "," + to;
        String subject = "You have a message from Xholon Mail";
        String text = "<HTML><HEAD><TITLE>" + subject + "</TITLE></HEAD>" + "<BODY><p><strong>A bolded line.</strong></p></BODY></HTML>";
        String attachment = "/example.jpg";
        IMail mail = new Mail(mailParams);
        mail.sendMailMessage(subject, text, attachment);
    }
}
