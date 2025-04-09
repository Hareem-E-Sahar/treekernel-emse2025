package lif.webclient.controller.userregister;

import lif.core.dao.DataAccessException;
import lif.core.domain.SecurityRole;
import lif.core.service.ProfileService;
import lif.core.service.UserDoesNotExistException;
import lif.core.util.SystemUtil;
import lif.webclient.service.LifUserService;
import lif.webclient.view.UserBean;
import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractWizardFormController;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: niranjan
 * Date: Apr 23, 2008
 * Time: 12:19:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class RegisterController extends AbstractWizardFormController {

    private LifUserService lifUserService;

    private ProfileService profileService;

    Logger logger = Logger.getLogger(RegisterController.class);

    boolean isDebugEnabled = logger.isDebugEnabled();

    protected ModelAndView processFinish(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object object, BindException bindException) throws Exception {
        UserBean user = (UserBean) object;
        debug("new user creation [" + user.toString() + "]");
        sendMailToAdmin(user);
        Map<String, String> myModel = new HashMap<String, String>();
        String now = (new java.util.Date()).toString();
        myModel.put("now", now);
        return new ModelAndView("welcome", "model", myModel);
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        UserBean user = new UserBean();
        String loginName = request.getParameter("loginName");
        debug("request for new user with userName [" + loginName + "] ");
        user.setProfiles(profileService.getAllDbProfiles());
        return user;
    }

    protected ModelAndView processCancel(HttpServletRequest request, HttpServletResponse response, Object command, BindException bindException) throws Exception {
        Map<String, String> myModel = new HashMap<String, String>();
        String now = (new java.util.Date()).toString();
        myModel.put("now", now);
        return new ModelAndView("welcome", "model", myModel);
    }

    protected void validatePage(Object command, Errors errors, int page) {
        UserBean user = (UserBean) command;
        if (page == 0) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "loginName", "ccuser.username.required", "User Name is required");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "contactName1", "ccuser.contact1.required", "Contact Name 1 is required");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "ccuser.password.required", "Password is required");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "confirmPassword", "ccuser.confirmPassword.required", "Confirm Password is required");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "loginName", "ccuser.username.required", "User Name is required");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "ccuser.email.required", "E-mail is required");
            if (user.getEmail() != null && !user.getEmail().trim().equals("")) {
                if (!SystemUtil.isValideEmail(user.getEmail())) {
                    errors.rejectValue("email", "ccuser.email.invalid", "Format of the email address is invalid");
                }
            }
            if (user.getConfirmPassword() != null && user.getConfirmPassword().length() > 0) {
                if (!user.getPassword().equals(user.getConfirmPassword())) {
                    errors.rejectValue("password", "ccuser.password.requiredValidation", "Password and Confirm Password should be same");
                }
            }
            try {
                lifUserService.getUser(user.getLoginName());
                errors.rejectValue("loginName", "ccuser.user.exists", "User Name already exists");
            } catch (UserDoesNotExistException e) {
            } catch (DataAccessException e) {
                errors.reject("application.error");
            }
            if (errors.getErrorCount() > 0) {
                user.setPassword("");
                user.setConfirmPassword("");
            }
        }
        if (page == 1) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "profileName", "ccuser.profileName.required", "profile name is required");
        }
    }

    public void setLifUserService(LifUserService lifUserService) {
        this.lifUserService = lifUserService;
    }

    public void setProfileService(ProfileService profileService) {
        this.profileService = profileService;
    }

    private void debug(String str) {
        if (isDebugEnabled) {
            logger.debug("\n" + str + "\n");
        }
    }

    public void sendMailToAdmin(UserBean user) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", "cse.mrt.ac.lk");
            Session session1 = Session.getDefaultInstance(props, null);
            Message message = new MimeMessage(session1);
            message.setFrom(new InternetAddress("lifproject-admin@cse.mrt.ac.lk"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("niranjan.uom@gmail.com", false));
            message.setSubject("News: LIF User details");
            StringBuffer messageText = new StringBuffer();
            String lineBreak = "<br>";
            messageText.append(lineBreak).append(" User Details are :\n\n");
            messageText.append(lineBreak).append(" LoginName    : " + user.getLoginName());
            messageText.append(lineBreak).append(" Password     : " + user.getPassword());
            messageText.append(lineBreak).append(" Organization : " + user.getOrganization());
            messageText.append(lineBreak).append(" Telephone    : " + user.getTelephone1());
            messageText.append(lineBreak).append(" Email        : " + user.getEmail());
            messageText.append(lineBreak).append(" Contact Name : " + user.getContactName1());
            Set<SecurityRole> roles = user.getRoles();
            if (user.getRoles() != null) {
                messageText.append(lineBreak).append(" Requested Roles are: ");
                for (SecurityRole role : roles) {
                    messageText.append(role.name()).append(lineBreak);
                }
                messageText.append(lineBreak).append(" Requested profile     : " + user.getProfileName());
            }
            message.setContent(messageText.toString(), "text/html;charset=utf-8");
            Transport.send(message);
            System.out.println("message sent successfully");
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
