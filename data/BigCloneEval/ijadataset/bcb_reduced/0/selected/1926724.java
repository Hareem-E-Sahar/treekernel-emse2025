package dmp.mail;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.jboss.seam.annotations.Name;
import java.net.InetAddress;
import java.util.Properties;

/** MailNotifier - a utility class to send a SMTP mail notification **/
@Name("jmail")
public class Jmail {

    private String localhost;

    private String mailhost;

    private String mailuser;

    private String email_notify;

    protected Session session = null;

    /**
	 * @return the localhost
	 */
    public String getLocalhost() {
        return localhost;
    }

    /**
	 * @return the mailhost
	 */
    public String getMailhost() {
        return mailhost;
    }

    /**
	 * @return the mailuser
	 */
    public String getMailuser() {
        return mailuser;
    }

    /**
	 * @return the email_notify
	 */
    public String getEmail_notify() {
        return email_notify;
    }

    /**
	 * @return the session
	 */
    public Session getSession() {
        return session;
    }

    /**
	 * @param localhost the localhost to set
	 */
    public void setLocalhost(String localhost) {
        this.localhost = localhost;
    }

    /**
	 * @param mailhost the mailhost to set
	 */
    public void setMailhost(String mailhost) {
        this.mailhost = mailhost;
    }

    /**
	 * @param mailuser the mailuser to set
	 */
    public void setMailuser(String mailuser) {
        this.mailuser = mailuser;
    }

    /**
	 * @param email_notify the email_notify to set
	 */
    public void setEmail_notify(String email_notify) {
        this.email_notify = email_notify;
    }

    /**
	 * @param session the session to set
	 */
    public void setSession(Session session) {
        this.session = session;
    }

    public Jmail(String _localhost, String _mailhost, String _mailuser, String _email_notify) {
        localhost = _localhost;
        mailhost = _mailhost;
        mailuser = _mailuser;
        email_notify = _email_notify;
    }

    public void send(String subject, String text) throws Exception {
        send(email_notify, subject, text);
    }

    public void send(String _to, String subject, String text) throws Exception {
        if (session == null) {
            Properties p = new Properties();
            p.put("mail.host", mailhost);
            p.put("mail.user", mailuser);
            session = Session.getDefaultInstance(p, null);
            Properties properties = session.getProperties();
            String key = "mail.smtp.localhost";
            String prop = properties.getProperty(key);
            if (prop == null) properties.put(key, localhost); else System.out.println(key + ": " + prop);
        }
        MimeMessage msg = new MimeMessage(session);
        msg.setText(text);
        msg.setSubject(subject);
        Address fromAddr = new InternetAddress(mailuser);
        msg.setFrom(fromAddr);
        Address toAddr = new InternetAddress(_to);
        msg.addRecipient(Message.RecipientType.TO, toAddr);
        Transport.send(msg);
    }

    /**
     * Get the name of the local host, for use in the EHLO and HELO commands.
     * The property mail.smtp.localhost overrides what InetAddress would tell
     * us.
      Adapted from SMTPTransport.java
     */
    public String getLocalHost() {
        String localHostName = null;
        String name = "smtp";
        try {
            if (localHostName == null || localHostName.length() <= 0) localHostName = session.getProperty("mail." + name + ".localhost");
            if (localHostName == null || localHostName.length() <= 0) localHostName = InetAddress.getLocalHost().getHostName();
        } catch (Exception uhex) {
        }
        return localHostName;
    }
}
