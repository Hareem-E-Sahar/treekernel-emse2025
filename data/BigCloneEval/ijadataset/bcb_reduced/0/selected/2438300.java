package com.incendiaryblue.user;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import java.io.*;
import java.util.*;
import java.net.URLEncoder;

/** Another mailing class. Basically, the current set of mailing classes
    do not have the ability to attatch files. This class has been written
    to rectify the situation. It makes use of the JavaMail reference
    implementation provided by Sun.

    <P>
    Much of this code is basically taken from the example application
    sendfile.java supplied with the JavaMail download. There have
    been some additions though.

    <P>
    The methods do not provide great error reporting. All they do is return
    a boolean indicating whether the mail sending process was successful.
    Note that if the recipient of the message does not exist, false will
    <B>not</B> be returned.
*/
public class Notification {

    public static boolean sendMail(String server, String fromAddress, String toAddress, String subject, String message) {
        String[] toAddresses = { toAddress };
        File[] files = {};
        return sendMail(server, fromAddress, fromAddress, fromAddress, fromAddress, toAddresses, subject, message, files);
    }

    public static boolean sendMail(String server, String fromAddress, String toAddress, String subject, String message, File aFile) {
        String[] toAddresses = { toAddress };
        File[] files = { aFile };
        return sendMail(server, fromAddress, fromAddress, fromAddress, fromAddress, toAddresses, subject, message, files);
    }

    /**
    @param server The SMTP server that the method should use to send a message
    @param fromAddress The email address that the message should say it's from
    @param fromName The name of the person who sent the email (mail clients will probably
                    use this to display as the sender of the mail)
    @param replyTo The email address that any reply message should be directed to
    @param replyToName The name of the person that the reply should be sent to.
    @param toAddress An array of email addresses that the mail should be sent to
    @param subject The subject field of the message
    @param message The actual message as a string
    @param attatchedFiles An array of files that should be attatched to this message.
                          If this array has no elements, then no files will be attatched.

  */
    public static boolean sendMail(String server, String fromAddress, String fromName, String replyToAddress, String replyToName, String[] toAddresses, String subject, String message, File[] attatchedFiles) {
        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", server);
        Session session = Session.getDefaultInstance(properties, null);
        try {
            MimeMessage msg = new MimeMessage(session);
            int numToAddress = toAddresses.length;
            int numFiles = attatchedFiles.length;
            int i;
            Address[] toInternetAddresses = new InternetAddress[numToAddress];
            Address[] replyToAddresses = { new InternetAddress(replyToAddress, replyToName) };
            MimeBodyPart textPart = new MimeBodyPart();
            MimeBodyPart[] fileParts = new MimeBodyPart[numFiles];
            MimeMultipart allTheParts = new MimeMultipart();
            msg.setFrom(new InternetAddress(fromAddress, fromName));
            msg.setReplyTo(replyToAddresses);
            msg.setSubject(subject);
            for (i = 0; i < numToAddress; i++) {
                toInternetAddresses[i] = new InternetAddress(toAddresses[i]);
            }
            msg.setRecipients(Message.RecipientType.TO, toInternetAddresses);
            if (numFiles > 0) {
                textPart.setText(message);
                allTheParts.addBodyPart(textPart);
                msg.setText("This message is in MIME format. Since your mail reader does not understand this format, some or all of this message may not be legible.");
                for (i = 0; i < numFiles; i++) {
                    if (attatchedFiles[i].exists()) {
                        fileParts[i] = new MimeBodyPart();
                        fileParts[i].setFileName(attatchedFiles[i].getName());
                        fileParts[i].setDataHandler(new DataHandler(new FileDataSource(attatchedFiles[i])));
                        allTheParts.addBodyPart(fileParts[i]);
                    }
                }
                msg.setContent(allTheParts);
            } else msg.setText(message);
            Transport.send(msg);
            return true;
        } catch (UnsupportedEncodingException uee) {
            return false;
        } catch (AddressException ae) {
            return false;
        } catch (MessagingException me) {
            return false;
        }
    }

    public static void sendBulkMail(String from, String subject, String message, String receiverGroup) {
        String SMTP_SERVER = com.incendiaryblue.appframework.ServerConfig.get("mailhost");
        String FROM = com.incendiaryblue.appframework.ServerConfig.get("bulk_mail_from");
        if (!from.equals("")) FROM = from;
        String FROM_NAME = com.incendiaryblue.appframework.ServerConfig.get("bulk_mail_from_name");
        String[] TO_ADDRESSES = { com.incendiaryblue.appframework.ServerConfig.get("bulk_mail_to") };
        String REPLY_TO = com.incendiaryblue.appframework.ServerConfig.get("bulk_mail_reply_to");
        String REPLY_NAME = com.incendiaryblue.appframework.ServerConfig.get("bulk_mail_reply_name");
        String TEMP_DIR = com.incendiaryblue.appframework.ServerConfig.get("bulk_mail_temp_dir");
        String USER_EMAIL_PROPERTY = com.incendiaryblue.appframework.ServerConfig.get("users_email_property");
        String SITE_NAME = com.incendiaryblue.appframework.ServerConfig.get("site_name");
        if (from.equals("") || from == null) from = FROM;
        String mesFileName = TEMP_DIR + "message.txt", emailFileName = TEMP_DIR + "emails.txt", headerFileName = TEMP_DIR + "header.txt", emails = "", headers = "", subject_message = "";
        List users = (Group.getGroup(receiverGroup)).getUsers();
        Iterator it = users.iterator();
        while (it.hasNext()) {
            User user = (User) it.next();
            String emailAddress = user.getPropertyValue(USER_EMAIL_PROPERTY);
            if (emailAddress != null) emails += emailAddress + "\n";
        }
        headers = "site_name: " + SITE_NAME + "\n" + "from_name: " + FROM_NAME + "\n" + "from_addr: " + FROM + "\n" + "reply_name: " + REPLY_NAME + "\n" + "reply_to: " + REPLY_TO + "\n";
        File mesFile, emailFile, headerFile;
        FileWriter mesFileWr, emailFileWr, headerFileWr;
        try {
            mesFile = new File(mesFileName);
            emailFile = new File(emailFileName);
            headerFile = new File(headerFileName);
            mesFileWr = new FileWriter(mesFile);
            emailFileWr = new FileWriter(emailFile);
            headerFileWr = new FileWriter(headerFile);
            subject_message = "subject: " + subject + "\n\n" + message + "\n";
            synchronized (mesFileWr) {
                mesFileWr.write(subject_message);
                mesFileWr.close();
            }
            synchronized (emailFileWr) {
                emailFileWr.write(emails);
                emailFileWr.close();
            }
            synchronized (headerFileWr) {
                headerFileWr.write(headers);
                headerFileWr.close();
            }
            File[] attachments = { mesFile, emailFile, headerFile };
            Notification.sendMail(SMTP_SERVER, FROM, FROM_NAME, REPLY_TO, REPLY_NAME, TO_ADDRESSES, subject, "", attachments);
        } catch (FileNotFoundException e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean sendMail(String server, String toAddress, String fromName, String fromAddress, String ccAddress, String bccAddress, String subject, String message) {
        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", server);
        Session session = Session.getDefaultInstance(properties, null);
        Address ccInternetAddress = null, bccInternetAddress = null;
        try {
            MimeMessage msg = new MimeMessage(session);
            int ii;
            Address toInternetAddress = new InternetAddress(toAddress);
            msg.setRecipients(Message.RecipientType.TO, new Address[] { toInternetAddress });
            if (ccAddress != null && !ccAddress.equals("")) {
                ccInternetAddress = new InternetAddress(ccAddress);
                msg.setRecipients(Message.RecipientType.CC, new Address[] { ccInternetAddress });
            }
            if (bccAddress != null && !bccAddress.equals("")) {
                bccInternetAddress = new InternetAddress(bccAddress);
                msg.setRecipients(Message.RecipientType.BCC, new Address[] { bccInternetAddress });
            }
            MimeBodyPart textPart = new MimeBodyPart();
            msg.setFrom(new InternetAddress(fromAddress, fromName));
            msg.setSubject(subject);
            msg.setText(message);
            Transport.send(msg);
            return true;
        } catch (UnsupportedEncodingException uee) {
            System.err.println("U:" + uee);
            return false;
        } catch (AddressException ae) {
            System.err.println("A:" + ae);
            return false;
        } catch (MessagingException me) {
            System.err.println("M:" + me);
            return false;
        }
    }
}
