package org.jazzteam.edu.mail;

import jade.util.leap.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailController {

    private String port;

    private String email;

    private String password;

    private Properties props;

    private Session sess;

    private Message msg;

    public MailController(String port, String email, String password) {
        super();
        this.port = port;
        this.email = email;
        this.password = password;
        props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.port.socketFactory.port", MailController.this.port);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        sess = Session.getDefaultInstance(props, new javax.mail.Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(MailController.this.email, MailController.this.password);
            }
        });
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void sendEmail(String receiverEmail, String TitleMessage, String textMessage) throws AddressException, MessagingException {
        msg = new MimeMessage(sess);
        msg.setFrom(new InternetAddress(email));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiverEmail));
        msg.setSubject(TitleMessage);
        msg.setText(textMessage);
        Transport.send(msg);
    }
}
