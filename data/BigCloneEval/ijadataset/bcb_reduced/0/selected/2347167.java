package auxiliary_classes;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Send emails to an array of recipients from a sender account using Gmail SMTP with SSL encryption.
 * 
 * Once constructed, this object can repetitively send emails with only a subject and body without
 * inputing recipients and a sender account for each email.
 * 
 * @author Lane Aasen
 */
public class Notifier {

    private String[] recipients;

    private String senderAddress;

    private String senderPassword;

    /**
	 * Constructs a new Notifier object
	 * 
	 * @param String[] recipients
	 * @param String senderAddress
	 * @param String senderPassword
	 */
    public Notifier(String[] recipients, String senderAddress, String senderPassword) {
        this.setRecipients(recipients);
        this.senderAddress = senderAddress;
        this.senderPassword = senderPassword;
    }

    /**
	 * Sends an email to the notifier's recipients from the notifier's senderAddress using Gmail SMTP with SSL encryption.
	 * 
	 * @param String subject 
	 * @param String body
	 */
    public void sendGmailSSL(String subject, String body) {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "465");
        Authenticator sessionAuthenticator = new Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderAddress.substring(0, senderAddress.indexOf("@")), senderPassword);
            }
        };
        Session currentSession = Session.getDefaultInstance(properties, sessionAuthenticator);
        try {
            Message message = new MimeMessage(currentSession);
            message.setFrom(new InternetAddress(this.senderAddress));
            InternetAddress[] recipientAddresses = new InternetAddress[this.getRecipients().length];
            for (int i = 0; i < this.getRecipients().length; i++) recipientAddresses[i] = new InternetAddress(this.getRecipients()[i]);
            message.setRecipients(Message.RecipientType.TO, recipientAddresses);
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
        } catch (MessagingException me) {
            me.printStackTrace();
        }
    }

    /**
	 * Returns the contents of a file in a string.
	 * 
	 * @param File file 
	 * @return String output file in string format
	 * @throws FileNotFoundException
	 */
    public static String fileToString(File file) throws FileNotFoundException {
        Date currentDate = new Date(System.currentTimeMillis());
        String output = "Time: " + currentDate.toString();
        Scanner fileScan = new Scanner(file);
        while (fileScan.hasNextLine()) {
            output = (output + "\n" + fileScan.nextLine());
        }
        return output;
    }

    /**
	 * Generates a permutation of a Version 1 UUID (Universally Unique IDentifier), because normal UUIDs are much too mainstream.
	 * 
	 * The original V1 UUID combines the user's MAC address and the time in 100-nanosecond intervals since the Western adoption of the Gregorian Calendar.
	 * However, this approach reveals a significant amount of personal information.
	 * To remedy this and provide the user with virtually complete anonymity, this version concatenates a one-way hash of the user's MAC address
	 * with the current Unix time.
	 * If the system's MAC address is not available, its host name (computer name) is used instead.
	 * 
	 * TODO: Change time measurement method to avoid Unix apocalypse in 2038.
	 * 
	 * @return String UUID
	 */
    public static String generateUUID() {
        String UUID = "0d";
        byte[] systemID = null;
        try {
            NetworkInterface netInterface = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            systemID = netInterface.getHardwareAddress();
        } catch (SocketException se) {
            se.printStackTrace();
        } catch (UnknownHostException uhe) {
            uhe.printStackTrace();
        } catch (NullPointerException ne) {
            System.err.println("~OS does not support MAC address~");
            try {
                InetAddress inet = InetAddress.getLocalHost();
                String hostName = inet.getHostName();
                systemID = hostName.getBytes();
            } catch (UnknownHostException uhe) {
                uhe.printStackTrace();
            } catch (SecurityException se) {
                se.printStackTrace();
                System.err.println("~Retrieving host name forbidden by security manager~");
            }
        } finally {
            BigInteger macInt = new BigInteger(systemID);
            String macHex = macInt.toString(16);
            int macHash = macHex.hashCode();
            String macHashHex = Integer.toHexString(macHash);
            if (macHashHex.length() % 2 != 0) macHashHex = "0" + macHashHex;
            UUID = (UUID + "-" + macHashHex);
        }
        Long unixTime = System.currentTimeMillis() / 1000L;
        String unixHex = Long.toHexString(unixTime);
        if (unixHex.length() % 2 != 0) unixHex = "0" + unixHex;
        UUID = (UUID + "-" + unixHex);
        return UUID;
    }

    /**
	 * Sets the Notifier's recipients.
	 * 
	 * @param recipients
	 */
    public void setRecipients(String[] recipients) {
        this.recipients = recipients;
    }

    /**
	 * Returns the Notifier's recipients.
	 * 
	 * @return recipients.
	 */
    public String[] getRecipients() {
        return this.recipients;
    }
}
