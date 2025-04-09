package com.ctocafe.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.util.ParserException;

/**
 * 作者: baibiao
 * 日期: 2007-3-8 13:46:51
 */
public abstract class MailSender extends Authenticator {

    private String username = null;

    private String userpasswd = null;

    protected BodyPart messageBodyPart = null;

    protected Multipart multipart = new MimeMultipart("related");

    protected MimeMessage mailMessage = null;

    protected Session mailSession = null;

    protected InternetAddress mailToAddress = null;

    protected MailSender() {
    }

    /**
     * 构造函数
     *
     * @param smtpHost 邮件服务器地址
     * @param username 用户名
     * @param password 密码
     */
    protected MailSender(String smtpHost, String username, String password) {
        this(smtpHost, 465, username, password);
    }

    /**
     * 构造函数
     *
     * @param smtpHost
     * @param smtpPort
     * @param username
     * @param password
     */
    @SuppressWarnings({ "JavaDoc" })
    protected MailSender(String smtpHost, int smtpPort, String username, String password) {
        this.username = username;
        this.userpasswd = password;
        Properties mailProperties = System.getProperties();
        mailProperties.put("mail.smtp.host", smtpHost);
        if (smtpPort > 0 && smtpPort != 25) mailProperties.put("mail.smtp.port", String.valueOf(smtpPort));
        mailProperties.put("mail.smtp.auth", "true");
        mailSession = Session.getDefaultInstance(mailProperties, this);
        mailMessage = new MimeMessage(mailSession);
        messageBodyPart = new MimeBodyPart();
    }

    /**
     * 构造一个纯文本邮件发送实例
     *
     * @param smtpHost
     * @param username
     * @param password
     * @return
     */
    @SuppressWarnings({ "JavaDoc" })
    public static MailSender getTextMailSender(String smtpHost, String username, String password) {
        return getTextMailSender(smtpHost, 465, username, password);
    }

    /**
     * 构造一个纯文本邮件发送实例
     *
     * @param smtpHost SMTP服务器地址
     * @param smtpPort SMTP服务器端口
     * @param username SMTP邮件发送帐号
     * @param password SMTP邮件发送帐号对应的密码
     * @return
     */
    @SuppressWarnings({ "JavaDoc" })
    public static MailSender getTextMailSender(String smtpHost, int smtpPort, String username, String password) {
        return new MailSender(smtpHost, smtpPort, username, password) {

            public void setMailContent(String mailContent) throws MessagingException {
                messageBodyPart.setText(mailContent);
                multipart.addBodyPart(messageBodyPart);
            }
        };
    }

    /**
     * 构造一个超文本邮件发送实例
     *
     * @param smtpHost
     * @param username
     * @param password
     * @return
     */
    @SuppressWarnings({ "JavaDoc" })
    public static MailSender getHtmlMailSender(String smtpHost, String username, String password) {
        return getHtmlMailSender(smtpHost, 25, username, password);
    }

    /**
     * 构造一个超文本邮件发送实例
     *
     * @param smtpHost SMTP服务器地址
     * @param smtpPort SMTP服务器端口
     * @param username SMTP邮件发送帐号
     * @param password SMTP邮件发送帐号对应的密码
     * @return
     */
    @SuppressWarnings({ "JavaDoc" })
    public static MailSender getHtmlMailSender(String smtpHost, int smtpPort, String username, String password) {
        return new MailSender(smtpHost, smtpPort, username, password) {

            private ArrayList arrayList1 = new ArrayList();

            private ArrayList arrayList2 = new ArrayList();

            private String mailContent;

            public void setMailContent(String mailContent) throws MessagingException {
                String htmlContent = getContent(mailContent);
                messageBodyPart.setContent(htmlContent, CONTENT_TYPE);
                multipart.addBodyPart(messageBodyPart);
                processHtmlImage(mailContent);
            }

            private void processHtmlImage(String mailContent) throws MessagingException {
                this.mailContent = mailContent;
                for (int i = 0; i < arrayList1.size(); i++) {
                    messageBodyPart = new MimeBodyPart();
                    DataSource source = new FileDataSource((String) arrayList1.get(i));
                    messageBodyPart.setDataHandler(new DataHandler(source));
                    String contentId = new StringBuilder().append("<").append((String) arrayList2.get(i)).append(">").toString();
                    messageBodyPart.setHeader("Content-ID", contentId);
                    messageBodyPart.setFileName((String) arrayList1.get(i));
                    multipart.addBodyPart(messageBodyPart);
                }
            }

            @SuppressWarnings({ "unchecked", "EmptyCatchBlock" })
            private String getContent(String mailContent) {
                try {
                    Parser parser = Parser.createParser(new String(mailContent.getBytes(), ISO8859_1));
                    Node[] images = parser.extractAllNodesThatAre(ImageTag.class);
                    for (Node image : images) {
                        ImageTag imgTag = (ImageTag) image;
                        if (!imgTag.getImageURL().toLowerCase().startsWith("http://")) arrayList1.add(imgTag.getImageURL());
                    }
                } catch (UnsupportedEncodingException e1) {
                } catch (ParserException e) {
                }
                String afterReplaceStr = mailContent;
                for (int m = 0; m < arrayList1.size(); m++) {
                    arrayList2.add(createRandomStr());
                    String addString = new StringBuilder().append("cid:").append((String) arrayList2.get(m)).toString();
                    afterReplaceStr = mailContent.replaceAll((String) arrayList1.get(m), addString);
                }
                return afterReplaceStr;
            }

            private String createRandomStr() {
                char[] randomChar = new char[8];
                for (int i = 0; i < 8; i++) {
                    randomChar[i] = (char) (Math.random() * 26 + 'a');
                }
                String replaceStr;
                replaceStr = new String(randomChar);
                return replaceStr;
            }

            private static final String CONTENT_TYPE = "text/html;charset=GB2312";

            private static final String ISO8859_1 = "8859_1";

            public String getMailContent() {
                return mailContent;
            }
        };
    }

    /**
     * 用于实现邮件发送用户验证
     *
     * @see javax.mail.Authenticator#getPasswordAuthentication
     */
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, userpasswd);
    }

    /**
     * 设置邮件标题
     *
     * @param mailSubject
     * @throws MessagingException
     */
    @SuppressWarnings({ "JavaDoc" })
    public void setSubject(String mailSubject) throws MessagingException {
        mailMessage.setSubject(mailSubject);
    }

    /**
     * 所有子类都需要实现的抽象方法，为了支持不同的邮件类型
     *
     * @param mailContent
     * @throws MessagingException
     */
    @SuppressWarnings({ "JavaDoc" })
    public abstract void setMailContent(String mailContent) throws MessagingException;

    /**
     * 设置邮件发送日期
     *
     * @param sendDate
     * @throws MessagingException
     */
    @SuppressWarnings({ "JavaDoc" })
    public void setSendDate(Date sendDate) throws MessagingException {
        mailMessage.setSentDate(sendDate);
    }

    /**
     * 设置邮件发送附件
     *
     * @param attachmentName
     * @throws MessagingException
     */
    @SuppressWarnings({ "JavaDoc" })
    public void setAttachments(String attachmentName) throws MessagingException {
        messageBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(attachmentName);
        messageBodyPart.setDataHandler(new DataHandler(source));
        int index = attachmentName.lastIndexOf(File.separator);
        String attachmentRealName = attachmentName.substring(index + 1);
        messageBodyPart.setFileName(attachmentRealName);
        multipart.addBodyPart(messageBodyPart);
    }

    /**
     * 设置发件人地址
     *
     * @param mailFrom
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */
    @SuppressWarnings({ "JavaDoc" })
    public void setMailFrom(String mailFrom, String sender) throws UnsupportedEncodingException, MessagingException {
        if (sender != null) mailMessage.setFrom(new InternetAddress(mailFrom, sender)); else mailMessage.setFrom(new InternetAddress(mailFrom));
    }

    /**
     * 设置收件人地址，收件人类型为to,cc,bcc(大小写不限)
     *
     * @param mailTo   邮件接收者地址
     * @param mailType 值为to,cc,bcc
     * @author Liudong
     * @throws Exception
     */
    @SuppressWarnings({ "JavaDoc" })
    public void setMailTo(String[] mailTo, String mailType) throws Exception {
        for (String aMailTo : mailTo) {
            mailToAddress = new InternetAddress(aMailTo);
            if (mailType.equalsIgnoreCase("to")) {
                mailMessage.addRecipient(Message.RecipientType.TO, mailToAddress);
            } else if (mailType.equalsIgnoreCase("cc")) {
                mailMessage.addRecipient(Message.RecipientType.CC, mailToAddress);
            } else if (mailType.equalsIgnoreCase("bcc")) {
                mailMessage.addRecipient(Message.RecipientType.BCC, mailToAddress);
            } else {
                throw new Exception("Unknown mailType: " + mailType + "!");
            }
        }
    }

    /**
     * 开始发送邮件
     *
     * @throws MessagingException
     * @throws SendFailedException
     */
    @SuppressWarnings({ "JavaDoc" })
    public void sendMail() throws MessagingException {
        if (mailToAddress == null) throw new MessagingException("请你必须你填写收件人地址！");
        mailMessage.setContent(multipart);
        Transport.send(mailMessage);
    }

    /**
     * 邮件发送测试
     *
     * @param args
     */
    @SuppressWarnings({ "JavaDoc" })
    public static void main(String args[]) {
        String mailHost = "mail.diglover.com";
        String mailUser = "service";
        String mailPassword = "diglover";
        String[] toAddress = { "baibiao@gmail.com" };
        MailSender sendmail = MailSender.getHtmlMailSender(mailHost, mailUser, mailPassword);
        try {
            sendmail.setSubject("邮件发送测试");
            sendmail.setSendDate(new Date());
            String content = "<H1>你好,中国hhhhhhhhhhhh</H1><br/><a href=\"www.diglover.com\">www.diglover.com</a>";
            sendmail.setMailContent(content);
            sendmail.setMailFrom("service@diglover.com", "service");
            sendmail.setMailTo(toAddress, "to");
            System.out.println("正在发送邮件，请稍候.......");
            sendmail.sendMail();
            System.out.println("恭喜你，邮件已经成功发送!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
