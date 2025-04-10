package com.liferay.portal.ejb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.mail.internet.InternetAddress;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.dotmarketing.util.Logger;
import com.liferay.mail.ejb.MailManagerUtil;
import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.PortalException;
import com.liferay.portal.RequiredUserException;
import com.liferay.portal.SystemException;
import com.liferay.portal.UserEmailAddressException;
import com.liferay.portal.UserIdException;
import com.liferay.portal.UserPasswordException;
import com.liferay.portal.auth.AuthPipeline;
import com.liferay.portal.auth.Authenticator;
import com.liferay.portal.auth.PrincipalException;
import com.liferay.portal.auth.PrincipalFinder;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.Layout;
import com.liferay.portal.model.Portlet;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.pwd.PwdToolkitUtil;
import com.liferay.portal.util.PropsUtil;
import com.liferay.portlet.admin.ejb.AdminConfigManagerUtil;
import com.liferay.portlet.admin.model.EmailConfig;
import com.liferay.portlet.admin.model.UserConfig;
import com.liferay.util.Encryptor;
import com.liferay.util.EncryptorException;
import com.liferay.util.GetterUtil;
import com.liferay.util.InstancePool;
import com.liferay.util.KeyValuePair;
import com.liferay.util.StringUtil;
import com.liferay.util.Validator;
import com.liferay.util.mail.MailMessage;

/**
 * <a href="UserManagerImpl.java.html"><b><i>View Source</i></b></a>
 *
 * @author  Brian Wing Shun Chan
 * @version $Revision: 1.3 $
 *
 */
public class UserManagerImpl extends PrincipalBean implements UserManager {

    public boolean addGroup(String userId, Group group) throws PortalException, SystemException {
        if (!hasAdmin(userId)) {
            throw new PrincipalException();
        }
        return UserUtil.addGroup(userId, group);
    }

    public boolean addRole(String userId, Role role) throws PortalException, SystemException {
        if (!hasAdmin(userId)) {
            throw new PrincipalException();
        }
        return UserUtil.addRole(userId, role);
    }

    public User addUser(String companyId, boolean autoUserId, String userId, boolean autoPassword, String password1, String password2, boolean passwordReset, String firstName, String middleName, String lastName, String nickName, boolean male, Date birthday, String emailAddress, Locale locale) throws PortalException, SystemException {
        Company company = CompanyUtil.findByPrimaryKey(companyId);
        if (!company.isStrangers() && !hasAdministrator(companyId)) {
            throw new PrincipalException();
        }
        return UserLocalManagerUtil.addUser(companyId, autoUserId, userId, autoPassword, password1, password2, passwordReset, firstName, middleName, lastName, nickName, male, birthday, emailAddress, locale);
    }

    public int authenticateByEmailAddress(String companyId, String emailAddress, String password) throws PortalException, SystemException {
        return _authenticate(companyId, emailAddress, password, true);
    }

    public int authenticateByUserId(String companyId, String userId, String password) throws PortalException, SystemException {
        return _authenticate(companyId, userId, password, false);
    }

    public KeyValuePair decryptUserId(String companyId, String userId, String password) throws PortalException, SystemException {
        Company company = CompanyUtil.findByPrimaryKey(companyId);
        try {
            userId = Encryptor.decrypt(company.getKeyObj(), userId);
        } catch (EncryptorException ee) {
            throw new SystemException(ee);
        }
        String liferayUserId = userId;
        try {
            PrincipalFinder principalFinder = (PrincipalFinder) InstancePool.get(PropsUtil.get(PropsUtil.PRINCIPAL_FINDER));
            liferayUserId = principalFinder.toLiferay(userId);
        } catch (Exception e) {
        }
        User user = UserUtil.findByPrimaryKey(liferayUserId);
        try {
            password = Encryptor.decrypt(company.getKeyObj(), password);
        } catch (EncryptorException ee) {
            throw new SystemException(ee);
        }
        String encPwd = Encryptor.digest(password);
        if (user.getPassword().equals(encPwd)) {
            if (user.isPasswordExpired()) {
                user.setPasswordReset(true);
                UserUtil.update(user);
            }
            return new KeyValuePair(userId, password);
        } else {
            throw new PrincipalException();
        }
    }

    public boolean deleteGroup(String userId, Group group) throws PortalException, SystemException {
        if (!hasAdmin(userId)) {
            throw new PrincipalException();
        }
        return UserUtil.removeGroup(userId, group);
    }

    public boolean deleteRole(String userId, Role role) throws PortalException, SystemException {
        if (!hasAdmin(userId)) {
            throw new PrincipalException();
        }
        return UserUtil.removeRole(userId, role);
    }

    public void deleteUser(String userId) throws PortalException, SystemException {
        if (!hasAdmin(userId)) {
            throw new PrincipalException();
        }
        if (getUserId().equals(userId)) {
            throw new RequiredUserException();
        }
        UserLocalManagerUtil.deleteUser(userId);
    }

    public String encryptUserId(String userId) throws PortalException, SystemException {
        userId = userId.trim().toLowerCase();
        String liferayUserId = userId;
        try {
            PrincipalFinder principalFinder = (PrincipalFinder) InstancePool.get(PropsUtil.get(PropsUtil.PRINCIPAL_FINDER));
            liferayUserId = principalFinder.toLiferay(userId);
        } catch (Exception e) {
        }
        User user = UserUtil.findByPrimaryKey(liferayUserId);
        Company company = CompanyUtil.findByPrimaryKey(user.getCompanyId());
        try {
            return Encryptor.encrypt(company.getKeyObj(), userId);
        } catch (EncryptorException ee) {
            throw new SystemException(ee);
        }
    }

    public List findByAnd_C_FN_MN_LN_EA_M_BD_IM_A(String firstName, String middleName, String lastName, String emailAddress, Boolean male, Date age1, Date age2, String im, String street1, String street2, String city, String state, String zip, String phone, String fax, String cell) throws PortalException, SystemException {
        return UserFinder.findByAnd_C_FN_MN_LN_EA_M_BD_IM_A(getUser().getCompanyId(), firstName, middleName, lastName, emailAddress, male, age1, age2, im, street1, street2, city, state, zip, phone, fax, cell);
    }

    public List findByC_SMS() throws PortalException, SystemException {
        return UserFinder.findByC_SMS(getUser().getCompanyId());
    }

    public List findByOr_C_FN_MN_LN_EA_M_BD_IM_A(String firstName, String middleName, String lastName, String emailAddress, Boolean male, Date age1, Date age2, String im, String street1, String street2, String city, String state, String zip, String phone, String fax, String cell) throws PortalException, SystemException {
        return UserFinder.findByOr_C_FN_MN_LN_EA_M_BD_IM_A(getUser().getCompanyId(), firstName, middleName, lastName, emailAddress, male, age1, age2, im, street1, street2, city, state, zip, phone, fax, cell);
    }

    public List getAllLayouts(String userId) throws PortalException, SystemException {
        List layouts = new ArrayList();
        layouts.addAll(getLayouts(userId));
        Iterator itr = getGroups(userId).iterator();
        while (itr.hasNext()) {
            Group group = (Group) itr.next();
            layouts.addAll(GroupManagerUtil.getLayouts(group.getGroupId()));
        }
        return layouts;
    }

    public String getCompanyId(String userId) throws PortalException, SystemException {
        User user = UserUtil.findByPrimaryKey(userId);
        return user.getCompanyId();
    }

    public User getDefaultUser(String companyId) throws PortalException, SystemException {
        return UserLocalManagerUtil.getDefaultUser(companyId);
    }

    public List getGroups(String userId) throws PortalException, SystemException {
        return UserUtil.getGroups(userId);
    }

    public List getGroups(String userId, int begin, int end) throws PortalException, SystemException {
        return UserUtil.getGroups(userId, begin, end);
    }

    public int getGroupsSize(String userId) throws SystemException {
        return UserUtil.getGroupsSize(userId);
    }

    public List getLayouts(String userId) throws PortalException, SystemException {
        User user = UserUtil.findByPrimaryKey(userId);
        List layouts = new ArrayList();
        String[] layoutIds = StringUtil.split(user.getLayoutIds());
        for (int i = 0; i < layoutIds.length; i++) {
            Layout layout = LayoutUtil.findByPrimaryKey(new LayoutPK(layoutIds[i], userId));
            if (i == 0) {
                layout.setDefaultLayout(true);
            } else {
                layout.setDefaultLayout(false);
            }
            layouts.add(layout);
        }
        return layouts;
    }

    public List getRoles(String userId) throws PortalException, SystemException {
        return UserUtil.getRoles(userId);
    }

    public List getRoles(String userId, int begin, int end) throws PortalException, SystemException {
        return UserUtil.getRoles(userId, begin, end);
    }

    public int getRolesSize(String userId) throws SystemException {
        return UserUtil.getRolesSize(userId);
    }

    public User getUserByEmailAddress(String emailAddress) throws PortalException, SystemException {
        emailAddress = emailAddress.trim().toLowerCase();
        User user = UserUtil.findByC_EA(getUser().getCompanyId(), emailAddress);
        if (getUserId().equals(user.getUserId()) || hasAdministrator(user.getCompanyId())) {
            return user;
        } else {
            return (User) user.getProtected();
        }
    }

    public User getUserById(String userId) throws PortalException, SystemException {
        userId = userId.trim().toLowerCase();
        User user = UserUtil.findByPrimaryKey(userId);
        if (getUserId().equals(userId) || hasAdministrator(user.getCompanyId())) {
            return user;
        } else {
            return (User) user.getProtected();
        }
    }

    public User getUserById(String companyId, String userId) throws PortalException, SystemException {
        userId = userId.trim().toLowerCase();
        User user = UserUtil.findByC_U(companyId, userId);
        if (getUserId().equals(userId) || hasAdministrator(user.getCompanyId())) {
            return user;
        } else {
            return (User) user.getProtected();
        }
    }

    public String getUserId(String companyId, String emailAddress) throws PortalException, SystemException {
        emailAddress = emailAddress.trim().toLowerCase();
        User user = UserUtil.findByC_EA(companyId, emailAddress);
        return user.getUserId();
    }

    public boolean hasRedoUpdateSkin() throws PortalException, SystemException {
        return UserLocalManagerUtil.hasRedoUpdateSkin(getUserId());
    }

    public boolean hasUndoUpdateSkin() throws PortalException, SystemException {
        return UserLocalManagerUtil.hasUndoUpdateSkin(getUserId());
    }

    public int notifyNewUsers() throws PortalException, SystemException {
        String companyId = getUser().getCompanyId();
        if (!hasAdministrator(companyId)) {
            throw new PrincipalException();
        }
        UserConfig userConfig = AdminConfigManagerUtil.getUserConfig(companyId);
        EmailConfig registrationEmail = userConfig.getRegistrationEmail();
        if (registrationEmail == null || !registrationEmail.isSend()) {
            return 0;
        }
        Company company = CompanyUtil.findByPrimaryKey(companyId);
        String adminName = company.getAdminName();
        String subject = registrationEmail.getSubject();
        String body = registrationEmail.getBody();
        List users = UserUtil.findByC_P(companyId, "password");
        for (int i = 0; i < users.size(); i++) {
            User user = (User) users.get(i);
            user.setPassword(PwdToolkitUtil.generate());
            UserUtil.update(user);
            subject = StringUtil.replace(subject, new String[] { "[$ADMIN_EMAIL_ADDRESS$]", "[$ADMIN_NAME$]", "[$COMPANY_MX$]", "[$COMPANY_NAME$]", "[$PORTAL_URL$]", "[$USER_EMAIL_ADDRESS$]", "[$USER_NAME$]", "[$USER_PASSWORD$]" }, new String[] { company.getEmailAddress(), adminName, company.getMx(), company.getName(), company.getPortalURL(), user.getEmailAddress(), user.getFullName(), user.getPassword() });
            body = StringUtil.replace(body, new String[] { "[$ADMIN_EMAIL_ADDRESS$]", "[$ADMIN_NAME$]", "[$COMPANY_MX$]", "[$COMPANY_NAME$]", "[$PORTAL_URL$]", "[$USER_EMAIL_ADDRESS$]", "[$USER_NAME$]", "[$USER_PASSWORD$]" }, new String[] { company.getEmailAddress(), adminName, company.getMx(), company.getName(), company.getPortalURL(), user.getEmailAddress(), user.getFullName(), user.getPassword() });
            try {
                MailManagerUtil.sendEmail(new MailMessage(new InternetAddress(company.getEmailAddress(), adminName), new InternetAddress(user.getEmailAddress(), user.getFullName()), subject, body));
            } catch (IOException ioe) {
                throw new SystemException(ioe);
            }
        }
        return users.size();
    }

    public User redoUpdateSkin() throws PortalException, SystemException {
        return UserLocalManagerUtil.redoUpdateSkin(getUserId());
    }

    public void sendPassword(String companyId, String emailAddress) throws PortalException, SystemException {
        emailAddress = emailAddress.trim().toLowerCase();
        if (!Validator.isEmailAddress(emailAddress)) {
            throw new UserEmailAddressException();
        }
        User user = UserUtil.findByC_EA(companyId, emailAddress);
        user.setPassword(PwdToolkitUtil.generate());
        user.setPasswordEncrypted(false);
        user.setPasswordReset(GetterUtil.getBoolean(PropsUtil.get(PropsUtil.PASSWORDS_CHANGE_ON_FIRST_USE)));
        UserUtil.update(user);
        Company company = CompanyUtil.findByPrimaryKey(companyId);
        String adminName = company.getAdminName();
        StringBuffer body = new StringBuffer();
        body.append("Your new password is -- ");
        body.append(user.getPassword()).append(".\n\n");
        body.append("The portal is located at http://");
        body.append(company.getPortalURL()).append(".\n\n");
        body.append("Please use ").append(emailAddress);
        body.append(" as your login.");
        try {
            MailManagerUtil.sendEmail(new MailMessage(new InternetAddress(company.getEmailAddress(), adminName), new InternetAddress(emailAddress, user.getFullName()), company.getName() + " Password Assistance", body.toString()));
        } catch (IOException ioe) {
            throw new SystemException(ioe);
        }
    }

    public void setGroups(String userId, String[] groupIds) throws PortalException, SystemException {
        if (!hasAdmin(userId)) {
            throw new PrincipalException();
        }
        UserUtil.setGroups(userId, groupIds);
    }

    public void setLayouts(String userId, String[] layoutIds) throws PortalException, SystemException {
        User user = UserUtil.findByPrimaryKey(userId);
        if (!getUserId().equals(userId) && !hasAdministrator(user.getCompanyId())) {
            throw new PrincipalException();
        }
        UserLocalManagerUtil.setLayouts(userId, layoutIds);
    }

    public void setRoles(String userId, String[] roleIds) throws PortalException, SystemException {
        if (!hasAdmin(userId)) {
            throw new PrincipalException();
        }
        UserUtil.setRoles(userId, roleIds);
    }

    public void test() {
        String userId = null;
        try {
            userId = getUserId();
        } catch (Exception e) {
            Logger.error(this, e.getMessage(), e);
        }
        _log.info(userId);
    }

    public User undoUpdateSkin() throws PortalException, SystemException {
        return UserLocalManagerUtil.undoUpdateSkin(getUserId());
    }

    public User updateActive(String userId, boolean active) throws PortalException, SystemException {
        userId = userId.trim().toLowerCase();
        User user = UserUtil.findByPrimaryKey(userId);
        if (!hasAdministrator(user.getCompanyId())) {
            throw new PrincipalException();
        }
        if (active == false && getUserId().equals(userId)) {
            throw new RequiredUserException();
        }
        user.setActive(active);
        UserUtil.update(user);
        return user;
    }

    public User updateAgreedToTermsOfUse(boolean agreedToTermsOfUse) throws PortalException, SystemException {
        User user = UserUtil.findByPrimaryKey(getUserId());
        user.setAgreedToTermsOfUse(agreedToTermsOfUse);
        UserUtil.update(user);
        return user;
    }

    public User updateLastLogin(String loginIP) throws PortalException, SystemException {
        User user = UserUtil.findByPrimaryKey(getUserId());
        if (user.getLoginDate() == null && user.getLastLoginDate() == null) {
            boolean universalPersonalization = GetterUtil.get(PropsUtil.get(PropsUtil.UNIVERSAL_PERSONALIZATION), false);
            boolean powerUser = true;
            if (!universalPersonalization) {
                powerUser = RoleLocalManagerUtil.isPowerUser(user.getUserId());
            }
            if (powerUser) {
                Group group = GroupLocalManagerUtil.getGroupByName(user.getCompanyId(), Group.GENERAL_USER);
                Layout groupDefaultLayout = group.getDefaultLayout();
                String[] portletIds = groupDefaultLayout.getPortletIds();
                List defaultPortletIds = new ArrayList();
                for (int i = 0; i < portletIds.length; i++) {
                    try {
                        Portlet portlet = PortletManagerUtil.getPortletById(user.getCompanyId(), portletIds[i]);
                        if (RoleLocalManagerUtil.hasRoles(getUserId(), StringUtil.split(portlet.getRoles()))) {
                            defaultPortletIds.add(portlet.getPortletId());
                        }
                    } catch (Exception e) {
                    }
                }
                Layout userDefaultLayout = LayoutLocalManagerUtil.addUserLayout(user.getUserId(), groupDefaultLayout.getName(), (String[]) defaultPortletIds.toArray(new String[0]));
                LayoutLocalManagerUtil.updateLayout(userDefaultLayout.getPrimaryKey(), groupDefaultLayout.getName(), groupDefaultLayout.getColumnOrder(), groupDefaultLayout.getNarrow1(), groupDefaultLayout.getNarrow2(), groupDefaultLayout.getWide(), groupDefaultLayout.getStateMax(), groupDefaultLayout.getStateMin(), groupDefaultLayout.getModeEdit(), groupDefaultLayout.getModeHelp());
            }
        }
        user.setLastLoginDate(user.getLoginDate());
        user.setLastLoginIP(user.getLoginIP());
        user.setLoginDate(new Date());
        user.setLoginIP(loginIP);
        user.setFailedLoginAttempts(0);
        UserUtil.update(user);
        return user;
    }

    public void updatePortrait(String userId, byte[] bytes) throws PortalException, SystemException {
        userId = userId.trim().toLowerCase();
        if (!getUserId().equals(userId) && !hasAdmin(userId)) {
            throw new PrincipalException();
        }
        ImageLocalUtil.put(userId, bytes);
    }

    public User updateSkin(String skinId) throws PortalException, SystemException {
        return UserLocalManagerUtil.updateSkin(getUserId(), skinId);
    }

    public User updateUser(String userId, String password1, String password2, boolean passwordReset) throws PortalException, SystemException {
        User user = UserUtil.findByPrimaryKey(userId);
        if (!getUserId().equals(userId) && !hasAdministrator(user.getCompanyId())) {
            throw new PrincipalException();
        }
        return UserLocalManagerUtil.updateUser(userId, password1, password2, passwordReset);
    }

    public User updateUser(String userId, String password, String firstName, String middleName, String lastName, String nickName, boolean male, Date birthday, String emailAddress, String smsId, String aimId, String icqId, String msnId, String ymId, String favoriteActivity, String favoriteBibleVerse, String favoriteFood, String favoriteMovie, String favoriteMusic, String languageId, String timeZoneId, String skinId, boolean dottedSkins, boolean roundedSkins, String greeting, String resolution, String refreshRate, String comments) throws PortalException, SystemException {
        User user = UserUtil.findByPrimaryKey(userId);
        if (!getUserId().equals(userId) && !hasAdministrator(user.getCompanyId())) {
            throw new PrincipalException();
        }
        return UserLocalManagerUtil.updateUser(userId, password, firstName, middleName, lastName, nickName, male, birthday, emailAddress, smsId, aimId, icqId, msnId, ymId, favoriteActivity, favoriteBibleVerse, favoriteFood, favoriteMovie, favoriteMusic, languageId, timeZoneId, skinId, dottedSkins, roundedSkins, greeting, resolution, refreshRate, comments);
    }

    public boolean hasAdmin(String userId) throws PortalException, SystemException {
        User user = UserUtil.findByPrimaryKey(userId);
        if (hasAdministrator(user.getCompanyId())) {
            return true;
        } else {
            return false;
        }
    }

    private int _authenticate(String companyId, String login, String password, boolean byEmailAddress) throws PortalException, SystemException {
        login = login.trim().toLowerCase();
        if (byEmailAddress) {
            if (!Validator.isEmailAddress(login)) {
                throw new UserEmailAddressException();
            }
        } else {
            if (Validator.isNull(login)) {
                throw new UserIdException();
            }
        }
        if (Validator.isNull(password)) {
            throw new UserPasswordException(UserPasswordException.PASSWORD_INVALID);
        }
        int authResult = Authenticator.FAILURE;
        if (byEmailAddress) {
            authResult = AuthPipeline.authenticateByEmailAddress(PropsUtil.getArray(PropsUtil.AUTH_PIPELINE_PRE), companyId, login, password);
        } else {
            authResult = AuthPipeline.authenticateByUserId(PropsUtil.getArray(PropsUtil.AUTH_PIPELINE_PRE), companyId, login, password);
        }
        User user = null;
        try {
            if (byEmailAddress) {
                user = UserUtil.findByC_EA(companyId, login);
            } else {
                user = UserUtil.findByC_U(companyId, login);
            }
        } catch (NoSuchUserException nsue) {
            return Authenticator.DNE;
        }
        if (!user.isPasswordEncrypted()) {
            user.setPassword(Encryptor.digest(user.getPassword()));
            user.setPasswordEncrypted(true);
            user.setPasswordReset(GetterUtil.getBoolean(PropsUtil.get(PropsUtil.PASSWORDS_CHANGE_ON_FIRST_USE)));
            UserUtil.update(user);
        } else if (user.isPasswordExpired()) {
            user.setPasswordReset(true);
            UserUtil.update(user);
        }
        if (authResult == Authenticator.SUCCESS) {
            String encPwd = Encryptor.digest(password);
            if (user.getPassword().equals(encPwd)) {
                authResult = Authenticator.SUCCESS;
            } else {
                authResult = Authenticator.FAILURE;
            }
        }
        if (authResult == Authenticator.SUCCESS) {
            if (byEmailAddress) {
                authResult = AuthPipeline.authenticateByEmailAddress(PropsUtil.getArray(PropsUtil.AUTH_PIPELINE_POST), companyId, login, password);
            } else {
                authResult = AuthPipeline.authenticateByUserId(PropsUtil.getArray(PropsUtil.AUTH_PIPELINE_POST), companyId, login, password);
            }
        }
        if (authResult == Authenticator.FAILURE) {
            try {
                if (byEmailAddress) {
                    AuthPipeline.onFailureByEmailAddress(PropsUtil.getArray(PropsUtil.AUTH_FAILURE), companyId, login);
                } else {
                    AuthPipeline.onFailureByUserId(PropsUtil.getArray(PropsUtil.AUTH_FAILURE), companyId, login);
                }
                int failedLoginAttempts = user.getFailedLoginAttempts();
                user.setFailedLoginAttempts(++failedLoginAttempts);
                UserUtil.update(user);
                int maxFailures = GetterUtil.get(PropsUtil.get(PropsUtil.AUTH_MAX_FAILURES_LIMIT), 0);
                if ((failedLoginAttempts >= maxFailures) && (maxFailures != 0)) {
                    if (byEmailAddress) {
                        AuthPipeline.onMaxFailuresByEmailAddress(PropsUtil.getArray(PropsUtil.AUTH_MAX_FAILURES), companyId, login);
                    } else {
                        AuthPipeline.onMaxFailuresByUserId(PropsUtil.getArray(PropsUtil.AUTH_MAX_FAILURES), companyId, login);
                    }
                }
            } catch (Exception e) {
                Logger.error(this, e.getMessage(), e);
            }
        }
        return authResult;
    }

    private static final Log _log = LogFactory.getLog(UserManagerImpl.class);
}
