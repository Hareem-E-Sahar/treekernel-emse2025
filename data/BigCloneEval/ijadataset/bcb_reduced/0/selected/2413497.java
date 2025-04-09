package br.org.acessobrasil.portal.util;

import java.io.File;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Implementacao para funcionar na locaweb,
 * Configurado no spring 
 */
public class SendMailUsingAuthentication implements SendMailProvider {

    private static String SMTP_HOST_NAME = "smtp.acessobrasil.org.br";

    private static String SMTP_AUTH_USER = "acesso@acessobrasil.org.br";

    private static String SMTP_AUTH_PWD = "acesso1234";

    private static String paraDefault = "fabio.oshiro@gmail.com";

    public static void main(String args[]) throws Exception {
        String emailMsgTxt = "Controle de Mensagens via Servlet - JAVA.<b>Bold</b>";
        String emailSubjectTxt = "Subject da Mensagem";
        String emailFromAddress = "fabio@acessobrasil.org.br";
        String[] emailList = { "fabio.oshiro@gmail.com", "fabio@acessobrasil.org.br" };
        SendMailUsingAuthentication smtpMailSender = new SendMailUsingAuthentication();
        smtpMailSender.postMail(emailList, emailSubjectTxt, emailMsgTxt, emailFromAddress, false);
        System.out.println("Sucesso , as mensagens foram enviadas para os destinatarios");
    }

    public void postMail(String recipients[], String subject, String message, String from, boolean html) throws MessagingException {
        boolean debug = false;
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST_NAME);
        props.put("mail.smtp.auth", "true");
        Authenticator auth = new SMTPAuthenticator();
        Session session = Session.getDefaultInstance(props, auth);
        session.setDebug(debug);
        Message msg = new MimeMessage(session);
        InternetAddress addressFrom = new InternetAddress(from);
        msg.setFrom(addressFrom);
        if (recipients == null || recipients.length == 0 || recipients[0] == null || recipients[0].equals("")) {
            InternetAddress[] addressTo = new InternetAddress[1];
            addressTo[0] = new InternetAddress(paraDefault);
        } else {
            InternetAddress[] addressTo = new InternetAddress[recipients.length];
            for (int i = 0; i < recipients.length; i++) {
                addressTo[i] = new InternetAddress(recipients[i]);
            }
            msg.setRecipients(Message.RecipientType.TO, addressTo);
        }
        msg.setSubject(subject);
        if (html) {
            msg.setContent(message, "text/html");
        } else {
            msg.setContent(message, "text/plain");
        }
        Transport.send(msg);
    }

    /**
	 * 
	 * Autenticacao simples
	 * 
	 * Caso o servidor solicite
	 */
    private class SMTPAuthenticator extends javax.mail.Authenticator {

        public PasswordAuthentication getPasswordAuthentication() {
            String username = SMTP_AUTH_USER;
            String password = SMTP_AUTH_PWD;
            return new PasswordAuthentication(username, password);
        }
    }

    @Override
    public void enviarEmail(String assunto, String mensagem, String para, String paraNome, String de, String deNome) throws Exception {
        postMail(new String[] { para }, assunto, mensagem, de, false);
    }

    @Override
    public void enviarHtmlEmail(String assunto, String mensagem, String para, String paraNome, String de, String deNome, File[] anexo) throws Exception {
        postMail(new String[] { para }, assunto, mensagem, de, true);
    }

    @Override
    public void enviarHtmlEmail(String assunto, String mensagem, String para, String paraNome, String de, String deNome) throws Exception {
        postMail(new String[] { para }, assunto, mensagem, de, true);
    }

    @Override
    public void setHostName(String hostName) {
        SMTP_HOST_NAME = hostName;
    }

    @Override
    public void setParaDefault(String paraDefault) {
        SendMailUsingAuthentication.paraDefault = paraDefault;
    }

    @Override
    public void setParaDefaultNome(String nome) {
    }

    @Override
    public void setPass(String pass) {
        SMTP_AUTH_PWD = pass;
    }

    @Override
    public void setUser(String user) {
        SMTP_AUTH_USER = user;
    }
}
