package com.lzy.jmail;

import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;

public class SimpleMailSender {

    static Logger logger = Logger.getLogger(SimpleMailSender.class.getName());

    /**
	 * 以文本格式发送邮件
	 * 
	 * @param mailInfo
	 *            待发送的邮件的信息
	 * @throws Exception
	 */
    public boolean sendTextMail(MailSenderInfo mailInfo, List fileList) throws Exception {
        MyAuthenticator authenticator = null;
        Properties pro = mailInfo.getProperties();
        if (mailInfo.isValidate()) {
            authenticator = new MyAuthenticator(mailInfo.getUserName(), mailInfo.getPassword());
        }
        Session sendMailSession = Session.getDefaultInstance(pro, authenticator);
        try {
            javax.mail.Message mailMessage = new MimeMessage(sendMailSession);
            Address from = new InternetAddress(mailInfo.getFromAddress());
            mailMessage.setFrom(from);
            Address to = new InternetAddress(mailInfo.getToAddress());
            mailMessage.setRecipient(javax.mail.Message.RecipientType.TO, to);
            mailMessage.setSubject(mailInfo.getSubject());
            mailMessage.setSentDate(new java.util.Date());
            String mailContent = mailInfo.getContent();
            logger.debug("mailContent is: " + mailContent);
            mailMessage.setText(mailContent);
            Multipart mainPart = new MimeMultipart();
            try {
                if (fileList.size() > 0) {
                    FileItem fileItem = (FileItem) fileList.get(0);
                    MimeBodyPart attachPart1 = createAttachment(fileItem);
                    mainPart.addBodyPart(attachPart1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mailMessage.setContent(mainPart);
            Transport.send(mailMessage);
            return true;
        } catch (MessagingException ex) {
            ex.printStackTrace();
            throw new Exception(ex);
        }
    }

    /**
	 * 以HTML格式发送邮件 待发送的邮件信息
	 */
    public static boolean sendHtmlMail(MailSenderInfo mailInfo, List fileList) {
        MyAuthenticator authenticator = null;
        Properties pro = mailInfo.getProperties();
        if (mailInfo.isValidate()) {
            authenticator = new MyAuthenticator(mailInfo.getUserName(), mailInfo.getPassword());
        }
        Session sendMailSession = Session.getDefaultInstance(pro, authenticator);
        try {
            javax.mail.Message mailMessage = new MimeMessage(sendMailSession);
            Address from = new InternetAddress(mailInfo.getFromAddress());
            mailMessage.setFrom(from);
            String toAddress = mailInfo.getToAddress();
            toAddress = decorativeToAddress(toAddress);
            InternetAddress[] toList = new InternetAddress().parse(toAddress);
            mailMessage.setRecipients(javax.mail.Message.RecipientType.TO, toList);
            mailMessage.setSubject(mailInfo.getSubject());
            mailMessage.setSentDate(new java.util.Date());
            Multipart mainPart = new MimeMultipart();
            BodyPart html = new MimeBodyPart();
            try {
                if (fileList.size() > 0) {
                    FileItem fileItem = (FileItem) fileList.get(0);
                    MimeBodyPart attachPart1 = createAttachment(fileItem);
                    mainPart.addBodyPart(attachPart1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            html.setContent(mainPart);
            html.setText(mailInfo.getContent());
            mainPart.addBodyPart(html);
            mailMessage.setContent(mainPart);
            Transport.send(mailMessage);
            return true;
        } catch (MessagingException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static String decorativeToAddress(String toAddress) {
        String mailAddress = toAddress.replaceAll("，", ",");
        mailAddress = mailAddress.replaceAll(";", ",");
        mailAddress = mailAddress.replaceAll("；", ",");
        logger.debug("mailAddress　is: " + mailAddress);
        return mailAddress;
    }

    public static MimeBodyPart createAttachment(FileItem fileItem) throws Exception {
        MimeBodyPart attachPart = new MimeBodyPart();
        DataSource fds = new UploadFileDataSource(fileItem);
        attachPart.setDataHandler(new DataHandler(fds));
        String attachmentFullName = fds.getName();
        logger.debug("attachmentFullName is: " + attachmentFullName);
        String attachName = getAttachName(attachmentFullName);
        logger.debug("attachName is: " + attachName);
        attachPart.setFileName(MimeUtility.encodeText(attachName));
        return attachPart;
    }

    private static String getAttachName(String attachmentFullName) {
        int indexOfDoubleSlash = attachmentFullName.lastIndexOf("\\");
        String attachName = null;
        attachName = attachmentFullName.substring(indexOfDoubleSlash + 1);
        return attachName;
    }
}
