package org.rjam.alert.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import org.rjam.alert.Rule;
import org.rjam.gui.api.IAlert;
import org.rjam.gui.api.IReport;
import org.rjam.gui.beans.Row;
import org.rjam.report.xml.Transformer;
import org.rjam.xml.Token;

public class ActionEmail extends Action {

    public static final Object PROP_MAIL_SMTP_HOST = "mail.smtp.host";

    private String smtpHost;

    private List<String> recipients = new ArrayList<String>();

    private String sender;

    private String subject;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public ActionEmail() {
        super("Email", "Send an email to the list of users");
    }

    public void addRecipient(String rec) {
        if (rec != null && rec.length() > 0) {
            recipients.add(rec);
        }
    }

    public boolean removeRrecipient(String rec) {
        return recipients.remove(rec);
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    @Override
    public Token toXml() {
        Token ret = super.toXml();
        ret.addChild(new Token(Transformer.TOKEN_SENDER, getSender()));
        ret.addChild(new Token(Transformer.TOKEN_SMTP_HOST, getSmtpHost()));
        ret.addChild(new Token(Transformer.TOKEN_SUBJECT, getSubject()));
        List<String> rec = getRecipients();
        if (rec != null) {
            for (int idx = 0, sz = rec.size(); idx < sz; idx++) {
                ret.addChild(new Token(Transformer.TOKEN_RECIPIENT, rec.get(idx)));
            }
        }
        return ret;
    }

    @Override
    public void setValues(Token tok) {
        super.setValues(tok);
        setSender(Transformer.getValue(tok, Transformer.TOKEN_SENDER));
        setSmtpHost(Transformer.getValue(tok, Transformer.TOKEN_SMTP_HOST));
        setSubject(Transformer.getValue(tok, Transformer.TOKEN_SUBJECT));
        List<Token> rec = tok.getChildren(Transformer.TOKEN_RECIPIENT);
        if (rec != null) {
            for (int idx = 0, sz = rec.size(); idx < sz; idx++) {
                addRecipient(rec.get(idx).getValue());
            }
        }
    }

    @Override
    public JComponent getEditComponent() {
        return new EditActionEmail(this);
    }

    @Override
    public void execute(IAlert alert, IReport report, Rule rule, Row row) {
        String msg = (formatOutput(report, alert, rule, row));
        String subject = evalMacros(getSubject(), row);
        Date date = row.getDate();
        try {
            postMail(subject, msg, date);
        } catch (Throwable e) {
            alert.setEnabled(false);
            logError("Can't send email " + e, e);
            if (!Action.isHeadless()) {
                alert.setEnabled(false);
                JOptionPane.showMessageDialog(null, "Can't send email " + e + "\n" + alert.getName() + " alert disabled.", "Action Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void postMail(String smtpHost, String subject, String message, Date date) throws MessagingException {
        boolean debug = true;
        Properties props = new Properties();
        props.put(PROP_MAIL_SMTP_HOST, smtpHost);
        Session session = Session.getInstance(props);
        session.setDebug(debug);
        Message msg = new MimeMessage(session);
        InternetAddress addressFrom = new InternetAddress(getSender());
        msg.setFrom(addressFrom);
        if (date == null) {
            date = new Date();
        }
        msg.setSentDate(date);
        InternetAddress[] addressTo = new InternetAddress[recipients.size()];
        for (int idx = 0; idx < addressTo.length; idx++) {
            addressTo[idx] = new InternetAddress(recipients.get(idx));
        }
        msg.setRecipients(Message.RecipientType.TO, addressTo);
        msg.setSubject(subject);
        msg.setContent(message, "text/plain");
        Transport.send(msg);
    }

    public void postMail(String subject, String message, Date date) throws Throwable {
        String[] hosts = getSmtpHost().split("[,]");
        Throwable[] error = new Throwable[hosts.length];
        boolean done = false;
        for (int idx = 0; !done && idx < hosts.length; idx++) {
            try {
                postMail(hosts[idx], subject, message, date);
                done = true;
            } catch (Throwable e) {
                error[idx] = e;
            }
        }
        if (!done) {
            if (isDebugEnabled()) {
                StringBuilder buf = new StringBuilder("Could not send the message to any host\n");
                for (int idx = 0; idx < error.length; idx++) {
                    buf.append(hosts[idx] + " error=" + error[idx]);
                }
                logDebug(buf.toString());
            }
            throw error[0];
        }
    }
}
