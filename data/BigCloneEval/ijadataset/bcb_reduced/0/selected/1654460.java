package com.liferay.mail.util;

import com.liferay.portal.util.PropsUtil;
import com.liferay.util.Encryptor;
import com.liferay.util.LDAPUtil;
import com.liferay.util.StringUtil;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

/**
 * <a href="RHEMSHook.java.html"><b><i>View Source</i></b></a>
 *
 * @author  Toma Bedolla
 * @author  Kevin Verde
 *
 */
public class RHEMSHook implements Hook {

    static Hashtable env = new Hashtable();

    static {
        env.put(Context.INITIAL_CONTEXT_FACTORY, PropsUtil.get(PropsUtil.MAIL_HOOK_RHEMS_LDAP_INITIAL_CONTEXT_FACTORY));
        env.put(Context.PROVIDER_URL, PropsUtil.get(PropsUtil.MAIL_HOOK_RHEMS_LDAP_SERVER_CONTEXT));
        env.put(Context.SECURITY_AUTHENTICATION, PropsUtil.get(PropsUtil.MAIL_HOOK_RHEMS_LDAP_SECURITY_AUTHENTICATION));
        env.put(Context.SECURITY_PRINCIPAL, PropsUtil.get(PropsUtil.MAIL_HOOK_RHEMS_LDAP_SECURITY_PRINCIPAL));
        env.put(Context.SECURITY_CREDENTIALS, PropsUtil.get(PropsUtil.MAIL_HOOK_RHEMS_LDAP_SECURITY_CREDENTIALS));
    }

    public void addForward(String userId, List emailAddresses) {
    }

    public void addUser(String userId, String password, String firstName, String middleName, String lastName, String emailAddress) {
        String addUserURL = PropsUtil.get(PropsUtil.MAIL_HOOK_RHEMS_ADD_USER);
        addUserURL = StringUtil.replace(addUserURL, "%1%", userId);
        addUserURL = StringUtil.replace(addUserURL, "%2%", password);
        if (addUserURL.indexOf('_') != -1) {
            String givenName = userId.substring(0, userId.indexOf('_'));
            addUserURL = StringUtil.replace(addUserURL, "%3%", givenName.replaceFirst(givenName.substring(0, 1), givenName.substring(0, 1).toUpperCase()));
            String sn = userId.substring(userId.indexOf('_') + 1);
            addUserURL = StringUtil.replace(addUserURL, "%4%", sn.replaceFirst(sn.substring(0, 1), sn.substring(0, 1).toUpperCase()));
            String alias = userId.replace('_', '.');
            addUserURL = StringUtil.replace(addUserURL, "%5%", alias);
        } else {
            addUserURL = StringUtil.replace(addUserURL, "%3%", "N%20/");
            addUserURL = StringUtil.replace(addUserURL, "%4%", "A");
            addUserURL = StringUtil.replace(addUserURL, "%5%", "none");
        }
        _userAction(addUserURL);
    }

    public void addVacationMessage(String userId, String emailAddress, String vacationMessage) {
    }

    public void deleteEmailAddress(String userId) {
    }

    public void deleteUser(String userId) {
        String deleteUserURL = PropsUtil.get(PropsUtil.MAIL_HOOK_RHEMS_DELETE_USER);
        deleteUserURL = StringUtil.replace(deleteUserURL, "%1%", userId);
        _userAction(deleteUserURL);
    }

    public void updateBlocked(String userId, List blocked) {
    }

    public void updateEmailAddress(String userId, String emailAddress) {
        DirContext ctx = LDAPUtil.getDirContext(env);
        try {
            ModificationItem[] mods = new ModificationItem[1];
            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("rfc822MailAlias", emailAddress.substring(0, emailAddress.indexOf('@'))));
            ctx.modifyAttributes("uid=" + userId, mods);
        } catch (NamingException ne) {
            ne.printStackTrace();
        }
    }

    public void updatePassword(String userId, String password) {
        DirContext ctx = LDAPUtil.getDirContext(env);
        String encryptedPassword = Encryptor.digest(password);
        try {
            ModificationItem[] mods = new ModificationItem[1];
            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("userPassword", "{SHA}" + encryptedPassword));
            ctx.modifyAttributes("uid=" + userId, mods);
        } catch (NamingException ne) {
        }
    }

    private void _userAction(String urlAction) {
        try {
            URL url = new URL(PropsUtil.get(PropsUtil.MAIL_HOOK_RHEMS_WEB_SERVER) + PropsUtil.get(PropsUtil.MAIL_HOOK_RHEMS_LOGIN));
            URLConnection conn = url.openConnection();
            HashMap headerHashMap = new HashMap(conn.getHeaderFields());
            if (headerHashMap.containsKey("Set-Cookie")) {
                String cookieValue = headerHashMap.get("Set-Cookie").toString();
                cookieValue = cookieValue.substring(1, cookieValue.indexOf(']'));
                url = new URL(PropsUtil.get(PropsUtil.MAIL_HOOK_RHEMS_WEB_SERVER) + urlAction);
                conn = url.openConnection();
                conn.setRequestProperty("Cookie", cookieValue);
                conn.connect();
                conn.getInputStream().close();
            }
        } catch (MalformedURLException me) {
            me.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
