package com.aimluck.eip.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileTypeMap;
import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import org.apache.jetspeed.services.logging.JetspeedLogFactoryService;
import org.apache.jetspeed.services.logging.JetspeedLogger;
import com.aimluck.eip.mail.util.ALMailUtils;
import com.aimluck.eip.services.storage.ALStorageService;
import com.sk_jp.mail.MailUtility;
import com.sun.mail.smtp.SMTPTransport;

/**
 * メール送信（SMTP）を操作する抽象クラスです。 <br />
 * 
 */
public abstract class ALSmtpMailSender implements ALMailSender {

    private static final JetspeedLogger logger = JetspeedLogFactoryService.getLogger(ALSmtpMailSender.class.getName());

    /** <code>AUTH_SEND_NONE</code> 送信時の認証方式（認証なし） */
    public static final int AUTH_SEND_NONE = 0;

    /** <code>AUTH_SEND_POP_BEFORE_SMTP</code> 送信時の認証方式（POP before SMTP） */
    public static final int AUTH_SEND_POP_BEFORE_SMTP = 1;

    /** <code>AUTH_SEND_SMTP_AUTH</code> 送信時の認証方式（SMTP 認証） */
    public static final int AUTH_SEND_SMTP_AUTH = 2;

    /** <code>AUTH_SEND_SSL_AUTH</code> 送信時の認証方式（暗号化なし） */
    public static final int ENCRYPTION_SEND_NONE = 0;

    /** <code>AUTH_SEND_SSL_AUTH</code> 送信時の認証方式（SSL暗号化） */
    public static final int ENCRYPTION_SEND_SSL = 1;

    /** メール送信時の処理結果（送信に成功した） */
    public static final int SEND_MSG_SUCCESS = 0;

    /** メール送信時の処理結果（送信に失敗した） */
    public static final int SEND_MSG_FAIL = 1;

    /** メール送信時の処理結果（メールサイズが送信可能サイズよりも大きいため，送信に失敗した） */
    public static final int SEND_MSG_OVER_MAIL_MAX_SIZE = 2;

    /** メール送信時の処理結果（ロックがかかっていて，送信に失敗した） */
    public static final int SEND_MSG_LOCK = 3;

    /** メール送信時の処理結果（Pop before SMTPの認証失敗で送信に失敗した） */
    public static final int SEND_MSG_FAIL_POP_BEFORE_SMTP_AUTH = 4;

    /** メール送信時の処理結果（SMTP認証の認証失敗で送信に失敗した） */
    public static final int SEND_MSG_FAIL_SMTP_AUTH = 5;

    /** メール送信時の処理結果（管理者のメールアカウントが設定されていないために送信に失敗した） */
    public static final int SEND_MSG_FAIL_NO_ACCOUNT = 6;

    /** 接続時のタイムアウト時間 */
    private final String CONNECTION_TIMEOUT = "120000";

    /** 接続後のタイムアウト時間 */
    private final String TIMEOUT = "420000";

    /** SMTP サーバ */
    public static final String MAIL_SMTP_HOST = "mail.smtp.host";

    /** SMTP サーバのポート番号 */
    public static final String MAIL_SMTP_PORT = "mail.smtp.port";

    /** SMTP サーバとの接続時のタイムアウト */
    public static final String MAIL_SMTP_CONNECTION_TIMEOUT = "mail.stmp.connectiontimeout";

    /** SMTP サーバとの接続後のタイムアウト */
    public static final String MAIL_SMTP_TIMEOUT = "mail.stmp.timeout";

    /** 文字コード（ISO-2022-JP） */
    public static final String CHARSET_ISO2022JP = "iso-2022-jp";

    /** SMTP サーバへの接続情報 */
    protected Properties smtpServerProp = null;

    /** POP BEFORE SMTP の WAIT 時間 (ms) */
    protected long POP_BEFORE_SMTP_WAIT_TIME = 1000;

    /** SSL ファクトリー */
    public static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

    /** 送信用セッション */
    protected Session session = null;

    protected ALSmtpMailSenderContext scontext = null;

    public ALSmtpMailSender(ALMailSenderContext scontext) {
        this.scontext = (ALSmtpMailSenderContext) scontext;
    }

    /**
   * 新規作成のメールを取得します。
   * 
   * @param mcontext
   * @return
   */
    private ALLocalMailMessage createMessage(ALSmtpMailContext mcontext) {
        System.setProperty("mail.mime.charset", "ISO-2022-JP");
        System.setProperty("mail.mime.decodetext.strict", "false");
        ALLocalMailMessage msg = null;
        smtpServerProp = new Properties();
        smtpServerProp.setProperty(MAIL_SMTP_HOST, scontext.getSmtpHost());
        smtpServerProp.setProperty(MAIL_SMTP_PORT, scontext.getSmtpPort());
        smtpServerProp.put(MAIL_SMTP_CONNECTION_TIMEOUT, CONNECTION_TIMEOUT);
        smtpServerProp.put(MAIL_SMTP_TIMEOUT, TIMEOUT);
        smtpServerProp.setProperty("mail.mime.address.strict", "false");
        smtpServerProp.put("mail.smtp.localhost", "localhost");
        if (scontext.getEncryptionFlag() == ENCRYPTION_SEND_SSL) {
            smtpServerProp.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
            smtpServerProp.setProperty("mail.smtp.socketFactory.fallback", "false");
            smtpServerProp.setProperty("mail.smtp.socketFactory.port", scontext.getSmtpPort());
        }
        if (scontext.getAuthSendFlag() == AUTH_SEND_SMTP_AUTH && scontext.getAuthSendUserId() != null && !"".equals(scontext.getAuthSendUserId()) && scontext.getAuthSendUserPassword() != null && !"".equals(scontext.getAuthSendUserPassword())) {
            smtpServerProp.put("mail.smtp.auth", "true");
            session = Session.getInstance(smtpServerProp, new ALSmtpAuth(scontext.getAuthSendUserId(), scontext.getAuthSendUserPassword()));
        } else {
            session = Session.getInstance(smtpServerProp, null);
        }
        try {
            msg = new ALLocalMailMessage(session);
            msg.setFrom(new InternetAddress(mcontext.getFrom(), ALMailUtils.encodeWordJIS(mcontext.getName())));
            if (mcontext.getTo() == null) {
                throw new MessagingException();
            }
            setRecipient(msg, Message.RecipientType.TO, mcontext.getTo());
            if (mcontext.getCc() != null) {
                setRecipient(msg, Message.RecipientType.CC, mcontext.getCc());
            }
            if (mcontext.getBcc() != null) {
                setRecipient(msg, Message.RecipientType.BCC, mcontext.getBcc());
            }
            msg.setHeader(ALLocalMailMessage.CONTENT_TYPE, "text/plain");
            msg.setHeader(ALLocalMailMessage.CONTENT_TRANSFER_ENCORDING, "7bit");
            msg.setHeader(ALLocalMailMessage.X_Mailer, ALLocalMailMessage.X_Mailer_Value);
            msg.setSubject(ALMailUtils.encodeWordJIS(mcontext.getSubject()));
            msg.setSentDate(new Date());
            Map<String, String> headers = mcontext.getAdditionalHeaders();
            if (headers != null) {
                synchronized (headers) {
                    String key = null;
                    String value = null;
                    Map.Entry<String, String> entry = null;
                    for (Iterator<Map.Entry<String, String>> i = headers.entrySet().iterator(); i.hasNext(); ) {
                        entry = i.next();
                        key = entry.getKey();
                        value = entry.getValue();
                        msg.setHeader(key, value);
                    }
                }
            }
            if (mcontext.getFilePaths() == null) {
                ALMailUtils.setTextContent(msg, mcontext.getMsgText());
            } else {
                String[] checkedFilePaths = mcontext.getFilePaths();
                int checkedFilePathsLength = checkedFilePaths.length;
                if (checkedFilePathsLength <= 0) {
                    ALMailUtils.setTextContent(msg, mcontext.getMsgText());
                } else {
                    Multipart multiPart = new MimeMultipart();
                    MimeBodyPart mimeText = new MimeBodyPart();
                    mimeText.setText(mcontext.getMsgText(), CHARSET_ISO2022JP);
                    multiPart.addBodyPart(mimeText);
                    MimeBodyPart mimeFile = null;
                    for (int i = 0; i < checkedFilePathsLength; i++) {
                        final String filePath = checkedFilePaths[i];
                        final String fileName = ALMailUtils.getFileNameFromText(checkedFilePaths[i]);
                        mimeFile = new MimeBodyPart();
                        mimeFile.setDataHandler(new DataHandler(new DataSource() {

                            @Override
                            public String getContentType() {
                                return FileTypeMap.getDefaultFileTypeMap().getContentType(fileName);
                            }

                            @Override
                            public InputStream getInputStream() throws IOException {
                                return ALStorageService.getFile(filePath);
                            }

                            @Override
                            public String getName() {
                                return fileName;
                            }

                            @Override
                            public OutputStream getOutputStream() throws IOException {
                                throw new UnsupportedOperationException("getOutputStream");
                            }
                        }));
                        MailUtility.setFileName(mimeFile, ALMailUtils.getFileNameFromText(checkedFilePaths[i]), "ISO-2022-JP", null);
                        multiPart.addBodyPart(mimeFile);
                    }
                    msg.setContent(multiPart);
                }
            }
        } catch (Exception e) {
            logger.error("Exception", e);
            return null;
        }
        return msg;
    }

    /**
   * SMTP サーバへメールを送信する．
   * 
   * @param to
   * @param cc
   * @param bcc
   * @param from
   * @param name
   * @param subject
   * @param msgText
   * @param filePaths
   * @return
   */
    @Override
    public int send(ALMailContext context) {
        try {
            ALSmtpMailContext mcontext = (ALSmtpMailContext) context;
            ALLocalMailMessage msg = createMessage(mcontext);
            if (msg == null) {
                return SEND_MSG_FAIL;
            }
            int mailSize = msg.getSize();
            if (mailSize > ALMailUtils.getMaxMailSize()) {
                return SEND_MSG_OVER_MAIL_MAX_SIZE;
            }
            if (scontext.getAuthSendFlag() == AUTH_SEND_NONE) {
                Transport.send(msg);
            } else if (scontext.getAuthSendFlag() == AUTH_SEND_POP_BEFORE_SMTP) {
                boolean success = ALPop3MailReceiver.isAuthenticatedUser(scontext.getPop3Host(), scontext.getPop3Port(), scontext.getPop3UserId(), scontext.getPop3UserPasswd(), scontext.getPop3EncryptionFlag());
                if (!success) {
                    return SEND_MSG_FAIL_POP_BEFORE_SMTP_AUTH;
                } else {
                    Thread.sleep(POP_BEFORE_SMTP_WAIT_TIME);
                }
                Transport.send(msg);
            } else if (scontext.getAuthSendFlag() == AUTH_SEND_SMTP_AUTH) {
                Transport transport = session.getTransport("smtp");
                SMTPTransport smtpt = (SMTPTransport) transport;
                smtpt.setSASLRealm("localhost");
                smtpt.connect(scontext.getSmtpHost(), scontext.getAuthSendUserId(), scontext.getAuthSendUserPassword());
                smtpt.sendMessage(msg, msg.getAllRecipients());
                smtpt.close();
            }
            ALFolder sendFolder = getALFolder();
            sendFolder.saveMail(msg, null);
        } catch (AuthenticationFailedException ex) {
            logger.error("Exception", ex);
            return SEND_MSG_FAIL_SMTP_AUTH;
        } catch (Exception ex) {
            logger.error("Exception", ex);
            return SEND_MSG_FAIL;
        } catch (Throwable e) {
            logger.error("Throwable", e);
            return SEND_MSG_FAIL;
        }
        return SEND_MSG_SUCCESS;
    }

    protected abstract ALFolder getALFolder();

    /**
   * メールの宛名をセットする．
   * 
   * @param msg
   * @param recipientType
   * @param addrString
   * @throws AddressException
   * @throws MessagingException
   */
    private void setRecipient(Message msg, Message.RecipientType recipientType, String[] addrString) throws AddressException, MessagingException {
        if (addrString == null) {
            return;
        }
        int addrStringLength = addrString.length;
        InternetAddress[] address = new InternetAddress[addrStringLength];
        for (int i = 0; i < addrStringLength; i++) {
            address[i] = ALMailUtils.getInternetAddress(addrString[i]);
        }
        msg.setRecipients(recipientType, address);
    }
}
