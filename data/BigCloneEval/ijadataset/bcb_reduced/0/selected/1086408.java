package magoffin.matt.ieat.web;

import java.lang.reflect.Constructor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import magoffin.matt.ieat.biz.DomainObjectFactory;
import magoffin.matt.ieat.biz.RecipeBiz;
import magoffin.matt.ieat.biz.RecipeSearchBiz;
import magoffin.matt.ieat.biz.UserBiz;
import magoffin.matt.util.StringUtil;
import magoffin.matt.xweb.util.MessagesSource;
import magoffin.matt.xweb.util.ServletRequestDataBinderTemplate;
import org.springframework.validation.BindException;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractWizardFormController;

/**
 * Abstract base class for wizard form controllers.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 28 $ $Date: 2009-05-03 21:19:45 -0400 (Sun, 03 May 2009) $
 */
public abstract class AbstractEatWizardForm extends AbstractWizardFormController {

    /**
	 * Parameter triggering the refresh action.
	 * Can be called from any wizard page!
	 */
    public static final String PARAM_REFRESH = "_refresh";

    /** The RecipeBiz. */
    protected RecipeBiz recipeBiz = null;

    /** The RecipeSearchBiz. */
    protected RecipeSearchBiz recipeSearchBiz = null;

    /** The UserBiz. */
    protected UserBiz userBiz = null;

    private String messagesSourceBeanName = "messageSource";

    private String recipeBizBeanName = "recipeBiz";

    private String recipeSearchBizBeanName = "recipeSearchBiz";

    private String userBizBeanName = "userBiz";

    private String domainObjectFactoryBeanName = "domainObjectFactory";

    private DomainObjectFactory domainObjectFactory = null;

    private MessagesSource messagesSource = null;

    private ServletRequestDataBinderTemplate binderTemplate = null;

    private String successView = null;

    private String cancelView = null;

    @Override
    protected void initApplicationContext() {
        super.initApplicationContext();
        if (recipeBiz == null) {
            recipeBiz = (RecipeBiz) getApplicationContext().getBean(recipeBizBeanName);
        }
        if (recipeSearchBiz == null) {
            recipeSearchBiz = (RecipeSearchBiz) getApplicationContext().getBean(recipeSearchBizBeanName);
        }
        if (userBiz == null) {
            userBiz = (UserBiz) getApplicationContext().getBean(userBizBeanName);
        }
        if (messagesSource == null) {
            messagesSource = (MessagesSource) getApplicationContext().getBean(messagesSourceBeanName);
        }
        if (domainObjectFactory == null) {
            domainObjectFactory = (DomainObjectFactory) getApplicationContext().getBean(domainObjectFactoryBeanName);
        }
        String cmdName = StringUtil.trimToNull(getCommandName());
        if (cmdName == null || cmdName.equals("command")) {
            setCommandName(WebConstants.DEFALUT_MODEL_OBJECT);
        }
    }

    /**
	 * Create a DataBinder object based on the <code>dataBinderClass</code> property.
	 * 
	 * <p>If the <code>dataBinderClass</code> property is set, this method will
	 * attempt to instantiate that class by calling a constructor with a method 
	 * signature of <code>ServletRequestDataBinder(Object,String,Map)</code>. 
	 * The Object and String passed into the constructor are the standard 
	 * command and command name objects normally passed to ServetRequestDataBinder
	 * implementations. The Map argument will be the <code>dataBinderInitializerMap</code>
	 * object configured in this controller instance.</p>
	 */
    @Override
    protected ServletRequestDataBinder createBinder(HttpServletRequest request, Object command) throws Exception {
        if (binderTemplate == null) {
            return super.createBinder(request, command);
        }
        Constructor<?> c = binderTemplate.getClass().getConstructor(new Class[] { Object.class, String.class, DataBinder.class });
        ServletRequestDataBinder binder = (ServletRequestDataBinder) c.newInstance(new Object[] { command, getCommandName(), binderTemplate });
        if (getMessageCodesResolver() != null) {
            binder.setMessageCodesResolver(getMessageCodesResolver());
        }
        initBinder(request, binder);
        return binder;
    }

    @Override
    protected ModelAndView processCancel(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        return new ModelAndView(getCancelView(), null);
    }

    /**
	 * Returns <em>true</em> if the request parameter {@link #PARAM_REFRESH}
	 * is set to the String <code>true</code>.
	 * 
	 * @param request the current request
	 * @return <em>true</em> if the request parameter {@link #PARAM_REFRESH}
	 * is set to the String <code>true</code>
	 */
    protected boolean isRefresh(HttpServletRequest request) {
        if (request.getParameter(PARAM_REFRESH) != null && "true".equalsIgnoreCase(request.getParameter(PARAM_REFRESH))) {
            return true;
        }
        return false;
    }

    /**
	 * @return the recipeBiz
	 */
    public RecipeBiz getRecipeBiz() {
        return recipeBiz;
    }

    /**
	 * @param recipeBiz the recipeBiz to set
	 */
    public void setRecipeBiz(RecipeBiz recipeBiz) {
        this.recipeBiz = recipeBiz;
    }

    /**
	 * @return the recipeSearchBiz
	 */
    public RecipeSearchBiz getRecipeSearchBiz() {
        return recipeSearchBiz;
    }

    /**
	 * @param recipeSearchBiz the recipeSearchBiz to set
	 */
    public void setRecipeSearchBiz(RecipeSearchBiz recipeSearchBiz) {
        this.recipeSearchBiz = recipeSearchBiz;
    }

    /**
	 * @return the userBiz
	 */
    public UserBiz getUserBiz() {
        return userBiz;
    }

    /**
	 * @param userBiz the userBiz to set
	 */
    public void setUserBiz(UserBiz userBiz) {
        this.userBiz = userBiz;
    }

    /**
	 * @return the messagesSourceBeanName
	 */
    public String getMessagesSourceBeanName() {
        return messagesSourceBeanName;
    }

    /**
	 * @param messagesSourceBeanName the messagesSourceBeanName to set
	 */
    public void setMessagesSourceBeanName(String messagesSourceBeanName) {
        this.messagesSourceBeanName = messagesSourceBeanName;
    }

    /**
	 * @return the recipeBizBeanName
	 */
    public String getRecipeBizBeanName() {
        return recipeBizBeanName;
    }

    /**
	 * @param recipeBizBeanName the recipeBizBeanName to set
	 */
    public void setRecipeBizBeanName(String recipeBizBeanName) {
        this.recipeBizBeanName = recipeBizBeanName;
    }

    /**
	 * @return the recipeSearchBizBeanName
	 */
    public String getRecipeSearchBizBeanName() {
        return recipeSearchBizBeanName;
    }

    /**
	 * @param recipeSearchBizBeanName the recipeSearchBizBeanName to set
	 */
    public void setRecipeSearchBizBeanName(String recipeSearchBizBeanName) {
        this.recipeSearchBizBeanName = recipeSearchBizBeanName;
    }

    /**
	 * @return the userBizBeanName
	 */
    public String getUserBizBeanName() {
        return userBizBeanName;
    }

    /**
	 * @param userBizBeanName the userBizBeanName to set
	 */
    public void setUserBizBeanName(String userBizBeanName) {
        this.userBizBeanName = userBizBeanName;
    }

    /**
	 * @return the domainObjectFactoryBeanName
	 */
    public String getDomainObjectFactoryBeanName() {
        return domainObjectFactoryBeanName;
    }

    /**
	 * @param domainObjectFactoryBeanName the domainObjectFactoryBeanName to set
	 */
    public void setDomainObjectFactoryBeanName(String domainObjectFactoryBeanName) {
        this.domainObjectFactoryBeanName = domainObjectFactoryBeanName;
    }

    /**
	 * @return the domainObjectFactory
	 */
    public DomainObjectFactory getDomainObjectFactory() {
        return domainObjectFactory;
    }

    /**
	 * @param domainObjectFactory the domainObjectFactory to set
	 */
    public void setDomainObjectFactory(DomainObjectFactory domainObjectFactory) {
        this.domainObjectFactory = domainObjectFactory;
    }

    /**
	 * @return the messagesSource
	 */
    public MessagesSource getMessagesSource() {
        return messagesSource;
    }

    /**
	 * @param messagesSource the messagesSource to set
	 */
    public void setMessagesSource(MessagesSource messagesSource) {
        this.messagesSource = messagesSource;
    }

    /**
	 * @return the binderTemplate
	 */
    public ServletRequestDataBinderTemplate getBinderTemplate() {
        return binderTemplate;
    }

    /**
	 * @param binderTemplate the binderTemplate to set
	 */
    public void setBinderTemplate(ServletRequestDataBinderTemplate binderTemplate) {
        this.binderTemplate = binderTemplate;
    }

    /**
	 * @return the successView
	 */
    public String getSuccessView() {
        return successView;
    }

    /**
	 * @param successView the successView to set
	 */
    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    /**
	 * @return the cancelView
	 */
    public String getCancelView() {
        return cancelView;
    }

    /**
	 * @param cancelView the cancelView to set
	 */
    public void setCancelView(String cancelView) {
        this.cancelView = cancelView;
    }
}
