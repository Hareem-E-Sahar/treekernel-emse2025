package com.jlccaires.mss.server.mail;

import java.io.FileInputStream;
import java.util.Date;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendMail {

    private static final String account = "account";

    private static final String password = "password";

    private static final String conPropsFile = SendMail.class.getResource("mail.properties").getFile();

    private SendMail() {
    }

    public static void postMail(String recipient, String subject, String message) {
        postMail(new String[] { recipient }, subject, message);
    }

    /**Envia emails para endereço(s) 
	 * 
	 * @param recipient destinatario da mensagem
	 * @param subject assunto
	 * @param message corpo da mensagem
	 * @return true se enviado
	 */
    public static boolean postMail(String[] recipients, String subject, String message) {
        if (recipients.length == 0) throw new IllegalArgumentException("É necessário informar pelo menos um destinatário.");
        final Properties authProps = new Properties();
        try {
            authProps.load(new FileInputStream(conPropsFile));
        } catch (Exception e1) {
            e1.printStackTrace();
            return false;
        }
        Authenticator auth = new Authenticator() {

            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(authProps.get(account).toString(), authProps.get(password).toString());
            }
        };
        Session session = Session.getInstance(authProps, auth);
        session.setDebug(true);
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(authProps.get(account).toString()));
            InternetAddress[] addrs = new InternetAddress[recipients.length];
            for (int i = 0; i < recipients.length; i++) {
                addrs[i] = new InternetAddress(recipients[i]);
            }
            msg.addRecipients(Message.RecipientType.BCC, addrs);
            msg.setSentDate(new Date());
            msg.setSubject(subject);
            msg.setContent(message, "text/html");
            Transport.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
