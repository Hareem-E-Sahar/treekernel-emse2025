package mailManagement;

import java.io.File;
import java.security.Security;
import java.util.Date;
import java.util.Properties;
import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;
import logManagement.Log4k;
import objects.Macro;

/**
 *
 * @author FastLDL
 */
public class Mailer {

    private static final String UNIhostName = "mail.unitn.it";

    private static final String GoogleHostName = "smtp.gmail.com";

    private static final String GoogleUsername = "Camel.MCR@gmail.com";

    private static final String Google_pwd = "3cammelli";

    private static String strConcat(String[] list) {
        String resString = "";
        for (int i = 0; i < list.length; i++) {
            resString += list[i] + ",";
        }
        return resString;
    }

    /**
     * Permette di mandare una email e separa il codice in base al fatto che
     * il parametro attachName sia null o meno.
     * In caso ci sia un allegato, il suo nome corrispondera' a quello del file
     * specificato.
     */
    private static synchronized void sendMail(String sender, String[] receivers, String subject, String msg, String attachName) {
        try {
            Properties props = System.getProperties();
            Session session = null;
            if (Macro.UNI) {
                props.put("mail.smtp.host", UNIhostName);
                props.put("mail.debug", "true");
                session = Session.getDefaultInstance(props);
            } else {
                Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
                props.setProperty("mail.smtp.host", GoogleHostName);
                props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.setProperty("mail.smtp.socketFactory.fallback", "false");
                props.setProperty("mail.smtp.port", "465");
                props.setProperty("mail.smtp.socketFactory.port", "465");
                props.put("mail.smtp.auth", "true");
                props.put("mail.debug", "true");
                Authenticator authenticator = new Authenticator() {

                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(GoogleUsername, Google_pwd);
                    }
                };
                session = Session.getDefaultInstance(props, authenticator);
            }
            Message message = new MimeMessage(session);
            InternetAddress from = new InternetAddress(sender);
            String strReceiversList = strConcat(receivers);
            InternetAddress to[] = InternetAddress.parse(strReceiversList);
            message.setFrom(from);
            message.setRecipients(Message.RecipientType.TO, to);
            message.setSubject(subject);
            message.setSentDate(new Date());
            if (attachName == null) {
                message.setText(msg);
            } else {
                Multipart multipart = new MimeMultipart();
                BodyPart messageBodyPart1 = new MimeBodyPart();
                messageBodyPart1.setText(msg);
                BodyPart messageBodyPart2 = new MimeBodyPart();
                DataSource source = new FileDataSource(new File(attachName));
                messageBodyPart2.setDataHandler(new DataHandler(source));
                String cutAttachName = attachName.substring(attachName.lastIndexOf("/") + 1);
                messageBodyPart2.setFileName(cutAttachName);
                multipart.addBodyPart(messageBodyPart1);
                multipart.addBodyPart(messageBodyPart2);
                message.setContent(multipart);
            }
            Transport.send(message);
        } catch (MessagingException ex) {
            Log4k.error(Mailer.class.getName(), ex.getMessage());
        }
    }

    /**
     * Permette di mandare una email di testo con un allegato.
     * Non sincronizzato perche' lo e' gia' il metodo privato a cui riferisce
     */
    public static void mail(String sender, String[] receivers, String subject, String msg) {
        sendMail(sender, receivers, subject, msg, null);
    }

    /**
     * Permette di mandare una email di testo con un allegato.
     * Non sincronizzato perche' lo e' gia' il metodo privato a cui riferisce
     */
    public static void mail(String sender, String[] receivers, String subject, String msg, String attachName) {
        sendMail(sender, receivers, subject, msg, attachName);
    }
}
