package Mailing;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.Transport;

public class Mail {

    private String msgBody, msgBodyEnd;

    private String msgSubject;

    private Properties prop;

    private Session session;

    Message msg;

    public Mail() throws UnsupportedEncodingException, MessagingException {
        msgBody = "Dziękujemy za rejestrację w serwisie.\n Aby aktywować swoje konto kliknij " + "poniższy link lub skopiuj go do przeglądarki:\n http://2.latest.thegame-331.appspot.com/thegame331?regid=";
        msgBodyEnd = "\">aktywuj</a>";
        msgSubject = "Rejestracja w serwisie gry TheGame331";
        prop = new Properties();
        session = Session.getDefaultInstance(prop, null);
        msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("thegame331idz@gmail.com"));
    }

    public void Send(String mailAddress, String regid) throws MessagingException {
        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(mailAddress));
        msg.setSubject(msgSubject);
        msg.setText(msgBody + regid + msgBodyEnd);
        Transport.send(msg);
    }
}
