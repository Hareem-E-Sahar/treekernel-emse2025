package com.shenming.sms.util;

import java.util.Date;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailUtil {

    /**
	 * @param args
	 * @throws Exception 
	 */
    public static void main(String[] args) throws Exception {
        System.out.println("Send mail start!");
        MailUtil mu = new MailUtil();
        mu.send("<font color=\"red\">Hello!!</font><br/>" + "http://oaps2.ncic.com.tw:8080/oaps");
        System.out.println("Send mail finish!");
    }

    public void send(String msgBody) throws Exception {
        String to = "simonsu@ncic.com.tw";
        String subject = "JMail Test!";
        String from = "simonsu@ncic.com.tw";
        String mailhost = "tphqms3.ncic.corp";
        String mailer = "JMail Mailler";
        Properties props = new Properties();
        props = System.getProperties();
        props.put("mail.smtp.host", mailhost);
        Session session = Session.getDefaultInstance(props, null);
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
        msg.setSubject(subject);
        msg.setContent(msgBody, "text/html");
        msg.setSentDate(new Date());
        Transport.send(msg);
    }
}
