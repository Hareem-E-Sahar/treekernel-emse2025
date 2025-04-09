package net.sourceforge.juploader.mailsender;

import java.security.Security;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Klasa pozwalająca na wysyłanie e-maili.
 * 
 * @author Adam Pawelec
 */
public class MailSender {

    /**
     * Wysyła email do kilku odbiorców.
     * 
     * @param recipients lista odbiorców
     * @param subject temat wiadomości
     * @param message treść wiadomośi
     * @param from zawartość pola Od:
     * @param login login dla serwera wysyłającego
     * @param password hasło
     * @throws javax.mail.MessagingException
     */
    public static void sendMail(String[] recipients, String subject, String message, String from, final String login, final String password) throws MessagingException {
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        boolean debug = false;
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", "smtp.gmail.com");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");
        Session session = Session.getDefaultInstance(props, new Authenticator() {

            @Override
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(login, password);
            }
        });
        session.setDebug(debug);
        Message msg = new MimeMessage(session);
        InternetAddress addresFrom = new InternetAddress(from);
        msg.setFrom(addresFrom);
        InternetAddress[] addressTo = new InternetAddress[recipients.length];
        for (int i = 0; i < recipients.length; i++) {
            addressTo[i] = new InternetAddress(recipients[i]);
        }
        msg.setRecipients(Message.RecipientType.TO, addressTo);
        msg.setSubject(subject);
        msg.setContent(message, "text/plain");
        Transport.send(msg);
    }

    /**
     * Wysyła email do jednego odbiorcy.
     * 
     * @param recipient odbiorca
     * @param subject temat wiadomości
     * @param message treść wiadomośi
     * @param from zawartość pola Od:
     * @param login login dla serwera wysyłającego
     * @param password hasło
     * @throws javax.mail.MessagingException
     */
    public static void sendMail(String recipient, String subject, String message, String from, final String login, final String password) throws MessagingException {
        sendMail(new String[] { recipient }, subject, message, from, login, password);
    }
}
