package com.release.utils;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;

public class MailClass {

    private String mailServer;

    public MailClass(String mailServer) {
        this.mailServer = mailServer;
    }

    public static void main(String[] args) {
        MailClass mail = new MailClass("PHX-MSG-02.xyz.com");
        String to[] = new String[2];
        to[0] = "xyz@cgi.com";
        to[1] = "xyz@cgi.com";
        try {
            mail.postMail(to, "Mail from java program", "Hi mail send", "xyz@cgi.com");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendLog(String msg) {
        mailServer = "PHX-MSG-02.xyz.com";
        String to[] = new String[2];
        to[0] = "xyz@cgi.com";
        to[1] = "xyz@cgi.com";
        try {
            postMail(to, "Mail from java program", msg, "xyz@cgi.com");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void postMail(String recipients[], String subject, String message, String from) throws MessagingException {
        System.out.println("Posting mail---->");
        boolean debug = false;
        Properties props = new Properties();
        props.put("mail.smtp.host", mailServer);
        Session session = Session.getDefaultInstance(props, null);
        session.setDebug(debug);
        Message msg = new MimeMessage(session);
        InternetAddress addressFrom = new InternetAddress(from);
        msg.setFrom(addressFrom);
        InternetAddress[] addressTo = new InternetAddress[recipients.length];
        for (int i = 0; i < recipients.length; i++) {
            addressTo[i] = new InternetAddress(recipients[i]);
        }
        msg.setRecipients(Message.RecipientType.TO, addressTo);
        msg.addHeader("MyHeaderName", "myHeaderValue");
        msg.setSubject(subject);
        msg.setContent(message, "text/plain");
        Transport.send(msg);
    }
}
