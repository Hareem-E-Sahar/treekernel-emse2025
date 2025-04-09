package utils;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import beans.UserDetailsBean;

public class SendEmail {

    String d_email = "alexnoel81@gmail.com";

    String d_password = "12345alex";

    private String d_host = "smtp.gmail.com";

    private String d_port = "465";

    private String m_subject = "Online Social Network: Comments";

    private String m_text = "";

    private String loginPageLink = "http://";

    public SendEmail(String to, String pass, UserDetailsBean userInfo) {
        Properties props = new Properties();
        props.put("mail.smtp.user", d_email);
        props.put("mail.smtp.host", d_host);
        props.put("mail.smtp.port", d_port);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.socketFactory.port", d_port);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        @SuppressWarnings("unused") SecurityManager security = System.getSecurityManager();
        try {
            Authenticator auth = new SMTPAuthenticator();
            Session session = Session.getInstance(props, auth);
            MimeMessage msg = new MimeMessage(session);
            m_text = "Dear " + userInfo.getFirstName() + " " + userInfo.getLastName() + "\n\n" + "Here is the new password for your account at netSalesForum. " + "\nOnce you login using this password;" + "go to your profile and change the password if this one is difficult to remember.\n\n" + "New password: " + pass + " \n\n" + "Click here to open the netsalesforum home page: " + loginPageLink + "\n\n netSalesForum automated system.";
            msg.setText(m_text);
            msg.setSubject(m_subject);
            msg.setFrom(new InternetAddress(d_email));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            Transport.send(msg);
        } catch (Exception mex) {
            mex.printStackTrace();
        }
    }

    public SendEmail(String from, String subject, String message) {
        Properties props = new Properties();
        props.put("mail.smtp.user", d_email);
        props.put("mail.smtp.host", d_host);
        props.put("mail.smtp.port", d_port);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.socketFactory.port", d_port);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        @SuppressWarnings("unused") SecurityManager security = System.getSecurityManager();
        try {
            Authenticator auth = new SMTPAuthenticator();
            Session session = Session.getInstance(props, auth);
            MimeMessage msg = new MimeMessage(session);
            msg.setText(message);
            msg.setSubject(subject);
            msg.setFrom(new InternetAddress(from));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(d_email));
            Transport.send(msg);
        } catch (Exception mex) {
            mex.printStackTrace();
            Exception e = mex;
            Log.debug(Log.GENERAL, " Email can not be sent");
            Log.fatal(Log.ADMIN, "Send e-mail does not work", e);
        }
    }

    /**
	 * 
	 * @author Phesto
	 * I you uncoment the following method and comment the main.
	 * you can call this method from other classes and pass email as a parameter.
	 */
    public class SMTPAuthenticator extends javax.mail.Authenticator {

        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(d_email, d_password);
        }
    }
}
