package krico.arara.util.mail;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Locale;
import java.util.Date;
import java.text.MessageFormat;
import java.net.InetAddress;
import java.net.UnknownHostException;
import krico.arara.model.User;
import krico.arara.model.UserProfile;
import krico.arara.model.calendar.RecordAlarm;
import krico.arara.model.calendar.Record;
import org.apache.log4j.Category;

/**
 * This class has utility methos for mailing
 * @author @arara-author@
 * @version @arara-version@
 * @sf $Header: /cvsroot/arara/arara/sources/krico/arara/util/mail/MailUtils.java,v 1.6 2002/02/15 18:58:32 krico Exp $
 */
public class MailUtils {

    public static final String CREDITS = "Arara Web Agenda by (krico@kriconet.com.br)";

    private static final String BUNDLE_NAME = "krico/arara/i18n/Fancy";

    static Category logger = Category.getInstance(MailUtils.class);

    private String from;

    Properties props;

    public String url;

    public MailUtils(Properties prop) {
        this.props = prop;
        from = props.getProperty("krico.arara.fromAddress");
        url = props.getProperty("krico.arara.mail.url");
    }

    public void send(RecordAlarm alarm, User user) throws MessagingException {
        if (alarm == null) throw new MessagingException("Alarm cannot be null");
        Record r = alarm.getRecord();
        if (r == null) throw new MessagingException("Alarm must contain a record");
        String to = "\"" + user.getName() + "\" <" + ((UserProfile) user.getProfile()).getEmail() + ">";
        String description = r.getDescription();
        Locale loc = user.getLocale() == null ? Locale.getDefault() : user.getLocale();
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, loc);
        String title = "Message Alarm from {0}";
        String body = "-----------------\nRecord information\n start: {0,time} {0,date}\nend: {1,time} {1,date}.\nowner: {2}\n-----------------\nRecord content:\n";
        String body_after = "\n-----------------\nTo access the service point your browser to {0}";
        if (bundle != null) {
            title = bundle.getString("alarm.mail.title");
            body = bundle.getString("alarm.mail.pre_body");
            body_after = bundle.getString("alarm.mail.post_body");
        }
        title = MessageFormat.format(title, new Object[] { new String(r.getOwner().getLogin()) });
        MessageFormat mf = new MessageFormat(body);
        mf.setLocale(loc);
        mf.applyPattern(body);
        description = mf.format(new Object[] { r.getStartTime(), r.getEndTime(), r.getOwner().getLogin() }) + "\n\n" + description;
        if (url != null) description += "\n\n" + MessageFormat.format(body_after, new Object[] { url });
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(title);
        message.setText(description);
        try {
            message.setHeader("X-Mailer", MimeUtility.encodeText(CREDITS));
        } catch (Exception e) {
            logger.debug("Could not set credits", e);
        }
        message.setSentDate(new Date());
        Transport.send(message);
    }

    /**
   * Verifies (as well as possible) if the <code>email</code> is a valid email by finding out if the domain exists
   */
    public static void isEmailValid(String email) throws MessagingException, UnknownHostException {
        if (email == null || email.trim().equals("")) throw new MessagingException("EMail cannot be null");
        int idx = -1;
        if ((idx = email.indexOf("@")) == -1) throw new MessagingException("EMail must contain \"@\"");
        new InternetAddress(email);
        java.net.InetAddress.getByName(email.substring(idx + 1, email.length()));
    }
}
