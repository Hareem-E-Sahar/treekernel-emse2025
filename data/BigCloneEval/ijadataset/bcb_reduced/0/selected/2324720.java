package com.vgkk.hula.jsp.tags;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.TagSupport;
import org.mortbay.jetty.servlet.ServletHttpRequest;
import com.vgkk.hula.i18n.PropertiesLocalizer;
import com.vgkk.hula.jsp.controller.Controller;
import com.vgkk.hula.util.UrlUtil;

@SuppressWarnings("serial")
public class PageTag extends BodyTagSupport {

    private static Logger logger = Logger.getLogger("hula");

    private Class controllerClass;

    /**
     * Exception, caught during controller service, null if no exception is thrown.
     */
    private Exception caughtException = null;

    /**
     * Instatates a new instance of the specified controller, executes its
     * servie method and then adds the language specific resources
     * @see TagSupport#doStartTag
     * @author  (2006-Jan-31) Vladimir Dimchev CR: ??
     */
    public int doStartTag() throws JspException {
        caughtException = null;
        HttpSession session = pageContext.getSession();
        String contKey = controllerClass.getName();
        Controller controller = (Controller) session.getAttribute(contKey);
        if (controller == null) {
            controller = instantiateController();
            session.setAttribute(contKey, controller);
        }
        try {
            controller.initalizeService(pageContext);
            controller.service();
            Locale sessionLocale = controller.getLocale();
            Map localeMap = PropertiesLocalizer.getInstance().getProperties(sessionLocale);
            pageContext.setAttribute("Text", localeMap);
        } catch (Exception e) {
            caughtException = e;
        }
        return BodyTagSupport.EVAL_BODY_BUFFERED;
    }

    /**
     * Instatates the controller specified by the setController mehtod
     * @return the specified controller
     * @throws JspException if the contoller cannot be intantiated for any reason.
     * @author  (2004-Oct-24) Tim Romero CR: Velin Doychinov
     */
    private Controller instantiateController() throws JspException {
        try {
            Constructor constructor = controllerClass.getConstructor(new Class[0]);
            Object obj = constructor.newInstance(new Object[0]);
            return (Controller) obj;
        } catch (Exception e) {
            logger.severe("Controller intantiatioin error: " + e.getMessage());
            throw new JspException(e);
        }
    }

    /**
     * This method is executed right before the content of the response is returned. 
     * It appends hidden field to each form and GET parameter to the links to the same
     * resource, containing the page referrer. 
     * @return
     * @throws JspException
     * @see javax.servlet.jsp.tagext.IterationTag#doAfterBody()
     * @author  (2006-Jan-31) Vladimir Dimchev  CR: ??
     */
    public int doAfterBody() throws JspException {
        BodyContent bodyContent = getBodyContent();
        String body = bodyContent.getString();
        JspWriter out = bodyContent.getEnclosingWriter();
        try {
            if (caughtException != null) {
                out.print("<html>" + "<title>Hula Application Error</title>" + "<body>" + "Hula application error, caused by the following exception:<br><br>" + renderStacktrace(caughtException) + "</body></html>");
            } else {
                String requestingPage = ((ServletHttpRequest) pageContext.getRequest()).getRequestURL().toString();
                out.print(appendRequestingPage(body, requestingPage));
            }
        } catch (IOException e) {
            throw new JspException("Cannot write to JspWriter", e);
        }
        return BodyTagSupport.SKIP_BODY;
    }

    /**
	 * Renders in HTML format the stracktrace of the given throwable object.
	 * @param e the throwable
	 * @return the stacktrace as HTML
	 * @author  (2006-Jan-31) Vladimir Dimchev  CR: ??
	 */
    private String renderStacktrace(Throwable e) {
        StringBuilder buf = new StringBuilder();
        buf.append(e.getClass().getName() + ": " + e.getMessage() + "<br>");
        StackTraceElement[] trace = e.getStackTrace();
        for (StackTraceElement el : trace) {
            buf.append("&nbsp;&nbsp;&nbsp;&nbsp;" + el.toString() + "<br>");
        }
        if (e.getCause() != null) {
            buf.append("<br>");
            buf.append("Caused by: ");
            buf.append(renderStacktrace(e.getCause()));
        }
        return buf.toString();
    }

    /** 
	 * Appends hidden field to each form of the given content and additional GET parameter
	 * to each link to the same page, containing information about the page, which is
	 * making the request.
	 * @param content the content to process
	 * @param requestingPage the requesting page to add
	 * @return the modified content
	 * @author  (2005-Nov-09) Vladimir Dimchev  CR: ??
	 */
    static String appendRequestingPage(String content, String requestingPage) {
        requestingPage = UrlUtil.getPagePath(requestingPage);
        String pageReferrer = null;
        int index = requestingPage.lastIndexOf("/");
        if (index < requestingPage.length() - 1) {
            pageReferrer = requestingPage.substring(index + 1).trim().toLowerCase();
        }
        if (pageReferrer == null) {
            return content;
        }
        StringBuilder result = new StringBuilder();
        Pattern formPattern = Pattern.compile("\\s*</form>", Pattern.CASE_INSENSITIVE);
        Matcher matcher = formPattern.matcher(content);
        int lastMatchEnd = 0;
        int matchStart = 0;
        while (matcher.find()) {
            matchStart = matcher.start();
            if (matchStart > lastMatchEnd) {
                result.append(content.substring(lastMatchEnd, matchStart));
            }
            result.append("\r\n<input type=\"hidden\" name=\"hula_page_referrer\" value=\"" + requestingPage + "\">");
            lastMatchEnd = matcher.end();
            result.append(content.substring(matchStart, lastMatchEnd));
        }
        result.append(content.substring(lastMatchEnd));
        content = result.toString();
        result = new StringBuilder();
        Pattern linkPattern = Pattern.compile("<(a|area)\\s[\\s\\S]*?href[\\s\\r\\n]*?=[\\s\\r\\n]*?\"?([\\w\\./:#=\\?]+)\"?[\\s\\S]*?>", Pattern.CASE_INSENSITIVE);
        matcher = linkPattern.matcher(content);
        matchStart = 0;
        lastMatchEnd = 0;
        while (matcher.find()) {
            matchStart = matcher.start();
            if (matchStart > lastMatchEnd) {
                result.append(content.substring(lastMatchEnd, matchStart));
            }
            lastMatchEnd = matcher.end();
            String linkTag = matcher.group();
            String linkUrl = matcher.group(2).trim();
            String linkResource = "";
            String linkQuery = "";
            int queryIndex = linkUrl.lastIndexOf("?");
            if (queryIndex > 0 && queryIndex < linkUrl.length() - 1) {
                linkResource = linkUrl.substring(0, queryIndex);
                linkQuery = linkUrl.substring(queryIndex + 1);
            } else {
                linkResource = linkUrl;
                linkQuery = "";
            }
            if (linkResource.endsWith("/") || linkResource.toLowerCase().endsWith(pageReferrer)) {
                if (linkQuery.length() > 0) {
                    linkQuery = "&" + linkQuery;
                }
                linkQuery = "hula_page_referrer=" + requestingPage + linkQuery;
            }
            if (linkQuery.length() > 0) {
                linkResource = linkResource + "?" + linkQuery;
            }
            linkTag = linkTag.replace(linkUrl, linkResource);
            result.append(linkTag);
        }
        result.append(content.substring(lastMatchEnd));
        return result.toString();
    }

    /**
    * Instatiaes the controller class specified in the PageTag.
    * @param   className The name of the controller class
    * @author  (2004-Oct-20) Tim Romero CR: Velin Doychinov
    */
    public void setController(String className) throws ClassNotFoundException {
        controllerClass = Class.forName(className);
    }
}
