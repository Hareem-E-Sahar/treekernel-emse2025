package de.beas.explicanto.server;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.log4j.Logger;
import de.beas.explicanto.types.WSRoleElem;
import de.beas.explicanto.types.WSRoleType;
import de.beas.explicanto.types.WSUser;

/**
 * NotificationManager
 *
 * @author marius.staicu
 * @version 1.0
 *
 */
public class NotificationManager {

    private Logger logger;

    private SysPropertiesMgr props;

    private Persistency persistency;

    private ExplicantoServerProperties esp;

    private final String NOTIFICATION_ENABLE = "Notification enable";

    private final String NOTIFICATION_USER_NEW_USER_ADD = "Notification new user add";

    private final String NOTIFICATION_USER_NEW_USER_PASSWORD = "Notification new user password";

    private final String NOTIFICATION_REMOVE_USER = "Notification remove user";

    private final String NOTIFICATION_ROLE_COURSE_ADD_USER = "Notification role add course user";

    private final String NOTIFICATION_ROLE_COURSE_REMOVE_USER = "Notification role remove course user";

    private final String NOTIFICATION_ROLE_PROJECT_ADD_USER = "Notification role add project user";

    private final String NOTIFICATION_ROLE_PROJECT_REMOVE_USER = "Notification role remove project user";

    private final String MACRO_FIRSTNAME = "xxxFIRSTNAMExxx";

    private final String MACRO_LASTNAME = "xxxLASTNAMExxx";

    private final String MACRO_USERNAME = "xxxUSERNAMExxx";

    private final String MACRO_EMAIL = "xxxEMAILxxx";

    private final String MACRO_PASSWORD = "xxxPASSWORDxxx";

    private final String MACRO_ROLENAME = "xxxROLENAMExxx";

    private final String MACRO_PROJECTNAME = "xxxPROJECTNAMExxx";

    private final String MACRO_COURSENAME = "xxxCOURSENAMExxx";

    public NotificationManager(ExplicantoServerProperties e, Persistency p) {
        logger = Logger.getLogger(NotificationManager.class);
        persistency = p;
        esp = e;
    }

    protected void setProps(SysPropertiesMgr props) {
        this.props = props;
    }

    public void sendEmail(String to, String subject, String body) {
        sendEmail(to, "", subject, body);
    }

    public void sendEmail(String address, String replyTo, String subject, String body) {
        Session session = Session.getDefaultInstance(props.getMailProperties(), null);
        MimeMessage message = new MimeMessage(session);
        try {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(address));
            if (replyTo != null && (!replyTo.equals(""))) {
                message.addFrom(new InternetAddress[] { new InternetAddress(replyTo) });
                message.addHeader("Reply-To", replyTo);
            }
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
        } catch (MessagingException ex) {
            logger.error("NotificationManager: Cannot send email to '" + address + "' with subject '" + subject + "'");
            ex.printStackTrace();
        }
    }

    /**
     * 
     * Send notification emails to all the new added users
     *
     * @param newUsers list with the users add in the system
     */
    public void notifyNewUsers(List newUsers) {
        try {
            if (props.getValBool(NOTIFICATION_ENABLE, false).booleanValue()) {
                for (Iterator iter = newUsers.iterator(); iter.hasNext(); ) {
                    WSUser u = (WSUser) iter.next();
                    String to = u.getEmail();
                    if (to.equals("")) continue;
                    Hashtable macros = new Hashtable();
                    macros.put(MACRO_FIRSTNAME, u.getFirstName());
                    macros.put(MACRO_LASTNAME, u.getLastName());
                    macros.put(MACRO_USERNAME, u.getUsername());
                    macros.put(MACRO_EMAIL, u.getEmail());
                    String content = props.getContentString(NOTIFICATION_USER_NEW_USER_ADD);
                    int p1 = content.indexOf("\n");
                    String subject = content.substring(0, p1);
                    content = content.substring(p1 + 1);
                    subject = macroSubstitution(subject, macros);
                    content = macroSubstitution(content, macros);
                    if (props.getValBool(NOTIFICATION_USER_NEW_USER_ADD, false).booleanValue()) {
                        sendEmail(to, subject, content);
                    }
                    macros.put(MACRO_PASSWORD, XPCUtil.generatePassword(u));
                    content = props.getContentString(NOTIFICATION_USER_NEW_USER_PASSWORD);
                    p1 = content.indexOf("\n");
                    subject = content.substring(0, p1);
                    content = content.substring(p1 + 1);
                    subject = macroSubstitution(subject, macros);
                    content = macroSubstitution(content, macros);
                    if (props.getValBool(NOTIFICATION_USER_NEW_USER_PASSWORD, false).booleanValue()) {
                        sendEmail(to, subject, content);
                    }
                }
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    /**
     * 
     * Send notification emails to all deleted users
     *
     * @param dbUsers list with the users that will be deleted
     */
    public void notifyRemoveUsers(List dbUsers) {
        try {
            if (!props.getValBool(NOTIFICATION_ENABLE, false).booleanValue()) return;
            for (Iterator iter = dbUsers.iterator(); iter.hasNext(); ) {
                WSUser u = (WSUser) iter.next();
                String to = u.getEmail();
                if (to.equals("")) continue;
                Hashtable macros = new Hashtable();
                macros.put(MACRO_FIRSTNAME, u.getFirstName());
                macros.put(MACRO_LASTNAME, u.getLastName());
                macros.put(MACRO_USERNAME, u.getUsername());
                macros.put(MACRO_EMAIL, u.getEmail());
                String content = props.getContentString(NOTIFICATION_REMOVE_USER);
                int p1 = content.indexOf("\n");
                String subject = content.substring(0, p1);
                content = content.substring(p1 + 1);
                subject = macroSubstitution(subject, macros);
                content = macroSubstitution(content, macros);
                if (props.getValBool(NOTIFICATION_REMOVE_USER, false).booleanValue()) sendEmail(to, subject, content);
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    public void notifyRoleAddUser(List roles, int type, String tag) {
        String n;
        if (type == 0) n = NOTIFICATION_ROLE_PROJECT_ADD_USER; else n = NOTIFICATION_ROLE_COURSE_ADD_USER;
        notifyRoleUsers(roles, n, type, tag);
    }

    public void notifyRoleRemoveUser(List roles, int type, String tag) {
        String n;
        if (type == 0) n = NOTIFICATION_ROLE_PROJECT_REMOVE_USER; else n = NOTIFICATION_ROLE_COURSE_REMOVE_USER;
        notifyRoleUsers(roles, n, type, tag);
    }

    private void notifyRoleUsers(List roles, String notification, int type, String tag) {
        try {
            if (!props.getValBool(NOTIFICATION_ENABLE, false).booleanValue()) return;
            List xpcRoles = XPCUtil.getRoleTypesFromProps(esp);
            for (Iterator iter = roles.iterator(); iter.hasNext(); ) {
                WSRoleElem role = (WSRoleElem) iter.next();
                WSUser u = persistency.loadUserNoSecrets(role.getUserID());
                String to = u.getEmail();
                if (to.equals("")) continue;
                WSRoleType rt = getRoleType(xpcRoles, role.getRoleID());
                if (rt == null) continue;
                Hashtable macros = new Hashtable();
                macros.put(MACRO_FIRSTNAME, u.getFirstName());
                macros.put(MACRO_LASTNAME, u.getLastName());
                macros.put(MACRO_USERNAME, u.getUsername());
                macros.put(MACRO_EMAIL, u.getEmail());
                macros.put(MACRO_ROLENAME, rt.getRoleName());
                if (type == 0) macros.put(MACRO_PROJECTNAME, tag); else if (type == 1) macros.put(MACRO_COURSENAME, tag); else logger.error("NotificationManager: unsupported notification type ! (" + Integer.toString(type) + ")(" + notification + ")");
                logger.info("NotificationManager: (" + to + ")(" + Integer.toString(type) + ")(" + tag + ")");
                String content = props.getContentString(notification);
                int p1 = content.indexOf("\n");
                String subject = content.substring(0, p1);
                content = content.substring(p1 + 1);
                subject = macroSubstitution(subject, macros);
                content = macroSubstitution(content, macros);
                if (props.getValBool(notification, false).booleanValue()) sendEmail(to, subject, content);
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    private WSRoleType getRoleType(List roles, long roleId) {
        for (Iterator i = roles.iterator(); i.hasNext(); ) {
            WSRoleType rt = (WSRoleType) i.next();
            if (rt.getRoleID() == roleId) return rt;
        }
        return null;
    }

    private String macroSubstitution(String original, Hashtable macros) {
        String result = original;
        for (Iterator iter = macros.keySet().iterator(); iter.hasNext(); ) {
            String key = (String) iter.next();
            String value = (String) macros.get(key);
            result = result.replaceAll(key, value);
        }
        return result;
    }
}
