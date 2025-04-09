package com.bugfree4j.tools.mail;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * @author bugfree4j To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SmtpMailSender {

    private String host;

    private String port;

    private String from;

    private SmtpMailAuthenticator sma;

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public SmtpMailAuthenticator getSma() {
        return sma;
    }

    public void setSma(SmtpMailAuthenticator sma) {
        this.sma = sma;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void Send(String to, String subject, String msg) throws AddressException, MessagingException {
        Properties props = System.getProperties();
        props.put("mail.smtp.host", this.getHost());
        props.put("mail.smtp.port", this.getPort());
        props.put("mail.smtp.auth", "true");
        Authenticator auth = this.getSma();
        Session session = Session.getDefaultInstance(props, auth);
        Transport transport = session.getTransport("smtp");
        transport.connect(host, this.getSma().getUsername(), this.getSma().getPassword());
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(this.getFrom()));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject);
        message.setText(msg);
        Transport.send(message);
    }
}
