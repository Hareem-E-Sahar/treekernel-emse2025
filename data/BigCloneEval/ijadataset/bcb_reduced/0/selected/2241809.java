package org.smslib.smsserver.interfaces;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.smslib.InboundMessage;
import org.smslib.MessageEncodings;
import org.smslib.OutboundMessage;
import org.smslib.smsserver.AInterface;
import org.smslib.smsserver.InterfaceTypes;
import org.smslib.smsserver.SMSServer;

/**
 * Interface for Email communication with SMSServer. <br />
 * Inbound messages are send via SMTP. Outbound messages are received via POP3.
 * 
 * @author Sebastian Just
 */
public class Email extends AInterface {

    /**
	 * Buffer around StringBuilder for some convinience methods.
	 * 
	 * @author Sebastian Just
	 */
    private class ExtStringBuilder {

        final StringBuilder sb;

        public ExtStringBuilder() {
            sb = new StringBuilder();
        }

        public ExtStringBuilder(StringBuilder sb) {
            this.sb = sb;
        }

        public void replaceAll(String search, int replace) {
            replaceAll(search, String.valueOf(replace));
        }

        public void replaceAll(String search, String replace) {
            int length = search.length();
            int start = sb.indexOf(search);
            while (start != -1) {
                sb.replace(start, start + length, replace);
                start = sb.indexOf(search);
            }
        }

        public String toString() {
            return sb.toString();
        }
    }

    private Session mailSession;

    private String messageSubject;

    private String messageBody;

    public Email(String infId, Properties props, SMSServer server, InterfaceTypes type) {
        super(infId, props, server, type);
        description = "Interface for Email communication.";
    }

    public void CallReceived(String gtwId, String callerId) {
    }

    public void MessagesReceived(List msgList) throws Exception {
        for (int i = 0; i < msgList.size(); i++) {
            InboundMessage im = (InboundMessage) msgList.get(i);
            Message msg = new MimeMessage(mailSession);
            msg.setFrom();
            msg.addRecipient(RecipientType.TO, new InternetAddress(getProperty("to")));
            msg.setSubject(updateTemplateString(messageSubject, im));
            if (messageBody != null) {
                msg.setText(updateTemplateString(messageBody, im));
            } else {
                msg.setText(im.toString());
            }
            msg.setSentDate(im.getDate());
            Transport.send(msg);
        }
    }

    public List getMessagesToSend() throws Exception {
        List retValue = new ArrayList();
        Store s = mailSession.getStore();
        s.connect();
        Folder inbox = s.getFolder(getProperty("mailbox_name", "INBOX"));
        inbox.open(Folder.READ_WRITE);
        Message[] m = inbox.getMessages();
        for (int i = 0; i < m.length; i++) {
            OutboundMessage om = new OutboundMessage(m[i].getSubject(), m[i].getContent().toString());
            om.setFrom(m[i].getFrom().toString());
            om.setDate(m[i].getReceivedDate());
            retValue.add(om);
            m[i].setFlag(Flags.Flag.DELETED, true);
        }
        inbox.close(true);
        s.close();
        return retValue;
    }

    public void markMessage(OutboundMessage msg) throws Exception {
        logWarn("NOOP!");
    }

    public void start() throws Exception {
        Properties mailProps = new Properties();
        mailProps.setProperty("mail.store.protocol", getProperty("mailbox_protocol"));
        if ("pop3".equals(getProperty("mailbox_protocol"))) {
            mailProps.setProperty("mail.pop3.host", getProperty("mailbox_host"));
            mailProps.setProperty("mail.pop3.port", getProperty("mailbox_port"));
            mailProps.setProperty("mail.pop3.user", getProperty("mailbox_user"));
            mailProps.setProperty("mail.pop3.password", getProperty("mailbox_password"));
        } else if ("pop3s".equals(getProperty("mailbox_protocol"))) {
            mailProps.setProperty("mail.pop3s.host", getProperty("mailbox_host"));
            mailProps.setProperty("mail.pop3s.port", getProperty("mailbox_port"));
            mailProps.setProperty("mail.pop3s.user", getProperty("mailbox_user"));
            mailProps.setProperty("mail.pop3s.password", getProperty("mailbox_password"));
        } else if ("imap".equals(getProperty("mailbox_protocol"))) {
            mailProps.setProperty("mail.imap.host", getProperty("mailbox_host"));
            mailProps.setProperty("mail.imap.port", getProperty("mailbox_port"));
            mailProps.setProperty("mail.imap.user", getProperty("mailbox_user"));
            mailProps.setProperty("mail.imap.password", getProperty("mailbox_password"));
        } else if ("imaps".equals(getProperty("mailbox_protocol"))) {
            mailProps.setProperty("mail.imaps.host", getProperty("mailbox_host"));
            mailProps.setProperty("mail.imaps.port", getProperty("mailbox_port"));
            mailProps.setProperty("mail.imaps.user", getProperty("mailbox_user"));
            mailProps.setProperty("mail.imaps.password", getProperty("mailbox_password"));
        } else {
            throw new IllegalArgumentException("mailbox_protocol have to be pop3(s) or imap(s)!");
        }
        mailProps.setProperty("mail.transport.protocol", "smtp");
        mailProps.setProperty("mail.from", getProperty("from"));
        mailProps.setProperty("mail.smtp.host", getProperty("smtp_host"));
        mailProps.setProperty("mail.smtp.port", getProperty("smtp_port"));
        mailProps.setProperty("mail.smtp.user", getProperty("smtp_user"));
        mailProps.setProperty("mail.smtp.password", getProperty("smtp_password"));
        mailSession = Session.getInstance(mailProps, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(getProperty("mailbox_user"), getProperty("mailbox_password"));
            }
        });
        if (isOutbound()) {
            prepareEmailTemplate();
        }
    }

    private String updateTemplateString(String template, InboundMessage msg) {
        ExtStringBuilder sb = new ExtStringBuilder();
        sb.replaceAll("%gatewayId%", msg.getGatewayId());
        sb.replaceAll("%encoding%", (msg.getEncoding() == MessageEncodings.ENC7BIT ? "7-bit" : (msg.getEncoding() == MessageEncodings.ENC8BIT ? "8-bit" : "UCS2 (Unicode)")));
        sb.replaceAll("%date%", msg.getDate().toString());
        sb.replaceAll("%text%", msg.getText());
        sb.replaceAll("%pduUserData%", msg.getPduUserData());
        sb.replaceAll("%originator%", msg.getOriginator());
        sb.replaceAll("%memIndex%", msg.getMemIndex());
        sb.replaceAll("%mpMemIndex%", msg.getMpMemIndex());
        return sb.toString();
    }

    private void prepareEmailTemplate() {
        messageSubject = getProperty("message_subject");
        if (messageSubject == null || messageSubject.length() == 0) {
            logWarn("No message_subject found - Using default");
            messageSubject = "SMS from %ORIGINATOR%";
        }
        File f = new File(getProperty("message_body"));
        if (f.canRead()) {
            try {
                Reader r = new FileReader(f);
                BufferedReader br = new BufferedReader(r);
                String line = null;
                StringBuilder sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                messageBody = sb.toString();
            } catch (IOException e) {
                logError("I/O-Exception while reading message body template: " + e.getMessage());
            }
        }
        if (messageBody == null || messageBody.length() == 0) {
            logWarn("message_body can't be read or is empty - Using default");
            messageBody = null;
        }
    }

    public void stop() throws Exception {
    }
}
