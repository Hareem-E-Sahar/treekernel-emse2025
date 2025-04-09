package testing;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

public class TestEmail {

    private static Properties defaultSMTPConfig = new Properties();

    static {
        defaultSMTPConfig.setProperty("mail.smtp.host", "localhost");
        defaultSMTPConfig.setProperty("mail.smtp.port", "25");
    }

    public static void javaMailSend(String from, String to, String subject, String content) throws Exception {
        Session session = Session.getDefaultInstance(defaultSMTPConfig);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject);
        message.setContent(content, "text/html; charset=UTF-8");
        Transport.send(message);
    }

    public static void springMailSend(String from, String to, String subject, String content) throws Exception {
        JavaMailSender javamailSender = new JavaMailSenderImpl();
        ((JavaMailSenderImpl) javamailSender).setSession(Session.getDefaultInstance(defaultSMTPConfig));
        MimeMessage message = javamailSender.createMimeMessage();
        message.setFrom(new InternetAddress(from));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject);
        message.setContent(content, "text/html; charset=UTF-8");
        javamailSender.send(message);
    }

    public static void main(String[] argv) throws Exception {
        javaMailSend("btnguyen2k@yahoo.com", "btnguyen2k@gmail.com", "JavaMail: Test Email", "Chào bạn,<br><br><br><br>Bạn nhận được email này vì địa chỉ email của bạn được sử dụng để đăng ký tài khoản tại <a href=\"http://localhost:8080/vcs/\">VCS</a>.<br><br>Xin vui lòng kích hoạt tài khoản của bạn bằng cách truy cập vào đường dẫn sau: <a href=\"http://localhost:880/vcs/member/activate.html?uid=115381&amp;ac=f10aab6838e24a8e4934c91a2c46195d\">http://localhost:880/vcs/member/activate.html?uid=115381&amp;ac=f10aab6838e24a8e4934c91a2c46195d</a>.<br><br><br><br>Trân trọng,<br><br>VCS<br><br>Lưu ý: Vui lòng đừng trả lời lại địa chỉ email này vì email được tự động tạo ra.<br><br><br><br>--------------------------------------------------<br><br><br><br>Greetings,<br><br><br><br>You received this email because your email address has been used for membership registration at <a href=\"http://localhost:8080/vcs/\">VCS</a>.<br><br>Just one more step to finish your registration. Please confirm your registration by clicking on this link:<br><br><a href=\"http://localhost:880/vcs/member/activate.html?uid=115381&amp;ac=f10aab6838e24a8e4934c91a2c46195d\">http://localhost:880/vcs/member/activate.html?uid=115381&amp;ac=f10aab6838e24a8e4934c91a2c46195d</a>.<br><br><br><br>Best regards,<br><br>VCS<br><br>P/S: Please do not reply to this email because it is auto-generated.");
        javaMailSend("btnguyen2k@yahoo.com", "btnguyen2k@gmail.com", "SpringJavaMail: Test Email", "Chào bạn,<br><br><br><br>Bạn nhận được email này vì địa chỉ email của bạn được sử dụng để đăng ký tài khoản tại <a href=\"http://localhost:8080/vcs/\">VCS</a>.<br><br>Xin vui lòng kích hoạt tài khoản của bạn bằng cách truy cập vào đường dẫn sau: <a href=\"http://localhost:880/vcs/member/activate.html?uid=115381&amp;ac=f10aab6838e24a8e4934c91a2c46195d\">http://localhost:880/vcs/member/activate.html?uid=115381&amp;ac=f10aab6838e24a8e4934c91a2c46195d</a>.<br><br><br><br>Trân trọng,<br><br>VCS<br><br>Lưu ý: Vui lòng đừng trả lời lại địa chỉ email này vì email được tự động tạo ra.<br><br><br><br>--------------------------------------------------<br><br><br><br>Greetings,<br><br><br><br>You received this email because your email address has been used for membership registration at <a href=\"http://localhost:8080/vcs/\">VCS</a>.<br><br>Just one more step to finish your registration. Please confirm your registration by clicking on this link:<br><br><a href=\"http://localhost:880/vcs/member/activate.html?uid=115381&amp;ac=f10aab6838e24a8e4934c91a2c46195d\">http://localhost:880/vcs/member/activate.html?uid=115381&amp;ac=f10aab6838e24a8e4934c91a2c46195d</a>.<br><br><br><br>Best regards,<br><br>VCS<br><br>P/S: Please do not reply to this email because it is auto-generated.");
    }
}
