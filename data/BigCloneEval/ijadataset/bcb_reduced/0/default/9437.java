import java.util.*;
import java.io.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

/**
 * sendfile will create a multipart message with the second
 * block of the message being the given file.<p>
 *
 * This demonstrates how to use the FileDataSource to send
 * a file via mail.<p>
 *
 * usage: <code>java sendfile <i>to from smtp file true|false</i></code>
 * where <i>to</i> and <i>from</i> are the destination and
 * origin email addresses, respectively, and <i>smtp</i>
 * is the hostname of the machine that has smtp server
 * running.  <i>file</i> is the file to send. The next parameter
 * either turns on or turns off debugging during sending.
 *
 * @author	Christopher Cotton
 */
public class sendfile {

    public static void main(String[] args) {
        if (args.length != 5) {
            System.out.println("usage: java sendfile <to> <from> <smtp> <file> true|false");
            System.exit(1);
        }
        String to = args[0];
        String from = args[1];
        String host = args[2];
        String filename = args[3];
        boolean debug = Boolean.valueOf(args[4]).booleanValue();
        String msgText1 = "Sending a file.\n";
        String subject = "Sending a file";
        Properties props = System.getProperties();
        props.put("mail.smtp.host", host);
        Session session = Session.getInstance(props, null);
        session.setDebug(debug);
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            InternetAddress[] address = { new InternetAddress(to) };
            msg.setRecipients(Message.RecipientType.TO, address);
            msg.setSubject(subject);
            MimeBodyPart mbp1 = new MimeBodyPart();
            mbp1.setText(msgText1);
            MimeBodyPart mbp2 = new MimeBodyPart();
            mbp2.attachFile(filename);
            Multipart mp = new MimeMultipart();
            mp.addBodyPart(mbp1);
            mp.addBodyPart(mbp2);
            msg.setContent(mp);
            msg.setSentDate(new Date());
            Transport.send(msg);
        } catch (MessagingException mex) {
            mex.printStackTrace();
            Exception ex = null;
            if ((ex = mex.getNextException()) != null) {
                ex.printStackTrace();
            }
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }
}
