package org.yarik.mail;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.java.jpa.EMF;
import org.java.jpa.TwStatus;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import com.google.apphosting.api.ApiProxy.OverQuotaException;

@SuppressWarnings("serial")
public class GmailServlet extends HttpServlet {

    EntityManager em;

    @Override
    public void init() throws ServletException {
        super.init();
        em = EMF.get().createEntityManager();
    }

    static final String MyMail = "twitter@yarikx.org.ua";

    public static void sendSMS(String subject, String message) {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        String msgBody = message;
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("yarikx@yarikx.org.ua"));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(MyMail));
            msg.setSubject(subject);
            msg.setText(msgBody);
            Address[] ads = { new InternetAddress("yarikx@yarikx.org.ua") };
            msg.setReplyTo(ads);
            Transport.send(msg);
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings({ "unchecked", "deprecation" })
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Date d = new Date();
        if (d.getHours() >= 10) {
            Twitter tw = new Twitter("Yarikx", "saxophon");
            try {
                List<Status> l = tw.getFriendsTimeline();
                PrintWriter pw = response.getWriter();
                for (Status status : l) {
                    TwStatus ts = new TwStatus(status);
                    List<TwStatus> l1 = em.createQuery("select from TwStatus where twId=" + ts.getId().longValue()).getResultList();
                    if (l1.isEmpty()) {
                        System.out.println(status.getText());
                        EntityTransaction tx = em.getTransaction();
                        tx.begin();
                        TwStatus q;
                        em.persist(q = new TwStatus(status));
                        pw.println(q.getText());
                        try {
                            sendSMS(ts.getName(), ts.getText());
                            tx.commit();
                        } catch (OverQuotaException e) {
                            tx.rollback();
                            pw.println("Out of Quota");
                        }
                    }
                }
                pw.close();
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        }
    }
}
