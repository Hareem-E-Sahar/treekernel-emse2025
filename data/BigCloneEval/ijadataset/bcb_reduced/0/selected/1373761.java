package neoAtlantis.utilidades.notifier;

import java.io.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import neoAtlantis.utilidades.notifier.interfaces.Notifier;
import org.jdom.*;
import org.jdom.input.SAXBuilder;

/**
 * Notificador que envia los mensaje por mail.
 * <h4>Configuraci&oacute;n del envio de mails</h4>
 * Para poder enviar mails se dispone de 2 medios, mediante un archivo xml o mediante un {@link java.util.Properties Properties}.
 * <br><br>
 * En caso de ser un archivo XML este debe de seguir la siguiente estrutura:
 * <pre>
 * <b>&lt;mail&gt;</b>
 *     <b>&lt;host&gt;</b><i>servidor_de_correos</i><b>&lt;/host&gt;</b>
 *     <b>&lt;from&gt;</b><i>mail_de_quien_envia_la_notificacion</i><b>&lt;/from&gt;</b>
 *     <b>&lt;to&gt;</b><i>mail_de_a_quien_se_envia_la_notificacion</i><b>&lt;/vGUSUARIO&gt;</b>
 * <b>&lt;/mail&gt;</b>
 *
 * Y en caso de ser un archivo properties este debe de seguir la siguiente estrutura:
 * <b>host</b> = servidor de correos
 * <b>from</b> = mail de quien envia la notificacion
 * <b>to</b> = mail de quien recive la notificacion
 * </pre>
 * @version 1.0
 * @author Hiryu (asl_hiryu@yahoo.com)
 */
public class MailNotifier extends Notifier {

    /**
     * Versión del notificador
     */
    public static final String VERSION = "1.0";

    private Properties config;

    private boolean debug;

    /**
     * Genera un Notificador por mail
     * @param titulo Titulo que enviara en sus mensaje el notificador
     * @param xml Flujo de entrada del XML con la configuración del envio de mails.
     * @throws java.lang.Exception
     */
    public MailNotifier(String titulo, InputStream xml) throws Exception {
        super(titulo);
        SAXBuilder builder = new SAXBuilder(false);
        Document doc = builder.build(xml);
        Element e, raiz = doc.getRootElement();
        Properties p = new Properties();
        if (raiz != null && (raiz.getName().equalsIgnoreCase("mail"))) {
            List hojas = raiz.getChildren();
            Iterator i = hojas.iterator();
            while (i.hasNext()) {
                e = (Element) i.next();
                if (e.getName().equalsIgnoreCase("HOST")) {
                    p.setProperty("host", e.getValue());
                } else if (e.getName().equalsIgnoreCase("FROM")) {
                    p.setProperty("from", e.getValue());
                } else if (e.getName().equalsIgnoreCase("TO")) {
                    p.setProperty("to", e.getValue());
                }
            }
        }
        this.validaConfiguracion(p);
    }

    /**
     * Genera un Notificador por mail
     * @param titulo Titulo que enviara en sus mensaje el notificador
     * @param xml Archivo XML con la configuración del envio de mails.
     * @throws java.lang.Exception
     */
    public MailNotifier(String titulo, File xml) throws Exception {
        this(titulo, new FileInputStream(xml));
    }

    /**
     * Genera un Notificador por mail
     * @param titulo Titulo que enviara en sus mensaje el notificador
     * @param xml Ruta completa del archivo XML con la configuración del envio de mails.
     * @throws java.lang.Exception
     */
    public MailNotifier(String titulo, String xml) throws Exception {
        this(titulo, new File(xml));
    }

    /**
     * Genera un Notificador por mail
     * @param titulo Titulo que enviara en sus mensaje el notificador
     * @param config Configuración del envio de mails.
     * @throws java.lang.Exception
     */
    public MailNotifier(String titulo, Properties config) throws Exception {
        super(titulo);
        this.validaConfiguracion(config);
    }

    public MailNotifier(String titulo, String host, String from, String to) throws Exception {
        super(titulo);
        this.config = new Properties();
        this.config.setProperty("host", host);
        this.config.setProperty("from", from);
        this.config.setProperty("to", to);
    }

    private void validaConfiguracion(Properties p) throws Exception {
        if (p == null) {
            throw new Exception("No existe configuración para enviar el mail.");
        }
        if (p.getProperty("host") == null || p.getProperty("host").length() == 0) {
            throw new Exception("Falta el Host para envviar el mail.");
        }
        if (p.getProperty("from") == null || p.getProperty("from").length() == 0) {
            throw new Exception("Falta el Remitente del mail.");
        }
        if (p.getProperty("to") == null || p.getProperty("to").length() == 0) {
            throw new Exception("Falta el destinatario del mail.");
        }
        this.config = p;
    }

    private Properties parseConfig() {
        Properties p = new Properties();
        p.setProperty("mail.smtp.host", this.config.getProperty("host"));
        p.setProperty("mail.smtp.starttls.enable", "true");
        p.setProperty("mail.smtp.auth", "false");
        return p;
    }

    /**
     * Envia una notificacion por mail
     * @param app Nombre de la aplicación
     * @param det Detalle de la notificación
     * @return true si se logro enviar
     * @throws java.lang.Exception
     */
    public boolean enviaNotificacion(String app, String det) throws Exception {
        StringBuffer sb = new StringBuffer("");
        StringTokenizer st = new StringTokenizer(this.config.getProperty("to"), ",");
        sb.append(this.sdf.format(new Date())).append("\n\n").append("La aplicación '").append(app).append("' ha presentado un error.\n\nDetalles del error:\n").append(det);
        Session session = Session.getInstance(this.parseConfig());
        session.setDebug(this.debug);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(this.config.getProperty("from")));
        while (st.hasMoreTokens()) {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(st.nextToken()));
        }
        message.setSubject(this.titulo);
        message.setContent(sb.toString(), "text/plain");
        Transport.send(message);
        return true;
    }

    /**
     * Activa el debug del notificador
     * @param debug true para activar
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
