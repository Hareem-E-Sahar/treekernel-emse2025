package org.cofax.cds;

import org.cofax.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

/**
 *  EmailServlet.java provides e-mail functionality for cofax "aemail" template.
 *  Last Updated 10/03/2001 -- This code was entirly redone using java.mail
 *  instead of sun.net.smtp.SmtpClient. -- Sam Cohen
 *
 *@author     Sam Cohen
 *@created    May 7, 2002
 *@version    1.9.7
 */
public class EmailServlet extends HttpServlet {

    static String contextPath;

    static String configLocation;

    static String defaultMailHost;

    String errorMsg;

    /**
     *  Description of the Method
     *
     *@param  config                Description of the Parameter
     *@exception  ServletException  Description of the Exception
     */
    public final void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext context = getServletContext();
        contextPath = context.getRealPath("/");
        defaultMailHost = config.getInitParameter("mailHost");
    }

    /**
     *  Description of the Method
     *
     *@param  req                   Description of the Parameter
     *@param  res                   Description of the Parameter
     *@exception  ServletException  Description of the Exception
     *@exception  IOException       Description of the Exception
     */
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doPost(req, res);
    }

    /**
     *  Description of the Method
     *
     *@param  req                   Description of the Parameter
     *@param  res                   Description of the Parameter
     *@exception  ServletException  Description of the Exception
     *@exception  IOException       Description of the Exception
     */
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        PrintWriter out = res.getWriter();
        HashMap glossary = new HashMap();
        String hostName = "";
        try {
            this.errorMsg = "";
            hostName = defaultMailHost;
            res.setContentType("text/html");
            if (!getParameters(req, glossary)) {
                sendErrorMessage(req, out, glossary);
                return;
            }
            if (!sendMail(glossary, hostName)) {
                sendErrorMessage(req, out, glossary);
                return;
            } else {
                res.sendRedirect(CofaxUtil.getString(glossary, "redirectPath"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.errorMsg = this.errorMsg + e.toString();
            sendErrorMessage(req, out, glossary);
        }
    }

    /**
     *  Description of the Method
     *
     *@param  req       Description of the Parameter
     *@param  out       Description of the Parameter
     *@param  glossary  Description of the Parameter
     */
    private void sendErrorMessage(HttpServletRequest req, PrintWriter out, HashMap glossary) {
        StringBuffer page = new StringBuffer();
        String COFAX_VERSION = (String) getServletContext().getAttribute("COFAX_VERSION");
        page.append("<title>Cofax Error -- " + req.getRequestURI() + "</title>\n");
        page.append("<body bgcolor=FFFFFF>\n");
        page.append("<img src=http://www.cofax.org/images/cofax.gif>\n");
        page.append(COFAX_VERSION + "<br>\n");
        page.append("Cofax has encountered and error while processing your request.<br><br>\n");
        page.append("<dd><font color=FF2121>Error Message:</font> " + this.errorMsg + "<br>\n");
        page.append("<dd>Request: " + req.getRequestURI() + "<br>\n");
        page.append("Please try one of the following:<ul>\n");
        page.append("<li><a href=javascript:window.location.reload()>reload this page</a></li>\n");
        page.append("<li><a href=javascript:window.history.back()>return to your previous page</a></li>\n");
        page.append("<li><a href=" + req.getContextPath() + "/" + CofaxUtil.getString(glossary, "request:pubName") + ">");
        page.append("go to the home for this publication</a></li>\n");
        page.append("<li><a href=mailto:info@cofax.org>contact Cofax</a></li></ul>\n");
        out.println(page.toString());
    }

    /**
     *  Gets the parameters attribute of the EmailServlet object
     *
     *@param  req       Description of the Parameter
     *@param  glossary  Description of the Parameter
     *@return           The parameters value
     */
    private boolean getParameters(HttpServletRequest req, HashMap glossary) {
        String to = req.getParameter("email");
        String from = req.getParameter("from_email");
        String subject = req.getParameter("subject");
        if (from == null || to == null || from.equals("") || to.equals("")) {
            this.errorMsg = "To and From can not be blank";
            return false;
        }
        if (from.indexOf("@") == -1 || to.indexOf("@") == -1) {
            this.errorMsg = "Both To and From must be valid email addresses!(@)";
            return false;
        }
        if (from.indexOf(".") == -1 || to.indexOf(".") == -1) {
            this.errorMsg = "Both To and From must be valid email addresses!(.)";
            return false;
        }
        glossary.put("msgSubject", subject);
        glossary.put("msgFrom", from);
        glossary.put("msgTo", to);
        glossary.put("msgPromotion", req.getParameter("promoMSG"));
        String redirectPath = req.getParameter("path");
        glossary.put("redirectPath", redirectPath + "");
        String msgBody = req.getParameter("message") + "";
        if (msgBody != null && redirectPath != null && msgBody.indexOf(redirectPath) > -1) {
            String linkPath = "<a href=\"" + redirectPath + "\">" + redirectPath + "</a>";
            msgBody = CofaxUtil.replace(msgBody, redirectPath, linkPath);
        }
        glossary.put("msgBody", msgBody);
        return true;
    }

    /**
     *  Description of the Method
     *
     *@param  glossary  Description of the Parameter
     *@param  hostName  Description of the Parameter
     *@return           Description of the Return Value
     */
    private boolean sendMail(HashMap glossary, String hostName) {
        try {
            String from = CofaxUtil.getString(glossary, "msgFrom");
            String to = CofaxUtil.getString(glossary, "msgTo");
            String subject = CofaxUtil.getString(glossary, "msgSubject");
            StringBuffer text = new StringBuffer();
            Session smtpSession;
            Properties props = new Properties();
            props.put("mail.smtp.host", hostName);
            smtpSession = Session.getDefaultInstance(props, null);
            text.append("This Story has been sent to you by : " + from);
            text.append("<pre>\n");
            text.append(CofaxUtil.getString(glossary, "msgBody"));
            text.append("\n");
            text.append("</pre><br>\n");
            text.append("<p>");
            text.append(CofaxUtil.getString(glossary, "msgPromotion"));
            text.append("\n");
            MimeMessage message = new MimeMessage(smtpSession);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setContent(text.toString(), "text/html");
            Transport.send(message);
        } catch (Exception e) {
            this.errorMsg = "An error occured while trying to send your message!";
            this.errorMsg = this.errorMsg + " Please be sure the to and from addresses are valid.";
            return false;
        }
        return true;
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public boolean smtpConnect() {
        try {
        } catch (Exception e) {
            this.errorMsg = "Could not connect to system defined SMTP server!";
            return false;
        }
        return true;
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public boolean smtpDisConnect() {
        try {
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
