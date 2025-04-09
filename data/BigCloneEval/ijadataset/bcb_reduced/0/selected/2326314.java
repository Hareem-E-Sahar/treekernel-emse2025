package br.ufc.quixada.adrs.util;

import java.util.Date;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author ismaily
 */
public final class SendMail {

    /**
     * Overcomes the default constructor. Utility classes should be final and must not have public constructors.
     */
    private SendMail() {
    }

    public static void sendMail(String mailServer, String from, String to, String subject, String mensagem) throws MessagingException {
        Properties mailProps = new Properties();
        mailProps.put("mail.smtp.host", mailServer);
        mailProps.put("mail.smtp.auth", "true");
        Session mailSession = Session.getDefaultInstance(mailProps, new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("sistema.adrs", "ufcquixada");
            }
        });
        String texto = mensagem;
        texto = texto.replaceAll("\n", "\r\n");
        mailSession.setDebug(true);
        mailProps.put("mail.debug", "true");
        mailProps.put("mail.smtp.debug", "true");
        mailProps.put("mail.mime.charset", "ISO-8859-1");
        mailProps.put("mail.smtp.port", "465");
        mailProps.put("mail.smtp.starttls.enable", "true");
        mailProps.put("mail.smtp.socketFactory.port", "465");
        mailProps.put("mail.smtp.socketFactory.fallback", "false");
        mailProps.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        Message message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(from));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSentDate(new Date());
        message.setSubject(subject);
        message.setText(mensagem);
        Transport.send(message);
    }

    public static void sendMail(String to, String subject, String mensagem) throws MessagingException {
        SendMail.sendMail("smtp.gmail.com", "sistema.adrs@gmail.com", to, subject, mensagem);
    }

    public static void main(String args[]) throws MessagingException {
        sendMail("ismailybf@gmail.com", "Meu primeiro teste para enviar email", "Meu primeiro teste para enviar email e o trabalho foi ralizado com sucesso.");
    }
}
