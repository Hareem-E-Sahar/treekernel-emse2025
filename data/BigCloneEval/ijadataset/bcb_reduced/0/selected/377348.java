package es.caib.mobtratel.persistence.util;

import java.util.Iterator;
import java.util.List;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.InitialContext;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import es.caib.mobtratel.model.Constantes;
import es.caib.mobtratel.model.Envio;
import es.caib.mobtratel.model.MensajeEmail;
import es.caib.xml.ConstantesXML;

/**
 * Clase de utilidad para enviar correos
 *
 */
public class EmailUtils {

    private static EmailUtils instance = null;

    private Log log = LogFactory.getLog(EmailUtils.class);

    protected EmailUtils() {
    }

    public static EmailUtils getInstance() {
        if (instance == null) {
            instance = new EmailUtils();
        }
        return instance;
    }

    public boolean enviar(MensajeEmail me, Envio envio, List destinatarios) throws Exception {
        try {
            InitialContext jndiContext = new InitialContext();
            Session mailSession = (Session) jndiContext.lookup("java:/" + envio.getCuenta().getEmail());
            MimeMessage msg = new MimeMessage(mailSession);
            InternetAddress[] direcciones = getDirecciones(destinatarios);
            msg.setRecipients(javax.mail.Message.RecipientType.BCC, direcciones);
            msg.setSubject(me.getTitulo());
            byte[] mensaje = me.getMensaje();
            String contenido;
            if (me.isHtml()) {
                contenido = new String(mensaje, ConstantesXML.ENCODING);
            } else {
                contenido = StringEscapeUtils.escapeHtml(new String(mensaje, ConstantesXML.ENCODING));
            }
            msg.setContent(contenido, "text/html");
            msg.setHeader("X-Mailer", "JavaMailer");
            msg.setSentDate(new java.util.Date());
            log.debug("Vamos a enviar Email: " + msg.getContent());
            try {
                Transport.send(msg);
            } catch (Exception ex) {
                throw ex;
            }
            return true;
        } catch (Exception ex) {
            throw ex;
        }
    }

    private InternetAddress[] getDirecciones(String destinatarios) throws AddressException {
        String[] destArray = destinatarios.split(Constantes.SEPARADOR_DESTINATARIOS);
        InternetAddress[] direcciones = new InternetAddress[destArray.length];
        for (int i = 0; i < destArray.length; i++) {
            direcciones[i] = new InternetAddress(destArray[i]);
        }
        return direcciones;
    }

    private InternetAddress[] getDirecciones(List destinatarios) throws AddressException {
        InternetAddress[] direcciones = new InternetAddress[destinatarios.size()];
        int i = 0;
        for (Iterator it = destinatarios.iterator(); it.hasNext(); ) {
            direcciones[i++] = new InternetAddress((String) it.next());
        }
        return direcciones;
    }
}
