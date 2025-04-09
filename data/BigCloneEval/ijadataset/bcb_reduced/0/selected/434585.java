package com.googlecode.articulando.framework.util.mail;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import com.googlecode.articulando.framework.exception.EmailException;

public class EnviarEmail {

    private static EnviarEmail instancia;

    private EnviarEmail() {
    }

    public static EnviarEmail getInstancia() {
        if (instancia == null) {
            instancia = new EnviarEmail();
        }
        return instancia;
    }

    public void enviarEmail(Servidor servidor, Email email) throws EmailException {
        try {
            MimeMessage message = defineEmail(servidor, email);
            Transport.send(message);
        } catch (UnsupportedEncodingException e) {
            throw new EmailException(e);
        } catch (MessagingException e) {
            throw new EmailException(e);
        }
    }

    private MimeMessage defineEmail(Servidor servidor, Email email) throws MessagingException, UnsupportedEncodingException {
        Properties props = System.getProperties();
        props.put("mail.smtp.host", servidor.getEndereco());
        MimeMessage message = defineAutenticacaoServidor(servidor, props);
        message.setFrom(new InternetAddress(email.getDe(), email.getDe()));
        defineDestinatarios(email, message);
        message.setSubject(email.getAssunto());
        message.setContent(email.getMensagem(), "text/plain");
        return message;
    }

    private MimeMessage defineAutenticacaoServidor(Servidor servidor, Properties props) {
        Session session;
        if ((servidor.getUsuario() != null) && (servidor.getSenha() != null)) {
            props.put("mail.smtp.auth", "true");
            AutenticacaoServidor auth = new AutenticacaoServidor(servidor.getUsuario(), servidor.getSenha());
            session = Session.getInstance(props, auth);
        } else {
            props.put("mail.smtp.auth", "false");
            session = Session.getInstance(props);
        }
        MimeMessage message = new MimeMessage(session);
        return message;
    }

    private void defineDestinatarios(Email email, MimeMessage message) throws MessagingException, UnsupportedEncodingException {
        for (String e : email.getPara()) {
            message.addRecipient(Message.RecipientType.BCC, new InternetAddress(e, e));
        }
    }
}
