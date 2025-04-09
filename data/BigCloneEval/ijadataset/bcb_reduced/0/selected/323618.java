package com.taobao.top.analysis.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 发送邮件的类
 * 
 * @author fangweng
 * 
 */
public class MailUtil {

    private static final transient Log log = LogFactory.getLog(MailUtil.class);

    private static final long MAIL_SMTP_CONNECTION_TIMEOUT = 10000;

    private static final long MAIL_SMTP_TIMEOUT = 10000;

    public MimeMessage mimeMsg;

    private Session session;

    private Properties props;

    private String username = "";

    private String password = "";

    private Multipart mp;

    public MailUtil(String stmp) {
        setSmtpHost(stmp);
        createMimeMessage();
    }

    /**
	 * 
	 * @param hostName
	 */
    public void setSmtpHost(String hostName) {
        log.debug("mail.stmp.host= " + hostName);
        if (props == null) {
            props = System.getProperties();
        }
        props.put("mail.smtp.host", hostName);
        props.put("mail.smtp.connectiontimeout", MAIL_SMTP_CONNECTION_TIMEOUT);
        props.put("mail.smtp.timeout", MAIL_SMTP_TIMEOUT);
        props.put("mail.smtp.starttls.enable", "true");
        props.setProperty("mail.smtp.port", "587");
    }

    public boolean createMimeMessage() {
        try {
            log.debug("Session begin-----------");
            session = Session.getInstance(props, null);
        } catch (Exception e) {
            log.debug("Session.getInstance faild!" + e);
            return false;
        }
        log.debug("MimeMEssage begin---------!");
        try {
            mimeMsg = new MimeMessage(session);
            mp = new MimeMultipart();
            return true;
        } catch (Exception e) {
            log.debug("MimeMessage fiald! " + e.toString());
            return false;
        }
    }

    /**
	 * 
	 * @param need
	 */
    public void setNeedAuth(boolean need) {
        log.debug(":mail.smtp.auth=" + need);
        if (props == null) {
            props = System.getProperties();
        }
        if (need) {
            props.put("mail.smtp.auth", "true");
        } else {
            props.put("mail.smtp.auth", "false");
        }
    }

    /**
	 * 
	 * @param name
	 * @param pass
	 */
    public void setNamePass(String name, String pass) {
        username = name;
        password = pass;
    }

    /**
	 * 
	 * @param mailSubject
	 * @return boolean
	 */
    public boolean setSubject(String mailSubject) {
        log.debug("set title begin.");
        try {
            if (!mailSubject.equals("") && mailSubject != null) {
                mimeMsg.setSubject(mailSubject);
            }
            return true;
        } catch (Exception e) {
            log.debug("set Title faild!");
            return false;
        }
    }

    /**
	 * 添加附件..
	 * 
	 * @param filename
	 * @return
	 */
    public boolean addFileAffix(String filename) {
        log.debug("增加附件..");
        if (filename.equals("") || filename == null) {
            return false;
        }
        String file[];
        file = filename.split(";");
        log.debug("你有 " + file.length + " 个附件!");
        try {
            for (int i = 0; i < file.length; i++) {
                String appendFileName = file[i];
                File f = new File(appendFileName);
                if (f.length() / 1024 > 1500) {
                    appendFileName = appendFileName + ".zip";
                    new File(appendFileName).createNewFile();
                    File zipFile = new File(appendFileName);
                    BufferedInputStream in = null;
                    ZipOutputStream zipOut = null;
                    try {
                        in = new BufferedInputStream(new FileInputStream(f));
                        zipOut = new ZipOutputStream(new BufferedOutputStream(new CheckedOutputStream(new FileOutputStream(zipFile), new CRC32()), 4096));
                        ZipEntry entry = new ZipEntry(f.getName());
                        zipOut.putNextEntry(entry);
                        byte[] b = new byte[4096];
                        int count = -1;
                        while ((count = in.read(b)) != -1) {
                            zipOut.write(b, 0, count);
                        }
                        zipOut.closeEntry();
                    } catch (Exception ex) {
                        log.error(ex);
                        continue;
                    } finally {
                        if (in != null) in.close();
                        if (zipOut != null) zipOut.close();
                    }
                }
                BodyPart bp = new MimeBodyPart();
                FileDataSource fileds = new FileDataSource(appendFileName);
                bp.setDataHandler(new DataHandler(fileds));
                bp.setFileName(fileds.getName());
                mp.addBodyPart(bp);
            }
            return true;
        } catch (Exception e) {
            log.error("增加附件: " + filename + "--faild!" + e);
            return false;
        }
    }

    /**
	 * 
	 * @param from
	 * @return
	 */
    public boolean setFrom(String from) {
        log.debug("Set From .");
        try {
            mimeMsg.setFrom(new InternetAddress(from));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
	 * 
	 * @param to
	 * @return
	 */
    public boolean setTo(String to) {
        log.debug("Set to.");
        if (to == null || to.equals("")) {
            return false;
        }
        try {
            mimeMsg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean setCopyTo(String copyto) {
        if (copyto.equals("") || copyto == null) {
            return false;
        }
        try {
            String copy[];
            copy = copyto.split(";");
            for (int i = 0; i < copy.length; i++) {
                mimeMsg.setRecipients(Message.RecipientType.TO, (Address[]) InternetAddress.parse(copy[i]));
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
	 * 设置信的内容!
	 * 
	 * @param mailBody
	 * @return boolean
	 */
    public boolean setBody(String mailBody) {
        try {
            BodyPart bp = new MimeBodyPart();
            bp.setContent("<meta http-equiv=Context-Type context=text/html;charset=utf-8>" + mailBody, "text/html;charset=utf-8");
            mp.addBodyPart(bp);
            return true;
        } catch (Exception e) {
            log.debug("Set context Faild! " + e);
            return false;
        }
    }

    /**
	 * 
	 * @param htmlpath
	 * @return boolean
	 */
    public boolean setHtml(String htmlpath) {
        try {
            if (!htmlpath.equals("") || htmlpath != null) {
                BodyPart mbp = new MimeBodyPart();
                DataSource ds = new FileDataSource(htmlpath);
                mbp.setDataHandler(new DataHandler(ds));
                mbp.setHeader("Context-ID", "meme");
                mp.addBodyPart(mbp);
            }
            return true;
        } catch (Exception e) {
            log.debug("Set Html Faild!" + e);
            return false;
        }
    }

    public boolean send() {
        try {
            mimeMsg.setContent(mp);
            mimeMsg.saveChanges();
            log.debug("正在SendMail.");
            Session mailSession = Session.getInstance(props, new Authenticator() {

                protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                    return new javax.mail.PasswordAuthentication(username, password);
                }
            });
            mailSession.setDebug(false);
            Transport tp = mailSession.getTransport("smtp");
            tp.connect((String) props.getProperty("mail.stmp.host"), username, password);
            tp.sendMessage(mimeMsg, mimeMsg.getRecipients(Message.RecipientType.TO));
            log.debug("Send Mail 成功..");
            tp.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        MailUtil sm = new MailUtil("email.alibaba-inc.com");
        sm.setNamePass("top@taobao.com", "hello1234");
        sm.setSubject("测试,测试");
        sm.setFrom("fangweng@taobao.com");
        sm.setTo("fangweng@taobao.com");
        sm.addFileAffix("d:\\testlog\\11.csv");
        StringBuffer bs = new StringBuffer();
        bs.append("wuying:\n");
        bs.append("       测试度奇珍异宝埼地在檌!!!!!!!!!!!");
        sm.setBody("DFSAAAAAAAAAAAAAAAAA");
        sm.setNeedAuth(true);
        boolean b = sm.send();
        if (b) {
            System.out.println("\n邮件发送成功!!!!!");
        } else {
            System.out.println("邮件发送失败!!!!");
        }
    }
}
