package si.cit.eprojekti.emailer.util;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import si.cit.eprojekti.emailer.dbobj.DelayedSubscription;
import si.cit.eprojekti.emailer.dbobj.Message;
import si.cit.eprojekti.emailer.dbobj.Template;
import com.jcorporate.expresso.core.db.DBException;
import com.jcorporate.expresso.core.misc.DateTime;
import com.jcorporate.expresso.services.dbobj.Setup;

/**
 * @author Luka Pavli�; CIT,  2004.3.23 
 *
 * HtmlTemplateMailer description:
 * 		Class, responsible for sending e-mails, using Expresso's
 * 		Setup fields and HTML template 
 */
public class HtmlTemplateMailer {

    /**
	 *  Send mail to multiple receipents
	 * 
	 * @param to		Vector, containing String objects (emails to send mail to)
	 * @param subject	Mail's subject	 
	 * @param body		Mail's body, to be integrated into template
	 * @param categ		Category to be used, -1 if none
	 */
    public static void sendMail(String requestGetDBName, Vector to, String subject, String body, int categ) throws DBException, AddressException, MessagingException {
        for (int i = 0; i < to.size(); i++) {
            String toEm = (String) to.elementAt(i);
            sendMail(requestGetDBName, toEm, subject, body, categ);
        }
    }

    /**
	 *  Send mail to receipent
	 * 
	 * @param to		Mail to send mail to; method does not handle verification
	 * @param subject	Mail's subject	 
	 * @param body		Mail's body, to be integrated into template
	 * @param categ		Category to be used
	 */
    public static void sendMail(String requestGetDBName, String to, String subject, String body, int categ) throws DBException, AddressException, MessagingException {
        sendMail(requestGetDBName, to, subject, body, categ, false);
    }

    /**
	 *  Send mail to receipent
	 * 
	 * @param to		Mail to send mail to; method does not handle verification
	 * @param subject	Mail's subject	 
	 * @param body		Mail's body, to be integrated into template
	 * @param categ		Category to be used
	 * @param thisIsPacket		True, if this is packet --> won't be saved
	 */
    public static void sendMail(String requestGetDBName, String to, String subject, String body, int categ, boolean thisIsPacket) throws DBException, AddressException, MessagingException {
        String schemaName = "si.cit.eprojekti.emailer.MailerSchema";
        String MAILFrom = Setup.getValue(requestGetDBName, schemaName, "MAILFrom");
        String MAILServer = Setup.getValue(requestGetDBName, schemaName, "MAILServer");
        String MAILUserName = Setup.getValue(requestGetDBName, schemaName, "MAILUserName");
        String MAILPassword = Setup.getValue(requestGetDBName, schemaName, "MAILPassword");
        String FakeSendingMails = Setup.getValue(requestGetDBName, schemaName, "FakeSendingMails");
        if (FakeSendingMails.toUpperCase().equals("Y")) return;
        boolean SaveNullCategoryMails = Setup.getValue(requestGetDBName, schemaName, "SaveNullCategoryMails").equalsIgnoreCase("Y");
        boolean SaveAllOutgoingMails = Setup.getValue(requestGetDBName, schemaName, "SaveAllOutgoingMails").equalsIgnoreCase("Y");
        boolean save = SaveAllOutgoingMails && (categ >= -1);
        if ((SaveNullCategoryMails) && (SaveAllOutgoingMails)) save = true;
        boolean sendimedeatly = true;
        if (DelayedSubscription.isDelayed(categ, to)) {
            sendimedeatly = false;
            save = true;
        }
        if (save && !thisIsPacket) {
            Message mes = new Message();
            if (sendimedeatly) mes.setField("DateSent", DateTime.getDateTimeForDB());
            mes.setField("Category", categ);
            mes.setField("Receipent", to);
            mes.setField("Subject", subject);
            mes.setField("Content", body);
            mes.add();
        }
        if (sendimedeatly) {
            StringBuffer sb = new StringBuffer();
            StringBuffer beg = new StringBuffer();
            StringBuffer en = new StringBuffer();
            Template.getDefaultTemplate(beg, en);
            sb.append(beg.toString());
            sb.append(body);
            sb.append(en.toString());
            String content = sb.toString();
            Properties props = System.getProperties();
            props.put("mail.smtp.host", MAILServer);
            props.put("mail.smtp.user", MAILUserName);
            if (MAILPassword != null) if (!MAILPassword.equals("")) {
                props.put("mail.smtp.password", MAILPassword);
                props.put("mail.smtp.auth", "true");
            }
            javax.mail.Session session_m = javax.mail.Session.getDefaultInstance(props, null);
            MimeMessage msg = new MimeMessage(session_m);
            DataHandler data = new DataHandler(content, "text/html");
            msg.setFrom(new InternetAddress(MAILFrom));
            InternetAddress[] address = { new InternetAddress(to) };
            msg.setRecipients(javax.mail.Message.RecipientType.TO, address);
            msg.setSubject(subject);
            msg.setSentDate(new Date());
            MimeBodyPart mbp1 = new MimeBodyPart();
            mbp1.setDataHandler(data);
            mbp1.setContent(content, "text/html");
            Multipart mp = new MimeMultipart();
            mp.addBodyPart(mbp1);
            msg.setContent(mp);
            Transport.send(msg);
        }
    }
}
