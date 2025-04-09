package ar.com.AmberSoft.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.log4j.Logger;
import ar.com.AmberSoft.iEvenTask.utils.Tools;

public class Email {

    private static Logger logger = Logger.getLogger(Email.class);

    private static final ResourceBundle config = ResourceBundle.getBundle("config");

    private String from;

    private String host;

    private String port;

    private String auth;

    private String user;

    private String password;

    @SuppressWarnings("rawtypes")
    private Collection destinatarios;

    private String asunto;

    private StringBuffer mensaje;

    private String debug;

    public Email(String auth, String debug, String from, String host, String password, String port, String user) {
        super();
        this.auth = auth;
        this.debug = debug;
        this.from = from;
        this.host = host;
        this.password = password;
        this.port = port;
        this.user = user;
    }

    public Email() {
        super();
        from = config.getString(EmailConst.SMTP_FROM);
        host = config.getString(EmailConst.SMTP_HOST);
        port = config.getString(EmailConst.SMTP_PORT);
        auth = config.getString(EmailConst.SMTP_AUTH);
        user = config.getString(EmailConst.SMTP_USER);
        password = config.getString(EmailConst.SMTP_PASSWORD);
        debug = config.getString(EmailConst.DEBUG);
    }

    /**
	 * Envia el email
	 */
    @SuppressWarnings("rawtypes")
    public String enviar() {
        String error = null;
        if (asunto == null) asunto = "(no subject)";
        if (mensaje == null) mensaje = new StringBuffer("");
        try {
            Properties props = new Properties();
            boolean doAuth = !"none".equals(auth);
            boolean useTLS = "tls".equals(auth);
            Session session;
            props.put("mail.from", from);
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.debug", debug);
            if (useTLS) {
                props.put("mail.smtp.socketFactory.port", port);
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.socketFactory.fallback", "false");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.ssl", "true");
            }
            if (doAuth) {
                Authenticator authenticator = new ar.com.AmberSoft.util.Authenticator(user, password);
                props.put("mail.user", user);
                props.put("mail.smtp.auth", "true");
                session = Session.getInstance(props, authenticator);
            } else session = Session.getInstance(props);
            MimeMessage message = new MimeMessage(session);
            for (Iterator iterator = destinatarios.iterator(); iterator.hasNext(); ) {
                String addressee = (String) iterator.next();
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(addressee));
            }
            message.setFrom(new InternetAddress(from));
            message.setText(mensaje.toString());
            message.setSubject(asunto);
            Transport.send(message);
        } catch (Throwable e) {
            logger.error(Tools.getStackTrace(e));
        }
        return error;
    }

    @SuppressWarnings("rawtypes")
    public Collection getDestinatarios() {
        return destinatarios;
    }

    @SuppressWarnings("rawtypes")
    public void setDestinatarios(Collection destinatarios) {
        this.destinatarios = destinatarios;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public StringBuffer getMensaje() {
        return mensaje;
    }

    public void setMensaje(StringBuffer mensaje) {
        this.mensaje = mensaje;
    }
}
