package org.academ.jabber.services;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import static org.academ.jabber.constants.Constant.*;

/**
 *
 * @author moskvin
 */
public class Mail {

    private static Properties config = new Properties();

    static {
        fetchConfig();
    }

    /**
     * Open a specific text file containing mail server
     * parameters, and populate a corresponding Properties object.
     */
    private static void fetchConfig() {
        InputStream input = null;
        try {
            input = new FileInputStream("src/main/resources/mail.properties");
            config.load(input);
        } catch (IOException ex) {
            System.err.println("Cannot open and load mail server properties file.");
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                System.err.println("Cannot close mail server properties file.");
            }
        }
    }

    /**
     * Send a single email.
     */
    public void sendEmail(String from, String to, String subject, String body) {
        Session session = Session.getDefaultInstance(config, null);
        MimeMessage message = new MimeMessage(session);
        try {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject, ENCODING);
            message.setText(body, ENCODING);
            Transport.send(message);
        } catch (MessagingException ex) {
            System.err.println("Cannot send email. " + ex);
        }
    }

    /**
     * Allows the config to be refreshed at runtime, instead of
     * requiring a restart.
     */
    public static void refreshConfig() {
        config.clear();
        fetchConfig();
    }
}
