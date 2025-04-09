package nl.tomvanzummeren.appengine.javamail;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import java.util.Properties;

/**
 * Small adjustment of the Spring mail sender implementation to make it work with the mail implementation of the
 * Google App Engine.
 *
 * @author Tom van Zummeren
 * @see org.springframework.mail.javamail.JavaMailSenderImpl
 */
public class GaeJavaMailSender extends JavaMailSenderImpl {

    /**
     * {@inheritDoc}
     */
    @Override
    public Session getSession() {
        return Session.getDefaultInstance(new Properties(), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Transport getTransport(Session session) throws NoSuchProviderException {
        return new Transport(session, null) {

            @Override
            public void connect(String host, int port, String username, String password) throws MessagingException {
            }

            @Override
            public void close() throws MessagingException {
            }

            @Override
            public void sendMessage(Message message, Address[] recipients) throws MessagingException {
                Transport.send(message, recipients);
            }
        };
    }
}
