package se.kth.speech.skatta.player;

import se.kth.speech.skatta.util.ExtendedElement;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.Attributes;
import javax.naming.directory.Attribute;
import javax.naming.NamingException;
import java.util.Properties;
import java.util.Comparator;
import java.util.Arrays;

/**
 * A class to hold e-mail information and send a notification message on
 * request.
 */
public class EmailNotifier {

    private Message message;

    private String messageText;

    private String generateTheRealMessage(String name) {
        StringBuffer headerBuffer = new StringBuffer(messageText);
        for (int i = 0; i < headerBuffer.length() - 1; i++) {
            if (headerBuffer.charAt(i) == '$') {
                if (headerBuffer.charAt(i + 1) == '$') {
                    headerBuffer.deleteCharAt(i);
                } else if (headerBuffer.charAt(i + 1) == 'n') {
                    headerBuffer.deleteCharAt(i);
                    headerBuffer.deleteCharAt(i);
                    headerBuffer.insert(i, name);
                }
            }
        }
        return new String(headerBuffer);
    }

    public EmailNotifier(String to, String from, String server, String subject, String text) throws MessagingException, NamingException {
        String username = null;
        String password = null;
        messageText = text;
        if (server == null || server.equals("")) {
            server = lookupMailHosts(to.substring(to.indexOf('@') + 1))[0];
        }
        Properties props = new Properties();
        props.put("mail.smtp.host", server);
        Authenticator auth;
        if (username != null && password != null) {
            props.put("mail.smtp.auth", "true");
            auth = new SMTPAuthenticator(username, password);
        } else auth = null;
        Session session = Session.getDefaultInstance(props, auth);
        message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject);
    }

    /**
     * Creates an EmailNotifier with a message to be sent later. Called by the
     * constructor of Test.
     *
     * @param source The source ExtendedElement
     */
    public EmailNotifier(ExtendedElement source) throws NamingException, MessagingException {
        this(source.attribute("receiver"), source.attribute("sender"), source.attribute("server"), source.textChild("subject"), source.textChild("message"));
    }

    /**
     * Sends the message loaded into the object. Called by Test.save.
     * @param theName The name of the subject.
     */
    public void sendMessage(String theName) throws MessagingException {
        message.setText(generateTheRealMessage(theName));
        Transport.send(message);
    }

    /**
     * SimpleAuthenticator is used to do simple authentication
     * when the SMTP server requires it.
     */
    private static class SMTPAuthenticator extends javax.mail.Authenticator {

        private String username, password;

        SMTPAuthenticator(String user, String pass) {
            username = user;
            password = pass;
        }

        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
        }
    }

    public static void main(String[] args) throws Exception {
        String to = "john.lindberg@gmail.com";
        String from = to;
        String server = null;
        String subject = "Test";
        String messageText = "Test text";
        String username = null;
        String password = null;
        if (server == null) {
            server = lookupMailHosts(to.substring(to.indexOf('@') + 1))[0];
        }
        System.out.println(server);
        System.exit(0);
        Properties props = new Properties();
        props.put("mail.smtp.host", server);
        Authenticator auth;
        if (username != null && password != null) {
            props.put("mail.smtp.auth", "true");
            auth = new SMTPAuthenticator(username, password);
        } else auth = null;
        Session session = Session.getDefaultInstance(props, auth);
        session.setDebug(true);
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject);
        message.setText(messageText);
        Transport.send(message);
    }

    private static String[] lookupMailHosts(String domainName) throws NamingException {
        InitialDirContext iDirC = new InitialDirContext();
        Attributes attributes = iDirC.getAttributes("dns:/" + domainName, new String[] { "MX" });
        Attribute attributeMX = attributes.get("MX");
        if (attributeMX == null) {
            return (new String[] { domainName });
        }
        String[][] pvhn = new String[attributeMX.size()][2];
        for (int i = 0; i < attributeMX.size(); i++) {
            pvhn[i] = ("" + attributeMX.get(i)).split("\\s+");
        }
        Arrays.sort(pvhn, new Comparator<String[]>() {

            public int compare(String[] o1, String[] o2) {
                return (Integer.parseInt(o1[0]) - Integer.parseInt(o2[0]));
            }
        });
        String[] sortedHostNames = new String[pvhn.length];
        for (int i = 0; i < pvhn.length; i++) {
            sortedHostNames[i] = pvhn[i][1].endsWith(".") ? pvhn[i][1].substring(0, pvhn[i][1].length() - 1) : pvhn[i][1];
        }
        return sortedHostNames;
    }
}
