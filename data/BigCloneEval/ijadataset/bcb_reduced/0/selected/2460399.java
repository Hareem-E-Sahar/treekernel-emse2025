package org.sensorweb.service.wns.protocol;

import org.sensorweb.core.ObjectFactory;
import org.sensorweb.service.wns.CommunicationProtocol;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * @author Xingchen Chu
 * @version 0.1
 *
 * <code> EmailCommunicationProtocol </code>
 */
public class EmailCommunicationProtocol implements CommunicationProtocol {

    private static ObjectFactory context = ObjectFactory.newInstance();

    private static Session session;

    private String emailAddress;

    public EmailCommunicationProtocol(String emailAddress) {
        init();
        this.emailAddress = emailAddress;
    }

    private void init() {
        try {
            final String smtp = context.getProperty("email.host");
            final String username = context.getProperty("email.username");
            final String password = context.getProperty("email.password");
            if (session == null) {
                Properties props = new Properties();
                props.put("mail.smtp.host", smtp);
                session = Session.getDefaultInstance(props, new Authenticator() {

                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(Object content) {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress("notification-admin@unimelb.edu.au"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailAddress));
            message.setSubject("SensorWeb Client Notification");
            message.setText(content.toString());
            Transport.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        EmailCommunicationProtocol protocol = new EmailCommunicationProtocol("starchu1981@hotmail.com");
        protocol.send("Test message");
    }
}
