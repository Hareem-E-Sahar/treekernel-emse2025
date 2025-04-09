package br.teste.com;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import com.ecomponentes.formularios.anexo.to.AnexoTO;

public class TesteEmail {

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        TesteEmail teste = new TesteEmail();
        try {
            teste.enviaEmail("localhost", "Dênis çaçaçaDúpòit<deniscass@deniscass.no-ip.org>", "deniscassiano@gmail.com", "A arte é uma beleza caçamba última", "& Céltica ungüento à maioridade", null, "Jesus é o Senhor");
            teste.enviaEmail("localhost", "deniscass@deniscass.no-ip.org", "webmaster@jubacbh.com.br", "A arte é uma beleza caçamba última", "& Céltica ungüento à maioridade", null, "Jesus é o Senhor");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public void enviaEmail(String smtpHost, String from, String to, String assunto, String conteudo, AnexoTO[] arquivos, String assinatura) throws Exception {
        Properties props = System.getProperties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", "25");
        props.put("mail.smtp.auth", "false");
        props.put("mail.mime.charset", "ISO-8859-1");
        Session session = Session.getDefaultInstance(props, null);
        session.setDebug(true);
        MimeMessage message = new MimeMessage(session);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z Z");
        message.addHeader("date-time", sdf.format(new Date()));
        message.setContentLanguage(new String[] { "pr_BR" });
        message.setFrom(new InternetAddress(from));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(assunto, "ISO-8859-1");
        Multipart multipart = new MimeMultipart();
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.addHeader("Content-Type: text/html;", "charset=\"iso-8859-1\"");
        messageBodyPart.setContent(conteudo, "text/html");
        multipart.addBodyPart(messageBodyPart);
        DataSource source;
        message.setContent(multipart);
        Transport.send(message);
    }

    class Autenticador extends Authenticator {

        public String username = null;

        public String password = null;

        public Autenticador(String user, String pwd) {
            username = user;
            password = pwd;
        }

        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
        }
    }
}
