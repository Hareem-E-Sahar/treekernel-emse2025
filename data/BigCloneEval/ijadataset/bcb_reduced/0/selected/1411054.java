package com.velocityme.session;

import com.velocityme.interfaces.ContactDetailLocal;
import com.velocityme.interfaces.ContactDetailUtil;
import com.velocityme.interfaces.ContactableLocal;
import com.velocityme.interfaces.KeySessionLocal;
import com.velocityme.interfaces.NotificationMechanismLocal;
import com.velocityme.interfaces.NotificationMechanismUtil;
import com.velocityme.interfaces.TaskLocal;
import com.velocityme.utility.InvalidKeyException;
import com.velocityme.utility.ServerConfiguration;
import com.velocityme.valueobjects.NotificationMechanismValue;
import com.velocityme.valueobjects.NotificationMechanismValueToString;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.NamingException;
import org.apache.log4j.Logger;

/**
 *
 * @author  Robert
 * @ejb.bean
 *           type="Stateless"
 *           cmp-version="2.x"
 *           name="NotificationMechanismSession"
 *           jndi-name="ejb/NotificationMechanismSession"
 *           view-type="local"
 *           transaction-type="Container"
 * @ejb.transaction type="Required"
 *
 * @ejb.util generate="physical"
 */
public class NotificationMechanismSessionBean implements SessionBean {

    private SessionContext m_context;

    private Logger m_logger = Logger.getLogger(NotificationMechanismSessionBean.class);

    /**
     * Get the full list of notification mechanism value objects.
     *
     * @param p_key User's security key
     *
     * @ejb.interface-method view-type="local"
     **/
    public com.velocityme.valueobjects.NotificationMechanismValue[] getAllValueObjects(KeySessionLocal p_keyLocal) throws InvalidKeyException {
        NotificationMechanismValue[] values = null;
        try {
            if (p_keyLocal.isValid()) {
                Collection mechanisms = NotificationMechanismUtil.getLocalHome().findAll();
                values = new NotificationMechanismValue[mechanisms.size()];
                int j = 0;
                Iterator i = mechanisms.iterator();
                while (i.hasNext()) {
                    NotificationMechanismLocal mechanismLocal = (NotificationMechanismLocal) i.next();
                    values[j++] = new NotificationMechanismValueToString(mechanismLocal.getNotificationMechanismValue());
                }
            } else throw new InvalidKeyException();
        } catch (NamingException e) {
            throw new EJBException(e);
        } catch (FinderException e) {
            throw new EJBException(e);
        }
        return values;
    }

    private InternetAddress[] getToAddresses(Set p_contactablesLocal) throws NamingException, FinderException, AddressException {
        Set toAddressSet = new HashSet();
        NotificationMechanismLocal emailMechanism = NotificationMechanismUtil.getLocalHome().findByName("Email");
        Iterator i = p_contactablesLocal.iterator();
        while (i.hasNext()) {
            ContactableLocal contactableLocal = (ContactableLocal) i.next();
            Collection emailContactDetails = ContactDetailUtil.getLocalHome().findByContactableAndMechanism(contactableLocal, emailMechanism);
            Iterator k = emailContactDetails.iterator();
            while (k.hasNext()) {
                ContactDetailLocal emailContactDetailLocal = (ContactDetailLocal) k.next();
                toAddressSet.add(new InternetAddress(emailContactDetailLocal.getValue()));
            }
            if (emailContactDetails.isEmpty()) m_logger.warn(contactableLocal.getNodeLocal().getName() + " has no email address");
        }
        InternetAddress[] toAddresses = new InternetAddress[toAddressSet.size()];
        int j = 0;
        i = toAddressSet.iterator();
        while (i.hasNext()) {
            toAddresses[j++] = (InternetAddress) i.next();
        }
        return toAddresses;
    }

    public void internalSendNotificationOfResponsibility(TaskLocal p_taskLocal, Set p_contactablesLocal) {
        URLName url = ServerConfiguration.getInstance().getMailURLName();
        if (url != null) {
            try {
                InternetAddress[] toAddresses = getToAddresses(p_contactablesLocal);
                if (toAddresses.length > 0) {
                    Properties props = System.getProperties();
                    props.put("mail.smtp.host", url.getHost());
                    props.put("mail.smtp.user", url.getUsername());
                    Session session = Session.getDefaultInstance(props, null);
                    session.setPasswordAuthentication(url, new PasswordAuthentication(url.getUsername(), url.getPassword()));
                    MimeMessage msg = new MimeMessage(session);
                    msg.setFrom(ServerConfiguration.getInstance().getMailFromAddress());
                    msg.setRecipients(Message.RecipientType.TO, toAddresses);
                    msg.setSubject("Velocityme responsibility assignment, task ID = " + p_taskLocal.getTaskId().toString());
                    msg.setSentDate(new Date());
                    MimeBodyPart mbp1 = new MimeBodyPart();
                    StringBuffer messageBuffer = new StringBuffer();
                    createResponsibilityMessage(messageBuffer, p_taskLocal);
                    mbp1.setContent(messageBuffer.toString(), "text/html");
                    Multipart mp = new MimeMultipart();
                    mp.addBodyPart(mbp1);
                    msg.setContent(mp);
                    Transport.send(msg);
                }
            } catch (MessagingException e) {
                m_logger.warn("Sending email notification of responsibility failed", e);
            } catch (NamingException e) {
                throw new EJBException(e);
            } catch (FinderException e) {
                throw new EJBException(e);
            }
        }
    }

    /**
     * Send notification of responsibility to each of the contactables.
     *
     * @param p_key User's security key
     *
     * @ejb.interface-method view-type="local"
     **/
    public void sendNotificationOfResponsibility(KeySessionLocal p_keyLocal, final TaskLocal p_taskLocal, final Set p_contactablesLocal) throws InvalidKeyException {
        if (p_keyLocal.isValid()) {
            final Set contactablesLocal = new HashSet(p_contactablesLocal);
            contactablesLocal.remove(p_keyLocal.getUserLocal().getPersonLocal().getContactableLocal());
            new Thread() {

                public void run() {
                    internalSendNotificationOfResponsibility(p_taskLocal, contactablesLocal);
                }
            }.start();
        } else throw new InvalidKeyException();
    }

    public void internalSendNotificationOfInterest(TaskLocal p_taskLocal, Set p_contactablesLocal) {
        URLName url = ServerConfiguration.getInstance().getMailURLName();
        if (url != null) {
            try {
                InternetAddress[] toAddresses = getToAddresses(p_contactablesLocal);
                if (toAddresses.length > 0) {
                    Properties props = System.getProperties();
                    props.put("mail.smtp.host", url.getHost());
                    props.put("mail.smtp.user", url.getUsername());
                    Session session = Session.getDefaultInstance(props, null);
                    session.setPasswordAuthentication(url, new PasswordAuthentication(url.getUsername(), url.getPassword()));
                    MimeMessage msg = new MimeMessage(session);
                    msg.setFrom(ServerConfiguration.getInstance().getMailFromAddress());
                    msg.setRecipients(Message.RecipientType.TO, toAddresses);
                    msg.setSubject("Velocityme update, task ID = " + p_taskLocal.getTaskId().toString() + " has changed.");
                    msg.setSentDate(new Date());
                    MimeBodyPart mbp1 = new MimeBodyPart();
                    StringBuffer messageBuffer = new StringBuffer();
                    createInterestMessage(messageBuffer, p_taskLocal);
                    mbp1.setContent(messageBuffer.toString(), "text/html");
                    Multipart mp = new MimeMultipart();
                    mp.addBodyPart(mbp1);
                    msg.setContent(mp);
                    Transport.send(msg);
                }
            } catch (MessagingException e) {
                m_logger.warn("Sending email notification of interest failed", e);
            } catch (NamingException e) {
                throw new EJBException(e);
            } catch (FinderException e) {
                throw new EJBException(e);
            }
        }
    }

    /**
     * Send notification of interest to each of the contactables.
     *
     * @param p_key User's security key
     *
     * @ejb.interface-method view-type="local"
     **/
    public void sendNotificationOfInterest(KeySessionLocal p_keyLocal, final TaskLocal p_taskLocal, final Set p_contactablesLocal) throws InvalidKeyException {
        if (p_keyLocal.isValid()) {
            final Set contactablesLocal = new HashSet(p_contactablesLocal);
            contactablesLocal.remove(p_keyLocal.getUserLocal().getPersonLocal().getContactableLocal());
            new Thread() {

                public void run() {
                    internalSendNotificationOfInterest(p_taskLocal, contactablesLocal);
                }
            }.start();
        } else throw new InvalidKeyException();
    }

    private void createMessageHead(StringBuffer p_message, String p_title) {
        p_message.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">");
        p_message.append("<HTML><HEAD><TITLE>");
        p_message.append(p_title);
        p_message.append("</TITLE><STYLE>");
        p_message.append("body {font-family: sans-serif; background-color: #e2dfd6; margin: 0; padding: 0;}");
        p_message.append("div.body-content {width: 100%; background-color: white; border-bottom: 1px solid #7f9db9;}");
        p_message.append("h1.layout {width: 100%; text-align: center; padding: 0.1em 0.5em; margin: 0; margin-bottom: 2pt; color: #8ac; background-color: #468; border-top: 1px solid #000; border-bottom: 1px solid #000; font-weight: bold;}}");
        p_message.append("table {background: no-repeat top right; float: left; width: 100%;}");
        p_message.append("th {text-align: right; vertical-align: top; white-space: nowrap; color: #468;}");
        p_message.append("td {width: 100%;}");
        p_message.append("</STYLE></HEAD>");
    }

    private void createMessageFoot(StringBuffer p_message) {
        p_message.append("</HTML>");
    }

    private void createResponsibilityMessage(StringBuffer p_message, TaskLocal p_taskLocal) {
        String title = "Velocityme Responsibility Assignment";
        createMessageHead(p_message, title);
        p_message.append("<body>");
        p_message.append("<div class=\"body-content\">");
        p_message.append("<h1 class=\"layout\">");
        p_message.append(title);
        p_message.append("</h1>");
        p_message.append("<p>You have been assigned responsibility the following task.</p>");
        p_message.append("<table><tr><th>Project Path:</th><td>");
        p_message.append(p_taskLocal.getProjectPathName());
        p_message.append("</td></tr><tr><th>Name:</th><td>");
        p_message.append(p_taskLocal.getNodeLocal().getName());
        p_message.append("</td></tr><tr><th>ID#:</th><td>");
        p_message.append(p_taskLocal.getTaskId());
        p_message.append("</td></tr><tr><th>State:</th><td>");
        p_message.append(p_taskLocal.getStateLocal().getNodeLocal().getName());
        p_message.append("</td></tr></table>");
        URL internetURL = ServerConfiguration.getInstance().getInternetServerURL(p_taskLocal.getTaskId());
        URL intranetURL = ServerConfiguration.getInstance().getIntranetServerURL(p_taskLocal.getTaskId());
        p_message.append("<p>Please follow a link to the task on either the <a href=\"");
        p_message.append(internetURL.toString());
        p_message.append("\">internet</a> or <a href=\"");
        p_message.append(intranetURL.toString());
        p_message.append("\">intranet</a>.</p>");
        p_message.append("</div>");
        p_message.append("</body>");
        createMessageFoot(p_message);
    }

    private void createInterestMessage(StringBuffer p_message, TaskLocal p_taskLocal) {
        String title = "Velocityme Task Update";
        createMessageHead(p_message, title);
        p_message.append("<body>");
        p_message.append("<div class=\"body-content\">");
        p_message.append("<h1 class=\"layout\">");
        p_message.append(title);
        p_message.append("</h1>");
        p_message.append("<p>The following task has been updated.</p>");
        p_message.append("<table><tr><th>Project Path:</th><td>");
        p_message.append(p_taskLocal.getProjectPathName());
        p_message.append("</td></tr><tr><th>Name:</th><td>");
        p_message.append(p_taskLocal.getNodeLocal().getName());
        p_message.append("</td></tr><tr><th>ID#:</th><td>");
        p_message.append(p_taskLocal.getTaskId());
        p_message.append("</td></tr><tr><th>State:</th><td>");
        p_message.append(p_taskLocal.getStateLocal().getNodeLocal().getName());
        p_message.append("</td></tr></table>");
        URL internetURL = ServerConfiguration.getInstance().getInternetServerURL(p_taskLocal.getTaskId());
        URL intranetURL = ServerConfiguration.getInstance().getIntranetServerURL(p_taskLocal.getTaskId());
        p_message.append("<p>Please follow a link to the task on either the <a href=\"");
        p_message.append(internetURL.toString());
        p_message.append("\">internet</a> or <a href=\"");
        p_message.append(intranetURL.toString());
        p_message.append("\">intranet</a>.</p>");
        p_message.append("</div>");
        p_message.append("</body>");
        createMessageFoot(p_message);
    }

    /**
     * Create the Session Bean.
     * @throws CreateException 
     */
    public void ejbCreate() throws CreateException {
    }

    public void ejbActivate() throws java.rmi.RemoteException {
    }

    public void ejbPassivate() throws java.rmi.RemoteException {
    }

    public void ejbRemove() throws java.rmi.RemoteException {
    }

    public void setSessionContext(javax.ejb.SessionContext sessionContext) throws java.rmi.RemoteException {
        m_context = sessionContext;
    }
}
