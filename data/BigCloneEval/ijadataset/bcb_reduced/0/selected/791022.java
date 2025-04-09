package net.sf.jerkbot.plugins.authentication.util;

import net.sf.jerkbot.exceptions.ConfigurationException;
import net.sf.jerkbot.util.IOUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

/**
 * @author Yves Zoundi <yveszoundi at users dot sf dot net>
 *         Utility class to send emails
 * @version 0.0.1
 */
public class EmailUtil {

    public static final String MAIL_ENV_PROP = "jerkbot.mail.config";

    private static final Logger Log = LoggerFactory.getLogger(EmailUtil.class.getName());

    public static final String USER_PROPERTY = "mail.user";

    public static final String PASSWORD_PROPERTY = "mail.password";

    private static final String JAVAMAIL_PROPERTIES_URL = "http://www.websina.com/bugzero/kb/javamail-properties.html";

    /**
     * Get the email configuration settings
     *
     * @return the email configuration settings
     * @throws Exception An error
     */
    private static Properties getConfig() throws IOException, ConfigurationException {
        final Properties config = new Properties();
        final String configPath = System.getProperty(MAIL_ENV_PROP);
        if (!StringUtils.isEmpty(configPath)) {
            File configFile = new File(configPath);
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(configFile);
                config.load(inputStream);
            } finally {
                IOUtil.closeQuietly(inputStream);
            }
        } else {
            StrBuilder sb = new StrBuilder();
            sb.append("Unable to find mail configuration,").append(" using system property:").append(MAIL_ENV_PROP).appendNewLine().appendNewLine().append("Javamail config:").append(IOUtil.getTinyUrl(JAVAMAIL_PROPERTIES_URL));
            throw new ConfigurationException(sb.toString());
        }
        return config;
    }

    /**
     * Sends an email
     *
     * @param to      The email destination address
     * @param subject The email subject
     * @param body    The email body
     * @throws Exception An error while sending the message
     */
    public static void send(String to, String subject, String body) throws Exception {
        Properties props = null;
        try {
            props = getConfig();
        } catch (ConfigurationException e) {
            throw e;
        }
        final String username = props.getProperty(USER_PROPERTY);
        final String password = props.getProperty(PASSWORD_PROPERTY);
        Authenticator authenticator = null;
        if (!StringUtils.isEmpty(password)) {
            authenticator = new javax.mail.Authenticator() {

                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            };
        } else {
            Log.warn("CouLn't find credentials using properties '{}' and '{}'", USER_PROPERTY, PASSWORD_PROPERTY);
        }
        Session session = Session.getDefaultInstance(props, authenticator);
        InternetAddress[] destinationAddress = InternetAddress.parse(to, false);
        Message msg = new MimeMessage(session);
        msg.setRecipients(Message.RecipientType.TO, destinationAddress);
        msg.setSubject(subject);
        msg.setDataHandler(new DataHandler(new ByteArrayDataSource(body, "text/plain")));
        msg.setHeader("X-Mailer", "JerkBotEmailer");
        msg.setSentDate(new Date());
        Transport.send(msg);
    }
}
