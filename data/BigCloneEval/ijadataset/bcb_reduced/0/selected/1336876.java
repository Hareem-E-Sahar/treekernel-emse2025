package com.arcucomp.xmlcommand;

import com.arcucomp.xmgel.XMgeLEnvironment;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

public class XCEmailCommand_i extends XCCommand_i {

    int emailInit_Mode_failure = 1000;

    int emailAfterInit_Mode_failure = 2000;

    int emailMailForm_Mode_failure = 3000;

    int emailMailFormStart_Task = 100;

    int emailMailFormBeforeSend_Task = 200;

    int emailAfterMailForm_Mode_failure = 4000;

    public XCEmailCommand_i(XMgeLEnvironment cm) {
        super(cm);
        (cm.logger()).println(5, "XCEmailCommand(XCCustomCommandManager cm)<");
        this.cm = cm;
        (cm.logger()).println(5, "XCEmailCommand()>");
    }

    public void finalize() {
        (cm.logger()).println(5, "XCEmailCommand::finalize ends ");
    }

    public int processCommand() {
        (cm.logger()).println(1, "XCEmailCommand::processCommand< 03/19/2002 1:31pm");
        status = 0;
        errorDescription = "";
        m_outgoingResultsBuffer = "";
        m_outgoingXMLBuffer = "";
        String replyTo = "";
        String message = "";
        String mailHost = "";
        String emailRecipient = "";
        String subject = "";
        pending_failure_start = 70000;
        pending_failure = pending_failure_start + emailInit_Mode_failure;
        try {
            m_action = getSubCommand("Action", "XCEmailCommand::processCommand-");
            if (m_action.equalsIgnoreCase("SkipCommand")) {
                return status;
            }
            mailHost = getSubCommand("MailHost", "XCEmailCommand::processCommand-");
            (cm.logger()).println(1, "XCEmailCommand::processCommand- emailRecipient" + emailRecipient);
            if (mailHost == null) {
                mailHost = "";
            }
            if (mailHost.equals("")) {
                mailHost = (cm.getXMLPlayground_i()).getVariable("MailHost");
                if (mailHost == null) {
                    status = XC_BAD_COMMAND_FORMAT;
                    return status;
                }
            }
            emailRecipient = getSubCommand("EmailRecipient", "XCEmailCommand::processCommand-");
            (cm.logger()).println(1, "XCEmailCommand::processCommand- emailRecipient" + emailRecipient);
            if (emailRecipient == null) {
                emailRecipient = "";
            }
            if (!emailRecipient.equals("")) {
                String firstChar = emailRecipient.substring(0, 1);
                if (firstChar.equals("^")) {
                    String valHash = emailRecipient.substring(1, emailRecipient.length());
                    emailRecipient = (cm.getXMLPlayground_i()).getVariable(valHash);
                }
                if (emailRecipient.length() >= 3) {
                    String firstTwoChars = emailRecipient.substring(0, 2);
                    if (firstTwoChars.equals("\\^")) {
                        emailRecipient = emailRecipient.substring(1, emailRecipient.length());
                    }
                }
            } else {
                emailRecipient = (cm.getXMLPlayground_i()).getVariable("EmailRecipient");
                (cm.logger()).println(1, "XCEmailCommand::processCommand- getVariable emailRecipient" + emailRecipient);
                if (emailRecipient == null) {
                    status = XC_BAD_COMMAND_FORMAT;
                    return status;
                }
            }
            replyTo = getSubCommand("ReplyTo", "XCEmailCommand::processCommand-");
            if (replyTo == null) {
                replyTo = "";
            }
            if (replyTo.equals("")) {
                replyTo = (cm.getXMLPlayground_i()).getVariable("ReplyTo");
                if (replyTo == null) {
                    replyTo = "noreply@noreply.com";
                }
            }
            subject = getSubCommand("Subject", "XCEmailCommand::processCommand-");
            if (subject == null) {
                subject = "";
            }
            if (subject.equals("")) {
                subject = (cm.getXMLPlayground_i()).getVariable("Subject");
                if (subject == null) {
                    subject = "";
                }
            }
            message = getSubCommand("Message", "XCEmailCommand::processCommand-");
            if (message == null) {
                message = "";
            }
            if (message.equals("")) {
                message = (cm.getXMLPlayground_i()).getVariable("Message");
                if (message == null) {
                    message = "";
                }
            }
            pending_failure = pending_failure_start + emailAfterInit_Mode_failure;
            (cm.logger()).println(5, "XCEmailCommand::processCommand- start");
            mailForm(mailHost, replyTo, subject, message, emailRecipient);
            (cm.logger()).println(1, "XCEmailCommand::processCommand-done");
            if (status == 0) {
                pending_failure = pending_failure_start + emailAfterMailForm_Mode_failure;
            }
        } catch (Exception xmlEx) {
            (cm.logger()).println(1, "XCEmailCommand::processCommand failed with Exception" + xmlEx.getMessage());
            errorDescription = xmlEx.getMessage();
            status = pending_failure;
        }
        super.setOutGoingXMLBuffer();
        super.setOutGoingResultsBuffer();
        exitCommand("XCEmailCommand", status, errorDescription);
        (cm.logger()).println(1, "XCEmailCommand::processCommand>");
        return status;
    }

    void mailForm(String mailHost, String senderAddress, String subject, String message, String emailRecipient) {
        if (status == 0) {
            Properties props = System.getProperties();
            props.put("mail.smtp.host", mailHost);
            Session emailsession = Session.getDefaultInstance(props, null);
            pending_failure = pending_failure_start + emailMailForm_Mode_failure;
            pending_failure += emailMailFormStart_Task;
            try {
                Message email = new MimeMessage(emailsession);
                email.setFrom(new InternetAddress(senderAddress));
                InternetAddress[] address = { new InternetAddress(emailRecipient) };
                email.setRecipients(Message.RecipientType.TO, address);
                email.setSubject(subject);
                email.setSentDate(new Date());
                email.setHeader("X-Mailer", "MailFormJava");
                email.setText(message);
                pending_failure = pending_failure_start + emailMailForm_Mode_failure;
                pending_failure += emailMailFormBeforeSend_Task;
                Transport.send(email);
            } catch (MessagingException e) {
                (cm.logger()).println(1, "XCEmailCommand::mailForm  Exception: " + e.getMessage());
                (cm.logger()).println(1, "XCEmailCommand::mailForm  Mail may not have been sent to " + emailRecipient);
                errorDescription = "e.getMessage() Mail may not have been sent to " + emailRecipient;
                status = pending_failure;
            }
        }
    }
}
