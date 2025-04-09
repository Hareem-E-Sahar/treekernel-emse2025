import java.io.*;
import java.net.*;
import ShopSession;
import EMail;
import Config;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * Verschicken von MIME EMails, ggf auch Multiparts.
 *
 * @see EMail.
 * @Thomas Tieke
 * 
 * History
 * 
 * Datum		Wer		Kommentar
 * 27.04.2000	TT		Komplette Umstellung auf das Mail API
 * 
 */
public class SMTPClient extends ShopSession {

    /**
	 *	Konstruktor
	 */
    public SMTPClient() throws Exception {
        Server = ConfigUtility.theConfig.getMailServer();
    }

    public static boolean sendMail(String Recipient, String Subject, String Text) {
        return sendMail(Recipient, Subject, Text, "Administrator@" + ConfigUtility.theConfig.getShopShortURL());
    }

    public static boolean sendMail(String Recipient, String Subject, String Text, String Sender) {
        try {
            SMTPClient MailClient = new SMTPClient();
            return MailClient.doSendMail(Recipient, Subject, Text, Sender);
        } catch (Exception e) {
        }
        return false;
    }

    public boolean doSendMail(String Recipient, String Subject, String Text, String Sender) {
        try {
            EMail aMail = new EMail();
            aMail.setSubject(Subject);
            aMail.setRecipientAddress(Recipient);
            aMail.setMessage(Text);
            aMail.setSenderAddress(Sender);
            send(aMail);
        } catch (Exception E) {
            logEvent(ERROR, "SMTPClient::sendMail()", "Couldn't send Message. " + "Sender: " + Sender + ", " + "Recipient: " + Recipient + ", " + "Subject: " + Subject + ", " + "Text: " + Text + ", " + "Exception: " + E.toString());
            return false;
        }
        return true;
    }

    /**
	 * Versendet die angegebene EMail
	 */
    public void send(EMail anEMailMessage) {
        String Sender = anEMailMessage.getSenderAddress();
        String Subject = anEMailMessage.getSubject();
        String Text = anEMailMessage.getMessage();
        String Recipient = anEMailMessage.getRecipientAddress();
        try {
            Properties Props = new Properties();
            Props.put("mail.smtp.host", Server);
            Session aSession = Session.getDefaultInstance(Props, null);
            Message Msg = new MimeMessage(aSession);
            InternetAddress From = new InternetAddress(Sender);
            Msg.setFrom(From);
            InternetAddress[] Recipients = { new InternetAddress(Recipient) };
            Msg.setRecipients(Message.RecipientType.TO, Recipients);
            Msg.setSubject(Subject);
            Msg.setContent(Text, "text/html");
            Transport.send(Msg);
        } catch (MessagingException MsgEx) {
            logEvent(ERROR, "Error during sending of EMail", MsgEx.toString());
            MsgEx.printStackTrace();
        } catch (Exception E) {
            logEvent(ERROR, "Error during sending of EMail", E.toString());
        }
    }

    private String Server;
}
