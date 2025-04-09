package adv.tools;

import adv.language.Config;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Properties;
import org.apache.commons.io.FilenameUtils;
import org.h2.util.FileUtils;

/**
 * Alberto Vilches Ratón
 * User: avilches
 * Date: 11-feb-2006
 * Time: 19:32:09
 * To change this template use File | Settings | File Templates.
 */
public class Mail {

    static Properties props = new Properties();

    static Session session = null;

    static {
    }

    public static void set(Properties p) {
        session = Session.getDefaultInstance(p, null);
    }

    public static void set(String host, int port, String login, String password) {
        props.setProperty("mail.smtp.host", host);
        props.setProperty("mail.smtp.port", String.valueOf(port));
        props.setProperty("mail.user", login);
        props.setProperty("mail.password", password);
        session = Session.getDefaultInstance(props, null);
    }

    public static boolean send(String type, String from, String to, String subject, String message) throws MessagingException {
        try {
            sendOnlyMal(from, to, subject, message);
            writeMail(type, message);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            message = e.getMessage() + "\n\n" + message;
            writeMail("!" + type, message);
            return false;
        }
    }

    public static void writeMail(String type, String message) {
        try {
            Config.getMng().getDirMail().mkdirs();
            File fout = new File(getDirMail(), type + "." + Config.sdf.format(new Date()) + ".txt");
            TextTools.print(message, fout, Config.getMng().getEncoding());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendOnlyMal(String from, String to, String subject, String message) throws MessagingException {
        MimeMessage msg = new MimeMessage(session);
        InternetAddress ifrom = null;
        InternetAddress ito = null;
        try {
            ifrom = new InternetAddress(from);
        } catch (Exception e) {
            throw new MessagingException("From address " + from + " is wrong (" + e + ")");
        }
        try {
            ito = new InternetAddress(to);
        } catch (Exception e) {
            throw new MessagingException("To address " + to + " is wrong (" + e + ")");
        }
        msg.setFrom(ifrom);
        msg.setRecipient(MimeMessage.RecipientType.TO, ito);
        msg.setSubject(subject);
        msg.setSentDate(new Date());
        msg.setContent(message, "text/plain");
        Transport.send(msg);
    }

    public static File[] listMails() {
        return getDirMail().listFiles();
    }

    public static String readMail(String name) {
        name = FilenameUtils.getName(name);
        File file = new File(getDirMail(), name);
        if (file.exists() && !file.isDirectory()) {
            try {
                return TextTools.read(new InputStreamReader(new FileInputStream(file)));
            } catch (IOException e) {
                return "Error " + e.getMessage() + " al leer fichero " + name;
            }
        } else {
            return "No existe el fichero " + name;
        }
    }

    public static boolean removeMail(String name) {
        name = FilenameUtils.getName(name);
        File file = new File(getDirMail(), name);
        if (file.exists() && !file.isDirectory()) {
            return file.delete();
        }
        return false;
    }

    public static File getDirMail() {
        return Config.getMng().getDirMail();
    }
}
