package net.sourceforge.pebble.util;

import net.sourceforge.pebble.domain.Blog;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import net.sourceforge.pebble.web.validation.ValidationContext;
import net.sourceforge.pebble.PebbleContext;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utilities for e-mail related functions.
 *
 * @author    Simon Brown
 */
public class MailUtils {

    /** the log used by this class */
    private static Log log = LogFactory.getLog(MailUtils.class);

    /** thread pool used to send e-mail */
    private static ExecutorService pool = Executors.newFixedThreadPool(1);

    /**
   * Sends an e-mail.
   *
   * @param blog    the notifying blog
   * @param to     the e-mail addresses of the recipients in the TO field
   * @param subject       the subject of the e-mail
   * @param message       the body of the e-mail
   */
    public static void sendMail(Session session, Blog blog, String to, String subject, String message) {
        Collection set = new HashSet();
        set.add(to);
        sendMail(session, blog, set, new HashSet(), new HashSet(), subject, message);
    }

    /**
   * Sends an e-mail.
   *
   * @param blog    the notifying blog
   * @param to     the e-mail addresses of the recipients in the TO field
   * @param subject       the subject of the e-mail
   * @param message       the body of the e-mail
   */
    public static void sendMail(Session session, Blog blog, Collection to, String subject, String message) {
        sendMail(session, blog, to, new HashSet(), new HashSet(), subject, message);
    }

    /**
   * Sends an e-mail.
   *
   * @param blog    the notifying blog
   * @param to     the e-mail addresses of the recipients in the TO field
   * @param cc     the e-mail addresses of the recipients in the CC field
   * @param subject       the subject of the e-mail
   * @param message       the body of the e-mail
   */
    public static void sendMail(Session session, Blog blog, Collection to, Collection cc, String subject, String message) {
        sendMail(session, blog, to, cc, new HashSet(), subject, message);
    }

    /**
   * Sends an e-mail.
   *
   * @param blog    the notifying blog
   * @param to     the e-mail addresses of the recipients in the TO field
   * @param cc     the e-mail addresses of the recipients in the CC field
   * @param bcc     the e-mail addresses of the recipients in the BCC field
   * @param subject       the subject of the e-mail
   * @param message       the body of the e-mail
   */
    public static void sendMail(Session session, Blog blog, Collection to, Collection cc, Collection bcc, String subject, String message) {
        Runnable r = new SendMailRunnable(session, blog, to, cc, bcc, subject, message);
        pool.execute(r);
    }

    /**
   * A thread allowing the e-mail to be sent asynchronously, so the requesting
   * thread (and therefore the user) isn't held up.
   */
    static class SendMailRunnable implements Runnable {

        /** the JavaMail session */
        private Session session;

        /** the notifying blog */
        private Blog blog;

        /** the e-mail addresses of the recipients in the TO field */
        private Collection to;

        /** the e-mail addresses of the recipients in the CC field */
        private Collection cc;

        /** the e-mail addresses of the recipients in the BCC field */
        private Collection bcc;

        /** the subject of the e-mail */
        private String subject;

        /** the body of the e-mail */
        private String message;

        /**
     * Creates a new thread to send a new e-mail.
     *
     * @param session   a JavaMail Session instance
     * @param blog    the notifying blog
     * @param to     the e-mail addresses of the recipients in the TO field
     * @param cc     the e-mail addresses of the recipients in the CC field
     * @param bcc     the e-mail addresses of the recipients in the BCC field
     * @param subject       the subject of the e-mail
     * @param message       the body of the e-mail
     */
        public SendMailRunnable(Session session, Blog blog, Collection to, Collection cc, Collection bcc, String subject, String message) {
            this.session = session;
            this.blog = blog;
            this.to = to;
            this.cc = cc;
            this.bcc = bcc;
            this.subject = subject;
            this.message = message;
        }

        /**
     * Performs the processing associated with this thread.
     */
        public void run() {
            try {
                Message msg = new MimeMessage(session);
                msg.setFrom(new InternetAddress(blog.getFirstEmailAddress(), blog.getName()));
                Collection internetAddresses = new HashSet();
                Iterator it = to.iterator();
                while (it.hasNext()) {
                    internetAddresses.add(new InternetAddress(it.next().toString()));
                }
                msg.addRecipients(Message.RecipientType.TO, (InternetAddress[]) internetAddresses.toArray(new InternetAddress[] {}));
                internetAddresses = new HashSet();
                it = cc.iterator();
                while (it.hasNext()) {
                    internetAddresses.add(new InternetAddress(it.next().toString()));
                }
                msg.addRecipients(Message.RecipientType.CC, (InternetAddress[]) internetAddresses.toArray(new InternetAddress[] {}));
                internetAddresses = new HashSet();
                it = bcc.iterator();
                while (it.hasNext()) {
                    internetAddresses.add(new InternetAddress(it.next().toString()));
                }
                msg.addRecipients(Message.RecipientType.BCC, (InternetAddress[]) internetAddresses.toArray(new InternetAddress[] {}));
                msg.setSubject(subject);
                msg.setSentDate(new Date());
                msg.setContent(message, "text/html");
                log.debug("From : " + blog.getName() + " (" + blog.getFirstEmailAddress() + ")");
                log.debug("Subject : " + subject);
                log.debug("Message : " + message);
                Transport.send(msg);
            } catch (Exception e) {
                log.error("Notification e-mail could not be sent", e);
            }
        }
    }

    /**
   * Creates a reference to a JavaMail Session.
   *
   * @return  a Session instance
   * @throws Exception    if something goes wrong creating a session
   */
    public static Session createSession() throws Exception {
        String ref = PebbleContext.getInstance().getConfiguration().getSmtpHost();
        if (ref.startsWith("java:comp/env")) {
            Context ctx = new InitialContext();
            return (Session) ctx.lookup(ref);
        } else {
            Properties props = new Properties();
            props.put("mail.smtp.host", ref);
            return Session.getDefaultInstance(props, null);
        }
    }

    /**
   * Validates the given comment.
   *
   * @param email   the Comment instance to validate
   * @param context   the context in which to perform validation
   */
    public static void validate(String email, ValidationContext context) {
        if (email != null) {
            try {
                InternetAddress ia = new InternetAddress(email, true);
                ia.validate();
            } catch (AddressException aex) {
                context.addError(aex.getMessage() + ": " + email);
            }
        }
    }
}
