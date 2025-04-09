package net.sf.mustang.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import net.sf.mustang.Constant;

public class Mail {

    private static final String MIXED = "mixed";

    private static final String RELATED = "related";

    private static final String ALTERNATIVE = "alternative";

    private static final String CONTENT_ID = "Content-Id";

    private static final String MAIL_HOST = "mail.smtp.host";

    protected String host = Constant.EMPTY;

    protected String from = null;

    protected List<String> to = new ArrayList<String>();

    protected List<String> cc = new ArrayList<String>();

    protected List<String> bcc = new ArrayList<String>();

    protected List<String> attachs = new ArrayList<String>();

    protected List<String> resources = new ArrayList<String>();

    protected List<Body> bodies = new ArrayList<Body>();

    protected String subject = Constant.EMPTY;

    public void setHost(String host) {
        this.host = host;
    }

    public void setFrom(String email) {
        this.from = email;
    }

    public void addTo(String email) {
        this.to.add(email);
    }

    public void addTos(ArrayList<String> to) {
        this.to.addAll(to);
    }

    public void resetTo() {
        this.to = new ArrayList<String>();
    }

    public void addCc(String mail) {
        this.cc.add(mail);
    }

    public void addCcs(List<String> cc) {
        this.cc.addAll(cc);
    }

    public void resetCc() {
        this.cc = new ArrayList<String>();
    }

    public void addBcc(String mail) {
        this.bcc.add(mail);
    }

    public void addBccs(List<String> bcc) {
        this.bcc.addAll(bcc);
    }

    public void resetBcc() {
        this.bcc = new ArrayList<String>();
    }

    public void addAttach(String file) {
        this.attachs.add(file);
    }

    public void addAttachs(List<String> attachs) {
        this.attachs.addAll(attachs);
    }

    public void resetAttachs() {
        this.attachs = new ArrayList<String>();
    }

    public void addResource(String file) {
        this.resources.add(file);
    }

    public void addResources(List<String> resources) {
        this.resources.addAll(resources);
    }

    public void resetResources() {
        this.resources = new ArrayList<String>();
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void addBody(String content, String contentType) {
        bodies.add(new Body(content, contentType));
    }

    public void addBodies(List<Body> bodies) {
        this.bodies.addAll(bodies);
    }

    public void resetBodies() {
        bodies = new ArrayList<Body>();
    }

    public void send(boolean synchronous) throws Exception {
        if (synchronous) send(); else sendAsynchronous();
    }

    public void send() throws Exception {
        MimeMessage msg;
        java.util.Properties p = new java.util.Properties();
        p.put(MAIL_HOST, host);
        Session session = Session.getInstance(p, null);
        msg = new MimeMessage(session);
        if (from != null) msg.setFrom(new InternetAddress(from));
        msg.setSubject(subject);
        for (int i = 0; i < to.size(); i++) msg.addRecipient(Message.RecipientType.TO, new InternetAddress((String) to.get(i)));
        for (int i = 0; i < cc.size(); i++) msg.addRecipient(Message.RecipientType.CC, new InternetAddress((String) cc.get(i)));
        for (int i = 0; i < bcc.size(); i++) msg.addRecipient(Message.RecipientType.BCC, new InternetAddress((String) bcc.get(i)));
        MimeBodyPart mbp = null;
        Body body = null;
        if ((attachs.size() + resources.size() + bodies.size()) > 1) {
            Multipart mp = null;
            Multipart mpBodies = null;
            Multipart mpResources = null;
            String mpType = Constant.EMPTY;
            if (attachs.size() > 0) mpType = MIXED; else if (resources.size() > 0) mpType = RELATED; else if (bodies.size() > 1) mpType = ALTERNATIVE;
            mp = new MimeMultipart(mpType);
            if (resources.size() > 0 && !mpType.equals(RELATED)) mpResources = new MimeMultipart(RELATED);
            if (bodies.size() > 1 && !mpType.equals(ALTERNATIVE)) mpBodies = new MimeMultipart(ALTERNATIVE);
            for (int i = 0; i < bodies.size(); i++) {
                body = (Body) bodies.get(i);
                mbp = new MimeBodyPart();
                mbp.setContent(body.getContent(), body.getContentType());
                if (mpBodies != null) mpBodies.addBodyPart(mbp); else if (mpResources != null) mpResources.addBodyPart(mbp); else mp.addBodyPart(mbp);
            }
            if (mpBodies != null) {
                mbp = new MimeBodyPart();
                mbp.setContent(mpBodies);
                if (mpResources != null) mpResources.addBodyPart(mbp); else mp.addBodyPart(mbp);
            }
            FileDataSource fds = null;
            for (int i = 0; i < resources.size(); i++) {
                fds = new FileDataSource((String) resources.get(i));
                mbp = new MimeBodyPart();
                mbp.setDataHandler(new DataHandler(fds));
                mbp.setHeader(CONTENT_ID, "<" + fds.getName() + ">");
                if (mpResources != null) mpResources.addBodyPart(mbp); else mp.addBodyPart(mbp);
            }
            if (mpResources != null) {
                mbp = new MimeBodyPart();
                mbp.setContent(mpResources);
                mp.addBodyPart(mbp);
            }
            for (int i = 0; i < attachs.size(); i++) {
                fds = new FileDataSource((String) attachs.get(i));
                mbp = new MimeBodyPart();
                mbp.setDataHandler(new DataHandler(fds));
                mbp.setFileName(fds.getName());
                mp.addBodyPart(mbp);
            }
            msg.setContent(mp);
        } else if (bodies.size() == 1) {
            body = (Body) bodies.get(0);
            msg.setContent(body.getContent(), body.getContentType());
        }
        msg.setSentDate(new java.util.Date());
        Transport.send(msg);
    }

    public void sendAsynchronous() {
        Thread t = new MailerThread();
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    public class MailerThread extends Thread {

        public void run() {
            try {
                send();
            } catch (Exception e) {
            }
        }
    }

    public class Body {

        public Body(String content, String contentType) {
            this.content = content;
            this.contentType = contentType;
        }

        private String contentType;

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public static void main(String args[]) {
        Mail mail = new Mail();
        try {
            mail.setHost("smtp.fastwebnet.it");
            mail.setFrom("tonino.mendicino@gmail.com (Tonino Mendicino)");
            mail.setSubject("doggy style newsletter");
            mail.addTo("emanuele.fumagalli@babelnetworks.com (Emanuele Fumagalli)");
            mail.addTo("ernesto.guisado@babelnetworks.com (Ernesto Guisado)");
            mail.addTo("tonino.mendicino@gmail.com (Tonino Mendicino)");
            mail.addBody("<html><body>newsletter " + new Date() + "<br><img  src=\"cid:140_4.jpg\"></body></html>", "text/html; charset=iso-8859-1");
            mail.addResource("d:/arena/140_4.jpg");
            mail.send();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
