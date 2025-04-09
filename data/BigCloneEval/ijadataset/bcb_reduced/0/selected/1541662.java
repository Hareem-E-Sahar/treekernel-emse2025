package ee.fctwister.wc2010.generic;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;

public class Mail {

    public void postMail(String[] recipients, String subject, String content, String from) throws MessagingException {
        boolean debug = false;
        Properties props = new Properties();
        props.put("mail.smtp.host", "mail.fctwister.ee");
        Session session = Session.getDefaultInstance(props, null);
        session.setDebug(debug);
        Message msg = new MimeMessage(session);
        InternetAddress addressFrom = new InternetAddress(from);
        msg.setFrom(addressFrom);
        InternetAddress[] addressTo = new InternetAddress[recipients.length];
        for (int i = 0; i < recipients.length; i++) {
            addressTo[i] = new InternetAddress(recipients[i]);
        }
        msg.setRecipients(Message.RecipientType.TO, addressTo);
        msg.addHeader("MyHeaderName", "myHeaderValue");
        msg.setSubject(subject);
        msg.setContent(content, "text/html");
        Transport.send(msg);
    }
}
