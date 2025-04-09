package org.jp.utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

public class KSendMailUtils {

    public static class KMailAddress {

        private String address;

        private String name;

        public KMailAddress(String address, String name) {
            this.address = address;
            this.name = name;
        }

        public KMailAddress(String address) {
            this.address = address;
            this.name = null;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        /**
		 * 送信用メールアドレス
		 * 
		 * @return
		 * @throws Exception
		 */
        public InternetAddress getInternetAddress() throws Exception {
            if (address == null || address.length() == 0) {
                return null;
            }
            if (name != null && name.length() > 0) {
                return new InternetAddress(this.address, this.name);
            } else {
                return new InternetAddress(this.address);
            }
        }
    }

    private String senderAddress;

    private String password;

    private List<KMailAddress> toAddressList;

    private List<KMailAddress> ccAddressList;

    private List<KMailAddress> bccAddressList;

    private String host;

    private String subject;

    private String body;

    private Multipart multipart = new MimeMultipart();

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<KMailAddress> getToAddressList() {
        return toAddressList;
    }

    public void setToAddressList(List<KMailAddress> toAddressList) {
        this.toAddressList = toAddressList;
    }

    public List<KMailAddress> getCcAddressList() {
        return ccAddressList;
    }

    public void setCcAddressList(List<KMailAddress> ccAddressList) {
        this.ccAddressList = ccAddressList;
    }

    public List<KMailAddress> getBccAddressList() {
        return bccAddressList;
    }

    public void setBccAddressList(List<KMailAddress> bccAddressList) {
        this.bccAddressList = bccAddressList;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    /**
	 * メールアドレスを設定することより、メール送信
	 * 
	 * @throws Exception
	 */
    public void sendMail() throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.auth", "true");
        Session session = Session.getInstance(props, new Authenticator() {

            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderAddress, password);
            }
        });
        session.setDebug(true);
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderAddress));
        if (toAddressList != null && toAddressList.size() > 0) {
            for (KMailAddress address : toAddressList) {
                message.addRecipient(RecipientType.TO, address.getInternetAddress());
            }
        }
        if (ccAddressList != null && ccAddressList.size() > 0) {
            for (KMailAddress address : ccAddressList) {
                message.addRecipient(RecipientType.CC, address.getInternetAddress());
            }
        }
        if (bccAddressList != null && bccAddressList.size() > 0) {
            for (KMailAddress address : bccAddressList) {
                message.addRecipient(RecipientType.BCC, address.getInternetAddress());
            }
        }
        message.setSubject(subject);
        message.setSentDate(new Date());
        BodyPart bodyPart = new MimeBodyPart();
        bodyPart.setContent(body, "text/html;charset=utf-8");
        multipart.addBodyPart(bodyPart);
        message.setContent(multipart);
        Transport.send(message);
    }

    /**
	 * メールアドレスリスト作成
	 * 
	 * メールと表示名間が「;」で、区分する
	 * 
	 * @param addressメールアドレス
	 * @return　メールアドレスリスト
	 */
    public static List<KMailAddress> getAddressList(String... address) {
        if (address == null || address.length < 0) {
            return null;
        }
        List<KMailAddress> list = new ArrayList<KMailAddress>();
        KMailAddress kmailAddress;
        for (String str : address) {
            String[] addressAndName = str.split(";");
            if (addressAndName.length == 2) {
                kmailAddress = new KMailAddress(addressAndName[0], addressAndName[1]);
            } else {
                kmailAddress = new KMailAddress(addressAndName[0]);
            }
            list.add(kmailAddress);
        }
        return list;
    }

    /**
	 * メールが添付ファイルを追加する
	 * @param filePath　添付ファイルのパスー
	 */
    public void addFileForMail(String... filePath) {
        if (filePath == null || filePath.length < 0) {
            return;
        }
        for (String str : filePath) {
            try {
                BodyPart bodyPart = new MimeBodyPart();
                FileDataSource fds = new FileDataSource(str);
                bodyPart.setDataHandler(new DataHandler(fds));
                bodyPart.setFileName(MimeUtility.encodeText(fds.getName()));
                multipart.addBodyPart(bodyPart);
            } catch (MessagingException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }
}
