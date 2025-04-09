package pe.com.bn.sach.common;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import javax.mail.Multipart;
import javax.mail.internet.MimeMultipart;
import javax.mail.BodyPart;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeBodyPart;
import java.io.*;
import org.apache.log4j.Logger;
import java.util.Hashtable;

public class NNotificacion {

    private static Logger log = Logger.getLogger(NNotificacion.class.getName());

    public int enviarEmail(String sCadenaConfigCorreo, String usuCorreo, Address[] to, Address[] cc, String asunto, String mensaje, Hashtable toMails) throws Exception {
        int estadoEjecucion = 99;
        Address[] toValids = null;
        try {
            if (to == null) {
                return -10;
            }
            Properties pProperties = new Properties();
            try {
                InputStream finDoc = new ByteArrayInputStream(sCadenaConfigCorreo.getBytes());
                pProperties.load(finDoc);
            } catch (IOException e) {
                log.error(e.getStackTrace());
                return -10;
            }
            Session sesion = Session.getInstance(pProperties, null);
            MimeMessage message = new MimeMessage(sesion);
            Address remitente = new InternetAddress(usuCorreo);
            message.setFrom(remitente);
            message.setSubject(asunto);
            message.setSentDate(new Date());
            usuCorreo = usuCorreo.toLowerCase();
            message.setHeader("Return-Path", usuCorreo);
            BodyPart mensajeCuerpo = new MimeBodyPart();
            mensajeCuerpo.setContent(mensaje, "text/html; charset=iso-8859-1");
            Multipart archivos = new MimeMultipart();
            archivos.addBodyPart(mensajeCuerpo);
            message.addRecipients(Message.RecipientType.TO, to);
            if (cc != null) {
                if (cc.length > 0) message.addRecipients(Message.RecipientType.CC, cc);
            }
            message.setContent(archivos);
            Transport.send(message);
            estadoEjecucion = 10;
        } catch (SendFailedException sendFailedException) {
            log.error(sendFailedException.getStackTrace());
            log.error("Error sendFailedException:" + sendFailedException.getMessage());
            log.error("Error String sendFailedException:" + sendFailedException.toString());
            if (sendFailedException.getValidUnsentAddresses() != null) {
                int invalidos = sendFailedException.getValidUnsentAddresses().length;
                if (invalidos > 0) {
                    toValids = new Address[invalidos];
                    toValids = sendFailedException.getValidUnsentAddresses();
                    toMails.put("mails", toValids);
                    for (int i = 0; i < toValids.length; i++) {
                    }
                    return -20;
                }
            }
            return -40;
        } catch (MessagingException messagingexception) {
            log.error("Error messagingexception:" + messagingexception.getMessage());
            log.error("Error String messagingexception:" + messagingexception.toString());
            log.error(messagingexception.getStackTrace());
            return -30;
        } catch (Exception e) {
            log.error(e.getStackTrace());
            return -40;
        }
        return estadoEjecucion;
    }
}
