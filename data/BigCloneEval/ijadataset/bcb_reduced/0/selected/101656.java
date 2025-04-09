package com.litt.core.mail;

import java.security.Security;
import java.util.Date;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * 用Gmail发送邮件，适用于需要SSL安全套接字验证的邮件服务器
 * @author <a href="mailto:littcai@hotmail.com">空心大白菜</a>
 * @date 2006-09-14
 * @version 1.0
 *
 */
public class Gmail {

    private String toEmail2 = "";

    private String toEmail = "";

    private String fromEmail = "";

    private String fromName = "";

    private String subject = "";

    private String content = "";

    private String smtpServer = "";

    private String smtpID = "";

    private String smtpPass = "";

    private boolean needAuth = false;

    private String message = "";

    private int needSSL = 0;

    private String smtpPort = "";

    public void setToEmail2(String toEmail2) {
        this.toEmail2 = toEmail2;
    }

    public String getMessage() {
        return this.message;
    }

    public void setSmtpPort(String s) {
        this.smtpPort = s;
    }

    public void setNeedSSL(int i) {
        this.needSSL = i;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public void setSmtpServer(String smtpServer) {
        this.smtpServer = smtpServer;
    }

    public void setSmtpID(String smtpID) {
        this.smtpID = smtpID;
    }

    public void setSmtpPass(String smtpPass) {
        this.smtpPass = smtpPass;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setNeedAuth(String needAuth) {
        try {
            this.needAuth = Boolean.getBoolean(needAuth);
        } catch (Exception e) {
            this.needAuth = true;
        }
    }

    public boolean mailSender() {
        boolean bea = false;
        Properties props = System.getProperties();
        props.setProperty("mail.smtp.host", smtpServer);
        if (needSSL == 1) {
            Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
            final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
            props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
            props.setProperty("mail.smtp.socketFactory.fallback", "false");
        }
        if (!smtpPort.trim().equals("")) {
            props.setProperty("mail.smtp.port", smtpPort);
            props.setProperty("mail.smtp.socketFactory.port", smtpPort);
        }
        if (needAuth) {
            props.setProperty("mail.smtp.auth", "true");
        } else {
            props.setProperty("mail.smtp.auth", "false");
        }
        Session session = Session.getDefaultInstance(props, new Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpID, smtpPass);
            }
        });
        try {
            MimeMessage msg = new MimeMessage(session);
            InternetAddress address = new InternetAddress(fromEmail);
            msg.setFrom(address);
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
            if (toEmail2 != null && !toEmail2.equals("")) {
                msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(toEmail2, false));
            }
            msg.setSubject(subject);
            msg.setText(content);
            msg.setSentDate(new Date());
            Transport.send(msg);
            message = "Email发送成功......";
            bea = true;
        } catch (MessagingException e) {
            message = e.toString();
            bea = false;
        }
        return bea;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }
}
