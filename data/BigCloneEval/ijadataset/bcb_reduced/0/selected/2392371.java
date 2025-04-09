package ru.scriptum.model.util;

import java.io.File;
import java.util.Date;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import ru.scriptum.model.exception.CatalogException;
import ru.scriptum.spring.session.SpringContext;

/**
 * Utility class to send email.
 * 
 * @author <a href="mailto:dev@scriptum.ru">Developer</a>
 */
public class EmailUtil {

    private static Log logger = LogFactory.getLog("ru.scriptum.view.util.EmailUtil");

    /**
	 * Send email to a list of recipients.
	 * 
	 * @param smtpHost
	 *            the SMTP email server address
	 * @param receiverAddress
	 *            the sender email address
	 * @param receiverName
	 *            the sender name
	 * @param recipients
	 *            a list of receipients email addresses
	 * @param sub
	 *            the subject of the email
	 * @param msg
	 *            the message content of the email
	 */
    public static void sendPlainEmail(String smtpHost, String smtpUsername, String smtpPassword, boolean smtpAuthRequired, String receiverAddress, String receiverName, String sendersAddress, String sendersName, String sub, String msg) throws CatalogException {
        if (smtpHost == null) {
            String errMsg = "Could not send email: smtp host address is null";
            logger.error(errMsg);
            throw new CatalogException(errMsg);
        }
        try {
            Properties props = System.getProperties();
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.auth", Boolean.toString(smtpAuthRequired));
            props.put("mail.smtp.debug", "true");
            Authenticator auth = null;
            if (smtpAuthRequired) {
                auth = new SMTPAuthenticator(smtpUsername, smtpPassword);
            }
            Session session = Session.getDefaultInstance(props, auth);
            MimeMessage message = new MimeMessage(session);
            message.addHeader("Content-type", "text/plain");
            message.setSubject(sub);
            message.setFrom(new InternetAddress(sendersAddress, sendersName));
            message.addRecipients(Message.RecipientType.TO, receiverAddress);
            message.setText(msg);
            message.setSentDate(new Date());
            Transport.send(message);
        } catch (Exception e) {
            String errorMsg = "Could not send email";
            logger.error(errorMsg, e);
            throw new CatalogException("errorMsg", e);
        }
    }

    /**
	 * Send email to a list of recipients.
	 * @param properties 
	 * 
	 * @param smtpHost
	 *            the SMTP email server address
	 * @param receiverAddress
	 *            the sender email address
	 * @param receiverName
	 *            the sender name
	 * @param recipients
	 *            a list of receipients email addresses
	 * @param subject
	 *            the subject of the email
	 * @param confirmationUrl
	 *            the message content of the email
	 * @param confirmationUrl2 
	 */
    public static void sendHtmlEmail(JavaMailSender mailSender, String smtpHost, String smtpUsername, String smtpPassword, boolean smtpAuthRequired, String receiverAddress, String receiverName, String sendersAddress, String sendersName, String smtpPort, String sub, String confirmationUrl, String htmlMessage) throws MailSendException {
        if (smtpHost == null) {
            String errMsg = "Could not send email: smtp host address is null";
            logger.error(errMsg);
            throw new MailSendException(errMsg);
        }
        Session session = null;
        Properties props = null;
        MimeMessage message = null;
        try {
            props = System.getProperties();
            Authenticator auth = null;
            if (smtpAuthRequired) {
                auth = new SMTPAuthenticator(smtpUsername, smtpPassword);
            }
            session = Session.getDefaultInstance(props, auth);
            message = new MimeMessage(session);
            message.addHeader("Content-type", "text/plain");
            message.setSubject(sub);
            message.setFrom(new InternetAddress(sendersAddress, sendersName));
            message.addRecipients(Message.RecipientType.TO, receiverAddress);
            message.setText(confirmationUrl);
            message.setSentDate(new Date());
        } catch (Exception e) {
            String errorMsg = "Could not send email";
            logger.error(errorMsg, e);
            throw new MailSendException(errorMsg, e);
        }
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(message, true);
            helper.setTo(receiverAddress);
            helper.setText(htmlMessage, true);
            String path = getRealContextRoot() + "/images/logo-theatron.gif";
            FileSystemResource res = new FileSystemResource(new File(path));
            helper.addInline("identifier1234", res);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public static void sendHtmlEmailBulk(JavaMailSender mailSender, String smtpHost, String smtpUsername, String smtpPassword, boolean smtpAuthRequired, String receiverAddress, String receiverName, String sendersAddress, String sendersName, String smtpPort, String subject, String confirmationUrl, String htmlMessage) throws MailSendException {
        if (smtpHost == null) {
            String errMsg = "Could not send email: smtp host address is null";
            logger.error(errMsg);
            throw new MailSendException(errMsg);
        }
        htmlMessage = "Я благодарю вас за незабываемые моменты, которые вы подарили мне в уходящем году. " + "Мы оказались в разных часовых поясах, но сумели поддержать живые связи, " + "которые и воодушевили меня на открытие " + "театрального сайта <a href='http://post.scriptum.ru'>post.scriptum.ru</a> – к театру в пространстве и времени. " + "В следующем году, прекрасно было бы продолжить создание единой картины мира через театр вместе с вами.<br/>" + "Читайте последний P.S. в этом году о возобновлении «Жизели» моей давней подругой Татьяной Легат.<br/>" + "С Новым Годом! Happy New Year! Честита Нова Година! Bonne Année! Frohes neues Jahr! Buon Anno! Szczęśliwego Nowego Roku! Gott Nytt år!<br/>" + "Ваша Майя Праматарова <br/>mayia@inbox.ru<br/> <a href='http://post.scriptum.ru'>post.scriptum.ru</a><br/><br/>";
        htmlMessage += "<div style='background-color: #fff; margin: 2em; border: 1px dotted #cccccc; padding: 1em; width: 300px;'><div style='margin-left: 100px; font-family:serif;'><a style='background-color: #fff;' href='http://post.scriptum.ru'><img border='0' src='http://post.scriptum.ru/img/ps-bg-40-b-w.png'></a><div style='display:block; font-size: 7px; margin-top: -20px;'><a href='http://post.scriptum.ru/article/gizelle-legat-perm'>театр в пространстве и времени</a></div></div><div style='dipslay: block; color: #F81A03; width: 240px;  font-size: 20px; font-family: sans-serif; margin: 1em'><a href='http://post.scriptum.ru/article/gizelle-legat-perm'><span style='color: #F81A03;'>Строго сохраняемые партитуры</span></a></div><div style='display: block; clear: both; margin: 1em'><a href='http://post.scriptum.ru/article/gizelle-legat-perm'><img border='0' src='http://post.scriptum.ru/content/gizelle-legat-perm/head-img.jpg'></a></div><div style='display:block; clear:both; width: 240px; margin: 1em 2em 1em 1em;'><a href='http://post.scriptum.ru/article/gizelle-legat-perm'>Татьяна Легат: Моя работа в «Жизели» сопоставима с работой реставратора, который стремится вернуться к первому живописному слою картины.</a></div><div style='font-size:7px;margin: 1em'>stage/Пермь/26-12-2008/Майя Праматарова</div><div style='position: relative; margin-left:250px; font-size: 75%'>ps6</div></div>";
        Session session = null;
        Properties props = null;
        MimeMessage message = null;
        try {
            props = System.getProperties();
            Authenticator auth = null;
            if (smtpAuthRequired) {
                auth = new SMTPAuthenticator(smtpUsername, smtpPassword);
            }
            session = Session.getDefaultInstance(props, auth);
            message = new MimeMessage(session);
            message.addHeader("Content-type", "text/plain; charset=UTF-8");
            subject = "Майя Праматарова и post.scriptum.ru поздравляют Вас с Новым Годом!";
            message.setSubject(subject, "UTF-8");
            String addressesss = "";
            message.setFrom(new InternetAddress(sendersAddress, sendersName));
            message.addRecipients(Message.RecipientType.TO, receiverAddress);
            message.addRecipients(Message.RecipientType.BCC, addressesss);
            message.setText(htmlMessage, "UTF-8");
            message.setSentDate(new Date());
        } catch (Exception e) {
            String errorMsg = "Could not send email";
            logger.error(errorMsg, e);
            throw new MailSendException(errorMsg, e);
        }
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(receiverAddress);
            helper.setText(htmlMessage, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public static String getRealContextRoot() {
        return SpringContext.getInstance().getServletContext().getRealPath(System.getProperty("file.separator"));
    }
}
